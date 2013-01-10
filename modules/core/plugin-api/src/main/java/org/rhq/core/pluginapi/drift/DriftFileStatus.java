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
 * @author Lukas Krejci
 *
 */
public class DriftFileStatus extends DriftFile {
    private boolean exists;
    private boolean readable;

    public DriftFileStatus(String hash) {
        super(hash);
    }

    public DriftFileStatus(String hash, boolean exists, boolean readable, long lastModified) {
        super(hash);
        this.exists = exists;
        this.readable = readable;
    }

    public boolean isExisting() {
        return exists;
    }

    public void setExisting(boolean exists) {
        this.exists = exists;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }
}
