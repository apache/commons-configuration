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

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.tree.DefaultConfigurationKey;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeModel;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code AbstractHierarchicalConfiguration}.
 */
public class TestAbstractHierarchicalConfiguration {
    /**
     * A concrete test implementation of {@code AbstractHierarchicalConfiguration}.
     */
    private static final class AbstractHierarchicalConfigurationTestImpl extends AbstractHierarchicalConfiguration<ImmutableNode> {
        public AbstractHierarchicalConfigurationTestImpl(final InMemoryNodeModel model) {
            super(model);
        }

        @Override
        public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(final String key, final boolean supportUpdates) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        protected NodeModel<ImmutableNode> cloneNodeModel() {
            return new InMemoryNodeModel(getModel().getNodeHandler().getRootNode());
        }

        @Override
        public SubnodeConfiguration configurationAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public SubnodeConfiguration configurationAt(final String key, final boolean supportUpdates) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(final String key, final boolean supportUpdates) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public List<ImmutableHierarchicalConfiguration> immutableChildConfigurationsAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public ImmutableHierarchicalConfiguration immutableConfigurationAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public ImmutableHierarchicalConfiguration immutableConfigurationAt(final String key, final boolean supportUpdates) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }

        @Override
        public List<ImmutableHierarchicalConfiguration> immutableConfigurationsAt(final String key) {
            throw new UnsupportedOperationException("Unexpected method call!");
        }
    }

    /**
     * Checks the content of the passed in configuration object. Used by some tests that copy a configuration.
     *
     * @param c the configuration to check
     */
    private static void checkContent(final Configuration c) {
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            assertEquals(NodeStructureHelper.table(i), c.getString("tables.table(" + i + ").name"));
            for (int j = 0; j < NodeStructureHelper.fieldsLength(i); j++) {
                assertEquals(NodeStructureHelper.field(i, j), c.getString("tables.table(" + i + ").fields.field(" + j + ").name"));
            }
        }
    }

    private static void checkGetProperty(final AbstractHierarchicalConfiguration<?> testConfig) {
        assertNull(testConfig.getProperty("tables.table.resultset"));
        assertNull(testConfig.getProperty("tables.table.fields.field"));

        Object prop = testConfig.getProperty("tables.table(0).fields.field.name");
        Collection<?> collection = assertInstanceOf(Collection.class, prop);
        assertEquals(NodeStructureHelper.fieldsLength(0), collection.size());

        prop = testConfig.getProperty("tables.table.fields.field.name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(totalFieldCount(), collection.size());

        prop = testConfig.getProperty("tables.table.fields.field(3).name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(2, collection.size());

        prop = testConfig.getProperty("tables.table(1).fields.field(2).name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());
    }

    /**
     * Creates a {@code DefaultConfigurationKey} object.
     *
     * @return the new key object
     */
    private static DefaultConfigurationKey createConfigurationKey() {
        return new DefaultConfigurationKey(DefaultExpressionEngine.INSTANCE);
    }

    /**
     * Returns the total number of fields in the test data structure.
     *
     * @return the total number of fields
     */
    private static int totalFieldCount() {
        int fieldCount = 0;
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            fieldCount += NodeStructureHelper.fieldsLength(i);
        }
        return fieldCount;
    }

    /** The test configuration. */
    private AbstractHierarchicalConfiguration<ImmutableNode> config;

    /**
     * Helper method for checking keys using an alternative syntax.
     */
    private void checkAlternativeSyntax() {
        assertNull(config.getProperty("tables/table/resultset"));
        assertNull(config.getProperty("tables/table/fields/field"));

        Object prop = config.getProperty("tables/table[0]/fields/field/name");
        Collection<?> collection = assertInstanceOf(Collection.class, prop);
        assertEquals(NodeStructureHelper.fieldsLength(0), collection.size());

        prop = config.getProperty("tables/table/fields/field/name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(totalFieldCount(), collection.size());

        prop = config.getProperty("tables/table/fields/field[3]/name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(2, collection.size());

        prop = config.getProperty("tables/table[1]/fields/field[2]/name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());

        final Set<String> keys = ConfigurationAssert.keysToSet(config);
        assertEquals(new HashSet<>(Arrays.asList("tables/table/name", "tables/table/fields/field/name")), keys);
    }

    /**
     * Helper method for testing the getKeys(String) method.
     *
     * @param prefix the key to pass into getKeys()
     * @param expected the expected result
     */
    private void checkKeys(final String prefix, final String[] expected) {
        final Set<String> expectedKeys = new HashSet<>();
        for (final String anExpected : expected) {
            expectedKeys.add(anExpected.startsWith(prefix) ? anExpected : prefix + "." + anExpected);
        }

        final Set<String> keys = new HashSet<>();
        final Iterator<String> itKeys = config.getKeys(prefix);
        while (itKeys.hasNext()) {
            final String key = itKeys.next();
            keys.add(key);
        }

        assertEquals(expectedKeys, keys);
    }

    private ExpressionEngine createAlternativeExpressionEngine() {
        return new DefaultExpressionEngine(new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS).setPropertyDelimiter("/")
            .setIndexStart("[").setIndexEnd("]").create());
    }

    /**
     * Convenience method for obtaining the root node of the test configuration.
     *
     * @return the root node of the test configuration
     */
    private ImmutableNode getRootNode() {
        return config.getModel().getNodeHandler().getRootNode();
    }

    @BeforeEach
    public void setUp() throws Exception {
        final ImmutableNode root = new ImmutableNode.Builder(1).addChild(NodeStructureHelper.ROOT_TABLES_TREE).create();
        config = new AbstractHierarchicalConfigurationTestImpl(new InMemoryNodeModel(root));
    }

    @Test
    public void testAddNodes() {
        final Collection<ImmutableNode> nodes = new ArrayList<>();
        nodes.add(NodeStructureHelper.createFieldNode("birthDate"));
        nodes.add(NodeStructureHelper.createFieldNode("lastLogin"));
        nodes.add(NodeStructureHelper.createFieldNode("language"));
        config.addNodes("tables.table(0).fields", nodes);
        assertEquals(7, config.getMaxIndex("tables.table(0).fields.field"));
        assertEquals("birthDate", config.getString("tables.table(0).fields.field(5).name"));
        assertEquals("lastLogin", config.getString("tables.table(0).fields.field(6).name"));
        assertEquals("language", config.getString("tables.table(0).fields.field(7).name"));
    }

    /**
     * Tests copying nodes from one configuration to another one.
     */
    @Test
    public void testAddNodesCopy() {
        final AbstractHierarchicalConfigurationTestImpl configDest = new AbstractHierarchicalConfigurationTestImpl(new InMemoryNodeModel());
        configDest.addProperty("test", "TEST");
        final Collection<ImmutableNode> nodes = getRootNode().getChildren();
        assertEquals(1, nodes.size());
        configDest.addNodes("newNodes", nodes);
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            final String keyTab = "newNodes.tables.table(" + i + ").";
            assertEquals(NodeStructureHelper.table(i), configDest.getString(keyTab + "name"), "Table " + i + " not found");
            for (int j = 0; j < NodeStructureHelper.fieldsLength(i); j++) {
                assertEquals(NodeStructureHelper.field(i, j), configDest.getString(keyTab + "fields.field(" + j + ").name"),
                        "Invalid field " + j + " in table " + i);
            }
        }
    }

    /**
     * Tests the addNodes() method if the provided key does not exist. In this case, a new node (or even a completely new
     * branch) is created.
     */
    @Test
    public void testAddNodesForNonExistingKey() {
        final Collection<ImmutableNode> nodes = new ArrayList<>();
        final ImmutableNode newNode = new ImmutableNode.Builder().name("usr").value("scott").addAttribute("pwd", "tiger").create();
        nodes.add(newNode);
        config.addNodes("database.connection.settings", nodes);

        assertEquals("scott", config.getString("database.connection.settings.usr"));
        assertEquals("tiger", config.getString("database.connection.settings.usr[@pwd]"));
    }

    /**
     * Tests the addNodes() method when the new nodes should be added to an attribute node. This is not allowed.
     */
    @Test
    public void testAddNodesWithAttributeKey() {
        final Collection<ImmutableNode> nodes = new ArrayList<>();
        nodes.add(NodeStructureHelper.createNode("testNode", "yes"));
        assertThrows(IllegalArgumentException.class, () -> config.addNodes("database.connection[@settings]", nodes));
    }

    @Test
    public void testAddProperty() {
        config.addProperty("tables.table(0).fields.field(-1).name", "phone");
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        Collection<?> collection = assertInstanceOf(Collection.class, prop);
        assertEquals(6, collection.size());

        config.addProperty("tables.table(0).fields.field.name", "fax");
        prop = config.getProperty("tables.table.fields.field(5).name");
        final List<?> list = assertInstanceOf(List.class, prop);
        assertEquals("phone", list.get(0));
        assertEquals("fax", list.get(1));

        config.addProperty("tables.table(-1).name", "config");
        prop = config.getProperty("tables.table.name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(3, collection.size());
        config.addProperty("tables.table(2).fields.field(0).name", "cid");
        config.addProperty("tables.table(2).fields.field(-1).name", "confName");
        prop = config.getProperty("tables.table(2).fields.field.name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(2, collection.size());
        assertEquals("confName", config.getProperty("tables.table(2).fields.field(1).name"));

        config.addProperty("connection.user", "scott");
        config.addProperty("connection.passwd", "tiger");
        assertEquals("tiger", config.getProperty("connection.passwd"));

        final DefaultConfigurationKey key = createConfigurationKey();
        key.append("tables").append("table").appendIndex(0);
        key.appendAttribute("tableType");
        config.addProperty(key.toString(), "system");
        assertEquals("system", config.getProperty(key.toString()));
    }

    @Test
    public void testAddPropertyInvalidKey() {
        assertThrows(IllegalArgumentException.class, () -> config.addProperty(".", "InvalidKey"));
    }

    /**
     * Tests whether list handling works correctly when adding properties.
     */
    @Test
    public void testAddPropertyWithListHandling() {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        final String key = "list.delimiter.value";
        config.addProperty(key + ".escaped", "3\\,1415");
        config.addProperty(key + ".elements", "3,1415");
        assertEquals("3,1415", config.getString(key + ".escaped"));
        assertEquals("3", config.getString(key + ".elements"));
    }

    @Test
    public void testClear() {
        config.setProperty(null, "value");
        config.addProperty("[@attr]", "defined");
        config.clear();
        assertTrue(config.isEmpty());
    }

    @Test
    public void testClearProperty() {
        config.clearProperty("tables.table(0).fields.field(0).name");
        assertEquals("uname", config.getProperty("tables.table(0).fields.field(0).name"));
        config.clearProperty("tables.table(0).name");
        assertFalse(config.containsKey("tables.table(0).name"));
        assertEquals("firstName", config.getProperty("tables.table(0).fields.field(1).name"));
        assertEquals("documents", config.getProperty("tables.table.name"));
        config.clearProperty("tables.table");
        assertEquals("documents", config.getProperty("tables.table.name"));

        config.addProperty("test", "first");
        config.addProperty("test.level", "second");
        config.clearProperty("test");
        assertEquals("second", config.getString("test.level"));
        assertFalse(config.containsKey("test"));
    }

    @Test
    public void testClearTree() {
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        config.clearTree("tables.table(0).fields.field(3)");
        prop = config.getProperty("tables.table(0).fields.field.name");
        Collection<?> collection = assertInstanceOf(Collection.class, prop);
        assertEquals(4, collection.size());

        config.clearTree("tables.table(0).fields");
        assertNull(config.getProperty("tables.table(0).fields.field.name"));
        prop = config.getProperty("tables.table.fields.field.name");
        collection = assertInstanceOf(Collection.class, prop);
        assertEquals(NodeStructureHelper.fieldsLength(1), collection.size());

        config.clearTree("tables.table(1)");
        assertNull(config.getProperty("tables.table.fields.field.name"));
    }

    /**
     * Tests removing more complex node structures.
     */
    @Test
    public void testClearTreeComplex() {
        final int count = 5;
        // create the structure
        for (int idx = 0; idx < count; idx++) {
            config.addProperty("indexList.index(-1)[@default]", Boolean.FALSE);
            config.addProperty("indexList.index[@name]", "test" + idx);
            config.addProperty("indexList.index.dir", "testDir" + idx);
        }
        assertEquals(count - 1, config.getMaxIndex("indexList.index[@name]"));

        // Remove a sub tree
        boolean found = false;
        for (int idx = 0; true; idx++) {
            final String name = config.getString("indexList.index(" + idx + ")[@name]");
            if (name == null) {
                break;
            }
            if ("test3".equals(name)) {
                assertEquals("testDir3", config.getString("indexList.index(" + idx + ").dir"));
                config.clearTree("indexList.index(" + idx + ")");
                found = true;
            }
        }
        assertTrue(found);
        assertEquals(count - 2, config.getMaxIndex("indexList.index[@name]"));
        assertEquals(count - 2, config.getMaxIndex("indexList.index.dir"));

        // Verify
        for (int idx = 0; true; idx++) {
            final String name = config.getString("indexList.index(" + idx + ")[@name]");
            if (name == null) {
                break;
            }
            assertNotEquals("test3", name);
        }
    }

    /**
     * Tests the clearTree() method on a hierarchical structure of nodes. This is a test case for CONFIGURATION-293.
     */
    @Test
    public void testClearTreeHierarchy() {
        config.addProperty("a.b.c", "c");
        config.addProperty("a.b.c.d", "d");
        config.addProperty("a.b.c.d.e", "e");
        config.clearTree("a.b.c");
        assertFalse(config.containsKey("a.b.c"));
        assertFalse(config.containsKey("a.b.c.d"));
    }

    @Test
    public void testClone() {
        final Configuration copy = (Configuration) config.clone();
        assertInstanceOf(AbstractHierarchicalConfiguration.class, copy);
        checkContent(copy);
    }

    /**
     * Tests whether interpolation works as expected after cloning.
     */
    @Test
    public void testCloneInterpolation() {
        final String keyAnswer = "answer";
        final String keyValue = "value";
        config.addProperty(keyAnswer, "The answer is ${" + keyValue + "}.");
        config.addProperty(keyValue, 42);
        final Configuration clone = (Configuration) config.clone();
        clone.setProperty(keyValue, 43);
        assertEquals("The answer is 42.", config.getString(keyAnswer));
        assertEquals("The answer is 43.", clone.getString(keyAnswer));
    }

    /**
     * Tests whether registered event handlers are handled correctly when a configuration is cloned. They should not be
     * registered at the clone.
     */
    @Test
    public void testCloneWithEventListeners() {
        final EventListener<ConfigurationEvent> l = new EventListenerTestImpl(null);
        config.addEventListener(ConfigurationEvent.ANY, l);
        final AbstractHierarchicalConfiguration<?> copy = (AbstractHierarchicalConfiguration<?>) config.clone();
        assertFalse(copy.getEventListeners(ConfigurationEvent.ANY).contains(l));
    }

    @Test
    public void testContainsKey() {
        assertTrue(config.containsKey("tables.table(0).name"));
        assertTrue(config.containsKey("tables.table(1).name"));
        assertFalse(config.containsKey("tables.table(2).name"));

        assertTrue(config.containsKey("tables.table(0).fields.field.name"));
        assertFalse(config.containsKey("tables.table(0).fields.field"));
        config.clearTree("tables.table(0).fields");
        assertFalse(config.containsKey("tables.table(0).fields.field.name"));

        assertTrue(config.containsKey("tables.table.fields.field.name"));
    }

    @Test
    public void testGetKeys() {
        final List<String> keys = new ArrayList<>();
        for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
            keys.add(it.next());
        }

        assertEquals(Arrays.asList("tables.table.name", "tables.table.fields.field.name"), keys);
    }

    /**
     * Tests whether attribute keys are contained in the iteration of keys.
     */
    @Test
    public void testGetKeysAttribute() {
        config.addProperty("tables.table(0)[@type]", "system");
        final Set<String> keys = new HashSet<>();
        for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
            keys.add(it.next());
        }
        assertTrue(keys.contains("tables.table[@type]"));
    }

    /**
     * Tests whether a prefix that points to an attribute is correctly handled.
     */
    @Test
    public void testGetKeysAttributePrefix() {
        config.addProperty("tables.table(0)[@type]", "system");
        final Iterator<String> itKeys = config.getKeys("tables.table[@type]");
        assertEquals("tables.table[@type]", itKeys.next());
        assertFalse(itKeys.hasNext());
    }

    /**
     * Tests whether keys are returned in a defined order.
     */
    @Test
    public void testGetKeysOrder() {
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key2", "value2");
        config.addProperty("order.key3", "value3");

        final Iterator<String> it = config.getKeys("order");
        assertEquals("order.key1", it.next());
        assertEquals("order.key2", it.next());
        assertEquals("order.key3", it.next());
    }

    @Test
    public void testGetKeysString() {
        // add some more properties to make it more interesting
        config.addProperty("tables.table(0).fields.field(1).type", "VARCHAR");
        config.addProperty("tables.table(0)[@type]", "system");
        config.addProperty("tables.table(0).size", "42");
        config.addProperty("tables.table(0).fields.field(0).size", "128");
        config.addProperty("connections.connection.param.url", "url1");
        config.addProperty("connections.connection.param.user", "me");
        config.addProperty("connections.connection.param.pwd", "secret");
        config.addProperty("connections.connection(-1).param.url", "url2");
        config.addProperty("connections.connection(1).param.user", "guest");

        checkKeys("tables.table(1)", new String[] {"name", "fields.field.name"});
        checkKeys("tables.table(0)", new String[] {"name", "fields.field.name", "tables.table(0)[@type]", "size", "fields.field.type", "fields.field.size"});
        checkKeys("connections.connection(0).param", new String[] {"url", "user", "pwd"});
        checkKeys("connections.connection(1).param", new String[] {"url", "user"});
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key.
     */
    @Test
    public void testGetKeysWithKeyAsPrefix() {
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key2", "value2");
        final Iterator<String> it = config.getKeys("order.key1");
        assertTrue(it.hasNext());
        assertEquals("order.key1", it.next());
        assertFalse(it.hasNext());
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key, and there are multiple keys starting with this
     * prefix.
     */
    @Test
    public void testGetKeysWithKeyAsPrefixMultiple() {
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key1.test", "value2");
        config.addProperty("order.key1.test.complex", "value2");
        final Iterator<String> it = config.getKeys("order.key1");
        assertEquals("order.key1", it.next());
        assertEquals("order.key1.test", it.next());
        assertEquals("order.key1.test.complex", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetMaxIndex() {
        assertEquals(NodeStructureHelper.fieldsLength(0) - 1, config.getMaxIndex("tables.table(0).fields.field"));
        assertEquals(NodeStructureHelper.fieldsLength(1) - 1, config.getMaxIndex("tables.table(1).fields.field"));
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals(1, config.getMaxIndex("tables.table.name"));
        assertEquals(0, config.getMaxIndex("tables.table(0).name"));
        assertEquals(0, config.getMaxIndex("tables.table(1).fields.field(1)"));
        assertEquals(-1, config.getMaxIndex("tables.table(2).fields"));

        final int maxIdx = config.getMaxIndex("tables.table(0).fields.field.name");
        for (int i = 0; i <= maxIdx; i++) {
            final DefaultConfigurationKey key = new DefaultConfigurationKey(DefaultExpressionEngine.INSTANCE, "tables.table(0).fields");
            key.append("field").appendIndex(i).append("name");
            assertNotNull(config.getProperty(key.toString()));
        }
    }

    /**
     * Tests whether the configuration's node model can be correctly accessed.
     */
    @Test
    public void testGetNodeModel() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        final NodeModel<ImmutableNode> model = config.getNodeModel();

        assertInstanceOf(InMemoryNodeModel.class, model);
        final ImmutableNode rootNode = model.getNodeHandler().getRootNode();
        assertEquals(1, rootNode.getChildren().size());
        assertTrue(rootNode.getChildren().contains(NodeStructureHelper.ROOT_TABLES_TREE));
        sync.verify(SynchronizerTestImpl.Methods.BEGIN_READ, SynchronizerTestImpl.Methods.END_READ);
    }

    @Test
    public void testGetProperty() {
        checkGetProperty(config);
    }

    /**
     * Tests whether keys that contains brackets can be used.
     */
    @Test
    public void testGetPropertyKeyWithBrackets() {
        final String key = "test.directory.platform(x86)";
        config.addProperty(key, "C:\\Temp");
        assertEquals("C:\\Temp", config.getString(key));
    }

    /**
     * Tests the copy constructor when a null reference is passed.
     */
    @Test
    public void testInitCopyNull() {
        final BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration((BaseHierarchicalConfiguration) null);
        assertTrue(copy.isEmpty());
    }

    /**
     * Tests obtaining a configuration with all variables substituted.
     */
    @Test
    public void testInterpolatedConfiguration() {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        final AbstractHierarchicalConfiguration<?> c = (AbstractHierarchicalConfiguration<?>) InterpolationTestHelper.testInterpolatedConfiguration(config);

        // tests whether the hierarchical structure has been maintained
        checkGetProperty(c);
    }

    /**
     * Tests interpolation facilities.
     */
    @Test
    public void testInterpolation() {
        config.addProperty("base.dir", "/home/foo");
        config.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        config.addProperty("test.absolute.dir.dir2", "${base.dir}/path2");
        config.addProperty("test.absolute.dir.dir3", "${base.dir}/path3");
        final Configuration sub = config.subset("test.absolute.dir");
        for (int i = 1; i < 4; i++) {
            assertEquals("/home/foo/path" + i, config.getString("test.absolute.dir.dir" + i));
            assertEquals("/home/foo/path" + i, sub.getString("dir" + i));
        }
    }

    /**
     * Basic interpolation tests.
     */
    @Test
    public void testInterpolationBasic() {
        InterpolationTestHelper.testInterpolation(config);
    }

    /**
     * Tests interpolation with constant values.
     */
    @Test
    public void testInterpolationConstants() {
        InterpolationTestHelper.testInterpolationConstants(config);
    }

    /**
     * Tests escaping variables.
     */
    @Test
    public void testInterpolationEscaped() {
        InterpolationTestHelper.testInterpolationEscaped(config);
    }

    /**
     * Tests interpolation with localhost values.
     */
    @Test
    public void testInterpolationLocalhost() {
        InterpolationTestHelper.testInterpolationLocalhost(config);
    }

    /**
     * Tests an invalid interpolation that causes an endless loop.
     */
    @Test
    public void testInterpolationLoop() {
        InterpolationTestHelper.testInterpolationLoop(config);
    }

    /**
     * Tests multiple levels of interpolation.
     */
    @Test
    public void testInterpolationMultipleLevels() {
        InterpolationTestHelper.testMultipleInterpolation(config);
    }

    /**
     * Tests interpolation with a subset.
     */
    @Test
    public void testInterpolationSubset() {
        InterpolationTestHelper.testInterpolationSubset(config);
    }

    /**
     * Tests whether interpolation with a subset configuration works over multiple layers.
     */
    @Test
    public void testInterpolationSubsetMultipleLayers() {
        config.clear();
        config.addProperty("var", "value");
        config.addProperty("prop2.prop[@attr]", "${var}");
        final Configuration sub1 = config.subset("prop2");
        final Configuration sub2 = sub1.subset("prop");
        assertEquals("value", sub2.getString("[@attr]"));
    }

    /**
     * Tests interpolation with system properties.
     */
    @Test
    public void testInterpolationSystemProperties() {
        InterpolationTestHelper.testInterpolationSystemProperties(config);
    }

    /**
     * Tests interpolation of a variable, which cannot be resolved.
     */
    @Test
    public void testInterpolationUnknownProperty() {
        InterpolationTestHelper.testInterpolationUnknownProperty(config);
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator() {
        InterpolationTestHelper.testGetInterpolator(config);
    }

    @Test
    public void testIsEmptyFalse() {
        assertFalse(config.isEmpty());
    }

    /**
     * Tests isEmpty() if the structure contains some nodes without values.
     */
    @Test
    public void testIsEmptyNodesWithNoValues() {
        final ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder(1);
        final ImmutableNode.Builder nodeBuilder = new ImmutableNode.Builder(1);
        nodeBuilder.addChild(NodeStructureHelper.createNode("child", null));
        rootBuilder.addChild(nodeBuilder.create());
        config = new AbstractHierarchicalConfigurationTestImpl(new InMemoryNodeModel(rootBuilder.create()));
        assertTrue(config.isEmpty());
    }

    /**
     * Tests isEmpty() if only the root node exists.
     */
    @Test
    public void testIsEmptyRootOnly() {
        config = new AbstractHierarchicalConfigurationTestImpl(new InMemoryNodeModel());
        assertTrue(config.isEmpty());
    }

    /**
     * Tests nodeKey() if the key is directly found in the cache.
     */
    @Test
    public void testNodeKeyCacheHit() {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        final String key = "someResultKey";
        cache.put(getRootNode(), key);
        assertEquals(key, config.nodeKey(getRootNode(), cache, config.getModel().getNodeHandler()));
    }

    /**
     * Tests whether the cache map is filled while generating node keys.
     */
    @Test
    public void testNodeKeyCachePopulated() {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        final ImmutableNode nodeTabName = NodeStructureHelper.nodeForKey(getRootNode(), "tables/table(0)/name");
        final NodeHandler<ImmutableNode> handler = config.getModel().getNodeHandler();
        config.nodeKey(nodeTabName, cache, handler);
        assertEquals(4, cache.size());
        assertEquals("tables(0).table(0).name(0)", cache.get(nodeTabName));
        assertEquals("tables(0).table(0)", cache.get(handler.getParent(nodeTabName)));
        assertEquals("tables(0)", cache.get(handler.getParent(handler.getParent(nodeTabName))));
        assertEquals("", cache.get(getRootNode()));
    }

    /**
     * Tests whether the cache is used by nodeKey().
     */
    @Test
    public void testNodeKeyCacheUsage() {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        final ImmutableNode nodeTabName = NodeStructureHelper.nodeForKey(getRootNode(), "tables/table(0)/name");
        final NodeHandler<ImmutableNode> handler = config.getModel().getNodeHandler();
        cache.put(handler.getParent(nodeTabName), "somePrefix");
        assertEquals("somePrefix.name(0)", config.nodeKey(nodeTabName, cache, handler));
    }

    /**
     * Tests whether a correct node key is generated if no data is contained in the cache.
     */
    @Test
    public void testNodeKeyEmptyCache() {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        final ImmutableNode nodeTabName = NodeStructureHelper.nodeForKey(getRootNode(), "tables/table(0)/name");
        final ImmutableNode nodeFldName = NodeStructureHelper.nodeForKey(getRootNode(), "tables/table(0)/fields/field(1)/name");
        assertEquals("tables(0).table(0).name(0)", config.nodeKey(nodeTabName, cache, config.getModel().getNodeHandler()));
        assertEquals("tables(0).table(0).fields(0).field(1).name(0)", config.nodeKey(nodeFldName, cache, config.getModel().getNodeHandler()));
    }

    /**
     * Tests whether a node key for the root node can be generated.
     */
    @Test
    public void testNodeKeyRootNode() {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        assertEquals("", config.nodeKey(getRootNode(), cache, config.getModel().getNodeHandler()));
    }

    /**
     * Tests whether node keys can be resolved.
     */
    @Test
    public void testResolveNodeKey() {
        final List<ImmutableNode> nodes = config.resolveNodeKey(getRootNode(), "tables.table.name", config.getModel().getNodeHandler());
        assertEquals(NodeStructureHelper.tablesLength(), nodes.size());
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            assertEquals(NodeStructureHelper.table(i), nodes.get(i).getValue(), "Wrong node value at " + i);
        }
    }

    /**
     * Tests whether attribute keys are filtered out when resolving node keys.
     */
    @Test
    public void testResolveNodeKeyAttribute() {
        final String attrKey = "tables.table(0)[@type]";
        config.addProperty(attrKey, "system");
        assertTrue(config.resolveNodeKey(getRootNode(), attrKey, config.getModel().getNodeHandler()).isEmpty());
    }

    /**
     * Tests setting a custom expression engine, which uses a slightly different syntax.
     */
    @Test
    public void testSetExpressionEngine() {
        config.setExpressionEngine(null);
        assertNotNull(config.getExpressionEngine());
        assertSame(DefaultExpressionEngine.INSTANCE, config.getExpressionEngine());

        config.setExpressionEngine(createAlternativeExpressionEngine());
        checkAlternativeSyntax();
    }

    @Test
    public void testSetProperty() {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.setProperty("tables.table(0).name", "resources");
        assertEquals("resources", config.getString("tables.table(0).name"));
        config.setProperty("tables.table.name", "tab1,tab2");
        assertEquals("tab1", config.getString("tables.table(0).name"));
        assertEquals("tab2", config.getString("tables.table(1).name"));

        config.setProperty("test.items.item", new int[] {2, 4, 8, 16});
        assertEquals(3, config.getMaxIndex("test.items.item"));
        assertEquals(8, config.getInt("test.items.item(2)"));
        config.setProperty("test.items.item(2)", Integer.valueOf(6));
        assertEquals(6, config.getInt("test.items.item(2)"));
        config.setProperty("test.items.item(2)", new int[] {7, 9, 11});
        assertEquals(5, config.getMaxIndex("test.items.item"));

        config.setProperty("test", Boolean.TRUE);
        config.setProperty("test.items", "01/01/05");
        assertEquals(5, config.getMaxIndex("test.items.item"));
        assertTrue(config.getBoolean("test"));
        assertEquals("01/01/05", config.getProperty("test.items"));

        config.setProperty("test.items.item", Integer.valueOf(42));
        assertEquals(0, config.getMaxIndex("test.items.item"));
        assertEquals(42, config.getInt("test.items.item"));
    }

    /**
     * Tests whether the correct size is calculated.
     */
    @Test
    public void testSize() {
        assertEquals(2, config.size());
    }
}
