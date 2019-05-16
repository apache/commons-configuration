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
package org.apache.commons.configuration2.builder;

import org.apache.commons.configuration2.PropertiesConfiguration.IOFactory;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;

/**
 * <p>
 * Definition of a parameters interface for properties configurations.
 * </p>
 * <p>
 * This interface defines additional properties which can be set when
 * initializing a {@code PropertiesConfiguration} object.
 * </p>
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface PropertiesBuilderProperties<T>
{
    /**
     * Sets a flag whether include files are supported by the properties
     * configuration object. If set to <b>true</b>, files listed by an include
     * property are loaded automatically.
     *
     * @param f the value of the flag
     * @return a reference to this object for method chaining
     */
    T setIncludesAllowed(boolean f);

    /**
     * Sets the layout object for the properties configuration object. With this
     * method a custom layout object can be set. If no layout is provided, the
     * configuration will use a default layout.
     *
     * @param layout the {@code PropertiesConfigurationLayout} object to be used
     *        by the configuration
     * @return a reference to this object for method chaining
     */
    T setLayout(PropertiesConfigurationLayout layout);

    /**
     * Sets the {@code IOFactory} to be used by the properties configuration
     * object. With this method a custom factory for input and output streams
     * can be set. This allows customizing the format of properties read or
     * written by the configuration. If no {@code IOFactory} is provided, the
     * configuration uses a default one.
     *
     * @param factory the {@code IOFactory} to be used by the configuration
     * @return a reference to this object for method chaining
     */
    T setIOFactory(IOFactory factory);
}
