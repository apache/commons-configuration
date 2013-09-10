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
package org.apache.commons.configuration.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

/**
 * Test class for {@code FileLocatorUtils}.
 *
 * @version $Id: $
 */
public class TestFileLocatorUtils
{
    /**
     * Tests whether an encoded "%" character in the file name is handled correctly by
     * fileFromURL(). This test is related to CONFIGURATION-521.
     */
    @Test
    public void testFileFromURLWithEncodedPercent() throws MalformedURLException
    {
        File file = new File("https%3A%2F%2Fwww.apache.org%2F.url").getAbsoluteFile();
        URL fileURL = file.toURI().toURL();
        File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals("Wrong file", file, file2);
    }

    /**
     * Tests whether a "+" character in the file name is handled correctly by
     * fileFromURL(). This test is related to CONFIGURATION-415.
     */
    @Test
    public void testFileFromURLWithPlus() throws MalformedURLException
    {
        File file = new File(new File("target"), "foo+bar.txt")
                .getAbsoluteFile();
        URL fileURL = file.toURI().toURL();
        File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals("Wrong file", file, file2);
    }

    /**
     * Tests whether fileFromURL() handles null URLs correctly.
     */
    @Test
    public void testFileFromURLNull() throws Exception
    {
        assertNull("Wrong file for null URL", FileLocatorUtils
                .fileFromURL(null));
    }

    @Test
    public void testGetBasePath() throws Exception
    {
        URL url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("base path of " + url, "http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo/");
        assertEquals("base path of " + url, "http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo");
        assertEquals("base path of " + url, "http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/");
        assertEquals("base path of " + url, "http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net");
        assertEquals("base path of " + url, "http://xyz.net", FileLocatorUtils.getBasePath(url));
    }

    @Test
    public void testGetFileName() throws Exception
    {
        assertEquals("file name for a null URL", null, FileLocatorUtils.getFileName(null));

        URL url = new URL("http://xyz.net/foo/");
        assertEquals("file for a directory URL " + url, null, FileLocatorUtils.getFileName(url));

        url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("file name for a valid URL " + url, "bar.xml", FileLocatorUtils.getFileName(url));
    }

    @Test
    public void testGetFile() throws Exception
    {
        File directory = new File("target");
        File reference = new File(directory, "test.txt").getAbsoluteFile();

        assertEquals(reference, FileLocatorUtils.getFile(null, reference.getAbsolutePath()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getAbsolutePath()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getName()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.toURI().toURL().toString(), reference.getName()));
        assertEquals(reference, FileLocatorUtils.getFile("invalid", reference.toURI().toURL().toString()));
        assertEquals(reference, FileLocatorUtils.getFile(
                "jar:file:/C:/myjar.jar!/my-config.xml/someprops.properties",
                reference.getAbsolutePath()));
    }

    @Test
    public void testLocateWithNullTCCL() throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(FileLocatorUtils.locate(new DefaultFileSystem(), "abase", "aname"));
            // This assert fails when maven 2 is used, so commented out
            //assertNotNull(FileLocatorUtils.locate("test.xml"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
