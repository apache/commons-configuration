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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code EventListenerList.}
 *
 * @since 2.0
 */
public class TestEventListenerList {
    /**
     * Test event class. For testing purposes, a small hierarchy of test event class is created. This way it can be checked
     * whether event types are correctly evaluated and take the event hierarchy into account.
     */
    private static class EventBase extends Event {
        private static final long serialVersionUID = 1L;

        /** An event message for testing pay-load. */
        private final String message;

        public EventBase(final Object source, final EventType<? extends EventBase> type, final String msg) {
            super(source, type);
            message = msg;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * A test event class derived from the base test event class.
     */
    private static class EventSub1 extends EventBase {
        private static final long serialVersionUID = 1L;

        public EventSub1(final Object source, final EventType<? extends EventSub1> type, final String msg) {
            super(source, type, msg);
        }
    }

    /**
     * Another test event class derived from the base class.
     */
    private static class EventSub2 extends EventBase {
        private static final long serialVersionUID = 1L;

        public EventSub2(final Object source, final EventType<? extends EventSub2> type, final String msg) {
            super(source, type, msg);
        }
    }

    /**
     * A test event listener implementation. This listener class expects that it receives at most a single event. This event
     * is stored for further evaluation.
     */
    private static class ListenerTestImpl implements EventListener<EventBase> {
        /** The event received by this object. */
        private EventBase receivedEvent;

        /**
         * Checks that this listener has received an event with the expected properties.
         *
         * @param expSource the expected source
         * @param expType the expected type
         * @param expMessage the expected message
         */
        public void assertEvent(final Object expSource, final EventType<?> expType, final String expMessage) {
            assertNotNull(receivedEvent);
            assertEquals(expSource, receivedEvent.getSource());
            assertEquals(expType, receivedEvent.getEventType());
            assertEquals(expMessage, receivedEvent.getMessage());
        }

        /**
         * Checks that this listener has not received any event.
         */
        public void assertNoEvent() {
            assertNull(receivedEvent);
        }

        @Override
        public void onEvent(final EventBase event) {
            assertNull(receivedEvent, "Too many events: " + event);
            receivedEvent = event;
        }
    }

    /** Constant for a test event message. */
    private static final String MESSAGE = "TestEventMessage";

    /** Type for the base event. */
    private static EventType<EventBase> typeBase;

    /** Type for sub event 1. */
    private static EventType<EventSub1> typeSub1;

    /** Type for sub event 2. */
    private static EventType<EventSub2> typeSub2;

    /**
     * Helper method for collecting the elements in the given iterable.
     *
     * @param iterable the iterable
     * @return a list with the content of the iterable
     */
    private static <T> List<T> fetchElements(final Iterable<? extends T> iterable) {
        final List<T> elems = new LinkedList<>();
        for (final T listener : iterable) {
            elems.add(listener);
        }
        return elems;
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        typeBase = new EventType<>(Event.ANY, "BASE");
        typeSub1 = new EventType<>(typeBase, "SUB1");
        typeSub2 = new EventType<>(typeBase, "SUB2");
    }

    /** The list to be tested. */
    private EventListenerList list;

    /**
     * Helper method for checking whether a specific set of event listeners is returned by getEventListeners().
     *
     * @param eventType the event type
     * @param expListeners the expected listeners
     */
    private void checkEventListenersForType(final EventType<? extends Event> eventType, final EventListener<?>... expListeners) {
        final List<?> listeners = fetchElements(list.getEventListeners(eventType));
        assertEquals(Arrays.asList(expListeners), listeners);
    }

    @BeforeEach
    public void setUp() throws Exception {
        list = new EventListenerList();
    }

    /**
     * Tests whether the content of another list can be added.
     */
    @Test
    public void testAddAll() {
        final EventListener<EventBase> l1 = new ListenerTestImpl();
        final EventListener<EventBase> l2 = new ListenerTestImpl();
        final EventListener<EventBase> l3 = new ListenerTestImpl();
        list.addEventListener(typeBase, l1);
        final EventListenerList list2 = new EventListenerList();
        list2.addEventListener(typeSub1, l2);
        list2.addEventListener(typeBase, l3);

        list.addAll(list2);
        final Iterator<EventListenerRegistrationData<?>> it = list.getRegistrations().iterator();
        EventListenerRegistrationData<?> reg = it.next();
        assertEquals(typeBase, reg.getEventType());
        assertEquals(l1, reg.getListener());
        reg = it.next();
        assertEquals(typeSub1, reg.getEventType());
        assertEquals(l2, reg.getListener());
        reg = it.next();
        assertEquals(typeBase, reg.getEventType());
        assertEquals(l3, reg.getListener());
    }

    /**
     * Tries to add the content of a null list.
     */
    @Test
    public void testAddAllNull() {
        assertThrows(IllegalArgumentException.class, () -> list.addAll(null));
    }

    /**
     * Tests whether the list can be cleared.
     */
    @Test
    public void testClear() {
        list.addEventListener(typeSub1, new ListenerTestImpl());
        list.addEventListener(typeSub2, new ListenerTestImpl());

        list.clear();
        assertTrue(list.getRegistrations().isEmpty());
    }

    /**
     * Tests that a null event is handled by the iterator.
     */
    @Test
    public void testEventListenerIteratorNullEvent() {
        list.addEventListener(typeBase, new ListenerTestImpl());
        final EventListenerList.EventListenerIterator<EventBase> iterator = list.getEventListenerIterator(typeBase);
        assertThrows(IllegalArgumentException.class, () -> iterator.invokeNext(null));
    }

    /**
     * Tests whether the event listener iterator validates the passed in event object.
     */
    @Test
    public void testEventListenerIteratorWrongEvent() {
        final EventListener<EventSub2> listener = event -> {};
        list.addEventListener(typeSub2, listener);
        final EventListenerList.EventListenerIterator<EventSub2> iterator = list.getEventListenerIterator(typeSub2);
        assertTrue(iterator.hasNext());
        final Event event = new EventBase(this, typeBase, "Test");
        assertThrows(IllegalArgumentException.class, () -> iterator.invokeNext(event));
    }

    /**
     * Tests that a null event is rejected by fire().
     */
    @Test
    public void testFireNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> list.fire(null));
    }

    /**
     * Tests whether event listener registrations derived from a super type can be queried.
     */
    @Test
    public void testGetEventListenerRegistrationsForSuperType() {
        final ListenerTestImpl l1 = new ListenerTestImpl();
        final ListenerTestImpl l2 = new ListenerTestImpl();
        @SuppressWarnings("unchecked")
        final EventListener<Event> l3 = mock(EventListener.class);
        list.addEventListener(typeSub1, l1);
        list.addEventListener(Event.ANY, l3);
        list.addEventListener(typeBase, l2);

        final List<EventListenerRegistrationData<? extends EventBase>> regs = list.getRegistrationsForSuperType(typeBase);
        final Iterator<EventListenerRegistrationData<? extends EventBase>> iterator = regs.iterator();
        assertEquals(l1, iterator.next().getListener());
        assertEquals(l2, iterator.next().getListener());
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests whether the base type is taken into account when querying for event listeners.
     */
    @Test
    public void testGetEventListenersBaseType() {
        final ListenerTestImpl listener1 = new ListenerTestImpl();
        final ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeBase, listener1);
        list.addEventListener(typeBase, listener2);
        checkEventListenersForType(typeSub1, listener1, listener2);
    }

    /**
     * Tests that the iterator returned by getEventListeners() throws an exception if the iteration goes beyond the last
     * element.
     */
    @Test
    public void testGetEventListenersIteratorNextNoElement() {
        final ListenerTestImpl listener1 = new ListenerTestImpl();
        final ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeBase, listener1);
        list.addEventListener(typeBase, listener2);
        final Iterator<EventListener<? super EventBase>> iterator = list.getEventListeners(typeBase).iterator();
        for (int i = 0; i < 2; i++) {
            iterator.next();
        }
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /**
     * Tests that the iterator returned by getEventListeners() does not support remove() operations.
     */
    @Test
    public void testGetEventListenersIteratorRemove() {
        list.addEventListener(typeBase, new ListenerTestImpl());
        final Iterator<EventListener<? super EventBase>> iterator = list.getEventListeners(typeBase).iterator();
        assertTrue(iterator.hasNext());
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    /**
     * Tests whether only matching event listeners are returned by getEventListeners().
     */
    @Test
    public void testGetEventListenersMatchingType() {
        final ListenerTestImpl listener1 = new ListenerTestImpl();
        final ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener1);
        list.addEventListener(typeSub2, listener2);
        checkEventListenersForType(typeSub1, listener1);
    }

    /**
     * Tests that an empty result is correctly handled by getEventListeners().
     */
    @Test
    public void testGetEventListenersNoMatch() {
        list.addEventListener(typeSub1, new ListenerTestImpl());
        checkEventListenersForType(typeSub2);
    }

    /**
     * Tests whether event listeners for a null type can be queried.
     */
    @Test
    public void testGetEventListenersNull() {
        assertTrue(fetchElements(list.getEventListeners(null)).isEmpty());
    }

    /**
     * Tests whether all event listener registrations can be queried.
     */
    @Test
    public void testGetRegistrations() {
        final EventListenerRegistrationData<EventSub1> reg1 = new EventListenerRegistrationData<>(typeSub1, new ListenerTestImpl());
        final EventListenerRegistrationData<EventSub2> reg2 = new EventListenerRegistrationData<>(typeSub2, new ListenerTestImpl());
        list.addEventListener(reg1);
        list.addEventListener(reg2);

        final List<EventListenerRegistrationData<?>> registrations = list.getRegistrations();
        assertEquals(Arrays.asList(reg1, reg2), registrations);
    }

    /**
     * Tests that the list with registration information cannot be modified.
     */
    @Test
    public void testGetRegistrationsModify() {
        final EventListenerRegistrationData<EventBase> registrationData = new EventListenerRegistrationData<>(typeBase, new ListenerTestImpl());
        List<EventListenerRegistrationData<?>> registrations = list.getRegistrations();
        assertThrows(UnsupportedOperationException.class, () -> registrations.add(registrationData));
    }

    /**
     * Tests whether an event listener can be registered via a registration data object.
     */
    @Test
    public void testListenerRegistrationWithListenerData() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        final EventListenerRegistrationData<EventSub1> regData = new EventListenerRegistrationData<>(typeSub1, listener);
        list.addEventListener(regData);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tries to register a listener with a null registration data object.
     */
    @Test
    public void testListenerRegistrationWithNullListenerData() {
        assertThrows(IllegalArgumentException.class, () -> list.addEventListener(null));
    }

    /**
     * Tests that a listener can be registered multiple times for different event types.
     */
    @Test
    public void testMultipleListenerRegistration() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);
        list.addEventListener(typeSub2, listener);

        list.fire(new EventSub2(this, typeSub2, MESSAGE));
        list.removeEventListener(typeSub1, listener);
        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub2, MESSAGE);
    }

    /**
     * Tests whether the event type is taken into account when calling listeners.
     */
    @Test
    public void testReceiveEventDifferentType() {
        final ListenerTestImpl listener1 = new ListenerTestImpl();
        final ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener1);
        list.addEventListener(typeSub2, listener2);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener1.assertEvent(this, typeSub1, MESSAGE);
        listener2.assertNoEvent();
    }

    /**
     * Tests whether multiple event listeners can be registered.
     */
    @Test
    public void testReceiveEventMultipleListeners() {
        final ListenerTestImpl listener1 = new ListenerTestImpl();
        final ListenerTestImpl listener2 = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener1);
        list.addEventListener(typeSub1, listener2);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener1.assertEvent(this, typeSub1, MESSAGE);
        listener2.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tests whether events matching the registration type are delivered.
     */
    @Test
    public void testReceiveEventOfExactType() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tests that events of a derived type are delivered to listeners registered for a base type.
     */
    @Test
    public void testReceiveEventSubType() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeBase, listener);

        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertEvent(this, typeSub1, MESSAGE);
    }

    /**
     * Tries to register a listener for a null event type.
     */
    @Test
    public void testRegisterEventTypeNull() {
        ListenerTestImpl listener = new ListenerTestImpl();
        assertThrows(IllegalArgumentException.class, () -> list.addEventListener(null, listener));
    }

    /**
     * Tests that null event listeners cannot be registered.
     */
    @Test
    public void testRegisterListenerNull() {
        assertThrows(IllegalArgumentException.class, () -> list.addEventListener(typeBase, null));
    }

    /**
     * Tests whether an event listener can be removed.
     */
    @Test
    public void testRemoveEventListenerExisting() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        assertTrue(list.removeEventListener(typeSub1, listener));
        list.fire(new EventSub1(this, typeSub1, MESSAGE));
        listener.assertNoEvent();
    }

    /**
     * Tests removeEventListener() if another event type is specified for an existing listener.
     */
    @Test
    public void testRemoveEventListenerNonExistingEventType() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        assertFalse(list.removeEventListener(typeBase, listener));
    }

    /**
     * Tests removeEventListener() for a non-existing event listener.
     */
    @Test
    public void testRemoveEventListenerNonExistingListener() {
        list.addEventListener(typeBase, new ListenerTestImpl());
        assertFalse(list.removeEventListener(typeBase, new ListenerTestImpl()));
    }

    /**
     * Tests that removeEventListener() can handle a null listener.
     */
    @Test
    public void testRemoveEventListenerNullListener() {
        assertFalse(list.removeEventListener(typeBase, null));
    }

    /**
     * Tests that removeEventListener() can handle a null registration object.
     */
    @Test
    public void testRemoveEventListenerNullRegistration() {
        assertFalse(list.removeEventListener(null));
    }

    /**
     * Tests that removeEventListener() can handle a null event type.
     */
    @Test
    public void testRemoveEventListenerNullType() {
        assertFalse(list.removeEventListener(null, new ListenerTestImpl()));
    }

    /**
     * Tests that events of a base type do not cause a listener to be invoked.
     */
    @Test
    public void testSuppressEventOfSuperType() {
        final ListenerTestImpl listener = new ListenerTestImpl();
        list.addEventListener(typeSub1, listener);

        list.fire(new EventBase(this, typeBase, MESSAGE));
        listener.assertNoEvent();
    }
}
