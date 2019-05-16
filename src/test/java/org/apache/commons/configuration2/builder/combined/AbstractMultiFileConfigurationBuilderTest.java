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
package org.apache.commons.configuration2.builder.combined;

import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.junit.After;

/**
 * A base class for test classes for {@code MultiFileConfigurationBuilder} and
 * derived classes. This class provides some common functionality related to
 * file name pattern which can be used by concrete tests.
 *
 */
public class AbstractMultiFileConfigurationBuilderTest
{
    /** The system property which selects a sub configuration. */
    private static final String PROP = "Id";

    /** The part of the pattern containing the variable. */
    protected static String PATTERN_VAR = "${sys:Id}";

    /** The pattern for file names. */
    protected static String PATTERN =
            "target/test-classes/testMultiConfiguration_" + PATTERN_VAR
                    + ".xml";

    /**
     * Sets a system property for accessing a specific configuration file from
     * the test builder. The passed in id can be null, then the system property
     * is removed.
     *
     * @param id the ID of the managed configuration to load
     */
    protected static void switchToConfig(final String id)
    {
        if (id != null)
        {
            System.setProperty(PROP, id);
        }
        else
        {
            System.getProperties().remove(PROP);
        }
    }

    @After
    public void tearDown() throws Exception
    {
        switchToConfig(null);
    }

    /**
     * Selects a specific configuration to be obtained from the builder.
     *
     * @param index the index of the configuration to be accessed (valid indices
     *        are 1-3)
     */
    protected static void switchToConfig(final int index)
    {
        switchToConfig("100" + index);
    }

    /**
     * Creates a {@code ConfigurationInterpolator} to be used by tests. This
     * object contains a lookup for system properties.
     *
     * @return the new {@code ConfigurationInterpolator}
     */
    protected static ConfigurationInterpolator createInterpolator()
    {
        final ConfigurationInterpolator ci = new ConfigurationInterpolator();
        ci.registerLookup(DefaultLookups.SYSTEM_PROPERTIES.getPrefix(),
                DefaultLookups.SYSTEM_PROPERTIES.getLookup());
        return ci;
    }

    /**
     * Creates a parameters object with default settings for a test builder
     * instance.
     *
     * @param managedParams the parameters for managed configurations
     * @return the test parameters
     */
    protected static BasicBuilderParameters createTestBuilderParameters(
            final BuilderParameters managedParams)
    {
        return new MultiFileBuilderParametersImpl().setFilePattern(PATTERN)
                .setManagedBuilderParameters(managedParams)
                .setInterpolator(createInterpolator());
    }
}
