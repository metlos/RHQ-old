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

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.rhq.core.domain.configuration.Configuration;

/**
 * Represents a file in a content repository.
 *
 * @author Lukas Krejci
 */
public class Artifact implements Serializable {
    private static final long serialVersionUID = 1L;

    private final URI repositoryUri;
    private final String identifier;
    private String path;
    private String mediaType;
    private String version;
    private Configuration definition;
    private Map<String, String> metadata;

    //for GWT
    protected Artifact() {
        repositoryUri = null;
        identifier = null;
    }

    public Artifact(URI repositoryUri, String identifier, String path, String mediaType, String version) {
        if (repositoryUri == null) {
            throw new IllegalArgumentException("repositoryUri can't be null");
        }

        if (identifier == null) {
            throw new IllegalArgumentException("identifier can't be null.");
        }

        if (path == null) {
            throw new IllegalArgumentException("path can't be null.");
        }

        if (mediaType == null) {
            throw new IllegalArgumentException("mediaType can't be null.");
        }

        this.repositoryUri = repositoryUri;
        this.identifier = identifier;
        this.path = path;
        this.mediaType = mediaType;
        this.version = version;
    }

    /**
     * @return an identifier uniquely identifying the artifact within the {@link #getRepositoryUri() repository}.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the media type of the artifact
     */
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        if (mediaType == null) {
            throw new IllegalArgumentException("mediaType can't be null.");
        }

        this.mediaType = mediaType;
    }

    /**
     * @return the path to the artifact (including the artifact's name) in the repository. The path separator is always '/'.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path can't be null.");
        }

        this.path = path;
    }

    /**
     * @return the additional metadata of the artifact, i.e. the metadata
     */
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<String, String>();
        }

        return metadata;
    }

    /**
     * @return the URI of the repository this artifact comes from
     */
    public URI getRepositoryUri() {
        return repositoryUri;
    }

    /**
     * @return the version of the artifact
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * This is distinct from {@link #getMetadata() metadata}. The definition of the artifact is a set of additional properties
     * required to describe or define the artifact in its repository.
     *
     * @return Additional definitions that describe the artifact in the repository.
     */
    public Configuration getDefinition() {
        return definition;
    }

    public void setDefinition(Configuration definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Artifact)) {
            return false;
        }

        Artifact that = (Artifact) o;

        if (!identifier.equals(that.identifier)) {
            return false;
        }
        if (!repositoryUri.equals(that.repositoryUri)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = repositoryUri.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("[");
        sb.append("identifier='").append(identifier).append('\'');
        sb.append(", mediaType='").append(mediaType).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", repositoryUri=").append(repositoryUri);
        sb.append(", version='").append(version).append('\'');
        addToString(sb);
        sb.append(']');
        return sb.toString();
    }

    protected void addToString(StringBuilder bld) {

    }
}
