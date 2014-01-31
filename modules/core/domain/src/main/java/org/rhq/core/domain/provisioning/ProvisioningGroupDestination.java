package org.rhq.core.domain.provisioning;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.rhq.core.domain.resource.group.ResourceGroup;

/**
 * @author Lukas Krejci
 */
@Entity
@DiscriminatorValue("G")
public final class ProvisioningGroupDestination extends ProvisioningDestination {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "TARGET_RES_GROUP_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private ResourceGroup targetResourceGroup;

    public ResourceGroup getTargetResourceGroup() {
        return targetResourceGroup;
    }

    public void setTargetResourceGroup(ResourceGroup targetResourceGroup) {
        this.targetResourceGroup = targetResourceGroup;
    }

    @Override
    protected void addToString(StringBuilder bld) {
        bld.append(", targetResourceGroup.id=").append(targetResourceGroup.getId());
    }
}
