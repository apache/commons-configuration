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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;

/**
 * Tests for MapConfiguration.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestMapConfiguration extends TestAbstractConfiguration
{
    /** Constant for a test key.*/
    private static final String KEY = "key1";

    /** Constant for a test property value with whitespace.*/
    private static final String SPACE_VALUE = "   Value with whitespace  ";

    /** The trimmed test value.*/
    private static final String TRIM_VALUE = SPACE_VALUE.trim();

    @Override
    protected AbstractConfiguration getConfiguration()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY, "value1");
        map.put("key2", "value2");
        map.put("list", "value1, value2");
        map.put("listesc", "value1\\,value2");

        return new MapConfiguration(map);
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new MapConfiguration(new HashMap<String, Object>());
    }

    public void testGetMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();

        MapConfiguration conf = new MapConfiguration(map);
        assertEquals(map, conf.getMap());
    }

    public void testClone()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        MapConfiguration copy = (MapConfiguration) config.clone();
        StrictConfigurationComparator comp = new StrictConfigurationComparator();
        assertTrue("Configurations are not equal", comp.compare(config, copy));
    }

    /**
     * Tests if the cloned configuration decoupled from the original.
     */
    public void testCloneModify()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        ConfigurationListener cl = new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                // Just a dummy
            }
        };
        config.addConfigurationListener(cl);
        MapConfiguration copy = (MapConfiguration) config.clone();
        assertFalse("Event listeners were copied", copy
                .getConfigurationListeners().contains(cl));

        config.addProperty("cloneTest", Boolean.TRUE);
        assertFalse("Map not decoupled", copy.containsKey("cloneTest"));
        copy.clearProperty("key1");
        assertEquals("Map not decoupled (2)", "value1", config
                .getString("key1"));
    }

    /**
     * Tries creating an instance with a null map. This should cause an
     * exception.
     */
    public void testInitNullMap()
    {
        try
        {
            new MapConfiguration(null);
            fail("Could create instance with null map!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests adding another value to an existing property.
     */
    public void testAddProperty()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.addProperty(KEY, TRIM_VALUE);
        config.addProperty(KEY, "anotherValue");
        List<?> values = config.getList(KEY);
        assertEquals("Wrong number of values", 3, values.size());
    }

    /**
     * Tests querying a property when trimming is active.
     */
    public void testGetPropertyTrim()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        assertEquals("Wrong trimmed value", TRIM_VALUE, config.getProperty(KEY));
    }

    /**
     * Tests querying a property when trimming is disabled.
     */
    public void testGetPropertyTrimDisabled()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        config.setTrimmingDisabled(true);
        assertEquals("Wrong trimmed value", SPACE_VALUE, config.getProperty(KEY));
    }

    /**
     * Tests querying a property when trimming is enabled, but list splitting is
     * disabled. In this case no trimming is performed (trimming only works if
     * list splitting is enabled).
     */
    public void testGetPropertyTrimNoSplit()
    {
        MapConfiguration config = (MapConfiguration) getConfiguration();
        config.getMap().put(KEY, SPACE_VALUE);
        config.setDelimiterParsingDisabled(true);
        assertEquals("Wrong trimmed value", SPACE_VALUE, config.getProperty(KEY));
    }
}
