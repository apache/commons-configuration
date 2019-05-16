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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Abstract layer to allow various types of file systems.
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public abstract class FileSystem
{
    /** Constant for the default logger. */
    private static final ConfigurationLogger DEFAULT_LOG = ConfigurationLogger.newDummyLogger();

    /** The Logger */
    private volatile ConfigurationLogger log;

    /** FileSystem options provider */
    private volatile FileOptionsProvider optionsProvider;

    /**
     * Returns the logger used by this FileSystem.
     *
     * @return the logger
     */
    public ConfigurationLogger getLogger()
    {
        final ConfigurationLogger result = log;
        return (result != null) ? result : DEFAULT_LOG;
    }

    /**
     * Allows setting the logger to be used by this FileSystem. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used. Passing in a <b>null</b> argument
     * disables logging.
     *
     * @param log the new logger
     */
    public void setLogger(final ConfigurationLogger log)
    {
        this.log = log;
    }

    /**
     * Set the FileOptionsProvider
     * @param provider The FileOptionsProvider
     */
    public void setFileOptionsProvider(final FileOptionsProvider provider)
    {
        this.optionsProvider = provider;
    }

    public FileOptionsProvider getFileOptionsProvider()
    {
        return this.optionsProvider;
    }

    public abstract InputStream getInputStream(URL url) throws ConfigurationException;

    public abstract OutputStream getOutputStream(URL url) throws ConfigurationException;

    public abstract OutputStream getOutputStream(File file) throws ConfigurationException;

    public abstract String getPath(File file, URL url, String basePath, String fileName);

    public abstract String getBasePath(String path);

    public abstract String getFileName(String path);

    public abstract URL locateFromURL(String basePath, String fileName);

    public abstract URL getURL(String basePath, String fileName) throws MalformedURLException;
}
