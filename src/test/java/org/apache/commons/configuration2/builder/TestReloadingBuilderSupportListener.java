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

import static org.junit.Assert.assertNotNull;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingDetector;
import org.apache.commons.configuration2.reloading.ReloadingEvent;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code ReloadingBuilderSupportListener}.
 */
public class TestReloadingBuilderSupportListener
{
    /**
     * Tests that the builder is reset when a reloading event notification
     * occurs.
     */
    @Test
    public void testResetBuilderOnReloadingEvent()
    {
        final ReloadingDetector detector =
                EasyMock.createMock(ReloadingDetector.class);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        EasyMock.replay(detector);
        final ReloadingController controller = new ReloadingController(detector);
        final BasicConfigurationBuilder<Configuration> builder =
                new BasicConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final BuilderEventListenerImpl builderListener =
                new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.ANY, builderListener);

        final ReloadingBuilderSupportListener listener =
                ReloadingBuilderSupportListener.connect(builder, controller);
        assertNotNull("No listener returned", listener);
        controller.checkForReloading(null);
        builderListener.nextEvent(ConfigurationBuilderEvent.RESET);
        builderListener.assertNoMoreEvents();
    }

    /**
     * Tests that the controller's reloading state is reset when a new result
     * object is created.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testResetReloadingStateOnResultCreation()
            throws ConfigurationException
    {
        final ReloadingController controller =
                EasyMock.createMock(ReloadingController.class);
        controller.addEventListener(EasyMock.eq(ReloadingEvent.ANY),
                EasyMock.anyObject(EventListener.class));
        controller.resetReloadingState();
        EasyMock.replay(controller);
        final BasicConfigurationBuilder<Configuration> builder =
                new BasicConfigurationBuilder<>(
                        PropertiesConfiguration.class);

        final ReloadingBuilderSupportListener listener =
                ReloadingBuilderSupportListener.connect(builder, controller);
        assertNotNull("No listener returned", listener);
        builder.getConfiguration();
        EasyMock.verify(controller);
    }
}
