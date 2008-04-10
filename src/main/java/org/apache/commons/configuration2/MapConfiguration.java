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

import org.apache.commons.configuration2.flat.BaseConfiguration;

/**
 * <p>A Map based Configuration.</p>
 * <p><em>Note:</em>Configuration objects of this type can be read concurrently
 * by multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.</p>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class MapConfiguration extends BaseConfiguration
{
    /**
     * Create a Configuration decorator around the specified Map. The map is
     * used to store the configuration properties, any change will also affect
     * the Map.
     *
     * @param map the map (must not be <b>null</b>)
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    @SuppressWarnings("unchecked")
    public MapConfiguration(Map map)
    {
        super(map);
    }

    /**
     * Return the Map decorated by this configuration.
     *
     * @return the map this configuration is based onto
     */
    public Map<String, Object> getMap()
    {
        return getStore();
    }

    /**
     * Returns the value of the specified property. This implementation checks
     * for list delimiters in string. (Because the map was created externally,
     * we cannot be sure that string splitting was performed when the properties
     * were added.)
     *
     * @param key the key of the property
     * @return the value of this property
     */
    @Override
    public Object getProperty(String key)
    {
        Object value = super.getProperty(key);
        if ((value instanceof String) && (!isDelimiterParsingDisabled()))
        {
            List<String> list = PropertyConverter.split((String) value,
                    getListDelimiter());
            return list.size() > 1 ? list : list.get(0);
        }
        else
        {
            return value;
        }
    }
}
