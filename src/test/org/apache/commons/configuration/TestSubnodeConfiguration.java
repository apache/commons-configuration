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

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import junit.framework.TestCase;

/**
 * Test case for SubnodeConfiguration.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestSubnodeConfiguration extends TestCase
{
    /** An array with names of tables (test data). */
    private static final String[] TABLE_NAMES =
    { "documents", "users" };

    /** An array with the fields of the test tables (test data). */
    private static final String[][] TABLE_FIELDS =
    {
    { "docid", "docname", "author", "dateOfCreation", "version", "size" },
    { "userid", "uname", "firstName", "lastName" } };

    /** The parent configuration. */
    HierarchicalConfiguration parent;

    /** The subnode configuration to be tested. */
    SubnodeConfiguration config;

    /** Stores the root node of the subnode config. */
    ConfigurationNode subnode;

    /** Stores a counter for the created nodes. */
    int nodeCounter;

    protected void setUp() throws Exception
    {
        super.setUp();
        parent = setUpParentConfig();
        nodeCounter = 0;
    }

    /**
     * Tests creation of a subnode config.
     */
    public void testInitSubNodeConfig()
    {
        setUpSubnodeConfig();
        assertSame("Wrong root node in subnode", getSubnodeRoot(parent), config
                .getRoot());
        assertSame("Wrong parent config", parent, config.getParent());
    }

    /**
     * Tests constructing a subnode configuration with a null parent. This
     * should cause an exception.
     */
    public void testInitSubNodeConfigWithNullParent()
    {
        try
        {
            config = new SubnodeConfiguration(null, getSubnodeRoot(parent));
            fail("Could set a null parent config!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests constructing a subnode configuration with a null root node. This
     * should cause an exception.
     */
    public void testInitSubNodeConfigWithNullNode()
    {
        try
        {
            config = new SubnodeConfiguration(parent, null);
            fail("Could set a null root node!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests if properties of the sub node can be accessed.
     */
    public void testGetProperties()
    {
        setUpSubnodeConfig();
        assertEquals("Wrong table name", TABLE_NAMES[0], config
                .getString("name"));
        List fields = config.getList("fields.field.name");
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
    public void testAddProperty()
    {
        setUpSubnodeConfig();
        config.addProperty("[@table-type]", "test");
        assertEquals("parent.createNode() was not called", 1, nodeCounter);
        assertEquals("Attribute not set", "test", parent
                .getString("tables.table(0)[@table-type]"));

        parent.addProperty("tables.table(0).fields.field(-1).name", "newField");
        List fields = config.getList("fields.field.name");
        assertEquals("New field was not added", TABLE_FIELDS[0].length + 1,
                fields.size());
        assertEquals("Wrong last field", "newField", fields
                .get(fields.size() - 1));
    }

    /**
     * Tests listing the defined keys.
     */
    public void testGetKeys()
    {
        setUpSubnodeConfig();
        Set keys = new HashSet();
        CollectionUtils.addAll(keys, config.getKeys());
        assertEquals("Incorrect number of keys", 2, keys.size());
        assertTrue("Key 1 not contained", keys.contains("name"));
        assertTrue("Key 2 not contained", keys.contains("fields.field.name"));
    }

    /**
     * Tests setting the exception on missing flag. The subnode config obtains
     * this flag from its parent.
     */
    public void testSetThrowExceptionOnMissing()
    {
        parent.setThrowExceptionOnMissing(true);
        setUpSubnodeConfig();
        assertTrue("Exception flag not fetchted from parent", config
                .isThrowExceptionOnMissing());
        try
        {
            config.getString("non existing key");
            fail("Could fetch non existing key!");
        }
        catch (NoSuchElementException nex)
        {
            // ok
        }

        config.setThrowExceptionOnMissing(false);
        assertTrue("Exception flag reset on parent", parent
                .isThrowExceptionOnMissing());
    }

    /**
     * Tests handling of the delimiter parsing disabled flag. This is shared
     * with the parent, too.
     */
    public void testSetDelimiterParsingDisabled()
    {
        parent.setDelimiterParsingDisabled(true);
        setUpSubnodeConfig();
        parent.setDelimiterParsingDisabled(false);
        assertTrue("Delimiter parsing flag was not received from parent",
                config.isDelimiterParsingDisabled());
        config.addProperty("newProp", "test1,test2,test3");
        assertEquals("New property was splitted", "test1,test2,test3", parent
                .getString("tables.table(0).newProp"));
        parent.setDelimiterParsingDisabled(true);
        config.setDelimiterParsingDisabled(false);
        assertTrue("Delimiter parsing flag was reset on parent", parent
                .isDelimiterParsingDisabled());
    }

    /**
     * Tests manipulating the list delimiter. This piece of data is derived from
     * the parent.
     */
    public void testSetListDelimiter()
    {
        parent.setListDelimiter('/');
        setUpSubnodeConfig();
        parent.setListDelimiter(';');
        assertEquals("List delimiter not obtained from parent", '/', config
                .getListDelimiter());
        config.addProperty("newProp", "test1,test2/test3");
        assertEquals("List was incorrectly splitted", "test1,test2", parent
                .getString("tables.table(0).newProp"));
        config.setListDelimiter(',');
        assertEquals("List delimiter changed on parent", ';', parent
                .getListDelimiter());
    }

    /**
     * Tests changing the expression engine.
     */
    public void testSetExpressionEngine()
    {
        parent.setExpressionEngine(new XPathExpressionEngine());
        setUpSubnodeConfig();
        assertEquals("Wrong field name", TABLE_FIELDS[0][1], config
                .getString("fields/field[2]/name"));
        Set keys = new HashSet();
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
    public void testConfiguarationAt()
    {
        setUpSubnodeConfig();
        SubnodeConfiguration sub2 = (SubnodeConfiguration) config
                .configurationAt("fields.field(1)");
        assertEquals("Wrong value of property", TABLE_FIELDS[0][1], sub2
                .getString("name"));
        assertEquals("Wrong parent", config.getParent(), sub2.getParent());
    }

    /**
     * Tests interpolation features. The subnode config should use its parent
     * for interpolation.
     */
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
     * Initializes the parent configuration. This method creates the typical
     * structure of tables and fields nodes.
     *
     * @return the parent configuration
     */
    protected HierarchicalConfiguration setUpParentConfig()
    {
        HierarchicalConfiguration conf = new HierarchicalConfiguration()
        {
            // Provide a special implementation of createNode() to check
            // if it is called by the subnode config
            protected Node createNode(String name)
            {
                nodeCounter++;
                return super.createNode(name);
            }
        };
        for (int i = 0; i < TABLE_NAMES.length; i++)
        {
            conf.addProperty("tables.table(-1).name", TABLE_NAMES[i]);
            for (int j = 0; j < TABLE_FIELDS[i].length; j++)
            {
                conf.addProperty("tables.table.fields.field(-1).name",
                        TABLE_FIELDS[i][j]);
            }
        }
        return conf;
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
        ConfigurationNode root = conf.getRoot();
        return root.getChild(0).getChild(0);
    }

    /**
     * Performs a standard initialization of the subnode config to test.
     */
    protected void setUpSubnodeConfig()
    {
        config = new SubnodeConfiguration(parent, getSubnodeRoot(parent));
    }
}
