/*
 * RHQ Management Platform
 * Copyright (C) 2014 Red Hat, Inc.
 * All rights reserved.
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

package org.rhq.core.pluginapi.provision;

import org.rhq.core.domain.configuration.Configuration;

/**
 * Describes the effect of provisioning a deployment on a resource.
 *
 * @author Lukas Krejci
 */
public final class DeploymentResourceEffect {

    private final DeploymentResourceEffectType effect;
    private final ResourceBacking backing;
    private final ResourceCoordinates resource;
    private final Configuration pluginConfiguration;
    private final Configuration resourceConfiguration;

    public static DeploymentResourceEffect createAddedEffect(ResourceCoordinates resource, ResourceBacking backing) {
        return new DeploymentResourceEffect(resource, DeploymentResourceEffectType.ADDED, backing, null, null);
    }

    public static DeploymentResourceEffect createRemovedEffect(ResourceCoordinates resource) {
        return new DeploymentResourceEffect(resource, DeploymentResourceEffectType.REMOVED, null, null, null);
    }

    public static DeploymentResourceEffect createContentModifiedEffect(ResourceCoordinates resource, ResourceBacking backing) {
        return new DeploymentResourceEffect(resource, DeploymentResourceEffectType.CONTENT_MODIFIED, backing, null, null);
    }

    public static DeploymentResourceEffect createPluginConfigurationChangeEffect(ResourceCoordinates resource, Configuration pluginConfiguration) {
        return new DeploymentResourceEffect(resource, DeploymentResourceEffectType.PLUGIN_CONFIGURATION_CHANGE, null, pluginConfiguration, null);
    }

    public static DeploymentResourceEffect createResourceConfigurationChangeEffect(ResourceCoordinates resource, Configuration resourceConfiguration) {
        return new DeploymentResourceEffect(resource, DeploymentResourceEffectType.RESOURCE_CONFIGURATION_CHANGE, null, null, resourceConfiguration);
    }

    /**
     * Use the provided static methods if possible.
     *
     * @param resource the affected resource
     * @param effect the effect on the resource
     * @param backing is the resource fully backed by the deployment?
     * @param pluginConfiguration the plugin configuration as it should look like after the deployment
     * @param resourceConfiguration the resource configuration once it should look like after the deployment
     */
    public DeploymentResourceEffect(ResourceCoordinates resource, DeploymentResourceEffectType effect,
        ResourceBacking backing,
        Configuration pluginConfiguration, Configuration resourceConfiguration) {
        this.effect = effect;
        this.resource = resource;
        this.backing = backing;
        this.pluginConfiguration = pluginConfiguration;
        this.resourceConfiguration = resourceConfiguration;
    }

    public DeploymentResourceEffectType getEffectType() {
        return effect;
    }

    public ResourceBacking getBacking() {
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
        if (!(o instanceof DeploymentResourceEffect)) return false;

        DeploymentResourceEffect that = (DeploymentResourceEffect) o;

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
