/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;

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
}
