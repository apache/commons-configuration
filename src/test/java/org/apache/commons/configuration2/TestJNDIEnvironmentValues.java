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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJNDIEnvironmentValues {
    private JNDIConfiguration conf;

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty("java.naming.factory.initial", TestJNDIConfiguration.CONTEXT_FACTORY);

        conf = new JNDIConfiguration();
        conf.setThrowExceptionOnMissing(true);
    }

    @Test
    void testClearProperty() {
        assertNotNull(conf.getShort("test.short", null));
        conf.clearProperty("test.short");
        assertNull(conf.getShort("test.short", null));
    }

    @Test
    void testContainsKey() throws Exception {
        assertTrue(conf.containsKey("test.key"));
        assertFalse(conf.containsKey("test.imaginarykey"));
    }

    @Test
    void testGetKeys() throws Exception {
        boolean found = false;
        final Iterator<String> it = conf.getKeys();

        assertTrue(it.hasNext());

        while (it.hasNext() && !found) {
            found = "test.boolean".equals(it.next());
        }

        assertTrue(found);
    }

    @Test
    void testGetKeysWithExistingPrefix() {
        // test for an existing prefix
        final Iterator<String> it = conf.getKeys("test");
        boolean found = false;
        while (it.hasNext() && !found) {
            found = "test.boolean".equals(it.next());
        }

        assertTrue(found);
    }

    @Test
    void testGetKeysWithKeyAsPrefix() {
        // test for a prefix matching exactly the key of a property
        final Iterator<String> it = conf.getKeys("test.boolean");
        boolean found = false;
        while (it.hasNext() && !found) {
            found = "test.boolean".equals(it.next());
        }

        assertTrue(found);
    }

    @Test
    void testGetKeysWithUnknownPrefix() {
        // test for a unknown prefix
        final Iterator<String> it = conf.getKeys("foo.bar");
        assertFalse(it.hasNext());
    }

    @Test
    void testGetMissingKey() throws Exception {
        assertThrows(NoSuchElementException.class, () -> conf.getString("test.imaginarykey"));
    }

    @Test
    void testGetMissingKeyWithDefault() throws Exception {
        final String result = conf.getString("test.imaginarykey", "bob");
        assertEquals("bob", result);
    }

    @Test
    void testIsEmpty() {
        assertFalse(conf.isEmpty());
    }

    @Test
    void testMoreGets() throws Exception {
        final String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
        assertEquals("jndivalue2", conf.getString("test.key2"));
        assertEquals(1, conf.getShort("test.short"));
    }

    @Test
    void testSimpleGet() throws Exception {
        final String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
    }

    @Test
    void testThrowExceptionOnMissing() {
        assertTrue(conf.isThrowExceptionOnMissing());
    }
}
