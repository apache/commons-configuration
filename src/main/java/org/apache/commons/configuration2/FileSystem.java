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

package org.apache.commons.configuration2;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract layer to allow various types of file systems.
 *
 * @since 1.7
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public abstract class FileSystem
{
    /** The name of the system property that can be used to set the file system class name */
    private static final String FILE_SYSTEM = "org.apache.commons.configuration.filesystem";

    /** The default file system */
    private static FileSystem fileSystem;

    /** The Logger */
    private Logger log;

    /** FileSystem options provider */
    private FileOptionsProvider optionsProvider;

    public FileSystem()
    {
        setLogger(null);
    }

    /**
     * Returns the logger used by this FileSystem.
     *
     * @return the logger
     */
    public Logger getLogger()
    {
        return log;
    }

    /**
     * Allows to set the logger to be used by this FileSystem. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used.
     *
     * @param log the new logger
     */
    public void setLogger(Logger log)
    {
        if (log == null)
        {
            // create a NoOp logger
            log = Logger.getLogger(getClass().getName() + "." + hashCode());
            log.setLevel(Level.OFF);
        }

        this.log = log;
    }

    static
    {
        String fsClassName = System.getProperty(FILE_SYSTEM);
        if (fsClassName != null)
        {
            Logger log = Logger.getLogger(FileSystem.class.getName());

            try
            {
                Class clazz = Class.forName(fsClassName);
                if (FileSystem.class.isAssignableFrom(clazz))
                {
                    fileSystem = (FileSystem) clazz.newInstance();
                    if (log.isLoggable(Level.FINE))
                    {
                        log.fine("Using " + fsClassName);
                    }
                }
            }
            catch (InstantiationException ex)
            {
                log.log(Level.SEVERE, "Unable to create " + fsClassName, ex);
            }
            catch (IllegalAccessException ex)
            {
                log.log(Level.SEVERE, "Unable to create " + fsClassName, ex);
            }
            catch (ClassNotFoundException ex)
            {
                log.log(Level.SEVERE, "Unable to create " + fsClassName, ex);
            }
        }

        if (fileSystem == null)
        {
            fileSystem = new DefaultFileSystem();
        }
    }

    /**
     * Set the FileSystem to use.
     * @param fs The FileSystem
     * @throws NullPointerException if fs is null.
     */
    public static void setDefaultFileSystem(FileSystem fs) throws NullPointerException
    {
        if (fs == null)
        {
            throw new NullPointerException("A FileSystem implementation is required");
        }
        fileSystem = fs;
    }

    /**
     * Reset the FileSystem to the default.
     */
    public static void resetDefaultFileSystem()
    {
        fileSystem = new DefaultFileSystem();
    }

    /**
     * Retrieve the FileSystem being used.
     * @return The FileSystem.
     */
    public static FileSystem getDefaultFileSystem()
    {
        return fileSystem;
    }

    /**
     * Set the FileOptionsProvider
     * @param provider The FileOptionsProvider
     */
    public void setFileOptionsProvider(FileOptionsProvider provider)
    {
        this.optionsProvider = provider;
    }

    public FileOptionsProvider getFileOptionsProvider()
    {
        return this.optionsProvider;
    }

    public abstract InputStream getInputStream(String basePath, String fileName)
            throws ConfigurationException;

    public abstract InputStream getInputStream(URL url) throws ConfigurationException;

    public abstract OutputStream getOutputStream(URL url) throws ConfigurationException;

    public abstract OutputStream getOutputStream(File file) throws ConfigurationException;

    public abstract String getPath(File file, URL url, String basePath, String fileName);

    public abstract String getBasePath(String path);

    public abstract String getFileName(String path);

    public abstract URL locateFromURL(String basePath, String fileName);

    public abstract URL getURL(String basePath, String fileName) throws MalformedURLException;
}
