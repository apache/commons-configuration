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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * A test class which tests functionality related to immutable configurations.
 */
public class TestImmutableConfiguration {
    /** Constant for the name of a test properties file. */
    private static final String TEST_FILE = "test.properties";

    /**
     * Creates a test configuration object filled with properties.
     *
     * @return the test configuration
     * @throws ConfigurationException if an error occurs
     */
    private static PropertiesConfiguration createTestConfig() throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
            .configure(new FileBasedBuilderParametersImpl().setFile(ConfigurationAssert.getTestFile(TEST_FILE))).getConfiguration();
    }

    /**
     * Obtains all keys from the given iteration.
     *
     * @param it the iterator
     * @return a set with all keys
     */
    private static Set<String> fetchKeys(final Iterator<String> it) {
        final Set<String> keys = new HashSet<>();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        return keys;
    }

    /**
     * Tests that exceptions thrown by the wrapped configuration are handled correctly.
     */
    @Test
    public void testExceptionHandling() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final String property = "nonExistingProperty";
        config.setThrowExceptionOnMissing(true);
        final ImmutableConfiguration ic = ConfigurationUtils.unmodifiableConfiguration(config);
        final NoSuchElementException e = assertThrows(NoSuchElementException.class, () -> ic.getString(property));
        assertThat(e.getMessage(), containsString(property));
    }

    /**
     * Tests whether an immutable subset can be queried.
     */
    @Test
    public void testImmutableSubset() throws ConfigurationException {
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(createTestConfig());
        final ImmutableConfiguration subset = conf.immutableSubset("test");
        assertFalse(subset.isEmpty());
        assertEquals(1000000, subset.getLong("long"));
    }

    /**
     * Tests whether data can be accessed from an unmodifiable configuration.
     */
    @Test
    public void testUnmodifiableConfigurationAccess() throws ConfigurationException {
        final Configuration confOrg = createTestConfig();
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(confOrg);
        assertFalse(conf.isEmpty());
        for (final Iterator<String> it = confOrg.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(conf.containsKey(key), "Key not contained: " + key);
            assertEquals(confOrg.getProperty(key), conf.getProperty(key), "Wrong value for " + key);
        }
    }

    /**
     * Tests that a cast to a mutable configuration is not possible.
     */
    @Test
    public void testUnmodifiableConfigurationCast() throws ConfigurationException {
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(createTestConfig());
        assertThrows(ClassCastException.class, () -> {
            final Configuration mutableConf = (Configuration) conf;
            mutableConf.clear();
        });
    }

    /**
     * Tests an iteration over the keys of the immutable configuration.
     */
    @Test
    public void testUnmodifiableConfigurationIterate() throws ConfigurationException {
        final Configuration confOrg = createTestConfig();
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(confOrg);
        assertEquals(fetchKeys(confOrg.getKeys()), fetchKeys(conf.getKeys()));
    }

    /**
     * Tests that it is not possible to remove keys using the iterator.
     */
    @Test
    public void testUnmodifiableConfigurationIteratorRemove() throws ConfigurationException {
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(createTestConfig());
        final Iterator<String> it = conf.getKeys();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    /**
     * Tests whether an update of the original configuration is visible for the immutable view.
     */
    @Test
    public void testUnmodifiableConfigurationLiveUpdate() throws ConfigurationException {
        final Configuration confOrg = createTestConfig();
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(confOrg);
        final String key = "new.property";
        final String value = "new value";
        confOrg.addProperty(key, value);
        assertEquals(value, conf.getString(key));
    }

    /**
     * Tries to create an immutable configuration from a null object.
     */
    @Test
    public void testUnmodifiableConfigurationNull() {
        assertThrows(NullPointerException.class, () -> ConfigurationUtils.unmodifiableConfiguration(null));
    }

    /**
     * Tests different access methods for properties.
     */
    @Test
    public void testUnmodifiableConfigurationOtherTypes() throws ConfigurationException {
        final ImmutableConfiguration conf = ConfigurationUtils.unmodifiableConfiguration(createTestConfig());
        assertEquals((byte) 10, conf.getByte("test.byte"));
        assertTrue(conf.getBoolean("test.boolean"));
        assertEquals(10.25, conf.getDouble("test.double"), .05);
        assertEquals(20.25f, conf.getFloat("test.float"), .05);
        assertEquals(10, conf.getInt("test.integer"));
        assertEquals(1000000L, conf.getLong("test.long"));
        assertEquals((short) 1, conf.getShort("test.short"));
    }

    /**
     * Tests whether an unmodifiable hierarchical configuration can be created.
     */
    @Test
    public void testUnmodifiableHierarchicalConfiguration() {
        final HierarchicalConfiguration<?> conf = new BaseHierarchicalConfiguration();
        final String key = "test";
        conf.addProperty(key, Boolean.TRUE);
        final ImmutableHierarchicalConfiguration ihc = ConfigurationUtils.unmodifiableConfiguration(conf);
        assertTrue(ihc.getBoolean(key));
        assertEquals(0, ihc.getMaxIndex(key));
    }
}
