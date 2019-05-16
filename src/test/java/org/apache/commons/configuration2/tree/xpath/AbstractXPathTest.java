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
import java.util.List;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.After;
import org.junit.Before;

/**
 * A base class for testing classes of the XPath package. This base class
 * creates a hierarchy of nodes in its setUp() method that can be used for test
 * cases.
 *
 */
public abstract class AbstractXPathTest
{
    /** Constant for the name of the counter attribute. */
    protected static final String ATTR_NAME = "counter";

    /** Constant for a name of an attribute of the root node. */
    protected static final String ATTR_ROOT = "rootAttr";

    /** Constant for the name of the first child. */
    protected static final String CHILD_NAME1 = "subNode";

    /** Constant for the name of the second child. */
    protected static final String CHILD_NAME2 = "childNode";

    /** Constant for the number of sub nodes. */
    protected static final int CHILD_COUNT = 5;

    /** Constant for the number of levels in the hierarchy. */
    protected static final int LEVEL_COUNT = 3;

    /** Stores the root node of the hierarchy. */
    protected ImmutableNode root;

    /** The node handler. */
    protected NodeHandler<ImmutableNode> handler;

    @Before
    public void setUp() throws Exception
    {
        root = constructHierarchy(LEVEL_COUNT);
        handler = new InMemoryNodeModel(root).getNodeHandler();
    }

    /**
     * Clears the test environment.
     */
    @After
    public void tearDown() throws Exception
    {
        root = null;
    }

    /**
     * Builds up a hierarchy of nodes. Each node has {@code CHILD_COUNT}
     * child nodes having the names {@code CHILD_NAME1} or
     * {@code CHILD_NAME2}. Their values are named like their parent
     * node with an additional index. Each node has an attribute with a counter
     * value. The root node has a special attribute named {@value #ATTR_ROOT}
     * with the value {@code true}.
     *
     * @param levels the number of levels in the hierarchy
     * @return the root node of the hierarchy
     */
    protected ImmutableNode constructHierarchy(final int levels)
    {
        final ImmutableNode.Builder resultBuilder = new ImmutableNode.Builder();
        createLevel(resultBuilder, null, levels);
        resultBuilder.addAttribute(ATTR_ROOT, String.valueOf(true));
        return resultBuilder.create();
    }

    /**
     * Determines the number of elements contained in the given iterator.
     *
     * @param iterator the iterator
     * @return the number of elements in this iteration
     */
    protected int iteratorSize(final NodeIterator iterator)
    {
        int cnt = 0;
        boolean ok;

        do
        {
            ok = iterator.setPosition(cnt + 1);
            if (ok)
            {
                cnt++;
            }
        } while (ok);

        return cnt;
    }

    /**
     * Returns a list with all node pointers contained in the specified
     * iteration.
     *
     * @param iterator the iterator
     * @return a list with the node pointers obtained from the iterator
     */
    protected List<NodePointer> iterationElements(final NodeIterator iterator)
    {
        final List<NodePointer> result = new ArrayList<>();
        for (int pos = 1; iterator.setPosition(pos); pos++)
        {
            result.add(iterator.getNodePointer());
        }
        return result;
    }

    /**
     * Recursive helper method for creating a level of the node hierarchy.
     *
     * @param parentBuilder the builder for the parent node
     * @param value the value of the parent node
     * @param level the level counter
     */
    private void createLevel(final ImmutableNode.Builder parentBuilder, final String value,
            final int level)
    {
        if (level >= 0)
        {
            final String prefix = (value == null) ? "" : value + ".";
            for (int i = 1; i <= CHILD_COUNT; i++)
            {
                final ImmutableNode.Builder childBuilder =
                        new ImmutableNode.Builder();
                childBuilder.name((i % 2 == 0) ? CHILD_NAME1 : CHILD_NAME2);
                final String currentValue = prefix + i;
                childBuilder.value(currentValue);
                createLevel(childBuilder, currentValue, level - 1);
                childBuilder.addAttribute(ATTR_NAME, String.valueOf(i));
                parentBuilder.addChild(childBuilder.create());
            }
        }
    }
}
