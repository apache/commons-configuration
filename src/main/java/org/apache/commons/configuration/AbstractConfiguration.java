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

package org.apache.commons.configuration;

import java.lang.reflect.Array;
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

import org.apache.commons.configuration.event.BaseEventSource;
import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.InterpolatorSpecification;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

/**
 * <p>Abstract configuration class. Provides basic functionality but does not
 * store any data.</p>
 * <p>If you want to write your own Configuration class then you should
 * implement only abstract methods from this class. A lot of functionality
 * needed by typical implementations of the {@code Configuration}
 * interface is already provided by this base class. Following is a list of
 * features implemented here:
 * <ul><li>Data conversion support. The various data types required by the
 * {@code Configuration} interface are already handled by this base class.
 * A concrete sub class only needs to provide a generic {@code getProperty()}
 * method.</li>
 * <li>Support for variable interpolation. Property values containing special
 * variable tokens (like <code>${var}</code>) will be replaced by their
 * corresponding values.</li>
 * <li>Support for string lists. The values of properties to be added to this
 * configuration are checked whether they contain a list delimiter character. If
 * this is the case and if list splitting is enabled, the string is split and
 * multiple values are added for this property. (With the
 * {@code setListDelimiter()} method the delimiter character can be
 * specified; per default a comma is used. The
 * {@code setDelimiterParsingDisabled()} method can be used to disable
 * list splitting completely.)</li>
 * <li>Allows to specify how missing properties are treated. Per default the
 * get methods returning an object will return <b>null</b> if the searched
 * property key is not found (and no default value is provided). With the
 * {@code setThrowExceptionOnMissing()} method this behavior can be
 * changed to throw an exception when a requested property cannot be found.</li>
 * <li>Basic event support. Whenever this configuration is modified registered
 * event listeners are notified. Refer to the various {@code EVENT_XXX}
 * constants to get an impression about which event types are supported.</li>
 * <li>Support for proper synchronization based on the {@link Synchronizer}
 * interface.</li>
 * </ul></p>
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
 * @version $Id$
 */
public abstract class AbstractConfiguration extends BaseEventSource implements Configuration
{
    /**
     * Constant for the add property event type.
     * @since 1.3
     */
    public static final int EVENT_ADD_PROPERTY = 1;

    /**
     * Constant for the clear property event type.
     * @since 1.3
     */
    public static final int EVENT_CLEAR_PROPERTY = 2;

    /**
     * Constant for the set property event type.
     * @since 1.3
     */
    public static final int EVENT_SET_PROPERTY = 3;

    /**
     * Constant for the clear configuration event type.
     * @since 1.3
     */
    public static final int EVENT_CLEAR = 4;

    /**
     * Constant for the get property event type. This event type is used for
     * error events.
     * @since 1.4
     */
    public static final int EVENT_READ_PROPERTY = 5;

    /** start token */
    protected static final String START_TOKEN = "${";

    /** end token */
    protected static final String END_TOKEN = "}";

    /**
     * Constant for the disabled list delimiter. This character is passed to the
     * list parsing methods if delimiter parsing is disabled. So this character
     * should not occur in string property values.
     */
    private static final char DISABLED_DELIMITER = '\0';

    /** The default value for listDelimiter */
    private static char defaultListDelimiter = ',';

    /** Delimiter used to convert single values to lists */
    private char listDelimiter = defaultListDelimiter;

    /**
     * When set to true the given configuration delimiter will not be used
     * while parsing for this configuration.
     */
    private boolean delimiterParsingDisabled;

    /**
     * Whether the configuration should throw NoSuchElementExceptions or simply
     * return null when a property does not exist. Defaults to return null.
     */
    private boolean throwExceptionOnMissing;

    /** Stores a reference to the object that handles variable interpolation. */
    private AtomicReference<ConfigurationInterpolator> interpolator;

    /** The object responsible for synchronization. */
    private volatile Synchronizer synchronizer;

    /** Stores the logger.*/
    private Log log;

    /**
     * Creates a new instance of {@code AbstractConfiguration}.
     */
    public AbstractConfiguration()
    {
        interpolator = new AtomicReference<ConfigurationInterpolator>();
        setLogger(null);
        installDefaultInterpolator();
    }

    /**
     * For configurations extending AbstractConfiguration, allow them to change
     * the listDelimiter from the default comma (","). This value will be used
     * only when creating new configurations. Those already created will not be
     * affected by this change
     *
     * @param delimiter The new listDelimiter
     */
    public static void setDefaultListDelimiter(char delimiter)
    {
        AbstractConfiguration.defaultListDelimiter = delimiter;
    }

    /**
     * Sets the default list delimiter.
     *
     * @param delimiter the delimiter character
     * @deprecated Use AbstractConfiguration.setDefaultListDelimiter(char)
     * instead
     */
    @Deprecated
    public static void setDelimiter(char delimiter)
    {
        setDefaultListDelimiter(delimiter);
    }

    /**
     * Retrieve the current delimiter. By default this is a comma (",").
     *
     * @return The delimiter in use
     */
    public static char getDefaultListDelimiter()
    {
        return AbstractConfiguration.defaultListDelimiter;
    }

    /**
     * Returns the default list delimiter.
     *
     * @return the default list delimiter
     * @deprecated Use AbstractConfiguration.getDefaultListDelimiter() instead
     */
    @Deprecated
    public static char getDelimiter()
    {
        return getDefaultListDelimiter();
    }

    /**
     * Change the list delimiter for this configuration.
     *
     * Note: this change will only be effective for new parsings. If you
     * want it to take effect for all loaded properties use the no arg constructor
     * and call this method before setting the source.
     *
     * @param listDelimiter The new listDelimiter
     */
    public void setListDelimiter(char listDelimiter)
    {
        this.listDelimiter = listDelimiter;
    }

    /**
     * Retrieve the delimiter for this configuration. The default
     * is the value of defaultListDelimiter.
     *
     * @return The listDelimiter in use
     */
    public char getListDelimiter()
    {
        return listDelimiter;
    }

    /**
     * Determine if this configuration is using delimiters when parsing
     * property values to convert them to lists of values. Defaults to false
     * @return true if delimiters are not being used
     */
    public boolean isDelimiterParsingDisabled()
    {
        return delimiterParsingDisabled;
    }

    /**
     * Set whether this configuration should use delimiters when parsing
     * property values to convert them to lists of values. By default delimiter
     * parsing is enabled
     *
     * Note: this change will only be effective for new parsings. If you
     * want it to take effect for all loaded properties use the no arg constructor
     * and call this method before setting source.
     * @param delimiterParsingDisabled a flag whether delimiter parsing should
     * be disabled
     */
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled)
    {
        this.delimiterParsingDisabled = delimiterParsingDisabled;
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
    public void setThrowExceptionOnMissing(boolean throwExceptionOnMissing)
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
    public final void setInterpolator(ConfigurationInterpolator ci)
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
    public final void installInterpolator(
            Map<String, ? extends Lookup> prefixLookups,
            Collection<? extends Lookup> defLookups)
    {
        InterpolatorSpecification spec =
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
    public void setPrefixLookups(Map<String, ? extends Lookup> lookups)
    {
        boolean success;
        do
        {
            // do this in a loop because the ConfigurationInterpolator
            // instance may be changed by another thread
            ConfigurationInterpolator ciOld = getInterpolator();
            ConfigurationInterpolator ciNew =
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
    public void setDefaultLookups(Collection<? extends Lookup> lookups)
    {
        boolean success;
        do
        {
            ConfigurationInterpolator ciOld = getInterpolator();
            ConfigurationInterpolator ciNew =
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
    public void setParentInterpolator(ConfigurationInterpolator parent)
    {
        boolean success;
        do
        {
            ConfigurationInterpolator ciOld = getInterpolator();
            ConfigurationInterpolator ciNew =
                    (ciOld != null) ? ciOld : new ConfigurationInterpolator();
            ciNew.setParentInterpolator(parent);
            success = interpolator.compareAndSet(ciOld, ciNew);
        } while (!success);
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
    protected void cloneInterpolator(AbstractConfiguration orgConfig)
    {
        interpolator = new AtomicReference<ConfigurationInterpolator>();
        ConfigurationInterpolator orgInterpolator = orgConfig.getInterpolator();
        List<Lookup> defaultLookups = orgInterpolator.getDefaultLookups();
        Lookup lookup = findConfigurationLookup(orgInterpolator, orgConfig);
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
    private Lookup findConfigurationLookup(ConfigurationInterpolator ci)
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
    private static Lookup findConfigurationLookup(ConfigurationInterpolator ci,
            ImmutableConfiguration targetConf)
    {
        for (Lookup l : ci.getDefaultLookups())
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
     * @since 1.4
     */
    public Log getLogger()
    {
        return log;
    }

    /**
     * Allows to set the logger to be used by this configuration object. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used.
     *
     * @param log the new logger
     * @since 1.4
     */
    public void setLogger(Log log)
    {
        this.log = (log != null) ? log : new NoOpLog();
    }

    /**
     * Adds a special
     * {@link org.apache.commons.configuration.event.ConfigurationErrorListener}
     * object to this configuration that will log all internal errors. This
     * method is intended to be used by certain derived classes, for which it is
     * known that they can fail on property access (e.g.
     * {@code DatabaseConfiguration}).
     *
     * @since 1.4
     */
    public void addErrorLogListener()
    {
        addErrorListener(new ConfigurationErrorListener()
        {
            public void configurationError(ConfigurationErrorEvent event)
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
    public final Synchronizer getSynchronizer()
    {
        Synchronizer sync = synchronizer;
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
    public final void setSynchronizer(Synchronizer synchronizer)
    {
        this.synchronizer = synchronizer;
    }

    public final void addProperty(String key, Object value)
    {
        getSynchronizer().beginWrite();
        try
        {
            fireEvent(EVENT_ADD_PROPERTY, key, value, true);
            addPropertyInternal(key, value);
            fireEvent(EVENT_ADD_PROPERTY, key, value, false);
        }
        finally
        {
            getSynchronizer().endWrite();
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
    protected void addPropertyInternal(String key, Object value)
    {
        addPropertyValues(key, value,
                isDelimiterParsingDisabled() ? DISABLED_DELIMITER
                        : getListDelimiter());
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
     * Adds the specified value for the given property. This method supports
     * single values and containers (e.g. collections or arrays) as well. In the
     * latter case, {@code addPropertyDirect()} will be called for each
     * element.
     *
     * @param key the property key
     * @param value the value object
     * @param delimiter the list delimiter character
     */
    private void addPropertyValues(String key, Object value, char delimiter)
    {
        Iterator<?> it = PropertyConverter.toIterator(value, delimiter);
        while (it.hasNext())
        {
            addPropertyDirect(key, it.next());
        }
    }

    /**
     * interpolate key names to handle ${key} stuff
     *
     * @param base string to interpolate
     *
     * @return returns the key name with the ${key} substituted
     */
    protected String interpolate(String base)
    {
        Object result = interpolate((Object) base);
        return (result == null) ? null : result.toString();
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
    protected Object interpolate(Object value)
    {
        ConfigurationInterpolator ci = getInterpolator();
        return (ci != null) ? ci.interpolate(value) : value;
    }

    public Configuration subset(String prefix)
    {
        return new SubsetConfiguration(this, prefix, ".");
    }

    public ImmutableConfiguration immutableSubset(String prefix)
    {
        return ConfigurationUtils.unmodifiableConfiguration(subset(prefix));
    }

    public final void setProperty(String key, Object value)
    {
        getSynchronizer().beginWrite();
        try
        {
            fireEvent(EVENT_SET_PROPERTY, key, value, true);
            setPropertyInternal(key, value);
            fireEvent(EVENT_SET_PROPERTY, key, value, false);
        }
        finally
        {
            getSynchronizer().endWrite();
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
    protected void setPropertyInternal(String key, Object value)
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
    public final void clearProperty(String key)
    {
        getSynchronizer().beginWrite();
        try
        {
            fireEvent(EVENT_CLEAR_PROPERTY, key, null, true);
            clearPropertyDirect(key);
            fireEvent(EVENT_CLEAR_PROPERTY, key, null, false);
        }
        finally
        {
            getSynchronizer().endWrite();
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

    public final void clear()
    {
        getSynchronizer().beginWrite();
        try
        {
            fireEvent(EVENT_CLEAR, null, null, true);
            clearInternal();
            fireEvent(EVENT_CLEAR, null, null, false);
        }
        finally
        {
            getSynchronizer().endWrite();
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
            Iterator<String> it = getKeys();
            while (it.hasNext())
            {
                String key = it.next();
                if (useIterator)
                {
                    try
                    {
                        it.remove();
                    }
                    catch (UnsupportedOperationException usoex)
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
     * {@inheritDoc} This implementation returns keys that either match the
     * prefix or start with the prefix followed by a dot ('.'). So the call
     * {@code getKeys("db");} will find the keys {@code db},
     * {@code db.user}, or {@code db.password}, but not the key
     * {@code dbdriver}.
     */
    public Iterator<String> getKeys(String prefix)
    {
        return new PrefixedKeysIterator(getKeys(), prefix);
    }

    public Properties getProperties(String key)
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
    public Properties getProperties(String key, Properties defaults)
    {
        /*
         * Grab an array of the tokens for this key.
         */
        String[] tokens = getStringArray(key);

        /*
         * Each token is of the form 'key=value'.
         */
        Properties props = defaults == null ? new Properties() : new Properties(defaults);
        for (String token : tokens)
        {
            int equalSign = token.indexOf('=');
            if (equalSign > 0)
            {
                String pkey = token.substring(0, equalSign).trim();
                String pvalue = token.substring(equalSign + 1).trim();
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

    /**
     * {@inheritDoc}
     * @see PropertyConverter#toBoolean(Object)
     */
    public boolean getBoolean(String key)
    {
        Boolean b = getBoolean(key, null);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     * @see PropertyConverter#toBoolean(Object)
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, BooleanUtils.toBooleanObject(defaultValue)).booleanValue();
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
     * @see PropertyConverter#toBoolean(Object)
     */
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toBoolean(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Boolean object", e);
            }
        }
    }

    public byte getByte(String key)
    {
        Byte b = getByte(key, null);
        if (b != null)
        {
            return b.byteValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
        }
    }

    public byte getByte(String key, byte defaultValue)
    {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    public Byte getByte(String key, Byte defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toByte(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Byte object", e);
            }
        }
    }

    public double getDouble(String key)
    {
        Double d = getDouble(key, null);
        if (d != null)
        {
            return d.doubleValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public double getDouble(String key, double defaultValue)
    {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }

    public Double getDouble(String key, Double defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toDouble(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Double object", e);
            }
        }
    }

    public float getFloat(String key)
    {
        Float f = getFloat(key, null);
        if (f != null)
        {
            return f.floatValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public float getFloat(String key, float defaultValue)
    {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }

    public Float getFloat(String key, Float defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toFloat(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Float object", e);
            }
        }
    }

    public int getInt(String key)
    {
        Integer i = getInteger(key, null);
        if (i != null)
        {
            return i.intValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public int getInt(String key, int defaultValue)
    {
        Integer i = getInteger(key, null);

        if (i == null)
        {
            return defaultValue;
        }

        return i.intValue();
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toInteger(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to an Integer object", e);
            }
        }
    }

    public long getLong(String key)
    {
        Long l = getLong(key, null);
        if (l != null)
        {
            return l.longValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public long getLong(String key, long defaultValue)
    {
        return getLong(key, new Long(defaultValue)).longValue();
    }

    public Long getLong(String key, Long defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toLong(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Long object", e);
            }
        }
    }

    public short getShort(String key)
    {
        Short s = getShort(key, null);
        if (s != null)
        {
            return s.shortValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    public short getShort(String key, short defaultValue)
    {
        return getShort(key, new Short(defaultValue)).shortValue();
    }

    public Short getShort(String key, Short defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toShort(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Short object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    public BigDecimal getBigDecimal(String key)
    {
        BigDecimal number = getBigDecimal(key, null);
        if (number != null)
        {
            return number;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toBigDecimal(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    public BigInteger getBigInteger(String key)
    {
        BigInteger number = getBigInteger(key, null);
        if (number != null)
        {
            return number;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toBigInteger(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigInteger object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see #setThrowExceptionOnMissing(boolean)
     */
    public String getString(String key)
    {
        String s = getString(key, null);
        if (s != null)
        {
            return s;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
    }

    public String getString(String key, String defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof String)
        {
            return interpolate((String) value);
        }
        else if (value == null)
        {
            return interpolate(defaultValue);
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a String object");
        }
    }

    /**
     * Get an array of strings associated with the given configuration key.
     * If the key doesn't map to an existing object, an empty array is returned.
     * If a property is added to a configuration, it is checked whether it
     * contains multiple values. This is obvious if the added object is a list
     * or an array. For strings it is checked whether the string contains the
     * list delimiter character that can be specified using the
     * {@code setListDelimiter()} method. If this is the case, the string
     * is split at these positions resulting in a property with multiple
     * values.
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a String/List of Strings.
     * @see #setListDelimiter(char)
     * @see #setDelimiterParsingDisabled(boolean)
     */
    public String[] getStringArray(String key)
    {
        Object value = readProperty(key);

        String[] array;

        if (value instanceof String)
        {
            array = new String[1];

            array[0] = interpolate((String) value);
        }
        else if (value instanceof List)
        {
            List<?> list = (List<?>) value;
            array = new String[list.size()];

            for (int i = 0; i < array.length; i++)
            {
                array[i] = interpolate(ObjectUtils.toString(list.get(i), null));
            }
        }
        else if (value == null)
        {
            array = new String[0];
        }
        else if (isScalarValue(value))
        {
            array = new String[1];
            array[0] = value.toString();
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a String/List object");
        }
        return array;
    }

    /**
     * {@inheritDoc}
     * @see #getStringArray(String)
     */
    public List<Object> getList(String key)
    {
        return getList(key, new ArrayList<Object>());
    }

    public List<Object> getList(String key, List<Object> defaultValue)
    {
        Object value = readProperty(key);
        List<Object> list;

        if (value instanceof String)
        {
            list = new ArrayList<Object>(1);
            list.add(interpolate((String) value));
        }
        else if (value instanceof List)
        {
            list = new ArrayList<Object>();
            List<?> l = (List<?>) value;

            // add the interpolated elements in the new list
            for (Object elem : l)
            {
                list.add(interpolate(elem));
            }
        }
        else if (value == null)
        {
            list = defaultValue;
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

    /**
     * Returns an object from the store described by the key. If the value is a
     * Collection object, replace it with the first object in the collection.
     *
     * @param key The property key.
     *
     * @return value Value, transparently resolving a possible collection dependency.
     */
    protected Object resolveContainerStore(String key)
    {
        Object value = readProperty(key);
        if (value != null)
        {
            if (value instanceof Collection)
            {
                Collection<?> collection = (Collection<?>) value;
                value = collection.isEmpty() ? null : collection.iterator().next();
            }
            else if (value.getClass().isArray() && Array.getLength(value) > 0)
            {
                value = Array.get(value, 0);
            }
        }

        return value;
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
    protected boolean isScalarValue(Object value)
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
    public void copy(Configuration c)
    {
        if (c != null)
        {
            for (Iterator<String> it = c.getKeys(); it.hasNext();)
            {
                String key = it.next();
                Object value = c.getProperty(key);
                fireEvent(EVENT_SET_PROPERTY, key, value, true);
                setDetailEvents(false);
                try
                {
                    clearProperty(key);
                    addPropertyValues(key, value, DISABLED_DELIMITER);
                }
                finally
                {
                    setDetailEvents(true);
                }
                fireEvent(EVENT_SET_PROPERTY, key, value, false);
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
    public void append(Configuration c)
    {
        if (c != null)
        {
            for (Iterator<String> it = c.getKeys(); it.hasNext();)
            {
                String key = it.next();
                Object value = c.getProperty(key);
                fireEvent(EVENT_ADD_PROPERTY, key, value, true);
                addPropertyValues(key, value, DISABLED_DELIMITER);
                fireEvent(EVENT_ADD_PROPERTY, key, value, false);
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
     * @throws ConfigurationRuntimeException if this configuration cannot be
     * cloned
     * @since 1.5
     */
    public Configuration interpolatedConfiguration()
    {
        // first clone this configuration
        AbstractConfiguration c = (AbstractConfiguration) ConfigurationUtils
                .cloneConfiguration(this);

        // now perform interpolation
        c.setDelimiterParsingDisabled(true);
        for (Iterator<String> it = getKeys(); it.hasNext();)
        {
            String key = it.next();
            c.setProperty(key, getList(key));
        }

        c.setDelimiterParsingDisabled(isDelimiterParsingDisabled());
        return c;
    }

    /**
     * Obtains a value of a property. Ensures proper synchronization.
     *
     * @param key the key to be read
     * @return the value of this property
     */
    private Object readProperty(String key)
    {
        getSynchronizer().beginRead();
        try
        {
            return getProperty(key);
        }
        finally
        {
            getSynchronizer().endRead();
        }
    }
}
