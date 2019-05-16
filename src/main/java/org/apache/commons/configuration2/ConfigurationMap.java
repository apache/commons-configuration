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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>The {@code ConfigurationMap} wraps a
 * configuration-collection
 * {@link org.apache.commons.configuration2.Configuration}
 * instance to provide a {@code Map} interface.</p>
 *
 * <p><em>Note:</em> This implementation is incomplete.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 * @since 1.0
 */
public class ConfigurationMap extends AbstractMap<Object, Object>
{
    /**
     * The {@code Configuration} wrapped by this class.
     */
    private final Configuration configuration;

    /**
     * Creates a new instance of a {@code ConfigurationMap}
     * that wraps the specified {@code Configuration}
     * instance.
     * @param configuration {@code Configuration}
     * instance.
     */
    public ConfigurationMap(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Returns the wrapped {@code Configuration} object.
     *
     * @return the wrapped configuration
     * @since 1.2
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Returns a set with the entries contained in this configuration-based map.
     *
     * @return a set with the contained entries
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet()
    {
        return new ConfigurationSet(configuration);
    }

    /**
     * Stores the value for the specified key. The value is stored in the
     * underlying configuration.
     *
     * @param key the key (will be converted to a string)
     * @param value the value
     * @return the old value of this key or <b>null</b> if it is new
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(final Object key, final Object value)
    {
        final String strKey = String.valueOf(key);
        final Object old = configuration.getProperty(strKey);
        configuration.setProperty(strKey, value);
        return old;
    }

    /**
     * Returns the value of the specified key. The key is converted to a string
     * and then passed to the underlying configuration.
     *
     * @param key the key
     * @return the value of this key
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(final Object key)
    {
        return configuration.getProperty(String.valueOf(key));
    }

    /**
     * Set of entries in the map.
     */
    static class ConfigurationSet extends AbstractSet<Map.Entry<Object, Object>>
    {
        /** The configuration mapped to this entry set. */
        private final Configuration configuration;

        /**
         * A Map entry in the ConfigurationMap.
         */
        private final class Entry implements Map.Entry<Object, Object>
        {
            /** The key of the map entry. */
            private final Object key;

            private Entry(final Object key)
            {
                this.key = key;
            }

            @Override
            public Object getKey()
            {
                return key;
            }

            @Override
            public Object getValue()
            {
                return configuration.getProperty((String) key);
            }

            @Override
            public Object setValue(final Object value)
            {
                final Object old = getValue();
                configuration.setProperty((String) key, value);
                return old;
            }
        }

        /**
         * Iterator over the entries in the ConfigurationMap.
         */
        private final class ConfigurationSetIterator implements Iterator<Map.Entry<Object, Object>>
        {
            /** An iterator over the keys in the configuration. */
            private final Iterator<String> keys;

            private ConfigurationSetIterator()
            {
                keys = configuration.getKeys();
            }

            @Override
            public boolean hasNext()
            {
                return keys.hasNext();
            }

            @Override
            public Map.Entry<Object, Object> next()
            {
                return new Entry(keys.next());
            }

            @Override
            public void remove()
            {
                keys.remove();
            }
        }

        ConfigurationSet(final Configuration configuration)
        {
            this.configuration = configuration;
        }

        /**
         * @see java.util.Collection#size()
         */
        @Override
        public int size()
        {
            // Ouch. Now _that_ one is expensive...
            int count = 0;
            for (final Iterator<String> iterator = configuration.getKeys(); iterator.hasNext();)
            {
                iterator.next();
                count++;
            }
            return count;
        }

        /**
         * @see java.util.Collection#iterator()
         */
        @Override
        public Iterator<Map.Entry<Object, Object>> iterator()
        {
            return new ConfigurationSetIterator();
        }
    }
}
