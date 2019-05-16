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
package org.apache.commons.configuration2.builder.fluent;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * <p>
 * A convenience class which simplifies the creation of standard configurations
 * and their builders.
 * </p>
 * <p>
 * Complex initializations of configuration builders can be done in a pretty
 * straight-forward way by making use of the provided fluent API. However, if
 * only default settings are used (and maybe a configuration file to be loaded
 * has to be specified), this approach tends to become a bit verbose. This class
 * was introduced to simplify the creation of configuration objects in such
 * cases. It offers a bunch of methods which allow the creation of some standard
 * configuration classes with default settings passing in only a minimum
 * required parameters.
 * </p>
 * <p>
 * An an example consider the creation of a {@code PropertiesConfiguration}
 * object from a file. Using a builder, code like the following one would have
 * to be written:
 * </p>
 * <pre>
 * Parameters params = new Parameters();
 * FileBasedConfigurationBuilder&lt;PropertiesConfiguration&gt; builder =
 *         new FileBasedConfigurationBuilder&lt;PropertiesConfiguration&gt;(
 *                 PropertiesConfiguration.class).configure(params.fileBased()
 *                 .setFile(new File(&quot;config.properties&quot;)));
 * PropertiesConfiguration config = builder.getConfiguration();
 * </pre>
 * <p>
 * With a convenience method of {@code Configurations} the same can be achieved
 * with the following:
 * </p>
 * <pre>
 * Configurations configurations = new Configurations();
 * PropertiesConfiguration config = configurations.properties(new File(
 *         &quot;config.properties&quot;));
 * </pre>
 * <p>
 * There are similar methods for constructing builder objects from which
 * configurations can then be obtained.
 * </p>
 * <p>
 * This class is thread-safe. A single instance can be created by an application
 * and used in a central way to create configuration objects. When an instance
 * is created a {@link Parameters} instance can be passed in. Otherwise, a
 * default instance is created. In any case, the {@code Parameters} instance
 * associated with a {@code Configurations} object can be used to define default
 * settings for the configurations to be created.
 * </p>
 *
 * @since 2.0
 * @see org.apache.commons.configuration2.builder.DefaultParametersManager
 */
public class Configurations
{
    /** The parameters object associated with this instance. */
    private final Parameters parameters;

    /**
     * Creates a new {@code Configurations} instance with default settings.
     */
    public Configurations()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code Configurations} and initializes it with
     * the specified {@code Parameters} object.
     *
     * @param params the {@code Parameters} (may be <b>null</b>, then a default
     *        instance is created)
     */
    public Configurations(final Parameters params)
    {
        parameters = (params != null) ? params : new Parameters();
    }

    /**
     * Returns the {@code Parameters} instance associated with this object.
     *
     * @return the associated {@code Parameters} object
     */
    public Parameters getParameters()
    {
        return parameters;
    }

    /**
     * Creates a {@code FileBasedConfigurationBuilder} for the specified
     * configuration class and initializes it with the file to be loaded.
     *
     * @param configClass the configuration class
     * @param file the file to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return the new {@code FileBasedConfigurationBuilder}
     */
    public <T extends FileBasedConfiguration> FileBasedConfigurationBuilder<T> fileBasedBuilder(
            final Class<T> configClass, final File file)
    {
        return createFileBasedBuilder(configClass, fileParams(file));
    }

    /**
     * Creates a {@code FileBasedConfigurationBuilder} for the specified
     * configuration class and initializes it with the URL to the file to be
     * loaded.
     *
     * @param configClass the configuration class
     * @param url the URL to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return the new {@code FileBasedConfigurationBuilder}
     */
    public <T extends FileBasedConfiguration> FileBasedConfigurationBuilder<T> fileBasedBuilder(
            final Class<T> configClass, final URL url)
    {
        return createFileBasedBuilder(configClass, fileParams(url));
    }

    /**
     * Creates a {@code FileBasedConfigurationBuilder} for the specified
     * configuration class and initializes it with the path to the file to be
     * loaded.
     *
     * @param configClass the configuration class
     * @param path the path to the file to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return the new {@code FileBasedConfigurationBuilder}
     */
    public <T extends FileBasedConfiguration> FileBasedConfigurationBuilder<T> fileBasedBuilder(
            final Class<T> configClass, final String path)
    {
        return createFileBasedBuilder(configClass, fileParams(path));
    }

    /**
     * Creates an instance of the specified file-based configuration class from
     * the content of the given file. This is a convenience method which can be
     * used if no builder is needed for managing the configuration object.
     * (Although, behind the scenes a builder is created).
     *
     * @param configClass the configuration class
     * @param file the file to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return a {@code FileBasedConfiguration} object initialized from this
     *         file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public <T extends FileBasedConfiguration> T fileBased(final Class<T> configClass,
            final File file) throws ConfigurationException
    {
        return fileBasedBuilder(configClass, file).getConfiguration();
    }

    /**
     * Creates an instance of the specified file-based configuration class from
     * the content of the given URL. This is a convenience method which can be
     * used if no builder is needed for managing the configuration object.
     * (Although, behind the scenes a builder is created).
     *
     * @param configClass the configuration class
     * @param url the URL to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return a {@code FileBasedConfiguration} object initialized from this
     *         file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public <T extends FileBasedConfiguration> T fileBased(final Class<T> configClass,
            final URL url) throws ConfigurationException
    {
        return fileBasedBuilder(configClass, url).getConfiguration();
    }

    /**
     * Creates an instance of the specified file-based configuration class from
     * the content of the file identified by the given path. This is a
     * convenience method which can be used if no builder is needed for managing
     * the configuration object. (Although, behind the scenes a builder is
     * created).
     *
     * @param configClass the configuration class
     * @param path the path to the file to be loaded
     * @param <T> the type of the configuration to be constructed
     * @return a {@code FileBasedConfiguration} object initialized from this
     *         file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public <T extends FileBasedConfiguration> T fileBased(final Class<T> configClass,
            final String path) throws ConfigurationException
    {
        return fileBasedBuilder(configClass, path).getConfiguration();
    }

    /**
     * Creates a builder for a {@code PropertiesConfiguration} and initializes
     * it with the given file to be loaded.
     *
     * @param file the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder(
            final File file)
    {
        return fileBasedBuilder(PropertiesConfiguration.class, file);
    }

    /**
     * Creates a builder for a {@code PropertiesConfiguration} and initializes
     * it with the given URL to be loaded.
     *
     * @param url the URL to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder(
            final URL url)
    {
        return fileBasedBuilder(PropertiesConfiguration.class, url);
    }

    /**
     * Creates a builder for a {@code PropertiesConfiguration} and initializes
     * it with the given path to the file to be loaded.
     *
     * @param path the path to the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<PropertiesConfiguration> propertiesBuilder(
            final String path)
    {
        return fileBasedBuilder(PropertiesConfiguration.class, path);
    }

    /**
     * Creates a {@code PropertiesConfiguration} instance from the content of
     * the given file. This is a convenience method which can be used if no
     * builder is needed for managing the configuration object. (Although,
     * behind the scenes a builder is created).
     *
     * @param file the file to be loaded
     * @return a {@code PropertiesConfiguration} object initialized from this
     *         file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public PropertiesConfiguration properties(final File file)
            throws ConfigurationException
    {
        return propertiesBuilder(file).getConfiguration();
    }

    /**
     * Creates a {@code PropertiesConfiguration} instance from the content of
     * the given URL. This is a convenience method which can be used if no
     * builder is needed for managing the configuration object. (Although,
     * behind the scenes a builder is created).
     *
     * @param url the URL to be loaded
     * @return a {@code PropertiesConfiguration} object initialized from this
     *         URL
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public PropertiesConfiguration properties(final URL url)
            throws ConfigurationException
    {
        return propertiesBuilder(url).getConfiguration();
    }

    /**
     * Creates a {@code PropertiesConfiguration} instance from the content of
     * the file identified by the given path. This is a convenience method which
     * can be used if no builder is needed for managing the configuration
     * object. (Although, behind the scenes a builder is created).
     *
     * @param path the path to the file to be loaded
     * @return a {@code PropertiesConfiguration} object initialized from this
     *         path
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public PropertiesConfiguration properties(final String path)
            throws ConfigurationException
    {
        return propertiesBuilder(path).getConfiguration();
    }

    /**
     * Creates a builder for a {@code XMLConfiguration} and initializes it with
     * the given file to be loaded.
     *
     * @param file the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<XMLConfiguration> xmlBuilder(final File file)
    {
        return fileBasedBuilder(XMLConfiguration.class, file);
    }

    /**
     * Creates a builder for a {@code XMLConfiguration} and initializes it with
     * the given URL to be loaded.
     *
     * @param url the URL to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<XMLConfiguration> xmlBuilder(final URL url)
    {
        return fileBasedBuilder(XMLConfiguration.class, url);
    }

    /**
     * Creates a builder for a {@code XMLConfiguration} and initializes it with
     * the given path to the file to be loaded.
     *
     * @param path the path to the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<XMLConfiguration> xmlBuilder(
            final String path)
    {
        return fileBasedBuilder(XMLConfiguration.class, path);
    }

    /**
     * Creates a {@code XMLConfiguration} instance from the content of the given
     * file. This is a convenience method which can be used if no builder is
     * needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param file the file to be loaded
     * @return a {@code XMLConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public XMLConfiguration xml(final File file) throws ConfigurationException
    {
        return xmlBuilder(file).getConfiguration();
    }

    /**
     * Creates a {@code XMLConfiguration} instance from the content of the given
     * URL. This is a convenience method which can be used if no builder is
     * needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param url the URL to be loaded
     * @return a {@code XMLConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public XMLConfiguration xml(final URL url) throws ConfigurationException
    {
        return xmlBuilder(url).getConfiguration();
    }

    /**
     * Creates a {@code XMLConfiguration} instance from the content of the file
     * identified by the given path. This is a convenience method which can be
     * used if no builder is needed for managing the configuration object.
     * (Although, behind the scenes a builder is created).
     *
     * @param path the path to the file to be loaded
     * @return a {@code XMLConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public XMLConfiguration xml(final String path) throws ConfigurationException
    {
        return xmlBuilder(path).getConfiguration();
    }

    /**
     * Creates a builder for a {@code INIConfiguration} and initializes it with
     * the given file to be loaded.
     *
     * @param file the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<INIConfiguration> iniBuilder(final File file)
    {
        return fileBasedBuilder(INIConfiguration.class, file);
    }

    /**
     * Creates a builder for a {@code INIConfiguration} and initializes it with
     * the given URL to be loaded.
     *
     * @param url the URL to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<INIConfiguration> iniBuilder(final URL url)
    {
        return fileBasedBuilder(INIConfiguration.class, url);
    }

    /**
     * Creates a builder for a {@code INIConfiguration} and initializes it with
     * the file file identified by the given path.
     *
     * @param path the path to the file to be loaded
     * @return the newly created {@code FileBasedConfigurationBuilder}
     */
    public FileBasedConfigurationBuilder<INIConfiguration> iniBuilder(
            final String path)
    {
        return fileBasedBuilder(INIConfiguration.class, path);
    }

    /**
     * Creates a {@code INIConfiguration} instance from the content of the given
     * file. This is a convenience method which can be used if no builder is
     * needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param file the file to be loaded
     * @return a {@code INIConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public INIConfiguration ini(final File file) throws ConfigurationException
    {
        return iniBuilder(file).getConfiguration();
    }

    /**
     * Creates a {@code INIConfiguration} instance from the content of the given
     * URL. This is a convenience method which can be used if no builder is
     * needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param url the URL to be loaded
     * @return a {@code INIConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public INIConfiguration ini(final URL url) throws ConfigurationException
    {
        return iniBuilder(url).getConfiguration();
    }

    /**
     * Creates a {@code INIConfiguration} instance from the content of the file
     * identified by the given path. This is a convenience method which can be
     * used if no builder is needed for managing the configuration object.
     * (Although, behind the scenes a builder is created).
     *
     * @param path the path to the file to be loaded
     * @return a {@code INIConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public INIConfiguration ini(final String path) throws ConfigurationException
    {
        return iniBuilder(path).getConfiguration();
    }

    /**
     * Creates a builder for a {@code CombinedConfiguration} and initializes it
     * with the given file to be loaded.
     *
     * @param file the file to be loaded
     * @return the newly created {@code CombinedConfigurationBuilder}
     */
    public CombinedConfigurationBuilder combinedBuilder(final File file)
    {
        return new CombinedConfigurationBuilder().configure(fileParams(file));
    }

    /**
     * Creates a builder for a {@code CombinedConfiguration} and initializes it
     * with the given URL to be loaded.
     *
     * @param url the URL to be loaded
     * @return the newly created {@code CombinedConfigurationBuilder}
     */
    public CombinedConfigurationBuilder combinedBuilder(final URL url)
    {
        return new CombinedConfigurationBuilder().configure(fileParams(url));
    }

    /**
     * Creates a builder for a {@code CombinedConfiguration} and initializes it
     * with the given path to the file to be loaded.
     *
     * @param path the path to the file to be loaded
     * @return the newly created {@code CombinedConfigurationBuilder}
     */
    public CombinedConfigurationBuilder combinedBuilder(final String path)
    {
        return new CombinedConfigurationBuilder().configure(fileParams(path));
    }

    /**
     * Creates a {@code CombinedConfiguration} instance from the content of the
     * given file. This is a convenience method which can be used if no builder
     * is needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param file the file to be loaded
     * @return a {@code CombinedConfiguration} object initialized from this file
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public CombinedConfiguration combined(final File file)
            throws ConfigurationException
    {
        return combinedBuilder(file).getConfiguration();
    }

    /**
     * Creates a {@code CombinedConfiguration} instance from the content of the
     * given URL. This is a convenience method which can be used if no builder
     * is needed for managing the configuration object. (Although, behind the
     * scenes a builder is created).
     *
     * @param url the URL to be loaded
     * @return a {@code CombinedConfiguration} object initialized from this URL
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public CombinedConfiguration combined(final URL url)
            throws ConfigurationException
    {
        return combinedBuilder(url).getConfiguration();
    }

    /**
     * Creates a {@code CombinedConfiguration} instance from the content of the
     * file identified by the given path. This is a convenience method which can
     * be used if no builder is needed for managing the configuration object.
     * (Although, behind the scenes a builder is created).
     *
     * @param path the path to the file to be loaded
     * @return a {@code CombinedConfiguration} object initialized from this URL
     * @throws ConfigurationException if an error occurred when loading the
     *         configuration
     */
    public CombinedConfiguration combined(final String path)
            throws ConfigurationException
    {
        return combinedBuilder(path).getConfiguration();
    }

    /**
     * Creates a configured builder for a file-based configuration of the
     * specified type.
     *
     * @param configClass the configuration class
     * @param params the parameters object for configuring the builder
     * @param <T> the type of the configuration to be constructed
     * @return the newly created builder
     */
    private <T extends FileBasedConfiguration> FileBasedConfigurationBuilder<T> createFileBasedBuilder(
            final Class<T> configClass, final FileBasedBuilderParameters params)
    {
        return new FileBasedConfigurationBuilder<>(configClass)
                .configure(params);
    }

    /**
     * Convenience method for creating a parameters object for a file-based
     * configuration.
     *
     * @return the newly created parameters object
     */
    private FileBasedBuilderParameters fileParams()
    {
        return getParameters().fileBased();
    }

    /**
     * Convenience method for creating a file-based parameters object
     * initialized with the given file.
     *
     * @param file the file to be loaded
     * @return the initialized parameters object
     */
    private FileBasedBuilderParameters fileParams(final File file)
    {
        return fileParams().setFile(file);
    }

    /**
     * Convenience method for creating a file-based parameters object
     * initialized with the given file.
     *
     * @param url the URL to be loaded
     * @return the initialized parameters object
     */
    private FileBasedBuilderParameters fileParams(final URL url)
    {
        return fileParams().setURL(url);
    }

    /**
     * Convenience method for creating a file-based parameters object
     * initialized with the given file path.
     *
     * @param path the path to the file to be loaded
     * @return the initialized parameters object
     */
    private FileBasedBuilderParameters fileParams(final String path)
    {
        return fileParams().setFileName(path);
    }
}
