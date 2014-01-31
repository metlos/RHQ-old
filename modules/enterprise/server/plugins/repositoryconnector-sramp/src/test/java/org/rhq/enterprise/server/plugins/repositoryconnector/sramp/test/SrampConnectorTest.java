package org.rhq.enterprise.server.plugins.repositoryconnector.sramp.test;

import java.net.URI;
import java.util.Collections;

import org.overlord.sramp.client.AbstractNoAuditingClientTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.provisioning.Artifact;
import org.rhq.enterprise.server.plugins.repositoryconnector.sramp.SrampConnector;

/**
 * @author Lukas Krejci
 */
@Test
public class SrampConnectorTest extends AbstractNoAuditingClientTest {

    private SrampConnector connector;

    @BeforeClass
    public void baseResourceTestSetup() throws Exception {
        before();
    }

    @BeforeClass(dependsOnMethods = "baseResourceTestSetup")
    public void setup() throws Exception {
        setUp();
    }

    @BeforeClass(dependsOnMethods = "setup")
    public void connect() throws Exception {
        connector = new SrampConnector();
        connector.connect(URI.create("http://localhost:8081"), new Configuration());
    }

    @AfterClass
    public void disconnect() throws Exception {
        connector.disconnect();
        connector = null;
    }

    @AfterClass(dependsOnMethods = "disconnect")
    public void tearDown() {
        cleanup();
    }

    @AfterClass
    public void baseResourceTestTearDown() throws Exception {
        after();
    }

    @BeforeMethod
    public void cleanRepository() {
        super.cleanRepository();
    }

    public void testQuerying() throws Exception {
        //TODO implement
    }

    public void testCreateArtifact() throws Exception {
        Configuration config = new Configuration();
        config.put(new PropertySimple("artifactType", "Document"));

        Artifact a = connector.create("kachny.txt", "1.0", config, Collections.<String, String>emptyMap());
    }
}
