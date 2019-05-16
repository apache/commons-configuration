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
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileHandlerListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A listener class implementing an auto save mechanism for file-based
 * configurations.
 * </p>
 * <p>
 * Instances of this class are used by {@link FileBasedConfigurationBuilder} to
 * save their managed configuration instances when they are changed. Objects are
 * registered at {@code Configuration} objects as event listeners and thus can
 * trigger save operations whenever a change event is received.
 * </p>
 * <p>
 * There is one complication however: Some configuration implementations fire
 * change events during a load operation. Such events must be ignored to prevent
 * corruption of the source file. This is achieved by monitoring the associated
 * {@code FileHandler}: during load operations no auto-save is performed.
 * </p>
 *
 * @since 2.0
 */
class AutoSaveListener extends FileHandlerListenerAdapter implements
        EventListener<ConfigurationEvent>
{
    /** The logger. */
    private final Log log = LogFactory.getLog(getClass());

    /** The associated builder. */
    private final FileBasedConfigurationBuilder<?> builder;

    /** Stores the file handler monitored by this listener. */
    private FileHandler handler;

    /**
     * A counter to keep track whether a load operation is currently in
     * progress.
     */
    private int loading;

    /**
     * Creates a new instance of {@code AutoSaveListener} and initializes it
     * with the associated builder.
     *
     * @param bldr the associated builder
     */
    public AutoSaveListener(final FileBasedConfigurationBuilder<?> bldr)
    {
        builder = bldr;
    }

    /**
     * {@inheritDoc} This implementation checks whether an auto-safe operation
     * should be performed. This is the case if the event indicates that an
     * update of the configuration has been performed and currently no load
     * operation is in progress.
     */
    @Override
    public void onEvent(final ConfigurationEvent event)
    {
        if (autoSaveRequired(event))
        {
            try
            {
                builder.save();
            }
            catch (final ConfigurationException ce)
            {
                log.warn("Auto save failed!", ce);
            }
        }
    }

    /**
     * {@inheritDoc} This implementation increments the counter for load
     * operations in progress.
     */
    @Override
    public synchronized void loading(final FileHandler handler)
    {
        loading++;
    }

    /**
     * {@inheritDoc} This implementation decrements the counter for load
     * operations in progress.
     */
    @Override
    public synchronized void loaded(final FileHandler handler)
    {
        loading--;
    }

    /**
     * Updates the {@code FileHandler}. This method is called by the builder
     * when a new configuration instance was created which is associated with a
     * new file handler. It updates the internal file handler reference and
     * performs necessary listener registrations.
     *
     * @param fh the new {@code FileHandler} (can be <b>null</b>)
     */
    public synchronized void updateFileHandler(final FileHandler fh)
    {
        if (handler != null)
        {
            handler.removeFileHandlerListener(this);
        }

        if (fh != null)
        {
            fh.addFileHandlerListener(this);
        }
        handler = fh;
    }

    /**
     * Returns a flag whether a load operation is currently in progress.
     *
     * @return a flag whether a load operation is in progress
     */
    private synchronized boolean inLoadOperation()
    {
        return loading > 0;
    }

    /**
     * Checks whether an auto save operation has to be performed based on the
     * passed in event and the current state of this object.
     *
     * @param event the configuration change event
     * @return <b>true</b> if a save operation should be performed, <b>false</b>
     *         otherwise
     */
    private boolean autoSaveRequired(final ConfigurationEvent event)
    {
        return !event.isBeforeUpdate() && !inLoadOperation();
    }
}
