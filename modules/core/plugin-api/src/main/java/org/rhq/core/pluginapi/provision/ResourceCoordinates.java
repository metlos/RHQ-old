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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rhq.core.domain.resource.Resource;

/**
 * Represents a path to a child resource.
 * <p/>
 * Use the static factory methods ({@link #builder(String, String)} and {@link
 * #from(org.rhq.core.domain.resource.Resource)} to create instances of this class.
 *
 * @author Lukas Krejci
 */
public final class ResourceCoordinates implements Iterable<ResourceCoordinates>, Serializable {

    public static final class Builder {
        private List<ResourceCoordinates> path = new ArrayList<ResourceCoordinates>();

        public Builder(String resourceKey, String resourceTypeName) {
            path.add(new ResourceCoordinates(resourceKey, resourceTypeName, path));
        }

        public Builder addParent(String resourceKey, String resourceTypeName) {
            path.add(new ResourceCoordinates(resourceKey, resourceTypeName, path));
            return this;
        }

        public ResourceCoordinates create() {
            return path.get(0);
        }
    }

    //this list is shared among all the members in it
    private final List<ResourceCoordinates> path;

    //the position of this instance in the path
    private final int position;

    //the resource identification
    private final String resourceKey;
    private final String resourceTypeName;

    /**
     * Returns a new builder of the resource coordinates initialized with the resource key and type name for the
     * leaf of the coordinates.
     *
     * @param resourceKey      the resource key of the leaf of the coordinates
     * @param resourceTypeName the type name of the leaf of the coordinates
     *
     * @return the builder to construct the full coordinates of the leaf
     */
    public static Builder builder(String resourceKey, String resourceTypeName) {
        return new Builder(resourceKey, resourceTypeName);
    }

    /**
     * Constructs the resource coordinates by examining the resource and its parent resources.
     * The leaf of the return coordinates corresponds to the supplied resource.
     *
     * @param resource the resource to construct the coordinates of
     *
     * @return the coordinates of the resource
     */
    public static ResourceCoordinates from(Resource resource) {
        Builder bld = builder(resource.getResourceKey(), resource.getResourceType().getName());

        resource = resource.getParentResource();
        while (resource != null) {
            bld.addParent(resource.getResourceKey(), resource.getResourceType().getName());
            resource = resource.getParentResource();
        }

        return bld.create();
    }


    private static void check(String resourceKey, String resourceTypeName) {
        if (resourceKey == null) {
            throw new IllegalArgumentException("resourceKey can't be null");
        }

        if (resourceTypeName == null) {
            throw new IllegalArgumentException("resourceTypeName can't be null");
        }
    }

    private ResourceCoordinates(String resourceKey, String resourceTypeName, List<ResourceCoordinates> path) {
        check(resourceKey, resourceTypeName);

        this.resourceKey = resourceKey;
        this.resourceTypeName = resourceTypeName;
        this.path = path;
        this.position = path.size();
        path.add(this);
    }

    @Override
    public Iterator<ResourceCoordinates> iterator() {
        return new Iterator<ResourceCoordinates>() {
            private Iterator<ResourceCoordinates> it = path.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ResourceCoordinates next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @return true if this instance is the leaf of the coordinates
     */
    public boolean isLeaf() {
        return position == 0;
    }

    /**
     * @return how far in the ancestry chain is the current instance from the leaf of the coordinates
     */
    public int getDistanceFromLeaf() {
        return position;
    }

    /**
     * @return true if this instance is the root of the coordinates
     */
    public boolean isRoot() {
        return position == path.size() - 1;
    }

    public String getResourceKey() {
        return path.get(position).resourceKey;
    }

    public String getResourceTypeName() {
        return path.get(position).resourceTypeName;
    }

    /**
     * @return an instance describing the parent of the current member in the coordinates or null if the current
     * instance {@link #isRoot()}.
     */
    public ResourceCoordinates ascend() {
        if (position < path.size() - 1) {
            return path.get(position + 1);
        } else {
            return null;
        }
    }

    /**
     * @return an instance describing the child of the current member in the coordinates or null if the current instance
     * {@link #isLeaf()}.
     */
    public ResourceCoordinates descend() {
        return position > 0 ? path.get(position - 1) : null;
    }

    /**
     * @return the leaf of the coordinates - this can be used to "go back" after examining the parents
     */
    public ResourceCoordinates getLeaf() {
        return path.get(0);
    }

    private boolean memberWiseEquals(ResourceCoordinates other) {
        for (int i = 0; i < path.size(); ++i) {
            ResourceCoordinates myMember = path.get(i);
            ResourceCoordinates otherMember = other.path.get(i);

            if (!dataEquals(myMember, otherMember)) {
                return false;
            }
        }

        return true;
    }

    private static boolean dataEquals(ResourceCoordinates a, ResourceCoordinates b) {
        if (a.position != b.position) {
            return false;
        }
        if (!a.resourceKey.equals(b.resourceKey)) {
            return false;
        }
        if (!a.resourceTypeName.equals(b.resourceTypeName)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceCoordinates)) {
            return false;
        }

        ResourceCoordinates that = (ResourceCoordinates) o;

        if (!dataEquals(this, that)) {
            return false;
        }
        if (path.size() != that.path.size()) {
            return false;
        }

        return memberWiseEquals(that);
    }

    @Override
    public int hashCode() {
        int result = position;
        result = 31 * result + resourceKey.hashCode();
        result = 31 * result + resourceTypeName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder("ResourceCoordinates[");
        for (int i = 0; i < path.size(); ++i) {
            ResourceCoordinates rc = path.get(i);
            bld.append(i == position ? "{{" : "(");
            bld.append(rc.resourceKey).append(", ").append(rc.resourceTypeName);
            bld.append(i == position ? "}}" : ")");
        }

        bld.append("]");

        return bld.toString();
    }
}
