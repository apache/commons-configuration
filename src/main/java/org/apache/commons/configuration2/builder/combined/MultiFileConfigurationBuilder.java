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
package org.apache.commons.configuration2.builder.combined;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ConfigurationBuilderResultCreatedEvent;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerList;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.InterpolatorSpecification;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;

/**
 * <p>
 * A specialized {@code ConfigurationBuilder} implementation providing access to
 * multiple file-based configurations based on a file name pattern.
 * </p>
 * <p>
 * This builder class is initialized with a pattern string and a
 * {@link ConfigurationInterpolator} object. Each time a configuration is
 * requested, the pattern is evaluated against the
 * {@code ConfigurationInterpolator} (so all variables are replaced by their
 * current values). The resulting string is interpreted as a file name for a
 * configuration file to be loaded. For example, providing a pattern of
 * <em>file:///opt/config/${product}/${client}/config.xml</em> will result in
 * <em>product</em> and <em>client</em> being resolved on every call. By storing
 * configuration files in a corresponding directory structure, specialized
 * configuration files associated with a specific product and client can be
 * loaded. Thus an application can be made multi-tenant in a transparent way.
 * </p>
 * <p>
 * This builder class keeps a map with configuration builders for configurations
 * already loaded. The {@code getConfiguration()} method first evaluates the
 * pattern string and checks whether a builder for the resulting file name is
 * available. If yes, it is queried for its configuration. Otherwise, a new
 * file-based configuration builder is created now and initialized.
 * </p>
 * <p>
 * Configuration of an instance happens in the usual way for configuration
 * builders. A {@link MultiFileBuilderParametersImpl} parameters object is
 * expected which must contain a file name pattern string and a
 * {@code ConfigurationInterpolator}. Other properties of this parameters object
 * are used to initialize the builders for managed configurations.
 * </p>
 *
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class MultiFileConfigurationBuilder<T extends FileBasedConfiguration>
        extends BasicConfigurationBuilder<T>
{
    /**
     * Constant for the name of the key referencing the
     * {@code ConfigurationInterpolator} in this builder's parameters.
     */
    private static final String KEY_INTERPOLATOR = "interpolator";

    /** A cache for already created managed builders. */
    private final ConcurrentMap<String, FileBasedConfigurationBuilder<T>> managedBuilders =
            new ConcurrentHashMap<>();

    /** Stores the {@code ConfigurationInterpolator} object. */
    private final AtomicReference<ConfigurationInterpolator> interpolator =
            new AtomicReference<>();

    /**
     * A flag for preventing reentrant access to managed builders on
     * interpolation of the file name pattern.
     */
    private final ThreadLocal<Boolean> inInterpolation =
            new ThreadLocal<>();

    /** A list for the event listeners to be passed to managed builders. */
    private final EventListenerList configurationListeners = new EventListenerList();

    /**
     * A specialized event listener which gets registered at all managed
     * builders. This listener just propagates notifications from managed
     * builders to the listeners registered at this
     * {@code MultiFileConfigurationBuilder}.
     */
    private final EventListener<ConfigurationBuilderEvent> managedBuilderDelegationListener =
            new EventListener<ConfigurationBuilderEvent>()
            {
                @Override
                public void onEvent(final ConfigurationBuilderEvent event)
                {
                    handleManagedBuilderEvent(event);
                }
            };

    /**
     * Creates a new instance of {@code MultiFileConfigurationBuilder} and sets
     * initialization parameters and a flag whether initialization failures
     * should be ignored.
     *
     * @param resCls the result configuration class
     * @param params a map with initialization parameters
     * @param allowFailOnInit a flag whether initialization errors should be
     *        ignored
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public MultiFileConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params, final boolean allowFailOnInit)
    {
        super(resCls, params, allowFailOnInit);
    }

    /**
     * Creates a new instance of {@code MultiFileConfigurationBuilder} and sets
     * initialization parameters.
     *
     * @param resCls the result configuration class
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public MultiFileConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params)
    {
        super(resCls, params);
    }

    /**
     * Creates a new instance of {@code MultiFileConfigurationBuilder} without
     * setting initialization parameters.
     *
     * @param resCls the result configuration class
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public MultiFileConfigurationBuilder(final Class<? extends T> resCls)
    {
        super(resCls);
    }

    /**
     * {@inheritDoc} This method is overridden to adapt the return type.
     */
    @Override
    public MultiFileConfigurationBuilder<T> configure(final BuilderParameters... params)
    {
        super.configure(params);
        return this;
    }

    /**
     * {@inheritDoc} This implementation evaluates the file name pattern using
     * the configured {@code ConfigurationInterpolator}. If this file has
     * already been loaded, the corresponding builder is accessed. Otherwise, a
     * new builder is created for loading this configuration file.
     */
    @Override
    public T getConfiguration() throws ConfigurationException
    {
        return getManagedBuilder().getConfiguration();
    }

    /**
     * Returns the managed {@code FileBasedConfigurationBuilder} for the current
     * file name pattern. It is determined based on the evaluation of the file
     * name pattern using the configured {@code ConfigurationInterpolator}. If
     * this is the first access to this configuration file, the builder is
     * created.
     *
     * @return the configuration builder for the configuration corresponding to
     *         the current evaluation of the file name pattern
     * @throws ConfigurationException if the builder cannot be determined (e.g.
     *         due to missing initialization parameters)
     */
    public FileBasedConfigurationBuilder<T> getManagedBuilder()
            throws ConfigurationException
    {
        final Map<String, Object> params = getParameters();
        final MultiFileBuilderParametersImpl multiParams =
                MultiFileBuilderParametersImpl.fromParameters(params, true);
        if (multiParams.getFilePattern() == null)
        {
            throw new ConfigurationException("No file name pattern is set!");
        }
        final String fileName = fetchFileName(multiParams);

        FileBasedConfigurationBuilder<T> builder =
                getManagedBuilders().get(fileName);
        if (builder == null)
        {
            builder =
                    createInitializedManagedBuilder(fileName,
                            createManagedBuilderParameters(params, multiParams));
            final FileBasedConfigurationBuilder<T> newBuilder =
                    ConcurrentUtils.putIfAbsent(getManagedBuilders(), fileName,
                            builder);
            if (newBuilder == builder)
            {
                initListeners(newBuilder);
            }
            else
            {
                builder = newBuilder;
            }
        }
        return builder;
    }

    /**
     * {@inheritDoc} This implementation ensures that the listener is also added
     * to managed configuration builders if necessary. Listeners for the builder-related
     * event types are excluded because otherwise they would be triggered by the
     * internally used configuration builders.
     */
    @Override
    public synchronized <E extends Event> void addEventListener(
            final EventType<E> eventType, final EventListener<? super E> l)
    {
        super.addEventListener(eventType, l);
        if (isEventTypeForManagedBuilders(eventType))
        {
            for (final FileBasedConfigurationBuilder<T> b : getManagedBuilders()
                    .values())
            {
                b.addEventListener(eventType, l);
            }
            configurationListeners.addEventListener(eventType, l);
        }
    }

    /**
     * {@inheritDoc} This implementation ensures that the listener is also
     * removed from managed configuration builders if necessary.
     */
    @Override
    public synchronized <E extends Event> boolean removeEventListener(
            final EventType<E> eventType, final EventListener<? super E> l)
    {
        final boolean result = super.removeEventListener(eventType, l);
        if (isEventTypeForManagedBuilders(eventType))
        {
            for (final FileBasedConfigurationBuilder<T> b : getManagedBuilders()
                    .values())
            {
                b.removeEventListener(eventType, l);
            }
            configurationListeners.removeEventListener(eventType, l);
        }
        return result;
    }

    /**
     * {@inheritDoc} This implementation clears the cache with all managed
     * builders.
     */
    @Override
    public synchronized void resetParameters()
    {
        for (final FileBasedConfigurationBuilder<T> b : getManagedBuilders().values())
        {
            b.removeEventListener(ConfigurationBuilderEvent.ANY,
                    managedBuilderDelegationListener);
        }
        getManagedBuilders().clear();
        interpolator.set(null);
        super.resetParameters();
    }

    /**
     * Returns the {@code ConfigurationInterpolator} used by this instance. This
     * is the object used for evaluating the file name pattern. It is created on
     * demand.
     *
     * @return the {@code ConfigurationInterpolator}
     */
    protected ConfigurationInterpolator getInterpolator()
    {
        ConfigurationInterpolator result;
        boolean done;

        // This might create multiple instances under high load,
        // however, always the same instance is returned.
        do
        {
            result = interpolator.get();
            if (result != null)
            {
                done = true;
            }
            else
            {
                result = createInterpolator();
                done = interpolator.compareAndSet(null, result);
            }
        } while (!done);

        return result;
    }

    /**
     * Creates the {@code ConfigurationInterpolator} to be used by this
     * instance. This method is called when a file name is to be constructed,
     * but no current {@code ConfigurationInterpolator} instance is available.
     * It obtains an instance from this builder's parameters. If no properties
     * of the {@code ConfigurationInterpolator} are specified in the parameters,
     * a default instance without lookups is returned (which is probably not
     * very helpful).
     *
     * @return the {@code ConfigurationInterpolator} to be used
     */
    protected ConfigurationInterpolator createInterpolator()
    {
        final InterpolatorSpecification spec =
                BasicBuilderParameters
                        .fetchInterpolatorSpecification(getParameters());
        return ConfigurationInterpolator.fromSpecification(spec);
    }

    /**
     * Determines the file name of a configuration based on the file name
     * pattern. This method is called on every access to this builder's
     * configuration. It obtains the {@link ConfigurationInterpolator} from this
     * builder's parameters and uses it to interpolate the file name pattern.
     *
     * @param multiParams the parameters object for this builder
     * @return the name of the configuration file to be loaded
     */
    protected String constructFileName(
            final MultiFileBuilderParametersImpl multiParams)
    {
        final ConfigurationInterpolator ci = getInterpolator();
        return String.valueOf(ci.interpolate(multiParams.getFilePattern()));
    }

    /**
     * Creates a builder for a managed configuration. This method is called
     * whenever a configuration for a file name is requested which has not yet
     * been loaded. The passed in map with parameters is populated from this
     * builder's configuration (i.e. the basic parameters plus the optional
     * parameters for managed builders). This base implementation creates a
     * standard builder for file-based configurations. Derived classes may
     * override it to create special purpose builders.
     *
     * @param fileName the name of the file to be loaded
     * @param params a map with initialization parameters for the new builder
     * @return the newly created builder instance
     * @throws ConfigurationException if an error occurs
     */
    protected FileBasedConfigurationBuilder<T> createManagedBuilder(
            final String fileName, final Map<String, Object> params)
            throws ConfigurationException
    {
        return new FileBasedConfigurationBuilder<>(getResultClass(), params,
                isAllowFailOnInit());
    }

    /**
     * Creates a fully initialized builder for a managed configuration. This
     * method is called by {@code getConfiguration()} whenever a configuration
     * file is requested which has not yet been loaded. This implementation
     * delegates to {@code createManagedBuilder()} for actually creating the
     * builder object. Then it sets the location to the configuration file.
     *
     * @param fileName the name of the file to be loaded
     * @param params a map with initialization parameters for the new builder
     * @return the newly created and initialized builder instance
     * @throws ConfigurationException if an error occurs
     */
    protected FileBasedConfigurationBuilder<T> createInitializedManagedBuilder(
            final String fileName, final Map<String, Object> params)
            throws ConfigurationException
    {
        final FileBasedConfigurationBuilder<T> managedBuilder =
                createManagedBuilder(fileName, params);
        managedBuilder.getFileHandler().setFileName(fileName);
        return managedBuilder;
    }

    /**
     * Returns the map with the managed builders created so far by this
     * {@code MultiFileConfigurationBuilder}. This map is exposed to derived
     * classes so they can access managed builders directly. However, derived
     * classes are not expected to manipulate this map.
     *
     * @return the map with the managed builders
     */
    protected ConcurrentMap<String, FileBasedConfigurationBuilder<T>> getManagedBuilders()
    {
        return managedBuilders;
    }

    /**
     * Registers event listeners at the passed in newly created managed builder.
     * This method registers a special {@code EventListener} which propagates
     * builder events to listeners registered at this builder. In addition,
     * {@code ConfigurationListener} and {@code ConfigurationErrorListener}
     * objects are registered at the new builder.
     *
     * @param newBuilder the builder to be initialized
     */
    private void initListeners(final FileBasedConfigurationBuilder<T> newBuilder)
    {
        copyEventListeners(newBuilder, configurationListeners);
        newBuilder.addEventListener(ConfigurationBuilderEvent.ANY,
                managedBuilderDelegationListener);
    }

    /**
     * Generates a file name for a managed builder based on the file name
     * pattern. This method prevents infinite loops which could happen if the
     * file name pattern cannot be resolved and the
     * {@code ConfigurationInterpolator} used by this object causes a recursive
     * lookup to this builder's configuration.
     *
     * @param multiParams the current builder parameters
     * @return the file name for a managed builder
     */
    private String fetchFileName(final MultiFileBuilderParametersImpl multiParams)
    {
        String fileName;
        final Boolean reentrant = inInterpolation.get();
        if (reentrant != null && reentrant.booleanValue())
        {
            fileName = multiParams.getFilePattern();
        }
        else
        {
            inInterpolation.set(Boolean.TRUE);
            try
            {
                fileName = constructFileName(multiParams);
            }
            finally
            {
                inInterpolation.set(Boolean.FALSE);
            }
        }
        return fileName;
    }

    /**
     * Handles events received from managed configuration builders. This method
     * creates a new event with a source pointing to this builder and propagates
     * it to all registered listeners.
     *
     * @param event the event received from a managed builder
     */
    private void handleManagedBuilderEvent(final ConfigurationBuilderEvent event)
    {
        if (ConfigurationBuilderEvent.RESET.equals(event.getEventType()))
        {
            resetResult();
        }
        else
        {
            fireBuilderEvent(createEventWithChangedSource(event));
        }
    }

    /**
     * Creates a new {@code ConfigurationBuilderEvent} based on the passed in
     * event, but with the source changed to this builder. This method is called
     * when an event was received from a managed builder. In this case, the
     * event has to be passed to the builder listeners registered at this
     * object, but with the correct source property.
     *
     * @param event the event received from a managed builder
     * @return the event to be propagated
     */
    private ConfigurationBuilderEvent createEventWithChangedSource(
            final ConfigurationBuilderEvent event)
    {
        if (ConfigurationBuilderResultCreatedEvent.RESULT_CREATED.equals(event
                .getEventType()))
        {
            return new ConfigurationBuilderResultCreatedEvent(this,
                    ConfigurationBuilderResultCreatedEvent.RESULT_CREATED,
                    ((ConfigurationBuilderResultCreatedEvent) event)
                            .getConfiguration());
        }
        @SuppressWarnings("unchecked")
        final
        // This is safe due to the constructor of ConfigurationBuilderEvent
        EventType<? extends ConfigurationBuilderEvent> type =
                (EventType<? extends ConfigurationBuilderEvent>) event
                        .getEventType();
        return new ConfigurationBuilderEvent(this, type);
    }

    /**
     * Creates a map with parameters for a new managed configuration builder.
     * This method merges the basic parameters set for this builder with the
     * specific parameters object for managed builders (if provided).
     *
     * @param params the parameters of this builder
     * @param multiParams the parameters object for this builder
     * @return the parameters for a new managed builder
     */
    private static Map<String, Object> createManagedBuilderParameters(
            final Map<String, Object> params,
            final MultiFileBuilderParametersImpl multiParams)
    {
        final Map<String, Object> newParams = new HashMap<>(params);
        newParams.remove(KEY_INTERPOLATOR);
        final BuilderParameters managedBuilderParameters =
                multiParams.getManagedBuilderParameters();
        if (managedBuilderParameters != null)
        {
            // clone parameters as they are applied to multiple builders
            final BuilderParameters copy =
                    (BuilderParameters) ConfigurationUtils
                            .cloneIfPossible(managedBuilderParameters);
            newParams.putAll(copy.getParameters());
        }
        return newParams;
    }

    /**
     * Checks whether the given event type is of interest for the managed
     * configuration builders. This method is called by the methods for managing
     * event listeners to find out whether a listener should be passed to the
     * managed builders, too.
     *
     * @param eventType the event type object
     * @return a flag whether this event type is of interest for managed
     *         builders
     */
    private static boolean isEventTypeForManagedBuilders(final EventType<?> eventType)
    {
        return !EventType
                .isInstanceOf(eventType, ConfigurationBuilderEvent.ANY);
    }
}
