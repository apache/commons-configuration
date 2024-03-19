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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.Test;

/**
 * Compare the behavior of various methods between CompositeConfiguration and normal (Properties) Configuration
 */
public class TestEqualBehavior {
    /**
     * Checks whether two configurations have the same size, the same key sequence and contain the same key -> value
     * mappings
     */
    private void checkEquality(final String msg, final Configuration c1, final Configuration c2) {
        final Iterator<String> it1 = c1.getKeys();
        final Iterator<String> it2 = c2.getKeys();

        while (it1.hasNext() && it2.hasNext()) {
            final String key1 = it1.next();
            final String key2 = it2.next();
            assertEquals(key1, key2, msg + ", Keys: ");
            assertEquals(c1.containsKey(key1), c2.containsKey(key2), msg + ", Contains: ");
        }
        assertEquals(it1.hasNext(), it2.hasNext(), msg + ", Iterator: ");
    }

    /**
     * Checks whether two configurations have the same key -> value mapping
     */
    private void checkSameKey(final String msg, final String key, final Configuration c1, final Configuration c2) {
        final String[] s1 = c1.getStringArray(key);
        final String[] s2 = c2.getStringArray(key);

        assertArrayEquals(s1, s2, msg + ", String Array: ");

        final List<Object> list1 = c1.getList(key);
        final List<Object> list2 = c2.getList(key);

        assertEquals(list1, list2, msg + ", List: ");
    }

    private Configuration setupCompositeConfiguration() throws ConfigurationException {
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFile(ConfigurationAssert.getTestFile("testEqualDigester.xml")));
        return builder.getConfiguration();
    }

    private Configuration setupSimpleConfiguration() throws Exception {
        final String simpleConfigurationFile = ConfigurationAssert.getTestFile("testEqual.properties").getAbsolutePath();
        final PropertiesConfiguration c = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(c);
        handler.setFileName(simpleConfigurationFile);
        handler.load();
        return c;
    }

    /**
     * If we add a to an existing key, does it work?
     */
    @Test
    public void testAddingSet() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "existing.property";
        final String value = "new value";

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.addProperty(key, value);
        composite.addProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testAddingSet", key, simple, composite);
        checkEquality("testAddingSet", simple, composite);
    }

    /**
     * If we add a key, does it work?
     */
    @Test
    public void testAddingUnset() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "nonexisting.property";
        final String value = "new value";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.addProperty(key, value);
        composite.addProperty(key, value);

        checkSameKey("testAddingUnset", key, simple, composite);
        checkEquality("testAddingUnset", simple, composite);
    }

    /**
     * If we delete a key, does it vanish? Does it leave all the other keys unchanged? How about an unset key?
     */
    @Test
    public void testDeletingExisting() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "clear.property";

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.clearProperty(key);
        composite.clearProperty(key);

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkEquality("testDeletingExisting", simple, composite);
    }

    @Test
    public void testDeletingNonExisting() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "nonexisting.clear.property";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.clearProperty(key);
        composite.clearProperty(key);

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkEquality("testDeletingNonExisting", simple, composite);
    }

    /**
     * Are both configurations equal after loading?
     */
    @Test
    public void testLoading() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        checkEquality("testLoading", simple, composite);
    }

    @Test
    public void testSettingExisting() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "existing.property";
        final String value = "new value";

        assertTrue(simple.containsKey(key));
        assertNotEquals(value, simple.getString(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.setProperty(key, value);
        composite.setProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.getString(key), value);
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testSettingExisting", key, simple, composite);
        checkEquality("testSettingExisting", simple, composite);
    }

    /**
     * If we set a key, does it work? How about an existing key? Can we change it?
     */
    @Test
    public void testSettingNonExisting() throws Exception {
        final Configuration simple = setupSimpleConfiguration();
        final Configuration composite = setupCompositeConfiguration();

        final String key = "nonexisting.property";
        final String value = "new value";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.setProperty(key, value);
        composite.setProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testSettingNonExisting", key, simple, composite);
        checkEquality("testSettingNonExisting", simple, composite);
    }
}
