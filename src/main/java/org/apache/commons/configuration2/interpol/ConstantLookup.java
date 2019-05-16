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
package org.apache.commons.configuration2.interpol;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A specialized lookup implementation that allows access to constant fields of
 * classes.
 * </p>
 * <p>
 * Sometimes it is necessary in a configuration file to refer to a constant
 * defined in a class. This can be done with this lookup implementation.
 * Variable names passed in must be of the form
 * {@code mypackage.MyClass.FIELD}. The {@code lookup()} method
 * will split the passed in string at the last dot, separating the fully
 * qualified class name and the name of the constant (i.e. <strong>static final</strong>)
 * member field. Then the class is loaded and the field's value is obtained
 * using reflection.
 * </p>
 * <p>
 * Once retrieved values are cached for fast access. This class is thread-safe.
 * It can be used as a standard (i.e. global) lookup object and serve multiple
 * clients concurrently.
 * </p>
 *
 * @since 1.4
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class ConstantLookup implements Lookup
{
    /** Constant for the field separator. */
    private static final char FIELD_SEPRATOR = '.';

    /** An internally used cache for already retrieved values. */
    private static Map<String, Object> constantCache = new HashMap<>();

    /** The logger. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Tries to resolve the specified variable. The passed in variable name is
     * interpreted as the name of a <b>static final</b> member field of a
     * class. If the value has already been obtained, it can be retrieved from
     * an internal cache. Otherwise this method will invoke the
     * {@code resolveField()} method and pass in the name of the class
     * and the field.
     *
     * @param var the name of the variable to be resolved
     * @return the value of this variable or <b>null</b> if it cannot be
     * resolved
     */
    @Override
    public Object lookup(final String var)
    {
        if (var == null)
        {
            return null;
        }

        Object result;
        synchronized (constantCache)
        {
            result = constantCache.get(var);
        }
        if (result != null)
        {
            return result;
        }

        final int fieldPos = var.lastIndexOf(FIELD_SEPRATOR);
        if (fieldPos < 0)
        {
            return null;
        }
        try
        {
            final Object value = resolveField(var.substring(0, fieldPos), var
                    .substring(fieldPos + 1));
            if (value != null)
            {
                synchronized (constantCache)
                {
                    // In worst case, the value will be fetched multiple times
                    // because of this lax synchronization, but for constant
                    // values this shouldn't be a problem.
                    constantCache.put(var, value);
                }
                result = value;
            }
        }
        catch (final Exception ex)
        {
            log.warn("Could not obtain value for variable " + var, ex);
        }

        return result;
    }

    /**
     * Clears the shared cache with the so far resolved constants.
     */
    public static void clear()
    {
        synchronized (constantCache)
        {
            constantCache.clear();
        }
    }

    /**
     * Determines the value of the specified constant member field of a class.
     * This implementation will call {@code fetchClass()} to obtain the
     * {@code java.lang.Class} object for the target class. Then it will
     * use reflection to obtain the field's value. For this to work the field
     * must be accessable.
     *
     * @param className the name of the class
     * @param fieldName the name of the member field of that class to read
     * @return the field's value
     * @throws Exception if an error occurs
     */
    protected Object resolveField(final String className, final String fieldName)
            throws Exception
    {
        final Class<?> clazz = fetchClass(className);
        final Field field = clazz.getField(fieldName);
        return field.get(null);
    }

    /**
     * Loads the class with the specified name. If an application has special
     * needs regarding the class loaders to be used, it can hook in here. This
     * implementation delegates to the {@code getClass()} method of
     * Commons Lang's
     * <code><a href="http://commons.apache.org/lang/api-release/org/apache/commons/lang/ClassUtils.html">
     * ClassUtils</a></code>.
     *
     * @param className the name of the class to be loaded
     * @return the corresponding class object
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    protected Class<?> fetchClass(final String className) throws ClassNotFoundException
    {
        return ClassUtils.getClass(className);
    }
}
