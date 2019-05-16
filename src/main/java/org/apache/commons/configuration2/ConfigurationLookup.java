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

import org.apache.commons.configuration2.interpol.Lookup;

/**
 * <p>
 * A specialized implementation of the {@code Lookup} interface which uses a
 * {@code Configuration} object to resolve variables.
 * </p>
 * <p>
 * This class is passed an {@link ImmutableConfiguration} object at construction
 * time. In its implementation of the {@code lookup()} method it simply queries
 * this configuration for the passed in variable name. So the keys passed to
 * {@code lookup()} are mapped directly to configuration properties.
 * </p>
 *
 * @since 2.0
 */
public class ConfigurationLookup implements Lookup
{
    /** The configuration to which lookups are delegated. */
    private final ImmutableConfiguration configuration;

    /**
     * Creates a new instance of {@code ConfigurationLookup} and sets the
     * associated {@code ImmutableConfiguration}.
     *
     * @param config the configuration to use for lookups (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the configuration is <b>null</b>
     */
    public ConfigurationLookup(final ImmutableConfiguration config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Configuration must not be null!");
        }
        configuration = config;
    }

    /**
     * Returns the {@code ImmutableConfiguration} used by this object.
     *
     * @return the associated {@code ImmutableConfiguration}
     */
    public ImmutableConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * {@inheritDoc} This implementation calls {@code getProperty()} on the
     * associated configuration. The return value is directly returned. Note
     * that this may be a complex object, e.g. a collection or an array.
     */
    @Override
    public Object lookup(final String variable)
    {
        return getConfiguration().getProperty(variable);
    }
}
