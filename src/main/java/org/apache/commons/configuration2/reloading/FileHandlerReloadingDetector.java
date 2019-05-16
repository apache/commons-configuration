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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocatorUtils;

/**
 * <p>
 * A specialized implementation of {@code ReloadingDetector} which monitors a
 * file specified by a {@link FileHandler}.
 * </p>
 * <p>
 * An instance of this class is passed a {@code FileHandler} at construction
 * time. Each time the {@code isReloadingRequired()} method is called, it checks
 * whether the {@code FileHandler} points to a valid location. If this is the
 * case, the file's last modification time is obtained and compared with the
 * last stored time. If it has changed, a reload operation should be performed.
 * </p>
 * <p>
 * Because file I/O may be expensive it is possible to configure a refresh delay
 * as a time in milliseconds. This is the minimum interval between two checks.
 * If the {@code isReloadingRequired()} method is called in shorter intervals,
 * it does not perform a check, but directly returns <b>false</b>.
 * </p>
 * <p>
 * To initialize an instance either {@code isReloadingRequired()} or
 * {@code reloadingPerformed()} can be called. The first call of
 * {@code isReloadingRequired} does not perform a check, but obtains the initial
 * modification date of the monitored file. {@code reloadingPerformed()} always
 * obtains the file's modification date and stores it internally.
 * </p>
 *
 * @since 2.0
 */
public class FileHandlerReloadingDetector implements ReloadingDetector
{
    /** Constant for the jar URL protocol. */
    private static final String JAR_PROTOCOL = "jar";

    /** Constant for the default refresh delay. */
    private static final int DEFAULT_REFRESH_DELAY = 5000;

    /** The associated file handler. */
    private final FileHandler fileHandler;

    /** The refresh delay. */
    private final long refreshDelay;

    /** The last time the configuration file was modified. */
    private long lastModified;

    /** The last time the file was checked for changes. */
    private long lastChecked;

    /**
     * Creates a new instance of {@code FileHandlerReloadingDetector} and
     * initializes it with the {@code FileHandler} to monitor and the refresh
     * delay. The handler is directly used, no copy is created. So it is
     * possible to change the location monitored by manipulating the
     * {@code FileHandler} object.
     *
     * @param handler the {@code FileHandler} associated with this detector (can
     *        be <b>null</b>)
     * @param refreshDelay the refresh delay; a value of 0 means that a check is
     *        performed in all cases
     */
    public FileHandlerReloadingDetector(final FileHandler handler, final long refreshDelay)
    {
        fileHandler = (handler != null) ? handler : new FileHandler();
        this.refreshDelay = refreshDelay;
    }

    /**
     * Creates a new instance of {@code FileHandlerReloadingDetector} and
     * initializes it with the {@code FileHandler} to monitor and a default
     * refresh delay.
     *
     * @param handler the {@code FileHandler} associated with this detector (can
     *        be <b>null</b>)
     */
    public FileHandlerReloadingDetector(final FileHandler handler)
    {
        this(handler, DEFAULT_REFRESH_DELAY);
    }

    /**
     * Creates a new instance of {@code FileHandlerReloadingDetector} with an
     * uninitialized {@code FileHandler} object. The file to be monitored has to
     * be set later by manipulating the handler object returned by
     * {@code getFileHandler()}.
     */
    public FileHandlerReloadingDetector()
    {
        this(null);
    }

    /**
     * Returns the {@code FileHandler} associated with this object. The
     * underlying handler is directly returned, so changing its location also
     * changes the file monitored by this detector.
     *
     * @return the associated {@code FileHandler}
     */
    public FileHandler getFileHandler()
    {
        return fileHandler;
    }

    /**
     * Returns the refresh delay. This is a time in milliseconds. The
     * {@code isReloadingRequired()} method first checks whether the time since
     * the previous check is more than this value in the past. Otherwise, no
     * check is performed. This is a means to limit file I/O caused by this
     * class.
     *
     * @return the refresh delay used by this object
     */
    public long getRefreshDelay()
    {
        return refreshDelay;
    }

    /**
     * {@inheritDoc} This implementation checks whether the associated
     * {@link FileHandler} points to a valid file and whether the last
     * modification time of this time has changed since the last check. The
     * refresh delay is taken into account, too; a check is only performed if at
     * least this time has passed since the last check.
     */
    @Override
    public boolean isReloadingRequired()
    {
        final long now = System.currentTimeMillis();
        if (now >= lastChecked + getRefreshDelay())
        {
            lastChecked = now;

            final long modified = getLastModificationDate();
            if (modified > 0)
            {
                if (lastModified == 0)
                {
                    // initialization
                    updateLastModified(modified);
                }
                else
                {
                    if (modified != lastModified)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc} This implementation updates the internally stored last
     * modification date with the current modification date of the monitored
     * file. So the next change is detected when this file is changed again.
     */
    @Override
    public void reloadingPerformed()
    {
        updateLastModified(getLastModificationDate());
    }

    /**
     * Tells this implementation that the internally stored state should be
     * refreshed. This method is intended to be called after the creation
     * of an instance.
     */
    public void refresh()
    {
        updateLastModified(getLastModificationDate());
    }

    /**
     * Returns the date of the last modification of the monitored file. A return
     * value of 0 indicates, that the monitored file does not exist.
     *
     * @return the last modification date
     */
    protected long getLastModificationDate()
    {
        final File file = getExistingFile();
        return (file != null) ? file.lastModified() : 0;
    }

    /**
     * Updates the last modification date of the monitored file. The need for a
     * reload is detected only if the file's modification date is different from
     * this value.
     *
     * @param time the new last modification date
     */
    protected void updateLastModified(final long time)
    {
        lastModified = time;
    }

    /**
     * Returns the {@code File} object which is monitored by this object. This
     * method is called every time the file's last modification time is needed.
     * If it returns <b>null</b>, no check is performed. This base
     * implementation obtains the {@code File} from the associated
     * {@code FileHandler}. It can also deal with URLs to jar files.
     *
     * @return the {@code File} to be monitored (can be <b>null</b>)
     */
    protected File getFile()
    {
        final URL url = getFileHandler().getURL();
        return (url != null) ? fileFromURL(url) : getFileHandler().getFile();
    }

    /**
     * Returns the monitored {@code File} or <b>null</b> if it does not exist.
     *
     * @return the monitored {@code File} or <b>null</b>
     */
    private File getExistingFile()
    {
        File file = getFile();
        if (file != null && !file.exists())
        {
            file = null;
        }

        return file;
    }

    /**
     * Helper method for transforming a URL into a file object. This method
     * handles file: and jar: URLs.
     *
     * @param url the URL to be converted
     * @return the resulting file or <b>null </b>
     */
    private static File fileFromURL(final URL url)
    {
        if (JAR_PROTOCOL.equals(url.getProtocol()))
        {
            final String path = url.getPath();
            try
            {
                return FileLocatorUtils.fileFromURL(new URL(path.substring(0,
                        path.indexOf('!'))));
            }
            catch (final MalformedURLException mex)
            {
                return null;
            }
        }
        return FileLocatorUtils.fileFromURL(url);
    }
}
