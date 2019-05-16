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

package org.apache.commons.configuration2;

import java.util.HashMap;

/**
 * <p>A Configuration implementation that reads the platform specific
 * environment variables using the map returned by {@link System#getenv()}.</p>
 *
 * <p>This configuration implementation is read-only. It allows read access to the
 * defined OS environment variables, but their values cannot be changed. Any
 * attempts to add or remove a property will throw an
 * {@link UnsupportedOperationException}</p>
 *
 * <p>Usage of this class is easy: After an instance has been created the get
 * methods provided by the {@code Configuration} interface can be used
 * for querying environment variables, e.g.:</p>
 *
 * <pre>
 * Configuration envConfig = new EnvironmentConfiguration();
 * System.out.println("JAVA_HOME=" + envConfig.getString("JAVA_HOME");
 * </pre>
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @since 1.5
 */
public class EnvironmentConfiguration extends MapConfiguration
{
    /**
     * Create a Configuration based on the environment variables.
     *
     * @see System#getenv()
     */
    public EnvironmentConfiguration()
    {
        super(new HashMap<String, Object>(System.getenv()));
    }

    /**
     * Adds a property to this configuration. Because this configuration is
     * read-only, this operation is not allowed and will cause an exception.
     *
     * @param key the key of the property to be added
     * @param value the property value
     */
    @Override
    protected void addPropertyDirect(final String key, final Object value)
    {
        throw new UnsupportedOperationException("EnvironmentConfiguration is read-only!");
    }

    /**
     * Removes a property from this configuration. Because this configuration is
     * read-only, this operation is not allowed and will cause an exception.
     *
     * @param key the key of the property to be removed
     */
    @Override
    protected void clearPropertyDirect(final String key)
    {
        throw new UnsupportedOperationException("EnvironmentConfiguration is read-only!");
    }

    /**
     * Removes all properties from this configuration. Because this
     * configuration is read-only, this operation is not allowed and will cause
     * an exception.
     */
    @Override
    protected void clearInternal()
    {
        throw new UnsupportedOperationException("EnvironmentConfiguration is read-only!");
    }
}
