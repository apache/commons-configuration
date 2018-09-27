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

import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Test;

/**
 * Tests for {@code XMLConfiguration} related to CONFIGURATION-605: XMLConfiguration drops
 * configuration key immediately following one whose value contains a comma
 */
public class TestXMLConfiguration_605
{
    /**
     * Checks whether the specified configuration contains all expected keys.
     *
     * @param config the configuration to be checked
     */
    private static void checkConfiguration(final Configuration config)
    {
        assertTrue("Configuration has key key0", config.containsKey("key0"));
        assertTrue("Configuration has key key1", config.containsKey("key1"));
        assertTrue("Configuration has key key3", config.containsKey("key3"));

        assertTrue("Configuration has key key2", config.containsKey("key2"));
    }

    /**
     * Creates a configuration with the specified content and the legacy list
     * delimiter handler.
     *
     * @param content the XML content
     * @return the newly created configuration
     */
    private static Configuration create(final String content) throws ConfigurationException
    {
        final XMLConfiguration config = new XMLConfiguration();
        config.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
        final FileHandler handler = new FileHandler(config);
        handler.load(new StringReader(content));
        return config;
    }

    /**
     * Creates a new configuration with the specified content and the given list
     * delimiter handler.
     *
     * @param content the XML content
     * @param delimiterHandler the list delimiter handler
     * @return the newly created configuration
     */
    private static Configuration create(final String content, final ListDelimiterHandler delimiterHandler)
            throws ConfigurationException
    {
        final XMLConfiguration config = new XMLConfiguration();
        config.setListDelimiterHandler(delimiterHandler);
        final FileHandler handler = new FileHandler(config);
        handler.load(new StringReader(content));
        return config;
    }

    @Test
    public void testWithNoComma() throws Exception
    {
        final String source = "<configuration><key0></key0><key1></key1><key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source));
    }

    @Test
    public void testWithOnlyComma() throws Exception
    {
        final String source = "<configuration><key0></key0><key1>,</key1><key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source));
    }

    @Test
    public void testWithCommaSeparatedList() throws Exception
    {
        final String source = "<configuration><key0></key0><key1>a,b</key1><key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source));
    }

    @Test
    public void testWithSeparatingWhitespace() throws Exception
    {
        final String source = "<configuration><key0></key0><key1>,</key1> <key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source));
    }

    @Test
    public void testWithSeparatingNonWhitespace() throws Exception
    {
        final String source = "<configuration><key0></key0><key1>,</key1>A<key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source));
    }

    @Test
    public void testWithOnlyCommaWithoutDelimiterParsing() throws Exception
    {
        final String source = "<configuration><key0></key0><key1>,</key1><key2></key2><key3></key3></configuration>";
        checkConfiguration(create(source, DisabledListDelimiterHandler.INSTANCE));
    }

    @Test
    public void testWithOnlyCommaWithStringBuilder() throws Exception
    {
        final StringBuilder sourceBuilder = new StringBuilder("<configuration>");
        sourceBuilder.append("<key0></key0>");
        sourceBuilder.append("<key1>,</key1>");
        sourceBuilder.append("<key2></key2>");
        sourceBuilder.append("<key3></key3>");
        sourceBuilder.append("</configuration>");
        checkConfiguration(create(sourceBuilder.toString()));
    }

    @Test
    public void testWithOnlyCommaWithStringBuilderWithoutDelimiterParsing() throws Exception
    {
        final StringBuilder sourceBuilder = new StringBuilder("<configuration>");
        sourceBuilder.append("<key0></key0>");
        sourceBuilder.append("<key1>,</key1>");
        sourceBuilder.append("<key2></key2>");
        sourceBuilder.append("<key3></key3>");
        sourceBuilder.append("</configuration>");
        checkConfiguration(create(sourceBuilder.toString(), DisabledListDelimiterHandler.INSTANCE));
    }
}
