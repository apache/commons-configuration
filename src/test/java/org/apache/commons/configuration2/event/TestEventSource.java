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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for BaseEventSource.
 *
 */
public class TestEventSource
{
    /** Constant for the event property name. */
    private final String TEST_PROPNAME = "test.property.name";

    /** Constant for the event property value. */
    private static final Object TEST_PROPVALUE = "a test property value";

    /** The object under test. */
    private CountingEventSource source;

    @Before
    public void setUp() throws Exception
    {
        source = new CountingEventSource();
    }

    /**
     * Tests a newly created source object.
     */
    @Test
    public void testInit()
    {
        assertTrue("Listeners list is not empty", source
                .getEventListenerRegistrations().isEmpty());
        assertFalse("Removing listener", source.removeEventListener(
                ConfigurationEvent.ANY, new EventListenerTestImpl(null)));
        assertFalse("Detail events are enabled", source.isDetailEvents());
    }

    /**
     * Tests registering a new listener.
     */
    @Test
    public void testAddEventListener()
    {
        final EventListenerTestImpl l = new EventListenerTestImpl(this);
        source.addEventListener(ConfigurationEvent.ANY, l);
        final Collection<EventListener<? super ConfigurationEvent>> listeners =
                source.getEventListeners(ConfigurationEvent.ANY);
        assertEquals("Wrong number of listeners", 1, listeners.size());
        assertTrue("Listener not in list", listeners.contains(l));
    }

    /**
     * Tests adding an undefined configuration listener. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullEventListener()
    {
        source.addEventListener(ConfigurationEvent.ANY, null);
    }

    /**
     * Tests removing a listener.
     */
    @Test
    public void testRemoveEventListener()
    {
        final EventListenerTestImpl l = new EventListenerTestImpl(this);
        assertFalse("Listener can be removed?", source
                .removeEventListener(ConfigurationEvent.ANY, l));
        source.addEventListener(ConfigurationEvent.ADD_NODES, new EventListenerTestImpl(this));
        source.addEventListener(ConfigurationEvent.ANY, l);
        assertFalse("Unknown listener can be removed", source
                .removeEventListener(ConfigurationEvent.ANY, new EventListenerTestImpl(null)));
        assertTrue("Could not remove listener", source
                .removeEventListener(ConfigurationEvent.ANY, l));
        assertFalse("Listener still in list", source
                .getEventListeners(ConfigurationEvent.ANY).contains(l));
    }

    /**
     * Tests if a null listener can be removed. This should be a no-op.
     */
    @Test
    public void testRemoveNullEventListener()
    {
        source.addEventListener(ConfigurationEvent.ANY, new EventListenerTestImpl(null));
        assertFalse("Null listener can be removed", source
                .removeEventListener(ConfigurationEvent.ANY, null));
        assertEquals("Listener list was modified", 1, source
                .getEventListeners(ConfigurationEvent.ANY).size());
    }

    /**
     * Tests whether the listeners list is read only.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetEventListenersUpdate()
    {
        source.addEventListener(ConfigurationEvent.ANY,
                new EventListenerTestImpl(null));
        final Collection<EventListener<? super ConfigurationEvent>> list =
                source.getEventListeners(ConfigurationEvent.ANY);
        list.clear();
    }

    /**
     * Tests that the collection returned by getEventListeners() is
     * really a snapshot. A later added listener must not be visible.
     */
    @Test
    public void testGetEventListenersAddNew()
    {
        final Collection<EventListener<? super ConfigurationEvent>> list =
                source.getEventListeners(ConfigurationEvent.ANY);
        source.addEventListener(ConfigurationEvent.ANY,
                new EventListenerTestImpl(null));
        assertTrue("Listener snapshot not empty", list.isEmpty());
    }

    /**
     * Tests enabling and disabling the detail events flag.
     */
    @Test
    public void testSetDetailEvents()
    {
        source.setDetailEvents(true);
        assertTrue("Detail events are disabled", source.isDetailEvents());
        source.setDetailEvents(true);
        source.setDetailEvents(false);
        assertTrue("Detail events are disabled again", source.isDetailEvents());
        source.setDetailEvents(false);
        assertFalse("Detail events are still enabled", source.isDetailEvents());
    }

    /**
     * Tests delivering an event to a listener.
     */
    @Test
    public void testFireEvent()
    {
        final EventListenerTestImpl l = new EventListenerTestImpl(source);
        source.addEventListener(ConfigurationEvent.ANY, l);
        source.fireEvent(ConfigurationEvent.ADD_PROPERTY, TEST_PROPNAME,
                TEST_PROPVALUE, true);
        l.checkEvent(ConfigurationEvent.ADD_PROPERTY, TEST_PROPNAME,
                TEST_PROPVALUE, true);
        l.done();
    }

    /**
     * Tests firing an event if there are no listeners.
     */
    @Test
    public void testFireEventNoListeners()
    {
        source.fireEvent(ConfigurationEvent.ADD_NODES, TEST_PROPNAME,
                TEST_PROPVALUE, false);
        assertEquals("An event object was created", 0, source.eventCount);
    }

    /**
     * Tests generating a detail event if detail events are not allowed.
     */
    @Test
    public void testFireEventNoDetails()
    {
        final EventListenerTestImpl l = new EventListenerTestImpl(source);
        source.addEventListener(ConfigurationEvent.ANY, l);
        source.setDetailEvents(false);
        source.fireEvent(ConfigurationEvent.SET_PROPERTY, TEST_PROPNAME, TEST_PROPVALUE, false);
        assertEquals("Event object was created", 0, source.eventCount);
        l.done();
    }

    /**
     * Tests whether an event listener can deregister itself in reaction of a
     * delivered event.
     */
    @Test
    public void testRemoveListenerInFireEvent()
    {
        final EventListener<ConfigurationEvent> lstRemove = new EventListener<ConfigurationEvent>()
        {
            @Override
            public void onEvent(final ConfigurationEvent event)
            {
                source.removeEventListener(ConfigurationEvent.ANY, this);
            }
        };

        source.addEventListener(ConfigurationEvent.ANY, lstRemove);
        final EventListenerTestImpl l = new EventListenerTestImpl(source);
        source.addEventListener(ConfigurationEvent.ANY, l);
        source.fireEvent(ConfigurationEvent.ADD_PROPERTY, TEST_PROPNAME,
                TEST_PROPVALUE, false);
        l.checkEvent(ConfigurationEvent.ADD_PROPERTY, TEST_PROPNAME,
                TEST_PROPVALUE, false);
        assertEquals("Listener was not removed", 1,
                source.getEventListeners(ConfigurationEvent.ANY).size());
    }

    /**
     * Tests delivering an error event to a listener.
     */
    @Test
    public void testFireError()
    {
        final ErrorListenerTestImpl lstRead = new ErrorListenerTestImpl(source);
        final ErrorListenerTestImpl lstWrite = new ErrorListenerTestImpl(source);
        final ErrorListenerTestImpl lstAll = new ErrorListenerTestImpl(source);
        source.addEventListener(ConfigurationErrorEvent.READ, lstRead);
        source.addEventListener(ConfigurationErrorEvent.WRITE, lstWrite);
        source.addEventListener(ConfigurationErrorEvent.ANY, lstAll);
        final Exception testException = new Exception("A test");

        source.fireError(ConfigurationErrorEvent.WRITE,
                ConfigurationEvent.ADD_PROPERTY, TEST_PROPNAME, TEST_PROPVALUE,
                testException);
        lstRead.done();
        assertEquals("Wrong exception (1)", testException, lstWrite.checkEvent(
                ConfigurationErrorEvent.WRITE, ConfigurationEvent.ADD_PROPERTY,
                TEST_PROPNAME, TEST_PROPVALUE));
        lstWrite.done();
        assertEquals("Wrong exception (2)", testException, lstAll.checkEvent(
                ConfigurationErrorEvent.WRITE, ConfigurationEvent.ADD_PROPERTY,
                TEST_PROPNAME, TEST_PROPVALUE));
        lstAll.done();
        assertEquals("Wrong number of error events created", 1,
                source.errorCount);
    }

    /**
     * Tests firing an error event if there are no error listeners.
     */
    @Test
    public void testFireErrorNoListeners()
    {
        source.fireError(ConfigurationErrorEvent.ANY, ConfigurationEvent.ANY,
                TEST_PROPNAME, TEST_PROPVALUE, new Exception());
        assertEquals("An error event object was created", 0, source.errorCount);
    }

    /**
     * Tests cloning an event source object. The registered listeners should not
     * be registered at the clone.
     */
    @Test
    public void testClone() throws CloneNotSupportedException
    {
        source.addEventListener(ConfigurationEvent.ANY, new EventListenerTestImpl(source));
        final BaseEventSource copy = (BaseEventSource) source.clone();
        assertTrue("Configuration listeners registered for clone", copy
                .getEventListenerRegistrations().isEmpty());
    }

    /**
     * Tests whether all event listeners can be removed.
     */
    @Test
    public void testClearEventListeners()
    {
        source.addEventListener(ConfigurationEvent.ANY,
                new EventListenerTestImpl(source));
        source.addEventListener(ConfigurationEvent.ANY_HIERARCHICAL,
                new EventListenerTestImpl(source));

        source.clearEventListeners();
        assertTrue("Got ANY listeners",
                source.getEventListeners(ConfigurationEvent.ANY).isEmpty());
        assertTrue("Got HIERARCHICAL listeners",
                source.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL)
                        .isEmpty());
    }

    /**
     * Tests whether event listeners can be copied to another source.
     */
    @Test
    public void testCopyEventListeners()
    {
        final EventListenerTestImpl l1 = new EventListenerTestImpl(source);
        final EventListenerTestImpl l2 = new EventListenerTestImpl(source);
        source.addEventListener(ConfigurationEvent.ANY, l1);
        source.addEventListener(ConfigurationEvent.ANY_HIERARCHICAL, l2);

        final BaseEventSource source2 = new BaseEventSource();
        source.copyEventListeners(source2);
        Collection<EventListener<? super ConfigurationEvent>> listeners =
                source2.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL);
        assertEquals("Wrong number of listeners (1)", 2, listeners.size());
        assertTrue("l1 not found", listeners.contains(l1));
        assertTrue("l2 not found", listeners.contains(l2));
        listeners = source2.getEventListeners(ConfigurationEvent.ANY);
        assertEquals("Wrong number of listeners (2)", 1, listeners.size());
        assertTrue("Wrong listener", listeners.contains(l1));
    }

    /**
     * Tries to copy event listeners to a null source.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyEventListenersNullSource()
    {
        source.copyEventListeners(null);
    }

    /**
     * Tests whether all error listeners can be cleared.
     */
    @Test
    public void testClearErrorListeners()
    {
        final EventListener<ConfigurationEvent> cl = new EventListenerTestImpl(null);
        final ErrorListenerTestImpl el1 = new ErrorListenerTestImpl(null);
        final ErrorListenerTestImpl el2 = new ErrorListenerTestImpl(null);
        final ErrorListenerTestImpl el3 = new ErrorListenerTestImpl(null);
        source.addEventListener(ConfigurationErrorEvent.READ, el1);
        source.addEventListener(ConfigurationErrorEvent.ANY, el2);
        source.addEventListener(ConfigurationEvent.ANY, cl);
        source.addEventListener(ConfigurationErrorEvent.WRITE, el3);

        source.clearErrorListeners();
        final List<EventListenerRegistrationData<?>> regs =
                source.getEventListenerRegistrations();
        assertEquals("Wrong number of event listener registrations", 1,
                regs.size());
        assertSame("Wrong remaining listener", cl, regs.get(0).getListener());
    }

    /**
     * A specialized event source implementation that counts the number of
     * created event objects. It is used to test whether the
     * {@code fireEvent()} methods only creates event objects if
     * necessary. It also allows testing the clone() operation.
     */
    private static class CountingEventSource extends BaseEventSource implements Cloneable
    {
        int eventCount;

        int errorCount;

        @Override
        protected <T extends ConfigurationEvent> ConfigurationEvent createEvent(
                final EventType<T> eventType, final String propName, final Object propValue,
                final boolean before)
        {
            eventCount++;
            return super.createEvent(eventType, propName, propValue, before);
        }

        @Override
        protected ConfigurationErrorEvent createErrorEvent(
                final EventType<? extends ConfigurationErrorEvent> type,
                final EventType<?> opType, final String propName, final Object propValue,
                final Throwable ex)
        {
            errorCount++;
            return super
                    .createErrorEvent(type, opType, propName, propValue, ex);
        }
    }
}
