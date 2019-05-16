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
 * @param <T> the type of the nodes this iterator deals with
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
            final ConfigurationNodePointer<T> parent, final NodeTest nodeTest,
            final boolean reverse, final ConfigurationNodePointer<T> startsWith)
    {
        super(parent, reverse);
        final T root = parent.getConfigurationNode();
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
    protected NodePointer createNodePointer(final int position)
    {
        return new ConfigurationNodePointer<>(getParent(), subNodes
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
    private List<T> createSubNodeList(final T node, final NodeTest test)
    {
        if (test == null)
        {
            return getNodeHandler().getChildren(node);
        }
        if (test instanceof NodeNameTest)
        {
            final NodeNameTest nameTest = (NodeNameTest) test;
            final QName name = nameTest.getNodeName();
            return nameTest.isWildcard() ? createSubNodeListForWildcardName(
                    node, name) : createSubNodeListForName(node, name);
        }

        else if (test instanceof NodeTypeTest)
        {
            final NodeTypeTest typeTest = (NodeTypeTest) test;
            if (typeTest.getNodeType() == Compiler.NODE_TYPE_NODE
                    || typeTest.getNodeType() == Compiler.NODE_TYPE_TEXT)
            {
                return getNodeHandler().getChildren(node);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Obtains the list of selected nodes for a {@code NodeNameTest} with either
     * a simple or a qualified name.
     *
     * @param node the current node
     * @param name the name to be selected
     * @return the list with selected sub nodes
     */
    private List<T> createSubNodeListForName(final T node, final QName name)
    {
        final String compareName = qualifiedName(name);
        final List<T> result = new ArrayList<>();
        for (final T child : getNodeHandler().getChildren(node))
        {
            if (StringUtils.equals(compareName, getNodeHandler()
                    .nodeName(child)))
            {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Obtains the list of selected sub nodes for a {@code NodeNameTest} with a
     * wildcard name.
     *
     * @param node the current node
     * @param name the name to be selected
     * @return the list with selected sub nodes
     */
    private List<T> createSubNodeListForWildcardName(final T node, final QName name)
    {
        final List<T> children = getNodeHandler().getChildren(node);
        if (name.getPrefix() == null)
        {
            return children;
        }
        final List<T> prefixChildren = new ArrayList<>(children.size());
        final String prefix = prefixName(name.getPrefix(), null);
        for (final T child : children)
        {
            if (StringUtils.startsWith(getNodeHandler().nodeName(child),
                    prefix))
            {
                prefixChildren.add(child);
            }
        }
        return prefixChildren;
    }

    /**
     * Determines the start position of the iteration. Finds the index of the
     * given start node in the children of the root node.
     *
     * @param children the children of the root node
     * @param startNode the start node
     * @return the start node's index
     */
    private int findStartIndex(final List<T> children, final T startNode)
    {
        int index = 0;
        for (final T child : children)
        {
            if (child == startNode)
            {
                return index;
            }
            index++;
        }

        return -1;
    }

}
