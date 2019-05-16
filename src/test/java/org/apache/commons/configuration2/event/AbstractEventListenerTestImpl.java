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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

/**
 * A base class for different types of event listeners which can be used in unit
 * tests. This class provides functionality for testing the received events.
 *
 * @param <T> the type of events supported by this listener
 */
public abstract class AbstractEventListenerTestImpl<T extends Event> implements
        EventListener<T>
{
    /** The expected event source. */
    private final Object expectedSource;

    /** Stores the received events. */
    private final List<T> events;

    /**
     * Creates a new instance of {@code AbstractEventListenerTestImpl} and sets
     * the expected event source.
     *
     * @param source the event source (<b>null</b> if the source need not to be
     *        checked)
     */
    protected AbstractEventListenerTestImpl(final Object source)
    {
        expectedSource = source;
        events = new LinkedList<>();
    }

    @Override
    public void onEvent(final T event)
    {
        events.add(event);
    }

    /**
     * Checks if at least {@code minEvents} events have been received.
     *
     * @param minEvents the minimum number of expected events
     */
    public void checkEventCount(final int minEvents)
    {
        assertTrue("Too view events received", events.size() >= minEvents);
    }

    /**
     * Returns the next received event and checks for the expected type.
     *
     * @param expectedType the expected type of the event
     * @return the event object
     */
    public T nextEvent(final EventType<?> expectedType)
    {
        assertFalse("Too few events received", events.isEmpty());
        final T e = events.remove(0);
        if (expectedSource != null)
        {
            assertEquals("Wrong event source", expectedSource, e.getSource());
        }
        assertEquals("Wrong event type", expectedType, e.getEventType());
        return e;
    }

    /**
     * Skips to the last received event and checks that no events of the given
     * type have been received. This method is used by checks for detail events
     * to ignore the detail events.
     *
     * @param type the event type
     */
    public void skipToLast(final EventType<?> type)
    {
        while (events.size() > 1)
        {
            final T e = events.remove(0);
            assertTrue("Found end event in details", type != e.getEventType());
        }
    }

    /**
     * Checks if all events has been processed.
     */
    public void done()
    {
        assertTrue("Too many events received", events.isEmpty());
    }
}
