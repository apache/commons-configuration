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
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Unit test for {@link YAMLConfiguration}
 */
public class TestYAMLConfiguration
{
    /** The files that we test with. */
    private final String testYaml =
            ConfigurationAssert.getTestFile("test.yaml").getAbsolutePath();

    private YAMLConfiguration yamlConfiguration;

    @Before
    public void setUp() throws Exception
    {
        yamlConfiguration = new YAMLConfiguration();
        yamlConfiguration.read(new FileReader(testYaml));
    }

    @Test
    public void testGetProperty_simple()
    {
        assertEquals("value1", yamlConfiguration.getProperty("key1"));
    }

    @Test
    public void testGetProperty_nested()
    {
        assertEquals("value23", yamlConfiguration.getProperty("key2.key3"));
    }

    @Test
    public void testGetProperty_nested_with_list()
    {
        assertEquals(Arrays.asList("col1", "col2"),
                yamlConfiguration.getProperty("key4.key5"));
    }

    @Test
    public void testGetProperty_subset()
    {
        final Configuration subset = yamlConfiguration.subset("key4");
        assertEquals(Arrays.asList("col1", "col2"), subset.getProperty("key5"));
    }

    @Test
    public void testGetProperty_very_nested_properties()
    {
        final Object property =
                yamlConfiguration.getProperty("very.nested.properties");
        assertEquals(Arrays.asList("nested1", "nested2", "nested3"), property);
    }

    @Test
    public void testGetProperty_integer()
    {
        final Object property = yamlConfiguration.getProperty("int1");
        assertTrue("property should be an Integer",
                property instanceof Integer);
        assertEquals(37, property);
    }

    @Test
    public void testSave() throws IOException, ConfigurationException
    {
        // save the YAMLConfiguration as a String...
        final StringWriter sw = new StringWriter();
        yamlConfiguration.write(sw);
        final String output = sw.toString();

        // ..and then try parsing it back as using SnakeYAML
        final Map parsed = new Yaml().loadAs(output, Map.class);
        assertEquals(6, parsed.entrySet().size());
        assertEquals("value1", parsed.get("key1"));

        final Map key2 = (Map) parsed.get("key2");
        assertEquals("value23", key2.get("key3"));

        final List<String> key5 =
                (List<String>) ((Map) parsed.get("key4")).get("key5");
        assertEquals(2, key5.size());
        assertEquals("col1", key5.get(0));
        assertEquals("col2", key5.get(1));
    }

    @Test
    public void testGetProperty_dictionary()
    {
        assertEquals("Martin D'vloper",
                yamlConfiguration.getProperty("martin.name"));
        assertEquals("Developer", yamlConfiguration.getProperty("martin.job"));
        assertEquals("Elite", yamlConfiguration.getProperty("martin.skill"));
    }

    @Test
    public void testCopyConstructor()
    {
        final BaseHierarchicalConfiguration c = new BaseHierarchicalConfiguration();
        c.addProperty("foo", "bar");

        yamlConfiguration = new YAMLConfiguration(c);
        assertEquals("bar", yamlConfiguration.getString("foo"));
    }
}
