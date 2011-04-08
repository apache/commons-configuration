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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileAlwaysReloadingStrategy;
import org.apache.commons.configuration.reloading.FileRandomReloadingStrategy;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.apache.commons.configuration.tree.MergeCombiner;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

/**
 * Test class for CombinedConfiguration.
 *
 * @version $Id$
 */
public class TestCombinedConfiguration extends TestCase
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

    /** Constant for the directory for writing test files.*/
    private static final File TEST_DIR = new File("target");

    /** A list with files created during a test.*/
    private Collection testFiles;

    /** The configuration to be tested. */
    private CombinedConfiguration config;

    /** The test event listener. */
    private CombinedListener listener;

    protected void setUp() throws Exception
    {
        super.setUp();
        config = new CombinedConfiguration();
        listener = new CombinedListener();
        config.addConfigurationListener(listener);
    }

    /**
     * Performs clean-up after a test run. If test files have been created, they
     * are removed now.
     */
    protected void tearDown() throws Exception
    {
        if (testFiles != null)
        {
            for (Iterator it = testFiles.iterator(); it.hasNext();)
            {
                File f = (File) it.next();
                if (f.exists())
                {
                    assertTrue("Cannot delete test file: " + f, f.delete());
                }
            }
        }
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
        assertFalse("Force reload check flag is set", config.isForceReloadCheck());
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
        listener.checkEvent(2, 0);
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
     * Tests if file-based configurations can be reloaded.
     */
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
     * Prepares a test of the getSource() method.
     */
    private void setUpSourceTest()
    {
        HierarchicalConfiguration c1 = new HierarchicalConfiguration();
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
    public void testGetSourceNull()
    {
        try
        {
            config.getSource(null);
            fail("Could resolve source for null key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the getSource() method when the passed in key belongs to the
     * combined configuration itself.
     */
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
    public void testGetSourceMultiSources()
    {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        config.getConfiguration(CHILD2).addProperty(key, "a,b,c");
        try
        {
            config.getSource(key);
            fail("Multiple sources not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            //ok
        }
    }

    /**
     * Tests whether escaped list delimiters are treated correctly.
     */
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

    public void testGetConfigurations() throws Exception
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        List list = config.getConfigurations();
        assertNotNull("No list of configurations returned", list);
        assertTrue("Incorrect number of configurations", list.size() == 3);
        AbstractConfiguration c = ((AbstractConfiguration)list.get(2));
        assertTrue("Incorrect configuration", c == pc);
    }

    public void testGetConfigurationNameList() throws Exception
    {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        List list = config.getConfigurationNameList();
        assertNotNull("No list of configurations returned", list);
        assertTrue("Incorrect number of configurations", list.size() == 3);
        String name = ((String)list.get(1));
        assertNotNull("No name returned", name);
        assertTrue("Incorrect configuration name", TEST_NAME.equals(name));
    }

    /**
     * Tests whether changes on a sub node configuration that is part of a
     * combined configuration are detected. This test is related to
     * CONFIGURATION-368.
     */
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

            if (testFiles == null)
            {
                testFiles = new ArrayList();
            }
            testFiles.add(file);
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
        File file = new File(TEST_DIR, fileName);
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
