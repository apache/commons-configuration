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

import org.apache.commons.configuration2.tree.ExpressionEngine;

/**
 * <p>
 * Definition of a parameters interface for hierarchical configurations.
 * </p>
 * <p>
 * This interface defines set methods for additional properties common to all
 * hierarchical configurations.
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
public interface HierarchicalBuilderProperties<T>
{
    /**
     * Sets the {@code ExpressionEngine} to be used when querying the
     * configuration.
     *
     * @param engine the {@code ExpressionEngine}
     * @return a reference to this object for method chaining
     */
    T setExpressionEngine(ExpressionEngine engine);
}
