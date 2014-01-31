package org.rhq.core.domain.provisioning;

import java.io.Serializable;
import java.net.URI;
import java.rmi.activation.Activatable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.rhq.core.domain.configuration.Configuration;

/**
 * @author Lukas Krejci
 */
@Entity
@Table(name = "RHQ_PRV_CONTENT_REPO")
public final class ContentRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "RHQ_CONTENT_REPO_ID_SEQ")
    private int id;

    private int kachna;

    private transient URI uri;

    @Column(name = "URI", nullable = false)
    private String uriString;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PLUGIN", nullable = false)
    private String connectorPlugin;

    @JoinColumn(name = "CONFIG_ID", referencedColumnName = "ID")
    @OneToOne(cascade = CascadeType.ALL)
    private Configuration connectorConfiguration;

    @Column(name = "SYNC_SCHEDULE")
    private String syncSchedule;

    @PostLoad
    protected void initUri() {
        uri = URI.create(uriString);
    }

    @PrePersist
    @PreUpdate
    protected void uriToString() {
        uriString = uri.toString();
    }

    public Configuration getConnectorConfiguration() {
        return connectorConfiguration;
    }

    public void setConnectorConfiguration(Configuration connectorConfiguration) {
        this.connectorConfiguration = connectorConfiguration;
    }

    public String getConnectorPlugin() {
        return connectorPlugin;
    }

    public void setConnectorPlugin(String connectorPlugin) {
        if (connectorPlugin == null) {
            throw new IllegalArgumentException("connectorPlugin can't be null.");
        }
        this.connectorPlugin = connectorPlugin;
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

    public String getSyncSchedule() {
        return syncSchedule;
    }

    public void setSyncSchedule(String syncSchedule) {
        this.syncSchedule = syncSchedule;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri can't be null.");
        }

        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentRepository)) {
            return false;
        }

        ContentRepository that = (ContentRepository) o;

        if (!uri.equals(that.uri)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContentRepository[");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", uri=").append(uri);
        sb.append(']');
        return sb.toString();
    }
}
