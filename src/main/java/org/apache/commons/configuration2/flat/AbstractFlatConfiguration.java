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
package org.apache.commons.configuration2.flat;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A base class for <code>Configuration</code> implementations that are not
 * hierarchical.
 * </p>
 * <p>
 * Not all configuration classes store their data in a hierarchical manner. This
 * class can serve as a base class for those non-hierarchical configurations. It
 * does not make any assumptions about the concrete way of storing data. This is
 * up to a concrete sub class, which has to implement the access methods like
 * <code>getProperty()</code>, <code>setProperty()</code>, or
 * <code>addPropertyDirect()</code> accordingly.
 * </p>
 * <p>
 * What this class does is to implement a pseudo hierarchical structure on top
 * of the data contained in the configuration. Whenever the root node of this
 * configuration is requested, a hierarchy of nodes corresponding to the actual
 * data is constructed on the fly. This node structure can then be used for
 * performing queries or integrating this configuration into a combined
 * configuration.
 * </p>
 * <p>
 * Internally, this class uses some tricks to keep the node structure up to date
 * and to avoid unnecessary operations on these nodes: It registers itself as a
 * configuration listener at itself to get notified whenever the configuration
 * is changed. When a change is detected, the node structure is invalidated, so
 * that it has to be re-created when it is accessed the next time. On the other
 * hand, this node structure is only created when it is needed, i.e. if truly
 * hierarchical operations are to be performed. This means that as long as the
 * configuration is only accessed and manipulated through the typical access
 * methods, no additional overhead is created.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public abstract class AbstractFlatConfiguration extends AbstractConfiguration
{
    /**
     * Constant for the property change event. This event is triggered by
     * <code>setPropertyValue()</code> and <code>clearPropertyValue()</code>,
     * which manipulate a value of a property with multiple values.
     */
    public static final int EVENT_PROPERTY_CHANGED = 9;

    /** Stores the <code>NodeHandler</code> used by this configuration. */
    private FlatNodeHandler nodeHandler;

    /** Stores the root node of this configuration. */
    private FlatNode rootNode;

    /** A lock for protecting the root node. */
    private Lock lockRoot;

    /**
     * Creates a new instance of <code>AbstractFlatConfiguration</code>.
     */
    protected AbstractFlatConfiguration()
    {
        lockRoot = new ReentrantLock();
        initNodeHandler();
        registerChangeListener();
    }

    /**
     * Returns the <code>NodeHandler</code> used by this configuration. This
     * is a handler that can deal with flat nodes.
     *
     * @return the <code>NodeHandler</code> used
     */
    public NodeHandler<FlatNode> getNodeHandler()
    {
        return nodeHandler;
    }

    /**
     * Modifies a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used to alter a specific
     * value (identified by its 0-based index) without affecting the other
     * values. If the index is invalid (i.e. less than 0 or greater than the
     * number of existing values), the value will be added to the existing
     * values of this property. Note that this method expects a scalar value
     * rather than an array or a collection. In the latter case the behavior is
     * undefined and may vary for different derived classes. This method takes
     * care of firing the appropriate events and delegates to
     * <code>setPropertyValueDirect()</code>. It generates a
     * <code>EVENT_PROPERTY_CHANGED</code> event that contains the key of the
     * affected property.
     *
     * @param key the key of the property
     * @param index the index of the value to change
     * @param value the new value; this should be a simple object; arrays or
     *        collections won't be treated specially, but directly added
     */
    public void setPropertyValue(String key, int index, Object value)
    {
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, true);
        setDetailEvents(true);
        try
        {
            setPropertyValueDirect(key, index, value);
        }
        finally
        {
            setDetailEvents(false);
        }
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, false);
    }

    /**
     * Removes a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used for removing a
     * single value (identified by its 0-based index). If the index is out of
     * range, no action is performed; in this case <b>false</b> is returned.
     * This method takes care of firing the appropriate events and delegates to
     * <code>clearPropertyValueDirect()</code>. It generates a
     * <code>EVENT_PROPERTY_CHANGED</code> event that contains the key of the
     * affected property.
     *
     * @param key the key of the property
     * @param index the index of the value to delete
     * @return a flag whether the value could be removed
     */
    public boolean clearPropertyValue(String key, int index)
    {
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, true);

        boolean result = false;
        setDetailEvents(true);
        try
        {
            result = clearPropertyValueDirect(key, index);
        }
        finally
        {
            setDetailEvents(false);
        }

        fireEvent(EVENT_PROPERTY_CHANGED, key, null, false);
        return result;
    }

    /**
     * Returns the root node of this configuration. A node hierarchy for this
     * configuration (consisting of <code>FlatNode</code> objects) is created
     * on demand. This method returns the root node of this hierarchy. It checks
     * whether a valid root node exists. If this is not the case,
     * <code>constructNodeHierarchy()</code> is called to setup the node
     * structure. Modifications on this configuration that cause the node
     * hierarchy to get out of sync with the data stored in this configuration
     * cause the root node to be invalidated. It will then be re-created on next
     * access. <em>Implementation note:</em> This method is thread-safe; it
     * can be invoked concurrently by multiple threads.
     *
     * @return the root node of this configuration
     */
    public FlatNode getRootNode()
    {
        lockRoot.lock();
        try
        {
            if (rootNode == null)
            {
                rootNode = constructNodeHierarchy();
            }
            return rootNode;
        }
        finally
        {
            lockRoot.unlock();
        }
    }

    /**
     * Creates a copy of this instance. This implementation ensures that a
     * change listener for invalidating the node structure is registered at the
     * copy.
     *
     * @return a copy of this object
     * @throws CloneNotSupportedException if cloning is not supported
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        AbstractFlatConfiguration copy = (AbstractFlatConfiguration) super
                .clone();
        copy.initNodeHandler();
        copy.clearConfigurationListeners();
        copy.registerChangeListener();
        return copy;
    }

    /**
     * Returns the maximum index of the property with the given key. This method
     * can be used to find out how many values are stored for a given property.
     * A return value of -1 means that this property is unknown. A value of 0
     * indicates that there is a single value, 1 means there are two values,
     * etc. This base implementation uses {@code getProperty()} to obtain the
     * property value(s). Derived classes should override this method if they
     * can provide a more efficient implementation.
     *
     * @param key the key of the property
     * @return the maximum index of a value of this property
     */
    public int getMaxIndex(String key)
    {
        Object value = getProperty(key);

        if (value == null)
        {
            return -1;
        }
        else if (value instanceof Collection)
        {
            return ((Collection<?>) value).size() - 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Creates a hierarchy of <code>FlatNode</code> objects that corresponds
     * to the data stored in this configuration. This implementation relies on
     * the methods <code>getKeys()</code> and <code>getMaxIndex()</code> to
     * obtain the data required for constructing the node hierarchy.
     *
     * @return the root node of this hierarchy
     */
    protected FlatNode constructNodeHierarchy()
    {
        FlatRootNode root = new FlatRootNode();
        for (Iterator<String> it = getKeys(); it.hasNext();)
        {
            String key = it.next();
            int maxIndex = getMaxIndex(key);
            for (int i = 0; i <= maxIndex; i++)
            {
                root.addChild(key, true);
            }
        }

        return root;
    }

    /**
     * Invalidates the root node of this configuration's node structure. This
     * method is called when this configuration instance is updated, so that its
     * data is out of sync with the node structure. It causes the root node and
     * the whole hierarchy to be re-created when it is accessed for the next
     * time.
     */
    protected void invalidateRootNode()
    {
        lockRoot.lock();
        try
        {
            rootNode = null;
        }
        finally
        {
            lockRoot.unlock();
        }
    }

    /**
     * Performs the actual modification of the specified property value. This
     * method is called by <code>setPropertyValue()</code>. The base
     * implementation provided by this class uses {@code getProperty()} for
     * obtaining the current value(s) of the specified property. If the property
     * does not exist or the index is invalid, the value is added as if
     * <code>addProperty()</code> was called. If the property has a single
     * value, the passed in index determines what happens: if it is 0, the
     * single value is replaced by the new one; all other indices cause the new
     * value to be added to the old one. The method delegates to {@code
     * setProperty()} or {@code addPropertyDirect()} for storing the new
     * property value. Derived classes should override it if they can provide a
     * more efficient implementation.
     *
     * @param key the key of the property
     * @param index the index of the value to change
     * @param value the new value
     */
    protected void setPropertyValueDirect(String key, int index, Object value)
    {
        Object oldValue = getProperty(key);

        if (oldValue instanceof List)
        {
            @SuppressWarnings("unchecked")
            List<Object> col = (List<Object>) oldValue;
            if (index >= 0 && index < col.size())
            {
                col.set(index, value);
                setProperty(key, col);
            }
            else
            {
                addPropertyDirect(key, value);
            }
        }

        else if (oldValue == null || index != 0)
        {
            addPropertyDirect(key, value);
        }
        else
        {
            setProperty(key, value);
        }
    }

    /**
     * Initializes the node handler of this configuration.
     */
    private void initNodeHandler()
    {
        nodeHandler = new FlatNodeHandler(this);
    }

    /**
     * Performs the actual remove property value operation. This method is
     * called by <code>clearPropertyValue()</code>. The base implementation
     * provided by this class uses {@code getProperty()} for obtaining the
     * value(s) of the property. If there are multiple values and the index is
     * in the allowed range, the value with the given index is removed, and the
     * new values are stored using {@code setProperty()}. Derived classes should
     * override this method if they can provide a more efficient implementation.
     *
     * @param key the key of the property
     * @param index the index of the value to delete
     * @return a flag whether the value could be removed
     */
    protected boolean clearPropertyValueDirect(String key, int index)
    {
        Object value = getProperty(key);

        if (value instanceof List && index >= 0)
        {
            List<?> col = (List<?>) value;
            if (index < col.size())
            {
                col.remove(index);
                setProperty(key, col);
                return true;
            }
        }

        return false;
    }

    /**
     * Registers a change listener at this configuration that causes the node
     * structure to be invalidated whenever the content is changed.
     */
    private void registerChangeListener()
    {
        addConfigurationListener(new ConfigurationListener()
        {
            /**
             * Reacts on change events. Asks the node handler whether the
             * received event was caused by an update of the node structure. If
             * not, the structure has to be invalidated.
             */
            public void configurationChanged(ConfigurationEvent event)
            {
                if (!event.isBeforeUpdate())
                {
                    if (!((FlatNodeHandler) getNodeHandler()).isInternalUpdate())
                    {
                        invalidateRootNode();
                    }
                }
            }
        });
    }
}
