/*
 * RHQ Management Platform
 * Copyright (C) 2013 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.rhq.core.pluginapi.provision;

import java.io.File;
import java.net.URI;
import java.util.Set;

/**
 * A representation of a deployment. A deployment can either be filesystem-based or can have been made using an API
 * call. A deployment is identifiable using its key and consists of files of some sort.
 * <p/>
 * Notice that the files might or might not correspond to some physical files in some kind of storage somewhere.
 * They can merely be logical names identifying "parts" of the deployment.
 *
 * @author Lukas Krejci
 */
public interface Deployment {

    /**
     * Possible types of deployments
     */
    public enum Type {
        /**
         * A file system deployment is laid down on the local filesystem.
         */
        FILESYSTEM,

        /**
         * An API deployment is deployed using some API call and cannot be directly accessed using the file system.
         * The plugins implementing {@link DeploymentDiscoveryFacet} can provide access to the data of the "files" in
         * such deployment.
         */
        API
    }

    /**
     * A key to uniquely identify a deployment. Each deployment is deployed or discovered by certain plugin, represented
     * in this key by the {@link #getOwnerType() owner type}. The owner type then has some logic to assign unique ids
     * to individual deployments ({@link #getId()}).
     */
    public final class Key {

        private final String id;
        private final Class<?> ownerType;

        public Key(String id, Class<?> ownerType) {
            if (id == null) {
                throw new IllegalArgumentException("name can't be null.");
            }

            if (ownerType == null) {
                throw new IllegalArgumentException("ownerType can't be null");
            }

            this.id = id;
            this.ownerType = ownerType;
        }

        public String getId() {
            return id;
        }

        public Class<?> getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key that = (Key) o;

            if (!id.equals(that.id)) return false;
            if (!ownerType.equals(that.ownerType)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + ownerType.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Deployment.Key[" +
                "name='" + id + '\'' +
                ", ownerType=" + ownerType +
                ']';
        }
    }

    /**
     * @return the type of the deployment
     */
    Type getType();

    /**
     * @return the key uniquely identifying the deployment as understood by the owner type
     */
    Key getKey();

    /**
     * @return the files in the deployment, depending on {@link #getType() type}, this can be either actual files or
     *         some logical representation of them.
     */
    Set<URI> getDeployedFiles();

}
