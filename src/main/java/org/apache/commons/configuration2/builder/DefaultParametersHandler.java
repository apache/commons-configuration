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
 * Definition of an interface for setting default values for specific
 * configuration parameter objects.
 * </p>
 * <p>
 * An object implementing this interface knows how to initialize a parameters
 * object of a specific class with default values. Such objects can be
 * registered at the {@link org.apache.commons.configuration2.builder.fluent.Parameters
 * Parameters} class. Whenever a specific parameters
 * object is created all registered {@code DefaultParametersHandler} objects
 * that can handle this parameters type are invoked, so that they get the chance
 * to perform arbitrary initialization.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of parameters supported by this handler
 */
public interface DefaultParametersHandler<T>
{
    /**
     * Initializes the specified parameters object with default values. This
     * method is called after the parameters object was created and before it is
     * passed to the calling code. A concrete implementation can perform
     * arbitrary initializations. Note that if there are multiple
     * {@code DefaultParametersHandler} objects registered supporting this
     * parameters type they are called in the order they have been registered.
     * So handlers registered later can override initializations done by
     * handlers registered earlier.
     *
     * @param parameters the parameters object to be initialized
     */
    void initializeDefaults(T parameters);
}
