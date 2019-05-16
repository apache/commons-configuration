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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerList;
import org.apache.commons.configuration2.event.EventListenerRegistrationData;
import org.apache.commons.configuration2.event.EventType;

/**
 * <p>
 * A specialized parameters implementation for {@link BasicConfigurationBuilder}
 * which allows for a convenient event listener initialization.
 * </p>
 * <p>
 * This class offers a fluent interface for registering event listeners. A fully
 * initialized instance can be passed to the
 * {@link BasicConfigurationBuilder#configure(BuilderParameters...)} method. All
 * event listeners which have been registered at the instance are then copied
 * over to the configuration builder.
 * </p>
 * <p>
 * The code fragment below shows a typical usage scenario:
 * </p>
 *
 * <pre>
 * BasicConfigurationBuilder&lt;Configuration&gt; builder =
 *         new BasicConfigurationBuilder&lt;Configuration&gt;(
 *                 PropertiesConfiguration.class)
 *                 .configure(new EventListenerParameters().addEventListener(
 *                         ConfigurationEvent.ANY, myListener));
 * </pre>
 *
 * <p>
 * In order to support a configuration builder's {@code configure()} method,
 * this class implements the {@code BuilderParameters} interface. However, this
 * is just a dummy implementation; no parameters are propagated to the builder.
 * </p>
 *
 * @since 2.0
 */
public class EventListenerParameters implements BuilderParameters,
        EventListenerProvider
{
    /** Stores the event listener registrations added to this object. */
    private final EventListenerList eventListeners;

    /**
     * Creates a new instance of {@code EventListenerParameters}.
     */
    public EventListenerParameters()
    {
        eventListeners = new EventListenerList();
    }

    /**
     * Adds an event listener of the specified event type to this object.
     *
     * @param eventType the event type object
     * @param listener the event listener
     * @param <T> the event type
     * @return a reference to this object for method chaining
     */
    public <T extends Event> EventListenerParameters addEventListener(
            final EventType<T> eventType, final EventListener<? super T> listener)
    {
        eventListeners.addEventListener(eventType, listener);
        return this;
    }

    /**
     * Adds the specified {@code EventListenerRegistrationData} instance to this
     * object.
     *
     * @param registrationData the registration object to be added
     * @param <T> the event type of the contained event listener
     * @return a reference to this object for method chaining
     */
    public <T extends Event> EventListenerParameters addEventListener(
            final EventListenerRegistrationData<T> registrationData)
    {
        eventListeners.addEventListener(registrationData);
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns an empty map.
     */
    @Override
    public Map<String, Object> getParameters()
    {
        return Collections.emptyMap();
    }

    @Override
    public EventListenerList getListeners()
    {
        return eventListeners;
    }
}
