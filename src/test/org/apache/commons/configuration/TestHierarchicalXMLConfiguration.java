package org.apache.commons.configuration;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import junit.framework.TestCase;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * Test class for XMLConfiguration. In addition to TestXMLConfiguration this
 * class especially tests the hierarchical nature of this class and structured
 * data access.
 *
 * @author Emmanuel Bourg
 * @author Mark Woodman
 * @author Oliver Heger
 * @version $Id: TestHierarchicalXMLConfiguration.java,v 1.6 2004/12/23 18:42:25 oheger Exp $
 */
public class TestHierarchicalXMLConfiguration extends TestCase
{
    /** Test resources directory. */
    private static final String TEST_DIR = "conf";

    /** Test file #1 **/
    private static final String TEST_FILENAME = "testHierarchicalXMLConfiguration.xml";

    /** Test file #2 **/
    private static final String TEST_FILENAME2 = "testHierarchicalXMLConfiguration2.xml";

    /** Test file path #1 **/
    private static final String TEST_FILE = TEST_DIR + File.separator + TEST_FILENAME;

    /** Test file path #2 **/
    private static final String TEST_FILE2 = TEST_DIR + File.separator + TEST_FILENAME2;

    /** Test file path #3.*/
    private static final String TEST_FILE3 = TEST_DIR + File.separator + "test.xml";

    /** File name for saving.*/
    private static final String TEST_SAVENAME = "testhierarchicalsave.xml";

    /** File path for saving.*/
    private static final String TEST_SAVE = "target" + File.separator + TEST_SAVENAME;

    /** Instance config used for tests. */
    private XMLConfiguration config;

    /** Fixture setup. */
    protected void setUp() throws Exception
    {
        config = new XMLConfiguration();
    }

    private void configTest(XMLConfiguration config)
    {
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals("system", config.getProperty("tables.table(0)[@tableType]"));
        assertEquals("application", config.getProperty("tables.table(1)[@tableType]"));

        assertEquals("users", config.getProperty("tables.table(0).name"));
        assertEquals("documents", config.getProperty("tables.table(1).name"));

        Object prop = config.getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());

        prop = config.getProperty("tables.table(0).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());

        prop = config.getProperty("tables.table(1).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
    }

    public void testGetProperty() throws Exception
    {
        config.setFileName(TEST_FILE);
        config.load();

        configTest(config);
    }

    public void testLoadURL() throws Exception
    {
        config.load(new File(TEST_FILE).getAbsoluteFile().toURL());
        configTest(config);
    }

    public void testLoadBasePath1() throws Exception
    {
        config.setBasePath(TEST_DIR);
        config.setFileName(TEST_FILENAME);
        config.load();
        configTest(config);
    }

    public void testLoadBasePath2() throws Exception
    {
        config.setBasePath(new File(TEST_FILE).getAbsoluteFile().toURL().toString());
        config.setFileName(TEST_FILENAME);
        config.load();
        configTest(config);
    }

    /**
     * Ensure various node types are correctly processed in config.
     * @throws Exception
     */
    public void testXmlNodeTypes() throws Exception
    {
        // Number of keys expected from test configuration file
        final int KEY_COUNT = 5;

        // Load the configuration file
        config.load(new File(TEST_FILE2).getAbsoluteFile().toURL());

        // Validate comment in element ignored
        assertEquals("Comment in element must not change element value.", "Case1Text", config
                .getString("case1"));

        // Validate sibling comment ignored
        assertEquals("Comment as sibling must not change element value.", "Case2Text", config
                .getString("case2.child"));

        // Validate comment ignored, CDATA processed
        assertEquals("Comment and use of CDATA must not change element value.", "Case3Text", config
                .getString("case3"));

        // Validate comment and processing instruction ignored
        assertEquals("Comment and use of PI must not change element value.", "Case4Text", config
                .getString("case4"));

        // Validate comment ignored in parent attribute
        assertEquals("Comment must not change attribute node value.", "Case5Text", config
                .getString("case5[@attr]"));

        // Validate non-text nodes haven't snuck in as keys
        Iterator iter = config.getKeys();
        int count = 0;
        while (iter.hasNext())
        {
            iter.next();
            count++;
        }
        assertEquals("Config must contain only " + KEY_COUNT + " keys.", KEY_COUNT, count);
    }

    public void testSave() throws Exception
    {
        removeTestSaveFile();
        try
        {
            config.setFileName(TEST_FILE3);
            config.load();
            File saveFile = new File(TEST_SAVE);
            config.save(saveFile);

            config = new XMLConfiguration();
            config.load(saveFile.toURL());
            assertEquals("value", config.getProperty("element"));
            assertEquals("I'm complex!", config.getProperty("element2.subelement.subsubelement"));
            assertEquals(8, config.getInt("test.short"));
            assertEquals("one", config.getString("list(0).item(0)[@name]"));
            assertEquals("two", config.getString("list(0).item(1)"));
            assertEquals("six", config.getString("list(1).sublist.item(1)"));

        }
        finally
        {
            removeTestSaveFile();
        }
    }

    /**
     * Tests to save a newly created configuration.
     *
     */
    public void testSaveNew() throws Exception
    {
        config.addProperty("connection.url", "jdbc://mydb:1234");
        config.addProperty("connection.user", "scott");
        config.addProperty("connection.passwd", "tiger");
        config.addProperty("connection[@type]", "system");
        config.addProperty("tables.table.name", "tests");
        config.addProperty("tables.table(0).fields.field.name", "test_id");
        config.addProperty("tables.table(0).fields.field(-1).name", "test_name");
        config.addProperty("tables.table(-1).name", "results");
        config.addProperty("tables.table(1).fields.field.name", "res_id");
        config.addProperty("tables.table(1).fields.field(0).type", "int");
        config.addProperty("tables.table(1).fields.field(-1).name", "value");
        config.addProperty("tables.table(1).fields.field(1).type", "string");
        config.addProperty("tables.table(1).fields.field(1)[@null]", "true");

        removeTestSaveFile();
        try
        {
            File saveFile = new File(TEST_SAVE);
            config.setFile(saveFile);
            config.setRootElementName("myconfig");
            config.save();

            config = new XMLConfiguration();
            config.load(saveFile);
            assertEquals(1, config.getMaxIndex("tables.table.name"));
            assertEquals("tests", config.getString("tables.table(0).name"));
            assertEquals("test_name", config.getString("tables.table(0).fields.field(1).name"));
            assertEquals("int", config.getString("tables.table(1).fields.field(0).type"));
            assertTrue(config.getBoolean("tables.table(1).fields.field(1)[@null]"));
            assertEquals("tiger", config.getString("connection.passwd"));
            assertEquals("system", config.getProperty("connection[@type]"));
            assertEquals("myconfig", config.getRootElementName());
        }
        finally
        {
            removeTestSaveFile();
        }
    }

    /**
     * Tests to save a modified configuration.
     *
     */
    public void testSaveModified() throws Exception
    {
        config.setFile(new File(TEST_FILE3));
        config.load();

        assertTrue(config.getString("mean").startsWith("This is\n A long story..."));
        assertTrue(config.getString("mean").indexOf("And even longer") > 0);
        config.clearProperty("test.entity[@name]");
        config.setProperty("element", "new value");
        config.setProperty("test(0)", "A <new> value");
        config.addProperty("test(1).int", new Integer(9));
        config.addProperty("list(1).sublist.item", "seven");
        config.setProperty("clear", "yes");
        config.setProperty("mean", "now it's simple");
        config.addProperty("[@topattr]", "available");
        config.addProperty("[@topattr]", "successfull");

        removeTestSaveFile();
        try
        {
            config.save(new File(TEST_SAVE));
            config = new XMLConfiguration();
            config.load(TEST_SAVE);
            assertFalse(config.containsKey("test.entity[@name]"));
            assertEquals("1<2", config.getProperty("test.entity"));
            assertEquals("new value", config.getString("element"));
            assertEquals("A <new> value", config.getProperty("test(0)"));
            assertEquals((short) 8, config.getShort("test(1).short"));
            assertEquals(9, config.getInt("test(1).int"));
            assertEquals("six", config.getProperty("list(1).sublist.item(1)"));
            assertEquals("seven", config.getProperty("list(1).sublist.item(2)"));
            assertEquals("yes", config.getProperty("clear"));
            assertEquals("now it's simple", config.getString("mean"));
            assertEquals("available", config.getString("[@topattr](0)"));
            assertEquals("successfull", config.getString("[@topattr](1)"));
        }
        finally
        {
            removeTestSaveFile();
        }
    }
    
    /**
     * Tests manipulation of the root element's name.
     *
     */
    public void testRootElement() throws Exception
    {
        assertEquals("configuration", config.getRootElementName());
        config.setRootElementName("newRootName");
        assertEquals("newRootName", config.getRootElementName());
        
        config.setFile(new File(TEST_FILE3));
        config.load();
        assertEquals("testconfig", config.getRootElementName());
        try
        {
            config.setRootElementName("anotherRootElement");
            fail("Setting root element name when loaded from file!");
        }
        catch(UnsupportedOperationException uex)
        {
            //fine
        }
    }

    /**
     * Helper method that ensures that the test save file has been removed. 
     *
     */
    private void removeTestSaveFile()
    {
        File saveFile = new File(TEST_SAVE);
        if (saveFile.exists())
        {
            assertTrue(saveFile.delete());
        }
    }
}