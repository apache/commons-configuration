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

import java.util.AbstractMap;
import java.util.Set;


/**
 * <p>The <code>ConfigurationMap</code> wraps a
 * configuration-collection
 * {@link org.apache.commons.configuration.Configuration}
 * instance to provide a <code>Map</code> interface.</p>
 *
 * @todo This implementation is incomplete. 
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 */

public class ConfigurationMap
        extends AbstractMap
{

    /**
     * The <code>Configuration</code> wrapped by this class.
     */
    Configuration configuration = null;

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

}
