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

import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * A test class which tests whether the types of basic configuration events are
 * correctly defined.
 *
 * @version $Id$
 */
public class TestConfigurationEventTypes
{
    /**
     * Tests the base event type for configuration events.
     */
    @Test
    public void testConfigurationEventType()
    {
        assertSame("Wrong super type", Event.ANY,
                ConfigurationEvent.ANY.getSuperType());
    }

    /**
     * Helper method for checking the relevant properties of a given event type
     * representing a configuration update event.
     *
     * @param eventType the event type to check
     */
    private void checkUpdateEvent(EventType<ConfigurationEvent> eventType)
    {
        assertSame("Wrong super type for " + eventType, ConfigurationEvent.ANY,
                eventType.getSuperType());
    }

    /**
     * Tests the event type for adding a property.
     */
    @Test
    public void testAddPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.ADD_PROPERTY);
    }

    /**
     * Tests the event type for setting a property.
     */
    @Test
    public void testSetPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.SET_PROPERTY);
    }

    /**
     * Tests the event type for clearing a property.
     */
    @Test
    public void testClearPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.CLEAR_PROPERTY);
    }

    /**
     * Tests the event type for clearing a whole configuration.
     */
    @Test
    public void testClearEventType()
    {
        checkUpdateEvent(ConfigurationEvent.CLEAR);
    }
}
