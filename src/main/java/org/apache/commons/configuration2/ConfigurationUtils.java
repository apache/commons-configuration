/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.sync.NoOpSynchronizer;
import org.apache.commons.configuration2.sync.Synchronizer;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Miscellaneous utility methods for configurations.
 *
 * @see ConfigurationConverter Utility methods to convert configurations.
 */
public final class ConfigurationUtils {

    /**
     * Constant for the name of the clone() method.
     */
    private static final String METHOD_CLONE = "clone";

    /**
     * An array with interfaces to be implemented by a proxy for an immutable configuration.
     */
    private static final Class<?>[] IMMUTABLE_CONFIG_IFCS = {ImmutableConfiguration.class};

    /**
     * An array with interfaces to be implemented by a proxy for an immutable hierarchical configuration.
     */
    private static final Class<?>[] IMMUTABLE_HIERARCHICAL_CONFIG_IFCS = {ImmutableHierarchicalConfiguration.class};
    /**
     * A dummy event source that is returned by {@code asEventSource()} if a mock object has to be returned. It provides
     * empty dummy implementations for all interface methods.
     */
    private static final EventSource DUMMY_EVENT_SOURCE = new EventSource() {

        @Override
        public <T extends Event> void addEventListener(final EventType<T> eventType, final EventListener<? super T> listener) {
            // empty
        }

        @Override
        public <T extends Event> boolean removeEventListener(final EventType<T> eventType, final EventListener<? super T> listener) {
            return false;
        }
    };

    /** The logger. */
    private static final Log LOG = LogFactory.getLog(ConfigurationUtils.class);

    /**
     * <p>
     * Append all properties from the source configuration to the target configuration. Properties in the source
     * configuration are appended to the properties with the same key in the target configuration.
     * </p>
     * <p>
     * <em>Note:</em> This method is not able to handle some specifics of configurations derived from
     * {@code AbstractConfiguration} (for example list delimiters). For a full support of all of these features the {@code copy()}
     * method of {@code AbstractConfiguration} should be used. In a future release this method might become deprecated.
     * </p>
     *
     * @param source the source configuration.
     * @param target the target configuration.
     * @since 1.1
     */
    public static void append(final Configuration source, final Configuration target) {
        append((ImmutableConfiguration) source, target);
    }

    /**
     * <p>
     * Append all properties from the source configuration to the target configuration. Properties in the source
     * configuration are appended to the properties with the same key in the target configuration.
     * </p>
     * <p>
     * <em>Note:</em> This method is not able to handle some specifics of configurations derived from
     * {@code AbstractConfiguration} (for example list delimiters). For a full support of all of these features the {@code copy()}
     * method of {@code AbstractConfiguration} should be used. In a future release this method might become deprecated.
     * </p>
     *
     * @param source the source configuration.
     * @param target the target configuration.
     * @since 2.2
     */
    public static void append(final ImmutableConfiguration source, final Configuration target) {
        source.forEach(target::addProperty);
    }

    /**
     * Casts the specified object to an {@code EventSource} if possible. The boolean argument determines the method's
     * behavior if the object does not implement the {@code EventSource} event: if set to <strong>false</strong>, a
     * {@code ConfigurationRuntimeException} is thrown; if set to <strong>true</strong>, a dummy {@code EventSource} is returned; on
     * this object all methods can be called, but they do not have any effect.
     *
     * @param obj the object to be cast as {@code EventSource}.
     * @param mockIfUnsupported a flag whether a mock object should be returned if necessary.
     * @return an {@code EventSource}.
     * @throws ConfigurationRuntimeException if the object cannot be cast to {@code EventSource} and the mock flag is
     *         <strong>false</strong>.
     * @since 2.0
     */
    public static EventSource asEventSource(final Object obj, final boolean mockIfUnsupported) {
        if (obj instanceof EventSource) {
            return (EventSource) obj;
        }
        if (!mockIfUnsupported) {
            throw new ConfigurationRuntimeException("Cannot cast to EventSource: " + obj);
        }
        return DUMMY_EVENT_SOURCE;
    }

    /**
     * An internally used helper method for cloning objects. This implementation is not very sophisticated nor efficient.
     * Maybe it can be replaced by an implementation from Commons Lang later. The method checks whether the passed in object
     * implements the {@code Cloneable} interface. If this is the case, the {@code clone()} method is invoked by reflection.
     * Errors that occur during the cloning process are re-thrown as runtime exceptions.
     *
     * @param obj the object to be cloned or null.
     * @return the cloned object or null.
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    @SuppressWarnings("unchecked")
    static <T> T clone(final T obj) throws CloneNotSupportedException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            try {
                return (T) obj.getClass().getMethod(METHOD_CLONE).invoke(obj);
            } catch (final NoSuchMethodException nmex) {
                throw new CloneNotSupportedException("No clone() method found for class" + obj.getClass().getName());
            } catch (final IllegalAccessException | InvocationTargetException itex) {
                throw new ConfigurationRuntimeException(itex);
            }
        }
        throw new CloneNotSupportedException(obj.getClass().getName() + " does not implement Cloneable");
    }

    /**
     * Clones the given configuration object if this is possible. If the passed in configuration object implements the
     * {@code Cloneable} interface, its {@code clone()} method will be invoked. Otherwise an exception will be thrown.
     *
     * @param config the configuration object to be cloned (can be <strong>null</strong>).
     * @return the cloned configuration (<strong>null</strong> if the argument was <strong>null</strong>, too).
     * @throws ConfigurationRuntimeException if cloning is not supported for this object.
     * @since 1.3
     */
    public static Configuration cloneConfiguration(final Configuration config) throws ConfigurationRuntimeException {
        try {
            return clone(config);
        } catch (final CloneNotSupportedException cnex) {
            throw new ConfigurationRuntimeException(cnex);
        }
    }

    /**
     * Returns a clone of the passed in object if cloning is supported or the object itself if not. This method checks
     * whether the passed in object implements the {@code Cloneable} interface. If this is the case, the {@code clone()}
     * method is invoked. Otherwise, the object is directly returned. Errors that might occur during reflection calls are
     * caught and also cause this method to return the original object.
     *
     * @param obj the object to be cloned.
     * @return the result of the cloning attempt.
     * @since 2.0
     */
    public static Object cloneIfPossible(final Object obj) {
        try {
            return clone(obj);
        } catch (final Exception ex) {
            return obj;
        }
    }

    /**
     * Creates a clone of the specified {@code Synchronizer}. This method can be called by {@code clone()} implementations
     * in configuration classes that also need to copy the {@code Synchronizer} object. This method can handle some
     * well-known {@code Synchronizer} implementations directly. For other classes, it uses the following algorithm:
     * <ul>
     * <li>If the class of the {@code Synchronizer} has a standard constructor, a new instance is created using
     * reflection.</li>
     * <li>If this is not possible, it is tried whether the object can be cloned.</li>
     * </ul>
     * If all attempts fail, a {@code ConfigurationRuntimeException} is thrown.
     *
     * @param sync the {@code Synchronizer} object to be cloned.
     * @return the clone of this {@code Synchronizer}.
     * @throws ConfigurationRuntimeException if no clone can be created.
     * @throws IllegalArgumentException if <strong>null</strong> is passed in.
     */
    public static Synchronizer cloneSynchronizer(final Synchronizer sync) {
        if (sync == null) {
            throw new IllegalArgumentException("Synchronizer must not be null!");
        }
        if (NoOpSynchronizer.INSTANCE == sync) {
            return sync;
        }
        try {
            return sync.getClass().getConstructor().newInstance();
        } catch (final Exception ignore) {
            try {
                return clone(sync);
            } catch (final CloneNotSupportedException e) {
                throw new ConfigurationRuntimeException("Cannot clone Synchronizer " + sync);
            }
        }
    }

    /**
     * Converts the passed in configuration to a hierarchical one. If the configuration is already hierarchical, it is
     * directly returned. Otherwise all properties are copied into a new hierarchical configuration.
     *
     * @param conf the configuration to convert.
     * @return the new hierarchical configuration (the result is <strong>null</strong> if and only if the passed in configuration is
     *         <strong>null</strong>).
     * @since 1.3
     */
    public static HierarchicalConfiguration<?> convertToHierarchical(final Configuration conf) {
        return convertToHierarchical(conf, null);
    }

    /**
     * Converts the passed in {@code Configuration} object to a hierarchical one using the specified
     * {@code ExpressionEngine}. This conversion works by adding the keys found in the configuration to a newly created
     * hierarchical configuration. When adding new keys to a hierarchical configuration the keys are interpreted by its
     * {@code ExpressionEngine}. If they contain special characters (for example brackets) that are treated in a special way by the
     * default expression engine, it may be necessary using a specific engine that can deal with such characters. Otherwise
     * <strong>null</strong> can be passed in for the {@code ExpressionEngine}; then the default expression engine is used. If the
     * passed in configuration is already hierarchical, it is directly returned. (However, the {@code ExpressionEngine} is
     * set if it is not <strong>null</strong>.) Otherwise all properties are copied into a new hierarchical configuration.
     *
     * @param conf the configuration to convert.
     * @param engine the {@code ExpressionEngine} for the hierarchical configuration or <strong>null</strong> for the default.
     * @return the new hierarchical configuration (the result is <strong>null</strong> if and only if the passed in configuration is
     *         <strong>null</strong>).
     * @since 1.6
     */
    public static HierarchicalConfiguration<?> convertToHierarchical(final Configuration conf, final ExpressionEngine engine) {
        if (conf == null) {
            return null;
        }
        if (conf instanceof HierarchicalConfiguration) {
            final HierarchicalConfiguration<?> hc = (HierarchicalConfiguration<?>) conf;
            if (engine != null) {
                hc.setExpressionEngine(engine);
            }
            return hc;
        }
        final BaseHierarchicalConfiguration hc = new BaseHierarchicalConfiguration();
        if (engine != null) {
            hc.setExpressionEngine(engine);
        }
        // Per default, a DisabledListDelimiterHandler is set.
        // So list delimiters in property values are not an issue.
        hc.copy(conf);
        return hc;
    }

    /**
     * <p>
     * Copy all properties from the source configuration to the target configuration. Properties in the target configuration
     * are replaced with the properties with the same key in the source configuration.
     * </p>
     * <p>
     * <em>Note:</em> This method is not able to handle some specifics of configurations derived from
     * {@code AbstractConfiguration} (for example list delimiters). For a full support of all of these features the {@code copy()}
     * method of {@code AbstractConfiguration} should be used. In a future release this method might become deprecated.
     * </p>
     *
     * @param source the source configuration.
     * @param target the target configuration.
     * @since 1.1
     */
    public static void copy(final Configuration source, final Configuration target) {
        copy((ImmutableConfiguration) source, target);
    }

    /**
     * <p>
     * Copy all properties from the source configuration to the target configuration. Properties in the target configuration
     * are replaced with the properties with the same key in the source configuration.
     * </p>
     * <p>
     * <em>Note:</em> This method is not able to handle some specifics of configurations derived from
     * {@code AbstractConfiguration} (for example list delimiters). For a full support of all of these features the {@code copy()}
     * method of {@code AbstractConfiguration} should be used. In a future release this method might become deprecated.
     * </p>
     *
     * @param source the source configuration.
     * @param target the target configuration.
     * @since 2.2
     */
    public static void copy(final ImmutableConfiguration source, final Configuration target) {
        source.forEach(target::setProperty);
    }

    /**
     * Helper method for creating a proxy for an unmodifiable configuration. The interfaces the proxy should implement are
     * passed as argument.
     *
     * @param ifcs an array with the interface classes the proxy must implement.
     * @param c the configuration object to be wrapped.
     * @return a proxy object for an immutable configuration.
     * @throws NullPointerException if the configuration is <strong>null</strong>.
     */
    private static ImmutableConfiguration createUnmodifiableConfiguration(final Class<?>[] ifcs, final Configuration c) {
        return (ImmutableConfiguration) Proxy.newProxyInstance(ConfigurationUtils.class.getClassLoader(), ifcs, new ImmutableConfigurationInvocationHandler(c));
    }

    /**
     * Dump the configuration key/value mappings to some ouput stream. This version of the method exists only for backwards
     * compatibility reason.
     *
     * @param configuration the configuration.
     * @param out the output stream to dump the configuration to.
     */
    public static void dump(final Configuration configuration, final PrintStream out) {
        dump((ImmutableConfiguration) configuration, out);
    }

    /**
     * Dump the configuration key/value mappings to some writer. This version of the method exists only for backwards
     * compatibility reason.
     *
     * @param configuration the configuration.
     * @param out the writer to dump the configuration to.
     */
    public static void dump(final Configuration configuration, final PrintWriter out) {
        dump((ImmutableConfiguration) configuration, out);
    }

    /**
     * Dump the configuration key/value mappings to some ouput stream.
     *
     * @param configuration the configuration.
     * @param out the output stream to dump the configuration to.
     * @since 2.2
     */
    public static void dump(final ImmutableConfiguration configuration, final PrintStream out) {
        dump(configuration, new PrintWriter(out));
    }

    /**
     * Dump the configuration key/value mappings to some writer.
     *
     * @param configuration the configuration.
     * @param out the writer to dump the configuration to.
     * @since 2.2
     */
    public static void dump(final ImmutableConfiguration configuration, final PrintWriter out) {
        AtomicInteger last = new AtomicInteger(configuration.size());
        configuration.forEach((k, v) -> {
            out.print(k);
            out.print("=");
            out.print(v);
            if (last.decrementAndGet() > 0) {
                out.println();
            }
        });
        out.flush();
    }

    /**
     * Enables runtime exceptions for the specified configuration object. This method can be used for configuration
     * implementations that may face errors on normal property access, for example {@code DatabaseConfiguration} or
     * {@code JNDIConfiguration}. Per default such errors are simply logged and then ignored. This implementation will
     * register a special {@link EventListener} that throws a runtime exception (namely a
     * {@code ConfigurationRuntimeException}) on each received error event.
     *
     * @param src the configuration, for which runtime exceptions are to be enabled; this configuration must implement
     *        {@link EventSource}.
     */
    public static void enableRuntimeExceptions(final Configuration src) {
        if (!(src instanceof EventSource)) {
            throw new IllegalArgumentException("Configuration must implement EventSource!");
        }
        ((EventSource) src).addEventListener(ConfigurationErrorEvent.ANY, event -> {
            // Throw a runtime exception
            throw new ConfigurationRuntimeException(event.getCause());
        });
    }

    /**
     * Loads the class with the given name. This method is used whenever a class has to be loaded dynamically. It first
     * tries the current thread's context class loader. If this fails, the class loader of this class is tried.
     *
     * @param clsName the name of the class to be loaded.
     * @return the loaded class.
     * @throws ClassNotFoundException if the class cannot be resolved.
     * @since 2.0
     */
    public static Class<?> loadClass(final String clsName) throws ClassNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading class " + clsName);
        }
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            if (cl != null) {
                return cl.loadClass(clsName);
            }
        } catch (final ClassNotFoundException cnfex) {
            LOG.info("Could not load class " + clsName + " using CCL. Falling back to default CL.", cnfex);
        }
        return ConfigurationUtils.class.getClassLoader().loadClass(clsName);
    }

    /**
     * Loads the class with the specified name re-throwing {@code ClassNotFoundException} exceptions as runtime exceptions.
     * This method works like {@link #loadClass(String)}. However, checked exceptions are caught and re-thrown as
     * {@code ConfigurationRuntimeException}.
     *
     * @param clsName the name of the class to be loaded.
     * @return the loaded class.
     * @throws ConfigurationRuntimeException if the class cannot be resolved.
     * @since 2.0
     */
    public static Class<?> loadClassNoEx(final String clsName) {
        try {
            return loadClass(clsName);
        } catch (final ClassNotFoundException cnfex) {
            throw new ConfigurationRuntimeException("Cannot load class " + clsName, cnfex);
        }
    }

    /**
     * Gets a string representation of the key/value mappings of a configuration. This version of the method exists only for
     * backwards compatibility reason.
     *
     * @param configuration the configuration.
     * @return a string representation of the configuration.
     */
    public static String toString(final Configuration configuration) {
        return toString((ImmutableConfiguration) configuration);
    }

    /**
     * Gets a string representation of the key/value mappings of a configuration.
     *
     * @param configuration the configuration.
     * @return a string representation of the configuration.
     * @since 2.2
     */
    public static String toString(final ImmutableConfiguration configuration) {
        final StringWriter writer = new StringWriter();
        dump(configuration, new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Creates an {@code ImmutableConfiguration} from the given {@code Configuration} object. This method creates a proxy
     * object wrapping the original configuration and making it available under the {@code ImmutableConfiguration}
     * interface. Through this interface the configuration cannot be manipulated. It is also not possible to cast the
     * returned object back to a {@code Configuration} instance to circumvent this protection.
     *
     * @param c the {@code Configuration} to be wrapped (must not be <strong>null</strong>).
     * @return an {@code ImmutableConfiguration} view on the specified {@code Configuration} object.
     * @throws NullPointerException if the passed in {@code Configuration} is <strong>null</strong>.
     * @since 2.0
     */
    public static ImmutableConfiguration unmodifiableConfiguration(final Configuration c) {
        return createUnmodifiableConfiguration(IMMUTABLE_CONFIG_IFCS, c);
    }

    /**
     * Creates an {@code ImmutableHierarchicalConfiguration} from the given {@code HierarchicalConfiguration} object. This
     * method works exactly like the method with the same name, but it operates on hierarchical configurations.
     *
     * @param c the {@code HierarchicalConfiguration} to be wrapped (must not be <strong>null</strong>).
     * @return an {@code ImmutableHierarchicalConfiguration} view on the specified {@code HierarchicalConfiguration} object.
     * @throws NullPointerException if the passed in {@code HierarchicalConfiguration} is <strong>null</strong>.
     * @since 2.0
     */
    public static ImmutableHierarchicalConfiguration unmodifiableConfiguration(final HierarchicalConfiguration<?> c) {
        return (ImmutableHierarchicalConfiguration) createUnmodifiableConfiguration(IMMUTABLE_HIERARCHICAL_CONFIG_IFCS, c);
    }

    /**
     * Private constructor. Prevents instances from being created.
     */
    private ConfigurationUtils() {
        // Prevents instantiation.
    }
}
