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
package org.apache.commons.configuration2.io;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized implementation of {@code FileLocationStrategy} which searches
 * for files in the user's home directory or another special configurable
 * directory.
 * </p>
 * <p>
 * This strategy implementation ignores the URL stored in the passed in
 * {@link FileLocator}. It constructs a file path from the configured home
 * directory (which is the user's home directory per default, but can be changed
 * to another path), optionally the base path, and the file name. If the
 * resulting path points to an existing file, its URL is returned.
 * </p>
 * <p>
 * When constructing an instance it can be configured whether the base path
 * should be taken into account. If this option is set, the base path is
 * appended to the home directory if it is not <b>null</b>. This is useful for
 * instance to select a specific sub directory of the user's home directory. If
 * this option is set to <b>false</b>, the base path is always ignored, and only
 * the file name is evaluated.
 * </p>
 *
 */
public class HomeDirectoryLocationStrategy implements FileLocationStrategy
{
    /** Constant for the system property with the user's home directory. */
    private static final String PROP_HOME = "user.home";

    /** The home directory to be searched for the requested file. */
    private final String homeDirectory;

    /** The flag whether the base path is to be taken into account. */
    private final boolean evaluateBasePath;

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} and
     * initializes it with the specified settings.
     *
     * @param homeDir the path to the home directory (can be <b>null</b>)
     * @param withBasePath a flag whether the base path should be evaluated
     */
    public HomeDirectoryLocationStrategy(final String homeDir, final boolean withBasePath)
    {
        homeDirectory = fetchHomeDirectory(homeDir);
        evaluateBasePath = withBasePath;
    }

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} and
     * initializes the base path flag. The home directory is set to the user's
     * home directory.
     *
     * @param withBasePath a flag whether the base path should be evaluated
     */
    public HomeDirectoryLocationStrategy(final boolean withBasePath)
    {
        this(null, withBasePath);
    }

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} with
     * default settings. The home directory is set to the user's home directory.
     * The base path flag is set to <b>false</b> (which means that the base path
     * is ignored).
     */
    public HomeDirectoryLocationStrategy()
    {
        this(false);
    }

    /**
     * Returns the home directory. In this directory the strategy searches for
     * files.
     *
     * @return the home directory used by this object
     */
    public String getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * Returns a flag whether the base path is to be taken into account when
     * searching for a file.
     *
     * @return the flag whether the base path is evaluated
     */
    public boolean isEvaluateBasePath()
    {
        return evaluateBasePath;
    }

    /**
     * {@inheritDoc} This implementation searches in the home directory for a
     * file described by the passed in {@code FileLocator}. If the locator
     * defines a base path and the {@code evaluateBasePath} property is
     * <b>true</b>, a sub directory of the home directory is searched.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator)
    {
        if (StringUtils.isNotEmpty(locator.getFileName()))
        {
            final String basePath = fetchBasePath(locator);
            final File file =
                    FileLocatorUtils.constructFile(basePath,
                            locator.getFileName());
            if (file.isFile())
            {
                return FileLocatorUtils.convertFileToURL(file);
            }
        }

        return null;
    }

    /**
     * Determines the base path to be used for the current locate() operation.
     *
     * @param locator the {@code FileLocator}
     * @return the base path to be used
     */
    private String fetchBasePath(final FileLocator locator)
    {
        if (isEvaluateBasePath()
                && StringUtils.isNotEmpty(locator.getBasePath()))
        {
            return FileLocatorUtils.appendPath(getHomeDirectory(),
                    locator.getBasePath());
        }
        return getHomeDirectory();
    }

    /**
     * Obtains the home directory to be used by a new instance. If a directory
     * name is provided, it is used. Otherwise, the user's home directory is
     * looked up.
     *
     * @param homeDir the passed in home directory
     * @return the directory to be used
     */
    private static String fetchHomeDirectory(final String homeDir)
    {
        return (homeDir != null) ? homeDir : System.getProperty(PROP_HOME);
    }
}
