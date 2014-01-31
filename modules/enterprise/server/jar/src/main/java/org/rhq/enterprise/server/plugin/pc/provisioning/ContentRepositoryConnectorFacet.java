package org.rhq.enterprise.server.plugin.pc.provisioning;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.criteria.ArtifactCriteria;
import org.rhq.core.domain.provisioning.Artifact;

/**
 * Defines the contract for the connectors to external content repositories.
 *
 * @author Lukas Krejci
 */
public interface ContentRepositoryConnectorFacet {

    /**
     * Types of the links we create between different artifacts.
     */
    enum LinkType {
        /**
         * Bundle link exists between a bundle recipe and the individual bundle files that comprise the bundle.
         */
        BUNDLE,

        /**
         * A deployment link exists between a deployed artifact and the individual deployment artifacts that RHQ creates
         * to give out the information about the deployments to the repository.
         */
        DEPLOYMENT
    }

    /**
     * Queries the repository for artifacts matching the provided criteria. <p> It is up to the implementation to
     * translate the criteria object into a query understood by the target content repository.
     *
     * @param criteria the criteria the artifacts need to match
     * @return the list of artifacts from the remote repository matching the criteria
     * @throws ContentRepositoryException
     */
    List<Artifact> query(ArtifactCriteria criteria) throws ContentRepositoryException;

    /**
     * Creates a new artifact in the remote repository.
     *
     * @param path               the path to store the artifact in including the name of the artifact
     * @param version            the version of the artifact or null if the repository should attempt auto-detection
     * @param artifactDefinition the additional definitions of the artifact as specified in the plugin descriptor
     * @param metadata           the metadata to attach to the artifact
     * @param content            the content of the artifact
     * @return an artifact object representing the stored artifact in the repository
     */
    Artifact create(String path, String version, Configuration artifactDefinition, Map<String, String> metadata,
        InputStream content) throws ContentRepositoryException;

    /**
     * Creates a new content-less artifact.
     *
     * @param path               the path to store the artifact in including the name of the artifact
     * @param version            the version of the artifact or null if the repository should attempt auto-detection
     * @param artifactDefinition the additional definitions of the artifact as specified in the plugin descriptor
     * @param metadata           the metadata to attach to the artifact
     * @return an artifact object representing the stored artifact in the repository
     */
    Artifact create(String path, String version, Configuration artifactDefinition, Map<String, String> metadata)
        throws ContentRepositoryException;

    /**
     * Creates a link of given type between the two artifacts.
     *
     * @param from     the source artifact
     * @param to       the target artifact
     * @param linkType the type of the link to create between the artifacts
     */
    void createLink(Artifact from, Artifact to, LinkType linkType) throws ContentRepositoryException;

    /**
     * Obtains the metadata of an artifact from the repository.
     *
     * @param artifactIdentifier the unique identifier of the artifact in the repository
     * @return the artifact object with the metadata of the artifact specified by the identifier
     * @see org.rhq.core.domain.provisioning.Artifact#getIdentifier()
     */
    Artifact getMetadata(String artifactIdentifier) throws ContentRepositoryException;

    /**
     * Obtains the content of an artifact from the repository.
     *
     * @param artifactIdentifier the unique identifier of the artifact in the repository
     * @return an input stream with the contents of the artifact
     * @see org.rhq.core.domain.provisioning.Artifact#getIdentifier()
     */
    InputStream getContent(String artifactIdentifier) throws ContentRepositoryException;

    /**
     * Obtains a set of artifacts that are linked to the artifact specified by the identifier.
     *
     * @param artifactIdentifier the unique identifier of the artifact in the repository
     * @param linkType           the type of the link
     * @return the set of artifacts linked to the specified artifact
     */
    Set<Artifact> getLinkedArtifacts(String artifactIdentifier, LinkType linkType) throws ContentRepositoryException;

    /**
     * TODO is it right to have this method here? Update of the content should be accompanied by the change of the
     * version.
     *
     * Updates the content of the artifact with the content from the input stream
     *
     * @param artifactIdentifier the unique identifier of the artifact in the repository
     * @param content            the new content of the artifact
     */
    void updateContent(String artifactIdentifier, InputStream content) throws ContentRepositoryException;

    /**
     * Updates the metadata of the artifact. The provided metadata completely replaces the existing metadata of the
     * artifact.
     *
     * @param artifactIdentifier the unique identifier of the artifact in the repository
     * @param metadata           the metadata to set on the artifact
     */
    void updateMetadata(String artifactIdentifier, Map<String, String> metadata) throws ContentRepositoryException;

    /**
     * TODO this is a very dangerous action, because it will destroy the deployment history of the artifact, too. If we
     * can get away without it, we should.
     *
     * Deletes the artifact from the repository.
     *
     * @param artifactIdentifier
     */
    void delete(String artifactIdentifier) throws ContentRepositoryException;

    /**
     * Connects to the repository.
     *
     * @param repositoryURI           the URI of the repository to connect to
     * @param connectionConfiguration the connection configuration, the definition of the configuration is specified in
     *                                the server plugin descriptor
     */
    void connect(URI repositoryURI, Configuration connectionConfiguration) throws ContentRepositoryException;

    /**
     * Disconnects from the repository and frees up all the resources associated with the connection.
     */
    void disconnect() throws ContentRepositoryException;
}
