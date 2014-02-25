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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationKey;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for BaseHierarchicalConfiguration.
 *
 * @version $Id$
 */
public class TestHierarchicalConfiguration
{
    private static String[] tables = { "users", "documents" };

    private static String[][] fields =
    {
        { "uid", "uname", "firstName", "lastName", "email" },
        { "docid", "name", "creationDate", "authorID", "version" }
    };

    private BaseHierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = new BaseHierarchicalConfiguration();
        fillConfiguration(tables);
    }

    /**
     * Initialize the configuration with the following structure:
     *
     * tables
     *      table
     *         name
     *         fields
     *             field
     *                 name
     *             field
     *                 name
     * @param tabNames the array with the names of the test tables
     */
    private void fillConfiguration(String[] tabNames)
    {
        ConfigurationNode nodeTables = createNode("tables", null);
        for(int i = 0; i < tabNames.length; i++)
        {
            ConfigurationNode nodeTable = createNode("table", null);
            nodeTables.addChild(nodeTable);
            ConfigurationNode nodeName = createNode("name", tabNames[i]);
            nodeTable.addChild(nodeName);
            ConfigurationNode nodeFields = createNode("fields", null);
            nodeTable.addChild(nodeFields);

            for (int j = 0; j < fields[i].length; j++)
            {
                nodeFields.addChild(createFieldNode(fields[i][j]));
            }
        }

        config.getRootNode().addChild(nodeTables);
    }

    @Test
    public void testSetRootNode()
    {
        ConfigurationNode root = new DefaultConfigurationNode("testNode");
        config.setRootNode(root);
        assertSame("Wrong root node", root, config.getRootNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRootNodeNull()
    {
        config.setRootNode(null);
    }

    @Test
    public void testIsEmpty()
    {
        assertFalse(config.isEmpty());
        BaseHierarchicalConfiguration conf2 = new BaseHierarchicalConfiguration();
        assertTrue(conf2.isEmpty());
        ConfigurationNode child1 = new DefaultConfigurationNode("child1");
        ConfigurationNode child2 = new DefaultConfigurationNode("child2");
        child1.addChild(child2);
        conf2.getRootNode().addChild(child1);
        assertTrue(conf2.isEmpty());
    }

    @Test
    public void testGetProperty()
    {
        assertNull(config.getProperty("tables.table.resultset"));
        assertNull(config.getProperty("tables.table.fields.field"));

        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());

        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection<?>) prop).size());

        prop = config.getProperty("tables.table.fields.field(3).name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection<?>) prop).size());

        prop = config.getProperty("tables.table(1).fields.field(2).name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());
    }

    @Test
    public void testSetProperty()
    {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.setProperty("tables.table(0).name", "resources");
        assertEquals("resources", config.getString("tables.table(0).name"));
        config.setProperty("tables.table.name", "tab1,tab2");
        assertEquals("tab1", config.getString("tables.table(0).name"));
        assertEquals("tab2", config.getString("tables.table(1).name"));

        config.setProperty("test.items.item", new int[] { 2, 4, 8, 16 });
        assertEquals(3, config.getMaxIndex("test.items.item"));
        assertEquals(8, config.getInt("test.items.item(2)"));
        config.setProperty("test.items.item(2)", new Integer(6));
        assertEquals(6, config.getInt("test.items.item(2)"));
        config.setProperty("test.items.item(2)", new int[] { 7, 9, 11 });
        assertEquals(5, config.getMaxIndex("test.items.item"));

        config.setProperty("test", Boolean.TRUE);
        config.setProperty("test.items", "01/01/05");
        assertEquals(5, config.getMaxIndex("test.items.item"));
        assertTrue(config.getBoolean("test"));
        assertEquals("01/01/05", config.getProperty("test.items"));

        config.setProperty("test.items.item", new Integer(42));
        assertEquals(0, config.getMaxIndex("test.items.item"));
        assertEquals(42, config.getInt("test.items.item"));
    }

    @Test
    public void testClear()
    {
        config.setProperty(null, "value");
        config.addProperty("[@attr]", "defined");
        config.clear();
        assertTrue("Configuration not empty", config.isEmpty());
    }

    @Test
    public void testClearProperty()
    {
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
    public void testClearTree()
    {
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        config.clearTree("tables.table(0).fields.field(3)");
        prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(4, ((Collection<?>) prop).size());

        config.clearTree("tables.table(0).fields");
        assertNull(config.getProperty("tables.table(0).fields.field.name"));
        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());

        config.clearTree("tables.table(1)");
        assertNull(config.getProperty("tables.table.fields.field.name"));
    }

    /**
     * Tests removing more complex node structures.
     */
    @Test
    public void testClearTreeComplex()
    {
        final int count = 5;
        // create the structure
        for (int idx = 0; idx < count; idx++)
        {
            config.addProperty("indexList.index(-1)[@default]", Boolean.FALSE);
            config.addProperty("indexList.index[@name]", "test" + idx);
            config.addProperty("indexList.index.dir", "testDir" + idx);
        }
        assertEquals("Wrong number of nodes", count - 1, config
                .getMaxIndex("indexList.index[@name]"));

        // Remove a sub tree
        boolean found = false;
        for (int idx = 0; true; idx++)
        {
            String name = config.getString("indexList.index(" + idx
                    + ")[@name]");
            if (name == null)
            {
                break;
            }
            if ("test3".equals(name))
            {
                assertEquals("Wrong dir", "testDir3", config
                        .getString("indexList.index(" + idx + ").dir"));
                config.clearTree("indexList.index(" + idx + ")");
                found = true;
            }
        }
        assertTrue("Key to remove not found", found);
        assertEquals("Wrong number of nodes after remove", count - 2, config
                .getMaxIndex("indexList.index[@name]"));
        assertEquals("Wrong number of dir nodes after remove", count - 2,
                config.getMaxIndex("indexList.index.dir"));

        // Verify
        for (int idx = 0; true; idx++)
        {
            String name = config.getString("indexList.index(" + idx
                    + ")[@name]");
            if (name == null)
            {
                break;
            }
            if ("test3".equals(name))
            {
                fail("Key was not removed!");
            }
        }
    }

    /**
     * Tests the clearTree() method on a hierarchical structure of nodes. This
     * is a test case for CONFIGURATION-293.
     */
    @Test
    public void testClearTreeHierarchy()
    {
        config.addProperty("a.b.c", "c");
        config.addProperty("a.b.c.d", "d");
        config.addProperty("a.b.c.d.e", "e");
        config.clearTree("a.b.c");
        assertFalse("Property not removed", config.containsKey("a.b.c"));
        assertFalse("Sub property not removed", config.containsKey("a.b.c.d"));
    }

    @Test
    public void testContainsKey()
    {
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
    public void testGetKeys()
    {
        List<String> keys = new ArrayList<String>();
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next());
        }

        assertEquals(2, keys.size());
        assertTrue(keys.contains("tables.table.name"));
        assertTrue(keys.contains("tables.table.fields.field.name"));

        // test the order of the keys returned
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key2", "value2");
        config.addProperty("order.key3", "value3");

        Iterator<String> it = config.getKeys("order");
        assertEquals("1st key", "order.key1", it.next());
        assertEquals("2nd key", "order.key2", it.next());
        assertEquals("3rd key", "order.key3", it.next());
    }

    @Test
    public void testGetKeysString()
    {
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

        checkKeys("tables.table(1)", new String[] { "name", "fields.field.name" });
        checkKeys("tables.table(0)",
                new String[] { "name", "fields.field.name", "tables.table(0)[@type]", "size", "fields.field.type", "fields.field.size" });
        checkKeys("connections.connection(0).param",
                new String[] {"url", "user", "pwd" });
        checkKeys("connections.connection(1).param",
                new String[] {"url", "user" });
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key.
     */
    @Test
    public void testGetKeysWithKeyAsPrefix()
    {
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key2", "value2");
        Iterator<String> it = config.getKeys("order.key1");
        assertTrue("no key found", it.hasNext());
        assertEquals("1st key", "order.key1", it.next());
        assertFalse("more keys than expected", it.hasNext());
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key, and
     * there are multiple keys starting with this prefix.
     */
    @Test
    public void testGetKeysWithKeyAsPrefixMultiple()
    {
        config.addProperty("order.key1", "value1");
        config.addProperty("order.key1.test", "value2");
        config.addProperty("order.key1.test.complex", "value2");
        Iterator<String> it = config.getKeys("order.key1");
        assertEquals("Wrong key 1", "order.key1", it.next());
        assertEquals("Wrong key 2", "order.key1.test", it.next());
        assertEquals("Wrong key 3", "order.key1.test.complex", it.next());
        assertFalse("More keys than expected", it.hasNext());
    }

    @Test
    public void testAddProperty()
    {
        config.addProperty("tables.table(0).fields.field(-1).name", "phone");
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(6, ((Collection<?>) prop).size());

        config.addProperty("tables.table(0).fields.field.name", "fax");
        prop = config.getProperty("tables.table.fields.field(5).name");
        assertNotNull(prop);
        assertTrue(prop instanceof List);
        List<?> list = (List<?>) prop;
        assertEquals("phone", list.get(0));
        assertEquals("fax", list.get(1));

        config.addProperty("tables.table(-1).name", "config");
        prop = config.getProperty("tables.table.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection<?>) prop).size());
        config.addProperty("tables.table(2).fields.field(0).name", "cid");
        config.addProperty("tables.table(2).fields.field(-1).name",
        "confName");
        prop = config.getProperty("tables.table(2).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection<?>) prop).size());
        assertEquals("confName",
        config.getProperty("tables.table(2).fields.field(1).name"));

        config.addProperty("connection.user", "scott");
        config.addProperty("connection.passwd", "tiger");
        assertEquals("tiger", config.getProperty("connection.passwd"));

        DefaultConfigurationKey key = createConfigurationKey();
        key.append("tables").append("table").appendIndex(0);
        key.appendAttribute("tableType");
        config.addProperty(key.toString(), "system");
        assertEquals("system", config.getProperty(key.toString()));
    }

    /**
     * Creates a {@code DefaultConfigurationKey} object.
     *
     * @return the new key object
     */
    private static DefaultConfigurationKey createConfigurationKey()
    {
        return new DefaultConfigurationKey(DefaultExpressionEngine.INSTANCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyInvalidKey()
    {
        config.addProperty(".", "InvalidKey");
    }

    @Test
    public void testGetMaxIndex()
    {
        assertEquals(4, config.getMaxIndex("tables.table(0).fields.field"));
        assertEquals(4, config.getMaxIndex("tables.table(1).fields.field"));
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals(1, config.getMaxIndex("tables.table.name"));
        assertEquals(0, config.getMaxIndex("tables.table(0).name"));
        assertEquals(0, config.getMaxIndex("tables.table(1).fields.field(1)"));
        assertEquals(-1, config.getMaxIndex("tables.table(2).fields"));

        int maxIdx = config.getMaxIndex("tables.table(0).fields.field.name");
        for(int i = 0; i <= maxIdx; i++)
        {
            DefaultConfigurationKey key =
                    new DefaultConfigurationKey(DefaultExpressionEngine.INSTANCE,
                            "tables.table(0).fields");
            key.append("field").appendIndex(i).append("name");
            assertNotNull(config.getProperty(key.toString()));
        }
    }

    @Test
    public void testSubset()
    {
        // test the subset on the first table
        Configuration subset = config.subset("tables.table(0)");
        assertEquals(tables[0], subset.getProperty("name"));

        Object prop = subset.getProperty("fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());

        for (int i = 0; i < fields[0].length; i++)
        {
            DefaultConfigurationKey key = createConfigurationKey();
            key.append("fields").append("field").appendIndex(i);
            key.append("name");
            assertEquals(fields[0][i], subset.getProperty(key.toString()));
        }

        // test the subset on the second table
        assertTrue("subset is not empty", config.subset("tables.table(2)").isEmpty());

        // test the subset on the fields
        subset = config.subset("tables.table.fields.field");
        prop = subset.getProperty("name");
        assertTrue("prop is not a collection", prop instanceof Collection);
        assertEquals(10, ((Collection<?>) prop).size());

        assertEquals(fields[0][0], subset.getProperty("name(0)"));

        // test the subset on the field names
        subset = config.subset("tables.table.fields.field.name");
        assertTrue("subset is not empty", subset.isEmpty());
    }

    /**
     * Tests the subset() method when the specified node has a value. This value
     * must be available in the subset, too. Related to CONFIGURATION-295.
     */
    @Test
    public void testSubsetNodeWithValue()
    {
        config.setProperty("tables.table(0).fields", "My fields");
        Configuration subset = config.subset("tables.table(0).fields");
        assertEquals("Wrong field name", fields[0][0], subset
                .getString("field(0).name"));
        assertEquals("Wrong value of root", "My fields", subset.getString(""));
    }

    /**
     * Tests the subset() method when the specified key selects multiple keys.
     * The resulting root node should have a value only if exactly one of the
     * selected nodes has a value. Related to CONFIGURATION-295.
     */
    @Test
    public void testSubsetMultipleNodesWithValues()
    {
        config.setProperty("tables.table(0).fields", "My fields");
        Configuration subset = config.subset("tables.table.fields");
        assertEquals("Wrong value of root", "My fields", subset.getString(""));
        config.setProperty("tables.table(1).fields", "My other fields");
        subset = config.subset("tables.table.fields");
        assertNull("Root value is not null though there are multiple values",
                subset.getString(""));
    }

    /**
     * Tests the configurationAt() method to obtain a configuration for a sub
     * tree.
     */
    @Test
    public void testConfigurationAt()
    {
        BaseHierarchicalConfiguration subConfig = config
                .configurationAt("tables.table(1)");
        assertEquals("Wrong table name", tables[1], subConfig.getString("name"));
        List<Object> lstFlds = subConfig.getList("fields.field.name");
        assertEquals("Wrong number of fields", fields[1].length, lstFlds.size());
        for (int i = 0; i < fields[1].length; i++)
        {
            assertEquals("Wrong field at position " + i, fields[1][i], lstFlds
                    .get(i));
        }

        subConfig.setProperty("name", "testTable");
        assertEquals("Change not visible in parent", "testTable", config
                .getString("tables.table(1).name"));
        config.setProperty("tables.table(1).fields.field(2).name", "testField");
        assertEquals("Change not visible in sub config", "testField", subConfig
                .getString("fields.field(2).name"));
    }

    /**
     * Tests whether an immutable configuration for a sub tree can be obtained.
     */
    @Test
    public void testImmutableConfigurationAt()
    {
        ImmutableHierarchicalConfiguration subConfig =
                config.immutableConfigurationAt("tables.table(1)");
        assertEquals("Wrong table name", tables[1], subConfig.getString("name"));
        List<Object> lstFlds = subConfig.getList("fields.field.name");
        assertEquals("Wrong number of fields", fields[1].length, lstFlds.size());
        for (int i = 0; i < fields[1].length; i++)
        {
            assertEquals("Wrong field at position " + i, fields[1][i],
                    lstFlds.get(i));
        }
    }

    /**
     * Tests whether the support updates flag is taken into account when
     * creating an immutable sub configuration.
     */
    @Test
    public void testImmutableConfigurationAtSupportUpdates()
    {
        String newTableName = tables[1] + "_other";
        ImmutableHierarchicalConfiguration subConfig =
                config.immutableConfigurationAt("tables.table(1)", true);
        config.addProperty("tables.table(-1).name", newTableName);
        config.clearTree("tables.table(1)");
        assertEquals("Name not updated", newTableName,
                subConfig.getString("name"));
    }

    /**
     * Tests the configurationAt() method when the passed in key does not exist.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConfigurationAtUnknownSubTree()
    {
        config.configurationAt("non.existing.key");
    }

    /**
     * Tests the configurationAt() method when the passed in key selects
     * multiple nodes. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConfigurationAtMultipleNodes()
    {
        config.configurationAt("tables.table.name");
    }

    /**
     * Tests whether a sub configuration obtained by configurationAt() can be
     * cleared.
     */
    @Test
    public void testConfigurationAtClear()
    {
        config.addProperty("test.sub.test", "fail");
        assertEquals("Wrong index (1)", 0, config.getMaxIndex("test"));
        SubnodeConfiguration sub = config.configurationAt("test.sub");
        assertEquals("Wrong value", "fail", sub.getString("test"));
        sub.clear();
        assertNull("Key still found", config.getString("test.sub.test"));
        sub.setProperty("test", "success");
        assertEquals("Property not set", "success",
                config.getString("test.sub.test"));
        assertEquals("Wrong index (2)", 0, config.getMaxIndex("test"));
    }

    /**
     * Tests whether a {@code SubnodeConfiguration} can be cleared and its root
     * node can be removed from its parent configuration.
     */
    @Test
    public void testConfigurationAtClearAndDetach()
    {
        config.addProperty("test.sub.test", "success");
        config.addProperty("test.other", "check");
        SubnodeConfiguration sub = config.configurationAt("test.sub");
        sub.clearAndDetachFromParent();
        assertTrue("Sub not empty", sub.isEmpty());
        assertNull("Key still found", config.getString("test.sub.test"));
        sub.setProperty("test", "failure!");
        assertNull("Node not detached", config.getString("test.sub.test"));
    }

    /**
     * Helper method for checking a list of sub configurations pointing to the
     * single fields of the table configuration.
     *
     * @param lstFlds the list with sub configurations
     */
    private void checkSubConfigurations(
            List<? extends ImmutableConfiguration> lstFlds)
    {
        assertEquals("Wrong size of fields", fields[1].length, lstFlds.size());
        for (int i = 0; i < fields[1].length; i++)
        {
            ImmutableConfiguration sub = lstFlds.get(i);
            assertEquals("Wrong field at position " + i, fields[1][i],
                    sub.getString("name"));
        }
    }

    /**
     * Tests the configurationsAt() method.
     */
    @Test
    public void testConfigurationsAt()
    {
        List<SubnodeConfiguration> lstFlds =
                config.configurationsAt("tables.table(1).fields.field");
        checkSubConfigurations(lstFlds);
    }

    /**
     * Tests whether a list of immutable sub configurations can be queried.
     */
    @Test
    public void testImmutableConfigurationsAt()
    {
        List<ImmutableHierarchicalConfiguration> lstFlds =
                config.immutableConfigurationsAt("tables.table(1).fields.field");
        checkSubConfigurations(lstFlds);
    }

    /**
     * Tests the configurationsAt() method when the passed in key does not
     * select any sub nodes.
     */
    @Test
    public void testConfigurationsAtEmpty()
    {
        assertTrue("List is not empty", config.configurationsAt("unknown.key")
                .isEmpty());
    }

    @Test
    public void testClone()
    {
        Configuration copy = (Configuration) config.clone();
        assertTrue(copy instanceof BaseHierarchicalConfiguration);
        checkContent(copy);
    }

    /**
     * Tests whether registered event handlers are handled correctly when a
     * configuration is cloned. They should not be registered at the clone.
     */
    @Test
    public void testCloneWithEventListeners()
    {
        ConfigurationListener l = new ConfigurationListener()
        {
            @Override
            public void configurationChanged(ConfigurationEvent event)
            {
                // just a dummy
            }
        };
        config.addConfigurationListener(l);
        BaseHierarchicalConfiguration copy = (BaseHierarchicalConfiguration) config
                .clone();
        assertFalse("Event listener registered at clone", copy
                .getConfigurationListeners().contains(l));
    }

    /**
     * Tests whether interpolation works as expected after cloning.
     */
    @Test
    public void testCloneInterpolation()
    {
        final String keyAnswer = "answer";
        final String keyValue = "value";
        config.addProperty(keyAnswer, "The answer is ${" + keyValue + "}.");
        config.addProperty(keyValue, 42);
        BaseHierarchicalConfiguration clone =
                (BaseHierarchicalConfiguration) config.clone();
        clone.setProperty(keyValue, 43);
        assertEquals("Wrong interpolation in original", "The answer is 42.",
                config.getString(keyAnswer));
        assertEquals("Wrong interpolation in clone", "The answer is 43.",
                clone.getString(keyAnswer));
    }

    @Test
    public void testAddNodes()
    {
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
        nodes.add(createFieldNode("birthDate"));
        nodes.add(createFieldNode("lastLogin"));
        nodes.add(createFieldNode("language"));
        config.addNodes("tables.table(0).fields", nodes);
        assertEquals(7, config.getMaxIndex("tables.table(0).fields.field"));
        assertEquals("birthDate", config.getString("tables.table(0).fields.field(5).name"));
        assertEquals("lastLogin", config.getString("tables.table(0).fields.field(6).name"));
        assertEquals("language", config.getString("tables.table(0).fields.field(7).name"));
    }

    /**
     * Tests the addNodes() method when the provided key does not exist. In
     * this case, a new node (or even a complete new branch) will be created.
     */
    @Test
    public void testAddNodesForNonExistingKey()
    {
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
        nodes.add(createNode("usr", "scott"));
        ConfigurationNode nd = createNode("pwd", "tiger");
        nd.setAttribute(true);
        nodes.add(nd);
        config.addNodes("database.connection.settings", nodes);

        assertEquals("Usr node not found", "scott", config.getString("database.connection.settings.usr"));
        assertEquals("Pwd node not found", "tiger", config.getString("database.connection.settings[@pwd]"));
    }

    /**
     * Tests the addNodes() method when the new nodes should be added to an
     * attribute node. This is not allowed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNodesWithAttributeKey()
    {
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
        nodes.add(createNode("testNode", "yes"));
        config.addNodes("database.connection[@settings]", nodes);
    }

    /**
     * Tests copying nodes from one configuration to another one.
     */
    @Test
    public void testAddNodesCopy()
    {
        BaseHierarchicalConfiguration configDest = new BaseHierarchicalConfiguration();
        configDest.addProperty("test", "TEST");
        Collection<ConfigurationNode> nodes = config.getRootNode().getChildren();
        assertEquals("Wrong number of children", 1, nodes.size());
        configDest.addNodes("newNodes", nodes);
        for (int i = 0; i < tables.length; i++)
        {
            String keyTab = "newNodes.tables.table(" + i + ").";
            assertEquals("Table " + i + " not found", tables[i], configDest
                    .getString(keyTab + "name"));
            for (int j = 0; j < fields[i].length; j++)
            {
                assertEquals("Invalid field " + j + " in table " + i,
                        fields[i][j], configDest.getString(keyTab
                                + "fields.field(" + j + ").name"));
            }
        }
    }

    /**
     * Tests adding an attribute node with the addNodes() method.
     */
    @Test
    public void testAddNodesAttributeNode()
    {
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
        ConfigurationNode nd = createNode("length", "10");
        nd.setAttribute(true);
        nodes.add(nd);
        config.addNodes("tables.table(0).fields.field(1)", nodes);
        assertEquals("Attribute was not added", "10", config
                .getString("tables.table(0).fields.field(1)[@length]"));
    }

    /**
     * Tests setting a custom expression engine, which uses a slightly different
     * syntax.
     */
    @Test
    public void testSetExpressionEngine()
    {
        config.setExpressionEngine(null);
        assertNotNull("Expression engine is null", config.getExpressionEngine());
        assertSame("Default engine is not used",
                DefaultExpressionEngine.INSTANCE, config.getExpressionEngine());

        config.setExpressionEngine(createAlternativeExpressionEngine());
        checkAlternativeSyntax();
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testInitCopy()
    {
        BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration(config);
        checkContent(copy);
    }

    /**
     * Tests whether the nodes of a copied configuration are independent from
     * the source configuration.
     */
    @Test
    public void testInitCopyUpdate()
    {
        BaseHierarchicalConfiguration copy = new BaseHierarchicalConfiguration(config);
        config.setProperty("tables.table(0).name", "NewTable");
        checkContent(copy);
    }

    /**
     * Tests interpolation facilities.
     */
    @Test
    public void testInterpolation()
    {
        config.addProperty("base.dir", "/home/foo");
        config.addProperty("test.absolute.dir.dir1", "${base.dir}/path1");
        config.addProperty("test.absolute.dir.dir2", "${base.dir}/path2");
        config.addProperty("test.absolute.dir.dir3", "${base.dir}/path3");

        Configuration sub = config.subset("test.absolute.dir");
        for (int i = 1; i < 4; i++)
        {
            assertEquals("Wrong interpolation in parent", "/home/foo/path" + i,
                    config.getString("test.absolute.dir.dir" + i));
            assertEquals("Wrong interpolation in subnode",
                    "/home/foo/path" + i, sub.getString("dir" + i));
        }
    }

    /**
     * Basic interpolation tests.
     */
    @Test
    public void testInterpolationBasic()
    {
        InterpolationTestHelper.testInterpolation(config);
    }

    /**
     * Tests multiple levels of interpolation.
     */
    @Test
    public void testInterpolationMultipleLevels()
    {
        InterpolationTestHelper.testMultipleInterpolation(config);
    }

    /**
     * Tests an invalid interpolation that causes an endless loop.
     */
    @Test
    public void testInterpolationLoop()
    {
        InterpolationTestHelper.testInterpolationLoop(config);
    }

    /**
     * Tests interpolation with a subset.
     */
    @Test
    public void testInterpolationSubset()
    {
        InterpolationTestHelper.testInterpolationSubset(config);
    }

    /**
     * Tests whether interpolation with a subset configuration works over
     * multiple layers.
     */
    @Test
    public void testInterpolationSubsetMultipleLayers()
    {
        config.clear();
        config.addProperty("var", "value");
        config.addProperty("prop2.prop[@attr]", "${var}");
        Configuration sub1 = config.subset("prop2");
        Configuration sub2 = sub1.subset("prop");
        assertEquals("Wrong value", "value", sub2.getString("[@attr]"));
    }

    /**
     * Tests interpolation of a variable, which cannot be resolved.
     */
    @Test
    public void testInterpolationUnknownProperty()
    {
        InterpolationTestHelper.testInterpolationUnknownProperty(config);
    }

    /**
     * Tests interpolation with system properties.
     */
    @Test
    public void testInterpolationSysProperties()
    {
        InterpolationTestHelper.testInterpolationSystemProperties(config);
    }

    /**
     * Tests interpolation with constant values.
     */
    @Test
    public void testInterpolationConstants()
    {
        InterpolationTestHelper.testInterpolationConstants(config);
    }

    /**
     * Tests escaping variables.
     */
    @Test
    public void testInterpolationEscaped()
    {
        InterpolationTestHelper.testInterpolationEscaped(config);
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator()
    {
        InterpolationTestHelper.testGetInterpolator(config);
    }

    /**
     * Tests obtaining a configuration with all variables substituted.
     */
    @Test
    public void testInterpolatedConfiguration()
    {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        BaseHierarchicalConfiguration c = (BaseHierarchicalConfiguration) InterpolationTestHelper
                .testInterpolatedConfiguration(config);

        // tests whether the hierarchical structure has been maintained
        config = c;
        testGetProperty();
    }

    /**
     * Tests the copy constructor when a null reference is passed.
     */
    @Test
    public void testInitCopyNull()
    {
        BaseHierarchicalConfiguration copy =
                new BaseHierarchicalConfiguration(
                        (HierarchicalConfiguration) null);
        assertTrue("Configuration not empty", copy.isEmpty());
    }

    /**
     * Tests the parents of nodes when setRootNode() is involved. This is
     * related to CONFIGURATION-334.
     */
    @Test
    public void testNodeParentsAfterSetRootNode()
    {
        DefaultConfigurationNode root = new DefaultConfigurationNode();
        DefaultConfigurationNode child1 = new DefaultConfigurationNode(
                "child1", "test1");
        root.addChild(child1);
        config.setRootNode(root);
        config.addProperty("child2", "test2");
        List<ConfigurationNode> nodes = config.getExpressionEngine().query(config.getRootNode(),
                "child2");
        assertEquals("Wrong number of result nodes", 1, nodes.size());
        ConfigurationNode child2 = nodes.get(0);
        assertEquals("Different parent nodes", child1.getParentNode(), child2
                .getParentNode());
    }

    /**
     * Tests calling getRoot() after a root node was set using setRootNode() and
     * further child nodes have been added. The newly add child nodes should be
     * present in the root node returned.
     */
    @Test
    public void testGetRootAfterSetRootNode()
    {
        DefaultConfigurationNode root = new DefaultConfigurationNode();
        DefaultConfigurationNode child1 = new DefaultConfigurationNode(
                "child1", "test1");
        root.addChild(child1);
        config.setRootNode(root);
        config.addProperty("child2", "test2");
        ConfigurationNode oldRoot = config.getRootNode();
        assertEquals("Wrong number of children", 2, oldRoot.getChildrenCount());
    }

    /**
     * Tests whether keys that contains brackets can be used.
     */
    @Test
    public void testGetPropertyKeyWithBrackets()
    {
        final String key = "test.directory.platform(x86)";
        config.addProperty(key, "C:\\Temp");
        assertEquals("Wrong property value", "C:\\Temp", config.getString(key));
    }

    /**
     * Tests whether immutable configurations for the children of a given node
     * can be queried.
     */
    @Test
    public void testImmutableChildConfigurationsAt()
    {
        List<ImmutableHierarchicalConfiguration> children =
                config.immutableChildConfigurationsAt("tables.table(0)");
        assertEquals("Wrong number of elements", 2, children.size());
        ImmutableHierarchicalConfiguration c1 = children.get(0);
        assertEquals("Wrong name (1)", "name", c1.getRootElementName());
        assertEquals("Wrong table name", tables[0], c1.getString(null));
        ImmutableHierarchicalConfiguration c2 = children.get(1);
        assertEquals("Wrong name (2)", "fields", c2.getRootElementName());
        assertEquals("Wrong field name", fields[0][0],
                c2.getString("field(0).name"));
    }

    /**
     * Tests whether sub configurations for the children of a given node can be
     * queried.
     */
    @Test
    public void testChildConfigurationsAt()
    {
        List<SubnodeConfiguration> children =
                config.childConfigurationsAt("tables.table(0)");
        assertEquals("Wrong number of elements", 2, children.size());
        SubnodeConfiguration sub = children.get(0);
        String newTabName = "otherTabe";
        sub.setProperty(null, newTabName);
        assertEquals("Table name not changed", newTabName,
                config.getString("tables.table(0).name"));
    }

    /**
     * Tests the result of childConfigurationsAt() if the key selects multiple
     * nodes.
     */
    @Test
    public void testChildConfigurationsAtNoUniqueKey()
    {
        assertTrue("Got children", config.childConfigurationsAt("tables.table")
                .isEmpty());
    }

    /**
     * Tests the result of childConfigurationsAt() if the key does not point to
     * an existing node.
     */
    @Test
    public void testChildConfigurationsAtNotFound()
    {
        assertTrue("Got children",
                config.childConfigurationsAt("not.existing.key").isEmpty());
    }

    /**
     * Tests the initialize() method. We can only test that a new configuration
     * listener was added.
     */
    @Test
    public void testInitialize()
    {
        Collection<ConfigurationListener> listeners =
                config.getConfigurationListeners();
        config.initialize();
        assertEquals("No new listener added", listeners.size() + 1, config
                .getConfigurationListeners().size());
    }

    /**
     * Tests that calling initialize() multiple times does not initialize
     * internal structures more than once.
     */
    @Test
    public void testInitializeTwice()
    {
        Collection<ConfigurationListener> listeners =
                config.getConfigurationListeners();
        config.initialize();
        config.initialize();
        assertEquals("Too many listener added", listeners.size() + 1, config
                .getConfigurationListeners().size());
    }

    /**
     * Helper method for testing the getKeys(String) method.
     *
     * @param prefix the key to pass into getKeys()
     * @param expected the expected result
     */
    private void checkKeys(String prefix, String[] expected)
    {
        Set<String> values = new HashSet<String>();
        for (String element : expected) {
            values.add((element.startsWith(prefix)) ? element :  prefix + "." + element);
        }

        Iterator<String> itKeys = config.getKeys(prefix);
        while(itKeys.hasNext())
        {
            String key = itKeys.next();
            if(!values.contains(key))
            {
                fail("Found unexpected key: " + key);
            }
            else
            {
                values.remove(key);
            }
        }

        assertTrue("Remaining keys " + values, values.isEmpty());
    }

    /**
     * Helper method for checking keys using an alternative syntax.
     */
    private void checkAlternativeSyntax()
    {
        assertNull(config.getProperty("tables/table/resultset"));
        assertNull(config.getProperty("tables/table/fields/field"));

        Object prop = config.getProperty("tables/table[0]/fields/field/name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());

        prop = config.getProperty("tables/table/fields/field/name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection<?>) prop).size());

        prop = config.getProperty("tables/table/fields/field[3]/name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection<?>) prop).size());

        prop = config.getProperty("tables/table[1]/fields/field[2]/name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());

        Set<String> keys = new HashSet<String>();
        CollectionUtils.addAll(keys, config.getKeys());
        assertEquals("Wrong number of defined keys", 2, keys.size());
        assertTrue("Key not found", keys.contains("tables/table/name"));
        assertTrue("Key not found", keys
                .contains("tables/table/fields/field/name"));
    }

    /**
     * Checks the content of the passed in configuration object. Used by some
     * tests that copy a configuration.
     *
     * @param c the configuration to check
     */
    private void checkContent(Configuration c)
    {
        for (int i = 0; i < tables.length; i++)
        {
            assertEquals(tables[i], c.getString("tables.table(" + i + ").name"));
            for (int j = 0; j < fields[i].length; j++)
            {
                assertEquals(fields[i][j], c.getString("tables.table(" + i
                        + ").fields.field(" + j + ").name"));
            }
        }
    }

    private ExpressionEngine createAlternativeExpressionEngine()
    {
        DefaultExpressionEngine engine =
                new DefaultExpressionEngine(
                        new DefaultExpressionEngineSymbols.Builder(
                                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
                                .setPropertyDelimiter("/").setIndexStart("[")
                                .setIndexEnd("]").create());
        return engine;
    }

    /**
     * Helper method for creating a field node with its children.
     *
     * @param name the name of the field
     * @return the field node
     */
    private static ConfigurationNode createFieldNode(String name)
    {
        ConfigurationNode fld = createNode("field", null);
        fld.addChild(createNode("name", name));
        return fld;
    }

    /**
     * Helper method for creating a configuration node.
     * @param name the node's name
     * @param value the node's value
     * @return the new node
     */
    private static ConfigurationNode createNode(String name, Object value)
    {
        ConfigurationNode node = new DefaultConfigurationNode(name);
        node.setValue(value);
        return node;
    }
}
