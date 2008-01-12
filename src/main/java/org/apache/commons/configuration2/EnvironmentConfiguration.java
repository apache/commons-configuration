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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.apache.tools.ant.taskdefs.Execute;

/**
 * <p>
 * A Configuration implementation that reads the platform specific environment
 * variables. On pre java5 JRE it uses Ant Execute task to read the environment.
 * (in this case ant must be present in classpath). On java >= 5 JRE it uses
 * {@link java.lang.System#getenv()} and ant is not required.
 * </p>
 * <p>
 * This configuration implementation is read-only. It allows read access to the
 * defined OS environment variables, but their values cannot be changed.
 * </p>
 * <p>
 * Usage of this class is easy: After an instance has been created the get
 * methods provided by the <code>Configuration</code> interface can be used
 * for querying environment variables, e.g.:
 *
 * <pre>
 * Configuration envConfig = new EnvironmentConfiguration();
 * System.out.println("JAVA_HOME=" + envConfig.getString("JAVA_HOME");
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * @see org.apache.tools.ant.taskdefs.Execute#getProcEnvironment()
 * @since 1.5
 */
public class EnvironmentConfiguration extends AbstractConfiguration
{
    /** Constant for the name of the getenv() method. */
    private static final String METHOD_NAME = "getenv";

    /** Constant for the Java version 1.5. */
    private static final int VERSION_1_5 = 150;

    /** Stores the environment properties. */
    private Map environment;

    /**
     * Constructor.
     */
    public EnvironmentConfiguration()
    {
        if (SystemUtils.isJavaVersionAtLeast(VERSION_1_5))
        {
            extractProperties15();
        }
        else
        {
            extractProperties14();
        }
    }

    /**
     * Adds a property to this configuration. Because this configuration is
     * read-only, this operation is not allowed and will cause an exception.
     *
     * @param key the key of the property to be added
     * @param value the property value
     */
    protected void addPropertyDirect(String key, Object value)
    {
        throw new UnsupportedOperationException("Configuration is read-only!");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#containsKey(java.lang.String)
     */
    public boolean containsKey(String key)
    {
        return environment.containsKey(key);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#getKeys()
     */
    public Iterator getKeys()
    {
        return environment.keySet().iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#getProperty(java.lang.String)
     */
    public Object getProperty(String key)
    {
        return environment.get(key);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.configuration2.AbstractConfiguration#isEmpty()
     */
    public boolean isEmpty()
    {
        return environment.isEmpty();
    }

    /**
     * Removes a property from this configuration. Because this configuration is
     * read-only, this operation is not allowed and will cause an exception.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        throw new UnsupportedOperationException("Configuration is read-only!");
    }

    /**
     * Removes all properties from this configuration. Because this
     * configuration is read-only, this operation is not allowed and will cause
     * an exception.
     */
    public void clear()
    {
        throw new UnsupportedOperationException("Configuration is read-only!");
    }

    /**
     * Extracts environment properties on a JRE &lt; 1.5. This implementation
     * uses ant for this purpose.
     */
    void extractProperties14()
    {
        extractPropertiesFromCollection(Execute.getProcEnvironment());
    }

    /**
     * An internally used method for processing a collection with environment
     * entries. The collection must contain strings like
     * <code>property=value</code>. Such a collection is returned by ant.
     *
     * @param env the collection with the properties
     */
    void extractPropertiesFromCollection(Collection env)
    {
        environment = new HashMap();
        for (Iterator it = env.iterator(); it.hasNext();)
        {
            String entry = (String) it.next();
            int pos = entry.indexOf('=');
            if (pos == -1)
            {
                getLogger().warn("Ignoring: " + entry);
            }
            else
            {
                environment.put(entry.substring(0, pos), entry
                        .substring(pos + 1));
            }
        }
    }

    /**
     * Extracts environment properties on a JR &gt;= 1.5. From this Java version
     * on, there is an official way of doing this. However because the code
     * should compile on lower Java versions, too, we have to invoke the method
     * using reflection.
     */
    void extractProperties15()
    {
        try
        {
            Method method = System.class.getMethod(METHOD_NAME, null);
            environment = (Map) method.invoke(null, null);
        }
        catch (Exception ex)
        {
            // this should normally not happen on a JRE >= 1.5
            throw new ConfigurationRuntimeException(
                    "Error when accessing environment properties", ex);
        }
    }
}
