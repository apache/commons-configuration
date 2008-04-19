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
package org.apache.commons.configuration2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.configuration2.expr.AbstractNodeHandler;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;

/**
 * <p>
 * A configuration implementation on top of the Java <code>Preferences</code>
 * API.
 * </p>
 * <p>
 * This implementation of the <code>Configuration</code> interface is backed
 * by a <code>java.util.prefs.Preferences</code> node. Query or update
 * operations are directly performed on this node and its descendants. When
 * creating this configuration the underlying <code>Preferences</code> node
 * can be determined:
 * <ul>
 * <li>the system root node</li>
 * <li>the user root node</li>
 * <li>a system node corresponding to a specific package</li>
 * <li>a user node corresponding to a specific package</li>
 * <li>alternatively a specific <code>Preferences</code> node can be passed to
 * a constructor, which will become the new root node.</li>
 * </ul>
 * This corresponds to the static factory methods provided by the
 * <code>Preferences</code> class. It is also possible to change this node
 * later by calling <code>setAssociatedClass()</code> or
 * <code>setSystem()</code>.
 * </p>
 * <p>
 * With this class the power provided by the <code>Configuration</code>
 * interface can be used for interacting with <code>Preferences</code> nodes.
 * Note however that some features provided by the <code>Configuration</code>
 * interface are not supported by the <code>Preferences</code> API. One
 * example of such a feature is the support for multiple values for a property:
 * If you call <code>addProperty()</code> multiple times with the same key, only
 * the last value will be stored.
 * </p>
 * <p>
 * The values stored in the underlying <code>Preferences</code> nodes can be
 * accessed per default using the dot notation that is also used by other
 * <code>Configuration</code> implementations (e.g.
 * <code>config.getString("path.to.property.name");</code>). Internally the
 * property values are mapped to <em>attribute</em> nodes in this hierarchical
 * configuration. The {@link ExpressionEngine} used by this class hides this
 * fact by defining the dot as both property delimiter and attribute marker. If
 * another expression engine is set or if this configuration is added to a
 * combined configuration, the keys have to be adapted, for instance - when
 * using the default expression engine:
 * <code>config.getString("path.to.property[@name]");</code></p>
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class PreferencesConfiguration extends
        AbstractHierarchicalConfiguration<Preferences>
{
    /** A lock for protecting the root node. */
    private Lock lockRoot;

    /** Stores the associated preferences node. */
    private Preferences root;

    /** Stores the class to be used when obtaining the root node. */
    private Class<?> associatedClass;

    /** Stores the system flag. */
    private boolean system;

    /**
     * Creates a new instance of <code>PreferencesConfiguration</code> that
     * accesses the user root node.
     */
    public PreferencesConfiguration()
    {
        this(false, null);
    }

    /**
     * Creates a new instance of <code>PreferencesConfiguration</code> that
     * accesses either the system or the user root node.
     *
     * @param system <b>true</b> for the system root node, <b>false</b> for
     *        the user root node
     */
    public PreferencesConfiguration(boolean system)
    {
        this(system, null);
    }

    /**
     * Creates a new instance of <code>PreferencesConfiguration</code> that
     * accesses the user preferences node associated with the package of the
     * specified class.
     *
     * @param c the class defining the desired package
     */
    public PreferencesConfiguration(Class<?> c)
    {
        this(false, c);
    }

    /**
     * Creates a new instance of <code>PreferencesConfiguration</code> that
     * accesses the node associated with the package of the specified class in
     * either the user or the system space.
     *
     * @param system <b>true</b> for the system root node, <b>false</b> for
     *        the user root node
     * @param c the class defining the desired package
     */
    public PreferencesConfiguration(boolean system, Class<?> c)
    {
        super(new PreferencesNodeHandler());
        lockRoot = new ReentrantLock();
        setExpressionEngine(setUpExpressionEngine());
        setAssociatedClass(c);
        setSystem(system);
    }

    /**
     * Creates a new instance of <code>PreferencesConfiguration</code> and
     * initializes it with the given node. This node will become the new root
     * node.
     *
     * @param rootNode the root node (must not be <b>null</b>)
     * @throws IllegalArgumentException if the passed in node is <b>null</b>
     */
    public PreferencesConfiguration(Preferences rootNode)
    {
        this(false, null);
        if (rootNode == null)
        {
            throw new IllegalArgumentException("Root node must be null!");
        }
        root = rootNode;
    }

    /**
     * Returns the class associated with this configuration.
     *
     * @return the associated class
     */
    public Class<?> getAssociatedClass()
    {
        return associatedClass;
    }

    /**
     * Sets the associated class. When obtaining the associated
     * <code>Preferences</code> node, this class is passed in.
     *
     * @param associatedClass the associated class
     */
    public void setAssociatedClass(Class<?> associatedClass)
    {
        this.associatedClass = associatedClass;
        clearRootNode();
    }

    /**
     * Returns the system flag. This flag determines whether system or user
     * preferences are used.
     *
     * @return the system flag
     */
    public boolean isSystem()
    {
        return system;
    }

    /**
     * Sets the system flag. This flag determines whether system or user
     * preferences are used.
     *
     * @param system the system flag
     */
    public void setSystem(boolean system)
    {
        this.system = system;
        clearRootNode();
    }

    /**
     * Returns the root node of this configuration. This is a
     * <code>Preferences</code> node, which is specified by the properties
     * <code>associatedClass</code> and <code>system</code>.
     *
     * @return the root node
     */
    @Override
    public Preferences getRootNode()
    {
        lockRoot.lock();
        try
        {
            if (root == null)
            {
                root = createRootNode();
            }
            return root;
        }
        finally
        {
            lockRoot.unlock();
        }
    }

    /**
     * Saves all changes made at this configuration. Calls <code>flush()</code>
     * on the underlying <code>Preferences</code> node. This causes the
     * preferences to be written back to the backing store.
     *
     * @throws ConfigurationRuntimeException if an error occurs
     */
    public void flush()
    {
        try
        {
            getRootNode().flush();
        }
        catch (BackingStoreException bex)
        {
            throw new ConfigurationRuntimeException(
                    "Could not flush configuration", bex);
        }
    }

    /**
     * Creates the root node of this configuration. This method has to evaluate
     * the system flag and the package to obtain the correct preferences node.
     *
     * @return the root preferences node of this configuration
     */
    protected Preferences createRootNode()
    {
        if (getAssociatedClass() != null)
        {
            return isSystem() ? Preferences
                    .systemNodeForPackage(getAssociatedClass()) : Preferences
                    .userNodeForPackage(getAssociatedClass());
        }
        else
        {
            return isSystem() ? Preferences.systemRoot() : Preferences
                    .userRoot();
        }
    }

    /**
     * Creates the expression engine to be used by this configuration. This
     * implementation returns an expression engine that uses dots for both nodes
     * and attributes.
     *
     * @return the expression engine to use
     */
    protected ExpressionEngine setUpExpressionEngine()
    {
        DefaultExpressionEngine ex = new DefaultExpressionEngine();
        ex.setAttributeEnd(null);
        ex.setAttributeStart(ex.getPropertyDelimiter());
        return ex;
    }

    /**
     * Resets the root node. This method is called when the parameters of this
     * configuration were been changed. When the root node is accessed next
     * time, it is re-created.
     */
    private void clearRootNode()
    {
        lockRoot.lock();
        try
        {
            root = null;
        }
        finally
        {
            lockRoot.unlock();
        }
    }

    /**
     * The node handler for dealing with <code>Preferences</code> nodes.
     */
    private static class PreferencesNodeHandler extends
            AbstractNodeHandler<Preferences>
    {

        public void addAttributeValue(Preferences node, String name,
                Object value)
        {
            node.put(name, String.valueOf(value));
        }

        public Preferences addChild(Preferences node, String name)
        {
            return node.node(name);
        }

        @Override
        public Preferences addChild(Preferences node, String name, Object value)
        {
            addAttributeValue(node, name, value);
            return null;
        }

        public Object getAttributeValue(Preferences node, String name)
        {
            return node.get(name, null);
        }

        public List<String> getAttributes(Preferences node)
        {
            try
            {
                return Arrays.asList(node.keys());
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public Preferences getChild(Preferences node, int index)
        {
            try
            {
                String[] childrenNames = node.childrenNames();
                return node.node(childrenNames[index]);
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public List<Preferences> getChildren(Preferences node)
        {
            try
            {
                String[] childrenNames = node.childrenNames();
                List<Preferences> result = new ArrayList<Preferences>(
                        childrenNames.length);
                for (String name : childrenNames)
                {
                    result.add(node.node(name));
                }
                return result;
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public List<Preferences> getChildren(Preferences node, String name)
        {
            try
            {
                if (node.nodeExists(name))
                {
                    return Collections.singletonList(node.node(name));
                }
                else
                {
                    return Collections.emptyList();
                }
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public int getChildrenCount(Preferences node, String name)
        {
            try
            {
                String[] childrenNames = node.childrenNames();
                return childrenNames.length;
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public Preferences getParent(Preferences node)
        {
            return node.parent();
        }

        public Object getValue(Preferences node)
        {
            return null;
        }

        public String nodeName(Preferences node)
        {
            return node.name();
        }

        public void removeAttribute(Preferences node, String name)
        {
            node.remove(name);
        }

        public void removeChild(Preferences node, Preferences child)
        {
            try
            {
                child.removeNode();
            }
            catch (BackingStoreException bex)
            {
                throw new ConfigurationRuntimeException(bex);
            }
        }

        public void setAttributeValue(Preferences node, String name,
                Object value)
        {
            addAttributeValue(node, name, value);
        }

        public void setValue(Preferences node, Object value)
        {
            throw new UnsupportedOperationException(
                    "Cannot set a value of a Preferences node!");
        }
    }
}
