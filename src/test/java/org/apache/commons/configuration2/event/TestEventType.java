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

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code EventType}.
 *
 */
public class TestEventType
{
    /** The event type to be tested. */
    private EventType<Event> eventType;

    @Before
    public void setUp() throws Exception
    {
        eventType = new EventType<>(null, "TEST");
    }

    /**
     * Tests the string representation.
     */
    @Test
    public void testToString()
    {
        final String s = eventType.toString();
        assertEquals("Wrong string", "EventType [ TEST ]", s);
    }
}
