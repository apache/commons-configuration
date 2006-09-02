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
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>The <code>ConfigurationMap</code> wraps a
 * configuration-collection
 * {@link org.apache.commons.configuration.Configuration}
 * instance to provide a <code>Map</code> interface.</p>
 *
 * <p><em>Note:</em> This implementation is incomplete.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 * @version $Revision$, $Date$
 * @since 1.0
 */
public class ConfigurationMap extends AbstractMap
{
    /**
     * The <code>Configuration</code> wrapped by this class.
     */
    private Configuration configuration;

    /**
     * Creates a new instance of a <code>ConfigurationMap</code>
     * that wraps the specified <code>Configuration</code>
     * instance.
     * @param configuration <code>Configuration</code>
     * instance.
     */
    public ConfigurationMap(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Returns the wrapped <code>Configuration</code> object.
     *
     * @return the wrapped configuration
     * @since 1.2
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        return new ConfigurationSet(configuration);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value)
    {
        String strKey = String.valueOf(key);
        Object old = configuration.getProperty(strKey);
        configuration.setProperty(strKey, value);
        return old;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        return configuration.getProperty(String.valueOf(key));
    }

    /**
     * Set of entries in the map.
     */
    static class ConfigurationSet extends AbstractSet
    {
        /** The configuration mapped to this entry set. */
        private Configuration configuration;

        /**
         * A Map entry in the ConfigurationMap.
         */
        private final class Entry implements Map.Entry
        {
            /** The key of the map entry. */
            private Object key;

            private Entry(Object key)
            {
                this.key = key;
            }

            public Object getKey()
            {
                return key;
            }

            public Object getValue()
            {
                return configuration.getProperty((String) key);
            }

            public Object setValue(Object value)
            {
                Object old = getValue();
                configuration.setProperty((String) key, value);
                return old;
            }
        }

        /**
         * Iterator over the entries in the ConfigurationMap.
         */
        private final class ConfigurationSetIterator implements Iterator
        {
            /** An iterator over the keys in the configuration. */
            private Iterator keys;

            private ConfigurationSetIterator()
            {
                keys = configuration.getKeys();
            }

            public boolean hasNext()
            {
                return keys.hasNext();
            }

            public Object next()
            {
                return new Entry(keys.next());
            }

            public void remove()
            {
                keys.remove();
            }
        }

        ConfigurationSet(Configuration configuration)
        {
            this.configuration = configuration;
        }

        /**
         * @see java.util.Collection#size()
         */
        public int size()
        {
            // Ouch. Now _that_ one is expensive...
            int count = 0;
            for (Iterator iterator = configuration.getKeys(); iterator.hasNext();)
            {
                iterator.next();
                count++;
            }
            return count;
        }

        /**
         * @see java.util.Collection#iterator()
         */
        public Iterator iterator()
        {
            return new ConfigurationSetIterator();
        }
    }
}
