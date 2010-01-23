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
package org.apache.commons.configuration2.base.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.base.Configuration;
import org.apache.commons.configuration2.base.ConfigurationImpl;
import org.apache.commons.configuration2.base.LocatorSupport;
import org.apache.commons.configuration2.expr.xpath.XPathExpressionEngine;
import org.apache.commons.configuration2.fs.Locator;
import org.apache.commons.configuration2.fs.URLLocator;
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test class for {@code XMLConfigurationSource}.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestXMLConfigurationSource
{
    /** Constant for the name of the test output file. */
    private static final String OUT_FILE = "testsave.xml";

    /** Constant for the test encoding. */
    private static final String ENCODING = "ISO-8859-1";

    /** Constant for the test system ID. */
    private static final String SYSTEM_ID = "properties.dtd";

    /** Constant for the test public ID. */
    private static final String PUBLIC_ID = "-//Commons Configuration//DTD Test Configuration 1.3//EN";

    /** Constant for the DOCTYPE declaration. */
    private static final String DOCTYPE_DECL = " PUBLIC \"" + PUBLIC_ID
            + "\" \"" + SYSTEM_ID + "\">";

    /** Constant for the DOCTYPE prefix. */
    private static final String DOCTYPE = "<!DOCTYPE ";

    /** Constant for the transformer factory property. */
    private static final String PROP_FACTORY = "javax.xml.transform.TransformerFactory";

    /** XML Catalog */
    private static final String CATALOG_FILES = ConfigurationAssert
            .getTestFile("catalog.xml").getAbsolutePath();

    /** The locator for the test XML file. */
    private static Locator testFileLocator;

    /** The locator for the test output file. */
    private static Locator testOutLocator;

    /** A configuration wrapping the source for convenient access to properties. */
    private Configuration<ConfigurationNode> conf;

    /** The configuration source to be tested. */
    private XMLConfigurationSource source;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        testFileLocator = new URLLocator(ConfigurationAssert
                .getTestURL("test.xml"));
        testOutLocator = new URLLocator(ConfigurationAssert.getOutURL(OUT_FILE));
    }

    @Before
    public void setUp() throws Exception
    {
        source = new XMLConfigurationSource();
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(testFileLocator);
        locSupport.load();
        conf = new ConfigurationImpl<ConfigurationNode>(source);
        removeTestFile();
    }

    /**
     * Removes the test output file if it exists.
     */
    private void removeTestFile()
    {
        File testSaveConf = ConfigurationAssert.getOutFile(OUT_FILE);
        if (testSaveConf.exists())
        {
            assertTrue(testSaveConf.delete());
        }
    }

    /**
     * Stores the test configuration and loads it again. This is used for
     * testing the save() facilities and to check whether all kind of properties
     * can be persisted.
     *
     * @return the newly loaded configuration
     * @throws ConfigurationException in case of an error
     */
    private Configuration<ConfigurationNode> reload()
            throws ConfigurationException
    {
        LocatorSupport locSupport = conf.getConfigurationSource()
                .getCapability(LocatorSupport.class);
        locSupport.save(testOutLocator);
        XMLConfigurationSource source2 = new XMLConfigurationSource();
        LocatorSupport locSupport2 = source2
                .getCapability(LocatorSupport.class);
        locSupport2.setLocator(testOutLocator);
        locSupport2.load();
        return new ConfigurationImpl<ConfigurationNode>(source2);
    }

    /**
     * Tests simple property access.
     */
    @Test
    public void testGetProperty()
    {
        assertEquals("Wrong value", "value", conf.getProperty("element"));
    }

    /**
     * Tests whether comments are correctly processed.
     */
    @Test
    public void testGetCommentedProperty()
    {
        assertEquals("Wrong value for commented property", "", conf
                .getProperty("test.comment"));
    }

    /**
     * Tests whether XML entities are correctly resolved.
     */
    @Test
    public void testGetPropertyWithXMLEntity()
    {
        assertEquals("Wrong value for entity", "1<2", conf
                .getProperty("test.entity"));
    }

    @Test
    public void testClearPropertyNonExisting()
    {
        String key = "clearly";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether a non-leaf XML element can be queried. It should not have a
     * value.
     */
    @Test
    public void testGetPropertyNonLeaf()
    {
        Object property = conf.getProperty("clear");
        assertNull("Got a value", property);
    }

    @Test
    public void testGetPropertyNonExisting()
    {
        assertNull("Got value for non-existing property (1)", conf
                .getProperty("e"));
        assertNull("Got value for non-existing property (2)", conf
                .getProperty("element3[@n]"));
    }

    /**
     * Tests whether an attribute value can be queried.
     */
    @Test
    public void testGetPropertySingleAttribute()
    {
        Object property = conf.getProperty("element3[@name]");
        assertNotNull("No value", property);
        assertTrue("Wrong type", property instanceof String);
        assertEquals("Wrong value", "foo", property);
    }

    /**
     * Tests whether the value of a CDATA section can be queried.
     */
    @Test
    public void testGetPropertyCData()
    {
        Object property = conf.getProperty("test.cdata");
        assertNotNull("No value", property);
        assertTrue("Wrong type", property instanceof String);
        assertEquals("Wrong value", "<cdata value>", property);
    }

    /**
     * Tests whether a list of values can be queried.
     */
    @Test
    public void testGetPropertyMultipleSiblings()
    {
        Object property = conf.getProperty("list.sublist.item");
        assertNotNull("No value", property);
        assertTrue("Not a list", property instanceof List<?>);
        List<?> list = (List<?>) property;
        assertEquals("Wrong size", 2, list.size());
        assertEquals("Wrong element at 0", "five", list.get(0));
        assertEquals("Wrong element at 1", "six", list.get(1));
    }

    /**
     * Tests whether a list consisting of multiple disjoined elements can be
     * queried.
     */
    @Test
    public void testGetPropertyMultipleDisjoined()
    {
        Object property = conf.getProperty("list.item");
        assertNotNull("No value", property);
        assertTrue("Not a list", property instanceof List<?>);
        List<?> list = (List<?>) property;
        assertEquals("Wrong size", 4, list.size());
        assertEquals("Wrong element at 0", "one", list.get(0));
        assertEquals("Wrong element at 1", "two", list.get(1));
        assertEquals("Wrong element at 2", "three", list.get(2));
        assertEquals("Wrong element at 3", "four", list.get(3));
    }

    /**
     * Tests whether the attributes of list elements can be queried.
     */
    @Test
    public void testGetPropertyMultipleDisjoinedAttributes()
    {
        Object property = conf.getProperty("list.item[@name]");
        assertNotNull("No value", property);
        assertTrue("Not a list", property instanceof List<?>);
        List<?> list = (List<?>) property;
        assertEquals("Wrong size", 2, list.size());
        assertEquals("Wrong element at 0", "one", list.get(0));
        assertEquals("Wrong element at 0", "three", list.get(1));
    }

    /**
     * Tests whether a deeply nested property can be accessed.
     */
    @Test
    public void testGetPropertyComplex()
    {
        assertEquals("Wrong value", "I'm complex!", conf
                .getProperty("element2.subelement.subsubelement"));
    }

    /**
     * Tests access to tag names with delimiter characters.
     */
    @Test
    public void testGetPropertyComplexNames()
    {
        assertEquals("Name with dot", conf.getString("complexNames.my..elem"));
        assertEquals("Another dot", conf
                .getString("complexNames.my..elem.sub..elem"));
    }

    /**
     * Tests the handling of empty elements.
     */
    @Test
    public void testGetPropertyEmptyElements() throws ConfigurationException
    {
        assertTrue("Empty key not found", conf.containsKey("empty"));
        assertEquals("Wrong value of empty property", "", conf
                .getString("empty"));
        conf.addProperty("empty2", "");
        conf.setProperty("empty", "no more empty");
        Configuration<ConfigurationNode> conf2 = reload();
        assertEquals("Wrong value after save 1", "no more empty", conf2
                .getString("empty"));
        assertEquals("Wrong value after save 2", "", conf2
                .getProperty("empty2"));
    }

    /**
     * Tests whether properties can be accessed if the XPATH expression engine
     * is set.
     */
    @Test
    public void testGetPropertyXPathExpressionEngine()
    {
        conf.setExpressionEngine(new XPathExpressionEngine());
        assertEquals("Wrong attribute value", "foo\"bar", conf
                .getString("test[1]/entity/@name"));
        conf.clear();
        assertNull("Value still found", conf.getString("test[1]/entity/@name"));
    }

    /**
     * Tests list nodes with multiple values and attributes.
     */
    @Test
    public void testGetPropertyListWithAttributes()
    {
        assertEquals("Wrong number of <a> elements", 3, conf.getList(
                "attrList.a").size());
        assertEquals("Wrong value of first element", "ABC", conf
                .getString("attrList.a(0)"));
        assertEquals("Wrong value of 2nd element", "1,2,3", conf
                .getString("attrList.a(1)"));
        assertEquals("Wrong value of first name attribute", "x", conf
                .getString("attrList.a(0)[@name]"));
        assertEquals("Wrong value of 2nd name attribute", "y", conf
                .getString("attrList.a(1)[@name]"));
        assertEquals("Wrong number of name attributes", 3, conf.getList(
                "attrList.a[@name]").size());
    }

    /**
     * Tests a list node with multiple attributes.
     */
    @Test
    public void testGetPropertyListWithMultiAttributesMultiValue()
    {
        assertEquals("Wrong value of list element", "value1,value2", conf
                .getString("attrList.a(2)"));
        assertEquals("Wrong value of test attribute", "yes", conf
                .getString("attrList.a(2)[@test]"));
        assertEquals("Wrong value of name attribute", "u,v,w", conf
                .getString("attrList.a(2)[@name]"));
    }

    @Test
    public void testClearPropertySingleElement()
    {
        String key = "clear.element";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
    }

    /**
     * Tests that attributes are not effected if their parent element is
     * cleared.
     */
    @Test
    public void testClearPropertySingleElementWithAttribute()
    {
        String key = "clear.element2";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        key = "clear.element2[@id]";
        assertNotNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether a property with a comment can be cleared.
     */
    @Test
    public void testClearPropertyCommentedElement()
    {
        String key = "clear.comment";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether a CDATA section can be cleared.
     */
    @Test
    public void testClearPropertyCData()
    {
        String key = "clear.cdata";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether a list of elements can be cleared and whether attributes
     * are retained.
     */
    @Test
    public void testClearPropertyMultipleSiblings()
    {
        String key = "clear.list.item";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
        key = "clear.list.item[@id]";
        assertNotNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether a list can be cleared that contains of multiple disjoint
     * elements.
     */
    @Test
    public void testClearPropertyMultipleDisjoint()
    {
        String key = "list.item";
        conf.clearProperty(key);
        assertNull(key, conf.getProperty(key));
    }

    /**
     * Tests whether the text of the root element can be removed.
     */
    @Test
    public void testClearTextRootElement() throws ConfigurationException,
            IOException
    {
        final String xml = "<e a=\"v\">text</e>";
        conf.clear();
        StringReader in = new StringReader(xml);
        source.load(in);
        assertEquals("Wrong text of root", "text", conf.getString(null));
        conf.clearProperty(null);
        assertNull("Still got root text", conf.getString(null));
        Configuration<ConfigurationNode> conf2 = reload();
        assertNull("Got root text after reload", conf2.getString(null));
    }

    /**
     * Tests whether an attribute can be replaced.
     */
    @Test
    public void testSetAttributeExisting()
    {
        conf.setProperty("element3[@name]", "bar");
        assertEquals("element3[@name]", "bar", conf
                .getProperty("element3[@name]"));
    }

    /**
     * Tests whether a new attribute can be set.
     */
    @Test
    public void testSetAttributeNew()
    {
        conf.setProperty("foo[@bar]", "value");
        assertEquals("foo[@bar]", "value", conf.getProperty("foo[@bar]"));
    }

    /**
     * Tests whether an attribute of a different type than string can be set.
     */
    @Test
    public void testAddObjectAttribute()
    {
        conf.addProperty("test.boolean[@value]", Boolean.TRUE);
        assertTrue("test.boolean[@value]", conf
                .getBoolean("test.boolean[@value]"));
    }

    /**
     * Tests whether attributes on the root element can be set.
     */
    @Test
    public void testSetRootAttribute() throws ConfigurationException
    {
        conf.setProperty("[@test]", "true");
        assertEquals("Root attribute not set", "true", conf
                .getString("[@test]"));
        Configuration<ConfigurationNode> conf2 = reload();
        assertTrue("Attribute not found after save", conf2
                .containsKey("[@test]"));
        conf2.setProperty("[@test]", "newValue");
        assertEquals("New value not set", "newValue", conf2
                .getString("[@test]"));
        conf = conf2;
        Configuration<ConfigurationNode> conf3 = reload();
        assertEquals("Attribute not modified after save", "newValue", conf3
                .getString("[@test]"));
    }

    /**
     * Tests whether the text of the root element can be set.
     */
    @Test
    public void testSetTextRootElement() throws ConfigurationException
    {
        final String text = "Root text";
        conf.setProperty(null, text);
        Configuration<ConfigurationNode> conf2 = reload();
        assertEquals("Wrong root text (1)", text, conf2.getString(""));
        assertEquals("Wrong root text (2)", text, conf2.getString(null));
    }

    /**
     * Tests whether a property can be set.
     */
    @Test
    public void testSetProperty()
    {
        conf.setProperty("element.string", "hello");
        assertEquals("'element.string'", "hello", conf
                .getString("element.string"));
        assertEquals("XML value of element.string", "hello", conf
                .getProperty("element.string"));
    }

    /**
     * Tests whether a property can be added to an uninitialized configuration.
     */
    @Test
    public void testAddProperty()
    {
        source.clear();
        conf.addProperty("test.string", "hello");
        assertEquals("'test.string'", "hello", conf.getString("test.string"));
    }

    /**
     * Tests whether properties of other types than string can be added.
     */
    @Test
    public void testAddObjectProperty()
    {
        conf.addProperty("test.boolean", Boolean.TRUE);
        assertTrue("'test.boolean'", conf.getBoolean("test.boolean"));
    }

    /**
     * Tests whether the configuration source's root node is initialized with a
     * reference to the corresponding XML element.
     */
    @Test
    public void testGetRootReference()
    {
        assertNotNull("Root node has no reference", source.getRootNode()
                .getReference());
    }

    /**
     * Tests whether a list property can be created by adding multiple values
     * with the same key.
     */
    @Test
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

    /**
     * Tries to load a non well-formed XML from a string.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadInvalidXML() throws ConfigurationException, IOException
    {
        String xml = "<?xml version=\"1.0\"?><config><test>1</rest></config>";
        source.load(new StringReader(xml));
    }

    /**
     * Tests if a second file can be appended to a first.
     */
    @Test
    public void testLoadAppend() throws ConfigurationException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        Locator loc = new URLLocator(ConfigurationAssert
                .getTestURL("testDigesterConfigurationInclude1.xml"));
        locSupport.load(loc);
        assertEquals("Property from file 1 not found", "value", conf
                .getString("element"));
        assertEquals("Property from file 2 not found", "tasks", conf
                .getString("table.name"));
        Configuration<ConfigurationNode> conf2 = reload();
        assertEquals("Property from file 1 not found after reload", "value",
                conf2.getString("element"));
        assertEquals("Property from file 2 not found after reload", "tasks",
                conf2.getString("table.name"));
        assertEquals("Property not found", "application", conf2
                .getString("table[@tableType]"));
    }

    /**
     * Tests whether an invalid XML file can be loaded with the default
     * (non-validating) document builder.
     */
    @Test
    public void testLoadNonValidatingDocBuilder() throws ConfigurationException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.load(new URLLocator(ConfigurationAssert
                .getTestURL("testValidateInvalid.xml")));
        assertEquals("key customers", "customers", conf.getString("table.name"));
        assertFalse("key type", conf.containsKey("table.fields.field(1).type"));
    }

    /**
     * Tries to load an invalid XML file with a custom, validating document
     * builder. This should cause an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadValidatingDocBuilder() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler()
        {
            @Override
            public void error(SAXParseException ex) throws SAXException
            {
                throw ex;
            }
        });
        source.setDocumentBuilder(builder);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.load(new URLLocator(ConfigurationAssert
                .getTestURL("testValidateInvalid.xml")));
    }

    /**
     * Tests whether a valid XML file can be loaded with a custom validating
     * document builder.
     */
    @Test
    public void testCustomDocBuilder() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler()
        {
            @Override
            public void error(SAXParseException ex) throws SAXException
            {
                throw ex;
            }
        });
        source.setDocumentBuilder(builder);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.load(new URLLocator(ConfigurationAssert
                .getTestURL("testValidateValid.xml")));
        assertTrue("Key not found", conf
                .containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests whether a DTD can be accessed.
     */
    @Test
    public void testDtd() throws ConfigurationException
    {
        conf.clear();
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(new URLLocator(ConfigurationAssert
                .getTestURL("testDtd.xml")));
        locSupport.load();
        assertEquals("Wrong value 1", "value1", conf.getString("entry(0)"));
        assertEquals("Wrong value 2", "test2", conf.getString("entry(1)[@key]"));
    }

    /**
     * Tests DTD validation using the setValidating() method if an invalid
     * document is loaded and validation is switched off.
     */
    @Test
    public void testValidatingFalse() throws ConfigurationException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(new URLLocator(ConfigurationAssert
                .getTestURL("testValidateInvalid.xml")));
        assertFalse("Validating is true", source.isValidating());
        locSupport.load();
        assertEquals("Wrong value", "customers", conf.getString("table.name"));
        assertFalse("Got type property", conf
                .containsKey("table.fields.field(1).type"));
    }

    /**
     * Tests DTD validation using the setValidating() method if an invalid
     * document is loaded and validation is turned on.
     */
    @Test(expected = ConfigurationException.class)
    public void testValidatingTrue() throws ConfigurationException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(new URLLocator(ConfigurationAssert
                .getTestURL("testValidateInvalid.xml")));
        source.setValidating(true);
        locSupport.load();
    }

    /**
     * Tests whether attributes can be saved and loaded (related to issue
     * 34442).
     */
    @Test
    public void testSaveAttributes() throws Exception
    {
        conf.clear();
        source.getCapability(LocatorSupport.class).load();
        Configuration<ConfigurationNode> conf2 = reload();
        assertEquals("Wrong attribute", "foo", conf2
                .getString("element3[@name]"));
    }

    /**
     * Tests whether the encoding is written to the generated XML file.
     */
    @Test
    public void testSaveWithEncoding() throws ConfigurationException,
            IOException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setEncoding(ENCODING);
        StringWriter out = new StringWriter();
        source.save(out);
        assertTrue("Encoding was not written to file", out.toString().indexOf(
                "encoding=\"" + ENCODING + "\"") >= 0);
    }

    /**
     * Tests whether a default encoding is used if no specific encoding is set.
     * According to the XSLT specification (http://www.w3.org/TR/xslt#output)
     * this should be either UTF-8 or UTF-16.
     */
    @Test
    public void testSaveWithNullEncoding() throws ConfigurationException,
            IOException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setEncoding(null);
        StringWriter out = new StringWriter();
        source.save(out);
        assertTrue("Encoding was written to file", out.toString().indexOf(
                "encoding=\"UTF-") >= 0);
    }

    /**
     * Tests whether the encoding is taken into account when loading a document
     * if it is explicitly specified.
     */
    @Test
    public void testLoadWithEncodingExplicit() throws ConfigurationException,
            IOException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setEncoding("UTF-16");
        locSupport.setLocator(new URLLocator(ConfigurationAssert
                .getTestURL("testEncoding.xml")));
        locSupport.load();
        assertEquals("Wrong value", "test3_yoge", conf.getString("yoge"));
    }

    /**
     * Tests whether the encoding is correctly detected by the XML parser. This
     * is done by loading an XML file with the encoding "UTF-16". If this
     * encoding is not detected correctly, an exception will be thrown that
     * "Content is not allowed in prolog". This test case is related to issue
     * 34204.
     */
    @Test
    public void testLoadWithEncoding() throws ConfigurationException
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(new URLLocator(ConfigurationAssert
                .getTestURL("testEncoding.xml")));
        locSupport.load();
        assertEquals("test3_yoge", conf.getString("yoge"));
    }

    /**
     * Tests whether the DOCTYPE survives a save operation.
     */
    @Test
    public void testSaveWithDoctype() throws ConfigurationException,
            IOException
    {
        String content = "<?xml  version=\"1.0\"?>"
                + DOCTYPE
                + "properties"
                + DOCTYPE_DECL
                + "<properties version=\"1.0\"><entry key=\"test\">value</entry></properties>";
        StringReader in = new StringReader(content);
        source = new XMLConfigurationSource();
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(testFileLocator);
        source.load(in);
        assertEquals("Wrong public ID", PUBLIC_ID, source.getPublicID());
        assertEquals("Wrong system ID", SYSTEM_ID, source.getSystemID());
        StringWriter out = new StringWriter();
        source.save(out);
        assertTrue("Did not find DOCTYPE", out.toString().indexOf(DOCTYPE) >= 0);
    }

    /**
     * Tests setting public and system IDs for the DOCTYPE and then saving the
     * configuration source. This should generate a DOCTYPE declaration.
     */
    @Test
    public void testSaveWithDoctypeIDs() throws ConfigurationException,
            IOException
    {
        assertNull("A public ID was found", source.getPublicID());
        assertNull("A system ID was found", source.getSystemID());
        source.setPublicID(PUBLIC_ID);
        source.setSystemID(SYSTEM_ID);
        StringWriter out = new StringWriter();
        source.save(out);
        assertTrue("Did not find DOCTYPE", out.toString().indexOf(
                DOCTYPE + "testconfig" + DOCTYPE_DECL) >= 0);
    }

    /**
     * Tries to save a configuration source if an invalid transformer factory is
     * specified. In this case the error thrown by the TransformerFactory class
     * should be caught and re-thrown as a ConfigurationException.
     */
    @Test
    public void testSaveWithInvalidTransformerFactory()
            throws ConfigurationException, IOException
    {
        System.setProperty(PROP_FACTORY, "an.invalid.Class");
        try
        {
            source.getCapability(LocatorSupport.class).save(testOutLocator);
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
     * Tests the mechanism for registering publicIds.
     */
    @Test
    public void testRegisterEntityId() throws ConfigurationException,
            IOException
    {
        URLLocator loc = new URLLocator(ConfigurationAssert
                .getTestURL("testDtd.xml"));
        URL dtdURL = ConfigurationAssert.getTestURL("properties.dtd");
        final String publicId = "http://commons.apache.org/test/properties.dtd";
        source = new XMLConfigurationSource();
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(loc);
        locSupport.load();
        source.setPublicID(publicId);
        locSupport.save(testOutLocator);
        XMLConfigurationSource source2 = new XMLConfigurationSource();
        LocatorSupport locSupport2 = source2
                .getCapability(LocatorSupport.class);
        locSupport2.setLocator(testOutLocator);
        source2.registerEntityId(publicId, dtdURL);
        source2.setValidating(true);
        locSupport2.load();
    }

    /**
     * Tries to register a null public ID. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterEntityIdNull() throws IOException
    {
        source.registerEntityId(null, new URL("http://commons.apache.org"));
    }

    /**
     * Tests modifying an XML document and saving it with schema validation
     * enabled.
     */
    @Test
    public void testSaveWithValidation() throws ConfigurationException
    {
        CatalogResolver resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        URLLocator loc = new URLLocator(ConfigurationAssert
                .getTestURL("sample.xml"));
        source = new XMLConfigurationSource();
        source.setEntityResolver(resolver);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(loc);
        source.setSchemaValidation(true);
        locSupport.load();
        conf = new ConfigurationImpl<ConfigurationNode>(source);
        conf.setProperty("Employee.SSN", "123456789");
        source.validate();
        Configuration<ConfigurationNode> conf2 = reload();
        assertEquals("123456789", conf2.getString("Employee.SSN"));
    }

    /**
     * Tests modifying an XML document and saving it with schema validation
     * enabled.
     */
    @Test
    public void testSaveWithValidationFailure() throws Exception
    {
        CatalogResolver resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        URLLocator loc = new URLLocator(ConfigurationAssert
                .getTestURL("sample.xml"));
        source = new XMLConfigurationSource();
        source.setEntityResolver(resolver);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(loc);
        source.setSchemaValidation(true);
        locSupport.load();
        conf = new ConfigurationImpl<ConfigurationNode>(source);
        conf.setProperty("Employee.Email", "JohnDoe@apache.org");
        try
        {
            source.validate();
            fail("No validation failure on save");
        }
        catch (ConfigurationException e)
        {
            Throwable cause = e.getCause();
            assertNotNull("No cause for exception on save", cause);
            assertTrue("Incorrect exception on save",
                    cause instanceof SAXParseException);
        }
    }

    /**
     * Tests whether spaces are preserved when the xml:space attribute is set.
     */
    @Test
    public void testPreserveSpace()
    {
        assertEquals("Wrong value of blanc", " ", conf.getString("space.blanc"));
        assertEquals("Wrong value of stars", " * * ", conf
                .getString("space.stars"));
    }

    /**
     * Tests whether the xml:space attribute can be overridden in nested
     * elements.
     */
    @Test
    public void testPreserveSpaceOverride()
    {
        assertEquals("Not trimmed", "Some text", conf
                .getString("space.description"));
    }

    /**
     * Tests an xml:space attribute with an invalid value. This will be
     * interpreted as default.
     */
    @Test
    public void testPreserveSpaceInvalid()
    {
        assertEquals("Invalid not trimmed", "Some other text", conf
                .getString("space.testInvalid"));
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testInitCopy() throws ConfigurationException
    {
        XMLConfigurationSource src2 = new XMLConfigurationSource(source);
        ConfigurationAssert.assertEquals(source, src2);
    }

    /**
     * Tests that the XML document is reset by the copy constructor.
     */
    @Test
    public void testInitCopyDocument()
    {
        XMLConfigurationSource copy = new XMLConfigurationSource(source);
        assertNull("Document was copied, too", copy.getDocument());
    }

    /**
     * Tests whether element references were cleared by the copy constructor.
     */
    @Test
    public void testInitCopyClearReferences()
    {
        XMLConfigurationSource copy = new XMLConfigurationSource(source);
        ConfigurationNode root = copy.getRootNode();
        for (ConfigurationNode node : root.getChildren())
        {
            assertNull("Reference was not cleared", node.getReference());
        }
    }

    /**
     * Tests whether a configuration source created by the copy constructor can
     * be correctly saved and reloaded.
     */
    @Test
    public void testInitCopySave() throws ConfigurationException
    {
        XMLConfigurationSource copy = new XMLConfigurationSource(source);
        conf = new ConfigurationImpl<ConfigurationNode>(copy);
        Configuration<ConfigurationNode> conf2 = reload();
        // Known issue: For elements with the xml:space="preserve" attribute
        // that have child elements the number of line feeds is changed.
        // Therefore the space property has a different value after reloading.
        conf.clearProperty("space");
        conf2.clearProperty("space");
        ConfigurationAssert.assertEquals(conf, conf2);
    }

    /**
     * Tests saving a configuration that was created from a hierarchical sub
     * configuration.
     */
    @Test
    public void testInitCopySaveAfterCreateWithCopyConstructorSub()
            throws ConfigurationException
    {
        Configuration<ConfigurationNode> hc = conf.configurationAt("element2");
        XMLConfigurationSource copy = new XMLConfigurationSource(hc
                .getConfigurationSource());
        conf = new ConfigurationImpl<ConfigurationNode>(copy);
        Configuration<ConfigurationNode> conf2 = reload();
        ConfigurationAssert.assertEquals(conf, conf2);
        XMLConfigurationSource src = (XMLConfigurationSource) conf2
                .getConfigurationSource();
        assertEquals("Wrong name of root element", "element2", src
                .getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied when a configuration
     * is created using the copy constructor.
     */
    @Test
    public void testInitCopyRootName() throws ConfigurationException,
            IOException
    {
        final String rootName = "rootElement";
        final String xml = "<" + rootName + "><test>true</test></" + rootName
                + ">";
        source.clear();
        source.load(new StringReader(xml));
        XMLConfigurationSource copy = new XMLConfigurationSource(source);
        assertEquals("Wrong name of root element", rootName, copy
                .getRootElementName());
        copy.getCapability(LocatorSupport.class).save(testOutLocator);
        copy = new XMLConfigurationSource();
        copy.getCapability(LocatorSupport.class).load(testOutLocator);
        assertEquals("Wrong name of root element after save", rootName, copy
                .getRootElementName());
    }

    /**
     * Tests whether the name of the root element is copied for a configuration
     * for which not yet a document exists.
     */
    @Test
    public void testInitCopyRootNameNoDocument() throws ConfigurationException
    {
        final String rootName = "rootElement";
        source = new XMLConfigurationSource();
        source.setRootElementName(rootName);
        conf = new ConfigurationImpl<ConfigurationNode>(source);
        conf.setProperty("test", Boolean.TRUE);
        XMLConfigurationSource copy = new XMLConfigurationSource(source);
        assertEquals("Wrong name of root element", rootName, copy
                .getRootElementName());
        copy.getCapability(LocatorSupport.class).save(testOutLocator);
        copy = new XMLConfigurationSource();
        copy.getCapability(LocatorSupport.class).load(testOutLocator);
        assertEquals("Wrong name of root element after save", rootName, copy
                .getRootElementName());
    }

    /**
     * Tests the copy constructor if null is passed in.
     */
    @Test
    public void testInitCopyNull()
    {
        source = new XMLConfigurationSource(null);
        conf = new ConfigurationImpl<ConfigurationNode>(source);
        assertTrue("Not empty", conf.isEmpty());
    }

    /**
     * Tests whether an empty configuration that was saved and reloaded is still
     * considered empty.
     */
    @Test
    public void testIsEmptyAfterReload() throws ConfigurationException
    {
        source.clear();
        assertTrue("Not empty", conf.isEmpty());
        Configuration<ConfigurationNode> conf2 = reload();
        assertTrue("Not empty after reload", conf2.isEmpty());
    }
}
