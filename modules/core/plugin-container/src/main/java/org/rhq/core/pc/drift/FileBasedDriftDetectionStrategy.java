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

import static org.rhq.common.drift.FileEntry.addedFileEntry;
import static org.rhq.core.util.file.FileUtil.forEachFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rhq.common.drift.ChangeSetWriter;
import org.rhq.common.drift.FileEntry;
import org.rhq.core.domain.drift.DriftDefinition;
import org.rhq.core.domain.drift.Filter;
import org.rhq.core.pluginapi.drift.DriftFileStatus;
import org.rhq.core.util.MessageDigestGenerator;
import org.rhq.core.util.file.FileUtil;
import org.rhq.core.util.file.FileVisitor;

/**
 * Drift detection using direct file system scanning.
 *
 * @author John Sanda
 */
public class FileBasedDriftDetectionStrategy extends AbstractDriftDetectionStrategy {

    private MessageDigestGenerator digestGenerator = new MessageDigestGenerator(MessageDigestGenerator.SHA_256);

    public FileBasedDriftDetectionStrategy(DriftClient driftClient, ChangeSetManager changeSetMgr) {
        super(driftClient, changeSetMgr);
    }

    protected void writeSnapshot(final DriftDetectionSchedule schedule,
        final String basedir, ChangeSetWriter writer) throws IOException {
        File b = new File(basedir);

        if (b.isDirectory()) {
            doDirectoryScan(schedule, schedule.getDriftDefinition(), b, writer);
            writer.close();
        }
    }

    @Override
    protected boolean isBaseDirValid(String baseDir) {
        File f = new File(baseDir);

        return f.exists() && f.isDirectory();
    }

    @Override
    protected Set<FileEntry> scanBaseDir(String basedir, DriftDefinition driftDef) throws IOException {
        // get a Set of all files in the detection, consider them initially new files, and we'll knock the
        // list down as we go.  As we build up FileEntries in memory this Set will shrink.  It's marginally
        // less memory than if we had both in memory at the same time.
        Set<FileEntry> newFiles = null;

        final File bd = new File(basedir);

        // If the basedir is still valid we need to do a directory tree scan to look for newly added files
        if (bd.isDirectory()) {
            newFiles = new HashSet<FileEntry>(1000);
            final Set<FileEntry> nf = newFiles;
            List<Filter> includes = driftDef.getIncludes();
            List<Filter> excludes = driftDef.getExcludes();

            for (File dir : getScanDirectories(bd, includes)) {
                forEachFile(dir, new FilterFileVisitor(bd, includes, excludes, new FileVisitor() {
                    @Override
                    public void visit(File file) {
                        if (file.canRead()) {
                            try {
                                nf.add(FileEntry.addedFileEntry(FileUtil.getRelativePath(file, bd), sha256(file), file.lastModified(), file.length()));
                            } catch (IOException e) {
                                log.warn("Failed to compute SHA256 of file " + file.getAbsolutePath());
                                //and continue, we don't want to break the whole scan
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Skipping " + file.getPath() + " as new file since it is not readable.");
                        }
                    }
                }));
            }
        } else {
            newFiles = Collections.emptySet();
        }

        return newFiles;
    }

    @Override
    protected DriftFileStatus getFileStatus(DriftDefinition definition, String basedir, String path) throws IOException {
        File f = new File(basedir, path);

        String sha = sha256(f);

        DriftFileStatus ret = new DriftFileStatus(sha);
        ret.setExisting(f.exists());
        ret.setReadable(f.canRead());
        ret.setLastModified(f.lastModified());
        ret.setSize(f.length());

        return ret;
    }

    private static Set<File> getScanDirectories(final File basedir, List<Filter> includes) {

        Set<File> directories = new HashSet<File>();

        if (null == includes || includes.isEmpty()) {
            directories.add(basedir);
        } else {
            for (Filter filter : includes) {
                String path = filter.getPath();
                if (".".equals(path)) {
                    directories.add(basedir);
                } else {
                    directories.add(new File(basedir, path));
                }
            }
        }

        return directories;
    }

    private String sha256(File file) throws IOException {
        return digestGenerator.calcDigestString(file);
    }

    /**
     * File.canRead() is basically a security check and does not guarantee that the file contents can truly be read.
     * Certain files, like socket files on linux, can not be processed and it's not known until actually trying to
     * construct a FileInputStream, as is done when we actually try to generate the digest. These files will generate
     * a FileNotFoundException. This method will catch, log and suppress that issue, and return null
     * indicating the file is not suitable for drift detection.
     *
     * @param basedir the drift def base directory
     * @param file the new file to add
     * @return the new FileEntry, or null if this file is not appropriate for drift detection (typically if the
     * underlying file does not support the needed File operations.
     * @throws Will throw unexpected IOExceptions, outside of the FileNotFoundException it looks for.
     */
    private FileEntry getAddedFileEntry(File basedir, File file) throws IOException {
        FileEntry result = null;

        try {
            String sha256 = sha256(file);
            String relativePath = relativePath(basedir, file);
            long lastModified = file.lastModified();
            long length = file.length();

            result = addedFileEntry(relativePath, sha256, lastModified, length);

        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping " + file.getPath() + " since it is missing or is not a physically readable file.");
            }
        }

        return result;
    }

    private static String relativePath(File basedir, File file) {
        //TODO replace this with FileUtil.getRelativePath() ?
        if (basedir.equals(file)) {
            return ".";
        }
        String filePath = file.getAbsolutePath();
        String basedirPath = basedir.getAbsolutePath();
        int basedirLen = basedirPath.length();
        if (!basedirPath.endsWith(File.separator)) {
            ++basedirLen;
        }
        return filePath.substring(basedirLen);
    }

    private void doDirectoryScan(final DriftDetectionSchedule schedule, DriftDefinition driftDef, final File basedir,
        final ChangeSetWriter writer) {

        List<Filter> includes = driftDef.getIncludes();
        List<Filter> excludes = driftDef.getExcludes();

        for (File dir : getScanDirectories(basedir, includes)) {
            forEachFile(dir, new FilterFileVisitor(basedir, includes, excludes, new FileVisitor() {
                @Override
                public void visit(File file) {
                    try {
                        if (!file.canRead()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping " + file.getPath() + " since we do not have read access.");
                            }
                            return;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Adding " + file.getPath() + " to coverage change set for " + schedule);
                        }

                        FileEntry addedFileEntry = getAddedFileEntry(basedir, file);
                        if (null != addedFileEntry) {
                            writer.write(addedFileEntry);
                        }

                    } catch (Throwable t) {
                        // report the error but keep going, perhaps it is specific to a single file, try to
                        // finish the detection.
                        log.error("An unexpected error occurred while generating a coverage change set for file "
                            + file.getPath() + " in schedule " + schedule + ". Skipping file.", t);
                    }
                }
            }));
        }
    }
}
