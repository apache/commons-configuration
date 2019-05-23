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

/**
 * <p>
 * Definition of a parameters interface for INI configurations.
 * </p>
 * <p>
 * The {@code INIConfiguration} class defines a bunch of additional properties
 * related to INI processing.
 * </p>
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.2
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface INIBuilderProperties<T>
{

    /**
     * Allows setting the leading comment separator which is used in reading an INI
     * file.
     *
     * @param separator String of the new separator for INI reading
     * @return a reference to this object for method chaining
     * @since 2.5
     */
    default T setCommentLeadingCharsUsedInInput(final String separator)
    {
        // NoOp
        return (T) this;
    }

    /**
     * Allows setting the key and value separator which is used in reading an INI
     * file.
     *
     * @param separator String of the new separator for INI reading
     * @return a reference to this object for method chaining
     * @since 2.5
     */
    default T setSeparatorUsedInInput(final String separator)
    {
        // NoOp
        return (T) this;
    }

    /**
     * Allows setting the separator between key and value to be used when writing an
     * INI file.
     *
     * @param separator the new separator for INI output
     * @return a reference to this object for method chaining
     */
    T setSeparatorUsedInOutput(String separator);
}
