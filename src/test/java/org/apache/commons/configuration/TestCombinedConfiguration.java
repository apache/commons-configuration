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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileAlwaysReloadingStrategy;
import org.apache.commons.configuration.reloading.FileRandomReloadingStrategy;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.MergeCombiner;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for CombinedConfiguration.
 *
 * @version $Id$
 */
public class TestCombinedConfiguration
{
    /** Constant for the name of a sub configuration. */
    private static final String TEST_NAME = "SUBCONFIG";

    /** Constant for a test key. */
    private static final String TEST_KEY = "test.value";

    /** Constant for the name of the first child configuration.*/
    private static final String CHILD1 = TEST_NAME + "1";

    /** Constant for the name of the second child configuration.*/
    private static final String CHILD2 = TEST_NAME + "2";

    /** Constant for the name of the XML reload test file.*/
    private static final String RELOAD_XML_NAME = "reload.xml";

    /** Constant for the content of a XML reload test file.*/
    private static final String RELOAD_XML_CONTENT = "<xml><xmlReload>{0}</xmlReload></xml>";

    /** Constant for the name of the properties reload test file.*/
    private static final String RELOAD_PROPS_NAME = "reload.properties";

    /** Constant for the content of a properties reload test file.*/
    private static final String RELOAD_PROPS_CONTENT = "propsReload = {0}";

    /** Helper object for managing temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** The configuration to be tested. */
    private CombinedConfiguration config;

    /** The test event listener. */
    private CombinedListener listener;

    @Before
    public void setUp() throws Exception
    {
        config = new CombinedConfiguration();
        listener = new CombinedListener();
        config.addConfigurationListener(listener);
    }

    /**
     * Tests accessing a newly created combined configuration.
     */
    @Test
    public void testInit()
    {
        assertEquals("Already configurations contained", 0, config
                .getNumberOfConfigurations());
        assertTrue("Set of names is not empty", config.getConfigurationNames()
                .isEmpty());
        assertTrue("Wrong node combiner",
                config.getNodeCombiner() instanceof UnionCombiner);
        assertNull("Test config was found", config.getConfiguration(TEST_NAME));
        assertFalse("Force reload check flag is set", config.isForceReloadCheck());
    }

    /**
     * Tests adding a configuration (without further information).
     */
    @Test
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
    @Test
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
        Set<String> names = config.getConfigurationNames();
        assertEquals("Wrong number of config names", 1, names.size());
        assertTrue("Name not found", names.contains(TEST_NAME));
        assertTrue("Wrong property value", config.getBoolean(TEST_KEY));
        listener.checkEvent(1, 0);
    }

    /**
     * Tests adding a configuration with a name when this name already exists.
     * This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testAddConfigurationWithNameTwice()
    {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME);
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME,
                "prefix");
    }

    /**
     * Tests adding a configuration and specifying an at position.
     */
    @Test
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
    @Test
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
        Collection<ConfigurationListener> listeners = c.getConfigurationListeners();
        assertEquals("Wrong number of configuration listeners", 1, listeners
                .size());
        assertTrue("Combined config is no listener", listeners.contains(config));
    }

    /**
     * Tests adding a null configuration. This should cause an exception to be
     * thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullConfiguration()
    {
        config.addConfiguration(null);
    }

    /**
     * Tests accessing properties if no configurations have been added.
     */
    @Test
    public void testAccessPropertyEmpty()
    {
        assertFalse("Found a key", config.containsKey(TEST_KEY));
        assertNull("Key has a value", config.getString("test.comment"));
        assertTrue("Config is not empty", config.isEmpty());
    }

    /**
     * Tests accessing properties if multiple configurations have been added.
     */
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testRemoveNonContainedConfiguration()
    {
        assertFalse("Could remove non contained config", config
                .removeConfiguration(setUpTestConfiguration()));
        listener.checkEvent(0, 0);
    }

    /**
     * Tests removing a configuration by name, which is not contained.
     */
    @Test
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
    @Test
    public void testUpdateContainedConfiguration()
    {
        AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        c.addProperty("test.otherTest", "yes");
        assertEquals("New property not found", "yes", config
                .getString("test.otherTest"));
        listener.checkEvent(2, 0);
    }

    /**
     * Tests if setting a node combiner causes an invalidation.
     */
    @Test
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
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullNodeCombiner()
    {
        config.setNodeCombiner(null);
    }

    /**
     * Tests cloning a combined configuration.
     */
    @Test
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
    @Test
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
    @Test
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
     * Tests if file-based configurations can be reloaded.
     */
    @Test
    public void testReloading() throws Exception
    {
        config.setForceReloadCheck(true);
        File testXmlFile = writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT, 0);
        File testPropsFile = writeReloadFile(RELOAD_PROPS_NAME, RELOAD_PROPS_CONTENT, 0);
        XMLConfiguration c1 = new XMLConfiguration(testXmlFile);
        c1.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        PropertiesConfiguration c2 = new PropertiesConfiguration(testPropsFile);
        c2.setThrowExceptionOnMissing(true);
        c2.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        config.addConfiguration(c1);
        config.addConfiguration(c2);
        assertEquals("Wrong xml reload value", 0, config.getInt("xmlReload"));
        assertEquals("Wrong props reload value", 0, config
                .getInt("propsReload"));

        writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT, 1);
        assertEquals("XML reload not detected", 1, config.getInt("xmlReload"));
        config.setForceReloadCheck(false);
        writeReloadFile(RELOAD_PROPS_NAME, RELOAD_PROPS_CONTENT, 1);
        assertEquals("Props reload detected though check flag is false", 0, config
                .getInt("propsReload"));
    }

    /**
     * Tests whether the reload check works with a subnode configuration. This
     * test is related to CONFIGURATION-341.
     */
    @Test
    public void testReloadingSubnodeConfig() throws IOException,
            ConfigurationException
    {
        config.setForceReloadCheck(true);
        File testXmlFile = writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT,
                0);
        XMLConfiguration c1 = new XMLConfiguration(testXmlFile);
        c1.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        final String prefix = "reloadCheck";
        config.addConfiguration(c1, CHILD1, prefix);
        SubnodeConfiguration sub = config.configurationAt(prefix, true);
        writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT, 1);
        assertEquals("Reload not detected", 1, sub.getInt("xmlReload"));
    }

    /**
     * Tests whether reloading works for a combined configuration nested in
     * another combined configuration.
     */
    @Test
    public void testReloadingNestedCC() throws IOException,
            ConfigurationException
    {
        config.setForceReloadCheck(true);
        File testXmlFile =
                writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT, 0);
        File testPropsFile =
                writeReloadFile(RELOAD_PROPS_NAME, RELOAD_PROPS_CONTENT, 0);
        XMLConfiguration c1 = new XMLConfiguration(testXmlFile);
        c1.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        PropertiesConfiguration c2 = new PropertiesConfiguration(testPropsFile);
        c2.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        config.addConfiguration(c2);
        CombinedConfiguration cc2 = new CombinedConfiguration();
        cc2.setForceReloadCheck(true);
        cc2.addConfiguration(c1);
        config.addConfiguration(cc2);
        assertEquals("Wrong xml reload value", 0, config.getInt("xmlReload"));
        writeReloadFile(RELOAD_XML_NAME, RELOAD_XML_CONTENT, 1);
        assertEquals("XML reload not detected", 1, config.getInt("xmlReload"));
    }

    /**
     * Prepares a test of the getSource() method.
     */
    private void setUpSourceTest()
    {
        BaseHierarchicalConfiguration c1 = new BaseHierarchicalConfiguration();
        PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.addProperty(TEST_KEY, TEST_NAME);
        c2.addProperty("another.key", "test");
        config.addConfiguration(c1, CHILD1);
        config.addConfiguration(c2, CHILD2);
    }

    /**
     * Tests the gestSource() method when the source property is defined in a
     * hierarchical configuration.
     */
    @Test
    public void testGetSourceHierarchical()
    {
        setUpSourceTest();
        assertEquals("Wrong source configuration", config
                .getConfiguration(CHILD1), config.getSource(TEST_KEY));
    }

    /**
     * Tests whether the source configuration can be detected for non
     * hierarchical configurations.
     */
    @Test
    public void testGetSourceNonHierarchical()
    {
        setUpSourceTest();
        assertEquals("Wrong source configuration", config
                .getConfiguration(CHILD2), config.getSource("another.key"));
    }

    /**
     * Tests the getSource() method when the passed in key is not contained.
     * Result should be null in this case.
     */
    @Test
    public void testGetSourceUnknown()
    {
        setUpSourceTest();
        assertNull("Wrong result for unknown key", config
                .getSource("an.unknown.key"));
    }

    /**
     * Tests the getSource() method when a null key is passed in. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSourceNull()
    {
        config.getSource(null);
    }

    /**
     * Tests the getSource() method when the passed in key belongs to the
     * combined configuration itself.
     */
    @Test
    public void testGetSourceCombined()
    {
        setUpSourceTest();
        final String key = "yet.another.key";
        config.addProperty(key, Boolean.TRUE);
        assertEquals("Wrong source for key", config, config.getSource(key));
    }

    /**
     * Tests the getSource() method when the passed in key refers to multiple
     * values, which are all defined in the same source configuration.
     */
    @Test
    public void testGetSourceMulti()
    {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        assertEquals("Wrong source for multi-value property", config
                .getConfiguration(CHILD1), config.getSource(key));
    }

    /**
     * Tests the getSource() method when the passed in key refers to multiple
     * values defined by different sources. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSourceMultiSources()
    {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        config.getConfiguration(CHILD2).addProperty(key, "a,b,c");
        config.getSource(key);
    }

    /**
     * Tests whether escaped list delimiters are treated correctly.
     */
    @Test
    public void testEscapeListDelimiters()
    {
        PropertiesConfiguration sub = new PropertiesConfiguration();
        sub.addProperty("test.pi", "3\\,1415");
        config.addConfiguration(sub);
        assertEquals("Wrong value", "3,1415", config.getString("test.pi"));
    }

    /**
     * Tests whether an invalidate event is fired only after a change. This test
     * is related to CONFIGURATION-315.
     */
    @Test
    public void testInvalidateAfterChange()
    {
        ConfigurationEvent event = new ConfigurationEvent(config, 0, null,
                null, true);
        config.configurationChanged(event);
        assertEquals("Invalidate event fired", 0, listener.invalidateEvents);
        event = new ConfigurationEvent(config, 0, null, null, false);
        config.configurationChanged(event);
        assertEquals("No invalidate event fired", 1, listener.invalidateEvents);
    }

    /**
     * Tests using a conversion expression engine for child configurations with
     * strange keys. This test is related to CONFIGURATION-336.
     */
    @Test
    public void testConversionExpressionEngine()
    {
        PropertiesConfiguration child = new PropertiesConfiguration();
        child.addProperty("test(a)", "1,2,3");
        config.addConfiguration(child);
        DefaultExpressionEngine engineQuery = new DefaultExpressionEngine();
        engineQuery.setIndexStart("<");
        engineQuery.setIndexEnd(">");
        config.setExpressionEngine(engineQuery);
        DefaultExpressionEngine engineConvert = new DefaultExpressionEngine();
        engineConvert.setIndexStart("[");
        engineConvert.setIndexEnd("]");
        config.setConversionExpressionEngine(engineConvert);
        assertEquals("Wrong property 1", "1", config.getString("test(a)<0>"));
        assertEquals("Wrong property 2", "2", config.getString("test(a)<1>"));
        assertEquals("Wrong property 3", "3", config.getString("test(a)<2>"));
    }

    /**
     * Tests whether reload operations can cause a deadlock when the combined
     * configuration is accessed concurrently. This test is related to
     * CONFIGURATION-344.
     */
    @Test
    public void testDeadlockWithReload() throws ConfigurationException,
            InterruptedException
    {
        final PropertiesConfiguration child = new PropertiesConfiguration(
                "test.properties");
        child.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        config.addConfiguration(child);
        final int count = 1000;

        class TestDeadlockReloadThread extends Thread
        {
            boolean error = false;

            @Override
            public void run()
            {
                for (int i = 0; i < count && !error; i++)
                {
                    try
                    {
                        if (!child.getBoolean("configuration.loaded"))
                        {
                            error = true;
                        }
                    }
                    catch (NoSuchElementException nsex)
                    {
                        error = true;
                    }
                }
            }
        }

        TestDeadlockReloadThread reloadThread = new TestDeadlockReloadThread();
        reloadThread.start();
        for (int i = 0; i < count; i++)
        {
            assertEquals("Wrong value of combined property", 10, config
                    .getInt("test.integer"));
        }
        reloadThread.join();
        assertFalse("Failure in thread", reloadThread.error);
    }

    @Test
    public void testGetConfigurations() throws Exception
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        List<AbstractConfiguration> list = config.getConfigurations();
        assertNotNull("No list of configurations returned", list);
        assertTrue("Incorrect number of configurations", list.size() == 3);
        AbstractConfiguration c = list.get(2);
        assertTrue("Incorrect configuration", c == pc);
    }

    @Test
    public void testGetConfigurationNameList() throws Exception
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        List<String> list = config.getConfigurationNameList();
        assertNotNull("No list of configurations returned", list);
        assertTrue("Incorrect number of configurations", list.size() == 3);
        String name = list.get(1);
        assertNotNull("No name returned", name);
        assertTrue("Incorrect configuration name", TEST_NAME.equals(name));
    }

    /**
     * Tests whether changes on a sub node configuration that is part of a
     * combined configuration are detected. This test is related to
     * CONFIGURATION-368.
     */
    @Test
    public void testReloadWithSubNodeConfig() throws Exception
    {
        final String reloadContent = "<config><default><xmlReload1>{0}</xmlReload1></default></config>";
        config.setForceReloadCheck(true);
        config.setNodeCombiner(new OverrideCombiner());
        File testXmlFile1 = writeReloadFile(RELOAD_XML_NAME, reloadContent, 0);
        final String prefix1 = "default";
        XMLConfiguration c1 = new XMLConfiguration(testXmlFile1);
        SubnodeConfiguration sub1 = c1.configurationAt(prefix1, true);
        assertEquals("Inital test for sub config 1 failed", 0, sub1
                .getInt("xmlReload1"));
        config.addConfiguration(sub1);
        assertEquals(
                "Could not get value for sub config 1 from combined config", 0,
                config.getInt("xmlReload1"));
        c1.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        writeReloadFile(RELOAD_XML_NAME, reloadContent, 1);
        assertEquals("Reload of sub config 1 not detected", 1, config
                .getInt("xmlReload1"));
    }

    @Test
    public void testConcurrentGetAndReload() throws Exception
    {
        final int threadCount = 5;
        final int loopCount = 1000;
        config.setForceReloadCheck(true);
        config.setNodeCombiner(new MergeCombiner());
        final XMLConfiguration xml = new XMLConfiguration("configA.xml");
        xml.setReloadingStrategy(new FileRandomReloadingStrategy());
        config.addConfiguration(xml);
        final XMLConfiguration xml2 = new XMLConfiguration("configB.xml");
        xml2.setReloadingStrategy(new FileRandomReloadingStrategy());
        config.addConfiguration(xml2);
        config.setExpressionEngine(new XPathExpressionEngine());

        assertEquals(config.getString("/property[@name='config']/@value"), "100");

        Thread testThreads[] = new Thread[threadCount];
        int failures[] = new int[threadCount];

        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(config, failures, i, loopCount);
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        assertTrue(totalFailures + " failures Occurred", totalFailures == 0);
    }

    /**
     * Tests whether a combined configuration can be copied to an XML
     * configuration. This test is related to CONFIGURATION-445.
     */
    @Test
    public void testCombinedCopyToXML() throws ConfigurationException
    {
        XMLConfiguration x1 = new XMLConfiguration();
        x1.addProperty("key1", "value1");
        x1.addProperty("key1[@override]", "USER1");
        x1.addProperty("key2", "value2");
        x1.addProperty("key2[@override]", "USER2");
        XMLConfiguration x2 = new XMLConfiguration();
        x2.addProperty("key2", "value2.2");
        x2.addProperty("key2[@override]", "USER2");
        config.setNodeCombiner(new OverrideCombiner());
        config.addConfiguration(x2);
        config.addConfiguration(x1);
        XMLConfiguration x3 = new XMLConfiguration(config);
        assertEquals("Wrong element value", "value2.2", x3.getString("key2"));
        assertEquals("Wrong attribute value", "USER2",
                x3.getString("key2[@override]"));
        StringWriter w = new StringWriter();
        x3.save(w);
        String s = w.toString();
        x3 = new XMLConfiguration();
        x3.load(new StringReader(s));
        assertEquals("Wrong element value after load", "value2.2",
                x3.getString("key2"));
        assertEquals("Wrong attribute value after load", "USER2",
                x3.getString("key2[@override]"));
    }

    private class ReloadThread extends Thread
    {
        CombinedConfiguration combined;
        int[] failures;
        int index;
        int count;

        ReloadThread(CombinedConfiguration config, int[] failures, int index, int count)
        {
            combined = config;
            this.failures = failures;
            this.index = index;
            this.count = count;
        }
        @Override
        public void run()
        {
            failures[index] = 0;
            for (int i = 0; i < count; i++)
            {
                try
                {
                    String value = combined.getString("/property[@name='config']/@value");
                    if (value == null || !value.equals("100"))
                    {
                        ++failures[index];
                    }
                }
                catch (Exception ex)
                {
                    ++failures[index];
                }
            }
        }
    }

    /**
     * Helper method for writing a file. The file is also added to a list and
     * will be deleted in teadDown() automatically.
     *
     * @param file the file to be written
     * @param content the file's content
     * @throws IOException if an error occurs
     */
    private void writeFile(File file, String content) throws IOException
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileWriter(file));
            out.print(content);
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }

    /**
     * Helper method for writing a test file. The file will be created in the
     * test directory. It is also scheduled for automatic deletion after the
     * test.
     *
     * @param fileName the name of the test file
     * @param content the content of the file
     * @return the <code>File</code> object for the test file
     * @throws IOException if an error occurs
     */
    private File writeFile(String fileName, String content) throws IOException
    {
        File file = new File(folder.getRoot(), fileName);
        writeFile(file, content);
        return file;
    }

    /**
     * Writes a file for testing reload operations.
     *
     * @param name the name of the reload test file
     * @param content the content of the file
     * @param value the value of the reload test property
     * @return the file that was written
     * @throws IOException if an error occurs
     */
    private File writeReloadFile(String name, String content, int value)
            throws IOException
    {
        return writeFile(name, MessageFormat.format(content, new Object[] {
            new Integer(value)
        }));
    }

    /**
     * Helper method for creating a test configuration to be added to the
     * combined configuration.
     *
     * @return the test configuration
     */
    private AbstractConfiguration setUpTestConfiguration()
    {
        BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
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
