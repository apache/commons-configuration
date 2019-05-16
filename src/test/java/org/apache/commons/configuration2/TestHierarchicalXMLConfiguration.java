package org.apache.commons.configuration2;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for XMLConfiguration. In addition to TestXMLConfiguration this
 * class especially tests the hierarchical nature of this class and structured
 * data access.
 *
 * @author Emmanuel Bourg
 * @author Mark Woodman
 */
public class TestHierarchicalXMLConfiguration
{
    /** Test resources directory. */
    private static final String TEST_DIR = "conf";

    /** Test file #1 **/
    private static final String TEST_FILENAME = "testHierarchicalXMLConfiguration.xml";

    /** Test file #2 **/
    private static final String TEST_FILENAME2 = "testHierarchicalXMLConfiguration2.xml";

    /** Test file path #1 **/
    private static final String TEST_FILE = ConfigurationAssert.getTestFile(TEST_FILENAME).getAbsolutePath();

    /** Test file path #2 **/
    private static final String TEST_FILE2 = ConfigurationAssert.getTestFile(TEST_FILENAME2).getAbsolutePath();

    /** Test file path #3.*/
    private static final String TEST_FILE3 = ConfigurationAssert.getTestFile("test.xml").getAbsolutePath();

    /** File name for saving.*/
    private static final String TEST_SAVENAME = "testhierarchicalsave.xml";

    /** Helper object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** Instance config used for tests. */
    private XMLConfiguration config;

    /** Fixture setup. */
    @Before
    public void setUp() throws Exception
    {
        config = new XMLConfiguration();
    }

    private void configTest(final XMLConfiguration config)
    {
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals("system", config.getProperty("tables.table(0)[@tableType]"));
        assertEquals("application", config.getProperty("tables.table(1)[@tableType]"));

        assertEquals("users", config.getProperty("tables.table(0).name"));
        assertEquals("documents", config.getProperty("tables.table(1).name"));

        Object prop = config.getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection<?>) prop).size());

        prop = config.getProperty("tables.table(0).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());

        prop = config.getProperty("tables.table(1).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection<?>) prop).size());
    }

    @Test
    public void testGetProperty() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.setFileName(TEST_FILE);
        handler.load();

        configTest(config);
    }

    @Test
    public void testLoadURL() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.load(new File(TEST_FILE).getAbsoluteFile().toURI().toURL());
        configTest(config);
    }

    @Test
    public void testLoadBasePath1() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.setBasePath(TEST_DIR);
        handler.setFileName(TEST_FILENAME);
        handler.load();
        configTest(config);
    }

    @Test
    public void testLoadBasePath2() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.setBasePath(new File(TEST_FILE).getAbsoluteFile().toURI().toURL().toString());
        handler.setFileName(TEST_FILENAME);
        handler.load();
        configTest(config);
    }

    /**
     * Ensure various node types are correctly processed in config.
     */
    @Test
    public void testXmlNodeTypes() throws Exception
    {
        // Number of keys expected from test configuration file
        final int KEY_COUNT = 5;

        // Load the configuration file
        final FileHandler handler = new FileHandler(config);
        handler.load(new File(TEST_FILE2).getAbsoluteFile().toURI().toURL());

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
        final Iterator<String> iter = config.getKeys();
        int count = 0;
        while (iter.hasNext())
        {
            iter.next();
            count++;
        }
        assertEquals("Config must contain only " + KEY_COUNT + " keys.", KEY_COUNT, count);
    }

    @Test
    public void testSave() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.setFileName(TEST_FILE3);
        handler.load();
        final File saveFile = folder.newFile(TEST_SAVENAME);
        handler.save(saveFile);

        config = new XMLConfiguration();
        final FileHandler handler2 = new FileHandler(config);
        handler2.load(saveFile.toURI().toURL());
        assertEquals("value", config.getProperty("element"));
        assertEquals("I'm complex!", config.getProperty("element2.subelement.subsubelement"));
        assertEquals(8, config.getInt("test.short"));
        assertEquals("one", config.getString("list(0).item(0)[@name]"));
        assertEquals("two", config.getString("list(0).item(1)"));
        assertEquals("six", config.getString("list(1).sublist.item(1)"));
    }

    /**
     * Tests to save a newly created configuration.
     */
    @Test
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

        config.setRootElementName("myconfig");
        final File saveFile = folder.newFile(TEST_SAVENAME);
        FileHandler handler = new FileHandler(config);
        handler.setFile(saveFile);
        handler.save();

        config = new XMLConfiguration();
        handler = new FileHandler(config);
        handler.load(saveFile);
        assertEquals(1, config.getMaxIndex("tables.table.name"));
        assertEquals("tests", config.getString("tables.table(0).name"));
        assertEquals("test_name", config.getString("tables.table(0).fields.field(1).name"));
        assertEquals("int", config.getString("tables.table(1).fields.field(0).type"));
        assertTrue(config.getBoolean("tables.table(1).fields.field(1)[@null]"));
        assertEquals("tiger", config.getString("connection.passwd"));
        assertEquals("system", config.getProperty("connection[@type]"));
        assertEquals("myconfig", config.getRootElementName());
    }

    /**
     * Tests to save a modified configuration.
     */
    @Test
    public void testSaveModified() throws Exception
    {
        FileHandler handler = new FileHandler(config);
        handler.setFile(new File(TEST_FILE3));
        handler.load();

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
        config.addProperty("[@topattr_other]", "successfull");

        final File saveFile = folder.newFile(TEST_SAVENAME);
        handler.save(saveFile);
        config = new XMLConfiguration();
        handler = new FileHandler(config);
        handler.load(saveFile.getAbsolutePath());
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
        assertEquals("successfull", config.getString("[@topattr_other]"));
    }

    /**
     * Tests manipulation of the root element's name.
     */
    @Test
    public void testRootElement() throws Exception
    {
        assertEquals("configuration", config.getRootElementName());
        config.setRootElementName("newRootName");
        assertEquals("newRootName", config.getRootElementName());
    }

    /**
     * Tests that it is not allowed to change the root element name when the
     * configuration was loaded from a file.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetRootElementNameWhenLoadedFromFile() throws Exception
    {
        final FileHandler handler = new FileHandler(config);
        handler.setFile(new File(TEST_FILE3));
        handler.load();
        assertEquals("testconfig", config.getRootElementName());
        config.setRootElementName("anotherRootElement");
    }
}
