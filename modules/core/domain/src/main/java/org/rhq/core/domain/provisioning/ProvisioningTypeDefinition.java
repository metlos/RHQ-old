package org.rhq.core.domain.provisioning;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.ws.rs.core.MediaType;

import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.util.StringUtils;

/**
 * @author Lukas Krejci
 */
@Entity
@Table(name = "RHQ_PRV_TYPE_DEF")
public final class ProvisioningTypeDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "RHQ_PRV_TYPE_DEF_ID_SEQ")
    private int id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @JoinColumn(name = "RESOURCE_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ResourceType handlerResourceType;

    @JoinColumn(name = "DEST_CONF_DEF_ID", referencedColumnName = "ID")
    @OneToOne(cascade = CascadeType.ALL)
    private ConfigurationDefinition destinationConfigurationDefinition;

    private transient Set<MediaType> applicableMediaTypes;

    @Column(name = "MEDIA_TYPES")
    private String applicableMediaTypesString;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "definition")
    private Set<ProvisioningDestination> destinations;

    @PostLoad
    protected void fromStrings() {
        if (applicableMediaTypesString != null) {
            applicableMediaTypes = MediaTypes.parse(applicableMediaTypesString);
        } else {
            applicableMediaTypes = Collections.emptySet();
        }
    }

    @PrePersist @PreUpdate
    protected void toStrings() {
        if (applicableMediaTypes == null) {
            applicableMediaTypesString = "";
        } else {
            applicableMediaTypesString = StringUtils.join(applicableMediaTypes, " ", false);
        }
    }

    public Set<MediaType> getApplicableMediaTypes() {
        return applicableMediaTypes;
    }

    public void setApplicableMediaTypes(Set<MediaType> applicableMediaTypes) {
        if (applicableMediaTypes == null) {
            throw new IllegalArgumentException("applicableMediaTypes can't be null.");
        }
        this.applicableMediaTypes = applicableMediaTypes;
    }

    public ConfigurationDefinition getDestinationConfigurationDefinition() {
        return destinationConfigurationDefinition;
    }

    public void setDestinationConfigurationDefinition(ConfigurationDefinition destinationConfigurationDefinition) {
        this.destinationConfigurationDefinition = destinationConfigurationDefinition;
    }

    public ResourceType getHandlerResourceType() {
        return handlerResourceType;
    }

    public void setHandlerResourceType(ResourceType handlerResourceType) {
        this.handlerResourceType = handlerResourceType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name can't be null.");
        }
        this.name = name;
    }

    public Set<ProvisioningDestination> getDestinations() {
        return destinations;
    }

    public void setDestinations(Set<ProvisioningDestination> destinations) {
        this.destinations = destinations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProvisioningTypeDefinition)) {
            return false;
        }

        ProvisioningTypeDefinition that = (ProvisioningTypeDefinition) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvisioningTypeDefinition[");
        sb.append("name='").append(name).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
