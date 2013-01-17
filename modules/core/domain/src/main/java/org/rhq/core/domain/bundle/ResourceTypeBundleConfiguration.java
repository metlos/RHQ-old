/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
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

package org.rhq.core.domain.bundle;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;

/**
 * If a resource type can be a target for bundle deployment, it will define some metadata values inside this configuration object.
 * We store these in a Configuration to support extensibility in the future. Stored in this configuration object will be things like
 * the bundle destination base directory definitions (the base locations where bundles can be deployed for resources that
 * are of the given type). Rather than expect users of this object to know the internal properties stored in the config, this
 * object has strongly-typed methods to extract the properties into more easily consumable POJOs, such as
 * {@link #getBundleDestinationBaseDirectory()} and {@link #addBundleDestinationBaseDirectory(String, String, String, String)}.
 *
 * @author John Mazzitelli
 * @author Lukas Krejci
 */
public class ResourceTypeBundleConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String BUNDLE_DEST_BASE_DIR_LIST_NAME = "bundleDestBaseDirsList";
    private static final String BUNDLE_DEST_BASE_DIR_LIST_ITEM_NAME = "bundleDestBaseDirsListItem";
    private static final String BUNDLE_DEST_BASE_DIR_NAME_NAME = "name";
    private static final String BUNDLE_DEST_BASE_DIR_VALUE_CONTEXT_NAME = "valueContext";
    private static final String BUNDLE_DEST_BASE_DIR_VALUE_NAME_NAME = "valueName";
    private static final String BUNDLE_DEST_BASE_DIR_DESCRIPTION_NAME = "description";

    private static final String BUNDLE_DEST_RESOURCE_LIST_NAME = "bundleDestResourcesList";
    private static final String BUNDLE_DEST_RESOURCE_LIST_ITEM_NAME = "bundleDestResourcesListItem";
    private static final String BUNDLE_DEST_RESOURCE_NAME = "name";
    private static final String BUNDLE_DEST_RESOURCE_DESCRIPTION = "description";
    private static final String BUNDLE_DEST_RESOURCE_BUNDLE_TYPE = "bundleType";

    // this is the actual bundle configuration - see ResourceType.bundleConfiguration
    private Configuration bundleConfiguration;

    public ResourceTypeBundleConfiguration() {
        this.bundleConfiguration = null;
    }

    public ResourceTypeBundleConfiguration(Configuration bundleConfiguration) {
        this.bundleConfiguration = bundleConfiguration;
    }

    /**
     * Returns the actual, raw configuration. Callers should rarely want to use this - use the more
     * strongly typed methods such as {@link #getBundleDestinationBaseDirectory()}.
     *
     * @return the raw bundle configuration object
     */
    public Configuration getBundleConfiguration() {
        return this.bundleConfiguration;
    }

    public void setBundleConfiguration(Configuration bundleConfiguration) {
        this.bundleConfiguration = bundleConfiguration;
    }

    /**
     * Returns the different destination base directories that can be the target for a bundle deployment.
     * If this bundle configuration doesn't have any base directories, null is returned.
     * <p>
     * After a resource type is fully initialized, at least one of this and {@link #getBundleDestinationResources()}
     * will return a non-null value, meaning that there exists at least one bundle destination on a bundle-enabled
     * resource type.
     *
     * @see {@link #getAllDestinations()} which always returns a non-null value
     *
     * @return the set of destination base directories that can be targets for bundle deployments
     */
    public Set<BundleDestinationBaseDirectory> getBundleDestinationBaseDirectories() {
        if (this.bundleConfiguration == null) {
            return null;
        }

        PropertyList propertyList = this.bundleConfiguration.getList(BUNDLE_DEST_BASE_DIR_LIST_NAME);
        if (propertyList == null) {
            return null;
        }

        List<Property> list = propertyList.getList();
        if (list.size() == 0) {
            return null;
        }

        Set<BundleDestinationBaseDirectory> retVal = new HashSet<BundleDestinationBaseDirectory>(list.size());
        for (Property listItem : list) {
            PropertyMap map = (PropertyMap) listItem;
            String name = map.getSimpleValue(BUNDLE_DEST_BASE_DIR_NAME_NAME, null);
            String valueContext = map.getSimpleValue(BUNDLE_DEST_BASE_DIR_VALUE_CONTEXT_NAME, null);
            String valueName = map.getSimpleValue(BUNDLE_DEST_BASE_DIR_VALUE_NAME_NAME, null);
            String description = map.getSimpleValue(BUNDLE_DEST_BASE_DIR_DESCRIPTION_NAME, null);
            BundleDestinationBaseDirectory bdbd = new BundleDestinationBaseDirectory(name, valueContext, valueName,
                description);
            retVal.add(bdbd);
        }

        return retVal;
    }

    /**
     * This method returns all destinations defined on the resource type.
     */
    public Set<BundleDestination> getAllDestinations() {
        Set<BundleDestination> ret = new HashSet<BundleDestination>();

        Set<BundleDestinationBaseDirectory> baseDirs = getBundleDestinationBaseDirectories();
        Set<BundleDestinationResource> resourceBased = getBundleDestinationResources();

        if (baseDirs != null) {
            ret.addAll(baseDirs);
        }

        if (resourceBased != null) {
            ret.addAll(resourceBased);
        }

        return ret;
    }

    /**
     * Returns the destinations handled by the resources that can be the target for a bundle deployment.
     * If this bundle configuration doesn't have any resource-handled destinations, null is returned.
     * <p>
     * After a resource type is fully initialized, at least one of this and {@link #getBundleDestinationBaseDirectories()}
     * will return a non-null value, meaning that there exists at least one bundle destination on a bundle-enabled
     * resource type.
     *
     * @see {@link #getAllDestinations()} which always returns a non-null value
     *
     * @return the set of resource-based destinations that can be targets for bundle deployments
     */
    public Set<BundleDestinationResource> getBundleDestinationResources() {
        if (this.bundleConfiguration == null) {
            return null;
        }

        PropertyList propertyList = this.bundleConfiguration.getList(BUNDLE_DEST_RESOURCE_LIST_NAME);
        if (propertyList == null) {
            return null;
        }

        List<Property> list = propertyList.getList();
        if (list.size() == 0) {
            return null;
        }

        Set<BundleDestinationResource> retVal = new HashSet<BundleDestinationResource>(list.size());
        for (Property listItem : list) {
            PropertyMap map = (PropertyMap) listItem;
            String name = map.getSimpleValue(BUNDLE_DEST_RESOURCE_NAME, null);
            String bundleType = map.getSimpleValue(BUNDLE_DEST_RESOURCE_BUNDLE_TYPE, null);
            String description = map.getSimpleValue(BUNDLE_DEST_RESOURCE_DESCRIPTION, null);
            BundleDestinationResource bdr = new BundleDestinationResource(name, description, bundleType);
            retVal.add(bdr);
        }

        return retVal;
    }

    /**
     * Adds a destination base directory that can be used as a target for a bundle deployment.
     *
     * @param name the name of this bundle destination base directory (must not be <code>null</code>)
     * @param valueContext indicates where the value's name can be looked up and found. This
     *                     must be the string form of one of the enums found
     *                     in {@link BundleDestinationBaseDirectory.Context}
     * @param valueName the name of the property found in the given context where the value
     *                  of the base directory is
     * @param description optional explanation for what this destination location is
     */
    public void addBundleDestinationBaseDirectory(String name, String valueContext, String valueName, String description) {
        if (this.bundleConfiguration == null) {
            throw new NullPointerException("bundleConfiguration == null");
        }

        // we create this just to make sure the context and value are valid. An exception will be thrown if they are not.
        BundleDestinationBaseDirectory destBaseDir = new BundleDestinationBaseDirectory(name, valueContext, valueName,
            description);

        PropertyList propertyList = this.bundleConfiguration.getList(BUNDLE_DEST_BASE_DIR_LIST_NAME);
        if (propertyList == null) {
            propertyList = new PropertyList(BUNDLE_DEST_BASE_DIR_LIST_NAME);
            this.bundleConfiguration.put(propertyList);
        }

        PropertySimple nameProp = new PropertySimple(BUNDLE_DEST_BASE_DIR_NAME_NAME, destBaseDir.getName());
        PropertySimple valueContextProp = new PropertySimple(BUNDLE_DEST_BASE_DIR_VALUE_CONTEXT_NAME, destBaseDir
            .getValueContext().name());
        PropertySimple valueNameProp = new PropertySimple(BUNDLE_DEST_BASE_DIR_VALUE_NAME_NAME, destBaseDir
            .getValueName());

        PropertyMap map = new PropertyMap(BUNDLE_DEST_BASE_DIR_LIST_ITEM_NAME);
        map.put(nameProp);
        map.put(valueContextProp);
        map.put(valueNameProp);

        if (destBaseDir.getDescription() != null) {
            PropertySimple descriptionProp = new PropertySimple(BUNDLE_DEST_BASE_DIR_DESCRIPTION_NAME, destBaseDir
                .getDescription());
            map.put(descriptionProp);
        }

        propertyList.add(map);
        return;
    }

    /**
     * Adds a destination base directory that can be used as a target for a bundle deployment.
     *
     * @param name the name of this bundle destination base directory (must not be <code>null</code>)
     * @param description optional explanation for what this destination location is
     * @param bundleType the name of the bundle type accepted by this destination
     */
    public void addBundleDestinationResource(String name, String description, String bundleType) {
        if (this.bundleConfiguration == null) {
            throw new NullPointerException("bundleConfiguration == null");
        }

        BundleDestinationResource destResource = new BundleDestinationResource(name, description, bundleType);

        PropertyList propertyList = this.bundleConfiguration.getList(BUNDLE_DEST_RESOURCE_LIST_NAME);
        if (propertyList == null) {
            propertyList = new PropertyList(BUNDLE_DEST_RESOURCE_LIST_NAME);
            this.bundleConfiguration.put(propertyList);
        }

        PropertySimple nameProp = new PropertySimple(BUNDLE_DEST_RESOURCE_NAME, destResource.getName());
        PropertySimple bundleTypeProp = new PropertySimple(BUNDLE_DEST_RESOURCE_BUNDLE_TYPE, destResource
            .getBundleType());

        PropertyMap map = new PropertyMap(BUNDLE_DEST_RESOURCE_LIST_ITEM_NAME);
        map.put(nameProp);
        map.put(bundleTypeProp);

        if (destResource.getDescription() != null) {
            PropertySimple descriptionProp = new PropertySimple(BUNDLE_DEST_RESOURCE_DESCRIPTION, destResource
                .getDescription());
            map.put(descriptionProp);
        }

        propertyList.add(map);
        return;
    }

    @Override
    public int hashCode() {
        return ((bundleConfiguration == null) ? 0 : bundleConfiguration.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceTypeBundleConfiguration)) {
            return false;
        }
        ResourceTypeBundleConfiguration other = (ResourceTypeBundleConfiguration) obj;
        if (this.bundleConfiguration == null) {
            if (other.bundleConfiguration != null) {
                return false;
            }
        } else if (!this.bundleConfiguration.equals(other.bundleConfiguration)) {
            return false;
        }
        return true;
    }

    public static abstract class BundleDestination implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String description;

        protected BundleDestination(String name, String description) {
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BundleDestination)) {
                return false;
            }
            BundleDestination other = (BundleDestination) obj;
            return this.name.equals(other.name);
        }
    }

    /**
     * Defines where bundles can be deployed on a resource that is of our resource type.
     */
    public static class BundleDestinationBaseDirectory extends BundleDestination {
        private static final long serialVersionUID = 2L;

        /**
         * Defines the different places where we can lookup the named value that contains
         * the actual location of the destination base directory.
         * The names of these enum constants match the valid values that the agent
         * plugin XML schema accepts, to allow for easy translation from the XML
         * to this Java representation.
         */
        public enum Context {
            /** the value is to be found in the resource's plugin configuration */
            pluginConfiguration,

            /** the value is to be found in the resource's resource configuration */
            resourceConfiguration,

            /** the value is to be found as a measurement trait */
            measurementTrait,

            /** the value is a hardcoded location on the file system - usually the root "/" directory */
            fileSystem
        };

        private final Context valueContext;
        private final String valueName;

        public BundleDestinationBaseDirectory(String name, String valueContext, String valueName, String description) {
            super(name, description);
            this.valueContext = Context.valueOf(valueContext); // will throw an exception if its not valid, which is what we want
            this.valueName = valueName;
        }

        /**
         * @return indicates where the {@link #getValueName() value's name} can be looked up
         *         and found. This must be one of the enums found in {@link BundleDestinationBaseDirectory.Context}
         */
        public Context getValueContext() {
            return valueContext;
        }

        /**
         * @return the name of the property found in the given {@link #getValueContext() context}
         *         where the value of the base directory is
         */
        public String getValueName() {
            return valueName;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BundleDestinationBaseDirectory [name=").append(getName()).append(", valueContext=").append(
                valueContext).append(", valueName=").append(valueName).append(", description=").append(getDescription())
                .append("]");
            return builder.toString();
        }
    }

    public static class BundleDestinationResource extends BundleDestination {

        private static final long serialVersionUID = 1L;

        private final String bundleType;

        public BundleDestinationResource(String name, String description, String bundleType) {
            super(name, description);
            if (bundleType == null) {
                throw new IllegalArgumentException("bundleType == null");
            }
            this.bundleType = bundleType;
        }

        /**
         * Indicates the type of a bundle that can be deployed to this destination
         * @return
         */
        public String getBundleType() {
            return bundleType;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BundleDestinationResource [name=").append(getName()).append(", bundleType=")
                .append(bundleType).append(", description=").append(getDescription()).append("]");
            return builder.toString();
        }
    }
}
