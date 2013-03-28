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

import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileBasedConfiguration;
import org.apache.commons.configuration.io.FileHandler;

/**
 * <p>
 * A specialized {@code ConfigurationBuilder} implementation which can handle
 * configurations read from a {@link FileHandler}.
 * </p>
 * <p>
 * This class extends its base class by the support of a
 * {@link FileBasedBuilderParametersImpl} object, and especially of the
 * {@link FileHandler} contained in this object. When the builder creates a new
 * object the resulting {@code Configuration} instance is associated with the
 * {@code FileHandler}. If the {@code FileHandler} has a location set, the
 * {@code Configuration} is directly loaded from this location.
 * </p>
 * <p>
 * The {@code FileHandler} is kept by this builder and can be queried later on.
 * It can be used for instance to save the current {@code Configuration} after
 * it was modified. Some care has to be taken when changing the location of the
 * {@code FileHandler}: The new location is recorded and also survives an
 * invocation of the {@code resetResult()} method. However, when the builder's
 * initialization parameters are reset by calling {@code resetParameters()} the
 * location is reset, too.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class FileBasedConfigurationBuilder<T extends FileBasedConfiguration>
        extends BasicConfigurationBuilder<T>
{
    /** Stores the FileHandler associated with the current configuration. */
    private FileHandler currentFileHandler;

    /** A specialized listener for the auto save mechanism. */
    private AutoSaveListener autoSaveListener;

    /** A flag whether the builder's parameters were reset. */
    private boolean resetParameters;

    /**
     * Creates a new instance of {@code FileBasedConfigurationBuilder} which
     * produces result objects of the specified class.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public FileBasedConfigurationBuilder(Class<T> resCls)
    {
        super(resCls);
    }

    /**
     * Creates a new instance of {@code FileBasedConfigurationBuilder} which
     * produces result objects of the specified class and sets initialization
     * parameters.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public FileBasedConfigurationBuilder(Class<T> resCls,
            Map<String, Object> params)
    {
        super(resCls, params);
    }

    /**
     * Creates a new instance of {@code FileBasedConfigurationBuilder} which
     * produces result objects of the specified class and sets initialization
     * parameters and the <em>allowFailOnInit</em> flag.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @param allowFailOnInit the <em>allowFailOnInit</em> flag
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public FileBasedConfigurationBuilder(Class<T> resCls,
            Map<String, Object> params, boolean allowFailOnInit)
    {
        super(resCls, params, allowFailOnInit);
    }

    /**
     * Returns the {@code FileHandler} associated with this builder. If already
     * a result object has been created, this {@code FileHandler} can be used to
     * save it. Otherwise, the {@code FileHandler} from the initialization
     * parameters is returned (which is not associated with a {@code FileBased}
     * object). Result is never <b>null</b>.
     *
     * @return the {@code FileHandler} associated with this builder
     */
    public synchronized FileHandler getFileHandler()
    {
        return (currentFileHandler != null) ? currentFileHandler
                : fetchFileHandlerFromParameters();
    }

    /**
     * {@inheritDoc} This implementation just records the fact that new
     * parameters have been set. This means that the next time a result object
     * is created, the {@code FileHandler} has to be initialized from
     * initialization parameters rather than reusing the existing one.
     */
    @Override
    public synchronized BasicConfigurationBuilder<T> setParameters(
            Map<String, Object> params)
    {
        super.setParameters(params);
        resetParameters = true;
        return this;
    }

    /**
     * Convenience method which saves the associated configuration. This method
     * expects that the managed configuration has already been created and that
     * a valid file location is available in the current {@code FileHandler}.
     * The file handler is then used to store the configuration.
     *
     * @throws ConfigurationException if an error occurs
     */
    public void save() throws ConfigurationException
    {
        getFileHandler().save();
    }

    /**
     * Returns a flag whether auto save mode is currently active.
     *
     * @return <b>true</b> if auto save is enabled, <b>false</b> otherwise
     */
    public synchronized boolean isAutoSave()
    {
        return autoSaveListener != null;
    }

    /**
     * Enables or disables auto save mode. If auto save mode is enabled, every
     * update of the managed configuration causes it to be saved automatically;
     * so changes are directly written to disk.
     *
     * @param enabled <b>true</b> if auto save mode is to be enabled,
     *        <b>false</b> otherwise
     */
    public synchronized void setAutoSave(boolean enabled)
    {
        if (enabled)
        {
            installAutoSaveListener();
        }
        else
        {
            removeAutoSaveListener();
        }
    }

    /**
     * {@inheritDoc} This implementation deals with the creation and
     * initialization of a {@code FileHandler} associated with the new result
     * object.
     */
    @Override
    protected void initResultInstance(T obj) throws ConfigurationException
    {
        super.initResultInstance(obj);
        FileHandler srcHandler =
                (currentFileHandler != null && !resetParameters) ? currentFileHandler
                        : fetchFileHandlerFromParameters();
        currentFileHandler = new FileHandler(obj, srcHandler);

        if (autoSaveListener != null)
        {
            autoSaveListener.updateFileHandler(currentFileHandler);
        }
        initFileHandler(currentFileHandler);
        resetParameters = false;
    }

    /**
     * Initializes the new current {@code FileHandler}. When a new result object
     * is created, a new {@code FileHandler} is created, too, and associated
     * with the result object. This new handler is passed to this method. If a
     * location is defined, the result object is loaded from this location.
     * Note: This method is called from a synchronized block.
     *
     * @param handler the new current {@code FileHandler}
     * @throws ConfigurationException if an error occurs
     */
    protected void initFileHandler(FileHandler handler)
            throws ConfigurationException
    {
        if (handler.isLocationDefined())
        {
            handler.load();
        }
    }

    /**
     * Obtains the {@code FileHandler} from this builder's parameters. If no
     * {@code FileBasedBuilderParametersImpl} object is found in this builder's
     * parameters, a new one is created now and stored. This makes it possible
     * to change the location of the associated file even if no parameters
     * object was provided.
     *
     * @return the {@code FileHandler} from initialization parameters
     */
    private FileHandler fetchFileHandlerFromParameters()
    {
        FileBasedBuilderParametersImpl fileParams =
                FileBasedBuilderParametersImpl.fromParameters(getParameters(),
                        false);
        if (fileParams == null)
        {
            fileParams = new FileBasedBuilderParametersImpl();
            addParameters(fileParams.getParameters());
        }
        return fileParams.getFileHandler();
    }

    /**
     * Installs the listener for the auto save mechanism if it is not yet
     * active.
     */
    private void installAutoSaveListener()
    {
        if (autoSaveListener == null)
        {
            autoSaveListener = new AutoSaveListener(this);
            addConfigurationListener(autoSaveListener);
            autoSaveListener.updateFileHandler(getFileHandler());
        }
    }

    /**
     * Removes the listener for the auto save mechanism if it is currently
     * active.
     */
    private void removeAutoSaveListener()
    {
        if (autoSaveListener != null)
        {
            removeConfigurationListener(autoSaveListener);
            autoSaveListener.updateFileHandler(null);
            autoSaveListener = null;
        }
    }
}
