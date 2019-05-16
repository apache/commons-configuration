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

import org.junit.Test;

/**
 * Test class for {@code Event}.
 *
 */
public class TestEvent
{
    /**
     * Tries to create an instance without a source.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoSource()
    {
        new Event(null, Event.ANY);
    }

    /**
     * Tries to create an instance without a type.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoType()
    {
        new Event(this, null);
    }

    /**
     * Tests the string representation.
     */
    @Test
    public void testToString()
    {
        final Event event = new Event(this, Event.ANY);
        final String s = event.toString();
        assertEquals("Wrong string representation", "Event [ source=" + this
                + " eventType=" + Event.ANY + " ]", s);
    }
}
