/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.commons.collections.ExtendedProperties;

/**
 * Tests the ConfigurationConverter class.
 *
 * @author Martin Poeschl
 * @author Emmanuel Bourg
 * @version $Revision: 1.5 $, $Date: 2004/06/16 15:17:09 $
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

        assertEquals("This returns 'teststring'", config.getString("string"), "teststring");
        List item1 = config.getList("list");
        assertEquals("This returns 'item 1'", (String) item1.get(0), "item 1");
        assertEquals("This returns 123", config.getInt("int"), 123);
    }

    public void testPropertiesToConfiguration()
    {
        Properties props = new Properties();
        props.setProperty("string", "teststring");
        props.setProperty("int", "123");
        props.setProperty("list", "item 1, item 2");

        Configuration config = ConfigurationConverter.getConfiguration(props);

        assertEquals("This returns 'teststring'", config.getString("string"), "teststring");
        List item1 = config.getList("list");
        assertEquals("This returns 'item 1'", (String) item1.get(0), "item 1");
        assertEquals("This returns 123", config.getInt("int"), 123);
    }

    public void testConfigurationToExtendedProperties()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("string", "teststring");
        config.setProperty("int", "123");
        config.addProperty("list", "item 1");
        config.addProperty("list", "item 2");

        ExtendedProperties eprops = ConfigurationConverter.getExtendedProperties(config);

        assertEquals("This returns 'teststring'", eprops.getString("string"), "teststring");
        List list = eprops.getVector("list");
        assertEquals("This returns 'item 1'", (String) list.get(0), "item 1");
        assertEquals("This returns 123", eprops.getInt("int"), 123);
    }

    public void testConfigurationToProperties()
    {
        Configuration config = new BaseConfiguration();
        config.addProperty("string", "teststring");
        config.addProperty("array", "item 1");
        config.addProperty("array", "item 2");

        Properties props = ConfigurationConverter.getProperties(config);

        assertNotNull("null properties", props);
        assertEquals("'string' property", "teststring", props.getProperty("string"));
        assertEquals("'array' property", "item 1, item 2", props.getProperty("array"));
    }

}
