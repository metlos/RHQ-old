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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;

/**
 * Desribes the effects a (un)provisioning of a deployment has on the inventory.
 *
 * @author Lukas Krejci
 */
public final class DeploymentEffect implements Serializable {

    /**
     * Desribes the effect of provisioning a deployment on a resource.
     */
    public final static class OnResource {

        /**
         * Describes what kind effect on a particular resource a deployment had
         */
        public enum Effect {
            ADDED, REMOVED, CONTENT_MODIFIED, PLUGIN_CONFIGURATION_CHANGE, RESOURCE_CONFIGURATION_CHANGE
        }

        /**
         * Is the resource backed by the deployment fully or just partially?
         */
        public enum Backing {
            FULL, PARTIAL
        }

        private final Effect effect;
        private final Backing backing;
        private final ResourceCoordinates resource;
        private final Configuration pluginConfiguration;
        private final Configuration resourceConfiguration;

        public static OnResource createAddedEffect(ResourceCoordinates resource, Backing backing) {
            return new OnResource(resource, Effect.ADDED, backing, null, null);
        }

        public static OnResource createRemovedEffect(ResourceCoordinates resource) {
            return new OnResource(resource, Effect.REMOVED, null, null, null);
        }

        public static OnResource createContentModifiedEffect(ResourceCoordinates resource, Backing backing) {
            return new OnResource(resource, Effect.CONTENT_MODIFIED, backing, null, null);
        }

        public static OnResource createPluginConfigurationChangeEffect(ResourceCoordinates resource, Configuration pluginConfiguration) {
            return new OnResource(resource, Effect.PLUGIN_CONFIGURATION_CHANGE, null, pluginConfiguration, null);
        }

        public static OnResource createResourceConfigurationChangeEffect(ResourceCoordinates resource, Configuration resourceConfiguration) {
            return new OnResource(resource, Effect.RESOURCE_CONFIGURATION_CHANGE, null, null, resourceConfiguration);
        }

        /**
         * Use the provided static methods if possible.
         *
         * @param resourceKey the affected resource key
         * @param effect the effect on the resource
         * @param backing is the resource fully backed by the deployment?
         * @param pluginConfiguration the plugin configuration as it should look like after the deployment
         * @param resourceConfiguration the resource configuration once it should look like after the deployment
         */
        public OnResource(ResourceCoordinates resource, Effect effect, Backing backing,
            Configuration pluginConfiguration, Configuration resourceConfiguration) {
            this.effect = effect;
            this.resource = resource;
            this.backing = backing;
            this.pluginConfiguration = pluginConfiguration;
            this.resourceConfiguration = resourceConfiguration;
        }

        public Effect getEffect() {
            return effect;
        }

        public Backing getBacking() {
            return backing;
        }

        public ResourceCoordinates getResourceCoordinates() {
            return resource;
        }

        public Configuration getPluginConfiguration() {
            return pluginConfiguration;
        }

        public Configuration getResourceConfiguration() {
            return resourceConfiguration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OnResource)) return false;

            OnResource that = (OnResource) o;

            if (!resource.equals(that.resource)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return resource.hashCode();
        }

        @Override
        public String toString() {
            return "ResourceEffect[" +
                "effect=" + effect +
                ", backing=" + backing +
                ", resourceCoordinates='" + resource + "'" +
                ", pluginConfiguraiton=" + pluginConfiguration +
                ", resourceConfiguration=" + resourceConfiguration +
                ']';
        }
    }

    /**
     * Representation of a set of files. A simple extension of {@link HashSet}.
     */
    public static final class FileSet extends HashSet<File> {

        public FileSet(File... files) {
            this(Arrays.asList(files));
        }

        public FileSet(Collection<File> files) {
            super(files);
        }
    }

    private Map<FileSet, Set<OnResource>> effects;

    public DeploymentEffect() {
        effects = new HashMap<FileSet, Set<OnResource>>();
    }

    /**
     * The keys in the returned map are {@link FileSet file sets}, each grouping the files in the deployment
     * into groups affecting a single resource. The values then represent the effect on each resource.
     * <p>
     * Note that there is no requirement on the file sets to be unique - they can overlap (i.e. a single file
     * may be present in multiple file sets).
     * </p>
     *
     * @return the map of effects a deployment has on the inventory
     */
    public Map<FileSet, Set<OnResource>> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        return "DeploymentEffect[" +
            "effects=" + effects +
            ']';
    }
}
