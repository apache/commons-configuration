/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanFactory;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.beanutils.DefaultBeanFactory;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

/**
 * <p>
 * A factory class that creates a composite configuration from an XML based
 * <em>configuration definition file</em>.
 * </p>
 * <p>
 * This class provides an easy and flexible means for loading multiple
 * configuration sources and combining the results into a single configuration
 * object. The sources to be loaded are defined in an XML document that can
 * contain certain tags representing the different supported configuration
 * classes. If such a tag is found, the corresponding <code>Configuration</code>
 * class is instantiated and initialized using the classes of the
 * <code>beanutils</code> package (namely
 * <code>{@link org.apache.commons.configuration.beanutils.XMLBeanDeclaration XMLBeanDeclaration}</code>
 * will be used to extract the configuration's initialization parameters, which
 * allows for complex initialization szenarios).
 * </p>
 * <p>
 * It is also possible to add custom tags to the configuration definition file.
 * For this purpose register your own <code>ConfigurationProvider</code>
 * implementation for your tag using the <code>addConfigurationProvider()</code>
 * method. This provider will then be called when the corresponding custom tag
 * is detected. For the default configuration classes providers are already
 * registered.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class DefaultConfigurationBuilder extends XMLConfiguration implements ConfigurationBuilder
{
    /** Constant for the expression engine used by this builder. */
    static final XPathExpressionEngine EXPRESSION_ENGINE = new XPathExpressionEngine();

    /** Constant for the name of the configuration bean factory. */
    static final String CONFIG_BEAN_FACTORY_NAME = DefaultConfigurationBuilder.class
            .getName()
            + ".CONFIG_BEAN_FACTORY_NAME";

    /** Constant for the reserved at attribute. */
    static final String ATTR_AT = "at";

    /** Constant for the reserved optional attribute. */
    static final String ATTR_OPTIONAL = "optional";

    /** Constant for the file name attribute. */
    static final String ATTR_FILENAME = "fileName";

    /** Constant for an expression that selects the union configurations. */
    static final String KEY_UNION = "/additional/*";

    /** Constant for an expression that selects override configurations. */
    static final String KEY_OVERRIDE1 = "/*[local-name() != 'additional' and local-name() != 'override']";

    /**
     * Constant for an expression that selects override configurations in the
     * override section.
     */
    static final String KEY_OVERRIDE2 = "/override/*";

    /** Constant for the XML file extension. */
    static final String EXT_XML = ".xml";

    /** Constant for the provider for properties files. */
    private static final ConfigurationProvider PROPERTIES_PROVIDER = new FileExtensionConfigurationProvider(
            XMLPropertiesConfiguration.class, PropertiesConfiguration.class,
            EXT_XML);

    /** Constant for the provider for XML files. */
    private static final ConfigurationProvider XML_PROVIDER = new FileConfigurationProvider(
            XMLConfiguration.class);

    /** Constant for the provider for JNDI sources. */
    private static final ConfigurationProvider JNDI_PROVIDER = new ConfigurationProvider(
            JNDIConfiguration.class);

    /** Constant for the provider for system properties. */
    private static final ConfigurationProvider SYSTEM_PROVIDER = new ConfigurationProvider(
            SystemConfiguration.class);

    /** Constant for the provider for plist files. */
    private static final ConfigurationProvider PLIST_PROVIDER = new FileExtensionConfigurationProvider(
            XMLPropertyListConfiguration.class,
            PropertyListConfiguration.class, EXT_XML);

    /** An array with the names of the default tags. */
    private static final String[] DEFAULT_TAGS =
    { "properties", "xml", "hierarchicalXml", "jndi", "system", "plist"};

    /** An array with the providers for the default tags. */
    private static final ConfigurationProvider[] DEFAULT_PROVIDERS =
    { PROPERTIES_PROVIDER, XML_PROVIDER, XML_PROVIDER, JNDI_PROVIDER,
            SYSTEM_PROVIDER, PLIST_PROVIDER};

    /** Stores a map with the registered configuration providers. */
    private Map providers;

    /** Stores the base path to the configuration sources to load. */
    private String configurationBasePath;

    /**
     * Creates a new instance of <code>DefaultConfigurationBuilder</code>. A
     * configuration definition file is not yet loaded. Use the diverse setter
     * methods provided by file based configurations to specify the
     * configuration definition file.
     */
    public DefaultConfigurationBuilder()
    {
        super();
        providers = new HashMap();
        setExpressionEngine(EXPRESSION_ENGINE);
        registerDefaultProviders();
    }

    /**
     * Creates a new instance of <code>DefaultConfigurationBuilder</code> and sets
     * the specified configuration definition file.
     *
     * @param file the configuration definition file
     */
    public DefaultConfigurationBuilder(File file)
    {
        this();
        setFile(file);
    }

    /**
     * Creates a new instance of <code>DefaultConfigurationBuilder</code> and sets
     * the specified configuration definition file.
     *
     * @param fileName the name of the configuration definition file
     * @throws ConfigurationException if an error occurs when the file is loaded
     */
    public DefaultConfigurationBuilder(String fileName)
            throws ConfigurationException
    {
        this();
        setFileName(fileName);
    }

    /**
     * Creates a new instance of <code>DefaultConfigurationBuilder</code> and sets
     * the specified configuration definition file.
     *
     * @param url the URL to the configuration definition file
     * @throws ConfigurationException if an error occurs when the file is loaded
     */
    public DefaultConfigurationBuilder(URL url) throws ConfigurationException
    {
        this();
        setURL(url);
    }

    /**
     * Returns the base path for the configuration sources to load. This path is
     * used to resolve relative paths in the configuration definition file.
     *
     * @return the base path for configuration sources
     */
    public String getConfigurationBasePath()
    {
        return (configurationBasePath != null) ? configurationBasePath
                : getBasePath();
    }

    /**
     * Sets the base path for the configuration sources to load. Normally a base
     * path need not to be set because it is determined by the location of the
     * configuration definition file to load. All relative pathes in this file
     * are resolved relative to this file. Setting a base path makes sense if
     * such relative pathes should be otherwise resolved, e.g. if the
     * configuration file is loaded from the class path and all sub
     * configurations it refers to are stored in a special config directory.
     *
     * @param configurationBasePath the new base path to set
     */
    public void setConfigurationBasePath(String configurationBasePath)
    {
        this.configurationBasePath = configurationBasePath;
    }

    /**
     * Adds a configuration provider for the specified tag. Whenever this tag is
     * encountered in the configuration definition file this provider will be
     * called to create the configuration object.
     *
     * @param tagName the name of the tag in the configuration definition file
     * @param provider the provider for this tag
     */
    public void addConfigurationProvider(String tagName,
            ConfigurationProvider provider)
    {
        if (tagName == null)
        {
            throw new IllegalArgumentException("Tag name must not be null!");
        }
        if (provider == null)
        {
            throw new IllegalArgumentException("Provider must not be null!");
        }

        providers.put(tagName, provider);
    }

    /**
     * Removes the configuration provider for the specified tag name.
     *
     * @param tagName the tag name
     * @return the removed configuration provider or <b>null</b> if none was
     * registered for that tag
     */
    public ConfigurationProvider removeConfigurationProvider(String tagName)
    {
        return (ConfigurationProvider) providers.remove(tagName);
    }

    /**
     * Returns the configuration provider for the given tag.
     *
     * @param tagName the name of the tag
     * @return the provider that was registered for this tag or <b>null</b> if
     * there is none
     */
    public ConfigurationProvider providerForTag(String tagName)
    {
        return (ConfigurationProvider) providers.get(tagName);
    }

    /**
     * Returns the configuration provided by this builder. Loads and parses the
     * configuration definition file and creates instances for the declared
     * configurations.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    public Configuration getConfiguration() throws ConfigurationException
    {
        return getConfiguration(true);
    }

    /**
     * Returns the configuration provided by this builder. If the boolean
     * parameter is <b>true</b>, the configuration definition file will be
     * loaded. It will then be parsed, and instances for the declared
     * configurations will be created.
     *
     * @param load a flag whether the configuration definition file should be
     * loaded; a value of <b>false</b> would make sense if the file has already
     * been created or its content was manipulated using some of the property
     * accessor methods
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    public Configuration getConfiguration(boolean load)
            throws ConfigurationException
    {
        if (load)
        {
            load();
        }

        List overrides = configurationsAt(KEY_OVERRIDE1);
        overrides.addAll(configurationsAt(KEY_OVERRIDE2));
        CompositeConfiguration result = createOverrideConfiguration(overrides);
        List additionals = configurationsAt(KEY_UNION);
        if (!additionals.isEmpty())
        {
            result.addConfiguration(createUnionConfiguration(additionals));
        }

        return result;
    }

    /**
     * Creates a composite configuration for the passed in configuration
     * declarations.
     *
     * @param subs a list with sub configurations that contain configuration
     * declarations for override configurations
     * @return the composite configuration
     * @throws ConfigurationException if an error occurs
     */
    protected CompositeConfiguration createOverrideConfiguration(List subs)
            throws ConfigurationException
    {
        CompositeConfiguration cc = new CompositeConfiguration();

        for (Iterator it = subs.iterator(); it.hasNext();)
        {
            cc
                    .addConfiguration(createConfigurationAt((HierarchicalConfiguration) it
                            .next()));
        }

        return cc;
    }

    /**
     * Creates a union configuration for the passed in configuration
     * declarations. This method will create configuration objects for the
     * passed in descriptions and combine them into a single union
     * configuration.
     *
     * @param subs a list with sub configurations that contain configuration
     * declarations
     * @return the union configuration
     * @throws ConfigurationException if an error occurs
     */
    protected HierarchicalConfiguration createUnionConfiguration(List subs)
            throws ConfigurationException
    {
        HierarchicalConfiguration union = new HierarchicalConfiguration();

        for (Iterator it = subs.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration conf = (HierarchicalConfiguration) it
                    .next();
            ConfigurationDeclaration decl = new ConfigurationDeclaration(this,
                    conf);
            union.addNodes(decl.getAt(), convertToHierarchical(
                    createConfigurationAt(decl)).getRoot().getChildren());
        }

        return union;
    }

    /**
     * Converts the passed in configuration to a hierarchical one. If the
     * configuration is already hierarchical, it is directly returned. Otherwise
     * all properties are copied into a new hierarchical configuration.
     *
     * @param conf the configuration to convert
     * @return the new hierarchical configuration
     */
    protected HierarchicalConfiguration convertToHierarchical(Configuration conf)
    {
        if (conf instanceof HierarchicalConfiguration)
        {
            return (HierarchicalConfiguration) conf;
        }
        else
        {
            HierarchicalConfiguration hc = new HierarchicalConfiguration();
            ConfigurationUtils.copy(conf, hc);
            return hc;
        }
    }

    /**
     * Registers the default configuration providers supported by this class.
     * This method will be called during initialization. It registers
     * configuration providers for the tags that are supported by default.
     */
    protected void registerDefaultProviders()
    {
        for (int i = 0; i < DEFAULT_TAGS.length; i++)
        {
            addConfigurationProvider(DEFAULT_TAGS[i], DEFAULT_PROVIDERS[i]);
        }
    }

    /**
     * Creates a configuration object from the specified configuration
     * declaration.
     *
     * @param decl the configuration declaration
     * @return the new configuration object
     * @throws ConfigurationException if an error occurs
     */
    private Configuration createConfigurationAt(ConfigurationDeclaration decl)
            throws ConfigurationException
    {
        try
        {
            return (Configuration) BeanHelper.createBean(decl);
        }
        catch (Exception ex)
        {
            // redirect to configuration exceptions
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Creates a configuration object from the specified sub configuration.
     *
     * @param sub the sub configuration
     * @return the new configuration object
     * @throws ConfigurationException if an error occurs
     */
    private Configuration createConfigurationAt(HierarchicalConfiguration sub)
            throws ConfigurationException
    {
        return createConfigurationAt(new ConfigurationDeclaration(this, sub));
    }

    /**
     * <p>
     * A base class for creating and initializing configuration sources.
     * </p>
     * <p>
     * Concrete sub classes of this base class are responsible for creating
     * specific <code>Configuration</code> objects for the tags in the
     * configuration definition file. The configuration factory will parse the
     * definition file and try to find a matching
     * <code>ConfigurationProvider</code> for each encountered tag. This
     * provider is then asked to create a corresponding
     * <code>Configuration</code> object. It is up to a concrete
     * implementation how this object is created and initialized.
     * </p>
     */
    public static class ConfigurationProvider extends DefaultBeanFactory
    {
        /** Stores the class of the configuration to be created. */
        private Class configurationClass;

        /**
         * Creates a new uninitialized instance of
         * <code>ConfigurationProvider</code>.
         */
        public ConfigurationProvider()
        {
            this(null);
        }

        /**
         * Creates a new instance of <code>ConfigurationProvider</code> and
         * sets the class of the configuration created by this provider.
         *
         * @param configClass the configuration class
         */
        public ConfigurationProvider(Class configClass)
        {
            setConfigurationClass(configClass);
        }

        /**
         * Returns the class of the configuration returned by this provider.
         *
         * @return the class of the provided configuration
         */
        public Class getConfigurationClass()
        {
            return configurationClass;
        }

        /**
         * Sets the class of the configuration returned by this provider.
         *
         * @param configurationClass the configuration class
         */
        public void setConfigurationClass(Class configurationClass)
        {
            this.configurationClass = configurationClass;
        }

        /**
         * Returns the configuration. This method is called to fetch the
         * configuration from the provider. This implementation will call the
         * inherited
         * <code>{@link org.apache.commons.configuration.beanutils.DefaultBeanFactory#createBean(Class, BeanDeclaration, Object) createBean()}</code>
         * method to create a new instance of the configuration class.
         *
         * @param decl the bean declaration with initialization parameters for
         * the configuration
         * @return the new configuration object
         * @throws Exception if an error occurs
         */
        public Configuration getConfiguration(ConfigurationDeclaration decl)
                throws Exception
        {
            return (Configuration) createBean(getConfigurationClass(), decl,
                    null);
        }
    }

    /**
     * <p>
     * A specialized <code>BeanDeclaration</code> implementation that
     * represents the declaration of a configuration source.
     * </p>
     * <p>
     * Instances of this class are able to extract all information about a
     * configuration source from the configuration definition file. The
     * declaration of a configuration source is very similar to a bean
     * declaration processed by <code>XMLBeanDeclaration</code>. There are
     * very few differences, e.g. the two reserved attributes
     * <code>optional</code> and <code>at</code> and the fact that a bean
     * factory is never needed.
     * </p>
     */
    protected static class ConfigurationDeclaration extends XMLBeanDeclaration
    {
        /** Stores a reference to the associated configuration factory. */
        private DefaultConfigurationBuilder configurationBuilder;

        /**
         * Creates a new instance of <code>ConfigurationDeclaration</code> and
         * initializes it.
         *
         * @param buikder the associated configuration builder
         * @param config the configuration this declaration is based onto
         */
        public ConfigurationDeclaration(DefaultConfigurationBuilder builder,
                HierarchicalConfiguration config)
        {
            super(config);
            configurationBuilder = builder;
        }

        /**
         * Returns the associated configuration builder.
         *
         * @return the configuration builder
         */
        public DefaultConfigurationBuilder getConfigurationBuilder()
        {
            return configurationBuilder;
        }

        /**
         * Returns the value of the <code>at</code> attribute.
         *
         * @return the value of the <code>at</code> attribute (can be <b>null</b>)
         */
        public String getAt()
        {
            return attributeValueStr(ATTR_AT);
        }

        /**
         * Returns a flag whether this is an optional configuration.
         *
         * @return a flag if this declaration points to an optional
         * configuration
         */
        public boolean isOptional()
        {
            Object value = attributeValue(ATTR_OPTIONAL);
            try
            {
                return (value != null) ? PropertyConverter.toBoolean(value)
                        .booleanValue() : false;
            }
            catch (ConversionException cex)
            {
                throw new ConfigurationRuntimeException(
                        "optional attribute does not have a valid boolean value",
                        cex);
            }
        }

        /**
         * Returns the name of the bean factory. For configuration source
         * declarations always a reserved factory is used. This factory's name
         * is returned by this implementation.
         *
         * @return the name of the bean factory
         */
        public String getBeanFactoryName()
        {
            return CONFIG_BEAN_FACTORY_NAME;
        }

        /**
         * Returns the bean's class name. This implementation will always return
         * <b>null</b>.
         *
         * @return the name of the bean's class
         */
        public String getBeanClassName()
        {
            return null;
        }

        /**
         * Returns the value of the specified attribute. This can be useful for
         * certain <code>ConfigurationProvider</code> implementations.
         *
         * @param attrName the attribute's name
         * @return the attribute's value (or <b>null</b> if it does not exist)
         */
        public Object attributeValue(String attrName)
        {
            return super.attributeValue(attrName);
        }

        /**
         * Returns the string value of the specified attribute.
         *
         * @param attrName the attribute's name
         * @return the attribute's value (or <b>null</b> if it does not exist)
         */
        public String attributeValueStr(String attrName)
        {
            return super.attributeValueStr(attrName);
        }

        /**
         * Checks whether the given node is reserved. This method will take
         * further reserved attributes into account
         *
         * @param nd the node
         * @return a flag whether this node is reserved
         */
        protected boolean isReservedNode(ConfigurationNode nd)
        {
            if (super.isReservedNode(nd))
            {
                return true;
            }

            return nd.isAttribute()
                    && (ATTR_AT.equals(nd.getName()) || ATTR_OPTIONAL.equals(nd
                            .getName()));
        }
    }

    /**
     * A specialized <code>BeanFactory</code> implementation that handles
     * configuration declarations. This class will retrieve the correct
     * configuration provider and delegate the task of creating the
     * configuration to this object.
     */
    static class ConfigurationBeanFactory implements BeanFactory
    {
        /**
         * Creates an instance of a bean class. This implementation expects that
         * the passed in bean declaration is a declaration for a configuration.
         * It will determine the responsible configuration provider and delegate
         * the call to this instance.
         *
         * @param beanClass the bean class (will be ignored)
         * @param data the declaration
         * @param param an additional parameter (will be ignored)
         * @return the newly created configuration
         * @throws Exception if an error occurs
         */
        public Object createBean(Class beanClass, BeanDeclaration data,
                Object param) throws Exception
        {
            ConfigurationDeclaration decl = (ConfigurationDeclaration) data;
            String tagName = decl.getNode().getName();
            ConfigurationProvider provider = decl.getConfigurationBuilder()
                    .providerForTag(tagName);
            if (provider == null)
            {
                throw new ConfigurationRuntimeException(
                        "No ConfigurationProvider registered for tag "
                                + tagName);
            }

            return provider.getConfiguration(decl);
        }

        /**
         * Returns the default class for this bean factory.
         *
         * @return the default class
         */
        public Class getDefaultBeanClass()
        {
            // Here some valid class must be returned, otherwise BeanHelper
            // will complain that the bean's class cannot be determined
            return Configuration.class;
        }
    }

    /**
     * A specialized provider implementation that deals with file based
     * configurations. Ensures that the base path is correctly set and that the
     * load() method gets called.
     */
    static class FileConfigurationProvider extends ConfigurationProvider
    {
        /**
         * Creates a new instance of <code>FileConfigurationProvider</code>.
         */
        public FileConfigurationProvider()
        {
            super();
        }

        /**
         * Creates a new instance of <code>FileConfigurationProvider</code>
         * and sets the configuration class.
         *
         * @param configClass the class for the configurations to be created
         */
        public FileConfigurationProvider(Class configClass)
        {
            super(configClass);
        }

        /**
         * Creates the configuration. After that <code>load()</code> will be
         * called. If this configuration is marked as optional, exceptions will
         * be ignored.
         *
         * @param decl the declaration
         * @return the new configuration
         * @throws Exception if an error occurs
         */
        public Configuration getConfiguration(ConfigurationDeclaration decl)
                throws Exception
        {
            FileConfiguration config = (FileConfiguration) super
                    .getConfiguration(decl);
            try
            {
                config.load();
            }
            catch (ConfigurationException cex)
            {
                if (!decl.isOptional())
                {
                    throw cex;
                }
            }
            return config;
        }

        /**
         * Initializes the bean instance. Ensures that the file configuration's
         * base path will be initialized with the base path of the factory so
         * that relative path names can be correctly resolved.
         *
         * @param bean the bean to be initialized
         * @param data the declaration
         * @throws Exception if an error occurs
         */
        protected void initBeanInstance(Object bean, BeanDeclaration data)
                throws Exception
        {
            FileConfiguration config = (FileConfiguration) bean;
            config.setBasePath(((ConfigurationDeclaration) data)
                    .getConfigurationBuilder().getConfigurationBasePath());
            super.initBeanInstance(bean, data);
        }
    }

    /**
     * A specialized configuration provider for file based configurations that
     * can handle configuration sources whose concrete type depends on the
     * extension of the file to be loaded. One example is the
     * <code>properties</code> tag: if the file ends with ".xml" a
     * XMLPropertiesConfiguration object must be created, otherwise a
     * PropertiesConfiguration object.
     */
    static class FileExtensionConfigurationProvider extends
            FileConfigurationProvider
    {
        /** Stores the class to be created when the file extension matches. */
        private Class matchingClass;

        /**
         * Stores the class to be created when the file extension does not
         * match.
         */
        private Class defaultClass;

        /** Stores the file extension to be checked against. */
        private String fileExtension;

        /**
         * Creates a new instance of
         * <code>FileExtensionConfigurationProvider</code> and initializes it.
         *
         * @param matchingClass the class to be created when the file extension
         * matches
         * @param defaultClass the class to be created when the file extension
         * does not match
         * @param extension the file extension to be checked agains
         */
        public FileExtensionConfigurationProvider(Class matchingClass,
                Class defaultClass, String extension)
        {
            this.matchingClass = matchingClass;
            this.defaultClass = defaultClass;
            fileExtension = extension;
        }

        /**
         * Creates the configuration object. The class is determined by the file
         * name's extension.
         *
         * @param beanClass the class
         * @param data the bean declaration
         * @return the new bean
         * @throws Exception if an error occurs
         */
        protected Object createBeanInstance(Class beanClass,
                BeanDeclaration data) throws Exception
        {
            String fileName = ((ConfigurationDeclaration) data)
                    .attributeValueStr(ATTR_FILENAME);
            if (fileName != null
                    && fileName.toLowerCase().trim().endsWith(fileExtension))
            {
                return super.createBeanInstance(matchingClass, data);
            }
            else
            {
                return super.createBeanInstance(defaultClass, data);
            }
        }
    }

    static
    {
        // register the configuration bean factory
        BeanHelper.registerBeanFactory(CONFIG_BEAN_FACTORY_NAME,
                new ConfigurationBeanFactory());
    }
}
