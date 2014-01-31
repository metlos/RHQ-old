/*
 * RHQ Management Platform
 * Copyright (C) 2013 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.rhq.core.pluginapi.provision;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Desribes the effects a (un)provisioning of a deployment has on the inventory.
 *
 * @author Lukas Krejci
 */
public final class DeploymentEffect implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Set<URI>, Set<DeploymentResourceEffect>> effects;

    public DeploymentEffect() {
        effects = new HashMap<Set<URI>, Set<DeploymentResourceEffect>>();
    }

    /**
     * The keys in the returned map are file sets, each grouping the files in the deployment
     * into groups affecting a single resource. The values then represent the effect on each resource.
     * <p/>
     * Note that there is no requirement on the file sets to be unique - they can overlap (i.e. a single file
     * may be present in multiple file sets).
     * <p/>
     * Also note that the URIs identifying the files are intended to be relative to some "root" that is identified
     * by the deployment's key.
     * <p/>
     * finally, the returned map is mutable and intended to be "filled" by interested parties.
     *
     * @return the map of effects a deployment has on the inventory
     */
    public Map<Set<URI>, Set<DeploymentResourceEffect>> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        return "DeploymentEffect[" +
            "effects=" + effects +
            ']';
    }
}
