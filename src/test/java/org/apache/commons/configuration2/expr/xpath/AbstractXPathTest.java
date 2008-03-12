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
package org.apache.commons.configuration2.expr.xpath;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

import junit.framework.TestCase;

/**
 * A base class for testing classes of the XPath package. This base class
 * creates a hierarchy of nodes in its setUp() method that can be used for test
 * cases.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public abstract class AbstractXPathTest extends TestCase
{
    /** Constant for the name of the counter attribute. */
    protected static final String ATTR_NAME = "counter";

    /** Constant for the name of the first child. */
    protected static final String CHILD_NAME1 = "subNode";

    /** Constant for the name of the second child. */
    protected static final String CHILD_NAME2 = "childNode";

    /** Constant for the number of sub nodes. */
    protected static final int CHILD_COUNT = 5;

    /** Constant for the number of levels in the hierarchy. */
    protected static final int LEVEL_COUNT = 3;

    /** Stores the root node of the hierarchy. */
    protected ConfigurationNode root;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        root = constructHierarchy(LEVEL_COUNT);
    }

    /**
     * Clears the test environment.
     */
    @Override
    protected void tearDown() throws Exception
    {
        root = null;
    }

    /**
     * Builds up a hierarchy of nodes. Each node has <code>CHILD_COUNT</code>
     * child nodes having the names <code>CHILD_NAME1</code> or
     * <code>CHILD_NAME2</code>. Their values are named like their parent
     * node with an additional index. Each node has an attribute with a counter
     * value.
     *
     * @param levels the number of levels in the hierarchy
     * @return the root node of the hierarchy
     */
    protected ConfigurationNode constructHierarchy(int levels)
    {
        ConfigurationNode result = new DefaultConfigurationNode();
        createLevel(result, levels);
        return result;
    }

    /**
     * Determines the number of elements contained in the given iterator.
     *
     * @param iterator the iterator
     * @return the number of elements in this iteration
     */
    protected int iteratorSize(NodeIterator iterator)
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
    protected List<NodePointer> iterationElements(NodeIterator iterator)
    {
        List<NodePointer> result = new ArrayList<NodePointer>();
        for (int pos = 1; iterator.setPosition(pos); pos++)
        {
            result.add(iterator.getNodePointer());
        }
        return result;
    }

    /**
     * Recursive helper method for creating a level of the node hierarchy.
     *
     * @param parent the parent node
     * @param level the level counter
     */
    private void createLevel(ConfigurationNode parent, int level)
    {
        if (level >= 0)
        {
            String prefix = (parent.getValue() == null) ? "" : parent
                    .getValue()
                    + ".";
            for (int i = 1; i <= CHILD_COUNT; i++)
            {
                ConfigurationNode child = new DefaultConfigurationNode(
                        (i % 2 == 0) ? CHILD_NAME1 : CHILD_NAME2, prefix + i);
                parent.addChild(child);
                child.addAttribute(new DefaultConfigurationNode(ATTR_NAME,
                        String.valueOf(i)));

                createLevel(child, level - 1);
            }
        }
    }
}
