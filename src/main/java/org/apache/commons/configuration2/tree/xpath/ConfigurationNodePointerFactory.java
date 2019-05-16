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
package org.apache.commons.configuration2.tree.xpath;

import java.util.Locale;

import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

/**
 * <p>
 * Implementation of the {@code NodePointerFactory} interface for configuration
 * nodes.
 * </p>
 * <p>
 * This class is able to create {@code NodePointer}s for the nodes of
 * hierarchical configurations. Because there is no common base class for
 * configuration nodes (any specific configuration implementation can use its
 * own node class) a trick is needed for activating this factory for a concrete
 * JXPath query: The {@code wrapNode()} method has to be called with the node
 * object and its corresponding {@code NodeHandler}. This creates a wrapper
 * object containing all information required by the factory for processing a
 * query. Then this wrapper object has to be passed to the query methods of the
 * JXPath context.
 * </p>
 *
 * @since 1.3
 */
public class ConfigurationNodePointerFactory implements NodePointerFactory
{
    /** Constant for the order of this factory. */
    public static final int CONFIGURATION_NODE_POINTER_FACTORY_ORDER = 200;

    /**
     * Returns the order of this factory between other factories.
     *
     * @return this order's factory
     */
    @Override
    public int getOrder()
    {
        return CONFIGURATION_NODE_POINTER_FACTORY_ORDER;
    }

    /**
     * Creates a node pointer for the specified bean. If the bean is a
     * configuration node (indicated by a wrapper object), a corresponding
     * pointer is returned.
     *
     * @param name the name of the node
     * @param bean the bean
     * @param locale the locale
     * @return a pointer for a configuration node if the bean is such a node
     */
    @Override
    @SuppressWarnings("unchecked")
    /* Type casts are safe here; because of the way the NodeWrapper was
       constructed the node handler must be compatible with the node.
     */
    public NodePointer createNodePointer(final QName name, final Object bean, final Locale locale)
    {
        if (bean instanceof NodeWrapper)
        {
            final NodeWrapper<?> wrapper = (NodeWrapper<?>) bean;
            return new ConfigurationNodePointer(wrapper.getNode(),
                    locale, wrapper.getNodeHandler());
        }
        return null;
    }

    /**
     * Creates a node pointer for the specified bean. If the bean is a
     * configuration node, a corresponding pointer is returned.
     *
     * @param parent the parent node
     * @param name the name
     * @param bean the bean
     * @return a pointer for a configuration node if the bean is such a node
     */
    @Override
    @SuppressWarnings("unchecked")
    /* Type casts are safe here, see above. Also, the hierarchy of node
       pointers is consistent, so a parent is compatible to a child.
     */
    public NodePointer createNodePointer(final NodePointer parent, final QName name,
            final Object bean)
    {
        if (bean instanceof NodeWrapper)
        {
            final NodeWrapper<?> wrapper = (NodeWrapper<?>) bean;
            return new ConfigurationNodePointer((ConfigurationNodePointer) parent,
                    wrapper.getNode(), wrapper.getNodeHandler());
        }
        return null;
    }

    /**
     * Creates a node wrapper for the specified node and its handler. This
     * wrapper has to be passed to the JXPath context instead of the original
     * node.
     *
     * @param <T> the type of the node
     * @param node the node
     * @param handler the corresponding node handler
     * @return a wrapper for this node
     */
    public static <T> Object wrapNode(final T node, final NodeHandler<T> handler)
    {
        return new NodeWrapper<>(node, handler);
    }

    /**
     * An internally used wrapper class that holds all information for
     * processing a query for a specific node.
     *
     * @param <T> the type of the nodes this class deals with
     */
    static class NodeWrapper<T>
    {
        /** Stores the node. */
        private final T node;

        /** Stores the corresponding node handler. */
        private final NodeHandler<T> nodeHandler;

        /**
         * Creates a new instance of {@code NodeWrapper} and initializes it.
         *
         * @param nd the node
         * @param handler the node handler
         */
        public NodeWrapper(final T nd, final NodeHandler<T> handler)
        {
            node = nd;
            nodeHandler = handler;
        }

        /**
         * Returns the wrapped node.
         *
         * @return the node
         */
        public T getNode()
        {
            return node;
        }

        /**
         * Returns the node handler for the wrapped node.
         *
         * @return the node handler
         */
        public NodeHandler<T> getNodeHandler()
        {
            return nodeHandler;
        }
    }
}
