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

import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingEvent;

/**
 * <p>
 * An internally used helper class for adding reloading support to an arbitrary
 * {@link ConfigurationBuilder}.
 * </p>
 * <p>
 * This class connects a configuration builder with a
 * {@link ReloadingController}. This is done in the following way:
 * <ul>
 * <li>An instance is registered as listener at a {@code ReloadingController}.
 * Whenever the controller indicates that a reload should happen, the associated
 * configuration builder's {@link BasicConfigurationBuilder#resetResult()}
 * method is called.</li>
 * <li>When the builder fires a {@link ConfigurationBuilderResultCreatedEvent}
 * event the reloading controller's reloading state is reset. At that time the
 * reload has actually happened, and the controller is prepared to observe new
 * changes.</li>
 * </ul>
 * </p>
 *
 * @since 2.0
 */
final class ReloadingBuilderSupportListener implements EventListener<Event>
{
    /** Stores the associated configuration builder. */
    private final BasicConfigurationBuilder<?> builder;

    /** Stores the associated reloading controller. */
    private final ReloadingController reloadingController;

    /**
     * Creates a new instance of {@code ReloadingBuilderSupportListener} and
     * initializes it with the associated objects.
     *
     * @param configBuilder the configuration builder
     * @param controller the {@code ReloadingController}
     */
    private ReloadingBuilderSupportListener(
            final BasicConfigurationBuilder<?> configBuilder,
            final ReloadingController controller)
    {
        builder = configBuilder;
        reloadingController = controller;
    }

    /**
     * Creates a new instance of {@code ReloadingBuilderSupportListener} which
     * connects the specified {@code ConfigurationBuilder} with the given
     * {@code ReloadingController}. Listeners are registered to react on
     * notifications and implement a reloading protocol as described in the
     * class comment.
     *
     * @param configBuilder the {@code ConfigurationBuilder}
     * @param controller the {@code ReloadingController}
     * @return the newly created listener object
     */
    public static ReloadingBuilderSupportListener connect(
            final BasicConfigurationBuilder<?> configBuilder,
            final ReloadingController controller)
    {
        final ReloadingBuilderSupportListener listener =
                new ReloadingBuilderSupportListener(configBuilder, controller);
        controller.addEventListener(ReloadingEvent.ANY, listener);
        configBuilder
                .installEventListener(
                        ConfigurationBuilderResultCreatedEvent.RESULT_CREATED,
                        listener);
        return listener;
    }

    /**
     * {@inheritDoc} This implementation resets the controller's reloading state
     * if an event about a newly created result was received. Otherwise, in case
     * of a reloading event, the builder's result object is reset.
     */
    @Override
    public void onEvent(final Event event)
    {
        if (ConfigurationBuilderResultCreatedEvent.RESULT_CREATED.equals(event
                .getEventType()))
        {
            reloadingController.resetReloadingState();
        }
        else
        {
            builder.resetResult();
        }
    }
}
