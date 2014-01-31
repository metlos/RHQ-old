package org.rhq.core.domain.provisioning;

import java.net.URI;

import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;

/**
 * @author Lukas Krejci
 */
public final class BundleRecipe extends Artifact {

    private static final long serialVersionUID = 1L;

    private ConfigurationDefinition configurationDefinition;

    public BundleRecipe(URI repositoryUri, String identifier, String path, String mimetype, String version) {
        super(repositoryUri, identifier, path, mimetype, version);
    }

    public ConfigurationDefinition getConfigurationDefinition() {
        return configurationDefinition;
    }

    public void setConfigurationDefinition(ConfigurationDefinition configurationDefinition) {
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    protected void addToString(StringBuilder bld) {
        bld.append(", configurationDefinition=").append(configurationDefinition).append('\n');
    }
}
