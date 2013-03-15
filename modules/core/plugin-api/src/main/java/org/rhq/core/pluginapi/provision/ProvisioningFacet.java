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

/**
 * Resource components implementing this interface can provision files. The deployment of the files can have various
 * effects that the resource component reports to the caller.
 *
 * @author Lukas Krejci
 */
public interface ProvisioningFacet {

    /**
     * Performs the deployment according to the request. If possible this method should not return before the deployment
     * has actually been "realized" on the managed resource.
     * <p>I.e. when deploying a WAR file, the implementation should check for and wait until the underlying application
     * server has processed and finished the deployment.
     *
     * @param request the details of the deployment to perform
     * @return a description of the deployment that occurred
     */
    ProvisionedDeployment provision(ProvisioningRequest request);

    /**
     * Removes the deployment on this resource defined by the given key. This is meant to go out to the managed resource
     * or file system and remove the deployment.
     *
     * @param deploymentKey the key identifying the deployment on this resource
     * @return the removed deployment as it existed before it was unprovisioned
     */
    ProvisionedDeployment unprovision(Deployment.Key deploymentKey);
}
