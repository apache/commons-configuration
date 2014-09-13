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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeAddData;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for XPathExpressionEngine.
 *
 * @version $Id$
 */
public class TestXPathExpressionEngine
{
    /** Constant for the valid test key. */
    private static final String TEST_KEY = "TESTKEY";

    /** Constant for the name of the root node. */
    private static final String ROOT_NAME = "testRoot";

    /** The test root node. */
    private static ImmutableNode root;

    /** A test node handler. */
    private static NodeHandler<ImmutableNode> handler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        root = new ImmutableNode.Builder().name(ROOT_NAME).create();
        handler = new InMemoryNodeModel(root).getNodeHandler();
    }

    /**
     * Creates a mock for a context and prepares it to expect a select
     * invocation yielding the provided results.
     *
     * @param results the results
     * @return the mock context
     */
    private JXPathContext expectSelect(Object... results)
    {
        JXPathContext ctx = EasyMock.createMock(JXPathContext.class);
        EasyMock.expect(ctx.selectNodes(TEST_KEY)).andReturn(
                Arrays.asList(results));
        EasyMock.replay(ctx);
        return ctx;
    }

    /**
     * Creates a test engine instance configured with a context factory which
     * returns the given test context.
     *
     * @param ctx the context mock
     * @return the test engine instance
     */
    private XPathExpressionEngine setUpEngine(JXPathContext ctx)
    {
        XPathContextFactory factory =
                EasyMock.createMock(XPathContextFactory.class);
        EasyMock.expect(factory.createContext(root, handler)).andReturn(ctx);
        EasyMock.replay(factory);
        return new XPathExpressionEngine(factory);
    }

    /**
     * Tests whether a correct default context factory is created.
     */
    @Test
    public void testDefaultContextFactory()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertNotNull("No context factory", engine.getContextFactory());
    }

    /**
     * Tests the query() method with an expression yielding a node.
     */
    @Test
    public void testQueryNodeExpression()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        List<QueryResult<ImmutableNode>> result =
                engine.query(root, TEST_KEY, handler);
        assertEquals("Incorrect number of results", 1, result.size());
        assertSame("Wrong result node", root, result.get(0).getNode());
        assertFalse("No node result", result.get(0).isAttributeResult());
    }

    /**
     * Tests a query which yields an attribute result.
     */
    @Test
    public void testQueryAttributeExpression()
    {
        QueryResult<ImmutableNode> attrResult =
                QueryResult.createAttributeResult(root, "attr");
        JXPathContext ctx = expectSelect(attrResult);
        XPathExpressionEngine engine = setUpEngine(ctx);
        List<QueryResult<ImmutableNode>> result =
                engine.query(root, TEST_KEY, handler);
        assertEquals("Incorrect number of results", 1, result.size());
        assertSame("Wrong result", attrResult, result.get(0));
    }

    /**
     * Tests a query that has no results. This should return an empty list.
     */
    @Test
    public void testQueryWithoutResult()
    {
        JXPathContext ctx = expectSelect();
        XPathExpressionEngine engine = setUpEngine(ctx);
        assertTrue("Got results", engine.query(root, TEST_KEY, handler)
                .isEmpty());
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
     * Helper method for testing queries with undefined keys.
     *
     * @param key the key
     */
    private void checkEmptyKey(String key)
    {
        XPathContextFactory factory =
                EasyMock.createMock(XPathContextFactory.class);
        EasyMock.replay(factory);
        XPathExpressionEngine engine = new XPathExpressionEngine(factory);
        List<QueryResult<ImmutableNode>> results =
                engine.query(root, key, handler);
        assertEquals("Incorrect number of results", 1, results.size());
        assertSame("Wrong result node", root, results.get(0).getNode());
    }

    /**
     * Tests if the JXPathContext is correctly initialized with the node pointer
     * factory.
     */
    @Test
    public void testNodePointerFactory()
    {
        JXPathContext.newContext(this);
        NodePointerFactory[] factories =
                JXPathContextReferenceImpl.getNodePointerFactories();
        boolean found = false;
        for (NodePointerFactory factory : factories)
        {
            if (factory instanceof ConfigurationNodePointerFactory)
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
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong node key", "parent/" + ROOT_NAME,
                engine.nodeKey(root, "parent", handler));
    }

    /**
     * Tests nodeKey() for the root node.
     */
    @Test
    public void testNodeKeyForRootNode()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key for root node", "",
                engine.nodeKey(root, null, handler));
    }

    /**
     * Tests a node key if the node does not have a name.
     */
    @Test
    public void testNodeKeyNoNodeName()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Null name not detected", "test", engine.nodeKey(
                new ImmutableNode.Builder().create(), "test", handler));
    }

    /**
     * Tests node key() for direct children of the root node.
     */
    @Test
    public void testNodeKeyForRootChild()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key for root child node", ROOT_NAME,
                engine.nodeKey(root, "", handler));
    }

    /**
     * Tests whether the key of an attribute can be generated..
     */
    @Test
    public void testNodeKeyAttribute()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong attribute key", "node/@attr",
                engine.attributeKey("node", "attr"));
    }

    /**
     * Tests the key of an attribute which belongs to the root node.
     */
    @Test
    public void testAttributeKeyOfRootNode()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key for root attribute", "@child",
                engine.attributeKey(null, "child"));
    }

    /**
     * Tests adding a single child node.
     */
    @Test
    public void testPrepareAddNode()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, TEST_KEY + "  newNode", handler);
        checkAddPath(data, false, "newNode");
    }

    /**
     * Tests adding a new attribute node.
     */
    @Test
    public void testPrepareAddAttribute()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, TEST_KEY + "\t@newAttr", handler);
        checkAddPath(data, true, "newAttr");
    }

    /**
     * Tests adding a complete path.
     */
    @Test
    public void testPrepareAddPath()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, TEST_KEY + " \t a/full/path/node",
                        handler);
        checkAddPath(data, false, "a", "full", "path", "node");
    }

    /**
     * Tests adding a complete path whose final node is an attribute.
     */
    @Test
    public void testPrepareAddAttributePath()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, TEST_KEY + " a/full/path@attr", handler);
        checkAddPath(data, true, "a", "full", "path", "attr");
    }

    /**
     * Tests adding a new node to the root.
     */
    @Test
    public void testPrepareAddRootChild()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, " newNode", handler);
        checkAddPath(data, false, "newNode");
    }

    /**
     * Tests adding a new attribute to the root.
     */
    @Test
    public void testPrepareAddRootAttribute()
    {
        JXPathContext ctx = expectSelect(root);
        XPathExpressionEngine engine = setUpEngine(ctx);
        NodeAddData<ImmutableNode> data =
                engine.prepareAdd(root, " @attr", handler);
        checkAddPath(data, true, "attr");
    }

    /**
     * Tests an add operation with a query that does not return a single node.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidParent()
    {
        JXPathContext ctx = expectSelect();
        XPathExpressionEngine engine = setUpEngine(ctx);
        engine.prepareAdd(root, TEST_KEY + " test", handler);
    }

    /**
     * Tests an add operation with an empty path for the new node.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddEmptyPath()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        engine.prepareAdd(root, TEST_KEY + " ", handler);
    }

    /**
     * Tests an add operation where the key is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddNullKey()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        engine.prepareAdd(root, null, handler);
    }

    /**
     * Tests an add operation where the key is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddEmptyKey()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        engine.prepareAdd(root, "", handler);
    }

    /**
     * Helper method for checking whether an exception is thrown for an invalid
     * path passed to prepareAdd().
     *
     * @param path the path to be tested
     * @throws IllegalArgumentException if the test is successful
     */
    private void checkInvalidAddPath(String path)
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        QueryResult<ImmutableNode> res = QueryResult.createNodeResult(root);
        engine.createNodeAddData(path, res);
    }

    /**
     * Tests an add operation with an invalid path.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPath()
    {
        checkInvalidAddPath("an/invalid//path");
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute in the middle part.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidAttributePath()
    {
        checkInvalidAddPath("a/path/with@an/attribute");
    }

    /**
     * Tests an add operation with an invalid path: the path contains an
     * attribute after a slash.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidAttributePath2()
    {
        checkInvalidAddPath("a/path/with/@attribute");
    }

    /**
     * Tests an add operation with an invalid path that starts with a slash.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPathWithSlash()
    {
        checkInvalidAddPath("/a/path/node");
    }

    /**
     * Tests an add operation with an invalid path that contains multiple
     * attribute components.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddInvalidPathMultipleAttributes()
    {
        checkInvalidAddPath("an@attribute@path");
    }

    /**
     * Tests that it is not possible to add nodes to an attribute.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPrepareAddToAttributeResult()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        QueryResult<ImmutableNode> result =
                QueryResult.createAttributeResult(root, TEST_KEY);
        engine.createNodeAddData("path", result);
    }

    /**
     * Helper method for testing the path nodes in the given add data object.
     *
     * @param data the data object to check
     * @param attr a flag if the new node is an attribute
     * @param expected an array with the expected path elements
     */
    private static void checkAddPath(NodeAddData<ImmutableNode> data,
            boolean attr, String... expected)
    {
        assertSame("Wrong parent node", root, data.getParent());
        List<String> path = data.getPathNodes();
        assertEquals("Incorrect number of path nodes", expected.length - 1,
                path.size());
        Iterator<String> it = path.iterator();
        for (int idx = 0; idx < expected.length - 1; idx++)
        {
            assertEquals("Wrong node at position " + idx, expected[idx],
                    it.next());
        }
        assertEquals("Wrong name of new node", expected[expected.length - 1],
                data.getNewNodeName());
        assertEquals("Incorrect attribute flag", attr, data.isAttribute());
    }

    /**
     * Tests whether a canonical key can be queried if all child nodes have
     * different names.
     */
    @Test
    public void testCanonicalKeyNoDuplicates()
    {
        ImmutableNode.Builder parentBuilder = new ImmutableNode.Builder(2);
        ImmutableNode c1 = new ImmutableNode.Builder().name("child").create();
        ImmutableNode c2 =
                new ImmutableNode.Builder().name("child_other").create();
        parentBuilder.addChildren(Arrays.asList(c2, c1));
        ImmutableNode parent = parentBuilder.create();
        NodeHandler<ImmutableNode> testHandler =
                new InMemoryNodeModel(parent).getNodeHandler();
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong canonical key", "parent/child[1]",
                engine.canonicalKey(c1, "parent", testHandler));
    }

    /**
     * Tests whether duplicates are correctly resolved when querying for
     * canonical keys.
     */
    @Test
    public void testCanonicalKeyWithDuplicates()
    {
        ImmutableNode.Builder parentBuilder = new ImmutableNode.Builder(3);
        ImmutableNode c1 = new ImmutableNode.Builder().name("child").create();
        ImmutableNode c2 = new ImmutableNode.Builder().name("child").create();
        ImmutableNode c3 =
                new ImmutableNode.Builder().name("child_other").create();
        parentBuilder.addChildren(Arrays.asList(c1, c2, c3));
        ImmutableNode parent = parentBuilder.create();
        NodeHandler<ImmutableNode> testHandler =
                new InMemoryNodeModel(parent).getNodeHandler();
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key 1", "parent/child[1]",
                engine.canonicalKey(c1, "parent", testHandler));
        assertEquals("Wrong key 2", "parent/child[2]",
                engine.canonicalKey(c2, "parent", testHandler));
    }

    /**
     * Tests whether the parent key can be undefined when querying a canonical
     * key.
     */
    @Test
    public void testCanonicalKeyNoParentKey()
    {
        ImmutableNode.Builder parentBuilder = new ImmutableNode.Builder(1);
        ImmutableNode c1 = new ImmutableNode.Builder().name("child").create();
        ImmutableNode parent = parentBuilder.addChild(c1).create();
        NodeHandler<ImmutableNode> testHandler =
                new InMemoryNodeModel(parent).getNodeHandler();
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key", "child[1]",
                engine.canonicalKey(c1, null, testHandler));
    }

    /**
     * Tests whether a canonical key for the parent node can be queried if no
     * parent key was passed in.
     */
    @Test
    public void testCanonicalKeyRootNoParentKey()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key", "", engine.canonicalKey(root, null, handler));
    }

    /**
     * Tests whether a parent key is evaluated when determining the canonical
     * key of the root node.
     */
    @Test
    public void testCanonicalKeyRootWithParentKey()
    {
        XPathExpressionEngine engine = new XPathExpressionEngine();
        assertEquals("Wrong key", "parent",
                engine.canonicalKey(root, "parent", handler));
    }
}
