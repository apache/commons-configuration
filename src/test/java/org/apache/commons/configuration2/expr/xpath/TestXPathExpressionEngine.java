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
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

/**
 * Test class for XPathExpressionEngine.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestXPathExpressionEngine extends TestCase
{
    /** Constant for the valid test key. */
    private static final String TEST_KEY = "TESTKEY";

    /** The test root node. */
    private ConfigurationNode root;

    /** The node handler.*/
    private ConfigurationNodeHandler handler;

    /** The expression engine to be tested. */
    private MockJXPathContextExpressionEngine engine;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        root = new DefaultConfigurationNode("testRoot");
        handler = new ConfigurationNodeHandler();
        engine = new MockJXPathContextExpressionEngine();
    }

    /**
     * Tests the query() method with a normal expression.
     */
    public void testQueryExpression()
    {
        NodeList<ConfigurationNode> nodes = engine.query(root, TEST_KEY, handler);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", root, nodes.getNode(0));
        checkSelectCalls(1);
    }

    /**
     * Tests a query that returns attribute nodes.
     */
    public void testQueryAttributes()
    {
        final int attrCount = 5;
        for(int i = 0; i < attrCount; i++)
        {
            root.addAttribute(new DefaultConfigurationNode("attr" + i, "value" + i));
        }
        engine.useMockContext = false;
        NodeList<ConfigurationNode> nodes = engine.query(root, "/@*", handler);
        assertEquals("Wrong number of attribute results", attrCount, nodes.size());
        for(int i = 0; i < attrCount; i++)
        {
            assertTrue("No attribute node", nodes.isAttribute(i));
            assertEquals("Wrong attribute name", "attr" + i, nodes.getName(i, handler));
            assertEquals("Wrong parent node", root, nodes.getAttributeParent(i));
        }
    }

    /**
     * Tests a query that has no results. This should return an empty list.
     */
    public void testQueryWithoutResult()
    {
        NodeList<ConfigurationNode> nodes = engine.query(root, "a non existing key", handler);
        assertTrue("Result list is not empty", nodes.size() == 0);
        checkSelectCalls(1);
    }

    /**
     * Tests a query with an empty key. This should directly return the root
     * node without invoking the JXPathContext.
     */
    public void testQueryWithEmptyKey()
    {
        checkEmptyKey("");
    }

    /**
     * Tests a query with a null key. Same as an empty key.
     */
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
        NodeList<ConfigurationNode> nodes = engine.query(root, key, handler);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", root, nodes.getNode(0));
        checkSelectCalls(0);
    }

    /**
     * Tests if the used JXPathContext is correctly initialized.
     */
    public void testCreateContext()
    {
        JXPathContext ctx = new XPathExpressionEngine().createContext(root,
                TEST_KEY, handler);
        assertNotNull("Context is null", ctx);
        assertTrue("Lenient mode is not set", ctx.isLenient());

        NodePointerFactory[] factories = JXPathContextReferenceImpl
                .getNodePointerFactories();
        boolean found = false;
        for (int i = 0; i < factories.length; i++)
        {
            if (factories[i] instanceof ConfigurationNodePointerFactory)
            {
                found = true;
            }
        }
        assertTrue("No configuration pointer factory found", found);
    }

    /**
     * Tests a normal call of nodeKey().
     */
    public void testNodeKeyNormal()
    {
        assertEquals("Wrong node key", "parent/child", engine.nodeKey(
                new DefaultConfigurationNode("child"), "parent", handler));
    }

    /**
     * Tests a normal call of uniqueNodeKey().
     */
    public void testUniqueNodeKeyNormal()
    {
        ConfigurationNode parent = new DefaultConfigurationNode();
        ConfigurationNode child = new DefaultConfigurationNode("child");
        parent.addChild(child);
        assertEquals("Wrong node key", "parent/child[1]", engine.uniqueNodeKey(
                child, "parent", handler));
    }

    /**
     * Tests querying the key of an attribute node.
     */
    public void testAttributeKey()
    {
        ConfigurationNode parent = new DefaultConfigurationNode("node");
        ConfigurationNode node = new DefaultConfigurationNode("attr");
        parent.addAttribute(node);
        assertEquals("Wrong attribute key", "node/@attr", engine.attributeKey(
                parent, parent.getName(), node.getName(), handler));
    }

    /**
     * Tests nodeKey() for the root node.
     */
    public void testNodeKeyForRootNode()
    {
        assertEquals("Wrong key for root node", "", engine.nodeKey(root, null, handler));
        assertEquals("Null name not detected", "test", engine.nodeKey(
                new DefaultConfigurationNode(), "test", handler));
    }

    /**
     * Tests uniqueNodeKey() for the root node.
     */
    public void testUniqueNodeKeyForRootNode()
    {
        assertEquals("Wrong key for root node", "", engine.uniqueNodeKey(root, null, handler));
        assertEquals("Null name not detected", "test", engine.uniqueNodeKey(
                new DefaultConfigurationNode(), "test", handler));
    }

    /**
     * Tests nodeKey() for direct children of the root node.
     */
    public void testNodeKeyForRootChild()
    {
        ConfigurationNode node = new DefaultConfigurationNode("child");
        assertEquals("Wrong key for root child node", "child", engine.nodeKey(
                node, "", handler));
    }

    /**
     * Tests uniqueNodeKey() for direct children of the root node.
     */
    public void testUniqueNodeKeyForRootChild()
    {
        ConfigurationNode root = new DefaultConfigurationNode();
        ConfigurationNode node = new DefaultConfigurationNode("child");
        root.addChild(node);
        assertEquals("Wrong key for root child node", "child[1]", engine.uniqueNodeKey(
                node, "", handler));
    }

    /**
     * Tests querying the key of an attribute that belongs to the root node when
     * the empty string is passed as parent name.
     */
    public void testAttributeKeyForRootEmpty()
    {
        checkAttributeKeyForRoot("");
    }

    /**
     * Tests querying the key of an attribute that belongs to the root node when
     * null is passed as parent name.
     */
    public void testAttributeKeyForRootNull()
    {
        checkAttributeKeyForRoot(null);
    }

    /**
     * Helper method for checking the keys of attributes that belong to the root
     * node.
     * @param rootName the key of the root node (null or empty)
     */
    private void checkAttributeKeyForRoot(String rootName)
    {
        ConfigurationNode parent = new DefaultConfigurationNode();
        ConfigurationNode attr = new DefaultConfigurationNode("attr");
        parent.addAttribute(attr);
        assertEquals("Wrong key of root attribute", "@attr", engine
                .attributeKey(parent, rootName, attr.getName(), handler));
    }

    /**
     * Tests adding a single child node.
     */
    public void testPrepareAddNode()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, TEST_KEY + "  newNode", handler);
        checkAddPath(data, new String[] { "newNode" }, false);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a new attribute node.
     */
    public void testPrepareAddAttribute()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, TEST_KEY + "\t@newAttr", handler);
        checkAddPath(data, new String[] { "newAttr" }, true);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a complete path.
     */
    public void testPrepareAddPath()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, TEST_KEY
                + " \t a/full/path/node", handler);
        checkAddPath(data, new String[]
        { "a", "full", "path", "node" }, false);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a complete path whose final node is an attribute.
     */
    public void testPrepareAddAttributePath()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, TEST_KEY
                + " a/full/path@attr", handler);
        checkAddPath(data, new String[]
        { "a", "full", "path", "attr" }, true);
        checkSelectCalls(1);
    }

    /**
     * Tests adding a new node to the root.
     */
    public void testPrepareAddRootChild()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, " newNode", handler);
        checkAddPath(data, new String[] { "newNode" }, false);
        checkSelectCalls(0);
    }

    /**
     * Tests adding a new attribute to the root.
     */
    public void testPrepareAddRootAttribute()
    {
        NodeAddData<ConfigurationNode> data = engine.prepareAdd(root, " @attr", handler);
        checkAddPath(data, new String[] { "attr" }, true);
        checkSelectCalls(0);
    }

    /**
     * Tests an add operation with a query that does not return a single node.
     */
    public void testPrepareAddInvalidParent()
    {
        try
        {
            engine.prepareAdd(root, "invalidKey newNode", handler);
            fail("Could add to invalid parent!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an empty path for the new node.
     */
    public void testPrepareAddEmptyPath()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " ", handler);
            fail("Could add empty path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation where the key is null.
     */
    public void testPrepareAddNullKey()
    {
        try
        {
            engine.prepareAdd(root, null, handler);
            fail("Could add null path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation where the key is empty.
     */
    public void testPrepareAddEmptyKey()
    {
        try
        {
            engine.prepareAdd(root, "", handler);
            fail("Could add empty path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an invalid path.
     */
    public void testPrepareAddInvalidPath()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " an/invalid//path", handler);
            fail("Could add invalid path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute in the middle part.
     */
    public void testPrepareAddInvalidAttributePath()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " a/path/with@an/attribute", handler);
            fail("Could add invalid attribute path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute after a slash.
     */
    public void testPrepareAddInvalidAttributePath2()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " a/path/with/@attribute", handler);
            fail("Could add invalid attribute path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an invalid path that starts with a slash.
     */
    public void testPrepareAddInvalidPathWithSlash()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " /a/path/node", handler);
            fail("Could add path starting with a slash!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation with an invalid path that contains multiple
     * attribute components.
     */
    public void testPrepareAddInvalidPathMultipleAttributes()
    {
        try
        {
            engine.prepareAdd(root, TEST_KEY + " an@attribute@path", handler);
            fail("Could add path with multiple attributes!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to add a node to an attribute. This should cause an exception.
     */
    public void testPrepareAddToAttribute()
    {
        ConfigurationNode attr = new DefaultConfigurationNode("attr");
        root.addAttribute(attr);
        engine.useMockContext = false;
        try
        {
            engine.prepareAdd(root, "/@attr newNode", handler);
            fail("Could add a node to an attribute!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Helper method for testing the path nodes in the given add data object.
     *
     * @param data the data object to check
     * @param expected an array with the expected path elements
     * @param attr a flag if the new node is an attribute
     */
    private void checkAddPath(NodeAddData<ConfigurationNode> data, String[] expected, boolean attr)
    {
        assertSame("Wrong parent node", root, data.getParent());
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
    class MockJXPathContext extends JXPathContextReferenceImpl
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
        public List<Object> selectNodes(String xpath)
        {
            selectInvocations++;
            if (TEST_KEY.equals(xpath))
            {
                List<Object> result = new ArrayList<Object>(1);
                result.add(root);
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
     * createContext() to optionally return a mock context object.
     */
    class MockJXPathContextExpressionEngine extends
            XPathExpressionEngine
    {
        /** Stores the context instance. */
        private MockJXPathContext context;

        /** A flag whether a mock context is to be created.*/
        boolean useMockContext = true;

        @Override
        protected <T> JXPathContext createContext(T root, String key, NodeHandler<T> handler)
        {
            if(useMockContext)
            {
                context = new MockJXPathContext(root);
                return context;
            }
            else
            {
                return super.createContext(root, key, handler);
            }
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
