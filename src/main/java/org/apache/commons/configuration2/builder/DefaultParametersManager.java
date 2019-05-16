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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * A class for managing a set of {@link DefaultParametersHandler} objects.
 * </p>
 * <p>
 * This class provides functionality for registering and removing
 * {@code DefaultParametersHandler} objects for arbitrary parameters classes.
 * The handlers registered at an instance can then be applied on a passed in
 * parameters object, so that it gets initialized with the provided default
 * values.
 * </p>
 * <p>
 * Usage of this class is as follows: First the {@code DefaultParametersHandler}
 * objects to be supported must be registered using one of the
 * {@code registerDefaultHandler()} methods. After that arbitrary parameters
 * objects can be passed to the {@code initializeParameters()} method. This
 * causes all {@code DefaultParametersHandler} objects supporting this
 * parameters class to be invoked on this object.
 * </p>
 * <p>
 * Implementation note: This class is thread-safe.
 * </p>
 *
 * @since 2.0
 */
public class DefaultParametersManager
{
    /** A collection with the registered default handlers. */
    private final Collection<DefaultHandlerData> defaultHandlers;

    /**
     * Creates a new instance of {@code DefaultParametersManager}.
     */
    public DefaultParametersManager()
    {
        defaultHandlers = new CopyOnWriteArrayList<>();
    }

    /**
     * Registers the specified {@code DefaultParametersHandler} object for the
     * given parameters class. This means that this handler object is invoked
     * every time a parameters object of the specified class or one of its
     * subclasses is initialized. The handler can set arbitrary default values
     * for the properties supported by this parameters object. If there are
     * multiple handlers registered supporting a specific parameters class, they
     * are invoked in the order in which they were registered. So handlers
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
    public <T> void registerDefaultsHandler(final Class<T> paramsClass,
            final DefaultParametersHandler<? super T> handler)
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
     * {@link org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters
     * FileBasedBuilderParameters} is a base interface of
     * {@link org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters
     * PropertiesBuilderParameters} (although, for technical reasons,
     * this relation is not reflected in the Java classes). A
     * {@link DefaultParametersHandler} object defined for a base interface can
     * also deal with parameter objects "derived" from this base interface (i.e.
     * supporting a super set of the methods defined by the base interface). Now
     * there may be the use case that there is an implementation of
     * {@code DefaultParametersHandler} for a base interface (e.g.
     * {@code FileBasedBuilderParameters}), but it should only process specific
     * derived interfaces (say {@code PropertiesBuilderParameters}, but not
     * {@link org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters
     * XMLBuilderParameters}). This can be achieved by passing in
     * {@code PropertiesBuilderParameters} as start class. In this case,
     * {@code DefaultParametersManager} ensures that the handler is only called
     * on parameter objects having both the start class and the actual type
     * supported by the handler as base interfaces. The passed in start class
     * can be <b>null</b>; then the parameter class supported by the handler is
     * used (which is the default behavior of the
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
    public <T> void registerDefaultsHandler(final Class<T> paramsClass,
            final DefaultParametersHandler<? super T> handler, final Class<?> startClass)
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
    public void unregisterDefaultsHandler(final DefaultParametersHandler<?> handler)
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
    public void unregisterDefaultsHandler(final DefaultParametersHandler<?> handler,
            final Class<?> startClass)
    {
        final Collection<DefaultHandlerData> toRemove =
                new LinkedList<>();
        for (final DefaultHandlerData dhd : defaultHandlers)
        {
            if (dhd.isOccurrence(handler, startClass))
            {
                toRemove.add(dhd);
            }
        }

        defaultHandlers.removeAll(toRemove);
    }

    /**
     * Initializes the passed in {@code BuilderParameters} object by applying
     * all matching {@link DefaultParametersHandler} objects registered at this
     * instance. Using this method the passed in parameters object can be
     * populated with default values.
     *
     * @param params the parameters object to be initialized (may be
     *        <b>null</b>, then this method has no effect)
     */
    public void initializeParameters(final BuilderParameters params)
    {
        if (params != null)
        {
            for (final DefaultHandlerData dhd : defaultHandlers)
            {
                dhd.applyHandlerIfMatching(params);
            }
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
        /** The handler object. */
        private final DefaultParametersHandler<?> handler;

        /** The class supported by this handler. */
        private final Class<?> parameterClass;

        /** The start class for applying this handler. */
        private final Class<?> startClass;

        /**
         * Creates a new instance of {@code DefaultHandlerData}.
         *
         * @param h the {@code DefaultParametersHandler}
         * @param cls the handler's data class
         * @param startCls the start class
         */
        public DefaultHandlerData(final DefaultParametersHandler<?> h, final Class<?> cls,
                final Class<?> startCls)
        {
            handler = h;
            parameterClass = cls;
            startClass = startCls;
        }

        /**
         * Checks whether the managed {@code DefaultParametersHandler} can be
         * applied to the given parameters object. If this is the case, it is
         * executed on this object and can initialize it with default values.
         *
         * @param obj the parameters object to be initialized
         */
        @SuppressWarnings("unchecked")
        // There are explicit isInstance() checks, so there won't be
        // ClassCastExceptions
        public void applyHandlerIfMatching(final BuilderParameters obj)
        {
            if (parameterClass.isInstance(obj)
                    && (startClass == null || startClass.isInstance(obj)))
            {
                @SuppressWarnings("rawtypes")
                final
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
        public boolean isOccurrence(final DefaultParametersHandler<?> h,
                final Class<?> startCls)
        {
            return h == handler
                    && (startCls == null || startCls.equals(startClass));
        }
    }
}
