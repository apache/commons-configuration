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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the StrintConfigurationComparator class
 *
 */
public class TestStrictConfigurationComparator {
    /**
     * The comparator.
     */
    protected ConfigurationComparator comparator = new StrictConfigurationComparator();

    /**
     * The first configuration.
     */
    protected Configuration configuration = new BaseConfiguration();

    /**
     * Tests the comparator.
     */
    @Test
    public void testCompare() {
        // Identity comparison for empty configuration
        assertTrue(comparator.compare(configuration, configuration));

        configuration.setProperty("one", "1");
        configuration.setProperty("two", "2");
        configuration.setProperty("three", "3");

        // Identify comparison for non-empty configuration
        assertTrue(comparator.compare(configuration, configuration));

        // Create the second configuration
        final Configuration other = new BaseConfiguration();
        assertFalse(comparator.compare(configuration, other));

        other.setProperty("one", "1");
        other.setProperty("two", "2");
        other.setProperty("three", "3");

        // Two identical, non-empty configurations
        assertTrue(comparator.compare(configuration, other));

        other.setProperty("four", "4");
        assertFalse(comparator.compare(configuration, other));

        configuration.setProperty("four", "4");
        assertTrue(comparator.compare(configuration, other));
    }

    @Test
    public void testCompareNull() {
        assertTrue(comparator.compare(null, null));
        assertFalse(comparator.compare(configuration, null));
        assertFalse(comparator.compare(null, configuration));
    }
}
