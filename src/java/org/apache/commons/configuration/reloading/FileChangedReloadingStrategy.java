/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;

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
public class FileChangedReloadingStrategy implements ReloadingStrategy
{
    /** Constant for the jar URL protocol.*/
    private static final String JAR_PROTOCOL = "jar";
    
    /** Stores a reference to the configuration to be monitored.*/
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

        if ((now > lastChecked + refreshDelay))
        {
            lastChecked = now;
            if(hasChanged())
            {
                reloading = true;
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
        File file = getFile();
        if (file != null)
        {
            lastModified = file.lastModified();
        }
    }

    /**
     * Check if the configuration has changed since the last time it was loaded.
     */
    protected boolean hasChanged()
    {
        File file = getFile();
        if (file == null || !file.exists())
        {
            return false;
        }

        return (file.lastModified() > lastModified);
    }

    /**
     * Returns the file that is monitored by this strategy. Note that the return
     * value can be <b>null </b> under some circumstances.
     * 
     * @return the monitored file
     */
    protected File getFile()
    {
        return (configuration.getURL() != null) ? fileFromURL(configuration
                .getURL()) : configuration.getFile();
    }

    /**
     * Helper method for transforming a URL into a file object. This method
     * handles file: and jar: URLs.
     * 
     * @param url the URL to be converted
     * @return the resulting file or <b>null </b>
     */
    private File fileFromURL(URL url)
    {
        if (JAR_PROTOCOL.equals(url.getProtocol()))
        {
            String path = url.getPath();
            try
            {
                return ConfigurationUtils.fileFromURL(new URL(path.substring(0,
                        path.indexOf('!'))));
            }
            catch (MalformedURLException mex)
            {
                return null;
            }
        }
        else
        {
            return ConfigurationUtils.fileFromURL(url);
        }
    }
}
