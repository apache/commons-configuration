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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeSelector;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.apache.commons.configuration2.tree.TrackedNodeModel;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for SubnodeConfiguration.
 */
public class TestSubnodeConfiguration {
    /** The key used for the SubnodeConfiguration. */
    private static final String SUB_KEY = "tables.table(0)";

    /** The selector used by the test configuration. */
    private static final NodeSelector SELECTOR = new NodeSelector(SUB_KEY);

    /**
     * Adds a tree structure to the root node of the given configuration.
     *
     * @param configuration the configuration
     * @param root the root of the tree structure to be added
     */
    private static void appendTree(final BaseHierarchicalConfiguration configuration, final ImmutableNode root) {
        configuration.addNodes(null, Collections.singleton(root));
    }

    /**
     * Initializes the parent configuration. This method creates the typical structure of tables and fields nodes.
     *
     * @return the parent configuration
     */
    private static BaseHierarchicalConfiguration setUpParentConfig() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        appendTree(conf, NodeStructureHelper.ROOT_TABLES_TREE);
        return conf;
    }

    /** The parent configuration. */
    private BaseHierarchicalConfiguration parent;

    /** The subnode configuration to be tested. */
    private SubnodeConfiguration config;

    /**
     * Helper method for testing interpolation facilities between a sub and its parent configuration.
     *
     * @param withUpdates the supports updates flag
     */
    private void checkInterpolationFromConfigurationAt(final boolean withUpdates) {
        parent.addProperty("base.dir", "/home/foo");
        parent.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        parent.addProperty("test.absolute.dir.dir2", "${base.dir}/path2");
        parent.addProperty("test.absolute.dir.dir3", "${base.dir}/path3");

        final Configuration sub = parent.configurationAt("test.absolute.dir", withUpdates);
        for (int i = 1; i < 4; i++) {
            assertEquals("/home/foo/path" + i, parent.getString("test.absolute.dir.dir" + i));
            assertEquals("/home/foo/path" + i, sub.getString("dir" + i));
        }
    }

    /**
     * Checks whether the sub configuration has the expected content.
     */
    private void checkSubConfigContent() {
        assertEquals(NodeStructureHelper.table(0), config.getString("name"));
        final List<Object> fields = config.getList("fields.field.name");

        final List<String> expected = new ArrayList<>();
        for (int i = 0; i < NodeStructureHelper.fieldsLength(0); i++) {
            expected.add(NodeStructureHelper.field(0, i));
        }
        assertEquals(expected, fields);
    }

    @BeforeEach
    public void setUp() throws Exception {
        parent = setUpParentConfig();
    }

    /**
     * Performs a standard initialization of the subnode config to test.
     */
    private void setUpSubnodeConfig() {
        setUpSubnodeConfig(SUB_KEY);
    }

    /**
     * Initializes the test configuration using the specified key.
     *
     * @param key the key
     */
    private void setUpSubnodeConfig(final String key) {
        config = (SubnodeConfiguration) parent.configurationAt(key, true);
    }

    /**
     * Sets up the tracked model for the sub configuration.
     *
     * @param selector the selector
     * @return the tracked model
     */
    private TrackedNodeModel setUpTrackedModel(final NodeSelector selector) {
        final InMemoryNodeModel parentModel = (InMemoryNodeModel) parent.getModel();
        parentModel.trackNode(selector, parent);
        return new TrackedNodeModel(parent, selector, true);
    }

    /**
     * Tests adding of properties.
     */
    @Test
    public void testAddProperty() {
        setUpSubnodeConfig();
        config.addProperty("[@table-type]", "test");
        assertEquals("test", parent.getString("tables.table(0)[@table-type]"));

        parent.addProperty("tables.table(0).fields.field(-1).name", "newField");
        final List<Object> fields = config.getList("fields.field.name");
        assertEquals(NodeStructureHelper.fieldsLength(0) + 1, fields.size());
        assertEquals("newField", fields.get(fields.size() - 1));
    }

    /**
     * Tests whether a clone of a sub configuration can be created.
     */
    @Test
    public void testClone() {
        setUpSubnodeConfig();
        final SubnodeConfiguration copy = (SubnodeConfiguration) config.clone();
        assertNotSame(config.getModel(), copy.getModel());
        final TrackedNodeModel subModel = (TrackedNodeModel) copy.getModel();
        assertEquals(SELECTOR, subModel.getSelector());
        final InMemoryNodeModel parentModel = (InMemoryNodeModel) parent.getModel();
        assertEquals(parentModel, subModel.getParentModel());

        // Check whether the track count was increased
        parentModel.untrackNode(SELECTOR);
        parentModel.untrackNode(SELECTOR);
        assertTrue(subModel.isReleaseTrackedNodeOnFinalize());
    }

    /**
     * Tests whether the configuration can be closed.
     */
    @Test
    public void testClose() {
        final TrackedNodeModel model = mock(TrackedNodeModel.class);

        when(model.getSelector()).thenReturn(SELECTOR);

        final SubnodeConfiguration config = new SubnodeConfiguration(parent, model);
        config.close();

        verify(model).getSelector();
        verify(model).close();
        verifyNoMoreInteractions(model);
    }

    /**
     * Tests the configurationAt() method if updates are not supported.
     */
    @Test
    public void testConfiguarationAtNoUpdates() {
        setUpSubnodeConfig();
        final HierarchicalConfiguration<ImmutableNode> sub2 = config.configurationAt("fields.field(1)");
        assertEquals(NodeStructureHelper.field(0, 1), sub2.getString("name"));
        parent.setProperty("tables.table(0).fields.field(1).name", "otherName");
        assertEquals(NodeStructureHelper.field(0, 1), sub2.getString("name"));
    }

    /**
     * Tests configurationAt() if updates are supported.
     */
    @Test
    public void testConfigurationAtWithUpdateSupport() {
        setUpSubnodeConfig();
        final SubnodeConfiguration sub2 = (SubnodeConfiguration) config.configurationAt("fields.field(1)", true);
        assertEquals(NodeStructureHelper.field(0, 1), sub2.getString("name"));
        assertEquals(config, sub2.getParent());
    }

    /**
     * Tests listing the defined keys.
     */
    @Test
    public void testGetKeys() {
        setUpSubnodeConfig();
        final Set<String> keys = new HashSet<>(ConfigurationAssert.keysToList(config));
        assertEquals(new HashSet<>(Arrays.asList("name", "fields.field.name")), keys);
    }

    /**
     * Tests whether a correct node model is returned for the sub configuration. This test is related to CONFIGURATION-670.
     */
    @Test
    public void testGetNodeModel() {
        setUpSubnodeConfig();
        final InMemoryNodeModel nodeModel = config.getNodeModel();

        assertEquals("table", nodeModel.getNodeHandler().getRootNode().getNodeName());
    }

    /**
     * Tests if properties of the sub node can be accessed.
     */
    @Test
    public void testGetProperties() {
        setUpSubnodeConfig();
        checkSubConfigContent();
    }

    /**
     * Tests creation of a subnode config.
     */
    @Test
    public void testInitSubNodeConfig() {
        setUpSubnodeConfig();
        assertSame(NodeStructureHelper.nodeForKey(parent.getModel().getNodeHandler().getRootNode(), "tables/table(0)"),
                config.getModel().getNodeHandler().getRootNode());
        assertSame(parent, config.getParent());
    }

    /**
     * Tests constructing a subnode configuration with a null node model. This should cause an exception.
     */
    @Test
    public void testInitSubNodeConfigWithNullNode() {
        assertThrows(IllegalArgumentException.class, () -> new SubnodeConfiguration(parent, null));
    }

    /**
     * Tests constructing a subnode configuration with a null parent. This should cause an exception.
     */
    @Test
    public void testInitSubNodeConfigWithNullParent() {
        final TrackedNodeModel model = setUpTrackedModel(SELECTOR);
        assertThrows(IllegalArgumentException.class, () -> new SubnodeConfiguration(null, model));
    }

    /**
     * Tests interpolation features. The subnode config should use its parent for interpolation.
     */
    @Test
    public void testInterpolation() {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");
        parent.addProperty("tables.table(0).tablespace", "${tablespaces.tablespace(0).name}");
        assertEquals("default", parent.getString("tables.table(0).tablespace"));

        setUpSubnodeConfig();
        assertEquals("default", config.getString("tablespace"));
    }

    /**
     * Tests whether interpolation works for a sub configuration obtained via configurationAt() if updates are not
     * supported.
     */
    @Test
    public void testInterpolationFromConfigurationAtNoUpdateSupport() {
        checkInterpolationFromConfigurationAt(false);
    }

    /**
     * Tests whether interpolation works for a sub configuration obtained via configurationAt() if updates are supported.
     */
    @Test
    public void testInterpolationFromConfigurationAtWithUpdateSupport() {
        checkInterpolationFromConfigurationAt(true);
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator() {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");

        setUpSubnodeConfig();
        InterpolationTestHelper.testGetInterpolator(config);
    }

    /**
     * An additional test for interpolation when the configurationAt() method is involved for a local interpolation.
     */
    @Test
    public void testLocalInterpolationFromConfigurationAt() {
        parent.addProperty("base.dir", "/home/foo");
        parent.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        parent.addProperty("test.absolute.dir.dir2", "${dir1}");

        final Configuration sub = parent.configurationAt("test.absolute.dir");
        assertEquals("/home/foo/path1", sub.getString("dir1"));
        assertEquals("/home/foo/path1", sub.getString("dir2"));
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited() {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");
        parent.addProperty("tables.table(0).var", "${brackets:x}");

        final ConfigurationInterpolator interpolator = parent.getInterpolator();
        interpolator.registerLookup("brackets", key -> "(" + key + ")");
        setUpSubnodeConfig();
        assertEquals("(x)", config.getString("var", ""));
    }

    /**
     * Tests a manipulation of the parent configuration that causes the subnode configuration to become invalid. In this
     * case the sub config should be detached and keep its old values.
     */
    @Test
    public void testParentChangeDetach() {
        setUpSubnodeConfig();
        parent.clear();
        checkSubConfigContent();
    }

    /**
     * Tests detaching a subnode configuration if an exception is thrown during reconstruction. This can happen e.g. if the
     * expression engine is changed for the parent.
     */
    @Test
    public void testParentChangeDetatchException() {
        setUpSubnodeConfig();
        parent.setExpressionEngine(new XPathExpressionEngine());
        parent.addProperty("newProp", "value");
        checkSubConfigContent();
    }

    /**
     * Tests changing the expression engine.
     */
    @Test
    public void testSetExpressionEngine() {
        parent.setExpressionEngine(new XPathExpressionEngine());
        setUpSubnodeConfig("tables/table[1]");
        assertEquals(NodeStructureHelper.field(0, 1), config.getString("fields/field[2]/name"));
        final Set<String> keys = ConfigurationAssert.keysToSet(config);
        assertEquals(new HashSet<>(Arrays.asList("name", "fields/field/name")), keys);
        config.setExpressionEngine(null);
        assertInstanceOf(XPathExpressionEngine.class, parent.getExpressionEngine());
    }

    /**
     * Tests manipulating the list delimiter handler. This object is derived from the parent.
     */
    @Test
    public void testSetListDelimiterHandler() {
        final ListDelimiterHandler handler1 = new DefaultListDelimiterHandler('/');
        final ListDelimiterHandler handler2 = new DefaultListDelimiterHandler(';');
        parent.setListDelimiterHandler(handler1);
        setUpSubnodeConfig();
        parent.setListDelimiterHandler(handler2);
        assertEquals(handler1, config.getListDelimiterHandler());
        config.addProperty("newProp", "test1,test2/test3");
        assertEquals("test1,test2", parent.getString("tables.table(0).newProp"));
        config.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        assertEquals(handler2, parent.getListDelimiterHandler());
    }

    /**
     * Tests setting of properties in both the parent and the subnode configuration and whether the changes are visible to
     * each other.
     */
    @Test
    public void testSetProperty() {
        setUpSubnodeConfig();
        config.setProperty(null, "testTable");
        config.setProperty("name", NodeStructureHelper.table(0) + "_tested");
        assertEquals("testTable", parent.getString("tables.table(0)"));
        assertEquals(NodeStructureHelper.table(0) + "_tested", parent.getString("tables.table(0).name"));

        parent.setProperty("tables.table(0).fields.field(1).name", "testField");
        assertEquals("testField", config.getString("fields.field(1).name"));
    }

    /**
     * Tests setting the exception on missing flag. The subnode config obtains this flag from its parent.
     */
    @Test
    public void testSetThrowExceptionOnMissing() {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        assertTrue(config.isThrowExceptionOnMissing());
        assertThrows(NoSuchElementException.class, () -> config.getString("non existing key"));
    }

    /**
     * Tests whether the exception flag can be set independently from the parent.
     */
    @Test
    public void testSetThrowExceptionOnMissingAffectsParent() {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        config.setThrowExceptionOnMissing(false);
        assertTrue(parent.isThrowExceptionOnMissing());
    }
}
