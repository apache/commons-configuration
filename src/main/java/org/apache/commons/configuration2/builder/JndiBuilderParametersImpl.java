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
 * A specialized parameters object for JNDI configurations.
 * </p>
 * <p>
 * In addition to the basic properties common to all configuration
 * implementations, a JNDI configuration has some special properties defining
 * the subset of the JNDI tree to be managed. This class provides fluent methods
 * for setting these. The {@code getParameters()} method puts all properties
 * defined by the user in a map from where they can be accessed by a builder for
 * JNDI configurations.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @since 2.0
 */
public class JndiBuilderParametersImpl extends BasicBuilderParameters implements
        JndiBuilderProperties<JndiBuilderParametersImpl>
{
    /** Constant for the name of the context property. */
    private static final String PROP_CONTEXT = "context";

    /** Constant for the name of the prefix property. */
    private static final String PROP_PREFIX = "prefix";

    @Override
    public JndiBuilderParametersImpl setContext(final Context ctx)
    {
        storeProperty(PROP_CONTEXT, ctx);
        return this;
    }

    @Override
    public JndiBuilderParametersImpl setPrefix(final String p)
    {
        storeProperty(PROP_PREFIX, p);
        return this;
    }
}
