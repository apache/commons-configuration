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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private String testProperties = new File("conf/test.properties").getAbsolutePath();

    private String testBasePath = new File("conf").getAbsolutePath();
    private String testBasePath2 = new File("conf").getAbsoluteFile().getParentFile().getAbsolutePath();
    private File testSavePropertiesFile = new File("target/testsave.properties");

    protected void setUp() throws Exception
    {
        conf = new PropertiesConfiguration(testProperties);
    }

    public void testLoad() throws Exception
    {
        String loaded = conf.getString("configuration.loaded");
        assertEquals("true", loaded);
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
        conf.load("conf/test.properties");

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
        // remove the file previously saved if necessary
        if (testSavePropertiesFile.exists())
        {
            assertTrue(testSavePropertiesFile.delete());
        }

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
        for (Iterator i = conf.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));
            assertEquals("Value of the '" + key + "' property", conf.getProperty(key), checkConfig.getProperty(key));
        }

        // Save it again, verifing a save with a filename works.
        checkConfig.save();
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
        // remove the file previously saved if necessary
        if (testSavePropertiesFile.exists())
        {
            assertTrue(testSavePropertiesFile.delete());
        }
        
        conf.setProperty("test", "true");
        conf.setBasePath(testSavePropertiesFile.getParentFile().toURL().toString());
        conf.setFileName(testSavePropertiesFile.getName());
        conf.save();
        assertTrue(testSavePropertiesFile.exists());
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
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));

        pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadFromJAR() throws Exception
    {
        conf = new PropertiesConfiguration();
        conf.setIncludesAllowed(true);
        conf.setFileName("test-jar.properties");
        conf.load();

        assertEquals("jar", conf.getProperty("configuration.location"));
        assertEquals("property in an included file", "jar", conf.getProperty("include.location"));
    }

    public void testLoadFromFile() throws Exception
    {
        File file = new File("conf/test.properties");
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

    public void testChangingDelimiter() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration(testProperties);
        assertEquals(4, pc.getList("test.mixed.array").size());

        char delimiter = PropertiesConfiguration.getDelimiter();
        PropertiesConfiguration.setDelimiter('^');
        pc = new PropertiesConfiguration(testProperties);
        assertEquals(2, pc.getList("test.mixed.array").size());
        PropertiesConfiguration.setDelimiter(delimiter);
    }

}
