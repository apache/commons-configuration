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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Spring FactoryBean test.
 */
public class TestConfigurationPropertiesFactoryBean
{

    private ConfigurationPropertiesFactoryBean configurationFactory;

    @Before
    public void setUp()
    {
        configurationFactory = new ConfigurationPropertiesFactoryBean();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfterPropertiesSet() throws Exception
    {
        configurationFactory.afterPropertiesSet();
    }

    @Test
    public void testGetObject() throws Exception
    {
        configurationFactory.setConfigurations(new Configuration[] {
                new BaseConfiguration()
        });
        Assert.assertNull(configurationFactory.getObject());
        configurationFactory.afterPropertiesSet();
        Assert.assertNotNull(configurationFactory.getObject());
    }

    @Test
    public void testMergeConfigurations() throws Exception
    {
        final Configuration one = new BaseConfiguration();
        one.setProperty("foo", "bar");
        final String properties =
                "## some header \n" + "foo = bar1\n" + "bar = foo\n";

        final PropertiesConfiguration two = new PropertiesConfiguration();
        final PropertiesConfigurationLayout layout =
                new PropertiesConfigurationLayout();
        layout.load(two, new StringReader(properties));

        configurationFactory.setConfigurations(new Configuration[] {
                one, two
        });
        configurationFactory.afterPropertiesSet();
        final Properties props = configurationFactory.getObject();
        Assert.assertEquals("foo", props.getProperty("bar"));
        Assert.assertEquals("bar", props.getProperty("foo"));
    }

    @Test
    public void testLoadResources() throws Exception
    {
        configurationFactory.setLocations(new Resource[] {
                new ClassPathResource("testConfigurationFactoryBean.file")
        });
        configurationFactory.setConfigurations(new Configuration[] {
                new BaseConfiguration()
        });
        configurationFactory.afterPropertiesSet();

        final Properties props = configurationFactory.getObject();
        Assert.assertEquals("duke", props.getProperty("java"));
    }

    @Test
    public void testInitialConfiguration() throws Exception
    {
        configurationFactory =
                new ConfigurationPropertiesFactoryBean(new BaseConfiguration());
        configurationFactory.afterPropertiesSet();
        Assert.assertNotNull(configurationFactory.getConfiguration());
    }

    @Test
    public void testSetLocationsDefensiveCopy()
    {
        final Resource[] locations = {
                new ClassPathResource("f1"), new ClassPathResource("f2")
        };
        final Resource[] locationsUpdate = locations.clone();

        configurationFactory.setLocations(locationsUpdate);
        locationsUpdate[0] = new ClassPathResource("other");
        assertArrayEquals("Locations were changed", locations,
                configurationFactory.getLocations());
    }

    @Test
    public void testSetLocationsNull()
    {
        configurationFactory.setLocations(null);
        assertNull("Got locations", configurationFactory.getLocations());
    }

    @Test
    public void testGetLocationsDefensiveCopy()
    {
        final Resource[] locations = {
                new ClassPathResource("f1"), new ClassPathResource("f2")
        };
        configurationFactory.setLocations(locations);

        final Resource[] locationsGet = configurationFactory.getLocations();
        locationsGet[1] = null;
        assertArrayEquals("Locations were changed", locations,
                configurationFactory.getLocations());
    }

    @Test
    public void testSetConfigurationsDefensiveCopy()
    {
        final Configuration[] configs = {
                new PropertiesConfiguration(), new XMLConfiguration()
        };
        final Configuration[] configsUpdate = configs.clone();

        configurationFactory.setConfigurations(configsUpdate);
        configsUpdate[0] = null;
        assertArrayEquals("Configurations were changed", configs,
                configurationFactory.getConfigurations());
    }

    @Test
    public void testGetConfigurationDefensiveCopy()
    {
        final Configuration[] configs = {
                new PropertiesConfiguration(), new XMLConfiguration()
        };
        configurationFactory.setConfigurations(configs);

        final Configuration[] configsGet = configurationFactory.getConfigurations();
        configsGet[0] = null;
        assertArrayEquals("Configurations were changed", configs,
                configurationFactory.getConfigurations());
    }
}
