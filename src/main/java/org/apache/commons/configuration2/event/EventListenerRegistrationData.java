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

/**
 * <p>
 * A data class holding information about an event listener registration.
 * </p>
 * <p>
 * An instance of this class stores all information required to determine
 * whether a specific event listener is to be invoked for a given event. The
 * class is used internally by {@link EventListenerList}, but is also useful in
 * general when information about event listeners is to be stored.
 * </p>
 * <p>
 * Implementation note: Instances of this class are immutable and can safely be
 * shared between multiple threads or components.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of events processed by the listener
 */
public final class EventListenerRegistrationData<T extends Event>
{
    /** Constant for the factor used by the calculation of the hash code. */
    private static final int HASH_FACTOR = 31;

    /** The event type. */
    private final EventType<T> eventType;

    /** The event listener. */
    private final EventListener<? super T> listener;

    /**
     * Creates a new instance of {@code EventListenerRegistrationData}.
     *
     * @param type the event type (must not be <b>null</b>)
     * @param lstnr the event listener (must not be <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public EventListenerRegistrationData(final EventType<T> type,
            final EventListener<? super T> lstnr)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Event type must not be null!");
        }
        if (lstnr == null)
        {
            throw new IllegalArgumentException(
                    "Listener to be registered must not be null!");
        }

        eventType = type;
        listener = lstnr;
    }

    /**
     * Returns the event type for this listener registration.
     *
     * @return the event type
     */
    public EventType<T> getEventType()
    {
        return eventType;
    }

    /**
     * Returns the listener this registration is about.
     *
     * @return the event listener
     */
    public EventListener<? super T> getListener()
    {
        return listener;
    }

    @Override
    public int hashCode()
    {
        int result = eventType.hashCode();
        result = HASH_FACTOR * result + listener.hashCode();
        return result;
    }

    /**
     * Compares this object with another one. Two instances of
     * {@code EventListenerRegistrationData} are considered equal if they
     * reference the same listener and event type.
     *
     * @param obj the object to be compared to
     * @return a flag whether these objects are equal
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof EventListenerRegistrationData))
        {
            return false;
        }

        final EventListenerRegistrationData<?> c =
                (EventListenerRegistrationData<?>) obj;
        return getListener() == c.getListener()
                && getEventType().equals(c.getEventType());
    }
}
