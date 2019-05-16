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
package org.apache.commons.configuration2.io;

import static org.junit.Assert.assertNull;

import java.net.URL;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code ClasspathLocationStrategy}.
 *
 */
public class TestClasspathLocationStrategy
{
    /** Constant for a test file name. */
    private static final String FILE_NAME = "test.xml";

    /** A mock for the file system. */
    private FileSystem fileSystem;

    /** The strategy to be tested. */
    private ClasspathLocationStrategy strategy;

    @Before
    public void setUp() throws Exception
    {
        fileSystem = EasyMock.createMock(FileSystem.class);
        EasyMock.replay(fileSystem);
        strategy = new ClasspathLocationStrategy();
    }

    /**
     * Tests a successful location of a provided resource name.
     */
    @Test
    public void testLocateSuccess() throws ConfigurationException
    {
        final FileLocator locator =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME)
                        .basePath("somePath").create();
        final URL url = strategy.locate(fileSystem, locator);
        final Configurations configurations = new Configurations();
        final XMLConfiguration config1 = configurations.xml(url);
        final XMLConfiguration config2 = configurations.xml(ConfigurationAssert.getTestURL(FILE_NAME));
        ConfigurationAssert.assertConfigurationEquals(config1, config2);
    }

    /**
     * Tests a failed locate() operation.
     */
    @Test
    public void testLocateFailed()
    {
        final FileLocator locator =
                FileLocatorUtils.fileLocator()
                        .fileName("non existing resource name!").create();
        assertNull("Got a URL", strategy.locate(fileSystem, locator));
    }

    /**
     * Tests a locate() operation if no file name is provided.
     */
    @Test
    public void testLocateNoFileName()
    {
        final FileLocator locator =
                FileLocatorUtils.fileLocator().fileName("").create();
        assertNull("Got a URL", strategy.locate(fileSystem, locator));
    }
}
