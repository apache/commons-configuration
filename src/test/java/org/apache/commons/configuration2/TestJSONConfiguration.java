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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

/**
 * Unit test for {@link JSONConfiguration} Not ideal: it uses the Jackson JSON parser just like
 * {@link JSONConfiguration} itself
 */
public class TestJSONConfiguration {
    /** The files that we test with. */
    private final String testJson = ConfigurationAssert.getTestFile("test.json").getAbsolutePath();

    private JSONConfiguration jsonConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        jsonConfiguration = new JSONConfiguration();
        jsonConfiguration.read(new FileReader(testJson));
    }

    @Test
    public void testCopyConstructor() {
        final BaseHierarchicalConfiguration c = new BaseHierarchicalConfiguration();
        c.addProperty("foo", "bar");

        jsonConfiguration = new JSONConfiguration(c);
        assertEquals("bar", jsonConfiguration.getString("foo"));
    }

    /**
     * Tests CONFIGURATION-793.
     */
    public void testGetList_nested_with_list() {
        assertEquals(Arrays.asList("col1", "col2"), jsonConfiguration.getList(String.class, "key4.key5"));
    }

    @Test
    public void testGetProperty_dictionary() {
        assertEquals("Martin D'vloper", jsonConfiguration.getProperty("martin.name"));
        assertEquals("Developer", jsonConfiguration.getProperty("martin.job"));
        assertEquals("Elite", jsonConfiguration.getProperty("martin.skill"));
    }

    @Test
    public void testGetProperty_dictionaryInList() {
        assertEquals("UK", jsonConfiguration.getString("capitals(1).country"));
        assertEquals("Washington", jsonConfiguration.getString("capitals(0).capital"));
    }

    @Test
    public void testGetProperty_integer() {
        final Object property = jsonConfiguration.getProperty("int1");
        assertInstanceOf(Integer.class, property);
        assertEquals(37, property);
    }

    @Test
    public void testGetProperty_nested() {
        assertEquals("value23", jsonConfiguration.getProperty("key2.key3"));
    }

    @Test
    public void testGetProperty_nested_with_list() {
        assertEquals(Arrays.asList("col1", "col2"), jsonConfiguration.getProperty("key4.key5"));
    }

    @Test
    public void testGetProperty_simple() {
        assertEquals("value1", jsonConfiguration.getProperty("key1"));
    }

    @Test
    public void testGetProperty_subset() {
        final Configuration subset = jsonConfiguration.subset("key4");
        assertEquals(Arrays.asList("col1", "col2"), subset.getProperty("key5"));
    }

    @Test
    public void testGetProperty_very_nested_properties() {
        final Object property = jsonConfiguration.getProperty("very.nested.properties");
        assertEquals(Arrays.asList("nested1", "nested2", "nested3"), property);
    }

    /**
     * Tests CONFIGURATION-793.
     */
    @Disabled
    @Test
    public void testListOfObjects() {
        final Configuration subset = jsonConfiguration.subset("capitals");
        assertNotNull(subset);
        assertEquals(2, subset.size());

        final List<Object> list = jsonConfiguration.getList("capitals");
        assertNotNull(list);
        assertEquals(2, list.size());

//        assertEquals(list.get(0).get("country"), "USA");
//        assertEquals(list.get(0).get("capital"), "Washington");
//        assertEquals(list.get(1).get("country"), "UK");
//        assertEquals(list.get(1).get("capital"), "London");
    }

    @Test
    public void testSave() throws IOException, ConfigurationException {
        // save the Configuration as a String...
        final StringWriter sw = new StringWriter();
        jsonConfiguration.write(sw);
        final String output = sw.toString();

        // ..and then try parsing it back
        final ObjectMapper mapper = new ObjectMapper();
        final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        final Map<String, Object> parsed = mapper.readValue(output, type);
        assertEquals(7, parsed.entrySet().size());
        assertEquals("value1", parsed.get("key1"));

        final Map<?, ?> key2 = (Map<? , ?>) parsed.get("key2");
        assertEquals("value23", key2.get("key3"));

        final List<?> key5 = (List<?>) ((Map<?, ?>) parsed.get("key4")).get("key5");
        assertEquals(Arrays.asList("col1", "col2"), key5);

        final List<?> capitals = (List<?>) parsed.get("capitals");
        final Map<?, ?> capUk = (Map<?, ?>) capitals.get(1);
        assertEquals("London", capUk.get("capital"));
    }
}
