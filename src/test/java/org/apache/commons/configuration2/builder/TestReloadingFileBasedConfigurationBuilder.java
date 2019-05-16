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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.FileHandlerReloadingDetector;
import org.apache.commons.configuration2.reloading.ReloadingDetector;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code ReloadingFileBasedConfigurationBuilder}.
 *
 */
public class TestReloadingFileBasedConfigurationBuilder
{
    /**
     * Tests whether a configuration can be created if no location is set. This
     * tests also ensures that the super constructor is called correctly.
     */
    @Test
    public void testGetConfigurationNoLocation() throws ConfigurationException
    {
        final Map<String, Object> params = new HashMap<>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class, params);
        final PropertiesConfiguration conf = builder.getConfiguration();
        assertTrue("Property not set", conf.isThrowExceptionOnMissing());
        assertTrue("Not empty", conf.isEmpty());
    }

    /**
     * Tests whether a correct reloading detector is created if no custom factory
     * was set.
     */
    @Test
    public void testCreateReloadingDetectorDefaultFactory() throws ConfigurationException
    {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final long refreshDelay = 60000L;
        params.setReloadingRefreshDelay(refreshDelay);
        final FileHandlerReloadingDetector detector =
                (FileHandlerReloadingDetector) builder.createReloadingDetector(
                        handler, params);
        assertSame("Wrong file handler", handler, detector.getFileHandler());
        assertEquals("Wrong refresh delay", refreshDelay,
                detector.getRefreshDelay());
    }

    /**
     * Tests whether a custom reloading detector factory can be installed.
     */
    @Test
    public void testCreateReloadingDetectoryCustomFactory()
            throws ConfigurationException
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        final ReloadingDetectorFactory factory =
                EasyMock.createMock(ReloadingDetectorFactory.class);
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl();
        EasyMock.expect(factory.createReloadingDetector(handler, params))
                .andReturn(detector);
        EasyMock.replay(detector, factory);
        params.setReloadingDetectorFactory(factory);
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        assertSame("Wrong detector", detector,
                builder.createReloadingDetector(handler, params));
        EasyMock.verify(factory);
    }

    /**
     * Tests the isReloadingRequired() implementation of the detector associated
     * with the reloading controller.
     */
    @Test
    public void testReloadingDetectorIsReloadingRequired()
            throws ConfigurationException
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        EasyMock.expect(detector.isReloadingRequired())
                .andReturn(Boolean.FALSE);
        EasyMock.replay(detector);
        final ReloadingFileBasedConfigurationBuilderTestImpl builder =
                new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        builder.getConfiguration();
        final ReloadingDetector ctrlDetector =
                builder.getReloadingController().getDetector();
        assertTrue("Wrong result (1)", ctrlDetector.isReloadingRequired());
        assertFalse("Wrong result (2)", ctrlDetector.isReloadingRequired());
        assertSame("Wrong file handler", builder.getFileHandler(),
                builder.getHandlerForDetector());
        EasyMock.verify(detector);
    }

    /**
     * Tests the reloadingPerformed() implementation of the detector associated
     * with the reloading controller.
     */
    @Test
    public void testReloadingDetectorReloadingPerformed()
            throws ConfigurationException
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        detector.reloadingPerformed();
        EasyMock.replay(detector);
        final ReloadingFileBasedConfigurationBuilderTestImpl builder =
                new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        builder.getConfiguration();
        final ReloadingDetector ctrlDetector =
                builder.getReloadingController().getDetector();
        ctrlDetector.reloadingPerformed();
        EasyMock.verify(detector);
    }

    /**
     * Tests the behavior of the reloading detector if no underlying detector is
     * available.
     */
    @Test
    public void testReloadingDetectorNoFileHandler()
    {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final ReloadingDetector ctrlDetector =
                builder.getReloadingController().getDetector();
        ctrlDetector.reloadingPerformed();
        assertFalse("Wrong result", ctrlDetector.isReloadingRequired());
    }

    /**
     * Tests whether the controller's reloading state is reset when a new result
     * configuration is created.
     */
    @Test
    public void testResetReloadingStateInGetConfiguration()
            throws ConfigurationException
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        detector.reloadingPerformed();
        EasyMock.replay(detector);
        final ReloadingFileBasedConfigurationBuilderTestImpl builder =
                new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        final PropertiesConfiguration config1 = builder.getConfiguration();
        builder.getReloadingController().checkForReloading(null);
        final PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("No new configuration instance", config1, config2);
        assertFalse("Still in reloading state", builder
                .getReloadingController().isInReloadingState());
        EasyMock.verify(detector);
    }

    /**
     * Tests whether this builder reacts on events fired by the reloading
     * controller.
     */
    @Test
    public void testReloadingControllerEvents() throws ConfigurationException
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        final ReloadingFileBasedConfigurationBuilderTestImpl builder =
                new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        EasyMock.replay(detector);
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        builder.getConfiguration();
        builder.getReloadingController().checkForReloading(null);
        listener.nextEvent(ConfigurationBuilderEvent.RESET);
        listener.assertNoMoreEvents();
        EasyMock.verify(detector);
    }

    /**
     * Tests whether the allowFailOnInit flag is correctly initialized.
     */
    @Test
    public void testInitAllowFailOnInitFlag()
    {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new ReloadingFileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class, null, true);
        assertTrue("Flag not set", builder.isAllowFailOnInit());
    }

    /**
     * A test builder implementation which allows mocking the underlying
     * reloading detector.
     */
    private static class ReloadingFileBasedConfigurationBuilderTestImpl extends
            ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration>
    {
        /** The mock for the reloading detector. */
        private final ReloadingDetector mockDetector;

        /** Stores the file handler passed to createReloadingDetector(). */
        private FileHandler handlerForDetector;

        /**
         * Creates a new instance of
         * {@code ReloadingFileBasedConfigurationBuilderTestImpl} and
         * initializes it with a mock reloading detector.
         *
         * @param detector the mock detector
         */
        public ReloadingFileBasedConfigurationBuilderTestImpl(
                final ReloadingDetector detector)
        {
            super(PropertiesConfiguration.class);
            mockDetector = detector;
        }

        /**
         * Returns the file handler that was passed to
         * createReloadingDetector().
         *
         * @return the file handler
         */
        public FileHandler getHandlerForDetector()
        {
            return handlerForDetector;
        }

        /**
         * Returns the mock file handler.
         */
        @Override
        protected ReloadingDetector createReloadingDetector(
                final FileHandler handler, final FileBasedBuilderParametersImpl fbparams)
        {
            handlerForDetector = handler;
            return mockDetector;
        }
    }
}
