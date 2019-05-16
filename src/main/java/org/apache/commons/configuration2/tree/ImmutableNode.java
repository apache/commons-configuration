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
package org.apache.commons.configuration2.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * An immutable default implementation for configuration nodes.
 * </p>
 * <p>
 * This class is used for an in-memory representation of hierarchical
 * configuration data. It stores typical information like a node name, a value,
 * child nodes, or attributes.
 * </p>
 * <p>
 * After their creation, instances cannot be manipulated. There are methods for
 * updating properties, but these methods return new {@code ImmutableNode}
 * instances. Instances are created using the nested {@code Builder} class.
 * </p>
 *
 * @since 2.0
 */
public final class ImmutableNode
{
    /** The name of this node. */
    private final String nodeName;

    /** The value of this node. */
    private final Object value;

    /** A collection with the child nodes of this node. */
    private final List<ImmutableNode> children;

    /** A map with the attributes of this node. */
    private final Map<String, Object> attributes;

    /**
     * Creates a new instance of {@code ImmutableNode} from the given
     * {@code Builder} object.
     *
     * @param b the {@code Builder}
     */
    private ImmutableNode(final Builder b)
    {
        children = b.createChildren();
        attributes = b.createAttributes();
        nodeName = b.name;
        value = b.value;
    }

    /**
     * Returns the name of this node.
     *
     * @return the name of this node
     */
    public String getNodeName()
    {
        return nodeName;
    }

    /**
     * Returns the value of this node.
     *
     * @return the value of this node
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Returns a list with the children of this node. This list cannot be
     * modified.
     *
     * @return a list with the child nodes
     */
    public List<ImmutableNode> getChildren()
    {
        return children;
    }

    /**
     * Returns a list with the children of this node.
     *
     * @param name the node name to find
     *
     * @return a list with the child nodes
     */
    public List<ImmutableNode> getChildren(final String name)
    {
        final List<ImmutableNode> list = new ArrayList<>();
        if (name == null)
        {
            return list;
        }
        for (final ImmutableNode node : children)
        {
            if (name.equals(node.getNodeName()))
            {
                list.add(node);
            }
        }
        return list;
    }

    /**
     * Returns a map with the attributes of this node. This map cannot be
     * modified.
     *
     * @return a map with this node's attributes
     */
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    /**
     * Creates a new {@code ImmutableNode} instance which is a copy of this
     * object with the name changed to the passed in value.
     *
     * @param name the name of the newly created node
     * @return the new node with the changed name
     */
    public ImmutableNode setName(final String name)
    {
        return new Builder(children, attributes).name(name).value(value)
                .create();
    }

    /**
     * Creates a new {@code ImmutableNode} instance which is a copy of this
     * object with the value changed to the passed in value.
     *
     * @param newValue the value of the newly created node
     * @return the new node with the changed value
     */
    public ImmutableNode setValue(final Object newValue)
    {
        return new Builder(children, attributes).name(nodeName).value(newValue)
                .create();
    }

    /**
     * Creates a new {@code ImmutableNode} instance which is a copy of this
     * object, but has the given child node added.
     *
     * @param child the child node to be added (must not be <b>null</b>)
     * @return the new node with the child node added
     * @throws IllegalArgumentException if the child node is <b>null</b>
     */
    public ImmutableNode addChild(final ImmutableNode child)
    {
        checkChildNode(child);
        final Builder builder = new Builder(children.size() + 1, attributes);
        builder.addChildren(children).addChild(child);
        return createWithBasicProperties(builder);
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with the given child node removed. If the child node does not
     * belong to this node, the same node instance is returned.
     *
     * @param child the child node to be removed
     * @return the new node with the child node removed
     */
    public ImmutableNode removeChild(final ImmutableNode child)
    {
        // use same size of children in case the child does not exist
        final Builder builder = new Builder(children.size(), attributes);
        boolean foundChild = false;
        for (final ImmutableNode c : children)
        {
            if (c == child)
            {
                foundChild = true;
            }
            else
            {
                builder.addChild(c);
            }
        }

        return foundChild ? createWithBasicProperties(builder) : this;
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with the given child replaced by the new one. If the child to
     * be replaced cannot be found, the same node instance is returned.
     *
     * @param oldChild the child node to be replaced
     * @param newChild the replacing child node (must not be <b>null</b>)
     * @return the new node with the child replaced
     * @throws IllegalArgumentException if the new child node is <b>null</b>
     */
    public ImmutableNode replaceChild(final ImmutableNode oldChild,
            final ImmutableNode newChild)
    {
        checkChildNode(newChild);
        final Builder builder = new Builder(children.size(), attributes);
        boolean foundChild = false;
        for (final ImmutableNode c : children)
        {
            if (c == oldChild)
            {
                builder.addChild(newChild);
                foundChild = true;
            }
            else
            {
                builder.addChild(c);
            }
        }

        return foundChild ? createWithBasicProperties(builder) : this;
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with the children replaced by the ones in the passed in
     * collection. With this method all children can be replaced in a single
     * step. For the collection the same rules apply as for
     * {@link Builder#addChildren(Collection)}.
     *
     * @param newChildren the collection with the new children (may be
     *        <b>null</b>)
     * @return the new node with replaced children
     */
    public ImmutableNode replaceChildren(final Collection<ImmutableNode> newChildren)
    {
        final Builder builder = new Builder(null, attributes);
        builder.addChildren(newChildren);
        return createWithBasicProperties(builder);
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with the specified attribute set to the given value. If an
     * attribute with this name does not exist, it is created now. Otherwise,
     * the new value overrides the old one.
     *
     * @param name the name of the attribute
     * @param value the attribute value
     * @return the new node with this attribute
     */
    public ImmutableNode setAttribute(final String name, final Object value)
    {
        final Map<String, Object> newAttrs = new HashMap<>(attributes);
        newAttrs.put(name, value);
        return createWithNewAttributes(newAttrs);
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with all attributes added defined by the given map. This
     * method is analogous to {@link #setAttribute(String, Object)}, but all
     * attributes in the given map are added. If the map is <b>null</b> or
     * empty, this method has no effect.
     *
     * @param newAttributes the map with attributes to be added
     * @return the new node with these attributes
     */
    public ImmutableNode setAttributes(final Map<String, ?> newAttributes)
    {
        if (newAttributes == null || newAttributes.isEmpty())
        {
            return this;
        }

        final Map<String, Object> newAttrs = new HashMap<>(attributes);
        newAttrs.putAll(newAttributes);
        return createWithNewAttributes(newAttrs);
    }

    /**
     * Returns a new {@code ImmutableNode} instance which is a copy of this
     * object, but with the specified attribute removed. If there is no
     * attribute with the given name, the same node instance is returned.
     *
     * @param name the name of the attribute
     * @return the new node without this attribute
     */
    public ImmutableNode removeAttribute(final String name)
    {
        final Map<String, Object> newAttrs = new HashMap<>(attributes);
        if (newAttrs.remove(name) != null)
        {
            return createWithNewAttributes(newAttrs);
        }
        return this;
    }

    /**
     * Initializes the given builder with basic properties (node name and value)
     * and returns the newly created node. This is a helper method for updating
     * a node when only children or attributes are affected.
     *
     * @param builder the already prepared builder
     * @return the newly created node
     */
    private ImmutableNode createWithBasicProperties(final Builder builder)
    {
        return builder.name(nodeName).value(value).create();
    }

    /**
     * Creates a new {@code ImmutableNode} instance with the same properties as
     * this object, but with the given new attributes.
     *
     * @param newAttrs the new attributes
     * @return the new node instance
     */
    private ImmutableNode createWithNewAttributes(final Map<String, Object> newAttrs)
    {
        return createWithBasicProperties(new Builder(children, null)
                .addAttributes(newAttrs));
    }

    /**
     * Checks whether the given child node is not null. This check is done at
     * multiple places to ensure that newly added child nodes are always
     * defined.
     *
     * @param child the child node to be checked
     * @throws IllegalArgumentException if the child node is <b>null</b>
     */
    private static void checkChildNode(final ImmutableNode child)
    {
        if (child == null)
        {
            throw new IllegalArgumentException("Child node must not be null!");
        }
    }

    /**
     * <p>
     * A <em>builder</em> class for creating instances of {@code ImmutableNode}.
     * </p>
     * <p>
     * This class can be used to set all properties of an immutable node
     * instance. Eventually call the {@code create()} method to obtain the
     * resulting instance.
     * </p>
     * <p>
     * Implementation note: This class is not thread-safe. It is intended to be
     * used to define a single node instance only.
     * </p>
     */
    public static final class Builder
    {
        /** The direct list of children of the new node. */
        private final List<ImmutableNode> directChildren;

        /** The direct map of attributes of the new node. */
        private final Map<String, Object> directAttributes;

        /**
         * A list for the children of the new node. This list is populated by
         * the {@code addChild()} method.
         */
        private List<ImmutableNode> children;

        /**
         * A map for storing the attributes of the new node. This map is
         * populated by {@code addAttribute()}.
         */
        private Map<String, Object> attributes;

        /** The name of the node. */
        private String name;

        /** The value of the node. */
        private Object value;

        /**
         * Creates a new instance of {@code Builder} which does not contain any
         * property definitions yet.
         */
        public Builder()
        {
            this(null, null);
        }

        /**
         * Creates a new instance of {@code Builder} and sets the number of
         * expected child nodes. Using this constructor helps the class to
         * create a properly sized list for the child nodes to be added.
         *
         * @param childCount the number of child nodes
         */
        public Builder(final int childCount)
        {
            this();
            initChildrenCollection(childCount);
        }

        /**
         * Creates a new instance of {@code Builder} and initializes the
         * children and attributes of the new node. This constructor is used
         * internally by the {@code ImmutableNode} class for creating instances
         * derived from another node. The passed in collections are passed
         * directly to the newly created instance; thus they already need to be
         * immutable. (Background is that the creation of intermediate objects
         * is to be avoided.)
         *
         * @param dirChildren the children of the new node
         * @param dirAttrs the attributes of the new node
         */
        private Builder(final List<ImmutableNode> dirChildren,
                final Map<String, Object> dirAttrs)
        {
            directChildren = dirChildren;
            directAttributes = dirAttrs;
        }

        /**
         * Creates a new instance of {@code Builder} and initializes the
         * attributes of the new node and prepares the collection for the
         * children. This constructor is used internally by methods of
         * {@code ImmutableNode} which update the node and change the children.
         * The new number of child nodes can be passed so that the collection
         * for the new children can be created with an appropriate size.
         *
         * @param childCount the expected number of new children
         * @param dirAttrs the attributes of the new node
         */
        private Builder(final int childCount, final Map<String, Object> dirAttrs)
        {
            this(null, dirAttrs);
            initChildrenCollection(childCount);
        }

        /**
         * Sets the name of the node to be created.
         *
         * @param n the node name
         * @return a reference to this object for method chaining
         */
        public Builder name(final String n)
        {
            name = n;
            return this;
        }

        /**
         * Sets the value of the node to be created.
         *
         * @param v the value
         * @return a reference to this object for method chaining
         */
        public Builder value(final Object v)
        {
            value = v;
            return this;
        }

        /**
         * Adds a child node to this builder. The passed in node becomes a child
         * of the newly created node. If it is <b>null</b>, it is ignored.
         *
         * @param c the child node (must not be <b>null</b>)
         * @return a reference to this object for method chaining
         */
        public Builder addChild(final ImmutableNode c)
        {
            if (c != null)
            {
                ensureChildrenExist();
                children.add(c);
            }
            return this;
        }

        /**
         * Adds multiple child nodes to this builder. This method works like
         * {@link #addChild(ImmutableNode)}, but it allows setting a number of
         * child nodes at once.
         *
         *
         * @param children a collection with the child nodes to be added
         * @return a reference to this object for method chaining
         */
        public Builder addChildren(final Collection<? extends ImmutableNode> children)
        {
            if (children != null)
            {
                ensureChildrenExist();
                this.children.addAll(filterNull(children));
            }
            return this;
        }

        /**
         * Adds an attribute to this builder. The passed in attribute key and
         * value are stored in an internal map. If there is already an attribute
         * with this name, it is overridden.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return a reference to this object for method chaining
         */
        public Builder addAttribute(final String name, final Object value)
        {
            ensureAttributesExist();
            attributes.put(name, value);
            return this;
        }

        /**
         * Adds all attributes of the given map to this builder. This method
         * works like {@link #addAttribute(String, Object)}, but it allows
         * setting multiple attributes at once.
         *
         * @param attrs the map with attributes to be added (may be <b>null</b>
         * @return a reference to this object for method chaining
         */
        public Builder addAttributes(final Map<String, ?> attrs)
        {
            if (attrs != null)
            {
                ensureAttributesExist();
                attributes.putAll(attrs);
            }
            return this;
        }

        /**
         * Creates a new {@code ImmutableNode} instance based on the properties
         * set for this builder.
         *
         * @return the newly created {@code ImmutableNode}
         */
        public ImmutableNode create()
        {
            final ImmutableNode newNode = new ImmutableNode(this);
            children = null;
            attributes = null;
            return newNode;
        }

        /**
         * Creates a list with the children of the newly created node. The list
         * returned here is always immutable. It depends on the way this builder
         * was populated.
         *
         * @return the list with the children of the new node
         */
        List<ImmutableNode> createChildren()
        {
            if (directChildren != null)
            {
                return directChildren;
            }
            if (children != null)
            {
                return Collections.unmodifiableList(children);
            }
            return Collections.emptyList();
        }

        /**
         * Creates a map with the attributes of the newly created node. This is
         * an immutable map. If direct attributes were set, they are returned.
         * Otherwise an unmodifiable map from the attributes passed to this
         * builder is constructed.
         *
         * @return a map with the attributes for the new node
         */
        private Map<String, Object> createAttributes()
        {
            if (directAttributes != null)
            {
                return directAttributes;
            }
            if (attributes != null)
            {
                return Collections.unmodifiableMap(attributes);
            }
            return Collections.emptyMap();
        }

        /**
         * Ensures that the collection for the child nodes exists. It is created
         * on demand.
         */
        private void ensureChildrenExist()
        {
            if (children == null)
            {
                children = new LinkedList<>();
            }
        }

        /**
         * Ensures that the map for the attributes exists. It is created on
         * demand.
         */
        private void ensureAttributesExist()
        {
            if (attributes == null)
            {
                attributes = new HashMap<>();
            }
        }

        /**
         * Creates the collection for child nodes based on the expected number
         * of children.
         *
         * @param childCount the expected number of new children
         */
        private void initChildrenCollection(final int childCount)
        {
            if (childCount > 0)
            {
                children = new ArrayList<>(childCount);
            }
        }

        /**
         * Filters null entries from the passed in collection with child nodes.
         *
         *
         * @param children the collection to be filtered
         * @return the collection with null entries removed
         */
        private static Collection<? extends ImmutableNode> filterNull(
                final Collection<? extends ImmutableNode> children)
        {
            final List<ImmutableNode> result =
                    new ArrayList<>(children.size());
            for (final ImmutableNode c : children)
            {
                if (c != null)
                {
                    result.add(c);
                }
            }
            return result;
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + "(" + nodeName + ")";
    }
}
