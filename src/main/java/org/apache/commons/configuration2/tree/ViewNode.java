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

import java.util.Iterator;

/**
 * <p>
 * A specialized node implementation to be used in view configurations.
 * </p>
 * <p>
 * Some configurations provide a logical view on the nodes of other
 * configurations. These configurations construct their own hierarchy of nodes
 * based on the node trees of their source configurations. This special node
 * class can be used for this purpose. It allows child nodes and attributes to
 * be added without changing their parent node. So a node can belong to a
 * hierarchy of nodes of a source configuration, but be also contained in a view
 * configuration.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public class ViewNode extends DefaultConfigurationNode
{
    /**
     * Adds an attribute to this view node. The new attribute's parent node will
     * be saved.
     *
     * @param attr the attribute node to be added
     */
    public void addAttribute(ConfigurationNode attr)
    {
        ConfigurationNode parent = null;

        if (attr != null)
        {
            parent = attr.getParentNode();
            super.addAttribute(attr);
            attr.setParentNode(parent);
        }
        else
        {
            throw new IllegalArgumentException("Attribute node must not be null!");
        }
    }

    /**
     * Adds a child node to this view node. The new child's parent node will be
     * saved.
     *
     * @param child the child node to be added
     */
    public void addChild(ConfigurationNode child)
    {
        ConfigurationNode parent = null;

        if (child != null)
        {
            parent = child.getParentNode();
            super.addChild(child);
            child.setParentNode(parent);
        }
        else
        {
            throw new IllegalArgumentException("Child node must not be null!");
        }
    }

    /**
     * Adds all attribute nodes of the given source node to this view node.
     *
     * @param source the source node
     */
    public void appendAttributes(ConfigurationNode source)
    {
        if (source != null)
        {
            for (Iterator it = source.getAttributes().iterator(); it.hasNext();)
            {
                addAttribute((ConfigurationNode) it.next());
            }
        }
    }

    /**
     * Adds all child nodes of the given source node to this view node.
     *
     * @param source the source node
     */
    public void appendChildren(ConfigurationNode source)
    {
        if (source != null)
        {
            for (Iterator it = source.getChildren().iterator(); it.hasNext();)
            {
                addChild((ConfigurationNode) it.next());
            }
        }
    }
}
