/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

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
     * Tests if properties can be appended by simply calling load() another
     * time.
     */
    public void testAppend() throws Exception
    {
        File file2 = new File("conf/threesome.properties");
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
    
    /**
     * Tests to load a file with enabled auto save mode.
     */
    public void testLoadWithAutoSave() throws Exception
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
            if(out != null)
            {
                out.close();
            }
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
        assertTrue("Property could not be found", content
                .indexOf("prop = value" + EOL) > 0);
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
}
