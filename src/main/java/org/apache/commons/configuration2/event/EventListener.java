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

/**
 * <p>
 * Definition of a generic event listener interface.
 * </p>
 * <p>
 * This event listener interface is used throughout the
 * <em>Commons Configuration</em> library for reacting on all kinds of supported
 * events. The interface is pretty minimalistic, defining only a single
 * {@code onEvent()} method. This simplifies the implementation of custom event
 * listeners and also supports the new language features introduced with Java 8
 * ({@code EventListener} is a functional interface and thus can be represented
 * by a Lambda expression).
 * </p>
 *
 * @since 2.0
 * @param <T> the type of events this listener can process
 */
public interface EventListener<T extends Event>
{
    /**
     * Notifies this event listener about the arrival of a new event. Typically,
     * event listeners are registered at an event source providing an
     * {@link EventType}. This event type acts as a filter; all events matched
     * by the filter are passed to the listener. The type parameters defined by
     * the {@code EventType} class and this interface guarantee that the events
     * delivered to the handler are compatible with the concrete method
     * signature of {@code onEvent()}.
     *
     * @param event the event
     */
    void onEvent(T event);
}
