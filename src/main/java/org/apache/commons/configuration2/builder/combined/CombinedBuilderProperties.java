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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.DefaultParametersManager;

/**
 * <p>
 * Definition of a properties interface for the parameters of a combined
 * configuration builder.
 * </p>
 * <p>
 * This interface defines a number of properties for adapting the construction
 * of a combined configuration based on a definition configuration. Properties
 * can be set in a fluent style.
 * </p>
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.0
 * @param <T> the return type of all methods for allowing method chaining
 */
public interface CombinedBuilderProperties<T>
{
    /**
     * Sets a flag whether the child configurations created by a
     * {@code CombinedConfigurationBuilder} should inherit the settings defined
     * for the builder. This is typically useful because for configurations
     * coming from homogeneous sources often similar conventions are used.
     * Therefore, this flag is <b>true</b> per default.
     *
     * @param f the flag whether settings should be inherited by child
     *        configurations
     * @return a reference to this object for method chaining
     */
    T setInheritSettings(boolean f);

    /**
     * Sets the {@code ConfigurationBuilder} for the definition configuration.
     * This is the configuration which contains the configuration sources that
     * form the combined configuration.
     *
     * @param builder the definition {@code ConfigurationBuilder}
     * @return a reference to this object for method chaining
     */
    T setDefinitionBuilder(
            ConfigurationBuilder<? extends HierarchicalConfiguration<?>> builder);

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
    T registerProvider(String tagName, ConfigurationBuilderProvider provider);

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
    T setBasePath(String path);

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
    T setDefinitionBuilderParameters(BuilderParameters params);

    /**
     * Sets a {@code DefaultParametersManager} object responsible for managing the default
     * parameter handlers to be applied on child configuration sources. When creating
     * builders for child configuration sources their parameters are initialized using
     * this {@code DefaultParametersManager} instance. This way, meaningful defaults can
     * be set. Note that calling this method overrides all
     * {@code DefaultParametersHandler} objects previously set by one of the
     * {@code registerChildDefaultsHandler()} methods! So either use this method if a
     * pre-configured manager object is to be set or call the
     * {@code registerChildDefaultHandler()} methods with the handlers to be registered
     * (in the latter case, it is not necessary to set a {@code DefaultParametersManager}
     * explicitly; a default one is created behind the scenes).
     *
     * @param manager the {@code DefaultParametersManager}
     * @return a reference to this object for method chaining
     */
    T setChildDefaultParametersManager(DefaultParametersManager manager);

    /**
     * Registers a {@code DefaultParametersHandler} for child configuration sources. With
     * this method an arbitrary number of handler objects can be set. When creating
     * builders for child configuration sources their parameters are initialized by
     * invoking all matching {@code DefaultParametersHandler}s on them. So, basically the
     * same mechanism is used for the initialization of parameters for child configuration
     * sources as for normal parameter objects.
     *
     * @param <D> the type of the handler to be registered
     * @param paramClass the parameter class supported by the handler
     * @param handler the {@code DefaultParametersHandler} to be registered
     * @return a reference to this object for method chaining
     * @see DefaultParametersManager#registerDefaultsHandler(Class,
     * DefaultParametersHandler)
     */
    <D> T registerChildDefaultsHandler(Class<D> paramClass,
            DefaultParametersHandler<? super D> handler);

    /**
     * Registers a {@code DefaultParametersHandler} for child configuration sources
     * derived from the given start class. This method works like the overloaded variant,
     * but limits the application of the defaults handler to specific child configuration
     * sources.
     *
     * @param <D> the type of the handler to be registered
     * @param paramClass the parameter class supported by the handler
     * @param handler the {@code DefaultParametersHandler} to be registered
     * @param startClass an optional start class in the hierarchy of parameter objects for
     * which this handler should be applied
     * @return a reference to this object for method chaining
     * @see DefaultParametersManager#registerDefaultsHandler(Class,
     * DefaultParametersHandler, Class)
     */
    <D> T registerChildDefaultsHandler(Class<D> paramClass,
            DefaultParametersHandler<? super D> handler, Class<?> startClass);
}
