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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

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
 * collection with the names of all available tables. Similarly the key
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
 * <code>{@link org.apache.commons.configuration2.expr.xpath.XPathExpressionEngine}</code>
 * there is an expression engine available that supports configuration keys in
 * XPATH syntax.</p>
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
 * @since 2.0
 * @version $Id$
 */
public class InMemoryConfiguration extends AbstractHierarchicalConfiguration<ConfigurationNode> implements Cloneable
{
    /**
     * Constant for the add nodes event.
     */
    public static final int EVENT_ADD_NODES = 11;

    /** Stores the root configuration node.*/
    private ConfigurationNode rootNode;

    /**
     * Creates a new instance of <code>InMemoryConfiguration</code>.
     */
    public InMemoryConfiguration()
    {
        super(new ConfigurationNodeHandler());
        setRootNode(new DefaultConfigurationNode());
    }

    /**
     * Creates a new instance of <code>InMemoryConfiguration</code> and
     * copies all data contained in the specified configuration into the new
     * one.
     *
     * @param c the configuration that is to be copied (if <b>null</b>, this
     * constructor will behave like the standard constructor)
     */
    public InMemoryConfiguration(AbstractHierarchicalConfiguration<? extends ConfigurationNode> c)
    {
        this();
        if (c != null)
        {
            CloneVisitor visitor = new CloneVisitor();
            visit(c.getRootNode(), visitor);
            setRootNode(visitor.getClone());
        }
    }

    /**
     * Returns the root node of this hierarchical configuration.
     *
     * @return the root node
     */
    @Override
    public ConfigurationNode getRootNode()
    {
        return rootNode;
    }

    /**
     * Sets the root node of this hierarchical configuration.
     *
     * @param rootNode the root node
     */
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (rootNode == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }
        this.rootNode = rootNode;
    }

    /**
     * Adds a collection of nodes at the specified position of the configuration
     * tree. This method works similar to <code>addProperty()</code>, but
     * instead of a single property a whole collection of nodes can be added -
     * and thus complete configuration sub trees. E.g. with this method it is
     * possible to add parts of another <code>HierarchicalConfiguration</code>
     * object to this object. (However be aware that a
     * <code>ConfigurationNode</code> object can only belong to a single
     * configuration. So if nodes from one configuration are directly added to
     * another one using this method, the structure of the source configuration
     * will be broken. In this case you should clone the nodes to be added
     * before calling <code>addNodes()</code>.) If the passed in key refers to
     * an existing and unique node, the new nodes are added to this node.
     * Otherwise a new node will be created at the specified position in the
     * hierarchy.
     *
     * @param key the key where the nodes are to be added; can be <b>null </b>,
     * then they are added to the root node
     * @param nodes a collection with the <code>Node</code> objects to be
     * added
     * @throws IllegalArgumentException if the key specifies an attribute
     */
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            return;
        }

        fireEvent(EVENT_ADD_NODES, key, nodes, true);
        ConfigurationNode parent;
        NodeList<ConfigurationNode> target = fetchNodeList(key);
        if (target.size() == 1)
        {
            // existing unique key
            parent = target.getNode(0);
        }
        else
        {
            // otherwise perform an add operation
            NodeAddData<ConfigurationNode> addData = getExpressionEngine()
                    .prepareAdd(getRootNode(), key, getNodeHandler());
            if (addData.isAttribute())
            {
                throw new IllegalArgumentException(
                        "Cannot add nodes to an attribute node!");
            }
            parent = processNodeAddData(addData, null);
        }

        for (ConfigurationNode child : nodes)
        {
            if (child.isAttribute())
            {
                parent.addAttribute(child);
            }
            else
            {
                parent.addChild(child);
            }
            clearReferences(child);
        }
        fireEvent(EVENT_ADD_NODES, key, nodes, false);
    }

    /**
     * Creates a new <code>Configuration</code> object containing all keys
     * that start with the specified prefix. This implementation will return an
     * <code>InMemoryConfiguration</code> object so that the structure of
     * the keys will be saved. The nodes selected by the prefix (it is possible
     * that multiple nodes are selected) are mapped to the root node of the
     * returned configuration, i.e. their children and attributes will become
     * children and attributes of the new root node. However a value of the root
     * node is only set if exactly one of the selected nodes contain a value (if
     * multiple nodes have a value, there is simply no way to decide how these
     * values are merged together). Note that the returned
     * <code>Configuration</code> object is not connected to its source
     * configuration: updates on the source configuration are not reflected in
     * the subset and vice versa.
     *
     * @param prefix the prefix of the keys for the subset
     * @return a new configuration object representing the selected subset
     */
    @Override
    public Configuration subset(String prefix)
    {
        NodeList<ConfigurationNode> nodes = fetchNodeList(prefix);
        if (nodes.size() < 1)
        {
            return new InMemoryConfiguration();
        }

        final InMemoryConfiguration parent = this;
        InMemoryConfiguration result = new InMemoryConfiguration()
        {
            // Override interpolate to always interpolate on the parent
            @Override
            protected Object interpolate(Object value)
            {
                return parent.interpolate(value);
            }
        };
        CloneVisitor visitor = new CloneVisitor();

        // Initialize the new root node
        Object value = null;
        int valueCount = 0;
        for (int index = 0; index < nodes.size(); index++)
        {
            Object v = nodes.getValue(index, getNodeHandler());
            if (v != null)
            {
                value = v;
                valueCount++;
            }

            if (nodes.isAttribute(index))
            {
                getNodeHandler().setAttributeValue(result.getRootNode(),
                        nodes.getName(index, getNodeHandler()),
                        nodes.getValue(index, getNodeHandler()));
            }

            else
            {
                visit(nodes.getNode(index), visitor);
                for (ConfigurationNode child : visitor.getClone().getChildren())
                {
                    result.getRootNode().addChild(child);
                }
                for (ConfigurationNode attr : visitor.getClone()
                        .getAttributes())
                {
                    result.getRootNode().addAttribute(attr);
                }
            }
        }

        // Determine the value of the new root
        if (valueCount == 1)
        {
            result.getRootNode().setValue(value);
        }
        return (result.isEmpty()) ? new InMemoryConfiguration() : result;
    }

    /**
     * Creates a copy of this object. This new configuration object will contain
     * copies of all nodes in the same structure. Registered event listeners
     * won't be cloned; so they are not registered at the returned copy.
     *
     * @return the copy
     */
    @Override
    public Object clone()
    {
        try
        {
            InMemoryConfiguration copy = (InMemoryConfiguration) super.clone();

            // clone the nodes, too
            CloneVisitor v = new CloneVisitor();
            visit(getRootNode(), v);
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
     * Returns a configuration with the same content as this configuration, but
     * with all variables replaced by their actual values. This implementation
     * is specific for hierarchical configurations. It clones the current
     * configuration and runs a specialized visitor on the clone, which performs
     * interpolation on the single configuration nodes.
     *
     * @return a configuration with all variables interpolated
     */
    @Override
    public Configuration interpolatedConfiguration()
    {
        InMemoryConfiguration c = (InMemoryConfiguration) clone();
        visit(c.getRootNode(), new NodeVisitorAdapter<ConfigurationNode>()
        {
            @Override
            public void visitAfterChildren(ConfigurationNode node,
                    NodeHandler<ConfigurationNode> handler)
            {
                node.setValue(interpolate(node.getValue()));
            }
        });
        return c;
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
     */
    protected void clearReferences(ConfigurationNode node)
    {
        visit(node, new NodeVisitorAdapter<ConfigurationNode>()
        {
            @Override
            public void visitBeforeChildren(ConfigurationNode node,
                    NodeHandler<ConfigurationNode> handler)
            {
                node.setReference(null);
                for (ConfigurationNode attr : node.getAttributes())
                {
                    attr.setReference(null);
                }
            }
        });
    }

    /**
     * A specialized visitor that is able to create a deep copy of a node
     * hierarchy.
     */
    static class CloneVisitor extends NodeVisitorAdapter<ConfigurationNode>
    {
        /** A stack with the actual object to be copied. */
        private Stack<ConfigurationNode> copyStack;

        /** Stores the result of the clone process. */
        private ConfigurationNode result;

        /**
         * Creates a new instance of <code>CloneVisitor</code>.
         */
        public CloneVisitor()
        {
            copyStack = new Stack<ConfigurationNode>();
        }

        /**
         * Visits the specified node after its children have been processed.
         *
         * @param node the node
         */
        @Override
        public void visitAfterChildren(ConfigurationNode node, NodeHandler<ConfigurationNode> handler)
        {
            ConfigurationNode copy = copyStack.pop();
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
        @Override
        public void visitBeforeChildren(ConfigurationNode node, NodeHandler<ConfigurationNode> handler)
        {
            ConfigurationNode copy = (ConfigurationNode) node.clone();
            copy.setParentNode(null);

            for(ConfigurationNode attr : node.getAttributes())
            {
                copy.addAttribute((ConfigurationNode) attr.clone());
            }
            if (!copyStack.isEmpty())
            {
                copyStack.peek().addChild(copy);
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
     */
    protected abstract static class BuilderVisitor extends NodeVisitorAdapter<ConfigurationNode>
    {
        /**
         * Visits the specified node before its children have been traversed.
         *
         * @param node the node to visit
         */
        @Override
        public void visitBeforeChildren(ConfigurationNode node, NodeHandler<ConfigurationNode> handler)
        {
            Collection<ConfigurationNode> subNodes = new LinkedList<ConfigurationNode>(node.getChildren());
            subNodes.addAll(node.getAttributes());
            Iterator<ConfigurationNode> children = subNodes.iterator();
            ConfigurationNode sibling1 = null;
            ConfigurationNode nd = null;

            while (children.hasNext())
            {
                // find the next new node
                do
                {
                    sibling1 = nd;
                    nd = children.next();
                } while (nd.getReference() != null && children.hasNext());

                if (nd.getReference() == null)
                {
                    // find all following new nodes
                    List<ConfigurationNode> newNodes = new LinkedList<ConfigurationNode>();
                    newNodes.add(nd);
                    while (children.hasNext())
                    {
                        nd = children.next();
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
                    ConfigurationNode sibling2 = (nd.getReference() == null) ? null : nd;
                    for (ConfigurationNode insertNode : newNodes)
                    {
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
        protected abstract Object insert(ConfigurationNode newNode,
                ConfigurationNode parent, ConfigurationNode sibling1,
                ConfigurationNode sibling2);
    }
}
