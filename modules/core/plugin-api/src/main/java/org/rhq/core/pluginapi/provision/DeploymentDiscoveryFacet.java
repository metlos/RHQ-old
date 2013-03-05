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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * Resource components implementing this interface can inform about the deployments present inside them. These
 * deployments can be mapped to either the resources (represented by the resource compontents) themselves or to any
 * number of child resources. <p> I.e. it is not necessary for each child resource component to implement this interface
 * but it is not disallowed either.
 *
 * @author Lukas Krejci
 */
public interface DeploymentDiscoveryFacet {

    /**
     * @return the deployments present on this resource or any of the child resources
     */
    Set<DiscoveredDeployment> discoverDeployments();

    /**
     * This method is only called for deployments with deployment type {@link Deployment.Type#API API}.
     *
     * <p>If the resource component also implements the {@link DeployerFacet}, the returned data must be deployable
     * again using the {@link DeployerFacet#deploy(DeploymentRequest)} method.
     *
     * @param deploymentKey the key of the deployment to download
     * @return the stream with the deployment contents or null if such operation is not supported
     * @throws IOException if the downloading is supported but failed for some reason
     */
    OutputStream download(Deployment.Key deploymentKey) throws IOException;
}
