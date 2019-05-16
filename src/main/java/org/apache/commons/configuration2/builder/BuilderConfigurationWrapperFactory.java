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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.event.EventSource;

/**
 * <p>
 * A class that allows the creation of configuration objects wrapping a
 * {@link ConfigurationBuilder}.
 * </p>
 * <p>
 * Using this class special {@code ImmutableConfiguration} proxies can be created that
 * delegate all method invocations to another {@code ImmutableConfiguration} obtained
 * from a {@code ConfigurationBuilder}. For instance, if there is a
 * configuration {@code c} wrapping the builder {@code builder}, the call
 * {@code c.getString(myKey)} is transformed to
 * {@code builder.getConfiguration().getString(myKey)}.
 * </p>
 * <p>
 * There are multiple use cases for such a constellation. One example is that
 * client code can continue working with {@code ImmutableConfiguration} objects while
 * under the hood builders are used. Another example is that dynamic
 * configurations can be realized in a transparent way: a client holds a single
 * configuration (proxy) object, but the underlying builder may return a
 * different data object on each call.
 * </p>
 *
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
    public BuilderConfigurationWrapperFactory(final EventSourceSupport evSrcSupport)
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
     * Creates a wrapper {@code ImmutableConfiguration} on top of the specified
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
     * @return the wrapper configuration
     * @throws IllegalArgumentException if a required parameter is missing
     * @throws org.apache.commons.configuration2.ex.ConfigurationRuntimeException if an error
     *         occurs when creating the result {@code ImmutableConfiguration}
     */
    public <T extends ImmutableConfiguration> T createBuilderConfigurationWrapper(
            final Class<T> ifcClass, final ConfigurationBuilder<? extends T> builder)
    {
        return createBuilderConfigurationWrapper(ifcClass, builder,
                getEventSourceSupport());
    }

    /**
     * Returns the level of {@code EventSource} support used when generating
     * {@code ImmutableConfiguration} objects.
     *
     * @return the level of {@code EventSource} support
     */
    public EventSourceSupport getEventSourceSupport()
    {
        return eventSourceSupport;
    }

    /**
     * Returns a {@code ImmutableConfiguration} object which wraps the specified
     * {@code ConfigurationBuilder}. Each access of the configuration is
     * delegated to a corresponding call on the {@code ImmutableConfiguration} object
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
     * @return the wrapper configuration
     * @throws IllegalArgumentException if a required parameter is missing
     * @throws org.apache.commons.configuration2.ex.ConfigurationRuntimeException if an error
     *         occurs when creating the result {@code ImmutableConfiguration}
     */
    public static <T extends ImmutableConfiguration> T createBuilderConfigurationWrapper(
            final Class<T> ifcClass, final ConfigurationBuilder<? extends T> builder,
            final EventSourceSupport evSrcSupport)
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
    private static Class<?>[] fetchSupportedInterfaces(final Class<?> ifcClass,
            final EventSourceSupport evSrcSupport)
    {
        if (EventSourceSupport.NONE == evSrcSupport)
        {
            return new Class<?>[] {
                ifcClass
            };
        }

        final Class<?>[] result = new Class<?>[2];
        result[0] = EventSource.class;
        result[1] = ifcClass;
        return result;
    }

    /**
     * <p>
     * An enumeration class with different options for supporting the
     * {@code EventSource} interface in generated {@code ImmutableConfiguration} proxies.
     * </p>
     * <p>
     * Using literals of this class it is possible to specify that a
     * {@code ImmutableConfiguration} object returned by
     * {@code BuilderConfigurationWrapperFactory} also implements the
     * {@code EventSource} interface and how this implementation should work.
     * See the documentation of the single constants for more details.
     * </p>
     */
    public enum EventSourceSupport
    {
        /**
         * No support of the {@code EventSource} interface. If this option is
         * set, {@code ImmutableConfiguration} objects generated by
         * {@code BuilderConfigurationWrapperFactory} do not implement the
         * {@code EventSource} interface.
         */
        NONE,

        /**
         * Dummy support of the {@code EventSource} interface. This option
         * causes {@code ImmutableConfiguration} objects generated by
         * {@code BuilderConfigurationWrapperFactory} to implement the
         * {@code EventSource} interface, however, this implementation consists
         * only of empty dummy methods without real functionality.
         */
        DUMMY,

        /**
         * {@code EventSource} support is implemented by delegating to the
         * associated {@code ConfigurationBuilder} object. If this option is
         * used, generated {@code ImmutableConfiguration} objects provide a fully
         * functional implementation of {@code EventSource} by delegating to the
         * builder. Because the {@code ConfigurationBuilder} interface extends
         * {@code EventSource} this delegation is always possible.
         */
        BUILDER
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
        private final ConfigurationBuilder<? extends ImmutableConfiguration> builder;

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
                final ConfigurationBuilder<? extends ImmutableConfiguration> wrappedBuilder,
                final EventSourceSupport evSrcSupport)
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
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable
        {
            if (EventSource.class.equals(method.getDeclaringClass()))
            {
                return handleEventSourceInvocation(method, args);
            }
            return handleConfigurationInvocation(method, args);
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
        private Object handleConfigurationInvocation(final Method method,
                final Object[] args) throws Exception
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
        private Object handleEventSourceInvocation(final Method method, final Object[] args)
                throws Exception
        {
            final Object target =
                    (EventSourceSupport.DUMMY == eventSourceSupport) ? ConfigurationUtils
                            .asEventSource(this, true) : builder;
            return method.invoke(target, args);
        }
    }
}
