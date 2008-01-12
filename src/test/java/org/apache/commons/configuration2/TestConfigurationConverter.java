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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;

/**
 * Tests the ConfigurationConverter class.
 *
 * @author Martin Poeschl
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestConfigurationConverter extends TestCase
{
    public void testExtendedPropertiesToConfiguration()
    {
        ExtendedProperties eprops = new ExtendedProperties();
        eprops.setProperty("string", "teststring");
        eprops.setProperty("int", "123");
        eprops.addProperty("list", "item 1");
        eprops.addProperty("list", "item 2");

        Configuration config = ConfigurationConverter.getConfiguration(eprops);

        assertEquals("This returns 'teststring'", "teststring", config.getString("string"));
        List item1 = config.getList("list");
        assertEquals("This returns 'item 1'", "item 1", (String) item1.get(0));

        assertEquals("This returns 123", 123, config.getInt("int"));
    }

    public void testPropertiesToConfiguration()
    {
        Properties props = new Properties();
        props.setProperty("string", "teststring");
        props.setProperty("int", "123");
        props.setProperty("list", "item 1, item 2");

        Configuration config = ConfigurationConverter.getConfiguration(props);

        assertEquals("This returns 'teststring'", "teststring", config.getString("string"));
        List item1 = config.getList("list");
        assertEquals("This returns 'item 1'", "item 1", (String) item1.get(0));

        assertEquals("This returns 123", 123, config.getInt("int"));
    }

    public void testConfigurationToExtendedProperties()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("string", "teststring");
        config.setProperty("int", "123");
        config.addProperty("list", "item 1");
        config.addProperty("list", "item 2");

        ExtendedProperties eprops = ConfigurationConverter.getExtendedProperties(config);

        assertEquals("This returns 'teststring'", "teststring", eprops.getString("string"));
        List list = eprops.getVector("list");
        assertEquals("This returns 'item 1'", "item 1", (String) list.get(0));
        assertEquals("This returns 123", 123, eprops.getInt("int"));
    }

    public void testConfigurationToProperties()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("string", "teststring");
        config.addProperty("array", "item 1");
        config.addProperty("array", "item 2");
        config.addProperty("interpolated", "${string}");
        config.addProperty("interpolated-array", "${interpolated}");
        config.addProperty("interpolated-array", "${interpolated}");

        Properties props = ConfigurationConverter.getProperties(config);

        assertNotNull("null properties", props);
        assertEquals("'string' property", "teststring", props.getProperty("string"));
        assertEquals("'interpolated' property", "teststring", props.getProperty("interpolated"));
        assertEquals("'array' property", "item 1,item 2", props.getProperty("array"));
        assertEquals("'interpolated-array' property", "teststring,teststring", props.getProperty("interpolated-array"));

        // change the list delimiter
        config.setListDelimiter(';');
        props = ConfigurationConverter.getProperties(config);
        assertEquals("'array' property", "item 1;item 2", props.getProperty("array"));
    }

    public void testConfigurationToMap()
    {
        Configuration config = new BaseConfiguration();
        config.addProperty("string", "teststring");

        Map map = ConfigurationConverter.getMap(config);

        assertNotNull("null map", map);
        assertEquals("'string' property", "teststring", map.get("string"));
    }

}
