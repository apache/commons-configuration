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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 */
public class TestConfigurationSet {

    ConfigurationMap.ConfigurationSet set;

    String[] properties = {"booleanProperty", "doubleProperty", "floatProperty", "intProperty", "longProperty", "shortProperty", "stringProperty"};

    Object[] values = {Boolean.TRUE, Double.valueOf(Double.MAX_VALUE), Float.valueOf(Float.MAX_VALUE), Integer.valueOf(Integer.MAX_VALUE),
        Long.valueOf(Long.MAX_VALUE), Short.valueOf(Short.MAX_VALUE), "This is a string"};

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() throws Exception {
        final BaseConfiguration configuration = new BaseConfiguration();
        for (int i = 0; i < properties.length; i++) {
            configuration.setProperty(properties[i], values[i]);
        }
        set = new ConfigurationMap.ConfigurationSet(configuration);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
        set = null;
    }

    /**
     * Class under test for Iterator iterator()
     */
    @Test
    public void testIterator() {
        final Iterator<Map.Entry<Object, Object>> iterator = set.iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Object, Object> entry = iterator.next();
            boolean found = false;
            for (int i = 0; i < properties.length; i++) {
                if (entry.getKey().equals(properties[i])) {
                    assertEquals(values[i], entry.getValue(), "Incorrect value for property " + properties[i]);
                    found = true;
                }
            }
            assertTrue(found, "Could not find property " + entry.getKey());
            iterator.remove();
        }
        assertTrue(set.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(properties.length, set.size());
    }
}
