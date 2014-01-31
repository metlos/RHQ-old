package org.rhq.enterprise.server.plugin.pc.provisioning;

/**
 * @author Lukas Krejci
 */
public class ContentRepositoryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContentRepositoryException() {
    }

    public ContentRepositoryException(Throwable cause) {
        super(cause);
    }

    public ContentRepositoryException(String message) {
        super(message);
    }

    public ContentRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
