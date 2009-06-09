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
package org.apache.commons.configuration2.reloading;

import org.apache.commons.configuration2.FileConfiguration;
import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.FileSystem;
import org.apache.commons.configuration2.FileSystemBased;
import org.apache.commons.configuration2.AbstractFileConfiguration;
import org.apache.commons.vfs.impl.DefaultFileMonitor;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemException;

import java.util.Map;
import java.util.HashMap;


/**
 * <p>A reloading strategy that will reload the configuration every time its
 * underlying file is changed.</p>
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public class VFSFileMonitorReloadingStrategy implements ReloadingStrategy, FileListener
{
    /** Used to synchronize initialization of the monitor. */
    private static final String INIT_GATE = "gate";

    /** The FileMonitor */
    private static DefaultFileMonitor fm;

    /** The files being monitored */
    private static Map<FileObject, VFSFileMonitorReloadingStrategy> strategies =
            new HashMap<FileObject, VFSFileMonitorReloadingStrategy>();

    /** Mimimum delay value */
    private static final long DEFAULT_DELAY = 1000;

    /** Stores a reference to the configuration to be monitored. */
    protected FileConfiguration configuration;

    /** The reload status */
    private boolean reloadRequired;

    /** Delay interval between checking the files. */
    private long delay;

    /**
     * Return the current delay interval.
     * @return The delay interval.
     */
    public long getDelay()
    {
        return fm.getDelay();
    }

    /**
     * Request a new delay interval. If the interval specified is less than
     * what the monitor is currently using the interval will be ignored. If
     * this method is called after the strategy has started it will be ignored.
     * @param delay The requested delay interval.
     */
    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    /**
     * Specify the configuration to monitor. The configuration must be set before
     * init is called.
     * @param configuration The configuration to monitor.
     */
    public void setConfiguration(FileConfiguration configuration)
    {
        if (configuration == null || configuration instanceof FileSystemBased)
        {
            this.configuration = configuration;
        }
        else
        {
            throw new ConfigurationRuntimeException("Configuration must be based on a FileSystem");
        }
    }

    /**
     * Initialize the ReloadingStrategy.
     */
    public void init()
    {
        if (configuration.getURL() == null && configuration.getFileName() == null)
        {
            return;
        }
        if (this.configuration == null)
        {
            throw new IllegalStateException("No configuration has been set for this strategy");
        }
        FileObject file;

        try
        {
            FileSystemManager fsManager = VFS.getManager();
            FileSystem fs = ((FileSystemBased) configuration).getFileSystem();
            String uri = fs.getPath(null, configuration.getURL(), configuration.getBasePath(),
                configuration.getFileName());
            if (uri == null)
            {
                throw new ConfigurationRuntimeException("Unable to determine file to monitor");
            }
            file = fsManager.resolveFile(uri);
        }
        catch (FileSystemException fse)
        {
            String msg = "Unable to monitor " + configuration.getURL().toString();
            throw new ConfigurationRuntimeException(msg, fse);
        }
        synchronized (INIT_GATE)
        {
            if (fm == null)
            {
                fm = new DefaultFileMonitor(null);
                long delayTime = (delay > DEFAULT_DELAY) ? delay : DEFAULT_DELAY;
                fm.setDelay(delayTime);
                fm.start();
            }
            else
            {
                long delayTime = fm.getDelay();
                if (delay > delayTime)
                {
                    fm.setDelay(delay);
                }
            }
            file.getFileSystem().addListener(file, this);
            fm.addFile(file);
            strategies.put(file, this);
        }

    }

    /**
     * Shutdown all reloading strategies
     */
    public static void stopMonitor()
    {
        synchronized (INIT_GATE)
        {
            if (fm != null)
            {
                fm.stop();
                fm = null;
            }

            for (Map.Entry<FileObject, VFSFileMonitorReloadingStrategy> entry : strategies.entrySet())
            {
                FileObject file = entry.getKey();
                file.getFileSystem().removeListener(file, entry.getValue());
            }
            strategies.clear();
        }
    }

    /**
     * Tell if the evaluation of the strategy requires to reload the configuration.
     *
     * @return a flag whether a reload should be performed
     */
    public boolean reloadingRequired()
    {
        return reloadRequired;
    }

    /**
     * Notify the strategy that the file has been reloaded.
     */
    public void reloadingPerformed()
    {
        reloadRequired = false;
    }


    /**
     * Called when a file is created.
     * @param event The event.
     * @throws Exception If an error occurs.
     */
    public void fileCreated(FileChangeEvent event) throws Exception
    {
        reloadRequired = true;
        fireEvent();
    }

    /**
     * Called when a file is deleted.
     * @param event The event.
     * @throws Exception If an error occurs.
     */
    public void fileDeleted(FileChangeEvent event) throws Exception
    {
        // Ignore this event
    }

    /**
     * Called when a file is changed.
     * @param event The event.
     * @throws Exception If an exception occurs.
     */
    public void fileChanged(FileChangeEvent event) throws Exception
    {
        reloadRequired = true;
        fireEvent();
    }

    private void fireEvent()
    {
        if (configuration instanceof AbstractFileConfiguration)
        {
            ((AbstractFileConfiguration) configuration).configurationChanged();
        }
    }
}
