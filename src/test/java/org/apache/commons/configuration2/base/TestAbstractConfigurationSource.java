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
package org.apache.commons.configuration2.base;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.easymock.EasyMock;

/**
 * Test class for {@code AbstractConfigurationSource}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestAbstractConfigurationSource extends TestCase
{
    /** The source to be tested. */
    private AbstractConfigurationSourceTestImpl source;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        source = new AbstractConfigurationSourceTestImpl();
    }

    /**
     * Tries to add a configuration listener. This is not supported.
     */
    public void testAddConfigurationListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        EasyMock.replay(l);
        try
        {
            source.addConfigurationSourceListener(l);
            fail("Could add a listener!");
        }
        catch (UnsupportedOperationException uoex)
        {
            EasyMock.verify(l);
        }
    }

    /**
     * Tries to remove a configuration listener. This is not supported.
     */
    public void testRemoveConfigurationListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        EasyMock.replay(l);
        try
        {
            source.removeConfigurationSourceListener(l);
            fail("Could remove a listener!");
        }
        catch (UnsupportedOperationException uoex)
        {
            EasyMock.verify(l);
        }
    }

    /**
     * Tests whether capabilities from implemented interfaces can be queried.
     */
    public void testGetCapabilityImplemented()
    {
        assertEquals("Wrong source capability", source, source
                .getCapability(ConfigurationSource.class));
        assertEquals("Wrong runnable capability", source, source
                .getCapability(Runnable.class));
    }

    /**
     * Tests whether capabilities provided by appendCapabilities() can be
     * queried.
     */
    public void testGetCapabilityProvided()
    {
        Runnable r = EasyMock.createNiceMock(Runnable.class);
        source.capabilityList = Collections.singleton(new Capability(
                Runnable.class, r));
        assertEquals("Wrong runnable capability", r, source
                .getCapability(Runnable.class));
    }

    /**
     * Tests whether the capabilities object is created once and cached.
     */
    public void testGetCapabilitiesCached()
    {
        Capabilities caps = source.getCapabilities();
        assertNotNull("No capabilities", caps);
        assertSame("Different instance", caps, source.getCapabilities());
    }

    /**
     * Tests concurrent access to the capabilities.
     */
    public void testGetCapabilitiesMultiThreaded() throws InterruptedException
    {
        final int threadCount = 64;
        CountDownLatch latch = new CountDownLatch(1);
        CapabilitiesTestThread[] threads = new CapabilitiesTestThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new CapabilitiesTestThread(latch);
            threads[i].start();
        }
        latch.countDown(); // start all threads
        Capabilities caps = source.getCapabilities();
        for (CapabilitiesTestThread t : threads)
        {
            t.join();
            assertSame("Different capabilities", caps, t.caps);
        }
    }

    /**
     * A concrete test implementation of {@code AbstractConfigurationSource}.
     */
    private static class AbstractConfigurationSourceTestImpl extends
            AbstractConfigurationSource implements Runnable
    {
        /** A list with capabilities to be added to the capabilities object. */
        Collection<Capability> capabilityList;

        public void clear()
        {
        }

        public void run()
        {
        }

        /**
         * Appends capabilities if the list is defined.
         */
        @Override
        protected void appendCapabilities(Collection<Capability> caps)
        {
            super.appendCapabilities(caps);
            if (capabilityList != null)
            {
                caps.addAll(capabilityList);
            }
        }
    }

    /**
     * A test thread class for testing concurrent access to the source's
     * capabilities.
     */
    private class CapabilitiesTestThread extends Thread
    {
        /** The latch for synchronizing the start. */
        private final CountDownLatch startLatch;

        /** The capabilities obtained from the source. */
        Capabilities caps;

        public CapabilitiesTestThread(CountDownLatch latch)
        {
            startLatch = latch;
        }

        @Override
        public void run()
        {
            try
            {
                startLatch.await();
                caps = source.getCapabilities();
            }
            catch (InterruptedException iex)
            {
                // fall through
            }
        }
    }
}
