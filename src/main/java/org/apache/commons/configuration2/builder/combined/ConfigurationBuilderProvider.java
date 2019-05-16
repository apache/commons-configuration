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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * <p>
 * An interface for creating builders for configuration sources of a
 * {@link CombinedConfigurationBuilder}.
 * </p>
 * <p>
 * When processing its definition file {@code CombinedConfigurationBuilder}
 * scans for tags declaring configuration sources and maps them to
 * implementations of this interface. The instances are then used to obtain
 * builder objects to create the corresponding configuration sources. Parameters
 * of the builders are provided as {@link ConfigurationDeclaration} objects.
 * </p>
 *
 * @since 2.0
 */
public interface ConfigurationBuilderProvider
{
    /**
     * Returns the builder for the configuration source managed by this
     * provider. This method is called during processing of the combined
     * configuration definition file.
     *
     * @param decl the bean declaration with initialization parameters for the
     *        configuration builder
     * @return the {@code ConfigurationBuilder} object created by this provider
     * @throws ConfigurationException if an error occurs
     */
    ConfigurationBuilder<? extends Configuration> getConfigurationBuilder(
            ConfigurationDeclaration decl)
            throws ConfigurationException;
}
