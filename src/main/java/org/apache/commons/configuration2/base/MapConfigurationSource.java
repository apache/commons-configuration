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
package org.apache.commons.configuration2.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * An implementation of the {@code FlatConfigurationSource} interface that holds its
 * data in a map.
 * </p>
 * <p>
 * Using a map as store for configuration settings is a natural choice as the
 * methods defined by the {@code FlatConfigurationSource} interface can be easily
 * implemented on top of a map. So most methods more or less directly delegate
 * to the map. At construction time either a map can be provided which will then
 * be used as store or a new one is created.
 * </p>
 * <p>
 * One extension to to a plain map is the feature that each key can be
 * associated with multiple values. If a property key is added that already
 * exists, a list is created containing all property values.
 * </p>
 * <p>
 * This class can also be used as a base class for other configuration sources
 * that have a map-like structure and keep their whole data in memory. An example
 * could be a configuration source wrapping a properties file.
 * </p>
 * <p>
 * {@code MapConfigurationSource} implements the {@code Serializable} interface.
 * The data that is serialized basically consists of the map used as data store.
 * Serialization can only be successful if all property values can be
 * serialized.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class MapConfigurationSource extends AbstractConfigurationSource
        implements FlatConfigurationSource, Serializable
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3921765607397858876L;

    /** The map acting as data store. */
    private final Map<String, Object> store;

    /**
     * Creates a new instance of {@code MapConfigurationSource} and initializes
     * it with the given map. The map is then used as store for the
     * configuration settings managed by this object. After passing the map to
     * this constructor it should not be accessed any more from other code. (As
     * the map may become large, we do not want to make a defensive copy.)
     *
     * @param map the map serving as data store for this {@code
     *        MapConfigurationSource} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    public MapConfigurationSource(Map<String, Object> map)
    {
        if (map == null)
        {
            throw new IllegalArgumentException("Map must not be null!");
        }

        store = map;
    }

    /**
     * Creates a new, empty instance of {@code MapConfigurationSource}. A new
     * map is created that is used as internal data store.
     */
    public MapConfigurationSource()
    {
        this(new LinkedHashMap<String, Object>());
    }

    /**
     * Creates a new instance of {@code MapConfigurationSource} and initializes
     * it from the data of the specified {@code ConfigurationSource}. This
     * constructor copies all properties stored in the passed in {@code
     * ConfigurationSource} into this source.
     *
     * @param c the {@code ConfigurationSource} to be copied (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the source to be copied is
     *         <b>null</b>
     */
    public MapConfigurationSource(FlatConfigurationSource c)
    {
        if (c == null)
        {
            throw new IllegalArgumentException(
                    "Source to copy must not be null!");
        }

        store = new LinkedHashMap<String, Object>();
        for (Iterator<String> it = c.getKeys(); it.hasNext();)
        {
            String key = it.next();
            Object value = c.getProperty(key);

            // special treatment of collection properties: the collections
            // must be copied, too
            if (value instanceof Collection<?>)
            {
                value = new ArrayList<Object>((Collection<?>) value);
            }

            store.put(key, value);
        }
    }

    /**
     * Adds the given property to this {@code ConfigurationSource}. If the
     * property does not exist yet, it is newly added. Otherwise only the new
     * value is added to the existing values of this property.
     *
     * @param key the key of the property
     * @param value the value to be added
     */
    public void addProperty(String key, Object value)
    {
        Object previousValue = getProperty(key);

        if (previousValue == null)
        {
            getStore().put(key, value);
        }
        else if (previousValue instanceof List<?>)
        {
            // the value is added to the existing list
            @SuppressWarnings("unchecked")
            List<Object> currentValues = (List<Object>) previousValue;
            currentValues.add(value);
        }
        else
        {
            // the previous value is replaced by a list containing the previous
            // value and the new value
            List<Object> list = new ArrayList<Object>();
            list.add(previousValue);
            list.add(value);

            getStore().put(key, list);
        }
    }

    /**
     * Clears all data from this {@code ConfigurationSource}. This
     * implementation clears the internal map used as data storage.
     */
    public void clear()
    {
        getStore().clear();
    }

    /**
     * Removes the specified property. This implementation removes the given key
     * from the internal map used as data storage.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        getStore().remove(key);
    }

    /**
     * Checks whether this {@code ConfigurationSource} contains a property with
     * the given key. This implementation checks the internal data map.
     *
     * @param key the key of the property in question
     * @return a flag whether a property with this key exists
     */
    public boolean containsKey(String key)
    {
        return getStore().containsKey(key);
    }

    /**
     * Returns an iterator for the keys of the properties contained in this
     * {@code ConfigurationSource}. This iterator is obtained from the internal
     * map.
     *
     * @return an iterator with the keys of all properties
     */
    public Iterator<String> getKeys()
    {
        return getStore().keySet().iterator();
    }

    /**
     * Returns an iterator for all property keys starting with the specified
     * prefix. This operation is not supported, so always an exception is
     * thrown.
     *
     * @param prefix the desired prefix
     * @return an iterator with all the keys starting with this prefix
     * @throws UnsupportedOperationException as this operation is not
     *         implemented
     */
    public Iterator<String> getKeys(String prefix)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Returns the value of the property with the given key. This implementation
     * directly accesses the internal map. If the property has multiple values,
     * the result is a {@code List} object with all values. If the property is
     * unknown, <b>null</b> is returned.
     *
     * @param key the key of the property
     * @return the value of the property
     */
    public Object getProperty(String key)
    {
        return getStore().get(key);
    }

    /**
     * Returns a flag whether this {@code ConfigurationSource} is empty. This
     * implementation checks the internal data map whether it is empty.
     *
     * @return <b>true</b> if this {@code ConfigurationSource} is empty,
     *         <b>false</b> otherwise
     */
    public boolean isEmpty()
    {
        return getStore().isEmpty();
    }

    /**
     * Sets the value of the specified property. If the property does not exist
     * yet, this method has the same effect as
     * {@link #addProperty(String, Object)}. Otherwise the value of this
     * property is replaced by the new one.
     *
     * @param key the key of the property
     * @param value the new value of this property
     */
    public void setProperty(String key, Object value)
    {
        getStore().put(key, value);
    }

    /**
     * Returns the size of this {@code ConfigurationSource}. This implementation
     * returns the size of the internal data map.
     *
     * @return the size of this {@code ConfigurationSource}
     */
    public int size()
    {
        return getStore().size();
    }

    /**
     * Returns the number of values stored for the property with the given name.
     * This operation is not supported, so this implementation always throws an
     * exception.
     *
     * @param key the key of the property in question
     * @return the number of values stored for this property
     * @throws UnsupportedOperationException as this operation is not supported
     */
    public int valueCount(String key)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Returns the underlying map, in which the data of this {@code
     * MapConfigurationSource} is stored. This method is intended to be used by
     * derived classes that need direct access to this store.
     *
     * @return the map with the data
     */
    protected Map<String, Object> getStore()
    {
        return store;
    }
}
