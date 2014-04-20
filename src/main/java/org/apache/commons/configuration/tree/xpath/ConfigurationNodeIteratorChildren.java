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
package org.apache.commons.configuration.tree.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.lang3.StringUtils;

/**
 * A specialized iterator implementation for the child nodes of a configuration
 * node.
 *
 * @since 1.3
 * @version $Id$
 */
class ConfigurationNodeIteratorChildren<T> extends
        ConfigurationNodeIteratorBase<T>
{
    /** The list with the sub nodes to iterate over. */
    private final List<T> subNodes;

    /**
     * Creates a new instance of {@code ConfigurationNodeIteratorChildren} and
     * initializes it.
     *
     * @param parent the parent pointer
     * @param nodeTest the test selecting the sub nodes
     * @param reverse the reverse flag
     * @param startsWith the first element of the iteration
     */
    public ConfigurationNodeIteratorChildren(
            ConfigurationNodePointer<T> parent, NodeTest nodeTest,
            boolean reverse, ConfigurationNodePointer<T> startsWith)
    {
        super(parent, reverse);
        T root = parent.getConfigurationNode();
        subNodes = createSubNodeList(root, nodeTest);

        if (startsWith != null)
        {
            setStartOffset(findStartIndex(subNodes,
                    startsWith.getConfigurationNode()));
        }
        else
        {
            if (reverse)
            {
                setStartOffset(size());
            }
        }
    }

    /**
     * Creates the configuration node pointer for the current position.
     *
     * @param position the current position in the iteration
     * @return the node pointer
     */
    @Override
    protected NodePointer createNodePointer(int position)
    {
        return new ConfigurationNodePointer<T>(getParent(), subNodes
                .get(position), getNodeHandler());
    }

    /**
     * Returns the number of elements in this iteration. This is the number of
     * elements in the children list.
     *
     * @return the number of elements
     */
    @Override
    protected int size()
    {
        return subNodes.size();
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
    private List<T> createSubNodeList(T node, NodeTest test)
    {
        List<T> children = getNodeHandler().getChildren(node);

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

                    List<T> result = new ArrayList<T>();
                    for (T child : children)
                    {
                        if (StringUtils.equals(name.getName(), getNodeHandler()
                                .nodeName(child)))
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

        return Collections.emptyList();
    }

    /**
     * Determines the start position of the iteration. Finds the index of the
     * given start node in the children of the root node.
     *
     * @param children the children of the root node
     * @param startNode the start node
     * @return the start node's index
     */
    private int findStartIndex(List<T> children, T startNode)
    {
        int index = 0;
        for(T child : children)
        {
            if(child == startNode)
            {
                return index;
            }
            index++;
        }

        return -1;
    }
}
