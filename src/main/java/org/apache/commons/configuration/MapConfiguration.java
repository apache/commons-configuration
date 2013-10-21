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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * A Map based Configuration.
 * </p>
 * <p>
 * This implementation of the {@code Configuration} interface is
 * initialized with a {@code java.util.Map}. The methods of the
 * {@code Configuration} interface are implemented on top of the content of
 * this map. The following storage scheme is used:
 * </p>
 * <p>
 * Property keys are directly mapped to map keys, i.e. the
 * {@code getProperty()} method directly performs a {@code get()} on
 * the map. Analogously, {@code setProperty()} or
 * {@code addProperty()} operations write new data into the map. If a value
 * is added to an existing property, a {@code java.util.List} is created,
 * which stores the values of this property.
 * </p>
 * <p>
 * An important use case of this class is to treat a map as a
 * {@code Configuration} allowing access to its data through the richer
 * interface. This can be a bit problematic in some cases because the map may
 * contain values that need not adhere to the default storage scheme used by
 * typical configuration implementations, e.g. regarding lists. In such cases
 * care must be taken when manipulating the data through the
 * {@code Configuration} interface, e.g. by calling
 * {@code addProperty()}; results may be different than expected.
 * </p>
 * <p>
 * An important point is the handling of list delimiters: If delimiter parsing
 * is enabled (which it is per default), {@code getProperty()} checks
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
 * As the underlying {@code Map} is directly used as store of the property
 * values, the thread-safety of this {@code Configuration} implementation
 * depends on the map passed to the constructor.
 * </p>
 * <p>
 * Notes about type safety: For properties with multiple values this implementation
 * creates lists of type {@code Object} and stores them. If a property is assigned
 * another value, the value is added to the list. This can cause problems if the
 * map passed to the constructor already contains lists of other types. This
 * should be avoided, otherwise it cannot be guaranteed that the application
 * might throw {@code ClassCastException} exceptions later.
 * </p>
 *
 * @author Emmanuel Bourg
 * @version $Id$
 * @since 1.1
 */
public class MapConfiguration extends AbstractConfiguration implements Cloneable
{
    /** The Map decorated by this configuration. */
    protected Map<String, Object> map;

    /** A flag whether trimming of property values should be disabled.*/
    private boolean trimmingDisabled;

    /**
     * Create a Configuration decorator around the specified Map. The map is
     * used to store the configuration properties, any change will also affect
     * the Map.
     *
     * @param map the map
     */
    public MapConfiguration(Map<String, Object> map)
    {
        this.map = map;
    }

    /**
     * Creates a new instance of {@code MapConfiguration} and initializes its
     * content from the specified {@code Properties} object. The resulting
     * configuration is not connected to the {@code Properties} object, but all
     * keys which are strings are copied (keys of other types are ignored).
     *
     * @param props the {@code Properties} object defining the content of this
     *        configuration
     * @throws NullPointerException if the {@code Properties} object is
     *         <b>null</b>
     * @since 1.8
     */
    public MapConfiguration(Properties props)
    {
        map = convertPropertiesToMap(props);
    }

    /**
     * Return the Map decorated by this configuration.
     *
     * @return the map this configuration is based onto
     */
    public Map<String, Object> getMap()
    {
        return map;
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

    public Object getProperty(String key)
    {
        Object value = map.get(key);
        if ((value instanceof String) && (!isDelimiterParsingDisabled()))
        {
            List<String> list = PropertyConverter.split((String) value, getListDelimiter(), !isTrimmingDisabled());
            return list.size() > 1 ? list : list.get(0);
        }
        else
        {
            return value;
        }
    }

    @Override
    protected void addPropertyDirect(String key, Object value)
    {
        Object previousValue = getProperty(key);

        if (previousValue == null)
        {
            map.put(key, value);
        }
        else if (previousValue instanceof List)
        {
            // the value is added to the existing list
            // Note: This is problematic. See header comment!
            ((List<Object>) previousValue).add(value);
        }
        else
        {
            // the previous value is replaced by a list containing the previous value and the new value
            List<Object> list = new ArrayList<Object>();
            list.add(previousValue);
            list.add(value);

            map.put(key, list);
        }
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public boolean containsKey(String key)
    {
        return map.containsKey(key);
    }

    @Override
    protected void clearPropertyDirect(String key)
    {
        map.remove(key);
    }

    public Iterator<String> getKeys()
    {
        return map.keySet().iterator();
    }

    /**
     * Returns a copy of this object. The returned configuration will contain
     * the same properties as the original. Event listeners are not cloned.
     *
     * @return the copy
     * @since 1.3
     */
    @Override
    public Object clone()
    {
        try
        {
            MapConfiguration copy = (MapConfiguration) super.clone();
            copy.clearConfigurationListeners();
            // Safe because ConfigurationUtils returns a map of the same types.
            @SuppressWarnings("unchecked")
            Map<String, Object> clonedMap = (Map<String, Object>) ConfigurationUtils.clone(map);
            copy.map = clonedMap;
            return copy;
        }
        catch (CloneNotSupportedException cex)
        {
            // cannot happen
            throw new ConfigurationRuntimeException(cex);
        }
    }

    /**
     * Helper method for copying all string keys from the given
     * {@code Properties} object to a newly created map.
     *
     * @param props the {@code Properties} to be copied
     * @return a newly created map with all string keys of the properties
     */
    private static Map<String, Object> convertPropertiesToMap(final Properties props)
    {
        return new AbstractMap<String, Object>() {

            @Override
            public Set<Map.Entry<String, Object>> entrySet()
            {
                Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
                for (final Map.Entry<Object, Object> propertyEntry : props.entrySet()) {
                    if (propertyEntry.getKey() instanceof String) {
                        entries.add(new Map.Entry<String, Object>() {

                            public String getKey()
                            {
                                return propertyEntry.getKey().toString();
                            }

                            public Object getValue()
                            {
                                return propertyEntry.getValue();
                            }

                            public Object setValue(Object value)
                            {
                                throw new UnsupportedOperationException();
                            }
                        });
                    }
                }
                return entries;
            }
        };
    }
}
