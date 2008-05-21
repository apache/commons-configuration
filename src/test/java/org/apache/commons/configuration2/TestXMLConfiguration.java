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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.xpath.XPathExpressionEngine;
import org.apache.commons.configuration2.reloading.FileAlwaysReloadingStrategy;
import org.apache.commons.configuration2.reloading.InvariantReloadingStrategy;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * test for loading and saving xml properties files
 *
 * @version $Id$
 */
public class TestXMLConfiguration extends TestCase
{
    /** Constant for the used encoding.*/
    static final String ENCODING = "ISO-8859-1";

    /** Constant for the test system ID.*/
    static final String SYSTEM_ID = "properties.dtd";

    /** Constant for the test public ID.*/
    static final String PUBLIC_ID = "-//Commons Configuration//DTD Test Configuration 1.3//EN";

    /** Constant for the DOCTYPE declaration.*/
    static final String DOCTYPE_DECL = " PUBLIC \"" + PUBLIC_ID + "\" \"" + SYSTEM_ID + "\">";

    /** Constant for the DOCTYPE prefix.*/
    static final String DOCTYPE = "<!DOCTYPE ";

    /** Constant for the transformer factory property.*/
    static final String PROP_FACTORY = "javax.xml.transform.TransformerFactory";

    /** The File that we test with */
    private String testProperties = ConfigurationAssert.getTestFile("test.xml").getAbsolutePath();
    private String testProperties2 = ConfigurationAssert.getTestFile("testDigesterConfigurationInclude1.xml").getAbsolutePath();
    private String testBasePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();
    private File testSaveConf = ConfigurationAssert.getOutFile("testsave.xml");

    private XMLConfiguration conf;

    protected void setUp() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFile(new File(testProperties));
        conf.load();
        removeTestFile();
    }

    public void testGetProperty()
    {
        assertEquals("value", conf.getProperty("element"));
    }

    public void testGetCommentedProperty()
    {
        assertEquals("", conf.getProperty("test.comment"));
    }

    public void testGetPropertyWithXMLEntity()
    {
        assertEquals("1<2", conf.getProperty("test.entity"));
    }

    public void testClearProperty() throws ConfigurationException, IOException
    {
        // test non-existent element
        String key = "clearly";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));

        // test single element
        conf.load();
        key = "clear.element";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));

        // test single element with attribute
        conf.load();
        key = "clear.element2";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));
        key = "clear.element2[@id]";
        assertNotNull(key, conf.getProperty(key));
        assertNotNull(key, conf.getProperty(key));

        // test non-text/cdata element
        conf.load();
        key = "clear.comment";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));

        // test cdata element
        conf.load();
        key = "clear.cdata";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));

        // test multiple sibling elements
        conf.load();
        key = "clear.list.item";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));
        key = "clear.list.item[@id]";
        assertNotNull(key, conf.getProperty(key));
        assertNotNull(key, conf.getProperty(key));

        // test multiple, disjoined elements
        conf.load();
        key = "list.item";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));
    }

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
        assertNotNull(property);
        assertTrue(property instanceof String);
        assertEquals("value", property);

        // test single attribute
        property = conf.getProperty("element3[@name]");
        assertNotNull(property);
        assertTrue(property instanceof String);
        assertEquals("foo", property);

        // test non-text/cdata element
        property = conf.getProperty("test.comment");
        assertEquals("", property);

        // test cdata element
        property = conf.getProperty("test.cdata");
        assertNotNull(property);
        assertTrue(property instanceof String);
        assertEquals("<cdata value>", property);

        // test multiple sibling elements
        property = conf.getProperty("list.sublist.item");
        assertNotNull(property);
        assertTrue(property instanceof List);
        List<?> list = (List<?>)property;
        assertEquals(2, list.size());
        assertEquals("five", list.get(0));
        assertEquals("six", list.get(1));

        // test multiple, disjoined elements
        property = conf.getProperty("list.item");
        assertNotNull(property);
        assertTrue(property instanceof List);
        list = (List<?>)property;
        assertEquals(4, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
        assertEquals("four", list.get(3));

        // test multiple, disjoined attributes
        property = conf.getProperty("list.item[@name]");
        assertNotNull(property);
        assertTrue(property instanceof List);
        list = (List<?>)property;
        assertEquals(2, list.size());
        assertEquals("one", list.get(0));
        assertEquals("three", list.get(1));
    }

    public void testGetAttribute()
    {
        assertEquals("element3[@name]", "foo", conf.getProperty("element3[@name]"));
    }

    public void testClearAttribute() throws Exception
    {
        // test non-existent attribute
        String key = "clear[@id]";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));

        // test single attribute
        conf.load();
        key = "clear.element2[@id]";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));
        key = "clear.element2";
        assertNotNull(key, conf.getProperty(key));
        assertNotNull(key, conf.getProperty(key));

        // test multiple, disjoined attributes
        conf.load();
        key = "clear.list.item[@id]";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        assertNull(key, conf.getProperty(key));
        key = "clear.list.item";
        assertNotNull(key, conf.getProperty(key));
        assertNotNull(key, conf.getProperty(key));
    }

    public void testSetAttribute()
    {
        // replace an existing attribute
        conf.setProperty("element3[@name]", "bar");
        assertEquals("element3[@name]", "bar", conf.getProperty("element3[@name]"));

        // set a new attribute
        conf.setProperty("foo[@bar]", "value");
        assertEquals("foo[@bar]", "value", conf.getProperty("foo[@bar]"));

        conf.setProperty("name1","value1");
        assertEquals("value1",conf.getProperty("name1"));
    }

    public void testAddAttribute()
    {
        conf.addProperty("element3[@name]", "bar");

        List<?> list = conf.getList("element3[@name]");
        assertNotNull("null list", list);
        assertTrue("'foo' element missing", list.contains("foo"));
        assertTrue("'bar' element missing", list.contains("bar"));
        assertEquals("list size", 2, list.size());
    }

    public void testAddObjectAttribute()
    {
        conf.addProperty("test.boolean[@value]", Boolean.TRUE);
        assertTrue("test.boolean[@value]", conf.getBoolean("test.boolean[@value]"));
    }

    /**
     * Tests setting an attribute on the root element.
     */
    public void testSetRootAttribute() throws ConfigurationException
    {
        conf.setProperty("[@test]", "true");
        assertEquals("Root attribute not set", "true", conf
                .getString("[@test]"));
        conf.save(testSaveConf);
        XMLConfiguration checkConf = new XMLConfiguration();
        checkConf.setFile(testSaveConf);
        checkSavedConfig(checkConf);
        assertTrue("Attribute not found after save", checkConf
                .containsKey("[@test]"));
        checkConf.setProperty("[@test]", "newValue");
        checkConf.save();
        conf = checkConf;
        checkConf = new XMLConfiguration();
        checkConf.setFile(testSaveConf);
        checkSavedConfig(checkConf);
        assertEquals("Attribute not modified after save", "newValue", checkConf
                .getString("[@test]"));
    }

    /**
     * Tests whether the configuration's root node is initialized with a
     * reference to the corresponding XML element.
     */
    public void testGetRootReference()
    {
        assertNotNull("Root node has no reference", conf.getRootNode()
                .getReference());
    }

    public void testAddList()
    {
        conf.addProperty("test.array", "value1");
        conf.addProperty("test.array", "value2");

        List<?> list = conf.getList("test.array");
        assertNotNull("null list", list);
        assertTrue("'value1' element missing", list.contains("value1"));
        assertTrue("'value2' element missing", list.contains("value2"));
        assertEquals("list size", 2, list.size());
    }

    public void testGetComplexProperty()
    {
        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    public void testSettingFileNames()
    {
        conf = new XMLConfiguration();
        conf.setFileName(testProperties);
        assertEquals(testProperties.toString(), conf.getFileName());

        conf.setBasePath(testBasePath);
        conf.setFileName("hello.xml");
        assertEquals("hello.xml", conf.getFileName());
        assertEquals(testBasePath.toString(), conf.getBasePath());
        assertEquals(new File(testBasePath, "hello.xml"), conf.getFile());

        conf.setBasePath(testBasePath);
        conf.setFileName("subdir/hello.xml");
        assertEquals("subdir/hello.xml", conf.getFileName());
        assertEquals(testBasePath.toString(), conf.getBasePath());
        assertEquals(new File(testBasePath, "subdir/hello.xml"), conf.getFile());
    }

    public void testLoad() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFileName(testProperties);
        conf.load();

        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadWithBasePath() throws Exception
    {
        conf = new XMLConfiguration();

        conf.setFileName("test.xml");
        conf.setBasePath(testBasePath);
        conf.load();

        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    /**
     * Tests constructing an XMLConfiguration from a non existing file and
     * later saving to this file.
     */
    public void testLoadAndSaveFromFile() throws Exception
    {
        // If the file does not exist, an empty config is created
        conf = new XMLConfiguration(testSaveConf);
        assertTrue(conf.isEmpty());
        conf.addProperty("test", "yes");
        conf.save();

        conf = new XMLConfiguration(testSaveConf);
        assertEquals("yes", conf.getString("test"));
    }

    /**
     * Tests loading a configuration from a URL.
     */
    public void testLoadFromURL() throws Exception
    {
        URL url = new File(testProperties).toURL();
        conf = new XMLConfiguration(url);
        assertEquals("value", conf.getProperty("element"));
        assertEquals(url, conf.getURL());
    }

    /**
     * Tests loading from a stream.
     */
    public void testLoadFromStream() throws Exception
    {
        String xml = "<?xml version=\"1.0\"?><config><test>1</test></config>";
        conf = new XMLConfiguration();
        conf.load(new ByteArrayInputStream(xml.getBytes()));
        assertEquals(1, conf.getInt("test"));

        conf = new XMLConfiguration();
        conf.load(new ByteArrayInputStream(xml.getBytes()), "UTF8");
        assertEquals(1, conf.getInt("test"));
    }

    /**
     * Tests loading a non well formed XML from a string.
     */
    public void testLoadInvalidXML() throws Exception
    {
        String xml = "<?xml version=\"1.0\"?><config><test>1</rest></config>";
        conf = new XMLConfiguration();
        try
        {
            conf.load(new StringReader(xml));
            fail("Could load invalid XML!");
        }
        catch(ConfigurationException cex)
        {
            //ok
        }
    }

    public void testSetProperty() throws Exception
    {
        conf.setProperty("element.string", "hello");

        assertEquals("'element.string'", "hello", conf.getString("element.string"));
        assertEquals("XML value of element.string", "hello", conf.getProperty("element.string"));
    }

    public void testAddProperty()
    {
        // add a property to a non initialized xml configuration
        XMLConfiguration config = new XMLConfiguration();
        config.addProperty("test.string", "hello");

        assertEquals("'test.string'", "hello", config.getString("test.string"));
    }

    public void testAddObjectProperty()
    {
        // add a non string property
        conf.addProperty("test.boolean", Boolean.TRUE);
        assertTrue("'test.boolean'", conf.getBoolean("test.boolean"));
    }

    public void testSave() throws Exception
    {
        // add an array of strings to the configuration
        conf.addProperty("string", "value1");
        for (int i = 1; i < 5; i++)
        {
            conf.addProperty("test.array", "value" + i);
        }

        // add an array of strings in an attribute
        for (int i = 1; i < 5; i++)
        {
           conf.addProperty("test.attribute[@array]", "value" + i);
        }

        // add comma delimited lists with escaped delimiters
        conf.addProperty("split.list5", "a\\,b\\,c");
        conf.setProperty("element3", "value\\,value1\\,value2");
        conf.setProperty("element3[@name]", "foo\\,bar");

        // save the configuration
        conf.save(testSaveConf.getAbsolutePath());

        // read the configuration and compare the properties
        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFileName(testSaveConf.getAbsolutePath());
        checkSavedConfig(checkConfig);
    }

    /**
     * Tests saving to a URL.
     */
    public void testSaveToURL() throws Exception
    {
        conf.save(testSaveConf.toURL());
        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFile(testSaveConf);
        checkSavedConfig(checkConfig);
    }

    /**
     * Tests saving to a stream.
     */
    public void testSaveToStream() throws Exception
    {
        assertNull(conf.getEncoding());
        conf.setEncoding("UTF8");
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(testSaveConf);
            conf.save(out);
        }
        finally
        {
            if(out != null)
            {
                out.close();
            }
        }

        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFile(testSaveConf);
        checkSavedConfig(checkConfig);

        try
        {
            out = new FileOutputStream(testSaveConf);
            conf.save(out, "UTF8");
        }
        finally
        {
            if(out != null)
            {
                out.close();
            }
        }

        checkConfig.clear();
        checkSavedConfig(checkConfig);
    }

    public void testAutoSave() throws Exception
    {
        conf.setFile(testSaveConf);
        assertFalse(conf.isAutoSave());
        conf.setAutoSave(true);
        assertTrue(conf.isAutoSave());
        conf.setProperty("autosave", "ok");

        // reload the configuration
        XMLConfiguration conf2 = new XMLConfiguration(conf.getFile());
        assertEquals("'autosave' property", "ok", conf2.getString("autosave"));

        conf.clearTree("clear");
        conf2 = new XMLConfiguration(conf.getFile());
        Configuration sub = conf2.subset("clear");
        assertTrue(sub.isEmpty());
    }

    /**
     * Tests if a second file can be appended to a first.
     */
    public void testAppend() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFileName(testProperties);
        conf.load();
        conf.load(testProperties2);
        assertEquals("value", conf.getString("element"));
        assertEquals("tasks", conf.getString("table.name"));

        conf.save(testSaveConf);
        conf = new XMLConfiguration(testSaveConf);
        assertEquals("value", conf.getString("element"));
        assertEquals("tasks", conf.getString("table.name"));
        assertEquals("application", conf.getString("table[@tableType]"));
    }

    /**
     * Tests saving attributes (related to issue 34442).
     */
    public void testSaveAttributes() throws Exception
    {
        conf.clear();
        conf.load();
        conf.save(testSaveConf);
        conf = new XMLConfiguration();
        conf.load(testSaveConf);
        assertEquals("foo", conf.getString("element3[@name]"));
    }

    /**
     * Tests collaboration between XMLConfiguration and a reloading strategy.
     */
    public void testReloading() throws Exception
    {
        assertNotNull(conf.getReloadingStrategy());
        assertTrue(conf.getReloadingStrategy() instanceof InvariantReloadingStrategy);
        PrintWriter out = null;

        try
        {
            out = new PrintWriter(new FileWriter(testSaveConf));
            out.println("<?xml version=\"1.0\"?><config><test>1</test></config>");
            out.close();
            out = null;
            conf.setFile(testSaveConf);
            FileAlwaysReloadingStrategy strategy = new FileAlwaysReloadingStrategy();
            strategy.setRefreshDelay(100);
            conf.setReloadingStrategy(strategy);
            assertEquals(strategy, conf.getReloadingStrategy());
            assertEquals("Wrong file monitored", testSaveConf.getAbsolutePath(),
                    strategy.getMonitoredFile().getAbsolutePath());
            conf.load();
            assertEquals(1, conf.getInt("test"));

            out = new PrintWriter(new FileWriter(testSaveConf));
            out.println("<?xml version=\"1.0\"?><config><test>2</test></config>");
            out.close();
            out = null;

            int value = conf.getInt("test");
            assertEquals("No reloading performed", 2, value);
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
     * Tests access to tag names with delimiter characters.
     */
    public void testComplexNames()
    {
        assertEquals("Name with dot", conf.getString("complexNames.my..elem"));
        assertEquals("Another dot", conf.getString("complexNames.my..elem.sub..elem"));
    }

    /**
     * Tests setting a custom document builder.
     */
    public void testCustomDocBuilder() throws Exception
    {
        File testFile = ConfigurationAssert.getTestFile("testValidateInvalid.xml");
        // Load an invalid XML file with the default (non validating)
        // doc builder. This should work...
        conf = new XMLConfiguration();
        conf.load(testFile);
        assertEquals("customers", conf.getString("table.name"));
        assertFalse(conf.containsKey("table.fields.field(1).type"));

        // Now create a validating doc builder and set it.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler() {
            public void error(SAXParseException ex) throws SAXException
            {
                throw ex;
            }
        });
        conf = new XMLConfiguration();
        conf.setDocumentBuilder(builder);
        try
        {
            conf.load(testFile);
            fail("Could load invalid file with validating set to true!");
        }
        catch(ConfigurationException ex)
        {
            //ok
        }

        // Try to load a valid document with a validating builder
        conf = new XMLConfiguration();
        conf.setDocumentBuilder(builder);
        conf.load(ConfigurationAssert.getTestFile("testValidateValid.xml"));
        assertTrue(conf.containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests the clone() method.
     */
    public void testClone()
    {
        Configuration c = (Configuration) conf.clone();
        assertTrue(c instanceof XMLConfiguration);
        XMLConfiguration copy = (XMLConfiguration) c;
        assertNotNull(conf.getDocument());
        assertNull(copy.getDocument());
        assertNotNull(conf.getFileName());
        assertNull(copy.getFileName());

        copy.setProperty("element3", "clonedValue");
        assertEquals("value", conf.getString("element3"));
        conf.setProperty("element3[@name]", "originalFoo");
        assertEquals("foo", copy.getString("element3[@name]"));
    }

    /**
     * Tests saving a configuration after cloning to ensure that the clone and
     * the original are completely detachted.
     */
    public void testCloneWithSave() throws ConfigurationException
    {
        XMLConfiguration c = (XMLConfiguration) conf.clone();
        c.addProperty("test.newProperty", Boolean.TRUE);
        conf.addProperty("test.orgProperty", Boolean.TRUE);
        c.save(testSaveConf);
        XMLConfiguration c2 = new XMLConfiguration(testSaveConf);
        assertTrue("New property after clone() was not saved", c2
                .getBoolean("test.newProperty"));
        assertFalse("Property of original config was saved", c2
                .containsKey("test.orgProperty"));
    }

    /**
     * Tests the subset() method. There was a bug that calling subset() had
     * undesired side effects.
     */
    public void testSubset() throws ConfigurationException
    {
        conf = new XMLConfiguration();
        conf.load(ConfigurationAssert.getTestFile("testHierarchicalXMLConfiguration.xml"));
        conf.subset("tables.table(0)");
        conf.save(testSaveConf);

        conf = new XMLConfiguration(testSaveConf);
        assertEquals("users", conf.getString("tables.table(0).name"));
    }

    /**
     * Tests string properties with list delimiters and escaped delimiters.
     */
    public void testSplitLists()
    {
        assertEquals("a", conf.getString("split.list3[@values]"));
        assertEquals(2, conf.getMaxIndex("split.list3[@values]"));
        assertEquals("a,b,c", conf.getString("split.list4[@values]"));
        assertEquals("a", conf.getString("split.list1"));
        assertEquals(2, conf.getMaxIndex("split.list1"));
        assertEquals("a,b,c", conf.getString("split.list2"));
    }

    /**
     * Tests string properties with list delimiters when delimiter parsing
     * is disabled
     */
    public void testDelimiterParsingDisabled() throws ConfigurationException {
        XMLConfiguration conf2 = new XMLConfiguration();
        conf2.setDelimiterParsingDisabled(true);
        conf2.setFile(new File(testProperties));
        conf2.load();

        assertEquals("a,b,c", conf2.getString("split.list3[@values]"));
        assertEquals(0, conf2.getMaxIndex("split.list3[@values]"));
        assertEquals("a\\,b\\,c", conf2.getString("split.list4[@values]"));
        assertEquals("a,b,c", conf2.getString("split.list1"));
        assertEquals(0, conf2.getMaxIndex("split.list1"));
        assertEquals("a\\,b\\,c", conf2.getString("split.list2"));
    }

    /**
     * Tests whether a DTD can be accessed.
     */
    public void testDtd() throws ConfigurationException
    {
        conf = new XMLConfiguration("testDtd.xml");
        assertEquals("value1", conf.getString("entry(0)"));
        assertEquals("test2", conf.getString("entry(1)[@key]"));
    }

    /**
     * Tests DTD validation using the setValidating() method.
     */
    public void testValidating() throws ConfigurationException
    {
        File nonValidFile = ConfigurationAssert.getTestFile("testValidateInvalid.xml");
        conf = new XMLConfiguration();
        assertFalse(conf.isValidating());

        // Load a non valid XML document. Should work for isValidating() == false
        conf.load(nonValidFile);
        assertEquals("customers", conf.getString("table.name"));
        assertFalse(conf.containsKey("table.fields.field(1).type"));

        // Now set the validating flag to true
        conf.setValidating(true);
        try
        {
            conf.load(nonValidFile);
            fail("Validation was not performed!");
        }
        catch(ConfigurationException cex)
        {
            //ok
        }
    }

    /**
     * Tests handling of empty elements.
     */
    public void testEmptyElements() throws ConfigurationException
    {
        assertTrue(conf.containsKey("empty"));
        assertEquals("", conf.getString("empty"));
        conf.addProperty("empty2", "");
        conf.setProperty("empty", "no more empty");
        conf.save(testSaveConf);

        conf = new XMLConfiguration(testSaveConf);
        assertEquals("no more empty", conf.getString("empty"));
        assertEquals("", conf.getProperty("empty2"));
    }

    /**
     * Tests whether the encoding is correctly detected by the XML parser. This
     * is done by loading an XML file with the encoding "UTF-16". If this
     * encoding is not detected correctly, an exception will be thrown that
     * "Content is not allowed in prolog". This test case is related to issue
     * 34204.
     */
    public void testLoadWithEncoding() throws ConfigurationException
    {
        File file = ConfigurationAssert.getTestFile("testEncoding.xml");
        conf = new XMLConfiguration();
        conf.load(file);
        assertEquals("test3_yoge", conf.getString("yoge"));
    }

    /**
     * Tests whether the encoding is written to the generated XML file.
     */
    public void testSaveWithEncoding() throws ConfigurationException
    {
        conf = new XMLConfiguration();
        conf.setProperty("test", "a value");
        conf.setEncoding(ENCODING);

        StringWriter out = new StringWriter();
        conf.save(out);
        assertTrue("Encoding was not written to file", out.toString().indexOf(
                "encoding=\"" + ENCODING + "\"") >= 0);
    }

    /**
     * Tests whether a default encoding is used if no specific encoding is set.
     * According to the XSLT specification (http://www.w3.org/TR/xslt#output)
     * this should be either UTF-8 or UTF-16.
     */
    public void testSaveWithNullEncoding() throws ConfigurationException
    {
        conf = new XMLConfiguration();
        conf.setProperty("testNoEncoding", "yes");
        conf.setEncoding(null);

        StringWriter out = new StringWriter();
        conf.save(out);
        assertTrue("Encoding was written to file", out.toString().indexOf(
                "encoding=\"UTF-") >= 0);
    }

    /**
     * Tests whether the DOCTYPE survives a save operation.
     */
    public void testSaveWithDoctype() throws ConfigurationException
    {
        String content = "<?xml  version=\"1.0\"?>"
                + DOCTYPE
                + "properties"
                + DOCTYPE_DECL
                + "<properties version=\"1.0\"><entry key=\"test\">value</entry></properties>";
        StringReader in = new StringReader(content);
        conf = new XMLConfiguration();
        conf.setFileName(ConfigurationAssert.getTestFile("testDtd.xml").getAbsolutePath());
        conf.load();
        conf.clear();
        conf.load(in);

        assertEquals("Wrong public ID", PUBLIC_ID, conf.getPublicID());
        assertEquals("Wrong system ID", SYSTEM_ID, conf.getSystemID());
        StringWriter out = new StringWriter();
        conf.save(out);
        System.out.println(out.toString());
        assertTrue("Did not find DOCTYPE", out.toString().indexOf(DOCTYPE) >= 0);
    }

    /**
     * Tests setting public and system IDs for the D'OCTYPE and then saving the
     * configuration. This should generate a DOCTYPE declaration.
     */
    public void testSaveWithDoctypeIDs() throws ConfigurationException
    {
        assertNull("A public ID was found", conf.getPublicID());
        assertNull("A system ID was found", conf.getSystemID());
        conf.setPublicID(PUBLIC_ID);
        conf.setSystemID(SYSTEM_ID);
        StringWriter out = new StringWriter();
        conf.save(out);
        assertTrue("Did not find DOCTYPE", out.toString().indexOf(
                DOCTYPE + "testconfig" + DOCTYPE_DECL) >= 0);
    }

    /**
     * Tests saving a configuration when an invalid transformer factory is
     * specified. In this case the error thrown by the TransformerFactory class
     * should be caught and re-thrown as a ConfigurationException.
     */
    public void testSaveWithInvalidTransformerFactory()
    {
        System.setProperty(PROP_FACTORY, "an.invalid.Class");
        try
        {
            conf.save(testSaveConf);
            fail("Could save with invalid TransformerFactory!");
        }
        catch (ConfigurationException cex)
        {
            // ok
        }
        finally
        {
            System.getProperties().remove(PROP_FACTORY);
        }
    }

    /**
     * Tests if reloads are recognized by subset().
     */
    public void testSubsetWithReload() throws ConfigurationException
    {
        XMLConfiguration c = setUpReloadTest();
        Configuration sub = c.subset("test");
        assertEquals("New value not read", "newValue", sub.getString("entity"));
    }

    /**
     * Tests if reloads are recognized by configurationAt().
     */
    public void testConfigurationAtWithReload() throws ConfigurationException
    {
        XMLConfiguration c = setUpReloadTest();
        AbstractHierarchicalConfiguration<ConfigurationNode> sub = c.configurationAt("test(0)");
        assertEquals("New value not read", "newValue", sub.getString("entity"));
    }

    /**
     * Tests if reloads are recognized by configurationsAt().
     */
    public void testConfigurationsAtWithReload() throws ConfigurationException
    {
        XMLConfiguration c = setUpReloadTest();
        List<SubConfiguration<ConfigurationNode>> configs =
            c.configurationsAt("test");
        assertEquals("New value not read", "newValue",
                (configs.get(0)).getString("entity"));
    }

    /**
     * Tests accessing properties when the XPATH expression engine is set.
     */
    public void testXPathExpressionEngine()
    {
        conf.setExpressionEngine(new XPathExpressionEngine());
        assertEquals("Wrong attribute value", "foo\"bar", conf
                .getString("test[1]/entity/@name"));
        conf.clear();
        assertNull(conf.getString("test[1]/entity/@name"));
    }

    /**
     * Tests the copy constructor.
     */
    public void testInitCopy() throws ConfigurationException
    {
        XMLConfiguration copy = new XMLConfiguration(conf);
        assertEquals("value", copy.getProperty("element"));
        assertNull("Document was copied, too", copy.getDocument());
        ConfigurationNode root = copy.getRootNode();
        for (ConfigurationNode node : root.getChildren())
        {
            assertNull("Reference was not cleared", node.getReference());
        }

        removeTestFile();
        copy.setFile(testSaveConf);
        copy.save();
        copy.clear();
        checkSavedConfig(copy);
    }

    /**
     * Tests setting text of the root element.
     */
    public void testSetTextRootElement() throws ConfigurationException
    {
        conf.setProperty("", "Root text");
        conf.save(testSaveConf);
        XMLConfiguration copy = new XMLConfiguration();
        copy.setFile(testSaveConf);
        checkSavedConfig(copy);
    }

    /**
     * Tests removing the text of the root element.
     */
    public void testClearTextRootElement() throws ConfigurationException
    {
        final String xml = "<e a=\"v\">text</e>";
        conf.clear();
        StringReader in = new StringReader(xml);
        conf.load(in);
        assertEquals("Wrong text of root", "text", conf.getString(""));

        conf.clearProperty("");
        conf.save(testSaveConf);
        XMLConfiguration copy = new XMLConfiguration();
        copy.setFile(testSaveConf);
        checkSavedConfig(copy);
    }

    /**
     * Tests list nodes with multiple values and attributes.
     */
    public void testListWithAttributes()
    {
        assertEquals("Wrong number of <a> elements", 6, conf.getList(
                "attrList.a").size());
        assertEquals("Wrong value of first element", "ABC", conf
                .getString("attrList.a(0)"));
        assertEquals("Wrong value of first name attribute", "x", conf
                .getString("attrList.a(0)[@name]"));
        assertEquals("Wrong number of name attributes", 5, conf.getList(
                "attrList.a[@name]").size());
    }

    /**
     * Tests a list node with attributes that has multiple values separated by
     * the list delimiter. In this scenario the attribute should be added to the
     * node with the first value.
     */
    public void testListWithAttributesMultiValue()
    {
        assertEquals("Wrong value of 2nd element", "1", conf
                .getString("attrList.a(1)"));
        assertEquals("Wrong value of 2nd name attribute", "y", conf
                .getString("attrList.a(1)[@name]"));
        for (int i = 2; i <= 3; i++)
        {
            assertEquals("Wrong value of element " + (i + 1), i, conf
                    .getInt("attrList.a(" + i + ")"));
            assertFalse("element " + (i + 1) + " has attribute", conf
                    .containsKey("attrList.a(2)[@name]"));
        }
    }

    /**
     * Tests a list node with a multi-value attribute and multiple values. All
     * attribute values should be assigned to the node with the first value.
     */
    public void testListWithMultiAttributesMultiValue()
    {
        for (int i = 1; i <= 2; i++)
        {
            assertEquals("Wrong value of multi-valued node", "value" + i, conf
                    .getString("attrList.a(" + (i + 3) + ")"));
        }
        List<?> attrs = conf.getList("attrList.a(4)[@name]");
        final String attrVal = "uvw";
        assertEquals("Wrong number of name attributes", attrVal.length(), attrs
                .size());
        for (int i = 0; i < attrVal.length(); i++)
        {
            assertEquals("Wrong value for attribute " + i, String
                    .valueOf(attrVal.charAt(i)), attrs.get(i));
        }
        assertEquals("Wrong value of test attribute", "yes", conf
                .getString("attrList.a(4)[@test]"));
        assertFalse("Name attribute for 2nd value", conf
                .containsKey("attrList.a(5)[@name]"));
        assertFalse("Test attribute for 2nd value", conf
                .containsKey("attrList.a(5)[@test]"));
    }

    /**
     * Tests whether the auto save mechanism is triggered by changes at a
     * subnode configuration.
     */
    public void testAutoSaveWithSubnodeConfig() throws ConfigurationException
    {
        final String newValue = "I am autosaved";
        conf.setFile(testSaveConf);
        conf.setAutoSave(true);
        Configuration sub = conf.configurationAt("element2.subelement");
        sub.setProperty("subsubelement", newValue);
        assertEquals("Change not visible to parent", newValue, conf
                .getString("element2.subelement.subsubelement"));
        XMLConfiguration conf2 = new XMLConfiguration(testSaveConf);
        assertEquals("Change was not saved", newValue, conf2
                .getString("element2.subelement.subsubelement"));
    }

    /**
     * Tests whether a subnode configuration created from another subnode
     * configuration of a XMLConfiguration can trigger the auto save mechanism.
     */
    public void testAutoSaveWithSubSubnodeConfig() throws ConfigurationException
    {
        final String newValue = "I am autosaved";
        conf.setFile(testSaveConf);
        conf.setAutoSave(true);
        AbstractHierarchicalConfiguration<ConfigurationNode> sub1 = conf.configurationAt("element2");
        AbstractHierarchicalConfiguration<ConfigurationNode> sub2 = sub1.configurationAt("subelement");
        sub2.setProperty("subsubelement", newValue);
        assertEquals("Change not visible to parent", newValue, conf
                .getString("element2.subelement.subsubelement"));
        XMLConfiguration conf2 = new XMLConfiguration(testSaveConf);
        assertEquals("Change was not saved", newValue, conf2
                .getString("element2.subelement.subsubelement"));
    }

    /**
     * Tests saving and loading a configuration when delimiter parsing is
     * disabled.
     */
    public void testSaveDelimiterParsingDisabled()
            throws ConfigurationException
    {
        checkSaveDelimiterParsingDisabled("list.delimiter.test");
    }

    /**
     * Tests saving and loading a configuration when delimiter parsing is
     * disabled and attributes are involved.
     */
    public void testSaveDelimiterParsingDisabledAttrs()
            throws ConfigurationException
    {
        checkSaveDelimiterParsingDisabled("list.delimiter.test[@attr]");
    }

    /**
     * Helper method for testing saving and loading a configuration when
     * delimiter parsing is disabled.
     *
     * @param key the key to be checked
     * @throws ConfigurationException if an error occurs
     */
    private void checkSaveDelimiterParsingDisabled(String key)
            throws ConfigurationException
    {
        conf.clear();
        conf.setDelimiterParsingDisabled(true);
        conf.load();
        conf.setProperty(key, "C:\\Temp\\,C:\\Data\\");
        conf.addProperty(key, "a,b,c");
        conf.save(testSaveConf);
        XMLConfiguration checkConf = new XMLConfiguration();
        checkConf.setDelimiterParsingDisabled(true);
        checkConf.setFile(testSaveConf);
        checkSavedConfig(checkConf);
    }

    /**
     * Tests multiple attribute values in delimiter parsing disabled mode.
     */
    public void testDelimiterParsingDisabledMultiAttrValues() throws ConfigurationException
    {
        conf.clear();
        conf.setDelimiterParsingDisabled(true);
        conf.load();
        List<?> expr = conf.getList("expressions[@value]");
        assertEquals("Wrong list size", 2, expr.size());
        assertEquals("Wrong element 1", "a || (b && c)", expr.get(0));
        assertEquals("Wrong element 2", "!d", expr.get(1));
    }

    /**
     * Tests using multiple attribute values, which are partly escaped when
     * delimiter parsing is not disabled.
     */
    public void testMultipleAttrValuesEscaped() throws ConfigurationException
    {
        conf.addProperty("test.dir[@name]", "C:\\Temp\\");
        conf.addProperty("test.dir[@name]", "C:\\Data\\");
        conf.save(testSaveConf);
        XMLConfiguration checkConf = new XMLConfiguration();
        checkConf.setFile(testSaveConf);
        checkSavedConfig(checkConf);
    }

    /**
     * Tests a combination of auto save = true and an associated reloading
     * strategy.
     */
    public void testAutoSaveWithReloadingStrategy() throws ConfigurationException
    {
        conf.setFile(testSaveConf);
        conf.save();
        conf.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        conf.setAutoSave(true);
        assertEquals("Value not found", "value", conf.getProperty("element"));
    }

    /**
     * Tests adding nodes from another configuration.
     */
    public void testAddNodesCopy() throws ConfigurationException
    {
        XMLConfiguration c2 = new XMLConfiguration(testProperties2);
        conf.addNodes("copiedProperties", c2.getRootNode().getChildren());
        conf.save(testSaveConf);
        XMLConfiguration checkConf = new XMLConfiguration();
        checkConf.setFile(testSaveConf);
        checkSavedConfig(checkConf);
    }

    /**
     * Tests whether the addNodes() method triggers an auto save.
     */
    public void testAutoSaveAddNodes() throws ConfigurationException
    {
        conf.setFile(testSaveConf);
        conf.setAutoSave(true);
        ConfigurationNode node = new DefaultConfigurationNode(
                "addNodesTest", Boolean.TRUE);
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>(1);
        nodes.add(node);
        conf.addNodes("test.autosave", nodes);
        XMLConfiguration c2 = new XMLConfiguration(testSaveConf);
        assertTrue("Added nodes are not saved", c2
                .getBoolean("test.autosave.addNodesTest"));
    }

    /**
     * Tests saving a configuration after a node was added. Test for
     * CONFIGURATION-294.
     */
    public void testAddNodesAndSave() throws ConfigurationException
    {
        ConfigurationNode node = new DefaultConfigurationNode("test");
        ConfigurationNode child = new DefaultConfigurationNode("child");
        node.addChild(child);
        ConfigurationNode attr = new DefaultConfigurationNode("attr");
        node.addAttribute(attr);
        ConfigurationNode node2 = conf.createNode(null, "test2");
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>(2);
        nodes.add(node);
        nodes.add(node2);
        conf.addNodes("add.nodes", nodes);
        conf.setFile(testSaveConf);
        conf.save();
        conf.setProperty("add.nodes.test", "true");
        conf.setProperty("add.nodes.test.child", "yes");
        conf.setProperty("add.nodes.test[@attr]", "existing");
        conf.setProperty("add.nodes.test2", "anotherValue");
        conf.save();
        XMLConfiguration c2 = new XMLConfiguration(testSaveConf);
        assertEquals("Value was not saved", "true", c2
                .getString("add.nodes.test"));
        assertEquals("Child value was not saved", "yes", c2
                .getString("add.nodes.test.child"));
        assertEquals("Attr value was not saved", "existing", c2
                .getString("add.nodes.test[@attr]"));
        assertEquals("Node2 not saved", "anotherValue", c2
                .getString("add.nodes.test2"));
    }

    /**
     * Tests registering the publicId of a DTD.
     */
    public void testRegisterEntityId() throws ConfigurationException,
            IOException
    {
        File dtdFile = ConfigurationAssert.getTestFile("properties.dtd");
        final String publicId = "http://commons.apache.org/test/properties.dtd";
        conf = new XMLConfiguration("testDtd.xml");
        conf.setPublicID(publicId);
        conf.save(testSaveConf);
        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFile(testSaveConf);
        checkConfig.registerEntityId(publicId, dtdFile.toURL());
        checkConfig.setValidating(true);
        checkSavedConfig(checkConfig);
    }

    /**
     * Tries to register a null public ID. This should cause an exception.
     */
    public void testRegisterEntityIdNull() throws IOException
    {
        try
        {
            conf.registerEntityId(null, new URL("http://commons.apache.org"));
            fail("Could register null public ID!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests saving a configuration that was created from a hierarchical
     * configuration. This test exposes bug CONFIGURATION-301.
     */
    public void testSaveAfterCreateWithCopyConstructor()
            throws ConfigurationException
    {
        AbstractHierarchicalConfiguration<ConfigurationNode> hc = conf.configurationAt("element2");
        conf = new XMLConfiguration(hc);
        conf.save(testSaveConf);
        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFile(testSaveConf);
        checkSavedConfig(checkConfig);
        assertEquals("Wrong name of root element", "element2", checkConfig
                .getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied when a configuration
     * is created using the copy constructor.
     */
    public void testCopyRootName() throws ConfigurationException
    {
        final String rootName = "rootElement";
        final String xml = "<" + rootName + "><test>true</test></" + rootName
                + ">";
        conf.clear();
        conf.load(new StringReader(xml));
        XMLConfiguration copy = new XMLConfiguration(conf);
        assertEquals("Wrong name of root element", rootName, copy
                .getRootElementName());
        copy.save(testSaveConf);
        copy = new XMLConfiguration(testSaveConf);
        assertEquals("Wrong name of root element after save", rootName, copy
                .getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied for a configuration
     * for which not yet a document exists.
     */
    public void testCopyRootNameNoDocument() throws ConfigurationException
    {
        final String rootName = "rootElement";
        conf = new XMLConfiguration();
        conf.setRootElementName(rootName);
        conf.setProperty("test", Boolean.TRUE);
        XMLConfiguration copy = new XMLConfiguration(conf);
        assertEquals("Wrong name of root element", rootName, copy
                .getRootElementName());
        copy.save(testSaveConf);
        copy = new XMLConfiguration(testSaveConf);
        assertEquals("Wrong name of root element after save", rootName, copy
                .getRootElementName());
    }

    /**
     * Tests adding an attribute node using the addNodes() method.
     */
    public void testAddNodesAttributeNode()
    {
        conf.addProperty("testAddNodes.property[@name]", "prop1");
        conf.addProperty("testAddNodes.property(0).value", "value1");
        conf.addProperty("testAddNodes.property(-1)[@name]", "prop2");
        conf.addProperty("testAddNodes.property(1).value", "value2");
        Collection<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
        nodes.add(new DefaultConfigurationNode("property"));
        conf.addNodes("testAddNodes", nodes);
        nodes.clear();
        ConfigurationNode nd = new DefaultConfigurationNode("name", "prop3");
        nd.setAttribute(true);
        nodes.add(nd);
        conf.addNodes("testAddNodes.property(2)", nodes);
        assertEquals("Attribute not added", "prop3", conf
                .getString("testAddNodes.property(2)[@name]"));
    }

    /**
     * Prepares a configuration object for testing a reload operation.
     *
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private XMLConfiguration setUpReloadTest() throws ConfigurationException
    {
        removeTestFile();
        conf.save(testSaveConf);
        XMLConfiguration c = new XMLConfiguration(testSaveConf);
        c.setReloadingStrategy(new FileAlwaysReloadingStrategy());
        conf.setProperty("test(0).entity", "newValue");
        conf.save(testSaveConf);
        return c;
    }

    /**
     * Removes the test output file if it exists.
     */
    private void removeTestFile()
    {
        if (testSaveConf.exists())
        {
            assertTrue(testSaveConf.delete());
        }
    }

    /**
     * Helper method for checking if a save operation was successful. Loads a
     * saved configuration and then tests against a reference configuration.
     * @param checkConfig the configuration to check
     * @throws ConfigurationException if an error occurs
     */
    private void checkSavedConfig(FileConfiguration checkConfig) throws ConfigurationException
    {
        checkConfig.load();
        ConfigurationAssert.assertEquals(conf, checkConfig);
    }
}
