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

import org.rhq.core.domain.drift.DriftCategory;

/**
 * Represents a plugin generated drift of a single file.
 *
 * @author Lukas Krejci
 */
public class Drift {

    private final String path;

    private DriftFile oldFile;

    private DriftFile newFile;

    private DriftCategory category;

    public Drift(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public DriftFile getOldFile() {
        return oldFile;
    }

    public void setOldFile(DriftFile oldFile) {
        this.oldFile = oldFile;
    }

    public DriftFile getNewFile() {
        return newFile;
    }

    public void setNewFile(DriftFile newFile) {
        this.newFile = newFile;
    }

    public DriftCategory getCategory() {
        return category;
    }

    public void setCategory(DriftCategory category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        int pathHash = path == null ? 0 : path.hashCode();

        return 31 * pathHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Drift)) {
            return false;
        }

        Drift other = (Drift) o;

        return path == null ? other.getPath() == null : path.equals(other.getPath());
    }
}
