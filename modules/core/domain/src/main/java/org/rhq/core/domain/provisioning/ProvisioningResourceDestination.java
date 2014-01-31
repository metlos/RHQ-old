package org.rhq.core.domain.provisioning;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.rhq.core.domain.resource.Resource;

/**
 * @author Lukas Krejci
 */
@Entity
@DiscriminatorValue("R")
public final class ProvisioningResourceDestination extends ProvisioningDestination {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "TARGET_RES_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(cascade = CascadeType.ALL)
    private Resource targetResource;

    public Resource getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(Resource targetResource) {
        this.targetResource = targetResource;
    }

    @Override
    protected void addToString(StringBuilder bld) {
        bld.append(", targetResource.id=").append(targetResource.getId());
    }
}
