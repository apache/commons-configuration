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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * Test class for PreferencesConfiguration.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestPreferencesConfiguration extends TestCase
{
    /** Constant for the node with the test data. */
    private static final String TEST_NODE = "PreferencesConfigurationTest";

    /** A preferences node with the test data. */
    private Preferences node;

    /**
     * Clears the test environment. If the test node was created, it is now
     * removed.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (node != null && node.nodeExists(TEST_NODE))
        {
            Preferences testNode = node.node(TEST_NODE);
            testNode.removeNode();
        }

        super.tearDown();
    }

    /**
     * Helper method for creating the final key for the given key. Adds the name
     * of the test node.
     *
     * @param k the key
     * @return the final key
     */
    private static String key(String k)
    {
        StringBuilder buf = new StringBuilder(TEST_NODE);
        buf.append('.').append(k);
        return buf.toString();
    }

    /**
     * Adds some test data to the test preferences node.
     */
    private void setUpTestData()
    {
        Preferences testNode = node.node(TEST_NODE);
        testNode.putBoolean("test", true);
        Preferences guiNode = testNode.node("gui");
        guiNode.put("background", "black");
        guiNode.put("foreground", "blue");
        Preferences dbNode = testNode.node("db");
        dbNode.put("user", "scott");
        dbNode.put("pwd", "tiger");
        Preferences tabNode = dbNode.node("tables");
        tabNode.put("tab1", "users");
        tabNode.put("tab2", "documents");
        try
        {
            testNode.flush();
        }
        catch (BackingStoreException bex)
        {
            throw new ConfigurationRuntimeException(bex);
        }
    }

    /**
     * Tests whether the configuration contains the expected properties.
     *
     * @param config the test configuration
     */
    private void checkProperties(PreferencesConfiguration config)
    {
        assertTrue("Wrong value for test", config.getBoolean(key("test")));
        assertEquals("Wrong value for background", "black", config
                .getString(key("gui.background")));
        assertEquals("Wrong value for foreground", "blue", config
                .getString(key("gui.foreground")));
        assertEquals("Wrong value for user", "scott", config
                .getString(key("db.user")));
        assertEquals("Wrong value for pwd", "tiger", config
                .getString(key("db.pwd")));
        assertEquals("Wrong value for tab1", "users", config
                .getString(key("db.tables.tab1")));
        assertEquals("Wrong value for tab2", "documents", config
                .getString(key("db.tables.tab2")));
    }

    /**
     * Creates some test data and a configuration for accessing it.
     *
     * @return the test configuration
     */
    private PreferencesConfiguration setUpTestConfig()
    {
        node = Preferences.userNodeForPackage(getClass());
        setUpTestData();
        return new PreferencesConfiguration(false, getClass());
    }

    /**
     * Tests querying properties from the system root node.
     */
    public void testGetPropertiesSystem()
    {
        node = Preferences.systemRoot();
        setUpTestData();
        PreferencesConfiguration config = new PreferencesConfiguration(true);
        checkProperties(config);
    }

    /**
     * Tests querying properties from the user root node.
     */
    public void testGetPropertiesUser()
    {
        node = Preferences.userRoot();
        setUpTestData();
        PreferencesConfiguration config = new PreferencesConfiguration();
        checkProperties(config);
    }

    /**
     * Tests querying properties from the system node with the given package.
     */
    public void testGetPropertiesSystemPackage()
    {
        node = Preferences.systemNodeForPackage(getClass());
        setUpTestData();
        PreferencesConfiguration config = new PreferencesConfiguration(true,
                getClass());
        checkProperties(config);
    }

    /**
     * Tests querying properties from the user node with the given package.
     */
    public void testGetPropertiesUserPackage()
    {
        PreferencesConfiguration config = setUpTestConfig();
        checkProperties(config);
    }

    /**
     * Tests setting a specific node as root node.
     */
    public void testGetPropertiesSpecificNode()
    {
        node = Preferences.userNodeForPackage(getClass());
        setUpTestData();
        PreferencesConfiguration config = new PreferencesConfiguration(node);
        checkProperties(config);
    }

    /**
     * Tests creating a configuration with a null node. This should cause an
     * exception.
     */
    public void testInitNullNode()
    {
        try
        {
            new PreferencesConfiguration((Preferences) null);
            fail("Could create instance with null node!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether changing the configuration's parameters causes the root
     * node to be re-created.
     */
    public void testChangeParameters()
    {
        PreferencesConfiguration config = new PreferencesConfiguration();
        Preferences root = config.getRootNode();
        config.setAssociatedClass(getClass());
        assertNotSame("Root node not changed", root, config.getRootNode());
    }

    /**
     * Tests whether the expected keys are returned.
     */
    public void testGetKeys()
    {
        PreferencesConfiguration config = setUpTestConfig();
        Set<String> keys = new HashSet<String>();
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next());
        }
        assertTrue("Key not found: background", keys
                .contains(key("gui.background")));
        assertTrue("Key not found: user", keys.contains(key("db.user")));
        assertTrue("Key not found: tab1", keys.contains(key("db.tables.tab1")));
    }

    /**
     * Tests the isEmpty() method.
     */
    public void testIsEmpty()
    {
        PreferencesConfiguration config = setUpTestConfig();
        assertFalse("Configuration is empty", config.isEmpty());
    }

    /**
     * Tests adding a new property.
     */
    public void testAddProperty()
    {
        PreferencesConfiguration config = setUpTestConfig();
        config.addProperty(key("anotherTest"), Boolean.TRUE);
        config.addProperty(key("db.url"), "testdb");
        config.flush();
        Preferences nd = node.node(TEST_NODE);
        assertTrue("test key not set", nd.getBoolean("anotherTest", false));
        nd = nd.node("db");
        assertEquals("Db property not set", "testdb", nd.get("url", null));
    }

    /**
     * Tests the addProperty() method when a new node has to be added.
     */
    public void testAddPropertyNewNode()
    {
        PreferencesConfiguration config = setUpTestConfig();
        config.addProperty(key("db.meta.session.mode"), "debug");
        config.flush();
        Preferences nd = node.node(TEST_NODE + "/db/meta/session");
        assertEquals("Property not added", "debug", nd.get("mode", null));
    }

    /**
     * Tests overriding a property.
     */
    public void testSetProperty()
    {
        PreferencesConfiguration config = setUpTestConfig();
        config.setProperty(key("db.user"), "harry");
        config.setProperty(key("db.url"), "testdb");
        Preferences nd = node.node(TEST_NODE + "/db");
        assertEquals("Property not added", "testdb", nd.get("url", null));
        assertEquals("Property not set", "harry", nd.get("user", null));
    }

    /**
     * Tests whether a property can be removed.
     */
    public void testClearProperty() throws BackingStoreException
    {
        PreferencesConfiguration config = setUpTestConfig();
        config.clearProperty(key("db.tables.tab1"));
        Preferences nd = node.node(TEST_NODE + "/db/tables");
        String[] keys = nd.keys();
        assertEquals("Key not removed", 1, keys.length);
        assertEquals("Wrong key removed", "tab2", keys[0]);
    }

    /**
     * Tests removing a whole property tree.
     */
    public void testClearTree() throws BackingStoreException
    {
        PreferencesConfiguration config = setUpTestConfig();
        config.clearTree(key("db"));
        assertFalse("Node not removed", node.nodeExists(TEST_NODE + "/db"));
    }

    /**
     * Tests querying the number of property values.
     */
    public void testGetMaxIndex()
    {
        PreferencesConfiguration config = setUpTestConfig();
        assertEquals("Wrong number of values", 0, config
                .getMaxIndex(key("db.user")));
        assertEquals("Wrong number of values for node", 0, config
                .getMaxIndex(key("db")));
        assertEquals("Wrong number of values for non ex. property", -1, config
                .getMaxIndex("non.ex.key"));
    }

    /**
     * Tests obtaining a sub configuration.
     */
    public void testConfigurationAt()
    {
        PreferencesConfiguration config = setUpTestConfig();
        SubConfiguration<Preferences> sub = config.configurationAt(TEST_NODE);
        assertEquals("Wrong user", "scott", sub.getString("db.user"));
    }

    /**
     * Tests modifying a sub configuration.
     */
    public void testConfigurationAtModify()
    {
        PreferencesConfiguration config = setUpTestConfig();
        SubConfiguration<Preferences> sub = config.configurationAt(TEST_NODE);
        sub.setProperty("db.user", "harry");
        config.setProperty(key("db.pwd"), "dolphin");
        sub.addProperty("db.url", "testdb");
        assertEquals("User not changed", "harry", config
                .getString(key("db.user")));
        assertEquals("URL not found", "testdb", config.getString(key("db.url")));
        assertEquals("Pwd not changed", "dolphin", sub.getString("db.pwd"));
    }
}
