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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableObject;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code PeriodicReloadingTrigger}.
 *
 */
public class TestPeriodicReloadingTrigger
{
    /** Constant for a parameter to be passed to the controller. */
    private static final Object CTRL_PARAM = "Test controller parameter";

    /** Constant for the period. */
    private static final long PERIOD = 60;

    /** Constant for the period's time unit. */
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    /** A mock for the executor service. */
    private ScheduledExecutorService executor;

    /** A mock for the reloading controller. */
    private ReloadingController controller;

    @Before
    public void setUp() throws Exception
    {
        executor = EasyMock.createMock(ScheduledExecutorService.class);
        controller = EasyMock.createMock(ReloadingController.class);
    }

    /**
     * Creates a test instance with default parameters.
     *
     * @return the test instance
     */
    private PeriodicReloadingTrigger createTrigger()
    {
        return new PeriodicReloadingTrigger(controller, CTRL_PARAM, PERIOD, UNIT,
                executor);
    }

    /**
     * Tests whether a default executor service is created if necessary.
     */
    @Test
    public void testDefaultExecutor()
    {
        final PeriodicReloadingTrigger trigger =
                new PeriodicReloadingTrigger(controller, CTRL_PARAM, PERIOD, UNIT);
        assertNotNull("No executor service", trigger.getExecutorService());
    }

    /**
     * Tries to create an instance without a controller.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoController()
    {
        new PeriodicReloadingTrigger(null, CTRL_PARAM, PERIOD, UNIT);
    }

    /**
     * Tests that a newly created trigger is not running.
     */
    @Test
    public void testIsRunningAfterInit()
    {
        assertFalse("Running", createTrigger().isRunning());
    }

    /**
     * Creates a mock object for a scheduled future.
     *
     * @return the mock
     */
    private static ScheduledFuture<Void> createFutureMock()
    {
        @SuppressWarnings("unchecked")
        final
        ScheduledFuture<Void> mock = EasyMock.createMock(ScheduledFuture.class);
        return mock;
    }

    /**
     * Prepares the executor mock to expect an invocation which schedules the
     * trigger task.
     *
     * @param future the future object to return
     */
    private void expectSchedule(final ScheduledFuture<Void> future)
    {
        executor.scheduleAtFixedRate(EasyMock.anyObject(Runnable.class),
                EasyMock.eq(PERIOD), EasyMock.eq(PERIOD), EasyMock.eq(UNIT));
        if (future != null)
        {
            EasyMock.expectLastCall().andReturn(future);
        }
    }

    /**
     * Tests whether the trigger can be started.
     */
    @Test
    public void testStart()
    {
        final ScheduledFuture<Void> future = createFutureMock();
        final MutableObject<Runnable> refTask = new MutableObject<>();
        expectSchedule(null);
        EasyMock.expectLastCall().andAnswer(
                new IAnswer<ScheduledFuture<Void>>()
                {
                    @Override
                    public ScheduledFuture<Void> answer() throws Throwable
                    {
                        refTask.setValue((Runnable) EasyMock
                                .getCurrentArguments()[0]);
                        return future;
                    }
                });
        EasyMock.expect(controller.checkForReloading(CTRL_PARAM)).andReturn(
                Boolean.FALSE);
        EasyMock.replay(future, controller, executor);
        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        assertTrue("Not started", trigger.isRunning());
        refTask.getValue().run();
        EasyMock.verify(future, controller, executor);
    }

    /**
     * Tests whether start() is a noop if the trigger is already running.
     */
    @Test
    public void testStartTwice()
    {
        final ScheduledFuture<Void> future = createFutureMock();
        expectSchedule(future);
        EasyMock.replay(future, controller, executor);
        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.start();
        EasyMock.verify(future, controller, executor);
    }

    /**
     * Tests stop() if the trigger is not running.
     */
    @Test
    public void testStopNotRunning()
    {
        EasyMock.replay(controller, executor);
        createTrigger().stop();
    }

    /**
     * Tests whether a running trigger can be stopped.
     */
    @Test
    public void testStop()
    {
        final ScheduledFuture<Void> future = createFutureMock();
        expectSchedule(future);
        EasyMock.expect(future.cancel(false)).andReturn(Boolean.TRUE);
        EasyMock.replay(future, controller, executor);
        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.stop();
        assertFalse("Still running", trigger.isRunning());
        EasyMock.verify(future, controller, executor);
    }

    /**
     * Tests a shutdown operation.
     */
    @Test
    public void testShutdown()
    {
        final ScheduledFuture<Void> future = createFutureMock();
        expectSchedule(future);
        EasyMock.expect(future.cancel(false)).andReturn(Boolean.TRUE);
        executor.shutdown();
        EasyMock.replay(future, controller, executor);
        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.shutdown();
        EasyMock.verify(future, controller, executor);
    }

    /**
     * Tests a shutdown operation which excludes the executor service.
     */
    @Test
    public void testShutdownNoExecutor()
    {
        EasyMock.replay(controller, executor);
        createTrigger().shutdown(false);
    }
}
