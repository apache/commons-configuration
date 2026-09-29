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

import java.net.URL;

/**
 * A specialized implementation of {@code FileLocationStrategy} which uses the passed in {@link FileSystem} to locate a
 * file.
 * <p>
 * This strategy implementation ignores the URL of the passed in {@link FileLocator} and operates on its base path and
 * file name. These properties are passed to the {@code locateFromURL()} method of {@code FileSystem}. So the burden of
 * resolving the file is delegated to the {@code FileSystem}.
 * </p>
 * <p>
 * See {@link AbstractFileLocationStrategy} learn how to grant an deny URL schemes and hosts.
 * </p>
 *
 * @see AbstractFileLocationStrategy
 * @since 2.0
 */
public class FileSystemLocationStrategy extends AbstractFileLocationStrategy {

    /**
     * Builds new instances of {@link ProvidedURLLocationStrategy}.
     *
     * @return A new builder.
     * @since 2.15.0
     */
    public static StrategyBuilder<FileSystemLocationStrategy> builder() {
        return new StrategyBuilder<>(FileSystemLocationStrategy::new);
    }

    /**
     * Constructs a new instance.
     */
    public FileSystemLocationStrategy() {
        // empty
    }

    /**
     * Constructs a new instance.
     *
     * @param builder How to build the instance.
     * @since 2.15.0
     */
    public FileSystemLocationStrategy(final AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    /**
     * {@inheritDoc} This implementation delegates to the {@code FileSystem}.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator) {
        return check(fileSystem.locateFromURL(locator.getBasePath(), locator.getFileName()));
    }
}
