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

import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * A Map based Configuration.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/18 12:50:41 $
 * @since 1.1
 */
public class MapConfiguration extends AbstractConfiguration
{
    /** The Map decorated by this configuration. */
    protected Map map;

    /**
     * Create a Configuration decorator around the specified Map. The map is
     * used to store the configuration properties, any change will also affect
     * the Map.
     *
     * @param map
     */
    public MapConfiguration(Map map)
    {
        this.map = map;
    }

    /**
     * Return the Map decorated by this configuration.
     */
    public Map getMap()
    {
        return map;
    }

    protected Object getPropertyDirect(String key)
    {
        Object value = map.get(key);
        if (value instanceof String)
        {
            List list = split((String) value);
            return list.size() > 1 ? list : value;
        }
        else
        {
            return value;
        }
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        map.put(key, obj);
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public boolean containsKey(String key)
    {
        return map.containsKey(key);
    }

    public void clearProperty(String key)
    {
        map.remove(key);
    }

    public Iterator getKeys()
    {
        return map.keySet().iterator();
    }
}
