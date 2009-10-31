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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.base.Configuration;
import org.apache.commons.configuration2.base.ConfigurationImpl;
import org.apache.commons.configuration2.base.LocatorSupport;
import org.apache.commons.configuration2.fs.Locator;
import org.apache.commons.configuration2.fs.URLLocator;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code INIConfigurationSource}
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestINIConfigurationSource
{
    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Constant for the content of an ini file. */
    private static final String INI_DATA = "[section1]" + LINE_SEPARATOR
            + "var1 = foo" + LINE_SEPARATOR + "var2 = 451" + LINE_SEPARATOR
            + LINE_SEPARATOR + "[section2]" + LINE_SEPARATOR + "var1 = 123.45"
            + LINE_SEPARATOR + "var2 = bar" + LINE_SEPARATOR + LINE_SEPARATOR
            + "[section3]" + LINE_SEPARATOR + "var1 = true" + LINE_SEPARATOR
            + "interpolated = ${section3.var1}" + LINE_SEPARATOR
            + "multi = foo" + LINE_SEPARATOR + "multi = bar" + LINE_SEPARATOR
            + LINE_SEPARATOR;

    private static final String INI_DATA2 = "[section4]" + LINE_SEPARATOR
            + "var1 = \"quoted value\"" + LINE_SEPARATOR
            + "var2 = \"quoted value\\nwith \\\"quotes\\\"\"" + LINE_SEPARATOR
            + "var3 = 123 ; comment" + LINE_SEPARATOR
            + "var4 = \"1;2;3\" ; comment" + LINE_SEPARATOR
            + "var5 = '\\'quoted\\' \"value\"' ; comment" + LINE_SEPARATOR
            + "var6 = \"\"" + LINE_SEPARATOR;

    private static final String INI_DATA3 = "[section5]" + LINE_SEPARATOR
            + "multiLine = one \\" + LINE_SEPARATOR + "    two      \\"
            + LINE_SEPARATOR + " three" + LINE_SEPARATOR
            + "singleLine = C:\\Temp\\" + LINE_SEPARATOR
            + "multiQuoted = one \\" + LINE_SEPARATOR + "\"  two  \" \\"
            + LINE_SEPARATOR + "  three" + LINE_SEPARATOR
            + "multiComment = one \\ ; a comment" + LINE_SEPARATOR + "two"
            + LINE_SEPARATOR + "multiQuotedComment = \" one \" \\ ; comment"
            + LINE_SEPARATOR + "two" + LINE_SEPARATOR + "noFirstLine = \\"
            + LINE_SEPARATOR + "  line 2" + LINE_SEPARATOR
            + "continueNoLine = one \\" + LINE_SEPARATOR;

    /** An ini file with a global section. */
    private static final String INI_DATA_GLOBAL = "globalVar = testGlobal"
            + LINE_SEPARATOR + LINE_SEPARATOR + INI_DATA;

    /** Constant for the name of the test output file. */
    private static final String TEST_OUT_FILE = "test.ini";

    /** A test ini file. */
    private static File testFile;

    /** The URL to the test file. */
    private static URL testFileURL;

    /** The source to be tested. */
    private INIConfigurationSource source;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        testFile = ConfigurationAssert.getOutFile(TEST_OUT_FILE);
        testFileURL = ConfigurationAssert.getOutURL(TEST_OUT_FILE);
    }

    @Before
    public void setUp() throws Exception
    {
        source = new INIConfigurationSource();
    }

    @After
    public void tearDown() throws Exception
    {
        if (testFile.exists())
        {
            assertTrue("Cannot remove test file: " + testFile, testFile
                    .delete());
        }
    }

    /**
     * Populates the test INIConfigurationSource object with the given data.
     *
     * @param data the data of the configuration source (an ini file as string)
     * @throws ConfigurationException if an error occurs
     */
    private void load(String data) throws ConfigurationException
    {
        StringReader reader = new StringReader(data);
        try
        {
            source.load(reader);
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        reader.close();
    }

    /**
     * Creates a configuration with the test configuration source and
     * initializes the source with the given data.
     *
     * @param data the data (an ini file as string)
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    private Configuration<ConfigurationNode> setUpConfig(String data)
            throws ConfigurationException
    {
        load(data);
        return new ConfigurationImpl<ConfigurationNode>(source);
    }

    /**
     * Tests whether a source can be correctly saved.
     */
    @Test
    public void testSave() throws Exception
    {
        Writer writer = new StringWriter();
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                source);
        config.addProperty("section1.var1", "foo");
        config.addProperty("section1.var2", "451");
        config.addProperty("section2.var1", "123.45");
        config.addProperty("section2.var2", "bar");
        config.addProperty("section3.var1", "true");
        config.addProperty("section3.interpolated", "${section3.var1}");
        config.addProperty("section3.multi", "foo");
        config.addProperty("section3.multi", "bar");
        source.save(writer);

        assertEquals("Wrong content of ini file", INI_DATA, writer.toString());
    }

    /**
     * Tests whether a configuration source with a global section can be saved.
     */
    @Test
    public void testSaveWithGlobalSection() throws ConfigurationException,
            IOException
    {
        load(INI_DATA_GLOBAL);
        StringWriter writer = new StringWriter();
        source.save(writer);
        assertEquals("Wrong content of ini file", INI_DATA_GLOBAL, writer
                .toString());
    }

    /**
     * Tests whether the configuration contains the expected data.
     *
     * @param config the configuration to check
     */
    private void checkContent(Configuration<ConfigurationNode> config)
    {
        assertEquals("Wrong string 1", "foo", config.getString("section1.var1"));
        assertEquals("Wrong int", 451, config.getInt("section1.var2"));
        assertEquals("Wrong double", 123.45, config.getDouble("section2.var1"),
                .001);
        assertEquals("Wrong string 2", "bar", config.getString("section2.var2"));
        assertTrue("Wrong boolean", config.getBoolean("section3.var1"));
        assertEquals("Wrong number of sections", 3, source.getSections().size());
    }

    /**
     * Helper method for testing the load operation. Loads the specified content
     * into a configuration and then checks some properties.
     *
     * @param data the data to load
     */
    private void checkLoad(String data) throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(data);
        checkContent(config);
    }

    /**
     * Tests whether an ini file can be parsed.
     */
    @Test
    public void testLoad() throws ConfigurationException
    {
        checkLoad(INI_DATA);
    }

    /**
     * Tests the load() method if the alternative value separator is used (a ':'
     * for '=').
     */
    @Test
    public void testLoadAlternativeSeparator() throws ConfigurationException
    {
        checkLoad(INI_DATA.replace('=', ':'));
    }

    /**
     * Test whether comment lines are correctly detected.
     */
    @Test
    public void testIsCommentLine()
    {
        assertTrue("# not detected", source.isCommentLine("#comment1"));
        assertTrue("; not detected", source.isCommentLine(";comment1"));
        assertFalse("Wrong comment", source.isCommentLine("nocomment=true"));
        assertFalse("Null detected as comment", source.isCommentLine(null));
    }

    /**
     * Test whether section declarations are correctly detected.
     */
    @Test
    public void testIsSectionLine()
    {
        assertTrue("Not a section", source.isSectionLine("[section]"));
        assertFalse("A section", source.isSectionLine("nosection=true"));
        assertFalse("Null a section", source.isSectionLine(null));
    }

    /**
     * Tests whether sections are correctly extracted.
     */
    @Test
    public void testGetSections()
    {
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                source);
        config.addProperty("test1.foo", "bar");
        config.addProperty("test2.foo", "abc");
        Set<String> expResult = new HashSet<String>();
        expResult.add("test1");
        expResult.add("test2");
        Set<String> result = source.getSections();
        assertEquals("Wrong set with sections", expResult, result);
    }

    @Test
    public void testQuotedValue() throws Exception
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value", config.getString("section4.var1"));
    }

    @Test
    public void testQuotedValueWithQuotes() throws Exception
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value\\nwith \"quotes\"", config
                .getString("section4.var2"));
    }

    @Test
    public void testValueWithComment() throws Exception
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("value", "123", config.getString("section4.var3"));
    }

    @Test
    public void testQuotedValueWithComment() throws Exception
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("value", "1;2;3", config.getString("section4.var4"));
    }

    @Test
    public void testQuotedValueWithSingleQuotes() throws Exception
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("value", "'quoted' \"value\"", config
                .getString("section4.var5"));
    }

    @Test
    public void testWriteValueWithCommentChar() throws Exception
    {
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                source);
        config.setProperty("section.key1", "1;2;3");

        StringWriter writer = new StringWriter();
        source.save(writer);
        source = new INIConfigurationSource();

        Configuration<ConfigurationNode> config2 = setUpConfig(writer
                .toString());
        assertEquals("value", "1;2;3", config2.getString("section.key1"));
    }

    /**
     * Tests whether whitespace is left unchanged for quoted values.
     */
    @Test
    public void testQuotedValueWithWhitespace() throws Exception
    {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \"";
        Configuration<ConfigurationNode> config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests a quoted value with space and a comment.
     */
    @Test
    public void testQuotedValueWithWhitespaceAndComment() throws Exception
    {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \" ; a comment";
        Configuration<ConfigurationNode> config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests an empty quoted value.
     */
    @Test
    public void testQuotedValueEmpty() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        assertEquals("Wrong value for empty property", "", config
                .getString("section4.var6"));
    }

    /**
     * Tests a property that has no value.
     */
    @Test
    public void testGetPropertyNoValue() throws ConfigurationException
    {
        final String data = INI_DATA2 + LINE_SEPARATOR + "noValue ="
                + LINE_SEPARATOR;
        Configuration<ConfigurationNode> config = setUpConfig(data);
        assertEquals("Wrong value of key", "", config
                .getString("section4.noValue"));
    }

    /**
     * Tests a property that has no key.
     */
    @Test
    public void testGetPropertyNoKey() throws ConfigurationException
    {
        final String data = INI_DATA2 + LINE_SEPARATOR + "= noKey"
                + LINE_SEPARATOR;
        Configuration<ConfigurationNode> config = setUpConfig(data);
        assertEquals("Cannot find property with no key", "noKey", config
                .getString("section4. "));
    }

    /**
     * Tests reading a property from the global section.
     */
    @Test
    public void testGlobalProperty() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA_GLOBAL);
        assertEquals("Wrong value of global property", "testGlobal", config
                .getString("globalVar"));
    }

    /**
     * Tests whether the test configuration source contains exactly the expected
     * sections.
     *
     * @param expected an array with the expected sections
     */
    private void checkSectionNames(String[] expected)
    {
        Set<String> sectionNames = source.getSections();
        Iterator<String> it = sectionNames.iterator();
        for (int idx = 0; idx < expected.length; idx++)
        {
            assertEquals("Wrong section at " + idx, expected[idx], it.next());
        }
        assertFalse("Too many sections", it.hasNext());
    }

    /**
     * Tests the names of the sections returned by the configuration source.
     *
     * @param data the data of the ini configuration source
     * @param expected the expected section names
     * @return a configuration wrapping the test source
     */
    private Configuration<ConfigurationNode> checkSectionNames(String data,
            String[] expected) throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(data);
        checkSectionNames(expected);
        return config;
    }

    /**
     * Tests querying the sections if a global section if available.
     */
    @Test
    public void testGetSectionsWithGlobal() throws ConfigurationException
    {
        checkSectionNames(INI_DATA_GLOBAL, new String[] {
                null, "section1", "section2", "section3"
        });
    }

    /**
     * Tests querying the sections if there is no global section.
     */
    @Test
    public void testGetSectionsNoGlobal() throws ConfigurationException
    {
        checkSectionNames(INI_DATA, new String[] {
                "section1", "section2", "section3"
        });
    }

    /**
     * Tests whether variables containing a dot are not misinterpreted as
     * sections. This test is related to CONFIGURATION-327.
     */
    @Test
    public void testGetSectionsDottedVar() throws ConfigurationException
    {
        final String data = "dotted.var = 1" + LINE_SEPARATOR + INI_DATA_GLOBAL;
        Configuration<ConfigurationNode> config = checkSectionNames(data,
                new String[] {
                        null, "section1", "section2", "section3"
                });
        assertEquals("Wrong value of dotted variable", 1, config
                .getInt("dotted..var"));
    }

    /**
     * Tests whether a section added later is also found by getSections().
     */
    @Test
    public void testGetSectionsAdded() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA2);
        config.addProperty("section5.test", Boolean.TRUE);
        checkSectionNames(new String[] {
                "section4", "section5"
        });
    }

    /**
     * Tests whether a sub configuration with the content of a section can be
     * queried.
     */
    @Test
    public void testGetSubConfigurationForSection()
            throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA);
        Configuration<ConfigurationNode> section = config
                .configurationAt("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
    }

    /**
     * Tests a property whose value spans multiple lines.
     */
    @Test
    public void testLineContinuation() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "two"
                + LINE_SEPARATOR + "three", config
                .getString("section5.multiLine"));
    }

    /**
     * Tests a property value that ends on a backslash, which is no line
     * continuation character.
     */
    @Test
    public void testLineContinuationNone() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "C:\\Temp\\", config
                .getString("section5.singleLine"));
    }

    /**
     * Tests a property whose value spans multiple lines when quoting is
     * involved. In this case whitespace must not be trimmed.
     */
    @Test
    public void testLineContinuationQuoted() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "  two  "
                + LINE_SEPARATOR + "three", config
                .getString("section5.multiQuoted"));
    }

    /**
     * Tests a property whose value spans multiple lines with a comment.
     */
    @Test
    public void testLineContinuationComment() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "two", config
                .getString("section5.multiComment"));
    }

    /**
     * Tests a property with a quoted value spanning multiple lines and a
     * comment.
     */
    @Test
    public void testLineContinuationQuotedComment()
            throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", " one " + LINE_SEPARATOR + "two", config
                .getString("section5.multiQuotedComment"));
    }

    /**
     * Tests a multi-line property value with an empty line.
     */
    @Test
    public void testLineContinuationEmptyLine() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", LINE_SEPARATOR + "line 2", config
                .getString("section5.noFirstLine"));
    }

    /**
     * Tests a line continuation at the end of the file.
     */
    @Test
    public void testLineContinuationAtEnd() throws ConfigurationException
    {
        Configuration<ConfigurationNode> config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR, config
                .getString("section5.continueNoLine"));
    }

    /**
     * Writes a test ini file.
     *
     * @param content the content of the file
     * @throws IOException if an error occurs
     */
    private static void writeTestFile(String content) throws IOException
    {
        PrintWriter out = new PrintWriter(new FileWriter(testFile));
        try
        {
            out.println(content);
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Tests whether the LocatorSupport capability is provided.
     */
    @Test
    public void testLocatorSupport()
    {
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        assertNotNull("No locator support", locSupport);
        assertNull("Got already a locator", locSupport.getLocator());
    }

    /**
     * Tests whether the configuration source can be loaded from a locator.
     */
    @Test
    public void testLoadFromLocator() throws IOException,
            ConfigurationException
    {
        writeTestFile(INI_DATA);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.setLocator(new URLLocator(testFileURL));
        locSupport.load();
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                source);
        checkContent(config);
    }

    /**
     * Tests whether the source can save its data to a locator.
     */
    @Test
    public void testSaveToLocator() throws IOException, ConfigurationException
    {
        Locator loc = new URLLocator(testFileURL);
        load(INI_DATA);
        LocatorSupport locSupport = source.getCapability(LocatorSupport.class);
        locSupport.save(loc);
        INIConfigurationSource source2 = new INIConfigurationSource();
        LocatorSupport locSupport2 = source2
                .getCapability(LocatorSupport.class);
        locSupport2.load(loc);
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                source2);
        checkContent(config);
    }
}
