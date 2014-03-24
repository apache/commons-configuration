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
package org.apache.commons.configuration.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.event.EventSource;
import org.apache.commons.configuration.ex.ConfigurationRuntimeException;

/**
 * <p>
 * A class that allows the creation of configuration objects wrapping a
 * {@link ConfigurationBuilder}.
 * </p>
 * <p>
 * Using this class special {@code Configuration} proxies can be created that
 * delegate all method invocations to another {@code Configuration} obtained
 * from a {@code ConfigurationBuilder}. For instance, if there is a
 * configuration {@code c} wrapping the builder {@code builder}, the call
 * {@code c.getString(myKey)} is transformed to
 * {@code builder.getConfiguration().getString(myKey)}.
 * </p>
 * <p>
 * There are multiple use cases for such a constellation. One example is that
 * client code can continue working with {@code Configuration} objects while
 * under the hood builders are used. Another example is that dynamic
 * configurations can be realized in a transparent way: a client holds a single
 * configuration (proxy) object, but the underlying builder may return a
 * different data object on each call.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class BuilderConfigurationWrapperFactory
{
    /** The current {@code EventSourceSupport} value. */
    private final EventSourceSupport eventSourceSupport;

    /**
     * Creates a new instance of {@code BuilderConfigurationWrapperFactory} and
     * sets the property for supporting the {@code EventSource} interface.
     *
     * @param evSrcSupport the level of {@code EventSource} support
     */
    public BuilderConfigurationWrapperFactory(EventSourceSupport evSrcSupport)
    {
        eventSourceSupport = evSrcSupport;
    }

    /**
     * Creates a new instance of {@code BuilderConfigurationWrapperFactory}
     * setting the default {@code EventSourceSupport} <em>NONE</em>.
     */
    public BuilderConfigurationWrapperFactory()
    {
        this(EventSourceSupport.NONE);
    }

    /**
     * Creates a wrapper {@code Configuration} on top of the specified
     * {@code ConfigurationBuilder}. This implementation delegates to
     * {@link #createBuilderConfigurationWrapper(Class, ConfigurationBuilder, EventSourceSupport)}
     * .
     *
     * @param <T> the type of the configuration objects returned by this method
     * @param ifcClass the class of the configuration objects returned by this
     *        method; this must be an interface class and must not be
     *        <b>null</b>
     * @param builder the wrapped {@code ConfigurationBuilder} (must not be
     *        <b>null</b>)
     * @param evSrcSupport the level of {@code EventSource} support
     * @throws IllegalArgumentException if a required parameter is missing
     * @throws ConfigurationRuntimeException if an error occurs when creating
     *         the result {@code Configuration}
     */
    public <T extends Configuration> T createBuilderConfigurationWrapper(
            Class<T> ifcClass, ConfigurationBuilder<? extends T> builder)
    {
        return createBuilderConfigurationWrapper(ifcClass, builder,
                getEventSourceSupport());
    }

    /**
     * Returns the level of {@code EventSource} support used when generating
     * {@code Configuration} objects.
     *
     * @return the level of {@code EventSource} support
     */
    public EventSourceSupport getEventSourceSupport()
    {
        return eventSourceSupport;
    }

    /**
     * Returns a {@code Configuration} object which wraps the specified
     * {@code ConfigurationBuilder}. Each access of the configuration is
     * delegated to a corresponding call on the {@code Configuration} object
     * managed by the builder. This is a convenience method which allows
     * creating wrapper configurations without having to instantiate this class.
     *
     * @param <T> the type of the configuration objects returned by this method
     * @param ifcClass the class of the configuration objects returned by this
     *        method; this must be an interface class and must not be
     *        <b>null</b>
     * @param builder the wrapped {@code ConfigurationBuilder} (must not be
     *        <b>null</b>)
     * @param evSrcSupport the level of {@code EventSource} support
     * @throws IllegalArgumentException if a required parameter is missing
     * @throws ConfigurationRuntimeException if an error occurs when creating
     *         the result {@code Configuration}
     */
    public static <T extends Configuration> T createBuilderConfigurationWrapper(
            Class<T> ifcClass, ConfigurationBuilder<? extends T> builder,
            EventSourceSupport evSrcSupport)
    {
        if (ifcClass == null)
        {
            throw new IllegalArgumentException(
                    "Interface class must not be null!");
        }
        if (builder == null)
        {
            throw new IllegalArgumentException("Builder must not be null!");
        }

        return ifcClass.cast(Proxy.newProxyInstance(
                BuilderConfigurationWrapperFactory.class.getClassLoader(),
                fetchSupportedInterfaces(ifcClass, evSrcSupport),
                new BuilderConfigurationWrapperInvocationHandler(builder,
                        evSrcSupport)));
    }

    /**
     * Returns an array with the classes the generated proxy has to support.
     *
     * @param ifcClass the class of the configuration objects returned by this
     *        method; this must be an interface class and must not be
     *        <b>null</b>
     * @param evSrcSupport the level of {@code EventSource} support
     * @return an array with the interface classes to implement
     */
    private static Class<?>[] fetchSupportedInterfaces(Class<?> ifcClass,
            EventSourceSupport evSrcSupport)
    {
        if (EventSourceSupport.NONE == evSrcSupport)
        {
            return new Class<?>[] {
                ifcClass
            };
        }

        Class<?>[] result = new Class<?>[2];
        result[0] = EventSource.class;
        result[1] = ifcClass;
        return result;
    }

    /**
     * <p>
     * An enumeration class with different options for supporting the
     * {@code EventSource} interface in generated {@code Configuration} proxies.
     * </p>
     * <p>
     * Using literals of this class it is possible to specify that a
     * {@code Configuration} object returned by
     * {@code BuilderConfigurationWrapperFactory} also implements the
     * {@code EventSource} interface and how this implementation should work.
     * See the documentation of the single constants for more details
     * </p>
     */
    public static enum EventSourceSupport
    {
        /**
         * No support of the {@code EventSource} interface. If this option is
         * set, {@code Configuration} objects generated by
         * {@code BuilderConfigurationWrapperFactory} do not implement the
         * {@code EventSource} interface.
         */
        NONE,

        /**
         * Dummy support of the {@code EventSource} interface. This option
         * causes {@code Configuration} objects generated by
         * {@code BuilderConfigurationWrapperFactory} to implement the
         * {@code EventSource} interface, however, this implementation consists
         * only of empty dummy methods without real functionality.
         */
        DUMMY,

        /**
         * {@code EventSource} support is implemented by delegating to the
         * associated {@code ConfigurationBuilder} object. If this option is
         * used, generated {@code Configuration} objects provide a fully
         * functional implementation of {@code EventSource} by delegating to the
         * builder. The builder must implement the {@code EventSource}
         * interface, otherwise an exception is thrown.
         */
        BUILDER,

        /**
         * {@code EventSource} support is implemented by delegating to the
         * associated {@code ConfigurationBuilder} object if it supports this
         * interface. Otherwise, a dummy implementation is used.
         */
        BUILDER_OPTIONAL
    }

    /**
     * A specialized {@code InvocationHandler} implementation for wrapper
     * configurations. Here the logic of accessing a wrapped builder is
     * implemented.
     */
    private static class BuilderConfigurationWrapperInvocationHandler implements
            InvocationHandler
    {
        /** The wrapped builder. */
        private final ConfigurationBuilder<? extends Configuration> builder;

        /** The level of {@code EventSource} support. */
        private final EventSourceSupport eventSourceSupport;

        /**
         * Creates a new instance of
         * {@code BuilderConfigurationWrapperInvocationHandler}.
         *
         * @param wrappedBuilder the wrapped builder
         * @param evSrcSupport the level of {@code EventSource} support
         */
        public BuilderConfigurationWrapperInvocationHandler(
                ConfigurationBuilder<? extends Configuration> wrappedBuilder,
                EventSourceSupport evSrcSupport)
        {
            builder = wrappedBuilder;
            eventSourceSupport = evSrcSupport;
        }

        /**
         * Handles method invocations. This implementation handles methods of
         * two different interfaces:
         * <ul>
         * <li>Methods from the {@code EventSource} interface are handled
         * according to the current support level.</li>
         * <li>Other method calls are delegated to the builder's configuration
         * object.</li>
         * </ul>
         *
         * @param proxy the proxy object
         * @param method the method to be invoked
         * @param args method arguments
         * @return the return value of the method
         * @throws Throwable if an error occurs
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            if (EventSource.class.equals(method.getDeclaringClass()))
            {
                return handleEventSourceInvocation(method, args);
            }
            else
            {
                return handleConfigurationInvocation(method, args);
            }
        }

        /**
         * Handles a method invocation on the associated builder's configuration
         * object.
         *
         * @param method the method to be invoked
         * @param args method arguments
         * @return the return value of the method
         * @throws Exception if an error occurs
         */
        private Object handleConfigurationInvocation(Method method,
                Object[] args) throws Exception
        {
            return method.invoke(builder.getConfiguration(), args);
        }

        /**
         * Handles a method invocation on the {@code EventSource} interface.
         * This method evaluates the current {@code EventSourceSupport} object
         * in order to find the appropriate target for the invocation.
         *
         * @param method the method to be invoked
         * @param args method arguments
         * @return the return value of the method
         * @throws Exception if an error occurs
         */
        private Object handleEventSourceInvocation(Method method, Object[] args)
                throws Exception
        {
            Object src;
            boolean mockIfUnsupported;
            if (EventSourceSupport.DUMMY == eventSourceSupport)
            {
                src = this;
                mockIfUnsupported = true;
            }
            else
            {
                src = builder;
                mockIfUnsupported =
                        EventSourceSupport.BUILDER_OPTIONAL == eventSourceSupport;
            }

            Object target =
                    ConfigurationUtils.asEventSource(src, mockIfUnsupported);
            return method.invoke(target, args);
        }
    }
}
