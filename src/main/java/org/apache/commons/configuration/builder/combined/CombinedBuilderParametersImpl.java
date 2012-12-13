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
import org.apache.commons.configuration.builder.BasicBuilderParameters;
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
public class CombinedBuilderParametersImpl extends BasicBuilderParameters
        implements CombinedBuilderProperties<CombinedBuilderParametersImpl>
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY = RESERVED_PARAMETER_PREFIX
            + CombinedBuilderParametersImpl.class.getName();

    /** The definition configuration builder. */
    private ConfigurationBuilder<? extends HierarchicalConfiguration> definitionBuilder;

    /** A parameters object for the definition configuration builder. */
    private BuilderParameters definitionBuilderParameters;

    /** A map with registered configuration builder providers. */
    private final Map<String, ConfigurationBuilderProvider> providers;

    /** The base path for configuration sources to be loaded. */
    private String basePath;

    /**
     * Creates a new instance of {@code CombinedBuilderParametersImpl}.
     */
    public CombinedBuilderParametersImpl()
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
    public static CombinedBuilderParametersImpl fromParameters(
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
    public static CombinedBuilderParametersImpl fromParameters(
            Map<String, Object> params, boolean createIfMissing)
    {
        CombinedBuilderParametersImpl result =
                (CombinedBuilderParametersImpl) params.get(PARAM_KEY);
        if (result == null && createIfMissing)
        {
            result = new CombinedBuilderParametersImpl();
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
    public CombinedBuilderParametersImpl setDefinitionBuilder(
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
    public CombinedBuilderParametersImpl registerProvider(String tagName,
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
     * @param providers a map with tag names and corresponding providers (must
     *        not be <b>null</b> or contain <b>null</b> entries)
     * @return a reference to this object for method chaining
     * @throws IllegalArgumentException if the map with providers is <b>null</b>
     *         or contains <b>null</b> entries
     */
    public CombinedBuilderParametersImpl registerMissingProviders(
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
     * Returns the base path for relative names of configuration sources. Result
     * may be <b>null</b> if no base path has been set.
     *
     * @return the base path for resolving relative file names
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Sets the base path for this combined configuration builder. Normally it
     * it not necessary to set the base path explicitly. Per default, relative
     * file names of configuration sources are resolved based on the location of
     * the definition file. If this is not desired or if the definition
     * configuration is loaded by a different means, the base path for relative
     * file names can be specified using this method.
     *
     * @param path the base path for resolving relative file names
     * @return a reference to this object for method chaining
     */
    public CombinedBuilderParametersImpl setBasePath(String path)
    {
        basePath = path;
        return this;
    }

    /**
     * Returns the parameters object for the definition configuration builder if
     * present.
     *
     * @return the parameters object for the definition configuration builder or
     *         <b>null</b>
     */
    public BuilderParameters getDefinitionBuilderParameters()
    {
        return definitionBuilderParameters;
    }

    /**
     * Sets the parameters object for the definition configuration builder. This
     * property is evaluated only if the definition configuration builder is not
     * set explicitly (using the
     * {@link #setDefinitionBuilder(ConfigurationBuilder)} method). In this
     * case, a builder for an XML configuration is created and configured with
     * this parameters object.
     *
     * @param params the parameters object for the definition configuration
     *        builder
     * @return a reference to this object for method chaining
     */
    public CombinedBuilderParametersImpl setDefinitionBuilderParameters(
            BuilderParameters params)
    {
        definitionBuilderParameters = params;
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns a map which contains this
     * object itself under a specific key. The static {@code fromParameters()}
     * method can be used to extract an instance from a parameters map.
     */
    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = super.getParameters();
        params.put(PARAM_KEY, this);
        return params;
    }
}
