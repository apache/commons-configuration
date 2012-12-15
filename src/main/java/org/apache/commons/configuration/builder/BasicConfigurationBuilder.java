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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.beanutils.ConstructorArg;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * <p>
 * An implementation of the {@code ConfigurationBuilder} interface which is able
 * to create different concrete {@code Configuration} implementations based on
 * reflection.
 * </p>
 * <p>
 * When constructing an instance of this class the concrete
 * {@code Configuration} implementation class has to be provided. Then
 * properties for the new {@code Configuration} instance can be set. The first
 * call to {@code getConfiguration()} creates and initializes the new
 * {@code Configuration} object. It is cached and returned by subsequent calls.
 * This cache - and also the initialization properties set so far - can be
 * flushed by calling one of the {@code reset()} methods. That way other
 * {@code Configuration} instances with different properties can be created.
 * </p>
 * <p>
 * There are multiple options for setting up a {@code BasicConfigurationBuilder}
 * instance:
 * <ul>
 * <li>All initialization properties can be set in one or multiple calls of the
 * {@code configure()} method. In each call an arbitrary number of
 * {@link BuilderParameters} objects can be passed. The API allows method
 * chaining and is intended to be used from Java code.</li>
 * <li>If builder instances are created by other means - e.g. using a dependency
 * injection framework -, the fluent API approach may not be suitable. For those
 * use cases it is also possible to pass in all initialization parameters as a
 * map. The keys of the map have to match initialization properties of the
 * {@code Configuration} object to be created, the values are the corresponding
 * property values. For instance, the key <em>throwExceptionOnMissing</em> in
 * the map will cause the method {@code setThrowExceptionOnMissing()} on the
 * {@code Configuration} object to be called with the corresponding value as
 * parameter.</li>
 * </ul>
 * </p>
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
 * that always the same {@code Configuration} instance is returned until the
 * builder is reset.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class BasicConfigurationBuilder<T extends Configuration> implements
        ConfigurationBuilder<T>
{
    /**
     * A dummy event source that is used for registering listeners if no
     * compatible result object is available. This source has empty dummy
     * implementations for listener registration methods.
     */
    private static final EventSource DUMMY_EVENT_SOURCE = new EventSource()
    {
        public void addConfigurationListener(ConfigurationListener l)
        {
        }

        public boolean removeConfigurationListener(ConfigurationListener l)
        {
            return false;
        }

        public void addErrorListener(ConfigurationErrorListener l)
        {
        }

        public boolean removeErrorListener(ConfigurationErrorListener l)
        {
            return false;
        }
    };

    /** The class of the objects produced by this builder instance. */
    private final Class<T> resultClass;

    /**
     * A collection with configuration listeners to be registered at newly
     * created configuration objects.
     */
    private final Collection<ConfigurationListener> configListeners;

    /**
     * A collection with error listeners to be registered at newly created
     * configuration objects.
     */
    private final Collection<ConfigurationErrorListener> errorListeners;

    /** An object managing the builder listeners registered at this builder. */
    private final EventListenerSupport<BuilderListener> builderListeners;

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
     * @param resCls the result class (must not be <b>null</b>
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(Class<T> resCls)
    {
        this(resCls, null);
    }

    /**
     * Creates a new instance of {@code BasicConfigurationBuilder} and
     * initializes it with the given result class and an initial set of builder
     * parameters. The <em>allowFailOnInit</em> flag is set to
     * <strong>false</strong>.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(Class<T> resCls, Map<String, Object> params)
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
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @param allowFailOnInit a flag whether exceptions on initializing a newly
     *        created {@code Configuration} object are allowed
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public BasicConfigurationBuilder(Class<T> resCls,
            Map<String, Object> params, boolean allowFailOnInit)
    {
        if (resCls == null)
        {
            throw new IllegalArgumentException("Result class must not be null!");
        }

        resultClass = resCls;
        this.allowFailOnInit = allowFailOnInit;
        configListeners = new ArrayList<ConfigurationListener>();
        errorListeners = new ArrayList<ConfigurationErrorListener>();
        builderListeners = EventListenerSupport.create(BuilderListener.class);
        updateParameters(params);
    }

    /**
     * Returns the result class of this builder. The objects produced by this
     * builder have the class returned here.
     *
     * @return the result class of this builder
     */
    public Class<T> getResultClass()
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
            Map<String, Object> params)
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
            Map<String, Object> params)
    {
        Map<String, Object> newParams =
                new HashMap<String, Object>(getParameters());
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
    public BasicConfigurationBuilder<T> configure(BuilderParameters... params)
    {
        Map<String, Object> newParams = new HashMap<String, Object>();
        for (BuilderParameters p : params)
        {
            newParams.putAll(p.getParameters());
        }

        return setParameters(newParams);
    }

    /**
     * Adds the specified listener for {@code ConfigurationEvent}s to this
     * builder. It is also registered at the result objects produced by this
     * builder.
     *
     * @param l the listener to be registered
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> addConfigurationListener(
            ConfigurationListener l)
    {
        configListeners.add(l);
        fetchEventSource().addConfigurationListener(l);
        return this;
    }

    /**
     * Removes the specified listener for {@code ConfigurationEvent}s from this
     * builder. It is also removed from the current result object if it exists.
     *
     * @param l the listener to be removed
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> removeConfigurationListener(
            ConfigurationListener l)
    {
        configListeners.remove(l);
        fetchEventSource().removeConfigurationListener(l);
        return this;
    }

    /**
     * Adds the specified listener for {@code ConfigurationErrorEvent}s to this
     * builder. It is also registered at the result objects produced by this
     * builder.
     *
     * @param l the listener to be registered
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> addErrorListener(
            ConfigurationErrorListener l)
    {
        errorListeners.add(l);
        fetchEventSource().addErrorListener(l);
        return this;
    }

    /**
     * Removes the specified listener for {@code ConfigurationErrorEvent}s from
     * this builder. It is also removed from the current result object if it
     * exists.
     *
     * @param l the listener to be removed
     * @return a reference to this builder for method chaining
     */
    public synchronized BasicConfigurationBuilder<T> removeErrorListener(
            ConfigurationErrorListener l)
    {
        errorListeners.remove(l);
        fetchEventSource().removeErrorListener(l);
        return this;
    }

    /**
     * {@inheritDoc} This implementation creates the result configuration on
     * first access. Later invocations return the same object until this builder
     * is reset. The double-check idiom for lazy initialization is used (Bloch,
     * Effective Java, item 71).
     */
    public T getConfiguration() throws ConfigurationException
    {
        T resObj = result;
        if (resObj == null)
        {
            synchronized (this)
            {
                resObj = result;
                if (resObj == null)
                {
                    result = resObj = createResult();
                }
            }
        }
        return resObj;
    }

    /**
     * {@inheritDoc} The listener must not be <b>null</b>, otherwise an
     * exception is thrown.
     *
     * @throws IllegalArgumentException if the listener is <b>null</b>
     */
    public void addBuilderListener(BuilderListener l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException(
                    "Builder listener must not be null!");
        }
        builderListeners.addListener(l);
    }

    /**
     * {@inheritDoc} If the specified listener is not registered at this object,
     * this method has no effect.
     */
    public void removeBuilderListener(BuilderListener l)
    {
        builderListeners.removeListener(l);
    }

    /**
     * Clears an existing result object. An invocation of this method causes a
     * new {@code Configuration} object to be created the next time
     * {@link #getConfiguration()} is called.
     */
    public void resetResult()
    {
        synchronized (this)
        {
            result = null;
            resultDeclaration = null;
        }

        builderListeners.fire().builderReset(this);
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
        T resObj = createResultInstance();

        try
        {
            initResultInstance(resObj);
        }
        catch (ConfigurationException cex)
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
        return getResultClass().cast(
                BeanHelper.createBean(getResultDeclaration()));
    }

    /**
     * Initializes a newly created result object. This is the second step of the
     * process of producing a result object for this builder. This
     * implementation uses the {@link BeanHelper} class to initialize the
     * object's property based on the {@link BeanDeclaration} returned by
     * {@link #getResultDeclaration()}. Note: This method is invoked in a
     * synchronized block
     *
     * @param obj the object to be initialized
     * @throws ConfigurationException if an error occurs
     */
    protected void initResultInstance(T obj) throws ConfigurationException
    {
        BeanHelper.initBean(obj, getResultDeclaration());
        registerEventListeners(obj);
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
    protected synchronized final BeanDeclaration getResultDeclaration()
            throws ConfigurationException
    {
        if (resultDeclaration == null)
        {
            resultDeclaration = createResultDeclaration(getFilteredParameters());
            checkResultClass(resultDeclaration);
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
    protected synchronized final Map<String, Object> getParameters()
    {
        if (parameters != null)
        {
            return parameters;
        }
        return Collections.emptyMap();
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
            public Map<String, Object> getNestedBeanDeclarations()
            {
                // no nested beans
                return Collections.emptyMap();
            }

            public Collection<ConstructorArg> getConstructorArgs()
            {
                // no constructor arguments
                return Collections.emptySet();
            }

            public Map<String, Object> getBeanProperties()
            {
                // the properties are equivalent to the parameters
                return params;
            }

            public Object getBeanFactoryParameter()
            {
                return null;
            }

            public String getBeanFactoryName()
            {
                return null;
            }

            public String getBeanClassName()
            {
                return getResultClass().getName();
            }
        };
    }

    /**
     * Copies all {@code ConfigurationListener} and
     * {@code ConfigurationErrorListener} objects to the specified target
     * configuration builder. This method is intended to be used by derived
     * classes which support inheritance of their properties to other builder
     * objects.
     *
     * @param target the target configuration builder (must not be <b>null</b>)
     * @throws NullPointerException if the target builder is <b>null</b>
     */
    protected synchronized void copyEventListeners(
            BasicConfigurationBuilder<?> target)
    {
        for (ConfigurationListener l : configListeners)
        {
            target.addConfigurationListener(l);
        }
        for (ConfigurationErrorListener l : errorListeners)
        {
            target.addErrorListener(l);
        }
    }

    /**
     * Replaces the current map with parameters by a new one.
     *
     * @param newParams the map with new parameters (may be <b>null</b>)
     */
    private void updateParameters(Map<String, Object> newParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();
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
    private void registerEventListeners(T obj)
    {
        EventSource evSrc = fetchEventSource(obj);
        for (ConfigurationListener l : configListeners)
        {
            evSrc.addConfigurationListener(l);
        }
        for (ConfigurationErrorListener l : errorListeners)
        {
            evSrc.addErrorListener(l);
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
        return fetchEventSource(result);
    }

    /**
     * Checks whether the bean class of the given {@code BeanDeclaration} equals
     * this builder's result class. This is done to ensure that only objects of
     * the expected result class are created.
     *
     * @param decl the declaration to be checked
     * @throws ConfigurationRuntimeException if an invalid bean class is
     *         detected
     */
    private void checkResultClass(BeanDeclaration decl)
    {
        if (!getResultClass().getName().equals(decl.getBeanClassName()))
        {
            throw new ConfigurationRuntimeException("Unexpected bean class: "
                    + decl.getBeanClassName());
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
        Map<String, Object> filteredMap =
                new HashMap<String, Object>(getParameters());
        for (Iterator<String> it = filteredMap.keySet().iterator(); it
                .hasNext();)
        {
            String key = it.next();
            if (key.startsWith(BuilderParameters.RESERVED_PARAMETER_PREFIX))
            {
                it.remove();
            }
        }
        return filteredMap;
    }

    /**
     * Returns an {@code EventSource} for the specified object. If the object is
     * an {@code EventSource}, it is returned. Otherwise, a dummy event source
     * is returned.
     *
     * @param obj the object in question
     * @return an {@code EventSource} for this object
     */
    private static EventSource fetchEventSource(Object obj)
    {
        return (obj instanceof EventSource) ? (EventSource) obj
                : DUMMY_EVENT_SOURCE;
    }
}
