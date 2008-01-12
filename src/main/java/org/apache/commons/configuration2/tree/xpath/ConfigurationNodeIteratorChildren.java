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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.lang.StringUtils;

/**
 * A specialized iterator implementation for the child nodes of a configuration
 * node.
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
class ConfigurationNodeIteratorChildren extends ConfigurationNodeIteratorBase
{
    /**
     * Creates a new instance of <code>ConfigurationNodeIteratorChildren</code>
     * and initializes it.
     *
     * @param parent the parent pointer
     * @param nodeTest the test selecting the sub nodes
     * @param reverse the reverse flag
     * @param startsWith the first element of the iteration
     */
    public ConfigurationNodeIteratorChildren(NodePointer parent,
            NodeTest nodeTest, boolean reverse, NodePointer startsWith)
    {
        super(parent, reverse);
        ConfigurationNode root = (ConfigurationNode) parent.getNode();
        List childNodes = createSubNodeList(root, nodeTest);
        initSubNodeList(childNodes);
        if (startsWith != null)
        {
            setStartOffset(findStartIndex(root,
                    (ConfigurationNode) startsWith.getNode()));
        }
    }

    /**
     * Creates the list with sub nodes. This method gets called during
     * initialization phase. It finds out, based on the given test, which nodes
     * must be iterated over.
     *
     * @param node the current node
     * @param test the test object
     * @return a list with the matching nodes
     */
    protected List createSubNodeList(ConfigurationNode node, NodeTest test)
    {
        List children = node.getChildren();

        if (test == null)
        {
            return children;
        }
        else
        {
            if (test instanceof NodeNameTest)
            {
                NodeNameTest nameTest = (NodeNameTest) test;
                QName name = nameTest.getNodeName();
                if (name.getPrefix() == null)
                {
                    if (nameTest.isWildcard())
                    {
                        return children;
                    }

                    List result = new ArrayList();
                    for (Iterator it = children.iterator(); it.hasNext();)
                    {
                        ConfigurationNode child = (ConfigurationNode) it.next();
                        if (StringUtils.equals(name.getName(), child.getName()))
                        {
                            result.add(child);
                        }
                    }
                    return result;
                }
            }

            else if (test instanceof NodeTypeTest)
            {
                NodeTypeTest typeTest = (NodeTypeTest) test;
                if (typeTest.getNodeType() == Compiler.NODE_TYPE_NODE
                        || typeTest.getNodeType() == Compiler.NODE_TYPE_TEXT)
                {
                    return children;
                }
            }
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * Determines the start position of the iteration. Finds the index of the
     * given start node in the children of the root node.
     *
     * @param node the root node
     * @param startNode the start node
     * @return the start node's index
     */
    protected int findStartIndex(ConfigurationNode node,
            ConfigurationNode startNode)
    {
        for (int index = 0; index < node.getChildrenCount(); index++)
        {
            if (node.getChild(index) == startNode)
            {
                return index;
            }
        }

        return -1;
    }
}
