/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;

/**
 * A specialized implementation of {@code FileLocationStrategy} which searches for files in the user's home directory or
 * another special configurable directory.
 * <p>
 * This strategy implementation ignores the URL stored in the passed in {@link FileLocator}. It constructs a file path
 * from the configured home directory (which is the user's home directory per default, but can be changed to another
 * path), optionally the base path, and the file name. If the resulting path points to an existing file, its URL is
 * returned.
 * </p>
 * <p>
 * When constructing an instance it can be configured whether the base path should be taken into account. If this option
 * is set, the base path is appended to the home directory if it is not {@code null}. This is useful for instance to
 * select a specific sub directory of the user's home directory. If this option is set to <strong>false</strong>, the base path is
 * always ignored, and only the file name is evaluated.
 * </p>
 * <p>
 * See {@link AbstractFileLocationStrategy} learn how to grant an deny URL schemes and hosts.
 * </p>
 *
 * @see AbstractFileLocationStrategy
 */
public class HomeDirectoryLocationStrategy extends AbstractFileLocationStrategy {

    /**
     * Builds new instances of {@link HomeDirectoryLocationStrategy}.
     *
     * @since 2.15.0
     */
    public static class Builder extends AbstractBuilder<HomeDirectoryLocationStrategy, Builder> {

        /** The flag whether the base path is to be taken into account. */
        private boolean evaluateBasePath;

        /** The home directory to be searched for the requested file. */
        private String homeDirectory;

        /**
         * Constructs a new instance.
         */
        public Builder() {
            // empty
        }

        @Override
        public HomeDirectoryLocationStrategy get() throws IOException {
            return new HomeDirectoryLocationStrategy(this);
        }

        /**
         * Sets whether the base path should be evaluated.
         *
         * @param evaluateBasePath whether the base path should be evaluated.
         * @return {@code this} instance..
         */
        public Builder setEvaluateBasePath(final boolean evaluateBasePath) {
            this.evaluateBasePath = evaluateBasePath;
            return asThis();
        }

        /**
         * Sets the path to the home directory (may be {@code null}).
         *
         * @param homeDirectory the path to the home directory (may be {@code null})
         * @return {@code this} instance..
         */
        public Builder setHomeDirectory(final String homeDirectory) {
            this.homeDirectory = homeDirectory;
            return asThis();
        }
    }

    /**
     * Gets the home directory to be used by a new instance. If a directory name is provided, it is used. Otherwise, the
     * user's home directory is looked up.
     *
     * @param homeDir the passed in home directory
     * @return The directory to be used
     */
    private static String getHomeDirectory(final String homeDir) {
        return homeDir != null ? homeDir : SystemProperties.getUserHome();
    }

    /** The flag whether the base path is to be taken into account. */
    private final boolean evaluateBasePath;

    /** The home directory to be searched for the requested file. */
    private final String homeDirectory;

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} with default settings. The home directory is set to
     * the user's home directory. The base path flag is set to <strong>false</strong> (which means that the base path is ignored).
     */
    public HomeDirectoryLocationStrategy() {
        this(false);
    }

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} and initializes the base path flag. The home
     * directory is set to the user's home directory.
     *
     * @param withBasePath a flag whether the base path should be evaluated.
     * @deprecated Use {@link Builder#setEvaluateBasePath(boolean)}.
     */
    @Deprecated
    public HomeDirectoryLocationStrategy(final boolean withBasePath) {
        this(new Builder().setHomeDirectory(null).setEvaluateBasePath(withBasePath));
    }

    /**
     * Constructs a new instance.
     *
     * @param builder How to build the instance.
     */
    private HomeDirectoryLocationStrategy(final Builder builder) {
        super(builder);
        homeDirectory = getHomeDirectory(builder.homeDirectory);
        evaluateBasePath = builder.evaluateBasePath;
    }

    /**
     * Creates a new instance of {@code HomeDirectoryLocationStrategy} and initializes it with the specified settings.
     *
     * @param homeDir the path to the home directory (may be {@code null}).
     * @param withBasePath a flag whether the base path should be evaluated.
     * @deprecated Use {@link Builder#setHomeDirectory(String)}.
     */
    @Deprecated
    public HomeDirectoryLocationStrategy(final String homeDir, final boolean withBasePath) {
        this(new Builder().setHomeDirectory(homeDir).setEvaluateBasePath(withBasePath));
    }

    /**
     * Determines the base path to be used for the current locate() operation.
     *
     * @param locator the {@code FileLocator}
     * @return The base path to be used
     */
    private String getBasePath(final FileLocator locator) {
        if (isEvaluateBasePath() && StringUtils.isNotEmpty(locator.getBasePath())) {
            return FileLocatorUtils.appendPath(getHomeDirectory(), locator.getBasePath());
        }
        return getHomeDirectory();
    }

    /**
     * Gets the home directory. In this directory the strategy searches for files.
     *
     * @return The home directory used by this object.
     */
    public String getHomeDirectory() {
        return homeDirectory;
    }

    /**
     * Returns a flag whether the base path is to be taken into account when searching for a file.
     *
     * @return The flag whether the base path is evaluated
     */
    public boolean isEvaluateBasePath() {
        return evaluateBasePath;
    }

    /**
     * {@inheritDoc} This implementation searches in the home directory for a file described by the passed in
     * {@code FileLocator}. If the locator defines a base path and the {@code evaluateBasePath} property is <strong>true</strong>, a
     * sub directory of the home directory is searched.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator) {
        if (StringUtils.isNotEmpty(locator.getFileName())) {
            final String basePath = getBasePath(locator);
            final File file = FileLocatorUtils.constructFile(basePath, locator.getFileName());
            if (file.isFile()) {
                return check(FileLocatorUtils.convertFileToURL(file));
            }
        }
        return null;
    }
}
