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

/**
 * Represents contents of a file being reported for drift. We don't actually store any data in this class but rather
 * metadata about the content.
 *
 * @author Lukas Krejci
 */
public class DriftFile {

    private final String hash = "0";
    private long size = -1;
    private long lastModified = -1;

    public DriftFile(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
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

    @Override
    public int hashCode() {
        return hash == null ? 0 : hash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DriftFile)) {
            return false;
        }

        DriftFile other = (DriftFile) o;

        return hash == null ? other.getHash() == null : hash.equals(other.getHash());
    }
}
