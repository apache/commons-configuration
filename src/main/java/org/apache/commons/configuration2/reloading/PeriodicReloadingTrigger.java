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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 * <p>
 * A timer-based trigger for reloading checks.
 * </p>
 * <p>
 * An instance of this class is constructed with a reference to a
 * {@link ReloadingController} and a period. After calling the {@code start()}
 * method a periodic task is started which calls
 * {@link ReloadingController#checkForReloading(Object)} on the associated
 * reloading controller. This way changes on a configuration source can be
 * detected without client code having to poll actively. The
 * {@code ReloadingController} will perform its checks and generates events if
 * it detects the need for a reloading operation.
 * </p>
 * <p>
 * Triggering of the controller can be disabled by calling the {@code stop()}
 * method and later be resumed by calling {@code start()} again. When the
 * trigger is no more needed its {@code shutdown()} method should be called.
 * </p>
 * <p>
 * When creating an instance a {@code ScheduledExecutorService} can be provided
 * which is then used by the object. Otherwise, a default executor service is
 * created and used. When shutting down this object it can be specified whether
 * the {@code ScheduledExecutorService} should be shut down, too.
 * </p>
 *
 * @since 2.0
 * @see ReloadingController
 */
public class PeriodicReloadingTrigger
{
    /** The executor service used by this trigger. */
    private final ScheduledExecutorService executorService;

    /** The associated reloading controller. */
    private final ReloadingController controller;

    /** The parameter to be passed to the controller. */
    private final Object controllerParam;

    /** The period. */
    private final long period;

    /** The time unit. */
    private final TimeUnit timeUnit;

    /** Stores the future object for the current trigger task. */
    private ScheduledFuture<?> triggerTask;

    /**
     * Creates a new instance of {@code PeriodicReloadingTrigger} and sets all
     * parameters.
     *
     * @param ctrl the {@code ReloadingController} (must not be <b>null</b>)
     * @param ctrlParam the optional parameter to be passed to the controller
     *        when doing reloading checks
     * @param triggerPeriod the period in which the controller is triggered
     * @param unit the time unit for the period
     * @param exec the executor service to use (can be <b>null</b>, then a
     *        default executor service is created
     * @throws IllegalArgumentException if a required argument is missing
     */
    public PeriodicReloadingTrigger(final ReloadingController ctrl, final Object ctrlParam,
            final long triggerPeriod, final TimeUnit unit, final ScheduledExecutorService exec)
    {
        if (ctrl == null)
        {
            throw new IllegalArgumentException(
                    "ReloadingController must not be null!");
        }

        controller = ctrl;
        controllerParam = ctrlParam;
        period = triggerPeriod;
        timeUnit = unit;
        executorService =
                (exec != null) ? exec : createDefaultExecutorService();
    }

    /**
     * Creates a new instance of {@code PeriodicReloadingTrigger} with a default
     * executor service.
     *
     * @param ctrl the {@code ReloadingController} (must not be <b>null</b>)
     * @param ctrlParam the optional parameter to be passed to the controller
     *        when doing reloading checks
     * @param triggerPeriod the period in which the controller is triggered
     * @param unit the time unit for the period
     * @throws IllegalArgumentException if a required argument is missing
     */
    public PeriodicReloadingTrigger(final ReloadingController ctrl, final Object ctrlParam,
            final long triggerPeriod, final TimeUnit unit)
    {
        this(ctrl, ctrlParam, triggerPeriod, unit, null);
    }

    /**
     * Starts this trigger. The associated {@code ReloadingController} will be
     * triggered according to the specified period. The first triggering happens
     * after a period. If this trigger is already started, this invocation has
     * no effect.
     */
    public synchronized void start()
    {
        if (!isRunning())
        {
            triggerTask =
                    getExecutorService().scheduleAtFixedRate(
                            createTriggerTaskCommand(), period, period,
                            timeUnit);
        }
    }

    /**
     * Stops this trigger. The associated {@code ReloadingController} is no more
     * triggered. If this trigger is already stopped, this invocation has no
     * effect.
     */
    public synchronized void stop()
    {
        if (isRunning())
        {
            triggerTask.cancel(false);
            triggerTask = null;
        }
    }

    /**
     * Returns a flag whether this trigger is currently active.
     *
     * @return a flag whether this trigger is running
     */
    public synchronized boolean isRunning()
    {
        return triggerTask != null;
    }

    /**
     * Shuts down this trigger and optionally shuts down the
     * {@code ScheduledExecutorService} used by this object. This method should
     * be called if this trigger is no more needed. It ensures that the trigger
     * is stopped. If the parameter is <b>true</b>, the executor service is also
     * shut down. This should be done if this trigger is the only user of this
     * executor service.
     *
     * @param shutdownExecutor a flag whether the associated
     *        {@code ScheduledExecutorService} is to be shut down
     */
    public void shutdown(final boolean shutdownExecutor)
    {
        stop();
        if (shutdownExecutor)
        {
            getExecutorService().shutdown();
        }
    }

    /**
     * Shuts down this trigger and its {@code ScheduledExecutorService}. This is
     * a shortcut for {@code shutdown(true)}.
     *
     * @see #shutdown(boolean)
     */
    public void shutdown()
    {
        shutdown(true);
    }

    /**
     * Returns the {@code ScheduledExecutorService} used by this object.
     *
     * @return the associated {@code ScheduledExecutorService}
     */
    ScheduledExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Creates the task which triggers the reloading controller.
     *
     * @return the newly created trigger task
     */
    private Runnable createTriggerTaskCommand()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                controller.checkForReloading(controllerParam);
            }
        };
    }

    /**
     * Creates a default executor service. This method is called if no executor
     * has been passed to the constructor.
     *
     * @return the default executor service
     */
    private static ScheduledExecutorService createDefaultExecutorService()
    {
        final ThreadFactory factory =
                new BasicThreadFactory.Builder()
                        .namingPattern("ReloadingTrigger-%s").daemon(true)
                        .build();
        return Executors.newScheduledThreadPool(1, factory);
    }
}
