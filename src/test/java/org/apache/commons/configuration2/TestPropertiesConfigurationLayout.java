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
package org.apache.commons.configuration2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for PropertiesConfigurationLayout.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class TestPropertiesConfigurationLayout
{
    /** Constant for the line break character. */
    private static final String CR = System.getProperty("line.separator");

    /** Constant for the normalized line break character. */
    private static final String CRNORM = "\n";

    /** Constant for a test property key. */
    private static final String TEST_KEY = "myProperty";

    /** Constant for a test comment. */
    private static final String TEST_COMMENT = "A comment for my test property";

    /** Constant for a test property value. */
    private static final String TEST_VALUE = "myPropertyValue";

    /** The layout object under test. */
    private PropertiesConfigurationLayout layout;

    /** The associated configuration object. */
    private LayoutTestConfiguration config;

    /** A properties builder that can be used for testing. */
    private PropertiesBuilder builder;

    @Before
    public void setUp() throws Exception
    {
        config = new LayoutTestConfiguration();
        config.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        layout = new PropertiesConfigurationLayout();
        config.setLayout(layout);
        builder = new PropertiesBuilder();
    }

    /**
     * Tests a newly created instance.
     */
    @Test
    public void testInit()
    {
        assertTrue("Object contains keys", layout.getKeys().isEmpty());
        assertNull("Header comment not null", layout.getHeaderComment());
        final Iterator<EventListener<? super ConfigurationEvent>> it =
                config.getEventListeners(ConfigurationEvent.ANY).iterator();
        assertTrue("No event listener registered", it.hasNext());
        assertSame("Layout not registered as event listener", layout, it.next());
        assertFalse("Multiple event listeners registered", it.hasNext());
        assertFalse("Force single line flag set", layout.isForceSingleLine());
        assertNull("Got a global separator", layout.getGlobalSeparator());
    }

    /**
     * Tests the copy constructor if no other layout object is passed.
     */
    @Test
    public void testInitNull()
    {
        layout = new PropertiesConfigurationLayout(null);
        assertTrue("Object contains keys", layout.getKeys().isEmpty());
    }

    /**
     * Tests reading a simple properties file.
     */
    @Test
    public void testReadSimple() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertNull("A header comment was found", layout.getHeaderComment());
        assertEquals("Wrong number of properties", 1, layout.getKeys().size());
        assertTrue("Property not found", layout.getKeys().contains(TEST_KEY));
        assertEquals("Comment not found", TEST_COMMENT, layout
                .getCanonicalComment(TEST_KEY, false));
        assertEquals("Wrong number of blanc lines", 0, layout
                .getBlancLinesBefore(TEST_KEY));
        assertTrue("Wrong single line flag", layout.isSingleLine(TEST_KEY));
        assertEquals("Property not stored in config", TEST_VALUE, config
                .getString(TEST_KEY));
    }

    /**
     * Tests whether blanc lines before a property are correctly detected.
     */
    @Test
    public void testBlancLines() throws ConfigurationException
    {
        builder.addProperty("prop", "value");
        builder.addComment(null);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertEquals("Wrong number of blanc lines", 2, layout
                .getBlancLinesBefore(TEST_KEY));
        assertEquals("Wrong comment", TEST_COMMENT + CRNORM, layout
                .getCanonicalComment(TEST_KEY, false));
        assertEquals("Wrong property value", TEST_VALUE, config
                .getString(TEST_KEY));
    }

    /**
     * Tests the single line flag for a simple property definition.
     */
    @Test
    public void testIsSingleLine() throws ConfigurationException
    {
        builder.addProperty(TEST_KEY, TEST_VALUE + "," + TEST_VALUE + "2");
        layout.load(config, builder.getReader());
        assertTrue("Wrong single line flag", layout.isSingleLine(TEST_KEY));
        assertEquals("Wrong number of values", 2, config.getList(TEST_KEY)
                .size());
    }

    /**
     * Tests the single line flag if there are multiple property definitions.
     */
    @Test
    public void testIsSingleLineMulti() throws ConfigurationException
    {
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addProperty("anotherProp", "a value");
        builder.addProperty(TEST_KEY, TEST_VALUE + "2");
        layout.load(config, builder.getReader());
        assertFalse("Wrong single line flag", layout.isSingleLine(TEST_KEY));
        assertEquals("Wrong number of values", 2, config.getList(TEST_KEY)
                .size());
    }

    /**
     * Tests whether comments are combined for multiple occurrences.
     */
    @Test
    public void testCombineComments() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE + "2");
        layout.load(config, builder.getReader());
        assertEquals("Wrong combined comment",
                TEST_COMMENT + CRNORM + TEST_COMMENT, layout.getCanonicalComment(
                        TEST_KEY, false));
        assertEquals("Wrong combined blanc numbers", 0, layout
                .getBlancLinesBefore(TEST_KEY));
    }

    /**
     * Tests if a header comment is detected.
     */
    @Test
    public void testHeaderComment() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT, layout
                .getCanonicalHeaderComment(false));
        assertNull("Wrong comment for property", layout.getCanonicalComment(
                TEST_KEY, false));
    }

    /**
     * Tests if a header comment containing blanc lines is correctly detected.
     */
    @Test
    public void testHeaderCommentWithBlancs() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT + CRNORM + CRNORM
                + TEST_COMMENT, layout.getCanonicalHeaderComment(false));
        assertNull("Wrong comment for property", layout.getComment(TEST_KEY));
    }

    /**
     * Tests if a header comment containing blanc lines is correctly detected and doesn't overflow into the property
     * comment in the case that the header comment is already set
     */
    @Test
    public void testHeaderCommentWithBlancsAndPresetHeaderComment() throws ConfigurationException
    {
        final String presetHeaderComment = "preset" + TEST_COMMENT + CRNORM + CRNORM + TEST_COMMENT;
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.setHeaderComment(presetHeaderComment);
        layout.load(config, builder.getReader());
        assertEquals("Wrong header comment", presetHeaderComment,
                     layout.getCanonicalHeaderComment(false));
        assertNull("Wrong comment for property", layout.getComment(TEST_KEY));
    }

    /**
     * Tests if a header comment is correctly detected when it contains blanc
     * lines and the first property has a comment, too.
     */
    @Test
    public void testHeaderCommentWithBlancsAndPropComment()
            throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT + CRNORM + CRNORM
                + TEST_COMMENT, layout.getCanonicalHeaderComment(false));
        assertEquals("Wrong comment for property", TEST_COMMENT, layout
                .getCanonicalComment(TEST_KEY, false));
    }

    /**
     * Tests fetching a canonical header comment when no comment is set.
     */
    @Test
    public void testHeaderCommentNull()
    {
        assertNull("No null comment with comment chars", layout
                .getCanonicalHeaderComment(true));
        assertNull("No null comment without comment chars", layout
                .getCanonicalHeaderComment(false));
    }

    /**
     * Tests if a property add event is correctly processed.
     */
    @Test
    public void testEventAdd()
    {
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.onEvent(event);
        assertTrue("Property not stored", layout.getKeys().contains(TEST_KEY));
        assertEquals("Blanc lines before new property", 0, layout
                .getBlancLinesBefore(TEST_KEY));
        assertTrue("No single line property", layout.isSingleLine(TEST_KEY));
        assertEquals("Wrong separator", " = ", layout.getSeparator(TEST_KEY));
    }

    /**
     * Tests adding a property multiple time through an event. The property
     * should then be a multi-line property.
     */
    @Test
    public void testEventAddMultiple()
    {
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.onEvent(event);
        layout.onEvent(event);
        assertFalse("No multi-line property", layout.isSingleLine(TEST_KEY));
    }

    /**
     * Tests if an add event is correctly processed if the affected property is
     * already stored in the layout object.
     */
    @Test
    public void testEventAddExisting() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.onEvent(event);
        assertFalse("No multi-line property", layout.isSingleLine(TEST_KEY));
        assertEquals("Comment was modified", TEST_COMMENT, layout
                .getCanonicalComment(TEST_KEY, false));
    }

    /**
     * Tests if a set property event for a non existing property is correctly
     * handled.
     */
    @Test
    public void testEventSetNonExisting()
    {
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.SET_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.onEvent(event);
        assertTrue("New property was not found", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests if a property delete event is correctly processed.
     */
    @Test
    public void testEventDelete()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.onEvent(event);
        event = new ConfigurationEvent(this,
                ConfigurationEvent.CLEAR_PROPERTY, TEST_KEY, null,
                false);
        layout.onEvent(event);
        assertFalse("Property still existing", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests if a clear event is correctly processed.
     */
    @Test
    public void testEventClearConfig() throws Exception
    {
        fillLayout();
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.CLEAR, null, null, false);
        layout.onEvent(event);
        assertTrue("Keys not empty", layout.getKeys().isEmpty());
        assertNull("Header comment was not reset", layout.getHeaderComment());
    }

    /**
     * Tests if a before update event is correctly ignored.
     */
    @Test
    public void testEventAddBefore()
    {
        final ConfigurationEvent event = new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                true);
        layout.onEvent(event);
        assertFalse("Property already stored", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests a recursive load call.
     */
    @Test
    public void testRecursiveLoadCall() throws ConfigurationException
    {
        final PropertiesBuilder b = new PropertiesBuilder();
        b.addComment("A nested header comment.");
        b.addComment("With multiple lines");
        b.addComment(null);
        b.addComment("Second comment");
        b.addProperty(TEST_KEY, TEST_VALUE);
        b.addProperty(TEST_KEY + "2", TEST_VALUE + "2");
        config.builder = b;

        builder.addComment("Header comment");
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addComment("Include file");
        builder.addProperty(PropertiesConfiguration.getInclude(), "test");

        layout.load(config, builder.getReader());

        assertEquals("Wrong header comment", "Header comment", layout
                .getCanonicalHeaderComment(false));
        assertFalse("Include property was stored", layout.getKeys().contains(
                PropertiesConfiguration.getInclude()));
        assertEquals("Wrong comment for property", TEST_COMMENT + CRNORM
                + "A nested header comment." + CRNORM + "With multiple lines" + CRNORM
                + CRNORM + "Second comment", layout.getCanonicalComment(TEST_KEY,
                false));
    }

    /**
     * Tests whether the output of the layout object is identical to the source
     * file (at least for simple properties files).
     */
    @Test
    public void testReadAndWrite() throws ConfigurationException
    {
        builder.addComment("This is my test properties file,");
        builder.addComment("which contains a header comment.");
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(null);
        builder.addComment("Another comment");
        builder.addProperty("property", "and a value");
        layout.load(config, builder.getReader());
        checkLayoutString(builder.toString());
    }

    /**
     * Tests if the content of the layout object is correctly written.
     */
    @Test
    public void testSave() throws ConfigurationException
    {
        config.addProperty(TEST_KEY, TEST_VALUE);
        layout.setComment(TEST_KEY, TEST_COMMENT);
        config.addProperty(TEST_KEY, TEST_VALUE + "2");
        config.addProperty("AnotherProperty", "AnotherValue");
        config.addProperty("AnotherProperty", "3rdValue");
        layout.setComment("AnotherProperty", "AnotherComment");
        layout.setBlancLinesBefore("AnotherProperty", 2);
        layout.setSingleLine("AnotherProperty", true);
        layout.setHeaderComment("A header comment" + CRNORM + "for my properties");
        checkLayoutString("# A header comment" + CR + "# for my properties"
                + CR + CR + "# " + TEST_COMMENT + CR + TEST_KEY + " = "
                + TEST_VALUE + CR + TEST_KEY + " = " + TEST_VALUE + "2" + CR
                + CR + CR + "# AnotherComment" + CR
                + "AnotherProperty = AnotherValue,3rdValue" + CR);
    }

    /**
     * Tests the force single line flag.
     */
    @Test
    public void testSaveForceSingleLine() throws ConfigurationException
    {
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        config.addProperty(TEST_KEY, TEST_VALUE);
        config.addProperty(TEST_KEY, TEST_VALUE + "2");
        config.addProperty("AnotherProperty", "value1;value2;value3");
        layout.setComment(TEST_KEY, TEST_COMMENT);
        layout.setForceSingleLine(true);
        checkLayoutString("# " + TEST_COMMENT + CR + TEST_KEY + " = "
                + TEST_VALUE + ';' + TEST_VALUE + "2" + CR
                + "AnotherProperty = value1;value2;value3" + CR);
    }

    /**
     * Tests the trimComment method.
     */
    @Test
    public void testTrimComment()
    {
        assertEquals("Wrong trimmed comment", "This is a comment" + CR
                + "that spans multiple" + CR + "lines in a" + CR
                + " complex way.", PropertiesConfigurationLayout.trimComment(
                "   # This is a comment" + CR + "that spans multiple" + CR
                        + "!lines in a" + CR + " complex way.", false));
    }

    /**
     * Tests trimming a comment with trailing CRs.
     */
    @Test
    public void testTrimCommentTrainlingCR()
    {
        assertEquals("Wrong trimmed comment", "Comment with" + CR
                + "trailing CR" + CR, PropertiesConfigurationLayout
                .trimComment("Comment with" + CR + "! trailing CR" + CR, false));
    }

    /**
     * Tests enforcing comment characters in a comment.
     */
    @Test
    public void testTrimCommentFalse()
    {
        assertEquals("Wrong trimmed comment", "# Comment with" + CR
                + " ! some mixed " + CR + "#comment" + CR + "# lines",
                PropertiesConfigurationLayout.trimComment("Comment with" + CR
                        + " ! some mixed " + CR + "#comment" + CR + "lines",
                        true));
    }

    /**
     * Tests accessing data for a property, which is not stored.
     */
    @Test
    public void testGetNonExistingLayouData()
    {
        assertNull("A comment was found", layout.getComment("unknown"));
        assertTrue("A multi-line property", layout.isSingleLine("unknown"));
        assertEquals("Leading blanc lines", 0, layout
                .getBlancLinesBefore("unknown"));
    }

    /**
     * Tests accessing a property with a null key. This should throw an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetNullLayouttData()
    {
        layout.setComment(null, TEST_COMMENT);
    }

    /**
     * Tests resetting a comment.
     */
    @Test
    public void testSetNullComment()
    {
        fillLayout();
        layout.setComment(TEST_KEY, null);
        assertNull("Comment was not reset", layout.getComment(TEST_KEY));
    }

    /**
     * Tests saving when a comment for a non existing property is contained in
     * the layout object. This comment should be ignored.
     */
    @Test
    public void testSaveCommentForUnexistingProperty()
            throws ConfigurationException
    {
        fillLayout();
        layout.setComment("NonExistingKey", "NonExistingComment");
        final String output = getLayoutString();
        assertTrue("Non existing key was found", !output.contains("NonExistingKey"));
        assertTrue("Non existing comment was found", !output.contains("NonExistingComment"));
    }

    /**
     * Tests saving an empty layout object.
     */
    @Test
    public void testSaveEmptyLayout() throws ConfigurationException
    {
        checkLayoutString("");
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testInitCopy()
    {
        fillLayout();
        final PropertiesConfigurationLayout l2 = new PropertiesConfigurationLayout(layout);
        assertEquals("Wrong number of keys", layout.getKeys().size(), l2
                .getKeys().size());
        for (final String key : layout.getKeys())
        {
            assertTrue("Key was not found: " + key, l2.getKeys().contains(key));
        }
        assertEquals("Wrong header comment", layout.getHeaderComment(),
                l2.getHeaderComment());
        assertEquals("Wrong footer comment", layout.getFooterComment(),
                l2.getFooterComment());
    }

    /**
     * Tests if the copy and the original are independent from each other.
     */
    @Test
    public void testInitCopyModify()
    {
        fillLayout();
        final PropertiesConfigurationLayout l2 = new PropertiesConfigurationLayout(layout);
        assertEquals("Comments are not equal", layout.getComment(TEST_KEY), l2
                .getComment(TEST_KEY));
        layout.setComment(TEST_KEY, "A new comment");
        assertEquals("Comment was changed", TEST_COMMENT, l2
                .getCanonicalComment(TEST_KEY, false));
        l2.setBlancLinesBefore(TEST_KEY, l2.getBlancLinesBefore(TEST_KEY) + 1);
        assertFalse("Blanc lines do not differ", layout
                .getBlancLinesBefore(TEST_KEY) == l2
                .getBlancLinesBefore(TEST_KEY));
    }

    /**
     * Tests changing the separator for a property.
     */
    @Test
    public void testSetSeparator() throws ConfigurationException
    {
        config.addProperty(TEST_KEY, TEST_VALUE);
        layout.setSeparator(TEST_KEY, ":");
        checkLayoutString(TEST_KEY + ":" + TEST_VALUE + CR);
    }

    /**
     * Tests setting the global separator. This separator should override the
     * separators for all properties.
     */
    @Test
    public void testSetGlobalSeparator() throws ConfigurationException
    {
        final String sep = "=";
        config.addProperty(TEST_KEY, TEST_VALUE);
        config.addProperty("key2", "value2");
        layout.setSeparator(TEST_KEY, " : ");
        layout.setGlobalSeparator(sep);
        checkLayoutString(TEST_KEY + sep + TEST_VALUE + CR + "key2" + sep
                + "value2" + CR);
    }

    /**
     * Tests setting the line separator.
     */
    @Test
    public void testSetLineSeparator() throws ConfigurationException
    {
        final String lf = CR + CR;
        config.addProperty(TEST_KEY, TEST_VALUE);
        layout.setBlancLinesBefore(TEST_KEY, 2);
        layout.setComment(TEST_KEY, TEST_COMMENT);
        layout.setHeaderComment("Header comment");
        layout.setLineSeparator(lf);
        checkLayoutString("# Header comment" + lf + lf + lf + lf + "# "
                + TEST_COMMENT + lf + TEST_KEY + " = " + TEST_VALUE + lf);
    }

    /**
     * Tests whether the line separator is also taken into account within
     * comments.
     */
    @Test
    public void testSetLineSeparatorInComments() throws ConfigurationException
    {
        final String lf = "<-\n";
        config.addProperty(TEST_KEY, TEST_VALUE);
        layout.setComment(TEST_KEY, TEST_COMMENT + "\nMore comment");
        layout.setHeaderComment("Header\ncomment");
        layout.setLineSeparator(lf);
        checkLayoutString("# Header" + lf + "# comment" + lf + lf + "# "
                + TEST_COMMENT + lf + "# More comment" + lf + TEST_KEY + " = "
                + TEST_VALUE + lf);
    }

    /**
     * Tests whether a line with whitespace is handled correctly. This is
     * related to CONFIGURATION-582.
     */
    @Test
    public void testLineWithBlank() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addLine(" ");
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(config, builder.getReader());
        assertEquals("Wrong comment", TEST_COMMENT + CRNORM + " ",
                layout.getCanonicalComment(TEST_KEY, false));
    }

    /**
     * Helper method for filling the layout object with some properties.
     */
    private void fillLayout()
    {
        builder.addComment("A header comment");
        builder.addComment(null);
        builder.addProperty("prop", "value");
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addProperty("anotherProp", "anotherValue");
        builder.addComment("A footer comment");
        try
        {
            layout.load(config, builder.getReader());
        }
        catch (final ConfigurationException cex)
        {
            // should not happen
            fail("Exception was thrown: " + cex);
        }
    }

    /**
     * Writes the layout's data into a string.
     *
     * @return the layout file's content as string
     * @throws ConfigurationException if an error occurs
     */
    private String getLayoutString() throws ConfigurationException
    {
        final StringWriter out = new StringWriter();
        layout.save(config, out);
        return out.toString();
    }

    /**
     * Checks if the layout's output is correct.
     *
     * @param expected the expected result
     * @throws ConfigurationException if an error occurs
     */
    private void checkLayoutString(final String expected)
            throws ConfigurationException
    {
        assertEquals("Wrong layout file content", expected, getLayoutString());
    }

    /**
     * A helper class used for constructing test properties files.
     */
    static class PropertiesBuilder
    {
        /** A buffer for storing the data. */
        private final StringBuilder buf = new StringBuilder();

        /** A counter for varying the comment character. */
        private int commentCounter;

        /**
         * Adds a line verbatim to the simulated file.
         *
         * @param s the content of the line
         */
        public void addLine(final String s)
        {
            buf.append(s).append(CR);
        }

        /**
         * Adds a property to the simulated file.
         *
         * @param key the property key
         * @param value the value
         */
        public void addProperty(final String key, final String value)
        {
            buf.append(key).append(" = ").append(value).append(CR);
        }

        /**
         * Adds a comment line.
         *
         * @param s the comment (can be <b>null</b>, then a blanc line is
         * added)
         */
        public void addComment(final String s)
        {
            if (s != null)
            {
                if (commentCounter % 2 == 0)
                {
                    buf.append("# ");
                }
                else
                {
                    buf.append("! ");
                }
                buf.append(s);
                commentCounter++;
            }
            buf.append(CR);
        }

        /**
         * Returns a reader for the simulated properties.
         *
         * @return a reader
         */
        public Reader getReader()
        {
            return new StringReader(buf.toString());
        }

        /**
         * Returns a string representation of the buffer's content.
         *
         * @return the buffer as string
         */
        @Override
        public String toString()
        {
            return buf.toString();
        }
    }

    /**
     * A mock properties configuration implementation that is used to check
     * whether some expected methods are called.
     */
    static class LayoutTestConfiguration extends PropertiesConfiguration
    {
        /** Stores a builder object. */
        public PropertiesBuilder builder;

        /**
         * Simulates the propertyLoaded() callback. If a builder was set, a
         * load() call on the layout is invoked.
         */
        @Override
        boolean propertyLoaded(final String key, final String value)
                throws ConfigurationException
        {
            if (builder == null)
            {
                return super.propertyLoaded(key, value);
            }
            if (PropertiesConfiguration.getInclude().equals(key))
            {
                getLayout().load(this, builder.getReader());
                return false;
            }
            return true;
        }
    }
}
