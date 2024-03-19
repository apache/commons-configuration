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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.DefaultConfigurationKey;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code BaseHierarchicalConfiguration}.
 */
public class TestHierarchicalConfiguration {
    /** Constant for a changed name. */
    private static final String NEW_NAME = "alteredName";

    /**
     * Creates a {@code DefaultConfigurationKey} object.
     *
     * @return the new key object
     */
    private static DefaultConfigurationKey createConfigurationKey() {
        return new DefaultConfigurationKey(DefaultExpressionEngine.INSTANCE);
    }

    /** The configuration to be tested. */
    private BaseHierarchicalConfiguration config;

    /**
     * Tests access to sub configurations as children of a defined node.
     *
     * @param withUpdates the updates flag
     * @param expectedName the expected table name when reading a property
     */
    private void checkChildConfigurationsAtWithUpdates(final boolean withUpdates, final String expectedName) {
        final String key = "tables.table(0)";
        final List<HierarchicalConfiguration<ImmutableNode>> children = withUpdates ? config.childConfigurationsAt(key, true)
            : config.childConfigurationsAt(key);
        assertEquals(2, children.size());
        final HierarchicalConfiguration<ImmutableNode> sub = children.get(0);
        sub.setProperty(null, NEW_NAME);
        assertEquals(expectedName, config.getString(key + ".name"));
    }

    /**
     * Checks configurationAt() if the passed in key selects an attribute.
     *
     * @param withUpdates the updates flag
     */
    private void checkConfigurationAtAttributeNode(final boolean withUpdates) {
        final String key = "tables.table(0)[@type]";
        config.addProperty(key, "system");
        assertThrows(ConfigurationRuntimeException.class, () -> config.configurationAt(key, withUpdates));
    }

    /**
     * Helper method for checking a configurationsAt() method. It is also tested whether the configuration is connected to
     * its parent.
     *
     * @param withUpdates the updates flag
     * @param expName the expected name in the parent configuration
     */
    private void checkConfigurationsAtWithUpdate(final boolean withUpdates, final String expName) {
        final String key = "tables.table(1).fields.field";
        final List<HierarchicalConfiguration<ImmutableNode>> lstFlds = withUpdates ? config.configurationsAt(key, true) : config.configurationsAt(key);
        checkSubConfigurations(lstFlds);
        lstFlds.get(0).setProperty("name", NEW_NAME);
        assertEquals(expName, config.getString("tables.table(1).fields.field(0).name"));
    }

    /**
     * Checks the content of the passed in configuration object. Used by some tests that copy a configuration.
     *
     * @param c the configuration to check
     */
    private void checkContent(final Configuration c) {
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            assertEquals(NodeStructureHelper.table(i), c.getString("tables.table(" + i + ").name"));
            for (int j = 0; j < NodeStructureHelper.fieldsLength(i); j++) {
                assertEquals(NodeStructureHelper.field(i, j), c.getString("tables.table(" + i + ").fields.field(" + j + ").name"));
            }
        }
    }

    /**
     * Helper method for checking a list of sub configurations pointing to the single fields of the table configuration.
     *
     * @param lstFlds the list with sub configurations
     */
    private void checkSubConfigurations(final List<? extends ImmutableConfiguration> lstFlds) {
        assertEquals(NodeStructureHelper.fieldsLength(1), lstFlds.size());
        for (int i = 0; i < NodeStructureHelper.fieldsLength(1); i++) {
            final ImmutableConfiguration sub = lstFlds.get(i);
            assertEquals(NodeStructureHelper.field(1, i), sub.getString("name"), "Wrong field at position " + i);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        final ImmutableNode root = new ImmutableNode.Builder(1).addChild(NodeStructureHelper.ROOT_TABLES_TREE).create();
        config = new BaseHierarchicalConfiguration();
        config.getNodeModel().setRootNode(root);
    }

    /**
     * Tests the result of childConfigurationsAt() if the key does not point to an existing node.
     */
    @Test
    public void testChildConfigurationsAtNotFound() {
        assertTrue(config.childConfigurationsAt("not.existing.key").isEmpty());
    }

    /**
     * Tests the result of childConfigurationsAt() if the key selects multiple nodes.
     */
    @Test
    public void testChildConfigurationsAtNoUniqueKey() {
        assertTrue(config.childConfigurationsAt("tables.table").isEmpty());
    }

    /**
     * Tests whether sub configurations for the children of a given node can be queried if no updates are propagated.
     */
    @Test
    public void testChildConfigurationsAtNoUpdates() {
        checkChildConfigurationsAtWithUpdates(false, NodeStructureHelper.table(0));
    }

    /**
     * Tests whether sub configurations for the children of a given node can be queried that support updates.
     */
    @Test
    public void testChildConfigurationsAtWithUpdates() {
        checkChildConfigurationsAtWithUpdates(true, NEW_NAME);
    }

    @Test
    public void testClone() {
        final Configuration copy = (Configuration) config.clone();
        assertInstanceOf(BaseHierarchicalConfiguration.class, copy);
        config.setProperty("tables.table(0).name", "changed table name");
        checkContent(copy);
    }

    /**
     * Tests configurationAt() if the passed in key selects an attribute result.
     */
    @Test
    public void testConfigurationAtAttributeNode() {
        checkConfigurationAtAttributeNode(false);
    }

    /**
     * Tests configurationAt() if the passed in key selects an attribute result and the updates flag is set.
     */
    @Test
    public void testConfigurationAtAttributeNodeWithUpdates() {
        checkConfigurationAtAttributeNode(true);
    }

    /**
     * Tests whether a {@code SubnodeConfiguration} can be cleared and its root node can be removed from its parent
     * configuration.
     */
    @Test
    public void testConfigurationAtClearAndDetach() {
        config.addProperty("test.sub.test", "success");
        config.addProperty("test.other", "check");
        final HierarchicalConfiguration<ImmutableNode> sub = config.configurationAt("test.sub", true);
        sub.clear();
        assertTrue(sub.isEmpty());
        assertNull(config.getString("test.sub.test"));
        sub.setProperty("test", "failure!");
        assertNull(config.getString("test.sub.test"));
    }

    /**
     * Tests the configurationAt() method if the passed in key selects multiple nodes. This should cause an exception.
     */
    @Test
    public void testConfigurationAtMultipleNodes() {
        assertThrows(ConfigurationRuntimeException.class, () -> config.configurationAt("tables.table.name"));
    }

    /**
     * Tests configurationAt() if the passed in key selects multiple nodes and the update flag is set.
     */
    @Test
    public void testConfigurationAtMultipleNodesWithUpdates() {
        assertThrows(ConfigurationRuntimeException.class, () -> config.configurationAt("tables.table.name", true));
    }

    /**
     * Tests whether a configuration obtained via configurationAt() contains the expected properties.
     */
    @Test
    public void testConfigurationAtReadAccess() {
        final HierarchicalConfiguration<ImmutableNode> subConfig = config.configurationAt("tables.table(1)");
        assertEquals(NodeStructureHelper.table(1), subConfig.getString("name"));
        final List<Object> lstFlds = subConfig.getList("fields.field.name");

        final List<String> expected = new ArrayList<>();
        for (int i = 0; i < NodeStructureHelper.fieldsLength(1); i++) {
            expected.add(NodeStructureHelper.field(1, i));
        }
        assertEquals(expected, lstFlds);
    }

    /**
     * Tests the configurationAt() method if the passed in key does not exist.
     */
    @Test
    public void testConfigurationAtUnknownSubTree() {
        assertThrows(ConfigurationRuntimeException.class, () -> config.configurationAt("non.existing.key"));
    }

    /**
     * Tests configurationAt() for a non existing key if the update flag is set.
     */
    @Test
    public void testConfigurationAtUnknownSubTreeWithUpdates() {
        assertThrows(ConfigurationRuntimeException.class, () -> config.configurationAt("non.existing.key", true));
    }

    /**
     * Tests an update operation on a parent configuration if the sub configuration is connected.
     */
    @Test
    public void testConfigurationAtUpdateParentConnected() {
        final HierarchicalConfiguration<ImmutableNode> subConfig = config.configurationAt("tables.table(1)", true);
        config.setProperty("tables.table(1).fields.field(2).name", "testField");
        assertEquals("testField", subConfig.getString("fields.field(2).name"));
    }

    /**
     * Tests an update operation on a parent configuration if the sub configuration is independent.
     */
    @Test
    public void testConfigurationAtUpdateParentIndependent() {
        final HierarchicalConfiguration<ImmutableNode> subConfig = config.configurationAt("tables.table(1)");
        config.setProperty("tables.table(1).fields.field(2).name", "testField");
        assertEquals(NodeStructureHelper.field(1, 2), subConfig.getString("fields.field(2).name"));
    }

    /**
     * Tests an update operation on a sub configuration which is connected to its parent.
     */
    @Test
    public void testConfigurationAtUpdateSubConfigConnected() {
        final HierarchicalConfiguration<ImmutableNode> subConfig = config.configurationAt("tables.table(1)", true);
        subConfig.setProperty("name", "testTable");
        assertEquals("testTable", config.getString("tables.table(1).name"));
    }

    /**
     * Tests an update operation on a sub configuration which is independent on its parent.
     */
    @Test
    public void testConfigurationAtUpdateSubConfigIndependent() {
        final HierarchicalConfiguration<ImmutableNode> subConfig = config.configurationAt("tables.table(1)");
        subConfig.setProperty("name", "testTable");
        assertEquals("testTable", subConfig.getString("name"));
        assertEquals(NodeStructureHelper.table(1), config.getString("tables.table(1).name"));
    }

    /**
     * Tests whether a connected configuration is correctly initialized with properties of its parent.
     */
    @Test
    public void testConfigurationAtWithUpdateInitialized() {
        final String key = "tables.table";
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        config.setThrowExceptionOnMissing(true);
        final List<HierarchicalConfiguration<ImmutableNode>> subs = config.configurationsAt(key, true);
        final BaseHierarchicalConfiguration sub = (BaseHierarchicalConfiguration) subs.get(0);
        assertEquals(config.getListDelimiterHandler(), sub.getListDelimiterHandler());
        assertTrue(sub.isThrowExceptionOnMissing());
    }

    /**
     * Tests configurationsAt() if an attribute key is passed in.
     */
    @Test
    public void testConfigurationsAtAttributeKey() {
        final String attrKey = "tables.table(0)[@type]";
        config.addProperty(attrKey, "user");
        assertTrue(config.configurationsAt(attrKey).isEmpty());
    }

    /**
     * Tests the configurationsAt() method when the passed in key does not select any sub nodes.
     */
    @Test
    public void testConfigurationsAtEmpty() {
        assertTrue(config.configurationsAt("unknown.key").isEmpty());
    }

    /**
     * Tests the configurationsAt() method if the sub configurations are not connected..
     */
    @Test
    public void testConfigurationsAtNoUpdate() {
        checkConfigurationsAtWithUpdate(false, NodeStructureHelper.field(1, 0));
    }

    /**
     * Tests configurationsAt() if the sub configurations are connected.
     */
    @Test
    public void testConfigurationsAtWithUpdates() {
        checkConfigurationsAtWithUpdate(true, NEW_NAME);
    }

    /**
     * Tests whether immutable configurations for the children of a given node can be queried.
     */
    @Test
    public void testImmutableChildConfigurationsAt() {
        final List<ImmutableHierarchicalConfiguration> children = config.immutableChildConfigurationsAt("tables.table(0)");
        assertEquals(2, children.size());
        final ImmutableHierarchicalConfiguration c1 = children.get(0);
        assertEquals("name", c1.getRootElementName());
        assertEquals(NodeStructureHelper.table(0), c1.getString(null));
        final ImmutableHierarchicalConfiguration c2 = children.get(1);
        assertEquals("fields", c2.getRootElementName());
        assertEquals(NodeStructureHelper.field(0, 0), c2.getString("field(0).name"));
    }

    /**
     * Tests whether an immutable configuration for a sub tree can be obtained.
     */
    @Test
    public void testImmutableConfigurationAt() {
        final ImmutableHierarchicalConfiguration subConfig = config.immutableConfigurationAt("tables.table(1)");
        assertEquals(NodeStructureHelper.table(1), subConfig.getString("name"));
        final List<Object> lstFlds = subConfig.getList("fields.field.name");

        final List<String> expected = new ArrayList<>();
        for (int i = 0; i < NodeStructureHelper.fieldsLength(1); i++) {
            expected.add(NodeStructureHelper.field(1, i));
        }
        assertEquals(expected, lstFlds);
    }

    /**
     * Tests whether the support updates flag is taken into account when creating an immutable sub configuration.
     */
    @Test
    public void testImmutableConfigurationAtSupportUpdates() {
        final String newTableName = NodeStructureHelper.table(1) + "_other";
        final ImmutableHierarchicalConfiguration subConfig = config.immutableConfigurationAt("tables.table(1)", true);
        config.addProperty("tables.table(-1).name", newTableName);
        config.clearTree("tables.table(1)");
        assertEquals(newTableName, subConfig.getString("name"));
    }

    /**
     * Tests whether a list of immutable sub configurations can be queried.
     */
    @Test
    public void testImmutableConfigurationsAt() {
        final List<ImmutableHierarchicalConfiguration> lstFlds = config.immutableConfigurationsAt("tables.table(1).fields.field");
        checkSubConfigurations(lstFlds);
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testInitCopy() {
        final BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration(config);
        checkContent(copy);
    }

    /**
     * Tests the copy constructor when a null reference is passed.
     */
    @Test
    public void testInitCopyNull() {
        final BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration((HierarchicalConfiguration<ImmutableNode>) null);
        assertTrue(copy.isEmpty());
    }

    /**
     * Tests whether the nodes of a copied configuration are independent from the source configuration.
     */
    @Test
    public void testInitCopyUpdate() {
        final BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration(config);
        config.setProperty("tables.table(0).name", "NewTable");
        checkContent(copy);
    }

    /**
     * Tests obtaining a configuration with all variables substituted.
     */
    @Test
    public void testInterpolatedConfiguration() {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        final BaseHierarchicalConfiguration c = (BaseHierarchicalConfiguration) InterpolationTestHelper.testInterpolatedConfiguration(config);

        // tests whether the hierarchical structure has been maintained
        checkContent(c);
    }

    /**
     * Tests whether interpolation works on an empty configuration.
     */
    @Test
    public void testInterpolatedConfigurationEmpty() {
        config = new BaseHierarchicalConfiguration();
        assertTrue(config.interpolatedConfiguration().isEmpty());
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

    @Test
    public void testSubset() {
        // test the subset on the first table
        Configuration subset = config.subset("tables.table(0)");
        assertEquals(NodeStructureHelper.table(0), subset.getProperty("name"));

        Object prop = subset.getProperty("fields.field.name");
        Collection<?> collection = assertInstanceOf(Collection.class, prop);
        assertEquals(5, collection.size());

        for (int i = 0; i < NodeStructureHelper.fieldsLength(0); i++) {
            final DefaultConfigurationKey key = createConfigurationKey();
            key.append("fields").append("field").appendIndex(i);
            key.append("name");
            assertEquals(NodeStructureHelper.field(0, i), subset.getProperty(key.toString()));
        }

        // test the subset on the second table
        assertTrue(config.subset("tables.table(2)").isEmpty());

        // test the subset on the fields
        subset = config.subset("tables.table.fields.field");
        prop = subset.getProperty("name");
        collection = assertInstanceOf(Collection.class, prop);
        int expectedFieldCount = 0;
        for (int i = 0; i < NodeStructureHelper.tablesLength(); i++) {
            expectedFieldCount += NodeStructureHelper.fieldsLength(i);
        }
        assertEquals(expectedFieldCount, collection.size());

        assertEquals(NodeStructureHelper.field(0, 0), subset.getProperty("name(0)"));

        // test the subset on the field names
        subset = config.subset("tables.table.fields.field.name");
        assertTrue(subset.isEmpty());
    }

    /**
     * Tests subset() if the passed in key selects an attribute.
     */
    @Test
    public void testSubsetAttributeResult() {
        final String key = "tables.table(0)[@type]";
        config.addProperty(key, "system");
        final BaseHierarchicalConfiguration subset = (BaseHierarchicalConfiguration) config.subset(key);
        assertTrue(subset.getModel().getNodeHandler().getRootNode().getChildren().isEmpty());
        assertEquals("system", subset.getString("[@type]"));
    }

    /**
     * Tests the subset() method if the specified key selects multiple keys. The resulting root node should have a value
     * only if exactly one of the selected nodes has a value. Related to CONFIGURATION-295.
     */
    @Test
    public void testSubsetMultipleNodesWithValues() {
        config.setProperty("tables.table(0).fields", "My fields");
        Configuration subset = config.subset("tables.table.fields");
        assertEquals("My fields", subset.getString(""));
        config.setProperty("tables.table(1).fields", "My other fields");
        subset = config.subset("tables.table.fields");
        assertNull(subset.getString(""));
    }

    /**
     * Tests the subset() method if the specified node has a value. This value must be available in the subset, too. Related
     * to CONFIGURATION-295.
     */
    @Test
    public void testSubsetNodeWithValue() {
        config.setProperty("tables.table(0).fields", "My fields");
        final Configuration subset = config.subset("tables.table(0).fields");
        assertEquals(NodeStructureHelper.field(0, 0), subset.getString("field(0).name"));
        assertEquals("My fields", subset.getString(""));
    }
}
