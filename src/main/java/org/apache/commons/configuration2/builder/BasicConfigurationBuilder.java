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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.Initializable;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.ConstructorArg;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerList;
import org.apache.commons.configuration2.event.EventListenerRegistrationData;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.reloading.ReloadingController;

/**
 * <p>
 * An implementation of the {@code ConfigurationBuilder} interface which is able
 * to create different concrete {@code ImmutableConfiguration} implementations based on
 * reflection.
 * </p>
 * <p>
 * When constructing an instance of this class the concrete
 * {@code ImmutableConfiguration} implementation class has to be provided. Then
 * properties for the new {@code ImmutableConfiguration} instance can be set. The first
 * call to {@code getConfiguration()} creates and initializes the new
 * {@code ImmutableConfiguration} object. It is cached and returned by subsequent calls.
 * This cache - and also the initialization properties set so far - can be
 * flushed by calling one of the {@code reset()} methods. That way other
 * {@code ImmutableConfiguration} instances with different properties can be created.
 * </p>
 * <p>
 * If the newly created {@code ImmutableConfiguration} object implements the
 * {@code Initializable} interface, its {@code initialize()} method is called
 * after all initialization properties have been set. This way a concrete
 * implementation class can perform arbitrary initialization steps.
 * </p>
 * <p>
 * There are multiple options for setting up a {@code BasicConfigurationBuilder}
 * instance:
 * </p>
 * <ul>
 * <li>All initialization properties can be set in one or multiple calls of the
 * {@code configure()} method. In each call an arbitrary number of
 * {@link BuilderParameters} objects can be passed. The API allows method
 * chaining and is intended to be used from Java code.</li>
 * <li>If builder instances are created by other means - e.g. using a dependency
 * injection framework -, the fluent API approach may not be suitable. For those
 * use cases it is also possible to pass in all initialization parameters as a
 * map. The keys of the map have to match initialization properties of the
 * {@code ImmutableConfiguration} object to be created, the values are the corresponding
 * property values. For instance, the key <em>throwExceptionOnMissing</em> in
 * the map will cause the method {@code setThrowExceptionOnMissing()} on the
 * {@code ImmutableConfiguration} object to be called with the corresponding value as
 * parameter.</li>
 * </ul>
 * <p>
 * A builder instance can be constructed with an <em>allowFailOnInit</em>
 * flag. If set to <strong>true</strong>, exceptions during initialization
 * of the configuration are ignored; in such a case an empty configuration
 * object is returned. A use case for this flag is a scenario in which a
 * configuration is optional and created on demand the first time configuration
 * data is to be stored. Consider an application that stores user-specific
 * configuration data in the user's home directory: When started for the first
 * time by a new user there is no configuration file; so it makes sense to
 * start with an empty configuration object. On application exit, settings
 * can be stored in this object and written to the associated file. Then they
 * are available on next application start.
 * </p>
 * <p>
 * This class is thread-safe. Multiple threads can modify initialization
 * properties and call {@code getConfiguration()}. However, the intended use
 * case is that the builder is configured by a single thread first. Then
 * {@code getConfiguration()} can be called concurrently, and it is guaranteed
 * that always the same {@code ImmutableConfiguration} instance is returned until the
 * builder is reset.
 * </p>
 *
 * @since 2.0
 * @param <T> the concrete type of {@code ImmutableConfiguration} objects created by this
 *        builder
 */
public class BasicConfigurationBuilder<T extends ImmutableConfiguration> implements
        ConfigurationBuilder<T>
{
    /** The class of the objects produced by this builder instance. */
    private final Class<? extends T> resultClass;

    /** An object managing the event listeners registered at this builder. */
    private final EventListenerList eventListeners;

    /** A flag whether exceptions on initializing configurations are allowed. */
    private final boolean allowFailOnInit;

    /** The map with current initialization parameters. */
    private Map<String, Object> parameters;

    /** The current bean declaration. */
    private BeanDeclaration resultDeclaration;

    /** The result object of this builder. */
    private volatile T result;

    /**
     * Creates a new instance of {@code BasicConfigurationBuilder} and
     * initializes it with the given result class. No initialization properties
     * are set.
     *
     * @param resCls the result class (must not be <b>null</b>)
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(final Class<? extends T> resCls)
    {
        this(resCls, null);
    }

    /**
     * Creates a new instance of {@code BasicConfigurationBuilder} and
     * initializes it with the given result class and an initial set of builder
     * parameters. The <em>allowFailOnInit</em> flag is set to
     * <strong>false</strong>.
     *
     * @param resCls the result class (must not be <b>null</b>)
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(final Class<? extends T> resCls, final Map<String, Object> params)
    {
        this(resCls, params, false);
    }

    /**
     * Creates a new instance of {@code BasicConfigurationBuilder} and
     * initializes it with the given result class, an initial set of builder
     * parameters, and the <em>allowFailOnInit</em> flag. The map with
     * parameters may be <b>null</b>, in this case no initialization parameters
     * are set.
     *
     * @param resCls the result class (must not be <b>null</b>)
     * @param params a map with initialization parameters
     * @param allowFailOnInit a flag whether exceptions on initializing a newly
     *        created {@code ImmutableConfiguration} object are allowed
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params, final boolean allowFailOnInit)
    {
        if (resCls == null)
        {
            throw new IllegalArgumentException("Result class must not be null!");
        }

        resultClass = resCls;
        this.allowFailOnInit = allowFailOnInit;
        eventListeners = new EventListenerList();
        updateParameters(params);
    }

    /**
     * Returns the result class of this builder. The objects produced by this
     * builder have the class returned here.
     *
     * @return the result class of this builder
     */
    public Class<? extends T> getResultClass()
    {
        return resultClass;
    }

    /**
     * Returns the <em>allowFailOnInit</em> flag. See the header comment for
     * information about this flag.
     *
     * @return the <em>allowFailOnInit</em> flag
     */
    public boolean isAllowFailOnInit()
    {
        return allowFailOnInit;
    }

    /**
     * Sets the initialization parameters of this builder. Already existing
     * parameters are replaced by the content of the given map.
     *
     * @param params the new initialization parameters of this builder; can be
     *        <b>null</b>, then all initialization parameters are removed
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> setParameters(
            final Map<String, Object> params)
    {
        updateParameters(params);
        return this;
    }

    /**
     * Adds the content of the given map to the already existing initialization
     * parameters.
     *
     * @param params the map with additional initialization parameters; may be
     *        <b>null</b>, then this call has no effect
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> addParameters(
            final Map<String, Object> params)
    {
        final Map<String, Object> newParams =
                new HashMap<>(getParameters());
        if (params != null)
        {
            newParams.putAll(params);
        }
        updateParameters(newParams);
        return this;
    }

    /**
     * Appends the content of the specified {@code BuilderParameters} objects to
     * the current initialization parameters. Calling this method multiple times
     * will create a union of the parameters provided.
     *
     * @param params an arbitrary number of objects with builder parameters
     * @return a reference to this builder for method chaining
     * @throws NullPointerException if a <b>null</b> array is passed
     */
    public BasicConfigurationBuilder<T> configure(final BuilderParameters... params)
    {
        final Map<String, Object> newParams = new HashMap<>();
        for (final BuilderParameters p : params)
        {
            newParams.putAll(p.getParameters());
            handleEventListenerProviders(p);
        }

        return setParameters(newParams);
    }

    /**
     * {@inheritDoc} This implementation creates the result configuration on
     * first access. Later invocations return the same object until this builder
     * is reset. The double-check idiom for lazy initialization is used (Bloch,
     * Effective Java, item 71).
     */
    @Override
    public T getConfiguration() throws ConfigurationException
    {
        fireBuilderEvent(new ConfigurationBuilderEvent(this,
                ConfigurationBuilderEvent.CONFIGURATION_REQUEST));

        T resObj = result;
        boolean created = false;
        if (resObj == null)
        {
            synchronized (this)
            {
                resObj = result;
                if (resObj == null)
                {
                    result = resObj = createResult();
                    created = true;
                }
            }
        }

        if (created)
        {
            fireBuilderEvent(new ConfigurationBuilderResultCreatedEvent(this,
                    ConfigurationBuilderResultCreatedEvent.RESULT_CREATED,
                    resObj));
        }
        return resObj;
    }

    /**
     * {@inheritDoc} This implementation also takes care that the event listener
     * is added to the managed configuration object.
     *
     * @throws IllegalArgumentException if the event type or the listener is
     *         <b>null</b>
     */
    @Override
    public <E extends Event> void addEventListener(
            final EventType<E> eventType, final EventListener<? super E> listener)
    {
        installEventListener(eventType, listener);
    }

    /**
     * {@inheritDoc} This implementation also takes care that the event listener
     * is removed from the managed configuration object.
     */
    @Override
    public <E extends Event> boolean removeEventListener(
            final EventType<E> eventType, final EventListener<? super E> listener)
    {
        fetchEventSource().removeEventListener(eventType, listener);
        return eventListeners.removeEventListener(eventType, listener);
    }

    /**
     * Clears an existing result object. An invocation of this method causes a
     * new {@code ImmutableConfiguration} object to be created the next time
     * {@link #getConfiguration()} is called.
     */
    public void resetResult()
    {
        T oldResult;
        synchronized (this)
        {
            oldResult = result;
            result = null;
            resultDeclaration = null;
        }

        if (oldResult != null)
        {
            removeEventListeners(oldResult);
        }
        fireBuilderEvent(new ConfigurationBuilderEvent(this,
                ConfigurationBuilderEvent.RESET));
    }

    /**
     * Removes all initialization parameters of this builder. This method can be
     * called if this builder is to be reused for creating result objects with a
     * different configuration.
     */
    public void resetParameters()
    {
        setParameters(null);
    }

    /**
     * Resets this builder. This is a convenience method which combines calls to
     * {@link #resetResult()} and {@link #resetParameters()}.
     */
    public synchronized void reset()
    {
        resetParameters();
        resetResult();
    }

    /**
     * Connects this builder with a {@code ReloadingController}. With this
     * method support for reloading can be added to an arbitrary builder object.
     * Event listeners are registered at the reloading controller and this
     * builder with connect both objects:
     * <ul>
     * <li>When the reloading controller detects that a reload is required, the
     * builder's {@link #resetResult()} method is called; so the managed result
     * object is invalidated.</li>
     * <li>When a new result object has been created the controller's reloading
     * state is reset, so that new changes can be detected again.</li>
     * </ul>
     *
     * @param controller the {@code ReloadingController} to connect to (must not
     *        be <b>null</b>)
     * @throws IllegalArgumentException if the controller is <b>null</b>
     */
    public final void connectToReloadingController(
            final ReloadingController controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException(
                    "ReloadingController must not be null!");
        }
        ReloadingBuilderSupportListener.connect(this, controller);
    }

    /**
     * Creates a new, initialized result object. This method is called by
     * {@code getConfiguration()} if no valid result object exists. This base
     * implementation performs two steps:
     * <ul>
     * <li>{@code createResultInstance()} is called to create a new,
     * uninitialized result object.</li>
     * <li>{@code initResultInstance()} is called to process all initialization
     * parameters.</li>
     * </ul>
     * It also evaluates the <em>allowFailOnInit</em> flag, i.e. if
     * initialization causes an exception and this flag is set, the exception is
     * ignored, and the newly created, uninitialized configuration is returned.
     * Note that this method is called in a synchronized block.
     *
     * @return the newly created result object
     * @throws ConfigurationException if an error occurs
     */
    protected T createResult() throws ConfigurationException
    {
        final T resObj = createResultInstance();

        try
        {
            initResultInstance(resObj);
        }
        catch (final ConfigurationException cex)
        {
            if (!isAllowFailOnInit())
            {
                throw cex;
            }
        }

        return resObj;
    }

    /**
     * Creates the new, uninitialized result object. This is the first step of
     * the process of producing a result object for this builder. This
     * implementation uses the {@link BeanHelper} class to create a new object
     * based on the {@link BeanDeclaration} returned by
     * {@link #getResultDeclaration()}. Note: This method is invoked in a
     * synchronized block.
     *
     * @return the newly created, yet uninitialized result object
     * @throws ConfigurationException if an exception occurs
     */
    protected T createResultInstance() throws ConfigurationException
    {
        final Object bean = fetchBeanHelper().createBean(getResultDeclaration());
        checkResultInstance(bean);
        return getResultClass().cast(bean);
    }

    /**
     * Initializes a newly created result object. This is the second step of the
     * process of producing a result object for this builder. This
     * implementation uses the {@link BeanHelper} class to initialize the
     * object's property based on the {@link BeanDeclaration} returned by
     * {@link #getResultDeclaration()}. Note: This method is invoked in a
     * synchronized block. This is required because internal state is accessed.
     * Sub classes must not call this method without proper synchronization.
     *
     * @param obj the object to be initialized
     * @throws ConfigurationException if an error occurs
     */
    protected void initResultInstance(final T obj) throws ConfigurationException
    {
        fetchBeanHelper().initBean(obj, getResultDeclaration());
        registerEventListeners(obj);
        handleInitializable(obj);
    }

    /**
     * Returns the {@code BeanDeclaration} that is used to create and initialize
     * result objects. The declaration is created on first access (by invoking
     * {@link #createResultDeclaration(Map)}) based on the current
     * initialization parameters.
     *
     * @return the {@code BeanDeclaration} for dynamically creating a result
     *         object
     * @throws ConfigurationException if an error occurs
     */
    protected final synchronized BeanDeclaration getResultDeclaration()
            throws ConfigurationException
    {
        if (resultDeclaration == null)
        {
            resultDeclaration = createResultDeclaration(getFilteredParameters());
        }
        return resultDeclaration;
    }

    /**
     * Returns a (unmodifiable) map with the current initialization parameters
     * set for this builder. The map is populated with the parameters set using
     * the various configuration options.
     *
     * @return a map with the current set of initialization parameters
     */
    protected final synchronized Map<String, Object> getParameters()
    {
        if (parameters != null)
        {
            return parameters;
        }
        return Collections.emptyMap();
    }

    /**
     * Obtains the {@code BeanHelper} object to be used when dealing with bean
     * declarations. This method checks whether this builder was configured with
     * a specific {@code BeanHelper} instance. If so, this instance is used.
     * Otherwise, the default {@code BeanHelper} is returned.
     *
     * @return the {@code BeanHelper} to be used
     */
    protected final BeanHelper fetchBeanHelper()
    {
        final BeanHelper helper =
                BasicBuilderParameters.fetchBeanHelper(getParameters());
        return (helper != null) ? helper : BeanHelper.INSTANCE;
    }

    /**
     * Creates a new {@code BeanDeclaration} which is used for creating new
     * result objects dynamically. This implementation creates a specialized
     * {@code BeanDeclaration} object that is initialized from the given map of
     * initialization parameters. The {@code BeanDeclaration} must be
     * initialized with the result class of this builder, otherwise exceptions
     * will be thrown when the result object is created. Note: This method is
     * invoked in a synchronized block.
     *
     * @param params a snapshot of the current initialization parameters
     * @return the {@code BeanDeclaration} for creating result objects
     * @throws ConfigurationException if an error occurs
     */
    protected BeanDeclaration createResultDeclaration(
            final Map<String, Object> params) throws ConfigurationException
    {
        return new BeanDeclaration()
        {
            @Override
            public Map<String, Object> getNestedBeanDeclarations()
            {
                // no nested beans
                return Collections.emptyMap();
            }

            @Override
            public Collection<ConstructorArg> getConstructorArgs()
            {
                // no constructor arguments
                return Collections.emptySet();
            }

            @Override
            public Map<String, Object> getBeanProperties()
            {
                // the properties are equivalent to the parameters
                return params;
            }

            @Override
            public Object getBeanFactoryParameter()
            {
                return null;
            }

            @Override
            public String getBeanFactoryName()
            {
                return null;
            }

            @Override
            public String getBeanClassName()
            {
                return getResultClass().getName();
            }
        };
    }

    /**
     * Copies all {@code EventListener} objects registered at this builder to
     * the specified target configuration builder. This method is intended to be
     * used by derived classes which support inheritance of their properties to
     * other builder objects.
     *
     * @param target the target configuration builder (must not be <b>null</b>)
     * @throws NullPointerException if the target builder is <b>null</b>
     */
    protected synchronized void copyEventListeners(
            final BasicConfigurationBuilder<?> target)
    {
        copyEventListeners(target, eventListeners);
    }

    /**
     * Copies all event listeners in the specified list to the specified target
     * configuration builder. This method is intended to be used by derived
     * classes which have to deal with managed configuration builders that need
     * to be initialized with event listeners.
     *
     * @param target the target configuration builder (must not be <b>null</b>)
     * @param listeners the event listeners to be copied over
     * @throws NullPointerException if the target builder is <b>null</b>
     */
    protected void copyEventListeners(final BasicConfigurationBuilder<?> target,
            final EventListenerList listeners)
    {
        target.eventListeners.addAll(listeners);
    }

    /**
     * Adds the specified event listener to this object. This method is called
     * by {@code addEventListener()}, it does the actual listener registration.
     * Because it is final it can be called by sub classes in the constructor if
     * there is already the need to register an event listener.
     *
     * @param eventType the event type object
     * @param listener the listener to be registered
     * @param <E> the event type
     */
    protected final <E extends Event> void installEventListener(
            final EventType<E> eventType, final EventListener<? super E> listener)
    {
        fetchEventSource().addEventListener(eventType, listener);
        eventListeners.addEventListener(eventType, listener);
    }

    /**
     * Sends the specified builder event to all registered listeners.
     *
     * @param event the event to be fired
     */
    protected void fireBuilderEvent(final ConfigurationBuilderEvent event)
    {
        eventListeners.fire(event);
    }

    /**
     * Replaces the current map with parameters by a new one.
     *
     * @param newParams the map with new parameters (may be <b>null</b>)
     */
    private void updateParameters(final Map<String, Object> newParams)
    {
        final Map<String, Object> map = new HashMap<>();
        if (newParams != null)
        {
            map.putAll(newParams);
        }
        parameters = Collections.unmodifiableMap(map);
    }

    /**
     * Registers the available event listeners at the given object. This method
     * is called for each result object created by the builder.
     *
     * @param obj the object to initialize
     */
    private void registerEventListeners(final T obj)
    {
        final EventSource evSrc = ConfigurationUtils.asEventSource(obj, true);
        for (final EventListenerRegistrationData<?> regData : eventListeners
                .getRegistrations())
        {
            registerListener(evSrc, regData);
        }
    }

    /**
     * Removes all available event listeners from the given result object. This
     * method is called when the result of this builder is reset. Then the old
     * managed configuration should no longer generate events.
     *
     * @param obj the affected result object
     */
    private void removeEventListeners(final T obj)
    {
        final EventSource evSrc = ConfigurationUtils.asEventSource(obj, true);
        for (final EventListenerRegistrationData<?> regData : eventListeners
                .getRegistrations())
        {
            removeListener(evSrc, regData);
        }
    }

    /**
     * Returns an {@code EventSource} for the current result object. If there is
     * no current result or if it does not extend {@code EventSource}, a dummy
     * event source is returned.
     *
     * @return the {@code EventSource} for the current result object
     */
    private EventSource fetchEventSource()
    {
        return ConfigurationUtils.asEventSource(result, true);
    }

    /**
     * Checks whether the specified parameters object implements the
     * {@code EventListenerProvider} interface. If so, the event listeners it
     * provides are added to this builder.
     *
     * @param params the parameters object
     */
    private void handleEventListenerProviders(final BuilderParameters params)
    {
        if (params instanceof EventListenerProvider)
        {
            eventListeners.addAll(((EventListenerProvider) params)
                    .getListeners());
        }
    }

    /**
     * Checks whether the class of the result configuration is compatible with
     * this builder's result class. This is done to ensure that only objects of
     * the expected result class are created.
     *
     * @param inst the result instance to be checked
     * @throws ConfigurationRuntimeException if an invalid result class is
     *         detected
     */
    private void checkResultInstance(final Object inst)
    {
        if (!getResultClass().isInstance(inst))
        {
            throw new ConfigurationRuntimeException(
                    "Incompatible result object: " + inst);
        }
    }

    /**
     * Returns a map with initialization parameters where all parameters
     * starting with the reserved prefix have been filtered out.
     *
     * @return the filtered parameters map
     */
    private Map<String, Object> getFilteredParameters()
    {
        final Map<String, Object> filteredMap =
                new HashMap<>(getParameters());
        for (final Iterator<String> it = filteredMap.keySet().iterator(); it
                .hasNext();)
        {
            final String key = it.next();
            if (key.startsWith(BuilderParameters.RESERVED_PARAMETER_PREFIX))
            {
                it.remove();
            }
        }
        return filteredMap;
    }

    /**
     * Performs special initialization of the result object. This method is
     * called after parameters have been set on a newly created result instance.
     * If supported by the result class, the {@code initialize()} method is now
     * called.
     *
     * @param obj the newly created result object
     */
    private void handleInitializable(final T obj)
    {
        if (obj instanceof Initializable)
        {
            ((Initializable) obj).initialize();
        }
    }

    /**
     * Registers an event listener at an event source object.
     *
     * @param evSrc the event source
     * @param regData the registration data object
     * @param <E> the type of the event listener
     */
    private static <E extends Event> void registerListener(final EventSource evSrc,
            final EventListenerRegistrationData<E> regData)
    {
        evSrc.addEventListener(regData.getEventType(), regData.getListener());
    }

    /**
     * Removes an event listener from an event source object.
     *
     * @param evSrc the event source
     * @param regData the registration data object
     * @param <E> the type of the event listener
     */
    private static <E extends Event> void removeListener(final EventSource evSrc,
            final EventListenerRegistrationData<E> regData)
    {
        evSrc.removeEventListener(regData.getEventType(), regData.getListener());
    }
}
