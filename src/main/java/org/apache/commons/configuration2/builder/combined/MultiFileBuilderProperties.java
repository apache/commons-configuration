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

import org.apache.commons.configuration2.builder.BuilderParameters;

/**
 * <p>
 * Definition of a properties interface for the parameters of a multiple file
 * configuration builder.
 * </p>
 * <p>
 * This interface defines a number of properties for configuring a builder
 * managing multiple file-based configurations which are selected by a pattern.
 * Properties can be set in a fluent style.
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
public interface MultiFileBuilderProperties<T>
{
    /**
     * Sets the pattern string. Based on this pattern the configuration file to
     * be loaded is determined.
     *
     * @param p the pattern string
     * @return a reference to this object for method chaining
     */
    T setFilePattern(String p);

    /**
     * Sets a parameters object to be used when creating a managed
     * configuration. These parameters configure sub configurations.
     *
     * @param p the parameters object for a sub configuration
     * @return a reference to this object for method chaining
     */
    T setManagedBuilderParameters(BuilderParameters p);
}
