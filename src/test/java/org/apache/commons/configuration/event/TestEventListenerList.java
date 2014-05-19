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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code EventListenerList.}
 *
 * @since 2.0
 */
public class TestEventListenerList
{
    /** Constant for a test event message. */
    private static final String MESSAGE = "TestEventMessage";

    /** Type for the base event. */
    private static EventType<EventBase> typeBase;

    /** Type for sub event 1. */
    private static EventType<EventSub1> typeSub1;

    /** Type for sub event 2. */
    private static EventType<EventSub2> typeSub2;

    /** The list to be tested. */
    private EventListenerList list;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        typeBase = new EventType<EventBase>(Event.ANY, "BASE");
        typeSub1 = new EventType<EventSub1>(typeBase, "SUB1");
        typeSub2 = new EventType<EventSub2>(typeBase, "SUB2");
    }

    @Before
    public void setUp() throws Exception
    {
        list = new EventListenerList();
    }

    /**
     * Tries to register a listener for a null event type.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterEventTypeNull()
    {
        list.addEventListener(null, new ListenerTestImpl());
    }

    /**
     * Tests that null event listeners cannot be registered.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterListenerNull()
    {
        list.addEventListener(typeBase, null);
    }

    /**
     * Tests that a null event is rejected by fire().
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFireNullEvent()
    {
        list.fire(null);
    }

    /**
     * Tests whether events matching the registration type are delivered.
     */
    @Test
    public void testReceiveEventOfExactType()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tests whether multiple event listeners can be registered.
     */
    @Test
    public void testReceiveEventMultipleListeners()
    {
        ListenerTestImpl listener1 = new ListenerTestImpl();
        ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener1);
        list.addEventListener(typeSub1, listener2);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener1.assertEvent(this, typeSub1, MESSAGE);
        listener2.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tests whether the event type is taken into account when calling
     * listeners.
     */
    @Test
    public void testReceiveEventDifferentType()
    {
        ListenerTestImpl listener1 = new ListenerTestImpl();
        ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener1);
        list.addEventListener(typeSub2, listener2);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener1.assertEvent(this, typeSub1, MESSAGE);
        listener2.assertNoEvent();
    }

    /**
     * Tests that events of a base type do not cause a listener to be invoked.
     */
    @Test
    public void testSuppressEventOfSuperType()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        list.fire(new EventBase(this, typeBase, MESSAGE));
        listener.assertNoEvent();
    }

    /**
     * Tests that events of a derived type are delivered to listeners registered
     * for a base type.
     */
    @Test
    public void testReceiveEventSubType()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeBase, listener);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tests whether an event listener can be registered via a registration data
     * object.
     */
    @Test
    public void testListenerRegistrationWithListenerData()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        EventListenerRegistrationData<EventSub1> regData =
                new EventListenerRegistrationData<EventSub1>(typeSub1, listener);
        list.addEventListener(regData);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tries to register a listener with a null registration data object.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testListenerRegistrationWithNullListenerData()
    {
        list.addEventListener(null);
    }

    /**
     * Tests removeEventListener() for a non-existing event listener.
     */
    @Test
    public void testRemoveEventListenerNonExistingListener()
    {
        list.addEventListener(typeBase, new ListenerTestImpl());
        assertFalse("Wrong result",
                list.removeEventListener(typeBase, new ListenerTestImpl()));
    }

    /**
     * Tests removeEventListener() if another event type is specified for an
     * existing listener.
     */
    @Test
    public void testRemoveEventListenerNonExistingEventType()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        assertFalse("Wrong result",
                list.removeEventListener(typeBase, listener));
    }

    /**
     * Tests whether an event listener can be removed.
     */
    @Test
    public void testRemoveEventListenerExisting()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        assertTrue("Wrong result", list.removeEventListener(typeSub1, listener));
        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertNoEvent();
    }

    /**
     * Tests that removeEventListener() can handle a null registration object.
     */
    @Test
    public void testRemoveEventListenerNullRegistration()
    {
        assertFalse("Wrong result", list.removeEventListener(null));
    }

    /**
     * Tests that removeEventListener() can handle a null listener.
     */
    @Test
    public void testRemoveEventListenerNullListener()
    {
        assertFalse("Wrong result", list.removeEventListener(typeBase, null));
    }

    /**
     * Tests that removeEventListener() can handle a null event type.
     */
    @Test
    public void testRemoveEventListenerNullType()
    {
        assertFalse("Wrong result",
                list.removeEventListener(null, new ListenerTestImpl()));
    }

    /**
     * Tests that a listener can be registered multiple times for different
     * event types.
     */
    @Test
    public void testMultipleListenerRegistration()
    {
        ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);
        list.addEventListener(typeSub2, listener);

        list.fire(new EventSub2(this, typeSub2, MESSAGE));
        list.removeEventListener(typeSub1, listener);
        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub2, MESSAGE);
    }

    /**
     * Test event class. For testing purposes, a small hierarchy of test event
     * class is created. This way it can be checked whether event types are
     * correctly evaluated and take the event hierarchy into account.
     */
    private static class EventBase extends Event
    {
        /** An event message for testing pay-load. */
        private final String message;

        public EventBase(Object source, EventType<? extends EventBase> type,
                String msg)
        {
            super(source, type);
            message = msg;
        }

        public String getMessage()
        {
            return message;
        }
    }

    /**
     * A test event class derived from the base test event class.
     */
    private static class EventSub1 extends EventBase
    {
        public EventSub1(Object source, EventType<? extends EventSub1> type,
                String msg)
        {
            super(source, type, msg);
        }
    }

    /**
     * Another test event class derived from the base class.
     */
    private static class EventSub2 extends EventBase
    {
        public EventSub2(Object source, EventType<? extends EventSub2> type,
                String msg)
        {
            super(source, type, msg);
        }
    }

    /**
     * A test event listener implementation. This listener class expects that it
     * receives at most a single event. This event is stored for further
     * evaluation.
     */
    private static class ListenerTestImpl implements EventListener<EventBase>
    {
        /** The event received by this object. */
        private EventBase receivedEvent;

        @Override
        public void onEvent(EventBase event)
        {
            assertNull("Too many events: " + event, receivedEvent);
            receivedEvent = event;
        }

        /**
         * Checks that this listener has not received any event.
         */
        public void assertNoEvent()
        {
            assertNull("Unexpected event received: " + receivedEvent,
                    receivedEvent);
        }

        /**
         * Checks that this listener has received an event with the expected
         * properties.
         *
         * @param expSource the expected source
         * @param expType the expected type
         * @param expMessage the expected message
         */
        public void assertEvent(Object expSource, EventType<?> expType,
                String expMessage)
        {
            assertNotNull("No event received", receivedEvent);
            assertEquals("Wrong source", expSource, receivedEvent.getSource());
            assertEquals("Wrong event type", expType,
                    receivedEvent.getEventType());
            assertEquals("Wrong message", expMessage,
                    receivedEvent.getMessage());
        }
    }
}
