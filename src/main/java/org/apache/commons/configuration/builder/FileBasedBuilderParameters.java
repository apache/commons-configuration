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
package org.apache.commons.configuration.builder;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.io.FileHandler;

/**
 * <p>
 * An implementation of {@code BuilderParameters} which contains parameters
 * related to {@code Configuration} implementations that are loaded from files.
 * </p>
 * <p>
 * The parameters defined here are interpreted by builder implementations that
 * can deal with file-based configurations. Note that these parameters are
 * typically no initialization properties of configuration objects (i.e. they
 * are not passed to set methods after the creation of the result
 * configuration). Rather, the parameters object is stored as a whole in the
 * builder's map with initialization parameters and can be accessed from there.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class FileBasedBuilderParameters implements BuilderParameters
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY =
            BasicConfigurationBuilder.RESERVED_PARAMETER
                    + FileBasedBuilderParameters.class.getName();

    /**
     * Stores the associated file handler for the location of the configuration.
     */
    private final FileHandler fileHandler;

    /** The refresh delay for reloading support. */
    private long reloadingRefreshDelay;

    /**
     * Creates a new instance of {@code FileBasedBuilderParameters} with an
     * uninitialized {@code FileHandler} object.
     */
    public FileBasedBuilderParameters()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code FileBasedBuilderParameters} and
     * associates it with the given {@code FileHandler} object. If the handler
     * is <b>null</b>, a new handler instance is created.
     *
     * @param handler the associated {@code FileHandler} (can be <b>null</b>)
     */
    public FileBasedBuilderParameters(FileHandler handler)
    {
        fileHandler = (handler != null) ? handler : new FileHandler();
    }

    /**
     * Looks up an instance of this class in the specified parameters map. This
     * is equivalent to {@code fromParameters(params, false};}
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @return the instance obtained from the map or <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static FileBasedBuilderParameters fromParameters(
            Map<String, Object> params)
    {
        return fromParameters(params, false);
    }

    /**
     * Looks up an instance of this class in the specified parameters map and
     * optionally creates a new one if none is found. This method can be used to
     * obtain an instance of this class which has been stored in a parameters
     * map. It is compatible with the {@code getParameters()} method.
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @param createIfMissing determines the behavior if no instance is found in
     *        the map; if <b>true</b>, a new instance with default settings is
     *        created; if <b>false</b>, <b>null</b> is returned
     * @return the instance obtained from the map or <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static FileBasedBuilderParameters fromParameters(
            Map<String, Object> params, boolean createIfMissing)
    {
        FileBasedBuilderParameters instance =
                (FileBasedBuilderParameters) params.get(PARAM_KEY);
        if (instance == null && createIfMissing)
        {
            instance = new FileBasedBuilderParameters();
        }
        return instance;
    }

    /**
     * Returns the {@code FileHandler} associated with this parameters object.
     *
     * @return the {@code FileHandler}
     */
    public FileHandler getFileHandler()
    {
        return fileHandler;
    }

    /**
     * Returns the refresh delay for reloading support.
     *
     * @return the refresh delay (in milliseconds)
     */
    public long getReloadingRefreshDelay()
    {
        return reloadingRefreshDelay;
    }

    /**
     * Sets the refresh delay for reloading support
     *
     * @param reloadingRefreshDelay the refresh delay (in milliseconds)
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setReloadingRefreshDelay(
            long reloadingRefreshDelay)
    {
        this.reloadingRefreshDelay = reloadingRefreshDelay;
        return this;
    }

    /**
     * Sets the location of the associated {@code FileHandler} as a {@code File}
     * object.
     *
     * @param file the {@code File} location
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setFile(File file)
    {
        getFileHandler().setFile(file);
        return this;
    }

    /**
     * Sets the location of the associated {@code FileHandler} as a {@code URL}
     * object.
     *
     * @param url the {@code URL} location
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setURL(URL url)
    {
        getFileHandler().setURL(url);
        return this;
    }

    /**
     * Sets the location of the associated {@code FileHandler} as an absolute
     * file path.
     *
     * @param path the path location
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setPath(String path)
    {
        getFileHandler().setPath(path);
        return this;
    }

    /**
     * Sets the file name of the associated {@code FileHandler}.
     *
     * @param name the file name
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setFileName(String name)
    {
        getFileHandler().setFileName(name);
        return this;
    }

    /**
     * Sets the base path of the associated {@code FileHandler}.
     *
     * @param path the base path
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setBasePath(String path)
    {
        getFileHandler().setBasePath(path);
        return this;
    }

    /**
     * Sets the {@code FileSystem} of the associated {@code FileHandler}.
     *
     * @param fs the {@code FileSystem}
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setFileSystem(FileSystem fs)
    {
        getFileHandler().setFileSystem(fs);
        return this;
    }

    /**
     * Sets the encoding of the associated {@code FileHandler}.
     *
     * @param enc the encoding
     * @return a reference to this object for method chaining
     */
    public FileBasedBuilderParameters setEncoding(String enc)
    {
        getFileHandler().setEncoding(enc);
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns a map which contains this
     * object itself under a specific key. The static {@code fromParameters()}
     * method can be used to extract an instance from a parameters map.
     */
    public Map<String, Object> getParameters()
    {
        return Collections.singletonMap(PARAM_KEY, (Object) this);
    }
}
