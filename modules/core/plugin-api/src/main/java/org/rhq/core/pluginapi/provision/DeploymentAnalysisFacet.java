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
 * Resource components that implement this interface can inform about the effects of a deployment on the resource and
 * its child resources.
 *
 * @author Lukas Krejci
 */
public interface DeploymentAnalysisFacet {

    /**
     * Analyzes the results of a deployment. This method is called right after the files have been deployed on given
     * paths.
     *
     * @param deployment the deployment that has just been made
     *
     * @return a description of the effect the deployment of the files is going to have on the inventory tree or null
     * if analysis was not possible.
     */
    DeploymentEffect analyzeDeployment(Deployment deployment);

    /**
     * Analyzes the results of a undeployment. This method is called right after the files have been undeployed from given
     * paths.
     *
     * @param deployment the deployment that has just been undeployed
     *
     * @return a description of the effect the undeployment of the files is going to have on the inventory tree or null
     * if analysis was not possible.
     */
    DeploymentEffect analyzeUndeployment(Deployment deployment);
}
