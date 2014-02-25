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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.NodeAddData;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for XPathExpressionEngine.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestXPathExpressionEngine
{
    /** Constant for the test root node. */
    static final ConfigurationNode ROOT = new DefaultConfigurationNode(
            "testRoot");

    /** Constant for the valid test key. */
    static final String TEST_KEY = "TESTKEY";

    /** The expression engine to be tested. */
    XPathExpressionEngine engine;

    @Before
    public void setUp() throws Exception
    {
        engine = new MockJXPathContextExpressionEngine();
    }

    /**
     * Tests the query() method with a normal expression.
     */
    @Test
    public void testQueryExpression()
    {
        List<ConfigurationNode> nodes = engine.query(ROOT, TEST_KEY);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", ROOT, nodes.get(0));
        checkSelectCalls(1);
    }

    /**
     * Tests a query that has no results. This should return an empty list.
     */
    @Test
    public void testQueryWithoutResult()
    {
        List<ConfigurationNode> nodes = engine.query(ROOT, "a non existing key");
        assertTrue("Result list is not empty", nodes.isEmpty());
        checkSelectCalls(1);
    }

    /**
     * Tests a query with an empty key. This should directly return the root
     * node without invoking the JXPathContext.
     */
    @Test
    public void testQueryWithEmptyKey()
    {
        checkEmptyKey("");
    }

    /**
     * Tests a query with a null key. Same as an empty key.
     */
    @Test
    public void testQueryWithNullKey()
    {
        checkEmptyKey(null);
    }

    /**
     * Helper method for testing undefined keys.
     *
     * @param key the key
     */
    private void checkEmptyKey(String key)
    {
        List<ConfigurationNode> nodes = engine.query(ROOT, key);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", ROOT, nodes.get(0));
        checkSelectCalls(0);
    }

    /**
     * Tests if the used JXPathContext is correctly initialized.
     */
    @Test
    public void testCreateContext()
    {
        JXPathContext ctx = new XPathExpressionEngine().createContext(ROOT,
                TEST_KEY);
        assertNotNull("Context is null", ctx);
        assertTrue("Lenient mode is not set", ctx.isLenient());
        assertSame("Incorrect context bean set", ROOT, ctx.getContextBean());

        NodePointerFactory[] factories = JXPathContextReferenceImpl
                .getNodePointerFactories();
        boolean found = false;
        for (NodePointerFactory factorie : factories) {
            if (factorie instanceof ConfigurationNodePointerFactory)
            {
                found = true;
            }
        }
        assertTrue("No configuration pointer factory found", found);
    }

    /**
     * Tests a normal call of nodeKey().
     */
    @Test
    public void testNodeKeyNormal()
    {
        assertEquals("Wrong node key", "parent/child", engine.nodeKey(
                new DefaultConfigurationNode("child"), "parent"));
    }

    /**
     * Tests nodeKey() for an attribute node.
     */
    @Test
    public void testNodeKeyAttribute()
    {
        ConfigurationNode node = new DefaultConfigurationNode("attr");
        node.setAttribute(true);
        assertEquals("Wrong attribute key", "node/@attr", engine.nodeKey(node,
                "node"));
    }

    /**
     * Tests nodeKey() for the root node.
     */
    @Test
    public void testNodeKeyForRootNode()
    {
        assertEquals("Wrong key for root node", "", engine.nodeKey(ROOT, null));
        assertEquals("Null name not detected", "test", engine.nodeKey(
                new DefaultConfigurationNode(), "test"));
    }

    /**
     * Tests node key() for direct children of the root node.
     */
    @Test
    public void testNodeKeyForRootChild()
    {
        ConfigurationNode node = new DefaultConfigurationNode("child");
        assertEquals("Wrong key for root child node", "child", engine.nodeKey(
                node, ""));
        node.setAttribute(true);
        assertEquals("Wrong key for root attribute", "@child", engine.nodeKey(
                node, ""));
    }

    /**
     * Tests adding a single child node.
     */
    @Test
    public void testPrepareAddNode()
    {
        NodeAddData data = engine.prepareAdd(ROOT, TEST_KEY + "  newNode");
        checkAddPath(data, new String[]
        { "newNode" }, false);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a new attribute node.
     */
    @Test
    public void testPrepareAddAttribute()
    {
        NodeAddData data = engine.prepareAdd(ROOT, TEST_KEY + "\t@newAttr");
        checkAddPath(data, new String[]
        { "newAttr" }, true);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a complete path.
     */
    @Test
    public void testPrepareAddPath()
    {
        NodeAddData data = engine.prepareAdd(ROOT, TEST_KEY
                + " \t a/full/path/node");
        checkAddPath(data, new String[]
        { "a", "full", "path", "node" }, false);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a complete path whose final node is an attribute.
     */
    @Test
    public void testPrepareAddAttributePath()
    {
        NodeAddData data = engine.prepareAdd(ROOT, TEST_KEY
                + " a/full/path@attr");
        checkAddPath(data, new String[]
        { "a", "full", "path", "attr" }, true);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a new node to the root.
     */
    @Test
    public void testPrepareAddRootChild()
    {
        NodeAddData data = engine.prepareAdd(ROOT, " newNode");
        checkAddPath(data, new String[]
        { "newNode" }, false);
        checkSelectCalls(0);
    }

    /**
     * Tests adding a new attribute to the root.
     */
    @Test
    public void testPrepareAddRootAttribute()
    {
        NodeAddData data = engine.prepareAdd(ROOT, " @attr");
        checkAddPath(data, new String[]
        { "attr" }, true);
        checkSelectCalls(0);
    }

    /**
     * Tests an add operation with a query that does not return a single node.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidParent()
    {
        engine.prepareAdd(ROOT, "invalidKey newNode");
    }

    /**
     * Tests an add operation with an empty path for the new node.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddEmptyPath()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " ");
    }

    /**
     * Tests an add operation where the key is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddNullKey()
    {
        engine.prepareAdd(ROOT, null);
    }

    /**
     * Tests an add operation where the key is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddEmptyKey()
    {
        engine.prepareAdd(ROOT, "");
    }

    /**
     * Tests an add operation with an invalid path.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPath()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " an/invalid//path");
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute in the middle part.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidAttributePath()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " a/path/with@an/attribute");
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute after a slash.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidAttributePath2()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " a/path/with/@attribute");
    }

    /**
     * Tests an add operation with an invalid path that starts with a slash.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPathWithSlash()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " /a/path/node");
    }

    /**
     * Tests an add operation with an invalid path that contains multiple
     * attribute components.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPathMultipleAttributes()
    {
        engine.prepareAdd(ROOT, TEST_KEY + " an@attribute@path");
    }

    /**
     * Helper method for testing the path nodes in the given add data object.
     *
     * @param data the data object to check
     * @param expected an array with the expected path elements
     * @param attr a flag if the new node is an attribute
     */
    private void checkAddPath(NodeAddData data, String[] expected, boolean attr)
    {
        assertSame("Wrong parent node", ROOT, data.getParent());
        List<String> path = data.getPathNodes();
        assertEquals("Incorrect number of path nodes", expected.length - 1,
                path.size());
        Iterator<String> it = path.iterator();
        for (int idx = 0; idx < expected.length - 1; idx++)
        {
            assertEquals("Wrong node at position " + idx, expected[idx], it
                    .next());
        }
        assertEquals("Wrong name of new node", expected[expected.length - 1],
                data.getNewNodeName());
        assertEquals("Incorrect attribute flag", attr, data.isAttribute());
    }

    /**
     * Checks if the JXPath context's selectNodes() method was called as often
     * as expected.
     *
     * @param expected the number of expected calls
     */
    protected void checkSelectCalls(int expected)
    {
        MockJXPathContext ctx = ((MockJXPathContextExpressionEngine) engine).getContext();
        int calls = (ctx == null) ? 0 : ctx.selectInvocations;
        assertEquals("Incorrect number of select calls", expected, calls);
    }

    /**
     * A mock implementation of the JXPathContext class. This implementation
     * will overwrite the <code>selectNodes()</code> method that is used by
     * <code>XPathExpressionEngine</code> to count the invocations of this
     * method.
     */
    static class MockJXPathContext extends JXPathContextReferenceImpl
    {
        int selectInvocations;

        public MockJXPathContext(Object bean)
        {
            super(null, bean);
        }

        /**
         * Dummy implementation of this method. If the passed in string is the
         * test key, the root node will be returned in the list. Otherwise the
         * return value is <b>null</b>.
         */
        @Override
        public List<?> selectNodes(String xpath)
        {
            selectInvocations++;
            if (TEST_KEY.equals(xpath))
            {
                List<ConfigurationNode> result = new ArrayList<ConfigurationNode>(1);
                result.add(ROOT);
                return result;
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * A special implementation of XPathExpressionEngine that overrides
     * createContext() to return a mock context object.
     */
    static class MockJXPathContextExpressionEngine extends
            XPathExpressionEngine
    {
        /** Stores the context instance. */
        private MockJXPathContext context;

        @Override
        protected JXPathContext createContext(ConfigurationNode root, String key)
        {
            context = new MockJXPathContext(root);
            return context;
        }

        /**
         * Returns the context created by the last newContext() call.
         *
         * @return the current context
         */
        public MockJXPathContext getContext()
        {
            return context;
        }
    }
}
