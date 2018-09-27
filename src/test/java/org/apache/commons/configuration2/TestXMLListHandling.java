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

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test class to test the handling of list structures in XMLConfiguration.
 */
public class TestXMLListHandling
{
    /** XML to be loaded by the configuration. */
    private static final String SOURCE = "<config>" + "<values>a,b,c</values>"
            + "<split><value>1</value><value>2</value></split>"
            + "<mixed><values>foo,blah</values><values>bar,baz</values></mixed>"
            + "</config>";

    /** Key for the string property with multiple values. */
    private static final String KEY_VALUES = "values";

    /** Key for the split list property. */
    private static final String KEY_SPLIT = "split.value";

    /** The XML element name for the single values of the split list. */
    private static final String ELEM_SPLIT = "value";

    /** Configuration to be tested. */
    private XMLConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = readFromString(SOURCE);
    }

    /**
     * Parses the specified string into an XML configuration.
     *
     * @param xml the XML to be parsed
     * @return the resulting configuration
     */
    private static XMLConfiguration readFromString(final String xml)
            throws ConfigurationException
    {
        final XMLConfiguration config = new XMLConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        final FileHandler handler = new FileHandler(config);
        handler.load(new StringReader(xml));
        return config;
    }

    /**
     * Saves the test configuration into a string.
     *
     * @return the resulting string
     */
    private String saveToString() throws ConfigurationException
    {
        final StringWriter writer = new StringWriter(4096);
        final FileHandler handler = new FileHandler(config);
        handler.save(writer);
        return writer.toString();
    }

    /**
     * Generates an XML element with the specified value as body.
     *
     * @param key the key
     * @param value the value
     * @return the string representation of this element
     */
    private static String element(final String key, final String value)
    {
        return "<" + key + '>' + value + "</" + key + '>';
    }

    /**
     * Checks whether the specified XML contains a list of values as a single,
     * comma-separated string.
     *
     * @param xml the XML
     * @param key the key
     * @param values the expected values
     */
    private static void checkCommaSeparated(final String xml, final String key,
            final String... values)
    {
        final String strValues = StringUtils.join(values, ',');
        final String element = element(key, strValues);
        assertThat(xml, containsString(element));
    }

    /**
     * Checks whether the specified XML contains a list of values as multiple
     * XML elements.
     *
     * @param xml the XML
     * @param key the key
     * @param values the expected values
     */
    private static void checkSplit(final String xml, final String key, final String... values)
    {
        for (final String v : values)
        {
            assertThat(xml, containsString(element(key, v)));
        }
    }

    /**
     * Tests that the list format is kept if properties are not touched,
     */
    @Test
    public void testSaveNoChanges() throws ConfigurationException
    {
        final String xml = saveToString();

        checkSplit(xml, ELEM_SPLIT, "1", "2");
        checkCommaSeparated(xml, KEY_VALUES, "a", "b", "c");
    }

    /**
     * Tests that a list item can be added without affecting the format.
     */
    @Test
    public void testAddListItem() throws ConfigurationException
    {
        config.addProperty(KEY_VALUES, "d");
        config.addProperty(KEY_SPLIT, "3");
        final String xml = saveToString();

        checkSplit(xml, ELEM_SPLIT, "1", "2", "3");
        checkCommaSeparated(xml, KEY_VALUES, "a", "b", "c", "d");
    }

    /**
     * Tests that a list item can be removed without affecting the format.
     */
    @Test
    public void testRemoveListItem() throws ConfigurationException
    {
        config.clearProperty(KEY_VALUES + "(2)");
        config.clearProperty(KEY_SPLIT + "(1)");
        final String xml = saveToString();

        checkSplit(xml, ELEM_SPLIT, "1");
        checkCommaSeparated(xml, KEY_VALUES, "a", "b");
    }

    /**
     * Tests whether a list consisting of multiple elements where some elements
     * define multiple values is handled correctly.
     */
    @Test
    public void testMixedList() throws ConfigurationException
    {
        final List<String> expected = Arrays.asList("foo", "blah", "bar", "baz");
        assertEquals("Wrong list value (1)", expected,
                config.getList("mixed.values"));
        final String xml = saveToString();

        final XMLConfiguration c2 = readFromString(xml);
        assertEquals("Wrong list value (2)", expected,
                c2.getList("mixed.values"));
    }

    /**
     * Tries to save the configuration with a different list delimiter handler
     * which does not support escaping of lists. This should fail with a
     * meaningful exception message.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testIncompatibleListDelimiterOnSaving()
            throws ConfigurationException
    {
        config.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        saveToString();
    }
}
