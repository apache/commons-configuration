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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.DefaultFileSystem;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

/**
 * Test for loading and saving properties files.
 *
 */
public class TestPropertiesConfiguration
{
    /**
     * A dummy layout implementation for checking whether certain methods are
     * correctly called by the configuration.
     */
    static class DummyLayout extends PropertiesConfigurationLayout
    {
        /** Stores the number how often load() was called. */
        public int loadCalls;

        @Override
        public void load(final PropertiesConfiguration config, final Reader in)
                throws ConfigurationException
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
        private final int returnCode;

        /** The output file. The output stream will point to this file.*/
        private final File outputFile;

        protected MockHttpURLConnection(final URL u, final int respCode, final File outFile)
        {
            super(u);
            returnCode = respCode;
            outputFile = outFile;
        }

        @Override
        public void connect() throws IOException
        {
        }

        @Override
        public void disconnect()
        {
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
            return new FileOutputStream(outputFile);
        }

        @Override
        public int getResponseCode() throws IOException
        {
            return returnCode;
        }

        @Override
        public boolean usingProxy()
        {
            return false;
        }
    }

    /**
     * A mock stream handler for working with the mock HttpURLConnection.
     */
    static class MockHttpURLStreamHandler extends URLStreamHandler
    {
        /** Stores the response code.*/
        private final int responseCode;

        /** Stores the output file.*/
        private final File outputFile;

        /** Stores the connection.*/
        private MockHttpURLConnection connection;

        public MockHttpURLStreamHandler(final int respCode, final File outFile)
        {
            responseCode = respCode;
            outputFile = outFile;
        }

        public MockHttpURLConnection getMockConnection()
        {
            return connection;
        }

        @Override
        protected URLConnection openConnection(final URL u) throws IOException
        {
            connection = new MockHttpURLConnection(u, responseCode, outputFile);
            return connection;
        }
    }

    /**
     * A test PropertiesReader for testing whether a custom reader can be
     * injected. This implementation creates a configurable number of synthetic
     * test properties.
     */
    private static class PropertiesReaderTestImpl extends
            PropertiesConfiguration.PropertiesReader
    {
        /** The number of test properties to be created. */
        private final int maxProperties;

        /** The current number of properties. */
        private int propertyCount;

        public PropertiesReaderTestImpl(final Reader reader, final int maxProps)
        {
            super(reader);
            maxProperties = maxProps;
        }

        @Override
        public String getPropertyName()
        {
            return PROP_NAME + propertyCount;
        }

        @Override
        public String getPropertyValue()
        {
            return PROP_VALUE + propertyCount;
        }

        @Override
        public boolean nextProperty() throws IOException
        {
            propertyCount++;
            return propertyCount <= maxProperties;
        }
    }

    /**
     * A test PropertiesWriter for testing whether a custom writer can be
     * injected. This implementation simply redirects all output into a test
     * file.
     */
    private static class PropertiesWriterTestImpl extends
            PropertiesConfiguration.PropertiesWriter
    {
        public PropertiesWriterTestImpl(final ListDelimiterHandler handler) throws IOException
        {
            super(new FileWriter(testSavePropertiesFile), handler);
        }
    }

    /** Constant for a test property name.*/
    private static final String PROP_NAME = "testProperty";
    /** Constant for a test property value.*/
    private static final String PROP_VALUE = "value";
    /** Constant for the line break character. */
    private static final String CR = System.getProperty("line.separator");

    /** The File that we test with */
    private static String testProperties = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();

    private static String testBasePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();

    private static String testBasePath2 = ConfigurationAssert.TEST_DIR.getParentFile().getAbsolutePath();

    private static File testSavePropertiesFile = ConfigurationAssert.getOutFile("testsave.properties");

    /**
     * Helper method for loading a configuration from a given file.
     *
     * @param pc the configuration to be loaded
     * @param fileName the file name
     * @return the file handler associated with the configuration
     * @throws ConfigurationException if an error occurs
     */
    private static FileHandler load(final PropertiesConfiguration pc, final String fileName)
            throws ConfigurationException
    {
        final FileHandler handler = new FileHandler(pc);
        handler.setFileName(fileName);
        handler.load();
        return handler;
    }

    /** The configuration to be tested.*/
    private PropertiesConfiguration conf;

    /** Helper object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Helper method for testing the content of a list with elements that
     * contain backslashes.
     *
     * @param key the key
     */
    private void checkBackslashList(final String key)
    {
        final Object prop = conf.getProperty("test." + key);
        assertTrue("Not a list", prop instanceof List);
        final List<?> list = (List<?>) prop;
        assertEquals("Wrong number of list elements", 2, list.size());
        final String prefix = "\\\\" + key;
        assertEquals("Wrong element 1", prefix + "a", list.get(0));
        assertEquals("Wrong element 2", prefix + "b", list.get(1));
    }

    /**
     * Tests whether the data of a configuration that was copied into the test
     * configuration is correctly saved.
     *
     * @param copyConf the copied configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkCopiedConfig(final Configuration copyConf)
            throws ConfigurationException
    {
        saveTestConfig();
        final PropertiesConfiguration checkConf = new PropertiesConfiguration();
        load(checkConf, testSavePropertiesFile.getAbsolutePath());
        for (final Iterator<String> it = copyConf.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertEquals("Wrong value for property " + key, checkConf
                    .getProperty(key), copyConf.getProperty(key));
        }
    }

    /**
     * Checks for a property without a value.
     *
     * @param key the key to be checked
     */
    private void checkEmpty(final String key)
    {
        final String empty = conf.getString(key);
        assertNotNull("Property not found: " + key, empty);
        assertEquals("Wrong value for property " + key, "", empty);
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
        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        load(checkConfig, testSavePropertiesFile.getAbsolutePath());
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
        return checkConfig;
    }

    /**
     * Saves the test configuration to a default output file.
     *
     * @throws ConfigurationException if an error occurs
     */
    private void saveTestConfig() throws ConfigurationException
    {
        final FileHandler handler = new FileHandler(conf);
        handler.save(testSavePropertiesFile);
    }

    @Before
    public void setUp() throws Exception
    {
        conf = new PropertiesConfiguration();
        conf.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        load(conf, testProperties);

        // remove the test save file if it exists
        if (testSavePropertiesFile.exists())
        {
            assertTrue("Test output file could not be deleted",
                    testSavePropertiesFile.delete());
        }
    }

    /**
     * Creates a configuration that can be used for testing copy operations.
     *
     * @return the configuration to be copied
     */
    private Configuration setUpCopyConfig()
    {
        final int count = 25;
        final Configuration result = new BaseConfiguration();
        for (int i = 1; i <= count; i++)
        {
            result.addProperty("copyKey" + i, "copyValue" + i);
        }
        return result;
    }

    /**
     * Tests if properties can be appended by simply calling load() another
     * time.
     */
    @Test
    public void testAppend() throws Exception
    {
        final File file2 = ConfigurationAssert.getTestFile("threesome.properties");
        final FileHandler handler = new FileHandler(conf);
        handler.load(file2);
        assertEquals("aaa", conf.getString("test.threesome.one"));
        assertEquals("true", conf.getString("configuration.loaded"));
    }

    /**
     * Tests appending a configuration to the test configuration. Again it has
     * to be ensured that the layout object is correctly updated.
     */
    @Test
    public void testAppendAndSave() throws ConfigurationException
    {
        final Configuration copyConf = setUpCopyConfig();
        conf.append(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Tests whether backslashes are correctly handled if lists are parsed. This
     * test is related to CONFIGURATION-418.
     */
    @Test
    public void testBackslashEscapingInLists() throws Exception
    {
        checkBackslashList("share2");
        checkBackslashList("share1");
    }

    /**
     * Tests whether another list delimiter character can be set (by using an
     * alternative list delimiter handler).
     */
    @Test
    public void testChangingListDelimiter() throws Exception
    {
        assertEquals("Wrong initial string", "a^b^c",
                conf.getString("test.other.delimiter"));
        final PropertiesConfiguration pc2 = new PropertiesConfiguration();
        pc2.setListDelimiterHandler(new DefaultListDelimiterHandler('^'));
        load(pc2, testProperties);
        assertEquals("Should obtain the first value", "a",
                pc2.getString("test.other.delimiter"));
        assertEquals("Wrong list size", 3, pc2.getList("test.other.delimiter")
                .size());
    }

    /**
     * Tests whether a clear() operation clears the footer comment.
     */
    @Test
    public void testClearFooterComment()
    {
        conf.clear();
        assertNull("Still got a footer comment", conf.getFooter());
        assertNull("Still got a header comment", conf.getHeader());
    }

    /**
     * Tests whether a properties configuration can be successfully cloned. It
     * is especially checked whether the layout object is taken into account.
     */
    @Test
    public void testClone() throws ConfigurationException
    {
        final PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame("Copy has same layout object", conf.getLayout(),
                copy.getLayout());
        assertEquals("Wrong number of event listeners for original", 1, conf
                .getEventListeners(ConfigurationEvent.ANY).size());
        assertEquals("Wrong number of event listeners for clone", 1, copy
                .getEventListeners(ConfigurationEvent.ANY).size());
        assertSame("Wrong event listener for original", conf.getLayout(), conf
                .getEventListeners(ConfigurationEvent.ANY).iterator().next());
        assertSame("Wrong event listener for clone", copy.getLayout(), copy
                .getEventListeners(ConfigurationEvent.ANY).iterator().next());
        final StringWriter outConf = new StringWriter();
        new FileHandler(conf).save(outConf);
        final StringWriter outCopy = new StringWriter();
        new FileHandler(copy).save(outCopy);
        assertEquals("Output from copy is different", outConf.toString(), outCopy.toString());
    }

    /**
     * Tests the clone() method when no layout object exists yet.
     */
    @Test
    public void testCloneNullLayout()
    {
        conf = new PropertiesConfiguration();
        final PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame("Layout objects are the same", conf.getLayout(), copy.getLayout());
    }

    /**
     * Test if the lines starting with # or ! are properly ignored.
     */
    @Test
    public void testComment() {
        assertFalse("comment line starting with '#' parsed as a property", conf.containsKey("#comment"));
        assertFalse("comment line starting with '!' parsed as a property", conf.containsKey("!comment"));
    }

    /**
     * Tests copying another configuration into the test configuration. This
     * test ensures that the layout object is informed about the newly added
     * properties.
     */
    @Test
    public void testCopyAndSave() throws ConfigurationException
    {
        final Configuration copyConf = setUpCopyConfig();
        conf.copy(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Tests whether include files can be disabled.
     */
    @Test
    public void testDisableIncludes() throws ConfigurationException,
            IOException
    {
        final String content =
                PropertiesConfiguration.getInclude()
                        + " = nonExistingIncludeFile" + CR + PROP_NAME + " = "
                        + PROP_VALUE + CR;
        final StringReader in = new StringReader(content);
        conf = new PropertiesConfiguration();
        conf.setIncludesAllowed(false);
        conf.read(in);
        assertEquals("Data not loaded", PROP_VALUE, conf.getString(PROP_NAME));
    }

    @Test
    public void testDisableListDelimiter() throws Exception
    {
        assertEquals(4, conf.getList("test.mixed.array").size());

        final PropertiesConfiguration pc2 = new PropertiesConfiguration();
        load(pc2, testProperties);
        assertEquals(2, pc2.getList("test.mixed.array").size());
    }

    /**
     * Tests that empty properties are treated as the empty string (rather than
     * as null).
     */
    @Test
    public void testEmpty()
    {
        checkEmpty("test.empty");
    }

    /**
     * Tests that properties are detected that do not have a separator and a
     * value.
     */
    @Test
    public void testEmptyNoSeparator()
    {
        checkEmpty("test.empty2");
    }

    @Test
    public void testEscapedKey() throws Exception
    {
        conf.clear();
        final FileHandler handler = new FileHandler(conf);
        handler.load(new StringReader("\\u0066\\u006f\\u006f=bar"));

        assertEquals("value of the 'foo' property", "bar", conf.getString("foo"));
    }

    /**
     * Check that key/value separators can be part of a key.
     */
    @Test
    public void testEscapedKeyValueSeparator()
    {
        assertEquals("Escaped separator '=' not supported in keys", "foo", conf.getProperty("test.separator=in.key"));
        assertEquals("Escaped separator ':' not supported in keys", "bar", conf.getProperty("test.separator:in.key"));
        assertEquals("Escaped separator '\\t' not supported in keys", "foo", conf.getProperty("test.separator\tin.key"));
        assertEquals("Escaped separator '\\f' not supported in keys", "bar", conf.getProperty("test.separator\fin.key"));
        assertEquals("Escaped separator ' ' not supported in keys"  , "foo", conf.getProperty("test.separator in.key"));
    }

    /**
     * Tests the escaping of quotation marks in a properties value. This test is
     * related to CONFIGURATION-516.
     */
    @Test
    public void testEscapeQuote() throws ConfigurationException
    {
        conf.clear();
        final String text = "\"Hello World!\"";
        conf.setProperty(PROP_NAME, text);
        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        assertTrue("Value was escaped: " + out,
                out.toString().contains(text));
        saveTestConfig();
        final PropertiesConfiguration c2 = new PropertiesConfiguration();
        load(c2, testSavePropertiesFile.getAbsolutePath());
        assertEquals("Wrong value", text, c2.getString(PROP_NAME));
    }

    /**
     * Test the creation of a file containing a '#' in its name.
     */
    @Test
    public void testFileWithSharpSymbol() throws Exception
    {
        final File file = folder.newFile("sharp#1.properties");

        final PropertiesConfiguration conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(file);
        handler.load();
        handler.save();

        assertTrue("Missing file " + file, file.exists());
    }

    /**
     * Tests whether read access to the footer comment is synchronized.
     */
    @Test
    public void testGetFooterSynchronized()
    {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        assertNotNull("No footer comment", conf.getFooter());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether read access to the header comment is synchronized.
     */
    @Test
    public void testGetHeaderSynchronized()
    {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        assertNull("Got a header comment", conf.getHeader());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether a default IOFactory is set.
     */
    @Test
    public void testGetIOFactoryDefault()
    {
        assertNotNull("No default IO factory", conf.getIOFactory());
    }

    /**
     * Tests accessing the layout object.
     */
    @Test
    public void testGetLayout()
    {
        final PropertiesConfigurationLayout layout = conf.getLayout();
        assertNotNull("Layout is null", layout);
        assertSame("Different object returned", layout, conf.getLayout());
        conf.setLayout(null);
        final PropertiesConfigurationLayout layout2 = conf.getLayout();
        assertNotNull("Layout 2 is null", layout2);
        assertNotSame("Same object returned", layout, layout2);
    }

    @Test
    public void testGetStringWithEscapedChars()
    {
        final String property = conf.getString("test.unescape");
        assertEquals("String with escaped characters", "This \n string \t contains \" escaped \\ characters", property);
    }

    @Test
    public void testGetStringWithEscapedComma()
    {
        final String property = conf.getString("test.unescape.list-separator");
        assertEquals("String with an escaped list separator", "This string contains , an escaped list separator", property);
    }

    /**
     * Tests including properties when they are loaded from a nested directory
     * structure.
     */
    @Test
    public void testIncludeInSubDir() throws ConfigurationException
    {
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFileName("testFactoryPropertiesInclude.xml"));
        final Configuration config = builder.getConfiguration();
        assertTrue(config.getBoolean("deeptest"));
        assertTrue(config.getBoolean("deepinclude"));
        assertFalse(config.containsKey("deeptestinvalid"));
    }

    /**
     * Tests initializing a properties configuration from a non existing file.
     * There was a bug, which caused properties getting lost when later save()
     * is called.
     */
    @Test
    public void testInitFromNonExistingFile() throws ConfigurationException
    {
        final String testProperty = "test.successfull";
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(testSavePropertiesFile);
        conf.addProperty(testProperty, "true");
        handler.save();
        checkSavedConfig();
    }

    @Test
    public void testInMemoryCreatedSave() throws Exception
    {
        conf = new PropertiesConfiguration();
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        final List<Object> list = new ArrayList<>();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        saveTestConfig();
        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // read the configuration and compare the properties
        checkSavedConfig();
    }

    /**
     * Tests whether comment lines are correctly detected.
     */
    @Test
    public void testIsCommentLine()
    {
        assertTrue("Comment not detected", PropertiesConfiguration.isCommentLine("# a comment"));
        assertTrue("Alternative comment not detected", PropertiesConfiguration.isCommentLine("! a comment"));
        assertTrue("Comment with no space not detected", PropertiesConfiguration.isCommentLine("#a comment"));
        assertTrue("Comment with leading space not detected", PropertiesConfiguration.isCommentLine("    ! a comment"));
        assertFalse("Wrong comment", PropertiesConfiguration.isCommentLine("   a#comment"));
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} reads the same keys
     * and values as {@link Properties} based on a test file.
     */
    @Test
    public void testJupRead() throws IOException, ConfigurationException
    {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory());

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        load(conf, testFilePath);

        final Properties jup = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath)))
        {
            jup.load(in);
        }

        @SuppressWarnings("unchecked")
        final
        Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(jup.keySet(), pcKeys);

        for (final Object key : jup.keySet())
        {
            final String keyString = key.toString();
            System.out.println(keyString);
            assertEquals("Wrong property value for '" + keyString + "'", jup.getProperty(keyString),
                    conf.getProperty(keyString));
        }
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} writes properties in
     * a way that allows {@link Properties} to read them exactly like they were set.
     */
    @Test
    public void testJupWrite() throws IOException, ConfigurationException
    {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory());

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        // read the test properties and set them on the PropertiesConfiguration
        final Properties origProps = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath)))
        {
            origProps.load(in);
        }
        for (final Object key : origProps.keySet())
        {
            final String keyString = key.toString();
            conf.setProperty(keyString, origProps.getProperty(keyString));
        }

        // save the configuration
        saveTestConfig();
        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // load the saved file...
        final Properties testProps = new Properties();
        try (InputStream in = Files.newInputStream(testSavePropertiesFile.toPath()))
        {
            testProps.load(in);
        }

        // ... and compare the properties to the originals
        @SuppressWarnings("unchecked")
        final
        Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(testProps.keySet(), pcKeys);

        for (final Object key : testProps.keySet())
        {
            final String keyString = key.toString();
            assertEquals("Wrong property value for '" + keyString + "'", testProps.getProperty(keyString),
                    conf.getProperty(keyString));
        }
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} writes properties in
     * a way that allows {@link Properties} to read them exactly like they were set.
     * This test writes in UTF-8 encoding, with Unicode escapes turned off.
     */
    @Test
    public void testJupWriteUtf8WithoutUnicodeEscapes() throws IOException, ConfigurationException
    {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory(false));

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        // read the test properties and set them on the PropertiesConfiguration
        final Properties origProps = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath)))
        {
            origProps.load(in);
        }
        for (final Object key : origProps.keySet())
        {
            final String keyString = key.toString();
            conf.setProperty(keyString, origProps.getProperty(keyString));
        }

        // save the configuration as UTF-8
        final FileHandler handler = new FileHandler(conf);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        handler.save(testSavePropertiesFile);
        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // load the saved file...
        final Properties testProps = new Properties();
        try (BufferedReader in = Files.newBufferedReader(testSavePropertiesFile.toPath(), StandardCharsets.UTF_8))
        {
            testProps.load(in);
        }

        // ... and compare the properties to the originals
        @SuppressWarnings("unchecked")
        final
        Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(testProps.keySet(), pcKeys);

        for (final Object key : testProps.keySet())
        {
            final String keyString = key.toString();
            assertEquals("Wrong property value for '" + keyString + "'", testProps.getProperty(keyString),
                    conf.getProperty(keyString));
        }

        // ensure that the written properties file contains no Unicode escapes
        for (final String line : Files.readAllLines(testSavePropertiesFile.toPath()))
        {
            if (line.contains("\\u"))
            {
                fail("Unicode escape found in line: " + line);
            }
        }
    }

    /**
     * Tests that the property separators are retained when saving the
     * configuration.
     */
    @Test
    public void testKeepSeparators() throws ConfigurationException, IOException
    {
        saveTestConfig();
        final String[] separatorTests = {
                "test.separator.equal = foo", "test.separator.colon : foo",
                "test.separator.tab\tfoo", "test.separator.whitespace foo",
                "test.separator.no.space=foo"
        };
        final Set<String> foundLines = new HashSet<>();
        final BufferedReader in = new BufferedReader(new FileReader(
                testSavePropertiesFile));
        try
        {
            String s;
            while ((s = in.readLine()) != null)
            {
                for (final String separatorTest : separatorTests) {
                    if (separatorTest.equals(s))
                    {
                        foundLines.add(s);
                    }
                }
            }
        }
        finally
        {
            in.close();
        }
        assertEquals("No all separators were found: " + foundLines,
                separatorTests.length, foundLines.size());
    }

    /**
     * Test all acceptable key/value separators ('=', ':' or white spaces).
     */
    @Test
    public void testKeyValueSeparators() {
        assertEquals("equal separator not properly parsed",      "foo", conf.getProperty("test.separator.equal"));
        assertEquals("colon separator not properly parsed",      "foo", conf.getProperty("test.separator.colon"));
        assertEquals("tab separator not properly parsed",        "foo", conf.getProperty("test.separator.tab"));
        assertEquals("formfeed separator not properly parsed",   "foo", conf.getProperty("test.separator.formfeed"));
        assertEquals("whitespace separator not properly parsed", "foo", conf.getProperty("test.separator.whitespace"));
    }

    /**
     * Tests whether the correct line separator is used.
     */
    @Test
    public void testLineSeparator() throws ConfigurationException
    {
        final String EOL = System.getProperty("line.separator");
        conf = new PropertiesConfiguration();
        conf.setHeader("My header");
        conf.setProperty("prop", "value");

        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        final String content = out.toString();
        assertTrue("Header could not be found", content.indexOf("# My header"
                + EOL + EOL) == 0);
        assertTrue("Property could not be found", content.indexOf("prop = value" + EOL) > 0);
    }

    /**
     * Tests {@code List} parsing.
     */
    @Test
    public void testList() throws Exception
    {
        final List<Object> packages = conf.getList("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    @Test
    public void testLoad() throws Exception
    {
        final String loaded = conf.getString("configuration.loaded");
        assertEquals("true", loaded);
    }

    @Test
    public void testLoadFromFile() throws Exception
    {
        final File file = ConfigurationAssert.getTestFile("test.properties");
        conf.clear();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(file);
        handler.load();

        assertEquals("true", conf.getString("configuration.loaded"));
    }

    /**
     * test if includes properties get loaded too
     */
    @Test
    public void testLoadInclude() throws Exception
    {
        final String loaded = conf.getString("include.loaded");
        assertEquals("true", loaded);
    }

    /**
     * Tests whether the correct file system is used when loading an include
     * file. This test is related to CONFIGURATION-609.
     */
    @Test
    public void testLoadIncludeFileViaFileSystem() throws ConfigurationException
    {
        conf.clear();
        conf.addProperty("include", "include.properties");
        saveTestConfig();

        final FileSystem fs = new DefaultFileSystem()
        {
            @Override
            public InputStream getInputStream(final URL url)
                    throws ConfigurationException
            {
                if (url.toString().endsWith("include.properties"))
                {
                    try
                    {
                        return new ByteArrayInputStream(
                                "test.outcome = success".getBytes("UTF-8"));
                    }
                    catch (final UnsupportedEncodingException e)
                    {
                        throw new ConfigurationException("Unsupported encoding",
                                e);
                    }
                }
                return super.getInputStream(url);
            }
        };
        final Parameters params = new Parameters();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        builder.configure(params.fileBased().setFile(testSavePropertiesFile)
                .setBasePath(ConfigurationAssert.OUT_DIR.toURI().toString())
                .setFileSystem(fs));
        final PropertiesConfiguration configuration = builder.getConfiguration();
        assertEquals("success", configuration.getString("test.outcome"));
    }

    /**
     * Tests if included files are loaded when the source lies in the class path.
     */
    @Test
    public void testLoadIncludeFromClassPath()
    {
        assertEquals("true", conf.getString("include.loaded"));
    }

    /**
     * Tests whether include files can be resolved if a configuration file is
     * read from a reader.
     */
    @Test
    public void testLoadIncludeFromReader() throws ConfigurationException
    {
        final StringReader in =
                new StringReader(PropertiesConfiguration.getInclude() + " = "
                        + ConfigurationAssert.getTestURL("include.properties"));
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.load(in);
        assertEquals("Include file not loaded", "true",
                conf.getString("include.loaded"));
    }

    /**
     * test if includes properties from interpolated file
     * name get loaded
     */
    @Test
    public void testLoadIncludeInterpol() throws Exception
    {
        final String loaded = conf.getString("include.interpol.loaded");
        assertEquals("true", loaded);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadUnexistingFile() throws ConfigurationException
    {
        load(conf, "unexisting file");
    }

    @Test
    public void testLoadViaPropertyWithBasePath() throws Exception
    {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(testBasePath);
        handler.setFileName("test.properties");
        handler.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    @Test
    public void testLoadIncludeOptional() throws Exception
    {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(testBasePath);
        handler.setFileName("includeoptional.properties");
        handler.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("includeoptional.loaded"));
    }

    @Test
    public void testLoadViaPropertyWithBasePath2() throws Exception
    {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(testBasePath2);
        handler.setFileName("test.properties");
        handler.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    @Test
    public void testMixedArray()
    {
        final String[] array = conf.getStringArray("test.mixed.array");

        assertEquals("array length", 4, array.length);
        assertEquals("1st element", "a", array[0]);
        assertEquals("2nd element", "b", array[1]);
        assertEquals("3rd element", "c", array[2]);
        assertEquals("4th element", "d", array[3]);
    }

    @Test
    public void testMultilines()
    {
        final String property = "This is a value spread out across several adjacent "
                + "natural lines by escaping the line terminator with "
                + "a backslash character.";

        assertEquals("'test.multilines' property", property, conf.getString("test.multilines"));
    }

    /**
     * Tests whether multiple include files can be resolved.
     */
    @Test
    public void testMultipleIncludeFiles() throws ConfigurationException
    {
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.load(ConfigurationAssert.getTestFile("config/testMultiInclude.properties"));
        assertEquals("Wrong top-level property", "topValue",
                conf.getString("top"));
        assertEquals("Wrong included property (1)", 100,
                conf.getInt("property.c"));
        assertEquals("Wrong included property (2)", true,
                conf.getBoolean("include.loaded"));
    }

    /**
     * Tests escaping of an end of line with a backslash.
     */
    @Test
    public void testNewLineEscaping()
    {
        final List<Object> list = conf.getList("test.path");
        assertEquals(3, list.size());
        assertEquals("C:\\path1\\", list.get(0));
        assertEquals("C:\\path2\\", list.get(1));
        assertEquals("C:\\path3\\complex\\test\\", list.get(2));
    }

    /**
     * Tests the propertyLoaded() method for a simple property.
     */
    @Test
    public void testPropertyLoaded() throws ConfigurationException
    {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.propertyLoaded("layoutLoadedProperty", "yes");
        assertEquals("Layout's load() was called", 0, layout.loadCalls);
        assertEquals("Property not added", "yes", conf.getString("layoutLoadedProperty"));
    }

    /**
     * Tests the propertyLoaded() method for an include property.
     */
    @Test
    public void testPropertyLoadedInclude() throws ConfigurationException
    {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClasspath.properties,testEqual.properties");
        assertEquals("Layout's load() was not correctly called", 2, layout.loadCalls);
        assertFalse("Property was added", conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests propertyLoaded() for an include property, when includes are
     * disabled.
     */
    @Test
    public void testPropertyLoadedIncludeNotAllowed() throws ConfigurationException
    {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.setIncludesAllowed(false);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClassPath.properties,testEqual.properties");
        assertEquals("Layout's load() was called", 0, layout.loadCalls);
        assertFalse("Property was added", conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests a direct invocation of the read() method. This is not allowed
     * because certain initializations have not been done. This test is
     * related to CONFIGURATION-641.
     */
    @Test
    public void testReadCalledDirectly() throws IOException
    {
        conf = new PropertiesConfiguration();
        final Reader in = new FileReader(ConfigurationAssert.getTestFile("test.properties"));
        try
        {
            conf.read(in);
            fail("No exception thrown!");
        }
        catch (final ConfigurationException e)
        {
            assertThat(e.getMessage(), containsString("FileHandler"));
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Tests whether a footer comment is correctly read.
     */
    @Test
    public void testReadFooterComment()
    {
        assertEquals("Wrong footer comment", "\n# This is a foot comment\n",
                conf.getFooter());
        assertEquals("Wrong footer comment from layout",
                "\nThis is a foot comment\n", conf.getLayout()
                        .getCanonicalFooterCooment(false));
    }

    /**
     * Tests that references to other properties work
     */
    @Test
    public void testReference() throws Exception
    {
        assertEquals("baseextra", conf.getString("base.reference"));
    }

    @Test
    public void testSave() throws Exception
    {
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        final List<Object> list = new ArrayList<>();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        saveTestConfig();
        assertTrue("The saved file doesn't exist", testSavePropertiesFile.exists());

        // read the configuration and compare the properties
        checkSavedConfig();
    }

    /**
     * Tests whether the escape character for list delimiters can be itself
     * escaped and survives a save operation.
     */
    @Test
    public void testSaveEscapedEscapingCharacter()
            throws ConfigurationException
    {
        conf.addProperty("test.dirs", "C:\\Temp\\\\,D:\\Data\\\\,E:\\Test\\");
        final List<Object> dirs = conf.getList("test.dirs");
        assertEquals("Wrong number of list elements", 3, dirs.size());
        saveTestConfig();
        checkSavedConfig();
    }

    @Test(expected = ConfigurationException.class)
    public void testSaveMissingFilename() throws ConfigurationException
    {
        final FileHandler handler = new FileHandler(conf);
        handler.save();
    }

    @Test
    public void testSaveToCustomURL() throws Exception
    {
        // save the configuration to a custom URL
        final URL url = new URL("foo", "", 0, folder.newFile("testsave-custom-url.properties").getAbsolutePath(), new FileURLStreamHandler());
        final FileHandler handlerSave = new FileHandler(conf);
        handlerSave.save(url);

        // reload the configuration
        final PropertiesConfiguration config2 = new PropertiesConfiguration();
        final FileHandler handlerLoad = new FileHandler(config2);
        handlerLoad.load(url);
        assertEquals("true", config2.getString("configuration.loaded"));
    }

    /**
     * Tests saving a file-based configuration to a HTTP server when the server
     * reports a failure. This should cause an exception.
     */
    @Test
    public void testSaveToHTTPServerFail() throws Exception
    {
        final MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(
                HttpURLConnection.HTTP_BAD_REQUEST, testSavePropertiesFile);
        final URL url = new URL(null, "http://jakarta.apache.org", handler);
        try
        {
            new FileHandler(conf).save(url);
            fail("Response code was not checked!");
        }
        catch (final ConfigurationException cex)
        {
            assertTrue("Wrong root cause: " + cex,
                    cex.getCause() instanceof IOException);
        }
    }

    /**
     * Tests saving a file-based configuration to a HTTP server.
     */
    @Test
    public void testSaveToHTTPServerSuccess() throws Exception
    {
        final MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(
                HttpURLConnection.HTTP_OK, testSavePropertiesFile);
        final URL url = new URL(null, "http://jakarta.apache.org", handler);
        new FileHandler(conf).save(url);
        final MockHttpURLConnection con = handler.getMockConnection();
        assertTrue("Wrong output flag", con.getDoOutput());
        assertEquals("Wrong method", "PUT", con.getRequestMethod());
        checkSavedConfig();
    }

    /**
     * Tests if the base path is taken into account by the save() method.
     */
    @Test
    public void testSaveWithBasePath() throws Exception
    {
        conf.setProperty("test", "true");
        final FileHandler handler = new FileHandler(conf);
        handler.setBasePath(testSavePropertiesFile.getParentFile().toURI().toURL()
                .toString());
        handler.setFileName(testSavePropertiesFile.getName());
        handler.save();
        assertTrue(testSavePropertiesFile.exists());
    }

    /**
     * Tests adding properties through a DataConfiguration. This is related to
     * CONFIGURATION-332.
     */
    @Test
    public void testSaveWithDataConfig() throws ConfigurationException
    {
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(testSavePropertiesFile);
        final DataConfiguration dataConfig = new DataConfiguration(conf);
        dataConfig.setProperty("foo", "bar");
        assertEquals("Property not set", "bar", conf.getString("foo"));

        handler.save();
        final PropertiesConfiguration config2 = new PropertiesConfiguration();
        load(config2, testSavePropertiesFile.getAbsolutePath());
        assertEquals("Property not saved", "bar", config2.getString("foo"));
    }

    /**
     * Tests whether saving works correctly with the default list delimiter
     * handler implementation.
     */
    @Test
    public void testSaveWithDefaultListDelimiterHandler() throws ConfigurationException
    {
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        saveTestConfig();

        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(conf.getListDelimiterHandler());
        new FileHandler(checkConfig).load(testSavePropertiesFile);
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
    }

    /**
     * Tests saving a configuration if delimiter parsing is disabled.
     */
    @Test
    public void testSaveWithDelimiterParsingDisabled() throws ConfigurationException
    {
        conf.clear();
        conf.setListDelimiterHandler(new DisabledListDelimiterHandler());
        conf.addProperty("test.list", "a,b,c");
        conf.addProperty("test.dirs", "C:\\Temp\\,D:\\Data\\");
        saveTestConfig();

        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(new DisabledListDelimiterHandler());
        new FileHandler(checkConfig).load(testSavePropertiesFile);
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
    }

    /**
     * Tests whether write access to the footer comment is synchronized.
     */
    @Test
    public void testSetFooterSynchronized()
    {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setFooter("new comment");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether write access to the header comment is synchronized.
     */
    @Test
    public void testSetHeaderSynchronized()
    {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setHeader("new comment");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    @Test
    public void testSetInclude() throws Exception
    {
        conf.clear();
        // change the include key
        PropertiesConfiguration.setInclude("import");

        // load the configuration
        load(conf, testProperties);

        // restore the previous value for the other tests
        PropertiesConfiguration.setInclude("include");

        assertNull(conf.getString("include.loaded"));
    }

    /**
     * Tests setting the IOFactory to null. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetIOFactoryNull()
    {
        conf.setIOFactory(null);
    }

    /**
     * Tests setting an IOFactory that uses a specialized reader.
     */
    @Test
    public void testSetIOFactoryReader() throws ConfigurationException
    {
        final int propertyCount = 10;
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.IOFactory()
        {
            @Override
            public PropertiesConfiguration.PropertiesReader createPropertiesReader(
                    final Reader in)
            {
                return new PropertiesReaderTestImpl(in, propertyCount);
            }

            @Override
            public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(
                    final Writer out, final ListDelimiterHandler handler)
            {
                throw new UnsupportedOperationException("Unexpected call!");
            }
        });
        load(conf, testProperties);
        for (int i = 1; i <= propertyCount; i++)
        {
            assertEquals("Wrong property value at " + i, PROP_VALUE + i, conf
                    .getString(PROP_NAME + i));
        }
    }

    /**
     * Tests setting an IOFactory that uses a specialized writer.
     */
    @Test
    public void testSetIOFactoryWriter() throws ConfigurationException, IOException
    {
        final MutableObject<Writer> propertiesWriter = new MutableObject<>();
        conf.setIOFactory(new PropertiesConfiguration.IOFactory()
        {
            @Override
            public PropertiesConfiguration.PropertiesReader createPropertiesReader(
                    final Reader in)
            {
                throw new UnsupportedOperationException("Unexpected call!");
            }

            @Override
            public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(
                    final Writer out, final ListDelimiterHandler handler)
            {
                try
                {
                    final PropertiesWriterTestImpl propWriter = new PropertiesWriterTestImpl(handler);
                    propertiesWriter.setValue(propWriter);
                    return propWriter;
                }
                catch (final IOException e)
                {
                    return null;
                }
            }
        });
        new FileHandler(conf).save(new StringWriter());
        propertiesWriter.getValue().close();
        checkSavedConfig();
    }

    /**
     * Tests whether a list property is handled correctly if delimiter parsing
     * is disabled. This test is related to CONFIGURATION-495.
     */
    @Test
    public void testSetPropertyListWithDelimiterParsingDisabled()
            throws ConfigurationException
    {
        final String prop = "delimiterListProp";
        conf.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        final List<String> list = Arrays.asList("val", "val2", "val3");
        conf.setProperty(prop, list);
        saveTestConfig();
        conf.clear();
        load(conf, testSavePropertiesFile.getAbsolutePath());
        assertEquals("Wrong list property", list, conf.getProperty(prop));
    }

    /**
     * Tests whether properties with slashes in their values can be saved. This
     * test is related to CONFIGURATION-408.
     */
    @Test
    public void testSlashEscaping() throws ConfigurationException
    {
        conf.setProperty(PROP_NAME, "http://www.apache.org");
        final StringWriter writer = new StringWriter();
        new FileHandler(conf).save(writer);
        final String s = writer.toString();
        assertTrue("Value not found: " + s, s.contains(PROP_NAME
                + " = http://www.apache.org"));
    }

    /**
     * Tests whether special characters in a property value are un-escaped. This
     * test is related to CONFIGURATION-640.
     */
    @Test
    public void testUnEscapeCharacters()
    {
        assertEquals("Wrong value", "#1 =: me!",
                conf.getString("test.unescape.characters"));
    }

    @Test
    public void testUnescapeJava()
    {
        assertEquals("test\\,test", PropertiesConfiguration.unescapeJava("test\\,test"));
    }

    /**
     * Tests whether a footer comment is correctly written out.
     */
    @Test
    public void testWriteFooterComment() throws ConfigurationException,
            IOException
    {
        final String footer = "my footer";
        conf.clear();
        conf.setProperty(PROP_NAME, PROP_VALUE);
        conf.setFooter(footer);
        final StringWriter out = new StringWriter();
        conf.write(out);
        assertEquals("Wrong result", PROP_NAME + " = " + PROP_VALUE + CR + "# "
                + footer + CR, out.toString());
    }
}
