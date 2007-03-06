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

package org.apache.commons.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

/**
 * Tests for MapConfiguration.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestMapConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        Map map = new HashMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("list", "value1, value2");
        map.put("listesc", "value1\\,value2");

        return new MapConfiguration(map);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new MapConfiguration(new HashMap());
    }

    public void testGetMap()
    {
        Map map = new HashMap();

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
        config.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                // Just a dummy
            }
        });
        MapConfiguration copy = (MapConfiguration) config.clone();
        assertTrue("Event listeners were copied", copy
                .getConfigurationListeners().isEmpty());

        config.addProperty("cloneTest", Boolean.TRUE);
        assertFalse("Map not decoupled", copy.containsKey("cloneTest"));
        copy.clearProperty("key1");
        assertEquals("Map not decoupled (2)", "value1", config
                .getString("key1"));
    }
}
