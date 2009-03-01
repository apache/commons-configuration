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
package org.apache.commons.configuration2.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

/**
 * Test class for {@link InMemoryConfigurationSource}. This class also tests
 * functionality provided by the base class.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestInMemoryConfigurationSource extends TestCase
{
    /** An array with the names of the test TABLES. */
    private static final String[] TABLES = {
            "users", "documents"
    };

    /** An array with the names of the table FIELDS. */
    private static final String[][] FIELDS = {
            {
                    "uid", "uname", "firstName", "lastName", "email"
            }, {
                    "docid", "name", "creationDate", "authorID", "version"
            }
    };

    /** An array with flags whether the test TABLES are system TABLES. */
    private static final Boolean[] SYS_TABLES = {
            Boolean.TRUE, Boolean.FALSE
    };

    /** The source to be tested. */
    private InMemoryConfigurationSource source;

    /**
     * Initializes the configuration source with the following structure: tables
     * table name fields field name field name
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationNode nodeTables = createNode("tables", null);
        for (int i = 0; i < TABLES.length; i++)
        {
            ConfigurationNode nodeTable = createNode("table", null);
            nodeTables.addChild(nodeTable);
            ConfigurationNode nodeName = createNode("name", TABLES[i]);
            nodeTable.addChild(nodeName);
            ConfigurationNode attrType = createNode("sysTab", SYS_TABLES[i]);
            nodeTable.addAttribute(attrType);
            ConfigurationNode nodeFields = createNode("fields", null);
            nodeTable.addChild(nodeFields);

            for (int j = 0; j < FIELDS[i].length; j++)
            {
                nodeFields.addChild(createFieldNode(FIELDS[i][j]));
            }
        }

        source = new InMemoryConfigurationSource();
        source.getRootNode().addChild(nodeTables);
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
     *
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

    /**
     * Returns the number of nodes that are stored in the test source.
     * Optionally only the nodes with a value are counted.
     *
     * @param withValue if true, only the nodes with a value are taken into
     *        account
     * @return the number of nodes
     */
    private static int nodeCount(boolean withValue)
    {
        int tabNodes = 2; // each table has a name and system flag
        if (!withValue)
        {
            tabNodes += 2; // also table and fields node
        }
        int count = TABLES.length * tabNodes;

        int fieldNodes = 1; // the name of each field
        if (!withValue)
        {
            fieldNodes += 1; // also the field node itself
        }
        for (int i = 0; i < FIELDS.length; i++)
        {
            count += FIELDS[i].length * fieldNodes; // the number of fields
        }
        return count;
    }

    /**
     * Helper method for determining the number of values of a given property.
     * This method expects that the property value is a collection.
     *
     * @param key the property key
     * @return the number of values stored for this property
     */
    private int valueCount(String key)
    {
        return ((Collection<?>) source.getProperty(key)).size();
    }

    /**
     * Tests setting a new root node.
     */
    public void testSetRootNode()
    {
        DefaultConfigurationNode node = new DefaultConfigurationNode();
        source.setRootNode(node);
        assertSame("Root node was not changed", node, source.getRootNode());
    }

    /**
     * Tests setting the root node to null. This should cause an exception.
     */
    public void testSetRootNodeNull()
    {
        try
        {
            source.setRootNode(null);
            fail("Could set null root node!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests visiting the root node.
     */
    public void testVisitRoot()
    {
        CountVisitor visitor = new CountVisitor();
        source.visit(null, visitor);
        visitor.check(nodeCount(false));
    }

    /**
     * Tests visiting only a part of the node structure.
     */
    public void testVisitPartly()
    {
        NodeList<ConfigurationNode> list = source
                .find("tables.table(0).fields.field(1)");
        assertEquals("Wrong size", 1, list.size());
        CountVisitor visitor = new CountVisitor();
        source.visit(list.getNode(0), visitor);
        visitor.check(2);
    }

    /**
     * Tests the isEmpty() method when the source contains data.
     */
    public void testIsEmptyDataAvailable()
    {
        assertFalse("Source is empty", source.isEmpty());
    }

    /**
     * Tests isEmpty() when the source does not contain data.
     */
    public void testIsEmptyNoData()
    {
        source.setRootNode(new DefaultConfigurationNode());
        assertTrue("Source not empty", source.isEmpty());
    }

    /**
     * Tests the size of the source.
     */
    public void testSize()
    {
        assertEquals("Wrong size", nodeCount(true), source.size());
    }

    /**
     * Tests the size of the source when there is only an empty root node.
     */
    public void testSizeEmpty()
    {
        source.setRootNode(new DefaultConfigurationNode());
        assertEquals("Wrong size", 0, source.size());
    }

    /**
     * Tests accessing properties.
     */
    public void testGetProperty()
    {
        Object prop = source.getProperty("tables.table(0).fields.field.name");
        assertNotNull("No field names found for tab 0", prop);
        assertTrue("No multiple field names for tab 0",
                prop instanceof Collection);
        assertEquals("Wrong number of field names for tab 0", 5,
                ((Collection<?>) prop).size());

        prop = source.getProperty("tables.table.fields.field.name");
        assertNotNull("No field names found", prop);
        assertTrue("No multiple field names", prop instanceof Collection);
        assertEquals("Wrong number of field names", 10, ((Collection<?>) prop)
                .size());

        prop = source.getProperty("tables.table.fields.field(3).name");
        assertNotNull("No names for field 3 found", prop);
        assertTrue("No multiple field 3 names", prop instanceof Collection);
        assertEquals("Wrong number of field 3 names", 2, ((Collection<?>) prop)
                .size());

        prop = source.getProperty("tables.table(1).fields.field(2).name");
        assertEquals("Wrong field name", "creationDate", prop.toString());
    }

    /**
     * Tests getProperty() for an unknown property.
     */
    public void testGetPropertyUnknown()
    {
        assertNull("Got value for unknown property", source
                .getProperty("TABLES.table.resultset"));
    }

    /**
     * Tests getProperty() for a property that is stored, but has no value.
     */
    public void testGetPropertyUndefined()
    {
        assertNull("Got value for undefined property", source
                .getProperty("tables.table.fields.field"));
    }

    /**
     * Tests setting property values.
     */
    public void testSetProperty()
    {
        source.setProperty("tables.table(0).name", "resources");
        assertEquals("Name not changed", "resources", source
                .getProperty("tables.table(0).name"));
        source.setProperty("tables.table.name", Arrays.asList(new String[] {
                "tab1", "tab2"
        }));
        assertEquals("Tab 1 name not changed", "tab1", source
                .getProperty("tables.table(0).name"));
        assertEquals("Tab 2 name not changed", "tab2", source
                .getProperty("tables.table(1).name"));

        final Integer[] testValues = new Integer[] {
                2, 4, 8, 16
        };
        source.setProperty("test.items.item", Arrays.asList(testValues));
        List<?> values = (List<?>) source.getProperty("test.items.item");
        assertEquals("Wrong number of test items", testValues.length, values
                .size());
        assertEquals("Wrong test item", testValues[2], source
                .getProperty("test.items.item(2)"));
        final Integer newValue = 6;
        source.setProperty("test.items.item(2)", newValue);
        assertEquals("Item not changed", newValue, source
                .getProperty("test.items.item(2)"));
        final Integer[] moreValues = new Integer[] {
                7, 9, 11
        };
        source.setProperty("test.items.item(2)", Arrays.asList(moreValues));
        values = (List<?>) source.getProperty("test.items.item");
        assertEquals("Wrong number of added items", 6, values.size());

        source.setProperty("test", Boolean.TRUE);
        source.setProperty("test.items", "01/01/05");
        values = (List<?>) source.getProperty("test.items.item");
        assertEquals("Items were changed", 6, values.size());
        assertEquals("Wrong boolean", Boolean.TRUE, source.getProperty("test"));
        assertEquals("Wrong string", "01/01/05", source
                .getProperty("test.items"));

        final Integer replaceValue = 42;
        source.setProperty("test.items.item", replaceValue);
        assertEquals("Items not replaced", replaceValue, source
                .getProperty("test.items.item"));
    }

    /**
     * Tests removing properties.
     */
    public void testClearProperty()
    {
        source.clearProperty("tables.table(0).fields.field(0).name");
        assertEquals("Field name not removed", "uname", source
                .getProperty("tables.table(0).fields.field(0).name"));
        source.clearProperty("tables.table(0).name");
        assertFalse("Table name still present", source
                .containsKey("tables.table(0).name"));
        assertEquals("Wrong field name", "firstName", source
                .getProperty("tables.table(0).fields.field(1).name"));
        assertEquals("Wrong table name", "documents", source
                .getProperty("tables.table.name"));
        source.clearProperty("tables.table");
        assertEquals("Table name affected", "documents", source
                .getProperty("tables.table.name"));

        source.addProperty("test", "first");
        source.addProperty("test.level", "second");
        source.clearProperty("test");
        assertEquals("Sub property was changed", "second", source
                .getProperty("test.level"));
        assertFalse("Property not removed", source.containsKey("test"));
    }

    /**
     * Tests clearing whole property trees.
     */
    public void testClearTree()
    {
        Object prop = source.getProperty("tables.table(0).fields.field.name");
        assertNotNull("Property not found", prop);
        source.clearTree("tables.table(0).fields.field(3)");
        prop = source.getProperty("tables.table(0).fields.field.name");
        assertNotNull("Property not found (2)", prop);
        assertTrue("Not multiple values (1)", prop instanceof Collection);
        assertEquals("Element not removed", 4, ((Collection<?>) prop).size());

        source.clearTree("tables.table(0).fields");
        assertNull("Sub property still found", source
                .getProperty("tables.table(0).fields.field.name"));
        prop = source.getProperty("tables.table.fields.field.name");
        assertNotNull("Property not found (3)", prop);
        assertTrue("Not multiple values (2)", prop instanceof Collection);
        assertEquals("Wrong number of elements", 5, ((Collection<?>) prop)
                .size());

        source.clearTree("tables.table(1)");
        assertNull("Still found table names", source
                .getProperty("tables.table.fields.field.name"));
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
            source.addProperty("indexList.index(-1)[@default]", Boolean.FALSE);
            source.addProperty("indexList.index[@name]", "test" + idx);
            source.addProperty("indexList.index.dir", "testDir" + idx);
        }
        assertEquals("Wrong number of nodes", count,
                valueCount("indexList.index[@name]"));

        // Remove a sub tree
        boolean found = false;
        for (int idx = 0; true; idx++)
        {
            String name = (String) source.getProperty("indexList.index(" + idx
                    + ")[@name]");
            if (name == null)
            {
                break;
            }
            if ("test3".equals(name))
            {
                assertEquals("Wrong dir", "testDir3", source
                        .getProperty("indexList.index(" + idx + ").dir"));
                source.clearTree("indexList.index(" + idx + ")");
                found = true;
            }
        }
        assertTrue("Key to remove not found", found);
        assertEquals("Wrong number of nodes after remove", count - 1,
                valueCount("indexList.index[@name]"));
        assertEquals("Wrong number of dir nodes after remove", count - 1,
                valueCount("indexList.index.dir"));

        // Verify
        for (int idx = 0; true; idx++)
        {
            String name = (String) source.getProperty("indexList.index(" + idx
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
     * Tests the clearTree() method on a hierarchical structure of nodes.
     */
    public void testClearTreeHierarchy()
    {
        source.addProperty("a.b.c", "c");
        source.addProperty("a.b.c.d", "d");
        source.addProperty("a.b.c.d.e", "e");
        source.clearTree("a.b.c");
        assertFalse("Property not removed", source.containsKey("a.b.c"));
        assertFalse("Sub property not removed", source.containsKey("a.b.c.d"));
    }

    /**
     * Tests for the containsKey() method.
     */
    public void testContainsKey()
    {
        assertTrue("No table name 1", source
                .containsKey("tables.table(0).name"));
        assertTrue("No table name 2", source
                .containsKey("tables.table(1).name"));
        assertFalse("Got name 3", source.containsKey("tables.table(2).name"));

        assertTrue("No field name", source
                .containsKey("tables.table(0).fields.field.name"));
        assertFalse("Got a field", source
                .containsKey("tables.table(0).fields.field"));
        source.clearTree("tables.table(0).fields");
        assertFalse("Got fields after remove", source
                .containsKey("tables.table(0).fields.field.name"));

        assertTrue("No more names", source
                .containsKey("tables.table.fields.field.name"));
    }

    /**
     * Tests the keys returned by the configuration source.
     */
    public void testGetKeys()
    {
        List<String> keys = new ArrayList<String>();
        for (Iterator<String> it = source.getKeys(); it.hasNext();)
        {
            keys.add(it.next());
        }

        assertEquals("Wrong number of keys", 3, keys.size());
        assertTrue("No table names", keys.contains("tables.table.name"));
        assertTrue("No field names", keys
                .contains("tables.table.fields.field.name"));
        assertTrue("No sys tab", keys.contains("tables.table[@sysTab]"));
    }

    /**
     * Tests whether keys are returned in the order they are added.
     */
    public void testGetKeysOrdered()
    {
        source.addProperty("order.key1", "value1");
        source.addProperty("order.key2", "value2");
        source.addProperty("order.key3", "value3");

        Iterator<String> it = source.getKeys("order");
        assertEquals("1st key", "order.key1", it.next());
        assertEquals("2nd key", "order.key2", it.next());
        assertEquals("3rd key", "order.key3", it.next());
    }

    /**
     * Tests the getKeys() method with a string prefix.
     */
    public void testGetKeysString()
    {
        // add some more properties to make it more interesting
        source.addProperty("tables.table(0).fields.field(1).type", "VARCHAR");
        source.addProperty("tables.table(0)[@type]", "system");
        source.addProperty("tables.table(0).size", "42");
        source.addProperty("tables.table(0).fields.field(0).size", "128");
        source.addProperty("connections.connection.param.url", "url1");
        source.addProperty("connections.connection.param.user", "me");
        source.addProperty("connections.connection.param.pwd", "secret");
        source.addProperty("connections.connection(-1).param.url", "url2");
        source.addProperty("connections.connection(1).param.user", "guest");

        checkKeys("tables.table(1)", new String[] {
                "name", "fields.field.name", "tables.table(1)[@sysTab]"
        });
        checkKeys("tables.table(0)", new String[] {
                "name", "fields.field.name", "tables.table(0)[@type]",
                "tables.table(0)[@sysTab]", "size", "fields.field.type",
                "fields.field.size"
        });
        checkKeys("connections.connection(0).param", new String[] {
                "url", "user", "pwd"
        });
        checkKeys("connections.connection(1).param", new String[] {
                "url", "user"
        });
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key.
     */
    public void testGetKeysWithKeyAsPrefix()
    {
        source.addProperty("order.key1", "value1");
        source.addProperty("order.key2", "value2");
        Iterator<String> it = source.getKeys("order.key1");
        assertTrue("no key found", it.hasNext());
        assertEquals("1st key", "order.key1", it.next());
        assertFalse("more keys than expected", it.hasNext());
    }

    /**
     * Tests getKeys() with a prefix when the prefix matches exactly a key, and
     * there are multiple keys starting with this prefix.
     */
    public void testGetKeysWithKeyAsPrefixMultiple()
    {
        source.addProperty("order.key1", "value1");
        source.addProperty("order.key1.test", "value2");
        source.addProperty("order.key1.test.complex", "value2");
        Iterator<String> it = source.getKeys("order.key1");
        assertEquals("Wrong key 1", "order.key1", it.next());
        assertEquals("Wrong key 2", "order.key1.test", it.next());
        assertEquals("Wrong key 3", "order.key1.test.complex", it.next());
        assertFalse("More keys than expected", it.hasNext());
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
        for (String exp : expected)
        {
            values.add((exp.startsWith(prefix)) ? exp : prefix + "." + exp);
        }

        Iterator<String> itKeys = source.getKeys(prefix);
        while (itKeys.hasNext())
        {
            String key = itKeys.next();
            if (!values.contains(key))
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
     * Tests adding different new properties.
     */
    public void testAddProperty()
    {
        source.addProperty("tables.table(0).fields.field(-1).name", "phone");
        assertEquals("Field phone not added", 6,
                valueCount("tables.table(0).fields.field.name"));

        source.addProperty("tables.table(0).fields.field.name", "fax");
        Object prop = source.getProperty("tables.table.fields.field(5).name");
        assertTrue("Not multiple values", prop instanceof List);
        List<?> list = (List<?>) prop;
        assertEquals("Wrong element 0", "phone", list.get(0));
        assertEquals("Wrong element 1", "fax", list.get(1));

        source.addProperty("tables.table(-1).name", "config");
        prop = source.getProperty("tables.table.name");
        assertTrue("Not multiple table names", prop instanceof Collection);
        assertEquals("Wrong number of tables", 3, ((Collection<?>) prop).size());
        source.addProperty("tables.table(2).fields.field(0).name", "cid");
        source.addProperty("tables.table(2).fields.field(-1).name", "confName");
        assertEquals("Wrong number of fields in new table", 2,
                valueCount("tables.table(2).fields.field.name"));
        assertEquals("Wrong name of new field", "confName", source
                .getProperty("tables.table(2).fields.field(1).name"));

        source.addProperty("connection.user", "scott");
        source.addProperty("connection.passwd", "tiger");
        assertEquals("Wrong password", "tiger", source
                .getProperty("connection.passwd"));
    }

    /**
     * Tests addProperty() when an invalid key is passed in. This should cause
     * an exception.
     */
    public void testAddPropertyInvalidKey()
    {
        try
        {
            source.addProperty(".", "InvalidKey");
            fail("Could add invalid key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests counting the values stored for a property key.
     */
    public void testValueCount()
    {
        assertEquals("Wrong fields in tab 0", 5, source
                .valueCount("tables.table(0).fields.field.name"));
        assertEquals("Wrong fields in tab 1", 5, source
                .valueCount("tables.table(1).fields.field.name"));
        assertEquals("Wrong table names", 2, source
                .valueCount("tables.table.name"));
        assertEquals("Wrong name count for tab ", 1, source
                .valueCount("tables.table(0).name"));
    }

    /**
     * Tests the valueCount() method when the passed in key does not exist.
     */
    public void testValueCountNonExisting()
    {
        assertEquals("Wrong value for non existing key", 0, source
                .valueCount("non.existing.key"));
    }

    /**
     * Tests the valueCount() method when the passed in key references a node
     * that does not have a value.
     */
    public void testValueCountUndefined()
    {
        assertEquals("Wrong result for key without a value", 0, source
                .valueCount("tables.table(1).fields.field(1)"));
    }

    /**
     * Tests querying the number of values for attributes with multiple values.
     */
    public void testValueCountAttributesMultipleValues()
    {
        source.addProperty("tables.table(0)[@mode]", "test");
        source.addProperty("tables.table(0)[@mode]", "production");
        source.addProperty("tables.table(1)[@mode]", "staging");
        assertEquals("Wrong value count", 3, source
                .valueCount("tables.table[@mode]"));
    }

    /**
     * Tests adding multiple values to an attribute.
     */
    public void testAddMultipleAttributeValues()
    {
        final String attrKey = "tables.table(0)[@mode]";
        source.addProperty(attrKey, "system");
        source.addProperty(attrKey, "security");
        List<?> values = (List<?>) source.getProperty(attrKey);
        assertEquals("Wrong number of values", 2, values.size());
        assertEquals("Wrong value 1", "system", values.get(0));
        assertEquals("Wrong value 2", "security", values.get(1));
    }

    /**
     * Tests overriding an attribute with multiple values.
     */
    public void testOverrideMultipleAttributeValues()
    {
        final String attrKey = "tables.table(0)[@mode]";
        testAddMultipleAttributeValues(); // set attribute values
        source.setProperty(attrKey, "NewValue");
        assertEquals("Wrong changed value", "NewValue", source
                .getProperty(attrKey));
    }

    /**
     * Tests find() for a null key. This should return the root node.
     */
    public void testFindNull()
    {
        NodeList<ConfigurationNode> list = source.find(null);
        assertEquals("Wrong number of elements", 1, list.size());
        assertEquals("Wrong result for null key", source.getRootNode(), list
                .getNode(0));
    }

    /**
     * Tests clearing the whole source.
     */
    public void testClear()
    {
        source.clear();
        assertTrue("Source not empty", source.isEmpty());
    }

    /**
     * A specialized visitor that only counts the visited nodes.
     */
    private static class CountVisitor extends
            NodeVisitorAdapter<ConfigurationNode>
    {
        int countBefore;

        int countAfter;

        @Override
        public void visitBeforeChildren(ConfigurationNode node,
                NodeHandler<ConfigurationNode> handler)
        {
            countBefore++;
        }

        @Override
        public void visitAfterChildren(ConfigurationNode node,
                NodeHandler<ConfigurationNode> handler)
        {
            countAfter++;
        }

        /**
         * Tests whether the expected number of nodes was found.
         *
         * @param expectedCount the expected number
         */
        public void check(int expectedCount)
        {
            assertEquals("Wrong before count", expectedCount, countBefore);
            assertEquals("Wrong after count", expectedCount, countAfter);
        }
    }
}
