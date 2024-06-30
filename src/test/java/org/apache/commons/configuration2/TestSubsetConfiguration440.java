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
import static org.junit.jupiter.api.Assertions.assertFalse;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
// import org.junit.Test;

/**
 * Test case for the {@link SubsetConfiguration} class.
 */
public class TestSubsetConfiguration440 {

    /**
     * Tests CONFIGURATION-848.
     */
    @Test
    @Disabled
    public void testSubsetConfigurationWithIndexAndDelimiter() throws ConfigurationException, IOException {
        final JSONConfiguration jsonConfiguration = new JSONConfiguration();
        try (FileReader in = new FileReader(ConfigurationAssert.getTestFile("test-configuration-440.json").getAbsolutePath())) {
            jsonConfiguration.read(in);
        }
        // 1. using composite configuration
        final List<Configuration> list = new ArrayList<>();
        list.add(jsonConfiguration);
        list.add(jsonConfiguration);
        final CompositeConfiguration composite = new CompositeConfiguration(list);
        Configuration subset = composite.subset("books(0).details");
        assertFalse(subset.isEmpty());
        assertEquals(2, subset.size());
        assertEquals("No Longer Human", subset.getString("title"));
        // 2. using '.' delimiter
        subset = new SubsetConfiguration(jsonConfiguration, "books(0).details", ".");
        assertFalse(subset.isEmpty());
        assertEquals(2, subset.size());
        assertEquals("No Longer Human", subset.getString("title"));
        // 3. using '@' delimiter
        subset = new SubsetConfiguration(jsonConfiguration, "books(1)@details", "@");
        assertFalse(subset.isEmpty());
        assertEquals(2, subset.size());
        assertEquals("White Nights", subset.getString("title"));
    }

    @Test
    @Disabled
    public void testSubsetWithJSONConfiguration() throws ConfigurationException, IOException {
        final JSONConfiguration jsonConfiguration = new JSONConfiguration();
        try (FileReader in = new FileReader(ConfigurationAssert.getTestFile("test-configuration-440.json").getAbsolutePath())) {
            jsonConfiguration.read(in);
        }
        final SubsetConfiguration subset = new SubsetConfiguration(jsonConfiguration, "capitals(0)", ".");
        assertFalse(subset.isEmpty());
        assertEquals(2, subset.size());
        assertEquals("USA", subset.getString("country"));
    }
}
