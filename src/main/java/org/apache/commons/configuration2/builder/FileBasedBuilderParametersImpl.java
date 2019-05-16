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
package org.apache.commons.configuration2.builder;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystem;

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
 * @since 2.0
 */
public class FileBasedBuilderParametersImpl extends BasicBuilderParameters
        implements FileBasedBuilderProperties<FileBasedBuilderParametersImpl>
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY = RESERVED_PARAMETER_PREFIX
            + "fileBased";

    /** Property name for the reloading refresh delay. */
    private static final String PROP_REFRESH_DELAY = "reloadingRefreshDelay";

    /** Property name of the reloading detector factory. */
    private static final String PROP_DETECTOR_FACTORY =
            "reloadingDetectorFactory";

    /**
     * Stores the associated file handler for the location of the configuration.
     */
    private FileHandler fileHandler;

    /** The factory for reloading detectors. */
    private ReloadingDetectorFactory reloadingDetectorFactory;

    /** The refresh delay for reloading support. */
    private Long reloadingRefreshDelay;

    /**
     * Creates a new instance of {@code FileBasedBuilderParametersImpl} with an
     * uninitialized {@code FileHandler} object.
     */
    public FileBasedBuilderParametersImpl()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code FileBasedBuilderParametersImpl} and
     * associates it with the given {@code FileHandler} object. If the handler
     * is <b>null</b>, a new handler instance is created.
     *
     * @param handler the associated {@code FileHandler} (can be <b>null</b>)
     */
    public FileBasedBuilderParametersImpl(final FileHandler handler)
    {
        fileHandler = (handler != null) ? handler : new FileHandler();
    }

    /**
     * Looks up an instance of this class in the specified parameters map. This
     * is equivalent to {@code fromParameters(params, false};}
     *
     * @param params the map with parameters (must not be <b>null</b>
     * @return the instance obtained from the map or <b>null</b>
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    public static FileBasedBuilderParametersImpl fromParameters(
            final Map<String, ?> params)
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
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    public static FileBasedBuilderParametersImpl fromParameters(
            final Map<String, ?> params, final boolean createIfMissing)
    {
        if (params == null)
        {
            throw new IllegalArgumentException(
                    "Parameters map must not be null!");
        }

        FileBasedBuilderParametersImpl instance =
                (FileBasedBuilderParametersImpl) params.get(PARAM_KEY);
        if (instance == null && createIfMissing)
        {
            instance = new FileBasedBuilderParametersImpl();
        }
        return instance;
    }

    /**
     * Creates a new {@code FileBasedBuilderParametersImpl} object from the
     * content of the given map. While {@code fromParameters()} expects that an
     * object already exists and is stored in the given map, this method creates
     * a new instance based on the content of the map. The map can contain
     * properties of a {@code FileHandler} and some additional settings which
     * are stored directly in the newly created object. If the map is
     * <b>null</b>, an uninitialized instance is returned.
     *
     * @param map the map with properties (must not be <b>null</b>)
     * @return the newly created instance
     * @throws ClassCastException if the map contains invalid data
     */
    public static FileBasedBuilderParametersImpl fromMap(final Map<String, ?> map)
    {
        final FileBasedBuilderParametersImpl params =
                new FileBasedBuilderParametersImpl(FileHandler.fromMap(map));
        if (map != null)
        {
            params.setReloadingRefreshDelay((Long) map.get(PROP_REFRESH_DELAY));
            params.setReloadingDetectorFactory((ReloadingDetectorFactory) map
                    .get(PROP_DETECTOR_FACTORY));
        }
        return params;
    }

    /**
     * {@inheritDoc} This implementation takes some properties defined in this
     * class into account.
     */
    @Override
    public void inheritFrom(final Map<String, ?> source)
    {
        super.inheritFrom(source);

        final FileBasedBuilderParametersImpl srcParams = fromParameters(source);
        if (srcParams != null)
        {
            setFileSystem(srcParams.getFileHandler().getFileSystem());
            setLocationStrategy(
                    srcParams.getFileHandler().getLocationStrategy());
            if (srcParams.getFileHandler().getEncoding() != null)
            {
                setEncoding(srcParams.getFileHandler().getEncoding());
            }
            if (srcParams.getReloadingDetectorFactory() != null)
            {
                setReloadingDetectorFactory(
                        srcParams.getReloadingDetectorFactory());
            }
            if (srcParams.getReloadingRefreshDelay() != null)
            {
                setReloadingRefreshDelay(srcParams.getReloadingRefreshDelay());
            }
        }
    }

    /**
     * Returns the {@code FileHandler} managed by this object. This object is
     * updated every time the file location is changed.
     *
     * @return the managed {@code FileHandler}
     */
    public FileHandler getFileHandler()
    {
        return fileHandler;
    }

    /**
     * Returns the refresh delay for reload operations. Result may be
     * <b>null</b> if this value has not been set.
     *
     * @return the reloading refresh delay
     */
    public Long getReloadingRefreshDelay()
    {
        return reloadingRefreshDelay;
    }

    @Override
    public FileBasedBuilderParametersImpl setReloadingRefreshDelay(
            final Long reloadingRefreshDelay)
    {
        this.reloadingRefreshDelay = reloadingRefreshDelay;
        return this;
    }

    /**
     * Returns the {@code ReloadingDetectorFactory}. Result may be <b>null</b>
     * which means that the default factory is to be used.
     *
     * @return the {@code ReloadingDetectorFactory}
     */
    public ReloadingDetectorFactory getReloadingDetectorFactory()
    {
        return reloadingDetectorFactory;
    }

    @Override
    public FileBasedBuilderParametersImpl setReloadingDetectorFactory(
            final ReloadingDetectorFactory reloadingDetectorFactory)
    {
        this.reloadingDetectorFactory = reloadingDetectorFactory;
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setFile(final File file)
    {
        getFileHandler().setFile(file);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setURL(final URL url)
    {
        getFileHandler().setURL(url);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setPath(final String path)
    {
        getFileHandler().setPath(path);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setFileName(final String name)
    {
        getFileHandler().setFileName(name);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setBasePath(final String path)
    {
        getFileHandler().setBasePath(path);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setFileSystem(final FileSystem fs)
    {
        getFileHandler().setFileSystem(fs);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setLocationStrategy(
            final FileLocationStrategy strategy)
    {
        getFileHandler().setLocationStrategy(strategy);
        return this;
    }

    @Override
    public FileBasedBuilderParametersImpl setEncoding(final String enc)
    {
        getFileHandler().setEncoding(enc);
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns a map which contains this
     * object itself under a specific key. The static {@code fromParameters()}
     * method can be used to extract an instance from a parameters map. Of
     * course, the properties inherited from the base class are also added to
     * the result map.
     */
    @Override
    public Map<String, Object> getParameters()
    {
        final Map<String, Object> params = super.getParameters();
        params.put(PARAM_KEY, this);
        return params;
    }

    /**
     * {@inheritDoc} This implementation also creates a copy of the
     * {@code FileHandler}.
     */
    @Override
    public FileBasedBuilderParametersImpl clone()
    {
        final FileBasedBuilderParametersImpl copy =
                (FileBasedBuilderParametersImpl) super.clone();
        copy.fileHandler =
                new FileHandler(fileHandler.getContent(), fileHandler);
        return copy;
    }
}
