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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLPropertiesConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

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
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class FileBasedConfigurationBuilder<T extends FileBasedConfiguration>
        extends BasicConfigurationBuilder<T>
{
    /** A map for storing default encodings for specific configuration classes. */
    private static final Map<Class<?>, String> DEFAULT_ENCODINGS =
            initializeDefaultEncodings();

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
    public FileBasedConfigurationBuilder(final Class<? extends T> resCls)
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
    public FileBasedConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params)
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
    public FileBasedConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params, final boolean allowFailOnInit)
    {
        super(resCls, params, allowFailOnInit);
    }

    /**
     * Returns the default encoding for the specified configuration class. If an
     * encoding has been set for the specified class (or one of its super
     * classes), it is returned. Otherwise, result is <b>null</b>.
     *
     * @param configClass the configuration class in question
     * @return the default encoding for this class (may be <b>null</b>)
     */
    public static String getDefaultEncoding(final Class<?> configClass)
    {
        String enc = DEFAULT_ENCODINGS.get(configClass);
        if (enc != null || configClass == null)
        {
            return enc;
        }

        final List<Class<?>> superclasses =
                ClassUtils.getAllSuperclasses(configClass);
        for (final Class<?> cls : superclasses)
        {
            enc = DEFAULT_ENCODINGS.get(cls);
            if (enc != null)
            {
                return enc;
            }
        }

        final List<Class<?>> interfaces = ClassUtils.getAllInterfaces(configClass);
        for (final Class<?> cls : interfaces)
        {
            enc = DEFAULT_ENCODINGS.get(cls);
            if (enc != null)
            {
                return enc;
            }
        }

        return null;
    }

    /**
     * Sets a default encoding for a specific configuration class. This encoding
     * is used if an instance of this configuration class is to be created and
     * no encoding has been set in the parameters object for this builder. The
     * encoding passed here not only applies to the specified class but also to
     * its sub classes. If the encoding is <b>null</b>, it is removed.
     *
     * @param configClass the name of the configuration class (must not be
     *        <b>null</b>)
     * @param encoding the default encoding for this class
     * @throws IllegalArgumentException if the class is <b>null</b>
     */
    public static void setDefaultEncoding(final Class<?> configClass, final String encoding)
    {
        if (configClass == null)
        {
            throw new IllegalArgumentException(
                    "Configuration class must not be null!");
        }

        if (encoding == null)
        {
            DEFAULT_ENCODINGS.remove(configClass);
        }
        else
        {
            DEFAULT_ENCODINGS.put(configClass, encoding);
        }
    }

    /**
     * {@inheritDoc} This method is overridden here to change the result type.
     */
    @Override
    public FileBasedConfigurationBuilder<T> configure(
            final BuilderParameters... params)
    {
        super.configure(params);
        return this;
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
            final Map<String, Object> params)
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
    public synchronized void setAutoSave(final boolean enabled)
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
    protected void initResultInstance(final T obj) throws ConfigurationException
    {
        super.initResultInstance(obj);
        final FileHandler srcHandler =
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
    protected void initFileHandler(final FileHandler handler)
            throws ConfigurationException
    {
        initEncoding(handler);
        if (handler.isLocationDefined())
        {
            handler.locate();
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
            addEventListener(ConfigurationEvent.ANY, autoSaveListener);
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
            removeEventListener(ConfigurationEvent.ANY, autoSaveListener);
            autoSaveListener.updateFileHandler(null);
            autoSaveListener = null;
        }
    }

    /**
     * Initializes the encoding of the specified file handler. If already an
     * encoding is set, it is used. Otherwise, the default encoding for the
     * result configuration class is obtained and set.
     *
     * @param handler the handler to be initialized
     */
    private void initEncoding(final FileHandler handler)
    {
        if (StringUtils.isEmpty(handler.getEncoding()))
        {
            final String encoding = getDefaultEncoding(getResultClass());
            if (encoding != null)
            {
                handler.setEncoding(encoding);
            }
        }
    }

    /**
     * Creates a map with default encodings for configuration classes and
     * populates it with default entries.
     *
     * @return the map with default encodings
     */
    private static Map<Class<?>, String> initializeDefaultEncodings()
    {
        final Map<Class<?>, String> enc = new ConcurrentHashMap<>();
        enc.put(PropertiesConfiguration.class,
                PropertiesConfiguration.DEFAULT_ENCODING);
        enc.put(XMLPropertiesConfiguration.class,
                XMLPropertiesConfiguration.DEFAULT_ENCODING);
        return enc;
    }
}
