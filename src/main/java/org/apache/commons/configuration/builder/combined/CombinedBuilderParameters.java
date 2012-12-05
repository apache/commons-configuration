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
import java.util.HashMap;
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

    /** A map with registered configuration builder providers. */
    private final Map<String, ConfigurationBuilderProvider> providers;

    /**
     * Creates a new instance of {@code CombinedBuilderParameters}.
     */
    public CombinedBuilderParameters()
    {
        providers = new HashMap<String, ConfigurationBuilderProvider>();
    }

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
     * Registers the given {@code ConfigurationBuilderProvider} for the
     * specified tag name. This means that whenever this tag is encountered in a
     * configuration definition file, the corresponding builder provider is
     * invoked.
     *
     * @param tagName the name of the tag (must not be <b>null</b>)
     * @param provider the {@code ConfigurationBuilderProvider} (must not be
     *        <b>null</b>)
     * @return a reference to this object for method chaining
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public CombinedBuilderParameters registerProvider(String tagName,
            ConfigurationBuilderProvider provider)
    {
        if (tagName == null)
        {
            throw new IllegalArgumentException("Tag name must not be null!");
        }
        if (provider == null)
        {
            throw new IllegalArgumentException("Provider must not be null!");
        }

        providers.put(tagName, provider);
        return this;
    }

    /**
     * Registers all {@code ConfigurationBuilderProvider}s in the given map to
     * this object which have not yet been registered. This method is mainly
     * used for internal purposes: a {@code CombinedConfigurationBuilder} takes
     * the providers contained in a parameters object and adds all standard
     * providers. This way it is possible to override a standard provider by
     * registering a provider object for the same tag name at the parameters
     * object.
     *
     * @param providers a map with tag names and corresponding providers
     * @return a reference to this object for method chaining (must not be
     *         <b>null</b> or contain <b>null</b> entries
     * @throws IllegalArgumentException if the map with providers is <b>null</b>
     *         or contains <b>null</b> entries
     */
    public CombinedBuilderParameters registerMissingProviders(
            Map<String, ConfigurationBuilderProvider> providers)
    {
        if (providers == null)
        {
            throw new IllegalArgumentException(
                    "Map with providers must not be null!");
        }

        for (Map.Entry<String, ConfigurationBuilderProvider> e : providers
                .entrySet())
        {
            if (!this.providers.containsKey(e.getKey()))
            {
                registerProvider(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    /**
     * Returns an (unmodifiable) map with the currently registered
     * {@code ConfigurationBuilderProvider} objects.
     *
     * @return the map with {@code ConfigurationBuilderProvider} objects (the
     *         keys are the tag names)
     */
    public Map<String, ConfigurationBuilderProvider> getProviders()
    {
        return Collections.unmodifiableMap(providers);
    }

    /**
     * Returns the {@code ConfigurationBuilderProvider} which is registered for
     * the specified tag name or <b>null</b> if there is no registration for
     * this tag.
     *
     * @param tagName the tag name
     * @return the provider registered for this tag or <b>null</b>
     */
    public ConfigurationBuilderProvider providerForTag(String tagName)
    {
        return providers.get(tagName);
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
