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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.NodeAddData;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathContextFactoryConfigurationError;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

import junit.framework.TestCase;

/**
 * Test class for XPathExpressionEngine.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestXPathExpressionEngine extends TestCase
{
    /** Constant for the test root node. */
    static final ConfigurationNode ROOT = new DefaultConfigurationNode(
            "testRoot");

    /** Constant for the valid test key. */
    static final String TEST_KEY = "TESTKEY";

    /** The expression engine to be tested. */
    XPathExpressionEngine engine;

    protected void setUp() throws Exception
    {
        super.setUp();
        initMockContextFactory();
        engine = new XPathExpressionEngine();
    }

    protected void tearDown() throws Exception
    {
        MockJXPathContextFactory.context = null; // reset context
        super.tearDown();
    }

    /**
     * Tests the query() method with a normal expression.
     */
    public void testQueryExpression()
    {
        List nodes = engine.query(ROOT, TEST_KEY);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", ROOT, nodes.get(0));
        checkSelectCalls(1);
    }

    /**
     * Tests a query that has no results. This should return an empty list.
     */
    public void testQueryWithoutResult()
    {
        List nodes = engine.query(ROOT, "a non existing key");
        assertTrue("Result list is not empty", nodes.isEmpty());
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
        List nodes = engine.query(ROOT, key);
        assertEquals("Incorrect number of results", 1, nodes.size());
        assertSame("Wrong result node", ROOT, nodes.get(0));
        checkSelectCalls(0);
    }

    /**
     * Tests if the used JXPathContext is correctly initialized.
     */
    public void testCreateContext()
    {
        JXPathContext ctx = engine.createContext(ROOT, TEST_KEY);
        assertNotNull("Context is null", ctx);
        assertTrue("Lenient mode is not set", ctx.isLenient());
        assertSame("Incorrect context bean set", ROOT, ctx.getContextBean());

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
                new DefaultConfigurationNode("child"), "parent"));
    }

    /**
     * Tests nodeKey() for an attribute node.
     */
    public void testNodeKeyAttribute()
    {
        ConfigurationNode node = new DefaultConfigurationNode("attr");
        node.setAttribute(true);
        assertEquals("Wrong attribute key", "node@attr", engine.nodeKey(node,
                "node"));
    }

    /**
     * Tests nodeKey() for the root node.
     */
    public void testNodeKeyForRootNode()
    {
        assertEquals("Wrong key for root node", "", engine.nodeKey(ROOT, null));
        assertEquals("Null name not detected", "test", engine.nodeKey(
                new DefaultConfigurationNode(), "test"));
    }

    /**
     * Tests node key() for direct children of the root node.
     */
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
    public void testPrepareAddInvalidParent()
    {
        try
        {
            engine.prepareAdd(ROOT, "invalidKey newNode");
            fail("Could add to invalid parent!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation where the passed in key has an invalid format: it
     * does not contain a whitspace. This will cause an error.
     */
    public void testPrepareAddInvalidFormat()
    {
        try
        {
            engine.prepareAdd(ROOT, "anInvalidKey");
            fail("Could add an invalid key!");
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
            engine.prepareAdd(ROOT, TEST_KEY + " ");
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
            engine.prepareAdd(ROOT, null);
            fail("Could add null path!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests an add operation where the key is null.
     */
    public void testPrepareAddEmptyKey()
    {
        try
        {
            engine.prepareAdd(ROOT, "");
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
            engine.prepareAdd(ROOT, TEST_KEY + " an/invalid//path");
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
            engine.prepareAdd(ROOT, TEST_KEY + " a/path/with@an/attribute");
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
            engine.prepareAdd(ROOT, TEST_KEY + " a/path/with/@attribute");
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
            engine.prepareAdd(ROOT, TEST_KEY + " /a/path/node");
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
            engine.prepareAdd(ROOT, TEST_KEY + " an@attribute@path");
            fail("Could add path with multiple attributes!");
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
    private void checkAddPath(NodeAddData data, String[] expected, boolean attr)
    {
        assertSame("Wrong parent node", ROOT, data.getParent());
        List path = data.getPathNodes();
        assertEquals("Incorrect number of path nodes", expected.length - 1,
                path.size());
        Iterator it = path.iterator();
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
     * Initializes the mock JXPath context factory. Sets a system property, so
     * that this implementation will be used.
     */
    protected void initMockContextFactory()
    {
        System.setProperty(JXPathContextFactory.FACTORY_NAME_PROPERTY,
                MockJXPathContextFactory.class.getName());
    }

    /**
     * Checks if the JXPath context's selectNodes() method was called as often
     * as expected.
     *
     * @param expected the number of expected calls
     */
    protected void checkSelectCalls(int expected)
    {
        MockJXPathContext ctx = MockJXPathContextFactory.getContext();
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
        public List selectNodes(String xpath)
        {
            selectInvocations++;
            if (TEST_KEY.equals(xpath))
            {
                List result = new ArrayList(1);
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
     * A mock implementation of the JXPathContextFactory class. This class is
     * used to inject the mock context, so that we can trace the invocations of
     * selectNodes().
     */
    public static class MockJXPathContextFactory extends JXPathContextFactory
    {
        /** Stores the context instance. */
        static MockJXPathContext context;

        public JXPathContext newContext(JXPathContext parentContext,
                Object contextBean)
                throws JXPathContextFactoryConfigurationError
        {
            context = new MockJXPathContext(contextBean);
            return context;
        }

        /**
         * Returns the context created by the last newContext() call.
         *
         * @return the current context
         */
        public static MockJXPathContext getContext()
        {
            return context;
        }
    }
}
