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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration.builder.fluent.Parameters;
import org.apache.commons.configuration.reloading.FileAlwaysReloadingStrategy;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestFileConfiguration
{
    /** Constant for the name of a test file.*/
    private static final String TEST_FILENAME = "test.properties";

    /** Constant for a test file.*/
    private static final File TEST_FILE = ConfigurationAssert.getTestFile(TEST_FILENAME);

    /** Constant for a test output file. */
    private static final File OUT_FILE = new File(
            "target/test-resources/foo/bar/test.properties");

    /** Constant for the name of a resource to be resolved.*/
    private static final String RESOURCE_NAME = "config/deep/deeptest.properties";

    /** Helper object for managing temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Initializes the test environment. This implementation ensures that the
     * test output file does not exist.
     */
    @Before
    public void setUp() throws Exception
    {
        removeOutFile();
    }

    /**
     * Performs cleanup after a test case. This implementation removes temporary
     * files that have been created.
     */
    protected void tearDown() throws Exception
    {
        removeOutFile();
    }

    /**
     * Removes a file if it exists.
     *
     * @param file the file to be removed
     */
    private static void removeFile(File file)
    {
        if (file.exists())
        {
            assertTrue("Cannot remove file: " + file, file.delete());
        }
    }

    /**
     * Removes the test output file if it exists. Its parent directories are
     * also removed.
     */
    private static void removeOutFile()
    {
        removeFile(OUT_FILE);
        File parent = OUT_FILE.getParentFile();
        removeFile(parent);
        parent = parent.getParentFile();
        removeFile(parent);
    }

    @Test
    public void testSetURL() throws Exception
    {
        // http URL
        FileConfiguration config = new PropertiesConfiguration();
        config.setURL(new URL("http://commons.apache.org/configuration/index.html"));

        assertEquals("base path", "http://commons.apache.org/configuration/", config.getBasePath());
        assertEquals("file name", "index.html", config.getFileName());

        // file URL - This url is invalid, a valid url would be file:///temp/test.properties.
        config.setURL(new URL("file:/temp/test.properties"));
        assertEquals("base path", "file:///temp/", config.getBasePath());
        assertEquals("file name", "test.properties", config.getFileName());
    }

    @Test
    public void testSetURLWithParams() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        URL url = new URL("http://issues.apache.org/bugzilla/show_bug.cgi?id=37886");
        config.setURL(url);
        assertEquals("Base path incorrect", "http://issues.apache.org/bugzilla/", config.getBasePath());
        assertEquals("File name incorrect", "show_bug.cgi", config.getFileName());
        assertEquals("URL was not correctly stored", url, config.getURL());
    }

    @Test
    public void testLocations() throws Exception
    {
        PropertiesConfiguration config = new PropertiesConfiguration();

        File directory = ConfigurationAssert.TEST_DIR;
        File file = TEST_FILE;
        config.setFile(file);
        assertEquals(directory.getAbsolutePath(), config.getBasePath());
        assertEquals(TEST_FILENAME, config.getFileName());
        assertEquals(file.getAbsolutePath(), config.getPath());

        config.setPath(ConfigurationAssert.TEST_DIR_NAME + File.separator + TEST_FILENAME);
        assertEquals(TEST_FILENAME, config.getFileName());
        assertEquals(directory.getAbsolutePath(), config.getBasePath());
        assertEquals(file.getAbsolutePath(), config.getPath());
        assertEquals(file.toURI().toURL(), config.getURL());

        config.setBasePath(null);
        config.setFileName(TEST_FILENAME);
        assertNull(config.getBasePath());
        assertEquals(TEST_FILENAME, config.getFileName());
    }

    @Test
    public void testCreateFile1() throws Exception
    {
        assertFalse("The file should not exist", OUT_FILE.exists());

        FileConfiguration config = new PropertiesConfiguration();
        config.setFile(OUT_FILE);
        config.save();

        assertTrue("The file doesn't exist", OUT_FILE.exists());
    }

    @Test
    public void testCreateFile2() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.setFile(OUT_FILE);
        config.save();

        assertTrue("The file doesn't exist", OUT_FILE.exists());
    }

    @Test
    public void testCreateFile3() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.save(OUT_FILE);

        assertTrue("The file doesn't exist", OUT_FILE.exists());
    }

    /**
     * Tests collaboration with ConfigurationFactory: Is the base path set on
     * loading is valid in file based configurations?
     *
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testWithConfigurationFactory() throws Exception
    {
        File file = folder.newFile();

        ConfigurationFactory factory = new ConfigurationFactory();
        factory.setConfigurationURL(ConfigurationAssert.getTestURL(
                "testDigesterConfiguration2.xml"));
        CompositeConfiguration cc =
                (CompositeConfiguration) factory.getConfiguration();
        PropertiesConfiguration config = null;
        for (int i = 0; config == null; i++)
        {
            if (cc.getConfiguration(i) instanceof PropertiesConfiguration)
            {
                config = (PropertiesConfiguration) cc.getConfiguration(i);
            }
        }

        config.setProperty("test", "yes");
        config.save(file);
        assertTrue(file.exists());
        config = new PropertiesConfiguration();
        config.setFile(file);
        config.load();

        assertEquals("yes", config.getProperty("test"));
        assertEquals("masterOfPost", config.getProperty("mail.account.user"));
    }

    /**
     * Tests if invalid URLs cause an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testSaveInvalidURL() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.save(new URL("http://jakarta.apache.org/test.properties"));
    }

    /**
     * Tests if an invalid URL string causes an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testSaveInvalidURLString() throws ConfigurationException
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.save("http://www.apache.org/test.properties");
    }

    /**
     * Tests if the URL used by the load() method is also used by save().
     */
    @Test
    public void testFileOverwrite() throws Exception
    {
        FileOutputStream out = null;
        FileInputStream in = null;
        File tempFile = null;
        try
        {
            tempFile = folder.newFile();
            Properties props = new Properties();
            props.setProperty("1", "one");
            out = new FileOutputStream(tempFile);
            props.store(out, "TestFileOverwrite");
            out.close();
            out = null;
            FileConfiguration config = new PropertiesConfiguration();
            config.setFile(tempFile);
            config.load();
            String value = config.getString("1");
            assertTrue("one".equals(value));
            config.setProperty("1", "two");
            config.save();
            props = new Properties();
            in = new FileInputStream(tempFile);
            props.load(in);
            String value2 = props.getProperty("1");
            assertTrue("two".equals(value2));
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioex)
                {
                    ioex.printStackTrace();
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioex)
                {
                    ioex.printStackTrace();
                }
            }
        }
    }

    /**
     * Tests setting a file changed reloading strategy together with the auto
     * save feature.
     */
    @Test
    public void testReloadingWithAutoSave() throws Exception
    {
        File configFile = folder.newFile();
        PrintWriter out = null;

        try
        {
            out = new PrintWriter(new FileWriter(configFile));
            out.println("a = one");
            out.close();
            out = null;

            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setFile(configFile);
            config.load();
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setAutoSave(true);

            assertEquals("one", config.getProperty("a"));
            config.setProperty("b", "two");
            assertEquals("one", config.getProperty("a"));
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
     * Tests loading and saving a configuration file with a complicated path
     * name including spaces. (related to issue 35210)
     */
    @Test
    public void testPathWithSpaces() throws Exception
    {
        File path = folder.newFolder("path with spaces");
        File confFile = new File(path, "config-test.properties");
        PrintWriter out = null;

        try
        {
            out = new PrintWriter(new FileWriter(confFile));
            out.println("saved = false");
            out.close();
            out = null;

            URL url = confFile.toURI().toURL();
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setURL(url);
            config.load();
            assertFalse(config.getBoolean("saved"));

            config.setProperty("saved", Boolean.TRUE);
            config.save();
            config = new PropertiesConfiguration();
            config.setFile(confFile);
            config.load();
            assertTrue(config.getBoolean("saved"));
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
     * Tests whether file names containing a "+" character are handled
     * correctly. This test is related to CONFIGURATION-415.
     */
    @Test
    public void testPathWithPlus() throws ConfigurationException, IOException
    {
        File saveFile = folder.newFile("test+config.properties");
        FileConfiguration config = new PropertiesConfiguration();
        config.setFile(saveFile);
        config.addProperty("test", Boolean.TRUE);
        config.save();
        File configFile = config.getFile();
        assertEquals("Wrong configuration file", saveFile, configFile);
    }

    /**
     * Tests the getFile() method.
     */
    @Test
    public void testGetFile() throws ConfigurationException
    {
        FileConfiguration config = new PropertiesConfiguration();
        assertNull(config.getFile());
        File file = TEST_FILE.getAbsoluteFile();
        config.setFile(file);
        assertEquals(file, config.getFile());
        config.load();
        assertEquals(file, config.getFile());
    }

    /**
     * Tests whether getFile() returns a valid file after a configuration has
     * been loaded.
     */
    @Test
    public void testGetFileAfterLoad() throws ConfigurationException,
            IOException
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.load(TEST_FILE.getAbsolutePath());
        assertNotNull("No source URL set", config.getURL());
        assertEquals("Wrong source file", TEST_FILE.getCanonicalFile(), config
                .getFile().getCanonicalFile());
    }

    /**
     * Tests whether calling load() multiple times changes the source. This
     * should not be the case.
     */
    @Test
    public void testLoadMultiple() throws ConfigurationException
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.load(TEST_FILE.getAbsolutePath());
        URL srcUrl = config.getURL();
        File srcFile = config.getFile();
        File file2 = ConfigurationAssert.getTestFile("testEqual.properties");
        config.load(file2.getAbsolutePath());
        assertEquals("Source URL was changed", srcUrl, config.getURL());
        assertEquals("Source file was changed", srcFile, config.getFile());
    }

    @Test(expected = ConfigurationException.class)
    public void testSaveWithoutFileNameFile() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.load(TEST_FILE);
        config.save();
    }

    @Test(expected = ConfigurationException.class)
    public void testSaveWithoutFileNameURL() throws Exception
    {
        FileConfiguration config = new PropertiesConfiguration();
        config.load(TEST_FILE.toURI().toURL());
        config.save();
    }

    /**
     * Checks that loading a directory instead of a file throws an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadDirectoryString() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.load("target");
    }

    /**
     * Tests that it is not possible to load a directory using the load() method
     * which expects a File.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadDirectoryFile() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.load(new File("target"));
    }

    /**
     * Tests that it is not possible to load a directory using the String constructor.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadDirectoryConstrString() throws ConfigurationException
    {
        new PropertiesConfiguration("target");
    }

    /**
     * Tests that it is not possible to load a directory using the File constructor.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadDirectoryConstrFile() throws ConfigurationException
    {
        new PropertiesConfiguration(new File("target"));
    }

    /**
     * Tests whether the constructor behaves the same as setFileName() when the
     * configuration source is in the classpath.
     */
    @Test
    public void testInitFromClassPath() throws ConfigurationException
    {
        PropertiesConfiguration config1 = new PropertiesConfiguration();
        config1.setFileName(RESOURCE_NAME);
        config1.load();
        PropertiesConfiguration config2 = new PropertiesConfiguration();
        config2.load(RESOURCE_NAME);
        compare(config1, config2);
    }

    /**
     * Tests the loading of configuration file in a Combined configuration
     * when the configuration source is in the classpath.
     */
    @Test
    public void testLoadFromClassPath() throws ConfigurationException
    {
        CombinedConfigurationBuilder builder =
                new CombinedConfigurationBuilder();
        builder.configure(Parameters.combined().setDefinitionBuilderParameters(
                Parameters.fileBased().setFileName(
                        "config/deep/testFileFromClasspath.xml")));
        CombinedConfiguration config = builder.getConfiguration();
        Configuration config1 = config.getConfiguration("propConf");
        Configuration config2 = config.getConfiguration("propConfDeep");
        compare(config1, config2);
    }

    /**
     * Tests cloning a file based configuration.
     */
    @Test
    public void testClone() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(RESOURCE_NAME);
        config.load();
        PropertiesConfiguration copy = (PropertiesConfiguration) config.clone();
        compare(config, copy);
        assertNull("URL was not reset", copy.getURL());
        assertNull("Base path was not reset", copy.getBasePath());
        assertNull("File name was not reset", copy.getFileName());
        assertNotSame("Reloading strategy was not reset", config
                .getReloadingStrategy(), copy.getReloadingStrategy());
    }

    /**
     * Tests whether an error log listener was registered at the configuration.
     */
    @Test
    public void testLogErrorListener()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        assertEquals("No error log listener registered", 1, config
                .getErrorListeners().size());
    }

    /**
     * Tests handling of errors in the reload() method.
     */
    @Test
    public void testReloadError() throws ConfigurationException
    {
        ConfigurationErrorListenerImpl l = new ConfigurationErrorListenerImpl();
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(RESOURCE_NAME);
        config.load();
        config.clearErrorListeners();
        config.addErrorListener(l);
        config.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        config.getString("test");
        config.setFileName("Not existing file");
        config.getString("test");
        l.verify(AbstractFileConfiguration.EVENT_RELOAD, null, null);
        assertNotNull("Exception is not set", l.getLastEvent().getCause());
    }

    /**
     * Tests iterating over the keys of a non hierarchical file-based
     * configuration while a reload happens. This test is related to
     * CONFIGURATION-347.
     */
    @Test
    public void testIterationWithReloadFlat() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFile(TEST_FILE);
        config.load();
        checkIterationWithReload(config);
    }

    /**
     * Tests iterating over the keys of a hierarchical file-based configuration
     * while a reload happens. This test is related to CONFIGURATION-347.
     */
    @Test
    public void testIterationWithReloadHierarchical()
            throws ConfigurationException
    {
        XMLConfiguration config = new XMLConfiguration("test.xml");
        checkIterationWithReload(config);
    }

    /**
     * Tests whether a configuration can be refreshed.
     */
    @Test
    public void testRefresh() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFile(TEST_FILE);
        config.load();
        assertEquals("Wrong value", 10, config.getInt("test.integer"));
        config.setProperty("test.integer", new Integer(42));
        assertEquals("Wrong value after update", 42,
                config.getInt("test.integer"));
        config.refresh();
        assertEquals("Wrong value after refresh", 10,
                config.getInt("test.integer"));
    }

    /**
     * Tests refresh if the configuration is not associated with a file.
     */
    @Test(expected = ConfigurationException.class)
    public void testRefreshNoFile() throws ConfigurationException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.refresh();
    }

    /**
     * Helper method for testing an iteration over the keys of a file-based
     * configuration while a reload happens.
     *
     * @param config the configuration to test
     */
    private void checkIterationWithReload(FileConfiguration config)
    {
        config.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            String key = it.next();
            assertNotNull("No value for key " + key, config.getProperty(key));
        }
    }

    /**
     * Helper method for comparing the content of two configuration objects.
     *
     * @param config1 the first configuration
     * @param config2 the second configuration
     */
    private void compare(Configuration config1, Configuration config2)
    {
        StrictConfigurationComparator cc = new StrictConfigurationComparator();
        assertTrue("Configurations are different", cc.compare(config1, config2));
    }
}