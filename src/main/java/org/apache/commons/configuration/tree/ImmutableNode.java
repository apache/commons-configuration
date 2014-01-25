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
 * @version $Id$
 */
public class ImmutableNode
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
    private ImmutableNode(Builder b)
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
        public Builder(int childCount)
        {
            this();
            children = new ArrayList<ImmutableNode>(childCount);
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
        private Builder(List<ImmutableNode> dirChildren,
                Map<String, Object> dirAttrs)
        {
            directChildren = dirChildren;
            directAttributes = dirAttrs;
        }

        /**
         * Sets the name of the node to be created.
         *
         * @param n the node name
         * @return a reference to this object for method chaining
         */
        public Builder name(String n)
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
        public Builder value(Object v)
        {
            value = v;
            return this;
        }

        /**
         * Adds a child node to this builder. The passed in node becomes a child
         * of the newly created node.
         *
         * @param c the child node
         * @return a reference to this object for method chaining
         */
        public Builder addChild(ImmutableNode c)
        {
            ensureChildrenExist();
            children.add(c);
            return this;
        }

        /**
         * Adds multiple child nodes to this builder. This method works like
         * {@link #addChild(ImmutableNode)}, but it allows setting a number of
         * child nodes at once.
         *
         * @param children a collection with the child nodes to be added
         * @return a reference to this object for method chaining
         */
        public Builder addChildren(Collection<ImmutableNode> children)
        {
            if (children != null)
            {
                ensureChildrenExist();
                this.children.addAll(children);
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
        public Builder addAttribute(String name, Object value)
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
        public Builder addAttributes(Map<String, ?> attrs)
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
            ImmutableNode newNode = new ImmutableNode(this);
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
            else
            {
                if (children != null)
                {
                    return Collections.unmodifiableList(children);
                }
                else
                {
                    return Collections.emptyList();
                }
            }
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
            else
            {
                if (attributes != null)
                {
                    return Collections.unmodifiableMap(attributes);
                }
                else
                {
                    return Collections.emptyMap();
                }
            }
        }

        /**
         * Ensures that the collection for the child nodes exists. It is created
         * on demand.
         */
        private void ensureChildrenExist()
        {
            if (children == null)
            {
                children = new LinkedList<ImmutableNode>();
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
                attributes = new HashMap<String, Object>();
            }
        }
    }
}
