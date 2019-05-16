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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.FileHandlerReloadingDetector;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code DefaultReloadingDetectorFactory}.
 *
 */
public class TestDefaultReloadingDetectorFactory
{
    /** The factory to be tested. */
    private DefaultReloadingDetectorFactory factory;

    @Before
    public void setUp() throws Exception
    {
        factory = new DefaultReloadingDetectorFactory();
    }

    /**
     * Tests whether a reloading detector is created correctly.
     */
    @Test
    public void testCreateReloadingDetector() throws ConfigurationException
    {
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        final Long refreshDelay = 10000L;
        params.setReloadingRefreshDelay(refreshDelay);
        final FileHandlerReloadingDetector detector =
                (FileHandlerReloadingDetector) factory.createReloadingDetector(
                        handler, params);
        assertSame("Wrong file handler", handler, detector.getFileHandler());
        assertEquals("Wrong refresh delay", refreshDelay.longValue(),
                detector.getRefreshDelay());
    }

    /**
     * Tests whether an undefined refresh delay is handled correctly.
     */
    @Test
    public void testCreateReloadingDetectorDefaultRefreshDelay()
            throws ConfigurationException
    {
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        final FileHandlerReloadingDetector detector =
                (FileHandlerReloadingDetector) factory.createReloadingDetector(
                        handler, params);
        assertTrue("No default refresh delay", detector.getRefreshDelay() != 0);
    }
}
