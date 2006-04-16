/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.LinkedList;

/**
 * <p>
 * A base class for objects that can generate configuration events.
 * </p>
 * <p>
 * This class implements functionality for managing a set of event listeners
 * that can be notified when an event occurs. It can be extended by
 * configuration classes that support the event machanism. In this case these
 * classes only need to call the <code>fireEvent()</code> method when an event
 * is to be delivered to the registered listeners.
 * </p>
 * <p>
 * Adding and removing event listeners can happen concurrently to manipulations
 * on a configuration that cause events. The operations are synchronized.
 * </p>
 * <p>
 * With the <code>detailEvents</code> property the number of detail events can
 * be controlled. Some methods in configuration classes are implemented in a way
 * that they call other methods that can generate their own events. One example
 * is the <code>setProperty()</code> method that can be implemented as a
 * combination of <code>clearProperty()</code> and <code>addProperty()</code>.
 * With <code>detailEvents</code> set to <b>true</b>, all involved methods
 * will generate events (i.e. listeners will receive property set events,
 * property clear events, and property add events). If this mode is turned off
 * (which is the default), detail events are suppressed, so only property set
 * events will be received. Note that the number of received detail events may
 * differ for different configuration implementations.
 * <code>{@link org.apache.commons.configuration.HierarchicalConfiguration HierarchicalConfiguration}</code>
 * for instance has a custom implementation of <code>setProperty()</code>, which
 * does not generate any detail events.
 * </p>
 *
 * @version $Id$
 * @since 1.3
 */
public class EventSource
{
    /** A collection for the registered event listeners. */
    private Collection listeners;

    /** A counter for the detail events. */
    private int detailEvents;

    /**
     * Creates a new instance of <code>EventSource</code>.
     */
    public EventSource()
    {
        listeners = new LinkedList();
    }

    /**
     * Adds a configuration listener to this object.
     *
     * @param l the listener to add
     */
    public void addConfigurationListener(ConfigurationListener l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException("Listener must not be null!");
        }
        synchronized (listeners)
        {
            listeners.add(l);
        }
    }

    /**
     * Removes the specified event listener so that it does not receive any
     * further events caused by this object.
     *
     * @param l the listener to be removed
     * @return a flag whether the event listener was found
     */
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        synchronized (listeners)
        {
            return listeners.remove(l);
        }
    }

    /**
     * Returns a collection with all configuration event listeners that are
     * currently registered at this object.
     *
     * @return a collection with the registered
     * <code>ConfigurationListener</code>s (this collection cannot be
     * changed)
     */
    public Collection getConfigurationListeners()
    {
        synchronized (listeners)
        {
            return Collections.unmodifiableCollection(listeners);
        }
    }

    /**
     * Returns a flag whether detail events are enabled.
     *
     * @return a flag if detail events are generated
     */
    public boolean isDetailEvents()
    {
        synchronized (listeners)
        {
            return detailEvents > 0;
        }
    }

    /**
     * Determines whether detail events should be generated. If enabled, some
     * methods can generate multiple update events. Note that this method
     * records the number of calls, i.e. if for instance
     * <code>setDetailEvents(false)</code> was called three times, you will
     * have to invoke the method as often to enable the details.
     *
     * @param enable a flag if detail events should be enabled or disabled
     */
    public void setDetailEvents(boolean enable)
    {
        synchronized (listeners)
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

    /**
     * Creates an event object and delivers it to all registered event
     * listeners. The method will check first if sending an event is allowed
     * (making use of the <code>detailEvents</code> property), and if
     * listeners are registered.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     */
    protected void fireEvent(int type, String propName, Object propValue,
            boolean before)
    {
        Collection listenersToCall = null;

        synchronized (listeners)
        {
            if (detailEvents >= 0 && listeners.size() > 0)
            {
                // Copy listeners to another collection so that manipulating
                // the listener list during event delivery won't cause problems
                listenersToCall = new ArrayList(listeners);
            }
        }

        if (listenersToCall != null)
        {
            ConfigurationEvent event = createEvent(type, propName, propValue,
                    before);
            for (Iterator it = listenersToCall.iterator(); it.hasNext();)
            {
                ((ConfigurationListener) it.next()).configurationChanged(event);
            }
        }
    }

    /**
     * Creates a <code>ConfigurationEvent</code> object based on the passed in
     * parameters. This is called by <code>fireEvent()</code> if it decides
     * that an event needs to be generated.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param before the before update flag
     * @return the newly created event object
     */
    protected ConfigurationEvent createEvent(int type, String propName,
            Object propValue, boolean before)
    {
        return new ConfigurationEvent(this, type, propName, propValue, before);
    }
}
