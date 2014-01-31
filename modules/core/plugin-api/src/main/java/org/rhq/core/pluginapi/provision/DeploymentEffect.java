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
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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

    private static final long serialVersionUID = 1L;

    private static abstract class ResourceEffect implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(Object other) {
            return other != null && getClass() == other.getClass();
        }

        /**
         * This effectively means that we cannot have more than 1 instance a type of resource effect in a set or map.
         */
        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static abstract class ConfigurationModifiedEffect extends ResourceEffect {
        private static final long serialVersionUID = 1L;
        private final Configuration configuration;

        protected ConfigurationModifiedEffect(Configuration configuration) {
            this.configuration = configuration;
        }

        public Configuration getConfiguration() {
            return configuration;
        }
    }

    /**
     * Describes the effect of a new resource being added as a result of a deployment.
     */
    public static final class ResourceAdded extends ResourceEffect {
        private static final long serialVersionUID = 1L;
        private final ResourceBacking backing;

        public ResourceAdded(ResourceBacking backing) {
            this.backing = backing;
        }

        public ResourceBacking getBacking() {
            return backing;
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other) || (other != null && other instanceof ResourceRemoved);
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Describes the effect of a resource being removed as a result of a deployment.
     */
    public static final class ResourceRemoved extends ResourceEffect {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(Object other) {
            return super.equals(other) || (other != null && other instanceof ResourceAdded);
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Describes the effect of the backing content of an existing resource being modified as a result of a deployment.
     */
    public static final class ContentModified extends ResourceEffect {
        private static final long serialVersionUID = 1L;
        private final ResourceBacking backing;

        public ContentModified(ResourceBacking backing) {
            this.backing = backing;
        }

        public ResourceBacking getBacking() {
            return backing;
        }
    }

    /**
     * Describes the effect of a plugin configuration of an existing resource being modified as a result of a
     * deployment.
     */
    public static final class PluginConfigurationModified extends ConfigurationModifiedEffect {
        private static final long serialVersionUID = 1L;

        public PluginConfigurationModified(Configuration configuration) {
            super(configuration);
        }
    }

    /**
     * Describes the effect of a resource configuration of an existing resource being modified as a result of a
     * deployment.
     */
    public static final class ResourceConfigurationModified extends ConfigurationModifiedEffect {
        private static final long serialVersionUID = 1L;

        public ResourceConfigurationModified(Configuration configuration) {
            super(configuration);
        }
    }

    /**
     * A simple builder that can be used to initialize the deployment effect.
     */
    public static final class Builder {
        private final DeploymentEffect effect;

        public Builder(DeploymentEffect effect) {
            this.effect = effect;
        }

        public OnResourceBuilder onResource(ResourceCoordinates resource) {
            return new OnResourceBuilder(effect, resource);
        }

        public DeploymentEffect build() {
            return effect;
        }
    }

    public static final class OnResourceBuilder {
        private final DeploymentEffect effect;
        private final ResourceCoordinates coords;

        public OnResourceBuilder(DeploymentEffect effect, ResourceCoordinates coords) {
            this.effect = effect;
            this.coords = coords;
        }

        public EffectsBuilder files(URI... files) {
            return new EffectsBuilder(effect, coords, new HashSet<URI>(Arrays.asList(files)));
        }
    }

    public static final class EffectsBuilder {
        private final DeploymentEffect effect;
        private final ResourceCoordinates coords;
        private final Set<URI> files;

        public EffectsBuilder(DeploymentEffect effect, ResourceCoordinates coords, Set<URI> files) {
            this.effect = effect;
            this.coords = coords;
            this.files = files;
        }

        public Builder haveEffects(ResourceEffect... effects) {
            Map<Set<URI>, Set<ResourceEffect>> re = effect.getEffects().get(coords);
            if (re == null) {
                re = new HashMap<Set<URI>, Set<ResourceEffect>>(1);
                effect.getEffects().put(coords, re);
            }

            Set<ResourceEffect> r = re.get(files);
            if (r == null) {
                r = new HashSet<ResourceEffect>(effects.length);
                Collections.addAll(r, effects);
                re.put(files, r);
            }

            return new Builder(effect);
        }
    }

    private Map<ResourceCoordinates, Map<Set<URI>, Set<ResourceEffect>>> effects;

    /**
     * Consider using the builder ({@link #builder()} to initialize the instance instead of using a bare constructor.
     */
    public DeploymentEffect() {
        effects = new HashMap<ResourceCoordinates, Map<Set<URI>, Set<ResourceEffect>>>();
    }

    public static Builder builder() {
        return new Builder(new DeploymentEffect());
    }

    /**
     * Keyed by paths to individual resources (down the hierarchy from the resource doing the deployment discovery or
     * analysis), the values describe the effect of groups of files on the keyed resources.
     * <p/>
     * Multiple sets of files can have different effects on a single resource.
     * <p/>
     * The effect on the resource is described by an instance of:
     * <ul>
     * <li>{@link org.rhq.core.pluginapi.provision.DeploymentEffect.ResourceAdded}</li>
     * <li>{@link org.rhq.core.pluginapi.provision.DeploymentEffect.ResourceRemoved}</li>
     * <li>{@link org.rhq.core.pluginapi.provision.DeploymentEffect.ContentModified}</li>
     * <li>{@link org.rhq.core.pluginapi.provision.DeploymentEffect.PluginConfigurationModified}</li>
     * <li>{@link org.rhq.core.pluginapi.provision.DeploymentEffect.ResourceConfigurationModified}</li>
     * </ul>
     * The set of effects a set of files can have can contain at most 1 instance of each of the types above.
     * Additionally, at most one of {@code ResourceAdded} or {@code ResourceRemoved} can be present in the set at the
     * same time.
     * <p/>
     * Note that the returned instance is mutable.
     * <p/>
     * Consider using the {@link #builder() builder} when initializing the effects.
     *
     * @return the map of effects a deployment has on the inventory
     */
    public Map<ResourceCoordinates, Map<Set<URI>, Set<ResourceEffect>>> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        return "DeploymentEffect[" +
            "effects=" + effects +
            ']';
    }
}
