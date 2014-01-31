package org.rhq.enterprise.server.plugins.repositoryconnector.sramp;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.criteria.ArtifactCriteria;
import org.rhq.core.domain.provisioning.Artifact;
import org.rhq.enterprise.server.plugin.pc.provisioning.ContentRepositoryConnectorFacet;
import org.rhq.enterprise.server.plugin.pc.provisioning.ContentRepositoryException;

/**
 * @author Lukas Krejci
 */
public class SrampConnector implements ContentRepositoryConnectorFacet {
    private URI repositoryURI;
    private SrampAtomApiClient client;

    private void checkClient() {
        if (client == null) {
            throw new ContentRepositoryException("Not connected to the server.");
        }
    }

    @Override
    public List<Artifact> query(ArtifactCriteria criteria) throws ContentRepositoryException {
        checkClient();

        //TODO the criteria should explicitly state which properties to fetch so that we can then later on
        //init those props in the "convert" method. The below code is not ready for that...

        String q = composeQuery(criteria);
        try {
            QueryResultSet results = client.buildQuery(q).query();
            List<Artifact> ret = new ArrayList<Artifact>();

            for (ArtifactSummary as : results) {
                ret.add(convert(as));
            }

            return ret;
        } catch (Exception e) {
            throw new ContentRepositoryException("Could not perform the query: " + criteria, e);
        }
    }

    @Override
    public Artifact create(String path, String version, Configuration artifactDefinition, Map<String, String> metadata,
        InputStream content) {
        checkClient();

        BaseArtifactType srampArtifact = instantiateArtifact(path, version, artifactDefinition, metadata);

        try {
            srampArtifact = client.uploadArtifact(srampArtifact, content);

            return convert(srampArtifact);
        } catch (Exception e) {
            throw new ContentRepositoryException(
                "Failed to create artifact on path " + path + " in version " + version + " with definition " +
                    artifactDefinition + " from content.", e);
        }
    }

    @Override
    public Artifact create(String path, String version, Configuration artifactDefinition,
        Map<String, String> metadata) {
        checkClient();

        BaseArtifactType srampArtifact = instantiateArtifact(path, version, artifactDefinition, metadata);

        try {
            srampArtifact = client.createArtifact(srampArtifact);

            return convert(srampArtifact);
        } catch (Exception e) {
            throw new ContentRepositoryException(
                "Failed to create artifact on path " + path + " in version " + version + " with definition " +
                    artifactDefinition + ".", e);
        }
    }

    @Override
    public void createLink(Artifact from, Artifact to, LinkType linkType) {
        checkClient();

        BaseArtifactType srampFrom = null;
        BaseArtifactType srampTo = null;

        try {
            srampFrom = client.getArtifactMetaData(from.getIdentifier());
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact " + from, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact " + from, e);
        }

        try {
            srampTo = client.getArtifactMetaData(to.getIdentifier());
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact " + to, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact " + to, e);
        }

        Relationship rel = new Relationship();
        Target relTarget = new Target();
        relTarget.setHref(to.getIdentifier());
        rel.getRelationshipTarget().add(relTarget);
        rel.setRelationshipType(getRelationshipName(linkType));

        srampFrom.getRelationship().add(rel);

        try {
            client.updateArtifactMetaData(srampFrom);
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while updating the metadata of artifact " + from, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact " + from, e);
        }
    }

    @Override
    public Artifact getMetadata(String artifactIdentifier) {
        checkClient();
        try {
            BaseArtifactType sramp = client.getArtifactMetaData(artifactIdentifier);
            return convert(sramp);
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while fetching the metadata of artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public InputStream getContent(String artifactIdentifier) {
        checkClient();

        Artifact a = getMetadata(artifactIdentifier);
        ArtifactType at = ArtifactType.valueOf(a.getDefinition().getSimpleValue("artifactType"));

        try {
            return client.getArtifactContent(at, artifactIdentifier);
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while fetching the content of artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while fetching the content of artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public Set<Artifact> getLinkedArtifacts(String artifactIdentifier, LinkType linkType) {
        checkClient();

        try {
            BaseArtifactType srampArtifact = client.getArtifactMetaData(artifactIdentifier);
            String relationshipName = getRelationshipName(linkType);

            HashSet<Artifact> ret = new HashSet<Artifact>();
            for(Relationship r : srampArtifact.getRelationship()) {
                if (relationshipName.equals(r.getRelationshipType())) {
                    for(Target t : r.getRelationshipTarget()) {
                        String targetUuid = t.getHref();
                        BaseArtifactType targetMetadata = client.getArtifactMetaData(targetUuid);
                        ret.add(convert(targetMetadata));
                    }
                }
            }

            return ret;
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while querying the linked artifacts of artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while querying the linked artifacts of artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public void updateContent(String artifactIdentifier, InputStream content) {
        checkClient();

        try {
            BaseArtifactType srampArtifact = client.getArtifactMetaData(artifactIdentifier);
            client.updateArtifactContent(srampArtifact, content);
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while updating content of artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while updating content of artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public void updateMetadata(String artifactIdentifier, Map<String, String> metadata) {
        checkClient();

        try {
            BaseArtifactType srampArtifact = client.getArtifactMetaData(artifactIdentifier);
            srampArtifact.getProperty().clear();
            for(Map.Entry<String, String> e : metadata.entrySet()) {
                Property p = new Property();
                p.setPropertyName(e.getKey());
                p.setPropertyValue(e.getValue());
                srampArtifact.getProperty().add(p);
            }

            client.updateArtifactMetaData(srampArtifact);
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while updating metadata of artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while updating metadata of artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public void delete(String artifactIdentifier) {
        checkClient();

        try {
            BaseArtifactType srampArtifact = client.getArtifactMetaData(artifactIdentifier);
            client.deleteArtifact(artifactIdentifier, ArtifactType.valueOf(srampArtifact.getArtifactType()));
        } catch (SrampClientException e) {
            throw new ContentRepositoryException("Error while deleting the artifact with identifier " + artifactIdentifier, e);
        } catch (SrampAtomException e) {
            throw new ContentRepositoryException("Error while deleting the artifact with identifier " + artifactIdentifier, e);
        }
    }

    @Override
    public void connect(URI repositoryURI, Configuration connectionConfiguration) throws ContentRepositoryException {
        disconnect();

        String username = null;
        String password = null;

        if (connectionConfiguration != null) {
            username = connectionConfiguration.getSimpleValue("username");
            password = connectionConfiguration.getSimpleValue("password");
        }

        try {
            client = new SrampAtomApiClient(repositoryURI.toString(), username, password, true);

            //if we were able to establish the client, let's store the URI, too
            this.repositoryURI = repositoryURI;
        } catch (Exception e) {
            throw new ContentRepositoryException("Failed to connect to the repository " + repositoryURI, e);
        }
    }

    @Override
    public void disconnect() {
        client = null;
    }

    private String composeQuery(ArtifactCriteria criteria) {
        return null;  //TODO implement
    }

    private Artifact convert(BaseArtifactType a) {
        String mediaType = null;
        if (a instanceof DocumentArtifactType) {
            mediaType = ((DocumentArtifactType) a).getContentType();
        }

        Artifact ret = new Artifact(repositoryURI, a.getUuid(), a.getName(), mediaType, a.getVersion());

        for (Property p : a.getProperty()) {
            ret.getMetadata().put(p.getPropertyName(), p.getPropertyValue());
        }

        Configuration definition = new Configuration();
        definition.put(new PropertySimple("artifactType", a.getArtifactType().name()));

        ret.setDefinition(definition);

        return ret;
    }

    private Artifact convert(ArtifactSummary as) {
        String mediaType = null;
        if (as.getType().newArtifactInstance() instanceof DocumentArtifactType) {
            mediaType = as.getCustomPropertyValue("contentType");
        }

        String version = as.getCustomPropertyValue("version");

        Artifact ret = new Artifact(repositoryURI, as.getUuid(), as.getName(), mediaType, version);

        Configuration definition = new Configuration();
        definition.put(new PropertySimple("artifactType", as.getType().getArtifactType().name()));

        ret.setDefinition(definition);

        return ret;
    }

    private static String getName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return path;
        } else {
            return path.substring(lastSlash + 1);
        }
    }

    private BaseArtifactType instantiateArtifact(String path, String version, Configuration artifactDefinition,
        Map<String, String> metadata) {
        String artifactType = artifactDefinition.getSimpleValue("artifactType");
        BaseArtifactType srampArtifact = ArtifactType.valueOf(artifactType).newArtifactInstance();
        srampArtifact.setVersion(version);
        srampArtifact.setName(getName(path));
        List<Property> srampProperties = srampArtifact.getProperty();

        for (Map.Entry<String, String> e : metadata.entrySet()) {
            Property p = new Property();
            p.setPropertyName(e.getKey());
            p.setPropertyValue(e.getValue());

            srampProperties.add(p);
        }

        return srampArtifact;
    }

    private static String getRelationshipName(LinkType linkType) {
        switch (linkType) {
        case BUNDLE:
            return "rhq.bundle-file";
        case DEPLOYMENT:
            return "rhq.deployment";
        default:
            throw new IllegalStateException("Unknown LinkType value");
        }
    }
}
