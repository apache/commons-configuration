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

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for ConfigurationNodePointerFactory. This class does not directly
 * call the factory's methods, but rather checks if it can be installed in a
 * {@code JXPathContext} and if XPath expressions can be evaluated.
 *
 */
public class TestConfigurationNodePointerFactory extends AbstractXPathTest
{
    /** Stores the JXPathContext used for testing. */
    private JXPathContext context;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        JXPathContextReferenceImpl
                .addNodePointerFactory(new ConfigurationNodePointerFactory());
    }

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        context = new XPathContextFactory().createContext(root, handler);
    }

    /**
     * Tests simple XPath expressions.
     */
    @Test
    public void testSimpleXPath()
    {
        List<?> results = context.selectNodes(CHILD_NAME1);
        assertEquals("Incorrect number of results", 2, results.size());
        for (final Object result : results) {
            final ImmutableNode node = (ImmutableNode) result;
            assertEquals("Incorrect node name", CHILD_NAME1, node.getNodeName());
        }

        results = context.selectNodes("/" + CHILD_NAME1);
        assertEquals("Incorrect number of results", 2, results.size());

        results = context.selectNodes(CHILD_NAME2 + "/" + CHILD_NAME1 + "/"
                + CHILD_NAME2);
        assertEquals("Incorrect number of results", 18, results.size());
    }

    /**
     * Tests using indices to specify elements.
     */
    @Test
    public void testIndices()
    {
        assertEquals("Incorrect value", "1.2.3", context.getValue("/"
                + CHILD_NAME2 + "[1]/" + CHILD_NAME1 + "[1]/" + CHILD_NAME2
                + "[2]"));
        assertEquals("Incorrect value of last node", String
                .valueOf(CHILD_COUNT), context.getValue(CHILD_NAME2
                + "[last()]"));

        final List<?> nodes = context.selectNodes("/" + CHILD_NAME1 + "[1]/*");
        assertEquals("Wrong number of children", CHILD_COUNT, nodes.size());
        int index = 1;
        for (final Iterator<?> it = nodes.iterator(); it.hasNext(); index++)
        {
            final ImmutableNode node = (ImmutableNode) it.next();
            assertEquals("Wrong node value for child " + index, "2." + index,
                    node.getValue());
        }
    }

    /**
     * Tests whether the attribute of a node can be queried.
     */
    @Test
    public void testQueryAttribute()
    {
        assertEquals("Incorrect attribute value", "1", context.getValue("/"
                + CHILD_NAME2 + "[1]/@" + ATTR_NAME));
    }

    /**
     * Tests whether an attribute of the root node can be queried.
     */
    @Test
    public void testQueryRootAttribute()
    {
        assertEquals("Did not find attribute of root node", "true", context
                .getValue("@" + ATTR_ROOT));
    }

    /**
     * Tests accessing a node's text.
     */
    @Test
    public void testText()
    {
        final List<?> nodes = context.selectNodes("//" + CHILD_NAME2
                + "[text()='1.1.1']");
        assertEquals("Incorrect number of result nodes", 1, nodes.size());
    }

    /**
     * Tests accessing the parent axis.
     */
    @Test
    public void testParentAxis()
    {
        final List<?> nodes = context.selectNodes("/" + CHILD_NAME2 + "/parent::*");
        assertEquals("Wrong number of parent nodes", 1, nodes.size());
    }

    /**
     * Tests accessing the following sibling axis.
     */
    @Test
    public void testFollowingSiblingAxis()
    {
        final List<?> nodes = context.selectNodes("/" + CHILD_NAME1
                + "[2]/following-sibling::*");
        assertEquals("Wrong number of following siblings", 1, nodes.size());
        final ImmutableNode node = (ImmutableNode) nodes.get(0);
        assertEquals("Wrong node type", CHILD_NAME2, node.getNodeName());
        assertEquals("Wrong index", String.valueOf(CHILD_COUNT), node
                .getValue());
    }

    /**
     * Tests accessing the preceding sibling axis.
     */
    @Test
    public void testPrecedingSiblingAxis()
    {
        final List<?> nodes = context.selectNodes("/" + CHILD_NAME1
                + "[2]/preceding-sibling::*");
        assertEquals("Wrong number of preceding siblings", 3, nodes.size());
        for (int index = 0, value = 3; index < nodes.size(); index++, value--)
        {
            assertEquals("Wrong node index", String.valueOf(value),
                    ((ImmutableNode) nodes.get(index)).getValue());
        }
    }
}
