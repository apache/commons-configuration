/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.commons.configuration.web;

import org.apache.commons.configuration.AbstractConfiguration;

/**
 * <p>
 * An abstract base class for all web configurations.
 * </p>
 * <p>
 * This class implements common functionality used by all web based
 * configurations. E.g. some methods are not supported by configurations of this
 * type, so they throw a <code>UnsupportedOperationException</code> exception.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @since 1.2
 */
abstract class BaseWebConfiguration extends AbstractConfiguration
{
    /**
     * Checks if this configuration is empty. This implementation makes use of
     * the <code>getKeys()</code> method (which must be defined by concrete
     * sub classes) to find out whether properties exist.
     *
     * @return a flag whether this configuration is empty
     */
    public boolean isEmpty()
    {
        return !getKeys().hasNext();
    }

    /**
     * Checks whether the specified key is stored in this configuration.
     *
     * @param key the key
     * @return a flag whether this key exists in this configuration
     */
    public boolean containsKey(String key)
    {
        return getProperty(key) != null;
    }

    /**
     * Removes the property with the given key. <strong>This operation is not
     * supported and will throw an UnsupportedOperationException.</strong>
     *
     * @param key the key of the property to be removed
     * @throws UnsupportedOperationException because this operation is not
     * allowed
     */
    public void clearProperty(String key)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    /**
     * Adds a property to this configuration. <strong>This operation is not
     * supported and will throw an UnsupportedOperationException.</strong>
     *
     * @param key the key of the property
     * @param obj the value to be added
     * @throws UnsupportedOperationException because this operation is not
     * allowed
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }
}
