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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration2.AbstractHierarchicalConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;

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
public abstract class AbstractFlatConfiguration extends AbstractHierarchicalConfiguration<FlatNode>
{
    /** Constant for the default property delimiter. */
    private static final String DEF_PROPERTY_DELIMITER = "|";

    /** Stores the root node of this configuration. */
    private FlatNode rootNode;

    /** A lock for protecting the root node. */
    private Lock lockRoot;

    /**
     * Creates a new instance of <code>AbstractFlatConfiguration</code>.
     */
    protected AbstractFlatConfiguration()
    {
        super(null);
        lockRoot = new ReentrantLock();
        initNodeHandler();
        initExpressionEngine();
        registerChangeListener();
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
    @Override
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
    @Override
    public int getMaxIndex(String key)
    {
        Object value = getProperty(key);

        if (value == null)
        {
            return -1;
        }
        else if (value instanceof Collection<?>)
        {
            return ((Collection<?>) value).size() - 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * An alternative implementation of {@code setProperty()}. We cannot use the
     * implementation inherited from {@link AbstractHierarchicalConfiguration}
     * because it makes use of the configuration's node structure. This
     * implementation delegates to the default implementation in {@code
     * AbstractConfiguration}.
     *
     * @param key the property key
     * @param value the new value
     */
    @Override
    public void setProperty(String key, Object value)
    {
        doSetProperty(key, value);
    }

    /**
     * An alternative implementation of {@code getKeys(String)}. We cannot use
     * the implementation inherited from
     * {@link AbstractHierarchicalConfiguration} because it makes use of the
     * configuration's node structure. This implementation delegates to the
     * default implementation in {@code AbstractConfiguration}.
     *
     * @param prefix the prefix for the keys to be returned
     * @return an iterator with all the keys starting with the given prefix
     */
    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return doGetKeys(prefix);
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
        FlatRootNode root = new FlatRootNode(this);
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
     * Initializes the node handler of this configuration.
     */
    private void initNodeHandler()
    {
        setNodeHandler(new FlatNodeHandler());
    }

    /**
     * Initializes the expression engine of this configuration. Per default an
     * expression engine is used with a special property delimiter that does not
     * interfere with the dot character (which frequently appears in
     * configuration keys).
     */
    private void initExpressionEngine()
    {
        DefaultExpressionEngine expr = new DefaultExpressionEngine();
        expr.setPropertyDelimiter(DEF_PROPERTY_DELIMITER);
        expr.setEscapedDelimiter(DEF_PROPERTY_DELIMITER + DEF_PROPERTY_DELIMITER);
        setExpressionEngine(expr);
    }

    /**
     * Returns a flag whether the latest change on this configuration was caused
     * by a manipulation of the associated flat node structure. If this is the
     * case, the node structure need not be invalidated.
     *
     * @return the internal update flag
     */
    private boolean isInternalUpdate()
    {
        return rootNode != null && rootNode.isInternalUpdate();
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
                    if (!isInternalUpdate())
                    {
                        invalidateRootNode();
                    }
                }
            }
        });
    }
}
