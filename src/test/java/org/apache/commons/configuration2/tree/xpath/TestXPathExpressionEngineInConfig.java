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
package org.apache.commons.configuration2.tree.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.util.Iterator;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * A test class for XPathExpressionEngine that tests the engine integrated into
 * a hierarchical configuration.
 *
 */
public class TestXPathExpressionEngineInConfig
{
    /** Constant for a test key. */
    private static final String KEY = "test/expression/xpath";

    /** Constant for a value for test properties. */
    private static final String VALUE = "success";

    /** The test configuration. */
    private XMLConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = new XMLConfiguration();
        config.setExpressionEngine(new XPathExpressionEngine());
    }

    /**
     * Tests whether an already existing property can be changed using
     * setProperty().
     */
    @Test
    public void testSetPropertyExisting()
    {
        config.addProperty(" " + KEY, "failure");
        config.setProperty(KEY, VALUE);
        assertEquals("Value not changed", VALUE, config.getString(KEY));
    }

    /**
     * Tests setProperty() if the specified path partly exists.
     */
    @Test
    public void testSetPropertyPartlyExisting()
    {
        final String testKey = KEY + "/sub";
        config.addProperty(" " + KEY, "test");
        config.setProperty(testKey, VALUE);
        assertEquals("Value not set", VALUE, config.getString(testKey));
    }

    /**
     * Tests whether setProperty() can be used to add a new attribute.
     */
    @Test
    public void testSetPropertyNewAttribute()
    {
        final String keyAttr = KEY + "/@attr";
        config.addProperty(" " + KEY, "test");
        config.setProperty(keyAttr, VALUE);
        assertEquals("Value not set", VALUE, config.getString(keyAttr));
    }

    /**
     * Tests whether setProperty() can be used to create a completely new key.
     */
    @Test
    public void testSetPropertyNewKey()
    {
        config.setProperty(KEY, VALUE);
        assertEquals("Value not set", VALUE, config.getString(KEY));
    }

    /**
     * Tests whether addProperty() can be used to create more complex
     * hierarchical structures.
     */
    @Test
    public void testAddPropertyComplexStructures()
    {
        config.addProperty("tables/table/name", "tasks");
        config.addProperty("tables/table[last()]/@type", "system");
        config.addProperty("tables/table[last()]/fields/field/name", "taskid");
        config.addProperty("tables/table[last()]/fields/field[last()]/@type",
                "int");
        config.addProperty("tables table/name", "documents");
        assertEquals("Wrong table 1", "tasks",
                config.getString("tables/table[1]/name"));
        assertEquals("Wrong table 2", "documents",
                config.getString("tables/table[2]/name"));
        assertEquals("Wrong field type", "int",
                config.getString("tables/table[1]/fields/field[1]/@type"));
    }

    /**
     * Tests whether configuration properties with a namespace can be handled.
     */
    @Test
    public void testPropertiesWithNamespace() throws ConfigurationException
    {
        final String xml =
                "<Config>\n"
                        + "<dsig:Transforms xmlns:dsig=\"http://www.w3.org/2000/09/xmldsig#\">\n"
                        + "  <dsig:Transform Algorithm=\"http://www.w3.org/TR/1999/REC-xpath-19991116\">\n"
                        + "    <dsig:XPath xmlns:ietf=\"http://www.ietf.org\" xmlns:pl=\"http://test.test\">self::pl:policy1</dsig:XPath>\n"
                        + "  </dsig:Transform>\n"
                        + "  <dsig:Transform Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>\n"
                        + "</dsig:Transforms>" + "</Config>";
        final FileHandler handler = new FileHandler(config);
        handler.load(new StringReader(xml));

        for (final Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertNotNull("No value for " + key, config.getString(key));
        }
    }
}
