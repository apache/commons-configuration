/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;


/**
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 */
class ConfigurationSet extends AbstractSet {

    class Entry implements Map.Entry {

        Object key;

        public Entry(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return configuration.getProperty((String) key);
        }

        public Object setValue(Object value) {
            Object old = getValue();
            configuration.setProperty((String) key, value);
            return old;
        }

    }

    class ConfigurationSetIterator implements Iterator {

        Iterator keys;

        public ConfigurationSetIterator() {
        	keys = configuration.getKeys();
        }

        public boolean hasNext() {
            return keys.hasNext();
         }

        public Object next() {
            return new Entry(keys.next());
         }

         public void remove() {
         	keys.remove();
         }

    }

    Configuration configuration;

    public ConfigurationSet(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
	 * @see java.util.Collection#size()
	 */
	public int size() {
        int count = 0;
		Iterator iterator = configuration.getKeys();
        while(iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
	}

	/**
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return new ConfigurationSetIterator();
	}

}
