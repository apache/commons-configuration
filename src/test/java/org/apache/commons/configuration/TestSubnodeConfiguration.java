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
package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration.convert.ListDelimiterHandler;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.ex.ConfigurationException;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for SubnodeConfiguration.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestSubnodeConfiguration
{
    /** An array with names of tables (test data). */
    private static final String[] TABLE_NAMES =
    { "documents", "users" };

    /** An array with the fields of the test tables (test data). */
    private static final String[][] TABLE_FIELDS =
    {
    { "docid", "docname", "author", "dateOfCreation", "version", "size" },
    { "userid", "uname", "firstName", "lastName" } };

    /** Constant for an updated table name.*/
    private static final String NEW_TABLE_NAME = "newTable";

    /** The key used for the SubnodeConfiguration. */
    private static final String SUB_KEY = "tables.table(1)";

    /** The parent configuration. */
    private BaseHierarchicalConfiguration parent;

    /** The subnode configuration to be tested. */
    private SubnodeConfiguration config;

    /** Stores a counter for the created nodes. */
    private int nodeCounter;

    @Before
    public void setUp() throws Exception
    {
        parent = setUpParentConfig();
        nodeCounter = 0;
    }

    /**
     * Tests creation of a subnode config.
     */
    @Test
    public void testInitSubNodeConfig()
    {
        setUpSubnodeConfig();
        assertSame("Wrong root node in subnode", getSubnodeRoot(parent), config
                .getRootNode());
        assertSame("Wrong parent config", parent, config.getParent());
    }

    /**
     * Tests constructing a subnode configuration with a null parent. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitSubNodeConfigWithNullParent()
    {
        config = new SubnodeConfiguration(null, getSubnodeRoot(parent), null);
    }

    /**
     * Tests constructing a subnode configuration with a null root node. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitSubNodeConfigWithNullNode()
    {
        config = new SubnodeConfiguration(parent, null, null);
    }

    /**
     * Tests if properties of the sub node can be accessed.
     */
    @Test
    public void testGetProperties()
    {
        setUpSubnodeConfig();
        assertEquals("Wrong table name", TABLE_NAMES[0], config
                .getString("name"));
        List<Object> fields = config.getList("fields.field.name");
        assertEquals("Wrong number of fields", TABLE_FIELDS[0].length, fields
                .size());
        for (int i = 0; i < TABLE_FIELDS[0].length; i++)
        {
            assertEquals("Wrong field at position " + i, TABLE_FIELDS[0][i],
                    fields.get(i));
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
        config.setProperty("name", TABLE_NAMES[0] + "_tested");
        assertEquals("Root value was not set", "testTable", parent
                .getString("tables.table(0)"));
        assertEquals("Table name was not changed", TABLE_NAMES[0] + "_tested",
                parent.getString("tables.table(0).name"));

        parent.setProperty("tables.table(0).fields.field(1).name", "testField");
        assertEquals("Field name was not changed", "testField", config
                .getString("fields.field(1).name"));
    }

    /**
     * Tests adding of properties.
     */
    @Test
    public void testAddProperty()
    {
        setUpSubnodeConfig();
        config.addProperty("[@table-type]", "test");
        assertEquals("parent.createNode() was not called", 1, nodeCounter);
        assertEquals("Attribute not set", "test", parent
                .getString("tables.table(0)[@table-type]"));

        parent.addProperty("tables.table(0).fields.field(-1).name", "newField");
        List<Object> fields = config.getList("fields.field.name");
        assertEquals("New field was not added", TABLE_FIELDS[0].length + 1,
                fields.size());
        assertEquals("Wrong last field", "newField", fields
                .get(fields.size() - 1));
    }

    /**
     * Tests listing the defined keys.
     */
    @Test
    public void testGetKeys()
    {
        setUpSubnodeConfig();
        Set<String> keys = new HashSet<String>();
        CollectionUtils.addAll(keys, config.getKeys());
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
        assertTrue("Exception flag not fetchted from parent", config
                .isThrowExceptionOnMissing());
        config.getString("non existing key");
    }

    /**
     * Tests whether the exception flag can be set independently from the parent.
     */
    @Test
    public void testSetThrowExceptionOnMissingAffectsParent()
    {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        config.setThrowExceptionOnMissing(false);
        assertTrue("Exception flag reset on parent", parent
                .isThrowExceptionOnMissing());
    }

    /**
     * Tests manipulating the list delimiter handler. This object is derived
     * from the parent.
     */
    @Test
    public void testSetListDelimiterHandler()
    {
        ListDelimiterHandler handler1 = new DefaultListDelimiterHandler('/');
        ListDelimiterHandler handler2 = new DefaultListDelimiterHandler(';');
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
        setUpSubnodeConfig();
        assertEquals("Wrong field name", TABLE_FIELDS[0][1], config
                .getString("fields/field[2]/name"));
        Set<String> keys = new HashSet<String>();
        CollectionUtils.addAll(keys, config.getKeys());
        assertEquals("Wrong number of keys", 2, keys.size());
        assertTrue("Key 1 not contained", keys.contains("name"));
        assertTrue("Key 2 not contained", keys.contains("fields/field/name"));
        config.setExpressionEngine(null);
        assertTrue("Expression engine reset on parent", parent
                .getExpressionEngine() instanceof XPathExpressionEngine);
    }

    /**
     * Tests the configurationAt() method.
     */
    @Test
    public void testConfiguarationAt()
    {
        setUpSubnodeConfig();
        SubnodeConfiguration sub2 = config
                .configurationAt("fields.field(1)");
        assertEquals("Wrong value of property", TABLE_FIELDS[0][1], sub2
                .getString("name"));
        assertEquals("Wrong parent", config.getParent(), sub2.getParent());
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
        assertEquals("Wrong interpolated tablespace", "default", parent
                .getString("tables.table(0).tablespace"));

        setUpSubnodeConfig();
        assertEquals("Wrong interpolated tablespace in subnode", "default",
                config.getString("tablespace"));
    }

    /**
     * An additional test for interpolation when the configurationAt() method is
     * involved.
     */
    @Test
    public void testInterpolationFromConfigurationAt()
    {
        parent.addProperty("base.dir", "/home/foo");
        parent.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        parent.addProperty("test.absolute.dir.dir2", "${base.dir}/path2");
        parent.addProperty("test.absolute.dir.dir3", "${base.dir}/path3");

        Configuration sub = parent.configurationAt("test.absolute.dir");
        for (int i = 1; i < 4; i++)
        {
            assertEquals("Wrong interpolation in parent", "/home/foo/path" + i,
                    parent.getString("test.absolute.dir.dir" + i));
            assertEquals("Wrong interpolation in subnode",
                    "/home/foo/path" + i, sub.getString("dir" + i));
        }
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

        Configuration sub = parent.configurationAt("test.absolute.dir");
        assertEquals("Wrong interpolation in subnode",
            "/home/foo/path1", sub.getString("dir1"));
        assertEquals("Wrong local interpolation in subnode",
            "/home/foo/path1", sub.getString("dir2"));
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
    public void testLocalLookupsInInterpolatorAreInherited() {
        parent.addProperty("tablespaces.tablespace.name", "default");
        parent.addProperty("tablespaces.tablespace(-1).name", "test");
        parent.addProperty("tables.table(0).var", "${brackets:x}");

        ConfigurationInterpolator interpolator = parent.getInterpolator();
        interpolator.registerLookup("brackets", new Lookup(){

            @Override
            public String lookup(String key) {
                return "(" + key +")";
            }

        });
        setUpSubnodeConfig();
        assertEquals("Local lookup was not inherited", "(x)", config.getString("var", ""));
    }

    /**
     * Tests a reload operation for the parent configuration when the subnode
     * configuration does not support reloads. Then the new value should not be
     * detected.
     */
    @Test
    public void testParentReloadNotSupported() throws ConfigurationException
    {
        Configuration c = setUpLiveUpdateTest(false);
        assertEquals("Name changed in sub config", TABLE_NAMES[1], config
                .getString("name"));
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
    }

    /**
     * Tests a reload operation for the parent configuration when the subnode
     * configuration does support reloads. The new value should be returned.
     */
    @Test
    public void testParentReloadSupported() throws ConfigurationException
    {
        Configuration c = setUpLiveUpdateTest(true);
        assertEquals("Name not changed in sub config", NEW_TABLE_NAME, config
                .getString("name"));
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
    }

    /**
     * Tests whether events are fired if a change of the parent is detected.
     */
    @Test
    public void testParentReloadEvents() throws ConfigurationException
    {
        config = parent.configurationAt(SUB_KEY, true);
        ConfigurationListenerTestImpl l = new ConfigurationListenerTestImpl();
        config.addConfigurationListener(l);
        updateParent();
        assertEquals("Wrong number of events", 4, l.events.size());
        boolean before = true;
        for (ConfigurationEvent e : l.events)
        {
            assertEquals("Wrong configuration", config, e.getSource());
            assertEquals("Wrong event type",
                    BaseHierarchicalConfiguration.EVENT_SUBNODE_CHANGED, e
                            .getType());
            assertNull("Got a property name", e.getPropertyName());
            assertNull("Got a property value", e.getPropertyValue());
            assertEquals("Wrong before flag", before, e.isBeforeUpdate());
            before = !before;
        }
    }

    /**
     * Tests a reload operation for the parent configuration when the subnode
     * configuration is aware of reloads, and the parent configuration is
     * accessed first. The new value should be returned.
     */
    @Test
    public void testParentReloadSupportAccessParent()
            throws ConfigurationException
    {
        Configuration c = setUpLiveUpdateTest(true);
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
        assertEquals("Name not changed in sub config", NEW_TABLE_NAME, config
                .getString("name"));
    }

    /**
     * Tests whether reloads work with sub subnode configurations.
     */
    @Test
    public void testParentReloadSubSubnode() throws ConfigurationException
    {
        setUpLiveUpdateTest(true);
        SubnodeConfiguration sub = config.configurationAt("fields", true);
        assertEquals("Wrong subnode key", "tables.table(1).fields", sub
                .getSubnodeKey());
        assertEquals("Changed field not detected", "newField", sub
                .getString("field(0).name"));
    }

    /**
     * Tests creating a sub sub config when the sub config is not aware of
     * changes. Then the sub sub config shouldn't be either.
     */
    @Test
    public void testParentReloadSubSubnodeNoChangeSupport()
            throws ConfigurationException
    {
        setUpLiveUpdateTest(false);
        SubnodeConfiguration sub = config.configurationAt("fields", true);
        assertNull("Sub sub config is attached to parent", sub.getSubnodeKey());
        assertEquals("Changed field name returned", TABLE_FIELDS[1][0], sub
                .getString("field(0).name"));
    }

    /**
     * Prepares a test for updates of a SubnodeConfiguration if the node
     * structure of the parent changes. This method replaces the nodes for the
     * tables with new ones.
     *
     * @param supportReload a flag whether the SubnodeConfiguration should
     *        support reload operations
     * @return the parent configuration that can be used for testing
     */
    private HierarchicalConfiguration setUpLiveUpdateTest(boolean supportReload)
    {
        config = parent.configurationAt(SUB_KEY, supportReload);
        updateParent();
        return parent;
    }

    /**
     * Updates the parent configuration. Replaces the node structure so that
     * an attached SubnodeConfiguration should be removed now.
     */
    private void updateParent()
    {
        String[] tableNamesNew = TABLE_NAMES.clone();
        String[][] fieldNamesNew = new String[TABLE_FIELDS.length][];
        for(int i = 0; i < TABLE_FIELDS.length; i++)
        {
            fieldNamesNew[i] = TABLE_FIELDS[i].clone();
        }
        tableNamesNew[1] = NEW_TABLE_NAME;
        fieldNamesNew[1][0] = "newField";
        addTableData(parent, tableNamesNew, fieldNamesNew);
        String keyClear = "tables.table(0)";
        parent.clearTree(keyClear);
        parent.clearTree(keyClear);
    }

    /**
     * Tests a manipulation of the parent configuration that causes the subnode
     * configuration to become invalid. In this case the sub config should be
     * detached and keep its old values.
     */
    @Test
    public void testParentChangeDetach()
    {
        final String key = SUB_KEY;
        config = parent.configurationAt(key, true);
        assertEquals("Wrong subnode key", key, config.getSubnodeKey());
        assertEquals("Wrong table name", TABLE_NAMES[1], config
                .getString("name"));
        parent.clearTree(key);
        assertEquals("Wrong table name after change", TABLE_NAMES[1], config
                .getString("name"));
        assertNull("Sub config was not detached", config.getSubnodeKey());
    }

    /**
     * Tests detaching a subnode configuration when an exception is thrown
     * during reconstruction. This can happen e.g. if the expression engine is
     * changed for the parent.
     */
    @Test
    public void testParentChangeDetatchException()
    {
        config = parent.configurationAt(SUB_KEY, true);
        parent.setExpressionEngine(new XPathExpressionEngine());
        parent.addProperty("newProp", "value");
        assertEquals("Wrong name of table", TABLE_NAMES[1], config
                .getString("name"));
        assertNull("Sub config was not detached", config.getSubnodeKey());
    }

    /**
     * Initializes the parent configuration. This method creates the typical
     * structure of tables and fields nodes.
     *
     * @return the parent configuration
     */
    protected BaseHierarchicalConfiguration setUpParentConfig()
    {
        BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration()
        {
            /**
             * Serial version UID.
             */
            private static final long serialVersionUID = 1L;

            // Provide a special implementation of createNode() to check
            // if it is called by the subnode config
            @Override
            protected ConfigurationNode createNode(String name)
            {
                nodeCounter++;
                return super.createNode(name);
            }
        };
        addTableData(conf, TABLE_NAMES, TABLE_FIELDS);
        return conf;
    }

    /**
     * Appends properties for table names and their fields to the given
     * configuration.
     *
     * @param conf the configuration to be filled
     * @param tableNames an array with the names of the tables to add
     * @param fields and array with the field names per table
     */
    private static void addTableData(Configuration conf, String[] tableNames,
            String[][] fields)
    {
        for (int i = 0; i < tableNames.length; i++)
        {
            conf.addProperty("tables.table(-1).name", tableNames[i]);
            for (int j = 0; j < fields[i].length; j++)
            {
                conf.addProperty("tables.table.fields.field(-1).name",
                        fields[i][j]);
            }
        }
    }

    /**
     * Returns the root node for the subnode config. This method returns the
     * first table node.
     *
     * @param conf the parent config
     * @return the root node for the subnode config
     */
    protected ConfigurationNode getSubnodeRoot(HierarchicalConfiguration conf)
    {
        ConfigurationNode root = conf.getRootNode();
        return root.getChild(0).getChild(0);
    }

    /**
     * Performs a standard initialization of the subnode config to test.
     */
    protected void setUpSubnodeConfig()
    {
        config = new SubnodeConfiguration(parent, getSubnodeRoot(parent), null);
    }

    /**
     * A specialized configuration listener for testing whether the expected
     * events are fired.
     */
    private static class ConfigurationListenerTestImpl implements ConfigurationListener
    {
        /** Stores the events received.*/
        final List<ConfigurationEvent> events = new ArrayList<ConfigurationEvent>();

        @Override
        public void configurationChanged(ConfigurationEvent event)
        {
            events.add(event);
        }
    }
}
