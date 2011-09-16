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

package org.apache.commons.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import junit.framework.Assert;

/**
 * Assertions on configurations for the unit tests. This class also provides
 * access to test files.
 *
 * @author Emmanuel Bourg
 * @version $Id$
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
    public static void assertEquals(Configuration expected, Configuration actual)
    {
        // check that the actual configuration contains all the properties of the expected configuration
        for (Iterator it = expected.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            Assert.assertTrue("The actual configuration doesn't contain the expected key '" + key + "'", actual.containsKey(key));
            Assert.assertEquals("Value of the '" + key + "' property", expected.getProperty(key), actual.getProperty(key));
        }

        // check that the actual configuration has no extra properties
        for (Iterator it = actual.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            Assert.assertTrue("The actual configuration contains an extra key '" + key + "'", expected.containsKey(key));
        }
    }

    /**
     * Returns a <code>File</code> object for the specified test file.
     *
     * @param name the name of the test file
     * @return a <code>File</code> object pointing to that test file
     */
    public static File getTestFile(String name)
    {
        return new File(TEST_DIR, name);
    }

    /**
     * Returns a <code>File</code> object for the specified out file.
     *
     * @param name the name of the out file
     * @return a <code>File</code> object pointing to that out file
     */
    public static File getOutFile(String name)
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
    public static URL getTestURL(String name)
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
    public static URL getOutURL(String name)
    {
        return urlFromFile(getOutFile(name));
    }

    /**
     * Helper method for converting a file to a URL.
     *
     * @param file the file
     * @return the corresponding URL
     * @throws ConfigurationRuntimeException if the URL cannot be constructed
     */
    private static URL urlFromFile(File file)
    {
        try
        {
            return file.toURI().toURL();
        }
        catch (MalformedURLException mex)
        {
            throw new ConfigurationRuntimeException(mex);
        }
    }
}
