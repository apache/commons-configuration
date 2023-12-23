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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * test for loading and saving xml properties files
 */
public class TestXMLConfiguration {
    /**
     * A thread used for testing concurrent access to a builder.
     */
    private static final class ReloadThread extends Thread {
        private final FileBasedConfigurationBuilder<?> builder;

        ReloadThread(final FileBasedConfigurationBuilder<?> confBulder) {
            builder = confBulder;
        }

        @Override
        public void run() {
            for (int i = 0; i < LOOP_COUNT; i++) {
                builder.resetResult();
            }
        }
    }

    /** XML Catalog */
    private static final String CATALOG_FILES = ConfigurationAssert.getTestFile("catalog.xml").getAbsolutePath();

    /** Constant for the used encoding. */
    static final String ENCODING = StandardCharsets.ISO_8859_1.name();

    /** Constant for the test system ID. */
    static final String SYSTEM_ID = "properties.dtd";

    /** Constant for the test public ID. */
    static final String PUBLIC_ID = "-//Commons Configuration//DTD Test Configuration 1.3//EN";

    /** Constant for the DOCTYPE declaration. */
    static final String DOCTYPE_DECL = " PUBLIC \"" + PUBLIC_ID + "\" \"" + SYSTEM_ID + "\">";

    /** Constant for the DOCTYPE prefix. */
    static final String DOCTYPE = "<!DOCTYPE ";

    /** Constant for the transformer factory property. */
    static final String PROP_FACTORY = "javax.xml.transform.TransformerFactory";

    /** Constant for the number of test threads. */
    private static final int THREAD_COUNT = 5;
    /** Constant for the number of loops in tests with multiple threads. */
    private static final int LOOP_COUNT = 100;

    /**
     * Creates a new XMLConfiguration and loads the specified file.
     *
     * @param fileName the name of the file to be loaded
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    private static XMLConfiguration createFromFile(final String fileName) throws ConfigurationException {
        final XMLConfiguration config = new XMLConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        load(config, fileName);
        return config;
    }

    /**
     * Helper method for loading the specified configuration file.
     *
     * @param config the configuration
     * @param fileName the name of the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(final XMLConfiguration config, final String fileName) throws ConfigurationException {
        final FileHandler handler = new FileHandler(config);
        handler.setFileName(fileName);
        handler.load();
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /** The File that we test with */
    private final String testProperties = ConfigurationAssert.getTestFile("test.xml").getAbsolutePath();

    private final String testProperties2 = ConfigurationAssert.getTestFile("testDigesterConfigurationInclude1.xml").getAbsolutePath();

    private File testSaveConf;

    private File testSaveFile;

    private final String testFile2 = ConfigurationAssert.getTestFile("sample.xml").getAbsolutePath();

    private XMLConfiguration conf;

    /**
     * Helper method for testing whether a configuration was correctly saved to the default output file.
     *
     * @return the newly loaded configuration
     * @throws ConfigurationException if an error occurs
     */
    private XMLConfiguration checkSavedConfig() throws ConfigurationException {
        return checkSavedConfig(testSaveConf);
    }

    /**
     * Tests whether the saved configuration file matches the original data.
     *
     * @param saveFile the saved configuration file
     * @return the newly loaded configuration
     * @throws ConfigurationException if an error occurs
     */
    private XMLConfiguration checkSavedConfig(final File saveFile) throws ConfigurationException {
        final XMLConfiguration config = createFromFile(saveFile.getAbsolutePath());
        ConfigurationAssert.assertConfigurationEquals(conf, config);
        return config;
    }

    /**
     * Helper method for testing saving and loading a configuration when delimiter parsing is disabled.
     *
     * @param key the key to be checked
     * @throws ConfigurationException if an error occurs
     */
    private void checkSaveDelimiterParsingDisabled(final String key) throws ConfigurationException {
        conf.clear();
        conf.setListDelimiterHandler(new DisabledListDelimiterHandler());
        load(conf, testProperties);
        conf.setProperty(key, "C:\\Temp\\,C:\\Data\\");
        conf.addProperty(key, "a,b,c");
        saveTestConfig();
        final XMLConfiguration checkConf = new XMLConfiguration();
        checkConf.setListDelimiterHandler(conf.getListDelimiterHandler());
        load(checkConf, testSaveConf.getAbsolutePath());
        ConfigurationAssert.assertConfigurationEquals(conf, checkConf);
    }

    /**
     * Creates a validating document builder.
     *
     * @return the document builder
     * @throws ParserConfigurationException if an error occurs
     */
    private DocumentBuilder createValidatingDocBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler() {
            @Override
            public void error(final SAXParseException ex) throws SAXException {
                throw ex;
            }
        });
        return builder;
    }

    private Document parseXml(final String xml) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Removes the test output file if it exists.
     */
    private void removeTestFile() {
        if (testSaveConf.exists()) {
            assertTrue(testSaveConf.delete());
        }
    }

    /**
     * Helper method for saving the test configuration to the default output file.
     *
     * @throws ConfigurationException if an error occurs
     */
    private void saveTestConfig() throws ConfigurationException {
        final FileHandler handler = new FileHandler(conf);
        handler.save(testSaveConf);
    }

    @BeforeEach
    public void setUp() throws Exception {
        testSaveConf = newFile("testsave.xml", tempFolder);
        testSaveFile = newFile("testsample2.xml", tempFolder);
        conf = createFromFile(testProperties);
        removeTestFile();
    }

    @Test
    public void testAddList() {
        conf.addProperty("test.array", "value1");
        conf.addProperty("test.array", "value2");

        final List<Object> list = conf.getList("test.array");
        assertEquals(Arrays.asList("value1", "value2"), list);
    }

    /**
     * Tests saving a configuration after a node was added. Test for CONFIGURATION-294.
     */
    @Test
    public void testAddNodesAndSave() throws ConfigurationException {
        final ImmutableNode.Builder bldrNode = new ImmutableNode.Builder(1);
        bldrNode.addChild(NodeStructureHelper.createNode("child", null));
        bldrNode.addAttribute("attr", "");
        final ImmutableNode node2 = NodeStructureHelper.createNode("test2", null);
        conf.addNodes("add.nodes", Arrays.asList(bldrNode.name("test").create(), node2));
        saveTestConfig();
        conf.setProperty("add.nodes.test", "true");
        conf.setProperty("add.nodes.test.child", "yes");
        conf.setProperty("add.nodes.test[@attr]", "existing");
        conf.setProperty("add.nodes.test2", "anotherValue");
        saveTestConfig();
        final XMLConfiguration c2 = new XMLConfiguration();
        load(c2, testSaveConf.getAbsolutePath());
        assertEquals("true", c2.getString("add.nodes.test"));
        assertEquals("yes", c2.getString("add.nodes.test.child"));
        assertEquals("existing", c2.getString("add.nodes.test[@attr]"));
        assertEquals("anotherValue", c2.getString("add.nodes.test2"));
    }

    /**
     * Tests adding nodes from another configuration.
     */
    @Test
    public void testAddNodesCopy() throws ConfigurationException {
        final XMLConfiguration c2 = new XMLConfiguration();
        load(c2, testProperties2);
        conf.addNodes("copiedProperties", c2.getModel().getNodeHandler().getRootNode().getChildren());
        saveTestConfig();
        checkSavedConfig();
    }

    /**
     * Tests whether it is possible to add nodes to a XMLConfiguration through a SubnodeConfiguration and whether these
     * nodes have the correct type. This test is related to CONFIGURATION-472.
     */
    @Test
    public void testAddNodesToSubnodeConfiguration() throws Exception {
        final HierarchicalConfiguration<ImmutableNode> sub = conf.configurationAt("element2", true);
        sub.addProperty("newKey", "newvalue");
        assertEquals("newvalue", conf.getString("element2.newKey"));
    }

    @Test
    public void testAddObjectAttribute() {
        conf.addProperty("test.boolean[@value]", Boolean.TRUE);
        assertTrue(conf.getBoolean("test.boolean[@value]"));
    }

    @Test
    public void testAddObjectProperty() {
        // add a non string property
        conf.addProperty("test.boolean", Boolean.TRUE);
        assertTrue(conf.getBoolean("test.boolean"));
    }

    @Test
    public void testAddProperty() {
        // add a property to a non initialized xml configuration
        final XMLConfiguration config = new XMLConfiguration();
        config.addProperty("test.string", "hello");

        assertEquals("hello", config.getString("test.string"));
    }

    /**
     * Tests whether list properties are added correctly if delimiter parsing is disabled. This test is related to
     * CONFIGURATION-495.
     */
    @Test
    public void testAddPropertyListWithDelimiterParsingDisabled() throws ConfigurationException {
        conf.clear();
        final String prop = "delimiterListProp";
        conf.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        final List<String> list = Arrays.asList("val", "val2", "val3");
        conf.addProperty(prop, list);
        saveTestConfig();
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testSaveConf.getAbsolutePath());
        assertEquals(list, conf2.getProperty(prop));
    }

    /**
     * Tests if a second file can be appended to a first.
     */
    @Test
    public void testAppend() throws Exception {
        load(conf, testProperties2);
        assertEquals("value", conf.getString("element"));
        assertEquals("tasks", conf.getString("table.name"));

        saveTestConfig();
        conf = createFromFile(testSaveConf.getAbsolutePath());
        assertEquals("value", conf.getString("element"));
        assertEquals("tasks", conf.getString("table.name"));
        assertEquals("application", conf.getString("table[@tableType]"));
    }

    /**
     * Tries to create an attribute with multiple values. Only the first value is taken into account.
     */
    @Test
    public void testAttributeKeyWithMultipleValues() throws ConfigurationException {
        conf.addProperty("errorTest[@multiAttr]", Arrays.asList("v1", "v2"));
        saveTestConfig();
        final XMLConfiguration checkConfig = new XMLConfiguration();
        load(checkConfig, testSaveConf.getAbsolutePath());
        assertEquals("v1", checkConfig.getString("errorTest[@multiAttr]"));
    }

    /**
     * Tests whether the addNodes() method triggers an auto save.
     */
    @Test
    public void testAutoSaveAddNodes() throws ConfigurationException {
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(testProperties));
        conf = builder.getConfiguration();
        builder.getFileHandler().setFile(testSaveConf);
        builder.setAutoSave(true);
        final ImmutableNode node = NodeStructureHelper.createNode("addNodesTest", Boolean.TRUE);
        final Collection<ImmutableNode> nodes = new ArrayList<>(1);
        nodes.add(node);
        conf.addNodes("test.autosave", nodes);
        final XMLConfiguration c2 = new XMLConfiguration();
        load(c2, testSaveConf.getAbsolutePath());
        assertTrue(c2.getBoolean("test.autosave.addNodesTest"));
    }

    /**
     * Tests whether the auto save mechanism is triggered by changes at a subnode configuration.
     */
    @Test
    public void testAutoSaveWithSubnodeConfig() throws ConfigurationException {
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(testProperties));
        conf = builder.getConfiguration();
        builder.getFileHandler().setFile(testSaveConf);
        builder.setAutoSave(true);
        final String newValue = "I am autosaved";
        final Configuration sub = conf.configurationAt("element2.subelement", true);
        sub.setProperty("subsubelement", newValue);
        assertEquals(newValue, conf.getString("element2.subelement.subsubelement"));
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testSaveConf.getAbsolutePath());
        assertEquals(newValue, conf2.getString("element2.subelement.subsubelement"));
    }

    /**
     * Tests whether a subnode configuration created from another subnode configuration of a XMLConfiguration can trigger
     * the auto save mechanism.
     */
    @Test
    public void testAutoSaveWithSubSubnodeConfig() throws ConfigurationException {
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(testProperties));
        conf = builder.getConfiguration();
        builder.getFileHandler().setFile(testSaveConf);
        builder.setAutoSave(true);
        final String newValue = "I am autosaved";
        final HierarchicalConfiguration<?> sub1 = conf.configurationAt("element2", true);
        final HierarchicalConfiguration<?> sub2 = sub1.configurationAt("subelement", true);
        sub2.setProperty("subsubelement", newValue);
        assertEquals(newValue, conf.getString("element2.subelement.subsubelement"));
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testSaveConf.getAbsolutePath());
        assertEquals(newValue, conf2.getString("element2.subelement.subsubelement"));
    }

    @Test
    public void testClearAttributeMultipleDisjoined() throws Exception {
        String key = "clear.list.item[@id]";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
        key = "clear.list.item";
        assertNotNull(conf.getProperty(key));
        assertNotNull(conf.getProperty(key));
    }

    @Test
    public void testClearAttributeNonExisting() {
        final String key = "clear[@id]";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearAttributeSingle() {
        String key = "clear.element2[@id]";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
        key = "clear.element2";
        assertNotNull(conf.getProperty(key));
        assertNotNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertyCData() {
        final String key = "clear.cdata";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertyMultipleDisjoined() throws Exception {
        final String key = "list.item";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertyMultipleSiblings() {
        String key = "clear.list.item";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
        key = "clear.list.item[@id]";
        assertNotNull(conf.getProperty(key));
        assertNotNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertyNonText() {
        final String key = "clear.comment";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertyNotExisting() {
        final String key = "clearly";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertySingleElement() {
        final String key = "clear.element";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
    }

    @Test
    public void testClearPropertySingleElementWithAttribute() {
        String key = "clear.element2";
        conf.clearProperty(key);
        assertNull(conf.getProperty(key));
        assertNull(conf.getProperty(key));
        key = "clear.element2[@id]";
        assertNotNull(conf.getProperty(key));
        assertNotNull(conf.getProperty(key));
    }

    /**
     * Tests removing the text of the root element.
     */
    @Test
    public void testClearTextRootElement() throws ConfigurationException {
        final String xml = "<e a=\"v\">text</e>";
        conf.clear();
        final StringReader in = new StringReader(xml);
        final FileHandler handler = new FileHandler(conf);
        handler.load(in);
        assertEquals("text", conf.getString(""));

        conf.clearProperty("");
        saveTestConfig();
        checkSavedConfig();
    }

    /**
     * Tests the clone() method.
     */
    @Test
    public void testClone() {
        final Configuration c = (Configuration) conf.clone();
        final XMLConfiguration copy = assertInstanceOf(XMLConfiguration.class, c);
        assertNotNull(conf.getDocument());
        assertNull(copy.getDocument());

        copy.setProperty("element3", "clonedValue");
        assertEquals("value", conf.getString("element3"));
        conf.setProperty("element3[@name]", "originalFoo");
        assertEquals("foo", copy.getString("element3[@name]"));
    }

    /**
     * Tests saving a configuration after cloning to ensure that the clone and the original are completely detached.
     */
    @Test
    public void testCloneWithSave() throws ConfigurationException {
        final XMLConfiguration c = (XMLConfiguration) conf.clone();
        c.addProperty("test.newProperty", Boolean.TRUE);
        conf.addProperty("test.orgProperty", Boolean.TRUE);
        new FileHandler(c).save(testSaveConf);
        final XMLConfiguration c2 = new XMLConfiguration();
        load(c2, testSaveConf.getAbsolutePath());
        assertTrue(c2.getBoolean("test.newProperty"));
        assertFalse(c2.containsKey("test.orgProperty"));
    }

    /**
     * Tests access to tag names with delimiter characters.
     */
    @Test
    public void testComplexNames() {
        assertEquals("Name with dot", conf.getString("complexNames.my..elem"));
        assertEquals("Another dot", conf.getString("complexNames.my..elem.sub..elem"));
    }

    @Test
    public void testConcurrentGetAndReload() throws ConfigurationException, InterruptedException {
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(testProperties));
        XMLConfiguration config = builder.getConfiguration();
        assertNotNull(config.getProperty("test.short"));

        final Thread[] testThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < testThreads.length; ++i) {
            testThreads[i] = new ReloadThread(builder);
            testThreads[i].start();
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            config = builder.getConfiguration();
            assertNotNull(config.getProperty("test.short"));
        }

        for (final Thread testThread : testThreads) {
            testThread.join();
        }
    }

    /**
     * Tests the copy constructor for null input.
     */
    @Test
    public void testCopyNull() {
        conf = new XMLConfiguration(null);
        assertTrue(conf.isEmpty());
        assertEquals("configuration", conf.getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied when a configuration is created using the copy constructor.
     */
    @Test
    public void testCopyRootName() throws ConfigurationException {
        final String rootName = "rootElement";
        final String xml = "<" + rootName + "><test>true</test></" + rootName + ">";
        conf.clear();
        new FileHandler(conf).load(new StringReader(xml));
        XMLConfiguration copy = new XMLConfiguration(conf);
        assertEquals(rootName, copy.getRootElementName());
        new FileHandler(copy).save(testSaveConf);
        copy = new XMLConfiguration();
        load(copy, testSaveConf.getAbsolutePath());
        assertEquals(rootName, copy.getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied for a configuration for which not yet a document exists.
     */
    @Test
    public void testCopyRootNameNoDocument() throws ConfigurationException {
        final String rootName = "rootElement";
        conf = new XMLConfiguration();
        conf.setRootElementName(rootName);
        conf.setProperty("test", Boolean.TRUE);
        final XMLConfiguration copy = new XMLConfiguration(conf);
        assertEquals(rootName, copy.getRootElementName());
        new FileHandler(copy).save(testSaveConf);
        load(copy, testSaveConf.getAbsolutePath());
        assertEquals(rootName, copy.getRootElementName());
    }

    /**
     * Tests setting a custom document builder.
     */
    @Test
    public void testCustomDocBuilder() throws Exception {
        // Load an invalid XML file with the default (non validating)
        // doc builder. This should work...
        conf = new XMLConfiguration();
        load(conf, ConfigurationAssert.getTestFile("testValidateInvalid.xml").getAbsolutePath());
        assertEquals("customers", conf.getString("table.name"));
        assertFalse(conf.containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests whether a validating document builder detects a validation error.
     */
    @Test
    public void testCustomDocBuilderValidationError() throws Exception {
        final DocumentBuilder builder = createValidatingDocBuilder();
        conf = new XMLConfiguration();
        conf.setDocumentBuilder(builder);
        final String fileName = ConfigurationAssert.getTestFile("testValidateInvalid.xml").getAbsolutePath();
        assertThrows(ConfigurationException.class, () -> load(conf, fileName));
    }

    /**
     * Tests whether a valid document can be loaded with a validating document builder.
     */
    @Test
    public void testCustomDocBuilderValidationSuccess() throws Exception {
        final DocumentBuilder builder = createValidatingDocBuilder();
        conf = new XMLConfiguration();
        conf.setDocumentBuilder(builder);
        load(conf, ConfigurationAssert.getTestFile("testValidateValid.xml").getAbsolutePath());
        assertTrue(conf.containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests string properties with list delimiters when delimiter parsing is disabled
     */
    @Test
    public void testDelimiterParsingDisabled() throws ConfigurationException {
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testProperties);

        assertEquals("a,b,c", conf2.getString("split.list3[@values]"));
        assertEquals(0, conf2.getMaxIndex("split.list3[@values]"));
        assertEquals("a\\,b\\,c", conf2.getString("split.list4[@values]"));
        assertEquals("a,b,c", conf2.getString("split.list1"));
        assertEquals(0, conf2.getMaxIndex("split.list1"));
        assertEquals("a\\,b\\,c", conf2.getString("split.list2"));
    }

    /**
     * Tests whether string properties with list delimiters can be accessed if delimiter parsing is disabled and the XPath
     * expression engine is used.
     */
    @Test
    public void testDelimiterParsingDisabledXPath() throws ConfigurationException {
        final XMLConfiguration conf2 = new XMLConfiguration();
        conf2.setExpressionEngine(new XPathExpressionEngine());
        load(conf2, testProperties);

        assertEquals("a,b,c", conf2.getString("split/list3/@values"));
        assertEquals(0, conf2.getMaxIndex("split/list3/@values"));
        assertEquals("a\\,b\\,c", conf2.getString("split/list4/@values"));
        assertEquals("a,b,c", conf2.getString("split/list1"));
        assertEquals(0, conf2.getMaxIndex("split/list1"));
        assertEquals("a\\,b\\,c", conf2.getString("split/list2"));
    }

    /**
     * Tests whether a DTD can be accessed.
     */
    @Test
    public void testDtd() throws ConfigurationException {
        conf = new XMLConfiguration();
        load(conf, "testDtd.xml");
        assertEquals("value1", conf.getString("entry(0)"));
        assertEquals("test2", conf.getString("entry(1)[@key]"));
    }

    /**
     * Tests whether an attribute can be set to an empty string. This test is related to CONFIGURATION-446.
     */
    @Test
    public void testEmptyAttribute() throws ConfigurationException {
        final String key = "element3[@value]";
        conf.setProperty(key, "");
        assertTrue(conf.containsKey(key));
        assertEquals("", conf.getString(key));
        saveTestConfig();
        conf = new XMLConfiguration();
        load(conf, testSaveConf.getAbsolutePath());
        assertTrue(conf.containsKey(key));
        assertEquals("", conf.getString(key));
    }

    /**
     * Tests handling of empty elements.
     */
    @Test
    public void testEmptyElements() throws ConfigurationException {
        assertTrue(conf.containsKey("empty"));
        assertEquals("", conf.getString("empty"));
        conf.addProperty("empty2", "");
        conf.setProperty("empty", "no more empty");
        saveTestConfig();

        conf = new XMLConfiguration();
        load(conf, testSaveConf.getAbsolutePath());
        assertEquals("no more empty", conf.getString("empty"));
        assertEquals("", conf.getProperty("empty2"));
    }

    /**
     * Tests the isEmpty() method for an empty configuration that was reloaded.
     */
    @Test
    public void testEmptyReload() throws ConfigurationException {
        conf = new XMLConfiguration();
        assertTrue(conf.isEmpty());
        saveTestConfig();
        load(conf, testSaveConf.getAbsolutePath());
        assertTrue(conf.isEmpty());
    }

    @Test
    public void testGetAttribute() {
        assertEquals("foo", conf.getProperty("element3[@name]"));
    }

    @Test
    public void testGetCommentedProperty() {
        assertEquals("", conf.getProperty("test.comment"));
    }

    @Test
    public void testGetComplexProperty() {
        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    @Test
    public void testgetProperty() {
        // test non-leaf element
        Object property = conf.getProperty("clear");
        assertNull(property);

        // test non-existent element
        property = conf.getProperty("e");
        assertNull(property);

        // test non-existent element
        property = conf.getProperty("element3[@n]");
        assertNull(property);

        // test single element
        property = conf.getProperty("element");
        assertInstanceOf(String.class, property);
        assertEquals("value", property);

        // test single attribute
        property = conf.getProperty("element3[@name]");
        assertInstanceOf(String.class, property);
        assertEquals("foo", property);

        // test non-text/cdata element
        property = conf.getProperty("test.comment");
        assertEquals("", property);

        // test cdata element
        property = conf.getProperty("test.cdata");
        assertInstanceOf(String.class, property);
        assertEquals("<cdata value>", property);

        // test multiple sibling elements
        property = conf.getProperty("list.sublist.item");
        List<?> list = assertInstanceOf(List.class, property);
        assertEquals(Arrays.asList("five", "six"), list);

        // test multiple, disjoined elements
        property = conf.getProperty("list.item");
        list = assertInstanceOf(List.class, property);
        assertEquals(Arrays.asList("one", "two", "three", "four"), list);

        // test multiple, disjoined attributes
        property = conf.getProperty("list.item[@name]");
        list = assertInstanceOf(List.class, property);
        assertEquals(Arrays.asList("one", "three"), list);
    }

    @Test
    public void testGetProperty() {
        assertEquals("value", conf.getProperty("element"));
    }

    @Test
    public void testGetPropertyWithXMLEntity() {
        assertEquals("1<2", conf.getProperty("test.entity"));
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testInitCopy() throws ConfigurationException {
        final XMLConfiguration copy = new XMLConfiguration(conf);
        copy.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        assertEquals("value", copy.getProperty("element"));
        assertNull(copy.getDocument());

        new FileHandler(copy).save(testSaveConf);
        checkSavedConfig();
    }

    /**
     * Tests list nodes with multiple values and attributes.
     */
    @Test
    public void testListWithAttributes() {
        assertEquals(6, conf.getList("attrList.a").size());
        assertEquals("ABC", conf.getString("attrList.a(0)"));
        assertEquals("x", conf.getString("attrList.a(0)[@name]"));
        assertEquals(6, conf.getList("attrList.a[@name]").size());
    }

    /**
     * Tests a list node with attributes that has multiple values separated by the list delimiter. In this scenario the
     * attribute should be added to all list nodes.
     */
    @Test
    public void testListWithAttributesMultiValue() {
        assertEquals("1", conf.getString("attrList.a(1)"));
        assertEquals("y", conf.getString("attrList.a(1)[@name]"));
        for (int i = 1; i <= 3; i++) {
            assertEquals(i, conf.getInt("attrList.a(" + i + ")"));
            assertEquals("y", conf.getString("attrList.a(" + i + ")[@name]"));
        }
    }

    /**
     * Tests a list node with multiple values and multiple attributes. All attribute values should be assigned to all list
     * nodes.
     */
    @Test
    public void testListWithMultipleAttributesMultiValue() {
        for (int i = 1; i <= 2; i++) {
            final String idxStr = String.format("(%d)", Integer.valueOf(i + 3));
            final String nodeKey = "attrList.a" + idxStr;
            assertEquals("value" + i, conf.getString(nodeKey));
            assertEquals("u", conf.getString(nodeKey + "[@name]"));
            assertEquals("yes", conf.getString(nodeKey + "[@test]"));
        }
    }

    /**
     * Tests constructing an XMLConfiguration from a non existing file and later saving to this file.
     */
    @Test
    public void testLoadAndSaveFromFile() throws Exception {
        // If the file does not exist, an empty config is created
        assertFalse(testSaveConf.exists());
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class, null, true);
        builder.configure(new FileBasedBuilderParametersImpl().setFile(testSaveConf));
        conf = builder.getConfiguration();
        assertTrue(conf.isEmpty());
        conf.addProperty("test", "yes");
        builder.save();

        final XMLConfiguration checkConfig = createFromFile(testSaveConf.getAbsolutePath());
        assertEquals("yes", checkConfig.getString("test"));
    }

    @Test
    public void testLoadChildNamespace() throws ConfigurationException {
        conf = new XMLConfiguration();
        new FileHandler(conf).load(ConfigurationAssert.getTestFile("testChildNamespace.xml"));
        assertEquals("http://example.com/", conf.getString("foo:bar.[@xmlns:foo]"));
    }

    /**
     * Tests loading from a stream.
     */
    @Test
    public void testLoadFromStream() throws Exception {
        final String xml = "<?xml version=\"1.0\"?><config><test>1</test></config>";
        conf = new XMLConfiguration();
        FileHandler handler = new FileHandler(conf);
        handler.load(new ByteArrayInputStream(xml.getBytes()));
        assertEquals(1, conf.getInt("test"));

        conf = new XMLConfiguration();
        handler = new FileHandler(conf);
        handler.load(new ByteArrayInputStream(xml.getBytes()), "UTF8");
        assertEquals(1, conf.getInt("test"));
    }

    /**
     * Tests loading a non well formed XML from a string.
     */
    @Test
    public void testLoadInvalidXML() throws Exception {
        final String xml = "<?xml version=\"1.0\"?><config><test>1</rest></config>";
        conf = new XMLConfiguration();
        final FileHandler handler = new FileHandler(conf);
        final StringReader reader = new StringReader(xml);
        assertThrows(ConfigurationException.class, () -> handler.load(reader));
    }

    /**
     * Tests whether the encoding is correctly detected by the XML parser. This is done by loading an XML file with the
     * encoding "UTF-16". If this encoding is not detected correctly, an exception will be thrown that "Content is not
     * allowed in prolog". This test case is related to issue 34204.
     */
    @Test
    public void testLoadWithEncoding() throws ConfigurationException {
        conf = new XMLConfiguration();
        new FileHandler(conf).load(ConfigurationAssert.getTestFile("testEncoding.xml"));
        assertEquals("test3_yoge", conf.getString("yoge"));
    }

    @Test
    public void testLoadWithRootNamespace() throws ConfigurationException {
        conf = new XMLConfiguration();
        new FileHandler(conf).load(ConfigurationAssert.getTestFile("testRootNamespace.xml"));
        assertEquals("http://example.com/", conf.getString("[@xmlns:foo]"));
    }

    /**
     * Tests that attribute values are not split.
     */
    @Test
    public void testNoDelimiterParsingInAttrValues() throws ConfigurationException {
        conf.clear();
        load(conf, testProperties);
        final List<Object> expr = conf.getList("expressions[@value]");
        assertEquals(Arrays.asList("a || (b && c) | !d"), expr);
    }

    /**
     * Tests whether an attribute value can be overridden.
     */
    @Test
    public void testOverrideAttribute() {
        conf.addProperty("element3[@name]", "bar");

        final List<Object> list = conf.getList("element3[@name]");
        assertEquals(Arrays.asList("bar"), list);
    }

    /**
     * Tests whether spaces are preserved when the xml:space attribute is set.
     */
    @Test
    public void testPreserveSpace() {
        assertEquals(" ", conf.getString("space.blank"));
        assertEquals(" * * ", conf.getString("space.stars"));
    }

    /**
     * Tests an xml:space attribute with an invalid value. This will be interpreted as default.
     */
    @Test
    public void testPreserveSpaceInvalid() {
        assertEquals("Some other text", conf.getString("space.testInvalid"));
    }

    /**
     * Tests whether the xml:space attribute works directly on the current element. This test is related to
     * CONFIGURATION-555.
     */
    @Test
    public void testPreserveSpaceOnElement() {
        assertEquals(" preserved ", conf.getString("spaceElement"));
        assertEquals("   ", conf.getString("spaceBlankElement"));
    }

    /**
     * Tests whether the xml:space attribute can be overridden in nested elements.
     */
    @Test
    public void testPreserveSpaceOverride() {
        assertEquals("Some text", conf.getString("space.description"));
    }

    /**
     * Tests whether the public ID is accessed in a synchronized manner.
     */
    @Test
    public void testPublicIdSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setPublicID(PUBLIC_ID);
        assertEquals(PUBLIC_ID, conf.getPublicID());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests a direct invocation of the read() method. This is not allowed because certain initializations have not been
     * done. This test is related to CONFIGURATION-641.
     */
    @Test
    public void testReadCalledDirectly() {
        conf = new XMLConfiguration();
        final String content = "<configuration><test>1</test></configuration>";
        final ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
        final ConfigurationException e = assertThrows(ConfigurationException.class, () -> conf.read(bis));
        assertThat(e.getMessage(), containsString("FileHandler"));
    }

    @Test
    public void testSave() throws Exception {
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        for (int i = 1; i < 5; i++) {
            conf.addProperty("test.array", "value" + i);
        }

        // add comma delimited lists with escaped delimiters
        conf.addProperty("split.list5", "a\\,b\\,c");
        conf.setProperty("element3", "value\\,value1\\,value2");
        conf.setProperty("element3[@name]", "foo\\,bar");

        // save the configuration
        saveTestConfig();

        // read the configuration and compare the properties
        checkSavedConfig();
    }

    /**
     * Tests saving a configuration that was created from a hierarchical configuration. This test exposes bug
     * CONFIGURATION-301.
     */
    @Test
    public void testSaveAfterCreateWithCopyConstructor() throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> hc = conf.configurationAt("element2");
        conf = new XMLConfiguration(hc);
        saveTestConfig();
        final XMLConfiguration checkConfig = checkSavedConfig();
        assertEquals("element2", checkConfig.getRootElementName());
    }

    /**
     * Tests saving attributes (related to issue 34442).
     */
    @Test
    public void testSaveAttributes() throws Exception {
        conf.clear();
        load(conf, testProperties);
        saveTestConfig();
        conf = new XMLConfiguration();
        load(conf, testSaveConf.getAbsolutePath());
        assertEquals("foo", conf.getString("element3[@name]"));
    }

    /**
     * Tests saving and loading a configuration when delimiter parsing is disabled.
     */
    @Test
    public void testSaveDelimiterParsingDisabled() throws ConfigurationException {
        checkSaveDelimiterParsingDisabled("list.delimiter.test");
    }

    /**
     * Tests saving to a stream.
     */
    @Test
    public void testSaveToStream() throws ConfigurationException, IOException {
        final FileHandler handler = new FileHandler(conf);
        try (FileOutputStream out = new FileOutputStream(testSaveConf)) {
            handler.save(out, "UTF8");
        }

        checkSavedConfig(testSaveConf);
    }

    /**
     * Tests whether a configuration can be saved to a stream with a specific encoding.
     */
    @Test
    public void testSaveToStreamWithEncoding() throws ConfigurationException, IOException {
        final FileHandler handler = new FileHandler(conf);
        handler.setEncoding("UTF8");
        try (FileOutputStream out = new FileOutputStream(testSaveConf)) {
            handler.save(out);
        }

        checkSavedConfig(testSaveConf);
    }

    /**
     * Tests saving to a URL.
     */
    @Test
    public void testSaveToURL() throws Exception {
        final FileHandler handler = new FileHandler(conf);
        handler.save(testSaveConf.toURI().toURL());
        checkSavedConfig(testSaveConf);
    }

    /**
     * Tests whether a windows path can be saved correctly. This test is related to CONFIGURATION-428.
     */
    @Test
    public void testSaveWindowsPath() throws ConfigurationException {
        conf.clear();
        conf.setListDelimiterHandler(new DisabledListDelimiterHandler());
        conf.addProperty("path", "C:\\Temp");
        final StringWriter writer = new StringWriter();
        new FileHandler(conf).save(writer);
        final String content = writer.toString();
        assertThat("Path not found: ", content, containsString("<path>C:\\Temp</path>"));
        saveTestConfig();
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testSaveConf.getAbsolutePath());
        assertEquals("C:\\Temp", conf2.getString("path"));
    }

    /**
     * Tests string properties with list delimiters when delimiter parsing is disabled
     */
    @Test
    public void testSaveWithDelimiterParsingDisabled() throws ConfigurationException {
        conf = new XMLConfiguration();
        conf.setExpressionEngine(new XPathExpressionEngine());
        load(conf, testProperties);

        assertEquals("a,b,c", conf.getString("split/list3/@values"));
        assertEquals(0, conf.getMaxIndex("split/list3/@values"));
        assertEquals("a\\,b\\,c", conf.getString("split/list4/@values"));
        assertEquals("a,b,c", conf.getString("split/list1"));
        assertEquals(0, conf.getMaxIndex("split/list1"));
        assertEquals("a\\,b\\,c", conf.getString("split/list2"));
        // save the configuration
        saveTestConfig();

        XMLConfiguration config = new XMLConfiguration();
        // config.setExpressionEngine(new XPathExpressionEngine());
        load(config, testFile2);
        config.setProperty("Employee[@attr1]", "3,2,1");
        assertEquals("3,2,1", config.getString("Employee[@attr1]"));
        new FileHandler(config).save(testSaveFile);
        config = new XMLConfiguration();
        // config.setExpressionEngine(new XPathExpressionEngine());
        load(config, testSaveFile.getAbsolutePath());
        config.setProperty("Employee[@attr1]", "1,2,3");
        assertEquals("1,2,3", config.getString("Employee[@attr1]"));
        config.setProperty("Employee[@attr2]", "one, two, three");
        assertEquals("one, two, three", config.getString("Employee[@attr2]"));
        config.setProperty("Employee.text", "a,b,d");
        assertEquals("a,b,d", config.getString("Employee.text"));
        config.setProperty("Employee.Salary", "100,000");
        assertEquals("100,000", config.getString("Employee.Salary"));
        new FileHandler(config).save(testSaveFile);
        final XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setExpressionEngine(new XPathExpressionEngine());
        load(checkConfig, testSaveFile.getAbsolutePath());
        assertEquals("1,2,3", checkConfig.getString("Employee/@attr1"));
        assertEquals("one, two, three", checkConfig.getString("Employee/@attr2"));
        assertEquals("a,b,d", checkConfig.getString("Employee/text"));
        assertEquals("100,000", checkConfig.getString("Employee/Salary"));
    }

    /**
     * Tests whether the DOCTYPE survives a save operation.
     */
    @Test
    public void testSaveWithDoctype() throws ConfigurationException {
        conf = new XMLConfiguration();
        load(conf, "testDtdPublic.xml");

        assertEquals(PUBLIC_ID, conf.getPublicID());
        assertEquals(SYSTEM_ID, conf.getSystemID());
        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        assertThat(out.toString(), containsString(DOCTYPE));
    }

    /**
     * Tests setting public and system IDs for the DOCTYPE and then saving the configuration. This should generate a DOCTYPE
     * declaration.
     */
    @Test
    public void testSaveWithDoctypeIDs() throws ConfigurationException {
        assertNull(conf.getPublicID());
        assertNull(conf.getSystemID());
        conf.setPublicID(PUBLIC_ID);
        conf.setSystemID(SYSTEM_ID);
        final StringWriter out = new StringWriter();
        new FileHandler(conf).save(out);
        assertThat(out.toString(), containsString(DOCTYPE + "testconfig" + DOCTYPE_DECL));
    }

    /**
     * Tests whether the encoding is written to the generated XML file.
     */
    @Test
    public void testSaveWithEncoding() throws ConfigurationException {
        conf = new XMLConfiguration();
        conf.setProperty("test", "a value");
        final FileHandler handler = new FileHandler(conf);
        handler.setEncoding(ENCODING);

        final StringWriter out = new StringWriter();
        handler.save(out);
        assertThat(out.toString(), containsString("encoding=\"" + ENCODING + "\""));
    }

    /**
     * Tests saving a configuration if an invalid transformer factory is specified. In this case an error is thrown by the
     * transformer factory. XMLConfiguration should not catch this error.
     */
    @Test
    public void testSaveWithInvalidTransformerFactory() {
        System.setProperty(PROP_FACTORY, "an.invalid.Class");
        try {
            assertThrows(TransformerFactoryConfigurationError.class, this::saveTestConfig);
        } finally {
            System.getProperties().remove(PROP_FACTORY);
        }
    }

    /**
     * Tests whether a default encoding is used if no specific encoding is set. According to the XSLT specification
     * (http://www.w3.org/TR/xslt#output) this should be either UTF-8 or UTF-16.
     */
    @Test
    public void testSaveWithNullEncoding() throws ConfigurationException {
        conf = new XMLConfiguration();
        conf.setProperty("testNoEncoding", "yes");
        final FileHandler handler = new FileHandler(conf);

        final StringWriter out = new StringWriter();
        handler.save(out);
        assertThat("Encoding was written to file", out.toString(), containsString("encoding=\"UTF-"));
    }

    @Test
    public void testSaveWithRootAttributes() throws ConfigurationException {
        conf.setProperty("[@xmlns:ex]", "http://example.com/");
        assertEquals("http://example.com/", conf.getString("[@xmlns:ex]"));
        final FileHandler handler = new FileHandler(conf);

        final StringWriter out = new StringWriter();
        handler.save(out);
        assertThat("Encoding was not written to file", out.toString(), containsString("testconfig xmlns:ex=\"http://example.com/\""));
    }

    @Test
    public void testSaveWithRootAttributes_ByHand() throws ConfigurationException {
        conf = new XMLConfiguration();
        conf.addProperty("[@xmlns:foo]", "http://example.com/");
        assertEquals("http://example.com/", conf.getString("[@xmlns:foo]"));
        final FileHandler handler = new FileHandler(conf);

        final StringWriter out = new StringWriter();
        handler.save(out);
        assertThat("Encoding was not written to file", out.toString(), containsString("configuration xmlns:foo=\"http://example.com/\""));
    }

    /**
     * Tests modifying an XML document and saving it with schema validation enabled.
     */
    @Test
    public void testSaveWithValidation() throws Exception {
        final CatalogResolver resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        conf = new XMLConfiguration();
        conf.setEntityResolver(resolver);
        conf.setSchemaValidation(true);
        load(conf, testFile2);
        conf.setProperty("Employee.SSN", "123456789");
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.validate();
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
        saveTestConfig();
        conf = new XMLConfiguration();
        load(conf, testSaveConf.getAbsolutePath());
        assertEquals("123456789", conf.getString("Employee.SSN"));
    }

    /**
     * Tests modifying an XML document and validating it against the schema.
     */
    @Test
    public void testSaveWithValidationFailure() throws Exception {
        final CatalogResolver resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        conf = new XMLConfiguration();
        conf.setEntityResolver(resolver);
        conf.setSchemaValidation(true);
        load(conf, testFile2);
        conf.setProperty("Employee.Email", "JohnDoe@test.org");
        final Exception e = assertThrows(Exception.class, conf::validate);
        final Throwable cause = e.getCause();
        assertInstanceOf(SAXParseException.class, cause);
    }

    @Test
    public void testSetAttribute() {
        // replace an existing attribute
        conf.setProperty("element3[@name]", "bar");
        assertEquals("bar", conf.getProperty("element3[@name]"));

        // set a new attribute
        conf.setProperty("foo[@bar]", "value");
        assertEquals("value", conf.getProperty("foo[@bar]"));

        conf.setProperty("name1", "value1");
        assertEquals("value1", conf.getProperty("name1"));
    }

    @Test
    public void testSetProperty() throws Exception {
        conf.setProperty("element.string", "hello");

        assertEquals("hello", conf.getString("element.string"));
        assertEquals("hello", conf.getProperty("element.string"));
    }

    /**
     * Tests whether list properties are set correctly if delimiter parsing is disabled. This test is related to
     * CONFIGURATION-495.
     */
    @Test
    public void testSetPropertyListWithDelimiterParsingDisabled() throws ConfigurationException {
        final String prop = "delimiterListProp";
        final List<String> list = Arrays.asList("val", "val2", "val3");
        conf.setProperty(prop, list);
        saveTestConfig();
        final XMLConfiguration conf2 = new XMLConfiguration();
        load(conf2, testSaveConf.getAbsolutePath());
        assertEquals(list, conf2.getProperty(prop));
    }

    /**
     * Tests setting an attribute on the root element.
     */
    @Test
    public void testSetRootAttribute() throws ConfigurationException {
        conf.setProperty("[@test]", "true");
        assertEquals("true", conf.getString("[@test]"));
        saveTestConfig();
        XMLConfiguration checkConf = checkSavedConfig();
        assertTrue(checkConf.containsKey("[@test]"));
        checkConf.setProperty("[@test]", "newValue");
        conf = checkConf;
        saveTestConfig();
        checkConf = checkSavedConfig();
        assertEquals("newValue", checkConf.getString("[@test]"));
    }

    @Test
    public void testSetRootNamespace() throws ConfigurationException {
        conf.addProperty("[@xmlns:foo]", "http://example.com/");
        conf.addProperty("foo:bar", "foobar");
        assertEquals("http://example.com/", conf.getString("[@xmlns:foo]"));
        saveTestConfig();
        final XMLConfiguration checkConf = checkSavedConfig();
        assertTrue(checkConf.containsKey("[@xmlns:foo]"));
        checkConf.setProperty("[@xmlns:foo]", "http://example.net/");
    }

    /**
     * Tests setting text of the root element.
     */
    @Test
    public void testSetTextRootElement() throws ConfigurationException {
        conf.setProperty("", "Root text");
        saveTestConfig();
        checkSavedConfig();
    }

    /**
     * Tests string properties with list delimiters and escaped delimiters.
     */
    @Test
    public void testSplitLists() {
        assertEquals("a,b,c", conf.getString("split.list3[@values]"));
        assertEquals(0, conf.getMaxIndex("split.list3[@values]"));
        assertEquals("a\\,b\\,c", conf.getString("split.list4[@values]"));
        assertEquals("a", conf.getString("split.list1"));
        assertEquals(2, conf.getMaxIndex("split.list1"));
        assertEquals("a,b,c", conf.getString("split.list2"));
    }

    /**
     * Tests the subset() method. There was a bug that calling subset() had undesired side effects.
     */
    @Test
    public void testSubset() throws ConfigurationException {
        conf = new XMLConfiguration();
        load(conf, "testHierarchicalXMLConfiguration.xml");
        conf.subset("tables.table(0)");
        saveTestConfig();

        conf = new XMLConfiguration();
        load(conf, "testHierarchicalXMLConfiguration.xml");
        assertEquals("users", conf.getString("tables.table(0).name"));
    }

    /**
     * Tests whether the system ID is accessed in a synchronized manner.
     */
    @Test
    public void testSystemIdSynchronized() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        conf.setSynchronizer(sync);
        conf.setSystemID(SYSTEM_ID);
        assertEquals(SYSTEM_ID, conf.getSystemID());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests DTD validation using the setValidating() method.
     */
    @Test
    public void testValidating() throws ConfigurationException {
        final File nonValidFile = ConfigurationAssert.getTestFile("testValidateInvalid.xml");
        conf = new XMLConfiguration();
        assertFalse(conf.isValidating());

        // Load a non valid XML document. Should work for isValidating() == false
        load(conf, nonValidFile.getAbsolutePath());
        assertEquals("customers", conf.getString("table.name"));
        assertFalse(conf.containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests whether an invalid file is detected when validating is enabled.
     */
    @Test
    public void testValidatingInvalidFile() {
        conf = new XMLConfiguration();
        conf.setValidating(true);
        assertThrows(ConfigurationException.class, () -> load(conf, "testValidateInvalid.xml"));
    }

    @Test
    public void testWrite() throws Exception {
        final XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setRootElementName("IAmRoot");
        final StringWriter sw = new StringWriter();
        xmlConfig.write(sw);
        // Check that we can parse the XML.
        assertNotNull(parseXml(sw.toString()));
    }

    @Test
    public void testWriteIndentSize() throws Exception {
        final XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setRootElementName("IAmRoot");
        final StringWriter sw = new StringWriter();
        xmlConfig.setProperty("Child", "Alexander");
        xmlConfig.write(sw);
        // Check that we can parse the XML.
        final String xml = sw.toString();
        assertNotNull(parseXml(xml));
        final String indent = StringUtils.repeat(' ', XMLConfiguration.DEFAULT_INDENT_SIZE);
        assertTrue(xml.contains(System.lineSeparator() + indent + "<Child>"));
    }

    @Test
    public void testWriteWithTransformer() throws Exception {
        final XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setRootElementName("IAmRoot");
        xmlConfig.setProperty("Child", "Alexander");
        final StringWriter sw = new StringWriter();
        final Transformer transformer = xmlConfig.createTransformer();
        final int indentSize = 8;
        transformer.setOutputProperty(XMLConfiguration.INDENT_AMOUNT_PROPERTY, Integer.toString(indentSize));
        xmlConfig.write(sw, transformer);
        final String xml = sw.toString();
        assertNotNull(parseXml(xml));
        final String indent = StringUtils.repeat(' ', indentSize);
        assertTrue(xml.contains(System.lineSeparator() + indent + "<Child>"));
    }

    /**
     * Tests accessing properties when the XPATH expression engine is set.
     */
    @Test
    public void testXPathExpressionEngine() {
        conf.setExpressionEngine(new XPathExpressionEngine());
        assertEquals("foo\"bar", conf.getString("test[1]/entity/@name"));
        conf.clear();
        assertNull(conf.getString("test[1]/entity/@name"));
    }
}
