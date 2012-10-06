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
package org.apache.commons.configuration.reloading;

import org.apache.commons.lang3.event.EventListenerSupport;

/**
 * <p>
 * A class for adding support for reload operations in a generic way.
 * </p>
 * <p>
 * A {@code ReloadingController} monitors a specific source and triggers
 * reloading events if necessary. So it does not perform reloading itself, but
 * only sends out notifications when it thinks that this should be done. This
 * allows for a very generic setup in which different components involved in
 * reloading are loosely coupled via events.
 * </p>
 * <p>
 * A typical usage scenario is as follows:
 * <ul>
 * <li>A {@code ReloadingController} instance is created and initialized with a
 * {@link ReloadingDetector} object.</li>
 * <li>A number of {@link ReloadingListener} objects can be registered at the
 * controller.</li>
 * <li>Now the controller's {@code checkForReloading()} method is called
 * whenever a check is to be performed. This could be done for instance by a
 * timer in regular intervals or by any other means appropriate for a specific
 * application.</li>
 * <li>When a check reveals that a reload operation is necessary all registered
 * event listeners are notified.</li>
 * <li>Typically one of the listeners is responsible to perform the actual
 * reload operation. (How this is done is not in the scope of the controller
 * object.) After this has been done, the controller's
 * {@code resetReloadingState()} method must be called. It tells the controller
 * that the last notification has been processed and that new checks are
 * possible again. It is important that this method is called. Otherwise,
 * {@code checkForReloading()} will not do any new checks or send out event
 * notifications any more.</li>
 * </ul>
 * </p>
 * <p>
 * This class can be accessed from multiple threads concurrently. It shields the
 * associated {@link ReloadingDetector} object for concurrent access, so that a
 * concrete detector implementation does not have to be thread-safe.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class ReloadingController
{
    /** Stores a reference to the reloading detector. */
    private final ReloadingDetector detector;

    /** The helper object which manages the registered event listeners. */
    private final EventListenerSupport<ReloadingListener> listeners;

    /** A flag whether this controller is in reloading state. */
    private boolean reloadingState;

    /**
     * Creates a new instance of {@code ReloadingController} and associates it
     * with the given {@code ReloadingDetector} object.
     *
     * @param detect the {@code ReloadingDetector} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the detector is undefined
     */
    public ReloadingController(ReloadingDetector detect)
    {
        if (detect == null)
        {
            throw new IllegalArgumentException(
                    "ReloadingDetector must not be null!");
        }

        detector = detect;
        listeners = EventListenerSupport.create(ReloadingListener.class);
    }

    /**
     * Returns the {@code ReloadingDetector} used by this controller.
     *
     * @return the {@code ReloadingDetector}
     */
    public ReloadingDetector getDetector()
    {
        return detector;
    }

    /**
     * Adds the specified {@code ReloadingListener} to this controller. It will
     * receive notifications whenever a reload operation is necessary.
     *
     * @param l the listener to be added (must not be <b>null</b>)
     */
    public void addReloadingListener(ReloadingListener l)
    {
        listeners.addListener(l);
    }

    /**
     * Removes the specified {@code ReloadingListener} from this controller.
     *
     * @param l
     */
    public void removeReloadingListener(ReloadingListener l)
    {
        listeners.removeListener(l);
    }

    /**
     * Tests whether this controller is in <em>reloading state</em>. A return
     * value of <b>true</b> means that a previous invocation of
     * {@code checkForReloading()} has detected the necessity for a reload
     * operation, but {@code resetReloadingState()} has not been called yet. In
     * this state no further reloading checks are possible.
     *
     * @return a flag whether this controller is in reloading state
     */
    public synchronized boolean isInReloadingState()
    {
        return reloadingState;
    }

    /**
     * Performs a check whether a reload operation is necessary. This method has
     * to be called to trigger the generation of reloading events. It delegates
     * to the associated {@link ReloadingDetector} and sends out notifications
     * if necessary. The argument can be an arbitrary data object; it will be
     * part of the event notification sent out when a reload operation should be
     * performed.
     *
     * @param data additional data for an event notification
     */
    public void checkForReloading(Object data)
    {
        boolean sendEvent = false;
        synchronized (this)
        {
            if (!isInReloadingState())
            {
                if (getDetector().isReloadingRequired())
                {
                    sendEvent = true;
                    reloadingState = true;
                }
            }
        }

        if (sendEvent)
        {
            listeners.fire().reloadingRequired(new ReloadingEvent(this, data));
        }
    }

    /**
     * Resets the reloading state. This tells the controller that reloading has
     * been performed and new checks are possible again. If this controller is
     * not in reloading state, this method has no effect.
     */
    public synchronized void resetReloadingState()
    {
        if (isInReloadingState())
        {
            getDetector().reloadingPerformed();
            reloadingState = false;
        }
    }
}
