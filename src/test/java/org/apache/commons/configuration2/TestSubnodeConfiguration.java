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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.reloading.FileAlwaysReloadingStrategy;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;

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

    /** Constant for a test output file.*/
    private static final File TEST_FILE = new File("target/test.xml");

    /** Constant for an updated table name.*/
    private static final String NEW_TABLE_NAME = "newTable";

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

    protected void tearDown() throws Exception
    {
        // remove the test output file if necessary
        if (TEST_FILE.exists())
        {
            TEST_FILE.delete();
        }
    }

    /**
     * Tests creation of a subnode config.
     */
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
        List<?> fields = config.getList("fields.field.name");
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
        List<?> fields = config.getList("fields.field.name");
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
        Set<String> keys = new HashSet<String>();
        for (Iterator<?> it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next().toString());
        }
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
        Set<String> keys = new HashSet<String>();
        for (Iterator<?> it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next().toString());
        }
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
     * An additional test for interpolation when the configurationAt() method is
     * involved.
     */
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
     * Tests a reload operation for the parent configuration when the subnode
     * configuration does not support reloads. Then the new value should not be
     * detected.
     */
    public void testParentReloadNotSupported() throws ConfigurationException
    {
        Configuration c = setUpReloadTest(false);
        assertEquals("Name changed in sub config", TABLE_NAMES[1], config
                .getString("name"));
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
    }

    /**
     * Tests a reload operation for the parent configuration when the subnode
     * configuration does support reloads. The new value should be returned.
     */
    public void testParentReloadSupported() throws ConfigurationException
    {
        Configuration c = setUpReloadTest(true);
        assertEquals("Name not changed in sub config", NEW_TABLE_NAME, config
                .getString("name"));
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
    }

    /**
     * Tests a reload operation for the parent configuration when the subnode
     * configuration is aware of reloads, and the parent configuration is
     * accessed first. The new value should be returned.
     */
    public void testParentReloadSupportAccessParent()
            throws ConfigurationException
    {
        Configuration c = setUpReloadTest(true);
        assertEquals("Name not changed in parent", NEW_TABLE_NAME, c
                .getString("tables.table(1).name"));
        assertEquals("Name not changed in sub config", NEW_TABLE_NAME, config
                .getString("name"));
    }

    /**
     * Tests whether reloads work with sub subnode configurations.
     */
    public void testParentReloadSubSubnode() throws ConfigurationException
    {
        setUpReloadTest(true);
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
    public void testParentReloadSubSubnodeNoChangeSupport()
            throws ConfigurationException
    {
        setUpReloadTest(false);
        SubnodeConfiguration sub = config.configurationAt("fields", true);
        assertNull("Sub sub config is attached to parent", sub.getSubnodeKey());
        assertEquals("Changed field name returned", TABLE_FIELDS[1][0], sub
                .getString("field(0).name"));
    }

    /**
     * Prepares a test for a reload operation.
     *
     * @param supportReload a flag whether the subnode configuration should
     * support reload operations
     * @return the parent configuration that can be used for testing
     * @throws ConfigurationException if an error occurs
     */
    private XMLConfiguration setUpReloadTest(boolean supportReload)
            throws ConfigurationException
    {
        XMLConfiguration xmlConf = new XMLConfiguration(parent);
        xmlConf.setFile(TEST_FILE);
        xmlConf.save();
        config = xmlConf.configurationAt("tables.table(1)", supportReload);
        assertEquals("Wrong table name", TABLE_NAMES[1], config
                .getString("name"));
        xmlConf.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        // Now change the configuration file
        XMLConfiguration confUpdate = new XMLConfiguration(TEST_FILE);
        confUpdate.setProperty("tables.table(1).name", NEW_TABLE_NAME);
        confUpdate.setProperty("tables.table(1).fields.field(0).name",
                "newField");
        confUpdate.save();
        return xmlConf;
    }

    /**
     * Tests a manipulation of the parent configuration that causes the subnode
     * configuration to become invalid. In this case the sub config should be
     * detached and keep its old values.
     */
    public void testParentChangeDetach()
    {
        final String key = "tables.table(1)";
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
    public void testParentChangeDetatchException()
    {
        config = parent.configurationAt("tables.table(1)", true);
        parent.setExpressionEngine(new XPathExpressionEngine());
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
    protected HierarchicalConfiguration setUpParentConfig()
    {
        @SuppressWarnings("serial")
        HierarchicalConfiguration conf = new HierarchicalConfiguration()
        {
            // Provide a special implementation of createNode() to check
            // if it is called by the subnode config
            @Override
            protected ConfigurationNode createNode(String name)
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
        ConfigurationNode root = conf.getRootNode();
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
