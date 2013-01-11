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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.rhq.core.pluginapi.drift.FileInfo;
import org.rhq.core.pluginapi.drift.FileStatus;
import org.rhq.core.util.file.FileUtil;

/**
 * Drift detection using direct file system scanning.
 *
 * @author John Sanda
 */
public class FileBasedDriftDetectionStrategy extends AbstractDriftDetectionStrategy {

    public FileBasedDriftDetectionStrategy(DriftClient driftClient, ChangeSetManager changeSetMgr) {
        super(driftClient, changeSetMgr);
    }

    @Override
    protected boolean isBaseDirValid(String baseDir) {
        File f = new File(baseDir);

        return f.exists() && f.isDirectory();
    }

    @Override
    protected Iterator<FileInfo> allFilesIterator(final String basedir) throws IOException {
        final File bd = new File(basedir);

        // If the basedir is still valid we need to do a directory tree scan to look for newly added files
        if (isBaseDirValid(basedir)) {
            return new Iterator<FileInfo>() {
                Deque<File> stack = new LinkedList<File>();
                {
                    stack.push(bd);
                }

                List<File> nextFiles = new ArrayList<File>();

                @Override
                public boolean hasNext() {
                    prepareNext();
                    return !nextFiles.isEmpty();
                }

                @Override
                public FileInfo next() {
                    prepareNext();

                    if (nextFiles.isEmpty()) {
                        throw new NoSuchElementException();
                    }

                    File next = nextFiles.remove(0);

                    if (next.isDirectory()) {
                        stack.push(next);
                    }

                    return FileInfo.fromRelativeFile(new File(FileUtil.getRelativePath(next, bd)));
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private void prepareNext() {
                    while (nextFiles.isEmpty()) {
                        File root = stack.poll();
                        if (root != null) {
                            stack.pop();
                        } else {
                            break;
                        }

                        File[] children = root.listFiles();
                        if (children != null) {
                            nextFiles = new ArrayList<File>(Arrays.asList(children));

                            //check readability
                            for(Iterator<File> it = nextFiles.iterator(); it.hasNext();) {
                                if (!it.next().canRead()) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            };
        } else {
            return Collections.<FileInfo>emptySet().iterator();
        }
    }

    @Override
    protected FileStatus getFileStatus(String basedir, String path) throws IOException {
        File f = new File(basedir, path);

        FileStatus ret = new FileStatus(path); //we always want relative paths
        ret.setExisting(f.exists());
        ret.setReadable(f.canRead());
        ret.setLastModified(f.lastModified());
        ret.setSize(f.length());

        return ret;
    }

    @Override
    protected String sha256(String basedir, String filePath) throws IOException {
        return sha256(new File(basedir, filePath));
    }

    private String sha256(File file) throws IOException {
        return digestGenerator.calcDigestString(file);
    }
}
