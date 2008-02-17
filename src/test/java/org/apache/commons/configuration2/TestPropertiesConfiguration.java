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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.reloading.FileChangedReloadingStrategy;

import junit.framework.TestCase;

/**
 * Test for loading and saving properties files.
 *
 * @version $Id$
 */
public class TestPropertiesConfiguration extends TestCase
{
    private PropertiesConfiguration conf;

    /** The File that we test with */
    private String testProperties = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();

    private String testBasePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();
    private String testBasePath2 = ConfigurationAssert.TEST_DIR.getAbsoluteFile().getParentFile().getAbsolutePath();
    private File testSavePropertiesFile = ConfigurationAssert.getOutFile("testsave.properties");

    protected void setUp() throws Exception
    {
        conf = new PropertiesConfiguration(testProperties);

        // remove the test save file if it exists
        if (testSavePropertiesFile.exists())
        {
            assertTrue("Test output file could not be deleted",
                    testSavePropertiesFile.delete());
        }
    }

    public void testLoad() throws Exception
    {
        String loaded = conf.getString("configuration.loaded");
        assertEquals("true", loaded);
    }

    /**
     * Tests if properties can be appended by simply calling load() another
     * time.
     */
    public void testAppend() throws Exception
    {
        File file2 = ConfigurationAssert.getTestFile("threesome.properties");
        conf.load(file2);
        assertEquals("aaa", conf.getString("test.threesome.one"));
        assertEquals("true", conf.getString("configuration.loaded"));
    }

    /**
     * Tests that empty properties are treated as the empty string
     * (rather than as null).
     */
    public void testEmpty() throws Exception
    {
        String empty = conf.getString("test.empty");
        assertNotNull(empty);
        assertEquals("", empty);
    }

    /**
     * Tests that references to other properties work
     */
    public void testReference() throws Exception
    {
        assertEquals("baseextra", conf.getString("base.reference"));
    }

    /**
     * test if includes properties get loaded too
     */
    public void testLoadInclude() throws Exception
    {
        String loaded = conf.getString("include.loaded");
        assertEquals("true", loaded);
    }

    public void testSetInclude() throws Exception
    {
        // change the include key
        PropertiesConfiguration.setInclude("import");

        // load the configuration
        PropertiesConfiguration conf = new PropertiesConfiguration();
        conf.load(testProperties);

        // restore the previous value for the other tests
        PropertiesConfiguration.setInclude("include");

        assertNull(conf.getString("include.loaded"));
    }

    /**
     * Tests <code>List</code> parsing.
     */
    public void testList() throws Exception
    {
        List packages = conf.getList("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    public void testSave() throws Exception
    {
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        List list = new ArrayList();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        String filename = testSavePropertiesFile.getAbsolutePath();
        conf.save(filename);

        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // read the configuration and compare the properties
        PropertiesConfiguration checkConfig = new PropertiesConfiguration(filename);
        ConfigurationAssert.assertEquals(conf, checkConfig);

        // Save it again, verifing a save with a filename works.
        checkConfig.save();
    }

    public void testSaveToCustomURL() throws Exception
    {
        // save the configuration to a custom URL
        URL url = new URL("foo", "", 0, "./target/testsave-custom-url.properties", new FileURLStreamHandler());
        conf.save(url);

        // reload the configuration
        Configuration config2 = new PropertiesConfiguration(url);
        assertEquals("true", config2.getString("configuration.loaded"));
    }

    public void testInMemoryCreatedSave() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        // add an array of strings to the configuration
        pc.addProperty("string", "value1");
        List list = new ArrayList();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        pc.addProperty("array", list);

        // save the configuration
        String filename = testSavePropertiesFile.getAbsolutePath();
        pc.save(filename);

        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // read the configuration and compare the properties
        PropertiesConfiguration checkConfig = new PropertiesConfiguration(filename);
        ConfigurationAssert.assertEquals(pc, checkConfig);

        // Save it again, verifing a save with a filename works.
        checkConfig.save();
    }

    /**
     * Tests saving a configuration when delimiter parsing is disabled.
     */
    public void testSaveWithDelimiterParsingDisabled() throws ConfigurationException
    {
        conf.clear();
        conf.setDelimiterParsingDisabled(true);
        conf.addProperty("test.list", "a,b,c");
        conf.addProperty("test.dirs", "C:\\Temp\\,D:\\Data\\");
        conf.save(testSavePropertiesFile);

        PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setDelimiterParsingDisabled(true);
        checkConfig.setFile(testSavePropertiesFile);
        checkConfig.load();
        ConfigurationAssert.assertEquals(conf, checkConfig);
    }

    public void testSaveMissingFilename()
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        try
        {
            pc.save();
            fail("Should have throw ConfigurationException");
        }
        catch (ConfigurationException ce)
        {
            //good
        }
    }

    /**
     * Tests if the base path is taken into account by the save() method.
     * @throws Exception if an error occurs
     */
    public void testSaveWithBasePath() throws Exception
    {
        conf.setProperty("test", "true");
        conf.setBasePath(testSavePropertiesFile.getParentFile().toURL().toString());
        conf.setFileName(testSavePropertiesFile.getName());
        conf.save();
        assertTrue(testSavePropertiesFile.exists());
    }

    /**
     * Tests whether the escape character for list delimiters can be itself
     * escaped and survives a save operation.
     */
    public void testSaveEscapedEscapingCharacter()
            throws ConfigurationException
    {
        conf.addProperty("test.dirs", "C:\\Temp\\\\,D:\\Data\\\\,E:\\Test\\");
        List dirs = conf.getList("test.dirs");
        assertEquals("Wrong number of list elements", 3, dirs.size());
        conf.save(testSavePropertiesFile);

        PropertiesConfiguration checkConfig = new PropertiesConfiguration(
                testSavePropertiesFile);
        ConfigurationAssert.assertEquals(conf, checkConfig);
    }

    public void testLoadViaProperty() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setFileName(testProperties);
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath);
        pc.setFileName("test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath2() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName(testProperties);
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));

        pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName(testProperties);
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadFromFile() throws Exception
    {
        File file = ConfigurationAssert.getTestFile("test.properties");
        conf = new PropertiesConfiguration(file);

        assertEquals("true", conf.getString("configuration.loaded"));
    }

    public void testLoadUnexistingFile()
    {
        try
        {
            conf = new PropertiesConfiguration("Unexisting file");
            fail("Unexisting file was loaded.");
        }
        catch(ConfigurationException cex)
        {
            // fine
        }
    }

    /**
     * Tests to load a file with enabled auto save mode.
     */
    public void testLoadWithAutoSave() throws Exception
    {
        setUpSavedProperties();
    }

    /**
     * Tests the auto save functionality when an existing property is modified.
     */
    public void testLoadWithAutoSaveAndSetExisting() throws Exception
    {
        setUpSavedProperties();
        conf.setProperty("a", "moreThanOne");
        checkSavedConfig();
    }

    /**
     * Tests the auto save functionality when a new property is added using the
     * setProperty() method.
     */
    public void testLoadWithAutoSaveAndSetNew() throws Exception
    {
        setUpSavedProperties();
        conf.setProperty("d", "four");
        checkSavedConfig();
    }

    /**
     * Tests the auto save functionality when a new property is added using the
     * addProperty() method.
     */
    public void testLoadWithAutoSaveAndAdd() throws Exception
    {
        setUpSavedProperties();
        conf.addProperty("d", "four");
        checkSavedConfig();
    }

    /**
     * Tests the auto save functionality when a property is removed.
     */
    public void testLoadWithAutoSaveAndClear() throws Exception
    {
        setUpSavedProperties();
        conf.clearProperty("c");
        PropertiesConfiguration checkConfig = checkSavedConfig();
        assertFalse("The saved configuration contain the key '" + "c" + "'", checkConfig.containsKey("c"));
    }

    /**
     * Creates a properties file on disk. Used for testing load and save
     * operations.
     *
     * @throws IOException if an I/O error occurs
     */
    private void setUpSavedProperties() throws IOException, ConfigurationException
    {
        PrintWriter out = null;

        try
        {
            out = new PrintWriter(new FileWriter(testSavePropertiesFile));
            out.println("a = one");
            out.println("b = two");
            out.println("c = three");
            out.close();
            out = null;

            conf = new PropertiesConfiguration();
            conf.setAutoSave(true);
            conf.setFile(testSavePropertiesFile);
            conf.load();
            assertEquals("one", conf.getString("a"));
            assertEquals("two", conf.getString("b"));
            assertEquals("three", conf.getString("c"));
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
     * Helper method for testing a saved configuration. Reads in the file using
     * a new instance and compares this instance with the original one.
     *
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    private PropertiesConfiguration checkSavedConfig()
            throws ConfigurationException
    {
        PropertiesConfiguration checkConfig = new PropertiesConfiguration(testSavePropertiesFile);
        for (Iterator i = conf.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));
            assertEquals("Value of the '" + key + "' property", conf.getProperty(key), checkConfig.getProperty(key));
        }
        return checkConfig;
    }

    public void testGetStringWithEscapedChars()
    {
        String property = conf.getString("test.unescape");
        assertEquals("String with escaped characters", "This \n string \t contains \" escaped \\ characters", property);
    }

    public void testGetStringWithEscapedComma()
    {
        String property = conf.getString("test.unescape.list-separator");
        assertEquals("String with an escaped list separator", "This string contains , an escaped list separator", property);
    }

    public void testUnescapeJava()
    {
        assertEquals("test\\,test", PropertiesConfiguration.unescapeJava("test\\,test", ','));
    }

    public void testEscapedKey() throws Exception
    {
        PropertiesConfiguration conf = new PropertiesConfiguration();
        conf.load(new StringReader("\\u0066\\u006f\\u006f=bar"));

        assertEquals("value of the 'foo' property", "bar", conf.getString("foo"));
    }

    public void testMixedArray()
    {
        String[] array = conf.getStringArray("test.mixed.array");

        assertEquals("array length", 4, array.length);
        assertEquals("1st element", "a", array[0]);
        assertEquals("2nd element", "b", array[1]);
        assertEquals("3rd element", "c", array[2]);
        assertEquals("4th element", "d", array[3]);
    }

    public void testMultilines()
    {
        String property = "This is a value spread out across several adjacent "
                + "natural lines by escaping the line terminator with "
                + "a backslash character.";

        assertEquals("'test.multilines' property", property, conf.getString("test.multilines"));
    }

    public void testChangingDefaultListDelimiter() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration(testProperties);
        assertEquals(4, pc.getList("test.mixed.array").size());

        char delimiter = PropertiesConfiguration.getDefaultListDelimiter();
        PropertiesConfiguration.setDefaultListDelimiter('^');
        pc = new PropertiesConfiguration(testProperties);
        assertEquals(2, pc.getList("test.mixed.array").size());
        PropertiesConfiguration.setDefaultListDelimiter(delimiter);
    }

    public void testChangingListDelimiter() throws Exception
    {
        PropertiesConfiguration pc1 = new PropertiesConfiguration(testProperties);
        assertEquals(4, pc1.getList("test.mixed.array").size());

        PropertiesConfiguration pc2 = new PropertiesConfiguration();
        pc2.setListDelimiter('^');
        pc2.setFileName(testProperties);
        pc2.load();
        assertEquals("Should obtain the first value", "a", pc2.getString("test.mixed.array"));
        assertEquals(2, pc2.getList("test.mixed.array").size());
    }

    public void testDisableListDelimiter() throws Exception
    {
        PropertiesConfiguration pc1 = new PropertiesConfiguration(testProperties);
        assertEquals(4, pc1.getList("test.mixed.array").size());

        PropertiesConfiguration pc2 = new PropertiesConfiguration();
        pc2.setDelimiterParsingDisabled(true);
        pc2.setFileName(testProperties);
        pc2.load();
        assertEquals(2, pc2.getList("test.mixed.array").size());
    }

    /**
     * Tests escaping of an end of line with a backslash.
     */
    public void testNewLineEscaping()
    {
        List list = conf.getList("test.path");
        assertEquals(3, list.size());
        assertEquals("C:\\path1\\", list.get(0));
        assertEquals("C:\\path2\\", list.get(1));
        assertEquals("C:\\path3\\complex\\test\\", list.get(2));
    }

    /**
     * Tests if included files are loaded when the source lies in the class path.
     */
    public void testLoadIncludeFromClassPath() throws ConfigurationException
    {
        conf = new PropertiesConfiguration("test.properties");
        assertEquals("true", conf.getString("include.loaded"));
    }

    /**
     * Test if the lines starting with # or ! are properly ignored.
     */
    public void testComment() {
        assertFalse("comment line starting with '#' parsed as a property", conf.containsKey("#comment"));
        assertFalse("comment line starting with '!' parsed as a property", conf.containsKey("!comment"));
    }

    /**
     * Check that key/value separators can be part of a key.
     */
    public void testEscapedKeyValueSeparator()
    {
        assertEquals("Escaped separator '=' not supported in keys", "foo", conf.getProperty("test.separator=in.key"));
        assertEquals("Escaped separator ':' not supported in keys", "bar", conf.getProperty("test.separator:in.key"));
        assertEquals("Escaped separator '\\t' not supported in keys", "foo", conf.getProperty("test.separator\tin.key"));
        assertEquals("Escaped separator '\\f' not supported in keys", "bar", conf.getProperty("test.separator\fin.key"));
        assertEquals("Escaped separator ' ' not supported in keys"  , "foo", conf.getProperty("test.separator in.key"));
    }

    /**
     * Test all acceptable key/value separators ('=', ':' or white spaces).
     */
    public void testKeyValueSeparators() {
        assertEquals("equal separator not properly parsed",      "foo", conf.getProperty("test.separator.equal"));
        assertEquals("colon separator not properly parsed",      "foo", conf.getProperty("test.separator.colon"));
        assertEquals("tab separator not properly parsed",        "foo", conf.getProperty("test.separator.tab"));
        assertEquals("formfeed separator not properly parsed",   "foo", conf.getProperty("test.separator.formfeed"));
        assertEquals("whitespace separator not properly parsed", "foo", conf.getProperty("test.separator.whitespace"));
    }

    /**
     * Tests including properties when they are loaded from a nested directory
     * structure.
     */
    public void testIncludeInSubDir() throws ConfigurationException
    {
        ConfigurationFactory factory = new ConfigurationFactory("conf/testFactoryPropertiesInclude.xml");
        Configuration config = factory.getConfiguration();
        assertEquals(true, config.getBoolean("deeptest"));
        assertEquals(true, config.getBoolean("deepinclude"));
        assertFalse(config.containsKey("deeptestinvalid"));
    }

    /**
     * Tests whether the correct line separator is used.
     */
    public void testLineSeparator() throws ConfigurationException
    {
        final String EOL = System.getProperty("line.separator");
        conf = new PropertiesConfiguration();
        conf.setHeader("My header");
        conf.setProperty("prop", "value");

        StringWriter out = new StringWriter();
        conf.save(out);
        String content = out.toString();
        assertTrue("Header could not be found", content.indexOf("# My header"
                + EOL + EOL) == 0);
        assertTrue("Property could not be found", content.indexOf("prop = value" + EOL) > 0);
    }

    /**
     * Tests what happens if a reloading strategy's <code>reloadingRequired()</code>
     * implementation accesses methods of the configuration that in turn cause a reload.
     */
    public void testReentrantReload()
    {
        conf.setProperty("shouldReload", Boolean.FALSE);
        conf.setReloadingStrategy(new FileChangedReloadingStrategy()
        {
            public boolean reloadingRequired()
            {
                return configuration.getBoolean("shouldReload");
            }
        });
        assertFalse("Property has wrong value", conf.getBoolean("shouldReload"));
    }

    /**
     * Tests accessing the layout object.
     */
    public void testGetLayout()
    {
        PropertiesConfigurationLayout layout = conf.getLayout();
        assertNotNull("Layout is null", layout);
        assertSame("Different object returned", layout, conf.getLayout());
        conf.setLayout(null);
        PropertiesConfigurationLayout layout2 = conf.getLayout();
        assertNotNull("Layout 2 is null", layout2);
        assertNotSame("Same object returned", layout, layout2);
    }

    /**
     * Tests the propertyLoaded() method for a simple property.
     */
    public void testPropertyLoaded() throws ConfigurationException
    {
        DummyLayout layout = new DummyLayout(conf);
        conf.setLayout(layout);
        conf.propertyLoaded("layoutLoadedProperty", "yes");
        assertEquals("Layout's load() was called", 0, layout.loadCalls);
        assertEquals("Property not added", "yes", conf.getString("layoutLoadedProperty"));
    }

    /**
     * Tests the propertyLoaded() method for an include property.
     */
    public void testPropertyLoadedInclude() throws ConfigurationException
    {
        DummyLayout layout = new DummyLayout(conf);
        conf.setLayout(layout);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClasspath.properties,testEqual.properties");
        assertEquals("Layout's load() was not correctly called", 2, layout.loadCalls);
        assertFalse("Property was added", conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests propertyLoaded() for an include property, when includes are
     * disabled.
     */
    public void testPropertyLoadedIncludeNotAllowed() throws ConfigurationException
    {
        DummyLayout layout = new DummyLayout(conf);
        conf.setLayout(layout);
        conf.setIncludesAllowed(false);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClassPath.properties,testEqual.properties");
        assertEquals("Layout's load() was called", 0, layout.loadCalls);
        assertFalse("Property was added", conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests whether comment lines are correctly detected.
     */
    public void testIsCommentLine()
    {
        assertTrue("Comment not detected", PropertiesConfiguration.isCommentLine("# a comment"));
        assertTrue("Alternative comment not detected", PropertiesConfiguration.isCommentLine("! a comment"));
        assertTrue("Comment with no space not detected", PropertiesConfiguration.isCommentLine("#a comment"));
        assertTrue("Comment with leading space not detected", PropertiesConfiguration.isCommentLine("    ! a comment"));
        assertFalse("Wrong comment", PropertiesConfiguration.isCommentLine("   a#comment"));
    }

    /**
     * Tests whether a properties configuration can be successfully cloned. It
     * is especially checked whether the layout object is taken into account.
     */
    public void testClone() throws ConfigurationException
    {
        PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame("Copy has same layout object", conf.getLayout(), copy.getLayout());
        assertEquals("Wrong number of event listeners for original", 1, conf.getConfigurationListeners().size());
        assertEquals("Wrong number of event listeners for clone", 1, copy.getConfigurationListeners().size());
        assertSame("Wrong event listener for original", conf.getLayout(), conf.getConfigurationListeners().iterator().next());
        assertSame("Wrong event listener for clone", copy.getLayout(), copy.getConfigurationListeners().iterator().next());
        StringWriter outConf = new StringWriter();
        conf.save(outConf);
        StringWriter outCopy = new StringWriter();
        copy.save(outCopy);
        assertEquals("Output from copy is different", outConf.toString(), outCopy.toString());
    }

    /**
     * Tests the clone() method when no layout object exists yet.
     */
    public void testCloneNullLayout()
    {
        conf = new PropertiesConfiguration();
        PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame("Layout objects are the same", conf.getLayout(), copy.getLayout());
    }

    /**
     * Tests saving a file-based configuration to a HTTP server.
     */
    public void testSaveToHTTPServerSuccess() throws Exception
    {
        MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(
                HttpURLConnection.HTTP_OK, testSavePropertiesFile);
        URL url = new URL(null, "http://jakarta.apache.org", handler);
        conf.save(url);
        MockHttpURLConnection con = handler.getMockConnection();
        assertTrue("Wrong output flag", con.getDoOutput());
        assertEquals("Wrong method", "PUT", con.getRequestMethod());

        PropertiesConfiguration checkConfig = new PropertiesConfiguration(
                testSavePropertiesFile);
        ConfigurationAssert.assertEquals(conf, checkConfig);
    }

    /**
     * Tests saving a file-based configuration to a HTTP server when the server
     * reports a failure. This should cause an exception.
     */
    public void testSaveToHTTPServerFail() throws Exception
    {
        MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(
                HttpURLConnection.HTTP_BAD_REQUEST, testSavePropertiesFile);
        URL url = new URL(null, "http://jakarta.apache.org", handler);
        try
        {
            conf.save(url);
            fail("Response code was not checked!");
        }
        catch (ConfigurationException cex)
        {
            assertTrue("Wrong root cause: " + cex,
                    cex.getCause() instanceof IOException);
        }
    }

    /**
     * Test the creation of a file containing a '#' in its name.
     */
    public void testFileWithSharpSymbol() throws Exception
    {
        File file = new File("target/sharp#1.properties");
        file.createNewFile();

        PropertiesConfiguration conf = new PropertiesConfiguration(file);
        conf.save();

        assertTrue("Missing file " + file, file.exists());
    }

    /**
     * Tests initializing a properties configuration from a non existing file.
     * There was a bug, which caused properties getting lost when later save()
     * is called.
     */
    public void testInitFromNonExistingFile() throws ConfigurationException
    {
        final String testProperty = "test.successfull";
        conf = new PropertiesConfiguration(testSavePropertiesFile);
        conf.addProperty(testProperty, Boolean.TRUE);
        conf.save();
        PropertiesConfiguration checkConfig = new PropertiesConfiguration(
                testSavePropertiesFile);
        assertTrue("Test property not found", checkConfig
                .getBoolean(testProperty));
    }

    /**
     * Tests copying another configuration into the test configuration. This
     * test ensures that the layout object is informed about the newly added
     * properties.
     */
    public void testCopyAndSave() throws ConfigurationException
    {
        Configuration copyConf = setUpCopyConfig();
        conf.copy(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Tests appending a configuration to the test configuration. Again it has
     * to be ensured that the layout object is correctly updated.
     */
    public void testAppendAndSave() throws ConfigurationException
    {
        Configuration copyConf = setUpCopyConfig();
        conf.append(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Creates a configuration that can be used for testing copy operations.
     *
     * @return the configuration to be copied
     */
    private Configuration setUpCopyConfig()
    {
        final int count = 25;
        Configuration result = new BaseConfiguration();
        for (int i = 1; i <= count; i++)
        {
            result.addProperty("copyKey" + i, "copyValue" + i);
        }
        return result;
    }

    /**
     * Tests whether the data of a configuration that was copied into the test
     * configuration is correctly saved.
     *
     * @param copyConf the copied configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkCopiedConfig(Configuration copyConf)
            throws ConfigurationException
    {
        conf.save(testSavePropertiesFile);
        PropertiesConfiguration checkConf = new PropertiesConfiguration(
                testSavePropertiesFile);
        for (Iterator it = copyConf.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            assertEquals("Wrong value for property " + key, checkConf
                    .getProperty(key), copyConf.getProperty(key));
        }
    }

    /**
     * A dummy layout implementation for checking whether certain methods are
     * correctly called by the configuration.
     */
    static class DummyLayout extends PropertiesConfigurationLayout
    {
        /** Stores the number how often load() was called. */
        public int loadCalls;

        public DummyLayout(PropertiesConfiguration config)
        {
            super(config);
        }

        public void load(Reader in) throws ConfigurationException
        {
            loadCalls++;
        }
    }

    /**
     * A mock implementation of a HttpURLConnection used for testing saving to
     * a HTTP server.
     */
    static class MockHttpURLConnection extends HttpURLConnection
    {
        /** The response code to return.*/
        private int responseCode;

        /** The output file. The output stream will point to this file.*/
        private File outputFile;

        protected MockHttpURLConnection(URL u, int respCode, File outFile)
        {
            super(u);
            responseCode = respCode;
            outputFile = outFile;
        }

        public void disconnect()
        {
        }

        public boolean usingProxy()
        {
            return false;
        }

        public void connect() throws IOException
        {
        }

        public int getResponseCode() throws IOException
        {
            return responseCode;
        }

        public OutputStream getOutputStream() throws IOException
        {
            return new FileOutputStream(outputFile);
        }
    }

    /**
     * A mock stream handler for working with the mock HttpURLConnection.
     */
    static class MockHttpURLStreamHandler extends URLStreamHandler
    {
        /** Stores the response code.*/
        private int responseCode;

        /** Stores the output file.*/
        private File outputFile;

        /** Stores the connection.*/
        private MockHttpURLConnection connection;

        public MockHttpURLStreamHandler(int respCode, File outFile)
        {
            responseCode = respCode;
            outputFile = outFile;
        }

        public MockHttpURLConnection getMockConnection()
        {
            return connection;
        }

        protected URLConnection openConnection(URL u) throws IOException
        {
            connection = new MockHttpURLConnection(u, responseCode, outputFile);
            return connection;
        }
    }
}
