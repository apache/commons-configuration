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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration.ex.ConfigurationException;
import org.apache.commons.configuration.sync.ReadWriteSynchronizer;
import org.apache.commons.configuration.tree.ImmutableNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@code INIConfiguration}.
 *
 * @version $Id$
 */
public class TestINIConfiguration
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

    private static final String INI_DATA_SEPARATORS = "[section]"
            + LINE_SEPARATOR + "var1 = value1" + LINE_SEPARATOR
            + "var2 : value2" + LINE_SEPARATOR
            + "var3=value3" + LINE_SEPARATOR
            + "var4:value4" + LINE_SEPARATOR
            + "var5 : value=5" + LINE_SEPARATOR
            + "var:6=value" + LINE_SEPARATOR
            + "var:7=\"value7\"" + LINE_SEPARATOR
            + "var:8 =  \"value8\"" + LINE_SEPARATOR;

    /** An ini file that contains only a property in the global section. */
    private static final String INI_DATA_GLOBAL_ONLY = "globalVar = testGlobal"
            + LINE_SEPARATOR + LINE_SEPARATOR;

    /** An ini file with a global section. */
    private static final String INI_DATA_GLOBAL = INI_DATA_GLOBAL_ONLY
            + INI_DATA;

    /** A helper object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Creates a INIConfiguration object that is initialized from
     * the given data.
     *
     * @param data the data of the configuration (an ini file as string)
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private static INIConfiguration setUpConfig(String data)
            throws ConfigurationException
    {
        INIConfiguration instance = new INIConfiguration();
        instance.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        load(instance, data);
        return instance;
    }

    /**
     * Loads the specified content into the given configuration instance.
     *
     * @param instance the configuration
     * @param data the data to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(INIConfiguration instance, String data)
            throws ConfigurationException
    {
        StringReader reader = new StringReader(data);
        try
        {
            instance.read(reader);
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e);
        }
        reader.close();
    }

    /**
     * Saves the specified configuration to a string. The string can be compared
     * with an expected value or again loaded into a configuration.
     *
     * @param config the configuration to be saved
     * @return the content of this configuration saved to a string
     * @throws ConfigurationException if an error occurs
     */
    private static String saveToString(INIConfiguration config)
            throws ConfigurationException
    {
        StringWriter writer = new StringWriter();
        try
        {
            config.write(writer);
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e);
        }
        return writer.toString();
    }

    /**
     * Writes a test ini file.
     *
     * @param content the content of the file
     * @return the newly created file
     * @throws IOException if an error occurs
     */
    private File writeTestFile(String content) throws IOException
    {
        File file = folder.newFile();
        PrintWriter out = new PrintWriter(new FileWriter(file));
        try
        {
            out.println(content);
        }
        finally
        {
            out.close();
        }
        return file;
    }

    /**
     * Test of save method, of class {@link INIConfiguration}.
     */
    @Test
    public void testSave() throws Exception
    {
        Writer writer = new StringWriter();
        INIConfiguration instance = new INIConfiguration();
        instance.addProperty("section1.var1", "foo");
        instance.addProperty("section1.var2", "451");
        instance.addProperty("section2.var1", "123.45");
        instance.addProperty("section2.var2", "bar");
        instance.addProperty("section3.var1", "true");
        instance.addProperty("section3.interpolated", "${section3.var1}");
        instance.addProperty("section3.multi", "foo");
        instance.addProperty("section3.multi", "bar");
        instance.write(writer);

        assertEquals("Wrong content of ini file", INI_DATA, writer.toString());
    }

    /**
     * Helper method for testing a save operation. This method constructs a
     * configuration from the specified content string. Then it saves this
     * configuration and checks whether the result matches the original content.
     *
     * @param content the content of the configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkSave(String content) throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(content);
        String sOutput = saveToString(config);
        assertEquals("Wrong content of ini file", content, sOutput);
    }

    /**
     * Tests saving a configuration that contains a global section.
     */
    @Test
    public void testSaveWithGlobalSection() throws ConfigurationException
    {
        checkSave(INI_DATA_GLOBAL);
    }

    /**
     * Tests whether a configuration that contains only a global section can be
     * saved correctly.
     */
    @Test
    public void testSaveWithOnlyGlobalSection() throws ConfigurationException
    {
        checkSave(INI_DATA_GLOBAL_ONLY);
    }

    /**
     * Tests whether list delimiter parsing can be disabled.
     */
    @Test
    public void testSaveWithDelimiterParsingDisabled()
            throws ConfigurationException
    {
        INIConfiguration config = new INIConfiguration();
        String data =
                INI_DATA.substring(0, INI_DATA.length() - LINE_SEPARATOR.length())
                        + "nolist = 1,2, 3";
        load(config, data);
        assertEquals("Wrong property value", "1,2, 3",
                config.getString("section3.nolist"));
        String content = saveToString(config);
        INIConfiguration config2 = new INIConfiguration();
        load(config2, content);
        assertEquals("Wrong property value after reload", "1,2, 3",
                config2.getString("section3.nolist"));
    }

    /**
     * Test of load method, of class {@link INIConfiguration}.
     */
    @Test
    public void testLoad() throws Exception
    {
        checkLoad(INI_DATA);
    }

    /**
     * Tests the load() method when the alternative value separator is used (a
     * ':' for '=').
     */
    @Test
    public void testLoadAlternativeSeparator() throws Exception
    {
        checkLoad(INI_DATA.replace('=', ':'));
    }

    /**
     * Tests whether an instance can be created using a file-based builder.
     */
    @Test
    public void testLoadFromBuilder() throws ConfigurationException,
            IOException
    {
        File file = writeTestFile(INI_DATA);
        FileBasedConfigurationBuilder<INIConfiguration> builder =
                new FileBasedConfigurationBuilder<INIConfiguration>(
                        INIConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl()
                .setFile(file));
        INIConfiguration config = builder.getConfiguration();
        checkContent(config);
    }

    /**
     * Tests the values of some properties to ensure that the configuration was
     * correctly loaded.
     *
     * @param instance the configuration to check
     */
    private void checkContent(INIConfiguration instance)
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
        INIConfiguration instance = setUpConfig(data);
        checkContent(instance);
    }

    /**
     * Test of isCommentLine method, of class
     * {@link INIConfiguration}.
     */
    @Test
    public void testIsCommentLine()
    {
        INIConfiguration instance = new INIConfiguration();
        assertTrue(instance.isCommentLine("#comment1"));
        assertTrue(instance.isCommentLine(";comment1"));
        assertFalse(instance.isCommentLine("nocomment=true"));
        assertFalse(instance.isCommentLine(null));
    }

    /**
     * Test of isSectionLine method, of class
     * {@link INIConfiguration}.
     */
    @Test
    public void testIsSectionLine()
    {
        INIConfiguration instance = new INIConfiguration();
        assertTrue(instance.isSectionLine("[section]"));
        assertFalse(instance.isSectionLine("nosection=true"));
        assertFalse(instance.isSectionLine(null));
    }

    /**
     * Test of getSections method, of class {@link INIConfiguration}
     * .
     */
    @Test
    public void testGetSections()
    {
        INIConfiguration instance = new INIConfiguration();
        instance.addProperty("test1.foo", "bar");
        instance.addProperty("test2.foo", "abc");
        Set<String> expResult = new HashSet<String>();
        expResult.add("test1");
        expResult.add("test2");
        Set<String> result = instance.getSections();
        assertEquals(expResult, result);
    }

    @Test
    public void testQuotedValue() throws Exception
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value", config.getString("section4.var1"));
    }

    @Test
    public void testQuotedValueWithQuotes() throws Exception
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value\\nwith \"quotes\"", config
                .getString("section4.var2"));
    }

    @Test
    public void testValueWithComment() throws Exception
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "123", config.getString("section4.var3"));
    }

    @Test
    public void testQuotedValueWithComment() throws Exception
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "1;2;3", config.getString("section4.var4"));
    }

    @Test
    public void testQuotedValueWithSingleQuotes() throws Exception
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "'quoted' \"value\"", config
                .getString("section4.var5"));
    }

    @Test
    public void testWriteValueWithCommentChar() throws Exception
    {
        INIConfiguration config = new INIConfiguration();
        config.setProperty("section.key1", "1;2;3");

        StringWriter writer = new StringWriter();
        config.write(writer);

        INIConfiguration config2 = new INIConfiguration();
        config2.read(new StringReader(writer.toString()));

        assertEquals("value", "1;2;3", config2.getString("section.key1"));
    }

    /**
     * Tests whether whitespace is left unchanged for quoted values.
     */
    @Test
    public void testQuotedValueWithWhitespace() throws Exception
    {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \"";
        INIConfiguration config = setUpConfig(content);
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
        INIConfiguration config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests an empty quoted value.
     */
    @Test
    public void testQuotedValueEmpty() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA2);
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
        INIConfiguration config = setUpConfig(data);
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
        INIConfiguration config = setUpConfig(data);
        assertEquals("Cannot find property with no key", "noKey", config
                .getString("section4. "));
    }

    /**
     * Tests reading a property from the global section.
     */
    @Test
    public void testGlobalProperty() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
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
    private void checkSectionNames(INIConfiguration config,
            String[] expected)
    {
        Set<String> sectionNames = config.getSections();
        Iterator<String> it = sectionNames.iterator();
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
    private INIConfiguration checkSectionNames(String data,
            String[] expected) throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(data);
        checkSectionNames(config, expected);
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
     * Tests whether the sections of a configuration can be queried that
     * contains only a global section.
     */
    @Test
    public void testGetSectionsGlobalOnly() throws ConfigurationException
    {
        checkSectionNames(INI_DATA_GLOBAL_ONLY, new String[] {
            null
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
        INIConfiguration config = checkSectionNames(data,
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
        INIConfiguration config = setUpConfig(INI_DATA2);
        config.addProperty("section5.test", Boolean.TRUE);
        checkSectionNames(config, new String[] {
                "section4", "section5"
        });
    }

    /**
     * Tests querying the properties of an existing section.
     */
    @Test
    public void testGetSectionExisting() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
    }

    /**
     * Tests querying the properties of a section that was merged from two
     * sections with the same name.
     */
    @Test
    public void testGetSectionMerged() throws ConfigurationException
    {
        final String data = INI_DATA + "[section1]" + LINE_SEPARATOR
                + "var3 = merged" + LINE_SEPARATOR;
        INIConfiguration config = setUpConfig(data);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
        assertEquals("Wrong value of var3", "merged", section.getString("var3"));
    }

    /**
     * Tests querying the content of the global section.
     */
    @Test
    public void testGetSectionGlobal() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
        assertEquals("Wrong value of global variable", "testGlobal", section
                .getString("globalVar"));
    }

    /**
     * Tests concurrent access to the global section.
     */
    @Test
    public void testGetSectionGloabalMultiThreaded()
            throws ConfigurationException, InterruptedException
    {
        INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        config.setSynchronizer(new ReadWriteSynchronizer());
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
    @Test
    public void testGetSectionGlobalNonExisting() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests querying a non existing section.
     */
    @Test
    public void testGetSectionNonExisting() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config
                .getSection("Non existing section");
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests a property whose value spans multiple lines.
     */
    @Test
    public void testLineContinuation() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA3);
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
        INIConfiguration config = setUpConfig(INI_DATA3);
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
        INIConfiguration config = setUpConfig(INI_DATA3);
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
        INIConfiguration config = setUpConfig(INI_DATA3);
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
        INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", " one " + LINE_SEPARATOR + "two", config
                .getString("section5.multiQuotedComment"));
    }

    /**
     * Tests a multi-line property value with an empty line.
     */
    @Test
    public void testLineContinuationEmptyLine() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", LINE_SEPARATOR + "line 2", config
                .getString("section5.noFirstLine"));
    }

    /**
     * Tests a line continuation at the end of the file.
     */
    @Test
    public void testLineContinuationAtEnd() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", "one" + LINE_SEPARATOR, config
                .getString("section5.continueNoLine"));
    }

    /**
     * Tests whether a configuration can be saved that contains section keys
     * with delimiter characters. This test is related to CONFIGURATION-409.
     */
    @Test
    public void testSaveKeysWithDelimiters() throws ConfigurationException, IOException
    {
        INIConfiguration conf = new INIConfiguration();
        final String section = "Section..with..dots";
        conf.addProperty(section + ".test1", "test1");
        conf.addProperty(section + ".test2", "test2");
        StringWriter writer = new StringWriter();
        conf.write(writer);
        conf = new INIConfiguration();
        conf.read(new StringReader(writer.toString()));
        assertEquals("Wrong value (1)", "test1", conf.getString(section + ".test1"));
        assertEquals("Wrong value (2)", "test2", conf.getString(section + ".test2"));
    }

    /**
     * Tests whether a value which contains a semicolon can be loaded
     * successfully. This test is related to CONFIGURATION-434.
     */
    @Test
    public void testValueWithSemicolon() throws ConfigurationException
    {
        final String path =
                "C:\\Program Files\\jar\\manage.jar;"
                        + "C:\\Program Files\\jar\\guiLauncher.jar";
        final String content =
                "[Environment]" + LINE_SEPARATOR + "Application Type=any"
                        + LINE_SEPARATOR + "Class Path=" + path + "  ;comment"
                        + LINE_SEPARATOR + "Path=" + path
                        + "\t; another comment";
        INIConfiguration config = setUpConfig(content);
        assertEquals("Wrong class path", path,
                config.getString("Environment.Class Path"));
        assertEquals("Wrong path", path, config.getString("Environment.Path"));
    }

    /**
     * Tests whether the different separators with or without whitespace are
     * recognized.
     */
    @Test
    public void testSeparators() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        for (int i = 1; i <= 4; i++)
        {
            assertEquals("Wrong value", "value" + i,
                    config.getString("section.var" + i));
        }
    }

    /**
     * Tests property definitions containing multiple separators.
     */
    @Test
    public void testMultipleSeparators() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        assertEquals("Wrong value for var5", "value=5",
                config.getString("section.var5"));
        assertEquals("Wrong value for var6", "6=value",
                config.getString("section.var"));
    }

    /**
     * Tests property definitions containing multiple separators that are
     * quoted.
     */
    @Test
    public void testMultipleSeparatorsQuoted() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        assertEquals("Wrong value for var7", "value7",
                config.getString("section.var:7"));
        assertEquals("Wrong value for var8", "value8",
                config.getString("section.var:8"));
    }

    /**
     * Tests whether a section that has been cleared can be manipulated and
     * saved later.
     */
    @Test
    public void testSaveClearedSection() throws ConfigurationException, IOException
    {
        final String data = "[section]\ntest = failed\n";
        INIConfiguration config = setUpConfig(data);
        HierarchicalConfiguration<ImmutableNode> sub = config.getSection("section");
        assertFalse("No content", sub.isEmpty());
        sub.clear();
        sub.setProperty("test", "success");
        StringWriter writer = new StringWriter();
        config.write(writer);
        HierarchicalConfiguration config2 = setUpConfig(writer.toString());
        assertEquals("Wrong value", "success",
                config2.getString("section.test"));
    }

    /**
     * Tests whether a duplicate session is merged.
     */
    @Test
    public void testMergeDuplicateSection() throws ConfigurationException, IOException
    {
        final String data =
                "[section]\nvar1 = sec1\n\n" + "[section]\nvar2 = sec2\n";
        INIConfiguration config = setUpConfig(data);
        assertEquals("Wrong value 1", "sec1", config.getString("section.var1"));
        assertEquals("Wrong value 2", "sec2", config.getString("section.var2"));
        HierarchicalConfiguration<ImmutableNode> sub = config.getSection("section");
        assertEquals("Wrong sub value 1", "sec1", sub.getString("var1"));
        assertEquals("Wrong sub value 2", "sec2", sub.getString("var2"));
        StringWriter writer = new StringWriter();
        config.write(writer);
        String content = writer.toString();
        int pos = content.indexOf("[section]");
        assertTrue("Section not found: " + content, pos >= 0);
        assertTrue("Section found multiple times: " + content,
                content.indexOf("[section]", pos + 1) < 0);
    }

    /**
     * Tests whether a section that was created by getSection() can be
     * manipulated.
     */
    @Test
    public void testGetSectionNonExistingManipulate()
            throws ConfigurationException, IOException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("newSection");
        section.addProperty("test", "success");
        assertEquals("Main config not updated", "success",
                config.getString("newSection.test"));
        StringWriter writer = new StringWriter();
        config.write(writer);
        INIConfiguration config2 = setUpConfig(writer.toString());
        section = config2.getSection("newSection");
        assertEquals("Wrong value", "success", section.getString("test"));
    }

    /**
     * Tests whether getSection() can deal with duplicate sections.
     */
    @Test
    public void testGetSectionDuplicate()
    {
        INIConfiguration config =
                new INIConfiguration();
        config.addProperty("section.var1", "value1");
        config.addProperty("section(-1).var2", "value2");
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("section");
        Iterator<String> keys = section.getKeys();
        assertEquals("Wrong key", "var1", keys.next());
        assertFalse("Too many keys", keys.hasNext());
    }

    /**
     * Tests whether the list delimiter character is recognized.
     */
    @Test
    public void testValueWithDelimiters() throws ConfigurationException
    {
        INIConfiguration config =
                setUpConfig("[test]" + LINE_SEPARATOR + "list=1,2,3"
                        + LINE_SEPARATOR);
        List<Object> list = config.getList("test.list");
        assertEquals("Wrong number of elements", 3, list.size());
        assertEquals("Wrong element at 1", "1", list.get(0));
        assertEquals("Wrong element at 2", "2", list.get(1));
        assertEquals("Wrong element at 3", "3", list.get(2));
    }

    /**
     * Tests whether parsing of lists can be disabled.
     */
    @Test
    public void testListParsingDisabled() throws ConfigurationException
    {
        INIConfiguration config =
                new INIConfiguration();
        load(config, "[test]" + LINE_SEPARATOR + "nolist=1,2,3");
        assertEquals("Wrong value", "1,2,3", config.getString("test.nolist"));
    }

    /**
     * Tests whether synchronization is performed when querying the
     * configuration's sections.
     */
    @Test
    public void testGetSectionsSynchronized() throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        assertFalse("No sections", config.getSections().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Helper method for testing whether the access to a section is
     * synchronized. This method delegates to the method with the same name
     * passing in default methods.
     *
     * @param sectionName the name of the section to be queried
     * @throws ConfigurationException if an error occurs
     */
    private void checkGetSectionSynchronized(String sectionName)
            throws ConfigurationException
    {
        checkGetSectionSynchronized(sectionName, Methods.BEGIN_WRITE,
                Methods.END_WRITE);
    }

    /**
     * Helper method for testing whether the access to a section causes the
     * specified methods to be called on the synchronizer.
     *
     * @param sectionName the name of the section to be queried
     * @param expMethods the expected methods
     * @throws ConfigurationException if an error occurs
     */
    private void checkGetSectionSynchronized(String sectionName,
            Methods... expMethods) throws ConfigurationException
    {
        INIConfiguration config = setUpConfig(INI_DATA);
        SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        assertNotNull("No global section", config.getSection(sectionName));
        sync.verify(expMethods);
    }

    /**
     * Tests whether access to the global section is synchronized.
     */
    @Test
    public void testGetSectionGlobalSynchronized()
            throws ConfigurationException
    {
        checkGetSectionSynchronized(null);
    }

    /**
     * Tests whether access to an existing section is synchronized.
     */
    @Test
    public void testGetSectionExistingSynchronized()
            throws ConfigurationException
    {
        // 1 x configurationAt(), then direct access to root node
        checkGetSectionSynchronized("section1");
    }

    /**
     * Tests whether access to a non-existing section is synchronized.
     */
    @Test
    public void testGetSectionNonExistingSynchronized()
            throws ConfigurationException
    {
        checkGetSectionSynchronized("Non-existing-section?",
                Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_WRITE,
                Methods.END_WRITE);
    }

    /**
     * Tests whether the configuration deals correctly with list delimiters.
     */
    @Test
    public void testListDelimiterHandling() throws ConfigurationException
    {
        INIConfiguration config = new INIConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.addProperty("list", "a,b,c");
        config.addProperty("listesc", "3\\,1415");
        String output = saveToString(config);
        INIConfiguration config2 = setUpConfig(output);
        assertEquals("Wrong list size", 3, config2.getList("list").size());
        assertEquals("Wrong list element", "b", config2.getList("list").get(1));
        assertEquals("Wrong escaped list element", "3,1415",
                config2.getString("listesc"));
    }

    /**
     * Tests whether property values are correctly escaped even if they are part
     * of a property with multiple values.
     */
    @Test
    public void testListDelimiterHandlingInList() throws ConfigurationException
    {
        String data =
                INI_DATA + "[sectest]" + LINE_SEPARATOR
                        + "list = 3\\,1415,pi,\\\\Test\\,5" + LINE_SEPARATOR;
        INIConfiguration config = setUpConfig(data);
        INIConfiguration config2 = setUpConfig(saveToString(config));
        List<Object> list = config2.getList("sectest.list");
        assertEquals("Wrong number of values", 3, list.size());
        assertEquals("Wrong element 1", "3,1415", list.get(0));
        assertEquals("Wrong element 3", "\\Test,5", list.get(2));
    }

    /**
     * A thread class for testing concurrent access to the global section.
     */
    private static class GlobalSectionTestThread extends Thread
    {
        /** The configuration. */
        private final INIConfiguration config;

        /** A flag whether an error was found. */
        volatile boolean error;

        /**
         * Creates a new instance of <code>GlobalSectionTestThread</code> and
         * initializes it.
         *
         * @param conf the configuration object
         */
        public GlobalSectionTestThread(INIConfiguration conf)
        {
            config = conf;
        }

        /**
         * Accesses the global section in a loop. If there is no correct
         * synchronization, this can cause an exception.
         */
        @Override
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
