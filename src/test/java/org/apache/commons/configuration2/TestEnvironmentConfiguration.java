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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for EnvironmentConfiguration.
 */
public class TestEnvironmentConfiguration {
    /** Stores the configuration to be tested. */
    private EnvironmentConfiguration config;

    @BeforeEach
    public void setUp() throws Exception {
        config = new EnvironmentConfiguration();
    }

    /**
     * Tries to add another property. This should cause an exception.
     */
    @Test
    public void testAddProperty() {
        assertThrows(UnsupportedOperationException.class, () -> config.addProperty("JAVA_HOME", "C:\\java"));
    }

    /**
     * Tests removing all properties. This should not be possible.
     */
    @Test
    public void testClear() {
        assertThrows(UnsupportedOperationException.class, config::clear);
    }

    /**
     * Tests removing properties. This should not be possible.
     */
    @Test
    public void testClearProperty() {
        final String key = config.getKeys().next();
        assertThrows(UnsupportedOperationException.class, () -> config.clearProperty(key));
    }

    /**
     * Tests whether a newly created configuration contains some properties. (We expect that at least some properties are
     * set in each environment.)
     */
    @Test
    public void testInit() {
        boolean found = false;
        assertFalse(config.isEmpty());
        for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(config.containsKey(key), "Key not found: " + key);
            assertNotNull(config.getString(key), "No value for property " + key);
            found = true;
        }
        assertTrue(found);
    }

    /**
     * Tries to set the value of a property. This should cause an exception.
     */
    @Test
    public void testSetProperty() {
        assertThrows(UnsupportedOperationException.class, () -> config.setProperty("JAVA_HOME", "C:\\java"));
    }
}
