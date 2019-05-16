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

import javax.naming.Context;

/**
 * <p>
 * Definition of a properties interface for parameters of a JNDI configuration.
 * </p>
 * <p>
 * This interface defines properties related to the JNDI tree to be represented
 * by a {@code JNDIConfiguration}.
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
public interface JndiBuilderProperties<T>
{
    /**
     * Sets the JNDI context to be used by the JNDI configuration.
     *
     * @param ctx the JNDI {@code Context}
     * @return a reference to this object for method chaining
     */
    T setContext(Context ctx);

    /**
     * Sets the prefix in the JNDI tree. When creating the root JNDI context
     * this prefix is taken into account.
     *
     * @param p the prefix
     * @return a reference to this object for method chaining
     */
    T setPrefix(String p);
}
