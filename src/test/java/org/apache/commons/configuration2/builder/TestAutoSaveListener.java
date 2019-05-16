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

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code AutoSaveListener}.
 *
 */
public class TestAutoSaveListener
{
    /** A mock for the associated builder. */
    private FileBasedConfigurationBuilder<?> builder;

    /** The listener to be tested. */
    private AutoSaveListener listener;

    @Before
    public void setUp() throws Exception
    {
        builder = EasyMock.createMock(FileBasedConfigurationBuilder.class);
        listener = new AutoSaveListener(builder);
    }

    /**
     * Sends a configuration changed notification to the test listener.
     *
     * @param before flag whether the event is before the update
     */
    private void fireChangeEvent(final boolean before)
    {
        listener.onEvent(new ConfigurationEvent(this,
                ConfigurationEvent.ADD_PROPERTY, "someProperty",
                "someValue", before));
    }

    /**
     * Tests whether the file handler can be updated and is correctly
     * initialized.
     */
    @Test
    public void testUpdateFileHandler()
    {
        final FileHandler handler = EasyMock.createMock(FileHandler.class);
        final FileHandler handler2 = EasyMock.createMock(FileHandler.class);
        handler.addFileHandlerListener(listener);
        handler.removeFileHandlerListener(listener);
        handler2.addFileHandlerListener(listener);
        EasyMock.replay(handler, handler2);
        listener.updateFileHandler(handler);
        listener.updateFileHandler(handler2);
        EasyMock.verify(handler, handler2);
    }

    /**
     * Tests whether updateFileHandler() can deal with null input. This is used
     * for removing the listener when it is no longer needed.
     */
    @Test
    public void testUpdateFileHandlerNull()
    {
        final FileHandler handler = EasyMock.createMock(FileHandler.class);
        handler.addFileHandlerListener(listener);
        handler.removeFileHandlerListener(listener);
        EasyMock.replay(handler);
        listener.updateFileHandler(handler);
        listener.updateFileHandler(null);
        EasyMock.verify(handler);
    }

    /**
     * Tests whether a change of the monitored configuration causes a save
     * operation.
     */
    @Test
    public void testConfigurationChangedAutoSave()
            throws ConfigurationException
    {
        builder.save();
        EasyMock.replay(builder);
        fireChangeEvent(false);
        EasyMock.verify(builder);
    }

    /**
     * Tests whether an exception thrown by the builder's save() method is
     * handled.
     */
    @Test
    public void testConfigurationChangedAutoSaveException()
            throws ConfigurationException
    {
        builder.save();
        EasyMock.expectLastCall().andThrow(new ConfigurationException());
        EasyMock.replay(builder);
        fireChangeEvent(false);
        EasyMock.verify(builder);
    }

    /**
     * Tests whether no auto save is triggered before the change to the
     * monitored configuration actually happens.
     */
    @Test
    public void testConfigurationChangedBeforeUpdateNoSave()
    {
        EasyMock.replay(builder);
        fireChangeEvent(true);
    }

    /**
     * Tests that updated during load operations do not create an auto save.
     */
    @Test
    public void testConfigurationChangedWhileLoading()
    {
        EasyMock.replay(builder);
        listener.loading(new FileHandler());
        fireChangeEvent(false);
    }

    /**
     * Tests that after a load operation changes on the monitored configuration
     * are detected again.
     */
    @Test
    public void testConfigurationChangedAfterLoading()
            throws ConfigurationException
    {
        builder.save();
        EasyMock.replay(builder);
        final FileHandler handler = new FileHandler();
        listener.loading(handler);
        fireChangeEvent(false);
        listener.loaded(handler);
        fireChangeEvent(false);
        EasyMock.verify(builder);
    }
}
