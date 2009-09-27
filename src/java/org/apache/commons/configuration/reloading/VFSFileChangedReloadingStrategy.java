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

package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.FileSystemBased;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>A reloading strategy that will reload the configuration every time its
 * underlying file is changed.</p>
 * <p>This reloading strategy does not actively monitor a configuration file,
 * but is triggered by its associated configuration whenever properties are
 * accessed. It then checks the configuration file's last modification date
 * and causes a reload if this has changed.</p>
 * <p>To avoid permanent disc access on successive property lookups a refresh
 * delay can be specified. This has the effect that the configuration file's
 * last modification date is only checked once in this delay period. The default
 * value for this refresh delay is 5 seconds.</p>
 * <p>This strategy only works with FileConfiguration instances.</p>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class VFSFileChangedReloadingStrategy implements ReloadingStrategy
{
    /** Constant for the default refresh delay.*/
    private static final int DEFAULT_REFRESH_DELAY = 5000;

    /** Stores a reference to the configuration to be monitored.*/
    protected FileConfiguration configuration;

    /** The last time the configuration file was modified. */
    protected long lastModified;

    /** The last time the file was checked for changes. */
    protected long lastChecked;

    /** The minimum delay in milliseconds between checks. */
    protected long refreshDelay = DEFAULT_REFRESH_DELAY;

    /** A flag whether a reload is required.*/
    private boolean reloading;

    /** Stores the logger.*/
    private Log log = LogFactory.getLog(getClass());

    public void setConfiguration(FileConfiguration configuration)
    {
        this.configuration = configuration;
    }

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
        updateLastModified();
    }

    public boolean reloadingRequired()
    {
        if (!reloading)
        {
            long now = System.currentTimeMillis();

            if (now > lastChecked + refreshDelay)
            {
                lastChecked = now;
                if (hasChanged())
                {
                    reloading = true;
                }
            }
        }

        return reloading;
    }

    public void reloadingPerformed()
    {
        updateLastModified();
    }

    /**
     * Return the minimal time in milliseconds between two reloadings.
     *
     * @return the refresh delay (in milliseconds)
     */
    public long getRefreshDelay()
    {
        return refreshDelay;
    }

    /**
     * Set the minimal time between two reloadings.
     *
     * @param refreshDelay refresh delay in milliseconds
     */
    public void setRefreshDelay(long refreshDelay)
    {
        this.refreshDelay = refreshDelay;
    }

    /**
     * Update the last modified time.
     */
    protected void updateLastModified()
    {
        FileObject file = getFile();
        if (file != null)
        {
            try
            {
                lastModified = file.getContent().getLastModifiedTime();
            }
            catch (FileSystemException fse)
            {
                log.error("Unable to get last modified time for" + file.getName().getURI());
            }
        }
        reloading = false;
    }

    /**
     * Check if the configuration has changed since the last time it was loaded.
     *
     * @return a flag whether the configuration has changed
     */
    protected boolean hasChanged()
    {
        FileObject file = getFile();
        try
        {
            if (file == null || !file.exists())
            {
                return false;
            }

            return file.getContent().getLastModifiedTime() > lastModified;
        }
        catch (FileSystemException ex)
        {
            log.error("Unable to get last modified time for" + file.getName().getURI());
            return false;
        }
    }

    /**
     * Returns the file that is monitored by this strategy. Note that the return
     * value can be <b>null </b> under some circumstances.
     *
     * @return the monitored file
     */
    protected FileObject getFile()
    {
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
            return fsManager.resolveFile(uri);
        }
        catch (FileSystemException fse)
        {
            String msg = "Unable to monitor " + configuration.getURL().toString();
            log.error(msg);
            throw new ConfigurationRuntimeException(msg, fse);
        }
    }
}
