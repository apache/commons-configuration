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

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.jupiter.api.Test;

/**
 * Tests the ConfigurationConverter class.
 */
public class TestConfigurationConverter {
    /**
     * Creates a configuration object with some test values.
     *
     * @return the test configuration
     */
    private static BaseConfiguration createTestConfiguration() {
        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty("string", "teststring");
        config.addProperty("array", "item 1");
        config.addProperty("array", "item 2");
        config.addProperty("interpolated", "${string}");
        config.addProperty("interpolated-array", "${interpolated}");
        config.addProperty("interpolated-array", "${interpolated}");
        return config;
    }

    @Test
    public void testConfigurationToMap() {
        final Configuration config = new BaseConfiguration();
        config.addProperty("string", "teststring");

        final Map<Object, Object> map = ConfigurationConverter.getMap(config);

        assertNotNull(map);
        assertEquals("teststring", map.get("string"));
    }

    /**
     * Tests a conversion to Properties if the default list delimiter handler is used (which does not support list joining).
     */
    @Test
    public void testConfigurationToPropertiesDefaultListHandling() {
        final BaseConfiguration config = createTestConfiguration();
        final Properties props = ConfigurationConverter.getProperties(config);

        assertNotNull(props);
        assertEquals("teststring", props.getProperty("string"));
        assertEquals("teststring", props.getProperty("interpolated"));
        assertEquals("item 1,item 2", props.getProperty("array"));
        assertEquals("teststring,teststring", props.getProperty("interpolated-array"));
    }

    /**
     * Tests a conversion to Properties if the list delimiter handler supports list joining.
     */
    @Test
    public void testConfigurationToPropertiesListDelimiterHandler() {
        final BaseConfiguration config = createTestConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        final Properties props = ConfigurationConverter.getProperties(config);
        assertEquals("item 1;item 2", props.getProperty("array"));
    }

    /**
     * Tests a conversion to Properties if the source configuration does not extend AbstractConfiguration. In this case,
     * properties with multiple values have to be handled in a special way.
     */
    @Test
    public void testConfigurationToPropertiesNoAbstractConfiguration() {
        final Configuration src = mock(Configuration.class);
        final BaseConfiguration config = createTestConfiguration();

        when(src.getKeys()).thenReturn(config.getKeys());
        when(src.getList(any())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0, String.class);
            return config.getList(key);
        });

        final Properties props = ConfigurationConverter.getProperties(src);
        assertEquals("item 1,item 2", props.getProperty("array"));
    }

    /**
     * Tests the conversion of a configuration object to properties if scalar values are involved. This test is related to
     * CONFIGURATION-432.
     */
    @Test
    public void testConfigurationToPropertiesScalarValue() {
        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty("scalar", Integer.valueOf(42));
        final Properties props = ConfigurationConverter.getProperties(config);
        assertEquals("42", props.getProperty("scalar"));
    }

    @Test
    public void testPropertiesToConfiguration() {
        final Properties props = new Properties();
        props.setProperty("string", "teststring");
        props.setProperty("int", "123");
        props.setProperty("list", "item 1, item 2");

        final AbstractConfiguration config = (AbstractConfiguration) ConfigurationConverter.getConfiguration(props);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));

        assertEquals("teststring", config.getString("string"));
        final List<Object> item1 = config.getList("list");
        assertEquals("item 1", item1.get(0));

        assertEquals(123, config.getInt("int"));
    }

}
