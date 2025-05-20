/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2;

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for loading and saving properties files.
 */
public class TestPropertiesConfiguration {
    /**
     * A dummy layout implementation for checking whether certain methods are correctly called by the configuration.
     */
    static class DummyLayout extends PropertiesConfigurationLayout {
        /** Stores the number how often load() was called. */
        private int loadCalls;

        @Override
        public void load(final PropertiesConfiguration config, final Reader in) throws ConfigurationException {
            loadCalls++;
        }
    }

    /**
     * A mock implementation of a HttpURLConnection used for testing saving to a HTTP server.
     */
    static class MockHttpURLConnection extends HttpURLConnection {
        /** The response code to return. */
        private final int returnCode;

        /** The output file. The output stream will point to this file. */
        private final File outputFile;

        protected MockHttpURLConnection(final URL u, final int respCode, final File outFile) {
            super(u);
            returnCode = respCode;
            outputFile = outFile;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(outputFile);
        }

        @Override
        public int getResponseCode() throws IOException {
            return returnCode;
        }

        @Override
        public boolean usingProxy() {
            return false;
        }
    }

    /**
     * A mock stream handler for working with the mock HttpURLConnection.
     */
    static class MockHttpURLStreamHandler extends URLStreamHandler {
        /** Stores the response code. */
        private final int responseCode;

        /** Stores the output file. */
        private final File outputFile;

        /** Stores the connection. */
        private MockHttpURLConnection connection;

        public MockHttpURLStreamHandler(final int respCode, final File outFile) {
            responseCode = respCode;
            outputFile = outFile;
        }

        public MockHttpURLConnection getMockConnection() {
            return connection;
        }

        @Override
        protected URLConnection openConnection(final URL u) throws IOException {
            connection = new MockHttpURLConnection(u, responseCode, outputFile);
            return connection;
        }
    }

    /**
     * A test PropertiesReader for testing whether a custom reader can be injected. This implementation creates a
     * configurable number of synthetic test properties.
     */
    private static final class PropertiesReaderTestImpl extends PropertiesConfiguration.PropertiesReader {
        /** The number of test properties to be created. */
        private final int maxProperties;

        /** The current number of properties. */
        private int propertyCount;

        public PropertiesReaderTestImpl(final Reader reader, final int maxProps) {
            super(reader);
            maxProperties = maxProps;
        }

        @Override
        public String getPropertyName() {
            return PROP_NAME + propertyCount;
        }

        @Override
        public String getPropertyValue() {
            return PROP_VALUE + propertyCount;
        }

        @Override
        public boolean nextProperty() throws IOException {
            propertyCount++;
            return propertyCount <= maxProperties;
        }
    }

    /**
     * A test PropertiesWriter for testing whether a custom writer can be injected. This implementation simply redirects all
     * output into a test file.
     */
    private static final class PropertiesWriterTestImpl extends PropertiesConfiguration.PropertiesWriter {
        public PropertiesWriterTestImpl(final ListDelimiterHandler handler) throws IOException {
            super(new FileWriter(TEST_SAVE_PROPERTIES_FILE), handler);
        }
    }

    /** Constant for a test property name. */
    private static final String PROP_NAME = "testProperty";

    /** Constant for a test property value. */
    private static final String PROP_VALUE = "value";

    /** Constant for the line break character. */
    private static final String CR = System.lineSeparator();

    /** The File that we test with */
    private static final String TEST_PROPERTIES = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();

    private static final String TEST_BASE_PATH = ConfigurationAssert.TEST_DIR.getAbsolutePath();

    private static final String TEST_BASE_PATH_2 = ConfigurationAssert.TEST_DIR.getParentFile().getAbsolutePath();

    private static final File TEST_SAVE_PROPERTIES_FILE = ConfigurationAssert.getOutFile("testsave.properties");

    /**
     * Helper method for loading a configuration from a given file.
     *
     * @param pc the configuration to be loaded
     * @param fileName the file name
     * @return the file handler associated with the configuration
     * @throws ConfigurationException if an error occurs
     */
    private static FileHandler load(final PropertiesConfiguration pc, final String fileName) throws ConfigurationException {
        final FileHandler handler = new FileHandler(pc);
        handler.setFileName(fileName);
        handler.load();
        return handler;
    }

    /** The configuration to be tested. */
    private PropertiesConfiguration conf;

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Helper method for testing the content of a list with elements that contain backslashes.
     *
     * @param key the key
     */
    private void checkBackslashList(final String key) {
        final Object prop = conf.getProperty("test." + key);
        final List<?> list = assertInstanceOf(List.class, prop);
        final String prefix = "\\\\" + key;
        assertEquals(Arrays.asList(prefix + "a", prefix + "b"), list);
    }

    /**
     * Tests whether the data of a configuration that was copied into the test configuration is correctly saved.
     *
     * @param copyConf the copied configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkCopiedConfig(final Configuration copyConf) throws ConfigurationException {
        saveTestConfig();
        final PropertiesConfiguration checkConf = new PropertiesConfiguration();
        load(checkConf, TEST_SAVE_PROPERTIES_FILE.getAbsolutePath());
        for (final Iterator<String> it = copyConf.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertEquals(checkConf.getProperty(key), copyConf.getProperty(key), "Wrong value for property " + key);
        }
    }

    /**
     * Checks for a property without a value.
     *
     * @param key the key to be checked
     */
    private void checkEmpty(final String key) {
        final String empty = conf.getString(key);
        assertNotNull(empty, "Property not found: " + key);
        assertEquals("", empty, "Wrong value for property " + key);
    }

    /**
     * Helper method for testing a saved configuration. Reads in the file using a new instance and compares this instance
     * with the original one.
     *
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    private PropertiesConfiguration checkSavedConfig() throws ConfigurationException {
        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        load(checkConfig, TEST_SAVE_PROPERTIES_FILE.getAbsolutePath());
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
        return checkConfig;
    }

    /**
     * Saves the test configuration to a default output file.
     *
     * @throws ConfigurationException if an error occurs
     */
    private void saveTestConfig() throws ConfigurationException {
        final FileHandler handler = new FileHandler(conf);
        handler.save(TEST_SAVE_PROPERTIES_FILE);
    }

    @BeforeEach
    public void setUp() throws Exception {
        conf = new PropertiesConfiguration();
        conf.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        load(conf, TEST_PROPERTIES);

        // remove the test save file if it exists
        if (TEST_SAVE_PROPERTIES_FILE.exists()) {
            assertTrue(TEST_SAVE_PROPERTIES_FILE.delete());
        }
    }

    /**
     * Creates a configuration that can be used for testing copy operations.
     *
     * @return the configuration to be copied
     */
    private Configuration setUpCopyConfig() {
        final int count = 25;
        final Configuration result = new BaseConfiguration();
        for (int i = 1; i <= count; i++) {
            result.addProperty("copyKey" + i, "copyValue" + i);
        }
        return result;
    }

    /**
     * Tests if properties can be appended by simply calling load() another time.
     */
    @Test
    public void testAppend() throws Exception {
        final File file2 = ConfigurationAssert.getTestFile("threesome.properties");
        final FileHandler handler = new FileHandler(conf);
        handler.load(file2);
        assertEquals("aaa", conf.getString("test.threesome.one"));
        assertEquals("true", conf.getString("configuration.loaded"));
    }

    /**
     * Tests appending a configuration to the test configuration. Again it has to be ensured that the layout object is
     * correctly updated.
     */
    @Test
    public void testAppendAndSave() throws ConfigurationException {
        final Configuration copyConf = setUpCopyConfig();
        conf.append(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Tests whether backslashes are correctly handled if lists are parsed. This test is related to CONFIGURATION-418.
     */
    @Test
    public void testBackslashEscapingInLists() throws Exception {
        checkBackslashList("share2");
        checkBackslashList("share1");
    }

    /**
     * Tests whether another list delimiter character can be set (by using an alternative list delimiter handler).
     */
    @Test
    public void testChangingListDelimiter() throws Exception {
        assertEquals("a^b^c", conf.getString("test.other.delimiter"));
        final PropertiesConfiguration pc2 = new PropertiesConfiguration();
        pc2.setListDelimiterHandler(new DefaultListDelimiterHandler('^'));
        load(pc2, TEST_PROPERTIES);
        assertEquals("a", pc2.getString("test.other.delimiter"));
        assertEquals(3, pc2.getList("test.other.delimiter").size());
    }

    /**
     * Tests whether a clear() operation clears the footer comment.
     */
    @Test
    public void testClearFooterComment() {
        conf.clear();
        assertNull(conf.getFooter());
        assertNull(conf.getHeader());
    }

    /**
     * Tests whether a properties configuration can be successfully cloned. It is especially checked whether the layout
     * object is taken into account.
     */
    @Test
    public void testClone() throws ConfigurationException {
        final PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame(conf.getLayout(), copy.getLayout());
        assertEquals(1, conf.getEventListeners(ConfigurationEvent.ANY).size());
        assertEquals(1, copy.getEventListeners(ConfigurationEvent.ANY).size());
        assertSame(conf.getLayout(), conf.getEventListeners(ConfigurationEvent.ANY).iterator().next());
        assertSame(copy.getLayout(), copy.getEventListeners(ConfigurationEvent.ANY).iterator().next());
        final StringWriter outConf = new StringWriter();
        new FileHandler(conf).save(outConf);
        final StringWriter outCopy = new StringWriter();
        new FileHandler(copy).save(outCopy);
        assertEquals(outConf.toString(), outCopy.toString());
    }

    /**
     * Tests the clone() method when no layout object exists yet.
     */
    @Test
    public void testCloneNullLayout() {
        conf = new PropertiesConfiguration();
        final PropertiesConfiguration copy = (PropertiesConfiguration) conf.clone();
        assertNotSame(conf.getLayout(), copy.getLayout());
    }

    /**
     * Test if the lines starting with # or ! are properly ignored.
     */
    @Test
    public void testComment() {
        assertFalse(conf.containsKey("#comment"));
        assertFalse(conf.containsKey("!comment"));
    }

    private Collection<?> testCompress840(final Iterable<?> object) {
        final PropertiesConfiguration configuration = new PropertiesConfiguration();
        final ListDelimiterHandler listDelimiterHandler = configuration.getListDelimiterHandler();
        listDelimiterHandler.flatten(object, 0);
        // Stack overflow:
        listDelimiterHandler.flatten(object, 1);
        listDelimiterHandler.flatten(object, Integer.MAX_VALUE);
        listDelimiterHandler.parse(object);
        configuration.addProperty("foo", object);
        configuration.toString();
        return listDelimiterHandler.flatten(object, Integer.MAX_VALUE);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2, 4, 8, 16 })
    public void testCompress840ArrayList(final int size) {
        final ArrayList<Object> object = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            object.add(i);
        }
        final Collection<?> result = testCompress840(object);
        assertNotNull(result);
        assertEquals(size, result.size());
        assertEquals(object, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2, 4, 8, 16 })
    public void testCompress840ArrayListCycle(final int size) {
        final ArrayList<Object> object = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            object.add(i);
            object.add(object);
            object.add(new ArrayList<>(object));
        }
        final Collection<?> result = testCompress840(object);
        assertNotNull(result);
        assertEquals(size, result.size());
        object.add(object);
        testCompress840(object);
    }

    @Test
    public void testCompress840BeanContextServicesSupport() {
        testCompress840(new BeanContextServicesSupport());
        testCompress840(new BeanContextServicesSupport(new BeanContextServicesSupport()));
        final BeanContextSupport bcs = new BeanContextSupport();
        final BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        bcs.add(FileSystems.getDefault().getPath("bar"));
        bcss.add(bcs);
        testCompress840(bcss);
        bcss.add(FileSystems.getDefault().getPath("bar"));
        testCompress840(bcss);
        bcss.add(bcss);
        testCompress840(bcss);
    }

    @Test
    public void testCompress840BeanContextSupport() {
        testCompress840(new BeanContextSupport());
        testCompress840(new BeanContextSupport(new BeanContextSupport()));
        final BeanContextSupport bcs = new BeanContextSupport();
        bcs.add(FileSystems.getDefault().getPath("bar"));
        testCompress840(bcs);
        bcs.add(bcs);
        testCompress840(bcs);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2, 4, 8, 16 })
    public void testCompress840Exception(final int size) {
        final ArrayList<Object> object = new ArrayList<>();
        final Exception bottom = new Exception();
        object.add(bottom);
        Exception top = bottom;
        for (int i = 0; i < size; i++) {
            object.add(i);
            top = new Exception(top);
            object.add(top);
        }
        if (bottom != top) {
            // direct self-causation is not allowed.
            bottom.initCause(top);
        }
        final Collection<?> result = testCompress840(object);
        assertNotNull(result);
        assertEquals(size * 2 + 1, result.size());
        assertEquals(object, result);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2, 4, 8, 16 })
    public void testCompress840Path(final int size) {
        final PriorityQueue<Object> object = new PriorityQueue<>();
        for (int i = 0; i < size; i++) {
            object.add(FileSystems.getDefault().getPath("foo"));
            object.add(FileSystems.getDefault().getPath("foo", "bar"));
        }
        testCompress840(object);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2, 4, 8, 16 })
    public void testCompress840PriorityQueue(final int size) {
        final PriorityQueue<Object> object = new PriorityQueue<>();
        for (int i = 0; i < size; i++) {
            object.add(FileSystems.getDefault().getPath("foo"));
        }
        testCompress840(object);
    }

    @Test
    void testConfiguration() throws ConfigurationException {
        final Configurations configManager = new Configurations();
        final Configuration config = configManager.properties("src/test/resources/config/test.properties");

        assertTrue(config.containsValue("jndivalue2"));
        assertFalse(config.containsValue("notFound"));
        assertFalse(config.containsValue(null));
        assertFalse(config.containsValue(""));
    }

    /**
     * Tests copying another configuration into the test configuration. This test ensures that the layout object is informed
     * about the newly added properties.
     */
    @Test
    public void testCopyAndSave() throws ConfigurationException {
        final Configuration copyConf = setUpCopyConfig();
        conf.copy(copyConf);
        checkCopiedConfig(copyConf);
    }

    /**
     * Tests whether include files can be disabled.
     */
    @Test
    public void testDisableIncludes() throws ConfigurationException, IOException {
        final String content = PropertiesConfiguration.getInclude() + " = nonExistingIncludeFile" + CR + PROP_NAME + " = " + PROP_VALUE + CR;
        final StringReader in = new StringReader(content);
        conf = new PropertiesConfiguration();
        conf.setIncludesAllowed(false);
        conf.read(in);
        assertEquals(PROP_VALUE, conf.getString(PROP_NAME));
    }

    @Test
    public void testDisableListDelimiter() throws Exception {
        assertEquals(4, conf.getList("test.mixed.array").size());

        final PropertiesConfiguration pc2 = new PropertiesConfiguration();
        load(pc2, TEST_PROPERTIES);
        assertEquals(2, pc2.getList("test.mixed.array").size());
    }

    /**
     * Tests that empty properties are treated as the empty string (rather than as null).
     */
    @Test
    public void testEmpty() {
        checkEmpty("test.empty");
    }

    /**
     * Tests that properties are detected that do not have a separator and a value.
     */
    @Test
    public void testEmptyNoSeparator() {
        checkEmpty("test.empty2");
    }

    @Test
    public void testEscapedKey() throws Exception {
        conf.clear();
        final FileHandler handler = new FileHandler(conf);
        handler.load(new StringReader("\\u0066\\u006f\\u006f=bar"));

        assertEquals("bar", conf.getString("foo"));
    }

    /**
     * Check that key/value separators can be part of a key.
     */
    @Test
    public void testEscapedKeyValueSeparator() {
        assertEquals("foo", conf.getProperty("test.separator=in.key"));
        assertEquals("bar", conf.getProperty("test.separator:in.key"));
        assertEquals("foo", conf.getProperty("test.separator\tin.key"));
        assertEquals("bar", conf.getProperty("test.separator\fin.key"));
        assertEquals("foo", conf.getProperty("test.separator in.key"));
    }

    /**
     * Tests the escaping of quotation marks in a properties value. This test is related to CONFIGURATION-516.
     */
    @Test
    public void testEscapeQuote() throws ConfigurationException {
        conf.clear();
        final String text = "\"Hello World!\"";
        conf.setProperty(PROP_NAME, text);
        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        assertTrue(out.toString().contains(text));
        saveTestConfig();
        final PropertiesConfiguration c2 = new PropertiesConfiguration();
        load(c2, TEST_SAVE_PROPERTIES_FILE.getAbsolutePath());
        assertEquals(text, c2.getString(PROP_NAME));
    }

    /**
     * Test the creation of a file containing a '#' in its name.
     */
    @Test
    public void testFileWithSharpSymbol() throws Exception {
        final File file = newFile("sharp#1.properties", tempFolder);

        final PropertiesConfiguration conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(file);
        handler.load();
        handler.save();

        assertTrue(file.exists());
    }

    /**
     * Tests whether read access to the footer comment is synchronized.
     */
    @Test
    public void testGetFooterSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        assertNotNull(conf.getFooter());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether read access to the header comment is synchronized.
     */
    @Test
    public void testGetHeaderSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        assertNull(conf.getHeader());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether a default IOFactory is set.
     */
    @Test
    public void testGetIOFactoryDefault() {
        assertNotNull(conf.getIOFactory());
    }

    /**
     * Tests accessing the layout object.
     */
    @Test
    public void testGetLayout() {
        final PropertiesConfigurationLayout layout = conf.getLayout();
        assertNotNull(layout);
        assertSame(layout, conf.getLayout());
        conf.setLayout(null);
        final PropertiesConfigurationLayout layout2 = conf.getLayout();
        assertNotNull(layout2);
        assertNotSame(layout, layout2);
    }

    @Test
    public void testGetStringWithEscapedChars() {
        final String property = conf.getString("test.unescape");
        assertEquals("This \n string \t contains \" escaped \\ characters", property);
    }

    @Test
    public void testGetStringWithEscapedComma() {
        final String property = conf.getString("test.unescape.list-separator");
        assertEquals("This string contains , an escaped list separator", property);
    }

    @Test
    public void testIncludeIncludeLoadAllOnNotFound() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-include-not-found.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
        assertEquals("valueB", pc.getString("keyB"));
    }

    @Test
    public void testIncludeIncludeLoadCyclicalReferenceFail() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-include-cyclical-reference.properties");
        assertThrows(ConfigurationException.class, handler::load);
        assertNull(pc.getString("keyA"));
    }

    @Test
    public void testIncludeIncludeLoadCyclicalReferenceIgnore() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-include-cyclical-reference.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
    }

    /**
     * Tests including properties when they are loaded from a nested directory structure.
     */
    @Test
    public void testIncludeInSubDir() throws ConfigurationException {
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFileName("testFactoryPropertiesInclude.xml"));
        final Configuration config = builder.getConfiguration();
        assertTrue(config.getBoolean("deeptest"));
        assertTrue(config.getBoolean("deepinclude"));
        assertFalse(config.containsKey("deeptestinvalid"));
    }

    @Test
    public void testIncludeLoadAllOnLoadException() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-load-exception.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadAllOnNotFound() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-not-found.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadCyclicalMultiStepReferenceFail() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-cyclical-root.properties");
        assertThrows(ConfigurationException.class, handler::load);
        assertNull(pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadCyclicalMultiStepReferenceIgnore() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-cyclical-root.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadCyclicalReferenceFail() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-cyclical-reference.properties");
        assertThrows(ConfigurationException.class, handler::load);
        assertNull(pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadCyclicalReferenceIgnore() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER);
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("include-cyclical-reference.properties");
        handler.load();
        assertEquals("valueA", pc.getString("keyA"));
    }

    /**
     * Tests initializing a properties configuration from a non existing file. There was a bug, which caused properties
     * getting lost when later save() is called.
     */
    @Test
    public void testInitFromNonExistingFile() throws ConfigurationException {
        final String testProperty = "test.successfull";
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(TEST_SAVE_PROPERTIES_FILE);
        conf.addProperty(testProperty, "true");
        handler.save();
        checkSavedConfig();
    }

    @Test
    public void testInMemoryCreatedSave() throws Exception {
        conf = new PropertiesConfiguration();
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        final List<Object> list = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        saveTestConfig();
        assertTrue(TEST_SAVE_PROPERTIES_FILE.exists());

        // read the configuration and compare the properties
        checkSavedConfig();
    }

    /**
     * Tests whether comment lines are correctly detected.
     */
    @Test
    public void testIsCommentLine() {
        assertTrue(PropertiesConfiguration.isCommentLine("# a comment"));
        assertTrue(PropertiesConfiguration.isCommentLine("! a comment"));
        assertTrue(PropertiesConfiguration.isCommentLine("#a comment"));
        assertTrue(PropertiesConfiguration.isCommentLine("    ! a comment"));
        assertFalse(PropertiesConfiguration.isCommentLine("   a#comment"));
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} reads the same keys and values as {@link Properties} based on
     * a test file.
     */
    @Test
    public void testJupRead() throws IOException, ConfigurationException {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory());

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        load(conf, testFilePath);

        final Properties jup = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath))) {
            jup.load(in);
        }

        @SuppressWarnings("unchecked")
        final Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(jup.keySet(), pcKeys);

        for (final Object key : jup.keySet()) {
            final String keyString = key.toString();
            assertEquals(jup.getProperty(keyString), conf.getProperty(keyString), "Wrong property value for '" + keyString + "'");
        }
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} writes properties in a way that allows {@link Properties} to
     * read them exactly like they were set.
     */
    @Test
    public void testJupWrite() throws IOException, ConfigurationException {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory());

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        // read the test properties and set them on the PropertiesConfiguration
        final Properties origProps = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath))) {
            origProps.load(in);
        }
        for (final Object key : origProps.keySet()) {
            final String keyString = key.toString();
            conf.setProperty(keyString, origProps.getProperty(keyString));
        }

        // save the configuration
        saveTestConfig();
        assertTrue(TEST_SAVE_PROPERTIES_FILE.exists());

        // load the saved file...
        final Properties testProps = new Properties();
        try (InputStream in = Files.newInputStream(TEST_SAVE_PROPERTIES_FILE.toPath())) {
            testProps.load(in);
        }

        // ... and compare the properties to the originals
        @SuppressWarnings("unchecked")
        final Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(testProps.keySet(), pcKeys);

        for (final Object key : testProps.keySet()) {
            final String keyString = key.toString();
            assertEquals(testProps.getProperty(keyString), conf.getProperty(keyString), "Wrong property value for '" + keyString + "'");
        }
    }

    /**
     * Tests that {@link PropertiesConfiguration.JupIOFactory} writes properties in a way that allows {@link Properties} to
     * read them exactly like they were set. This test writes in UTF-8 encoding, with Unicode escapes turned off.
     */
    @Test
    public void testJupWriteUtf8WithoutUnicodeEscapes() throws IOException, ConfigurationException {
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.JupIOFactory(false));

        final String testFilePath = ConfigurationAssert.getTestFile("jup-test.properties").getAbsolutePath();

        // read the test properties and set them on the PropertiesConfiguration
        final Properties origProps = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(testFilePath))) {
            origProps.load(in);
        }
        for (final Object key : origProps.keySet()) {
            final String keyString = key.toString();
            conf.setProperty(keyString, origProps.getProperty(keyString));
        }

        // save the configuration as UTF-8
        final FileHandler handler = new FileHandler(conf);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        handler.save(TEST_SAVE_PROPERTIES_FILE);
        assertTrue(TEST_SAVE_PROPERTIES_FILE.exists());

        // load the saved file...
        final Properties testProps = new Properties();
        try (BufferedReader in = Files.newBufferedReader(TEST_SAVE_PROPERTIES_FILE.toPath(), StandardCharsets.UTF_8)) {
            testProps.load(in);
        }

        // ... and compare the properties to the originals
        @SuppressWarnings("unchecked")
        final Set<Object> pcKeys = new HashSet<>(IteratorUtils.toList(conf.getKeys()));
        assertEquals(testProps.keySet(), pcKeys);

        for (final Object key : testProps.keySet()) {
            final String keyString = key.toString();
            assertEquals(testProps.getProperty(keyString), conf.getProperty(keyString), "Wrong property value for '" + keyString + "'");
        }

        // ensure that the written properties file contains no Unicode escapes
        for (final String line : Files.readAllLines(TEST_SAVE_PROPERTIES_FILE.toPath())) {
            assertFalse(line.contains("\\u"));
        }
    }

    /**
     * Tests that the property separators are retained when saving the configuration.
     */
    @Test
    public void testKeepSeparators() throws ConfigurationException, IOException {
        saveTestConfig();
        // @formatter:off
        final Set<String> separatorTests = new HashSet<>(Arrays.asList(
                "test.separator.equal = foo",
                "test.separator.colon : foo",
                "test.separator.tab\tfoo",
                "test.separator.whitespace foo",
                "test.separator.no.space=foo"));
        // @formatter:on
        final Set<String> foundLines = new HashSet<>();
        try (BufferedReader in = new BufferedReader(new FileReader(TEST_SAVE_PROPERTIES_FILE))) {
            String s;
            while ((s = in.readLine()) != null) {
                for (final String separatorTest : separatorTests) {
                    if (separatorTest.equals(s)) {
                        foundLines.add(s);
                    }
                }
            }
        }
        assertEquals(separatorTests, foundLines);
    }

    /**
     * Test all acceptable key/value separators ('=', ':' or white spaces).
     */
    @Test
    public void testKeyValueSeparators() {
        assertEquals("foo", conf.getProperty("test.separator.equal"));
        assertEquals("foo", conf.getProperty("test.separator.colon"));
        assertEquals("foo", conf.getProperty("test.separator.tab"));
        assertEquals("foo", conf.getProperty("test.separator.formfeed"));
        assertEquals("foo", conf.getProperty("test.separator.whitespace"));
    }

    @Test
    public void testLargeKey() throws Exception {
        conf.clear();
        final String key = String.join("", Collections.nCopies(10000, "x"));
        final FileHandler handler = new FileHandler(conf);
        handler.load(new StringReader(key));

        assertEquals("", conf.getString(key));
    }

    /**
     * Tests whether the correct line separator is used.
     */
    @Test
    public void testLineSeparator() throws ConfigurationException {
        final String eol = System.lineSeparator();
        conf = new PropertiesConfiguration();
        conf.setHeader("My header");
        conf.setProperty("prop", "value");

        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        final String content = out.toString();
        assertEquals(0, content.indexOf("# My header" + eol + eol));
        assertTrue(content.contains("prop = value" + eol));
    }

    /**
     * Tests {@code List} parsing.
     */
    @Test
    public void testList() throws Exception {
        final List<Object> packages = conf.getList("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    @Test
    public void testLoad() throws Exception {
        final String loaded = conf.getString("configuration.loaded");
        assertEquals("true", loaded);
    }

    @Test
    public void testLoadFromFile() throws Exception {
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
    public void testLoadInclude() throws Exception {
        final String loaded = conf.getString("include.loaded");
        assertEquals("true", loaded);
    }

    /**
     * Tests whether the correct file system is used when loading an include file. This test is related to
     * CONFIGURATION-609.
     */
    @Test
    public void testLoadIncludeFileViaFileSystem() throws ConfigurationException {
        conf.clear();
        conf.addProperty("include", "include.properties");
        saveTestConfig();

        final FileSystem fs = new DefaultFileSystem() {
            @Override
            public InputStream getInputStream(final URL url) throws ConfigurationException {
                if (url.toString().endsWith("include.properties")) {
                    return new ByteArrayInputStream("test.outcome = success".getBytes(StandardCharsets.UTF_8));
                }
                return super.getInputStream(url);
            }
        };
        final Parameters params = new Parameters();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.configure(params.fileBased().setFile(TEST_SAVE_PROPERTIES_FILE).setBasePath(ConfigurationAssert.OUT_DIR.toURI().toString()).setFileSystem(fs));
        final PropertiesConfiguration configuration = builder.getConfiguration();
        assertEquals("success", configuration.getString("test.outcome"));
    }

    /**
     * Tests if included files are loaded when the source lies in the class path.
     */
    @Test
    public void testLoadIncludeFromClassPath() {
        assertEquals("true", conf.getString("include.loaded"));
    }

    /**
     * Tests whether include files can be resolved if a configuration file is read from a reader.
     */
    @Test
    public void testLoadIncludeFromReader() throws ConfigurationException {
        final StringReader in = new StringReader(PropertiesConfiguration.getInclude() + " = " + ConfigurationAssert.getTestURL("include.properties"));
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.load(in);
        assertEquals("true", conf.getString("include.loaded"));
    }

    /**
     * test if includes properties from interpolated file name get loaded
     */
    @Test
    public void testLoadIncludeInterpol() throws Exception {
        final String loaded = conf.getString("include.interpol.loaded");
        assertEquals("true", loaded);
    }

    @Test
    public void testLoadIncludeOptional() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("includeoptional.properties");
        handler.load();

        assertTrue(pc.getBoolean("includeoptional.loaded"));
    }

    @Test
    public void testLoadUnexistingFile() {
        assertThrows(ConfigurationException.class, () -> load(conf, "unexisting file"));
    }

    @Test
    public void testLoadViaPropertyWithBasePath() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH);
        handler.setFileName("test.properties");
        handler.load();

        assertTrue(pc.getBoolean("test.boolean"));
    }

    @Test
    public void testLoadViaPropertyWithBasePath2() throws Exception {
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setBasePath(TEST_BASE_PATH_2);
        handler.setFileName("test.properties");
        handler.load();

        assertTrue(pc.getBoolean("test.boolean"));
    }

    @Test
    public void testMixedArray() {
        final String[] array = conf.getStringArray("test.mixed.array");

        assertArrayEquals(new String[] {"a", "b", "c", "d"}, array);
    }

    @Test
    public void testMultilines() {
        final String property = "This is a value spread out across several adjacent " + "natural lines by escaping the line terminator with "
            + "a backslash character.";

        assertEquals(property, conf.getString("test.multilines"));
    }

    /**
     * Tests whether multiple include files can be resolved.
     */
    @Test
    public void testMultipleIncludeFiles() throws ConfigurationException {
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.load(ConfigurationAssert.getTestFile("config/testMultiInclude.properties"));
        assertEquals("topValue", conf.getString("top"));
        assertEquals(100, conf.getInt("property.c"));
        assertTrue(conf.getBoolean("include.loaded"));
    }

    /**
     * Tests escaping of an end of line with a backslash.
     */
    @Test
    public void testNewLineEscaping() {
        final List<Object> list = conf.getList("test.path");
        assertEquals(Arrays.asList("C:\\path1\\", "C:\\path2\\", "C:\\path3\\complex\\test\\"), list);
    }

    /**
     * Tests the propertyLoaded() method for a simple property.
     */
    @Test
    public void testPropertyLoaded() throws ConfigurationException {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.propertyLoaded("layoutLoadedProperty", "yes", null);
        assertEquals(0, layout.loadCalls);
        assertEquals("yes", conf.getString("layoutLoadedProperty"));
    }

    /**
     * Tests the propertyLoaded() method for an include property.
     */
    @Test
    public void testPropertyLoadedInclude() throws ConfigurationException {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClasspath.properties,testEqual.properties", new ArrayDeque<>());
        assertEquals(2, layout.loadCalls);
        assertFalse(conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests propertyLoaded() for an include property, when includes are disabled.
     */
    @Test
    public void testPropertyLoadedIncludeNotAllowed() throws ConfigurationException {
        final DummyLayout layout = new DummyLayout();
        conf.setLayout(layout);
        conf.setIncludesAllowed(false);
        conf.propertyLoaded(PropertiesConfiguration.getInclude(), "testClassPath.properties,testEqual.properties", null);
        assertEquals(0, layout.loadCalls);
        assertFalse(conf.containsKey(PropertiesConfiguration.getInclude()));
    }

    /**
     * Tests a direct invocation of the read() method. This is not allowed because certain initializations have not been
     * done. This test is related to CONFIGURATION-641.
     */
    @Test
    public void testReadCalledDirectly() throws IOException {
        conf = new PropertiesConfiguration();
        try (Reader in = new FileReader(ConfigurationAssert.getTestFile("test.properties"))) {
            final ConfigurationException e = assertThrows(ConfigurationException.class, () -> conf.read(in));
            assertTrue(e.getMessage().contains("FileHandler"));
        }
    }

    /**
     * Tests whether a footer comment is correctly read.
     */
    @Test
    public void testReadFooterComment() {
        assertEquals("\n# This is a foot comment\n", conf.getFooter());
        assertEquals("\nThis is a foot comment\n", conf.getLayout().getCanonicalFooterCooment(false));
    }

    /**
     * Tests that references to other properties work
     */
    @Test
    public void testReference() throws Exception {
        assertEquals("baseextra", conf.getString("base.reference"));
    }

    @Test
    public void testSave() throws Exception {
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        final List<Object> list = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        saveTestConfig();
        assertTrue(TEST_SAVE_PROPERTIES_FILE.exists());

        // read the configuration and compare the properties
        checkSavedConfig();
    }

    /**
     * Tests whether the escape character for list delimiters can be itself escaped and survives a save operation.
     */
    @Test
    public void testSaveEscapedEscapingCharacter() throws ConfigurationException {
        conf.addProperty("test.dirs", "C:\\Temp\\\\,D:\\Data\\\\,E:\\Test\\");
        final List<Object> dirs = conf.getList("test.dirs");
        assertEquals(3, dirs.size());
        saveTestConfig();
        checkSavedConfig();
    }

    @Test
    public void testSaveMissingFileName() {
        final FileHandler handler = new FileHandler(conf);
        assertThrows(ConfigurationException.class, handler::save);
    }

    @Test
    public void testSaveToCustomURL() throws Exception {
        // save the configuration to a custom URL
        final URL url = new URL("foo", "", 0, newFile("testsave-custom-url.properties", tempFolder).getAbsolutePath(), new FileURLStreamHandler());
        final FileHandler handlerSave = new FileHandler(conf);
        handlerSave.save(url);

        // reload the configuration
        final PropertiesConfiguration config2 = new PropertiesConfiguration();
        final FileHandler handlerLoad = new FileHandler(config2);
        handlerLoad.load(url);
        assertEquals("true", config2.getString("configuration.loaded"));
    }

    /**
     * Tests saving a file-based configuration to a HTTP server when the server reports a failure. This should cause an
     * exception.
     */
    @Test
    public void testSaveToHTTPServerFail() throws Exception {
        final MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(HttpURLConnection.HTTP_BAD_REQUEST, TEST_SAVE_PROPERTIES_FILE);
        final URL url = new URL(null, "http://jakarta.apache.org", handler);
        final FileHandler fileHandler = new FileHandler(conf);
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> fileHandler.save(url));
        assertInstanceOf(IOException.class, cex.getCause());
    }

    /**
     * Tests saving a file-based configuration to a HTTP server.
     */
    @Test
    public void testSaveToHTTPServerSuccess() throws Exception {
        final MockHttpURLStreamHandler handler = new MockHttpURLStreamHandler(HttpURLConnection.HTTP_OK, TEST_SAVE_PROPERTIES_FILE);
        final URL url = new URL(null, "http://jakarta.apache.org", handler);
        new FileHandler(conf).save(url);
        final MockHttpURLConnection con = handler.getMockConnection();
        assertTrue(con.getDoOutput());
        assertEquals("PUT", con.getRequestMethod());
        checkSavedConfig();
    }

    /**
     * Tests if the base path is taken into account by the save() method.
     */
    @Test
    public void testSaveWithBasePath() throws Exception {
        conf.setProperty("test", "true");
        final FileHandler handler = new FileHandler(conf);
        handler.setBasePath(TEST_SAVE_PROPERTIES_FILE.getParentFile().toURI().toURL().toString());
        handler.setFileName(TEST_SAVE_PROPERTIES_FILE.getName());
        handler.save();
        assertTrue(TEST_SAVE_PROPERTIES_FILE.exists());
    }

    /**
     * Tests adding properties through a DataConfiguration. This is related to CONFIGURATION-332.
     */
    @Test
    public void testSaveWithDataConfig() throws ConfigurationException {
        conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFile(TEST_SAVE_PROPERTIES_FILE);
        final DataConfiguration dataConfig = new DataConfiguration(conf);
        dataConfig.setProperty("foo", "bar");
        assertEquals("bar", conf.getString("foo"));

        handler.save();
        final PropertiesConfiguration config2 = new PropertiesConfiguration();
        load(config2, TEST_SAVE_PROPERTIES_FILE.getAbsolutePath());
        assertEquals("bar", config2.getString("foo"));
    }

    /**
     * Tests whether saving works correctly with the default list delimiter handler implementation.
     */
    @Test
    public void testSaveWithDefaultListDelimiterHandler() throws ConfigurationException {
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        saveTestConfig();

        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(conf.getListDelimiterHandler());
        new FileHandler(checkConfig).load(TEST_SAVE_PROPERTIES_FILE);
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
    }

    /**
     * Tests saving a configuration if delimiter parsing is disabled.
     */
    @Test
    public void testSaveWithDelimiterParsingDisabled() throws ConfigurationException {
        conf.clear();
        conf.setListDelimiterHandler(new DisabledListDelimiterHandler());
        conf.addProperty("test.list", "a,b,c");
        conf.addProperty("test.dirs", "C:\\Temp\\,D:\\Data\\");
        saveTestConfig();

        final PropertiesConfiguration checkConfig = new PropertiesConfiguration();
        checkConfig.setListDelimiterHandler(new DisabledListDelimiterHandler());
        new FileHandler(checkConfig).load(TEST_SAVE_PROPERTIES_FILE);
        ConfigurationAssert.assertConfigurationEquals(conf, checkConfig);
    }

    /**
     * Tests whether write access to the footer comment is synchronized.
     */
    @Test
    public void testSetFooterSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setFooter("new comment");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether write access to the header comment is synchronized.
     */
    @Test
    public void testSetHeaderSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setHeader("new comment");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    @Test
    public void testSetInclude() throws Exception {
        conf.clear();
        // change the include key
        PropertiesConfiguration.setInclude("import");

        // load the configuration
        load(conf, TEST_PROPERTIES);

        // restore the previous value for the other tests
        PropertiesConfiguration.setInclude("include");

        assertNull(conf.getString("include.loaded"));
    }

    /**
     * Tests setting the IOFactory to null. This should cause an exception.
     */
    @Test
    public void testSetIOFactoryNull() {
        assertThrows(IllegalArgumentException.class, () -> conf.setIOFactory(null));
    }

    /**
     * Tests setting an IOFactory that uses a specialized reader.
     */
    @Test
    public void testSetIOFactoryReader() throws ConfigurationException {
        final int propertyCount = 10;
        conf.clear();
        conf.setIOFactory(new PropertiesConfiguration.IOFactory() {
            @Override
            public PropertiesConfiguration.PropertiesReader createPropertiesReader(final Reader in) {
                return new PropertiesReaderTestImpl(in, propertyCount);
            }

            @Override
            public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(final Writer out, final ListDelimiterHandler handler) {
                throw new UnsupportedOperationException("Unexpected call!");
            }
        });
        load(conf, TEST_PROPERTIES);
        for (int i = 1; i <= propertyCount; i++) {
            assertEquals(PROP_VALUE + i, conf.getString(PROP_NAME + i), "Wrong property value at " + i);
        }
    }

    /**
     * Tests setting an IOFactory that uses a specialized writer.
     */
    @Test
    public void testSetIOFactoryWriter() throws ConfigurationException, IOException {
        final MutableObject<Writer> propertiesWriter = new MutableObject<>();
        conf.setIOFactory(new PropertiesConfiguration.IOFactory() {
            @Override
            public PropertiesConfiguration.PropertiesReader createPropertiesReader(final Reader in) {
                throw new UnsupportedOperationException("Unexpected call!");
            }

            @Override
            public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(final Writer out, final ListDelimiterHandler handler) {
                try {
                    final PropertiesWriterTestImpl propWriter = new PropertiesWriterTestImpl(handler);
                    propertiesWriter.setValue(propWriter);
                    return propWriter;
                } catch (final IOException e) {
                    return null;
                }
            }
        });
        new FileHandler(conf).save(new StringWriter());
        propertiesWriter.getValue().close();
        checkSavedConfig();
    }

    /**
     * Tests whether a list property is handled correctly if delimiter parsing is disabled. This test is related to
     * CONFIGURATION-495.
     */
    @Test
    public void testSetPropertyListWithDelimiterParsingDisabled() throws ConfigurationException {
        final String prop = "delimiterListProp";
        conf.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        final List<String> list = Arrays.asList("val", "val2", "val3");
        conf.setProperty(prop, list);
        saveTestConfig();
        conf.clear();
        load(conf, TEST_SAVE_PROPERTIES_FILE.getAbsolutePath());
        assertEquals(list, conf.getProperty(prop));
    }

    /**
     * Tests whether properties with slashes in their values can be saved. This test is related to CONFIGURATION-408.
     */
    @Test
    public void testSlashEscaping() throws ConfigurationException {
        conf.setProperty(PROP_NAME, "http://www.apache.org");
        final StringWriter writer = new StringWriter();
        new FileHandler(conf).save(writer);
        final String s = writer.toString();
        assertTrue(s.contains(PROP_NAME + " = http://www.apache.org"));
    }

    /**
     * Tests whether special characters in a property value are un-escaped. This test is related to CONFIGURATION-640.
     */
    @Test
    public void testUnEscapeCharacters() {
        assertEquals("#1 =: me!", conf.getString("test.unescape.characters"));
    }

    @Test
    public void testUnescapeJava() {
        assertEquals("test\\,test", PropertiesConfiguration.unescapeJava("test\\,test"));
    }

    /**
     * Tests whether a footer comment is correctly written out.
     */
    @Test
    public void testWriteFooterComment() throws ConfigurationException, IOException {
        final String footer = "my footer";
        conf.clear();
        conf.setProperty(PROP_NAME, PROP_VALUE);
        conf.setFooter(footer);
        final StringWriter out = new StringWriter();
        conf.write(out);
        assertEquals(PROP_NAME + " = " + PROP_VALUE + CR + "# " + footer + CR, out.toString());
    }
}
