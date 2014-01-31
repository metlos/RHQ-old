package org.rhq.core.domain.provisioning;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.rhq.core.domain.configuration.Configuration;

/**
 * @author Lukas Krejci
 */
@Entity
@Table(name = "RHQ_PRV_DESTINATION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DEST_TYPE", discriminatorType = DiscriminatorType.CHAR)
public abstract class ProvisioningDestination implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "RHQ_PRV_DEST_ID_SEQ")
    private int id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @JoinColumn(name = "CONF_ID", referencedColumnName = "ID")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Configuration configuration;

    @JoinColumn(name = "DESTINATION_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne
    private ProvisioningDestinationDefinition definition;

    @JoinColumn(name = "TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne
    private ProvisioningTypeDefinition type;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ProvisioningDestinationDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProvisioningDestinationDefinition definition) {
        this.definition = definition;
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

    public ProvisioningTypeDefinition getType() {
        return type;
    }

    public void setType(ProvisioningTypeDefinition type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("[");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", configuration=").append(configuration);
        sb.append(", definition.id=").append(definition.getId());
        sb.append(", provisioning.type.name").append(type.getName());
        addToString(sb);
        sb.append(']');
        return sb.toString();
    }

    protected void addToString(StringBuilder bld) {

    }
}
