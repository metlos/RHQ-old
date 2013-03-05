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
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;

/**
 * @author Lukas Krejci
 */
public final class DiscoveredDeployment extends AbstractDeployment {

    public static class AffectedResource {
        public enum Backing {
            FULL, PARTIAL
        }

        private final ResourceCoordinates resourceCoordinates;
        private final Backing backing;

        public AffectedResource(ResourceCoordinates resourceCoordinates,
                                Backing backing) {
            this.resourceCoordinates = resourceCoordinates;
            this.backing = backing;
        }

        public ResourceCoordinates getResourceCoordinates() {
            return resourceCoordinates;
        }

        public Backing getBacking() {
            return backing;
        }
    }

    private Set<AffectedResource> affectedResources;

    /**
     * @param type
     * @param deploymentKey the key that identifies the deployment on the resource
     * @param deploymentConfiguration the configuration of the discovered deployment
     * @param deployedFiles the set of files the deployment consists of
     * @param affectedResources the resources affected by this deployment
     */
    public DiscoveredDeployment(Type type, Key deploymentKey, Configuration deploymentConfiguration,
                                Set<File> deployedFiles, Set<AffectedResource> affectedResources) {
        super(type, deploymentKey, deploymentConfiguration, deployedFiles);

        if (affectedResources == null || affectedResources.isEmpty()) {
            throw new IllegalArgumentException("affectedResources can't be null and must have at least 1 item.");
        }

        this.affectedResources = affectedResources;
    }

    public Set<AffectedResource> getAffectedResources() {
        return affectedResources;
    }

    @Override
    public String toString() {
        return "DiscoveredDeployment[" +
            "type=" + getType() +
            ", deploymentKey=" + getKey() +
            ", deploymentConfiguration=" + getDeploymentConfiguration() +
            ", deployedFiles=" + getDeployedFiles() +
            ", affectedResources=" + affectedResources +
            ']';
    }
}
