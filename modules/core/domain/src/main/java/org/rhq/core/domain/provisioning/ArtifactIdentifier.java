package org.rhq.core.domain.provisioning;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Lukas Krejci
 */
@Entity
@Table(name = "RHQ_ARTIFACT_IDENTIFIER")
public final class ArtifactIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "RHQ_ARTIFACT_ID_ID_SEQ")
    private int id;

    @JoinColumn(name = "CONTENT_REPO_ID", referencedColumnName = "ID")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private ContentRepository repository;

    @Column(name = "PATH", nullable = false)
    private String path;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ContentRepository getRepository() {
        return repository;
    }

    public void setRepository(ContentRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArtifactIdentifier)) {
            return false;
        }

        ArtifactIdentifier that = (ArtifactIdentifier) o;

        if (!path.equals(that.path)) {
            return false;
        }
        if (!repository.equals(that.repository)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = repository.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArtifactIdentifier[");
        sb.append("id=").append(id);
        sb.append(", path='").append(path).append('\'');
        sb.append(", repository.id=").append(repository.getId());
        sb.append(']');
        return sb.toString();
    }
}
