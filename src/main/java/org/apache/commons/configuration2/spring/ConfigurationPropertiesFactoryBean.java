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

import java.net.URL;
import java.util.Properties;

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
 * <p>FactoryBean which wraps a Commons CompositeConfiguration object for usage
 * with PropertiesLoaderSupport. This allows the compositeConfiguration object to behave
 * like a normal java.util.Properties object which can be passed on to
 * setProperties() method allowing PropertyOverrideConfigurer and
 * PropertyPlaceholderConfigurer to take advantage of Commons Configuration.
 * </p>
 * <p>Internally a CompositeConfiguration object is used for merging multiple
 * Configuration objects.</p>
 *
 * @see java.util.Properties
 * @see org.springframework.core.io.support.PropertiesLoaderSupport
 *
 */
public class ConfigurationPropertiesFactoryBean implements InitializingBean, FactoryBean<Properties>
{

    /** internal CompositeConfiguration containing the merged configuration objects **/
    private CompositeConfiguration compositeConfiguration;

    /** supplied configurations that will be merged in compositeConfiguration **/
    private Configuration[] configurations;

    /** Spring resources for loading configurations **/
    private Resource[] locations;

    /** @see org.apache.commons.configuration2.AbstractConfiguration#throwExceptionOnMissing **/
    private boolean throwExceptionOnMissing = true;

    public ConfigurationPropertiesFactoryBean()
    {
    }

    public ConfigurationPropertiesFactoryBean(final Configuration configuration)
    {
        Assert.notNull(configuration, "configuration");
        this.compositeConfiguration = new CompositeConfiguration(configuration);
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public Properties getObject() throws Exception
    {
        return (compositeConfiguration != null) ? ConfigurationConverter.getProperties(compositeConfiguration) : null;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType()
    {
        return java.util.Properties.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (compositeConfiguration == null && ArrayUtils.isEmpty(configurations) && ArrayUtils.isEmpty(locations))
        {
            throw new IllegalArgumentException("no configuration object or location specified");
        }

        if (compositeConfiguration == null)
        {
            compositeConfiguration = new CompositeConfiguration();
        }

        compositeConfiguration.setThrowExceptionOnMissing(throwExceptionOnMissing);

        if (configurations != null)
        {
            for (final Configuration configuration : configurations)
            {
                compositeConfiguration.addConfiguration(configuration);
            }
        }

        if (locations != null)
        {
            for (final Resource location : locations)
            {
                final URL url = location.getURL();
                final Configuration props = new Configurations().properties(url);
                compositeConfiguration.addConfiguration(props);
            }
        }
    }

    public Configuration[] getConfigurations()
    {
        return defensiveCopy(configurations);
    }

    /**
     * Set the commons configurations objects which will be used as properties.
     *
     * @param configurations commons configurations objects which will be used as properties.
     */
    public void setConfigurations(final Configuration[] configurations)
    {
        this.configurations = defensiveCopy(configurations);
    }

    public Resource[] getLocations()
    {
        return defensiveCopy(locations);
    }

    /**
     * Shortcut for loading compositeConfiguration from Spring resources. It will
     * internally create a PropertiesConfiguration object based on the URL
     * retrieved from the given Resources.
     *
     * @param locations resources of configuration files
     */
    public void setLocations(final Resource[] locations)
    {
        this.locations = defensiveCopy(locations);
    }

    public boolean isThrowExceptionOnMissing()
    {
        return throwExceptionOnMissing;
    }

    /**
     * Set the underlying Commons CompositeConfiguration throwExceptionOnMissing flag.
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#setThrowExceptionOnMissing(boolean)
     * @param throwExceptionOnMissing The new value for the property
     */
    public void setThrowExceptionOnMissing(final boolean throwExceptionOnMissing)
    {
        this.throwExceptionOnMissing = throwExceptionOnMissing;
    }

    public CompositeConfiguration getConfiguration()
    {
        return compositeConfiguration;
    }

    /**
     * Creates a defensive copy of the specified array. Handles null values
     * correctly.
     *
     * @param src the source array
     * @param <T> the type of the array
     * @return the defensive copy of the array
     */
    private static <T> T[] defensiveCopy(final T[] src)
    {
        return (src != null) ? src.clone() : null;
    }
}
