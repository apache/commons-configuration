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

import org.apache.commons.lang3.StringUtils;

/**
 * A specialized {@code FileLocationStrategy} implementation which searches for files on the class path.
 * <p>
 * This strategy implementation ignores the URL and the base path components of the passed in {@link FileLocator}. It
 * tries to look up the file name on both the class path and the system class path.
 * </p>
 * <p>
 * See {@link AbstractFileLocationStrategy} learn how to grant an deny URL schemes and hosts.
 * </p>
 *
 * @see AbstractFileLocationStrategy
 * @since 2.0
 */
public class ClasspathLocationStrategy extends AbstractFileLocationStrategy {

    /**
     * Builds new instances of {@link ProvidedURLLocationStrategy}.
     *
     * @return A new builder.
     * @since 2.15.0
     */
    public static StrategyBuilder<ClasspathLocationStrategy> builder() {
        return new StrategyBuilder<>(ClasspathLocationStrategy::new);
    }

    /**
     * Constructs a new instance.
     */
    public ClasspathLocationStrategy() {
        // empty
    }

    /**
     * Constructs a new instance.
     *
     * @param builder How to build the instance.
     * @since 2.15.0
     */
    public ClasspathLocationStrategy(final AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    /**
     * {@inheritDoc} This implementation looks up the locator's file name as a resource on the class path.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator) {
        return check(StringUtils.isEmpty(locator.getFileName()) ? null : FileLocatorUtils.getClasspathResource(locator.getFileName()));
    }
}
