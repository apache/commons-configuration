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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests {@link ConfigurationPropertySource}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class TestConfigurationPropertySource {

    @Configuration
    static class Config {

        @Bean
        public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(
                final ConfigurableEnvironment env) {
            final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            // https://jira.spring.io/browse/SPR-9631 may simplify this in
            // future
            final MutablePropertySources sources = new MutablePropertySources();
            sources.addLast(createConfigPropertySource());
            configurer.setPropertySources(sources);
            configurer.setEnvironment(env);
            return configurer;
        }
    }

    private static final String TEST_PROPERTY = "test.property";

    private static final String TEST_SYSTEM_PROPERTY = "test.system.property";

    private static final String TEST_VALUE = "testVALUE";

    private static ConfigurationPropertySource createConfigPropertySource() {
        final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.addProperty(TEST_PROPERTY, TEST_VALUE);
        propertiesConfiguration.addProperty(TEST_SYSTEM_PROPERTY, "${sys:" + TEST_SYSTEM_PROPERTY + "}");
        return new ConfigurationPropertySource("test configuration", propertiesConfiguration);
    }

    @BeforeAll
    public static void setUp() {
        System.setProperty(TEST_SYSTEM_PROPERTY, TEST_VALUE);
    }

    @AfterAll
    public static void tearDown() {
        System.clearProperty(TEST_SYSTEM_PROPERTY);
    }

    @Value("${" + TEST_PROPERTY + "}")
    private String value;

    @Value("${" + TEST_SYSTEM_PROPERTY + "}")
    private String systemPropertyValue;

    @Test
    public void testSystemPropertyValueInjection() {
        assertEquals(TEST_VALUE, systemPropertyValue);
    }

    @Test
    public void testValueInjection() {
        assertEquals(TEST_VALUE, value);
    }

}
