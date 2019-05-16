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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * <p>
 * A fully-functional, reflection-based implementation of the
 * {@code ConfigurationBuilderProvider} interface which can deal with the
 * default tags defining configuration sources.
 * </p>
 * <p>
 * An instance of this class is initialized with the names of the
 * {@code ConfigurationBuilder} class used by this provider and the concrete
 * {@code Configuration} class. The {@code ConfigurationBuilder} class must be
 * derived from {@link BasicConfigurationBuilder}. When asked for the builder
 * object, an instance of the builder class is created and initialized from the
 * bean declaration associated with the current configuration source.
 * </p>
 * <p>
 * {@code ConfigurationBuilder} objects are configured using parameter objects.
 * When declaring configuration sources in XML it should not be necessary to
 * define the single parameter objects. Rather, simple and complex properties
 * are set in the typical way of a bean declaration (i.e. as attributes of the
 * current XML element or as child elements). This class creates all supported
 * parameter objects (whose names also must be provided at construction time)
 * and takes care that their properties are initialized according to the current
 * bean declaration.
 * </p>
 * <p>
 * The use of reflection to create builder instances allows a generic
 * implementation supporting many concrete builder classes. Another reason for
 * this approach is that builder classes are only loaded if actually needed.
 * Some specialized {@code Configuration} implementations require specific
 * external dependencies which should not be mandatory for the use of
 * {@code CombinedConfigurationBuilder}. Because such classes are lazily loaded,
 * an application only has to include the dependencies it actually uses.
 * </p>
 *
 * @since 2.0
 */
public class BaseConfigurationBuilderProvider implements
        ConfigurationBuilderProvider
{
    /** The types of the constructor parameters for a basic builder. */
    private static final Class<?>[] CTOR_PARAM_TYPES = {
            Class.class, Map.class, Boolean.TYPE
    };

    /** The name of the builder class. */
    private final String builderClass;

    /** The name of a builder class with reloading support. */
    private final String reloadingBuilderClass;

    /** Stores the name of the configuration class to be created. */
    private final String configurationClass;

    /** A collection with the names of parameter classes. */
    private final Collection<String> parameterClasses;

    /**
     * Creates a new instance of {@code BaseConfigurationBuilderProvider} and
     * initializes all its properties.
     *
     * @param bldrCls the name of the builder class (must not be <b>null</b>)
     * @param reloadBldrCls the name of a builder class to be used if reloading
     *        support is required (<b>null</b> if reloading is not supported)
     * @param configCls the name of the configuration class (must not be
     *        <b>null</b>)
     * @param paramCls a collection with the names of parameters classes
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public BaseConfigurationBuilderProvider(final String bldrCls,
            final String reloadBldrCls, final String configCls, final Collection<String> paramCls)
    {
        if (bldrCls == null)
        {
            throw new IllegalArgumentException(
                    "Builder class must not be null!");
        }
        if (configCls == null)
        {
            throw new IllegalArgumentException(
                    "Configuration class must not be null!");
        }

        builderClass = bldrCls;
        reloadingBuilderClass = reloadBldrCls;
        configurationClass = configCls;
        parameterClasses = initParameterClasses(paramCls);
    }

    /**
     * Returns the name of the class of the builder created by this provider.
     *
     * @return the builder class
     */
    public String getBuilderClass()
    {
        return builderClass;
    }

    /**
     * Returns the name of the class of the builder created by this provider if
     * the reload flag is set. If this method returns <b>null</b>, reloading
     * builders are not supported by this provider.
     *
     * @return the reloading builder class
     */
    public String getReloadingBuilderClass()
    {
        return reloadingBuilderClass;
    }

    /**
     * Returns the name of the configuration class created by the builder
     * produced by this provider.
     *
     * @return the configuration class
     */
    public String getConfigurationClass()
    {
        return configurationClass;
    }

    /**
     * Returns an unmodifiable collection with the names of parameter classes
     * supported by this provider.
     *
     * @return the parameter classes
     */
    public Collection<String> getParameterClasses()
    {
        return parameterClasses;
    }

    /**
     * {@inheritDoc} This implementation delegates to some protected methods to
     * create a new builder instance using reflection and to configure it with
     * parameter values defined by the passed in {@code BeanDeclaration}.
     */
    @Override
    public ConfigurationBuilder<? extends Configuration> getConfigurationBuilder(
            final ConfigurationDeclaration decl) throws ConfigurationException
    {
        try
        {
            final Collection<BuilderParameters> params = createParameterObjects();
            initializeParameterObjects(decl, params);
            final BasicConfigurationBuilder<? extends Configuration> builder =
                    createBuilder(decl, params);
            configureBuilder(builder, decl, params);
            return builder;
        }
        catch (final ConfigurationException cex)
        {
            throw cex;
        }
        catch (final Exception ex)
        {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Determines the <em>allowFailOnInit</em> flag for the newly created
     * builder based on the given {@code ConfigurationDeclaration}. Some
     * combinations of flags in the declaration say that a configuration source
     * is optional, but an empty instance should be created if its creation
     * fail.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @return the value of the <em>allowFailOnInit</em> flag
     */
    protected boolean isAllowFailOnInit(final ConfigurationDeclaration decl)
    {
        return decl.isOptional() && decl.isForceCreate();
    }

    /**
     * Creates a collection of parameter objects to be used for configuring the
     * builder. This method creates instances of the parameter classes passed to
     * the constructor.
     *
     * @return a collection with parameter objects for the builder
     * @throws Exception if an error occurs while creating parameter objects via
     *         reflection
     */
    protected Collection<BuilderParameters> createParameterObjects()
            throws Exception
    {
        final Collection<BuilderParameters> params =
                new ArrayList<>(
                        getParameterClasses().size());
        for (final String paramcls : getParameterClasses())
        {
            params.add(createParameterObject(paramcls));
        }
        return params;
    }

    /**
     * Initializes the parameter objects with data stored in the current bean
     * declaration. This method is called before the newly created builder
     * instance is configured with the parameter objects. It maps attributes of
     * the bean declaration to properties of parameter objects. In addition,
     * it invokes the parent {@code CombinedConfigurationBuilder} so that the
     * parameters object can inherit properties already defined for this
     * builder.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @param params the collection with (uninitialized) parameter objects
     * @throws Exception if an error occurs
     */
    protected void initializeParameterObjects(final ConfigurationDeclaration decl,
            final Collection<BuilderParameters> params) throws Exception
    {
        inheritParentBuilderProperties(decl, params);
        final MultiWrapDynaBean wrapBean = new MultiWrapDynaBean(params);
        decl.getConfigurationBuilder().initBean(wrapBean, decl);
    }

    /**
     * Passes all parameter objects to the parent
     * {@code CombinedConfigurationBuilder} so that properties already defined
     * for the parent builder can be added. This method is called before the
     * parameter objects are initialized from the definition configuration. This
     * way properties from the parent builder are inherited, but can be
     * overridden for child configurations.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @param params the collection with (uninitialized) parameter objects
     */
    protected void inheritParentBuilderProperties(
            final ConfigurationDeclaration decl, final Collection<BuilderParameters> params)
    {
        for (final BuilderParameters p : params)
        {
            decl.getConfigurationBuilder().initChildBuilderParameters(p);
        }
    }

    /**
     * Creates a new, uninitialized instance of the builder class managed by
     * this provider. This implementation determines the builder class to be
     * used by delegating to {@code determineBuilderClass()}. It then calls the
     * constructor expecting the configuration class, the map with properties,
     * and the<em>allowFailOnInit</em> flag.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @param params initialization parameters for the new builder object
     * @return the newly created builder instance
     * @throws Exception if an error occurs
     */
    protected BasicConfigurationBuilder<? extends Configuration> createBuilder(
            final ConfigurationDeclaration decl, final Collection<BuilderParameters> params)
            throws Exception
    {
        final Class<?> bldCls =
                ConfigurationUtils.loadClass(determineBuilderClass(decl));
        final Class<?> configCls =
                ConfigurationUtils.loadClass(determineConfigurationClass(decl,
                        params));
        final Constructor<?> ctor = bldCls.getConstructor(CTOR_PARAM_TYPES);
        // ? extends Configuration is the minimum constraint
        @SuppressWarnings("unchecked")
        final
        BasicConfigurationBuilder<? extends Configuration> builder =
                (BasicConfigurationBuilder<? extends Configuration>) ctor
                        .newInstance(configCls, null, isAllowFailOnInit(decl));
        return builder;
    }

    /**
     * Configures a newly created builder instance with its initialization
     * parameters. This method is called after a new instance was created using
     * reflection. This implementation passes the parameter objects to the
     * builder's {@code configure()} method.
     *
     * @param builder the builder to be initialized
     * @param decl the current {@code ConfigurationDeclaration}
     * @param params the collection with initialization parameter objects
     * @throws Exception if an error occurs
     */
    protected void configureBuilder(
            final BasicConfigurationBuilder<? extends Configuration> builder,
            final ConfigurationDeclaration decl, final Collection<BuilderParameters> params)
            throws Exception
    {
        builder.configure(params.toArray(new BuilderParameters[params.size()]));
    }

    /**
     * Determines the name of the class to be used for a new builder instance.
     * This implementation selects between the normal and the reloading builder
     * class, based on the passed in {@code ConfigurationDeclaration}. If a
     * reloading builder is desired, but this provider has no reloading support,
     * an exception is thrown.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @return the name of the builder class
     * @throws ConfigurationException if the builder class cannot be determined
     */
    protected String determineBuilderClass(final ConfigurationDeclaration decl)
            throws ConfigurationException
    {
        if (decl.isReload())
        {
            if (getReloadingBuilderClass() == null)
            {
                throw new ConfigurationException(
                        "No support for reloading for builder class "
                                + getBuilderClass());
            }
            return getReloadingBuilderClass();
        }
        return getBuilderClass();
    }

    /**
     * Determines the name of the configuration class produced by the builder.
     * This method is called when obtaining the arguments for invoking the
     * constructor of the builder class. This implementation just returns the
     * pre-configured configuration class name. Derived classes may determine
     * this class name dynamically based on the passed in parameters.
     *
     * @param decl the current {@code ConfigurationDeclaration}
     * @param params the collection with parameter objects
     * @return the name of the builder's result configuration class
     * @throws ConfigurationException if an error occurs
     */
    protected String determineConfigurationClass(final ConfigurationDeclaration decl,
            final Collection<BuilderParameters> params) throws ConfigurationException
    {
        return getConfigurationClass();
    }

    /**
     * Creates an instance of a parameter class using reflection.
     *
     * @param paramcls the parameter class
     * @return the newly created instance
     * @throws Exception if an error occurs
     */
    private static BuilderParameters createParameterObject(final String paramcls)
            throws Exception
    {
        final Class<?> cls = ConfigurationUtils.loadClass(paramcls);
        final BuilderParameters p = (BuilderParameters) cls.newInstance();
        return p;
    }

    /**
     * Creates a new, unmodifiable collection for the parameter classes.
     *
     * @param paramCls the collection with parameter classes passed to the
     *        constructor
     * @return the collection to be stored
     */
    private static Collection<String> initParameterClasses(
            final Collection<String> paramCls)
    {
        if (paramCls == null)
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(new ArrayList<>(
                paramCls));
    }
}
