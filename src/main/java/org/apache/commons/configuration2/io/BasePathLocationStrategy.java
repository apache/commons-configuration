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
 * A specialized implementation of {@code FileLocationStrategy} which tries to
 * construct a file path from the locator's base path and file name.
 * </p>
 * <p>
 * This strategies ignores the URL stored in the passed in {@link FileLocator}.
 * It generates a path by concatenating the base path (if present) and the file
 * name. If the resulting path points to a valid file, the corresponding URL is
 * returned.
 * </p>
 *
 * @since 2.0
 */
public class BasePathLocationStrategy implements FileLocationStrategy
{
    /**
     * {@inheritDoc} This implementation uses utility methods from
     * {@code FileLocatorUtils} to generate a {@code File} from the locator's
     * base path and file name. If this {@code File} exists, its URL is
     * returned.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator)
    {
        if (StringUtils.isNotEmpty(locator.getFileName()))
        {
            final File file =
                    FileLocatorUtils.constructFile(locator.getBasePath(),
                            locator.getFileName());
            if (file.isFile())
            {
                return FileLocatorUtils.convertFileToURL(file);
            }
        }

        return null;
    }
}
