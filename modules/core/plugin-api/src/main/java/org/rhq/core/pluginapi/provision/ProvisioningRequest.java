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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.rhq.core.domain.configuration.Configuration;

/**
 * @author Lukas Krejci
 */
public final class ProvisioningRequest {
    @NotNull
    private final File deploymentSourceFile;

    @NotNull
    private final String mimetype;

    @Nullable
    private final String destination;

    @Nullable
    private final URI destinationUri;

    @Nullable
    private final Configuration deploymentConfiguration;

    public ProvisioningRequest(@NotNull File deploymentSourceFile, @NotNull String mimetype,
        @Nullable String destination, @Nullable URI destinationUri,
        @Nullable Configuration deploymentConfiguration) {

        if (destination == null && destinationUri == null) {
            throw new IllegalArgumentException("Exactly one of 'destination' and 'destinationUri' must not be null, but both are.");
        }
        if (destination != null && destinationUri != null) {
            throw new IllegalArgumentException("Exactly one of 'destination' and 'destinationUri' must not be null, but none is.");
        }

        this.deploymentSourceFile = deploymentSourceFile;
        this.mimetype = mimetype;
        this.destination = destination;
        this.destinationUri = destinationUri;
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
     * This is the name of the destination the deployment should go to.
     * This property is not null for the <code>deployable</code> elements in the plugin descriptor
     * which are handled by the plugin that manages the target resource.
     *
     * @return the name of the destination on the target resource.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * This is the URI to provision the deployment to. This property is not null when a generic
     * provisioning plugin handles the provisioning of a deployment to a <code>destination</code> defined
     * by some other plugin.
     *
     * @return the URI of the destination
     */
    public URI getDestinationUri() {
        return destinationUri;
    }

    @Override
    public String toString() {
        return "ProvisioningRequest[" +
            "deploymentSourceFile=" + deploymentSourceFile +
            ", mimetype='" + mimetype + '\'' +
            ", destination='" + destination + "'" +
            ", destinationUri='" + destinationUri + "'" +
            ", deploymentConfiguration=" + deploymentConfiguration +
            ']';
    }
}
