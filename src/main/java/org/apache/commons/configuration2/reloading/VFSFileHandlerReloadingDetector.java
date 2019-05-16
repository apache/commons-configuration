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

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 * <p>
 * A file-based reloading strategy that uses <a
 * href="http://commons.apache.org/vfs/">Commons VFS</a> to determine when a
 * file was changed.
 * </p>
 * <p>
 * This reloading strategy is very similar to
 * {@link FileHandlerReloadingDetector}, except for the fact that it uses VFS
 * and thus can deal with a variety of different configuration sources.
 * </p>
 * <p>
 * This strategy only works with FileConfiguration instances.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @since 1.7
 */
public class VFSFileHandlerReloadingDetector extends FileHandlerReloadingDetector
{
    /** Stores the logger.*/
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Creates a new instance of {@code VFSFileHandlerReloadingDetector} and
     * initializes it with an empty {@code FileHandler} object.
     */
    public VFSFileHandlerReloadingDetector()
    {
        super();
    }

    /**
     * Creates a new instance of {@code VFSFileHandlerReloadingDetector} and
     * initializes it with the given {@code FileHandler} object and the given
     * refresh delay.
     *
     * @param handler the {@code FileHandler}
     * @param refreshDelay the refresh delay
     */
    public VFSFileHandlerReloadingDetector(final FileHandler handler,
            final long refreshDelay)
    {
        super(handler, refreshDelay);
    }

    /**
     * Creates a new instance of {@code VFSFileHandlerReloadingDetector} and
     * initializes it with the given {@code FileHandler} object.
     *
     * @param handler the {@code FileHandler}
     */
    public VFSFileHandlerReloadingDetector(final FileHandler handler)
    {
        super(handler);
    }

    /**
     * {@inheritDoc} This implementation uses Commons VFS to obtain a
     * {@code FileObject} and read the date of the last modification.
     */
    @Override
    protected long getLastModificationDate()
    {
        final FileObject file = getFileObject();
        try
        {
            if (file == null || !file.exists())
            {
                return 0;
            }

            return file.getContent().getLastModifiedTime();
        }
        catch (final FileSystemException ex)
        {
            log.error("Unable to get last modified time for"
                    + file.getName().getURI(), ex);
            return 0;
        }
    }

    /**
     * Returns the file that is monitored by this strategy. Note that the return
     * value can be <b>null </b> under some circumstances.
     *
     * @return the monitored file
     */
    protected FileObject getFileObject()
    {
        if (!getFileHandler().isLocationDefined())
        {
            return null;
        }

        try
        {
            final FileSystemManager fsManager = VFS.getManager();
            final String uri = resolveFileURI();
            if (uri == null)
            {
                throw new ConfigurationRuntimeException("Unable to determine file to monitor");
            }
            return fsManager.resolveFile(uri);
        }
        catch (final FileSystemException fse)
        {
            final String msg = "Unable to monitor " + getFileHandler().getURL().toString();
            log.error(msg);
            throw new ConfigurationRuntimeException(msg, fse);
        }
    }

    /**
     * Resolves the URI of the monitored file.
     *
     * @return the URI of the monitored file or <b>null</b> if it cannot be
     *         resolved
     */
    protected String resolveFileURI()
    {
        final FileSystem fs = getFileHandler().getFileSystem();
        final String uri =
                fs.getPath(null, getFileHandler().getURL(), getFileHandler()
                        .getBasePath(), getFileHandler().getFileName());
        return uri;
    }
}
