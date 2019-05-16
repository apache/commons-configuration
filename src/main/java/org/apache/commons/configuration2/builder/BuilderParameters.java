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

import java.util.Map;

/**
 * <p>
 * An interface to be implemented by objects which can be used to parameterize a
 * {@link ConfigurationBuilder}.
 * </p>
 * <p>
 * This interface is part of a Java DSL for creating and initializing builders
 * for specific {@code Configuration} classes. Concrete implementations
 * typically collect a set of related properties for the builder. There will be
 * specific set methods for providing values for these properties. Then, this
 * interface requires a generic {@code getParameters()} method which has to
 * return all property values as a map. When constructing the builder the map is
 * evaluated to define properties of the {@code Configuration} objects to be
 * constructed.
 * </p>
 *
 * @since 2.0
 */
public interface BuilderParameters
{
    /**
     * Constant for a prefix for reserved initialization parameter keys. If a
     * parameter was set whose key starts with this prefix, it is filtered out
     * before the initialization of a newly created result object. This
     * mechanism allows implementing classes to store specific configuration
     * data in the parameters map which does not represent a property value for
     * the result object.
     */
    String RESERVED_PARAMETER_PREFIX = "config-";

    /**
     * Returns a map with all parameters defined by this objects. The keys of
     * the map correspond to concrete properties supported by the
     * {@code Configuration} implementation class the builder produces. The
     * values are the corresponding property values. The return value must not
     * be <b>null</b>.
     *
     * @return a map with builder parameters
     */
    Map<String, Object> getParameters();
}
