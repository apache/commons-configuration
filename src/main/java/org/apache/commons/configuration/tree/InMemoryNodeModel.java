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
package org.apache.commons.configuration.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized node model implementation which operates on
 * {@link ImmutableNode} structures.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class InMemoryNodeModel implements NodeHandler<ImmutableNode>
{
    /** Stores information about the current nodes structure. */
    private final AtomicReference<TreeData> structure;

    /**
     * Creates a new instance of {@code InMemoryNodeModel} which is initialized
     * with an empty root node.
     */
    public InMemoryNodeModel()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code InMemoryNodeModel} and initializes it
     * from the given root node. If the passed in node is <b>null</b>, a new,
     * empty root node is created.
     *
     * @param root the new root node for this model
     */
    public InMemoryNodeModel(ImmutableNode root)
    {
        structure =
                new AtomicReference<TreeData>(
                        createTreeData(initialRootNode(root)));
    }

    /**
     * Returns the root node of this model.
     *
     * @return the root node
     */
    public ImmutableNode getRootNode()
    {
        return getTreeData().getRoot();
    }

    /**
     * Returns a {@code NodeHandler} for dealing with the nodes managed by this
     * model.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<ImmutableNode> getNodeHandler()
    {
        return this;
    }

    public String nodeName(ImmutableNode node)
    {
        return node.getNodeName();
    }

    public Object getValue(ImmutableNode node)
    {
        return node.getValue();
    }

    /**
     * {@inheritDoc} This implementation uses internal mapping information to
     * determine the parent node of the given node. If the passed in node is the
     * root node of this model, result is <b>null</b>. If the node is not part
     * of this model, an exception is thrown. Otherwise, the parent node is
     * returned.
     *
     * @throws IllegalArgumentException if the passed in node does not belong to
     *         this model
     */
    public ImmutableNode getParent(ImmutableNode node)
    {
        return getTreeData().getParent(node);
    }

    public List<ImmutableNode> getChildren(ImmutableNode node)
    {
        return node.getChildren();
    }

    /**
     * {@inheritDoc} This implementation returns an immutable list with all
     * child nodes that have the specified name.
     */
    public List<ImmutableNode> getChildren(ImmutableNode node, String name)
    {
        List<ImmutableNode> result =
                new ArrayList<ImmutableNode>(node.getChildren().size());
        for (ImmutableNode c : node.getChildren())
        {
            if (StringUtils.equals(name, c.getNodeName()))
            {
                result.add(c);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public ImmutableNode getChild(ImmutableNode node, int index)
    {
        return node.getChildren().get(index);
    }

    public int indexOfChild(ImmutableNode parent, ImmutableNode child)
    {
        return parent.getChildren().indexOf(child);
    }

    public int getChildrenCount(ImmutableNode node, String name)
    {
        if (name == null)
        {
            return node.getChildren().size();
        }
        else
        {
            return getChildren(node, name).size();
        }
    }

    public Set<String> getAttributes(ImmutableNode node)
    {
        return node.getAttributes().keySet();
    }

    public boolean hasAttributes(ImmutableNode node)
    {
        return !node.getAttributes().isEmpty();
    }

    public Object getAttributeValue(ImmutableNode node, String name)
    {
        return node.getAttributes().get(name);
    }

    /**
     * {@inheritDoc} This implementation assumes that a node is defined if it
     * has a value or has children or has attributes.
     */
    public boolean isDefined(ImmutableNode node)
    {
        return checkIfNodeDefined(node);
    }

    /**
     * Adds a new property to this node model consisting of an arbitrary number
     * of values. The key for the add operation is provided. For each value a
     * new node has to be added.
     *
     * @param key the key
     * @param values the values to be added at the position defined by the key
     * @param resolver the {@code NodeKeyResolver}
     */
    public void addProperty(final String key, final Iterable<?> values,
            final NodeKeyResolver resolver)
    {
        if (valuesNotEmpty(values))
        {
            updateModel(new TransactionInitializer() {
                public boolean initTransaction(ModelTransaction tx) {
                    initializeAddTransaction(tx, key, values, resolver);
                    return true;
                }
            });
        }
    }

    /**
     * Adds a collection of new nodes to this model. This operation corresponds
     * to the {@code addNodes()} method of the {@code Configuration} interface.
     * The new nodes are either added to an existing node (if the passed in key
     * selects exactly one node) or to a newly created node.
     *
     * @param key the key
     * @param nodes the collection of nodes to be added (may be <b>null</b>)
     * @param resolver the {@code NodeKeyResolver}
     * @throws IllegalArgumentException if the key references an attribute (of
     *         course, it is not possible to add something to an attribute)
     */
    public void addNodes(final String key,
            final Collection<ImmutableNode> nodes,
            final NodeKeyResolver resolver)
    {
        if (nodes != null && !nodes.isEmpty())
        {
            updateModel(new TransactionInitializer()
            {
                public boolean initTransaction(ModelTransaction tx)
                {
                    List<QueryResult<ImmutableNode>> results =
                            resolver.resolveKey(tx.getCurrentData().getRoot(),
                                    key, InMemoryNodeModel.this);
                    if (results.size() == 1)
                    {
                        if (results.get(0).isAttributeResult())
                        {
                            throw new IllegalArgumentException(
                                    "New nodes cannot be added to an attribute key: "
                                            + key);
                        }
                        tx.addAddNodesOperation(results.get(0).getNode(), nodes);
                    }
                    else
                    {
                        NodeAddData<ImmutableNode> addData =
                                resolver.resolveAddKey(tx.getCurrentData()
                                        .getRoot(), key, InMemoryNodeModel.this);
                        ImmutableNode newNode =
                                new ImmutableNode.Builder(nodes.size())
                                        .name(addData.getNewNodeName())
                                        .addChildren(nodes).create();
                        addNodesByAddData(tx, addData,
                                Collections.singleton(newNode));
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Changes the value of a property. This is a more complex operation as it
     * might involve adding, updating, or deleting nodes and attributes from the
     * model. The object representing the new value is passed to the
     * {@code NodeKeyResolver} which will produce a corresponding
     * {@link NodeUpdateData} object. Based on the content of this object,
     * update operations are performed.
     *
     * @param key the key
     * @param value the new value for this property (to be evaluated by the
     *        {@code NodeKeyResolver})
     * @param resolver the {@code NodeKeyResolver}
     */
    public void setProperty(final String key, final Object value,
            final NodeKeyResolver resolver)
    {
        updateModel(new TransactionInitializer()
        {
            public boolean initTransaction(ModelTransaction tx)
            {
                boolean added = false;
                NodeUpdateData<ImmutableNode> updateData =
                        resolver.resolveUpdateKey(
                                tx.getCurrentData().getRoot(), key, value,
                                InMemoryNodeModel.this);
                if (!updateData.getNewValues().isEmpty())
                {
                    initializeAddTransaction(tx, key,
                            updateData.getNewValues(), resolver);
                    added = true;
                }
                boolean cleared =
                        initializeClearTransaction(tx,
                                updateData.getRemovedNodes());
                boolean updated =
                        initializeUpdateTransaction(tx,
                                updateData.getChangedValues());
                return added || cleared || updated;
            }
        });
    }

    /**
     * Removes the sub trees defined by the given key from this model. All nodes
     * selected by this key are retrieved and removed from the model. If this
     * causes other nodes to become undefined, they are removed, too.
     *
     * @param key the key selecting the properties to be removed
     * @param resolver the {@code NodeKeyResolver}
     */
    public void clearTree(final String key, final NodeKeyResolver resolver)
    {
        updateModel(new TransactionInitializer()
        {
            public boolean initTransaction(ModelTransaction tx)
            {
                TreeData currentStructure = tx.getCurrentData();
                for (QueryResult<ImmutableNode> result : resolver
                        .resolveKey(currentStructure.getRoot(), key,
                                InMemoryNodeModel.this))
                {
                    if (result.isAttributeResult())
                    {
                        tx.addRemoveAttributeOperation(result.getNode(),
                                result.getAttributeName());
                    }
                    else
                    {
                        if (result.getNode() == currentStructure.getRoot())
                        {
                            // the whole model is to be cleared
                            clear();
                            return false;
                        }
                        tx.addRemoveNodeOperation(
                                currentStructure.getParent(result.getNode()),
                                result.getNode());
                    }
                }
                return true;
            }
        });
    }

    /**
     * Clears the value of a property. This method is similar to
     * {@link #clearTree(String, NodeKeyResolver)}: However, the nodes
     * referenced by the passed in key are not removed completely, but only
     * their value is set to <b>null</b>. (If this operation leaves the affected
     * node in an undefined state, it is indeed removed.)
     *
     * @param key the key selecting the properties to be cleared
     * @param resolver the {@code NodeKeyResolver}
     */
    public void clearProperty(final String key, final NodeKeyResolver resolver)
    {
        updateModel(new TransactionInitializer() {
            public boolean initTransaction(ModelTransaction tx) {
                List<QueryResult<ImmutableNode>> results =
                        resolver.resolveKey(tx.getCurrentData().getRoot(), key,
                                InMemoryNodeModel.this);
                initializeClearTransaction(tx, results);
                return true;
            }
        });
    }

    /**
     * Removes all data from this model. A new empty root node is created with
     * the same name as the current root node. Implementation note: Because this
     * is a hard reset the usual dance for dealing with concurrent updates is
     * not required here.
     */
    public void clear()
    {
        ImmutableNode newRoot =
                new ImmutableNode.Builder().name(getRootNode().getNodeName())
                        .create();
        structure.set(new TreeData(newRoot, Collections
                .<ImmutableNode, ImmutableNode>emptyMap(), Collections
                .<ImmutableNode, ImmutableNode>emptyMap()));
    }

    /**
     * Sets a new root node for this model. The whole structure is replaced by
     * the new node and its children. Care has to be taken when this method is
     * used and the model is accessed by multiple threads. It is not
     * deterministic which concurrent operations see the old root and which see
     * the new root node.
     *
     * @param newRoot the new root node to be set (can be <b>null</b>, then an
     *        empty root node is set)
     */
    public void setRoot(ImmutableNode newRoot)
    {
        structure.set(createTreeData(initialRootNode(newRoot)));
    }

    /**
     * Returns the current {@code TreeData} object. This object contains all
     * information about the current node structure.
     *
     * @return the current {@code TreeData} object
     */
    TreeData getTreeData()
    {
        return structure.get();
    }

    /**
     * Updates the mapping from nodes to their parents for the passed in
     * hierarchy of nodes. This method traverses all children and grand-children
     * of the passed in root node. For each node in the subtree the parent
     * relation is added to the map.
     *
     * @param parents the map with parent nodes
     * @param root the root node of the current tree
     */
    void updateParentMapping(final Map<ImmutableNode, ImmutableNode> parents,
            ImmutableNode root)
    {
        NodeTreeWalker.INSTANCE.walkBFS(root,
                new ConfigurationNodeVisitorAdapter<ImmutableNode>()
                {
                    public void visitBeforeChildren(ImmutableNode node,
                            NodeHandler<ImmutableNode> handler)
                    {
                        for (ImmutableNode c : node.getChildren())
                        {
                            parents.put(c, node);
                        }
                    }
                }, this);
    }

    /**
     * Checks if the passed in node is defined. Result is <b>true</b> if the
     * node contains any data.
     *
     * @param node the node in question
     * @return <b>true</b> if the node is defined, <b>false</b> otherwise
     */
    static boolean checkIfNodeDefined(ImmutableNode node)
    {
        return node.getValue() != null || !node.getChildren().isEmpty()
                || !node.getAttributes().isEmpty();
    }

    /**
     * Initializes a transaction for an add operation.
     *
     * @param tx the transaction to be initialized
     * @param key the key
     * @param values the collection with node values
     * @param resolver the {@code NodeKeyResolver}
     */
    private void initializeAddTransaction(ModelTransaction tx, String key,
            Iterable<?> values, NodeKeyResolver resolver)
    {
        NodeAddData<ImmutableNode> addData =
                resolver.resolveAddKey(tx.getCurrentData().getRoot(), key, this);
        if (addData.isAttribute())
        {
            addAttributeProperty(tx, addData, values);
        }
        else
        {
            addNodeProperty(tx, addData, values);
        }
    }

    /**
     * Creates a {@code TreeData} object for the specified root node.
     *
     * @param root the root node of the current tree
     * @return the {@code TreeData} describing the current tree
     */
    private TreeData createTreeData(ImmutableNode root)
    {
        return new TreeData(root, createParentMapping(root),
                new HashMap<ImmutableNode, ImmutableNode>());
    }

    /**
     * Handles an add property operation if the property to be added is a node.
     *
     * @param tx the transaction
     * @param addData the {@code NodeAddData}
     * @param values the collection with node values
     */
    private static void addNodeProperty(ModelTransaction tx,
            NodeAddData<ImmutableNode> addData, Iterable<?> values)
    {
        Collection<ImmutableNode> newNodes =
                createNodesToAdd(addData.getNewNodeName(), values);
        addNodesByAddData(tx, addData, newNodes);
    }

    /**
     * Initializes a transaction to add a collection of nodes as described by a
     * {@code NodeAddData} object. If necessary, new path nodes are created.
     * Eventually, the new nodes are added as children to the specified target
     * node.
     *
     * @param tx the transaction
     * @param addData the {@code NodeAddData}
     * @param newNodes the collection of new child nodes
     */
    private static void addNodesByAddData(ModelTransaction tx,
            NodeAddData<ImmutableNode> addData,
            Collection<ImmutableNode> newNodes)
    {
        if (addData.getPathNodes().isEmpty())
        {
            tx.addAddNodesOperation(addData.getParent(), newNodes);
        }
        else
        {
            ImmutableNode newChild = createNodeToAddWithPath(addData, newNodes);
            tx.addAddNodeOperation(addData.getParent(), newChild);
        }
    }

    /**
     * Handles an add property operation if the property to be added is an
     * attribute.
     *
     * @param tx the transaction
     * @param addData the {@code NodeAddData}
     * @param values the collection with node values
     */
    private static void addAttributeProperty(ModelTransaction tx,
            NodeAddData<ImmutableNode> addData, Iterable<?> values)
    {
        if (addData.getPathNodes().isEmpty())
        {
            tx.addAttributeOperation(addData.getParent(),
                    addData.getNewNodeName(), values.iterator().next());
        }
        else
        {
            int pathNodeCount = addData.getPathNodes().size();
            ImmutableNode childWithAttribute =
                    new ImmutableNode.Builder()
                            .name(addData.getPathNodes().get(pathNodeCount - 1))
                            .addAttribute(addData.getNewNodeName(),
                                    values.iterator().next()).create();
            ImmutableNode newChild =
                    (pathNodeCount > 1) ? createNodeOnPath(addData
                            .getPathNodes().subList(0, pathNodeCount - 1)
                            .iterator(),
                            Collections.singleton(childWithAttribute))
                            : childWithAttribute;
            tx.addAddNodeOperation(addData.getParent(), newChild);
        }
    }

    /**
     * Creates a collection with new nodes with a given name and a value from a
     * given collection.
     *
     * @param newNodeName the name of the new nodes
     * @param values the collection with node values
     * @return the newly created collection
     */
    private static Collection<ImmutableNode> createNodesToAdd(
            String newNodeName, Iterable<?> values)
    {
        Collection<ImmutableNode> nodes = new LinkedList<ImmutableNode>();
        for (Object value : values)
        {
            nodes.add(new ImmutableNode.Builder().name(newNodeName)
                    .value(value).create());
        }
        return nodes;
    }

    /**
     * Creates a node structure consisting of the path nodes defined by the
     * passed in {@code NodeAddData} instance and all new child nodes.
     *
     * @param addData the {@code NodeAddData}
     * @param newNodes the collection of new child nodes
     * @return the parent node of the newly created hierarchy
     */
    private static ImmutableNode createNodeToAddWithPath(
            NodeAddData<ImmutableNode> addData,
            Collection<ImmutableNode> newNodes)
    {
        return createNodeOnPath(addData.getPathNodes().iterator(), newNodes);
    }

    /**
     * Recursive helper method for creating a path node for an add operation.
     * All path nodes except for the last have a single child. The last path
     * node has the new nodes as children.
     *
     * @param it the iterator over the names of the path nodes
     * @param newNodes the collection of new child nodes
     * @return the newly created path node
     */
    private static ImmutableNode createNodeOnPath(Iterator<String> it,
            Collection<ImmutableNode> newNodes)
    {
        String nodeName = it.next();
        ImmutableNode.Builder builder;
        if (it.hasNext())
        {
            builder = new ImmutableNode.Builder(1);
            builder.addChild(createNodeOnPath(it, newNodes));
        }
        else
        {
            builder = new ImmutableNode.Builder(newNodes.size());
            builder.addChildren(newNodes);
        }
        return builder.name(nodeName).create();
    }

    /**
     * Initializes a transaction to clear the values of a property based on the
     * passed in collection of affected results.
     *
     * @param tx the transaction to be initialized
     * @param results a collection with results pointing to the nodes to be
     *        cleared
     * @return a flag whether there are elements to be cleared
     */
    private static boolean initializeClearTransaction(ModelTransaction tx,
            Collection<QueryResult<ImmutableNode>> results)
    {
        for (QueryResult<ImmutableNode> result : results)
        {
            if (result.isAttributeResult())
            {
                tx.addRemoveAttributeOperation(result.getNode(),
                        result.getAttributeName());
            }
            else
            {
                tx.addClearNodeValueOperation(result.getNode());
            }
        }

        return !results.isEmpty();
    }

    /**
     * Initializes a transaction to change the values of some query results
     * based on the passed in map.
     *
     * @param tx the transaction to be initialized
     * @param changedValues the map defining the elements to be changed
     * @return a flag whether there are elements to be updated
     */
    private static boolean initializeUpdateTransaction(ModelTransaction tx,
            Map<QueryResult<ImmutableNode>, Object> changedValues)
    {
        for (Map.Entry<QueryResult<ImmutableNode>, Object> e : changedValues
                .entrySet())
        {
            if (e.getKey().isAttributeResult())
            {
                tx.addAttributeOperation(e.getKey().getNode(), e.getKey()
                        .getAttributeName(), e.getValue());
            }
            else
            {
                tx.addChangeNodeValueOperation(e.getKey().getNode(),
                        e.getValue());
            }
        }

        return !changedValues.isEmpty();
    }

    /**
     * Determines the initial root node of this model. If a root node has been
     * provided, it is used. Otherwise, an empty dummy root node is created.
     *
     * @param providedRoot the passed in root node
     * @return the root node to be used
     */
    private static ImmutableNode initialRootNode(ImmutableNode providedRoot)
    {
        return (providedRoot != null) ? providedRoot
                : new ImmutableNode.Builder().create();
    }

    /**
     * Creates the mapping to parent nodes for the nodes structured represented
     * by the passed in root node. Each node is assigned its parent node. Here
     * an iterative algorithm is used rather than a recursive one to avoid stack
     * overflow for huge structures.
     *
     * @param root the root node of the structure
     * @return the parent node mapping
     */
    private Map<ImmutableNode, ImmutableNode> createParentMapping(
            ImmutableNode root)
    {
        Map<ImmutableNode, ImmutableNode> parents =
                new HashMap<ImmutableNode, ImmutableNode>();
        updateParentMapping(parents, root);
        return parents;
    }

    /**
     * Performs a non-blocking, thread-safe update of this model based on a
     * transaction initialized by the passed in initializer. This method uses
     * the atomic reference for the model's current data to ensure that an
     * update was successful even if the model is concurrently accessed.
     *
     * @param txInit the {@code TransactionInitializer}
     */
    private void updateModel(TransactionInitializer txInit)
    {
        boolean done;

        do
        {
            ModelTransaction tx = new ModelTransaction(this);
            if (!txInit.initTransaction(tx))
            {
                done = true;
            }
            else
            {
                TreeData newData = tx.execute();
                done = structure.compareAndSet(tx.getCurrentData(), newData);
            }
        } while (!done);
    }

    /**
     * Checks whether the specified collection with values is not empty.
     *
     * @param values the collection with node values
     * @return <b>true</b> if values are provided, <b>false</b> otherwise
     */
    private static boolean valuesNotEmpty(Iterable<?> values)
    {
        return values.iterator().hasNext();
    }

    /**
     * An internally used helper class for storing information about the managed
     * node structure. An instance of this class represents the current tree. It
     * stores the current root node and additional information which is not part
     * of the {@code ImmutableNode} class.
     */
    static class TreeData
    {
        /** The root node of the tree. */
        private final ImmutableNode root;

        /** A map that associates the parent node to each node. */
        private final Map<ImmutableNode, ImmutableNode> parentMapping;

        /**
         * Stores information about nodes which have been replaced by
         * manipulations of the structure. This map is used to avoid that the
         * parent mapping has to be updated after each change.
         */
        private final Map<ImmutableNode, ImmutableNode> replacementMapping;

        /** An inverse replacement mapping. */
        private final Map<ImmutableNode, ImmutableNode> inverseReplacementMapping;

        /**
         * Creates a new instance of {@code TreeData} and initializes it with
         * all data to be stored.
         *
         * @param root the root node of the current tree
         * @param parentMapping the mapping to parent nodes
         * @param replacements the map with the nodes that have been replaced
         */
        public TreeData(ImmutableNode root,
                Map<ImmutableNode, ImmutableNode> parentMapping,
                Map<ImmutableNode, ImmutableNode> replacements)
        {
            this.root = root;
            this.parentMapping = parentMapping;
            replacementMapping = replacements;
            inverseReplacementMapping = createInverseMapping(replacements);
        }

        /**
         * Returns the root node.
         *
         * @return the current root node
         */
        public ImmutableNode getRoot()
        {
            return root;
        }

        /**
         * Returns the parent node of the specified node. Result is <b>null</b>
         * for the root node. If the passed in node cannot be resolved, an
         * exception is thrown.
         *
         * @param node the node in question
         * @return the parent node for this node
         * @throws IllegalArgumentException if the node cannot be resolved
         */
        public ImmutableNode getParent(ImmutableNode node)
        {
            if (node == getRoot())
            {
                return null;
            }
            ImmutableNode org = handleReplacements(node, inverseReplacementMapping);

            ImmutableNode parent = parentMapping.get(org);
            if (parent == null)
            {
                throw new IllegalArgumentException("Cannot determine parent! "
                        + node + " is not part of this model.");
            }
            return handleReplacements(parent, replacementMapping);
        }

        /**
         * Returns a copy of the mapping from nodes to their parents.
         *
         * @return the copy of the parent mapping
         */
        public Map<ImmutableNode, ImmutableNode> copyParentMapping()
        {
            return new HashMap<ImmutableNode, ImmutableNode>(parentMapping);
        }

        /**
         * Returns a copy of the map storing the replaced nodes.
         *
         * @return the copy of the replacement mapping
         */
        public Map<ImmutableNode, ImmutableNode> copyReplacementMapping()
        {
            return new HashMap<ImmutableNode, ImmutableNode>(replacementMapping);
        }

        /**
         * Checks whether the passed in node is subject of a replacement by
         * another one. If so, the other node is returned. This is done until a
         * node is found which had not been replaced. Updating the parent
         * mapping may be expensive for large node structures. Therefore, it
         * initially remains constant, and a map with replacements is used. When
         * querying a parent node, the replacement map has to be consulted
         * whether the parent node is still valid.
         *
         * @param replace the replacement node
         * @param mapping the replacement mapping
         * @return the corresponding node according to the mapping
         */
        private static ImmutableNode handleReplacements(ImmutableNode replace,
                Map<ImmutableNode, ImmutableNode> mapping)
        {
            ImmutableNode node = replace;
            ImmutableNode org;
            do
            {
                org = mapping.get(node);
                if (org != null)
                {
                    node = org;
                }
            } while (org != null);
            return node;
        }

        /**
         * Creates the inverse replacement mapping.
         *
         * @param replacements the original replacement mapping
         * @return the inverse replacement mapping
         */
        private Map<ImmutableNode, ImmutableNode> createInverseMapping(
                Map<ImmutableNode, ImmutableNode> replacements)
        {
            Map<ImmutableNode, ImmutableNode> inverseMapping =
                    new HashMap<ImmutableNode, ImmutableNode>();
            for (Map.Entry<ImmutableNode, ImmutableNode> e : replacements
                    .entrySet())
            {
                inverseMapping.put(e.getValue(), e.getKey());
            }
            return inverseMapping;
        }
    }

    /**
     * An interface used internally for handling concurrent updates. An
     * implementation has to populate the passed in {@code ModelTransaction}.
     * The transaction is then executed, and an atomic update of the model's
     * {@code TreeData} is attempted. If this fails - because another update
     * came across -, the whole operation has to be tried anew.
     */
    private static interface TransactionInitializer
    {
        /**
         * Initializes the specified transaction for an update operation. The
         * return value indicates whether the transaction should be executed. A
         * result of <b>false</b> means that the update is to be aborted (maybe
         * another update method was called).
         *
         * @param tx the transaction to be initialized
         * @return a flag whether the update should continue
         */
        boolean initTransaction(ModelTransaction tx);
    }
}
