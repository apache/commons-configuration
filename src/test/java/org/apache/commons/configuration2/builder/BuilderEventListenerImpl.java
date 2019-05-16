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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;

/**
 * A test implementation of an event listener for configuration builders. This
 * class is used by some unit tests. It collects the events received by the
 * listener and provides some methods for querying them.
 *
 */
public class BuilderEventListenerImpl implements
        EventListener<ConfigurationBuilderEvent>
{
    /** A list with the received events. */
    private final List<ConfigurationBuilderEvent> events =
            new LinkedList<>();

    /** An iterator for inspecting the received events. */
    private Iterator<ConfigurationBuilderEvent> iterator;

    /**
     * {@inheritDoc} This implementation just records the event.
     */
    @Override
    public void onEvent(final ConfigurationBuilderEvent event)
    {
        events.add(event);
    }

    /**
     * Checks whether the next received event is of the specified event type and
     * returns it. Causes the test to fail if there are no more events or the
     * next event is of a different event type.
     *
     * @param eventType the expected event type
     * @param <T> the type of the received event
     * @return the next received event
     */
    public <T extends ConfigurationBuilderEvent> T nextEvent(
            final EventType<T> eventType)
    {
        final Iterator<ConfigurationBuilderEvent> it = initIterator();
        assertTrue("Too few events received", it.hasNext());
        final ConfigurationBuilderEvent nextEvent = it.next();
        assertEquals("Wrong event type", eventType, nextEvent.getEventType());
        // Safe cast because of the comparison of the event type
        @SuppressWarnings("unchecked")
        final
        T resultEvent = (T) nextEvent;
        return resultEvent;
    }

    /**
     * Checks that no further events have been received by this listener.
     */
    public void assertNoMoreEvents()
    {
        assertFalse("Too many events", initIterator().hasNext());
    }

    /**
     * Ensures that the iterator for received events has been initialized.
     *
     * @return the iterator to be used
     */
    private Iterator<ConfigurationBuilderEvent> initIterator()
    {
        if (iterator == null)
        {
            iterator = events.iterator();
        }
        return iterator;
    }
}
