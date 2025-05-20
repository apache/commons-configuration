/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

/**
 * Unit test for {@link YAMLConfiguration}
 */
public class TestYAMLConfiguration {

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /** The files that we test with. */
    private final String testYaml = ConfigurationAssert.getTestFile("test.yaml").getAbsolutePath();

    private YAMLConfiguration yamlConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        yamlConfiguration = new YAMLConfiguration();
        yamlConfiguration.read(new FileReader(testYaml));
    }

    @Test
    public void testCopyConstructor() {
        final BaseHierarchicalConfiguration c = new BaseHierarchicalConfiguration();
        c.addProperty("foo", "bar");

        yamlConfiguration = new YAMLConfiguration(c);
        assertEquals("bar", yamlConfiguration.getString("foo"));
    }

    @Test
    public void testDoubleStringValues() {
        final Object property = yamlConfiguration.getProperty("key5.example");
        assertEquals(Arrays.asList("", "", "value"), property);
    }

    @Test
    public void testGetPropertyDictionary() {
        assertEquals("Martin D'vloper", yamlConfiguration.getProperty("martin.name"));
        assertEquals("Developer", yamlConfiguration.getProperty("martin.job"));
        assertEquals("Elite", yamlConfiguration.getProperty("martin.skill"));
    }

    @Test
    public void testGetPropertyInteger() {
        final Object property = yamlConfiguration.getProperty("int1");
        assertInstanceOf(Integer.class, property);
        assertEquals(37, property);
    }

    @Test
    public void testGetPropertyNested() {
        assertEquals("value23", yamlConfiguration.getProperty("key2.key3"));
    }

    @Test
    public void testGetPropertyNestedWithList() {
        assertEquals(Arrays.asList("col1", "col2"), yamlConfiguration.getProperty("key4.key5"));
    }

    @Test
    public void testGetPropertySimple() {
        assertEquals("value1", yamlConfiguration.getProperty("key1"));
    }

    @Test
    public void testGetPropertySubset() {
        final Configuration subset = yamlConfiguration.subset("key4");
        assertEquals(Arrays.asList("col1", "col2"), subset.getProperty("key5"));
    }

    @Test
    public void testGetPropertyVeryNestedProperties() {
        final Object property = yamlConfiguration.getProperty("very.nested.properties");
        assertEquals(Arrays.asList("nested1", "nested2", "nested3"), property);
    }

    @Test
    public void testObjectCreationFromReader() {
        final File createdFile = new File(tempFolder, "data.txt");
        final String yaml = "!!java.io.FileOutputStream [" + createdFile.getAbsolutePath() + "]";
        final StringReader reader = new StringReader(yaml);

        assertThrows(ConfigurationException.class, () -> yamlConfiguration.read(reader));
        assertFalse(createdFile.exists());
    }

    @Test
    public void testObjectCreationFromStream() {
        final File createdFile = new File(tempFolder, "data.txt");
        final String yaml = "!!java.io.FileOutputStream [" + createdFile.getAbsolutePath() + "]";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        assertThrows(ConfigurationException.class, () -> yamlConfiguration.read(inputStream));
        assertFalse(createdFile.exists());
    }

    @Test
    public void testSave() throws IOException, ConfigurationException {
        // save the YAMLConfiguration as a String...
        final StringWriter sw = new StringWriter();
        yamlConfiguration.write(sw);
        final String output = sw.toString();

        // ..and then try parsing it back as using SnakeYAML
        final Map<?, ?> parsed = new Yaml().loadAs(output, Map.class);
        assertEquals(7, parsed.entrySet().size());
        assertEquals("value1", parsed.get("key1"));

        final Map<?, ?> key2 = (Map<?, ?>) parsed.get("key2");
        assertEquals("value23", key2.get("key3"));

        final List<?> key5 = (List<?>) ((Map<?, ?>) parsed.get("key4")).get("key5");
        assertEquals(Arrays.asList("col1", "col2"), key5);
    }
}
