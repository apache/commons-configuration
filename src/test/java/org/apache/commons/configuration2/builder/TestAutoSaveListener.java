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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code AutoSaveListener}.
 */
public class TestAutoSaveListener {
    /** A mock for the associated builder. */
    private FileBasedConfigurationBuilder<?> builder;

    /** The listener to be tested. */
    private AutoSaveListener listener;

    /**
     * Sends a configuration changed notification to the test listener.
     *
     * @param before flag whether the event is before the update
     */
    private void fireChangeEvent(final boolean before) {
        listener.onEvent(new ConfigurationEvent(this, ConfigurationEvent.ADD_PROPERTY, "someProperty", "someValue", before));
    }

    @BeforeEach
    public void setUp() throws Exception {
        builder = mock(FileBasedConfigurationBuilder.class);
        listener = new AutoSaveListener(builder);
    }

    /**
     * Tests that after a load operation changes on the monitored configuration are detected again.
     */
    @Test
    public void testConfigurationChangedAfterLoading() throws ConfigurationException {
        final FileHandler handler = new FileHandler();
        listener.loading(handler);
        fireChangeEvent(false);
        listener.loaded(handler);
        fireChangeEvent(false);

        verify(builder).save();
        verifyNoMoreInteractions(builder);
    }

    /**
     * Tests whether a change of the monitored configuration causes a save operation.
     */
    @Test
    public void testConfigurationChangedAutoSave() throws ConfigurationException {
        fireChangeEvent(false);

        verify(builder).save();
        verifyNoMoreInteractions(builder);
    }

    /**
     * Tests whether an exception thrown by the builder's save() method is handled.
     */
    @Test
    public void testConfigurationChangedAutoSaveException() throws ConfigurationException {
        doThrow(new ConfigurationException()).when(builder).save();

        fireChangeEvent(false);

        verify(builder).save();
        verifyNoMoreInteractions(builder);
    }

    /**
     * Tests whether no auto save is triggered before the change to the monitored configuration actually happens.
     */
    @Test
    public void testConfigurationChangedBeforeUpdateNoSave() {
        fireChangeEvent(true);

        verifyNoInteractions(builder);
    }

    /**
     * Tests that updated during load operations do not create an auto save.
     */
    @Test
    public void testConfigurationChangedWhileLoading() {
        listener.loading(new FileHandler());
        fireChangeEvent(false);

        verifyNoInteractions(builder);
    }

    /**
     * Tests whether the file handler can be updated and is correctly initialized.
     */
    @Test
    public void testUpdateFileHandler() {
        final FileHandler handler = mock(FileHandler.class);
        final FileHandler handler2 = mock(FileHandler.class);

        listener.updateFileHandler(handler);
        listener.updateFileHandler(handler2);

        verify(handler).addFileHandlerListener(listener);
        verify(handler).removeFileHandlerListener(listener);
        verify(handler2).addFileHandlerListener(listener);
        verifyNoMoreInteractions(builder, handler, handler2);
    }

    /**
     * Tests whether updateFileHandler() can deal with null input. This is used for removing the listener when it is no
     * longer needed.
     */
    @Test
    public void testUpdateFileHandlerNull() {
        final FileHandler handler = mock(FileHandler.class);

        listener.updateFileHandler(handler);
        listener.updateFileHandler(null);

        verify(handler).addFileHandlerListener(listener);
        verify(handler).removeFileHandlerListener(listener);
        verifyNoMoreInteractions(builder, handler);
    }
}
