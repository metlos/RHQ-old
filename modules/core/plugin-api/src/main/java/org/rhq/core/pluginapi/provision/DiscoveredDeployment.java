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

    private final DeploymentEffect deploymentEffect;

    /**
     * @param type the type of the deployment, usually this will be {@link Type#API}.
     * @param deploymentKey the key that identifies the deployment on the resource
     * @param deploymentConfiguration the configuration of the discovered deployment
     * @param deployedFiles the set of files the deployment consists of
     * @param deploymentEffect the resources affected by this deployment
     */
    public DiscoveredDeployment(Type type, Key deploymentKey, Configuration deploymentConfiguration,
                                Set<File> deployedFiles, DeploymentEffect deploymentEffect) {
        super(type, deploymentKey, deploymentConfiguration, deployedFiles);

        this.deploymentEffect = deploymentEffect;
    }

    public DeploymentEffect getDeploymentEffect() {
        return deploymentEffect;
    }

    @Override
    public String toString() {
        return "DiscoveredDeployment[" +
            "type=" + getType() +
            ", deploymentKey=" + getKey() +
            ", deploymentConfiguration=" + getDeploymentConfiguration() +
            ", deployedFiles=" + getDeployedFiles() +
            ", affectedResources=" + deploymentEffect +
            ']';
    }
}
