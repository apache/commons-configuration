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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.ConfigurationBuilder;

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
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the return type of all methods for allowing method chaining
 */
public interface CombinedBuilderProperties<T>
{
    /**
     * Sets the {@code ConfigurationBuilder} for the definition configuration.
     * This is the configuration which contains the configuration sources that
     * form the combined configuration.
     *
     * @param builder the definition {@code ConfigurationBuilder}
     * @return a reference to this object for method chaining
     */
    T setDefinitionBuilder(
            ConfigurationBuilder<? extends HierarchicalConfiguration> builder);

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
}
