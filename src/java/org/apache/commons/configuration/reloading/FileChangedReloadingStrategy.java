/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;

import org.apache.commons.configuration.FileConfiguration;

/**
 * A reloading strategy that will reload the configuration every time its
 * underlying file is changed. The file is not reloaded more than once
 * every 5 seconds by default, this time can be changed by setting the refresh
 * delay. This strategy only works with FileConfiguration instances.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class FileChangedReloadingStrategy implements ReloadingStrategy
{
    protected FileConfiguration configuration;

    /** The last time the configuration file was modified. */
    protected long lastModified;

    /** The last time the file was checked for changes. */
    protected long lastChecked;

    /** The minimum delay in milliseconds between checks. */
    protected long refreshDelay = 5000;

    public void setConfiguration(FileConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void init()
    {
        updateLastModified();
    }

    public boolean reloadingRequired()
    {
        boolean reloading = false;

        long now = System.currentTimeMillis();

        if ((now > lastChecked + refreshDelay) && hasChanged())
        {
            lastChecked = now;
            reloading = true;
        }

        return reloading;
    }

    public void reloadingPerformed()
    {
        updateLastModified();
    }

    /**
     * Return the minimal time in milliseconds between two reloadings.
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
        File file = new File(configuration.getFileName());
        lastModified = file.lastModified();
    }

    /**
     * Check if the configuration has changed since the last
     * time it was loaded.
     */
    protected boolean hasChanged()
    {
        if (!configuration.getFile().exists())
        {
            return false;
        }

        return (configuration.getFile().lastModified() > lastModified);
    }

}
