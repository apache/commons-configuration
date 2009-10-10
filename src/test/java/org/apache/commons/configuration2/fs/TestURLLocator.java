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
package org.apache.commons.configuration2.fs;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code URLLocator}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestURLLocator
{
    /** Constant for a test URL. */
    private static final String URL1 = "http://www.apache.org";

    /** Constant for another test URL. */
    private static final String URL2 = "http://commons.apache.org";

    /** A test input URL. */
    private URL inputUrl;

    /** A test output URL. */
    private URL outputUrl;

    @Before
    public void setUp() throws Exception
    {
        inputUrl = new URL(URL1);
        outputUrl = new URL(URL2);
    }

    /**
     * Helper method for checking whether two URLs are equal.
     *
     * @param url1 URL1
     * @param url2 URL2
     */
    private static void equalsURLs(URL url1, URL url2)
    {
        assertEquals("URLs not equal", url1.toExternalForm(), url2
                .toExternalForm());
    }

    /**
     * Tries to create an instance with a null input URL. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNullInput()
    {
        new URLLocator(null, outputUrl);
    }

    /**
     * Tries to create an instance with a null output URL. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNullOutput()
    {
        new URLLocator(inputUrl, null);
    }

    /**
     * Tests the getURL() method if only a single URL is provided.
     */
    @Test
    public void testGetURLSingle()
    {
        URLLocator loc = new URLLocator(inputUrl);
        equalsURLs(inputUrl, loc.getURL(false));
        equalsURLs(inputUrl, loc.getURL(true));
    }

    /**
     * Tests the getURL() method if both URLs are provided.
     */
    @Test
    public void testGetURLInOut()
    {
        URLLocator loc = new URLLocator(inputUrl, outputUrl);
        equalsURLs(inputUrl, loc.getURL(false));
        equalsURLs(outputUrl, loc.getURL(true));
    }

    /**
     * Tests the string representation of the locator.
     */
    @Test
    public void testToString()
    {
        URLLocator loc = new URLLocator(inputUrl, outputUrl);
        String s = loc.toString();
        assertEquals("Wrong string", "URLLocator [ inputURL = "
                + inputUrl.toExternalForm() + ", outputURL = "
                + outputUrl.toExternalForm() + " ]", s);
    }

    /**
     * Tests equals() if the expected result is true.
     */
    @Test
    public void testEqualsTrue()
    {
        URLLocator loc1 = new URLLocator(inputUrl, outputUrl);
        ConfigurationAssert.checkEquals(loc1, loc1, true);
        URLLocator loc2 = new URLLocator(inputUrl, outputUrl);
        ConfigurationAssert.checkEquals(loc1, loc2, true);
    }

    /**
     * Tests equals() if the expected result is false.
     */
    @Test
    public void testEqualsFalse()
    {
        URLLocator loc1 = new URLLocator(inputUrl, outputUrl);
        URLLocator loc2 = new URLLocator(inputUrl);
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = new URLLocator(outputUrl);
        ConfigurationAssert.checkEquals(loc1, loc2, false);
    }

    /**
     * Tests equals() with objects from other classes.
     */
    @Test
    public void testEqualsOtherClass()
    {
        URLLocator loc = new URLLocator(inputUrl);
        ConfigurationAssert.checkEquals(loc, null, false);
        ConfigurationAssert.checkEquals(loc, this, false);
    }
}
