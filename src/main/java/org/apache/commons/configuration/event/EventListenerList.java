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
package org.apache.commons.configuration.event;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * A class for managing event listeners for an event source.
 * </p>
 * <p>
 * This class allows registering an arbitrary number of event listeners for
 * specific event types. Event types are specified using the {@link EventType}
 * class. Due to the type parameters in method signatures, it is guaranteed that
 * registered listeners are compatible with the event types they are interested
 * in.
 * </p>
 * <p>
 * There are also methods for firing events. Here all registered listeners are
 * determined - based on the event type specified at registration time - which
 * should receive the event to be fired. So basically, the event type at
 * listener registration serves as a filter criterion. Because of the
 * hierarchical nature of event types it can be determined in a fine-grained way
 * which events are propagated to which listeners. It is also possible to
 * register a listener multiple times for different event types.
 * </p>
 * <p>
 * Implementation note: This class is thread-safe.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class EventListenerList
{
    /** A list with the listeners added to this object. */
    private final List<EventListenerRegistrationData<?>> listeners;

    /**
     * Creates a new instance of {@code EventListenerList}.
     */
    public EventListenerList()
    {
        listeners =
                new CopyOnWriteArrayList<EventListenerRegistrationData<?>>();
    }

    /**
     * Adds an event listener for the specified event type. This listener is
     * notified about events of this type and all its sub types.
     *
     * @param type the event type (must not be <b>null</b>)
     * @param listener the listener to be registered (must not be <b>null</b>)
     * @param <T> the type of events processed by this listener
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public <T extends Event> void addEventListener(EventType<T> type,
            EventListener<? super T> listener)
    {
        listeners.add(new EventListenerRegistrationData<T>(type, listener));
    }

    /**
     * Adds the specified listener registration data object to the internal list
     * of event listeners. This is an alternative registration method; the event
     * type and the listener are passed as a single data object.
     *
     * @param regData the registration data object (must not be <b>null</b>)
     * @param <T> the type of events processed by this listener
     * @throws IllegalArgumentException if the registration data object is
     *         <b>null</b>
     */
    public <T extends Event> void addEventListener(
            EventListenerRegistrationData<T> regData)
    {
        if (regData == null)
        {
            throw new IllegalArgumentException(
                    "EventListenerRegistrationData must not be null!");
        }
        listeners.add(regData);
    }

    /**
     * Removes the event listener registration for the given event type and
     * listener. An event listener instance may be registered multiple times for
     * different event types. Therefore, when removing a listener the event type
     * of the registration in question has to be specified. The return value
     * indicates whether a registration was removed. A value of <b>false</b>
     * means that no such combination of event type and listener was found.
     *
     * @param eventType the event type
     * @param listener the event listener to be removed
     * @return a flag whether a listener registration was removed
     */
    public <T extends Event> boolean removeEventListener(
            EventType<T> eventType, EventListener<? super T> listener)
    {
        return !(listener == null || eventType == null)
                && removeEventListener(new EventListenerRegistrationData<T>(
                        eventType, listener));
    }

    /**
     * Removes the event listener registration defined by the passed in data
     * object. This is an alternative method for removing a listener which
     * expects the event type and the listener in a single data object.
     *
     * @param regData the registration data object
     * @param <T> the type of events processed by this listener
     * @return a flag whether a listener registration was removed
     * @see #removeEventListener(EventType, EventListener)
     */
    public <T extends Event> boolean removeEventListener(
            EventListenerRegistrationData<T> regData)
    {
        return listeners.remove(regData);
    }

    /**
     * Fires an event to all registered listeners matching the event type.
     *
     * @param event the event to be fired (must not be <b>null</b>)
     * @throws IllegalArgumentException if the event is <b>null</b>
     */
    public void fire(Event event)
    {
        if (event == null)
        {
            throw new IllegalArgumentException(
                    "Event to be fired must not be null!");
        }
        Set<EventType<?>> matchingTypes =
                fetchSuperEventTypes(event.getEventType());

        for (EventListenerRegistrationData<?> listenerData : listeners)
        {
            if (matchingTypes.contains(listenerData.getEventType()))
            {
                callListener(listenerData, event);
            }
        }
    }

    /**
     * Helper method for calling an event listener from a listener registration
     * data. We have to operate on raw types to make this code compile. However,
     * this is safe because of the way the listeners have been registered and
     * associated with event types - so it is ensured that the event is
     * compatible with the listener.
     *
     * @param listenerData the event listener data
     * @param event the event to be fired
     */
    @SuppressWarnings("unchecked, rawtypes")
    private static void callListener(
            EventListenerRegistrationData<?> listenerData, Event event)
    {
        EventListener listener = listenerData.getListener();
        listener.onEvent(event);
    }

    /**
     * Obtains a set of all super event types for the specified type (including
     * the type itself). If an event listener was registered for one of these
     * types, it can handle an event of the specified type.
     *
     * @param eventType the event type in question
     * @return the set with all super event types
     */
    private static Set<EventType<?>> fetchSuperEventTypes(EventType<?> eventType)
    {
        Set<EventType<?>> types = new HashSet<EventType<?>>();
        EventType<?> currentType = eventType;
        while (currentType != null)
        {
            types.add(currentType);
            currentType = currentType.getSuperType();
        }
        return types;
    }
}
