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
package org.apache.commons.configuration;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.configuration.event.ConfigurationEvent;

/**
 * Test class for PropertiesConfigurationLayout.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestPropertiesConfigurationLayout extends TestCase
{
    /** Constant for the line break character. */
    static final String CR = System.getProperty("line.separator");

    /** Constant for a test property key. */
    static final String TEST_KEY = "myProperty";

    /** Constant for a test comment. */
    static final String TEST_COMMENT = "A comment for my test property";

    /** Constant for a test property value. */
    static final String TEST_VALUE = "myPropertyValue";

    /** The layout object under test. */
    PropertiesConfigurationLayout layout;

    /** The associated configuration object. */
    LayoutTestConfiguration config;

    /** A properties builder that can be used for testing. */
    PropertiesBuilder builder;

    protected void setUp() throws Exception
    {
        super.setUp();
        config = new LayoutTestConfiguration();
        layout = new PropertiesConfigurationLayout(config);
        config.setLayout(layout);
        builder = new PropertiesBuilder();
    }

    /**
     * Tests a newly created instance.
     */
    public void testInit()
    {
        assertTrue("Object contains keys", layout.getKeys().isEmpty());
        assertNull("Header comment not null", layout.getHeaderComment());
        Iterator it = config.getConfigurationListeners().iterator();
        assertTrue("No event listener registered", it.hasNext());
        assertSame("Layout not registered as event listener", layout, it.next());
        assertFalse("Multiple event listeners registered", it.hasNext());
        assertSame("Configuration not stored", config, layout
                .getConfiguration());
        assertFalse("Force single line flag set", layout.isForceSingleLine());
    }

    /**
     * Tests creating a layout object with a null configuration. This should
     * cause an exception.
     */
    public void testInitNull()
    {
        try
        {
            new PropertiesConfigurationLayout(null);
            fail("Could create instance with null config!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests reading a simple properties file.
     */
    public void testReadSimple() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
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
    public void testBlancLines() throws ConfigurationException
    {
        builder.addProperty("prop", "value");
        builder.addComment(null);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
        assertEquals("Wrong number of blanc lines", 2, layout
                .getBlancLinesBefore(TEST_KEY));
        assertEquals("Wrong comment", TEST_COMMENT + CR, layout
                .getCanonicalComment(TEST_KEY, false));
        assertEquals("Wrong property value", TEST_VALUE, config
                .getString(TEST_KEY));
    }

    /**
     * Tests the single line flag for a simple property definition.
     */
    public void testIsSingleLine() throws ConfigurationException
    {
        builder.addProperty(TEST_KEY, TEST_VALUE + "," + TEST_VALUE + "2");
        layout.load(builder.getReader());
        assertTrue("Wrong single line flag", layout.isSingleLine(TEST_KEY));
        assertEquals("Wrong number of values", 2, config.getList(TEST_KEY)
                .size());
    }

    /**
     * Tests the single line flag if there are multiple property definitions.
     */
    public void testIsSingleLineMulti() throws ConfigurationException
    {
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addProperty("anotherProp", "a value");
        builder.addProperty(TEST_KEY, TEST_VALUE + "2");
        layout.load(builder.getReader());
        assertFalse("Wrong single line flag", layout.isSingleLine(TEST_KEY));
        assertEquals("Wrong number of values", 2, config.getList(TEST_KEY)
                .size());
    }

    /**
     * Tests whether comments are combined for multiple occurrences.
     */
    public void testCombineComments() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE + "2");
        layout.load(builder.getReader());
        assertEquals("Wrong combined comment",
                TEST_COMMENT + CR + TEST_COMMENT, layout.getCanonicalComment(
                        TEST_KEY, false));
        assertEquals("Wrong combined blanc numbers", 0, layout
                .getBlancLinesBefore(TEST_KEY));
    }

    /**
     * Tests if a header comment is detected.
     */
    public void testHeaderComment() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT, layout
                .getCanonicalHeaderComment(false));
        assertNull("Wrong comment for property", layout.getCanonicalComment(
                TEST_KEY, false));
    }

    /**
     * Tests if a header comment containing blanc lines is correctly detected.
     */
    public void testHeaderCommentWithBlancs() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT + CR + CR
                + TEST_COMMENT, layout.getCanonicalHeaderComment(false));
        assertNull("Wrong comment for property", layout.getComment(TEST_KEY));
    }

    /**
     * Tests if a header comment is correctly detected when it contains blanc
     * lines and the first property has a comment, too.
     */
    public void testHeaderCommentWithBlancsAndPropComment()
            throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addComment(null);
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
        assertEquals("Wrong header comment", TEST_COMMENT + CR + CR
                + TEST_COMMENT, layout.getCanonicalHeaderComment(false));
        assertEquals("Wrong comment for property", TEST_COMMENT, layout
                .getCanonicalComment(TEST_KEY, false));
    }

    /**
     * Tests fetching a canonical header comment when no comment is set.
     */
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
    public void testEventAdd()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.configurationChanged(event);
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
    public void testEventAddMultiple()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.configurationChanged(event);
        layout.configurationChanged(event);
        assertFalse("No multi-line property", layout.isSingleLine(TEST_KEY));
    }

    /**
     * Tests if an add event is correctly processed if the affected property is
     * already stored in the layout object.
     */
    public void testEventAddExisting() throws ConfigurationException
    {
        builder.addComment(TEST_COMMENT);
        builder.addProperty(TEST_KEY, TEST_VALUE);
        layout.load(builder.getReader());
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.configurationChanged(event);
        assertFalse("No multi-line property", layout.isSingleLine(TEST_KEY));
        assertEquals("Comment was modified", TEST_COMMENT, layout
                .getCanonicalComment(TEST_KEY, false));
    }

    /**
     * Tests if a set property event for a non existing property is correctly
     * handled.
     */
    public void testEventSetNonExisting()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_SET_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.configurationChanged(event);
        assertTrue("New property was not found", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests if a property delete event is correctly processed.
     */
    public void testEventDelete()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                false);
        layout.configurationChanged(event);
        event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_CLEAR_PROPERTY, TEST_KEY, null,
                false);
        layout.configurationChanged(event);
        assertFalse("Property still existing", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests if a clear event is correctly processed.
     */
    public void testEventClearConfig() throws ConfigurationException
    {
        fillLayout();
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_CLEAR, null, null, false);
        layout.configurationChanged(event);
        assertTrue("Keys not empty", layout.getKeys().isEmpty());
        assertNull("Header comment was not reset", layout.getHeaderComment());
    }

    /**
     * Tests if a before update event is correctly ignored.
     */
    public void testEventAddBefore()
    {
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractConfiguration.EVENT_ADD_PROPERTY, TEST_KEY, TEST_VALUE,
                true);
        layout.configurationChanged(event);
        assertFalse("Property already stored", layout.getKeys().contains(
                TEST_KEY));
    }

    /**
     * Tests if a reload update is correctly processed.
     */
    public void testEventReload()
    {
        fillLayout();
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractFileConfiguration.EVENT_RELOAD, null, null, true);
        layout.configurationChanged(event);
        assertTrue("Keys not empty", layout.getKeys().isEmpty());
        assertNull("Header comment was not reset", layout.getHeaderComment());
    }

    /**
     * Tests the event after a reload has been performed. This should be
     * ignored.
     */
    public void testEventReloadAfter()
    {
        fillLayout();
        ConfigurationEvent event = new ConfigurationEvent(this,
                AbstractFileConfiguration.EVENT_RELOAD, null, null, false);
        layout.configurationChanged(event);
        assertFalse("Keys are empty", layout.getKeys().isEmpty());
        assertNotNull("Header comment was reset", layout.getHeaderComment());
    }

    /**
     * Tests a recursive load call.
     */
    public void testRecursiveLoadCall() throws ConfigurationException
    {
        PropertiesBuilder b = new PropertiesBuilder();
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

        layout.load(builder.getReader());

        assertEquals("Wrong header comment", "Header comment", layout
                .getCanonicalHeaderComment(false));
        assertFalse("Include property was stored", layout.getKeys().contains(
                PropertiesConfiguration.getInclude()));
        assertEquals("Wrong comment for property", TEST_COMMENT + CR
                + "A nested header comment." + CR + "With multiple lines" + CR
                + CR + "Second comment", layout.getCanonicalComment(TEST_KEY,
                false));
    }

    /**
     * Tests whether the output of the layout object is identical to the source
     * file (at least for simple properties files).
     */
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
        layout.load(builder.getReader());
        checkLayoutString(builder.toString());
    }

    /**
     * Tests if the content of the layout object is correctly written.
     */
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
        layout.setHeaderComment("A header comment" + CR + "for my properties");
        checkLayoutString("# A header comment" + CR + "# for my properties"
                + CR + CR + "# " + TEST_COMMENT + CR + TEST_KEY + " = "
                + TEST_VALUE + CR + TEST_KEY + " = " + TEST_VALUE + "2" + CR
                + CR + CR + "# AnotherComment" + CR
                + "AnotherProperty = AnotherValue,3rdValue" + CR);
    }

    /**
     * Tests the force single line flag.
     */
    public void testSaveForceSingleLine() throws ConfigurationException
    {
        config.setListDelimiter(';');
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
    public void testTrimCommentTrainlingCR()
    {
        assertEquals("Wrong trimmed comment", "Comment with" + CR
                + "trailing CR" + CR, PropertiesConfigurationLayout
                .trimComment("Comment with" + CR + "! trailing CR" + CR, false));
    }

    /**
     * Tests enforcing comment characters in a comment.
     */
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
    public void testGetNullLayouttData()
    {
        try
        {
            layout.setComment(null, TEST_COMMENT);
            fail("Could access null property key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests resetting a comment.
     */
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
    public void testSaveCommentForUnexistingProperty()
            throws ConfigurationException
    {
        fillLayout();
        layout.setComment("NonExistingKey", "NonExistingComment");
        String output = getLayoutString();
        assertTrue("Non existing key was found", output
                .indexOf("NonExistingKey") < 0);
        assertTrue("Non existing comment was found", output
                .indexOf("NonExistingComment") < 0);
    }

    /**
     * Tests saving an empty layout object.
     */
    public void testSaveEmptyLayout() throws ConfigurationException
    {
        checkLayoutString("");
    }

    /**
     * Tests the copy constructor.
     */
    public void testInitCopy()
    {
        fillLayout();
        PropertiesConfigurationLayout l2 = new PropertiesConfigurationLayout(
                config, layout);
        assertEquals("Wrong number of keys", layout.getKeys().size(), l2
                .getKeys().size());
        for (Iterator it = layout.getKeys().iterator(); it.hasNext();)
        {
            Object key = it.next();
            assertTrue("Key was not found: " + key, l2.getKeys().contains(key));
        }
    }

    /**
     * Tests if the copy and the original are independent from each other.
     */
    public void testInitCopyModify()
    {
        fillLayout();
        PropertiesConfigurationLayout l2 = new PropertiesConfigurationLayout(
                config, layout);
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
    public void testSetSeparator() throws ConfigurationException
    {
        config.addProperty(TEST_KEY, TEST_VALUE);
        layout.setSeparator(TEST_KEY, ":");
        checkLayoutString(TEST_KEY + ":" + TEST_VALUE + CR);
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
        try
        {
            layout.load(builder.getReader());
        }
        catch (ConfigurationException cex)
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
        StringWriter out = new StringWriter();
        layout.save(out);
        return out.toString();
    }

    /**
     * Checks if the layout's output is correct.
     *
     * @param expected the expected result
     * @throws ConfigurationException if an error occurs
     */
    private void checkLayoutString(String expected)
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
        private StringBuffer buf = new StringBuffer();

        /** A counter for varying the comment character. */
        private int commentCounter;

        /**
         * Adds a property to the simulated file.
         *
         * @param key the property key
         * @param value the value
         */
        public void addProperty(String key, String value)
        {
            buf.append(key).append(" = ").append(value).append(CR);
        }

        /**
         * Adds a comment line.
         *
         * @param s the comment (can be <b>null</b>, then a blanc line is
         * added)
         */
        public void addComment(String s)
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
        boolean propertyLoaded(String key, String value)
                throws ConfigurationException
        {
            if (builder == null)
            {
                return super.propertyLoaded(key, value);
            }
            else
            {
                if (PropertiesConfiguration.getInclude().equals(key))
                {
                    getLayout().load(builder.getReader());
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }
    }
}
