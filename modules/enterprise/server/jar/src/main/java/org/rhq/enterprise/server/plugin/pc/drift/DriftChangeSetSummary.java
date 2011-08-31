/*
 * RHQ Management Platform
 * Copyright (C) 2005-2011 Red Hat, Inc.
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

package org.rhq.enterprise.server.plugin.pc.drift;

import java.util.ArrayList;
import java.util.List;

import org.rhq.core.domain.drift.DriftChangeSetCategory;

/**
 * Provides a summary of a change set.
 * 
 * This object is mainly used to pass to the alert subsystem in order to determine which alerts should be triggered.
 * It doesn't have all the information that a DriftChangeSet has - it's a summary that only has the data necessary
 * for the alert subsystem to be able to determine what alerts need to be triggered.
 * 
 * @author John Mazzitelli
 */
public class DriftChangeSetSummary {
    private long createdTime;
    private DriftChangeSetCategory category;
    private int resourceId;
    private String driftConfigurationName;
    private List<String> driftPathnames;

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public DriftChangeSetCategory getCategory() {
        return category;
    }

    public void setCategory(DriftChangeSetCategory category) {
        this.category = category;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getDriftConfigurationName() {
        return driftConfigurationName;
    }

    public void setDriftConfigurationName(String driftConfigurationName) {
        this.driftConfigurationName = driftConfigurationName;
    }

    public List<String> getDriftPathnames() {
        return obtainDriftPathnamesList();
    }

    public void setDriftPathnames(List<String> driftPathnames) {
        this.driftPathnames = driftPathnames;
    }

    public void addDriftPathname(String driftPathname) {
        obtainDriftPathnamesList().add(driftPathname);
    }

    private List<String> obtainDriftPathnamesList() {
        if (driftPathnames == null) {
            driftPathnames = new ArrayList<String>();
        }
        return driftPathnames;
    }
}
