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
package org.apache.commons.configuration.io;

import java.net.URL;

/**
 * <p>
 * An interface describing the location of a file.
 * </p>
 * <p>
 * An object implementing this interface can be used to obtain information about
 * the storage location of a file. It allows querying the typical components
 * used by {@link FileHandler} to locate a file.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface FileLocator
{
    /**
     * Returns the name of the represented file.
     *
     * @return the file name only
     */
    String getFileName();

    /**
     * Returns the base path of the represented file. This is typically the
     * directory in which the file is stored.
     *
     * @return the base path
     */
    String getBasePath();

    /**
     * Returns a full URL to the represented file.
     *
     * @return the URL pointing to the file
     */
    URL getSourceURL();

    /**
     * Returns the file system which is used for resolving files to be loaded.
     *
     * @return the {@code FileSystem}
     */
    FileSystem getFileSystem();

    /**
     * Returns the encoding of the represented file if known. Result can be
     * <b>null</b>, then default encoding should be assumed.
     *
     * @return the encoding
     */
    String getEncoding();
}
