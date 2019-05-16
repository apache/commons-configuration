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
package org.apache.commons.configuration2.event;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A base class for objects that can generate configuration events.
 * </p>
 * <p>
 * This class implements functionality for managing a set of event listeners
 * that can be notified when an event occurs. It can be extended by
 * configuration classes that support the event mechanism. In this case these
 * classes only need to call the {@code fireEvent()} method when an event is to
 * be delivered to the registered listeners.
 * </p>
 * <p>
 * Adding and removing event listeners can happen concurrently to manipulations
 * on a configuration that cause events. The operations are synchronized.
 * </p>
 * <p>
 * With the {@code detailEvents} property the number of detail events can be
 * controlled. Some methods in configuration classes are implemented in a way
 * that they call other methods that can generate their own events. One example
 * is the {@code setProperty()} method that can be implemented as a combination
 * of {@code clearProperty()} and {@code addProperty()}. With
 * {@code detailEvents} set to <b>true</b>, all involved methods will generate
 * events (i.e. listeners will receive property set events, property clear
 * events, and property add events). If this mode is turned off (which is the
 * default), detail events are suppressed, so only property set events will be
 * received. Note that the number of received detail events may differ for
 * different configuration implementations.
 * {@link org.apache.commons.configuration2.BaseHierarchicalConfiguration
 * BaseHierarchicalConfiguration} for instance has a custom implementation of
 * {@code setProperty()}, which does not generate any detail events.
 * </p>
 * <p>
 * In addition to &quot;normal&quot; events, error events are supported. Such
 * events signal an internal problem that occurred during access of properties.
 * They are handled via the regular {@link EventListener} interface, but there
 * are special event types defined by {@link ConfigurationErrorEvent}. The
 * {@code fireError()} method can be used by derived classes to send
 * notifications about errors to registered observers.
 * </p>
 *
 * @since 1.3
 */
public class BaseEventSource implements EventSource
{
    /** The list for managing registered event listeners. */
    private EventListenerList eventListeners;

    /** A lock object for guarding access to the detail events counter. */
    private final Object lockDetailEventsCount = new Object();

    /** A counter for the detail events. */
    private int detailEvents;

    /**
     * Creates a new instance of {@code BaseEventSource}.
     */
    public BaseEventSource()
    {
        initListeners();
    }

    /**
     * Returns a collection with all event listeners of the specified event type
     * that are currently registered at this object.
     *
     * @param eventType the event type object
     * @param <T> the event type
     * @return a collection with the event listeners of the specified event type
     *         (this collection is a snapshot of the currently registered
     *         listeners; it cannot be manipulated)
     */
    public <T extends Event> Collection<EventListener<? super T>> getEventListeners(
            final EventType<T> eventType)
    {
        final List<EventListener<? super T>> result =
                new LinkedList<>();
        for (final EventListener<? super T> l : eventListeners
                .getEventListeners(eventType))
        {
            result.add(l);
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
     * Returns a list with all {@code EventListenerRegistrationData} objects
     * currently contained for this event source. This method allows access to
     * all registered event listeners, independent on their type.
     *
     * @return a list with information about all registered event listeners
     */
    public List<EventListenerRegistrationData<?>> getEventListenerRegistrations()
    {
        return eventListeners.getRegistrations();
    }

    /**
     * Returns a flag whether detail events are enabled.
     *
     * @return a flag if detail events are generated
     */
    public boolean isDetailEvents()
    {
        return checkDetailEvents(0);
    }

    /**
     * Determines whether detail events should be generated. If enabled, some
     * methods can generate multiple update events. Note that this method
     * records the number of calls, i.e. if for instance
     * {@code setDetailEvents(false)} was called three times, you will
     * have to invoke the method as often to enable the details.
     *
     * @param enable a flag if detail events should be enabled or disabled
     */
    public void setDetailEvents(final boolean enable)
    {
        synchronized (lockDetailEventsCount)
        {
            if (enable)
            {
                detailEvents++;
            }
            else
            {
                detailEvents--;
            }
        }
    }

    @Override
    public <T extends Event> void addEventListener(final EventType<T> eventType,
            final EventListener<? super T> listener)
    {
        eventListeners.addEventListener(eventType, listener);
    }

    @Override
    public <T extends Event> boolean removeEventListener(
            final EventType<T> eventType, final EventListener<? super T> listener)
    {
        return eventListeners.removeEventListener(eventType, listener);
    }

    /**
     * Removes all registered event listeners.
     */
    public void clearEventListeners()
    {
        eventListeners.clear();
    }

    /**
     * Removes all registered error listeners.
     *
     * @since 1.4
     */
    public void clearErrorListeners()
    {
        for (final EventListenerRegistrationData<? extends ConfigurationErrorEvent> reg : eventListeners
                .getRegistrationsForSuperType(ConfigurationErrorEvent.ANY))
        {
            eventListeners.removeEventListener(reg);
        }
    }

    /**
     * Copies all event listener registrations maintained by this object to the
     * specified {@code BaseEventSource} object.
     *
     * @param source the target source for the copy operation (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the target source is <b>null</b>
     * @since 2.0
     */
    public void copyEventListeners(final BaseEventSource source)
    {
        if (source == null)
        {
            throw new IllegalArgumentException(
                    "Target event source must not be null!");
        }
        source.eventListeners.addAll(eventListeners);
    }

    /**
     * Creates an event object and delivers it to all registered event
     * listeners. The method checks first if sending an event is allowed (making
     * use of the {@code detailEvents} property), and if listeners are
     * registered.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     * @param <T> the type of the event to be fired
     */
    protected <T extends ConfigurationEvent> void fireEvent(final EventType<T> type,
            final String propName, final Object propValue, final boolean before)
    {
        if (checkDetailEvents(-1))
        {
            final EventListenerList.EventListenerIterator<T> it =
                    eventListeners.getEventListenerIterator(type);
            if (it.hasNext())
            {
                final ConfigurationEvent event =
                        createEvent(type, propName, propValue, before);
                while (it.hasNext())
                {
                    it.invokeNext(event);
                }
            }
        }
    }

    /**
     * Creates a {@code ConfigurationEvent} object based on the passed in
     * parameters. This method is called by {@code fireEvent()} if it decides
     * that an event needs to be generated.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     * @param <T> the type of the event to be created
     * @return the newly created event object
     */
    protected <T extends ConfigurationEvent> ConfigurationEvent createEvent(
            final EventType<T> type, final String propName, final Object propValue, final boolean before)
    {
        return new ConfigurationEvent(this, type, propName, propValue, before);
    }

    /**
     * Creates an error event object and delivers it to all registered error
     * listeners of a matching type.
     *
     * @param eventType the event's type
     * @param operationType the type of the failed operation
     * @param propertyName the name of the affected property (can be
     *        <b>null</b>)
     * @param propertyValue the value of the affected property (can be
     *        <b>null</b>)
     * @param cause the {@code Throwable} object that caused this error event
     * @param <T> the event type
     */
    public <T extends ConfigurationErrorEvent> void fireError(
            final EventType<T> eventType, final EventType<?> operationType,
            final String propertyName, final Object propertyValue, final Throwable cause)
    {
        final EventListenerList.EventListenerIterator<T> iterator =
                eventListeners.getEventListenerIterator(eventType);
        if (iterator.hasNext())
        {
            final ConfigurationErrorEvent event =
                    createErrorEvent(eventType, operationType, propertyName,
                            propertyValue, cause);
            while (iterator.hasNext())
            {
                iterator.invokeNext(event);
            }
        }
    }

    /**
     * Creates a {@code ConfigurationErrorEvent} object based on the passed in
     * parameters. This is called by {@code fireError()} if it decides that an
     * event needs to be generated.
     *
     * @param type the event's type
     * @param opType the operation type related to this error
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param ex the {@code Throwable} object that caused this error event
     * @return the event object
     */
    protected ConfigurationErrorEvent createErrorEvent(
            final EventType<? extends ConfigurationErrorEvent> type,
            final EventType<?> opType, final String propName, final Object propValue, final Throwable ex)
    {
        return new ConfigurationErrorEvent(this, type, opType, propName,
                propValue, ex);
    }

    /**
     * Overrides the {@code clone()} method to correctly handle so far
     * registered event listeners. This implementation ensures that the clone
     * will have empty event listener lists, i.e. the listeners registered at an
     * {@code BaseEventSource} object will not be copied.
     *
     * @return the cloned object
     * @throws CloneNotSupportedException if cloning is not allowed
     * @since 1.4
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        final BaseEventSource copy = (BaseEventSource) super.clone();
        copy.initListeners();
        return copy;
    }

    /**
     * Initializes the collections for storing registered event listeners.
     */
    private void initListeners()
    {
        eventListeners = new EventListenerList();
    }

    /**
     * Helper method for checking the current counter for detail events. This
     * method checks whether the counter is greater than the passed in limit.
     *
     * @param limit the limit to be compared to
     * @return <b>true</b> if the counter is greater than the limit,
     *         <b>false</b> otherwise
     */
    private boolean checkDetailEvents(final int limit)
    {
        synchronized (lockDetailEventsCount)
        {
            return detailEvents > limit;
        }
    }
}
