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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test event listener class that can be used for testing whether event sources generated correct events.
 *
 */
public class EventListenerTestImpl extends AbstractEventListenerTestImpl<ConfigurationEvent> {
    /**
     * Creates a new instance of {@code EventListenerTestImpl} and sets the expected event source.
     *
     * @param source the event source (<b>null</b> if the source need not to be checked)
     */
    public EventListenerTestImpl(final Object source) {
        super(source);
    }

    /**
     * Checks an expected event.
     *
     * @param type the event type
     * @param propName the expected property name
     * @param propValue the expected property value
     * @param before the expected before flag
     */
    public void checkEvent(final EventType<?> type, final String propName, final Object propValue, final boolean before) {
        final ConfigurationEvent e = nextEvent(type);
        assertEquals(propName, e.getPropertyName());
        assertEquals(propValue, e.getPropertyValue());
        assertEquals(before, e.isBeforeUpdate());
    }
}
