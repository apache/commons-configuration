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

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized {@code FileLocationStrategy} implementation which searches for
 * files on the class path.
 * </p>
 * <p>
 * This strategy implementation ignores the URL and the base path components of
 * the passed in {@link FileLocator}. It tries to look up the file name on both
 * the class path and the system class path.
 * </p>
 *
 * @since 2.0
 */
public class ClasspathLocationStrategy implements FileLocationStrategy
{
    /**
     * {@inheritDoc} This implementation looks up the locator's file name as a
     * resource on the class path.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator)
    {
        return StringUtils.isEmpty(locator.getFileName()) ? null
                : FileLocatorUtils.locateFromClasspath(locator.getFileName());
    }
}
