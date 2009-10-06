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
package org.apache.commons.configuration2.base;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

/**
 * <p>
 * A specialized implementation of {@code HierarchicalConfigurationSource} that
 * operates on a structure of {@link ConfigurationNode} objects that are hold in
 * memory.
 * </p>
 * <p>
 * Implementation note: an {@code InMemoryConfigurationSource} can be queried
 * concurrently by multiple threads. However, if updates are performed, client
 * code must ensure proper synchronization.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class InMemoryConfigurationSource extends AbstractConfigurationSource
        implements HierarchicalConfigurationSource<ConfigurationNode>
{
    /**
     * The node handler used by this configuration source class. Because {@code
     * ConfigurationNodeHandler} is stateless an instance can be shared between
     * all {@code InMemoryConfigurationSource} instances.
     */
    private static final NodeHandler<ConfigurationNode> NODE_HANDLER = new ConfigurationNodeHandler();

    /** Stores the root configuration node. */
    private volatile ConfigurationNode rootNode;

    /**
     * Creates a new instance of {@code InMemoryConfigurationSource}.
     */
    public InMemoryConfigurationSource()
    {
        rootNode = new DefaultConfigurationNode();
    }

    /**
     * Returns a reference to the root node.
     *
     * @return the root configuration node
     */
    public ConfigurationNode getRootNode()
    {
        return rootNode;
    }

    /**
     * Sets the root node for this configuration source. An {@code
     * InMemoryConfigurationSource} allows changing its root node. This will
     * change the whole content of the source.
     *
     * @param root the new root node (must not be <b>null</b>)
     * @throws IllegalArgumentException if the root node is <b>null</b>
     */
    public void setRootNode(ConfigurationNode root)
    {
        if (root == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }

        rootNode = root;
    }

    /**
     * Removes all data from this configuration source. This implementation
     * simply creates a new, empty root node.
     */
    public void clear()
    {
        rootNode = new DefaultConfigurationNode();
    }

    /**
     * Returns the {@code NodeHandler} used by this configuration source.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<ConfigurationNode> getNodeHandler()
    {
        return NODE_HANDLER;
    }
}
