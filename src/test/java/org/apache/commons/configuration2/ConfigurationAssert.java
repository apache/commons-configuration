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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

/**
 * Assertions on configurations for the unit tests. This class also provides access to test files.
 */
public final class ConfigurationAssert {

    /** Constant for the name of the directory with the test files. */
    public static final String TEST_DIR_NAME = "target/test-classes";

    /** Constant for the name of the directory with the output files. */
    public static final String OUT_DIR_NAME = "target";

    /** The directory with the test files. */
    public static final File TEST_DIR = new File(TEST_DIR_NAME);

    /** The directory with the output files. */
    public static final File OUT_DIR = new File(OUT_DIR_NAME);

    /**
     * Appends all keys in the specified configuration to the given collection.
     *
     * @param config The configuration
     * @param collection The target collection
     */
    public static void appendKeys(final ImmutableConfiguration config, final Collection<String> collection) {
        for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
            collection.add(it.next());
        }
    }

    /**
     * Checks the content of a configuration.
     *
     * @param expected The expected properties
     * @param actual The configuration to check
     */
    public static void assertConfigurationEquals(final ImmutableConfiguration expected, final ImmutableConfiguration actual) {
        // check that the actual configuration contains all the properties of the expected configuration
        for (final Iterator<String> it = expected.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(actual.containsKey(key), "The actual configuration doesn't contain the expected key '" + key + "'");
            assertEquals(expected.getProperty(key), actual.getProperty(key), "Value of the '" + key + "' property");
        }

        // check that the actual configuration has no extra properties
        for (final Iterator<String> it = actual.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(expected.containsKey(key), "The actual configuration contains an extra key '" + key + "'");
        }
    }

    /**
     * Helper method for testing the equals() implementation of a class. It is also checked, whether hashCode() is
     * compatible with equals().
     *
     * @param o1 test object 1
     * @param o2 test object 2
     * @param expEquals The expected result of equals()
     */
    public static void checkEquals(final Object o1, final Object o2, final boolean expEquals) {
        assertEquals(expEquals, o1.equals(o2));
        if (o2 != null) {
            assertEquals(expEquals, o2.equals(o1));
        }
        if (expEquals) {
            assertEquals(o1.hashCode(), o2.hashCode());
        }
    }

    /**
     * Returns a {@code File} object for the specified out file.
     *
     * @param name The name of the out file
     * @return A {@code File} object pointing to that out file
     */
    public static File getOutFile(final String name) {
        return new File(OUT_DIR, name);
    }

    /**
     * Returns a URL pointing to the specified output file. If the URL cannot be constructed, a runtime exception is thrown.
     *
     * @param name The name of the output file
     * @return The corresponding URL
     */
    public static URL getOutURL(final String name) {
        return urlFromFile(getOutFile(name));
    }

    /**
     * Returns a {@code File} object for the specified test file.
     *
     * @param name The name of the test file
     * @return A {@code File} object pointing to that test file
     */
    public static File getTestFile(final String name) {
        return new File(TEST_DIR, name);
    }

    /**
     * Returns a {@code File} object for the specified test file.
     *
     * @param name The name of the test file
     * @return A {@code File} object pointing to that test file
     */
    public static Path getTestPath(final String name) {
        return TEST_DIR.toPath().resolve(name);
    }

    /**
     * Returns a URL pointing to the specified test file. If the URL cannot be constructed, a runtime exception is thrown.
     *
     * @param name The name of the test file
     * @return The corresponding URL
     */
    public static URL getTestURL(final String name) {
        return urlFromFile(getTestFile(name));
    }

    /**
     * Returns a list with all keys defined for the specified configuration.
     *
     * @param config The configuration
     * @return A list with all keys of this configuration
     */
    public static List<String> keysToList(final ImmutableConfiguration config) {
        final List<String> keyList = new LinkedList<>();
        appendKeys(config, keyList);
        return keyList;
    }

    /**
     * Returns a set with all keys defined for the specified configuration.
     *
     * @param config The configuration
     * @return A set with all keys of this configuration
     */
    public static Set<String> keysToSet(final ImmutableConfiguration config) {
        final Set<String> keySet = new HashSet<>();
        appendKeys(config, keySet);
        return keySet;
    }

    /**
     * Helper method for converting a file to a URL.
     *
     * @param file The file
     * @return The corresponding URL
     * @throws ConfigurationRuntimeException if the URL cannot be constructed
     */
    private static URL urlFromFile(final File file) {
        try {
            return file.toURI().toURL();
        } catch (final MalformedURLException mex) {
            throw new ConfigurationRuntimeException(mex);
        }
    }

    private ConfigurationAssert() {
        // empty
    }
}
