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
package org.apache.commons.configuration2.tree;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code NodeSelector}.
 *
 */
public class TestNodeSelector
{
    /** Constant for a test key. */
    private static final String KEY = "tables.testKey";

    /** The root node for query operations. */
    private static ImmutableNode root;

    /** A NodeKeyResolver implementation. */
    private static NodeKeyResolver<ImmutableNode> resolver;

    /** The node handler object. */
    private static NodeHandler<ImmutableNode> handler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        resolver = createResolver();
        handler = new InMemoryNodeModel().getNodeHandler();
        root =
                new ImmutableNode.Builder(1).addChild(
                        NodeStructureHelper.ROOT_TABLES_TREE).create();
    }

    /**
     * Creates a {@code NodeKeyResolver} object to be used in the tests. This
     * resolver object uses a {@code DefaultExpressionEngine} instance for
     * resolving keys. Methods for obtaining add or update data objects are not
     * implemented.
     *
     * @return the {@code NodeKeyResolver}
     */
    private static NodeKeyResolver<ImmutableNode> createResolver()
    {
        final NodeKeyResolver<ImmutableNode> resolver =
                NodeStructureHelper.createResolverMock();
        NodeStructureHelper.expectResolveKeyForQueries(resolver);
        EasyMock.replay(resolver);
        return resolver;
    }

    /**
     * Tests a successful select operation for a single key.
     */
    @Test
    public void testSelectSingleKeySuccess()
    {
        final NodeSelector selector = new NodeSelector("tables.table(0).name");
        final ImmutableNode target = selector.select(root, resolver, handler);
        assertEquals("Wrong name", "name", target.getNodeName());
        assertEquals("Wrong value", NodeStructureHelper.table(0),
                target.getValue());
    }

    /**
     * Tests a select operation if the key selects an attribute node.
     */
    @Test
    public void testSelectSingleAttributeKey()
    {
        final NodeKeyResolver<ImmutableNode> resolverMock =
                NodeStructureHelper.createResolverMock();
        EasyMock.expect(resolverMock.resolveKey(root, KEY, handler)).andReturn(
                Collections.singletonList(QueryResult.createAttributeResult(
                        root, KEY)));
        EasyMock.replay(resolverMock);

        final NodeSelector selector = new NodeSelector(KEY);
        assertNull("Got a result", selector.select(root, resolverMock, handler));
    }

    /**
     * Tests whether attribute results are ignored when evaluating the key.
     */
    @Test
    public void testSelectIgnoreAttributeResults()
    {
        final NodeKeyResolver<ImmutableNode> resolverMock =
                NodeStructureHelper.createResolverMock();
        final List<QueryResult<ImmutableNode>> results =
                new LinkedList<>();
        results.add(QueryResult.createAttributeResult(
                NodeStructureHelper.nodeForKey(root, "tables/table(0)"), "type"));
        final ImmutableNode target =
                NodeStructureHelper.nodeForKey(root, "tables/table(1)");
        results.add(QueryResult.createNodeResult(target));
        results.add(QueryResult.createAttributeResult(NodeStructureHelper
                .nodeForKey(root, "tables/table(0)/fields/field(1)"), "type"));
        EasyMock.expect(resolverMock.resolveKey(root, KEY, handler)).andReturn(
                results);
        EasyMock.replay(resolverMock);

        final NodeSelector selector = new NodeSelector(KEY);
        assertSame("Wrong target", target,
                selector.select(root, resolverMock, handler));
    }

    /**
     * Tests a select operation with a key yielding multiple target nodes.
     */
    @Test
    public void testSelectMultipleTargets()
    {
        final NodeSelector selector = new NodeSelector("tables.table.name");
        assertNull("Got a result", selector.select(root, resolver, handler));
    }

    /**
     * Tests a select operation with a sub key.
     */
    @Test
    public void testSelectSubKey()
    {
        final NodeSelector selectorParent = new NodeSelector("tables.table(0)");
        final NodeSelector selector =
                selectorParent.subSelector("fields.field(1).name");
        final ImmutableNode target = selector.select(root, resolver, handler);
        assertEquals("Wrong node selected", NodeStructureHelper.field(0, 1),
                target.getValue());
    }

    /**
     * Tests select() if a key is used which does not yield any results.
     */
    @Test
    public void testSelectSubKeyUnknown()
    {
        final NodeSelector selectorParent = new NodeSelector("tables.unknown");
        final NodeSelector selector =
                selectorParent.subSelector("fields.field(1).name");
        assertNull("Got a result", selector.select(root, resolver, handler));
    }

    /**
     * Tests a select operation with a sub key which produces multiple results.
     */
    @Test
    public void testSelectSubKeyMultipleResults()
    {
        final NodeSelector selectorParent = new NodeSelector("tables.table");
        final NodeSelector selector =
                selectorParent.subSelector("fields.field(1).name");
        assertNull("Got a result", selector.select(root, resolver, handler));
    }

    /**
     * Tests a select operation with a sub key which requires complex
     * processing: The first kes produce multiple results; the final key reduces
     * the result set to a single node.
     */
    @Test
    public void testSelectSubKeyComplexEvaluation()
    {
        final NodeSelector first = new NodeSelector("tables.table");
        final NodeSelector second = first.subSelector("fields");
        final int fldIdx = NodeStructureHelper.fieldsLength(1) - 1;
        final NodeSelector selector =
                second.subSelector("field(" + fldIdx + ").name");
        final ImmutableNode target = selector.select(root, resolver, handler);
        assertEquals("Wrong target node", NodeStructureHelper.field(1, fldIdx),
                target.getValue());
    }

    /**
     * Tests equals() if the expected result is true.
     */
    @Test
    public void testEqualsTrue()
    {
        final NodeSelector selector = new NodeSelector(KEY);
        ConfigurationAssert.checkEquals(selector, selector, true);
        final NodeSelector sel2 = new NodeSelector(KEY);
        ConfigurationAssert.checkEquals(selector, sel2, true);
        final NodeSelector sub1 = selector.subSelector("k2");
        final NodeSelector sub2 = sel2.subSelector("k2");
        ConfigurationAssert.checkEquals(sub1, sub2, true);
    }

    /**
     * Tests equals() if the expected result is false.
     */
    @Test
    public void testEqualsFalse()
    {
        final NodeSelector selector = new NodeSelector(KEY);
        NodeSelector sel2 = new NodeSelector("other" + KEY);
        ConfigurationAssert.checkEquals(selector, sel2, false);
        sel2 = new NodeSelector(KEY).subSelector(KEY);
        ConfigurationAssert.checkEquals(selector, sel2, false);
    }

    /**
     * Tests equals() with other objects.
     */
    @Test
    public void testEqualsOtherObjects()
    {
        final NodeSelector selector = new NodeSelector(KEY);
        ConfigurationAssert.checkEquals(selector, null, false);
        ConfigurationAssert.checkEquals(selector, this, false);
    }

    /**
     * Tests the string representation.
     */
    @Test
    public void testToString()
    {
        final String key2 = "anotherSelectionKey";
        final NodeSelector selector = new NodeSelector(KEY).subSelector(key2);
        final String s = selector.toString();
        assertThat(s, containsString(KEY));
        assertThat(s, containsString(key2));
    }
}
