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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystem;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code FileBasedBuilderParametersImpl}.
 */
public class TestFileBasedBuilderParameters {
    /**
     * Tests whether reflection-based property access through BeanUtils is possible.
     */
    @Test
    public void testBeanPropertiesAccess() throws Exception {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        BeanHelper.setProperty(params, "throwExceptionOnMissing", Boolean.TRUE);
        BeanHelper.setProperty(params, "fileName", "test.xml");
        assertEquals("test.xml", params.getFileHandler().getFileName());
        final Map<String, Object> map = params.getParameters();
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests a clone operation.
     */
    @Test
    public void testClone() {
        final FileBased content = mock(FileBased.class);
        final FileHandler fh = new FileHandler(content);
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl(fh);
        params.setThrowExceptionOnMissing(true);
        params.setFileName("test.xml");
        final FileBasedBuilderParametersImpl clone = params.clone();
        assertEquals(Boolean.TRUE, clone.getParameters().get("throwExceptionOnMissing"));
        assertEquals("test.xml", clone.getFileHandler().getFileName());
        assertSame(content, clone.getFileHandler().getContent());
        assertNotSame(params.getFileHandler(), clone.getFileHandler());
    }

    /**
     * Tests whether an instance can be created from a map.
     */
    @Test
    public void testFromMap() {
        final ReloadingDetectorFactory factory = mock(ReloadingDetectorFactory.class);
        final Map<String, Object> map = new HashMap<>();
        final String fileName = "someFileName";
        final String basePath = "someBasePath";
        final Long refreshDelay = 20140628222302L;
        map.put("basePath", basePath);
        map.put("fileName", fileName);
        map.put("reloadingDetectorFactory", factory);
        map.put("reloadingRefreshDelay", refreshDelay);

        final FileBasedBuilderParametersImpl params = FileBasedBuilderParametersImpl.fromMap(map);
        assertEquals(basePath, params.getFileHandler().getBasePath());
        assertEquals(fileName, params.getFileHandler().getFileName());
        assertEquals(factory, params.getReloadingDetectorFactory());
        assertEquals(refreshDelay, params.getReloadingRefreshDelay());
    }

    /**
     * Tests fromMap() for null input.
     */
    @Test
    public void testFromMapNull() {
        final FileBasedBuilderParametersImpl params = FileBasedBuilderParametersImpl.fromMap(null);
        assertNull(params.getReloadingRefreshDelay());
        assertNull(params.getFileHandler().getFileName());
    }

    /**
     * Tests whether fromParameters() can return a default instance if the map does not contain an instance.
     */
    @Test
    public void testFromParametersDefaultInstance() {
        final FileBasedBuilderParametersImpl params = FileBasedBuilderParametersImpl.fromParameters(new HashMap<>(), true);
        assertFalse(params.getFileHandler().isLocationDefined());
    }

    /**
     * Tests whether an instance can be extracted from a parameters map.
     */
    @Test
    public void testFromParametersExtract() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final Map<String, Object> map = params.getParameters();
        assertSame(params, FileBasedBuilderParametersImpl.fromParameters(map));
    }

    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound() {
        assertNull(FileBasedBuilderParametersImpl.fromParameters(new HashMap<>()));
    }

    /**
     * Tries to obtain an instance from a null parameters map.
     */
    @Test
    public void testFromParametersNull() {
        assertThrows(IllegalArgumentException.class, () -> FileBasedBuilderParametersImpl.fromParameters(null));
    }

    /**
     * Tests whether a map with parameters can be queried.
     */
    @Test
    public void testGetParameters() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl().setReloadingRefreshDelay(1000L);
        params.setThrowExceptionOnMissing(true);
        final Map<String, Object> map = params.getParameters();
        assertTrue(map.containsValue(params));
        assertEquals(Boolean.TRUE, params.getParameters().get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether properties can be inherited from another object.
     */
    @Test
    public void testInheritFrom() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        params.setEncoding("ISO-8856-1");
        params.setPath("A path");
        params.setReloadingDetectorFactory(mock(ReloadingDetectorFactory.class));
        params.setFileSystem(mock(FileSystem.class));
        params.setLocationStrategy(mock(FileLocationStrategy.class));
        params.setReloadingRefreshDelay(20160213171737L);
        params.setThrowExceptionOnMissing(true);
        final FileBasedBuilderParametersImpl params2 = new FileBasedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        assertEquals(params.getFileHandler().getEncoding(), params2.getFileHandler().getEncoding());
        assertEquals(params.getFileHandler().getFileSystem(), params2.getFileHandler().getFileSystem());
        assertEquals(params.getFileHandler().getLocationStrategy(), params2.getFileHandler().getLocationStrategy());
        assertEquals(params.getReloadingDetectorFactory(), params2.getReloadingDetectorFactory());
        assertEquals(params.getReloadingRefreshDelay(), params2.getReloadingRefreshDelay());
        assertNull(params2.getFileHandler().getPath());
        assertEquals(Boolean.TRUE, params2.getParameters().get("throwExceptionOnMissing"));
    }

    /**
     * Tests inheritFrom() if no parameters object can be found in the map.
     */
    @Test
    public void testInheritFromNoParametersObject() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl().setReloadingRefreshDelay(20160213211429L);

        params.inheritFrom(new HashMap<>());
        assertNotNull(params.getReloadingRefreshDelay());
    }

    /**
     * Tests that missing properties in the passed in map are skipped by inheritFrom().
     */
    @Test
    public void testInheritFromSkipMissingProperties() {
        final String encoding = StandardCharsets.UTF_16.name();
        final ReloadingDetectorFactory factory = mock(ReloadingDetectorFactory.class);
        final Long refreshDelay = 20160213172611L;
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl().setEncoding(encoding).setReloadingDetectorFactory(factory)
            .setReloadingRefreshDelay(refreshDelay);

        params.inheritFrom(new FileBasedBuilderParametersImpl().getParameters());
        assertEquals(encoding, params.getFileHandler().getEncoding());
        assertEquals(factory, params.getReloadingDetectorFactory());
        assertEquals(refreshDelay, params.getReloadingRefreshDelay());
    }

    /**
     * Tests the standard constructor.
     */
    @Test
    public void testInitDefaults() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertFalse(params.getFileHandler().isLocationDefined());
        assertNull(params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a file handler is accepted by the constructor.
     */
    @Test
    public void testInitFileHandler() {
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl(handler);
        assertSame(handler, params.getFileHandler());
    }

    /**
     * Tests whether a base path can be set.
     */
    @Test
    public void testSetBasePath() {
        final String path = ConfigurationAssert.getTestFile("test.properties").getParentFile().getAbsolutePath();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setBasePath(path));
        assertEquals(path, params.getFileHandler().getBasePath());
    }

    /**
     * Tests whether an encoding can be set.
     */
    @Test
    public void testSetEncoding() {
        final String enc = StandardCharsets.ISO_8859_1.name();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setEncoding(enc));
        assertSame(enc, params.getFileHandler().getEncoding());
    }

    /**
     * Tests whether a file can be set.
     */
    @Test
    public void testSetFile() {
        final File file = ConfigurationAssert.getTestFile("test.properties").getAbsoluteFile();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setFile(file));
        assertEquals(file, params.getFileHandler().getFile());
    }

    /**
     * Tests whether a file name can be set.
     */
    @Test
    public void testSetFileName() {
        final String name = "testConfig.xml";
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setFileName(name));
        assertEquals(name, params.getFileHandler().getFileName());
    }

    /**
     * Tests whether a file system can be set.
     */
    @Test
    public void testSetFileSystem() {
        final FileSystem fs = mock(FileSystem.class);
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setFileSystem(fs));
        assertSame(fs, params.getFileHandler().getFileSystem());
    }

    /**
     * Tests whether a location strategy can be set.
     */
    @Test
    public void testSetLocationStrategy() {
        final FileLocationStrategy strat = mock(FileLocationStrategy.class);
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setLocationStrategy(strat));
        assertSame(strat, params.getFileHandler().getLocationStrategy());
    }

    /**
     * Tests whether a path can be set.
     */
    @Test
    public void testSetPath() {
        final String path = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setPath(path));
        assertEquals(path, params.getFileHandler().getPath());
    }

    /**
     * Tests whether a factory for reloading detectors can be set.
     */
    @Test
    public void testSetReloadingDetectorFactory() {
        final ReloadingDetectorFactory factory = mock(ReloadingDetectorFactory.class);
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertNull(params.getReloadingDetectorFactory());
        assertSame(params, params.setReloadingDetectorFactory(factory));
        assertSame(factory, params.getReloadingDetectorFactory());
    }

    /**
     * Tests whether the refresh delay can be set.
     */
    @Test
    public void testSetReloadingRefreshDelay() {
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final Long delay = 10000L;
        assertSame(params, params.setReloadingRefreshDelay(delay));
        assertEquals(delay, params.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a URL can be set.
     */
    @Test
    public void testSetURL() {
        final URL url = ConfigurationAssert.getTestURL("test.properties");
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        assertSame(params, params.setURL(url));
        assertEquals(url.toExternalForm(), params.getFileHandler().getURL().toExternalForm());
    }
}
