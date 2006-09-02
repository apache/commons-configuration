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

import java.util.Collection;

import junit.framework.TestCase;

/**
 * Test class for EventSource.
 *
 * @version $Id$
 */
public class TestEventSource extends TestCase
{
    /** Constant for the event type used for testing. */
    static final int TEST_TYPE = 42;

    /** Constant for the event property name. */
    static final String TEST_PROPNAME = "test.property.name";

    /** Constant for the event property value. */
    static final Object TEST_PROPVALUE = "a test property value";

    /** The object under test. */
    CountingEventSource source;

    protected void setUp() throws Exception
    {
        super.setUp();
        source = new CountingEventSource();
    }

    /**
     * Tests a newly created source object.
     */
    public void testInit()
    {
        assertTrue("Listeners list is not empty", source
                .getConfigurationListeners().isEmpty());
        assertFalse("Removing listener", source
                .removeConfigurationListener(new TestListener()));
        assertFalse("Detail events are enabled", source.isDetailEvents());
    }

    /**
     * Tests registering a new listener.
     */
    public void testAddConfigurationListener()
    {
        TestListener l = new TestListener();
        source.addConfigurationListener(l);
        Collection listeners = source.getConfigurationListeners();
        assertEquals("Wrong number of listeners", 1, listeners.size());
        assertTrue("Listener not in list", listeners.contains(l));
    }

    /**
     * Tests adding an undefined configuration listener. This should cause an
     * exception.
     */
    public void testAddNullConfigurationListener()
    {
        try
        {
            source.addConfigurationListener(null);
            fail("Could add null listener!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests removing a listener.
     */
    public void testRemoveConfigurationListener()
    {
        TestListener l = new TestListener();
        assertFalse("Listener can be removed?", source
                .removeConfigurationListener(l));
        source.addConfigurationListener(l);
        source.addConfigurationListener(new TestListener());
        assertFalse("Unknown listener can be removed", source
                .removeConfigurationListener(new TestListener()));
        assertTrue("Could not remove listener", source
                .removeConfigurationListener(l));
        assertFalse("Listener still in list", source
                .getConfigurationListeners().contains(l));
    }

    /**
     * Tests if a null listener can be removed. This should be a no-op.
     */
    public void testRemoveNullConfigurationListener()
    {
        source.addConfigurationListener(new TestListener());
        assertFalse("Null listener can be removed", source
                .removeConfigurationListener(null));
        assertEquals("Listener list was modified", 1, source
                .getConfigurationListeners().size());
    }

    /**
     * Tests whether the listeners list is read only.
     */
    public void testGetConfigurationListenersUpdate()
    {
        source.addConfigurationListener(new TestListener());
        Collection list = source.getConfigurationListeners();
        try
        {
            list.add("test");
            fail("Could manipulate list!");
        }
        catch (Exception ex)
        {
            // ok
        }
    }

    /**
     * Tests enabling and disabling the detail events flag.
     */
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
    public void testFireEvent()
    {
        TestListener l = new TestListener();
        source.addConfigurationListener(l);
        source.fireEvent(TEST_TYPE, TEST_PROPNAME, TEST_PROPVALUE, true);
        assertEquals("Not 1 event created", 1, source.eventCount);
        assertEquals("Listener not called once", 1, l.numberOfCalls);
        assertEquals("Wrong event type", TEST_TYPE, l.lastEvent.getType());
        assertEquals("Wrong property name", TEST_PROPNAME, l.lastEvent
                .getPropertyName());
        assertEquals("Wrong property value", TEST_PROPVALUE, l.lastEvent
                .getPropertyValue());
        assertTrue("Wrong before event flag", l.lastEvent.isBeforeUpdate());
    }

    /**
     * Tests firering an event if there are no listeners.
     */
    public void testFireEventNoListeners()
    {
        source.fireEvent(TEST_TYPE, TEST_PROPNAME, TEST_PROPVALUE, false);
        assertEquals("An event object was created", 0, source.eventCount);
    }

    /**
     * Tests generating a detail event if detail events are not allowed.
     */
    public void testFireEventNoDetails()
    {
        TestListener l = new TestListener();
        source.addConfigurationListener(l);
        source.setDetailEvents(false);
        source.fireEvent(TEST_TYPE, TEST_PROPNAME, TEST_PROPVALUE, false);
        assertEquals("Event object was created", 0, source.eventCount);
        assertEquals("Listener was called", 0, l.numberOfCalls);
    }

    /**
     * Tests whether an event listener can deregister itself in reaction of a
     * delivered event.
     */
    public void testRemoveListenerInFireEvent()
    {
        ConfigurationListener lstRemove = new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                source.removeConfigurationListener(this);
            }
        };

        source.addConfigurationListener(lstRemove);
        TestListener l = new TestListener();
        source.addConfigurationListener(l);
        source.fireEvent(TEST_TYPE, TEST_PROPNAME, TEST_PROPVALUE, false);
        assertEquals("Listener was not called", 1, l.numberOfCalls);
        assertEquals("Listener was not removed", 1, source
                .getConfigurationListeners().size());
    }

    /**
     * A test event listener implementation.
     */
    static class TestListener implements ConfigurationListener
    {
        ConfigurationEvent lastEvent;

        int numberOfCalls;

        public void configurationChanged(ConfigurationEvent event)
        {
            lastEvent = event;
            numberOfCalls++;
        }
    }

    /**
     * A specialized event source implementation that counts the number of
     * created event objects. It is used to test whether the
     * <code>fireEvent()</code> methods only creates event objects if
     * necessary.
     */
    static class CountingEventSource extends EventSource
    {
        int eventCount;

        protected ConfigurationEvent createEvent(int type, String propName,
                Object propValue, boolean before)
        {
            eventCount++;
            return super.createEvent(type, propName, propValue, before);
        }
    }
}
