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
package org.apache.commons.configuration2.reloading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.lang3.mutable.MutableObject;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code ReloadingController}.
 *
 */
public class TestReloadingController
{
    /** A mock for the detector. */
    private ReloadingDetector detector;

    @Before
    public void setUp() throws Exception
    {
        detector = EasyMock.createMock(ReloadingDetector.class);
    }

    /**
     * Creates a default test instance.
     *
     * @return the test instance
     */
    private ReloadingController createController()
    {
        return new ReloadingController(detector);
    }

    /**
     * Tries to create an instance without a detector.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoDetector()
    {
        new ReloadingController(null);
    }

    /**
     * Tests that a newly created instance is not in reloading state.
     */
    @Test
    public void testReloadingStateAfterInit()
    {
        assertFalse("In reloading state", createController()
                .isInReloadingState());
    }

    /**
     * Creates a mock event listener.
     *
     * @return the mock listener
     */
    private static EventListener<ReloadingEvent> createListenerMock()
    {
        @SuppressWarnings("unchecked")
        final
        EventListener<ReloadingEvent> listener =
                EasyMock.createMock(EventListener.class);
        return listener;
    }

    /**
     * Prepares the given event listener mock to expect an event notification.
     * The event received is stored in the given mutable object.
     *
     * @param l the listener mock
     * @param evRef the reference where to store the event
     */
    private void expectEvent(final EventListener<ReloadingEvent> l,
            final MutableObject<ReloadingEvent> evRef)
    {
        l.onEvent(EasyMock.anyObject(ReloadingEvent.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            @Override
            public Object answer() throws Throwable
            {
                evRef.setValue((ReloadingEvent) EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });
    }

    /**
     * Tests a reloading check with a positive result.
     */
    @Test
    public void testCheckForReloadingTrue()
    {
        final EventListener<ReloadingEvent> l = createListenerMock();
        final EventListener<ReloadingEvent> lRemoved = createListenerMock();
        final MutableObject<ReloadingEvent> evRef = new MutableObject<>();
        expectEvent(l, evRef);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        EasyMock.replay(detector, l, lRemoved);
        final ReloadingController ctrl = createController();
        ctrl.addEventListener(ReloadingEvent.ANY, lRemoved);
        ctrl.addEventListener(ReloadingEvent.ANY, l);
        assertTrue("Wrong result",
                ctrl.removeEventListener(ReloadingEvent.ANY, lRemoved));
        final Object testData = "Some test data";
        assertTrue("Wrong result", ctrl.checkForReloading(testData));
        assertTrue("Not in reloading state", ctrl.isInReloadingState());
        assertSame("Wrong event source", ctrl, evRef.getValue().getSource());
        assertSame("Wrong controller", ctrl, evRef.getValue().getController());
        assertEquals("Wrong event data", testData, evRef.getValue().getData());
        EasyMock.verify(l, lRemoved, detector);
    }

    /**
     * Tests a reloading check with a negative result.
     */
    @Test
    public void testCheckForReloadingFalse()
    {
        final EventListener<ReloadingEvent> l = createListenerMock();
        EasyMock.expect(detector.isReloadingRequired())
                .andReturn(Boolean.FALSE);
        EasyMock.replay(detector, l);
        final ReloadingController ctrl = createController();
        ctrl.addEventListener(ReloadingEvent.ANY, l);
        assertFalse("Wrong result", ctrl.checkForReloading(null));
        assertFalse("In reloading state", ctrl.isInReloadingState());
        EasyMock.verify(detector, l);
    }

    /**
     * Tests that no further checks are performed when already in reloading
     * state.
     */
    @Test
    public void testCheckForReloadingInReloadingState()
    {
        final EventListener<ReloadingEvent> l = createListenerMock();
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        expectEvent(l, new MutableObject<ReloadingEvent>());
        EasyMock.replay(detector, l);
        final ReloadingController ctrl = createController();
        ctrl.addEventListener(ReloadingEvent.ANY, l);
        assertTrue("Wrong result (1)", ctrl.checkForReloading(1));
        assertTrue("Wrong result (2)", ctrl.checkForReloading(2));
        EasyMock.verify(detector, l);
    }

    /**
     * Tests that the reloading state can be reset.
     */
    @Test
    public void testResetReloadingState()
    {
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        detector.reloadingPerformed();
        EasyMock.replay(detector);
        final ReloadingController ctrl = createController();
        ctrl.checkForReloading(null);
        ctrl.resetReloadingState();
        assertFalse("In reloading state", ctrl.isInReloadingState());
        EasyMock.verify(detector);
    }

    /**
     * Tests that resetReloadingState() has no effect if the controller is not
     * in reloading state.
     */
    @Test
    public void testResetReloadingNotInReloadingState()
    {
        EasyMock.replay(detector);
        createController().resetReloadingState();
    }

    /**
     * Tests the event type of the reloading event.
     */
    @Test
    public void testReloadingEventType()
    {
        assertEquals("Wrong super event type", Event.ANY,
                ReloadingEvent.ANY.getSuperType());
    }
}
