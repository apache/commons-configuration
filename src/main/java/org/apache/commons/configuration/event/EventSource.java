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

/**
 * <p>
 * An interface for configuration implementations which support registration of
 * event listeners.
 * </p>
 * <p>
 * Through the methods provided by this interface it is possible to register and
 * remove event listeners for configuration events. Both configuration change
 * listeners and error listeners are supported.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface EventSource
{
    /**
     * Adds a new configuration error listener to this object. This listener
     * will then be notified about internal problems.
     *
     * @param l the listener to register (must not be <b>null</b>)
     * @since 1.4
     * @deprecated Use {@code addEventListener()}
     */
    @Deprecated
    void addErrorListener(ConfigurationErrorListener l);

    /**
     * Removes the specified error listener so that it does not receive any
     * further events caused by this object.
     *
     * @param l the listener to remove
     * @return a flag whether the listener could be found and removed
     * @since 1.4
     * @deprecated Use {@code removeEventListener()}
     */
    @Deprecated
    boolean removeErrorListener(ConfigurationErrorListener l);

    /**
     * Adds an event listener for the specified event type. This listener is
     * notified about events of this type and all its sub types.
     *
     * @param eventType the event type (must not be <b>null</b>)
     * @param listener the listener to be registered (must not be <b>null</b>)
     * @param <T> the type of events processed by this listener
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    <T extends Event> void addEventListener(EventType<T> eventType,
            EventListener<? super T> listener);

    /**
     * Removes the event listener registration for the given event type and
     * listener. An event listener instance may be registered multiple times for
     * different event types. Therefore, when removing a listener the event type
     * of the registration in question has to be specified. The return value
     * indicates whether a registration was removed. A value of <b>false</b>
     * means that no such combination of event type and listener was found.
     *
     * @param eventType the event type
     * @param listener the event listener to be removed
     * @return a flag whether a listener registration was removed
     */
    <T extends Event> boolean removeEventListener(EventType<T> eventType,
            EventListener<? super T> listener);
}
