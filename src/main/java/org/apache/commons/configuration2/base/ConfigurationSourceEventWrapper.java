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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * A specialized implementation of {@code ConfigurationSource} that allows
 * adding support for event notifications to a wrapped {@code
 * ConfigurationSource}.
 * </p>
 * <p>
 * This class is a wrapper around another {@code ConfigurationSource}. The
 * methods defined by the {@code ConfigurationSource} interface are - to the
 * major part - implemented by delegating to the wrapped source. If a method
 * requires firing an event, the event is fired by this class before and after
 * the wrapped source is invoked. This way support for event notifications can
 * be added to arbitrary {@code ConfigurationSource} implementations
 * transparently.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class ConfigurationSourceEventWrapper implements ConfigurationSource
{
    /** Stores the wrapped configuration source. */
    private final ConfigurationSource wrappedSource;

    /** The list with the event listeners that have been registered. */
    private final List<ConfigurationSourceListener> listeners;

    /**
     * Creates a new instance of {@code ConfigurationSourceEventWrapper} and
     * sets the source to be wrapped.
     *
     * @param wrapped the source to be wrapped (must not be <b>null</b>)
     * @throws IllegalArgumentException if the wrapped source is <b>null</b>
     */
    public ConfigurationSourceEventWrapper(ConfigurationSource wrapped)
    {
        if (wrapped == null)
        {
            throw new IllegalArgumentException(
                    "Wrapped source must not be null!");
        }

        wrappedSource = wrapped;
        listeners = new CopyOnWriteArrayList<ConfigurationSourceListener>();
    }

    /**
     * Returns the {@code ConfigurationSource} that is wrapped by this object.
     *
     * @return the wrapped source
     */
    public ConfigurationSource getWrappedSource()
    {
        return wrappedSource;
    }

    /**
     * Adds a new {@code ConfigurationSourceListener} to this source.
     * Implementation note: It is safe to call this method concurrently from
     * multiple threads.
     *
     * @param l the listener to be added (must not be <b>null</b>)
     * @throws IllegalArgumentException if the listener is <b>null</b>
     */
    public void addConfigurationSourceListener(ConfigurationSourceListener l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationSourceListener must not be null!");
        }

        listeners.add(l);
    }

    /**
     * Adds a new property to this {@code ConfigurationSource}. This method
     * delegates to the wrapped source. It also produces the correct events for
     * adding a property.
     *
     * @param key the key of the new property
     * @param value the value of the new property
     */
    public void addProperty(String key, Object value)
    {
        fireEvent(ConfigurationSourceEvent.Type.ADD_PROPERTY, key, value, true);
        getWrappedSource().addProperty(key, value);
        fireEvent(ConfigurationSourceEvent.Type.ADD_PROPERTY, key, value, false);
    }

    /**
     * Clears this {@code ConfigurationSource}. This implementation delegates to
     * the wrapped source. It also produces the correct events for clearing the
     * source.
     */
    public void clear()
    {
        fireEvent(ConfigurationSourceEvent.Type.CLEAR_SOURCE, null, null, true);
        getWrappedSource().clear();
        fireEvent(ConfigurationSourceEvent.Type.CLEAR_SOURCE, null, null, false);
    }

    /**
     * Removes the specified property from this {@code ConfigurationSource}.
     * This implementation delegates to the wrapped source. It also produces the
     * correct events for removing a property.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        fireEvent(ConfigurationSourceEvent.Type.CLEAR_PROPERTY, key, null, true);
        getWrappedSource().clearProperty(key);
        fireEvent(ConfigurationSourceEvent.Type.CLEAR_PROPERTY, key, null,
                false);
    }

    /**
     * Tests whether the specified key is contained in this {@code
     * ConfigurationSource}. This implementation delegates to the wrapped
     * source.
     *
     * @param key the key in question
     * @return a flag whether this key is contained in this {@code
     *         ConfigurationSource}
     */
    public boolean containsKey(String key)
    {
        return getWrappedSource().containsKey(key);
    }

    /**
     * Returns an iterator with all keys contained in this {@code
     * ConfigurationSource}. This implementation delegates to the wrapped
     * source.
     *
     * @return an iterator with the keys of this {@code ConfigurationSource}
     */
    public Iterator<String> getKeys()
    {
        return getWrappedSource().getKeys();
    }

    /**
     * Returns an iterator with all keys contained in this {@code
     * ConfigurationSource} starting with the given prefix. This implementation
     * delegates to the wrapped source.
     *
     * @param prefix the prefix of the searched keys
     * @return an iterator with all keys starting with this prefix
     */
    public Iterator<String> getKeys(String prefix)
    {
        return getWrappedSource().getKeys(prefix);
    }

    /**
     * Returns the value of the specified property. This implementation
     * delegates to the wrapped source.
     *
     * @param key the key of the property
     * @return the value of this property or <b>null</b> if it cannot be found
     */
    public Object getProperty(String key)
    {
        return getWrappedSource().getProperty(key);
    }

    /**
     * Tests whether this {@code ConfigurationSource} is empty. This
     * implementation delegates to the wrapped source.
     *
     * @return <b>true</b> if this {@code ConfigurationSource} is empty,
     *         <b>false</b> otherwise
     */
    public boolean isEmpty()
    {
        return getWrappedSource().isEmpty();
    }

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * {@code ConfigurationSource}.
     *
     * @param l the listener to be removed
     * @return a flag whether this listener could be removed
     */
    public boolean removeConfigurationSourceListener(
            ConfigurationSourceListener l)
    {
        return listeners.remove(l);
    }

    /**
     * Sets the value of a property. This implementation delegates to the
     * wrapped source. It also produces the correct events for modifying a
     * property.
     *
     * @param key the key of the property to be set
     * @param value the new value of this property
     */
    public void setProperty(String key, Object value)
    {
        fireEvent(ConfigurationSourceEvent.Type.MODIFY_PROPERTY, key, value,
                true);
        getWrappedSource().setProperty(key, value);
        fireEvent(ConfigurationSourceEvent.Type.MODIFY_PROPERTY, key, value,
                false);
    }

    /**
     * Returns the size of this {@code ConfigurationSource}. This implementation
     * delegates to the wrapped source.
     *
     * @return the size of this {@code ConfigurationSource}
     */
    public int size()
    {
        return getWrappedSource().size();
    }

    /**
     * Returns the number of values stored for the property with the given key.
     * This implementation delegates to the wrapped source.
     *
     * @param key the key of the property in question
     * @return the number of values stored for this property
     */
    public int valueCount(String key)
    {
        return getWrappedSource().valueCount(key);
    }

    /**
     * Helper method for sending an event to all listeners currently registered
     * at this object.
     *
     * @param type the type of the event
     * @param key the property key
     * @param value the property value
     * @param before the before update flag
     */
    private void fireEvent(ConfigurationSourceEvent.Type type, String key,
            Object value, boolean before)
    {
        ConfigurationSourceEvent event = null; // lazy creation

        for (ConfigurationSourceListener l : listeners)
        {
            if (event == null)
            {
                event = new ConfigurationSourceEvent(this, type, key, value,
                        null, before);
            }

            l.configurationSourceChanged(event);
        }
    }
}
