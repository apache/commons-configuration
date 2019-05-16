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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

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
 *
 * @author <a href="mailto:herve.quiroz@esil.univ-mrs.fr">Herve Quiroz</a>
 * @author Emmanuel Bourg
 */
public final class ConfigurationUtils
{
    /** Constant for the name of the clone() method.*/
    private static final String METHOD_CLONE = "clone";

    /**
     * An array with interfaces to be implemented by a proxy for an immutable
     * configuration.
     */
    private static final Class<?>[] IMMUTABLE_CONFIG_IFCS = {
        ImmutableConfiguration.class
    };

    /**
     * An array with interfaces to be implemented by a proxy for an immutable
     * hierarchical configuration.
     */
    private static final Class<?>[] IMMUTABLE_HIERARCHICAL_CONFIG_IFCS = {
        ImmutableHierarchicalConfiguration.class
    };
    /**
     * A dummy event source that is returned by {@code asEventSource()} if a
     * mock object has to be returned. It provides empty dummy implementations
     * for all interface methods.
     */
    private static final EventSource DUMMY_EVENT_SOURCE = new EventSource()
    {

        @Override
        public <T extends Event> void addEventListener(final EventType<T> eventType,
                final EventListener<? super T> listener)
        {
        }

        @Override
        public <T extends Event> boolean removeEventListener(
                final EventType<T> eventType, final EventListener<? super T> listener)
        {
            return false;
        }
    };

    /** The logger.*/
    private static final Log LOG = LogFactory.getLog(ConfigurationUtils.class);

    /**
     * Private constructor. Prevents instances from being created.
     */
    private ConfigurationUtils()
    {
        // to prevent instantiation...
    }

    /**
     * Dump the configuration key/value mappings to some ouput stream.
     *
     * @param configuration the configuration
     * @param out the output stream to dump the configuration to
     * @since 2.2
     */
    public static void dump(final ImmutableConfiguration configuration, final PrintStream out)
    {
        dump(configuration, new PrintWriter(out));
    }

    /**
     * Dump the configuration key/value mappings to some ouput stream.
     * This version of the method exists only for backwards compatibility reason.
     *
     * @param configuration the configuration
     * @param out the output stream to dump the configuration to
     */
    public static void dump(final Configuration configuration, final PrintStream out)
    {
        dump((ImmutableConfiguration) configuration, out);
    }

    /**
     * Dump the configuration key/value mappings to some writer.
     *
     * @param configuration the configuration
     * @param out the writer to dump the configuration to
     * @since 2.2
     */
    public static void dump(final ImmutableConfiguration configuration, final PrintWriter out)
    {
        for (final Iterator<String> keys = configuration.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final Object value = configuration.getProperty(key);
            out.print(key);
            out.print("=");
            out.print(value);

            if (keys.hasNext())
            {
                out.println();
            }
        }

        out.flush();
    }

    /**
     * Dump the configuration key/value mappings to some writer.
     * This version of the method exists only for backwards compatibility reason.
     *
     * @param configuration the configuration
     * @param out the writer to dump the configuration to
     */
    public static void dump(final Configuration configuration, final PrintWriter out)
    {
        dump((ImmutableConfiguration) configuration, out);
    }

    /**
     * Get a string representation of the key/value mappings of a
     * configuration.
     *
     * @param configuration the configuration
     * @return a string representation of the configuration
     * @since 2.2
     */
    public static String toString(final ImmutableConfiguration configuration)
    {
        final StringWriter writer = new StringWriter();
        dump(configuration, new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Get a string representation of the key/value mappings of a
     * configuration.
     * This version of the method exists only for backwards compatibility reason.
     *
     * @param configuration the configuration
     * @return a string representation of the configuration
     */
    public static String toString(final Configuration configuration)
    {
        return toString((ImmutableConfiguration) configuration);
    }

    /**
     * <p>Copy all properties from the source configuration to the target
     * configuration. Properties in the target configuration are replaced with
     * the properties with the same key in the source configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from {@code AbstractConfiguration} (e.g.
     * list delimiters). For a full support of all of these features the
     * {@code copy()} method of {@code AbstractConfiguration} should
     * be used. In a future release this method might become deprecated.</p>
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 2.2
     */
    public static void copy(final ImmutableConfiguration source, final Configuration target)
    {
        for (final Iterator<String> keys = source.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            target.setProperty(key, source.getProperty(key));
        }
    }

    /**
     * <p>Copy all properties from the source configuration to the target
     * configuration. Properties in the target configuration are replaced with
     * the properties with the same key in the source configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from {@code AbstractConfiguration} (e.g.
     * list delimiters). For a full support of all of these features the
     * {@code copy()} method of {@code AbstractConfiguration} should
     * be used. In a future release this method might become deprecated.</p>
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 1.1
     */
    public static void copy(final Configuration source, final Configuration target)
    {
        copy((ImmutableConfiguration) source, target);
    }

    /**
     * <p>Append all properties from the source configuration to the target
     * configuration. Properties in the source configuration are appended to
     * the properties with the same key in the target configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from {@code AbstractConfiguration} (e.g.
     * list delimiters). For a full support of all of these features the
     * {@code copy()} method of {@code AbstractConfiguration} should
     * be used. In a future release this method might become deprecated.</p>
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 2.2
     */
    public static void append(final ImmutableConfiguration source, final Configuration target)
    {
        for (final Iterator<String> keys = source.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            target.addProperty(key, source.getProperty(key));
        }
    }

    /**
     * <p>Append all properties from the source configuration to the target
     * configuration. Properties in the source configuration are appended to
     * the properties with the same key in the target configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from {@code AbstractConfiguration} (e.g.
     * list delimiters). For a full support of all of these features the
     * {@code copy()} method of {@code AbstractConfiguration} should
     * be used. In a future release this method might become deprecated.</p>
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 1.1
     */
    public static void append(final Configuration source, final Configuration target)
    {
        append((ImmutableConfiguration) source, target);
    }

    /**
     * Converts the passed in configuration to a hierarchical one. If the
     * configuration is already hierarchical, it is directly returned. Otherwise
     * all properties are copied into a new hierarchical configuration.
     *
     * @param conf the configuration to convert
     * @return the new hierarchical configuration (the result is <b>null</b> if
     * and only if the passed in configuration is <b>null</b>)
     * @since 1.3
     */
    public static HierarchicalConfiguration<?> convertToHierarchical(
            final Configuration conf)
    {
        return convertToHierarchical(conf, null);
    }

    /**
     * Converts the passed in {@code Configuration} object to a
     * hierarchical one using the specified {@code ExpressionEngine}. This
     * conversion works by adding the keys found in the configuration to a newly
     * created hierarchical configuration. When adding new keys to a
     * hierarchical configuration the keys are interpreted by its
     * {@code ExpressionEngine}. If they contain special characters (e.g.
     * brackets) that are treated in a special way by the default expression
     * engine, it may be necessary using a specific engine that can deal with
     * such characters. Otherwise <b>null</b> can be passed in for the
     * {@code ExpressionEngine}; then the default expression engine is
     * used. If the passed in configuration is already hierarchical, it is
     * directly returned. (However, the {@code ExpressionEngine} is set if
     * it is not <b>null</b>.) Otherwise all properties are copied into a new
     * hierarchical configuration.
     *
     * @param conf the configuration to convert
     * @param engine the {@code ExpressionEngine} for the hierarchical
     *        configuration or <b>null</b> for the default
     * @return the new hierarchical configuration (the result is <b>null</b> if
     *         and only if the passed in configuration is <b>null</b>)
     * @since 1.6
     */
    public static HierarchicalConfiguration<?> convertToHierarchical(
            final Configuration conf, final ExpressionEngine engine)
    {
        if (conf == null)
        {
            return null;
        }

        if (conf instanceof HierarchicalConfiguration)
        {
            final HierarchicalConfiguration<?> hc = (HierarchicalConfiguration<?>) conf;
            if (engine != null)
            {
                hc.setExpressionEngine(engine);
            }

            return hc;
        }
        final BaseHierarchicalConfiguration hc = new BaseHierarchicalConfiguration();
        if (engine != null)
        {
            hc.setExpressionEngine(engine);
        }

        // Per default, a DisabledListDelimiterHandler is set.
        // So list delimiters in property values are not an issue.
        hc.copy(conf);
        return hc;
    }

    /**
     * Clones the given configuration object if this is possible. If the passed
     * in configuration object implements the {@code Cloneable}
     * interface, its {@code clone()} method will be invoked. Otherwise
     * an exception will be thrown.
     *
     * @param config the configuration object to be cloned (can be <b>null</b>)
     * @return the cloned configuration (<b>null</b> if the argument was
     * <b>null</b>, too)
     * @throws ConfigurationRuntimeException if cloning is not supported for
     * this object
     * @since 1.3
     */
    public static Configuration cloneConfiguration(final Configuration config)
            throws ConfigurationRuntimeException
    {
        if (config == null)
        {
            return null;
        }
        try
        {
            return (Configuration) clone(config);
        }
        catch (final CloneNotSupportedException cnex)
        {
            throw new ConfigurationRuntimeException(cnex);
        }
    }

    /**
     * Returns a clone of the passed in object if cloning is supported or the
     * object itself if not. This method checks whether the passed in object
     * implements the {@code Cloneable} interface. If this is the case, the
     * {@code clone()} method is invoked. Otherwise, the object is directly
     * returned. Errors that might occur during reflection calls are caught and
     * also cause this method to return the original object.
     *
     * @param obj the object to be cloned
     * @return the result of the cloning attempt
     * @since 2.0
     */
    public static Object cloneIfPossible(final Object obj)
    {
        try
        {
            return clone(obj);
        }
        catch (final Exception ex)
        {
            return obj;
        }
    }

    /**
     * An internally used helper method for cloning objects. This implementation
     * is not very sophisticated nor efficient. Maybe it can be replaced by an
     * implementation from Commons Lang later. The method checks whether the
     * passed in object implements the {@code Cloneable} interface. If
     * this is the case, the {@code clone()} method is invoked by
     * reflection. Errors that occur during the cloning process are re-thrown as
     * runtime exceptions.
     *
     * @param obj the object to be cloned
     * @return the cloned object
     * @throws CloneNotSupportedException if the object cannot be cloned
     */
    static Object clone(final Object obj) throws CloneNotSupportedException
    {
        if (obj instanceof Cloneable)
        {
            try
            {
                final Method m = obj.getClass().getMethod(METHOD_CLONE);
                return m.invoke(obj);
            }
            catch (final NoSuchMethodException nmex)
            {
                throw new CloneNotSupportedException(
                        "No clone() method found for class"
                                + obj.getClass().getName());
            }
            catch (final IllegalAccessException iaex)
            {
                throw new ConfigurationRuntimeException(iaex);
            }
            catch (final InvocationTargetException itex)
            {
                throw new ConfigurationRuntimeException(itex);
            }
        }
        throw new CloneNotSupportedException(obj.getClass().getName()
                + " does not implement Cloneable");
    }

    /**
     * Creates a clone of the specified {@code Synchronizer}. This method can be
     * called by {@code clone()} implementations in configuration classes that
     * also need to copy the {@code Synchronizer} object. This method can handle
     * some well-known {@code Synchronizer} implementations directly. For other
     * classes, it uses the following algorithm:
     * <ul>
     * <li>If the class of the {@code Synchronizer} has a standard constructor,
     * a new instance is created using reflection.</li>
     * <li>If this is not possible, it is tried whether the object can be
     * cloned.</li>
     * </ul>
     * If all attempts fail, a {@code ConfigurationRuntimeException} is thrown.
     *
     * @param sync the {@code Synchronizer} object to be cloned
     * @return the clone of this {@code Synchronizer}
     * @throws ConfigurationRuntimeException if no clone can be created
     * @throws IllegalArgumentException if <b>null</b> is passed in
     */
    public static Synchronizer cloneSynchronizer(final Synchronizer sync)
    {
        if (sync == null)
        {
            throw new IllegalArgumentException("Synchronizer must not be null!");
        }
        if (NoOpSynchronizer.INSTANCE == sync)
        {
            return sync;
        }

        try
        {
            return sync.getClass().newInstance();
        }
        catch (final Exception ex)
        {
            LOG.info("Cannot create new instance of " + sync.getClass());
        }

        try
        {
            return (Synchronizer) clone(sync);
        }
        catch (final CloneNotSupportedException cnex)
        {
            throw new ConfigurationRuntimeException(
                    "Cannot clone Synchronizer " + sync);
        }
    }

    /**
     * Enables runtime exceptions for the specified configuration object. This
     * method can be used for configuration implementations that may face errors
     * on normal property access, e.g. {@code DatabaseConfiguration} or
     * {@code JNDIConfiguration}. Per default such errors are simply
     * logged and then ignored. This implementation will register a special
     * {@link EventListener} that throws a runtime
     * exception (namely a {@code ConfigurationRuntimeException}) on
     * each received error event.
     *
     * @param src the configuration, for which runtime exceptions are to be
     * enabled; this configuration must implement {@link EventSource}
     */
    public static void enableRuntimeExceptions(final Configuration src)
    {
        if (!(src instanceof EventSource))
        {
            throw new IllegalArgumentException(
                    "Configuration must implement EventSource!");
        }
        ((EventSource) src).addEventListener(ConfigurationErrorEvent.ANY,
                new EventListener<ConfigurationErrorEvent>()
                {
                    @Override
                    public void onEvent(final ConfigurationErrorEvent event)
                    {
                        // Throw a runtime exception
                        throw new ConfigurationRuntimeException(event
                                .getCause());
                    }
                });
    }

    /**
     * Loads the class with the given name. This method is used whenever a class
     * has to be loaded dynamically. It first tries the current thread's context
     * class loader. If this fails, the class loader of this class is tried.
     *
     * @param clsName the name of the class to be loaded
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be resolved
     * @since 2.0
     */
    public static Class<?> loadClass(final String clsName)
            throws ClassNotFoundException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading class " + clsName);
        }

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            if (cl != null)
            {
                return cl.loadClass(clsName);
            }
        }
        catch (final ClassNotFoundException cnfex)
        {
            LOG.info("Could not load class " + clsName
                    + " using CCL. Falling back to default CL.", cnfex);
        }

        return ConfigurationUtils.class.getClassLoader().loadClass(clsName);
    }

    /**
     * Loads the class with the specified name re-throwing
     * {@code ClassNotFoundException} exceptions as runtime exceptions. This
     * method works like {@link #loadClass(String)}. However, checked exceptions
     * are caught and re-thrown as {@code ConfigurationRuntimeException}.
     *
     * @param clsName the name of the class to be loaded
     * @return the loaded class
     * @throws ConfigurationRuntimeException if the class cannot be resolved
     * @since 2.0
     */
    public static Class<?> loadClassNoEx(final String clsName)
    {
        try
        {
            return loadClass(clsName);
        }
        catch (final ClassNotFoundException cnfex)
        {
            throw new ConfigurationRuntimeException("Cannot load class "
                    + clsName, cnfex);
        }
    }

    /**
     * Creates an {@code ImmutableConfiguration} from the given
     * {@code Configuration} object. This method creates a proxy object wrapping
     * the original configuration and making it available under the
     * {@code ImmutableConfiguration} interface. Through this interface the
     * configuration cannot be manipulated. It is also not possible to cast the
     * returned object back to a {@code Configuration} instance to circumvent
     * this protection.
     *
     * @param c the {@code Configuration} to be wrapped (must not be
     *        <b>null</b>)
     * @return an {@code ImmutableConfiguration} view on the specified
     *         {@code Configuration} object
     * @throws NullPointerException if the passed in {@code Configuration} is
     *         <b>null</b>
     * @since 2.0
     */
    public static ImmutableConfiguration unmodifiableConfiguration(
            final Configuration c)
    {
        return createUnmodifiableConfiguration(IMMUTABLE_CONFIG_IFCS, c);
    }

    /**
     * Creates an {@code ImmutableHierarchicalConfiguration} from the given
     * {@code HierarchicalConfiguration} object. This method works exactly like
     * the method with the same name, but it operates on hierarchical
     * configurations.
     *
     * @param c the {@code HierarchicalConfiguration} to be wrapped (must not be
     *        <b>null</b>)
     * @return an {@code ImmutableHierarchicalConfiguration} view on the
     *         specified {@code HierarchicalConfiguration} object
     * @throws NullPointerException if the passed in
     *         {@code HierarchicalConfiguration} is <b>null</b>
     * @since 2.0
     */
    public static ImmutableHierarchicalConfiguration unmodifiableConfiguration(
            final HierarchicalConfiguration<?> c)
    {
        return (ImmutableHierarchicalConfiguration) createUnmodifiableConfiguration(
                IMMUTABLE_HIERARCHICAL_CONFIG_IFCS, c);
    }

    /**
     * Helper method for creating a proxy for an unmodifiable configuration. The
     * interfaces the proxy should implement are passed as argument.
     *
     * @param ifcs an array with the interface classes the proxy must implement
     * @param c the configuration object to be wrapped
     * @return a proxy object for an immutable configuration
     * @throws NullPointerException if the configuration is <b>null</b>
     */
    private static ImmutableConfiguration createUnmodifiableConfiguration(
            final Class<?>[] ifcs, final Configuration c)
    {
        return (ImmutableConfiguration) Proxy.newProxyInstance(
                ConfigurationUtils.class.getClassLoader(), ifcs,
                new ImmutableConfigurationInvocationHandler(c));
    }

    /**
     * Casts the specified object to an {@code EventSource} if possible. The
     * boolean argument determines the method's behavior if the object does not
     * implement the {@code EventSource} event: if set to <b>false</b>, a
     * {@code ConfigurationRuntimeException} is thrown; if set to <b>true</b>, a
     * dummy {@code EventSource} is returned; on this object all methods can be
     * called, but they do not have any effect.
     *
     * @param obj the object to be cast as {@code EventSource}
     * @param mockIfUnsupported a flag whether a mock object should be returned
     *        if necessary
     * @return an {@code EventSource}
     * @throws ConfigurationRuntimeException if the object cannot be cast to
     *         {@code EventSource} and the mock flag is <b>false</b>
     * @since 2.0
     */
    public static EventSource asEventSource(final Object obj,
            final boolean mockIfUnsupported)
    {
        if (obj instanceof EventSource)
        {
            return (EventSource) obj;
        }

        if (!mockIfUnsupported)
        {
            throw new ConfigurationRuntimeException(
                    "Cannot cast to EventSource: " + obj);
        }
        return DUMMY_EVENT_SOURCE;
    }
}
