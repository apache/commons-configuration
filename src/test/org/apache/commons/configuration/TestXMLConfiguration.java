/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

/**
 * test for loading and saving xml properties files
 *
 * @version $Id$
 */
public class TestXMLConfiguration extends TestCase
{
    /** The File that we test with */
    private String testProperties = new File("conf/test.xml").getAbsolutePath();
    private String testProperties2 = new File("conf/testDigesterConfigurationInclude1.xml").getAbsolutePath();
    private String testBasePath = new File("conf").getAbsolutePath();
    private File testSaveConf = new File("target/testsave.xml");

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
        List list = (List)property;
        assertEquals(2, list.size());
        assertEquals("five", list.get(0));
        assertEquals("six", list.get(1));

        // test multiple, disjoined elements
        property = conf.getProperty("list.item");
        assertNotNull(property);
        assertTrue(property instanceof List);
        list = (List)property;
        assertEquals(4, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
        assertEquals("four", list.get(3));

        // test multiple, disjoined attributes
        property = conf.getProperty("list.item[@name]");
        assertNotNull(property);
        assertTrue(property instanceof List);
        list = (List)property;
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

        List list = conf.getList("element3[@name]");
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

    public void testAddList()
    {
        conf.addProperty("test.array", "value1");
        conf.addProperty("test.array", "value2");

        List list = conf.getList("test.array");
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

    public void testLoadFromJAR() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFileName("test-jar.xml");
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
        conf.setFile(new File("target/testsave.xml"));
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
            FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
            strategy.setRefreshDelay(100);
            conf.setReloadingStrategy(strategy);
            assertEquals(strategy, conf.getReloadingStrategy());
            conf.load();
            assertEquals(1, conf.getInt("test"));
            Thread.sleep(1000);

            out = new PrintWriter(new FileWriter(testSaveConf));
            out.println("<?xml version=\"1.0\"?><config><test>2</test></config>");
            out.close();
            out = null;

            int trial = 0, value;
            // repeat multiple times because there are sometimes race conditions
            do
            {
                Thread.sleep(1000);
                value = conf.getInt("test");
            } while (value != 2 && ++trial <= 10);
            assertEquals(2, value);
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
        // Load an invalid XML file with the default (non validating)
        // doc builder. This should work...
        conf = new XMLConfiguration();
        conf.load(new File("conf/testValidateInvalid.xml"));
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
            conf.load(new File("conf/testValidateInvalid.xml"));
            fail("Could load invalid file with validating set to true!");
        }
        catch(ConfigurationException ex)
        {
            //ok
        }
        
        // Try to load a valid document with a validating builder
        conf = new XMLConfiguration();
        conf.setDocumentBuilder(builder);
        conf.load(new File("conf/testValidateValid.xml"));
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
     * Tests the subset() method. There was a bug that calling subset() had
     * undesired side effects.
     */
    public void testSubset() throws ConfigurationException
    {
        conf = new XMLConfiguration();
        conf.load(new File("conf/testHierarchicalXMLConfiguration.xml"));
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
        File nonValidFile = new File("conf/testValidateInvalid.xml");
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

        for (Iterator i = conf.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));
            assertEquals("Value of the '" + key + "' property", conf.getProperty(key), checkConfig.getProperty(key));
        }
    }
}
