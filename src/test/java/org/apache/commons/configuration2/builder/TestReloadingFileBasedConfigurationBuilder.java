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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.FileHandlerReloadingDetector;
import org.apache.commons.configuration2.reloading.ReloadingDetector;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code ReloadingFileBasedConfigurationBuilder}.
 */
public class TestReloadingFileBasedConfigurationBuilder {
    /**
     * A test builder implementation which allows mocking the underlying reloading detector.
     */
    private static final class ReloadingFileBasedConfigurationBuilderTestImpl extends ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> {
        /** The mock for the reloading detector. */
        private final ReloadingDetector mockDetector;

        /** Stores the file handler passed to createReloadingDetector(). */
        private FileHandler handlerForDetector;

        /**
         * Creates a new instance of {@code ReloadingFileBasedConfigurationBuilderTestImpl} and initializes it with a mock
         * reloading detector.
         *
         * @param detector the mock detector
         */
        public ReloadingFileBasedConfigurationBuilderTestImpl(final ReloadingDetector detector) {
            super(PropertiesConfiguration.class);
            mockDetector = detector;
        }

        /**
         * Returns the mock file handler.
         */
        @Override
        protected ReloadingDetector createReloadingDetector(final FileHandler handler, final FileBasedBuilderParametersImpl fbparams) {
            handlerForDetector = handler;
            return mockDetector;
        }

        /**
         * Returns the file handler that was passed to createReloadingDetector().
         *
         * @return the file handler
         */
        public FileHandler getHandlerForDetector() {
            return handlerForDetector;
        }
    }

    /**
     * Tests whether a correct reloading detector is created if no custom factory was set.
     */
    @Test
    public void testCreateReloadingDetectorDefaultFactory() throws ConfigurationException {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
            PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();
        final long refreshDelay = 60000L;
        params.setReloadingRefreshDelay(refreshDelay);
        final FileHandlerReloadingDetector detector = (FileHandlerReloadingDetector) builder.createReloadingDetector(handler, params);
        assertSame(handler, detector.getFileHandler());
        assertEquals(refreshDelay, detector.getRefreshDelay());
    }

    /**
     * Tests whether a custom reloading detector factory can be installed.
     */
    @Test
    public void testCreateReloadingDetectoryCustomFactory() throws ConfigurationException {
        final ReloadingDetector detector = mock(ReloadingDetector.class);
        final ReloadingDetectorFactory factory = mock(ReloadingDetectorFactory.class);
        final FileHandler handler = new FileHandler();
        final FileBasedBuilderParametersImpl params = new FileBasedBuilderParametersImpl();

        when(factory.createReloadingDetector(handler, params)).thenReturn(detector);

        params.setReloadingDetectorFactory(factory);
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
            PropertiesConfiguration.class);
        assertSame(detector, builder.createReloadingDetector(handler, params));

        verify(factory).createReloadingDetector(handler, params);
        verifyNoMoreInteractions(factory);
    }

    /**
     * Tests whether a configuration can be created if no location is set. This tests also ensures that the super
     * constructor is called correctly.
     */
    @Test
    public void testGetConfigurationNoLocation() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
            PropertiesConfiguration.class, params);
        final PropertiesConfiguration conf = builder.getConfiguration();
        assertTrue(conf.isThrowExceptionOnMissing());
        assertTrue(conf.isEmpty());
    }

    /**
     * Tests whether the allowFailOnInit flag is correctly initialized.
     */
    @Test
    public void testInitAllowFailOnInitFlag() {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
            PropertiesConfiguration.class, null, true);
        assertTrue(builder.isAllowFailOnInit());
    }

    /**
     * Tests whether this builder reacts on events fired by the reloading controller.
     */
    @Test
    public void testReloadingControllerEvents() throws ConfigurationException {
        final ReloadingDetector detector = mock(ReloadingDetector.class);

        when(detector.isReloadingRequired()).thenReturn(Boolean.TRUE);

        final ReloadingFileBasedConfigurationBuilderTestImpl builder = new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        builder.getConfiguration();
        builder.getReloadingController().checkForReloading(null);
        listener.nextEvent(ConfigurationBuilderEvent.RESET);
        listener.assertNoMoreEvents();

        verify(detector).isReloadingRequired();
        verifyNoMoreInteractions(detector);
    }

    /**
     * Tests the isReloadingRequired() implementation of the detector associated with the reloading controller.
     */
    @Test
    public void testReloadingDetectorIsReloadingRequired() throws ConfigurationException {
        final ReloadingDetector detector = mock(ReloadingDetector.class);

        when(detector.isReloadingRequired()).thenReturn(Boolean.TRUE, Boolean.FALSE);

        final ReloadingFileBasedConfigurationBuilderTestImpl builder = new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        builder.getConfiguration();
        final ReloadingDetector ctrlDetector = builder.getReloadingController().getDetector();
        assertTrue(ctrlDetector.isReloadingRequired());
        assertFalse(ctrlDetector.isReloadingRequired());
        assertSame(builder.getFileHandler(), builder.getHandlerForDetector());

        verify(detector, times(2)).isReloadingRequired();
        verifyNoMoreInteractions(detector);
    }

    /**
     * Tests the behavior of the reloading detector if no underlying detector is available.
     */
    @Test
    public void testReloadingDetectorNoFileHandler() {
        final ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder = new ReloadingFileBasedConfigurationBuilder<>(
            PropertiesConfiguration.class);
        final ReloadingDetector ctrlDetector = builder.getReloadingController().getDetector();
        ctrlDetector.reloadingPerformed();
        assertFalse(ctrlDetector.isReloadingRequired());
    }

    /**
     * Tests the reloadingPerformed() implementation of the detector associated with the reloading controller.
     */
    @Test
    public void testReloadingDetectorReloadingPerformed() throws ConfigurationException {
        final ReloadingDetector detector = mock(ReloadingDetector.class);
        final ReloadingFileBasedConfigurationBuilderTestImpl builder = new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        builder.getConfiguration();
        final ReloadingDetector ctrlDetector = builder.getReloadingController().getDetector();
        ctrlDetector.reloadingPerformed();

        verify(detector).reloadingPerformed();
        verifyNoMoreInteractions(detector);
    }

    /**
     * Tests whether the controller's reloading state is reset when a new result configuration is created.
     */
    @Test
    public void testResetReloadingStateInGetConfiguration() throws ConfigurationException {
        final ReloadingDetector detector = mock(ReloadingDetector.class);

        when(detector.isReloadingRequired()).thenReturn(Boolean.TRUE);

        final ReloadingFileBasedConfigurationBuilderTestImpl builder = new ReloadingFileBasedConfigurationBuilderTestImpl(detector);
        final PropertiesConfiguration config1 = builder.getConfiguration();
        builder.getReloadingController().checkForReloading(null);
        final PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame(config1, config2);
        assertFalse(builder.getReloadingController().isInReloadingState());

        verify(detector).isReloadingRequired();
        verify(detector).reloadingPerformed();
        verifyNoMoreInteractions(detector);
    }
}
