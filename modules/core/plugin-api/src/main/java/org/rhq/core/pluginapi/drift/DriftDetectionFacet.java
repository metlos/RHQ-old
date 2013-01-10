/*
 * RHQ Management Platform
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.rhq.core.pluginapi.drift;

import java.util.Set;

import org.rhq.core.domain.drift.DriftDefinition;


/**
 * Resource components implementing this facet can report on "file" changes using their own managed component specific
 * mechanisms.
 *
 * @author Lukas Krejci
 * @since 4.7.0
 */
public interface DriftDetectionFacet {

    Set<Drift> generateShapshot(DriftDefinition driftDefinition);

    DriftFileStatus getFileStatus(DriftDefinition definition, String filePath);
}
