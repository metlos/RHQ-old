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

import java.net.URI;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.util.exception.ThrowableUtil;

/**
 * A provisioned deployment describes the result of a provisioning operation.
 *
 * @author Lukas Krejci
 */
public final class ProvisionedDeployment extends AbstractDeployment {

    public ProvisionedDeployment(Deployment.Type type, Key deploymentKey, Configuration deploymentConfiguration,
                                 Set<URI> deployedFiles) {
        super(type, deploymentKey, deploymentConfiguration, deployedFiles);
    }

    @Override
    public String toString() {
        return "ProvisionedDeployment[" +
            ", type=" + getType() +
            ", deploymentKey='" + getKey() + '\'' +
            ", deployedFiles=" + getDeployedFiles() +
            ']';
    }
}
