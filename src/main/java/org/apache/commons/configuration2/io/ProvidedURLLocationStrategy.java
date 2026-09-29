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
 * A specialized implementation of {@code FileLocationStrategy} which checks whether a passed in {@link FileLocator}
 * already has a defined URL.
 * <p>
 * {@link FileLocator} objects that have a URL already reference a file in an unambiguous way. Therefore, this strategy
 * just returns the URL of the passed in {@code FileLocator}. It can be used as a first step of the file resolving
 * process. If it fails, more sophisticated attempts for resolving the file can be made.
 * </p>
 * <p>
 * See {@link AbstractFileLocationStrategy} learn how to grant an deny URL schemes and hosts.
 * </p>
 *
 * @see AbstractFileLocationStrategy
 * @since 2.0
 */
public class ProvidedURLLocationStrategy extends AbstractFileLocationStrategy {

    /**
     * Builds new instances of {@link ProvidedURLLocationStrategy}.
     *
     * @return A new builder.
     * @since 2.15.0
     */
    public static StrategyBuilder<ProvidedURLLocationStrategy> builder() {
        return new StrategyBuilder<>(ProvidedURLLocationStrategy::new);
    }

    /**
     * Constructs a new instance where URL resources are bound by {@link AbstractFileLocationStrategy.AbstractBuilder}.
     */
    public ProvidedURLLocationStrategy() {
    }

    /**
     * Constructs a new instance where URL resources are bound by {@link AbstractFileLocationStrategy.AbstractBuilder}.
     *
     * @param builder How to build the instance.
     * @since 2.15.0
     */
    public ProvidedURLLocationStrategy(final AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    /**
     * {@inheritDoc} This implementation just returns the URL stored in the given {@code FileLocator}.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator) {
        return check(locator.getSourceURL());
    }

}
