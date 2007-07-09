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

package org.apache.commons.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.NodeAddData;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;

/**
 * <p>A specialized configuration class that extends its base class by the
 * ability of keeping more structure in the stored properties.</p><p>There
 * are some sources of configuration data that cannot be stored very well in a
 * <code>BaseConfiguration</code> object because then their structure is lost.
 * This is especially true for XML documents. This class can deal with such
 * structured configuration sources by storing the properties in a tree-like
 * organization.</p><p>The internal used storage form allows for a more
 * sophisticated access to single properties. As an example consider the
 * following XML document:</p><p>
 *
 * <pre>
 * &lt;database&gt;
 *   &lt;tables&gt;
 *     &lt;table&gt;
 *       &lt;name&gt;users&lt;/name&gt;
 *       &lt;fields&gt;
 *         &lt;field&gt;
 *           &lt;name&gt;lid&lt;/name&gt;
 *           &lt;type&gt;long&lt;/name&gt;
 *         &lt;/field&gt;
 *         &lt;field&gt;
 *           &lt;name&gt;usrName&lt;/name&gt;
 *           &lt;type&gt;java.lang.String&lt;/type&gt;
 *         &lt;/field&gt;
 *        ...
 *       &lt;/fields&gt;
 *     &lt;/table&gt;
 *     &lt;table&gt;
 *       &lt;name&gt;documents&lt;/name&gt;
 *       &lt;fields&gt;
 *         &lt;field&gt;
 *           &lt;name&gt;docid&lt;/name&gt;
 *           &lt;type&gt;long&lt;/type&gt;
 *         &lt;/field&gt;
 *         ...
 *       &lt;/fields&gt;
 *     &lt;/table&gt;
 *     ...
 *   &lt;/tables&gt;
 * &lt;/database&gt;
 * </pre>
 *
 * </p><p>If this document is parsed and stored in a
 * <code>HierarchicalConfiguration</code> object (which can be done by one of
 * the sub classes), there are enhanced possibilities of accessing properties.
 * The keys for querying information can contain indices that select a certain
 * element if there are multiple hits.</p><p>For instance the key
 * <code>tables.table(0).name</code> can be used to find out the name of the
 * first table. In opposite <code>tables.table.name</code> would return a
 * collection with the names of all available tables. Similarily the key
 * <code>tables.table(1).fields.field.name</code> returns a collection with
 * the names of all fields of the second table. If another index is added after
 * the <code>field</code> element, a single field can be accessed:
 * <code>tables.table(1).fields.field(0).name</code>.</p><p>There is a
 * <code>getMaxIndex()</code> method that returns the maximum allowed index
 * that can be added to a given property key. This method can be used to iterate
 * over all values defined for a certain property.</p>
 * <p>Since the 1.3 release of <em>Commons Configuration</em> hierarchical
 * configurations support an <em>expression engine</em>. This expression engine
 * is responsible for evaluating the passed in configuration keys and map them
 * to the stored properties. The examples above are valid for the default
 * expression engine, which is used when a new <code>HierarchicalConfiguration</code>
 * instance is created. With the <code>setExpressionEngine()</code> method a
 * different expression engine can be set. For instance with
 * <code>{@link XPathExpressionEngine}</code> there is an expression engine
 * available that supports configuration keys in XPATH syntax.</p>
 * <p>In addition to the events common for all configuration classes hierarchical
 * configurations support some more events that correspond to some specific
 * methods and features:
 * <dl><dt><em>EVENT_ADD_NODES</em></dt><dd>The <code>addNodes()</code> method
 * was called; the event object contains the key, to which the nodes were added,
 * and a collection with the new nodes as value.</dd>
 * <dt><em>EVENT_CLEAR_TREE</em></dt><dd>The <code>clearTree()</code> method was
 * called; the event object stores the key of the removed sub tree.</dd>
 * <dt><em>EVENT_SUBNODE_CHANGED</em></dt><dd>A <code>SubnodeConfiguration</code>
 * that was created from this configuration has been changed. The value property
 * of the event object contains the original event object as it was sent by the
 * subnode configuration.</dd></dl></p>
 * <p><em>Note:</em>Configuration objects of this type can be read concurrently
 * by multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.</p>
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class HierarchicalConfiguration extends AbstractConfiguration implements Serializable, Cloneable
{
    /**
     * Constant for the clear tree event.
     * @since 1.3
     */
    public static final int EVENT_CLEAR_TREE = 10;

    /**
     * Constant for the add nodes event.
     * @since 1.3
     */
    public static final int EVENT_ADD_NODES = 11;

    /**
     * Constant for the subnode configuration modified event.
     * @since 1.5
     */
    public static final int EVENT_SUBNODE_CHANGED = 12;

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3373812230395363192L;

    /** Stores the default expression engine to be used for new objects.*/
    private static ExpressionEngine defaultExpressionEngine;

    /** Stores the root node of this configuration. This field is required for
     * backwards compatibility only.
     */
    private Node root;

    /** Stores the root configuration node.*/
    private ConfigurationNode rootNode;

    /** Stores the expression engine for this instance.*/
    private ExpressionEngine expressionEngine;

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code>.
     */
    public HierarchicalConfiguration()
    {
        setRootNode(new Node());
    }

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code> and
     * copies all data contained in the specified configuration into the new
     * one.
     *
     * @param c the configuration that is to be copied (if <b>null</b>, this
     * constructor will behave like the standard constructor)
     * @since 1.4
     */
    public HierarchicalConfiguration(HierarchicalConfiguration c)
    {
        this();
        if (c != null)
        {
            CloneVisitor visitor = new CloneVisitor();
            c.getRootNode().visit(visitor);
            setRootNode(visitor.getClone());
        }
    }

    /**
     * Returns the root node of this hierarchical configuration. This method
     * exists for backwards compatibility only. New code should use the
     * <code>{@link #getRootNode()}</code> method instead, which operates on
     * the preferred data type <code>ConfigurationNode</code>.
     *
     * @return the root node
     */
    public Node getRoot()
    {
        return root;
    }

    /**
     * Sets the root node of this hierarchical configuration. This method
     * exists for backwards compatibility only. New code should use the
     * <code>{@link #setRootNode(ConfigurationNode)}</code> method instead,
     * which operates on the preferred data type <code>ConfigurationNode</code>.
     *
     * @param node the root node
     */
    public void setRoot(Node node)
    {
        if (node == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }
        root = node;
        rootNode = null;
    }

    /**
     * Returns the root node of this hierarchical configuration.
     *
     * @return the root node
     * @since 1.3
     */
    public ConfigurationNode getRootNode()
    {
        return (rootNode != null) ? rootNode : root;
    }

    /**
     * Sets the root node of this hierarchical configuration.
     *
     * @param rootNode the root node
     * @since 1.3
     */
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (rootNode == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }
        this.rootNode = rootNode;

        // For backward compatibility also set the old root field.
        root = (rootNode instanceof Node) ? (Node) rootNode : new Node(rootNode);
    }

    /**
     * Returns the default expression engine.
     *
     * @return the default expression engine
     * @since 1.3
     */
    public static synchronized ExpressionEngine getDefaultExpressionEngine()
    {
        if (defaultExpressionEngine == null)
        {
            defaultExpressionEngine = new DefaultExpressionEngine();
        }
        return defaultExpressionEngine;
    }

    /**
     * Sets the default expression engine. This expression engine will be used
     * if no specific engine was set for an instance. It is shared between all
     * hierarchical configuration instances. So modifying its properties will
     * impact all instances, for which no specific engine is set.
     *
     * @param engine the new default expression engine
     * @since 1.3
     */
    public static synchronized void setDefaultExpressionEngine(ExpressionEngine engine)
    {
        if (engine == null)
        {
            throw new IllegalArgumentException(
                    "Default expression engine must not be null!");
        }
        defaultExpressionEngine = engine;
    }

    /**
     * Returns the expression engine used by this configuration. This method
     * will never return <b>null</b>; if no specific expression engine was set,
     * the default expression engine will be returned.
     *
     * @return the current expression engine
     * @since 1.3
     */
    public ExpressionEngine getExpressionEngine()
    {
        return (expressionEngine != null) ? expressionEngine
                : getDefaultExpressionEngine();
    }

    /**
     * Sets the expression engine to be used by this configuration. All property
     * keys this configuration has to deal with will be interpreted by this
     * engine.
     *
     * @param expressionEngine the new expression engine; can be <b>null</b>,
     * then the default expression engine will be used
     * @since 1.3
     */
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        this.expressionEngine = expressionEngine;
    }

    /**
     * Fetches the specified property. This task is delegated to the associated
     * expression engine.
     *
     * @param key the key to be looked up
     * @return the found value
     */
    public Object getProperty(String key)
    {
        List nodes = fetchNodeList(key);

        if (nodes.size() == 0)
        {
            return null;
        }
        else
        {
            List list = new ArrayList();
            for (Iterator it = nodes.iterator(); it.hasNext();)
            {
                ConfigurationNode node = (ConfigurationNode) it.next();
                if (node.getValue() != null)
                {
                    list.add(node.getValue());
                }
            }

            if (list.size() < 1)
            {
                return null;
            }
            else
            {
                return (list.size() == 1) ? list.get(0) : list;
            }
        }
    }

    /**
     * Adds the property with the specified key. This task will be delegated to
     * the associated <code>ExpressionEngine</code>, so the passed in key
     * must match the requirements of this implementation.
     *
     * @param key the key of the new property
     * @param obj the value of the new property
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        NodeAddData data = getExpressionEngine().prepareAdd(getRootNode(), key);
        ConfigurationNode node = processNodeAddData(data);
        node.setValue(obj);
    }

    /**
     * Adds a collection of nodes at the specified position of the configuration
     * tree. This method works similar to <code>addProperty()</code>, but
     * instead of a single property a whole collection of nodes can be added -
     * and thus complete configuration sub trees. E.g. with this method it is
     * possible to add parts of another <code>HierarchicalConfiguration</code>
     * object to this object. If the passed in key refers to an existing and
     * unique node, the new nodes are added to this node. Otherwise a new node
     * will be created at the specified position in the hierarchy.
     *
     * @param key the key where the nodes are to be added; can be <b>null </b>,
     * then they are added to the root node
     * @param nodes a collection with the <code>Node</code> objects to be
     * added
     */
    public void addNodes(String key, Collection nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            return;
        }

        fireEvent(EVENT_ADD_NODES, key, nodes, true);
        ConfigurationNode parent;
        List target = fetchNodeList(key);
        if (target.size() == 1)
        {
            // existing unique key
            parent = (ConfigurationNode) target.get(0);
        }
        else
        {
            // otherwise perform an add operation
            parent = processNodeAddData(getExpressionEngine().prepareAdd(
                    getRootNode(), key));
        }

        if (parent.isAttribute())
        {
            throw new IllegalArgumentException(
                    "Cannot add nodes to an attribute node!");
        }
        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            ConfigurationNode child = (ConfigurationNode) it.next();
            if (child.isAttribute())
            {
                parent.addAttribute(child);
            }
            else
            {
                parent.addChild(child);
            }
        }
        fireEvent(EVENT_ADD_NODES, key, nodes, false);
    }

    /**
     * Checks if this configuration is empty. Empty means that there are no keys
     * with any values, though there can be some (empty) nodes.
     *
     * @return a flag if this configuration is empty
     */
    public boolean isEmpty()
    {
        return !nodeDefined(getRootNode());
    }

    /**
     * Creates a new <code>Configuration</code> object containing all keys
     * that start with the specified prefix. This implementation will return a
     * <code>HierarchicalConfiguration</code> object so that the structure of
     * the keys will be saved.
     *
     * @param prefix the prefix of the keys for the subset
     * @return a new configuration object representing the selected subset
     */
    public Configuration subset(String prefix)
    {
        Collection nodes = fetchNodeList(prefix);
        if (nodes.isEmpty())
        {
            return new HierarchicalConfiguration();
        }

        final HierarchicalConfiguration parent = this;
        HierarchicalConfiguration result = new HierarchicalConfiguration()
        {
            // Override interpolate to always interpolate on the parent
            protected Object interpolate(Object value)
            {
                return parent.interpolate(value);
            }
        };
        CloneVisitor visitor = new CloneVisitor();

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            ConfigurationNode nd = (ConfigurationNode) it.next();
            nd.visit(visitor);

            for (Iterator it2 = visitor.getClone().getChildren().iterator(); it2.hasNext();)
            {
                result.getRootNode().addChild((ConfigurationNode) it2.next());
            }
            for (Iterator it2 = visitor.getClone().getAttributes().iterator(); it2.hasNext();)
            {
                result.getRootNode().addAttribute((ConfigurationNode) it2.next());
            }
        }

        return (result.isEmpty()) ? new HierarchicalConfiguration() : result;
    }

    /**
     * <p>
     * Returns a hierarchical subnode configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * <code>IllegalArgumentException</code> will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * <code>{@link #subset(String)}</code> method is that
     * <code>subset()</code> supports arbitrary subsets of configuration nodes
     * while <code>configurationAt()</code> only returns a single sub tree.
     * Please refer to the documentation of the
     * <code>SubnodeConfiguration</code> class to obtain further information
     * about subnode configurations and when they should be used.
     * </p>
     * <p>
     * With the <code>supportUpdate</code> flag the behavior of the returned
     * <code>SubnodeConfiguration</code> regarding updates of its parent
     * configuration can be determined. A subnode configuration operates on the
     * same nodes as its parent, so changes at one configuration are normally
     * directly visible for the other configuration. There are however changes
     * of the parent configuration, which are not recognized by the subnode
     * configuration per default. An example for this is a reload operation (for
     * file-based configurations): Here the complete node set of the parent
     * configuration is replaced, but the subnode configuration still references
     * the old nodes. If such changes should be detected by the subnode
     * configuration, the <code>supportUpdates</code> flag must be set to
     * <b>true</b>. This causes the subnode configuration to reevaluate the key
     * used for its creation each time it is accessed. This guarantees that the
     * subnode configuration always stays in sync with its key, even if the
     * parent configuration's data significantly changes. If such a change
     * makes the key invalid - because it now no longer points to exactly one
     * node -, the subnode configuration is not reconstructed, but keeps its
     * old data. It is then quasi detached from its parent.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned subnode configuration
     * should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     * @since 1.5
     */
    public SubnodeConfiguration configurationAt(String key,
            boolean supportUpdates)
    {
        List nodes = fetchNodeList(key);
        if (nodes.size() != 1)
        {
            throw new IllegalArgumentException(
                    "Passed in key must select exactly one node: " + key);
        }
        return supportUpdates ? createSubnodeConfiguration(
                (ConfigurationNode) nodes.get(0), key)
                : createSubnodeConfiguration((ConfigurationNode) nodes.get(0));
    }

    /**
     * Returns a hierarchical subnode configuration for the node specified by
     * the given key. This is a short form for <code>configurationAt(key,
     * <b>false</b>)</code>.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     * @since 1.3
     */
    public SubnodeConfiguration configurationAt(String key)
    {
        return configurationAt(key, false);
    }

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current <code>ExpressionEngine</code>) and then create a subnode
     * configuration for each returned node (like
     * <code>{@link #configurationAt(String)}</code>}). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
     *
     * <pre>
     * List fields = config.configurationsAt("tables.table(0).fields.field");
     * for(Iterator it = fields.iterator(); it.hasNext();)
     * {
     *     HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString("name");
     *     String fieldType = sub.getString("type");
     *     ...
     * </pre>
     *
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     * configuration represents one of the nodes selected by the passed in key
     * @since 1.3
     */
    public List configurationsAt(String key)
    {
        List nodes = fetchNodeList(key);
        List configs = new ArrayList(nodes.size());
        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            configs.add(createSubnodeConfiguration((ConfigurationNode) it.next()));
        }
        return configs;
    }

    /**
     * Creates a subnode configuration for the specified node. This method is
     * called by <code>configurationAt()</code> and
     * <code>configurationsAt()</code>.
     *
     * @param node the node, for which a subnode configuration is to be created
     * @return the configuration for the given node
     * @since 1.3
     */
    protected SubnodeConfiguration createSubnodeConfiguration(ConfigurationNode node)
    {
        SubnodeConfiguration result = new SubnodeConfiguration(this, node);
        registerSubnodeConfiguration(result);
        return result;
    }

    /**
     * Creates a new subnode configuration for the specified node and sets its
     * construction key. A subnode configuration created this way will be aware
     * of structural changes of its parent.
     *
     * @param node the node, for which a subnode configuration is to be created
     * @param subnodeKey the key used to construct the configuration
     * @return the configuration for the given node
     * @since 1.5
     */
    protected SubnodeConfiguration createSubnodeConfiguration(
            ConfigurationNode node, String subnodeKey)
    {
        SubnodeConfiguration result = createSubnodeConfiguration(node);
        result.setSubnodeKey(subnodeKey);
        return result;
    }

    /**
     * This method is always called when a subnode configuration created from
     * this configuration has been modified. This implementation transforms the
     * received event into an event of type <code>EVENT_SUBNODE_CHANGED</code>
     * and notifies the registered listeners.
     *
     * @param event the event describing the change
     * @since 1.5
     */
    protected void subnodeConfigurationChanged(ConfigurationEvent event)
    {
        fireEvent(EVENT_SUBNODE_CHANGED, null, event, event.isBeforeUpdate());
    }

    /**
     * Registers this instance at the given subnode configuration. This
     * implementation will register a change listener, so that modifications of
     * the subnode configuration can be tracked.
     *
     * @param config the subnode configuration
     * @since 1.5
     */
    void registerSubnodeConfiguration(SubnodeConfiguration config)
    {
        config.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                subnodeConfigurationChanged(event);
            }
        });
    }

    /**
     * Checks if the specified key is contained in this configuration. Note that
     * for this configuration the term &quot;contained&quot; means that the key
     * has an associated value. If there is a node for this key that has no
     * value but children (either defined or undefined), this method will still
     * return <b>false </b>.
     *
     * @param key the key to be chekced
     * @return a flag if this key is contained in this configuration
     */
    public boolean containsKey(String key)
    {
        return getProperty(key) != null;
    }

    /**
     * Sets the value of the specified property.
     *
     * @param key the key of the property to set
     * @param value the new value of this property
     */
    public void setProperty(String key, Object value)
    {
        fireEvent(EVENT_SET_PROPERTY, key, value, true);

        // Update the existing nodes for this property
        Iterator itNodes = fetchNodeList(key).iterator();
        Iterator itValues;
        if (!isDelimiterParsingDisabled())
        {
            itValues = PropertyConverter.toIterator(value, getListDelimiter());
        }
        else
        {
            itValues = new SingletonIterator(value);
        }

        while (itNodes.hasNext() && itValues.hasNext())
        {
            ((ConfigurationNode) itNodes.next()).setValue(itValues.next());
        }

        // Add additional nodes if necessary
        while (itValues.hasNext())
        {
            addPropertyDirect(key, itValues.next());
        }

        // Remove remaining nodes
        while (itNodes.hasNext())
        {
            clearNode((ConfigurationNode) itNodes.next());
        }

        fireEvent(EVENT_SET_PROPERTY, key, value, false);
    }

    /**
     * Removes all values of the property with the given name and of keys that
     * start with this name. So if there is a property with the key
     * &quot;foo&quot; and a property with the key &quot;foo.bar&quot;, a call
     * of <code>clearTree("foo")</code> would remove both properties.
     *
     * @param key the key of the property to be removed
     */
    public void clearTree(String key)
    {
        fireEvent(EVENT_CLEAR_TREE, key, null, true);
        List nodes = fetchNodeList(key);

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            removeNode((ConfigurationNode) it.next());
        }
        fireEvent(EVENT_CLEAR_TREE, key, nodes, false);
    }

    /**
     * Removes the property with the given key. Properties with names that start
     * with the given key (i.e. properties below the specified key in the
     * hierarchy) won't be affected.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        fireEvent(EVENT_CLEAR_PROPERTY, key, null, true);
        List nodes = fetchNodeList(key);

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            clearNode((ConfigurationNode) it.next());
        }

        fireEvent(EVENT_CLEAR_PROPERTY, key, null, false);
    }

    /**
     * Returns an iterator with all keys defined in this configuration.
     * Note that the keys returned by this method will not contain any
     * indices. This means that some structure will be lost.</p>
     *
     * @return an iterator with the defined keys in this configuration
     */
    public Iterator getKeys()
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor();
        getRootNode().visit(visitor);

        return visitor.getKeyList().iterator();
    }

    /**
     * Returns an iterator with all keys defined in this configuration that
     * start with the given prefix. The returned keys will not contain any
     * indices.
     *
     * @param prefix the prefix of the keys to start with
     * @return an iterator with the found keys
     */
    public Iterator getKeys(String prefix)
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor(prefix);
        List nodes = fetchNodeList(prefix);

        for (Iterator itNodes = nodes.iterator(); itNodes.hasNext();)
        {
            ConfigurationNode node = (ConfigurationNode) itNodes.next();
            for (Iterator it = node.getChildren().iterator(); it.hasNext();)
            {
                ((ConfigurationNode) it.next()).visit(visitor);
            }
            for (Iterator it = node.getAttributes().iterator(); it.hasNext();)
            {
                ((ConfigurationNode) it.next()).visit(visitor);
            }
        }

        return visitor.getKeyList().iterator();
    }

    /**
     * Returns the maximum defined index for the given key. This is useful if
     * there are multiple values for this key. They can then be addressed
     * separately by specifying indices from 0 to the return value of this
     * method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    public int getMaxIndex(String key)
    {
        return fetchNodeList(key).size() - 1;
    }

    /**
     * Creates a copy of this object. This new configuration object will contain
     * copies of all nodes in the same structure. Registered event listeners
     * won't be cloned; so they are not registered at the returned copy.
     *
     * @return the copy
     * @since 1.2
     */
    public Object clone()
    {
        try
        {
            HierarchicalConfiguration copy = (HierarchicalConfiguration) super
                    .clone();

            // clone the nodes, too
            CloneVisitor v = new CloneVisitor();
            getRootNode().visit(v);
            copy.setRootNode(v.getClone());

            return copy;
        }
        catch (CloneNotSupportedException cex)
        {
            // should not happen
            throw new ConfigurationRuntimeException(cex);
        }
    }

    /**
     * Helper method for fetching a list of all nodes that are addressed by the
     * specified key.
     *
     * @param key the key
     * @return a list with all affected nodes (never <b>null </b>)
     */
    protected List fetchNodeList(String key)
    {
        return getExpressionEngine().query(getRootNode(), key);
    }

    /**
     * Recursive helper method for fetching a property. This method processes
     * all facets of a configuration key, traverses the tree of properties and
     * fetches the the nodes of all matching properties.
     *
     * @param keyPart the configuration key iterator
     * @param node the actual node
     * @param nodes here the found nodes are stored
     * @deprecated Property keys are now evaluated by the expression engine
     * associated with the configuration; this method will no longer be called.
     * If you want to modify the way properties are looked up, consider
     * implementing you own <code>ExpressionEngine</code> implementation.
     */
    protected void findPropertyNodes(ConfigurationKey.KeyIterator keyPart,
            Node node, Collection nodes)
    {
    }

    /**
     * Checks if the specified node is defined.
     *
     * @param node the node to be checked
     * @return a flag if this node is defined
     * @deprecated Use the method <code>{@link #nodeDefined(ConfigurationNode)}</code>
     * instead.
     */
    protected boolean nodeDefined(Node node)
    {
        return nodeDefined((ConfigurationNode) node);
    }

    /**
     * Checks if the specified node is defined.
     *
     * @param node the node to be checked
     * @return a flag if this node is defined
     */
    protected boolean nodeDefined(ConfigurationNode node)
    {
        DefinedVisitor visitor = new DefinedVisitor();
        node.visit(visitor);
        return visitor.isDefined();
    }

    /**
     * Removes the specified node from this configuration. This method ensures
     * that parent nodes that become undefined by this operation are also
     * removed.
     *
     * @param node the node to be removed
     * @deprecated Use the method <code>{@link #removeNode(ConfigurationNode)}</code>
     * instead.
     */
    protected void removeNode(Node node)
    {
        removeNode((ConfigurationNode) node);
    }

    /**
     * Removes the specified node from this configuration. This method ensures
     * that parent nodes that become undefined by this operation are also
     * removed.
     *
     * @param node the node to be removed
     */
    protected void removeNode(ConfigurationNode node)
    {
        ConfigurationNode parent = node.getParentNode();
        if (parent != null)
        {
            parent.removeChild(node);
            if (!nodeDefined(parent))
            {
                removeNode(parent);
            }
        }
    }

    /**
     * Clears the value of the specified node. If the node becomes undefined by
     * this operation, it is removed from the hierarchy.
     *
     * @param node the node to be cleard
     * @deprecated Use the method <code>{@link #clearNode(ConfigurationNode)}</code>
     * instead
     */
    protected void clearNode(Node node)
    {
        clearNode((ConfigurationNode) node);
    }

    /**
     * Clears the value of the specified node. If the node becomes undefined by
     * this operation, it is removed from the hierarchy.
     *
     * @param node the node to be cleard
     */
    protected void clearNode(ConfigurationNode node)
    {
        node.setValue(null);
        if (!nodeDefined(node))
        {
            removeNode(node);
        }
    }

    /**
     * Returns a reference to the parent node of an add operation. Nodes for new
     * properties can be added as children of this node. If the path for the
     * specified key does not exist so far, it is created now.
     *
     * @param keyIt the iterator for the key of the new property
     * @param startNode the node to start the search with
     * @return the parent node for the add operation
     * @deprecated Adding new properties is now to a major part delegated to the
     * <code>ExpressionEngine</code> associated with this configuration instance.
     * This method will no longer be called. Developers who want to modify the
     * process of adding new properties should consider implementing their own
     * expression engine.
     */
    protected Node fetchAddNode(ConfigurationKey.KeyIterator keyIt, Node startNode)
    {
        return null;
    }

    /**
     * Finds the last existing node for an add operation. This method traverses
     * the configuration tree along the specified key. The last existing node on
     * this path is returned.
     *
     * @param keyIt the key iterator
     * @param node the actual node
     * @return the last existing node on the given path
     * @deprecated Adding new properties is now to a major part delegated to the
     * <code>ExpressionEngine</code> associated with this configuration instance.
     * This method will no longer be called. Developers who want to modify the
     * process of adding new properties should consider implementing their own
     * expression engine.
     */
    protected Node findLastPathNode(ConfigurationKey.KeyIterator keyIt, Node node)
    {
        return null;
    }

    /**
     * Creates the missing nodes for adding a new property. This method ensures
     * that there are corresponding nodes for all components of the specified
     * configuration key.
     *
     * @param keyIt the key iterator
     * @param root the base node of the path to be created
     * @return the last node of the path
     * @deprecated Adding new properties is now to a major part delegated to the
     * <code>ExpressionEngine</code> associated with this configuration instance.
     * This method will no longer be called. Developers who want to modify the
     * process of adding new properties should consider implementing their own
     * expression engine.
     */
    protected Node createAddPath(ConfigurationKey.KeyIterator keyIt, Node root)
    {
        return null;
    }

    /**
     * Creates a new <code>Node</code> object with the specified name. This
     * method can be overloaded in derived classes if a specific node type is
     * needed. This base implementation always returns a new object of the
     * <code>Node</code> class.
     *
     * @param name the name of the new node
     * @return the new node
     */
    protected Node createNode(String name)
    {
        return new Node(name);
    }

    /**
     * Helper method for processing a node add data object obtained from the
     * expression engine. This method will create all new nodes.
     *
     * @param data the data object
     * @return the new node
     * @since 1.3
     */
    private ConfigurationNode processNodeAddData(NodeAddData data)
    {
        ConfigurationNode node = data.getParent();

        // Create missing nodes on the path
        for (Iterator it = data.getPathNodes().iterator(); it.hasNext();)
        {
            ConfigurationNode child = createNode((String) it.next());
            node.addChild(child);
            node = child;
        }

        // Add new target node
        ConfigurationNode child = createNode(data.getNewNodeName());
        if (data.isAttribute())
        {
            node.addAttribute(child);
        }
        else
        {
            node.addChild(child);
        }
        return child;
    }

    /**
     * Clears all reference fields in a node structure. A configuration node can
     * store a so-called &quot;reference&quot;. The meaning of this data is
     * determined by a concrete sub class. Typically such references are
     * specific for a configuration instance. If this instance is cloned or
     * copied, they must be cleared. This can be done using this method.
     *
     * @param node the root node of the node hierarchy, in which the references
     * are to be cleared
     * @since 1.4
     */
    protected static void clearReferences(ConfigurationNode node)
    {
        node.visit(new ConfigurationNodeVisitorAdapter()
        {
            public void visitBeforeChildren(ConfigurationNode node)
            {
                node.setReference(null);
            }
        });
    }

    /**
     * A data class for storing (hierarchical) property information. A property
     * can have a value and an arbitrary number of child properties. From
     * version 1.3 on this class is only a thin wrapper over the
     * <code>{@link org.apache.commons.configuration.tree.DefaultConfigurationNode DefaultconfigurationNode}</code>
     * class that exists mainly for the purpose of backwards compatibility.
     */
    public static class Node extends DefaultConfigurationNode implements Serializable
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = -6357500633536941775L;

        /**
         * Creates a new instance of <code>Node</code>.
         */
        public Node()
        {
            super();
        }

        /**
         * Creates a new instance of <code>Node</code> and sets the name.
         *
         * @param name the node's name
         */
        public Node(String name)
        {
            super(name);
        }

        /**
         * Creates a new instance of <code>Node</code> and sets the name and the value.
         *
         * @param name the node's name
         * @param value the value
         */
        public Node(String name, Object value)
        {
            super(name, value);
        }

        /**
         * Creates a new instance of <code>Node</code> based on the given
         * source node. All properties of the source node, including its
         * children and attributes, will be copied.
         *
         * @param src the node to be copied
         */
        public Node(ConfigurationNode src)
        {
            this(src.getName(), src.getValue());
            setReference(src.getReference());
            for (Iterator it = src.getChildren().iterator(); it.hasNext();)
            {
                addChild((ConfigurationNode) it.next());
            }
            for (Iterator it = src.getAttributes().iterator(); it.hasNext();)
            {
                addAttribute((ConfigurationNode) it.next());
            }
        }

        /**
         * Returns the parent of this node.
         *
         * @return this node's parent (can be <b>null</b>)
         */
        public Node getParent()
        {
            return (Node) getParentNode();
        }

        /**
         * Sets the parent of this node.
         *
         * @param node the parent node
         */
        public void setParent(Node node)
        {
            setParentNode(node);
        }

        /**
         * Adds the given node to the children of this node.
         *
         * @param node the child to be added
         */
        public void addChild(Node node)
        {
            addChild((ConfigurationNode) node);
        }

        /**
         * Returns a flag whether this node has child elements.
         *
         * @return <b>true</b> if there is a child node, <b>false</b> otherwise
         */
        public boolean hasChildren()
        {
            return getChildrenCount() > 0 || getAttributeCount() > 0;
        }

        /**
         * Removes the specified child from this node.
         *
         * @param child the child node to be removed
         * @return a flag if the child could be found
         */
        public boolean remove(Node child)
        {
            return child.isAttribute() ? removeAttribute(child) : removeChild(child);
        }

        /**
         * Removes all children with the given name.
         *
         * @param name the name of the children to be removed
         * @return a flag if children with this name existed
         */
        public boolean remove(String name)
        {
            boolean childrenRemoved = removeChild(name);
            boolean attrsRemoved = removeAttribute(name);
            return childrenRemoved || attrsRemoved;
        }

        /**
         * A generic method for traversing this node and all of its children.
         * This method sends the passed in visitor to this node and all of its
         * children.
         *
         * @param visitor the visitor
         * @param key here a configuration key with the name of the root node of
         * the iteration can be passed; if this key is not <b>null </b>, the
         * full pathes to the visited nodes are builded and passed to the
         * visitor's <code>visit()</code> methods
         */
        public void visit(NodeVisitor visitor, ConfigurationKey key)
        {
            int length = 0;
            if (key != null)
            {
                length = key.length();
                if (getName() != null)
                {
                    key
                            .append(StringUtils
                                    .replace(
                                            isAttribute() ? ConfigurationKey
                                                    .constructAttributeKey(getName())
                                                    : getName(),
                                            String
                                                    .valueOf(ConfigurationKey.PROPERTY_DELIMITER),
                                            ConfigurationKey.ESCAPED_DELIMITER));
                }
            }

            visitor.visitBeforeChildren(this, key);

            for (Iterator it = getChildren().iterator(); it.hasNext()
                    && !visitor.terminate();)
            {
                ((Node) it.next()).visit(visitor, key);
            }
            for (Iterator it = getAttributes().iterator(); it.hasNext()
                    && !visitor.terminate();)
            {
                ((Node) it.next()).visit(visitor, key);
            }

            if (key != null)
            {
                key.setLength(length);
            }
            visitor.visitAfterChildren(this, key);
        }
    }

    /**
     * <p>Definition of a visitor class for traversing a node and all of its
     * children.</p><p>This class defines the interface of a visitor for
     * <code>Node</code> objects and provides a default implementation. The
     * method <code>visit()</code> of <code>Node</code> implements a generic
     * iteration algorithm based on the <em>Visitor</em> pattern. By providing
     * different implementations of visitors it is possible to collect different
     * data during the iteration process.</p>
     *
     */
    public static class NodeVisitor
    {
        /**
         * Visits the specified node. This method is called during iteration for
         * each node before its children have been visited.
         *
         * @param node the actual node
         * @param key the key of this node (may be <b>null </b>)
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
        }

        /**
         * Visits the specified node after its children have been processed.
         * This gives a visitor the opportunity of collecting additional data
         * after the child nodes have been visited.
         *
         * @param node the node to be visited
         * @param key the key of this node (may be <b>null </b>)
         */
        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
        }

        /**
         * Returns a flag that indicates if iteration should be stopped. This
         * method is called after each visited node. It can be useful for
         * visitors that search a specific node. If this node is found, the
         * whole process can be stopped. This base implementation always returns
         * <b>false </b>.
         *
         * @return a flag if iteration should be stopped
         */
        public boolean terminate()
        {
            return false;
        }
    }

    /**
     * A specialized visitor that checks if a node is defined.
     * &quot;Defined&quot; in this terms means that the node or at least one of
     * its sub nodes is associated with a value.
     *
     */
    static class DefinedVisitor extends ConfigurationNodeVisitorAdapter
    {
        /** Stores the defined flag. */
        private boolean defined;

        /**
         * Checks if iteration should be stopped. This can be done if the first
         * defined node is found.
         *
         * @return a flag if iteration should be stopped
         */
        public boolean terminate()
        {
            return isDefined();
        }

        /**
         * Visits the node. Checks if a value is defined.
         *
         * @param node the actual node
         */
        public void visitBeforeChildren(ConfigurationNode node)
        {
            defined = node.getValue() != null;
        }

        /**
         * Returns the defined flag.
         *
         * @return the defined flag
         */
        public boolean isDefined()
        {
            return defined;
        }
    }

    /**
     * A specialized visitor that fills a list with keys that are defined in a
     * node hierarchy.
     */
    class DefinedKeysVisitor extends ConfigurationNodeVisitorAdapter
    {
        /** Stores the list to be filled. */
        private Set keyList;

        /** A stack with the keys of the already processed nodes. */
        private Stack parentKeys;

        /**
         * Default constructor.
         */
        public DefinedKeysVisitor()
        {
            keyList = new ListOrderedSet();
            parentKeys = new Stack();
        }

        /**
         * Creates a new <code>DefinedKeysVisitor</code> instance and sets the
         * prefix for the keys to fetch.
         *
         * @param prefix the prefix
         */
        public DefinedKeysVisitor(String prefix)
        {
            this();
            parentKeys.push(prefix);
        }

        /**
         * Returns the list with all defined keys.
         *
         * @return the list with the defined keys
         */
        public Set getKeyList()
        {
            return keyList;
        }

        /**
         * Visits the node after its children has been processed. Removes this
         * node's key from the stack.
         *
         * @param node the node
         */
        public void visitAfterChildren(ConfigurationNode node)
        {
            parentKeys.pop();
        }

        /**
         * Visits the specified node. If this node has a value, its key is added
         * to the internal list.
         *
         * @param node the node to be visited
         */
        public void visitBeforeChildren(ConfigurationNode node)
        {
            String parentKey = parentKeys.isEmpty() ? null
                    : (String) parentKeys.peek();
            String key = getExpressionEngine().nodeKey(node, parentKey);
            parentKeys.push(key);
            if (node.getValue() != null)
            {
                keyList.add(key);
            }
        }
    }

    /**
     * A specialized visitor that is able to create a deep copy of a node
     * hierarchy.
     */
    static class CloneVisitor extends ConfigurationNodeVisitorAdapter
    {
        /** A stack with the actual object to be copied. */
        private Stack copyStack;

        /** Stores the result of the clone process. */
        private ConfigurationNode result;

        /**
         * Creates a new instance of <code>CloneVisitor</code>.
         */
        public CloneVisitor()
        {
            copyStack = new Stack();
        }

        /**
         * Visits the specified node after its children have been processed.
         *
         * @param node the node
         */
        public void visitAfterChildren(ConfigurationNode node)
        {
            ConfigurationNode copy = (ConfigurationNode) copyStack.pop();
            if (copyStack.isEmpty())
            {
                result = copy;
            }
        }

        /**
         * Visits and copies the specified node.
         *
         * @param node the node
         */
        public void visitBeforeChildren(ConfigurationNode node)
        {
            ConfigurationNode copy = (ConfigurationNode) node.clone();
            copy.setParentNode(null);

            if (!copyStack.isEmpty())
            {
                if (node.isAttribute())
                {
                    ((ConfigurationNode) copyStack.peek()).addAttribute(copy);
                }
                else
                {
                    ((ConfigurationNode) copyStack.peek()).addChild(copy);
                }
            }

            copyStack.push(copy);
        }

        /**
         * Returns the result of the clone process. This is the root node of the
         * cloned node hierarchy.
         *
         * @return the cloned root node
         */
        public ConfigurationNode getClone()
        {
            return result;
        }
    }

    /**
     * A specialized visitor base class that can be used for storing the tree of
     * configuration nodes. The basic idea is that each node can be associated
     * with a reference object. This reference object has a concrete meaning in
     * a derived class, e.g. an entry in a JNDI context or an XML element. When
     * the configuration tree is set up, the <code>load()</code> method is
     * responsible for setting the reference objects. When the configuration
     * tree is later modified, new nodes do not have a defined reference object.
     * This visitor class processes all nodes and finds the ones without a
     * defined reference object. For those nodes the <code>insert()</code>
     * method is called, which must be defined in concrete sub classes. This
     * method can perform all steps to integrate the new node into the original
     * structure.
     *
     */
    protected abstract static class BuilderVisitor extends NodeVisitor
    {
        /**
         * Visits the specified node before its children have been traversed.
         *
         * @param node the node to visit
         * @param key the current key
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            Collection subNodes = new LinkedList(node.getChildren());
            subNodes.addAll(node.getAttributes());
            Iterator children = subNodes.iterator();
            Node sibling1 = null;
            Node nd = null;

            while (children.hasNext())
            {
                // find the next new node
                do
                {
                    sibling1 = nd;
                    nd = (Node) children.next();
                } while (nd.getReference() != null && children.hasNext());

                if (nd.getReference() == null)
                {
                    // find all following new nodes
                    List newNodes = new LinkedList();
                    newNodes.add(nd);
                    while (children.hasNext())
                    {
                        nd = (Node) children.next();
                        if (nd.getReference() == null)
                        {
                            newNodes.add(nd);
                        }
                        else
                        {
                            break;
                        }
                    }

                    // Insert all new nodes
                    Node sibling2 = (nd.getReference() == null) ? null : nd;
                    for (Iterator it = newNodes.iterator(); it.hasNext();)
                    {
                        Node insertNode = (Node) it.next();
                        if (insertNode.getReference() == null)
                        {
                            Object ref = insert(insertNode, node, sibling1, sibling2);
                            if (ref != null)
                            {
                                insertNode.setReference(ref);
                            }
                            sibling1 = insertNode;
                        }
                    }
                }
            }
        }

        /**
         * Inserts a new node into the structure constructed by this builder.
         * This method is called for each node that has been added to the
         * configuration tree after the configuration has been loaded from its
         * source. These new nodes have to be inserted into the original
         * structure. The passed in nodes define the position of the node to be
         * inserted: its parent and the siblings between to insert. The return
         * value is interpreted as the new reference of the affected
         * <code>Node</code> object; if it is not <b>null </b>, it is passed
         * to the node's <code>setReference()</code> method.
         *
         * @param newNode the node to be inserted
         * @param parent the parent node
         * @param sibling1 the sibling after which the node is to be inserted;
         * can be <b>null </b> if the new node is going to be the first child
         * node
         * @param sibling2 the sibling before which the node is to be inserted;
         * can be <b>null </b> if the new node is going to be the last child
         * node
         * @return the reference object for the node to be inserted
         */
        protected abstract Object insert(Node newNode, Node parent, Node sibling1, Node sibling2);
    }
}
