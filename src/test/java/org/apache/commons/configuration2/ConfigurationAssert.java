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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

/**
 * Assertions on configurations for the unit tests. This class also provides
 * access to test files.
 *
 * @author Emmanuel Bourg
 */
public class ConfigurationAssert
{
    /** Constant for the name of the directory with the test files. */
    public static final String TEST_DIR_NAME = "target/test-classes";

    /** Constant for the name of the directory with the output files. */
    public static final String OUT_DIR_NAME = "target";

    /** The directory with the test files. */
    public static final File TEST_DIR = new File(TEST_DIR_NAME);

    /** The directory with the output files. */
    public static final File OUT_DIR = new File(OUT_DIR_NAME);

    /**
     * Checks the content of a configuration.
     *
     * @param expected the expected properties
     * @param actual the configuration to check
     */
    public static void assertConfigurationEquals(final ImmutableConfiguration expected, final ImmutableConfiguration actual)
    {
        // check that the actual configuration contains all the properties of the expected configuration
        for (final Iterator<String> it = expected.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertTrue("The actual configuration doesn't contain the expected key '" + key + "'", actual.containsKey(key));
            assertEquals("Value of the '" + key + "' property", expected.getProperty(key), actual.getProperty(key));
        }

        // check that the actual configuration has no extra properties
        for (final Iterator<String> it = actual.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertTrue("The actual configuration contains an extra key '" + key + "'", expected.containsKey(key));
        }
    }

    /**
     * Returns a {@code File} object for the specified test file.
     *
     * @param name the name of the test file
     * @return a {@code File} object pointing to that test file
     */
    public static File getTestFile(final String name)
    {
        return new File(TEST_DIR, name);
    }

    /**
     * Returns a {@code File} object for the specified out file.
     *
     * @param name the name of the out file
     * @return a {@code File} object pointing to that out file
     */
    public static File getOutFile(final String name)
    {
        return new File(OUT_DIR, name);
    }

    /**
     * Returns a URL pointing to the specified test file. If the URL cannot be
     * constructed, a runtime exception is thrown.
     *
     * @param name the name of the test file
     * @return the corresponding URL
     */
    public static URL getTestURL(final String name)
    {
        return urlFromFile(getTestFile(name));
    }

    /**
     * Returns a URL pointing to the specified output file. If the URL cannot be
     * constructed, a runtime exception is thrown.
     *
     * @param name the name of the output file
     * @return the corresponding URL
     */
    public static URL getOutURL(final String name)
    {
        return urlFromFile(getOutFile(name));
    }

    /**
     * Helper method for testing the equals() implementation of a class. It is
     * also checked, whether hashCode() is compatible with equals().
     *
     * @param o1 test object 1
     * @param o2 test object 2
     * @param expEquals the expected result of equals()
     */
    public static void checkEquals(final Object o1, final Object o2, final boolean expEquals)
    {
        assertEquals("Wrong result of equals()", expEquals, o1.equals(o2));
        if (o2 != null)
        {
            assertEquals("Not symmetric", expEquals, o2.equals(o1));
        }
        if (expEquals)
        {
            assertEquals("Different hash codes", o1.hashCode(), o2.hashCode());
        }
    }

    /**
     * Returns a list with all keys defined for the specified configuration.
     *
     * @param config the configuration
     * @return a list with all keys of this configuration
     */
    public static List<String> keysToList(final ImmutableConfiguration config)
    {
        final List<String> keyList = new LinkedList<>();
        appendKeys(config, keyList);
        return keyList;
    }

    /**
     * Returns a set with all keys defined for the specified configuration.
     *
     * @param config the configuration
     * @return a set with all keys of this configuration
     */
    public static Set<String> keysToSet(final ImmutableConfiguration config)
    {
        final Set<String> keySet = new HashSet<>();
        appendKeys(config, keySet);
        return keySet;
    }

    /**
     * Appends all keys in the specified configuration to the given collection.
     *
     * @param config the configuration
     * @param collection the target collection
     */
    public static void appendKeys(final ImmutableConfiguration config,
            final Collection<String> collection)
    {
        for (final Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            collection.add(it.next());
        }
    }

    /**
     * Helper method for converting a file to a URL.
     *
     * @param file the file
     * @return the corresponding URL
     * @throws ConfigurationRuntimeException if the URL cannot be constructed
     */
    private static URL urlFromFile(final File file)
    {
        try
        {
            return file.toURI().toURL();
        }
        catch (final MalformedURLException mex)
        {
            throw new ConfigurationRuntimeException(mex);
        }
    }
}
