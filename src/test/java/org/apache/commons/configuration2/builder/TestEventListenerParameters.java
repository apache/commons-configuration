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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListenerRegistrationData;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.junit.Test;

/**
 * Test class for {@code EventListenerParameters}.
 *
 */
public class TestEventListenerParameters
{
    /**
     * Tests the map with parameters.
     */
    @Test
    public void testGetParameters()
    {
        final EventListenerParameters parameters = new EventListenerParameters();
        assertTrue("Got parameters", parameters.getParameters().isEmpty());
    }

    /**
     * Tests that the list of event listeners is empty for a newly created
     * instance.
     */
    @Test
    public void testRegistrationsAfterCreation()
    {
        final EventListenerParameters parameters = new EventListenerParameters();
        assertTrue("Got registrations", parameters.getListeners()
                .getRegistrations().isEmpty());
    }

    /**
     * Tests whether an event listener with its type can be added.
     */
    @Test
    public void testAddEventListener()
    {
        final EventListenerTestImpl listener = new EventListenerTestImpl(null);
        final EventListenerParameters parameters = new EventListenerParameters();
        assertSame("Wrong result", parameters, parameters.addEventListener(
                ConfigurationEvent.ADD_PROPERTY, listener));
        assertEquals("Wrong number of registrations", 1, parameters
                .getListeners().getRegistrations().size());
        final EventListenerRegistrationData<?> reg =
                parameters.getListeners().getRegistrations().get(0);
        assertEquals("Wrong event type", ConfigurationEvent.ADD_PROPERTY,
                reg.getEventType());
        assertEquals("Wrong listener", listener, reg.getListener());
    }

    /**
     * Tests whether an event listener registration can be added.
     */
    @Test
    public void testAddEventListenerRegistration()
    {
        final EventListenerRegistrationData<ConfigurationEvent> reg =
                new EventListenerRegistrationData<>(
                        ConfigurationEvent.SET_PROPERTY,
                        new EventListenerTestImpl(null));
        final EventListenerParameters parameters = new EventListenerParameters();
        assertSame("Wrong result", parameters, parameters.addEventListener(reg));
        assertEquals("Wrong number of registrations", 1, parameters
                .getListeners().getRegistrations().size());
        assertEquals("Wrong registration", reg, parameters.getListeners()
                .getRegistrations().get(0));
    }
}
