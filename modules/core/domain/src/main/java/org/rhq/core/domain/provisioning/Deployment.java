/*
 * RHQ Management Platform
 * Copyright (C) 2013 Red Hat, Inc.
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

package org.rhq.core.domain.provisioning;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;

/**
 * Represents a single deployment of an artifact.
 *
 * @author Lukas Krejci
 */
public final class Deployment {

    public enum Status {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        MIXED("Mixed"),
        SUCCESS("Success"),
        FAILURE("Failure");

        private String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
    }

    public static final class OnResource {
        private final int resourceId;
        private Status status;

        public OnResource(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof OnResource)) {
                return false;
            }

            OnResource that = (OnResource) o;

            if (resourceId != that.resourceId) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return resourceId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("OnResource[");
            sb.append("resourceId=").append(resourceId);
            sb.append(", status=").append(status);
            sb.append(']');
            return sb.toString();
        }


    }

    public static final class DeploymentBuilder {
        private Deployment deployment;

        private DeploymentBuilder(Artifact artifact, int destinationId, Integer resourceGroupId, Integer resourceId) {
            deployment = new Deployment(artifact, destinationId, resourceGroupId, resourceId);
        }

        public DeploymentBuilder withDeploymentConfiguration(Configuration deploymentConfiguration) {
            deployment.deploymentConfiguration = deploymentConfiguration;
            return this;
        }

        public DeploymentBuilder withDestinationConfiguration(Configuration destinationConfiguration) {
            deployment.destinationConfiguration = destinationConfiguration;
            return this;
        }

        public DeploymentBuilder withName(String name) {
            deployment.name = name;
            return this;
        }

        public DeploymentBuilder withStatus(Status status) {
            deployment.status = status;
            return this;
        }

        public DeploymentBuilder withLive(boolean live) {
            deployment.live = live;
            return this;
        }

        public DeploymentBuilder withSubjectName(String subjectName) {
            deployment.subjectName = subjectName;
            return this;
        }

        public DeploymentBuilder withErrorMessage(String errorMessage) {
            deployment.errorMessage = errorMessage;
            return this;
        }

        public DeploymentBuilder onResources(OnResource... resources) {
            deployment.getResources().addAll(Arrays.asList(resources));
            return this;
        }

        public Deployment build() {
            return deployment;
        }
    }

    private final Artifact artifact;
    private Configuration deploymentConfiguration;
    private Configuration destinationConfiguration;
    private String name;
    private Status status;
    private boolean live;
    private String subjectName;
    private String errorMessage;
    private final int destinationId;
    private final Integer resourceGroupId;
    private final Integer resourceId;
    private final Set<OnResource> resources = new HashSet<OnResource>();

    public static DeploymentBuilder createGroupDeployment(Artifact artifact, int destinationId, int resourceGroupId) {
        return new DeploymentBuilder(artifact, destinationId, resourceGroupId, null);
    }

    public static DeploymentBuilder createResourceDeployment(Artifact artifact, int destinationId, int resourceId) {
        return new DeploymentBuilder(artifact, destinationId, null, resourceId);
    }

    //for GWT
    private Deployment() {
        artifact = null;
        destinationId = 0;
        resourceGroupId = null;
        resourceId = null;
    }

    protected Deployment(Artifact artifact, int destinationId, Integer resourceGroupId, Integer resourceId) {
        if (artifact == null) {
            throw new IllegalArgumentException("artifact can't be null.");
        }

        if (resourceGroupId == null && resourceId == null) {
            throw new IllegalArgumentException(
                "Exactly one of resourceGroupId or resourceId must not be null, but both are null.");
        }

        if (resourceGroupId != null && resourceId != null) {
            throw new IllegalArgumentException(
                "Exactly one of resourceGroupId or resourceId must not be null, but both are not null.");
        }

        if (destinationId == 0) {
            throw new IllegalArgumentException("destinationId must not be 0.");
        }

        this.artifact = artifact;
        this.resourceGroupId = resourceGroupId;
        this.resourceId = resourceId;
        this.destinationId = destinationId;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Configuration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(Configuration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public Configuration getDestinationConfiguration() {
        return destinationConfiguration;
    }

    public void setDestinationConfiguration(Configuration destinationConfiguration) {
        this.destinationConfiguration = destinationConfiguration;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getResourceGroupId() {
        return resourceGroupId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Set<OnResource> getResources() {
        return resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deployment)) {
            return false;
        }

        Deployment that = (Deployment) o;

        if (destinationId != that.destinationId) {
            return false;
        }
        if (!artifact.equals(that.artifact)) {
            return false;
        }
        if (resourceGroupId != null ? !resourceGroupId.equals(that.resourceGroupId) : that.resourceGroupId != null) {
            return false;
        }
        if (resourceId != null ? !resourceId.equals(that.resourceId) : that.resourceId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = artifact.hashCode();
        result = 31 * result + destinationId;
        result = 31 * result + (resourceGroupId != null ? resourceGroupId.hashCode() : 0);
        result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Deployment[");
        sb.append("artifact=").append(artifact);
        sb.append(", deploymentConfiguration=").append(deploymentConfiguration);
        sb.append(", destinationConfiguration=").append(destinationConfiguration);
        sb.append(", destinationId=").append(destinationId);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append(", live=").append(live);
        sb.append(", name='").append(name).append('\'');
        sb.append(", resourceGroupId=").append(resourceGroupId);
        sb.append(", resourceId=").append(resourceId);
        sb.append(", status=").append(status);
        sb.append(", subjectName='").append(subjectName).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
