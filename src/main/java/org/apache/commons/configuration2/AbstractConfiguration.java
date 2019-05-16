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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.event.BaseEventSource;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.InterpolatorSpecification;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.sync.NoOpSynchronizer;
import org.apache.commons.configuration2.sync.Synchronizer;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>Abstract configuration class. Provides basic functionality but does not
 * store any data.</p>
 * <p>If you want to write your own Configuration class then you should
 * implement only abstract methods from this class. A lot of functionality
 * needed by typical implementations of the {@code Configuration}
 * interface is already provided by this base class. Following is a list of
 * features implemented here:</p>
 * <ul><li>Data conversion support. The various data types required by the
 * {@code Configuration} interface are already handled by this base class.
 * A concrete sub class only needs to provide a generic {@code getProperty()}
 * method.</li>
 * <li>Support for variable interpolation. Property values containing special
 * variable tokens (like <code>${var}</code>) will be replaced by their
 * corresponding values.</li>
 * <li>Optional support for string lists. The values of properties to be added to this
 * configuration are checked whether they contain a list delimiter character. If
 * this is the case and if list splitting is enabled, the string is split and
 * multiple values are added for this property. List splitting is controlled
 * by a {@link ListDelimiterHandler} object which can be set using the
 * {@link #setListDelimiterHandler(ListDelimiterHandler)} method. It is
 * disabled per default. To enable this feature, set a suitable
 * {@code ListDelimiterHandler}, e.g. an instance of
 * {@link org.apache.commons.configuration2.convert.DefaultListDelimiterHandler
 * DefaultListDelimiterHandler} configured with the desired list delimiter character.</li>
 * <li>Allows specifying how missing properties are treated. Per default the
 * get methods returning an object will return <b>null</b> if the searched
 * property key is not found (and no default value is provided). With the
 * {@code setThrowExceptionOnMissing()} method this behavior can be
 * changed to throw an exception when a requested property cannot be found.</li>
 * <li>Basic event support. Whenever this configuration is modified registered
 * event listeners are notified. Refer to the various {@code EVENT_XXX}
 * constants to get an impression about which event types are supported.</li>
 * <li>Support for proper synchronization based on the {@link Synchronizer}
 * interface.</li>
 * </ul>
 * <p>
 * Most methods defined by the {@code Configuration} interface are already
 * implemented in this class. Many method implementations perform basic
 * book-keeping tasks (e.g. firing events, handling synchronization), and then
 * delegate to other (protected) methods executing the actual work. Subclasses
 * override these protected methods to define or adapt behavior. The public
 * entry point methods are final to prevent subclasses from breaking basic
 * functionality.
 * </p>
 *
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov </a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen </a>
 */
public abstract class AbstractConfiguration extends BaseEventSource implements Configuration
{
    /** The list delimiter handler. */
    private ListDelimiterHandler listDelimiterHandler;

    /** The conversion handler. */
    private ConversionHandler conversionHandler;

    /**
     * Whether the configuration should throw NoSuchElementExceptions or simply
     * return null when a property does not exist. Defaults to return null.
     */
    private boolean throwExceptionOnMissing;

    /** Stores a reference to the object that handles variable interpolation. */
    private AtomicReference<ConfigurationInterpolator> interpolator;

    /** The object responsible for synchronization. */
    private volatile Synchronizer synchronizer;

    /** The object used for dealing with encoded property values. */
    private ConfigurationDecoder configurationDecoder;

    /** Stores the logger.*/
    private ConfigurationLogger log;

    /**
     * Creates a new instance of {@code AbstractConfiguration}.
     */
    public AbstractConfiguration()
    {
        interpolator = new AtomicReference<>();
        initLogger(null);
        installDefaultInterpolator();
        listDelimiterHandler = DisabledListDelimiterHandler.INSTANCE;
        conversionHandler = DefaultConversionHandler.INSTANCE;
    }

    /**
     * Returns the {@code ListDelimiterHandler} used by this instance.
     *
     * @return the {@code ListDelimiterHandler}
     * @since 2.0
     */
    public ListDelimiterHandler getListDelimiterHandler()
    {
        return listDelimiterHandler;
    }

    /**
     * <p>
     * Sets the {@code ListDelimiterHandler} to be used by this instance. This
     * object is invoked every time when dealing with string properties that may
     * contain a list delimiter and thus have to be split to multiple values.
     * Per default, a {@code ListDelimiterHandler} implementation is set which
     * does not support list splitting. This can be changed for instance by
     * setting a {@link org.apache.commons.configuration2.convert.DefaultListDelimiterHandler
     * DefaultListDelimiterHandler} object.
     * </p>
     * <p>
     * <strong>Warning:</strong> Be careful when changing the list delimiter
     * handler when the configuration has already been loaded/populated. List
     * handling is typically applied already when properties are added to the
     * configuration. If later another handler is set which processes lists
     * differently, results may be unexpected; some operations may even cause
     * exceptions.
     * </p>
     *
     * @param listDelimiterHandler the {@code ListDelimiterHandler} to be used
     *        (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code ListDelimiterHandler} is
     *         <b>null</b>
     * @since 2.0
     */
    public void setListDelimiterHandler(
            final ListDelimiterHandler listDelimiterHandler)
    {
        if (listDelimiterHandler == null)
        {
            throw new IllegalArgumentException(
                    "List delimiter handler must not be null!");
        }
        this.listDelimiterHandler = listDelimiterHandler;
    }

    /**
     * Returns the {@code ConversionHandler} used by this instance.
     *
     * @return the {@code ConversionHandler}
     * @since 2.0
     */
    public ConversionHandler getConversionHandler()
    {
        return conversionHandler;
    }

    /**
     * Sets the {@code ConversionHandler} to be used by this instance. The
     * {@code ConversionHandler} is responsible for every kind of data type
     * conversion. It is consulted by all get methods returning results in
     * specific data types. A newly created configuration uses a default
     * {@code ConversionHandler} implementation. This can be changed while
     * initializing the configuration (e.g. via a builder). Note that access to
     * this property is not synchronized.
     *
     * @param conversionHandler the {@code ConversionHandler} to be used (must
     *        not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code ConversionHandler} is
     *         <b>null</b>
     * @since 2.0
     */
    public void setConversionHandler(final ConversionHandler conversionHandler)
    {
        if (conversionHandler == null)
        {
            throw new IllegalArgumentException(
                    "ConversionHandler must not be null!");
        }
        this.conversionHandler = conversionHandler;
    }

    /**
     * Allows to set the {@code throwExceptionOnMissing} flag. This
     * flag controls the behavior of property getter methods that return
     * objects if the requested property is missing. If the flag is set to
     * <b>false</b> (which is the default value), these methods will return
     * <b>null</b>. If set to <b>true</b>, they will throw a
     * {@code NoSuchElementException} exception. Note that getter methods
     * for primitive data types are not affected by this flag.
     *
     * @param throwExceptionOnMissing The new value for the property
     */
    public void setThrowExceptionOnMissing(final boolean throwExceptionOnMissing)
    {
        this.throwExceptionOnMissing = throwExceptionOnMissing;
    }

    /**
     * Returns true if missing values throw Exceptions.
     *
     * @return true if missing values throw Exceptions
     */
    public boolean isThrowExceptionOnMissing()
    {
        return throwExceptionOnMissing;
    }

    /**
     * Returns the {@code ConfigurationInterpolator} object that manages the
     * lookup objects for resolving variables.
     *
     * @return the {@code ConfigurationInterpolator} associated with this
     *         configuration
     * @since 1.4
     */
    @Override
    public ConfigurationInterpolator getInterpolator()
    {
        return interpolator.get();
    }

    /**
     * {@inheritDoc} This implementation sets the passed in object without
     * further modifications. A <b>null</b> argument is allowed; this disables
     * interpolation.
     *
     * @since 2.0
     */
    @Override
    public final void setInterpolator(final ConfigurationInterpolator ci)
    {
        interpolator.set(ci);
    }

    /**
     * {@inheritDoc} This implementation creates a new
     * {@code ConfigurationInterpolator} instance and initializes it with the
     * given {@code Lookup} objects. In addition, it adds a specialized default
     * {@code Lookup} object which queries this {@code Configuration}.
     *
     * @since 2.0
     */
    @Override
    public final void installInterpolator(
            final Map<String, ? extends Lookup> prefixLookups,
            final Collection<? extends Lookup> defLookups)
    {
        final InterpolatorSpecification spec =
                new InterpolatorSpecification.Builder()
                        .withPrefixLookups(prefixLookups)
                        .withDefaultLookups(defLookups)
                        .withDefaultLookup(new ConfigurationLookup(this))
                        .create();
        setInterpolator(ConfigurationInterpolator.fromSpecification(spec));
    }

    /**
     * Registers all {@code Lookup} objects in the given map at the current
     * {@code ConfigurationInterpolator} of this configuration. The set of
     * default lookup objects (for variables without a prefix) is not modified
     * by this method. If this configuration does not have a
     * {@code ConfigurationInterpolator}, a new instance is created. Note: This
     * method is mainly intended to be used for initializing a configuration
     * when it is created by a builder. Normal client code should better call
     * {@link #installInterpolator(Map, Collection)} to define the
     * {@code ConfigurationInterpolator} in a single step.
     *
     * @param lookups a map with new {@code Lookup} objects and their prefixes
     *        (may be <b>null</b>)
     * @since 2.0
     */
    public void setPrefixLookups(final Map<String, ? extends Lookup> lookups)
    {
        boolean success;
        do
        {
            // do this in a loop because the ConfigurationInterpolator
            // instance may be changed by another thread
            final ConfigurationInterpolator ciOld = getInterpolator();
            final ConfigurationInterpolator ciNew =
                    (ciOld != null) ? ciOld : new ConfigurationInterpolator();
            ciNew.registerLookups(lookups);
            success = interpolator.compareAndSet(ciOld, ciNew);
        } while (!success);
    }

    /**
     * Adds all {@code Lookup} objects in the given collection as default
     * lookups (i.e. lookups without a variable prefix) to the
     * {@code ConfigurationInterpolator} object of this configuration. In
     * addition, it adds a specialized default {@code Lookup} object which
     * queries this {@code Configuration}. The set of {@code Lookup} objects
     * with prefixes is not modified by this method. If this configuration does
     * not have a {@code ConfigurationInterpolator}, a new instance is created.
     * Note: This method is mainly intended to be used for initializing a
     * configuration when it is created by a builder. Normal client code should
     * better call {@link #installInterpolator(Map, Collection)} to define the
     * {@code ConfigurationInterpolator} in a single step.
     *
     * @param lookups the collection with default {@code Lookup} objects to be
     *        added
     * @since 2.0
     */
    public void setDefaultLookups(final Collection<? extends Lookup> lookups)
    {
        boolean success;
        do
        {
            final ConfigurationInterpolator ciOld = getInterpolator();
            final ConfigurationInterpolator ciNew =
                    (ciOld != null) ? ciOld : new ConfigurationInterpolator();
            Lookup confLookup = findConfigurationLookup(ciNew);
            if (confLookup == null)
            {
                confLookup = new ConfigurationLookup(this);
            }
            else
            {
                ciNew.removeDefaultLookup(confLookup);
            }
            ciNew.addDefaultLookups(lookups);
            ciNew.addDefaultLookup(confLookup);
            success = interpolator.compareAndSet(ciOld, ciNew);
        } while (!success);
    }

    /**
     * Sets the specified {@code ConfigurationInterpolator} as the parent of
     * this configuration's {@code ConfigurationInterpolator}. If this
     * configuration does not have a {@code ConfigurationInterpolator}, a new
     * instance is created. Note: This method is mainly intended to be used for
     * initializing a configuration when it is created by a builder. Normal
     * client code can directly update the {@code ConfigurationInterpolator}.
     *
     * @param parent the parent {@code ConfigurationInterpolator} to be set
     * @since 2.0
     */
    public void setParentInterpolator(final ConfigurationInterpolator parent)
    {
        boolean success;
        do
        {
            final ConfigurationInterpolator ciOld = getInterpolator();
            final ConfigurationInterpolator ciNew =
                    (ciOld != null) ? ciOld : new ConfigurationInterpolator();
            ciNew.setParentInterpolator(parent);
            success = interpolator.compareAndSet(ciOld, ciNew);
        } while (!success);
    }

    /**
     * Sets the {@code ConfigurationDecoder} for this configuration. This object
     * is used by {@link #getEncodedString(String)}.
     *
     * @param configurationDecoder the {@code ConfigurationDecoder}
     * @since 2.0
     */
    public void setConfigurationDecoder(
            final ConfigurationDecoder configurationDecoder)
    {
        this.configurationDecoder = configurationDecoder;
    }

    /**
     * Returns the {@code ConfigurationDecoder} used by this instance.
     *
     * @return the {@code ConfigurationDecoder}
     * @since 2.0
     */
    public ConfigurationDecoder getConfigurationDecoder()
    {
        return configurationDecoder;
    }

    /**
     * Creates a clone of the {@code ConfigurationInterpolator} used by this
     * instance. This method can be called by {@code clone()} implementations of
     * derived classes. Normally, the {@code ConfigurationInterpolator} of a
     * configuration instance must not be shared with other instances because it
     * contains a specific {@code Lookup} object pointing to the owning
     * configuration. This has to be taken into account when cloning a
     * configuration. This method creates a new
     * {@code ConfigurationInterpolator} for this configuration instance which
     * contains all lookup objects from the original
     * {@code ConfigurationInterpolator} except for the configuration specific
     * lookup pointing to the passed in original configuration. This one is
     * replaced by a corresponding {@code Lookup} referring to this
     * configuration.
     *
     * @param orgConfig the original configuration from which this one was
     *        cloned
     * @since 2.0
     */
    protected void cloneInterpolator(final AbstractConfiguration orgConfig)
    {
        interpolator = new AtomicReference<>();
        final ConfigurationInterpolator orgInterpolator = orgConfig.getInterpolator();
        final List<Lookup> defaultLookups = orgInterpolator.getDefaultLookups();
        final Lookup lookup = findConfigurationLookup(orgInterpolator, orgConfig);
        if (lookup != null)
        {
            defaultLookups.remove(lookup);
        }

        installInterpolator(orgInterpolator.getLookups(), defaultLookups);
    }

    /**
     * Creates a default {@code ConfigurationInterpolator} which is initialized
     * with all default {@code Lookup} objects. This method is called by the
     * constructor. It ensures that default interpolation works for every new
     * configuration instance.
     */
    private void installDefaultInterpolator()
    {
        installInterpolator(
                ConfigurationInterpolator.getDefaultPrefixLookups(), null);
    }

    /**
     * Finds a {@code ConfigurationLookup} pointing to this configuration in the
     * default lookups of the specified {@code ConfigurationInterpolator}. This
     * method is called to ensure that there is exactly one default lookup
     * querying this configuration.
     *
     * @param ci the {@code ConfigurationInterpolator} in question
     * @return the found {@code Lookup} object or <b>null</b>
     */
    private Lookup findConfigurationLookup(final ConfigurationInterpolator ci)
    {
        return findConfigurationLookup(ci, this);
    }

    /**
     * Finds a {@code ConfigurationLookup} pointing to the specified
     * configuration in the default lookups for the specified
     * {@code ConfigurationInterpolator}.
     *
     * @param ci the {@code ConfigurationInterpolator} in question
     * @param targetConf the target configuration of the searched lookup
     * @return the found {@code Lookup} object or <b>null</b>
     */
    private static Lookup findConfigurationLookup(final ConfigurationInterpolator ci,
            final ImmutableConfiguration targetConf)
    {
        for (final Lookup l : ci.getDefaultLookups())
        {
            if (l instanceof ConfigurationLookup)
            {
                if (targetConf == ((ConfigurationLookup) l).getConfiguration())
                {
                    return l;
                }
            }
        }
        return null;
    }

    /**
     * Returns the logger used by this configuration object.
     *
     * @return the logger
     * @since 2.0
     */
    public ConfigurationLogger getLogger()
    {
        return log;
    }

    /**
     * Allows setting the logger to be used by this configuration object. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used. It is legal to pass a
     * <b>null</b> logger; in this case, logging will be disabled.
     *
     * @param log the new logger
     * @since 2.0
     */
    public void setLogger(final ConfigurationLogger log)
    {
        initLogger(log);
    }

    /**
     * Adds a special {@link EventListener} object to this configuration that
     * will log all internal errors. This method is intended to be used by
     * certain derived classes, for which it is known that they can fail on
     * property access (e.g. {@code DatabaseConfiguration}).
     *
     * @since 1.4
     */
    public final void addErrorLogListener()
    {
        addEventListener(ConfigurationErrorEvent.ANY,
                new EventListener<ConfigurationErrorEvent>()
                {
                    @Override
                    public void onEvent(final ConfigurationErrorEvent event)
                    {
                        getLogger().warn("Internal error", event.getCause());
                    }
                });
    }

    /**
     * Returns the object responsible for synchronizing this configuration. All
     * access to this configuration - both read and write access - is controlled
     * by this object. This implementation never returns <b>null</b>. If no
     * {@code Synchronizer} has been set, a {@link NoOpSynchronizer} is
     * returned. So, per default, instances of {@code AbstractConfiguration} are
     * not thread-safe unless a suitable {@code Synchronizer} is set!
     *
     * @return the {@code Synchronizer} used by this instance
     * @since 2.0
     */
    @Override
    public final Synchronizer getSynchronizer()
    {
        final Synchronizer sync = synchronizer;
        return (sync != null) ? sync : NoOpSynchronizer.INSTANCE;
    }

    /**
     * Sets the object responsible for synchronizing this configuration. This
     * method has to be called with a suitable {@code Synchronizer} object when
     * initializing this configuration instance in order to make it thread-safe.
     *
     * @param synchronizer the new {@code Synchronizer}; can be <b>null</b>,
     *        then this instance uses a {@link NoOpSynchronizer}
     * @since 2.0
     */
    @Override
    public final void setSynchronizer(final Synchronizer synchronizer)
    {
        this.synchronizer = synchronizer;
    }

    /**
     * {@inheritDoc} This implementation delegates to {@code beginRead()} or
     * {@code beginWrite()}, depending on the {@code LockMode} argument.
     * Subclasses can override these protected methods to perform additional
     * steps when a configuration is locked.
     *
     * @since 2.0
     * @throws NullPointerException if the argument is <b>null</b>
     */
    @Override
    public final void lock(final LockMode mode)
    {
        switch (mode)
        {
        case READ:
            beginRead(false);
            break;
        case WRITE:
            beginWrite(false);
            break;
        default:
            throw new IllegalArgumentException("Unsupported LockMode: " + mode);
        }
    }

    /**
     * {@inheritDoc} This implementation delegates to {@code endRead()} or
     * {@code endWrite()}, depending on the {@code LockMode} argument.
     * Subclasses can override these protected methods to perform additional
     * steps when a configuration's lock is released.
     *
     * @throws NullPointerException if the argument is <b>null</b>
     */
    @Override
    public final void unlock(final LockMode mode)
    {
        switch (mode)
        {
        case READ:
            endRead();
            break;
        case WRITE:
            endWrite();
            break;
        default:
            throw new IllegalArgumentException("Unsupported LockMode: " + mode);
        }
    }

    /**
     * Notifies this configuration's {@link Synchronizer} that a read operation
     * is about to start. This method is called by all methods which access this
     * configuration in a read-only mode. Subclasses may override it to perform
     * additional actions before this read operation. The boolean
     * <em>optimize</em> argument can be evaluated by overridden methods in
     * derived classes. Some operations which require a lock do not need a fully
     * initialized configuration object. By setting this flag to
     * <strong>true</strong>, such operations can give a corresponding hint. An
     * overridden implementation of {@code beginRead()} can then decide to skip
     * some initialization steps. All basic operations in this class (and most
     * of the basic {@code Configuration} implementations) call this method with
     * a parameter value of <strong>false</strong>. <strong>In any case the
     * inherited method must be called! Otherwise, proper synchronization is not
     * guaranteed.</strong>
     *
     * @param optimize a flag whether optimization can be performed
     * @since 2.0
     */
    protected void beginRead(final boolean optimize)
    {
        getSynchronizer().beginRead();
    }

    /**
     * Notifies this configuration's {@link Synchronizer} that a read operation
     * has finished. This method is called by all methods which access this
     * configuration in a read-only manner at the end of their execution.
     * Subclasses may override it to perform additional actions after this read
     * operation. <strong>In any case the inherited method must be called!
     * Otherwise, the read lock will not be released.</strong>
     *
     * @since 2.0
     */
    protected void endRead()
    {
        getSynchronizer().endRead();
    }

    /**
     * Notifies this configuration's {@link Synchronizer} that an update
     * operation is about to start. This method is called by all methods which
     * modify this configuration. Subclasses may override it to perform
     * additional operations before an update. For a description of the boolean
     * <em>optimize</em> argument refer to the documentation of
     * {@code beginRead()}. <strong>In any case the inherited method must be
     * called! Otherwise, proper synchronization is not guaranteed.</strong>
     *
     * @param optimize a flag whether optimization can be performed
     * @see #beginRead(boolean)
     * @since 2.0
     */
    protected void beginWrite(final boolean optimize)
    {
        getSynchronizer().beginWrite();
    }

    /**
     * Notifies this configuration's {@link Synchronizer} that an update
     * operation has finished. This method is called by all methods which modify
     * this configuration at the end of their execution. Subclasses may override
     * it to perform additional operations after an update. <strong>In any case
     * the inherited method must be called! Otherwise, the write lock will not
     * be released.</strong>
     *
     * @since 2.0
     */
    protected void endWrite()
    {
        getSynchronizer().endWrite();
    }

    @Override
    public final void addProperty(final String key, final Object value)
    {
        beginWrite(false);
        try
        {
            fireEvent(ConfigurationEvent.ADD_PROPERTY, key, value, true);
            addPropertyInternal(key, value);
            fireEvent(ConfigurationEvent.ADD_PROPERTY, key, value, false);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Actually adds a property to this configuration. This method is called by
     * {@code addProperty()}. It performs list splitting if necessary and
     * delegates to {@link #addPropertyDirect(String, Object)} for every single
     * property value.
     *
     * @param key the key of the property to be added
     * @param value the new property value
     * @since 2.0
     */
    protected void addPropertyInternal(final String key, final Object value)
    {
        for (final Object obj : getListDelimiterHandler().parse(value))
        {
            addPropertyDirect(key, obj);
        }
    }

    /**
     * Adds a key/value pair to the Configuration. Override this method to
     * provide write access to underlying Configuration store.
     *
     * @param key key to use for mapping
     * @param value object to store
     */
    protected abstract void addPropertyDirect(String key, Object value);

    /**
     * interpolate key names to handle ${key} stuff
     *
     * @param base string to interpolate
     *
     * @return returns the key name with the ${key} substituted
     */
    protected String interpolate(final String base)
    {
        final Object result = interpolate((Object) base);
        return result == null ? null : result.toString();
    }

    /**
     * Returns the interpolated value. This implementation delegates to the
     * current {@code ConfigurationInterpolator}. If no
     * {@code ConfigurationInterpolator} is set, the passed in value is returned
     * without changes.
     *
     * @param value the value to interpolate
     * @return the value with variables substituted
     */
    protected Object interpolate(final Object value)
    {
        final ConfigurationInterpolator ci = getInterpolator();
        return ci != null ? ci.interpolate(value) : value;
    }

    @Override
    public Configuration subset(final String prefix)
    {
        return new SubsetConfiguration(this, prefix, ".");
    }

    @Override
    public ImmutableConfiguration immutableSubset(final String prefix)
    {
        return ConfigurationUtils.unmodifiableConfiguration(subset(prefix));
    }

    @Override
    public final void setProperty(final String key, final Object value)
    {
        beginWrite(false);
        try
        {
            fireEvent(ConfigurationEvent.SET_PROPERTY, key, value, true);
            setPropertyInternal(key, value);
            fireEvent(ConfigurationEvent.SET_PROPERTY, key, value, false);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Actually sets the value of a property. This method is called by
     * {@code setProperty()}. It provides a default implementation of this
     * functionality by clearing the specified key and delegating to
     * {@code addProperty()}. Subclasses should override this method if they can
     * provide a more efficient algorithm for setting a property value.
     *
     * @param key the property key
     * @param value the new property value
     * @since 2.0
     */
    protected void setPropertyInternal(final String key, final Object value)
    {
        setDetailEvents(false);
        try
        {
            clearProperty(key);
            addProperty(key, value);
        }
        finally
        {
            setDetailEvents(true);
        }
    }

    /**
     * Removes the specified property from this configuration. This
     * implementation performs some preparations and then delegates to
     * {@code clearPropertyDirect()}, which will do the real work.
     *
     * @param key the key to be removed
     */
    @Override
    public final void clearProperty(final String key)
    {
        beginWrite(false);
        try
        {
            fireEvent(ConfigurationEvent.CLEAR_PROPERTY, key, null, true);
            clearPropertyDirect(key);
            fireEvent(ConfigurationEvent.CLEAR_PROPERTY, key, null, false);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Removes the specified property from this configuration. This method is
     * called by {@code clearProperty()} after it has done some
     * preparations. It must be overridden in sub classes.
     *
     * @param key the key to be removed
     */
    protected abstract void clearPropertyDirect(String key);

    @Override
    public final void clear()
    {
        beginWrite(false);
        try
        {
            fireEvent(ConfigurationEvent.CLEAR, null, null, true);
            clearInternal();
            fireEvent(ConfigurationEvent.CLEAR, null, null, false);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Clears the whole configuration. This method is called by {@code clear()}
     * after some preparations have been made. This base implementation uses
     * the iterator provided by {@code getKeys()} to remove every single
     * property. Subclasses should override this method if there is a more
     * efficient way of clearing the configuration.
     */
    protected void clearInternal()
    {
        setDetailEvents(false);
        boolean useIterator = true;
        try
        {
            final Iterator<String> it = getKeys();
            while (it.hasNext())
            {
                final String key = it.next();
                if (useIterator)
                {
                    try
                    {
                        it.remove();
                    }
                    catch (final UnsupportedOperationException usoex)
                    {
                        useIterator = false;
                    }
                }

                if (useIterator && containsKey(key))
                {
                    useIterator = false;
                }

                if (!useIterator)
                {
                    // workaround for Iterators that do not remove the
                    // property
                    // on calling remove() or do not support remove() at all
                    clearProperty(key);
                }
            }
        }
        finally
        {
            setDetailEvents(true);
        }
    }

    /**
     * {@inheritDoc} This implementation takes care of synchronization and then
     * delegates to {@code getKeysInternal()} for obtaining the actual iterator.
     * Note that depending on a concrete implementation, an iteration may fail
     * if the configuration is updated concurrently.
     */
    @Override
    public final Iterator<String> getKeys()
    {
        beginRead(false);
        try
        {
            return getKeysInternal();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * {@inheritDoc} This implementation returns keys that either match the
     * prefix or start with the prefix followed by a dot ('.'). So the call
     * {@code getKeys("db");} will find the keys {@code db},
     * {@code db.user}, or {@code db.password}, but not the key
     * {@code dbdriver}.
     */
    @Override
    public final Iterator<String> getKeys(final String prefix)
    {
        beginRead(false);
        try
        {
            return getKeysInternal(prefix);
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Actually creates an iterator for iterating over the keys in this
     * configuration. This method is called by {@code getKeys()}, it has to be
     * defined by concrete subclasses.
     *
     * @return an {@code Iterator} with all property keys in this configuration
     * @since 2.0
     */
    protected abstract Iterator<String> getKeysInternal();

    /**
     * Returns an {@code Iterator} with all property keys starting with the
     * specified prefix. This method is called by {@link #getKeys(String)}. It
     * is fully implemented by delegating to {@code getKeysInternal()} and
     * returning a special iterator which filters for the passed in prefix.
     * Subclasses can override it if they can provide a more efficient way to
     * iterate over specific keys only.
     *
     * @param prefix the prefix for the keys to be taken into account
     * @return an {@code Iterator} returning the filtered keys
     * @since 2.0
     */
    protected Iterator<String> getKeysInternal(final String prefix)
    {
        return new PrefixedKeysIterator(getKeysInternal(), prefix);
    }

    /**
     * {@inheritDoc} This implementation ensures proper synchronization.
     * Subclasses have to define the abstract {@code getPropertyInternal()}
     * method which is called from here.
     */
    @Override
    public final Object getProperty(final String key)
    {
        beginRead(false);
        try
        {
            return getPropertyInternal(key);
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Actually obtains the value of the specified property. This method is
     * called by {@code getProperty()}. Concrete subclasses must define it to
     * fetch the value of the desired property.
     *
     * @param key the key of the property in question
     * @return the (raw) value of this property
     * @since 2.0
     */
    protected abstract Object getPropertyInternal(String key);

    /**
     * {@inheritDoc} This implementation handles synchronization and delegates
     * to {@code isEmptyInternal()}.
     */
    @Override
    public final boolean isEmpty()
    {
        beginRead(false);
        try
        {
            return isEmptyInternal();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Actually checks whether this configuration contains data. This method is
     * called by {@code isEmpty()}. It has to be defined by concrete subclasses.
     *
     * @return <b>true</b> if this configuration contains no data, <b>false</b>
     *         otherwise
     * @since 2.0
     */
    protected abstract boolean isEmptyInternal();

    /**
     * {@inheritDoc} This implementation handles synchronization and delegates
     * to {@code sizeInternal()}.
     */
    @Override
    public final int size()
    {
        beginRead(false);
        try
        {
            return sizeInternal();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Actually calculates the size of this configuration. This method is called
     * by {@code size()} with a read lock held. The base implementation provided
     * here calculates the size based on the iterator returned by
     * {@code getKeys()}. Sub classes which can determine the size in a more
     * efficient way should override this method.
     *
     * @return the size of this configuration (i.e. the number of keys)
     */
    protected int sizeInternal()
    {
        int size = 0;
        for (final Iterator<String> keyIt = getKeysInternal(); keyIt.hasNext(); size++)
        {
            keyIt.next();
        }
        return size;
    }

    /**
     * {@inheritDoc} This implementation handles synchronization and delegates
     * to {@code containsKeyInternal()}.
     */
    @Override
    public final boolean containsKey(final String key)
    {
        beginRead(false);
        try
        {
            return containsKeyInternal(key);
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Actually checks whether the specified key is contained in this
     * configuration. This method is called by {@code containsKey()}. It has to
     * be defined by concrete subclasses.
     *
     * @param key the key in question
     * @return <b>true</b> if this key is contained in this configuration,
     *         <b>false</b> otherwise
     * @since 2.0
     */
    protected abstract boolean containsKeyInternal(String key);

    @Override
    public Properties getProperties(final String key)
    {
        return getProperties(key, null);
    }

    /**
     * Get a list of properties associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaults Any default values for the returned
     * {@code Properties} object. Ignored if {@code null}.
     *
     * @return The associated properties if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     * is not a String/List of Strings.
     *
     * @throws IllegalArgumentException if one of the tokens is malformed (does
     * not contain an equals sign).
     */
    public Properties getProperties(final String key, final Properties defaults)
    {
        /*
         * Grab an array of the tokens for this key.
         */
        final String[] tokens = getStringArray(key);

        /*
         * Each token is of the form 'key=value'.
         */
        final Properties props = defaults == null ? new Properties() : new Properties(defaults);
        for (final String token : tokens)
        {
            final int equalSign = token.indexOf('=');
            if (equalSign > 0)
            {
                final String pkey = token.substring(0, equalSign).trim();
                final String pvalue = token.substring(equalSign + 1).trim();
                props.put(pkey, pvalue);
            }
            else if (tokens.length == 1 && "".equals(token))
            {
                // Semantically equivalent to an empty Properties
                // object.
                break;
            }
            else
            {
                throw new IllegalArgumentException('\'' + token + "' does not contain an equals sign");
            }
        }
        return props;
    }

    @Override
    public boolean getBoolean(final String key)
    {
        final Boolean b = convert(Boolean.class, key, null, true);
        return checkNonNullValue(key, b).booleanValue();
    }

    @Override
    public boolean getBoolean(final String key, final boolean defaultValue)
    {
        return getBoolean(key, Boolean.valueOf(defaultValue)).booleanValue();
    }

    /**
     * Obtains the value of the specified key and tries to convert it into a
     * {@code Boolean} object. If the property has no value, the passed
     * in default value will be used.
     *
     * @param key the key of the property
     * @param defaultValue the default value
     * @return the value of this key converted to a {@code Boolean}
     * @throws ConversionException if the value cannot be converted to a
     * {@code Boolean}
     */
    @Override
    public Boolean getBoolean(final String key, final Boolean defaultValue)
    {
        return convert(Boolean.class, key, defaultValue, false);
    }

    @Override
    public byte getByte(final String key)
    {
        final Byte b = convert(Byte.class, key, null, true);
        return checkNonNullValue(key, b).byteValue();
    }

    @Override
    public byte getByte(final String key, final byte defaultValue)
    {
        return getByte(key, Byte.valueOf(defaultValue)).byteValue();
    }

    @Override
    public Byte getByte(final String key, final Byte defaultValue)
    {
        return convert(Byte.class, key, defaultValue, false);
    }

    @Override
    public double getDouble(final String key)
    {
        final Double d = convert(Double.class, key, null, true);
        return checkNonNullValue(key, d).doubleValue();
    }

    @Override
    public double getDouble(final String key, final double defaultValue)
    {
        return getDouble(key, Double.valueOf(defaultValue)).doubleValue();
    }

    @Override
    public Double getDouble(final String key, final Double defaultValue)
    {
        return convert(Double.class, key, defaultValue, false);
    }

    @Override
    public float getFloat(final String key)
    {
        final Float f = convert(Float.class, key, null, true);
        return checkNonNullValue(key, f).floatValue();
    }

    @Override
    public float getFloat(final String key, final float defaultValue)
    {
        return getFloat(key, Float.valueOf(defaultValue)).floatValue();
    }

    @Override
    public Float getFloat(final String key, final Float defaultValue)
    {
        return convert(Float.class, key, defaultValue, false);
    }

    @Override
    public int getInt(final String key)
    {
        final Integer i = convert(Integer.class, key, null, true);
        return checkNonNullValue(key, i).intValue();
    }

    @Override
    public int getInt(final String key, final int defaultValue)
    {
        return getInteger(key, Integer.valueOf(defaultValue)).intValue();
    }

    @Override
    public Integer getInteger(final String key, final Integer defaultValue)
    {
        return convert(Integer.class, key, defaultValue, false);
    }

    @Override
    public long getLong(final String key)
    {
        final Long l = convert(Long.class, key, null, true);
        return checkNonNullValue(key, l).longValue();
    }

    @Override
    public long getLong(final String key, final long defaultValue)
    {
        return getLong(key, Long.valueOf(defaultValue)).longValue();
    }

    @Override
    public Long getLong(final String key, final Long defaultValue)
    {
        return convert(Long.class, key, defaultValue, false);
    }

    @Override
    public short getShort(final String key)
    {
        final Short s = convert(Short.class, key, null, true);
        return checkNonNullValue(key, s).shortValue();
    }

    @Override
    public short getShort(final String key, final short defaultValue)
    {
        return getShort(key, Short.valueOf(defaultValue)).shortValue();
    }

    @Override
    public Short getShort(final String key, final Short defaultValue)
    {
        return convert(Short.class, key, defaultValue, false);
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    @Override
    public BigDecimal getBigDecimal(final String key)
    {
        return convert(BigDecimal.class, key, null, true);
    }

    @Override
    public BigDecimal getBigDecimal(final String key, final BigDecimal defaultValue)
    {
        return convert(BigDecimal.class, key, defaultValue, false);
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    @Override
    public BigInteger getBigInteger(final String key)
    {
        return convert(BigInteger.class, key, null, true);
    }

    @Override
    public BigInteger getBigInteger(final String key, final BigInteger defaultValue)
    {
        return convert(BigInteger.class, key, defaultValue, false);
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    @Override
    public String getString(final String key)
    {
        return convert(String.class, key, null, true);
    }

    @Override
    public String getString(final String key, final String defaultValue)
    {
        final String result = convert(String.class, key, null, false);
        return (result != null) ? result : interpolate(defaultValue);
    }

    /**
     * {@inheritDoc} This implementation delegates to {@link #getString(String)}
     * in order to obtain the value of the passed in key. This value is passed
     * to the decoder. Because {@code getString()} is used behind the scenes all
     * standard features like handling of missing keys and interpolation work as
     * expected.
     */
    @Override
    public String getEncodedString(final String key, final ConfigurationDecoder decoder)
    {
        if (decoder == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationDecoder must not be null!");
        }

        final String value = getString(key);
        return (value != null) ? decoder.decode(value) : null;
    }

    /**
     * {@inheritDoc} This implementation makes use of the
     * {@code ConfigurationDecoder} set for this configuration. If no such
     * object has been set, an {@code IllegalStateException} exception is
     * thrown.
     *
     * @throws IllegalStateException if no {@code ConfigurationDecoder} is set
     * @see #setConfigurationDecoder(ConfigurationDecoder)
     */
    @Override
    public String getEncodedString(final String key)
    {
        final ConfigurationDecoder decoder = getConfigurationDecoder();
        if (decoder == null)
        {
            throw new IllegalStateException(
                    "No default ConfigurationDecoder defined!");
        }
        return getEncodedString(key, decoder);
    }

    /**
     * Get an array of strings associated with the given configuration key.
     * If the key doesn't map to an existing object, an empty array is returned.
     * When a property is added to a configuration, it is checked whether it
     * contains multiple values. This is obvious if the added object is a list
     * or an array. For strings the association {@link ListDelimiterHandler} is
     * consulted to find out whether the string can be split into multiple
     * values.
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a String/List of Strings.
     * @see #setListDelimiterHandler(ListDelimiterHandler)
     */
    @Override
    public String[] getStringArray(final String key)
    {
        final String[] result = (String[]) getArray(String.class, key);
        return (result == null) ? new String[0] : result;
    }

    /**
     * {@inheritDoc}
     * @see #getStringArray(String)
     */
    @Override
    public List<Object> getList(final String key)
    {
        return getList(key, new ArrayList<>());
    }

    @Override
    public List<Object> getList(final String key, final List<?> defaultValue)
    {
        final Object value = getProperty(key);
        List<Object> list;

        if (value instanceof String)
        {
            list = new ArrayList<>(1);
            list.add(interpolate((String) value));
        }
        else if (value instanceof List)
        {
            list = new ArrayList<>();
            final List<?> l = (List<?>) value;

            // add the interpolated elements in the new list
            for (final Object elem : l)
            {
                list.add(interpolate(elem));
            }
        }
        else if (value == null)
        {
            // This is okay because we just return this list to the caller
            @SuppressWarnings("unchecked")
            final
            List<Object> resultList = (List<Object>) defaultValue;
            list = resultList;
        }
        else if (value.getClass().isArray())
        {
            return Arrays.asList((Object[]) value);
        }
        else if (isScalarValue(value))
        {
            return Collections.singletonList((Object) value.toString());
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a List object: " + value + ", a "
                    + value.getClass().getName());
        }
        return list;
    }

    @Override
    public <T> T get(final Class<T> cls, final String key)
    {
        return convert(cls, key, null, true);
    }

    /**
     * {@inheritDoc} This implementation delegates to the
     * {@link ConversionHandler} to perform the actual type conversion.
     */
    @Override
    public <T> T get(final Class<T> cls, final String key, final T defaultValue)
    {
        return convert(cls, key, defaultValue, false);
    }

    @Override
    public Object getArray(final Class<?> cls, final String key)
    {
        return getArray(cls, key, null);
    }

    /**
     * {@inheritDoc} This implementation delegates to the
     * {@link ConversionHandler} to perform the actual type conversion. If this
     * results in a <b>null</b> result (because the property is undefined), the
     * default value is returned. It is checked whether the default value is an
     * array with the correct component type. If not, an exception is thrown.
     *
     * @throws IllegalArgumentException if the default value is not a compatible
     *         array
     */
    @Override
    public Object getArray(final Class<?> cls, final String key, final Object defaultValue)
    {
        return convertToArray(cls, key, defaultValue);
    }

    @Override
    public <T> List<T> getList(final Class<T> cls, final String key)
    {
        return getList(cls, key, null);
    }

    /**
     * {@inheritDoc} This implementation delegates to the generic
     * {@code getCollection()}. As target collection a newly created
     * {@code ArrayList} is passed in.
     */
    @Override
    public <T> List<T> getList(final Class<T> cls, final String key, final List<T> defaultValue)
    {
        final List<T> result = new ArrayList<>();
        if (getCollection(cls, key, result, defaultValue) == null)
        {
            return null;
        }
        return result;
    }

    @Override
    public <T> Collection<T> getCollection(final Class<T> cls, final String key,
            final Collection<T> target)
    {
        return getCollection(cls, key, target, null);
    }

    /**
     * {@inheritDoc} This implementation delegates to the
     * {@link ConversionHandler} to perform the actual conversion. If no target
     * collection is provided, an {@code ArrayList} is created.
     */
    @Override
    public <T> Collection<T> getCollection(final Class<T> cls, final String key,
            final Collection<T> target, final Collection<T> defaultValue)
    {
        final Object src = getProperty(key);
        if (src == null)
        {
            return handleDefaultCollection(target, defaultValue);
        }

        final Collection<T> targetCol =
                (target != null) ? target : new ArrayList<>();
        getConversionHandler().toCollection(src, cls, getInterpolator(),
                targetCol);
        return targetCol;
    }

    /**
     * Checks whether the specified object is a scalar value. This method is
     * called by {@code getList()} and {@code getStringArray()} if the
     * property requested is not a string, a list, or an array. If it returns
     * <b>true</b>, the calling method transforms the value to a string and
     * returns a list or an array with this single element. This implementation
     * returns <b>true</b> if the value is of a wrapper type for a primitive
     * type.
     *
     * @param value the value to be checked
     * @return a flag whether the value is a scalar
     * @since 1.7
     */
    protected boolean isScalarValue(final Object value)
    {
        return ClassUtils.wrapperToPrimitive(value.getClass()) != null;
    }

    /**
     * Copies the content of the specified configuration into this
     * configuration. If the specified configuration contains a key that is also
     * present in this configuration, the value of this key will be replaced by
     * the new value. <em>Note:</em> This method won't work well when copying
     * hierarchical configurations because it is not able to copy information
     * about the properties' structure (i.e. the parent-child-relationships will
     * get lost). So when dealing with hierarchical configuration objects their
     * {@link BaseHierarchicalConfiguration#clone() clone()} methods
     * should be used.
     *
     * @param c the configuration to copy (can be <b>null</b>, then this
     * operation will have no effect)
     * @since 1.5
     */
    public void copy(final Configuration c)
    {
        if (c != null)
        {
            c.lock(LockMode.READ);
            try
            {
                for (final Iterator<String> it = c.getKeys(); it.hasNext();)
                {
                    final String key = it.next();
                    final Object value = encodeForCopy(c.getProperty(key));
                    setProperty(key, value);
                }
            }
            finally
            {
                c.unlock(LockMode.READ);
            }
        }
    }

    /**
     * Appends the content of the specified configuration to this configuration.
     * The values of all properties contained in the specified configuration
     * will be appended to this configuration. So if a property is already
     * present in this configuration, its new value will be a union of the
     * values in both configurations. <em>Note:</em> This method won't work
     * well when appending hierarchical configurations because it is not able to
     * copy information about the properties' structure (i.e. the
     * parent-child-relationships will get lost). So when dealing with
     * hierarchical configuration objects their
     * {@link BaseHierarchicalConfiguration#clone() clone()} methods
     * should be used.
     *
     * @param c the configuration to be appended (can be <b>null</b>, then this
     * operation will have no effect)
     * @since 1.5
     */
    public void append(final Configuration c)
    {
        if (c != null)
        {
            c.lock(LockMode.READ);
            try
            {
                for (final Iterator<String> it = c.getKeys(); it.hasNext();)
                {
                    final String key = it.next();
                    final Object value = encodeForCopy(c.getProperty(key));
                    addProperty(key, value);
                }
            }
            finally
            {
                c.unlock(LockMode.READ);
            }
        }
    }

    /**
     * Returns a configuration with the same content as this configuration, but
     * with all variables replaced by their actual values. This method tries to
     * clone the configuration and then perform interpolation on all properties.
     * So property values of the form <code>${var}</code> will be resolved as
     * far as possible (if a variable cannot be resolved, it remains unchanged).
     * This operation is useful if the content of a configuration is to be
     * exported or processed by an external component that does not support
     * variable interpolation.
     *
     * @return a configuration with all variables interpolated
     * @throws org.apache.commons.configuration2.ex.ConfigurationRuntimeException if this
     * configuration cannot be cloned
     * @since 1.5
     */
    public Configuration interpolatedConfiguration()
    {
        // first clone this configuration
        final AbstractConfiguration c = (AbstractConfiguration) ConfigurationUtils
                .cloneConfiguration(this);

        // now perform interpolation
        c.setListDelimiterHandler(new DisabledListDelimiterHandler());
        for (final Iterator<String> it = getKeys(); it.hasNext();)
        {
            final String key = it.next();
            c.setProperty(key, getList(key));
        }

        c.setListDelimiterHandler(getListDelimiterHandler());
        return c;
    }

    /**
     * Initializes the logger. Supports <b>null</b> input. This method can be
     * called by derived classes in order to enable logging.
     *
     * @param log the logger
     * @since 2.0
     */
    protected final void initLogger(final ConfigurationLogger log)
    {
        this.log = (log != null) ? log : ConfigurationLogger.newDummyLogger();
    }

    /**
     * Encodes a property value so that it can be added to this configuration.
     * This method deals with list delimiters. The passed in object has to be
     * escaped so that an add operation yields the same result. If it is a list,
     * all of its values have to be escaped.
     *
     * @param value the value to be encoded
     * @return the encoded value
     */
    private Object encodeForCopy(final Object value)
    {
        if (value instanceof Collection)
        {
            return encodeListForCopy((Collection<?>) value);
        }
        return getListDelimiterHandler().escape(value,
                ListDelimiterHandler.NOOP_TRANSFORMER);
    }

    /**
     * Encodes a list with property values so that it can be added to this
     * configuration. This method calls {@code encodeForCopy()} for all list
     * elements.
     *
     * @param values the list to be encoded
     * @return a list with encoded elements
     */
    private Object encodeListForCopy(final Collection<?> values)
    {
        final List<Object> result = new ArrayList<>(values.size());
        for (final Object value : values)
        {
            result.add(encodeForCopy(value));
        }
        return result;
    }

    /**
     * Obtains the property value for the specified key and converts it to the
     * given target class.
     *
     * @param <T> the target type of the conversion
     * @param cls the target class
     * @param key the key of the desired property
     * @param defaultValue a default value
     * @return the converted value of this property
     * @throws ConversionException if the conversion cannot be performed
     */
    private <T> T getAndConvertProperty(final Class<T> cls, final String key, final T defaultValue)
    {
        final Object value = getProperty(key);
        try
        {
            return ObjectUtils.defaultIfNull(
                    getConversionHandler().to(value, cls, getInterpolator()),
                    defaultValue);
        }
        catch (final ConversionException cex)
        {
            // improve error message
            throw new ConversionException(
                    String.format(
                            "Key '%s' cannot be converted to class %s. Value is: '%s'.",
                            key, cls.getName(), String.valueOf(value)), cex.getCause());
        }
    }

    /**
     * Helper method for obtaining a property value with a type conversion.
     *
     * @param <T> the target type of the conversion
     * @param cls the target class
     * @param key the key of the desired property
     * @param defValue a default value
     * @param throwOnMissing a flag whether an exception should be thrown for a
     *        missing value
     * @return the converted value
     */
    private <T> T convert(final Class<T> cls, final String key, final T defValue,
            final boolean throwOnMissing)
    {
        if (cls.isArray())
        {
            return cls.cast(convertToArray(cls.getComponentType(), key, defValue));
        }

        final T result = getAndConvertProperty(cls, key, defValue);
        if (result == null)
        {
            if (throwOnMissing && isThrowExceptionOnMissing())
            {
                throwMissingPropertyException(key);
            }
            return defValue;
        }

        return result;
    }

    /**
     * Performs a conversion to an array result class. This implementation
     * delegates to the {@link ConversionHandler} to perform the actual type
     * conversion. If this results in a <b>null</b> result (because the property
     * is undefined), the default value is returned. It is checked whether the
     * default value is an array with the correct component type. If not, an
     * exception is thrown.
     *
     * @param cls the component class of the array
     * @param key the configuration key
     * @param defaultValue an optional default value
     * @return the converted array
     * @throws IllegalArgumentException if the default value is not a compatible
     *         array
     */
    private Object convertToArray(final Class<?> cls, final String key, final Object defaultValue)
    {
        checkDefaultValueArray(cls, defaultValue);
        return ObjectUtils.defaultIfNull(getConversionHandler().toArray(
                getProperty(key), cls, getInterpolator()), defaultValue);
    }

    /**
     * Checks an object provided as default value for the {@code getArray()}
     * method. Throws an exception if this is not an array with the correct
     * component type.
     *
     * @param cls the component class for the array
     * @param defaultValue the default value object to be checked
     * @throws IllegalArgumentException if this is not a valid default object
     */
    private static void checkDefaultValueArray(final Class<?> cls, final Object defaultValue)
    {
        if (defaultValue != null
                && (!defaultValue.getClass().isArray() || !cls
                        .isAssignableFrom(defaultValue.getClass()
                                .getComponentType())))
        {
            throw new IllegalArgumentException(
                    "The type of the default value (" + defaultValue.getClass()
                            + ")" + " is not an array of the specified class ("
                            + cls + ")");
        }
    }

    /**
     * Handles the default collection for a collection conversion. This method
     * fills the target collection with the content of the default collection.
     * Both collections may be <b>null</b>.
     *
     * @param target the target collection
     * @param defaultValue the default collection
     * @return the initialized target collection
     */
    private static <T> Collection<T> handleDefaultCollection(final Collection<T> target,
            final Collection<T> defaultValue)
    {
        if (defaultValue == null)
        {
            return null;
        }

        Collection<T> result;
        if (target == null)
        {
            result = new ArrayList<>(defaultValue);
        }
        else
        {
            target.addAll(defaultValue);
            result = target;
        }
        return result;
    }

    /**
     * Checks whether the specified value is <b>null</b> and throws an exception
     * in this case. This method is used by conversion methods returning
     * primitive Java types. Here values to be returned must not be <b>null</b>.
     *
     * @param <T> the type of the object to be checked
     * @param key the key which caused the problem
     * @param value the value to be checked
     * @return the passed in value for chaining this method call
     * @throws NoSuchElementException if the value is <b>null</b>
     */
    private static <T> T checkNonNullValue(final String key, final T value)
    {
        if (value == null)
        {
            throwMissingPropertyException(key);
        }
        return value;
    }

    /**
     * Helper method for throwing an exception for a key that does not map to an
     * existing object.
     *
     * @param key the key (to be part of the error message)
     */
    private static void throwMissingPropertyException(final String key)
    {
        throw new NoSuchElementException(String.format(
                "Key '%s' does not map to an existing object!", key));
    }
}
