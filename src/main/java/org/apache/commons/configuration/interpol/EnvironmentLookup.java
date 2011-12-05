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
package org.apache.commons.configuration.interpol;

import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.lang.text.StrLookup;

/**
 * <p>
 * A specialized lookup implementation that allows access to environment
 * variables.
 * </p>
 * <p>
 * This implementation relies on {@link EnvironmentConfiguration} to resolve
 * environment variables. It can be used for referencing environment variables
 * in configuration files in an easy way, for instance:
 *
 * <pre>
 * java.home = ${env:JAVA_HOME}
 * </pre>
 *
 * </p>
 * <p>
 * {@code EnvironmentLookup} is one of the standard lookups that is
 * registered per default for each configuration.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @since 1.7
 * @version $Id$
 */
public class EnvironmentLookup extends StrLookup
{
    /** Stores the underlying {@code EnvironmentConfiguration}. */
    private final EnvironmentConfiguration environmentConfig = new EnvironmentConfiguration();

    /**
     * Performs a lookup for the specified variable. This implementation
     * directly delegates to a {@code EnvironmentConfiguration}.
     *
     * @param key the key to lookup
     * @return the value of this key or <b>null</b> if it cannot be resolved
     */
    @Override
    public String lookup(String key)
    {
        return environmentConfig.getString(key);
    }
}
