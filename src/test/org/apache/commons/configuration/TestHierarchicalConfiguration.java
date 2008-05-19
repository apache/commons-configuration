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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalConfiguration.
 *
 * @version $Id$
 */
public class TestHierarchicalConfiguration extends TestCase
{
    private static String[] tables = { "users", "documents" };

    private static String[][] fields =
    {
        { "uid", "uname", "firstName", "lastName", "email" },
        { "docid", "name", "creationDate", "authorID", "version" }
    };

    private HierarchicalConfiguration config;

    protected void setUp() throws Exception
    {
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
         */
        config = new HierarchicalConfiguration();
        HierarchicalConfiguration.Node nodeTables = createNode("tables", null);
        for(int i = 0; i < tables.length; i++)
        {
            HierarchicalConfiguration.Node nodeTable = createNode("table", null);
            nodeTables.addChild(nodeTable);
            HierarchicalConfiguration.Node nodeName = createNode("name", tables[i]);
            nodeTable.addChild(nodeName);
            HierarchicalConfiguration.Node nodeFields = createNode("fields", null);
            nodeTable.addChild(nodeFields);

            for (int j = 0; j < fields[i].length; j++)
            {
                nodeFields.addChild(createFieldNode(fields[i][j]));
            }
        }

        config.getRoot().addChild(nodeTables);
    }

    public void testSetRoot()
    {
        try
        {
            config.setRoot(null);
            fail("Could set null root node!");
        }
        catch(IllegalArgumentException iex)
        {
            //ok
        }

        config.setRoot(new HierarchicalConfiguration.Node("test"));
        assertTrue(config.isEmpty());
    }

    public void testSetRootNode()
    {
        config.setRootNode(new DefaultConfigurationNode("testNode"));
        assertNotSame("Same root node", config.getRootNode(), config.getRoot());
        assertEquals("Wrong name of root node", "testNode", config.getRoot().getName());

        config.setRootNode(new HierarchicalConfiguration.Node("test"));
        assertSame("Wrong root node", config.getRootNode(), config.getRoot());
    }

    public void testSetRootNodeNull()
    {
        try
        {
            config.setRootNode(null);
            fail("Could set null root node!");
        }
        catch(IllegalArgumentException iex)
        {
            //ok
        }
    }

    public void testIsEmpty()
    {
        assertFalse(config.isEmpty());
        HierarchicalConfiguration conf2 = new HierarchicalConfiguration();
        assertTrue(conf2.isEmpty());
        HierarchicalConfiguration.Node child1 = new HierarchicalConfiguration.Node("child1");
        HierarchicalConfiguration.Node child2 = new HierarchicalConfiguration.Node("child2");
        child1.addChild(child2);
        conf2.getRoot().addChild(child1);
        assertTrue(conf2.isEmpty());
    }

    public void testGetProperty()
    {
        assertNull(config.getProperty("tables.table.resultset"));
        assertNull(config.getProperty("tables.table.fields.field"));

        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());

        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());

        prop = config.getProperty("tables.table.fields.field(3).name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection) prop).size());

        prop = config.getProperty("tables.table(1).fields.field(2).name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());
    }

    public void testSetProperty()
    {
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

    public void testClearTree()
    {
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        config.clearTree("tables.table(0).fields.field(3)");
        prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(4, ((Collection) prop).size());

        config.clearTree("tables.table(0).fields");
        assertNull(config.getProperty("tables.table(0).fields.field.name"));
        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());

        config.clearTree("tables.table(1)");
        assertNull(config.getProperty("tables.table.fields.field.name"));
    }

    /**
     * Tests removing more complex node structures.
     */
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
    public void testClearTreeHierarchy()
    {
        config.addProperty("a.b.c", "c");
        config.addProperty("a.b.c.d", "d");
        config.addProperty("a.b.c.d.e", "e");
        config.clearTree("a.b.c");
        assertFalse("Property not removed", config.containsKey("a.b.c"));
        assertFalse("Sub property not removed", config.containsKey("a.b.c.d"));
    }

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

    public void testGetKeys()
    {
        List keys = new ArrayList();
        for (Iterator it = config.getKeys(); it.hasNext();)
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

        Iterator it = config.getKeys("order");
        assertEquals("1st key", "order.key1", it.next());
        assertEquals("2nd key", "order.key2", it.next());
        assertEquals("3rd key", "order.key3", it.next());
    }

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

    public void testAddProperty()
    {
        config.addProperty("tables.table(0).fields.field(-1).name", "phone");
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(6, ((Collection) prop).size());

        config.addProperty("tables.table(0).fields.field.name", "fax");
        prop = config.getProperty("tables.table.fields.field(5).name");
        assertNotNull(prop);
        assertTrue(prop instanceof List);
        List list = (List) prop;
        assertEquals("phone", list.get(0));
        assertEquals("fax", list.get(1));

        config.addProperty("tables.table(-1).name", "config");
        prop = config.getProperty("tables.table.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection) prop).size());
        config.addProperty("tables.table(2).fields.field(0).name", "cid");
        config.addProperty("tables.table(2).fields.field(-1).name",
        "confName");
        prop = config.getProperty("tables.table(2).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection) prop).size());
        assertEquals("confName",
        config.getProperty("tables.table(2).fields.field(1).name"));

        config.addProperty("connection.user", "scott");
        config.addProperty("connection.passwd", "tiger");
        assertEquals("tiger", config.getProperty("connection.passwd"));

        ConfigurationKey key = new ConfigurationKey();
        key.append("tables").append("table").appendIndex(0);
        key.appendAttribute("tableType");
        config.addProperty(key.toString(), "system");
        assertEquals("system", config.getProperty(key.toString()));

        try
        {
            config.addProperty(".", "InvalidKey");
            fail("Could add invalid key!");
        }
        catch(IllegalArgumentException iex)
        {
            //ok
        }
    }

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
            ConfigurationKey key = new ConfigurationKey("tables.table(0).fields");
            key.append("field").appendIndex(i).append("name");
            assertNotNull(config.getProperty(key.toString()));
        }
    }

    public void testSubset()
    {
        // test the subset on the first table
        Configuration subset = config.subset("tables.table(0)");
        assertEquals(tables[0], subset.getProperty("name"));

        Object prop = subset.getProperty("fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());

        for (int i = 0; i < fields[0].length; i++)
        {
            ConfigurationKey key = new ConfigurationKey();
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
        assertEquals(10, ((Collection) prop).size());

        assertEquals(fields[0][0], subset.getProperty("name(0)"));

        // test the subset on the field names
        subset = config.subset("tables.table.fields.field.name");
        assertTrue("subset is not empty", subset.isEmpty());
    }

    /**
     * Tests the subset() method when the specified node has a value. This value
     * must be available in the subset, too. Related to CONFIGURATION-295.
     */
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
    public void testConfigurationAt()
    {
        HierarchicalConfiguration subConfig = config
                .configurationAt("tables.table(1)");
        assertEquals("Wrong table name", tables[1], subConfig.getString("name"));
        List lstFlds = subConfig.getList("fields.field.name");
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
     * Tests the configurationAt() method when the passed in key does not exist.
     */
    public void testConfigurationAtUnknownSubTree()
    {
        try
        {
            config.configurationAt("non.existing.key");
            fail("Could obtain sub config for unknown key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the configurationAt() method when the passed in key selects
     * multiple nodes. This should cause an exception.
     */
    public void testConfigurationAtMultipleNodes()
    {
        try
        {
            config.configurationAt("tables.table.name");
            fail("Could create sub config with non unique key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the configurationsAt() method.
     */
    public void testConfigurationsAt()
    {
        List lstFlds = config.configurationsAt("tables.table(1).fields.field");
        assertEquals("Wrong size of fields", fields[1].length, lstFlds.size());
        for (int i = 0; i < fields[1].length; i++)
        {
            HierarchicalConfiguration sub = (HierarchicalConfiguration) lstFlds
                    .get(i);
            assertEquals("Wrong field at position " + i, fields[1][i], sub
                    .getString("name"));
        }
    }

    /**
     * Tests the configurationsAt() method when the passed in key does not
     * select any sub nodes.
     */
    public void testConfigurationsAtEmpty()
    {
        assertTrue("List is not empty", config.configurationsAt("unknown.key")
                .isEmpty());
    }

    public void testClone()
    {
        Configuration copy = (Configuration) config.clone();
        assertTrue(copy instanceof HierarchicalConfiguration);
        checkContent(copy);
    }

    /**
     * Tests whether registered event handlers are handled correctly when a
     * configuration is cloned. They should not be registered at the clone.
     */
    public void testCloneWithEventListeners()
    {
        config.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                // just a dummy
            }
        });
        HierarchicalConfiguration copy = (HierarchicalConfiguration) config
                .clone();
        assertTrue("Event listener registered at clone", copy
                .getConfigurationListeners().isEmpty());
    }

    public void testAddNodes()
    {
        Collection nodes = new ArrayList();
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
    public void testAddNodesForNonExistingKey()
    {
        Collection nodes = new ArrayList();
        nodes.add(createNode("usr", "scott"));
        Node nd = createNode("pwd", "tiger");
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
    public void testAddNodesWithAttributeKey()
    {
        Collection nodes = new ArrayList();
        nodes.add(createNode("testNode", "yes"));
        try
        {
            config.addNodes("database.connection[@settings]", nodes);
            fail("Could add nodes to an attribute node!");
        }
        catch(IllegalArgumentException iex)
        {
            //ok
        }
    }

    /**
     * Tests copying nodes from one configuration to another one.
     */
    public void testAddNodesCopy()
    {
        HierarchicalConfiguration configDest = new HierarchicalConfiguration();
        configDest.addProperty("test", "TEST");
        Collection nodes = config.getRootNode().getChildren();
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
    public void testAddNodesAttributeNode()
    {
        Collection nodes = new ArrayList();
        ConfigurationNode nd = createNode("length", "10");
        nd.setAttribute(true);
        nodes.add(nd);
        config.addNodes("tables.table(0).fields.field(1)", nodes);
        assertEquals("Attribute was not added", "10", config
                .getString("tables.table(0).fields.field(1)[@length]"));
    }

    /**
     * Tests removing children from a configuration node.
     */
    public void testNodeRemove()
    {
        HierarchicalConfiguration.Node node = new HierarchicalConfiguration.Node(
                "parent", "test");
        assertFalse(node.hasChildren());
        node.removeChildren(); // should have no effect
        assertFalse(node.remove("child"));

        node.addChild(createNode("test", "test"));
        assertTrue(node.hasChildren());
        assertTrue(node.remove("test"));
        assertFalse(node.hasChildren());

        for (int i = 0; i < 10; i++)
        {
            node.addChild(createNode("child" + i, "test" + i));
        }
        assertTrue(node.hasChildren());
        assertFalse(node.remove("child"));
        assertTrue(node.remove("child2"));
        assertTrue(node.getChildren("child2").isEmpty());

        HierarchicalConfiguration.Node child = createNode("child0", "testChild");
        assertFalse(node.remove(child));
        node.addChild(child);
        assertTrue(node.remove(child));
        assertEquals(1, node.getChildren("child0").size());
        assertEquals("test0", ((HierarchicalConfiguration.Node) node
                .getChildren("child0").get(0)).getValue());

        assertTrue(node.remove("child0"));
        assertFalse(node.remove(child));

        node.removeChildren();
        assertTrue(node.getChildren().isEmpty());
        assertFalse(node.remove(child));
    }

    /**
     * Tests the visitor mechanism.
     */
    public void testNodeVisitor()
    {
        CountVisitor v = new CountVisitor();
        config.getRoot().visit(v, null);
        assertEquals(28, v.beforeCount);
        assertEquals(v.beforeCount, v.afterCount);
    }

    /**
     * Tests setting a custom expression engine, which uses a slightly different
     * syntax.
     */
    public void testSetExpressionEngine()
    {
        config.setExpressionEngine(null);
        assertNotNull("Expression engine is null", config.getExpressionEngine());
        assertSame("Default engine is not used", HierarchicalConfiguration
                .getDefaultExpressionEngine(), config.getExpressionEngine());

        config.setExpressionEngine(createAlternativeExpressionEngine());
        checkAlternativeSyntax();
    }

    /**
     * Tests setting the default expression engine. This should impact all
     * configuration instances that do not have their own engine.
     */
    public void testSetDefaultExpressionEngine()
    {
        ExpressionEngine engineOld = HierarchicalConfiguration.getDefaultExpressionEngine();
        HierarchicalConfiguration
                .setDefaultExpressionEngine(createAlternativeExpressionEngine());
        checkAlternativeSyntax();
        HierarchicalConfiguration.setDefaultExpressionEngine(engineOld);
    }

    /**
     * Tests setting the default expression engine to null. This should not be
     * allowed.
     */
    public void testSetDefaultExpressionEngineNull()
    {
        try
        {
            HierarchicalConfiguration.setDefaultExpressionEngine(null);
            fail("Could set default expression engine to null!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

	/**
     * Tests the copy constructor.
     */
	public void testInitCopy()
	{
		HierarchicalConfiguration copy = new HierarchicalConfiguration(config);
		checkContent(copy);
	}

	/**
     * Tests whether the nodes of a copied configuration are independent from
     * the source configuration.
     */
	public void testInitCopyUpdate()
	{
		HierarchicalConfiguration copy = new HierarchicalConfiguration(config);
		config.setProperty("tables.table(0).name", "NewTable");
		checkContent(copy);
	}

    /**
     * Tests interpolation facilities.
     */
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
    public void testInterpolationBasic()
    {
        InterpolationTestHelper.testInterpolation(config);
    }

    /**
     * Tests multiple levels of interpolation.
     */
    public void testInterpolationMultipleLevels()
    {
        InterpolationTestHelper.testMultipleInterpolation(config);
    }

    /**
     * Tests an invalid interpolation that causes an endless loop.
     */
    public void testInterpolationLoop()
    {
        InterpolationTestHelper.testInterpolationLoop(config);
    }

    /**
     * Tests interpolation with a subset.
     */
    public void testInterpolationSubset()
    {
        InterpolationTestHelper.testInterpolationSubset(config);
    }

    /**
     * Tests interpolation of a variable, which cannot be resolved.
     */
    public void testInterpolationUnknownProperty()
    {
        InterpolationTestHelper.testInterpolationUnknownProperty(config);
    }

    /**
     * Tests interpolation with system properties.
     */
    public void testInterpolationSysProperties()
    {
        InterpolationTestHelper.testInterpolationSystemProperties(config);
    }

    /**
     * Tests interpolation with constant values.
     */
    public void testInterpolationConstants()
    {
        InterpolationTestHelper.testInterpolationConstants(config);
    }

    /**
     * Tests escaping variables.
     */
    public void testInterpolationEscaped()
    {
        InterpolationTestHelper.testInterpolationEscaped(config);
    }

    /**
     * Tests manipulating the interpolator.
     */
    public void testInterpolator()
    {
        InterpolationTestHelper.testGetInterpolator(config);
    }

    /**
     * Tests obtaining a configuration with all variables substituted.
     */
    public void testInterpolatedConfiguration()
    {
        HierarchicalConfiguration c = (HierarchicalConfiguration) InterpolationTestHelper
                .testInterpolatedConfiguration(config);

        // tests whether the hierarchical structure has been maintained
        config = c;
        testGetProperty();
    }

	/**
     * Tests the copy constructor when a null reference is passed.
     */
	public void testInitCopyNull()
	{
		HierarchicalConfiguration copy = new HierarchicalConfiguration(null);
		assertTrue("Configuration not empty", copy.isEmpty());
	}

	/**
     * Helper method for testing the getKeys(String) method.
     *
     * @param prefix the key to pass into getKeys()
     * @param expected the expected result
     */
    private void checkKeys(String prefix, String[] expected)
    {
        Set values = new HashSet();
        for(int i = 0; i < expected.length; i++)
        {
            values.add((expected[i].startsWith(prefix)) ? expected[i] :  prefix + "." + expected[i]);
        }

        Iterator itKeys = config.getKeys(prefix);
        while(itKeys.hasNext())
        {
            String key = (String) itKeys.next();
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
        assertEquals(5, ((Collection) prop).size());

        prop = config.getProperty("tables/table/fields/field/name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());

        prop = config.getProperty("tables/table/fields/field[3]/name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection) prop).size());

        prop = config.getProperty("tables/table[1]/fields/field[2]/name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());

        Set keys = new HashSet();
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
        DefaultExpressionEngine engine = new DefaultExpressionEngine();
        engine.setPropertyDelimiter("/");
        engine.setIndexStart("[");
        engine.setIndexEnd("]");
        return engine;
    }

    /**
     * Helper method for creating a field node with its children.
     *
     * @param name the name of the field
     * @return the field node
     */
    private static HierarchicalConfiguration.Node createFieldNode(String name)
    {
        HierarchicalConfiguration.Node fld = createNode("field", null);
        fld.addChild(createNode("name", name));
        return fld;
    }

    /**
     * Helper method for creating a configuration node.
     * @param name the node's name
     * @param value the node's value
     * @return the new node
     */
    private static HierarchicalConfiguration.Node createNode(String name, Object value)
    {
        HierarchicalConfiguration.Node node = new HierarchicalConfiguration.Node(name);
        node.setValue(value);
        return node;
    }

    /**
     * A test visitor implementation for checking whether all visitor methods
     * are correctly called.
     */
    static class CountVisitor extends HierarchicalConfiguration.NodeVisitor
    {
        public int beforeCount;

        public int afterCount;

        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
            super.visitAfterChildren(node, key);
            afterCount++;
        }

        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            super.visitBeforeChildren(node, key);
            beforeCount++;
        }
    }
}
