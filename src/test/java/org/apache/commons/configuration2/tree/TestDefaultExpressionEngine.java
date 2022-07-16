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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for DefaultExpressionEngine.
 */
public class TestDefaultExpressionEngine {
    /** Stores the names of the test nodes representing tables. */
    private static final String[] tables = {"users", "documents"};

    /** Stores the types of the test table nodes. */
    private static final String[] tabTypes = {"system", "application"};

    /** Test data fields for the node hierarchy. */
    private static final String[][] fields = {{"uid", "uname", "firstName", "lastName", "email"}, {"docid", "name", "creationDate", "authorID", "version"}};

    /** The root of a hierarchy with test nodes. */
    private static ImmutableNode root;

    /** A node handler for the hierarchy of test nodes. */
    private static NodeHandler<ImmutableNode> handler;

    /**
     * Helper method for creating a field node with its children for the test node hierarchy.
     *
     * @param name the name of the field
     * @return the field node
     */
    private static ImmutableNode createFieldNode(final String name) {
        final ImmutableNode.Builder nodeFieldBuilder = new ImmutableNode.Builder(1);
        nodeFieldBuilder.addChild(createNode("name", name));
        return nodeFieldBuilder.name("field").create();
    }

    /**
     * Convenience method for creating a simple node with a name and a value.
     *
     * @param name the node name
     * @param value the node value
     * @return the node instance
     */
    private static ImmutableNode createNode(final String name, final Object value) {
        return new ImmutableNode.Builder().name(name).value(value).create();
    }

    @BeforeAll
    public static void setUpBeforeClass() {
        root = setUpNodes();
        handler = new InMemoryNodeModel(root).getNodeHandler();
    }

    /**
     * Creates a node hierarchy for testing that consists of tables, their fields, and some additional data:
     *
     * <pre>
     *  tables
     *       table
     *          name
     *          fields
     *              field
     *                  name
     *              field
     *                  name
     * </pre>
     *
     * @return the root of the test node hierarchy
     */
    private static ImmutableNode setUpNodes() {
        final ImmutableNode.Builder nodeTablesBuilder = new ImmutableNode.Builder(tables.length);
        nodeTablesBuilder.name("tables");
        for (int i = 0; i < tables.length; i++) {
            final ImmutableNode.Builder nodeTableBuilder = new ImmutableNode.Builder(2);
            nodeTableBuilder.name("table");
            nodeTableBuilder.addChild(new ImmutableNode.Builder().name("name").value(tables[i]).create());
            nodeTableBuilder.addAttribute("type", tabTypes[i]);

            final ImmutableNode.Builder nodeFieldsBuilder = new ImmutableNode.Builder(fields[i].length);
            for (int j = 0; j < fields[i].length; j++) {
                nodeFieldsBuilder.addChild(createFieldNode(fields[i][j]));
            }
            nodeTableBuilder.addChild(nodeFieldsBuilder.name("fields").create());
            nodeTablesBuilder.addChild(nodeTableBuilder.create());
        }

        final ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        rootBuilder.addChild(nodeTablesBuilder.create());
        final ImmutableNode.Builder nodeConnBuilder = new ImmutableNode.Builder();
        nodeConnBuilder.name("connection.settings");
        nodeConnBuilder.addChild(createNode("usr.name", "scott"));
        nodeConnBuilder.addChild(createNode("usr.pwd", "tiger"));
        rootBuilder.addAttribute("test", "true");
        rootBuilder.addChild(nodeConnBuilder.create());

        return rootBuilder.create();
    }

    /** The object to be tested. */
    private DefaultExpressionEngine engine;

    /**
     * Helper method for checking whether an attribute key is correctly evaluated.
     *
     * @param key the attribute key
     * @param attr the attribute name
     * @param expValue the expected attribute value
     */
    private void checkAttributeValue(final String key, final String attr, final Object expValue) {
        final List<QueryResult<ImmutableNode>> results = checkKey(key, attr, 1);
        final QueryResult<ImmutableNode> result = results.get(0);
        assertTrue(result.isAttributeResult());
        assertEquals(expValue, result.getAttributeValue(handler), "Wrong attribute value for key " + key);
    }

    /**
     * Helper method for checking the evaluation of a key. Queries the expression engine and tests if the expected results
     * are returned.
     *
     * @param key the key
     * @param name the name of the nodes to be returned
     * @param count the number of expected result nodes
     * @return the list with the results of the query
     */
    private List<QueryResult<ImmutableNode>> checkKey(final String key, final String name, final int count) {
        final List<QueryResult<ImmutableNode>> nodes = query(key, count);
        for (final QueryResult<ImmutableNode> result : nodes) {
            if (result.isAttributeResult()) {
                assertEquals(name, result.getAttributeName(), "Wrong attribute name for key " + key);
            } else {
                assertEquals(name, result.getNode().getNodeName(), "Wrong result node for key " + key);
            }
        }
        return nodes;
    }

    /**
     * Helper method for checking the value of a node specified by the given key. This method evaluates the key and checks
     * whether the resulting node has the expected value.
     *
     * @param key the key
     * @param name the expected name of the result node
     * @param value the expected value of the result node
     */
    private void checkKeyValue(final String key, final String name, final String value) {
        final List<QueryResult<ImmutableNode>> results = checkKey(key, name, 1);
        final QueryResult<ImmutableNode> result = results.get(0);
        assertFalse(result.isAttributeResult());
        assertEquals(value, result.getNode().getValue(), "Wrong value for key " + key);
    }

    /**
     * Helper method for checking the path of an add operation.
     *
     * @param data the add data object
     * @param expected the expected path nodes
     */
    private void checkNodePath(final NodeAddData<ImmutableNode> data, final String... expected) {
        assertEquals(Arrays.asList(expected), data.getPathNodes());
    }

    /**
     * Helper method for testing a query for the root node.
     *
     * @param key the key to be used
     */
    private void checkQueryRootNode(final String key) {
        final List<QueryResult<ImmutableNode>> results = checkKey(key, null, 1);
        final QueryResult<ImmutableNode> result = results.get(0);
        assertFalse(result.isAttributeResult());
        assertSame(root, result.getNode());
    }

    /**
     * Helper method for fetching a specific node by its key.
     *
     * @param key the key
     * @return the node with this key
     */
    private ImmutableNode fetchNode(final String key) {
        final QueryResult<ImmutableNode> result = query(key, 1).get(0);
        assertFalse(result.isAttributeResult());
        return result.getNode();
    }

    /**
     * Helper method for querying the test engine for a specific key.
     *
     * @param key the key
     * @param expCount the expected number of result nodes
     * @return the collection of retrieved nodes
     */
    private List<QueryResult<ImmutableNode>> query(final String key, final int expCount) {
        final List<QueryResult<ImmutableNode>> nodes = engine.query(root, key, handler);
        assertEquals(expCount, nodes.size());
        return nodes;
    }

    @BeforeEach
    public void setUp() throws Exception {
        engine = DefaultExpressionEngine.INSTANCE;
    }

    /**
     * Configures the test expression engine to use a special matcher. This matcher ignores underscore characters in node
     * names.
     */
    private void setUpAlternativeMatcher() {
        final NodeMatcher<String> matcher = new NodeMatcher<String>() {
            @Override
            public <T> boolean matches(final T node, final NodeHandler<T> handler, final String criterion) {
                return handler.nodeName(node).equals(StringUtils.remove(criterion, '_'));
            }
        };
        engine = new DefaultExpressionEngine(engine.getSymbols(), matcher);
    }

    /**
     * Configures the expression engine to use a different syntax.
     */
    private void setUpAlternativeSyntax() {
        final DefaultExpressionEngineSymbols symbols = new DefaultExpressionEngineSymbols.Builder().setAttributeEnd(null).setAttributeStart("@")
            .setPropertyDelimiter("/").setEscapedDelimiter(null).setIndexStart("[").setIndexEnd("]").create();
        engine = new DefaultExpressionEngine(symbols);
    }

    /**
     * Tests obtaining keys for attribute nodes.
     */
    @Test
    public void testAttributeKey() {
        assertEquals("tables.table[@type]", engine.attributeKey("tables.table", "type"));
    }

    /**
     * Tests that a null parent key is ignored when constructing an attribute key.
     */
    @Test
    public void testAttributeKeyNoParent() {
        assertEquals("[@test]", engine.attributeKey(null, "test"));
    }

    /**
     * Tests whether an attribute key can be queried if the root node is involved.
     */
    @Test
    public void testAttributeKeyRoot() {
        assertEquals("[@test]", engine.attributeKey("", "test"));
    }

    /**
     * Tests whether a correct attribute key with alternative syntax is generated.
     */
    @Test
    public void testAttributeKeyWithAlternativeSyntax() {
        setUpAlternativeSyntax();
        assertEquals("@test", engine.attributeKey("", "test"));
    }

    /**
     * Tests whether a canonical key can be queried if all child nodes have different names.
     */
    @Test
    public void testCanonicalKeyNoDuplicates() {
        final ImmutableNode node = fetchNode("tables.table(0).name");
        assertEquals("table.name(0)", engine.canonicalKey(node, "table", handler));
    }

    /**
     * Tests whether the parent key can be undefined when querying a canonical key.
     */
    @Test
    public void testCanonicalKeyNoParentKey() {
        final ImmutableNode node = fetchNode("tables.table(0).fields.field(1).name");
        assertEquals("name(0)", engine.canonicalKey(node, null, handler));
    }

    /**
     * Tests whether a canonical key for the parent node can be queried if no parent key was passed in.
     */
    @Test
    public void testCanonicalKeyRootNoParentKey() {
        assertEquals("", engine.canonicalKey(root, null, handler));
    }

    /**
     * Tests whether a parent key is evaluated when determining the canonical key of the root node.
     */
    @Test
    public void testCanonicalKeyRootWithParentKey() {
        assertEquals("parent", engine.canonicalKey(root, "parent", handler));
    }

    /**
     * Tests whether duplicates are correctly resolved when querying for canonical keys.
     */
    @Test
    public void testCanonicalKeyWithDuplicates() {
        final ImmutableNode tab1 = fetchNode("tables.table(0)");
        final ImmutableNode tab2 = fetchNode("tables.table(1)");
        assertEquals("tables.table(0)", engine.canonicalKey(tab1, "tables", handler));
        assertEquals("tables.table(1)", engine.canonicalKey(tab2, "tables", handler));
    }

    /**
     * Tests whether the default instance is initialized with default symbols.
     */
    @Test
    public void testDefaultSymbols() {
        assertSame(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS, engine.getSymbols());
    }

    /**
     * Tries to create an instance without symbols.
     */
    @Test
    public void testInitNoSymbols() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultExpressionEngine(null));
    }

    /**
     * Tests obtaining keys for nodes.
     */
    @Test
    public void testNodeKey() {
        final ImmutableNode node = root.getChildren().get(0);
        assertEquals("tables", engine.nodeKey(node, "", handler));
        assertEquals("test.tables", engine.nodeKey(node, "test", handler));
        assertEquals("a.full.parent.key.tables", engine.nodeKey(node, "a.full.parent.key", handler));
    }

    /**
     * Tests obtaining node keys if a different syntax is set.
     */
    @Test
    public void testNodeKeyWithAlternativeSyntax() {
        setUpAlternativeSyntax();
        assertEquals("tables/table", engine.nodeKey(root.getChildren().get(0).getChildren().get(0), "tables", handler));
    }

    /**
     * Tests obtaining node keys if a different syntax is set and the same string is used as property delimiter and
     * attribute start marker.
     */
    @Test
    public void testNodeKeyWithAlternativeSyntaxAttributePropertyDelimiter() {
        setUpAlternativeSyntax();
        final DefaultExpressionEngineSymbols symbols = new DefaultExpressionEngineSymbols.Builder(engine.getSymbols())
            .setAttributeStart(engine.getSymbols().getPropertyDelimiter()).create();
        engine = new DefaultExpressionEngine(symbols);
        assertEquals("/test", engine.attributeKey("", "test"));
    }

    /**
     * Tests obtaining keys for nodes that contain the delimiter character.
     */
    @Test
    public void testNodeKeyWithEscapedDelimiters() {
        final ImmutableNode node = root.getChildren().get(1);
        assertEquals("connection..settings", engine.nodeKey(node, "", handler));
        assertEquals("connection..settings.usr..name", engine.nodeKey(node.getChildren().get(0), engine.nodeKey(node, "", handler), handler));
    }

    /**
     * Tests obtaining keys if the root node is involved.
     */
    @Test
    public void testNodeKeyWithRoot() {
        assertEquals("", engine.nodeKey(root, null, handler));
        assertEquals("test", engine.nodeKey(root, "test", handler));
    }

    /**
     * Tests adding new attributes.
     */
    @Test
    public void testPrepareAddAttribute() {
        final NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables.table(0)[@tableSpace]", handler);
        assertEquals(tables[0], data.getParent().getChildren().get(0).getValue());
        assertEquals("tableSpace", data.getNewNodeName());
        assertTrue(data.isAttribute());
        assertTrue(data.getPathNodes().isEmpty());
    }

    /**
     * Tests whether an attribute to the root node can be added.
     */
    @Test
    public void testPrepareAddAttributeRoot() {
        final NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "[@newAttr]", handler);
        assertSame(root, data.getParent());
        assertEquals("newAttr", data.getNewNodeName());
        assertTrue(data.isAttribute());
    }

    /**
     * Tests adding direct child nodes to the existing hierarchy.
     */
    @Test
    public void testPrepareAddDirectly() {
        NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "newNode", handler);
        assertSame(root, data.getParent());
        assertTrue(data.getPathNodes().isEmpty());
        assertEquals("newNode", data.getNewNodeName());
        assertFalse(data.isAttribute());

        data = engine.prepareAdd(root, "tables.table.fields.field.name", handler);
        assertEquals("name", data.getNewNodeName());
        assertTrue(data.getPathNodes().isEmpty());
        assertEquals("field", data.getParent().getNodeName());
        final ImmutableNode nd = data.getParent().getChildren().get(0);
        assertEquals("name", nd.getNodeName());
        assertEquals("version", nd.getValue());
    }

    @Test
    public void testPrepareAddEmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> engine.prepareAdd(root, "", handler));
    }

    /**
     * Tests using invalid keys, e.g. if something should be added to attributes.
     */
    @Test
    public void testPrepareAddInvalidKey() {
        assertThrows(IllegalArgumentException.class, () -> engine.prepareAdd(root, "tables.table(0)[@type].new", handler));
    }

    @Test
    public void testPrepareAddInvalidKeyAttribute() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.prepareAdd(root, "a.complete.new.path.with.an[@attribute].at.a.non.allowed[@position]", handler));
    }

    @Test
    public void testPrepareAddNullKey() {
        assertThrows(IllegalArgumentException.class, () -> engine.prepareAdd(root, null, handler));
    }

    /**
     * Tests whether the node matcher is used when adding keys.
     */
    @Test
    public void testPrepareAddWithAlternativeMatcher() {
        setUpAlternativeMatcher();
        final NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables_.table._fields__._field.name", handler);
        assertEquals("name", data.getNewNodeName());
        assertTrue(data.getPathNodes().isEmpty());
    }

    /**
     * Tests add operations when an alternative syntax is set.
     */
    @Test
    public void testPrepareAddWithAlternativeSyntax() {
        setUpAlternativeSyntax();
        NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables/table[0]/test", handler);
        assertEquals("test", data.getNewNodeName());
        assertFalse(data.isAttribute());
        assertEquals(tables[0], data.getParent().getChildren().get(0).getValue());

        data = engine.prepareAdd(root, "a/complete/new/path@attr", handler);
        assertEquals("attr", data.getNewNodeName());
        checkNodePath(data, "a", "complete", "new", "path");
        assertSame(root, data.getParent());
    }

    /**
     * Tests adding if indices are involved.
     */
    @Test
    public void testPrepareAddWithIndex() {
        NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables.table(0).tableSpace", handler);
        assertEquals("tableSpace", data.getNewNodeName());
        assertTrue(data.getPathNodes().isEmpty());
        assertEquals("table", data.getParent().getNodeName());
        final ImmutableNode node = data.getParent().getChildren().get(0);
        assertEquals(tables[0], node.getValue());

        data = engine.prepareAdd(root, "tables.table(1).fields.field(2).alias", handler);
        assertEquals("alias", data.getNewNodeName());
        assertEquals("field", data.getParent().getNodeName());
        assertEquals("creationDate", data.getParent().getChildren().get(0).getValue());
    }

    /**
     * Tests add operations where complete paths are added.
     */
    @Test
    public void testPrepareAddWithPath() {
        NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables.table(1).fields.field(-1).name", handler);
        assertEquals("name", data.getNewNodeName());
        checkNodePath(data, "field");
        assertEquals("fields", data.getParent().getNodeName());

        data = engine.prepareAdd(root, "tables.table(-1).name", handler);
        assertEquals("name", data.getNewNodeName());
        checkNodePath(data, "table");
        assertEquals("tables", data.getParent().getNodeName());

        data = engine.prepareAdd(root, "a.complete.new.path", handler);
        assertEquals("path", data.getNewNodeName());
        checkNodePath(data, "a", "complete", "new");
        assertSame(root, data.getParent());
    }

    /**
     * Tests add operations if property and attribute delimiters are equal. Then it is not possible to add new attribute
     * nodes.
     */
    @Test
    public void testPrepareAddWithSameAttributeDelimiter() {
        final DefaultExpressionEngineSymbols symbols = new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
            .setAttributeEnd(null).setAttributeStart(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS.getPropertyDelimiter()).create();
        engine = new DefaultExpressionEngine(symbols);

        NodeAddData<ImmutableNode> data = engine.prepareAdd(root, "tables.table(0).test", handler);
        assertEquals("test", data.getNewNodeName());
        assertFalse(data.isAttribute());
        assertEquals("table", data.getParent().getNodeName());

        data = engine.prepareAdd(root, "a.complete.new.path", handler);
        assertFalse(data.isAttribute());
        checkNodePath(data, "a", "complete", "new");
    }

    /**
     * Tests a different query syntax. Sets other strings for the typical tokens used by the expression engine.
     */
    @Test
    public void testQueryAlternativeSyntax() {
        setUpAlternativeSyntax();
        checkKeyValue("tables/table[1]/name", "name", tables[1]);
        checkAttributeValue("tables/table[0]@type", "type", tabTypes[0]);
        checkAttributeValue("@test", "test", "true");
        checkKeyValue("connection.settings/usr.name", "usr.name", "scott");
    }

    /**
     * Tests some queries when the same delimiter is used for properties and attributes.
     */
    @Test
    public void testQueryAttributeEmulation() {
        final DefaultExpressionEngineSymbols symbols = new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
            .setAttributeEnd(null).setAttributeStart(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER).create();
        engine = new DefaultExpressionEngine(symbols);
        checkKeyValue("tables.table(0).name", "name", tables[0]);
        checkAttributeValue("tables.table(0).type", "type", tabTypes[0]);
        checkKey("tables.table.type", "type", 2);
    }

    /**
     * Tests querying nodes whose names contain a delimiter.
     */
    @Test
    public void testQueryEscapedKeys() {
        checkKeyValue("connection..settings.usr..name", "usr.name", "scott");
        checkKeyValue("connection..settings.usr..pwd", "usr.pwd", "tiger");
    }

    /**
     * Tests some simple queries.
     */
    @Test
    public void testQueryKeys() {
        checkKey("tables.table.name", "name", 2);
        checkKey("tables.table.fields.field.name", "name", 10);
        checkKey("tables.table[@type]", "type", 2);
        checkKey("tables.table(0).fields.field.name", "name", 5);
        checkKey("tables.table(1).fields.field.name", "name", 5);
        checkKey("tables.table.fields.field(1).name", "name", 2);
    }

    /**
     * Tests whether the node matcher is used when querying keys.
     */
    @Test
    public void testQueryKeyWithAlternativeMatcher() {
        setUpAlternativeMatcher();
        checkKey("tables_._table_.name_", "name", 2);
    }

    /**
     * Performs some queries and evaluates the values of the result nodes.
     */
    @Test
    public void testQueryNodes() {
        for (int i = 0; i < tables.length; i++) {
            checkKeyValue("tables.table(" + i + ").name", "name", tables[i]);
            checkAttributeValue("tables.table(" + i + ")[@type]", "type", tabTypes[i]);

            for (int j = 0; j < fields[i].length; j++) {
                checkKeyValue("tables.table(" + i + ").fields.field(" + j + ").name", "name", fields[i][j]);
            }
        }
    }

    /**
     * Tests querying keys that do not exist.
     */
    @Test
    public void testQueryNonExistingKeys() {
        checkKey("tables.tablespace.name", null, 0);
        checkKey("tables.table(2).name", null, 0);
        checkKey("a complete unknown key", null, 0);
        checkKey("tables.table(0).fields.field(-1).name", null, 0);
        checkKey("tables.table(0).fields.field(28).name", null, 0);
        checkKey("tables.table(0).fields.field().name", null, 0);
        checkKey("connection.settings.usr.name", null, 0);
        checkKey("tables.table(0)[@type].additional", null, 0);
    }

    /**
     * Tests whether an attribute of the root node can be queried.
     */
    @Test
    public void testQueryRootAttribute() {
        checkAttributeValue("[@test]", "test", "true");
    }

    /**
     * Tests whether the root node can be retrieved using the empty key.
     */
    @Test
    public void testQueryRootNodeEmptyKey() {
        checkQueryRootNode("");
    }

    /**
     * Tests whether the root node can be retrieved using the null key.
     */
    @Test
    public void testQueryRootNodeNullKey() {
        checkQueryRootNode(null);
    }
}
