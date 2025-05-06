/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2.spring;

import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>
 * FactoryBean which wraps a Commons CompositeConfiguration object for usage with PropertiesLoaderSupport. This allows
 * the compositeConfiguration object to behave like a normal {@link Properties} object which can be passed on to
 * setProperties() method allowing PropertyOverrideConfigurer and PropertyPlaceholderConfigurer to take advantage of
 * Commons Configuration.
 * </p>
 * <p>
 * Internally a CompositeConfiguration object is used for merging multiple Configuration objects.
 * </p>
 *
 * @see java.util.Properties
 * @see org.springframework.core.io.support.PropertiesLoaderSupport
 */
public class ConfigurationPropertiesFactoryBean implements InitializingBean, FactoryBean<Properties> {

    /**
     * Creates a defensive copy of the specified array. Handles null values correctly.
     *
     * @param src the source array
     * @param <T> the type of the array
     * @return the defensive copy of the array
     */
    private static <T> T[] clone(final T[] src) {
        return src != null ? src.clone() : null;
    }

    /** Internal CompositeConfiguration containing the merged configuration objects **/
    private CompositeConfiguration compositeConfiguration;

    /** Supplied configurations that will be merged in compositeConfiguration **/
    private Configuration[] configurations;

    /** Spring resources for loading configurations **/
    private Resource[] locations;

    /** @see org.apache.commons.configuration2.AbstractConfiguration#throwExceptionOnMissing **/
    private boolean throwExceptionOnMissing = true;

    /**
     * Constructs a new instance.
     */
    public ConfigurationPropertiesFactoryBean() {
    }

    /**
     * Constructs a new instance.
     *
     * @param configuration The configuration to compose.
     */
    public ConfigurationPropertiesFactoryBean(final Configuration configuration) {
        Assert.notNull(configuration, "configuration");
        this.compositeConfiguration = new CompositeConfiguration(configuration);
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (compositeConfiguration == null && ArrayUtils.isEmpty(configurations) && ArrayUtils.isEmpty(locations)) {
            throw new IllegalArgumentException("no configuration object or location specified");
        }

        if (compositeConfiguration == null) {
            compositeConfiguration = new CompositeConfiguration();
        }

        compositeConfiguration.setThrowExceptionOnMissing(throwExceptionOnMissing);

        if (configurations != null) {
            Stream.of(configurations).forEach(compositeConfiguration::addConfiguration);
        }

        if (locations != null) {
            for (final Resource location : locations) {
                compositeConfiguration.addConfiguration(new Configurations().properties(location.getURL()));
            }
        }
    }

    /**
     * Gets the composite configuration.
     *
     * @return the composite configuration.
     */
    public CompositeConfiguration getConfiguration() {
        return compositeConfiguration;
    }

    /**
     * Gets a copy of the configurations.
     *
     * @return a copy of the configurations.
     */
    public Configuration[] getConfigurations() {
        return clone(configurations);
    }

    /**
     * Gets a copy of the resource locations.
     *
     * @return a copy of the resource locations.
     */
    public Resource[] getLocations() {
        return clone(locations);
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public Properties getObject() throws Exception {
        return compositeConfiguration != null ? ConfigurationConverter.getProperties(compositeConfiguration) : null;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Tests the underlying CompositeConfiguration throwExceptionOnMissing flag.
     *
     * @return the underlying CompositeConfiguration throwExceptionOnMissing flag.
     */
    public boolean isThrowExceptionOnMissing() {
        return throwExceptionOnMissing;
    }

    /**
     * Sets the commons configurations objects which will be used as properties.
     *
     * @param configurations commons configurations objects which will be used as properties.
     */
    public void setConfigurations(final Configuration... configurations) {
        this.configurations = clone(configurations);
    }

    /**
     * Shortcut for loading compositeConfiguration from Spring resources. It will internally create a
     * PropertiesConfiguration object based on the URL retrieved from the given Resources.
     *
     * @param locations resources of configuration files
     */
    public void setLocations(final Resource... locations) {
        this.locations = clone(locations);
    }

    /**
     * Sets the underlying CompositeConfiguration throwExceptionOnMissing flag.
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#setThrowExceptionOnMissing(boolean)
     * @param throwExceptionOnMissing The new value for the property
     */
    public void setThrowExceptionOnMissing(final boolean throwExceptionOnMissing) {
        this.throwExceptionOnMissing = throwExceptionOnMissing;
    }
}
