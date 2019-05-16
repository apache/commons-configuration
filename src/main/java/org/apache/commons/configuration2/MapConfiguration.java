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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

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
 * The handling of list delimiters is a bit different for this configuration
 * implementation: When a property of type String is queried, it is passed to
 * the current {@link org.apache.commons.configuration2.convert.ListDelimiterHandler
 * ListDelimiterHandler} which may generate multiple values.
 * Note that per default a list delimiter handler is set which does not do any
 * list splitting, so this feature is disabled. It can be enabled by setting
 * a properly configured {@code ListDelimiterHandler} implementation, e.g. a
 * {@link org.apache.commons.configuration2.convert.DefaultListDelimiterHandler
 * DefaultListDelimiterHandler} object.
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
    public MapConfiguration(final Map<String, ?> map)
    {
        this.map = (Map<String, Object>) map;
    }

    /**
     * Creates a new instance of {@code MapConfiguration} which uses the
     * specified {@code Properties} object as its data store. All changes of
     * this configuration affect the given {@code Properties} object and
     * vice versa. Note that while {@code Properties} actually
     * implements {@code Map<Object, Object>}, we expect it to contain only
     * string keys. Other key types will lead to {@code ClassCastException}
     * exceptions on certain methods.
     *
     * @param props the {@code Properties} object defining the content of this
     *        configuration
     * @since 1.8
     */
    public MapConfiguration(final Properties props)
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
    public void setTrimmingDisabled(final boolean trimmingDisabled)
    {
        this.trimmingDisabled = trimmingDisabled;
    }

    @Override
    protected Object getPropertyInternal(final String key)
    {
        final Object value = map.get(key);
        if (value instanceof String)
        {
            final Collection<String> list = getListDelimiterHandler().split((String) value, !isTrimmingDisabled());
            return list.size() > 1 ? list : list.iterator().next();
        }
        return value;
    }

    @Override
    protected void addPropertyDirect(final String key, final Object value)
    {
        final Object previousValue = getProperty(key);

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
            final List<Object> list = new ArrayList<>();
            list.add(previousValue);
            list.add(value);

            map.put(key, list);
        }
    }

    @Override
    protected boolean isEmptyInternal()
    {
        return map.isEmpty();
    }

    @Override
    protected boolean containsKeyInternal(final String key)
    {
        return map.containsKey(key);
    }

    @Override
    protected void clearPropertyDirect(final String key)
    {
        map.remove(key);
    }

    @Override
    protected Iterator<String> getKeysInternal()
    {
        return map.keySet().iterator();
    }

    @Override
    protected int sizeInternal()
    {
        return map.size();
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
            final MapConfiguration copy = (MapConfiguration) super.clone();
            // Safe because ConfigurationUtils returns a map of the same types.
            @SuppressWarnings("unchecked")
            final
            Map<String, Object> clonedMap = (Map<String, Object>) ConfigurationUtils.clone(map);
            copy.map = clonedMap;
            copy.cloneInterpolator(this);
            return copy;
        }
        catch (final CloneNotSupportedException cex)
        {
            // cannot happen
            throw new ConfigurationRuntimeException(cex);
        }
    }

    /**
     * Helper method for converting the type of the {@code Properties} object
     * to a supported map type. As stated by the comment of the constructor,
     * we expect the {@code Properties} object to contain only String key;
     * therefore, it is safe to do this cast.
     *
     * @param props the {@code Properties} to be copied
     * @return a newly created map with all string keys of the properties
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertPropertiesToMap(final Properties props)
    {
        @SuppressWarnings("rawtypes")
        final
        Map map = props;
        return map;
    }

    /**
     * Converts this object to a String suitable for debugging and logging.
     *
     * @since 2.3
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " [map=" + map + ", trimmingDisabled=" + trimmingDisabled + "]";
    }
}
