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
package org.apache.commons.configuration.builder.fluent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.configuration.builder.BasicBuilderParameters;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.DatabaseBuilderParametersImpl;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.HierarchicalBuilderParametersImpl;
import org.apache.commons.configuration.builder.JndiBuilderParametersImpl;
import org.apache.commons.configuration.builder.PropertiesBuilderParametersImpl;
import org.apache.commons.configuration.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.CombinedBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.MultiFileBuilderParametersImpl;

/**
 * <p>
 * A convenience class for creating parameter objects for initializing
 * configuration builder objects.
 * </p>
 * <p>
 * For setting initialization properties of new configuration objects, a number
 * of specialized parameter classes exists. These classes use inheritance to
 * organize the properties they support in a logic way. For instance, parameters
 * for file-based configurations also support the basic properties common to all
 * configuration implementations, parameters for XML configurations also include
 * file-based and basic properties, etc.
 * </p>
 * <p>
 * When constructing a configuration builder, an easy-to-use fluent API is
 * desired to define specific properties for the configuration to be created.
 * However, the inheritance structure of the parameter classes makes it
 * surprisingly difficult to provide such an API. This class comes to rescue by
 * defining a set of methods for the creation of interface-based parameter
 * objects offering a truly fluent API. The methods provided can be
 * called directly when setting up a configuration builder as shown in the
 * following example code fragment:
 *
 * <pre>
 * Parameters params = new Parameters();
 * configurationBuilder.configure(params.fileBased()
 *         .setThrowExceptionOnMissing(true).setEncoding(&quot;UTF-8&quot;)
 *         .setListDelimiter('#').setFileName(&quot;test.xml&quot;));
 * </pre>
 *
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public final class Parameters
{
    /**
     * Creates a new instance of a parameters object for basic configuration
     * properties.
     *
     * @return the new parameters object
     */
    public BasicBuilderParameters basic()
    {
        return new BasicBuilderParameters();
    }

    /**
     * Creates a new instance of a parameters object for file-based
     * configuration properties.
     *
     * @return the new parameters object
     */
    public FileBasedBuilderParameters fileBased()
    {
        return createParametersProxy(new FileBasedBuilderParametersImpl(),
                FileBasedBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for combined configuration
     * builder properties.
     *
     * @return the new parameters object
     */
    public CombinedBuilderParameters combined()
    {
        return createParametersProxy(new CombinedBuilderParametersImpl(),
                CombinedBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for JNDI configurations.
     *
     * @return the new parameters object
     */
    public JndiBuilderParameters jndi()
    {
        return createParametersProxy(new JndiBuilderParametersImpl(),
                JndiBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for hierarchical
     * configurations.
     *
     * @return the new parameters object
     */
    public HierarchicalBuilderParameters hierarchical()
    {
        return createParametersProxy(new HierarchicalBuilderParametersImpl(),
                HierarchicalBuilderParameters.class,
                FileBasedBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for XML configurations.
     *
     * @return the new parameters object
     */
    public XMLBuilderParameters xml()
    {
        return createParametersProxy(new XMLBuilderParametersImpl(),
                XMLBuilderParameters.class, FileBasedBuilderParameters.class,
                HierarchicalBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for properties
     * configurations.
     *
     * @return the new parameters object
     */
    public PropertiesBuilderParameters properties()
    {
        return createParametersProxy(new PropertiesBuilderParametersImpl(),
                PropertiesBuilderParameters.class,
                FileBasedBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for a builder for multiple
     * file-based configurations.
     *
     * @return the new parameters object
     */
    public MultiFileBuilderParameters multiFile()
    {
        return createParametersProxy(new MultiFileBuilderParametersImpl(),
                MultiFileBuilderParameters.class);
    }

    /**
     * Creates a new instance of a parameters object for database
     * configurations.
     *
     * @return the new parameters object
     */
    public DatabaseBuilderParameters database()
    {
        return createParametersProxy(new DatabaseBuilderParametersImpl(),
                DatabaseBuilderParameters.class);
    }

    /**
     * Creates a proxy object for a given parameters interface based on the
     * given implementation object.
     *
     * @param <T> the type of the parameters interface
     * @param target the implementing target object
     * @param ifcClass the interface class
     * @param superIfcs an array with additional interface classes to be
     *        implemented
     * @return the proxy object
     */
    private static <T> T createParametersProxy(Object target,
            Class<T> ifcClass, Class<?>... superIfcs)
    {
        Class<?>[] ifcClasses = new Class<?>[1 + superIfcs.length];
        ifcClasses[0] = ifcClass;
        System.arraycopy(superIfcs, 0, ifcClasses, 1, superIfcs.length);
        return ifcClass.cast(Proxy.newProxyInstance(
                Parameters.class.getClassLoader(), ifcClasses,
                new ParametersIfcInvocationHandler(target)));
    }

    /**
     * A specialized {@code InvocationHandler} implementation which maps the
     * methods of a parameters interface to an implementation of the
     * corresponding property interfaces. The parameters interface is a union of
     * multiple property interfaces. The wrapped object implements all of these,
     * but not the union interface. Therefore, a reflection-based approach is
     * required. A special handling is required for the method of the
     * {@code BuilderParameters} interface because here no fluent return value
     * is used.
     */
    private static class ParametersIfcInvocationHandler implements
            InvocationHandler
    {
        /** The target object of reflection calls. */
        private final Object target;

        /**
         * Creates a new instance of {@code ParametersIfcInvocationHandler} and
         * sets the wrapped parameters object.
         *
         * @param targetObj the target object for reflection calls
         */
        public ParametersIfcInvocationHandler(Object targetObj)
        {
            target = targetObj;
        }

        /**
         * {@inheritDoc} This implementation delegates method invocations to the
         * target object and handles the return value correctly.
         */
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            Object result = method.invoke(target, args);
            return isFluentResult(method) ? proxy : result;
        }

        /**
         * Checks whether the specified method belongs to an interface which
         * requires fluent result values.
         *
         * @param method the method to be checked
         * @return a flag whether the method's result should be handled as a
         *         fluent result value
         */
        private static boolean isFluentResult(Method method)
        {
            Class<?> declaringClass = method.getDeclaringClass();
            return declaringClass.isInterface()
                    && !declaringClass.equals(BuilderParameters.class);
        }
    }
}
