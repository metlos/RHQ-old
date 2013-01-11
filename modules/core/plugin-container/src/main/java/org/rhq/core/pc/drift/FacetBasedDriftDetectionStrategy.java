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
import java.io.InputStream;
import java.util.Iterator;

import org.rhq.core.pluginapi.drift.DriftDetectionFacet;
import org.rhq.core.pluginapi.drift.FileInfo;
import org.rhq.core.pluginapi.drift.FileStatus;

/**
 * @author Lukas Krejci
 *
 */
public class FacetBasedDriftDetectionStrategy extends AbstractDriftDetectionStrategy {

    private final DriftDetectionFacet facet;

    public FacetBasedDriftDetectionStrategy(DriftDetectionFacet facet, DriftClient driftClient,
        ChangeSetManager changeSetManager) {
        super(driftClient, changeSetManager);
        this.facet = facet;
    }

    @Override
    protected boolean isBaseDirValid(String baseDir) {
        return true;
    }

    @Override
    protected FileStatus getFileStatus(String basedir, String path) throws IOException {
        return facet.getFileStatus(basedir, path);
    }

    @Override
    protected Iterator<FileInfo> allFilesIterator(String basedir) throws IOException {
        return facet.getAllFiles(basedir).iterator();
    }

    @Override
    protected String sha256(String basedir, String filePath) throws IOException {
        InputStream stream = facet.openStream(basedir, filePath);

        try {
            return sha256(stream);
        } finally {
            stream.close();
        }
    }
}
