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
package org.apache.commons.configuration.builder.combined;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.ConfigurationBuilder;

/**
 * <p>
 * A specialized parameters object for a {@link CombinedConfigurationBuilder}.
 * </p>
 * <p>
 * This class defines methods for setting properties for customizing a builder
 * for combined configurations. Note that some of these properties can also be
 * set in the configuration definition file. If this is the case, the settings
 * in the definition file override the content of this object.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class CombinedBuilderParameters implements BuilderParameters
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY =
            BasicConfigurationBuilder.RESERVED_PARAMETER
                    + CombinedBuilderParameters.class.getName();

    /** The definition configuration builder. */
    private ConfigurationBuilder<? extends HierarchicalConfiguration> definitionBuilder;

    /**
     * Looks up an instance of this class in the specified parameters map. This
     * is equivalent to {@code fromParameters(params, false};}
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @return the instance obtained from the map or <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static CombinedBuilderParameters fromParameters(
            Map<String, Object> params)
    {
        return fromParameters(params, false);
    }

    /**
     * Looks up an instance of this class in the specified parameters map and
     * optionally creates a new one if none is found. This method can be used to
     * obtain an instance of this class which has been stored in a parameters
     * map. It is compatible with the {@code getParameters()} method.
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @param createIfMissing determines the behavior if no instance is found in
     *        the map; if <b>true</b>, a new instance with default settings is
     *        created; if <b>false</b>, <b>null</b> is returned
     * @return the instance obtained from the map or <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static CombinedBuilderParameters fromParameters(
            Map<String, Object> params, boolean createIfMissing)
    {
        CombinedBuilderParameters result =
                (CombinedBuilderParameters) params.get(PARAM_KEY);
        if (result == null && createIfMissing)
        {
            result = new CombinedBuilderParameters();
        }
        return result;
    }

    /**
     * Returns the {@code ConfigurationBuilder} object for obtaining the
     * definition configuration.
     *
     * @return the definition {@code ConfigurationBuilder}
     */
    public ConfigurationBuilder<? extends HierarchicalConfiguration> getDefinitionBuilder()
    {
        return definitionBuilder;
    }

    /**
     * Sets the {@code ConfigurationBuilder} for the definition configuration.
     * This is the configuration which contains the configuration sources that
     * form the combined configuration.
     *
     * @param builder the definition {@code ConfigurationBuilder}
     * @return a reference to this object for method chaining
     */
    public CombinedBuilderParameters setDefinitionBuilder(
            ConfigurationBuilder<? extends HierarchicalConfiguration> builder)
    {
        definitionBuilder = builder;
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns a map which contains this
     * object itself under a specific key. The static {@code fromParameters()}
     * method can be used to extract an instance from a parameters map.
     */
    public Map<String, Object> getParameters()
    {
        return Collections.singletonMap(PARAM_KEY, (Object) this);
    }
}
