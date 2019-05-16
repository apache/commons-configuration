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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeSelector;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.apache.commons.configuration2.tree.TrackedNodeModel;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for SubnodeConfiguration.
 *
 */
public class TestSubnodeConfiguration
{
    /** The key used for the SubnodeConfiguration. */
    private static final String SUB_KEY = "tables.table(0)";

    /** The selector used by the test configuration. */
    private static final NodeSelector SELECTOR = new NodeSelector(SUB_KEY);

    /** The parent configuration. */
    private BaseHierarchicalConfiguration parent;

    /** The subnode configuration to be tested. */
    private SubnodeConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        parent = setUpParentConfig();
    }

    /**
     * Initializes the parent configuration. This method creates the typical
     * structure of tables and fields nodes.
     *
     * @return the parent configuration
     */
    private static BaseHierarchicalConfiguration setUpParentConfig()
    {
        final BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        appendTree(conf, NodeStructureHelper.ROOT_TABLES_TREE);
        return conf;
    }

    /**
     * Adds a tree structure to the root node of the given configuration.
     *
     * @param configuration the configuration
     * @param root the root of the tree structure to be added
     */
    private static void appendTree(final BaseHierarchicalConfiguration configuration,
            final ImmutableNode root)
    {
        configuration.addNodes(null, Collections.singleton(root));
    }

    /**
     * Performs a standard initialization of the subnode config to test.
     */
    private void setUpSubnodeConfig()
    {
        setUpSubnodeConfig(SUB_KEY);
    }

    /**
     * Initializes the test configuration using the specified key.
     *
     * @param key the key
     */
    private void setUpSubnodeConfig(final String key)
    {
        config = (SubnodeConfiguration) parent.configurationAt(key, true);
    }

    /**
     * Sets up the tracked model for the sub configuration.
     *
     * @param selector the selector
     * @return the tracked model
     */
    private TrackedNodeModel setUpTrackedModel(final NodeSelector selector)
    {
        final InMemoryNodeModel parentModel = (InMemoryNodeModel) parent.getModel();
        parentModel.trackNode(selector, parent);
        return new TrackedNodeModel(parent, selector, true);
    }

    /**
     * Tests creation of a subnode config.
     */
    @Test
    public void testInitSubNodeConfig()
    {
        setUpSubnodeConfig();
        assertSame(
                "Wrong root node in subnode",
                NodeStructureHelper.nodeForKey(parent.getModel()
                        .getNodeHandler().getRootNode(), "tables/table(0)"),
                config.getModel().getNodeHandler().getRootNode());
        assertSame("Wrong parent config", parent, config.getParent());
    }

    /**
     * Tests constructing a subnode configuration with a null parent. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitSubNodeConfigWithNullParent()
    {
        config =
                new SubnodeConfiguration(null, setUpTrackedModel(SELECTOR)
                );
    }

    /**
     * Tests constructing a subnode configuration with a null node model. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitSubNodeConfigWithNullNode()
    {
        config = new SubnodeConfiguration(parent, null);
    }

    /**
     * Tests if properties of the sub node can be accessed.
     */
    @Test
    public void testGetProperties()
    {
        setUpSubnodeConfig();
        checkSubConfigContent();
    }

    /**
     * Checks whether the sub configuration has the expected content.
     */
    private void checkSubConfigContent()
    {
        assertEquals("Wrong table name", NodeStructureHelper.table(0),
                config.getString("name"));
        final List<Object> fields = config.getList("fields.field.name");
        assertEquals("Wrong number of fields",
                NodeStructureHelper.fieldsLength(0), fields.size());
        for (int i = 0; i < NodeStructureHelper.fieldsLength(0); i++)
        {
            assertEquals("Wrong field at position " + i,
                    NodeStructureHelper.field(0, i), fields.get(i));
        }
    }

    /**
     * Tests setting of properties in both the parent and the subnode
     * configuration and whether the changes are visible to each other.
     */
    @Test
    public void testSetProperty()
    {
        setUpSubnodeConfig();
        config.setProperty(null, "testTable");
        config.setProperty("name", NodeStructureHelper.table(0) + "_tested");
        assertEquals("Root value was not set", "testTable",
                parent.getString("tables.table(0)"));
        assertEquals("Table name was not changed", NodeStructureHelper.table(0)
                + "_tested", parent.getString("tables.table(0).name"));

        parent.setProperty("tables.table(0).fields.field(1).name", "testField");
        assertEquals("Field name was not changed", "testField",
                config.getString("fields.field(1).name"));
    }

    /**
     * Tests adding of properties.
     */
    @Test
    public void testAddProperty()
    {
        setUpSubnodeConfig();
        config.addProperty("[@table-type]", "test");
        assertEquals("Attribute not set", "test",
                parent.getString("tables.table(0)[@table-type]"));

        parent.addProperty("tables.table(0).fields.field(-1).name", "newField");
        final List<Object> fields = config.getList("fields.field.name");
        assertEquals("New field was not added",
                NodeStructureHelper.fieldsLength(0) + 1, fields.size());
        assertEquals("Wrong last field", "newField",
                fields.get(fields.size() - 1));
    }

    /**
     * Tests listing the defined keys.
     */
    @Test
    public void testGetKeys()
    {
        setUpSubnodeConfig();
        final Set<String> keys = new HashSet<>();
        keys.addAll(ConfigurationAssert.keysToList(config));
        assertEquals("Incorrect number of keys", 2, keys.size());
        assertTrue("Key 1 not contained", keys.contains("name"));
        assertTrue("Key 2 not contained", keys.contains("fields.field.name"));
    }

    /**
     * Tests setting the exception on missing flag. The subnode config obtains
     * this flag from its parent.
     */
    @Test(expected = NoSuchElementException.class)
    public void testSetThrowExceptionOnMissing()
    {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        assertTrue("Exception flag not fetchted from parent",
                config.isThrowExceptionOnMissing());
        config.getString("non existing key");
    }

    /**
     * Tests whether the exception flag can be set independently from the
     * parent.
     */
    @Test
    public void testSetThrowExceptionOnMissingAffectsParent()
    {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        config.setThrowExceptionOnMissing(false);
        assertTrue("Exception flag reset on parent",
                parent.isThrowExceptionOnMissing());
    }

    /**
     * Tests manipulating the list delimiter handler. This object is derived
     * from the parent.
     */
    @Test
    public void testSetListDelimiterHandler()
    {
        final ListDelimiterHandler handler1 = new DefaultListDelimiterHandler('/');
        final ListDelimiterHandler handler2 = new DefaultListDelimiterHandler(';');
        parent.setListDelimiterHandler(handler1);
        setUpSubnodeConfig();
        parent.setListDelimiterHandler(handler2);
        assertEquals("List delimiter handler not obtained from parent",
                handler1, config.getListDelimiterHandler());
        config.addProperty("newProp", "test1,test2/test3");
        assertEquals("List was incorrectly splitted", "test1,test2",
                parent.getString("tables.table(0).newProp"));
        config.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        assertEquals("List delimiter changed on parent", handler2,
                parent.getListDelimiterHandler());
    }

    /**
     * Tests changing the expression engine.
     */
    @Test
    public void testSetExpressionEngine()
    {
        parent.setExpressionEngine(new XPathExpressionEngine());
        setUpSubnodeConfig("tables/table[1]");
        assertEquals("Wrong field name", NodeStructureHelper.field(0, 1),
                config.getString("fields/field[2]/name"));
        final Set<String> keys = ConfigurationAssert.keysToSet(config);
        assertEquals("Wrong number of keys", 2, keys.size());
        assertTrue("Key 1 not contained", keys.contains("name"));
        assertTrue("Key 2 not contained", keys.contains("fields/field/name"));
        config.setExpressionEngine(null);
        assertTrue("Expression engine reset on parent",
                parent.getExpressionEngine() instanceof XPathExpressionEngine);
    }

    /**
     * Tests the configurationAt() method if updates are not supported.
     */
    @Test
    public void testConfiguarationAtNoUpdates()
    {
        setUpSubnodeConfig();
        final HierarchicalConfiguration<ImmutableNode> sub2 =
                config.configurationAt("fields.field(1)");
        assertEquals("Wrong value of property",
                NodeStructureHelper.field(0, 1), sub2.getString("name"));
        parent.setProperty("tables.table(0).fields.field(1).name", "otherName");
        assertEquals("Change of parent is visible",
                NodeStructureHelper.field(0, 1), sub2.getString("name"));
    }

    /**
     * Tests configurationAt() if updates are supported.
     */
    @Test
    public void testConfigurationAtWithUpdateSupport()
    {
        setUpSubnodeConfig();
        final SubnodeConfiguration sub2 =
                (SubnodeConfiguration) config.configurationAt("fields.field(1)", true);
        assertEquals("Wrong value of property",
                NodeStructureHelper.field(0, 1), sub2.getString("name"));
        assertEquals("Wrong parent", config, sub2.getParent());
    }

    /**
     * Tests interpolation features. The subnode config should use its parent
     * for interpolation.
     */
    @Test
    public void testInterpolation()
    {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");
        parent.addProperty("tables.table(0).tablespace",
                "${tablespaces.tablespace(0).name}");
        assertEquals("Wrong interpolated tablespace", "default",
                parent.getString("tables.table(0).tablespace"));

        setUpSubnodeConfig();
        assertEquals("Wrong interpolated tablespace in subnode", "default",
                config.getString("tablespace"));
    }

    /**
     * Helper method for testing interpolation facilities between a sub and its
     * parent configuration.
     *
     * @param withUpdates the supports updates flag
     */
    private void checkInterpolationFromConfigurationAt(final boolean withUpdates)
    {
        parent.addProperty("base.dir", "/home/foo");
        parent.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        parent.addProperty("test.absolute.dir.dir2", "${base.dir}/path2");
        parent.addProperty("test.absolute.dir.dir3", "${base.dir}/path3");

        final Configuration sub =
                parent.configurationAt("test.absolute.dir", withUpdates);
        for (int i = 1; i < 4; i++)
        {
            assertEquals("Wrong interpolation in parent", "/home/foo/path" + i,
                    parent.getString("test.absolute.dir.dir" + i));
            assertEquals("Wrong interpolation in sub", "/home/foo/path" + i,
                    sub.getString("dir" + i));
        }
    }

    /**
     * Tests whether interpolation works for a sub configuration obtained via
     * configurationAt() if updates are not supported.
     */
    @Test
    public void testInterpolationFromConfigurationAtNoUpdateSupport()
    {
        checkInterpolationFromConfigurationAt(false);
    }

    /**
     * Tests whether interpolation works for a sub configuration obtained via
     * configurationAt() if updates are supported.
     */
    @Test
    public void testInterpolationFromConfigurationAtWithUpdateSupport()
    {
        checkInterpolationFromConfigurationAt(true);
    }

    /**
     * An additional test for interpolation when the configurationAt() method is
     * involved for a local interpolation.
     */
    @Test
    public void testLocalInterpolationFromConfigurationAt()
    {
        parent.addProperty("base.dir", "/home/foo");
        parent.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        parent.addProperty("test.absolute.dir.dir2", "${dir1}");

        final Configuration sub = parent.configurationAt("test.absolute.dir");
        assertEquals("Wrong interpolation in subnode", "/home/foo/path1",
                sub.getString("dir1"));
        assertEquals("Wrong local interpolation in subnode", "/home/foo/path1",
                sub.getString("dir2"));
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator()
    {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");

        setUpSubnodeConfig();
        InterpolationTestHelper.testGetInterpolator(config);
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited()
    {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");
        parent.addProperty("tables.table(0).var", "${brackets:x}");

        final ConfigurationInterpolator interpolator = parent.getInterpolator();
        interpolator.registerLookup("brackets", new Lookup() {

            @Override
            public String lookup(final String key) {
                return "(" + key + ")";
            }

        });
        setUpSubnodeConfig();
        assertEquals("Local lookup was not inherited", "(x)",
                config.getString("var", ""));
    }

    /**
     * Tests a manipulation of the parent configuration that causes the subnode
     * configuration to become invalid. In this case the sub config should be
     * detached and keep its old values.
     */
    @Test
    public void testParentChangeDetach()
    {
        setUpSubnodeConfig();
        parent.clear();
        checkSubConfigContent();
    }

    /**
     * Tests detaching a subnode configuration if an exception is thrown during
     * reconstruction. This can happen e.g. if the expression engine is changed
     * for the parent.
     */
    @Test
    public void testParentChangeDetatchException()
    {
        setUpSubnodeConfig();
        parent.setExpressionEngine(new XPathExpressionEngine());
        parent.addProperty("newProp", "value");
        checkSubConfigContent();
    }

    /**
     * Tests whether a clone of a sub configuration can be created.
     */
    @Test
    public void testClone()
    {
        setUpSubnodeConfig();
        final SubnodeConfiguration copy = (SubnodeConfiguration) config.clone();
        assertNotSame("Same model", config.getModel(), copy.getModel());
        final TrackedNodeModel subModel = (TrackedNodeModel) copy.getModel();
        assertEquals("Wrong selector", SELECTOR, subModel.getSelector());
        final InMemoryNodeModel parentModel = (InMemoryNodeModel) parent.getModel();
        assertEquals("Wrong parent model", parentModel,
                subModel.getParentModel());

        // Check whether the track count was increased
        parentModel.untrackNode(SELECTOR);
        parentModel.untrackNode(SELECTOR);
        assertTrue("Wrong finalize flag",
                subModel.isReleaseTrackedNodeOnFinalize());
    }

    /**
     * Tests whether the configuration can be closed.
     */
    @Test
    public void testClose()
    {
        final TrackedNodeModel model = EasyMock.createMock(TrackedNodeModel.class);
        EasyMock.expect(model.getSelector()).andReturn(SELECTOR).anyTimes();
        model.close();
        EasyMock.replay(model);

        final SubnodeConfiguration config = new SubnodeConfiguration(parent, model);
        config.close();
        EasyMock.verify(model);
    }

    /**
     * Tests whether a correct node model is returned for the sub
     * configuration. This test is related to CONFIGURATION-670.
     */
    @Test
    public void testGetNodeModel()
    {
        setUpSubnodeConfig();
        final InMemoryNodeModel nodeModel = config.getNodeModel();

        assertEquals("Wrong root node", "table",
                nodeModel.getNodeHandler().getRootNode().getNodeName());
    }
}
