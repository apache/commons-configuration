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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
                new CopyOnWriteArrayList<>();
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
    public <T extends Event> void addEventListener(final EventType<T> type,
            final EventListener<? super T> listener)
    {
        listeners.add(new EventListenerRegistrationData<>(type, listener));
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
            final EventListenerRegistrationData<T> regData)
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
     * @param <T> the type of events processed by this listener
     * @return a flag whether a listener registration was removed
     */
    public <T extends Event> boolean removeEventListener(
            final EventType<T> eventType, final EventListener<? super T> listener)
    {
        return !(listener == null || eventType == null)
                && removeEventListener(new EventListenerRegistrationData<>(
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
            final EventListenerRegistrationData<T> regData)
    {
        return listeners.remove(regData);
    }

    /**
     * Fires an event to all registered listeners matching the event type.
     *
     * @param event the event to be fired (must not be <b>null</b>)
     * @throws IllegalArgumentException if the event is <b>null</b>
     */
    public void fire(final Event event)
    {
        if (event == null)
        {
            throw new IllegalArgumentException(
                    "Event to be fired must not be null!");
        }

        for (final EventListenerIterator<? extends Event> iterator =
                getEventListenerIterator(event.getEventType()); iterator
                .hasNext();)
        {
            iterator.invokeNextListenerUnchecked(event);
        }
    }

    /**
     * Returns an {@code Iterable} allowing access to all event listeners stored
     * in this list which are compatible with the specified event type.
     *
     * @param eventType the event type object
     * @param <T> the event type
     * @return an {@code Iterable} with the selected event listeners
     */
    public <T extends Event> Iterable<EventListener<? super T>> getEventListeners(
            final EventType<T> eventType)
    {
        return new Iterable<EventListener<? super T>>()
        {
            @Override
            public Iterator<EventListener<? super T>> iterator()
            {
                return getEventListenerIterator(eventType);
            }
        };
    }

    /**
     * Returns a specialized iterator for obtaining all event listeners stored
     * in this list which are compatible with the specified event type.
     *
     * @param eventType the event type object
     * @param <T> the event type
     * @return an {@code Iterator} with the selected event listeners
     */
    public <T extends Event> EventListenerIterator<T> getEventListenerIterator(
            final EventType<T> eventType)
    {
        return new EventListenerIterator<>(listeners.iterator(), eventType);
    }

    /**
     * Returns an (unmodifiable) list with registration information about all
     * event listeners registered at this object.
     *
     * @return a list with event listener registration information
     */
    public List<EventListenerRegistrationData<?>> getRegistrations()
    {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Returns a list with {@code EventListenerRegistrationData} objects for all
     * event listener registrations of the specified event type or an event type
     * having this type as super type (directly or indirectly). Note that this
     * is the opposite direction than querying event types for firing events: in
     * this case event listener registrations are searched which are super event
     * types from a given type. This method in contrast returns event listener
     * registrations for listeners that extend a given super type.
     *
     * @param eventType the event type object
     * @param <T> the event type
     * @return a list with the matching event listener registration objects
     */
    public <T extends Event> List<EventListenerRegistrationData<? extends T>> getRegistrationsForSuperType(
            final EventType<T> eventType)
    {
        final Map<EventType<?>, Set<EventType<?>>> superTypes =
                new HashMap<>();
        final List<EventListenerRegistrationData<? extends T>> results =
                new LinkedList<>();

        for (final EventListenerRegistrationData<?> reg : listeners)
        {
            Set<EventType<?>> base = superTypes.get(reg.getEventType());
            if (base == null)
            {
                base = EventType.fetchSuperEventTypes(reg.getEventType());
                superTypes.put(reg.getEventType(), base);
            }
            if (base.contains(eventType))
            {
                @SuppressWarnings("unchecked")
                final
                // This is safe because we just did a check
                EventListenerRegistrationData<? extends T> result =
                        (EventListenerRegistrationData<? extends T>) reg;
                results.add(result);
            }
        }

        return results;
    }

    /**
     * Removes all event listeners registered at this object.
     */
    public void clear()
    {
        listeners.clear();
    }

    /**
     * Adds all event listener registrations stored in the specified
     * {@code EventListenerList} to this list.
     *
     * @param c the list to be copied (must not be <b>null</b>)
     * @throws IllegalArgumentException if the list to be copied is <b>null</b>
     */
    public void addAll(final EventListenerList c)
    {
        if (c == null)
        {
            throw new IllegalArgumentException(
                    "List to be copied must not be null!");
        }

        for (final EventListenerRegistrationData<?> regData : c.getRegistrations())
        {
            addEventListener(regData);
        }
    }

    /**
     * Helper method for calling an event listener with an event. We have to
     * operate on raw types to make this code compile. However, this is safe
     * because of the way the listeners have been registered and associated with
     * event types - so it is ensured that the event is compatible with the
     * listener.
     *
     * @param listener the event listener to be called
     * @param event the event to be fired
     */
    @SuppressWarnings("unchecked")
    private static void callListener(final EventListener<?> listener, final Event event)
    {
        @SuppressWarnings("rawtypes")
        final
        EventListener rowListener = listener;
        rowListener.onEvent(event);
    }

    /**
     * A special {@code Iterator} implementation used by the
     * {@code getEventListenerIterator()} method. This iterator returns only
     * listeners compatible with a specified event type. It has a convenience
     * method for invoking the current listener in the iteration with an event.
     *
     * @param <T> the event type
     */
    public static final class EventListenerIterator<T extends Event> implements
            Iterator<EventListener<? super T>>
    {
        /** The underlying iterator. */
        private final Iterator<EventListenerRegistrationData<?>> underlyingIterator;

        /** The base event type. */
        private final EventType<T> baseEventType;

        /** The set with accepted event types. */
        private final Set<EventType<?>> acceptedTypes;

        /** The next element in the iteration. */
        private EventListener<? super T> nextElement;

        private EventListenerIterator(
                final Iterator<EventListenerRegistrationData<?>> it, final EventType<T> base)
        {
            underlyingIterator = it;
            baseEventType = base;
            acceptedTypes = EventType.fetchSuperEventTypes(base);
            initNextElement();
        }

        @Override
        public boolean hasNext()
        {
            return nextElement != null;
        }

        @Override
        public EventListener<? super T> next()
        {
            if (nextElement == null)
            {
                throw new NoSuchElementException("No more event listeners!");
            }

            final EventListener<? super T> result = nextElement;
            initNextElement();
            return result;
        }

        /**
         * Obtains the next event listener in this iteration and invokes it with
         * the given event object.
         *
         * @param event the event object
         * @throws NoSuchElementException if iteration is at its end
         */
        public void invokeNext(final Event event)
        {
            validateEvent(event);
            invokeNextListenerUnchecked(event);
        }

        /**
         * {@inheritDoc} This implementation always throws an exception.
         * Removing elements is not supported.
         */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(
                    "Removing elements is not supported!");
        }

        /**
         * Determines the next element in the iteration.
         */
        private void initNextElement()
        {
            nextElement = null;
            while (underlyingIterator.hasNext() && nextElement == null)
            {
                final EventListenerRegistrationData<?> regData =
                        underlyingIterator.next();
                if (acceptedTypes.contains(regData.getEventType()))
                {
                    nextElement = castListener(regData);
                }
            }
        }

        /**
         * Checks whether the specified event can be passed to an event listener
         * in this iteration. This check is done via the hierarchy of event
         * types.
         *
         * @param event the event object
         * @throws IllegalArgumentException if the event is invalid
         */
        private void validateEvent(final Event event)
        {
            if (event == null
                    || !EventType.fetchSuperEventTypes(event.getEventType()).contains(
                    baseEventType))
            {
                throw new IllegalArgumentException(
                        "Event incompatible with listener iteration: " + event);
            }
        }

        /**
         * Invokes the next event listener in the iteration without doing a
         * validity check on the event. This method is called internally to
         * avoid duplicate event checks.
         *
         * @param event the event object
         */
        private void invokeNextListenerUnchecked(final Event event)
        {
            final EventListener<? super T> listener = next();
            callListener(listener, event);
        }

        /**
         * Extracts the listener from the given data object and performs a cast
         * to the target type. This is safe because it has been checked before
         * that the type is compatible.
         *
         * @param regData the data object
         * @return the extracted listener
         */
        @SuppressWarnings("unchecked")
        private EventListener<? super T> castListener(
                final EventListenerRegistrationData<?> regData)
        {
            @SuppressWarnings("rawtypes")
            final
            EventListener listener = regData.getListener();
            return listener;
        }
    }
}
