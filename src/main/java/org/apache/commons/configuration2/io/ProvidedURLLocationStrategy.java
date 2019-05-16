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

import java.net.URL;

/**
 * <p>
 * A specialized implementation of {@code FileLocationStrategy} which checks
 * whether a passed in {@link FileLocator} already has a defined URL.
 * </p>
 * <p>
 * {@code FileLocator} objects that have a URL already reference a file in an
 * unambiguous way. Therefore, this strategy just returns the URL of the passed
 * in {@code FileLocator}. It can be used as a first step of the file resolving
 * process. If it fails, more sophisticated attempts for resolving the file can
 * be made.
 * </p>
 *
 * @since 2.0
 */
public class ProvidedURLLocationStrategy implements FileLocationStrategy
{
    /**
     * {@inheritDoc} This implementation just returns the URL stored in the
     * given {@code FileLocator}.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator)
    {
        return locator.getSourceURL();
    }
}
