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
package org.apache.commons.configuration2.interpol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code SystemPropertiesLookup}.
 */
public class TestSystemPropertiesLookup {
    /** The lookup object to be tested. */
    private Lookup lookup;

    @BeforeEach
    public void setUp() throws Exception {
        lookup = DefaultLookups.SYSTEM_PROPERTIES.getLookup();
    }

    /**
     * Tests whether system properties can be looked up.
     */
    @Test
    public void testLookupProperties() {
        for (final Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            assertEquals(e.getValue(), lookup.lookup(String.valueOf(e.getKey())), "Wrong property value for " + e.getKey());
        }
    }

    /**
     * Tests whether an unknown property is handled correctly.
     */
    @Test
    public void testLookupUnknownProperty() {
        assertNull(lookup.lookup("a non existing system property!"));
    }
}
