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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.rhq.common.drift.ChangeSetWriter;
import org.rhq.common.drift.FileEntry;
import org.rhq.core.domain.drift.DriftDefinition;
import org.rhq.core.pluginapi.drift.Drift;
import org.rhq.core.pluginapi.drift.DriftDetectionFacet;
import org.rhq.core.pluginapi.drift.DriftFile;
import org.rhq.core.pluginapi.drift.DriftFileStatus;

/**
 * @author Lukas Krejci
 *
 */
public class FacetBasedDriftDetectionStrategy extends AbstractDriftDetectionStrategy {

    private final DriftDetectionFacet facet;

    public FacetBasedDriftDetectionStrategy(DriftDetectionFacet facet, DriftClient driftClient, ChangeSetManager changeSetManager) {
        super(driftClient, changeSetManager);
        this.facet = facet;
    }

    @Override
    protected boolean isBaseDirValid(String baseDir) {
        return true;
    }

    @Override
    protected void writeSnapshot(DriftDetectionSchedule schedule, String basedir,
        ChangeSetWriter writer) throws IOException {

        Set<Drift> drifts = facet.generateShapshot(schedule.getDriftDefinition());

        for(Drift drift : drifts) {
            DriftFile f = drift.getNewFile();
            FileEntry entry = FileEntry.addedFileEntry(drift.getPath(), f.getHash(), f.getLastModified(), f.getSize());
            writer.write(entry);
        }
    }

    @Override
    protected DriftFileStatus getFileStatus(DriftDefinition definition, String basedir, String path) throws IOException {
        return facet.getFileStatus(definition, path);
    }

    @Override
    protected Set<FileEntry> scanBaseDir(String basedir, DriftDefinition driftDef) throws IOException {
        Set<Drift> snapshot = facet.generateShapshot(driftDef);

        Set<FileEntry> ret = new HashSet<FileEntry>();
        for(Drift d : snapshot) {
            FileEntry entry = FileEntry.addedFileEntry(d.getPath(), d.getNewFile().getHash(), d.getNewFile().getLastModified(), d.getNewFile().getSize());
            ret.add(entry);
        }

        return ret;
    }
}
