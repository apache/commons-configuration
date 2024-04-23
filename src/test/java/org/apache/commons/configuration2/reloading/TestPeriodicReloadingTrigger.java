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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

/**
 * Test class for {@code PeriodicReloadingTrigger}.
 */
public class TestPeriodicReloadingTrigger {
    /** Constant for a parameter to be passed to the controller. */
    private static final Object CTRL_PARAM = "Test controller parameter";

    /** Constant for the period. */
    private static final long PERIOD = 60;

    /** Constant for the period's time unit. */
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    /**
     * Creates a mock object for a scheduled future.
     *
     * @return the mock
     */
    @SuppressWarnings("unchecked")
    private static ScheduledFuture<Void> createFutureMock() {
        return mock(ScheduledFuture.class);
    }

    /** A mock for the executor service. */
    private ScheduledExecutorService executor;

    /** A mock for the reloading controller. */
    private ReloadingController controller;

    /**
     * Creates a test instance with default parameters.
     *
     * @return the test instance
     */
    private PeriodicReloadingTrigger createTrigger() {
        return new PeriodicReloadingTrigger(controller, CTRL_PARAM, PERIOD, UNIT, executor);
    }

    @BeforeEach
    public void setUp() throws Exception {
        executor = mock(ScheduledExecutorService.class);
        controller = mock(ReloadingController.class);
    }

    /**
     * Tests whether a default executor service is created if necessary.
     */
    @Test
    public void testDefaultExecutor() {
        final PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(controller, CTRL_PARAM, PERIOD, UNIT);
        assertNotNull(trigger.getExecutorService());
    }

    /**
     * Tries to create an instance without a controller.
     */
    @Test
    public void testInitNoController() {
        assertThrows(IllegalArgumentException.class, () -> new PeriodicReloadingTrigger(null, CTRL_PARAM, PERIOD, UNIT));
    }

    /**
     * Tests that a newly created trigger is not running.
     */
    @Test
    public void testIsRunningAfterInit() {
        assertFalse(createTrigger().isRunning());
    }

    /**
     * Tests a shutdown operation.
     */
    @Test
    public void testShutdown() {
        final ScheduledFuture<Void> future = createFutureMock();

        whenScheduled().thenReturn(future);
        when(future.cancel(false)).thenReturn(Boolean.TRUE);

        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.shutdown();

        verifyScheduled();
        verify(future).cancel(false);
        verify(executor).shutdown();
        verifyNoMoreInteractions(future, controller, executor);
    }

    /**
     * Tests a shutdown operation which excludes the executor service.
     */
    @Test
    public void testShutdownNoExecutor() {
        createTrigger().shutdown(false);
    }

    /**
     * Tests whether the trigger can be started.
     */
    @Test
    public void testStart() {
        final ScheduledFuture<Void> future = createFutureMock();
        final MutableObject<Runnable> refTask = new MutableObject<>();

        whenScheduled().thenAnswer(invocation -> {
            refTask.setValue(invocation.getArgument(0, Runnable.class));
            return future;
        });
        when(controller.checkForReloading(CTRL_PARAM)).thenReturn(Boolean.FALSE);

        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        assertTrue(trigger.isRunning());
        refTask.getValue().run();

        verifyScheduled();
        verify(controller).checkForReloading(CTRL_PARAM);
        verifyNoMoreInteractions(future, controller, executor);
    }

    /**
     * Tests whether start() is a noop if the trigger is already running.
     */
    @Test
    public void testStartTwice() {
        final ScheduledFuture<Void> future = createFutureMock();

        whenScheduled().thenReturn(future);

        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.start();

        verifyScheduled();
        verifyNoMoreInteractions(future, controller, executor);
    }

    /**
     * Tests whether a running trigger can be stopped.
     */
    @Test
    public void testStop() {
        final ScheduledFuture<Void> future = createFutureMock();

        whenScheduled().thenReturn(future);
        when(future.cancel(false)).thenReturn(Boolean.TRUE);

        final PeriodicReloadingTrigger trigger = createTrigger();
        trigger.start();
        trigger.stop();
        assertFalse(trigger.isRunning());

        verifyScheduled();
        verify(future).cancel(false);
        verifyNoMoreInteractions(future, controller, executor);
    }

    /**
     * Tests stop() if the trigger is not running.
     */
    @Test
    public void testStopNotRunning() {
        createTrigger().stop();
    }

    /**
     * Verifies that an invocation has occurred on the executor mock which schedules the trigger task.
     */
    private void verifyScheduled() {
        verify(executor).scheduleAtFixedRate(any(), eq(PERIOD), eq(PERIOD), eq(UNIT));
    }

    /**
     * Prepares the executor mock to for any invocation which schedules the trigger task.
     * This method should be used to call one of the {@code thenReturn}, {@code thenAnswer} or {@code thenThrow} methods.
     *
     * @return An ongoing stubbing for the future
     */
    private OngoingStubbing<ScheduledFuture<?>> whenScheduled() {
        return when(executor.scheduleAtFixedRate(any(), eq(PERIOD), eq(PERIOD), eq(UNIT)));
    }
}
