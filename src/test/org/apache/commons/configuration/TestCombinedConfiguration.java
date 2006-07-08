/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.Set;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.configuration.tree.UnionCombiner;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test class for CombinedConfiguration.
 *
 * @version $Id$
 */
public class TestCombinedConfiguration extends TestCase
{
    /** Constant for the name of a sub configuration. */
    static final String TEST_NAME = "SUBCONFIG";

    /** Constant for a test key. */
    static final String TEST_KEY = "test.value";

    /** The configuration to be tested. */
    CombinedConfiguration config;

    /** The test event listener. */
    CombinedListener listener;

    protected void setUp() throws Exception
    {
        super.setUp();
        config = new CombinedConfiguration();
        listener = new CombinedListener();
        config.addConfigurationListener(listener);
    }

    /**
     * Tests accessing a newly created combined configuration.
     */
    public void testInit()
    {
        assertEquals("Already configurations contained", 0, config
                .getNumberOfConfigurations());
        assertTrue("Set of names is not empty", config.getConfigurationNames()
                .isEmpty());
        assertTrue("Wrong node combiner",
                config.getNodeCombiner() instanceof UnionCombiner);
        assertNull("Test config was found", config.getConfiguration(TEST_NAME));
    }

    /**
     * Tests adding a configuration (without further information).
     */
    public void testAddConfiguration()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        checkAddConfig(c);
        assertEquals("Wrong number of configs", 1, config
                .getNumberOfConfigurations());
        assertTrue("Name list is not empty", config.getConfigurationNames()
                .isEmpty());
        assertSame("Added config not found", c, config.getConfiguration(0));
        assertTrue("Wrong property value", config.getBoolean(TEST_KEY));
        listener.checkEvent(1, 0);
    }

    /**
     * Tests adding a configuration with a name.
     */
    public void testAddConfigurationWithName()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        checkAddConfig(c);
        assertEquals("Wrong number of configs", 1, config
                .getNumberOfConfigurations());
        assertSame("Added config not found", c, config.getConfiguration(0));
        assertSame("Added config not found by name", c, config
                .getConfiguration(TEST_NAME));
        Set names = config.getConfigurationNames();
        assertEquals("Wrong number of config names", 1, names.size());
        assertTrue("Name not found", names.contains(TEST_NAME));
        assertTrue("Wrong property value", config.getBoolean(TEST_KEY));
        listener.checkEvent(1, 0);
    }

    /**
     * Tests adding a configuration with a name when this name already exists.
     * This should cause an exception.
     */
    public void testAddConfigurationWithNameTwice()
    {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME);
        try
        {
            config.addConfiguration(setUpTestConfiguration(), TEST_NAME,
                    "prefix");
            fail("Could add config with same name!");
        }
        catch (ConfigurationRuntimeException cex)
        {
            // ok
        }
    }

    /**
     * Tests adding a configuration and specifying an at position.
     */
    public void testAddConfigurationAt()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, null, "my");
        checkAddConfig(c);
        assertTrue("Wrong property value", config.getBoolean("my." + TEST_KEY));
    }

    /**
     * Tests adding a configuration with a complex at position. Here the at path
     * contains a dot, which must be escaped.
     */
    public void testAddConfigurationComplexAt()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, null, "This..is.a.complex");
        checkAddConfig(c);
        assertTrue("Wrong property value", config
                .getBoolean("This..is.a.complex." + TEST_KEY));
    }

    /**
     * Checks if a configuration was correctly added to the combined config.
     *
     * @param c the config to check
     */
    private void checkAddConfig(AbstractConfiguration c)
    {
        Collection listeners = c.getConfigurationListeners();
        assertEquals("Wrong number of configuration listeners", 1, listeners
                .size());
        assertTrue("Combined config is no listener", listeners.contains(config));
    }

    /**
     * Tests adding a null configuration. This should cause an exception to be
     * thrown.
     */
    public void testAddNullConfiguration()
    {
        try
        {
            config.addConfiguration(null);
            fail("Could add null configuration!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests accessing properties if no configurations have been added.
     */
    public void testAccessPropertyEmpty()
    {
        assertFalse("Found a key", config.containsKey(TEST_KEY));
        assertNull("Key has a value", config.getString("test.comment"));
        assertTrue("Config is not empty", config.isEmpty());
    }

    /**
     * Tests accessing properties if multiple configurations have been added.
     */
    public void testAccessPropertyMulti()
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), null, "prefix1");
        config.addConfiguration(setUpTestConfiguration(), null, "prefix2");
        assertTrue("Prop1 not found", config.getBoolean(TEST_KEY));
        assertTrue("Prop 2 not found", config.getBoolean("prefix1." + TEST_KEY));
        assertTrue("Prop 3 not found", config.getBoolean("prefix2." + TEST_KEY));
        assertFalse("Configuration is empty", config.isEmpty());
        listener.checkEvent(3, 0);
    }

    /**
     * Tests removing a configuration.
     */
    public void testRemoveConfiguration()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        checkAddConfig(c);
        assertTrue("Config could not be removed", config.removeConfiguration(c));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration by index.
     */
    public void testRemoveConfigurationAt()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        assertSame("Wrong config removed", c, config.removeConfigurationAt(0));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration by name.
     */
    public void testRemoveConfigurationByName()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        assertSame("Wrong config removed", c, config
                .removeConfiguration(TEST_NAME));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration with a name.
     */
    public void testRemoveNamedConfiguration()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        config.removeConfiguration(c);
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a named configuration by index.
     */
    public void testRemoveNamedConfigurationAt()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        assertSame("Wrong config removed", c, config.removeConfigurationAt(0));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration that was not added prior.
     */
    public void testRemoveNonContainedConfiguration()
    {
        assertFalse("Could remove non contained config", config
                .removeConfiguration(setUpTestConfiguration()));
        listener.checkEvent(0, 0);
    }

    /**
     * Tests removing a configuration by name, which is not contained.
     */
    public void testRemoveConfigurationByUnknownName()
    {
        assertNull("Could remove configuration by unknown name", config
                .removeConfiguration("unknownName"));
        listener.checkEvent(0, 0);
    }

    /**
     * Tests whether a configuration was completely removed.
     *
     * @param c the removed configuration
     */
    private void checkRemoveConfig(AbstractConfiguration c)
    {
        assertTrue("Listener was not removed", c.getConfigurationListeners()
                .isEmpty());
        assertEquals("Wrong number of contained configs", 0, config
                .getNumberOfConfigurations());
        assertTrue("Name was not removed", config.getConfigurationNames()
                .isEmpty());
        listener.checkEvent(2, 0);
    }

    /**
     * Tests if an update of a contained configuration leeds to an invalidation
     * of the combined configuration.
     */
    public void testUpdateContainedConfiguration()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        c.addProperty("test.otherTest", "yes");
        assertEquals("New property not found", "yes", config
                .getString("test.otherTest"));
        listener.checkEvent(3, 0);
    }

    /**
     * Tests if setting a node combiner causes an invalidation.
     */
    public void testSetNodeCombiner()
    {
        NodeCombiner combiner = new UnionCombiner();
        config.setNodeCombiner(combiner);
        assertSame("Node combiner was not set", combiner, config
                .getNodeCombiner());
        listener.checkEvent(1, 0);
    }

    /**
     * Tests setting a null node combiner. This should cause an exception.
     */
    public void testSetNullNodeCombiner()
    {
        try
        {
            config.setNodeCombiner(null);
            fail("Could set null node combiner!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests cloning a combined configuration.
     */
    public void testClone()
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        config.addConfiguration(new PropertiesConfiguration(), "props");

        CombinedConfiguration cc2 = (CombinedConfiguration) config.clone();
        assertEquals("Wrong number of contained configurations", config
                .getNumberOfConfigurations(), cc2.getNumberOfConfigurations());
        assertSame("Wrong node combiner", config.getNodeCombiner(), cc2
                .getNodeCombiner());
        assertEquals("Wrong number of names", config.getConfigurationNames()
                .size(), cc2.getConfigurationNames().size());
        assertTrue("Event listeners were cloned", cc2
                .getConfigurationListeners().isEmpty());

        StrictConfigurationComparator comp = new StrictConfigurationComparator();
        for (int i = 0; i < config.getNumberOfConfigurations(); i++)
        {
            assertNotSame("Configuration at " + i + " was not cloned", config
                    .getConfiguration(i), cc2.getConfiguration(i));
            assertEquals("Wrong config class at " + i, config.getConfiguration(
                    i).getClass(), cc2.getConfiguration(i).getClass());
            assertTrue("Configs not equal at " + i, comp.compare(config
                    .getConfiguration(i), cc2.getConfiguration(i)));
        }

        assertTrue("Combined configs not equal", comp.compare(config, cc2));
    }

    /**
     * Tests if the cloned configuration is decoupled from the original.
     */
    public void testCloneModify()
    {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME);
        CombinedConfiguration cc2 = (CombinedConfiguration) config.clone();
        assertTrue("Name is missing", cc2.getConfigurationNames().contains(
                TEST_NAME));
        cc2.removeConfiguration(TEST_NAME);
        assertFalse("Names in original changed", config.getConfigurationNames()
                .isEmpty());
    }

    /**
     * Tests clearing a combined configuration. This should remove all contained
     * configurations.
     */
    public void testClear()
    {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "test");
        config.addConfiguration(setUpTestConfiguration());

        config.clear();
        assertEquals("Still configs contained", 0, config
                .getNumberOfConfigurations());
        assertTrue("Still names contained", config.getConfigurationNames()
                .isEmpty());
        assertTrue("Config is not empty", config.isEmpty());

        listener.checkEvent(3, 2);
    }

    /**
     * Helper method for creating a test configuration to be added to the
     * combined configuration.
     *
     * @return the test configuration
     */
    private AbstractConfiguration setUpTestConfiguration()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty(TEST_KEY, Boolean.TRUE);
        config.addProperty("test.comment", "This is a test");
        return config;
    }

    /**
     * Test event listener class for checking if the expected invalidate events
     * are fired.
     */
    static class CombinedListener implements ConfigurationListener
    {
        int invalidateEvents;

        int otherEvents;

        public void configurationChanged(ConfigurationEvent event)
        {
            if (event.getType() == CombinedConfiguration.EVENT_COMBINED_INVALIDATE)
            {
                invalidateEvents++;
            }
            else
            {
                otherEvents++;
            }
        }

        /**
         * Checks if the expected number of events was fired.
         *
         * @param expectedInvalidate the expected number of invalidate events
         * @param expectedOthers the expected number of other events
         */
        public void checkEvent(int expectedInvalidate, int expectedOthers)
        {
            Assert.assertEquals("Wrong number of invalidate events",
                    expectedInvalidate, invalidateEvents);
            Assert.assertEquals("Wrong number of other events", expectedOthers,
                    otherEvents);
        }
    }
}
