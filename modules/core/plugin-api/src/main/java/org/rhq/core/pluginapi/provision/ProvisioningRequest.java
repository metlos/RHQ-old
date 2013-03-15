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

import org.rhq.core.domain.configuration.Configuration;

/**
 * @author Lukas Krejci
 */
public final class ProvisioningRequest {
    private final File deploymentSourceFile;
    private final String mimetype;
    private final String destination;
    private final Configuration deploymentConfiguration;

    public ProvisioningRequest(File deploymentSourceFile, String mimetype, String destination,
                               Configuration deploymentConfiguration) {
        this.deploymentSourceFile = deploymentSourceFile;
        this.mimetype = mimetype;
        this.destination = destination;
        this.deploymentConfiguration = deploymentConfiguration;
    }

    /**
     * @return the file downloaded from the RHQ server that should be deployed
     */
    public File getDeploymentSourceFile() {
        return deploymentSourceFile;
    }

    /**
     * @return the mimetype of the file as identified by the RHQ server
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @return the configuration for the deployment (if any) as specified by the user
     */
    public Configuration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    /**
     * @return the name of the destination on the target resource. The deployer might or might not need this value.
     */
    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "ProvisioningRequest[" +
            "deploymentSourceFile=" + deploymentSourceFile +
            ", mimetype='" + mimetype + '\'' +
            ", destination='" + destination + "'" +
            ", deploymentConfiguration=" + deploymentConfiguration +
            ']';
    }
}
