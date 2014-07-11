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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

/**
 * A test event listener class that can be used for testing whether
 * event sources generated correct events.
 *
 * @version $Id$
 */
public class EventListenerTestImpl implements EventListener<ConfigurationEvent>
{
    /** The expected event source. */
    private final Object expectedSource;

    /** Stores the received events. */
    private final List<ConfigurationEvent> events;

    /**
     * Creates a new instance of {@code EventListenerTestImpl} and sets
     * the expected event source.
     *
     * @param source the event source (<b>null</b> if the source need not to be
     *        checked)
     */
    public EventListenerTestImpl(Object source)
    {
        expectedSource = source;
        events = new LinkedList<ConfigurationEvent>();
    }

    @Override
    public void onEvent(ConfigurationEvent event) {
        events.add(event);
    }

    /**
     * Checks if at least {@code minEvents} events have been received.
     *
     * @param minEvents the minimum number of expected events
     */
    public void checkEventCount(int minEvents)
    {
        assertTrue("Too view events received", events.size() >= minEvents);
    }

    /**
     * Checks an expected event.
     *
     * @param type the event type
     * @param propName the expected property name
     * @param propValue the expected property value
     * @param before the expected before flag
     */
    public void checkEvent(EventType<?> type, String propName, Object propValue,
            boolean before)
    {
        ConfigurationEvent e = nextEvent(type);
        assertEquals("Wrong property name", propName, e.getPropertyName());
        assertEquals("Wrong property value", propValue, e.getPropertyValue());
        assertEquals("Wrong before flag", before, e.isBeforeUpdate());
    }

    /**
     * Returns the next received event and checks for the expected type. This
     * method can be used instead of {@code checkEvent()} for comparing
     * complex event values.
     *
     * @param expectedType the expected type of the event
     * @return the event object
     */
    public ConfigurationEvent nextEvent(EventType<?> expectedType)
    {
        assertFalse("Too few events received", events.isEmpty());
        ConfigurationEvent e = events.remove(0);
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
    public void skipToLast(EventType<?> type)
    {
        while (events.size() > 1)
        {
            ConfigurationEvent e = events.remove(0);
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
