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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@code INIConfiguration}.
 */
public class TestINIConfiguration {

    /**
     * A thread class for testing concurrent access to the global section.
     */
    private static final class GlobalSectionTestThread extends Thread {

        /** The configuration. */
        private final INIConfiguration config;

        /** A flag whether an error was found. */
        volatile boolean error;

        /**
         * Creates a new instance of {@code GlobalSectionTestThread} and initializes it.
         *
         * @param conf the configuration object
         */
        public GlobalSectionTestThread(final INIConfiguration conf) {
            config = conf;
        }

        /**
         * Accesses the global section in a loop. If there is no correct synchronization, this can cause an exception.
         */
        @Override
        public void run() {
            final int loopCount = 250;

            for (int i = 0; i < loopCount && !error; i++) {
                try {
                    config.getSection(null);
                } catch (final IllegalStateException istex) {
                    error = true;
                }
            }
        }
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();

    /** Constant for the content of an ini file. */
    private static final String INI_DATA = "[section1]" + LINE_SEPARATOR + "var1 = foo" + LINE_SEPARATOR + "var2 = 451" + LINE_SEPARATOR + LINE_SEPARATOR
        + "[section2]" + LINE_SEPARATOR + "var1 = 123.45" + LINE_SEPARATOR + "var2 = bar" + LINE_SEPARATOR + LINE_SEPARATOR + "[section3]" + LINE_SEPARATOR
        + "var1 = true" + LINE_SEPARATOR + "interpolated = ${section3.var1}" + LINE_SEPARATOR + "multi = foo" + LINE_SEPARATOR + "multi = bar" + LINE_SEPARATOR
        + LINE_SEPARATOR;

    private static final String INI_DATA2 = "[section4]" + LINE_SEPARATOR + "var1 = \"quoted value\"" + LINE_SEPARATOR
        + "var2 = \"quoted value\\nwith \\\"quotes\\\"\"" + LINE_SEPARATOR + "var3 = 123 ; comment" + LINE_SEPARATOR + "var4 = \"1;2;3\" ; comment"
        + LINE_SEPARATOR + "var5 = '\\'quoted\\' \"value\"' ; comment" + LINE_SEPARATOR + "var6 = \"\"" + LINE_SEPARATOR;

    private static final String INI_DATA3 = "[section5]" + LINE_SEPARATOR + "multiLine = one \\" + LINE_SEPARATOR + "    two      \\" + LINE_SEPARATOR
        + " three" + LINE_SEPARATOR + "singleLine = C:\\Temp\\" + LINE_SEPARATOR + "multiQuoted = one \\" + LINE_SEPARATOR + "\"  two  \" \\" + LINE_SEPARATOR
        + "  three" + LINE_SEPARATOR + "multiComment = one \\ ; a comment" + LINE_SEPARATOR + "two" + LINE_SEPARATOR
        + "multiQuotedComment = \" one \" \\ ; comment" + LINE_SEPARATOR + "two" + LINE_SEPARATOR + "noFirstLine = \\" + LINE_SEPARATOR + "  line 2"
        + LINE_SEPARATOR + "continueNoLine = one \\" + LINE_SEPARATOR;

    private static final String INI_DATA4 = "[section6]" + LINE_SEPARATOR + "key1{0}value1" + LINE_SEPARATOR + "key2{0}value2" + LINE_SEPARATOR + LINE_SEPARATOR
        + "[section7]" + LINE_SEPARATOR + "key3{0}value3" + LINE_SEPARATOR;

    private static final String INI_DATA5 = "[section4]" + LINE_SEPARATOR + "var1 = \"quoted value\"" + LINE_SEPARATOR
        + "var2 = \"quoted value\\nwith \\\"quotes\\\"\"" + LINE_SEPARATOR + "var3 = 123 # comment" + LINE_SEPARATOR + "var4 = \"1#2;3\" # comment"
        + LINE_SEPARATOR + "var5 = '\\'quoted\\' \"value\"' # comment" + LINE_SEPARATOR + "var6 = \"\"" + LINE_SEPARATOR;

    /** Constant for the content of an ini file - with section inline comment defined with semicolon */
    private static final String INI_DATA6 = "[section1]; main section" + LINE_SEPARATOR + "var1 = foo" + LINE_SEPARATOR + LINE_SEPARATOR
        + "[section11] ; sub-section related to [section1]" + LINE_SEPARATOR + "var1 = 123.45" + LINE_SEPARATOR;

    /** Constant for the content of an ini file - with section inline comment defined with number sign */
    private static final String INI_DATA7 = "[section1]# main section" + LINE_SEPARATOR + "var1 = foo" + LINE_SEPARATOR + LINE_SEPARATOR
        + "[section11] # sub-section related to [section1]" + LINE_SEPARATOR + "var1 = 123.45" + LINE_SEPARATOR;

    private static final String INI_DATA_SEPARATORS = "[section]" + LINE_SEPARATOR + "var1 = value1" + LINE_SEPARATOR + "var2 : value2" + LINE_SEPARATOR
        + "var3=value3" + LINE_SEPARATOR + "var4:value4" + LINE_SEPARATOR + "var5 : value=5" + LINE_SEPARATOR + "var:6=value" + LINE_SEPARATOR
        + "var:7=\"value7\"" + LINE_SEPARATOR + "var:8 =  \"value8\"" + LINE_SEPARATOR;

    /** An ini file that contains only a property in the global section. */
    private static final String INI_DATA_GLOBAL_ONLY = "globalVar = testGlobal" + LINE_SEPARATOR + LINE_SEPARATOR;

    /** An ini file with a global section. */
    private static final String INI_DATA_GLOBAL = INI_DATA_GLOBAL_ONLY + INI_DATA;

    /**
     * Loads the specified content into the given configuration instance.
     *
     * @param instance the configuration
     * @param data the data to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(final INIConfiguration instance, final String data) throws ConfigurationException {
        try (StringReader reader = new StringReader(data)) {
            instance.read(reader);
        } catch (final IOException e) {
            throw new ConfigurationException(e);
        }
    }

    private static Stream<Arguments> provideSectionsWithComments() {
        return Stream.of(
                Arguments.of(INI_DATA6, false, new String[]{null, "section11] ; sub-section related to [section1"}),
                Arguments.of(INI_DATA7, false, new String[]{null, "section11] # sub-section related to [section1"}),
                Arguments.of(INI_DATA6, true, new String[]{"section1", "section11"}),
                Arguments.of(INI_DATA7, true, new String[]{"section1", "section11"})
        );
    }

    private static Stream<Arguments> provideValuesWithComments() {
        return Stream.of(
                Arguments.of(INI_DATA2, "section4.var3", "123"),
                Arguments.of(INI_DATA2, "section4.var4", "1;2;3"),
                Arguments.of(INI_DATA2, "section4.var5", "'quoted' \"value\""),
                Arguments.of(INI_DATA5, "section4.var3", "123"),
                Arguments.of(INI_DATA5, "section4.var4", "1#2;3"),
                Arguments.of(INI_DATA5, "section4.var5", "'quoted' \"value\"")
        );
    }

    /**
     * Saves the specified configuration to a string. The string can be compared with an expected value or again loaded into
     * a configuration.
     *
     * @param config the configuration to be saved
     * @return the content of this configuration saved to a string
     * @throws ConfigurationException if an error occurs
     */
    private static String saveToString(final INIConfiguration config) throws ConfigurationException {
        final StringWriter writer = new StringWriter();
        try {
            config.write(writer);
        } catch (final IOException e) {
            throw new ConfigurationException(e);
        }
        return writer.toString();
    }

    /**
     * Creates a INIConfiguration object that is initialized from the given data.
     *
     * @param data the data of the configuration (an ini file as string)
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private static INIConfiguration setUpConfig(final String data) throws ConfigurationException {
        return setUpConfig(data, false);
    }

    /**
     * Creates a INIConfiguration object that is initialized from the given data.
     *
     * @param data the data of the configuration (an ini file as string)
     * @param inLineCommentsAllowed when true, inline comments on section line are allowed
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private static INIConfiguration setUpConfig(final String data, final boolean inLineCommentsAllowed) throws ConfigurationException {
        // @formatter:off
        final INIConfiguration instance = INIConfiguration.builder()
                .setSectionInLineCommentsAllowed(inLineCommentsAllowed)
                .build();
        // @formatter:on
        instance.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        load(instance, data);
        return instance;
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Tests the values of some properties to ensure that the configuration was correctly loaded.
     *
     * @param instance the configuration to check
     */
    private void checkContent(final INIConfiguration instance) {
        assertEquals("foo", instance.getString("section1.var1"));
        assertEquals(451, instance.getInt("section1.var2"));
        assertEquals(123.45, instance.getDouble("section2.var1"), .001);
        assertEquals("bar", instance.getString("section2.var2"));
        assertTrue(instance.getBoolean("section3.var1"));
        assertEquals(new HashSet<>(Arrays.asList("section1", "section2", "section3")), instance.getSections());
    }

    /**
     * Helper method for testing the load operation. Loads the specified content into a configuration and then checks some
     * properties.
     *
     * @param data the data to load
     */
    private void checkLoad(final String data) throws ConfigurationException {
        final INIConfiguration instance = setUpConfig(data);
        checkContent(instance);
    }

    /**
     * Helper method for testing a save operation. This method constructs a configuration from the specified content string.
     * Then it saves this configuration and checks whether the result matches the original content.
     *
     * @param content the content of the configuration
     * @throws ConfigurationException if an error occurs
     */
    private void checkSave(final String content) throws ConfigurationException {
        final INIConfiguration config = setUpConfig(content);
        final String sOutput = saveToString(config);
        assertEquals(content, sOutput);
    }

    /**
     * Tests whether the specified configuration contains exactly the expected sections.
     *
     * @param config the configuration to check
     * @param expected an array with the expected sections
     */
    private void checkSectionNames(final INIConfiguration config, final String[] expected) {
        final Set<String> sectionNames = config.getSections();
        assertEquals(new HashSet<>(Arrays.asList(expected)), sectionNames);
    }

    /**
     * Tests the names of the sections returned by the configuration.
     *
     * @param data the data of the ini configuration
     * @param expected the expected section names
     * @return the configuration instance
     */
    private INIConfiguration checkSectionNames(final String data, final String[] expected) throws ConfigurationException {
        final INIConfiguration config = setUpConfig(data);
        checkSectionNames(config, expected);
        return config;
    }

    /**
     * Test of read method with changed comment leading separator
     */
    @Test
    public void testCommentLeadingSeparatorUsedInINIInput() throws Exception {
        final String inputCommentLeadingSeparator = ";";
        final String input = "[section]" + LINE_SEPARATOR + "key1=a;b;c" + LINE_SEPARATOR + "key2=a#b#c" + LINE_SEPARATOR + ";key3=value3" + LINE_SEPARATOR
            + "#key4=value4" + LINE_SEPARATOR;

        final INIConfiguration instance = new FileBasedConfigurationBuilder<>(INIConfiguration.class)
            .configure(new Parameters().ini().setCommentLeadingCharsUsedInInput(inputCommentLeadingSeparator)).getConfiguration();
        load(instance, input);

        assertEquals("a;b;c", instance.getString("section.key1"));
        assertEquals("a#b#c", instance.getString("section.key2"));
        assertNull(instance.getString("section.;key3"));
        assertEquals("value4", instance.getString("section.#key4"));
    }

    /**
     * Tests correct handling of empty sections "[ ]".
     */
    @Test
    public void testEmptySection() throws ConfigurationException {
        final INIConfiguration config = setUpConfig("[]" + LINE_SEPARATOR + "key=value" + LINE_SEPARATOR);
        final String value = config.getString(" .key");
        assertEquals("value", value);
    }

    /**
     * Tests whether an expression engine can be used which ignores case.
     */
    @Test
    public void testExpressionEngineIgnoringCase() throws ConfigurationException {
        final DefaultExpressionEngine engine = new DefaultExpressionEngine(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS, NodeNameMatchers.EQUALS_IGNORE_CASE);
        final INIConfiguration config = new INIConfiguration();
        config.setExpressionEngine(engine);
        load(config, INI_DATA);

        checkContent(config);
        assertEquals("foo", config.getString("Section1.var1"));
        assertEquals("foo", config.getString("section1.Var1"));
        assertEquals("foo", config.getString("SECTION1.VAR1"));
    }

    /**
     * Tests a property that has no key.
     */
    @Test
    public void testGetPropertyNoKey() throws ConfigurationException {
        final String data = INI_DATA2 + LINE_SEPARATOR + "= noKey" + LINE_SEPARATOR;
        final INIConfiguration config = setUpConfig(data);
        assertEquals("noKey", config.getString("section4. "));
    }

    /**
     * Tests a property that has no value.
     */
    @Test
    public void testGetPropertyNoValue() throws ConfigurationException {
        final String data = INI_DATA2 + LINE_SEPARATOR + "noValue =" + LINE_SEPARATOR;
        final INIConfiguration config = setUpConfig(data);
        assertEquals("", config.getString("section4.noValue"));
    }

    /**
     * Tests whether the sub configuration returned by getSection() is connected to the parent.
     */
    @Test
    public void testGetSectionConnected() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
        section.setProperty("var1", "foo2");
        assertEquals("foo2", config.getString("section1.var1"));
    }

    /**
     * Tests whether getSection() can deal with duplicate sections.
     */
    @Test
    public void testGetSectionDuplicate() {
        final INIConfiguration config = new INIConfiguration();
        config.addProperty("section.var1", "value1");
        config.addProperty("section(-1).var2", "value2");
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section");
        final Iterator<String> keys = section.getKeys();
        assertEquals("var1", keys.next());
        assertFalse(keys.hasNext());
    }

    /**
     * Tests querying the properties of an existing section.
     */
    @Test
    public void testGetSectionExisting() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
        assertEquals("foo", section.getString("var1"));
        assertEquals("451", section.getString("var2"));
    }

    /**
     * Tests concurrent access to the global section.
     */
    @Test
    public void testGetSectionGloabalMultiThreaded() throws ConfigurationException, InterruptedException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        config.setSynchronizer(new ReadWriteSynchronizer());
        final int threadCount = 10;
        final GlobalSectionTestThread[] threads = new GlobalSectionTestThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new GlobalSectionTestThread(config);
            threads[i].start();
        }
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
            assertFalse(threads[i].error);
        }
    }

    /**
     * Tests querying the content of the global section.
     */
    @Test
    public void testGetSectionGlobal() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
        assertEquals("testGlobal", section.getString("globalVar"));
    }

    /**
     * Tests querying the content of the global section if there is none.
     */
    @Test
    public void testGetSectionGlobalNonExisting() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection(null);
        assertTrue(section.isEmpty());
    }

    /**
     * Tests querying the properties of a section that was merged from two sections with the same name.
     */
    @Test
    public void testGetSectionMerged() throws ConfigurationException {
        final String data = INI_DATA + "[section1]" + LINE_SEPARATOR + "var3 = merged" + LINE_SEPARATOR;
        final INIConfiguration config = setUpConfig(data);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("section1");
        assertEquals("foo", section.getString("var1"));
        assertEquals("451", section.getString("var2"));
        assertEquals("merged", section.getString("var3"));
    }

    /**
     * Tests querying a non existing section.
     */
    @Test
    public void testGetSectionNonExisting() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final HierarchicalConfiguration<ImmutableNode> section = config.getSection("Non existing section");
        assertTrue(section.isEmpty());
    }

    /**
     * Tests whether a section that was created by getSection() can be manipulated.
     */
    @Test
    public void testGetSectionNonExistingManipulate() throws ConfigurationException, IOException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        HierarchicalConfiguration<ImmutableNode> section = config.getSection("newSection");
        section.addProperty("test", "success");
        assertEquals("success", config.getString("newSection.test"));
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final INIConfiguration config2 = setUpConfig(writer.toString());
        section = config2.getSection("newSection");
        assertEquals("success", section.getString("test"));
    }

    /**
     * Test of getSections method, of class {@link INIConfiguration} .
     */
    @Test
    public void testGetSections() {
        final INIConfiguration instance = new INIConfiguration();
        instance.addProperty("test1.foo", "bar");
        instance.addProperty("test2.foo", "abc");
        final Set<String> expResult = new HashSet<>();
        expResult.add("test1");
        expResult.add("test2");
        final Set<String> result = instance.getSections();
        assertEquals(expResult, result);
    }

    /**
     * Tests whether a section added later is also found by getSections().
     */
    @Test
    public void testGetSectionsAdded() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        config.addProperty("section5.test", Boolean.TRUE);
        checkSectionNames(config, new String[] {"section4", "section5"});
    }

    /**
     * Tests whether variables containing a dot are not misinterpreted as sections. This test is related to
     * CONFIGURATION-327.
     */
    @Test
    public void testGetSectionsDottedVar() throws ConfigurationException {
        final String data = "dotted.var = 1" + LINE_SEPARATOR + INI_DATA_GLOBAL;
        final INIConfiguration config = checkSectionNames(data, new String[] {null, "section1", "section2", "section3"});
        assertEquals(1, config.getInt("dotted..var"));
    }

    /**
     * Tests whether the sections of a configuration can be queried that contains only a global section.
     */
    @Test
    public void testGetSectionsGlobalOnly() throws ConfigurationException {
        checkSectionNames(INI_DATA_GLOBAL_ONLY, new String[] {null});
    }

    /**
     * Tests querying the sections if there is no global section.
     */
    @Test
    public void testGetSectionsNoGlobal() throws ConfigurationException {
        checkSectionNames(INI_DATA, new String[] {"section1", "section2", "section3"});
    }

    /**
     * Tests whether synchronization is performed when querying the configuration's sections.
     */
    @Test
    public void testGetSectionsSynchronized() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA);
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        assertFalse(config.getSections().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests querying the sections if a global section if available.
     */
    @Test
    public void testGetSectionsWithGlobal() throws ConfigurationException {
        checkSectionNames(INI_DATA_GLOBAL, new String[] {null, "section1", "section2", "section3"});
    }

    /**
     * Tests whether a section with inline comment is correctly parsed.
     */
    @ParameterizedTest
    @MethodSource("provideSectionsWithComments")
    public void testGetSectionsWithInLineComment(final String source, final boolean allowComments, final String[] results) throws ConfigurationException {
        final INIConfiguration config = setUpConfig(source, allowComments);
        checkSectionNames(config, results);
    }

    /**
     * Tests reading a property from the global section.
     */
    @Test
    public void testGlobalProperty() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        assertEquals("testGlobal", config.getString("globalVar"));
    }

    /**
     * Tests whether the sub configuration for the global section is connected to its parent.
     */
    @Test
    public void testGlobalSectionConnected() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection(null);
        config.setProperty("globalVar", "changed");
        assertEquals("changed", sub.getString("globalVar"));
    }

    /**
     * Tests whether the node handler of a global section correctly returns a child by index.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildByIndex() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        final ImmutableNode rootNode = handler.getRootNode();
        final ImmutableNode child = handler.getChild(rootNode, 0);
        assertEquals("globalVar", child.getNodeName());
        assertThrows(IndexOutOfBoundsException.class, () -> handler.getChild(rootNode, 1));
    }

    /**
     * Tests whether the node handler of a global section correctly filters named children.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildrenByName() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        assertTrue(handler.getChildren(sub.getModel().getNodeHandler().getRootNode(), "section1").isEmpty());
    }

    /**
     * Tests whether the node handler of a global section correctly determines the number of children.
     */
    @Test
    public void testGlobalSectionNodeHandlerGetChildrenCount() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        assertEquals(1, handler.getChildrenCount(handler.getRootNode(), null));
    }

    /**
     * Tests whether the node handler of a global section correctly determines the index of a child.
     */
    @Test
    public void testGlobalSectionNodeHandlerIndexOfChild() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final SubnodeConfiguration sub = config.getSection(null);
        final NodeHandler<ImmutableNode> handler = sub.getModel().getNodeHandler();
        final List<ImmutableNode> children = handler.getRootNode().getChildren();
        assertEquals(0, handler.indexOfChild(handler.getRootNode(), children.get(0)));
        assertEquals(-1, handler.indexOfChild(handler.getRootNode(), children.get(1)));
    }

    /**
     * Test of isCommentLine method, of class {@link INIConfiguration}.
     */
    @Test
    public void testIsCommentLine() {
        final INIConfiguration instance = new INIConfiguration();
        assertTrue(instance.isCommentLine("#comment1"));
        assertTrue(instance.isCommentLine(";comment1"));
        assertFalse(instance.isCommentLine("nocomment=true"));
        assertFalse(instance.isCommentLine(null));
    }

    /**
     * Test of isSectionLine method, of class {@link INIConfiguration}.
     */
    @Test
    public void testIsSectionLine() {
        final INIConfiguration instance = new INIConfiguration();
        assertTrue(instance.isSectionLine("[section]"));
        assertFalse(instance.isSectionLine("nosection=true"));
        assertFalse(instance.isSectionLine(null));
    }

    /**
     * Tests whether only properties with values occur in the enumeration of the global section.
     */
    @Test
    public void testKeysOfGlobalSection() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_GLOBAL);
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection(null);
        final Iterator<String> keys = sub.getKeys();
        assertEquals("globalVar", keys.next());
        if (keys.hasNext()) {
            final StringBuilder buf = new StringBuilder();
            do {
                buf.append(keys.next()).append(' ');
            } while (keys.hasNext());
            fail("Got additional keys: " + buf);
        }
    }

    /**
     * Tests a property whose value spans multiple lines.
     */
    @Test
    public void testLineContinuation() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("one" + LINE_SEPARATOR + "two" + LINE_SEPARATOR + "three", config.getString("section5.multiLine"));
    }

    /**
     * Tests a line continuation at the end of the file.
     */
    @Test
    public void testLineContinuationAtEnd() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("one" + LINE_SEPARATOR, config.getString("section5.continueNoLine"));
    }

    /**
     * Tests a property whose value spans multiple lines with a comment.
     */
    @Test
    public void testLineContinuationComment() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("one" + LINE_SEPARATOR + "two", config.getString("section5.multiComment"));
    }

    /**
     * Tests a multi-line property value with an empty line.
     */
    @Test
    public void testLineContinuationEmptyLine() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals(LINE_SEPARATOR + "line 2", config.getString("section5.noFirstLine"));
    }

    /**
     * Tests a property value that ends on a backslash, which is no line continuation character.
     */
    @Test
    public void testLineContinuationNone() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("C:\\Temp\\", config.getString("section5.singleLine"));
    }

    /**
     * Tests a property whose value spans multiple lines when quoting is involved. In this case whitespace must not be
     * trimmed.
     */
    @Test
    public void testLineContinuationQuoted() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals("one" + LINE_SEPARATOR + "  two  " + LINE_SEPARATOR + "three", config.getString("section5.multiQuoted"));
    }

    /**
     * Tests a property with a quoted value spanning multiple lines and a comment.
     */
    @Test
    public void testLineContinuationQuotedComment() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA3);
        assertEquals(" one " + LINE_SEPARATOR + "two", config.getString("section5.multiQuotedComment"));
    }

    /**
     * Tests whether the configuration deals correctly with list delimiters.
     */
    @Test
    public void testListDelimiterHandling() throws ConfigurationException {
        final INIConfiguration config = new INIConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.addProperty("list", "a,b,c");
        config.addProperty("listesc", "3\\,1415");
        final String output = saveToString(config);
        final INIConfiguration config2 = setUpConfig(output);
        assertEquals(Arrays.asList("a", "b", "c"), config2.getList("list"));
        assertEquals("3,1415", config2.getString("listesc"));
    }

    /**
     * Tests whether property values are correctly escaped even if they are part of a property with multiple values.
     */
    @Test
    public void testListDelimiterHandlingInList() throws ConfigurationException {
        final String data = INI_DATA + "[sectest]" + LINE_SEPARATOR + "list = 3\\,1415,pi,\\\\Test\\,5" + LINE_SEPARATOR;
        final INIConfiguration config = setUpConfig(data);
        final INIConfiguration config2 = setUpConfig(saveToString(config));
        final List<Object> list = config2.getList("sectest.list");
        assertEquals(Arrays.asList("3,1415", "pi", "\\Test,5"), list);
    }

    /**
     * Tests whether parsing of lists can be disabled.
     */
    @Test
    public void testListParsingDisabled() throws ConfigurationException {
        final INIConfiguration config = new INIConfiguration();
        load(config, "[test]" + LINE_SEPARATOR + "nolist=1,2,3");
        assertEquals("1,2,3", config.getString("test.nolist"));
    }

    /**
     * Test of load method, of class {@link INIConfiguration}.
     */
    @Test
    public void testLoad() throws Exception {
        checkLoad(INI_DATA);
    }

    /**
     * Tests the load() method when the alternative value separator is used (a ':' for '=').
     */
    @Test
    public void testLoadAlternativeSeparator() throws Exception {
        checkLoad(INI_DATA.replace('=', ':'));
    }

    /**
     * Tests whether an instance can be created using a file-based builder.
     */
    @Test
    public void testLoadFromBuilder() throws ConfigurationException, IOException {
        final File file = writeTestFile(INI_DATA);
        final FileBasedConfigurationBuilder<INIConfiguration> builder = new FileBasedConfigurationBuilder<>(INIConfiguration.class);
        builder.configure(new FileBasedBuilderParametersImpl().setFile(file));
        final INIConfiguration config = builder.getConfiguration();
        checkContent(config);
    }

    /**
     * Tests whether a duplicate session is merged.
     */
    @Test
    public void testMergeDuplicateSection() throws ConfigurationException, IOException {
        final String data = "[section]\nvar1 = sec1\n\n" + "[section]\nvar2 = sec2\n";
        final INIConfiguration config = setUpConfig(data);
        assertEquals("sec1", config.getString("section.var1"));
        assertEquals("sec2", config.getString("section.var2"));
        final HierarchicalConfiguration<ImmutableNode> sub = config.getSection("section");
        assertEquals("sec1", sub.getString("var1"));
        assertEquals("sec2", sub.getString("var2"));
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final String content = writer.toString();
        final int pos = content.indexOf("[section]");
        assertTrue(pos >= 0);
        assertTrue(content.indexOf("[section]", pos + 1) < 0);
    }

    /**
     * Tests property definitions containing multiple separators.
     */
    @Test
    public void testMultipleSeparators() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        assertEquals("value=5", config.getString("section.var5"));
        assertEquals("6=value", config.getString("section.var"));
    }

    /**
     * Tests property definitions containing multiple separators that are quoted.
     */
    @Test
    public void testMultipleSeparatorsQuoted() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        assertEquals("value7", config.getString("section.var:7"));
        assertEquals("value8", config.getString("section.var:8"));
    }

    /**
     * Tests that loading and saving a configuration that contains keys with delimiter characters works correctly. This test
     * is related to CONFIGURATION-622.
     */
    @Test
    public void testPropertyWithDelimiter() throws ConfigurationException {
        final String data = INI_DATA + "key.dot = dotValue";
        final INIConfiguration conf = new INIConfiguration();
        load(conf, data);
        assertEquals("dotValue", conf.getString("section3.key..dot"));
        final String output = saveToString(conf);
        assertThat(output, containsString("key.dot = dotValue"));
    }

    @Test
    public void testQuotedValue() throws Exception {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("quoted value", config.getString("section4.var1"));
    }

    /**
     * Tests an empty quoted value.
     */
    @Test
    public void testQuotedValueEmpty() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("", config.getString("section4.var6"));
    }

    @Test
    public void testQuotedValueWithComment() throws Exception {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("1;2;3", config.getString("section4.var4"));
    }

    @Test
    public void testQuotedValueWithQuotes() throws Exception {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("quoted value\\nwith \"quotes\"", config.getString("section4.var2"));
    }

    @Test
    public void testQuotedValueWithSingleQuotes() throws Exception {
        final INIConfiguration config = setUpConfig(INI_DATA2);
        assertEquals("'quoted' \"value\"", config.getString("section4.var5"));
    }

    /**
     * Tests whether whitespace is left unchanged for quoted values.
     */
    @Test
    public void testQuotedValueWithWhitespace() throws Exception {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \"";
        final INIConfiguration config = setUpConfig(content);
        assertEquals(" [test@cmd ~]$ ", config.getString("CmdPrompt"));
    }

    /**
     * Tests a quoted value with space and a comment.
     */
    @Test
    public void testQuotedValueWithWhitespaceAndComment() throws Exception {
        final String content = "CmdPrompt = \" [test@cmd ~]$ \" ; a comment";
        final INIConfiguration config = setUpConfig(content);
        assertEquals(" [test@cmd ~]$ ", config.getString("CmdPrompt"));
    }

    /**
     * Test of save method, of class {@link INIConfiguration}.
     */
    @Test
    public void testSave() throws Exception {
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

        assertEquals(INI_DATA, writer.toString());
    }

    /**
     * Tests whether a section that has been cleared can be manipulated and saved later.
     */
    @Test
    public void testSaveClearedSection() throws ConfigurationException, IOException {
        final String data = "[section]\ntest = failed\n";
        final INIConfiguration config = setUpConfig(data);
        SubnodeConfiguration sub = config.getSection("section");
        assertFalse(sub.isEmpty());
        sub.clear();
        sub.close();
        sub = config.getSection("section");
        sub.setProperty("test", "success");
        final StringWriter writer = new StringWriter();
        config.write(writer);
        final HierarchicalConfiguration<?> config2 = setUpConfig(writer.toString());
        assertEquals("success", config2.getString("section.test"));
    }

    /**
     * Tests whether a configuration can be saved that contains section keys with delimiter characters. This test is related
     * to CONFIGURATION-409.
     */
    @Test
    public void testSaveKeysWithDelimiters() throws ConfigurationException, IOException {
        INIConfiguration conf = new INIConfiguration();
        final String section = "Section..with..dots";
        conf.addProperty(section + ".test1", "test1");
        conf.addProperty(section + ".test2", "test2");
        final StringWriter writer = new StringWriter();
        conf.write(writer);
        conf = new INIConfiguration();
        conf.read(new StringReader(writer.toString()));
        assertEquals("test1", conf.getString(section + ".test1"));
        assertEquals("test2", conf.getString(section + ".test2"));
    }

    /**
     * Tests whether list delimiter parsing can be disabled.
     */
    @Test
    public void testSaveWithDelimiterParsingDisabled() throws ConfigurationException {
        final INIConfiguration config = new INIConfiguration();
        final String data = INI_DATA.substring(0, INI_DATA.length() - LINE_SEPARATOR.length()) + "nolist = 1,2, 3";
        load(config, data);
        assertEquals("1,2, 3", config.getString("section3.nolist"));
        final String content = saveToString(config);
        final INIConfiguration config2 = new INIConfiguration();
        load(config2, content);
        assertEquals("1,2, 3", config2.getString("section3.nolist"));
    }

    /**
     * Tests saving a configuration that contains a global section.
     */
    @Test
    public void testSaveWithGlobalSection() throws ConfigurationException {
        checkSave(INI_DATA_GLOBAL);
    }

    /**
     * Tests whether a configuration that contains only a global section can be saved correctly.
     */
    @Test
    public void testSaveWithOnlyGlobalSection() throws ConfigurationException {
        checkSave(INI_DATA_GLOBAL_ONLY);
    }

    /**
     * Tests whether the different separators with or without whitespace are recognized.
     */
    @Test
    public void testSeparators() throws ConfigurationException {
        final INIConfiguration config = setUpConfig(INI_DATA_SEPARATORS);
        for (int i = 1; i <= 4; i++) {
            assertEquals("value" + i, config.getString("section.var" + i));
        }
    }

    /**
     * Test of read method with changed separator.
     */
    @Test
    public void testSeparatorUsedInINIInput() throws Exception {
        final String inputSeparator = "=";
        final String input = "[section]" + LINE_SEPARATOR + "k1:v1$key1=value1" + LINE_SEPARATOR + "k1:v1,k2:v2$key2=value2" + LINE_SEPARATOR + "key3:value3"
            + LINE_SEPARATOR + "key4 = value4" + LINE_SEPARATOR;

        final INIConfiguration instance = new FileBasedConfigurationBuilder<>(INIConfiguration.class)
            .configure(new Parameters().ini().setSeparatorUsedInInput(inputSeparator)).getConfiguration();
        load(instance, input);

        assertEquals("value1", instance.getString("section.k1:v1$key1"));
        assertEquals("value2", instance.getString("section.k1:v1,k2:v2$key2"));
        assertEquals("", instance.getString("section.key3:value3"));
        assertEquals("value4", instance.getString("section.key4").trim());
    }

    /**
     * Test of save method with changed separator
     */
    @Test
    public void testSeparatorUsedInINIOutput() throws Exception {
        final String outputSeparator = ": ";
        final String input = MessageFormat.format(INI_DATA4, "=").trim();
        final String expectedOutput = MessageFormat.format(INI_DATA4, outputSeparator).trim();

        final INIConfiguration instance = new FileBasedConfigurationBuilder<>(INIConfiguration.class)
            .configure(new Parameters().ini().setSeparatorUsedInOutput(outputSeparator)).getConfiguration();
        load(instance, input);

        final Writer writer = new StringWriter();
        instance.write(writer);
        final String result = writer.toString().trim();

        assertEquals(expectedOutput, result);
    }

    /**
     * Test correct handling of in line comments on value line
     */
    @ParameterizedTest
    @MethodSource("provideValuesWithComments")
    public void testValueWithComment(final String source, final String key, final String value) throws Exception {
        final INIConfiguration config = setUpConfig(source);
        assertEquals(value, config.getString(key));
    }

    /**
     * Tests whether the list delimiter character is recognized.
     */
    @Test
    public void testValueWithDelimiters() throws ConfigurationException {
        final INIConfiguration config = setUpConfig("[test]" + LINE_SEPARATOR + "list=1,2,3" + LINE_SEPARATOR);
        final List<Object> list = config.getList("test.list");
        assertEquals(Arrays.asList("1", "2", "3"), list);
    }

    /**
     * Tests whether a value which contains a semicolon can be loaded successfully. This test is related to
     * CONFIGURATION-434.
     */
    @Test
    public void testValueWithSemicolon() throws ConfigurationException {
        final String path = "C:\\Program Files\\jar\\manage.jar;" + "C:\\Program Files\\jar\\guiLauncher.jar";
        final String content = "[Environment]" + LINE_SEPARATOR + "Application Type=any" + LINE_SEPARATOR + "Class Path=" + path + "  ;comment" + LINE_SEPARATOR
            + "Path=" + path + "\t; another comment";
        final INIConfiguration config = setUpConfig(content);
        assertEquals(path, config.getString("Environment.Class Path"));
        assertEquals(path, config.getString("Environment.Path"));
    }

    /**
     * Tests whether an empty section can be saved. This is related to CONFIGURATION-671.
     */
    @Test
    public void testWriteEmptySection() throws ConfigurationException, IOException {
        final String section = "[EmptySection]";
        final INIConfiguration config = setUpConfig(section);
        assertEquals(Collections.singleton("EmptySection"), config.getSections());

        final StringWriter writer = new StringWriter();
        config.write(writer);
        assertEquals(section + LINE_SEPARATOR + LINE_SEPARATOR, writer.toString());
    }

    @Test
    public void testWriteValueWithCommentChar() throws Exception {
        final INIConfiguration config = new INIConfiguration();
        config.setProperty("section.key1", "1;2;3");

        final StringWriter writer = new StringWriter();
        config.write(writer);

        final INIConfiguration config2 = new INIConfiguration();
        config2.read(new StringReader(writer.toString()));

        assertEquals("1;2;3", config2.getString("section.key1"));
    }

    /**
     * Writes a test ini file.
     *
     * @param content the content of the file
     * @return the newly created file
     * @throws IOException if an error occurs
     */
    private File writeTestFile(final String content) throws IOException {
        final File file = newFile(tempFolder);
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println(content);
        }
        return file;
    }
}
