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
package org.apache.commons.configuration2.interpol;

/**
 * <p>
 * An enumeration class defining constants for the {@code Lookup} objects
 * available for each {@code Configuration} object per default.
 * </p>
 * <p>
 * When a new configuration object derived from {@code AbstractConfiguration} is
 * created it installs a {@link ConfigurationInterpolator} with a default set of
 * {@link Lookup} objects. These lookups are defined by this enumeration class.
 * </p>
 * <p>
 * All the default {@code Lookup} classes are state-less, thus their instances
 * can be shared between multiple configuration objects. Therefore, it makes
 * sense to keep shared instances in this enumeration class.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public enum DefaultLookups
{
    /** The lookup for system properties. */
    SYSTEM_PROPERTIES("sys", new SystemPropertiesLookup()),

    /** The lookup for environment properties. */
    ENVIRONMENT("env", new EnvironmentLookup()),

    /** The lookup for constants. */
    CONST("const", new ConstantLookup());

    /** The prefix under which the associated lookup object is registered. */
    private final String prefix;

    /** The associated lookup instance. */
    private final Lookup lookup;

    /**
     * Creates a new instance of {@code DefaultLookups} and sets the prefix and
     * the associated lookup instance.
     *
     * @param prfx the prefix
     * @param look the {@code Lookup} instance
     */
    private DefaultLookups(String prfx, Lookup look)
    {
        prefix = prfx;
        lookup = look;
    }

    /**
     * Returns the standard prefix for the lookup object of this kind.
     *
     * @return the prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Returns the standard {@code Lookup} instance of this kind.
     *
     * @return the associated {@code Lookup} object
     */
    public Lookup getLookup()
    {
        return lookup;
    }
}
