package org.rhq.core.domain.criteria;

import org.rhq.core.domain.provisioning.Artifact;

/**
 * TODO This criteria won't be handled "normally" because it will be processed by the repository connectors.
 * Make sure we can query everything this class can define in the connectors.
 *
 * @author Lukas Krejci
 */
public class ArtifactCriteria extends Criteria {

    private static final long serialVersionUID = 1L;

    @Override
    public Class<?> getPersistentClass() {
        return Artifact.class;
    }
}
