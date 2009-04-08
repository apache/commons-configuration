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
 * <p>
 * A Map based Configuration.
 * </p>
 * <p>
 * This implementation of the <code>Configuration</code> interface is
 * initialized with a <code>java.util.Map</code>. The methods of the
 * <code>Configuration</code> interface are implemented on top of the content of
 * this map. The following storage scheme is used:
 * </p>
 * <p>
 * Property keys are directly mapped to map keys, i.e. the
 * <code>getProperty()</code> method directly performs a <code>get()</code> on
 * the map. Analogously, <code>setProperty()</code> or
 * <code>addProperty()</code> operations write new data into the map. If a value
 * is added to an existing property, a <code>java.util.List</code> is created,
 * which stores the values of this property.
 * </p>
 * <p>
 * An important use case of this class is to treat a map as a
 * <code>Configuration</code> allowing access to its data through the richer
 * interface. This can be a bit problematic in some cases because the map may
 * contain values that need not adhere to the default storage scheme used by
 * typical configuration implementations, e.g. regarding lists. In such cases
 * care must be taken when manipulating the data through the
 * <code>Configuration</code> interface, e.g. by calling
 * <code>addProperty()</code>; results may be different than expected.
 * </p>
 * <p>
 * An important point is the handling of list delimiters: If delimiter parsing
 * is enabled (which it is per default), <code>getProperty()</code> checks
 * whether the value of a property is a string and whether it contains the list
 * delimiter character. If this is the case, the value is split at the delimiter
 * resulting in a list. This split operation typically also involves trimming
 * the single values as the list delimiter character may be surrounded by
 * whitespace. Trimming can be disabled with the
 * {@link #setTrimmingDisabled(boolean)} method. The whole list splitting
 * behavior can be disabled using the
 * {@link #setDelimiterParsingDisabled(boolean)} method.
 * </p>
 * <p>
 * Notice that list splitting is only performed for single string values. If a
 * property has multiple values, the single values are not split even if they
 * contain the list delimiter character.
 * </p>
 * <p>
 * As the underlying <code>Map</code> is directly used as store of the property
 * values, the thread-safety of this <code>Configuration</code> implementation
 * depends on the map passed to the constructor.
 * </p>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class MapConfiguration extends BaseConfiguration
{
    /** A flag whether trimming of property values should be disabled.*/
    private boolean trimmingDisabled;

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
     * Returns the flag whether trimming of property values is disabled.
     *
     * @return <b>true</b> if trimming of property values is disabled;
     *         <b>false</b> otherwise
     * @since 1.7
     */
    public boolean isTrimmingDisabled()
    {
        return trimmingDisabled;
    }

    /**
     * Sets a flag whether trimming of property values is disabled. This flag is
     * only evaluated if list splitting is enabled. Refer to the header comment
     * for more information about list splitting and trimming.
     *
     * @param trimmingDisabled a flag whether trimming of property values should
     *        be disabled
     * @since 1.7
     */
    public void setTrimmingDisabled(boolean trimmingDisabled)
    {
        this.trimmingDisabled = trimmingDisabled;
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
                    getListDelimiter(), !isTrimmingDisabled());
            return list.size() > 1 ? list : list.get(0);
        }
        else
        {
            return value;
        }
    }
}
