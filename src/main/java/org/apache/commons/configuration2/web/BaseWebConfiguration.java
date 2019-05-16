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
package org.apache.commons.configuration2.web;

import java.util.Collection;

import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * <p>
 * An abstract base class for all web configurations.
 * </p>
 * <p>
 * This class implements common functionality used by all web based
 * configurations. E.g. some methods are not supported by configurations of this
 * type, so they throw a {@code UnsupportedOperationException} exception.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.2
 */
abstract class BaseWebConfiguration extends AbstractConfiguration
{
    /**
     * Checks if this configuration is empty. This implementation makes use of
     * the {@code getKeys()} method (which must be defined by concrete
     * sub classes) to find out whether properties exist.
     *
     * @return a flag whether this configuration is empty
     */
    @Override
    protected boolean isEmptyInternal()
    {
        return !getKeysInternal().hasNext();
    }

    /**
     * Checks whether the specified key is stored in this configuration.
     *
     * @param key the key
     * @return a flag whether this key exists in this configuration
     */
    @Override
    protected boolean containsKeyInternal(final String key)
    {
        return getPropertyInternal(key) != null;
    }

    /**
     * Removes the property with the given key. <strong>This operation is not
     * supported and will throw an UnsupportedOperationException.</strong>
     *
     * @param key the key of the property to be removed
     * @throws UnsupportedOperationException because this operation is not
     * allowed
     */
    @Override
    protected void clearPropertyDirect(final String key)
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
    @Override
    protected void addPropertyDirect(final String key, final Object obj)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    /**
     * Takes care of list delimiters in property values. This method checks if
     * delimiter parsing is enabled and the passed in value contains a delimiter
     * character. If this is the case, a split operation is performed.
     *
     * @param value the property value to be examined
     * @return the processed value
     */
    protected Object handleDelimiters(Object value)
    {
        if (value instanceof String)
        {
            final Collection<String> values =
                    getListDelimiterHandler().split((String) value, true);
            value = values.size() > 1 ? values : values.iterator().next();
        }

        return value;
    }
}
