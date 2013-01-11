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

package org.rhq.core.pluginapi.drift;

import java.io.File;

import org.rhq.core.util.file.FileUtil;

/**
 * @author Lukas Krejci
 *
 */
public class FileInfo {

    private final String path;
    private long size;
    private long lastModified;
    private boolean directory;

    public static FileInfo fromRelativeFile(File f) {
        return fromFile(f, false);
    }

    public static FileInfo fromAbsoluteFile(File f) {
        return fromFile(f, true);
    }

    public static FileInfo fromFile(File f, boolean useAbsolutePath) {
        FileInfo ret = new FileInfo(FileUtil.useForwardSlash(useAbsolutePath ? f.getAbsolutePath() : f.getPath()));
        ret.setLastModified(f.lastModified());
        ret.setSize(f.length());
        ret.setDirectory(f.isDirectory());

        return ret;
    }

    public FileInfo(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileInfo)) {
            return false;
        }

        FileInfo other = (FileInfo) o;

        return path.equals(other.path);
    }

    @Override
    public String toString() {
        return path;
    }
}
