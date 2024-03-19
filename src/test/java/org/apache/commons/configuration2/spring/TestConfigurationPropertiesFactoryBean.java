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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Spring FactoryBean test.
 */
public class TestConfigurationPropertiesFactoryBean {

    private ConfigurationPropertiesFactoryBean configurationFactory;

    @BeforeEach
    public void setUp() {
        configurationFactory = new ConfigurationPropertiesFactoryBean();
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        assertThrows(IllegalArgumentException.class, configurationFactory::afterPropertiesSet);
    }

    @Test
    public void testGetConfigurationDefensiveCopy() {
        final Configuration[] configs = {new PropertiesConfiguration(), new XMLConfiguration()};
        configurationFactory.setConfigurations(configs);

        final Configuration[] configsGet = configurationFactory.getConfigurations();
        configsGet[0] = null;
        assertArrayEquals(configs, configurationFactory.getConfigurations());
    }

    @Test
    public void testGetLocationsDefensiveCopy() {
        final Resource[] locations = {new ClassPathResource("f1"), new ClassPathResource("f2")};
        configurationFactory.setLocations(locations);

        final Resource[] locationsGet = configurationFactory.getLocations();
        locationsGet[1] = null;
        assertArrayEquals(locations, configurationFactory.getLocations());
    }

    @Test
    public void testGetObject() throws Exception {
        configurationFactory.setConfigurations(new BaseConfiguration());
        assertNull(configurationFactory.getObject());
        configurationFactory.afterPropertiesSet();
        assertNotNull(configurationFactory.getObject());
    }

    @Test
    public void testInitialConfiguration() throws Exception {
        configurationFactory = new ConfigurationPropertiesFactoryBean(new BaseConfiguration());
        configurationFactory.afterPropertiesSet();
        assertNotNull(configurationFactory.getConfiguration());
    }

    @Test
    public void testLoadResources() throws Exception {
        configurationFactory.setLocations(new ClassPathResource("testConfigurationFactoryBean.file"));
        configurationFactory.setConfigurations(new BaseConfiguration());
        configurationFactory.afterPropertiesSet();

        final Properties props = configurationFactory.getObject();
        assertEquals("duke", props.getProperty("java"));
    }

    @Test
    public void testMergeConfigurations() throws Exception {
        final Configuration one = new BaseConfiguration();
        one.setProperty("foo", "bar");
        final String properties = "## some header \n" + "foo = bar1\n" + "bar = foo\n";

        final PropertiesConfiguration two = new PropertiesConfiguration();
        final PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
        layout.load(two, new StringReader(properties));

        configurationFactory.setConfigurations(one, two);
        configurationFactory.afterPropertiesSet();
        final Properties props = configurationFactory.getObject();
        assertEquals("foo", props.getProperty("bar"));
        assertEquals("bar", props.getProperty("foo"));
    }

    @Test
    public void testSetConfigurationsDefensiveCopy() {
        final Configuration[] configs = {new PropertiesConfiguration(), new XMLConfiguration()};
        final Configuration[] configsUpdate = configs.clone();

        configurationFactory.setConfigurations(configsUpdate);
        configsUpdate[0] = null;
        assertArrayEquals(configs, configurationFactory.getConfigurations());
    }

    @Test
    public void testSetLocationsDefensiveCopy() {
        final Resource[] locations = {new ClassPathResource("f1"), new ClassPathResource("f2")};
        final Resource[] locationsUpdate = locations.clone();

        configurationFactory.setLocations(locationsUpdate);
        locationsUpdate[0] = new ClassPathResource("other");
        assertArrayEquals(locations, configurationFactory.getLocations());
    }

    @Test
    public void testSetLocationsNull() {
        configurationFactory.setLocations(null);
        assertNull(configurationFactory.getLocations());
    }
}
