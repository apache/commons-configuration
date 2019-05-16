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
package org.apache.commons.configuration2.builder.fluent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.DatabaseBuilderParametersImpl;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.DefaultParametersManager;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.HierarchicalBuilderParametersImpl;
import org.apache.commons.configuration2.builder.INIBuilderParametersImpl;
import org.apache.commons.configuration2.builder.JndiBuilderParametersImpl;
import org.apache.commons.configuration2.builder.PropertiesBuilderParametersImpl;
import org.apache.commons.configuration2.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.CombinedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.MultiFileBuilderParametersImpl;

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
 * objects offering a truly fluent API. The methods provided can be called
 * directly when setting up a configuration builder as shown in the following
 * example code fragment:
 * </p>
 *
 * <pre>
 * Parameters params = new Parameters();
 * configurationBuilder.configure(params.fileBased()
 *         .setThrowExceptionOnMissing(true).setEncoding(&quot;UTF-8&quot;)
 *         .setListDelimiter('#').setFileName(&quot;test.xml&quot;));
 * </pre>
 *
 * <p>
 * Using this class it is not only possible to create new parameters objects but
 * also to initialize the newly created objects with default values. This is
 * via the associated {@link DefaultParametersManager} object. Such an object
 * can be passed to the constructor, or a new (uninitialized) instance is
 * created. There are convenience methods for interacting with the associated
 * {@code DefaultParametersManager}, namely to register or remove
 * {@link DefaultParametersHandler} objects. On all newly created parameters
 * objects the handlers registered at the associated {@code DefaultParametersHandler}
 * are automatically applied.
 * </p>
 * <p>
 * Implementation note: This class is thread-safe.
 * </p>
 *
 * @since 2.0
 */
public final class Parameters
{
    /** The manager for default handlers. */
    private final DefaultParametersManager defaultParametersManager;

    /**
     * Creates a new instance of {@code Parameters}. A new, uninitialized
     * {@link DefaultParametersManager} is created.
     */
    public Parameters()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code Parameters} and initializes it with the
     * given {@code DefaultParametersManager}. Because
     * {@code DefaultParametersManager} is thread-safe, it makes sense to share
     * a single instance between multiple {@code Parameters} objects; that way
     * the same initialization is performed on newly created parameters objects.
     *
     * @param manager the {@code DefaultParametersHandler} (may be <b>null</b>,
     *        then a new default instance is created)
     */
    public Parameters(final DefaultParametersManager manager)
    {
        defaultParametersManager =
                (manager != null) ? manager : new DefaultParametersManager();
    }

    /**
     * Returns the {@code DefaultParametersManager} associated with this object.
     *
     * @return the {@code DefaultParametersManager}
     */
    public DefaultParametersManager getDefaultParametersManager()
    {
        return defaultParametersManager;
    }

    /**
     * Registers the specified {@code DefaultParametersHandler} object for the
     * given parameters class. This is a convenience method which just delegates
     * to the associated {@code DefaultParametersManager}.
     *
     * @param <T> the type of the parameters supported by this handler
     * @param paramsClass the parameters class supported by this handler (must
     *        not be <b>null</b>)
     * @param handler the {@code DefaultParametersHandler} to be registered
     *        (must not be <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is missing
     * @see DefaultParametersManager
     */
    public <T> void registerDefaultsHandler(final Class<T> paramsClass,
            final DefaultParametersHandler<? super T> handler)
    {
        getDefaultParametersManager().registerDefaultsHandler(paramsClass, handler);
    }

    /**
     * Registers the specified {@code DefaultParametersHandler} object for the
     * given parameters class and start class in the inheritance hierarchy. This
     * is a convenience method which just delegates to the associated
     * {@code DefaultParametersManager}.
     *
     * @param <T> the type of the parameters supported by this handler
     * @param paramsClass the parameters class supported by this handler (must
     *        not be <b>null</b>)
     * @param handler the {@code DefaultParametersHandler} to be registered
     *        (must not be <b>null</b>)
     * @param startClass an optional start class in the hierarchy of parameter
     *        objects for which this handler should be applied
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public <T> void registerDefaultsHandler(final Class<T> paramsClass,
            final DefaultParametersHandler<? super T> handler, final Class<?> startClass)
    {
        getDefaultParametersManager().registerDefaultsHandler(paramsClass,
                handler, startClass);
    }

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
     * Creates a new instance of a parameters object for INI configurations.
     *
     * @return the new parameters object
     */
    public INIBuilderParameters ini()
    {
        return createParametersProxy(new INIBuilderParametersImpl(),
                INIBuilderParameters.class, FileBasedBuilderParameters.class,
                HierarchicalBuilderParameters.class);
    }

    /**
     * Creates a proxy object for a given parameters interface based on the
     * given implementation object. The newly created object is initialized
     * with default values if there are matching {@link DefaultParametersHandler}
     * objects.
     *
     * @param <T> the type of the parameters interface
     * @param target the implementing target object
     * @param ifcClass the interface class
     * @param superIfcs an array with additional interface classes to be
     *        implemented
     * @return the proxy object
     */
    private <T> T createParametersProxy(final Object target, final Class<T> ifcClass,
            final Class<?>... superIfcs)
    {
        final Class<?>[] ifcClasses = new Class<?>[1 + superIfcs.length];
        ifcClasses[0] = ifcClass;
        System.arraycopy(superIfcs, 0, ifcClasses, 1, superIfcs.length);
        final Object obj =
                Proxy.newProxyInstance(Parameters.class.getClassLoader(),
                        ifcClasses, new ParametersIfcInvocationHandler(target));
        getDefaultParametersManager().initializeParameters(
                (BuilderParameters) obj);
        return ifcClass.cast(obj);
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
        public ParametersIfcInvocationHandler(final Object targetObj)
        {
            target = targetObj;
        }

        /**
         * {@inheritDoc} This implementation delegates method invocations to the
         * target object and handles the return value correctly.
         */
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable
        {
            final Object result = method.invoke(target, args);
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
        private static boolean isFluentResult(final Method method)
        {
            final Class<?> declaringClass = method.getDeclaringClass();
            return declaringClass.isInterface()
                    && !declaringClass.equals(BuilderParameters.class);
        }
    }
}
