/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.commons.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalINIConfiguration.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestHierarchicalINIConfiguration extends TestCase
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
            + "multiLine = one \\" + LINE_SEPARATOR
            + "    two      \\" + LINE_SEPARATOR
            + " three" + LINE_SEPARATOR
            + "singleLine = C:\\Temp\\" + LINE_SEPARATOR
            + "multiQuoted = one \\" + LINE_SEPARATOR
            + "\"  two  \" \\" + LINE_SEPARATOR
            + "  three" + LINE_SEPARATOR
            + "multiComment = one \\ ; a comment" + LINE_SEPARATOR
            + "two" + LINE_SEPARATOR
            + "multiQuotedComment = \" one \" \\ ; comment" + LINE_SEPARATOR
            + "two" + LINE_SEPARATOR
            + "noFirstLine = \\" + LINE_SEPARATOR
            + "  line 2" + LINE_SEPARATOR
            + "continueNoLine = one \\" + LINE_SEPARATOR;

    /** An ini file with a global section. */
    private static final String INI_DATA_GLOBAL = "globalVar = testGlobal"
            + LINE_SEPARATOR + LINE_SEPARATOR + INI_DATA;

    /** A test ini file. */
    private static final File TEST_FILE = new File("target/test.ini");

    protected void tearDown() throws Exception
    {
        if (TEST_FILE.exists())
        {
            assertTrue("Cannot remove test file: " + TEST_FILE, TEST_FILE
                    .delete());
        }

        super.tearDown();
    }

    /**
     * Creates a HierarchicalINIConfiguration object that is initialized from
     * the given data.
     *
     * @param data the data of the configuration (an ini file as string)
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private static HierarchicalINIConfiguration setUpConfig(String data)
            throws ConfigurationException
    {
        StringReader reader = new StringReader(data);
        HierarchicalINIConfiguration instance = new HierarchicalINIConfiguration();
        instance.load(reader);
        reader.close();
        return instance;
    }

    /**
     * Writes a test ini file.
     *
     * @param content the content of the file
     * @throws IOException if an error occurs
     */
    private static void writeTestFile(String content) throws IOException
    {
        PrintWriter out = new PrintWriter(new FileWriter(TEST_FILE));
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
     * Test of save method, of class {@link HierarchicalINIConfiguration}.
     */
    public void testSave() throws Exception
    {
        Writer writer = new StringWriter();
        HierarchicalINIConfiguration instance = new HierarchicalINIConfiguration();
        instance.addProperty("section1.var1", "foo");
        instance.addProperty("section1.var2", "451");
        instance.addProperty("section2.var1", "123.45");
        instance.addProperty("section2.var2", "bar");
        instance.addProperty("section3.var1", "true");
        instance.addProperty("section3.interpolated", "${section3.var1}");
        instance.addProperty("section3.multi", "foo");
        instance.addProperty("section3.multi", "bar");
        instance.save(writer);

        assertEquals("Wrong content of ini file", INI_DATA, writer.toString());
    }

    /**
     * Tests saving a configuration that contains a global section.
     */
    public void testSaveWithGlobalSection() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        StringWriter writer = new StringWriter();
        config.save(writer);
        assertEquals("Wrong content of ini file", INI_DATA_GLOBAL, writer
                .toString());
    }

    /**
     * Test of load method, of class {@link HierarchicalINIConfiguration}.
     */
    public void testLoad() throws Exception
    {
        checkLoad(INI_DATA);
    }

    /**
     * Tests the load() method when the alternative value separator is used (a
     * ':' for '=').
     */
    public void testLoadAlternativeSeparator() throws Exception
    {
        checkLoad(INI_DATA.replace('=', ':'));
    }

    /**
     * Tests loading a configuration from a File.
     */
    public void testLoadFile() throws ConfigurationException, IOException
    {
        writeTestFile(INI_DATA);
        HierarchicalINIConfiguration config = new HierarchicalINIConfiguration(
                TEST_FILE);
        checkContent(config);
    }

    /**
     * Tests loading a configuration from a file name.
     */
    public void testLoadFileName() throws ConfigurationException, IOException
    {
        writeTestFile(INI_DATA);
        HierarchicalINIConfiguration config = new HierarchicalINIConfiguration(
                TEST_FILE.getAbsolutePath());
        checkContent(config);
    }

    /**
     * Tests loading a configuration from a URL.
     */
    public void testLoadURL() throws ConfigurationException, IOException
    {
        writeTestFile(INI_DATA);
        HierarchicalINIConfiguration config = new HierarchicalINIConfiguration(
                TEST_FILE.toURL());
        checkContent(config);
    }

    /**
     * Tests the values of some properties to ensure that the configuration was
     * correctly loaded.
     *
     * @param instance the configuration to check
     */
    private void checkContent(HierarchicalINIConfiguration instance)
    {
        assertTrue(instance.getString("section1.var1").equals("foo"));
        assertTrue(instance.getInt("section1.var2") == 451);
        assertTrue(instance.getDouble("section2.var1") == 123.45);
        assertTrue(instance.getString("section2.var2").equals("bar"));
        assertTrue(instance.getBoolean("section3.var1"));
        assertTrue(instance.getSections().size() == 3);
    }

    /**
     * Helper method for testing the load operation. Loads the specified content
     * into a configuration and then checks some properties.
     *
     * @param data the data to load
     */
    private void checkLoad(String data) throws ConfigurationException
    {
        HierarchicalINIConfiguration instance = setUpConfig(data);
        checkContent(instance);
    }

    /**
     * Test of isCommentLine method, of class
     * {@link HierarchicalINIConfiguration}.
     */
    public void testIsCommentLine()
    {
        HierarchicalINIConfiguration instance = new HierarchicalINIConfiguration();
        assertTrue(instance.isCommentLine("#comment1"));
        assertTrue(instance.isCommentLine(";comment1"));
        assertFalse(instance.isCommentLine("nocomment=true"));
        assertFalse(instance.isCommentLine(null));
    }

    /**
     * Test of isSectionLine method, of class
     * {@link HierarchicalINIConfiguration}.
     */
    public void testIsSectionLine()
    {
        HierarchicalINIConfiguration instance = new HierarchicalINIConfiguration();
        assertTrue(instance.isSectionLine("[section]"));
        assertFalse(instance.isSectionLine("nosection=true"));
        assertFalse(instance.isSectionLine(null));
    }

    /**
     * Test of getSections method, of class {@link HierarchicalINIConfiguration}
     * .
     */
    public void testGetSections()
    {
        HierarchicalINIConfiguration instance = new HierarchicalINIConfiguration();
        instance.addProperty("test1.foo", "bar");
        instance.addProperty("test2.foo", "abc");
        Set expResult = new HashSet();
        expResult.add("test1");
        expResult.add("test2");
        Set result = instance.getSections();
        assertEquals(expResult, result);
    }

    public void testQuotedValue() throws Exception
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value", config.getString("section4.var1"));
    }

    public void testQuotedValueWithQuotes() throws Exception
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value\\nwith \"quotes\"", config
                .getString("section4.var2"));
    }

    public void testValueWithComment() throws Exception
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "123", config.getString("section4.var3"));
    }

    public void testQuotedValueWithComment() throws Exception
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "1;2;3", config.getString("section4.var4"));
    }

    public void testQuotedValueWithSingleQuotes() throws Exception
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "'quoted' \"value\"", config
                .getString("section4.var5"));
    }

    public void testWriteValueWithCommentChar() throws Exception
    {
        HierarchicalINIConfiguration config = new HierarchicalINIConfiguration();
        config.setProperty("section.key1", "1;2;3");

        StringWriter writer = new StringWriter();
        config.save(writer);

        HierarchicalINIConfiguration config2 = new HierarchicalINIConfiguration();
        config2.load(new StringReader(writer.toString()));

        assertEquals("value", "1;2;3", config2.getString("section.key1"));
    }

    /**
     * Tests whether whitespace is left unchanged for quoted values.
     */
    public void testQuotedValueWithWhitespace() throws Exception
    {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \"";
        HierarchicalINIConfiguration config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests a quoted value with space and a comment.
     */
    public void testQuotedValueWithWhitespaceAndComment() throws Exception
    {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \" ; a comment";
        HierarchicalINIConfiguration config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests an empty quoted value.
     */
    public void testQuotedValueEmpty() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("Wrong value for empty property", "", config
                .getString("section4.var6"));
    }

    /**
     * Tests a property that has no value.
     */
    public void testGetPropertyNoValue() throws ConfigurationException
    {
        final String data = INI_DATA2 + LINE_SEPARATOR + "noValue ="
                + LINE_SEPARATOR;
        HierarchicalINIConfiguration config = setUpConfig(data);
        assertEquals("Wrong value of key", "", config
                .getString("section4.noValue"));
    }

    /**
     * Tests a property that has no key.
     */
    public void testGetPropertyNoKey() throws ConfigurationException
    {
        final String data = INI_DATA2 + LINE_SEPARATOR + "= noKey"
                + LINE_SEPARATOR;
        HierarchicalINIConfiguration config = setUpConfig(data);
        assertEquals("Cannot find property with no key", "noKey", config
                .getString("section4. "));
    }

    /**
     * Tests reading a property from the global section.
     */
    public void testGlobalProperty() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        assertEquals("Wrong value of global property", "testGlobal", config
                .getString("globalVar"));
    }

    /**
     * Tests whether the specified configuration contains exactly the expected
     * sections.
     *
     * @param config the configuration to check
     * @param expected an array with the expected sections
     */
    private void checkSectionNames(HierarchicalINIConfiguration config,
            String[] expected)
    {
        Set sectionNames = config.getSections();
        Iterator it = sectionNames.iterator();
        for (int idx = 0; idx < expected.length; idx++)
        {
            assertEquals("Wrong section at " + idx, expected[idx], it.next());
        }
        assertFalse("Too many sections", it.hasNext());
    }

    /**
     * Tests the names of the sections returned by the configuration.
     *
     * @param data the data of the ini configuration
     * @param expected the expected section names
     * @return the configuration instance
     */
    private HierarchicalINIConfiguration checkSectionNames(String data,
            String[] expected) throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(data);
        checkSectionNames(config, expected);
        return config;
    }

    /**
     * Tests querying the sections if a global section if available.
     */
    public void testGetSectionsWithGlobal() throws ConfigurationException
    {
        checkSectionNames(INI_DATA_GLOBAL, new String[] {
                null, "section1", "section2", "section3"
        });
    }

    /**
     * Tests querying the sections if there is no global section.
     */
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
    public void testGetSectionsDottedVar() throws ConfigurationException
    {
        final String data = "dotted.var = 1" + LINE_SEPARATOR + INI_DATA_GLOBAL;
        HierarchicalINIConfiguration config = checkSectionNames(data,
                new String[] {
                        null, "section1", "section2", "section3"
                });
        assertEquals("Wrong value of dotted variable", 1, config
                .getInt("dotted..var"));
    }

    /**
     * Tests whether a section added later is also found by getSections().
     */
    public void testGetSectionsAdded() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA2);
        config.addProperty("section5.test", Boolean.TRUE);
        checkSectionNames(config, new String[] {
                "section4", "section5"
        });
    }

    /**
     * Tests querying the properties of an existing section.
     */
    public void testGetSectionExisting() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA);
        SubnodeConfiguration section = config.getSection("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
    }

    /**
     * Tests querying the properties of a section that was merged from two
     * sections with the same name.
     */
    public void testGetSectionMerged() throws ConfigurationException
    {
        final String data = INI_DATA + "[section1]" + LINE_SEPARATOR
                + "var3 = merged" + LINE_SEPARATOR;
        HierarchicalINIConfiguration config = setUpConfig(data);
        SubnodeConfiguration section = config.getSection("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
        assertEquals("Wrong value of var3", "merged", section.getString("var3"));
    }

    /**
     * Tests querying the content of the global section.
     */
    public void testGetSectionGlobal() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        SubnodeConfiguration section = config.getSection(null);
        assertEquals("Wrong value of global variable", "testGlobal", section
                .getString("globalVar"));
    }

    /**
     * Tests concurrent access to the global section.
     */
    public void testGetSectionGloabalMultiThreaded()
            throws ConfigurationException, InterruptedException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final int threadCount = 10;
        GlobalSectionTestThread[] threads = new GlobalSectionTestThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new GlobalSectionTestThread(config);
            threads[i].start();
        }
        for (int i = 0; i < threadCount; i++)
        {
            threads[i].join();
            assertFalse("Exception occurred", threads[i].error);
        }
    }

    /**
     * Tests querying the content of the global section if there is none.
     */
    public void testGetSectionGlobalNonExisting() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA);
        SubnodeConfiguration section = config.getSection(null);
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests querying a non existing section.
     */
    public void testGetSectionNonExisting() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA);
        SubnodeConfiguration section = config
                .getSection("Non existing section");
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests a property whose value spans multiple lines.
     */
    public void testLineContinuation() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "two"
                + LINE_SEPARATOR + "three", config
                .getString("section5.multiLine"));
    }

    /**
     * Tests a property value that ends on a backslash, which is no line
     * continuation character.
     */
    public void testLineContinuationNone() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "C:\\Temp\\", config
                .getString("section5.singleLine"));
    }

    /**
     * Tests a property whose value spans multiple lines when quoting is
     * involved. In this case whitespace must not be trimmed.
     */
    public void testLineContinuationQuoted() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "  two  "
                + LINE_SEPARATOR + "three", config
                .getString("section5.multiQuoted"));
    }

    /**
     * Tests a property whose value spans multiple lines with a comment.
     */
    public void testLineContinuationComment() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR + "two", config
                .getString("section5.multiComment"));
    }

    /**
     * Tests a property with a quoted value spanning multiple lines and a
     * comment.
     */
    public void testLineContinuationQuotedComment()
            throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", " one " + LINE_SEPARATOR + "two", config
                .getString("section5.multiQuotedComment"));
    }

    /**
     * Tests a multi-line property value with an empty line.
     */
    public void testLineContinuationEmptyLine() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", LINE_SEPARATOR + "line 2", config
                .getString("section5.noFirstLine"));
    }

    /**
     * Tests a line continuation at the end of the file.
     */
    public void testLineContinuationAtEnd() throws ConfigurationException
    {
        HierarchicalINIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR, config
                .getString("section5.continueNoLine"));
    }

    /**
     * A thread class for testing concurrent access to the global section.
     */
    private static class GlobalSectionTestThread extends Thread
    {
        /** The configuration. */
        private final HierarchicalINIConfiguration config;

        /** A flag whether an error was found. */
        volatile boolean error;

        /**
         * Creates a new instance of <code>GlobalSectionTestThread</code> and
         * initializes it.
         *
         * @param conf the configuration object
         */
        public GlobalSectionTestThread(HierarchicalINIConfiguration conf)
        {
            config = conf;
        }

        /**
         * Accesses the global section in a loop. If there is no correct
         * synchronization, this can cause an exception.
         */
        public void run()
        {
            final int loopCount = 250;

            for (int i = 0; i < loopCount && !error; i++)
            {
                try
                {
                    config.getSection(null);
                }
                catch (IllegalStateException istex)
                {
                    error = true;
                }
            }
        }
    }
}
