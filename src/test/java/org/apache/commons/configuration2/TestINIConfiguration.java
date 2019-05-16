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

package org.apache.commons.configuration2;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeNameMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@code INIConfiguration}.
 *
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

    private static final String INI_DATA4 = "[section6]" + LINE_SEPARATOR
    		+ "key1{0}value1" + LINE_SEPARATOR
    		+ "key2{0}value2" + LINE_SEPARATOR + LINE_SEPARATOR
    		+ "[section7]" + LINE_SEPARATOR
    		+ "key3{0}value3" + LINE_SEPARATOR;

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
    private static INIConfiguration setUpConfig(final String data)
            throws ConfigurationException
    {
        final INIConfiguration instance = new INIConfiguration();
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
    private static void load(final INIConfiguration instance, final String data)
            throws ConfigurationException
    {
        final StringReader reader = new StringReader(data);
        try
        {
            instance.read(reader);
        }
        catch (final IOException e)
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
    private static String saveToString(final INIConfiguration config)
            throws ConfigurationException
    {
        final StringWriter writer = new StringWriter();
        try
        {
            config.write(writer);
        }
        catch (final IOException e)
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
    private File writeTestFile(final String content) throws IOException
    {
        final File file = folder.newFile();
        final PrintWriter out = new PrintWriter(new FileWriter(file));
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
        final Writer writer = new StringWriter();
        final INIConfiguration instance = new INIConfiguration();
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
     * Test of save method with changed separator
     */
    @Test
    public void testSeparatorUsedInINIOutput() throws Exception
    {
    	final String outputSeparator = ": ";
    	final String input = MessageFormat.format(INI_DATA4, "=").trim();
    	final String expectedOutput = MessageFormat.format(INI_DATA4, outputSeparator).trim();

    	final INIConfiguration instance = new FileBasedConfigurationBuilder<>(
    	        INIConfiguration.class)
                .configure(new Parameters().ini().setSeparatorUsedInOutput(outputSeparator))
                .getConfiguration();
        load(instance, input);

        final Writer writer = new StringWriter();
        instance.write(writer);
        final String result = writer.toString().trim();

        assertEquals("Wrong content of ini file", expectedOutput, result);
    }

    /**
     * Test of read method with changed separator.
     */
    @Test
    public void testSeparatorUsedInINIInput() throws Exception
    {
        final String inputSeparator = "=";
        final String input = "[section]" + LINE_SEPARATOR
            + "k1:v1$key1=value1" + LINE_SEPARATOR
            + "k1:v1,k2:v2$key2=value2" + LINE_SEPARATOR
            + "key3:value3" + LINE_SEPARATOR
            + "key4 = value4" + LINE_SEPARATOR;

        final INIConfiguration instance = new FileBasedConfigurationBuilder<>(
            INIConfiguration.class)
            .configure(new Parameters().ini().setSeparatorUsedInInput(inputSeparator))
            .getConfiguration();
        load(instance, input);

        assertEquals("value1", instance.getString("section.k1:v1$key1"));
        assertEquals("value2", instance.getString("section.k1:v1,k2:v2$key2"));
        assertEquals("", instance.getString("section.key3:value3"));
        assertEquals("value4", instance.getString("section.key4").trim());
    }

    /**
     * Test of read method with changed comment leading separator
     */
    @Test
    public void testCommentLeadingSeparatorUsedInINIInput() throws Exception
    {
        final String inputCommentLeadingSeparator = ";";
        final String input = "[section]" + LINE_SEPARATOR
            + "key1=a;b;c" + LINE_SEPARATOR
            + "key2=a#b#c" + LINE_SEPARATOR
            + ";key3=value3" + LINE_SEPARATOR
            + "#key4=value4" + LINE_SEPARATOR;

        final INIConfiguration instance = new FileBasedConfigurationBuilder<>(
            INIConfiguration.class)
            .configure(new Parameters().ini()
                .setCommentLeadingCharsUsedInInput(inputCommentLeadingSeparator))
            .getConfiguration();
        load(instance, input);

        assertEquals("a;b;c", instance.getString("section.key1"));
        assertEquals("a#b#c", instance.getString("section.key2"));
        assertNull("", instance.getString("section.;key3"));
        assertEquals("value4", instance.getString("section.#key4"));
    }

    /**
     * Helper method for testing a save operation. This method constructs a
     * configuration from the specified content string. Then it saves this
     * configuration and checks whether the result matches the original content.
     *
     * @param content the content of the configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkSave(final String content) throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(content);
        final String sOutput = saveToString(config);
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
        final INIConfiguration config = new INIConfiguration();
        final String data =
                INI_DATA.substring(0, INI_DATA.length() - LINE_SEPARATOR.length())
                        + "nolist = 1,2, 3";
        load(config, data);
        assertEquals("Wrong property value", "1,2, 3",
                config.getString("section3.nolist"));
        final String content = saveToString(config);
        final INIConfiguration config2 = new INIConfiguration();
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
        final File file = writeTestFile(INI_DATA);
        final FileBasedConfigurationBuilder<INIConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        INIConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl()
                .setFile(file));
        final INIConfiguration config = builder.getConfiguration();
        checkContent(config);
    }

    /**
     * Tests the values of some properties to ensure that the configuration was
     * correctly loaded.
     *
     * @param instance the configuration to check
     */
    private void checkContent(final INIConfiguration instance)
    {
        assertEquals("var1", "foo", instance.getString("section1.var1"));
        assertEquals("var2", 451, instance.getInt("section1.var2"));
        assertEquals("section2.var1", 123.45,
                instance.getDouble("section2.var1"), .001);
        assertEquals("section2.var2", "bar",
                instance.getString("section2.var2"));
        assertEquals("section3.var1", true,
                instance.getBoolean("section3.var1"));
        assertEquals("Wrong number of sections", 3, instance.getSections()
                .size());
        assertTrue(
                "Wrong sections",
                instance.getSections().containsAll(
                        Arrays.asList("section1", "section2", "section3")));
    }

    /**
     * Helper method for testing the load operation. Loads the specified content
     * into a configuration and then checks some properties.
     *
     * @param data the data to load
     */
    private void checkLoad(final String data) throws ConfigurationException
    {
        final INIConfiguration instance = setUpConfig(data);
        checkContent(instance);
    }

    /**
     * Test of isCommentLine method, of class
     * {@link INIConfiguration}.
     */
    @Test
    public void testIsCommentLine()
    {
        final INIConfiguration instance = new INIConfiguration();
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
        final INIConfiguration instance = new INIConfiguration();
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
        final INIConfiguration instance = new INIConfiguration();
        instance.addProperty("test1.foo", "bar");
        instance.addProperty("test2.foo", "abc");
        final Set<String> expResult = new HashSet<>();
        expResult.add("test1");
        expResult.add("test2");
        final Set<String> result = instance.getSections();
        assertEquals(expResult, result);
    }

    @Test
    public void testQuotedValue() throws Exception
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value", config.getString("section4.var1"));
    }

    @Test
    public void testQuotedValueWithQuotes() throws Exception
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "quoted value\\nwith \"quotes\"", config
                .getString("section4.var2"));
    }

    @Test
    public void testValueWithComment() throws Exception
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "123", config.getString("section4.var3"));
    }

    @Test
    public void testQuotedValueWithComment() throws Exception
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "1;2;3", config.getString("section4.var4"));
    }

    @Test
    public void testQuotedValueWithSingleQuotes() throws Exception
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("value", "'quoted' \"value\"", config
                .getString("section4.var5"));
    }

    @Test
    public void testWriteValueWithCommentChar() throws Exception
    {
        final INIConfiguration config = new INIConfiguration();
        config.setProperty("section.key1", "1;2;3");

        final StringWriter writer = new StringWriter();
        config.write(writer);

        final INIConfiguration config2 = new INIConfiguration();
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
        final INIConfiguration config = setUpConfig(content);
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
        final INIConfiguration config = setUpConfig(content);
        assertEquals("Wrong propert value", " [test@cmd ~]$ ", config
                .getString("CmdPrompt"));
    }

    /**
     * Tests an empty quoted value.
     */
    @Test
    public void testQuotedValueEmpty() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA2);
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
        final INIConfiguration config = setUpConfig(data);
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
        final INIConfiguration config = setUpConfig(data);
        assertEquals("Cannot find property with no key", "noKey", config
                .getString("section4. "));
    }

    /**
     * Tests reading a property from the global section.
     */
    @Test
    public void testGlobalProperty() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        assertEquals("Wrong value of global property", "testGlobal", config
                .getString("globalVar"));
    }

    /**
     * Tests whether the sub configuration for the global section is connected
     * to its parent.
     */
    @Test
    public void testGlobalSectionConnected() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection(null);
        config.setProperty("globalVar", "changed");
        assertEquals("Wrong value in sub", "changed",
                sub.getString("globalVar"));
    }

    /**
     * Tests whether the specified configuration contains exactly the expected
     * sections.
     *
     * @param config the configuration to check
     * @param expected an array with the expected sections
     */
    private void checkSectionNames(final INIConfiguration config,
            final String[] expected)
    {
        final Set<String> sectionNames = config.getSections();
        final Iterator<String> it = sectionNames.iterator();
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
    private INIConfiguration checkSectionNames(final String data,
            final String[] expected) throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(data);
        checkSectionNames(config, expected);
        return config;
    }

    /**
     * Tests querying the sections if a global section if available.
     */
    @Test
    public void testGetSectionsWithGlobal() throws ConfigurationException
    {
        checkSectionNames(INI_DATA_GLOBAL, new String[]{
                null, "section1", "section2", "section3"
        });
    }

    /**
     * Tests querying the sections if there is no global section.
     */
    @Test
    public void testGetSectionsNoGlobal() throws ConfigurationException
    {
        checkSectionNames(INI_DATA, new String[]{
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
        checkSectionNames(INI_DATA_GLOBAL_ONLY, new String[]{
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
        final INIConfiguration config = checkSectionNames(data,
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
        final INIConfiguration config = setUpConfig(INI_DATA2);
        config.addProperty("section5.test", Boolean.TRUE);
        checkSectionNames(config, new String[]{
                "section4", "section5"
        });
    }

    /**
     * Tests querying the properties of an existing section.
     */
    @Test
    public void testGetSectionExisting() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section =
                config.getSection("section1");
        assertEquals("Wrong value of var1", "foo", section.getString("var1"));
        assertEquals("Wrong value of var2", "451", section.getString("var2"));
    }

    /**
     * Tests whether the sub configuration returned by getSection() is connected
     * to the parent.
     */
    @Test
    public void testGetSectionConnected() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section =
                config.getSection("section1");
        section.setProperty("var1", "foo2");
        assertEquals("Not connected to parent", "foo2",
                config.getString("section1.var1"));
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
        final INIConfiguration config = setUpConfig(data);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
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
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
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
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        config.setSynchronizer(new ReadWriteSynchronizer());
        final int threadCount = 10;
        final GlobalSectionTestThread[] threads = new GlobalSectionTestThread[threadCount];
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
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests querying a non existing section.
     */
    @Test
    public void testGetSectionNonExisting() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config
                .getSection("Non existing section");
        assertTrue("Sub config not empty", section.isEmpty());
    }

    /**
     * Tests a property whose value spans multiple lines.
     */
    @Test
    public void testLineContinuation() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA3);
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
        final INIConfiguration config = setUpConfig(INI_DATA3);
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
        final INIConfiguration config = setUpConfig(INI_DATA3);
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
        final INIConfiguration config = setUpConfig(INI_DATA3);
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
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", " one " + LINE_SEPARATOR + "two", config
                .getString("section5.multiQuotedComment"));
    }

    /**
     * Tests a multi-line property value with an empty line.
     */
    @Test
    public void testLineContinuationEmptyLine() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("Wrong value", LINE_SEPARATOR + "line 2", config
                .getString("section5.noFirstLine"));
    }

    /**
     * Tests a line continuation at the end of the file.
     */
    @Test
    public void testLineContinuationAtEnd() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA3);
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
        final StringWriter writer = new StringWriter();
        conf.write(writer);
        conf = new INIConfiguration();
        conf.read(new StringReader(writer.toString()));
        assertEquals("Wrong value (1)", "test1", conf.getString(section + ".test1"));
        assertEquals("Wrong value (2)", "test2", conf.getString(section + ".test2"));
    }

    /**
     * Tests that loading and saving a configuration that contains keys with
     * delimiter characters works correctly. This test is related to
     * CONFIGURATION-622.
     */
    @Test
    public void testPropertyWithDelimiter() throws ConfigurationException
    {
        final String data = INI_DATA + "key.dot = dotValue";
        final INIConfiguration conf = new INIConfiguration();
        load(conf, data);
        assertEquals("Wrong property value", "dotValue",
                conf.getString("section3.key..dot"));
        final String output = saveToString(conf);
        assertThat(output, containsString("key.dot = dotValue"));
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
        final INIConfiguration config = setUpConfig(content);
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
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
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
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
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
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
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
        final INIConfiguration config = setUpConfig(data);
        SubnodeConfiguration sub = config.getSection("section");
        assertFalse("No content", sub.isEmpty());
        sub.clear();
        sub.close();
        sub = config.getSection("section");
        sub.setProperty("test", "success");
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final HierarchicalConfiguration<?> config2 = setUpConfig(writer.toString());
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
        final INIConfiguration config = setUpConfig(data);
        assertEquals("Wrong value 1", "sec1", config.getString("section.var1"));
        assertEquals("Wrong value 2", "sec2", config.getString("section.var2"));
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection("section");
        assertEquals("Wrong sub value 1", "sec1", sub.getString("var1"));
        assertEquals("Wrong sub value 2", "sec2", sub.getString("var2"));
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final String content = writer.toString();
        final int pos = content.indexOf("[section]");
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
        final INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("newSection");
        section.addProperty("test", "success");
        assertEquals("Main config not updated", "success",
                config.getString("newSection.test"));
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final INIConfiguration config2 = setUpConfig(writer.toString());
        section = config2.getSection("newSection");
        assertEquals("Wrong value", "success", section.getString("test"));
    }

    /**
     * Tests whether getSection() can deal with duplicate sections.
     */
    @Test
    public void testGetSectionDuplicate()
    {
        final INIConfiguration config =
                new INIConfiguration();
        config.addProperty("section.var1", "value1");
        config.addProperty("section(-1).var2", "value2");
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section");
        final Iterator<String> keys = section.getKeys();
        assertEquals("Wrong key", "var1", keys.next());
        assertFalse("Too many keys", keys.hasNext());
    }

    /**
     * Tests whether the list delimiter character is recognized.
     */
    @Test
    public void testValueWithDelimiters() throws ConfigurationException
    {
        final INIConfiguration config =
                setUpConfig("[test]" + LINE_SEPARATOR + "list=1,2,3"
                        + LINE_SEPARATOR);
        final List<Object> list = config.getList("test.list");
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
        final INIConfiguration config =
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
        final INIConfiguration config = setUpConfig(INI_DATA);
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        assertFalse("No sections", config.getSections().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether the configuration deals correctly with list delimiters.
     */
    @Test
    public void testListDelimiterHandling() throws ConfigurationException
    {
        final INIConfiguration config = new INIConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.addProperty("list", "a,b,c");
        config.addProperty("listesc", "3\\,1415");
        final String output = saveToString(config);
        final INIConfiguration config2 = setUpConfig(output);
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
        final String data =
                INI_DATA + "[sectest]" + LINE_SEPARATOR
                        + "list = 3\\,1415,pi,\\\\Test\\,5" + LINE_SEPARATOR;
        final INIConfiguration config = setUpConfig(data);
        final INIConfiguration config2 = setUpConfig(saveToString(config));
        final List<Object> list = config2.getList("sectest.list");
        assertEquals("Wrong number of values", 3, list.size());
        assertEquals("Wrong element 1", "3,1415", list.get(0));
        assertEquals("Wrong element 3", "\\Test,5", list.get(2));
    }

    /**
     * Tests whether only properties with values occur in the enumeration of the
     * global section.
     */
    @Test
    public void testKeysOfGlobalSection() throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection(null);
        final Iterator<String> keys = sub.getKeys();
        assertEquals("Wrong key", "globalVar", keys.next());
        if (keys.hasNext())
        {
            final StringBuilder buf = new StringBuilder();
            do
            {
                buf.append(keys.next()).append(' ');
            } while (keys.hasNext());
            fail("Got additional keys: " + buf);
        }
    }

    /**
     * Tests whether the node handler of a global section correctly filters
     * named children.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildrenByName()
            throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        assertTrue(
                "Sections not filtered",
                handler.getChildren(
                        sub.getModel().getNodeHandler().getRootNode(),
                        "section1").isEmpty());
    }

    /**
     * Tests whether the node handler of a global section correctly determines
     * the number of children.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildrenCount()
            throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        assertEquals("Wrong number of children", 1,
                handler.getChildrenCount(handler.getRootNode(), null));
    }

    /**
     * Tests whether the node handler of a global section correctly returns a
     * child by index.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildByIndex()
            throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        final ImmutableNode child = handler.getChild(handler.getRootNode(), 0);
        assertEquals("Wrong child", "globalVar", child.getNodeName());
        try
        {
            handler.getChild(handler.getRootNode(), 1);
            fail("Could obtain child with invalid index!");
        }
        catch (final IndexOutOfBoundsException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether the node handler of a global section correctly determines
     * the index of a child.
     */
    @Test
    public void testGlobalSectionNodeHandlerIndexOfChild()
            throws ConfigurationException
    {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        final List<ImmutableNode> children = handler.getRootNode().getChildren();
        assertEquals("Wrong index", 0,
                handler.indexOfChild(handler.getRootNode(), children.get(0)));
        assertEquals("Wrong index of section child", -1,
                handler.indexOfChild(handler.getRootNode(), children.get(1)));
    }

    /**
     * Tests whether an expression engine can be used which ignores case.
     */
    @Test
    public void testExpressionEngineIgnoringCase()
            throws ConfigurationException
    {
        final DefaultExpressionEngine engine =
                new DefaultExpressionEngine(
                        DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS,
                        NodeNameMatchers.EQUALS_IGNORE_CASE);
        final INIConfiguration config = new INIConfiguration();
        config.setExpressionEngine(engine);
        load(config, INI_DATA);

        checkContent(config);
        assertEquals("Wrong result (1)", "foo",
                config.getString("Section1.var1"));
        assertEquals("Wrong result (2)", "foo",
                config.getString("section1.Var1"));
        assertEquals("Wrong result (1)", "foo",
                config.getString("SECTION1.VAR1"));
    }

    /**
     * Tests whether an empty section can be saved. This is related to
     * CONFIGURATION-671.
     */
    @Test
    public void testWriteEmptySection()
            throws ConfigurationException, IOException
    {
        final String section = "[EmptySection]";
        final INIConfiguration config = setUpConfig(section);
        assertEquals("Wrong number of sections", 1,
                config.getSections().size());
        assertTrue("Section not found",
                config.getSections().contains("EmptySection"));

        final StringWriter writer = new StringWriter();
        config.write(writer);
        assertEquals("Wrong saved configuration",
                section + LINE_SEPARATOR + LINE_SEPARATOR, writer.toString());
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
        public GlobalSectionTestThread(final INIConfiguration conf)
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
                catch (final IllegalStateException istex)
                {
                    error = true;
                }
            }
        }
    }
}
