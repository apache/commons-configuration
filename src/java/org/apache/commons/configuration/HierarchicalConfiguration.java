/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.commons.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;

/**
 * <p>A specialized configuration class that extends its base class by the
 * ability of keeping more structure in the stored properties.</p>
 * <p>There are some sources of configuration data that cannot be stored
 * very well in a <code>BaseConfiguration</code> object because then their
 * structure is lost. This is especially true for XML documents. This class
 * can deal with such structured configuration sources by storing the
 * properties in a tree-like organization.</p>
 * <p>The internal used storage form allows for a more sophisticated access to
 * single properties. As an example consider the following XML document:</p>
 * <p><pre>
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
 * </pre></p>
 * <p>If this document is parsed and stored in a
 * <code>HierarchicalConfiguration</code> object (which can be done by one of
 * the sub classes), there are enhanced possibilities of accessing properties.
 * The keys for querying information can contain indices that select a certain
 * element if there are multiple hits.</p>
 * <p>For instance the key <code>tables.table(0).name</code> can be used to
 * find out the name of the first table. In opposite
 * <code>tables.table.name</code> would return a collection with the names of
 * all available tables. Similarily the key
 * <code>tables.table(1).fields.field.name</code> returns a collection with the
 * names of all fields of the second table. If another index is added after the
 * <code>field</code> element, a single field can be accessed:
 * <code>tables.table(1).fields.field(0).name</code>.</p>
 * <p>There is a <code>getMaxIndex()</code> method that returns the maximum
 * allowed index that can be added to a given property key. This method can be
 * used to iterate over all values defined for a certain property.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: HierarchicalConfiguration.java,v 1.10 2004/09/20 09:37:07 henning Exp $
 */
public class HierarchicalConfiguration extends AbstractConfiguration
{
    /** Constant for a new dummy key.*/
    private static final String NEW_KEY = "newKey";

    /** Stores the root node of this configuration.*/
    private Node root = new Node();

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code>.
     */
    public HierarchicalConfiguration()
    {
        super();
    }

    /**
     * Returns the root node of this hierarchical configuration.
     *
     * @return the root node
     */
    public Node getRoot()
    {
        return root;
    }

    /**
     * Sets the root node of this hierarchical configuration.
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
    }

    /**
     * Fetches the specified property. Performs a recursive lookup in the
     * tree with the configuration properties.
     *
     * @param key the key to be looked up
     * @return the found value
     */
    protected Object getPropertyDirect(String key)
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
                Node node = (Node) it.next();
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
     * <p>Adds the property with the specified key.</p>
     * <p>To be able to deal with the structure supported by this configuration
     * implementation the passed in key is of importance, especially the
     * indices it might contain. The following example should clearify this:
     * Suppose the actual configuration contains the following elements:</p>
     * <p><pre>
     * tables
     *    +-- table
     *            +-- name = user
     *            +-- fields
     *                    +-- field
     *                            +-- name = uid
     *                    +-- field
     *                            +-- name = firstName
     *                    ...
     *    +-- table
     *            +-- name = documents
     *            +-- fields
     *                   ...
     * </pre></p>
     * <p>In this example a database structure is defined, e.g. all fields of
     * the first table could be accessed using the key
     * <code>tables.table(0).fields.field.name</code>. If now properties are
     * to be added, it must be exactly specified at which position in the
     * hierarchy the new property is to be inserted. So to add a new field name
     * to a table it is not enough to say just</p>
     * <p><pre>
     * config.addProperty("tables.table.fields.field.name", "newField");
     * </pre></p>
     * <p>The statement given above contains some ambiguity. For instance
     * it is not clear, to which table the new field should be added. If this
     * method finds such an ambiguity, it is resolved by following the last
     * valid path. Here this would be the last table. The same is true for the
     * <code>field</code>; because there are multiple fields and no explicit
     * index is provided, a new <code>name</code> property would be
     * added to the last field - which is propably not what was desired.</p>
     * <p>To make things clear explicit indices should be provided whenever
     * possible. In the example above the exact table could be specified by
     * providing an index for the <code>table</code> element as in
     * <code>tables.table(1).fields</code>. By specifying an index it can also
     * be expressed that at a given position in the configuration tree a new
     * branch should be added. In the example above we did not want to add
     * an additional <code>name</code> element to the last field of the table,
     * but we want a complete new <code>field</code> element. This can be
     * achieved by specifying an invalid index (like -1) after the element
     * where a new branch should be created. Given this our example would run:
     * </p><p><pre>
     * config.addProperty("tables.table(1).fields.field(-1).name", "newField");
     * </pre></p>
     * <p>With this notation it is possible to add new branches everywhere.
     * We could for instance create a new <code>table</code> element by
     * specifying</p>
     * <p><pre>
     * config.addProperty("tables.table(-1).fields.field.name", "newField2");
     * </pre></p>
     * <p>(Note that because after the <code>table</code> element a new
     * branch is created indices in following elements are not relevant; the
     * branch is new so there cannot be any ambiguities.)</p>
     *
     * @param key the key of the new property
     * @param obj the value of the new property
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        ConfigurationKey.KeyIterator it = new ConfigurationKey(key).iterator();
        Node parent = fetchAddNode(it, getRoot());

        Node child = new Node(it.currentKey(true));
        child.setValue(obj);
        parent.addChild(child);
    }

    /**
     * Adds a collection of nodes at the specified position of the
     * configuration tree. This method works similar to
     * <code>addProperty()</code>, but instead of a single property a whole
     * collection of nodes can be added - and thus complete configuration
     * sub trees. E.g. with this method it is possible to add parts of
     * another <code>HierarchicalConfiguration</code> object to this object.
     *
     * @param key the key where the nodes are to be added; can be <b>null</b>,
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

        Node parent;
        if (StringUtils.isEmpty(key))
        {
            parent = getRoot();
        }
        else
        {
            ConfigurationKey.KeyIterator kit =
                new ConfigurationKey(key).iterator();
            parent = fetchAddNode(kit, getRoot());

            // fetchAddNode() does not really fetch the last component,
            // but one before. So we must perform an additional step.
            ConfigurationKey keyNew =
                new ConfigurationKey(kit.currentKey(true));
            keyNew.append(NEW_KEY);
            parent = fetchAddNode(keyNew.iterator(), parent);
        }

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            parent.addChild((Node) it.next());
        }
    }

    /**
     * Checks if this configuration is empty. Empty means that there are
     * no keys with any values, though there can be some (empty) nodes.
     *
     * @return a flag if this configuration is empty
     */
    public boolean isEmpty()
    {
        return !nodeDefined(getRoot());
    }

    /**
     * Creates a new <code>Configuration</code> object containing all keys
     * that start with the specified prefix. This implementation will return
     * a <code>HierarchicalConfiguration</code> object so that the structure
     * of the keys will be saved.
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

        HierarchicalConfiguration result = new HierarchicalConfiguration();
        CloneVisitor visitor = new CloneVisitor();

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            Node nd = (Node) it.next();
            nd.visit(visitor, null);

            List children = visitor.getClone().getChildren();
            if (children.size() > 0)
            {
                for (int i = 0; i < children.size(); i++)
                {
                    result.getRoot().addChild((Node) children.get(i));
                }
            }
        }

        return (result.isEmpty()) ? new HierarchicalConfiguration() : result;
    }

    /**
     * Checks if the specified key is contained in this configuration.
     * Note that for this configuration the term &quot;contained&quot; means
     * that the key has an associated value. If there is a node for this key
     * that has no value but children (either defined or undefined), this
     * method will still return <b>false</b>.
     *
     * @param key the key to be chekced
     * @return a flag if this key is contained in this configuration
     */
    public boolean containsKey(String key)
    {
        return getPropertyDirect(key) != null;
    }

    /**
     * Removes all values of the property with the given name.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        List nodes = fetchNodeList(key);

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            removeNode((Node) it.next());
        }
    }

    /**
     * <p>Returns an iterator with all keys defined in this configuration.</p>
     * <p>Note that the keys returned by this method will not contain
     * any indices. This means that some structure will be lost.</p>
     *
     * @return an iterator with the defined keys in this configuration
     */
    public Iterator getKeys()
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor();
        getRoot().visit(visitor, new ConfigurationKey());
        return visitor.getKeyList().iterator();
    }

    /**
     * Returns the maximum defined index for the given key. This is
     * useful if there are multiple values for this key. They can then be
     * addressed separately by specifying indices from 0 to the return value
     * of this method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    public int getMaxIndex(String key)
    {
        return fetchNodeList(key).size() - 1;
    }

    /**
     * Helper method for fetching a list of all nodes that are addressed by
     * the specified key.
     *
     * @param key the key
     * @return a list with all affected nodes (never <b>null</b>)
     */
    protected List fetchNodeList(String key)
    {
        List nodes = new LinkedList();
        findPropertyNodes(
            new ConfigurationKey(key).iterator(),
            getRoot(),
            nodes);
        return nodes;
    }

    /**
     * Recursive helper method for fetching a property. This method
     * processes all facets of a configuration key, traverses the tree of
     * properties and fetches the the nodes of all matching properties.
     *
     * @param keyPart the configuration key iterator
     * @param node the actual node
     * @param data here the found nodes are stored
     */
    protected void findPropertyNodes(
        ConfigurationKey.KeyIterator keyPart,
        Node node,
        Collection data)
    {
        if (!keyPart.hasNext())
        {
            data.add(node);
        }
        else
        {
            String key = keyPart.nextKey(true);
            List children = node.getChildren(key);
            if (keyPart.hasIndex())
            {
                if (keyPart.getIndex() < children.size()
                    && keyPart.getIndex() >= 0)
                {
                    findPropertyNodes(
                        (ConfigurationKey.KeyIterator) keyPart.clone(),
                        (Node) children.get(keyPart.getIndex()),
                        data);
                }
            }
            else
            {
                for (Iterator it = children.iterator(); it.hasNext();)
                {
                    findPropertyNodes(
                        (ConfigurationKey.KeyIterator) keyPart.clone(),
                        (Node) it.next(),
                        data);
                }
            }
        }
    }

    /**
     * Checks if the specified node is defined.
     *
     * @param node the node to be checked
     * @return a flag if this node is defined
     */
    protected boolean nodeDefined(Node node)
    {
        DefinedVisitor visitor = new DefinedVisitor();
        node.visit(visitor, null);
        return visitor.isDefined();
    }

    /**
     * Removes the specified node from this configuration. This method
     * ensures that parent nodes that become undefined by this operation
     * are also removed.
     *
     * @param node the node to be removed
     */
    protected void removeNode(Node node)
    {
        Node parent = node.getParent();
        if (parent != null)
        {
            parent.remove(node);
            if (!nodeDefined(parent))
            {
                removeNode(parent);
            }
        }
    }

    /**
     * Returns a reference to the parent node of an add operation.
     * Nodes for new properties can be added as children of this node.
     * If the path for the specified key does not exist so far, it is created
     * now.
     *
     * @param keyIt the iterator for the key of the new property
     * @param startNode the node to start the search with
     * @return the parent node for the add operation
     */
    protected Node fetchAddNode(ConfigurationKey.KeyIterator keyIt, Node startNode)
    {
        if (!keyIt.hasNext())
        {
            throw new IllegalArgumentException("Key must be defined!");
        }

        return createAddPath(keyIt, findLastPathNode(keyIt, startNode));
    }

    /**
     * Finds the last existing node for an add operation. This method
     * traverses the configuration tree along the specified key. The last
     * existing node on this path is returned.
     *
     * @param keyIt the key iterator
     * @param node the actual node
     * @return the last existing node on the given path
     */
    protected Node findLastPathNode(ConfigurationKey.KeyIterator keyIt, Node node)
    {
        String keyPart = keyIt.nextKey(true);

        if (keyIt.hasNext())
        {
            List list = node.getChildren(keyPart);
            int idx = (keyIt.hasIndex()) ? keyIt.getIndex() : list.size() - 1;
            if (idx < 0 || idx >= list.size())
            {
                return node;
            }
            else
            {
                return findLastPathNode(keyIt, (Node) list.get(idx));
            }
        }

        else
        {
            return node;
        }
    }

    /**
     * Creates the missing nodes for adding a new property. This method
     * ensures that there are corresponding nodes for all components of the
     * specified configuration key.
     *
     * @param keyIt the key iterator
     * @param root the base node of the path to be created
     * @return the last node of the path
     */
    protected Node createAddPath(ConfigurationKey.KeyIterator keyIt, Node root)
    {
        if (keyIt.hasNext())
        {
            Node child = new Node(keyIt.currentKey(true));
            root.addChild(child);
            keyIt.next();
            return createAddPath(keyIt, child);
        }
        else
        {
            return root;
        }
    }

    /**
     * A data class for storing (hierarchical) property information. A property
     * can have a value and an arbitrary number of child properties.
     *
     */
    public static class Node implements Serializable, Cloneable
    {
        /** Stores a reference to this node's parent.*/
        private Node parent;

        /** Stores the name of this node.*/
        private String name;

        /** Stores the value of this node.*/
        private Object value;

        /** Stores the children of this node.*/
        private LinkedMap children; // Explict type here or we
                                    // will get a findbugs error 
                                    // because Map doesn't imply 
                                    // Serializable

        /**
         * Creates a new instance of <code>Node</code>.
         */
        public Node()
        {
            this(null);
        }

        /**
         * Creates a new instance of <code>Node</code> and sets the name.
         *
         * @param name the node's name
         */
        public Node(String name)
        {
            setName(name);
        }

        /**
         * Returns the name of this node.
         *
         * @return the node name
         */
        public String getName()
        {
            return name;
        }

        /**
         * Returns the value of this node.
         *
         * @return the node value (may be <b>null</b>)
         */
        public Object getValue()
        {
            return value;
        }

        /**
         * Returns the parent of this node.
         *
         * @return this node's parent (can be <b>null</b>)
         */
        public Node getParent()
        {
            return parent;
        }

        /**
         * Sets the name of this node.
         *
         * @param string the node name
         */
        public void setName(String string)
        {
            name = string;
        }

        /**
         * Sets the value of this node.
         *
         * @param object the node value
         */
        public void setValue(Object object)
        {
            value = object;
        }

        /**
         * Sets the parent of this node.
         *
         * @param node the parent node
         */
        public void setParent(Node node)
        {
            parent = node;
        }

        /**
         * Adds the specified child object to this node. Note that there can
         * be multiple children with the same name.
         *
         * @param child the child to be added
         */
        public void addChild(Node child)
        {
            if (children == null)
            {
                children = new LinkedMap();
            }

            List c = (List) children.get(child.getName());
            if (c == null)
            {
                c = new ArrayList();
                children.put(child.getName(), c);
            }

            c.add(child);
            child.setParent(this);
        }

        /**
         * Returns a list with the child nodes of this node.
         *
         * @return a list with the children (can be empty, but never
         * <b>null</b>)
         */
        public List getChildren()
        {
            List result = new ArrayList();

            if (children != null)
            {
                for (Iterator it = children.values().iterator(); it.hasNext();)
                {
                    result.addAll((Collection) it.next());
                }
            }

            return result;
        }

        /**
         * Returns a list with this node's children with the given name.
         *
         * @param name the name of the children
         * @return a list with all chidren with this name; may be empty, but
         * never <b>null</b>
         */
        public List getChildren(String name)
        {
            if (name == null || children == null)
            {
                return getChildren();
            }

            List list = new ArrayList();
            List c = (List) children.get(name);
            if (c != null)
            {
                list.addAll(c);
            }

            return list;
        }

        /**
         * Removes the specified child from this node.
         *
         * @param child the child node to be removed
         * @return a flag if the child could be found
         */
        public boolean remove(Node child)
        {
            if (children == null)
            {
                return false;
            }

            List c = (List) children.get(child.getName());
            if (c == null)
            {
                return false;
            }

            else
            {
                if (c.remove(child))
                {
                    if (c.isEmpty())
                    {
                        children.remove(child.getName());
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        /**
         * Removes all children with the given name.
         *
         * @param name the name of the children to be removed
         * @return a flag if children with this name existed
         */
        public boolean remove(String name)
        {
            if (children == null)
            {
                return false;
            }

            return children.remove(name) != null;
        }

        /**
         * Removes all children of this node.
         */
        public void removeChildren()
        {
            children = null;
        }

        /**
         * A generic method for traversing this node and all of its children.
         * This method sends the passed in visitor to this node and all of its
         * children.
         *
         * @param visitor the visitor
         * @param key here a configuration key with the name of the root node
         * of the iteration can be passed; if this key is not <b>null</b>, the
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
                    key.append(getName());
                }
            }

            visitor.visitBeforeChildren(this, key);

            if (children != null)
            {
                for (Iterator it = children.values().iterator();
                    it.hasNext() && !visitor.terminate();
                    )
                {
                    Collection col = (Collection) it.next();
                    for (Iterator it2 = col.iterator();
                        it2.hasNext() && !visitor.terminate();
                        )
                    {
                        ((Node) it2.next()).visit(visitor, key);
                    }
                }
            }

            if (key != null)
            {
                key.setLength(length);
            }
            visitor.visitAfterChildren(this, key);
        }

        /**
         * Creates a copy of this object. This is not a deep copy, the children
         * are not cloned.
         *
         * @return a copy of this object
         */
        protected Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException cex)
            {
                return null; // should not happen
            }
        }
    }

    /**
     * <p>Definition of a visitor class for traversing a node and all of its
     * children.</p>
     * <p>This class defines the interface of a visitor for <code>Node</code>
     * objects and provides a default implementation. The method
     * <code>visit()</code> of <code>Node</code> implements a generic
     * iteration algorithm based on the <em>Visitor</em> pattern. By
     * providing different implementations of visitors it is possible to
     * collect different data during the iteration process.</p>
     *
     */
    public static class NodeVisitor
    {
        /**
         * Visits the specified node. This method is called during iteration
         * for each node before its children have been visited.
         *
         * @param node the actual node
         * @param key the key of this node (may be <b>null</b>)
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
         * @param key the key of this node (may be <b>null</b>)
         */
        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
        }

        /**
         * Returns a flag that indicates if iteration should be stopped. This
         * method is called after each visited node. It can be useful for
         * visitors that search a specific node. If this node is found, the
         * whole process can be stopped. This base implementation always
         * returns <b>false</b>.
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
     * &quot;Defined&quot; in this terms means that the node or at least one
     * of its sub nodes is associated with a value.
     *
     */
    static class DefinedVisitor extends NodeVisitor
    {
        /** Stores the defined flag.*/
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
         * @param key the key of this node
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
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
     * A specialized visitor that fills a list with keys that are defined in
     * a node hierarchy.
     *
     */
    static class DefinedKeysVisitor extends NodeVisitor
    {
        /** Stores the list to be filled.*/
        private Set keyList;

        /**
         * Default constructor.
         */
        public DefinedKeysVisitor()
        {
            keyList = new HashSet();
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
         * Visits the specified node. If this node has a value, its key is
         * added to the internal list.
         *
         * @param node the node to be visited
         * @param key the key of this node
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            if (node.getValue() != null && key != null)
            {
                keyList.add(key.toString());
            }
        }
    }

    /**
     * A specialized visitor that is able to create a deep copy of a node
     * hierarchy.
     *
     */
    static class CloneVisitor extends NodeVisitor
    {
        /** A stack with the actual object to be copied.*/
        private Stack copyStack;

        /** Stores the result of the clone process.*/
        private Node result;

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
         * @param key the key of this node
         */
        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
            copyStack.pop();
            if (copyStack.isEmpty())
            {
                result = node;
            }
        }

        /**
         * Visits and copies the specified node.
         *
         * @param node the node
         * @param key the key of this node
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            Node copy = (Node) node.clone();
            copy.removeChildren();

            if (!copyStack.isEmpty())
            {
                ((Node) copyStack.peek()).addChild(copy);
            }

            copyStack.push(copy);
        }

        /**
         * Returns the result of the clone process. This is the root node of
         * the cloned node hierarchy.
         *
         * @return the cloned root node
         */
        public Node getClone()
        {
            return result;
        }
    }
}
