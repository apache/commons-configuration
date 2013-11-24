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
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * objects offering a truly fluent API. The methods provided can be called
 * directly when setting up a configuration builder as shown in the following
 * example code fragment:
 *
 * <pre>
 * Parameters params = new Parameters();
 * configurationBuilder.configure(params.fileBased()
 *         .setThrowExceptionOnMissing(true).setEncoding(&quot;UTF-8&quot;)
 *         .setListDelimiter('#').setFileName(&quot;test.xml&quot;));
 * </pre>
 *
 * </p>
 * <p>
 * Using this class it is not only possible to create new parameters objects but
 * also to initialize the newly created objects with default values. This is
 * achieved by registering objects implementing the
 * {@link DefaultParametersHandler} interface with the
 * {@code registerDefaultsHandler()} method. The handler object is then called
 * whenever a parameters object of the supported class (or one of its
 * subclasses) is created. This makes it easy to define default settings that
 * are applied for all parameters object created by this {@code Parameters}
 * instance.
 * </p>
 * <p>
 * Implementation note: This class is thread-safe.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public final class Parameters
{
    /** A collection with the registered default handlers. */
    private final Collection<DefaultHandlerData> defaultHandlers;

    /**
     * Creates a new instance of {@code Parameters}.
     */
    public Parameters()
    {
        defaultHandlers = new CopyOnWriteArrayList<DefaultHandlerData>();
    }

    /**
     * Registers the specified {@code DefaultParametersHandler} object for the
     * given parameters class. This means that this handler object is invoked
     * every time a new parameters object of the specified class or one of its
     * subclasses is created. The handler can set arbitrary default values for
     * the properties supported by this parameters object. If there are multiple
     * handlers registered supporting a specific parameters class, they are
     * invoked in the order in which they were registered. So handlers
     * registered later may override the values set by handlers registered
     * earlier.
     *
     * @param <T> the type of the parameters supported by this handler
     * @param paramsClass the parameters class supported by this handler (must
     *        not be <b>null</b>)
     * @param handler the {@code DefaultParametersHandler} to be registered
     *        (must not be <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public <T> void registerDefaultsHandler(Class<T> paramsClass,
            DefaultParametersHandler<? super T> handler)
    {
        registerDefaultsHandler(paramsClass, handler, null);
    }

    /**
     * Registers the specified {@code DefaultParametersHandler} object for the
     * given parameters class and start class in the inheritance hierarchy. This
     * method works like
     * {@link #registerDefaultsHandler(Class, DefaultParametersHandler)}, but
     * the defaults handler is only executed on parameter objects that are
     * instances of the specified start class. Parameter classes do not stand in
     * a real inheritance hierarchy; however, there is a logic hierarchy defined
     * by the methods supported by the different parameter objects. A properties
     * parameter object for instance supports all methods defined for a
     * file-based parameter object. So one can argue that
     * {@link FileBasedBuilderParameters} is a base interface of
     * {@link PropertiesBuilderParameters} (although, for technical reasons,
     * this relation is not reflected in the Java classes). A
     * {@link DefaultParametersHandler} object defined for a base interface can
     * also deal with parameter objects "derived" from this base interface (i.e.
     * supporting a super set of the methods defined by the base interface). Now
     * there may be the use case that there is an implementation of
     * {@code DefaultParametersHandler} for a base interface (e.g.
     * {@code FileBasedBuilderParameters}), but it should only process specific
     * derived interfaces (say {@code PropertiesBuilderParameters}, but not
     * {@link XMLBuilderParameters}). This can be achieved by passing in
     * {@code PropertiesBuilderParameters} as start class. In this case,
     * {@code Parameters} ensures that the handler is only called on parameter
     * objects having both the start class and the actual type supported by the
     * handler as base interfaces. The passed in start class can be <b>null</b>;
     * then the parameter class supported by the handler is used (which is the
     * default behavior of the
     * {@link #registerDefaultsHandler(Class, DefaultParametersHandler)}
     * method).
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
    public <T> void registerDefaultsHandler(Class<T> paramsClass,
            DefaultParametersHandler<? super T> handler, Class<?> startClass)
    {
        if (paramsClass == null)
        {
            throw new IllegalArgumentException(
                    "Parameters class must not be null!");
        }
        if (handler == null)
        {
            throw new IllegalArgumentException(
                    "DefaultParametersHandler must not be null!");
        }
        defaultHandlers.add(new DefaultHandlerData(handler, paramsClass,
                startClass));
    }

    /**
     * Removes the specified {@code DefaultParametersHandler} from this
     * instance. If this handler has been registered multiple times for
     * different start classes, all occurrences are removed.
     *
     * @param handler the {@code DefaultParametersHandler} to be removed
     */
    public void unregisterDefaultsHandler(DefaultParametersHandler<?> handler)
    {
        unregisterDefaultsHandler(handler, null);
    }

    /**
     * Removes the specified {@code DefaultParametersHandler} from this instance
     * if it is in combination with the given start class. If this handler has
     * been registered multiple times for different start classes, only
     * occurrences for the given start class are removed. The {@code startClass}
     * parameter can be <b>null</b>, then all occurrences of the handler are
     * removed.
     *
     * @param handler the {@code DefaultParametersHandler} to be removed
     * @param startClass the start class for which this handler is to be removed
     */
    public void unregisterDefaultsHandler(DefaultParametersHandler<?> handler,
            Class<?> startClass)
    {
        Collection<DefaultHandlerData> toRemove =
                new LinkedList<DefaultHandlerData>();
        for (DefaultHandlerData dhd : defaultHandlers)
        {
            if (dhd.isOccurrence(handler, startClass))
            {
                toRemove.add(dhd);
            }
        }

        defaultHandlers.removeAll(toRemove);
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
     * Initializes the passed in {@code BuilderParameters} object by applying
     * all matching {@link DefaultParametersHandler} objects registered at this
     * instance. Using this method the passed in parameters object can be
     * populated with default values. It is not necessary to call this method
     * for parameter objects that have been created by this {@code Parameters}
     * instance - in this case, it is called automatically. However, if a
     * parameters object was created by another source, this method can be used
     * to apply the {@code DefaultParametersHandler} objects which have been
     * registered here.
     *
     * @param params the parameters object to be initialized (may be
     *        <b>null</b>, then this method has no effect)
     */
    public void initializeParameters(BuilderParameters params)
    {
        if (params != null)
        {
            for (DefaultHandlerData dhd : defaultHandlers)
            {
                dhd.applyHandlerIfMatching(params);
            }
        }
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
    private <T> T createParametersProxy(Object target, Class<T> ifcClass,
            Class<?>... superIfcs)
    {
        Class<?>[] ifcClasses = new Class<?>[1 + superIfcs.length];
        ifcClasses[0] = ifcClass;
        System.arraycopy(superIfcs, 0, ifcClasses, 1, superIfcs.length);
        Object obj =
                Proxy.newProxyInstance(Parameters.class.getClassLoader(),
                        ifcClasses, new ParametersIfcInvocationHandler(target));
        initializeParameters((BuilderParameters) obj);
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

    /**
     * A data class storing information about {@code DefaultParametersHandler}
     * objects added to a {@code Parameters} object. Using this class it is
     * possible to find out which default handlers apply for a given parameters
     * object and to invoke them.
     */
    private static class DefaultHandlerData
    {
        /** The handler object.*/
        private final DefaultParametersHandler<?> handler;

        /** The class supported by this handler.*/
        private final Class<?> parameterClass;

        /** The start class for applying this handler.*/
        private final Class<?> startClass;

        /**
         *
         * Creates a new instance of {@code DefaultHandlerData}.
         * @param h the {@code DefaultParametersHandler}
         * @param cls the handler's data class
         * @param startCls the start class
         */
        public DefaultHandlerData(DefaultParametersHandler<?> h, Class<?> cls, Class<?> startCls)
        {
            handler = h;
            parameterClass = cls;
            startClass = startCls;
        }

        /**
         * Checks whether the managed {@code DefaultParametersHandler} can be
         * applied to the given parameters object. If this is the case, it is
         * executed on this object and can initialize it with default values.
         * @param obj the parameters object to be initialized
         */
        @SuppressWarnings("unchecked")
        // There are explicit isInstance() checks, so there won't be
        // ClassCastExceptions
        public void applyHandlerIfMatching(BuilderParameters obj)
        {
            if(parameterClass.isInstance(obj) && (startClass == null || startClass.isInstance(obj)))
            {
                @SuppressWarnings("rawtypes")
                DefaultParametersHandler handlerUntyped = handler;
                handlerUntyped.initializeDefaults(obj);
            }
        }

        /**
         * Tests whether this instance refers to the specified occurrence of a
         * {@code DefaultParametersHandler}.
         *
         * @param h the handler to be checked
         * @param startCls the start class
         * @return <b>true</b> if this instance refers to this occurrence,
         *         <b>false</b> otherwise
         */
        public boolean isOccurrence(DefaultParametersHandler<?> h,
                Class<?> startCls)
        {
            return h == handler
                    && (startCls == null || startCls.equals(startClass));
        }
    }
}
