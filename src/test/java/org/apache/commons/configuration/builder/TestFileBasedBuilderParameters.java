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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.io.FileHandler;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code FileBasedBuilderParameters}.
 *
 * @version $Id$
 */
public class TestFileBasedBuilderParameters
{
    /**
     * Tests the standard constructor.
     */
    @Test
    public void testInitDefaults()
    {
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertFalse("Got a location", params.getFileHandler()
                .isLocationDefined());
        assertEquals("Got a refresh delay", 0,
                params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a file handler is accepted by the constructor.
     */
    @Test
    public void testInitFileHandler()
    {
        FileHandler handler = new FileHandler();
        FileBasedBuilderParameters params =
                new FileBasedBuilderParameters(handler);
        assertSame("Wrong handler", handler, params.getFileHandler());
    }

    /**
     * Tests whether the refresh delay can be set.
     */
    @Test
    public void testSetReloadingRefreshDelay()
    {
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        final int delay = 10000;
        assertSame("Wrong result", params,
                params.setReloadingRefreshDelay(delay));
        assertEquals("Wrong delay", delay, params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a file can be set.
     */
    @Test
    public void testSetFile()
    {
        File file =
                ConfigurationAssert.getTestFile("test.properties")
                        .getAbsoluteFile();
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setFile(file));
        assertEquals("Wrong file", file, params.getFileHandler().getFile());
    }

    /**
     * Tests whether a URL can be set.
     */
    @Test
    public void testSetURL()
    {
        URL url = ConfigurationAssert.getTestURL("test.properties");
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setURL(url));
        assertEquals("Wrong URL", url.toExternalForm(), params.getFileHandler()
                .getURL().toExternalForm());
    }

    /**
     * Tests whether a path can be set.
     */
    @Test
    public void testSetPath()
    {
        String path =
                ConfigurationAssert.getTestFile("test.properties")
                        .getAbsolutePath();
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setPath(path));
        assertEquals("Wrong path", path, params.getFileHandler().getPath());
    }

    /**
     * Tests whether a file name can be set.
     */
    @Test
    public void testSetFileName()
    {
        String name = "testConfig.xml";
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setFileName(name));
        assertEquals("Wrong name", name, params.getFileHandler().getFileName());
    }

    /**
     * Tests whether a base path can be set.
     */
    @Test
    public void testSetBasePath()
    {
        String path =
                ConfigurationAssert.getTestFile("test.properties").getParentFile()
                        .getAbsolutePath();
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setBasePath(path));
        assertEquals("Wrong path", path, params.getFileHandler().getBasePath());
    }

    /**
     * Tests whether a file system can be set.
     */
    @Test
    public void testSetFileSystem()
    {
        FileSystem fs = EasyMock.createMock(FileSystem.class);
        EasyMock.replay(fs);
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setFileSystem(fs));
        assertSame("Wrong file system", fs, params.getFileHandler()
                .getFileSystem());
    }

    /**
     * Tests whether an encoding can be set.
     */
    @Test
    public void testSetEncoding()
    {
        String enc = "ISO-8859-1";
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        assertSame("Wrong result", params, params.setEncoding(enc));
        assertSame("Wrong encoding", enc, params.getFileHandler().getEncoding());
    }

    /**
     * Tests whether a map with parameters can be queried.
     */
    @Test
    public void testGetParameters()
    {
        FileBasedBuilderParameters params =
                new FileBasedBuilderParameters().setReloadingRefreshDelay(1000);
        Map<String, Object> map = params.getParameters();
        assertEquals("Wrong number of items", 1, map.size());
        assertTrue("Object not stored", map.values().contains(params));
    }

    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound()
    {
        assertNull("Got an instance",
                FileBasedBuilderParameters
                        .fromParameters(new HashMap<String, Object>()));
    }

    /**
     * Tests whether fromParameters() can return a default instance if the map
     * does not contain an instance.
     */
    @Test
    public void testFromParametersDefaultInstance()
    {
        FileBasedBuilderParameters params =
                FileBasedBuilderParameters.fromParameters(
                        new HashMap<String, Object>(), true);
        assertFalse("Got a location", params.getFileHandler()
                .isLocationDefined());
    }

    /**
     * Tests whether an instance can be extracted from a parameters map.
     */
    @Test
    public void testFromParametersExtract()
    {
        FileBasedBuilderParameters params = new FileBasedBuilderParameters();
        Map<String, Object> map = params.getParameters();
        assertSame("Wrong parameters", params,
                FileBasedBuilderParameters.fromParameters(map));
    }
}
