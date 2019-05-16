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
package org.apache.commons.configuration2.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystem;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code FileBasedBuilderParametersImpl}.
 *
 */
public class TestFileBasedBuilderParameters
{
    /**
     * Tests the standard constructor.
     */
    @Test
    public void testInitDefaults()
    {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertFalse("Got a location", params.getFileHandler()
                .isLocationDefined());
        assertNull("Got a refresh delay", params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a file handler is accepted by the constructor.
     */
    @Test
    public void testInitFileHandler()
    {
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl(handler);
        assertSame("Wrong handler", handler, params.getFileHandler());
    }

    /**
     * Tests whether the refresh delay can be set.
     */
    @Test
    public void testSetReloadingRefreshDelay()
    {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final Long delay = 10000L;
        assertSame("Wrong result", params,
                params.setReloadingRefreshDelay(delay));
        assertEquals("Wrong delay", delay, params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a factory for reloading detectors can be set.
     */
    @Test
    public void testSetReloadingDetectorFactory()
    {
        final ReloadingDetectorFactory factory =
                EasyMock.createMock(ReloadingDetectorFactory.class);
        EasyMock.replay(factory);
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        assertNull("Got a factory", params.getReloadingDetectorFactory());
        assertSame("Wrong result", params,
                params.setReloadingDetectorFactory(factory));
        assertSame("Factory not set", factory,
                params.getReloadingDetectorFactory());
    }

    /**
     * Tests whether a file can be set.
     */
    @Test
    public void testSetFile()
    {
        final File file =
                ConfigurationAssert.getTestFile("test.properties")
                        .getAbsoluteFile();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setFile(file));
        assertEquals("Wrong file", file, params.getFileHandler().getFile());
    }

    /**
     * Tests whether a URL can be set.
     */
    @Test
    public void testSetURL()
    {
        final URL url = ConfigurationAssert.getTestURL("test.properties");
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
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
        final String path =
                ConfigurationAssert.getTestFile("test.properties")
                        .getAbsolutePath();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setPath(path));
        assertEquals("Wrong path", path, params.getFileHandler().getPath());
    }

    /**
     * Tests whether a file name can be set.
     */
    @Test
    public void testSetFileName()
    {
        final String name = "testConfig.xml";
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setFileName(name));
        assertEquals("Wrong name", name, params.getFileHandler().getFileName());
    }

    /**
     * Tests whether a base path can be set.
     */
    @Test
    public void testSetBasePath()
    {
        final String path =
                ConfigurationAssert.getTestFile("test.properties").getParentFile()
                        .getAbsolutePath();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setBasePath(path));
        assertEquals("Wrong path", path, params.getFileHandler().getBasePath());
    }

    /**
     * Tests whether a file system can be set.
     */
    @Test
    public void testSetFileSystem()
    {
        final FileSystem fs = EasyMock.createMock(FileSystem.class);
        EasyMock.replay(fs);
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setFileSystem(fs));
        assertSame("Wrong file system", fs, params.getFileHandler()
                .getFileSystem());
    }

    /**
     * Tests whether a location strategy can be set.
     */
    @Test
    public void testSetLocationStrategy()
    {
        final FileLocationStrategy strat =
                EasyMock.createMock(FileLocationStrategy.class);
        EasyMock.replay(strat);
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setLocationStrategy(strat));
        assertSame("Wrong location strategy", strat, params.getFileHandler()
                .getLocationStrategy());
    }

    /**
     * Tests whether an encoding can be set.
     */
    @Test
    public void testSetEncoding()
    {
        final String enc = "ISO-8859-1";
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame("Wrong result", params, params.setEncoding(enc));
        assertSame("Wrong encoding", enc, params.getFileHandler().getEncoding());
    }

    /**
     * Tests whether a map with parameters can be queried.
     */
    @Test
    public void testGetParameters()
    {
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl()
                        .setReloadingRefreshDelay(1000L);
        params.setThrowExceptionOnMissing(true);
        final Map<String, Object> map = params.getParameters();
        assertTrue("Object not stored", map.values().contains(params));
        assertEquals("Wrong exception flag", Boolean.TRUE, params
                .getParameters().get("throwExceptionOnMissing"));
    }

    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound()
    {
        assertNull("Got an instance",
                FileBasedBuilderParametersImpl
                        .fromParameters(new HashMap<String, Object>()));
    }

    /**
     * Tests whether fromParameters() can return a default instance if the map
     * does not contain an instance.
     */
    @Test
    public void testFromParametersDefaultInstance()
    {
        final FileBasedBuilderParametersImpl params =
                FileBasedBuilderParametersImpl.fromParameters(
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
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final Map<String, Object> map = params.getParameters();
        assertSame("Wrong parameters", params,
                FileBasedBuilderParametersImpl.fromParameters(map));
    }

    /**
     * Tries to obtain an instance from a null parameters map.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFromParametersNull()
    {
        FileBasedBuilderParametersImpl.fromParameters(null);
    }

    /**
     * Tests whether reflection-based property access through BeanUtils is
     * possible.
     */
    @Test
    public void testBeanPropertiesAccess() throws Exception
    {
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        BeanHelper.setProperty(params, "throwExceptionOnMissing",
                Boolean.TRUE);
        BeanHelper.setProperty(params, "fileName", "test.xml");
        assertEquals("File name not set", "test.xml", params.getFileHandler()
                .getFileName());
        final Map<String, Object> map = params.getParameters();
        assertEquals("Property not stored", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests a clone operation.
     */
    @Test
    public void testClone()
    {
        final FileBased content = EasyMock.createMock(FileBased.class);
        EasyMock.replay(content);
        final FileHandler fh = new FileHandler(content);
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl(fh);
        params.setThrowExceptionOnMissing(true);
        params.setFileName("test.xml");
        final FileBasedBuilderParametersImpl clone = params.clone();
        assertEquals("Wrong exception flag", Boolean.TRUE, clone
                .getParameters().get("throwExceptionOnMissing"));
        assertEquals("File name not copied", "test.xml", clone.getFileHandler()
                .getFileName());
        assertSame("Content not copied", content, clone.getFileHandler()
                .getContent());
        assertNotSame("No copy of file handler", params.getFileHandler(),
                clone.getFileHandler());
    }

    /**
     * Tests whether an instance can be created from a map.
     */
    @Test
    public void testFromMap()
    {
        final ReloadingDetectorFactory factory =
                EasyMock.createMock(ReloadingDetectorFactory.class);
        EasyMock.replay(factory);
        final Map<String, Object> map = new HashMap<>();
        final String fileName = "someFileName";
        final String basePath = "someBasePath";
        final Long refreshDelay = 20140628222302L;
        map.put("basePath", basePath);
        map.put("fileName", fileName);
        map.put("reloadingDetectorFactory", factory);
        map.put("reloadingRefreshDelay", refreshDelay);

        final FileBasedBuilderParametersImpl params =
                FileBasedBuilderParametersImpl.fromMap(map);
        assertEquals("Wrong base path", basePath, params.getFileHandler()
                .getBasePath());
        assertEquals("Wrong file name", fileName, params.getFileHandler()
                .getFileName());
        assertEquals("Wrong detector factory", factory,
                params.getReloadingDetectorFactory());
        assertEquals("Wrong refresh delay", refreshDelay,
                params.getReloadingRefreshDelay());
    }

    /**
     * Tests fromMap() for null input.
     */
    @Test
    public void testFromMapNull()
    {
        final FileBasedBuilderParametersImpl params =
                FileBasedBuilderParametersImpl.fromMap(null);
        assertNull("Got refresh delay", params.getReloadingRefreshDelay());
        assertNull("Got a file name", params.getFileHandler().getFileName());
    }

    /**
     * Tests whether properties can be inherited from another object.
     */
    @Test
    public void testInheritFrom()
    {
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        params.setEncoding("ISO-8856-1");
        params.setPath("A path");
        params.setReloadingDetectorFactory(
                EasyMock.createMock(ReloadingDetectorFactory.class));
        params.setFileSystem(EasyMock.createMock(FileSystem.class));
        params.setLocationStrategy(EasyMock.createMock(FileLocationStrategy.class));
        params.setReloadingRefreshDelay(20160213171737L);
        params.setThrowExceptionOnMissing(true);
        final FileBasedBuilderParametersImpl params2 =
                new FileBasedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        assertEquals("Encoding not set", params.getFileHandler().getEncoding(),
                params2.getFileHandler().getEncoding());
        assertEquals("File system not set",
                params.getFileHandler().getFileSystem(),
                params2.getFileHandler().getFileSystem());
        assertEquals("Location strategy not set",
                params.getFileHandler().getLocationStrategy(),
                params2.getFileHandler().getLocationStrategy());
        assertEquals("Detector factory not set",
                params.getReloadingDetectorFactory(),
                params2.getReloadingDetectorFactory());
        assertEquals("Refresh delay not set", params.getReloadingRefreshDelay(),
                params2.getReloadingRefreshDelay());
        assertNull("Path was copied", params2.getFileHandler().getPath());
        assertEquals("Base properties not set", Boolean.TRUE,
                params2.getParameters().get("throwExceptionOnMissing"));
    }

    /**
     * Tests that missing properties in the passed in map are skipped by
     * inheritFrom().
     */
    @Test
    public void testInheritFromSkipMissingProperties()
    {
        final String encoding = "UTF-16";
        final ReloadingDetectorFactory factory =
                EasyMock.createMock(ReloadingDetectorFactory.class);
        final Long refreshDelay = 20160213172611L;
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl().setEncoding(encoding)
                        .setReloadingDetectorFactory(factory)
                        .setReloadingRefreshDelay(refreshDelay);

        params.inheritFrom(
                new FileBasedBuilderParametersImpl().getParameters());
        assertEquals("Encoding overwritten", encoding,
                params.getFileHandler().getEncoding());
        assertEquals("Detector factory overwritten", factory,
                params.getReloadingDetectorFactory());
        assertEquals("Refresh delay overwritten", refreshDelay,
                params.getReloadingRefreshDelay());
    }

    /**
     * Tests inheritFrom() if no parameters object can be found in the map.
     */
    @Test
    public void testInheritFromNoParametersObject()
    {
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl()
                        .setReloadingRefreshDelay(20160213211429L);

        params.inheritFrom(new HashMap<String, Object>());
        assertNotNull("Properties were overwritten",
                params.getReloadingRefreshDelay());
    }
}
