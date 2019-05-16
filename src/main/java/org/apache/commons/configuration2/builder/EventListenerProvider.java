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

import org.apache.commons.configuration2.event.EventListenerList;

/**
 * <p>
 * Definition of an interface that is evaluated by a
 * {@link ConfigurationBuilder} to initialize event listeners.
 * </p>
 * <p>
 * This interface allows a convenient initialization of a configuration builder
 * with event listeners to be registered at the managed configuration object.
 * The {@code configure()} method of {@link BasicConfigurationBuilder} checks
 * whether a parameters object passed to it implements this interface. If this
 * is the case, all event listeners defined by the object are added to the
 * internal list managed by the builder. They are then automatically registered
 * at the managed configuration when it is created. When using a corresponding
 * implementation the configuration of event listeners can be done in the same
 * fluent API style as the other initialization of the configuration builder.
 * </p>
 *
 * @since 2.0
 */
public interface EventListenerProvider
{
    /**
     * Returns an {@code EventListenerList} object with information about event
     * listener registrations. All listeners contained in this object are added
     * to the processing {@code ConfigurationBuilder}.
     *
     * @return the {@code EventListenerList} with event listener registrations
     *         (must not be <b>null</b>)
     */
    EventListenerList getListeners();
}
