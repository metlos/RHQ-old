package org.rhq.core.domain.provisioning;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.ws.rs.core.MediaType;

import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.util.StringUtils;

/**
 * @author Lukas Krejci
 */
@Entity
@Table(name = "RHQ_PRV_DEST_DEF")
public final class ProvisioningDestinationDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "RHQ_PRV_DEST_DEF_ID_SEQ")
    private int id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "DESCRIPTION")
    private String description;

    private transient Set<MediaType> applicableMediaTypes;

    @Column(name = "MEDIA_TYPES", length = 2048)
    private String applicableMediaTypesString;

    private transient Set<String> allowedFileExtensions;

    @Column(name = "FILE_EXTS", length = 1024)
    private String allowedFileExtensionsString;

    @JoinColumn(name = "RESOURCE_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private ResourceType definingResourceType;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "definition")
    private Set<ProvisioningDestination> destinations;

    @PostLoad
    protected void convertToLists() {
        if (applicableMediaTypesString != null) {
            applicableMediaTypes = MediaTypes.parse(applicableMediaTypesString);
        } else {
            applicableMediaTypes = Collections.emptySet();
        }

        if (allowedFileExtensions != null) {
            allowedFileExtensions = new HashSet<String>(Arrays.asList(allowedFileExtensionsString.split(" ")));
        } else {
            allowedFileExtensions = Collections.emptySet();
        }
    }

    @PrePersist @PreUpdate
    protected void convertToStrings() {
        if (applicableMediaTypes != null) {
            applicableMediaTypesString = MediaTypes.toString(applicableMediaTypes);
        } else {
            applicableMediaTypesString = "";
        }

        if (allowedFileExtensions != null) {
            allowedFileExtensionsString = StringUtils.join(allowedFileExtensions, " ", false);
        }
    }

    public Set<String> getAllowedFileExtensions() {
        if (allowedFileExtensions == null) {
            allowedFileExtensions = new HashSet<String>();
        }
        return allowedFileExtensions;
    }

    public void setAllowedFileExtensions(Set<String> allowedFileExtensions) {
        if (allowedFileExtensions == null) {
            throw new IllegalArgumentException("allowedFileExtensions can't be null.");
        }
        this.allowedFileExtensions = allowedFileExtensions;
    }

    public Set<MediaType> getApplicableMediaTypes() {
        if (applicableMediaTypes == null) {
            applicableMediaTypes = new HashSet<MediaType>();
        }
        return applicableMediaTypes;
    }

    public void setApplicableMediaTypes(Set<MediaType> applicableMediaTypes) {
        if (applicableMediaTypes == null) {
            throw new IllegalArgumentException("applicableMediaTypes can't be null.");
        }
        this.applicableMediaTypes = applicableMediaTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
        this.name = name;
    }

    public ResourceType getDefiningResourceType() {
        return definingResourceType;
    }

    public void setDefiningResourceType(ResourceType definingResourceType) {
        if (definingResourceType == null) {
            throw new IllegalArgumentException("definingResourceType can't be null.");
        }
        this.definingResourceType = definingResourceType;
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
        if (!(o instanceof ProvisioningDestinationDefinition)) {
            return false;
        }

        ProvisioningDestinationDefinition that = (ProvisioningDestinationDefinition) o;

        if (definingResourceType != null ? !definingResourceType.equals(that.definingResourceType) :
            that.definingResourceType != null) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (definingResourceType != null ? definingResourceType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvisioningDestinationDefinition[");
        sb.append("name='").append(name).append('\'');
        sb.append(", definingResourceType=").append(definingResourceType);
        sb.append(']');
        return sb.toString();
    }


}
