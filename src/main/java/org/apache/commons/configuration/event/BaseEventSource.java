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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * A base class for objects that can generate configuration events.
 * </p>
 * <p>
 * This class implements functionality for managing a set of event listeners
 * that can be notified when an event occurs. It can be extended by
 * configuration classes that support the event mechanism. In this case these
 * classes only need to call the {@code fireEvent()} method when an event
 * is to be delivered to the registered listeners.
 * </p>
 * <p>
 * Adding and removing event listeners can happen concurrently to manipulations
 * on a configuration that cause events. The operations are synchronized.
 * </p>
 * <p>
 * With the {@code detailEvents} property the number of detail events can
 * be controlled. Some methods in configuration classes are implemented in a way
 * that they call other methods that can generate their own events. One example
 * is the {@code setProperty()} method that can be implemented as a
 * combination of {@code clearProperty()} and {@code addProperty()}.
 * With {@code detailEvents} set to <b>true</b>, all involved methods
 * will generate events (i.e. listeners will receive property set events,
 * property clear events, and property add events). If this mode is turned off
 * (which is the default), detail events are suppressed, so only property set
 * events will be received. Note that the number of received detail events may
 * differ for different configuration implementations.
 * {@link org.apache.commons.configuration.HierarchicalConfiguration HierarchicalConfiguration}
 * for instance has a custom implementation of {@code setProperty()},
 * which does not generate any detail events.
 * </p>
 * <p>
 * In addition to &quot;normal&quot; events, error events are supported. Such
 * events signal an internal problem that occurred during access of properties.
 * For them a special listener interface exists:
 * {@link ConfigurationErrorListener}. There is another set of
 * methods dealing with event listeners of this type. The
 * {@code fireError()} method can be used by derived classes to send
 * notifications about errors to registered observers.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public class BaseEventSource implements EventSource
{
    /** A collection for the registered event listeners. */
    private Collection<ConfigurationListener> listeners;

    /** A collection for the registered error listeners.*/
    private Collection<ConfigurationErrorListener> errorListeners;

    /** A lock object for guarding access to the detail events counter. */
    private final Object lockDetailEventsCount = new Object();

    /** A counter for the detail events. */
    private int detailEvents;

    /**
     * Creates a new instance of {@code BaseEventSource}.
     */
    public BaseEventSource()
    {
        initListeners();
    }

    public void addConfigurationListener(ConfigurationListener l)
    {
        checkListener(l);
        listeners.add(l);
    }

    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return listeners.remove(l);
    }

    /**
     * Returns a collection with all configuration event listeners that are
     * currently registered at this object.
     *
     * @return a collection with the registered
     * {@code ConfigurationListener}s (this collection is a snapshot
     * of the currently registered listeners; manipulating it has no effect
     * on this event source object)
     */
    public Collection<ConfigurationListener> getConfigurationListeners()
    {
        return Collections.unmodifiableCollection(new ArrayList<ConfigurationListener>(listeners));
    }

    /**
     * Removes all registered configuration listeners.
     */
    public void clearConfigurationListeners()
    {
        listeners.clear();
    }

    /**
     * Returns a flag whether detail events are enabled.
     *
     * @return a flag if detail events are generated
     */
    public boolean isDetailEvents()
    {
        return checkDetailEvents(0);
    }

    /**
     * Determines whether detail events should be generated. If enabled, some
     * methods can generate multiple update events. Note that this method
     * records the number of calls, i.e. if for instance
     * {@code setDetailEvents(false)} was called three times, you will
     * have to invoke the method as often to enable the details.
     *
     * @param enable a flag if detail events should be enabled or disabled
     */
    public void setDetailEvents(boolean enable)
    {
        synchronized (lockDetailEventsCount)
        {
            if (enable)
            {
                detailEvents++;
            }
            else
            {
                detailEvents--;
            }
        }
    }

    public void addErrorListener(ConfigurationErrorListener l)
    {
        checkListener(l);
        errorListeners.add(l);
    }

    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return errorListeners.remove(l);
    }

    /**
     * Removes all registered error listeners.
     *
     * @since 1.4
     */
    public void clearErrorListeners()
    {
        errorListeners.clear();
    }

    /**
     * Returns a collection with all configuration error listeners that are
     * currently registered at this object.
     *
     * @return a collection with the registered
     * {@code ConfigurationErrorListener}s (this collection is a
     * snapshot of the currently registered listeners; it cannot be manipulated)
     * @since 1.4
     */
    public Collection<ConfigurationErrorListener> getErrorListeners()
    {
        return Collections.unmodifiableCollection(new ArrayList<ConfigurationErrorListener>(errorListeners));
    }

    /**
     * Creates an event object and delivers it to all registered event
     * listeners. The method will check first if sending an event is allowed
     * (making use of the {@code detailEvents} property), and if
     * listeners are registered.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     */
    protected void fireEvent(int type, String propName, Object propValue, boolean before)
    {
        if (checkDetailEvents(-1))
        {
            Iterator<ConfigurationListener> it = listeners.iterator();
            if (it.hasNext())
            {
                ConfigurationEvent event =
                        createEvent(type, propName, propValue, before);
                while (it.hasNext())
                {
                    it.next().configurationChanged(event);
                }
            }
        }
    }

    /**
     * Creates a {@code ConfigurationEvent} object based on the passed in
     * parameters. This is called by {@code fireEvent()} if it decides
     * that an event needs to be generated.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     * @return the newly created event object
     */
    protected ConfigurationEvent createEvent(int type, String propName, Object propValue, boolean before)
    {
        return new ConfigurationEvent(this, type, propName, propValue, before);
    }

    /**
     * Creates an error event object and delivers it to all registered error
     * listeners.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param ex the {@code Throwable} object that caused this error event
     * @since 1.4
     */
    protected void fireError(int type, String propName, Object propValue, Throwable ex)
    {
        Iterator<ConfigurationErrorListener> it = errorListeners.iterator();
        if (it.hasNext())
        {
            ConfigurationErrorEvent event =
                    createErrorEvent(type, propName, propValue, ex);
            while (it.hasNext())
            {
                it.next().configurationError(event);
            }
        }
    }

    /**
     * Creates a {@code ConfigurationErrorEvent} object based on the
     * passed in parameters. This is called by {@code fireError()} if it
     * decides that an event needs to be generated.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param ex the {@code Throwable} object that caused this error
     * event
     * @return the event object
     * @since 1.4
     */
    protected ConfigurationErrorEvent createErrorEvent(int type, String propName, Object propValue, Throwable ex)
    {
        return new ConfigurationErrorEvent(this, type, propName, propValue, ex);
    }

    /**
     * Overrides the {@code clone()} method to correctly handle so far
     * registered event listeners. This implementation ensures that the clone
     * will have empty event listener lists, i.e. the listeners registered at an
     * {@code BaseEventSource} object will not be copied.
     *
     * @return the cloned object
     * @throws CloneNotSupportedException if cloning is not allowed
     * @since 1.4
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        BaseEventSource copy = (BaseEventSource) super.clone();
        copy.initListeners();
        return copy;
    }

    /**
     * Checks whether the specified event listener is not <b>null</b>. If this
     * is the case, an {@code IllegalArgumentException} exception is thrown.
     *
     * @param l the listener to be checked
     * @throws IllegalArgumentException if the listener is <b>null</b>
     */
    private static void checkListener(Object l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException("Listener must not be null!");
        }
    }

    /**
     * Initializes the collections for storing registered event listeners.
     */
    private void initListeners()
    {
        listeners = new CopyOnWriteArrayList<ConfigurationListener>();
        errorListeners = new CopyOnWriteArrayList<ConfigurationErrorListener>();
    }

    /**
     * Helper method for checking the current counter for detail events. This
     * method checks whether the counter is greater than the passed in limit.
     *
     * @param limit the limit to be compared to
     * @return <b>true</b> if the counter is greater than the limit,
     *         <b>false</b> otherwise
     */
    private boolean checkDetailEvents(int limit)
    {
        synchronized (lockDetailEventsCount)
        {
            return detailEvents > limit;
        }
    }
}
