/*
 * RHQ Management Platform
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.rhq.core.pc.drift;

import static org.rhq.common.drift.FileEntry.changedFileEntry;
import static org.rhq.common.drift.FileEntry.removedFileEntry;
import static org.rhq.core.domain.drift.DriftChangeSetCategory.COVERAGE;
import static org.rhq.core.domain.drift.DriftChangeSetCategory.DRIFT;
import static org.rhq.core.util.file.FileUtil.copyFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.rhq.common.drift.ChangeSetReader;
import org.rhq.common.drift.ChangeSetWriter;
import org.rhq.common.drift.FileEntry;
import org.rhq.common.drift.Headers;
import org.rhq.core.domain.drift.DriftChangeSetCategory;
import org.rhq.core.domain.drift.DriftDefinition;
import org.rhq.core.pluginapi.drift.DriftFileStatus;

/**
 * @author Lukas Krejci
 */
public abstract class AbstractDriftDetectionStrategy implements DriftDetectionStrategy {

    protected final Log log = LogFactory.getLog(this.getClass());

    protected final DriftClient driftClient;
    protected final ChangeSetManager changeSetMgr;

    protected AbstractDriftDetectionStrategy(DriftClient driftClient, ChangeSetManager changeSetMgr) {
        this.driftClient = driftClient;
        this.changeSetMgr = changeSetMgr;
    }

    protected String basedir(int resourceId, DriftDefinition driftDef) {
        return driftClient.getAbsoluteBaseDirectory(resourceId, driftDef).getAbsolutePath();
    }

    protected Headers createHeaders(DriftDetectionSchedule schedule, DriftChangeSetCategory type, int version) {
        Headers headers = new Headers();
        headers.setResourceId(schedule.getResourceId());
        headers.setDriftDefinitionId(schedule.getDriftDefinition().getId());
        headers.setDriftDefinitionName(schedule.getDriftDefinition().getName());
        headers.setBasedir(basedir(schedule.getResourceId(), schedule.getDriftDefinition()));
        headers.setType(type);
        headers.setVersion(version);

        return headers;
    }

    protected abstract boolean isBaseDirValid(String baseDir);

    protected abstract void writeSnapshot(final DriftDetectionSchedule schedule, final String basedir,
        ChangeSetWriter writer) throws IOException;

    @Override
    public void generateSnapshot(DriftDetectionSummary summary) throws IOException {
        final DriftDetectionSchedule schedule = summary.getSchedule();
        final DriftDefinition driftDef = schedule.getDriftDefinition();
        final String basedir = basedir(schedule.getResourceId(), driftDef);

        if (!isBaseDirValid(basedir)) {
            if (log.isWarnEnabled()) {
                log.warn("The base directory ["
                    + basedir
                    + "] for "
                    + schedule
                    + " does not "
                    + "exist. You may want review the drift definition and verify that the value of the base "
                    + "directory is in fact correct.");
            }
            summary.setBaseDirExists(false);
            return;
        }

        log.debug("Generating coverage change set for " + schedule);

        File snapshot = changeSetMgr.findChangeSet(schedule.getResourceId(), schedule.getDriftDefinition().getName(),
            COVERAGE);

        ChangeSetWriter writer = null;
        try {
            writer = changeSetMgr.getChangeSetWriter(snapshot, createHeaders(schedule, COVERAGE, 0));

            writeSnapshot(schedule, basedir, writer);

            if (schedule.getDriftDefinition().isPinned()) {
                copyFile(snapshot, new File(snapshot.getParentFile(), DriftDetector.FILE_SNAPSHOT_PINNED));
            }
            summary.setNewSnapshot(snapshot);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    protected abstract Set<FileEntry> scanBaseDir(String basedir, DriftDefinition driftDef) throws IOException;

    @Override
    public void generateDriftChangeSet(DriftDetectionSummary summary) throws IOException {
        final DriftDetectionSchedule schedule = summary.getSchedule();

        log.debug("Generating drift change set for " + schedule);

        boolean isPinned = schedule.getDriftDefinition().isPinned();
        String basedir = basedir(schedule.getResourceId(), schedule.getDriftDefinition());

        File currentFullSnapshot = changeSetMgr.findChangeSet(schedule.getResourceId(), schedule.getDriftDefinition()
            .getName(), COVERAGE);

        // unless pinned use the current full snapshot file for the definition, otherwise use the pinned snapshot
        File snapshotFile = isPinned ? new File(currentFullSnapshot.getParentFile(), DriftDetector.FILE_SNAPSHOT_PINNED)
            : currentFullSnapshot;

        // get a Set of all files in the detection, consider them initially new files, and we'll knock the
        // list down as we go.  As we build up FileEntries in memory this Set will shrink.  It's marginally
        // less memory than if we had both in memory at the same time.
        Set<FileEntry> newFiles = scanBaseDir(basedir, schedule.getDriftDefinition());

        final List<FileEntry> unchangedEntries = new LinkedList<FileEntry>();
        final List<FileEntry> changedEntries = new LinkedList<FileEntry>();
        final List<FileEntry> removedEntries = new LinkedList<FileEntry>();
        final List<FileEntry> addedEntries = new LinkedList<FileEntry>();
        // for pinned snapshots, keep track of the original FileEntry objects for changed entries. These
        // are used if we re-write the pinned snapshot file.
        final List<FileEntry> changedPinnedEntries = isPinned ? new LinkedList<FileEntry>() : null;

        try {
            ChangeSetReader snapshotReader = null;
            int newVersion;
            boolean updateSnapshot = false;
            try {
                snapshotReader = changeSetMgr.getChangeSetReader(snapshotFile);

                if (!isBaseDirValid(basedir)) {
                    log.warn("The base directory [" + basedir + "] for " + schedule
                        + " does not exist.");
                }

                if (isPinned) {
                    // If pinned we compare against the pinned snapshot but we need to know the current snapshot version,
                    // get it from the current full snapshot.
                    ChangeSetReader currentFullSnapshotReader = null;
                    try {
                        currentFullSnapshotReader = changeSetMgr.getChangeSetReader(currentFullSnapshot);
                        newVersion = currentFullSnapshotReader.getHeaders().getVersion() + 1;
                    } finally {
                        if (null != currentFullSnapshotReader) {
                            currentFullSnapshotReader.close();
                        }
                    }
                } else {
                    newVersion = snapshotReader.getHeaders().getVersion() + 1;
                }

                // First look for files that have either been changed or removed
                updateSnapshot = scanSnapshot(schedule, basedir, snapshotReader, newFiles, unchangedEntries,
                    changedEntries, removedEntries, changedPinnedEntries);

            } finally {
                if (null != snapshotReader) {
                    snapshotReader.close();
                }
            }

            // if necessary, re-write the pinned snapshot file because we've updated timestamp/filesize info, which
            // on subsequent detection runs will help us avoid SHA generation. It must maintain the same entries.
            if (isPinned && updateSnapshot) {
                changedPinnedEntries.addAll(unchangedEntries);

                backupAndDeleteCurrentSnapshot(snapshotFile);
                updatePinnedSnapshot(schedule, snapshotFile, changedPinnedEntries);
            }

            // add new files to the snapshotEntries and deltaEntries
            for (FileEntry file : newFiles) {
                try {
                    if (log.isInfoEnabled()) {
                        log.info("Detected added file for " + schedule + " --> " + new File(basedir, file.getFile()).getAbsolutePath());
                    }

                    addedEntries.add(file);
                } catch (Throwable t) {
                    // report the error but keep going, perhaps it is specific to a single file, try to
                    // finish the change set generation.
                    log.error(
                        "An unexpected error occurred while generating a drift change set for file " + file.getFile()
                            + " in schedule " + schedule + ". Skipping file.", t);
                }
            }

            // The new snapshot contains all changed, unchanged and added files. Not removed files.
            final List<FileEntry> snapshotEntries = new LinkedList<FileEntry>(unchangedEntries);
            snapshotEntries.addAll(changedEntries);
            snapshotEntries.addAll(addedEntries);

            // The snapshot delta contains all changed, added and removed files.
            final List<FileEntry> deltaEntries = new LinkedList<FileEntry>(changedEntries);
            deltaEntries.addAll(removedEntries);
            deltaEntries.addAll(addedEntries);

            if (deltaEntries.isEmpty()) {
                File newSnapshot = currentFullSnapshot;

                if (!isPinned) {
                    // If unpinned and there is no detected drift then we generally don't need to add/update any files.
                    // But, if we have timestamp/filesize updates then we want to replace the current snapshot with
                    // the updated entries, so we can avoid SHA generation on subsequent runs.
                    if (updateSnapshot) {
                        currentFullSnapshot.delete();
                        newSnapshot = updateCurrentSnapshot(schedule, snapshotEntries, newVersion - 1);
                    }
                } else {
                    // If pinned and returning to compliance (meaning no drift now but the previous snapshot did have drift)
                    // then we need to reset the current snapshot to match the pinned snapshot. Note though that we
                    // increment the snapshot version in order to let the server know about the state change.
                    if (newVersion > 1
                        && !isPreviousChangeSetEmpty(schedule.getResourceId(), schedule.getDriftDefinition())) {
                        currentFullSnapshot.delete();
                        newSnapshot = updateCurrentSnapshot(schedule, snapshotEntries, newVersion);

                        updateDeltaSnapshot(summary, schedule, deltaEntries, newVersion, currentFullSnapshot,
                            newSnapshot);
                    }
                }

                summary.setNewSnapshot(newSnapshot);

            } else {
                // if there is drift, but we're pinned and the drift is the same as the previous detection, just
                // mark it as a repeat to indicate that we're out of compliance but not in any new way.
                if (isPinned && newVersion > 1 && isSameAsPreviousChangeSet(deltaEntries, currentFullSnapshot)) {
                    summary.setVersion(newVersion - 1);
                    summary.setRepeat(true);

                    return;
                }

                // otherwise, generate a new current snapshot, and a snapshot delta reflecting the latest drift
                File oldSnapshot = backupAndDeleteCurrentSnapshot(currentFullSnapshot);
                File newSnapshot = updateCurrentSnapshot(schedule, snapshotEntries, newVersion);

                updateDeltaSnapshot(summary, schedule, deltaEntries, newVersion, oldSnapshot, newSnapshot);
            }
        } finally {
            // Help out the garbage collector by clearing all of our collections
            safeClear(newFiles, unchangedEntries, changedEntries, changedPinnedEntries);
        }
    }

    protected abstract DriftFileStatus getFileStatus(DriftDefinition definition, String basedir, String path) throws IOException;

    /**
     * Process the entries for the snapshotReader. Each entry will be placed in one of the various Lists depending
     * on what bucket it fall into.
     * @return true if unchangedEntries (meaning no drift) had timestamp/filesize info updated, in which case the
     * snapshot should be re-written to disk even if there was no drift.
     * @throws IOException
     */
    private boolean scanSnapshot(DriftDetectionSchedule schedule, String basedir, ChangeSetReader snapshotReader,
        Set<FileEntry> newFiles, List<FileEntry> unchangedEntries, List<FileEntry> changedEntries,
        List<FileEntry> removedEntries, List<FileEntry> changedPinnedEntries) throws IOException {

        DriftDefinition driftDefinition = schedule.getDriftDefinition();

        boolean result = false;

        for (FileEntry entry : snapshotReader) {
            newFiles.remove(entry);

            DriftFileStatus fileStatus = getFileStatus(driftDefinition, basedir, entry.getFile());

            if (!(fileStatus.isExisting() && fileStatus.isReadable())) {
                // The file has been deleted or is no longer readable, since the last scan
                if (log.isDebugEnabled()) {
                    log.debug("Detected " + (fileStatus.isExisting() ? "unreadable" : "deleted") + " file for " + schedule
                        + " --> " + new File(basedir, entry.getFile()));
                }
                removedEntries.add(removedFileEntry(entry.getFile(), entry.getNewSHA()));

                if (null != changedPinnedEntries) {
                    changedPinnedEntries.add(entry);
                }

                continue;

            } else {
                String currentSHA = null;
                boolean isChanged = false;

                // perform a SHA comparison if we are unable to compare size and lastModified or if the
                // size or lastModified test fails.  We may not have size or lastModified values for the
                // entry when the current snapshot was provided by the server, either due to a synch or
                // pinning scenario.  The server does not store that information and will provide -1 for defaults.
                if (entry.getLastModified() == -1 || entry.getSize() == -1
                    || entry.getLastModified() != fileStatus.getLastModified() || entry.getSize() != fileStatus.getSize()) {

                    currentSHA = fileStatus.getHash();
                    isChanged = !entry.getNewSHA().equals(currentSHA);
                }

                if (isChanged) {
                    FileEntry changedEntry = changedFileEntry(entry.getFile(), entry.getNewSHA(), currentSHA,
                        fileStatus.getLastModified(), fileStatus.getSize());
                    changedEntries.add(changedEntry);

                    if (null != changedPinnedEntries) {
                        changedPinnedEntries.add(entry);
                    }

                } else {
                    if (-1 == entry.getLastModified()) {
                        entry.setLastModified(fileStatus.getLastModified());
                        result = true;
                    }
                    if (-1 == entry.getSize()) {
                        entry.setSize(fileStatus.getSize());
                        result = true;
                    }
                    unchangedEntries.add(entry);
                }
            }
        }

        return result;
    }

    private File backupAndDeleteCurrentSnapshot(File currentSnapshot) throws IOException {
        File oldSnapshot = new File(currentSnapshot.getParentFile(), currentSnapshot.getName() + ".previous");
        copyFile(currentSnapshot, oldSnapshot);
        currentSnapshot.delete();
        return oldSnapshot;
    }

    private File updatePinnedSnapshot(DriftDetectionSchedule schedule, File pinnedSnapshot,
        List<FileEntry> snapshotEntries) throws IOException {

        ChangeSetWriter newSnapshotWriter = null;

        try {
            Headers snapshotHeaders = createHeaders(schedule, COVERAGE, 0);
            newSnapshotWriter = changeSetMgr.getChangeSetWriter(pinnedSnapshot, snapshotHeaders);

            for (FileEntry entry : snapshotEntries) {
                newSnapshotWriter.write(entry);
            }

            return pinnedSnapshot;

        } finally {
            if (null != newSnapshotWriter) {
                newSnapshotWriter.close();
            }
        }
    }

    private File updateCurrentSnapshot(DriftDetectionSchedule schedule, List<FileEntry> snapshotEntries, int newVersion)
        throws IOException {

        ChangeSetWriter newSnapshotWriter = null;

        try {
            Headers snapshotHeaders = createHeaders(schedule, COVERAGE, newVersion);
            File newSnapshot = changeSetMgr.findChangeSet(schedule.getResourceId(), schedule.getDriftDefinition()
                .getName(), COVERAGE);
            newSnapshotWriter = changeSetMgr.getChangeSetWriter(schedule.getResourceId(), snapshotHeaders);

            for (FileEntry entry : snapshotEntries) {
                newSnapshotWriter.write(entry);
            }
            return newSnapshot;
        } finally {
            if (null != newSnapshotWriter) {
                newSnapshotWriter.close();
            }
        }
    }

    private boolean isPreviousChangeSetEmpty(int resourceId, DriftDefinition definition) throws IOException {
        File changeSet = changeSetMgr.findChangeSet(resourceId, definition.getName(), DRIFT);
        if (!changeSet.exists()) {
            return true;
        }
        ChangeSetReader reader = null;
        boolean isEmpty;
        try {
            reader = changeSetMgr.getChangeSetReader(changeSet);
            isEmpty = true;

            for (FileEntry entry : reader) {
                isEmpty = false;
                break;
            }

            //XXX how about isEmpty = !reader.iterator().hasNext();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return isEmpty;
    }

    private void updateDeltaSnapshot(DriftDetectionSummary summary, DriftDetectionSchedule schedule,
        List<FileEntry> deltaEntries, int newVersion, File oldSnapshot, File newSnapshot) throws IOException {

        ChangeSetWriter deltaWriter = null;

        try {
            Headers deltaHeaders = createHeaders(schedule, DRIFT, newVersion);
            File driftChangeSet = changeSetMgr.findChangeSet(schedule.getResourceId(), schedule.getDriftDefinition()
                .getName(), DRIFT);
            deltaWriter = changeSetMgr.getChangeSetWriter(driftChangeSet, deltaHeaders);

            summary.setDriftChangeSet(driftChangeSet);
            summary.setNewSnapshot(newSnapshot);
            summary.setOldSnapshot(oldSnapshot);

            for (FileEntry entry : deltaEntries) {
                deltaWriter.write(entry);
            }
        } finally {
            if (deltaWriter != null) {
                deltaWriter.close();
            }
        }
    }

    private boolean isSameAsPreviousChangeSet(List<FileEntry> entries, File currentSnapsotFile) throws IOException {
        HashMap<String, FileEntry> entriesMap = new HashMap<String, FileEntry>();
        for (FileEntry e : entries) {
            entriesMap.put(e.getFile(), e);
        }

        ChangeSetReader reader = null;
        try {
            File deltaChangeSet = new File(currentSnapsotFile.getParentFile(), DriftDetector.FILE_CHANGESET_DELTA);
            reader = changeSetMgr.getChangeSetReader(deltaChangeSet);

            int numEntries = 0;
            for (FileEntry entry : reader) {
                FileEntry newEntry = entriesMap.get(entry.getFile());
                if (newEntry == null) {
                    return false;
                }
                if (entry.getType() != newEntry.getType()) {
                    return false;
                }
                switch (entry.getType()) {
                case FILE_ADDED:
                    if (!entry.getNewSHA().equals(newEntry.getNewSHA())) {
                        return false;
                    }
                case FILE_CHANGED:
                    if (!entry.getNewSHA().equals(newEntry.getNewSHA())
                        || !entry.getOldSHA().equals(newEntry.getOldSHA())) {
                        return false;
                    }
                default: // FILE_REMOVED
                    if (!entry.getOldSHA().equals(newEntry.getOldSHA())) {
                        return false;
                    }
                }
                numEntries++;
            }

            return numEntries == entriesMap.size();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    static private void safeClear(Collection<?>... collections) {
        if (null == collections) {
            return;
        }
        for (Collection<?> c : collections) {
            if (null != c) {
                c.clear();
            }
        }
    }
}