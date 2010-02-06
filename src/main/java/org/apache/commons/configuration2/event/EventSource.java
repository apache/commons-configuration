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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 * differ for different configuration implementations. Hierarchical configurations
 * for instance have a custom implementation of <code>setProperty()</code>,
 * which does not generate any detail events.
 * </p>
 * <p>
 * In addition to &quot;normal&quot; events, error events are supported. Such
 * events signal an internal problem that occurred during access of properties.
 * For them a special listener interface exists:
 * <code>{@link ConfigurationErrorListener}</code>. There is another set of
 * methods dealing with event listeners of this type. The
 * <code>fireError()</code> method can be used by derived classes to send
 * notifications about errors to registered observers.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public class EventSource
{
    /** A collection for the registered event listeners. */
    private Collection<ConfigurationListener> listeners;

    /** A collection for the registered error listeners.*/
    private Collection<ConfigurationErrorListener> errorListeners;

    /** A counter for the detail events. */
    private int detailEvents;

    /**
     * Creates a new instance of <code>EventSource</code>.
     */
    public EventSource()
    {
        initListeners();
    }

    /**
     * Adds a configuration listener to this object.
     *
     * @param l the listener to add
     */
    public void addConfigurationListener(ConfigurationListener l)
    {
        doAddListener(listeners, l);
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
        return doRemoveListener(listeners, l);
    }

    /**
     * Returns a collection with all configuration event listeners that are
     * currently registered at this object.
     *
     * @return a collection with the registered
     * <code>ConfigurationListener</code>s (this collection is a snapshot
     * of the currently registered listeners; manipulating it has no effect
     * on this event source object)
     */
    public Collection<ConfigurationListener> getConfigurationListeners()
    {
        return doGetListeners(listeners);
    }

    /**
     * Removes all registered configuration listeners.
     */
    public void clearConfigurationListeners()
    {
        doClearListeners(listeners);
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
     * Adds a new configuration error listener to this object. This listener
     * will then be notified about internal problems.
     *
     * @param l the listener to register (must not be <b>null</b>)
     * @since 1.4
     */
    public void addErrorListener(ConfigurationErrorListener l)
    {
        doAddListener(errorListeners, l);
    }

    /**
     * Removes the specified error listener so that it does not receive any
     * further events caused by this object.
     *
     * @param l the listener to remove
     * @return a flag whether the listener could be found and removed
     * @since 1.4
     */
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return doRemoveListener(errorListeners, l);
    }

    /**
     * Removes all registered error listeners.
     *
     * @since 1.4
     */
    public void clearErrorListeners()
    {
        doClearListeners(errorListeners);
    }

    /**
     * Returns a collection with all configuration error listeners that are
     * currently registered at this object.
     *
     * @return a collection with the registered
     * <code>ConfigurationErrorListener</code>s (this collection is a
     * snapshot of the currently registered listeners; it cannot be manipulated)
     * @since 1.4
     */
    public Collection<ConfigurationErrorListener> getErrorListeners()
    {
        return doGetListeners(errorListeners);
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
    protected void fireEvent(int type, String propName, Object propValue, boolean before)
    {
        Collection<ConfigurationListener> listenersToCall = null;

        synchronized (listeners)
        {
            if (detailEvents >= 0 && listeners.size() > 0)
            {
                // Copy listeners to another collection so that manipulating
                // the listener list during event delivery won't cause problems
                listenersToCall = new ArrayList<ConfigurationListener>(listeners);
            }
        }

        if (listenersToCall != null)
        {
            ConfigurationEvent event = createEvent(type, propName, propValue, before);
            for (ConfigurationListener listener : listenersToCall)
            {
                listener.configurationChanged(event);
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
     * @param ex the <code>Throwable</code> object that caused this error event
     * @since 1.4
     */
    protected void fireError(int type, String propName, Object propValue, Throwable ex)
    {
        Collection<ConfigurationErrorListener> listenersToCall = null;

        synchronized (errorListeners)
        {
            if (errorListeners.size() > 0)
            {
                // Copy listeners to another collection so that manipulating
                // the listener list during event delivery won't cause problems
                listenersToCall = new ArrayList<ConfigurationErrorListener>(errorListeners);
            }
        }

        if (listenersToCall != null)
        {
            ConfigurationErrorEvent event = createErrorEvent(type, propName, propValue, ex);
            for (ConfigurationErrorListener listener : listenersToCall)
            {
                listener.configurationError(event);
            }
        }
    }

    /**
     * Creates a <code>ConfigurationErrorEvent</code> object based on the
     * passed in parameters. This is called by <code>fireError()</code> if it
     * decides that an event needs to be generated.
     *
     * @param type the event's type
     * @param propName the name of the affected property (can be <b>null</b>)
     * @param propValue the value of the affected property (can be <b>null</b>)
     * @param ex the <code>Throwable</code> object that caused this error
     * event
     * @return the event object
     * @since 1.4
     */
    protected ConfigurationErrorEvent createErrorEvent(int type, String propName, Object propValue, Throwable ex)
    {
        return new ConfigurationErrorEvent(this, type, propName, propValue, ex);
    }

    /**
     * Overrides the <code>clone()</code> method to correctly handle so far
     * registered event listeners. This implementation ensures that the clone
     * will have empty event listener lists, i.e. the listeners registered at an
     * <code>EventSource</code> object will not be copied.
     *
     * @return the cloned object
     * @throws CloneNotSupportedException if cloning is not allowed
     * @since 1.4
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        EventSource copy = (EventSource) super.clone();
        copy.initListeners();
        return copy;
    }

    /**
     * Adds a new listener object to a listener collection. This is done in a
     * synchronized block. The listener must not be <b>null</b>.
     *
     * @param <T> the type of listener to be added
     * @param listeners the collection with the listeners
     * @param l the listener object
     */
    private static <T> void doAddListener(Collection<T> listeners, T l)
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
     * Removes an event listener from a listener collection. This is done in a
     * synchronized block.
     *
     * @param <T> the type of listener to be removed
     * @param listeners the collection with the listeners
     * @param l the listener object
     * @return a flag whether the listener could be found and removed
     */
    private static <T> boolean doRemoveListener(Collection<T> listeners, T l)
    {
        synchronized (listeners)
        {
            return listeners.remove(l);
        }
    }

    /**
     * Removes all entries from the given list of event listeners.
     *
     * @param listeners the collection with the listeners
     */
    private static void doClearListeners(Collection<?> listeners)
    {
        synchronized (listeners)
        {
            listeners.clear();
        }
    }

    /**
     * Returns an unmodifiable snapshot of the given event listener collection.
     *
     * @param <T> the type of the collection with the listeners
     * @param listeners the collection with the listeners
     * @return a snapshot of the listeners collection
     */
    private static <T> Collection<T> doGetListeners(Collection<T> listeners)
    {
        synchronized (listeners)
        {
            return Collections.unmodifiableCollection(new ArrayList<T>(
                    listeners));
        }
    }

    /**
     * Initializes the collections for storing registered event listeners.
     */
    private void initListeners()
    {
        listeners = new LinkedList<ConfigurationListener>();
        errorListeners = new LinkedList<ConfigurationErrorListener>();
    }
}
