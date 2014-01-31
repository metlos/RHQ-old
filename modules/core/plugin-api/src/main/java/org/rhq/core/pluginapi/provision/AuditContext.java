/*
 * RHQ Management Platform
 * Copyright (C) 2014 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.rhq.core.pluginapi.provision;

import java.io.Serializable;

/**
 * An implementation of this interface is provided during the provisioning operations to provide incremental detailed
 * information on the progress of the provisioning.
 * <p/>
 * The methods are asynchronous and do not block the caller.
 *
 * @author Lukas Krejci
 */
public interface AuditContext {

    public enum Category {
        DEPLOY_STEP("Deploy Step"),
        FILE_ADD("File Add"),
        FILE_CHANGE("File Change"),
        FILE_DOWNLOAD("File Download"),
        FILE_REMOVE("File Remove"),
        AUDIT_MESSAGE("Audit Message");

        private final String displayName;

        private Category(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum Severity {
        SUCCESS("Success"), //
        FAILURE("Failure"), //
        WARN("Warning"), //
        INFO("Information"); // used mainly for informational audit messages

        private String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
    }

    /**
     * Use this method to provide audit information during provisioning operation, when the key of the deployment
     * is not known yet.
     *
     * @param request the request object that started the provisioning operation
     * @param action the action being performed
     * @param basicInfo the basic information about the action (like file name being copied, etc.)
     * @param category the category of the action, optional
     * @param severity the severity of the action's result, optional
     * @param description the detailed description of the action, optional
     * @param attachment binary data associated with the action, optional
     */
    void audit(ProvisioningRequest request, String action, String basicInfo, Category category, Severity severity,
        String description, Serializable attachment);

    /**
     * Use this method to provide audit information during unprovisioning operation, when the key of the deployment
     * is known.
     *
     * @param deployment the identifier of the deployment
     * @param action the action being performed
     * @param basicInfo the basic information about the action (like file name being copied, etc.)
     * @param category the category of the action, optional
     * @param severity the severity of the action's result, optional
     * @param description the detailed description of the action, optional
     * @param attachment binary data associated with the action, optional
     */
    void audit(Deployment.Key deployment, String action, String basicInfo, Category category, Severity severity,
        String description, Serializable attachment);
}
