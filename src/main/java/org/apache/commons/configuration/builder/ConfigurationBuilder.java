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
package org.apache.commons.configuration.builder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ex.ConfigurationException;

/**
 * <p>
 * Definition of an interface for objects that can create {@link Configuration}
 * objects of a specific type.
 * </p>
 * <p>
 * This interface defines an abstract way of creating a {@code Configuration}
 * object. It does not assume any specific way of how this is done; this is
 * completely in the responsibility of an implementation class. There is just a
 * single method that returns the configuration constructed by this builder.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the concrete type of the {@code Configuration} class produced by
 *        this builder
 */
public interface ConfigurationBuilder<T extends Configuration>
{
    /**
     * Returns the configuration provided by this builder. An implementation has
     * to perform all necessary steps for creating and initializing a
     * {@code Configuration} object.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    T getConfiguration() throws ConfigurationException;

    /**
     * Adds the specified {@code BuilderListener} to this builder.
     *
     * @param l the listener to be registered
     */
    void addBuilderListener(BuilderListener l);

    /**
     * Removes the specified {@code BuilderListener} from this builder.
     *
     * @param l the listener to be removed
     */
    void removeBuilderListener(BuilderListener l);
}
