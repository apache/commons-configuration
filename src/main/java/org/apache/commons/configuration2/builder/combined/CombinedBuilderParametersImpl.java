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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.DefaultParametersManager;

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
 * @since 2.0
 */
public class CombinedBuilderParametersImpl extends BasicBuilderParameters
        implements CombinedBuilderProperties<CombinedBuilderParametersImpl>
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY = RESERVED_PARAMETER_PREFIX
            + CombinedBuilderParametersImpl.class.getName();

    /** The definition configuration builder. */
    private ConfigurationBuilder<? extends HierarchicalConfiguration<?>> definitionBuilder;

    /** A parameters object for the definition configuration builder. */
    private BuilderParameters definitionBuilderParameters;

    /** A map with registered configuration builder providers. */
    private final Map<String, ConfigurationBuilderProvider> providers;

    /** A list with default parameters for child configuration sources. */
    private final Collection<BuilderParameters> childParameters;

    /** The manager for default handlers. */
    private DefaultParametersManager childDefaultParametersManager;

    /** The base path for configuration sources to be loaded. */
    private String basePath;

    /** A flag whether settings should be inherited by child builders. */
    private boolean inheritSettings;

    /**
     * Creates a new instance of {@code CombinedBuilderParametersImpl}.
     */
    public CombinedBuilderParametersImpl()
    {
        providers = new HashMap<>();
        childParameters = new LinkedList<>();
        inheritSettings = true;
    }

    /**
     * Looks up an instance of this class in the specified parameters map. This
     * is equivalent to {@code fromParameters(params, false);}
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @return the instance obtained from the map or <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static CombinedBuilderParametersImpl fromParameters(
            final Map<String, ?> params)
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
            final Map<String, ?> params, final boolean createIfMissing)
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
     * {@inheritDoc} This implementation additionally copies some properties
     * defined by this class.
     */
    @Override
    public void inheritFrom(final Map<String, ?> source)
    {
        super.inheritFrom(source);

        final CombinedBuilderParametersImpl srcParams = fromParameters(source);
        if (srcParams != null)
        {
            setChildDefaultParametersManager(
                    srcParams.getChildDefaultParametersManager());
            setInheritSettings(srcParams.isInheritSettings());
        }
    }

    /**
     * Returns the current value of the flag that controls whether the settings
     * of the parent combined configuration builder should be inherited by its
     * child configurations.
     *
     * @return the flag whether settings should be inherited by child
     *         configurations
     */
    public boolean isInheritSettings()
    {
        return inheritSettings;
    }

    @Override
    public CombinedBuilderParametersImpl setInheritSettings(
            final boolean inheritSettings)
    {
        this.inheritSettings = inheritSettings;
        return this;
    }

    /**
     * Returns the {@code ConfigurationBuilder} object for obtaining the
     * definition configuration.
     *
     * @return the definition {@code ConfigurationBuilder}
     */
    public ConfigurationBuilder<? extends HierarchicalConfiguration<?>> getDefinitionBuilder()
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
    @Override
    public CombinedBuilderParametersImpl setDefinitionBuilder(
            final ConfigurationBuilder<? extends HierarchicalConfiguration<?>> builder)
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
    @Override
    public CombinedBuilderParametersImpl registerProvider(final String tagName,
            final ConfigurationBuilderProvider provider)
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
            final Map<String, ConfigurationBuilderProvider> providers)
    {
        if (providers == null)
        {
            throw new IllegalArgumentException(
                    "Map with providers must not be null!");
        }

        for (final Map.Entry<String, ConfigurationBuilderProvider> e : providers
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
     * Registers all {@code ConfigurationBuilderProvider}s in the given
     * parameters object which have not yet been registered. This method works
     * like the method with the same name, but the map with providers is
     * obtained from the passed in parameters object.
     *
     * @param params the parameters object from which to copy providers(must not
     *        be <b>null</b>)
     * @return a reference to this object for method chaining
     * @throws IllegalArgumentException if the source parameters object is
     *         <b>null</b>
     */
    public CombinedBuilderParametersImpl registerMissingProviders(
            final CombinedBuilderParametersImpl params)
    {
        if (params == null)
        {
            throw new IllegalArgumentException(
                    "Source parameters must not be null!");
        }
        return registerMissingProviders(params.getProviders());
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
    public ConfigurationBuilderProvider providerForTag(final String tagName)
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
    @Override
    public CombinedBuilderParametersImpl setBasePath(final String path)
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
    @Override
    public CombinedBuilderParametersImpl setDefinitionBuilderParameters(
            final BuilderParameters params)
    {
        definitionBuilderParameters = params;
        return this;
    }

    /**
     * Returns a collection with default parameter objects for child
     * configuration sources. This collection contains the same objects (in the
     * same order) that were passed to {@code addChildParameters()}. The
     * returned collection is a defensive copy; it can be modified, but this has
     * no effect on the parameters stored in this object.
     *
     * @return a map with default parameters for child sources
     */
    public Collection<? extends BuilderParameters> getDefaultChildParameters()
    {
        return new ArrayList<>(childParameters);
    }

    /**
     * Returns the {@code DefaultParametersManager} object for initializing
     * parameter objects for child configuration sources. This method never
     * returns <b>null</b>. If no manager was set, a new instance is created
     * right now.
     *
     * @return the {@code DefaultParametersManager} for child configuration
     *         sources
     */
    public DefaultParametersManager getChildDefaultParametersManager()
    {
        if (childDefaultParametersManager == null)
        {
            childDefaultParametersManager = new DefaultParametersManager();
        }
        return childDefaultParametersManager;
    }

    /**
     * {@inheritDoc} This implementation stores the passed in manager object. An
     * already existing manager object (either explicitly set or created on
     * demand) is overridden. This also removes all default handlers registered
     * before!
     */
    @Override
    public CombinedBuilderParametersImpl setChildDefaultParametersManager(
            final DefaultParametersManager manager)
    {
        childDefaultParametersManager = manager;
        return this;
    }

    /**
     * {@inheritDoc} This implementation registers the passed in handler at an
     * internal {@link DefaultParametersManager} instance. If none was set, a
     * new instance is created now.
     */
    @Override
    public <D> CombinedBuilderParametersImpl registerChildDefaultsHandler(
            final Class<D> paramClass, final DefaultParametersHandler<? super D> handler)
    {
        getChildDefaultParametersManager().registerDefaultsHandler(paramClass,
                handler);
        return this;
    }

    /**
     * {@inheritDoc} This implementation registers the passed in handler at an
     * internal {@link DefaultParametersManager} instance. If none was set, a
     * new instance is created now.
     */
    @Override
    public <D> CombinedBuilderParametersImpl registerChildDefaultsHandler(
            final Class<D> paramClass, final DefaultParametersHandler<? super D> handler,
            final Class<?> startClass)
    {
        getChildDefaultParametersManager().registerDefaultsHandler(paramClass,
                handler, startClass);
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
        final Map<String, Object> params = super.getParameters();
        params.put(PARAM_KEY, this);
        return params;
    }

    /**
     * {@inheritDoc} This implementation also clones the parameters object for
     * the definition builder if possible.
     */
    @Override
    public CombinedBuilderParametersImpl clone()
    {
        final CombinedBuilderParametersImpl copy =
                (CombinedBuilderParametersImpl) super.clone();
        copy.setDefinitionBuilderParameters((BuilderParameters) ConfigurationUtils
                .cloneIfPossible(getDefinitionBuilderParameters()));
        return copy;
    }
}
