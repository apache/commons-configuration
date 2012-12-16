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
package org.apache.commons.configuration.builder.combined;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.beanutils.CombinedBeanDeclaration;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration.builder.BuilderListener;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.ConfigurationBuilder;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration.builder.XMLBuilderProperties;
import org.apache.commons.configuration.resolver.CatalogResolver;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.xml.sax.EntityResolver;

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
 * classes. If such a tag is found, the corresponding {@code Configuration}
 * class is instantiated and initialized using the classes of the
 * {@code beanutils} package (namely
 * {@link org.apache.commons.configuration.beanutils.XMLBeanDeclaration XMLBeanDeclaration}
 * will be used to extract the configuration's initialization parameters, which
 * allows for complex initialization scenarios).
 * </p>
 * <p>
 * It is also possible to add custom tags to the configuration definition file.
 * For this purpose register your own {@code BaseConfigurationBuilderProvider}
 * implementation for your tag using the {@code addConfigurationProvider()}
 * method. This provider will then be called when the corresponding custom tag
 * is detected. For the default configuration classes providers are already
 * registered.
 * </p>
 * <p>
 * The configuration definition file has the following basic structure:
 * </p>
 * <p>
 *
 * <pre>
 * &lt;configuration systemProperties="properties file name"&gt;
 *   &lt;header&gt;
 *     &lt;!-- Optional meta information about the composite configuration --&gt;
 *   &lt;/header&gt;
 *   &lt;override&gt;
 *     &lt;!-- Declarations for override configurations --&gt;
 *   &lt;/override&gt;
 *   &lt;additional&gt;
 *     &lt;!-- Declarations for union configurations --&gt;
 *   &lt;/additional&gt;
 * &lt;/configuration&gt;
 * </pre>
 *
 * </p>
 * <p>
 * The name of the root element (here {@code configuration}) is
 * arbitrary. The optional systemProperties attribute identifies the path to
 * a property file containing properties that should be added to the system
 * properties. If specified on the root element, the system properties are
 * set before the rest of the configuration is processed.
 * </p>
 * <p>
 * There are two sections (both of them are optional) for declaring
 * <em>override</em> and <em>additional</em> configurations. Configurations
 * in the former section are evaluated in the order of their declaration, and
 * properties of configurations declared earlier hide those of configurations
 * declared later. Configurations in the latter section are combined to a union
 * configuration, i.e. all of their properties are added to a large hierarchical
 * configuration. Configuration declarations that occur as direct children of
 * the root element are treated as override declarations.
 * </p>
 * <p>
 * Each configuration declaration consists of a tag whose name is associated
 * with a {@code BaseConfigurationBuilderProvider}. This can be one of the
 * predefined tags like {@code properties}, or {@code xml}, or
 * a custom tag, for which a configuration provider was registered. Attributes
 * and sub elements with specific initialization parameters can be added. There
 * are some reserved attributes with a special meaning that can be used in every
 * configuration declaration:
 * </p>
 * <p>
 * <table border="1">
 * <tr>
 * <th>Attribute</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-name}</td>
 * <td>Allows to specify a name for this configuration. This name can be used
 * to obtain a reference to the configuration from the resulting combined
 * configuration (see below).</td>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-at}</td>
 * <td>With this attribute an optional prefix can be specified for the
 * properties of the corresponding configuration.</td>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-optional}</td>
 * <td>Declares a configuration as optional. This means that errors that occur
 * when creating the configuration are ignored. (However
 * {@link org.apache.commons.configuration.event.ConfigurationErrorListener}s
 * registered at the builder instance will get notified about this error: they
 * receive an event of type {@code EVENT_ERR_LOAD_OPTIONAL}. The key
 * property of this event contains the name of the optional configuration source
 * that caused this problem.)</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The optional <em>header</em> section can contain some meta data about the
 * created configuration itself. For instance, it is possible to set further
 * properties of the {@code NodeCombiner} objects used for constructing
 * the resulting configuration.
 * </p>
 * <p>
 * The default configuration object returned by this builder is an instance of the
 * {@link CombinedConfiguration} class. The return value of the
 * {@code getConfiguration()} method can be casted to this type, and the
 * {@code getConfiguration(boolean)} method directly declares
 * {@code CombinedConfiguration} as return type. This allows for
 * convenient access to the configuration objects maintained by the combined
 * configuration (e.g. for updates of single configuration objects). It has also
 * the advantage that the properties stored in all declared configuration
 * objects are collected and transformed into a single hierarchical structure,
 * which can be accessed using different expression engines. The actual CombinedConfiguration
 * implementation can be overridden by specifying the class in the <em>config-class</em>
 * attribute of the result element.
 * </p>
 * <p>
 * A custom EntityResolver can be used for all XMLConfigurations by adding
 * <pre>
 * &lt;entity-resolver config-class="EntityResolver fully qualified class name"&gt;
 * </pre>
 * The CatalogResolver can be used for all XMLConfiguration by adding
 * <pre>
 * &lt;entity-resolver catalogFiles="comma separated list of catalog files"&gt;
 * </pre>
 * </p>
 * <p>
 * Additional ConfigurationProviders can be added by configuring them in the <em>header</em>
 * section.
 * <pre>
 * &lt;providers&gt;
 *   &lt;provider config-tag="tag name" config-class="provider fully qualified class name"/&gt;
 * &lt;/providers&gt;
 * </pre>
 * </p>
 * <p>
 * Additional variable resolvers can be added by configuring them in the <em>header</em>
 * section.
 * <pre>
 * &lt;lookups&gt;
 *   &lt;lookup config-prefix="prefix" config-class="StrLookup fully qualified class name"/&gt;
 * &lt;/lookups&gt;
 * </pre>
 * </p>
 * <p>
 * All declared override configurations are directly added to the resulting
 * combined configuration. If they are given names (using the
 * {@code config-name} attribute), they can directly be accessed using
 * the {@code getConfiguration(String)} method of
 * {@code CombinedConfiguration}. The additional configurations are
 * altogether added to another combined configuration, which uses a union
 * combiner. Then this union configuration is added to the resulting combined
 * configuration under the name defined by the {@code ADDITIONAL_NAME}
 * constant.
 * </p>
 * <p>
 * Implementation note: This class is not thread-safe. Especially the
 * {@code getConfiguration()} methods should be called by a single thread
 * only.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class CombinedConfigurationBuilder extends BasicConfigurationBuilder<CombinedConfiguration>
{
    /**
     * Constant for the name of the additional configuration. If the
     * configuration definition file contains an {@code additional}
     * section, a special union configuration is created and added under this
     * name to the resulting combined configuration.
     */
    public static final String ADDITIONAL_NAME = CombinedConfigurationBuilder.class
            .getName()
            + "/ADDITIONAL_CONFIG";

    /**
     * Constant for the type of error events caused by optional configurations
     * that cannot be loaded.
     */
    public static final int EVENT_ERR_LOAD_OPTIONAL = 51;

    /** Constant for the name of the configuration bean factory. */
    static final String CONFIG_BEAN_FACTORY_NAME = CombinedConfigurationBuilder.class
            .getName()
            + ".CONFIG_BEAN_FACTORY_NAME";

    /** Constant for the reserved name attribute. */
    static final String ATTR_NAME = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "name"
            + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the name of the at attribute. */
    static final String ATTR_ATNAME = "at";

    /** Constant for the reserved at attribute. */
    static final String ATTR_AT_RES = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + ATTR_ATNAME
            + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the at attribute without the reserved prefix. */
    static final String ATTR_AT = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + ATTR_ATNAME + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the name of the optional attribute. */
    static final String ATTR_OPTIONALNAME = "optional";

    /** Constant for the reserved optional attribute. */
    static final String ATTR_OPTIONAL_RES = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + ATTR_OPTIONALNAME
            + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the optional attribute without the reserved prefix. */
    static final String ATTR_OPTIONAL = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + ATTR_OPTIONALNAME + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the file name attribute. */
    static final String ATTR_FILENAME = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + "fileName" + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the forceCreate attribute. */
    static final String ATTR_FORCECREATE = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "forceCreate"
            + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /** Constant for the reload attribute. */
    static final String ATTR_RELOAD = DefaultExpressionEngine.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "reload"
            + DefaultExpressionEngine.DEFAULT_ATTRIBUTE_END;

    /**
     * Constant for the tag attribute for providers.
     */
    static final String KEY_SYSTEM_PROPS = "[@systemProperties]";

    /** Constant for the name of the header section. */
    static final String SEC_HEADER = "header";

    /** Constant for an expression that selects the union configurations. */
    static final String KEY_UNION = "additional";

    /** An array with the names of top level configuration sections.*/
    static final String[] CONFIG_SECTIONS = {
        "additional", "override", SEC_HEADER
    };

    /**
     * Constant for an expression that selects override configurations in the
     * override section.
     */
    static final String KEY_OVERRIDE = "override";

    /**
     * Constant for the key that points to the list nodes definition of the
     * override combiner.
     */
    static final String KEY_OVERRIDE_LIST = SEC_HEADER
            + ".combiner.override.list-nodes.node";

    /**
     * Constant for the key that points to the list nodes definition of the
     * additional combiner.
     */
    static final String KEY_ADDITIONAL_LIST = SEC_HEADER
            + ".combiner.additional.list-nodes.node";

    /**
     * Constant for the key for defining providers in the configuration file.
     */
    static final String KEY_CONFIGURATION_PROVIDERS = SEC_HEADER
            + ".providers.provider";

    /**
     * Constant for the tag attribute for providers.
     */
    static final String KEY_PROVIDER_KEY = XMLBeanDeclaration.ATTR_PREFIX + "tag]";

    /**
     * Constant for the key for defining variable resolvers
     */
    static final String KEY_CONFIGURATION_LOOKUPS = SEC_HEADER
            + ".lookups.lookup";

    /**
     * Constant for the key for defining entity resolvers
     */
    static final String KEY_ENTITY_RESOLVER = SEC_HEADER + ".entity-resolver";

    /**
     * Constant for the prefix attribute for lookups.
     */
    static final String KEY_LOOKUP_KEY = XMLBeanDeclaration.ATTR_PREFIX + "prefix]";

    /**
     * Constant for the FileSystem.
     */
    static final String FILE_SYSTEM = SEC_HEADER + ".fileSystem";

    /**
     * Constant for the key of the result declaration. This key can point to a
     * bean declaration, which defines properties of the resulting combined
     * configuration.
     */
    static final String KEY_RESULT = SEC_HEADER + ".result";

    /** Constant for the key of the combiner in the result declaration.*/
    static final String KEY_COMBINER = KEY_RESULT + ".nodeCombiner";

    /** Constant for the XML file extension. */
    static final String EXT_XML = "xml";

    /** Constant for the basic configuration builder class. */
    private static final String BASIC_BUILDER =
            "org.apache.commons.configuration.builder.BasicConfigurationBuilder";

    /** Constant for the file-based configuration builder class. */
    private static final String FILE_BUILDER =
            "org.apache.commons.configuration.builder.FileBasedConfigurationBuilder";

    /** Constant for the reloading file-based configuration builder class. */
    private static final String RELOADING_BUILDER =
            "org.apache.commons.configuration.builder.ReloadingFileBasedConfigurationBuilder";

    /** Constant for the name of the file-based builder parameters class. */
    private static final String FILE_PARAMS =
            "org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl";

    /** Constant for the provider for properties files. */
    private static final ConfigurationBuilderProvider PROPERTIES_PROVIDER =
            new FileExtensionConfigurationBuilderProvider(
                    FILE_BUILDER,
                    RELOADING_BUILDER,
                    "org.apache.commons.configuration.XMLPropertiesConfiguration",
                    "org.apache.commons.configuration.PropertiesConfiguration",
                    EXT_XML, Arrays.asList(FILE_PARAMS));

    /** Constant for the provider for XML files. */
    private static final ConfigurationBuilderProvider XML_PROVIDER =
            new BaseConfigurationBuilderProvider(FILE_BUILDER, RELOADING_BUILDER,
                    "org.apache.commons.configuration.XMLConfiguration",
                    Arrays.asList("org.apache.commons.configuration.builder.XMLBuilderParametersImpl"));

    /** Constant for the provider for JNDI sources. */
    private static final BaseConfigurationBuilderProvider JNDI_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration.JNDIConfiguration",
                    Arrays.asList("org.apache.commons.configuration.builder.JndiBuilderParametersImpl"));

    /** Constant for the provider for system properties. */
    private static final BaseConfigurationBuilderProvider SYSTEM_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration.SystemConfiguration",
                    Arrays.asList("org.apache.commons.configuration.builder.BasicBuilderParameters"));

    /** Constant for the provider for ini files. */
    private static final BaseConfigurationBuilderProvider INI_PROVIDER =
            new BaseConfigurationBuilderProvider(FILE_BUILDER, RELOADING_BUILDER,
                    "org.apache.commons.configuration.HierarchicalINIConfiguration",
                    Arrays.asList(FILE_PARAMS));

    /** Constant for the provider for environment properties. */
    private static final BaseConfigurationBuilderProvider ENV_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration.EnvironmentConfiguration",
                    Arrays.asList("org.apache.commons.configuration.builder.BasicBuilderParameters"));

    /** Constant for the provider for plist files. */
    private static final BaseConfigurationBuilderProvider PLIST_PROVIDER =
            new FileExtensionConfigurationBuilderProvider(
                    FILE_BUILDER,
                    RELOADING_BUILDER,
                    "org.apache.commons.configuration.plist.XMLPropertyListConfiguration",
                    "org.apache.commons.configuration.plist.PropertyListConfiguration",
                    EXT_XML, Arrays.asList(FILE_PARAMS));

    /** Constant for the provider for configuration definition files.*/
    private static final BaseConfigurationBuilderProvider BUILDER_PROVIDER = null;

    /** An array with the names of the default tags. */
    private static final String[] DEFAULT_TAGS = {
            "properties", "xml", "hierarchicalXml", "plist",
            "ini", "system", "env", "jndi"/*, "configuration"*/
    };

    /** An array with the providers for the default tags. */
    private static final ConfigurationBuilderProvider[] DEFAULT_PROVIDERS = {
            PROPERTIES_PROVIDER, XML_PROVIDER, XML_PROVIDER, PLIST_PROVIDER, INI_PROVIDER,
            SYSTEM_PROVIDER, ENV_PROVIDER, JNDI_PROVIDER/*, BUILDER_PROVIDER */
    };

    /** A map with the default configuration builder providers. */
    private static final Map<String, ConfigurationBuilderProvider> DEFAULT_PROVIDERS_MAP;

    /**
     * A specialized {@code StrLookup} object which operates on the combined
     * configuration constructed by this builder. This object is used as
     * default lookup for {@code ConfigurationInterpolator} objects assigned to
     * newly created configuration objects.
     */
//    private final StrLookup combinedConfigLookup = new StrLookup()
//    {
//        @Override
//        public String lookup(String key)
//        {
//            if (constructedConfiguration != null)
//            {
//                Object value =
//                        constructedConfiguration.resolveContainerStore(key);
//                return (value != null) ? value.toString() : null;
//            }
//            return null;
//        }
//    };
//

    /** The builder for the definition configuration. */
    private ConfigurationBuilder<? extends HierarchicalConfiguration> definitionBuilder;

    /** Stores temporarily the configuration with the builder definitions. */
    private HierarchicalConfiguration definitionConfiguration;

    /** The object with data about configuration sources. */
    private ConfigurationSourceData sourceData;

    /** Stores the current parameters object. */
    private CombinedBuilderParametersImpl currentParameters;

    /** The current XML parameters object. */
    private XMLBuilderParametersImpl currentXMLParameters;

    /**
     * Creates a new instance of {@code CombinedConfigurationBuilder}. No parameters
     * are set.
     */
    public CombinedConfigurationBuilder()
    {
        super(CombinedConfiguration.class);
    }

    /**
     *
     * Creates a new instance of {@code CombinedConfigurationBuilder} and sets
     * the specified initialization parameters.
     * @param params a map with initialization parameters
     */
    public CombinedConfigurationBuilder(Map<String, Object> params)
    {
        super(CombinedConfiguration.class, params);
    }

    /**
     *
     * Creates a new instance of {@code CombinedConfigurationBuilder} and sets
     * the specified initialization parameters and the <em>allowFailOnInit</em> flag.
     * @param params a map with initialization parameters
     * @param allowFailOnInit the <em>allowFailOnInit</em> flag
     */
    public CombinedConfigurationBuilder(Map<String, Object> params, boolean allowFailOnInit)
    {
        super(CombinedConfiguration.class, params, allowFailOnInit);
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
    public CombinedConfiguration getConfiguration(boolean load)
            throws ConfigurationException
    {
        registerConfiguredLookups();

//        CombinedConfiguration result = createResultConfiguration();
//        constructedConfiguration = result;

        return null;
    }

    /**
     * Returns the {@code ConfigurationBuilder} which creates the definition
     * configuration.
     *
     * @return the builder for the definition configuration
     * @throws ConfigurationException if an error occurs
     */
    public synchronized ConfigurationBuilder<? extends HierarchicalConfiguration> getDefinitionBuilder()
            throws ConfigurationException
    {
        if (definitionBuilder == null)
        {
            definitionBuilder = setupDefinitionBuilder(getParameters());
            addDefinitionBuilderChangeListener(definitionBuilder);
        }
        return definitionBuilder;
    }

    /**
     * Returns the configuration builder with the given name. With this method a
     * builder of a child configuration which was given a name in the
     * configuration definition file can be accessed directly.
     *
     * @param name the name of the builder in question
     * @return the child configuration builder with this name
     * @throws ConfigurationException if an error occurs setting up the
     *         definition configuration or no builder with this name exists
     */
    public synchronized ConfigurationBuilder<? extends Configuration> getNamedBuilder(
            String name) throws ConfigurationException
    {
        ConfigurationBuilder<? extends Configuration> builder =
                getSourceData().getNamedBuilder(name);
        if (builder == null)
        {
            throw new ConfigurationException("Builder cannot be resolved: "
                    + name);
        }
        return builder;
    }

    /**
     * Returns a set with the names of all child configuration builders. A tag
     * defining a configuration source in the configuration definition file can
     * have the {@code config-name} attribute. If this attribute is present, the
     * corresponding builder is assigned this name and can be directly accessed
     * through the {@link #getNamedBuilder(String)} method. This method returns
     * a collection with all available builder names.
     *
     * @return a collection with the names of all builders
     * @throws ConfigurationException if an error occurs setting up the
     *         definition configuration
     */
    public synchronized Set<String> builderNames()
            throws ConfigurationException
    {
        return Collections.unmodifiableSet(getSourceData().builderNames());
    }

    /**
     * {@inheritDoc} This implementation resets some specific internal state of
     * this builder.
     */
    @Override
    public synchronized void resetParameters()
    {
        super.resetParameters();
        definitionBuilder = null;
        definitionConfiguration = null;
        currentParameters = null;
        currentXMLParameters = null;

        if (sourceData != null)
        {
            sourceData.cleanUp();
            sourceData = null;
        }
    }

    /**
     * Obtains the {@code ConfigurationBuilder} object which provides access to
     * the configuration containing the definition of the combined configuration
     * to create. If a definition builder is defined in the parameters, it is
     * used. Otherwise, we check whether the combined builder parameters object
     * contains a parameters object for the definition builder. If this is the
     * case, a builder for an {@code XMLConfiguration} is created and configured
     * with this object. As a last resort, it is looked for a
     * {@link FileBasedBuilderParametersImpl} object in the properties. If
     * found, also a XML configuration builder is created which loads this file.
     * Note: This method is called from a synchronized block.
     *
     * @param params the current parameters for this builder
     * @return the builder for the definition configuration
     * @throws ConfigurationException if an error occurs
     */
    protected ConfigurationBuilder<? extends HierarchicalConfiguration> setupDefinitionBuilder(
            Map<String, Object> params) throws ConfigurationException
    {
        CombinedBuilderParametersImpl cbParams =
                CombinedBuilderParametersImpl.fromParameters(params);
        if (cbParams != null)
        {
            ConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                    cbParams.getDefinitionBuilder();
            if (defBuilder != null)
            {
                return defBuilder;
            }

            if (cbParams.getDefinitionBuilderParameters() != null)
            {
                return createXMLDefinitionBuilder(cbParams
                        .getDefinitionBuilderParameters());
            }
        }

        BuilderParameters fileParams =
                FileBasedBuilderParametersImpl.fromParameters(params);
        if (fileParams != null)
        {
            return createXMLDefinitionBuilder(fileParams);
        }

        throw new ConfigurationException(
                "No builder for configuration definition specified!");
    }

    /**
     * Returns the configuration containing the definition of the combined
     * configuration to be created. This method only returns a defined result
     * during construction of the result configuration. The definition
     * configuration is obtained from the definition builder at first access and
     * then stored temporarily to ensure that during result construction always
     * the same configuration instance is used. (Otherwise, it would be possible
     * that the definition builder returns a different instance when queried
     * multiple times.)
     *
     * @return the definition configuration
     * @throws ConfigurationException if an error occurs
     */
    protected HierarchicalConfiguration getDefinitionConfiguration()
            throws ConfigurationException
    {
        if (definitionConfiguration == null)
        {
            definitionConfiguration = getDefinitionBuilder().getConfiguration();
        }
        return definitionConfiguration;
    }

    /**
     * {@inheritDoc} This implementation evaluates the {@code result} property
     * of the definition configuration. It creates a combined bean declaration
     * with both the properties specified in the definition file and the
     * properties defined as initialization parameters.
     */
    @Override
    protected BeanDeclaration createResultDeclaration(Map<String, Object> params)
            throws ConfigurationException
    {
        BeanDeclaration paramsDecl = super.createResultDeclaration(params);
        XMLBeanDeclaration resultDecl =
                new XMLBeanDeclaration(getDefinitionConfiguration(),
                        KEY_RESULT, true, CombinedConfiguration.class.getName());
        return new CombinedBeanDeclaration(resultDecl, paramsDecl);
    }

    /**
     * {@inheritDoc} This implementation processes the definition configuration
     * in order to
     * <ul>
     * <li>initialize the resulting {@code CombinedConfiguration}</li>
     * <li>determine the builders for all configuration sources</li>
     * <li>populate the resulting {@code CombinedConfiguration}</li>
     * </ul>
     */
    @Override
    protected void initResultInstance(CombinedConfiguration result)
            throws ConfigurationException
    {
        HierarchicalConfiguration config = getDefinitionConfiguration();
        if (config.getMaxIndex(KEY_COMBINER) < 0)
        {
            // No combiner defined => set default
            result.setNodeCombiner(new OverrideCombiner());
        }

        setUpCurrentParameters();
        initNodeCombinerListNodes(result, config, KEY_OVERRIDE_LIST);
        registerConfiguredProviders(config);
        setUpCurrentXMLParameters();
        currentXMLParameters.setFileSystem(initFileSystem(config));
        initSystemProperties(config, getBasePath());
        configureEntityResolver(config, currentXMLParameters);

        ConfigurationSourceData data = getSourceData();
        createAndAddConfigurations(result, data.getOverrideBuilders(), data);
        if (!data.getUnionBuilders().isEmpty())
        {
            CombinedConfiguration addConfig = createAdditionalsConfiguration(result);
            result.addConfiguration(addConfig, ADDITIONAL_NAME);
            initNodeCombinerListNodes(addConfig, config, KEY_ADDITIONAL_LIST);
            createAndAddConfigurations(addConfig, data.getUnionBuilders(), data);
        }
    }

    /**
     * Creates the {@code CombinedConfiguration} for the configuration
     * sources in the <code>&lt;additional&gt;</code> section. This method is
     * called when the builder constructs the final configuration. It creates a
     * new {@code CombinedConfiguration} and initializes some properties
     * from the result configuration.
     *
     * @param resultConfig the result configuration (this is the configuration
     *        that will be returned by the builder)
     * @return the {@code CombinedConfiguration} for the additional
     *         configuration sources
     * @since 1.7
     */
    protected CombinedConfiguration createAdditionalsConfiguration(
            CombinedConfiguration resultConfig)
    {
        CombinedConfiguration addConfig =
                new CombinedConfiguration(new UnionCombiner());
        addConfig.setDelimiterParsingDisabled(resultConfig
                .isDelimiterParsingDisabled());
        addConfig.setForceReloadCheck(resultConfig.isForceReloadCheck());
        addConfig.setIgnoreReloadExceptions(resultConfig
                .isIgnoreReloadExceptions());
        return addConfig;
    }

    /**
     * Registers StrLookups defined in the configuration.
     *
     * @throws ConfigurationException if an error occurs
     */
    protected void registerConfiguredLookups() throws ConfigurationException
    {
//        List<SubnodeConfiguration> nodes = configurationsAt(KEY_CONFIGURATION_LOOKUPS);
//        for (SubnodeConfiguration config : nodes)
//        {
//            XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
//            String key = config.getString(KEY_LOOKUP_KEY);
//            StrLookup lookup = (StrLookup) BeanHelper.createBean(decl);
//            BeanHelper.setProperty(lookup, "configuration", this);
//            ConfigurationInterpolator.registerGlobalLookup(key, lookup);
//            this.getInterpolator().registerLookup(key, lookup);
//        }
    }

    /**
     * Creates and initializes a default {@code FileSystem} if the definition
     * configuration contains a corresponding declaration. The file system
     * returned by this method is used as default for all file-based child
     * configuration sources.
     *
     * @param config the definition configuration
     * @return the default {@code FileSystem} (may be <b>null</b>)
     * @throws ConfigurationException if an error occurs
     */
    protected FileSystem initFileSystem(HierarchicalConfiguration config)
            throws ConfigurationException
    {
        if (config.getMaxIndex(FILE_SYSTEM) == 0)
        {
            XMLBeanDeclaration decl =
                    new XMLBeanDeclaration(config, FILE_SYSTEM);
            return (FileSystem) BeanHelper.createBean(decl);
        }
        return null;
    }

    /**
     * Handles a file with system properties that may be defined in the
     * definition configuration. If such property file is configured, all of its
     * properties are added to the system properties.
     *
     * @param config the definition configuration
     * @param basePath the base path defined for this builder (may be
     *        <b>null</b>)
     * @throws ConfigurationException if an error occurs.
     */
    protected void initSystemProperties(HierarchicalConfiguration config,
            String basePath) throws ConfigurationException
    {
        String fileName = config.getString(KEY_SYSTEM_PROPS);
        if (fileName != null)
        {
            try
            {
                SystemConfiguration.setSystemProperties(basePath, fileName);
            }
            catch (Exception ex)
            {
                throw new ConfigurationException(
                        "Error setting system properties from " + fileName, ex);
            }
        }
    }

    /**
     * Creates and initializes a default {@code EntityResolver} if the
     * definition configuration contains a corresponding declaration.
     *
     * @param config the definition configuration
     * @param xmlParams the (already partly initialized) object with XML
     *        parameters; here the new resolver is to be stored
     * @throws ConfigurationException if an error occurs
     */
    protected void configureEntityResolver(HierarchicalConfiguration config,
            XMLBuilderParametersImpl xmlParams) throws ConfigurationException
    {
        if (config.getMaxIndex(KEY_ENTITY_RESOLVER) == 0)
        {
            XMLBeanDeclaration decl =
                    new XMLBeanDeclaration(config, KEY_ENTITY_RESOLVER, true);
            EntityResolver resolver =
                    (EntityResolver) BeanHelper.createBean(decl,
                            CatalogResolver.class);
            FileSystem fileSystem = xmlParams.getFileHandler().getFileSystem();
            if (fileSystem != null)
            {
                BeanHelper.setProperty(resolver, "fileSystem", fileSystem);
            }
            String basePath = xmlParams.getFileHandler().getBasePath();
            if (basePath != null)
            {
                BeanHelper.setProperty(resolver, "baseDir", basePath);
            }
            // BeanHelper.setProperty(resolver, "substitutor",
            // getSubstitutor());
            // setEntityResolver(resolver);
            xmlParams.setEntityResolver(resolver);
        }
    }

    /**
     * Performs interpolation. This method will not only take this configuration
     * instance into account (which is the one that loaded the configuration
     * definition file), but also the so far constructed combined configuration.
     * So variables can be used that point to properties that are defined in
     * configuration sources loaded by this builder.
     *
     * @param value the value to be interpolated
     * @return the interpolated value
     */
//    @Override
//    protected Object interpolate(Object value)
//    {
//        Object result = super.interpolate(value);
//        if (constructedConfiguration != null)
//        {
//            result = constructedConfiguration.interpolate(result);
//        }
//        return result;
//    }

    /**
     * Returns the {@code ConfigurationBuilderProvider} for the given tag. This
     * method is called during creation of the result configuration. (It is not
     * allowed to call it at another point of time; result is then
     * unpredictable!) It supports all default providers and custom providers
     * added through the parameters object as well.
     *
     * @param tagName the name of the tag
     * @return the provider that was registered for this tag or <b>null</b> if
     *         there is none
     */
    protected ConfigurationBuilderProvider providerForTag(String tagName)
    {
        return currentParameters.providerForTag(tagName);
    }

    /**
     * Initializes a parameters object for a child builder. This combined
     * configuration builder has a bunch of properties which may be inherited by
     * child configurations, e.g. the base path, the file system, etc. While
     * processing the builders for child configurations, this method is called
     * for each parameters object for a child builder. It initializes some
     * properties of the passed in parameters objects which are derived from
     * this parent builder.
     *
     * @param params the parameters object to be initialized
     */
    protected void initChildBuilderParameters(BuilderParameters params)
    {
        if (params instanceof XMLBuilderProperties<?>)
        {
            initChildXMLParameters((XMLBuilderProperties<?>) params);
        }
        if(params instanceof FileBasedBuilderProperties<?>)
        {
            initChildFileBasedParameters((FileBasedBuilderProperties<?>) params);
        }
    }

    /**
     * Initializes the current parameters object. This object has either been
     * passed at builder configuration time or it is newly created. In any
     * case, it is manipulated during result creation.
     */
    private void setUpCurrentParameters()
    {
        currentParameters =
                CombinedBuilderParametersImpl.fromParameters(getParameters(), true);
        currentParameters.registerMissingProviders(DEFAULT_PROVIDERS_MAP);
    }

    /**
     * Sets up an XML parameters object which is used to store properties
     * related to XML and file-based configurations during creation of the
     * result configuration. The properties stored in this object can be
     * inherited to child configurations.
     *
     * @throws ConfigurationException if an error occurs
     */
    private void setUpCurrentXMLParameters() throws ConfigurationException
    {
        currentXMLParameters = new XMLBuilderParametersImpl();
        initDefaultBasePath();
    }

    /**
     * Initializes the default base path for all file-based child configuration
     * sources. The base path can be explicitly defined in the parameters of
     * this builder. Otherwise, if the definition builder is a file-based
     * builder, it is obtained from there.
     *
     * @throws ConfigurationException if an error occurs
     */
    private void initDefaultBasePath() throws ConfigurationException
    {
        assert currentParameters != null : "Current parameters undefined!";
        if (currentParameters.getBasePath() != null)
        {
            currentXMLParameters.setBasePath(currentParameters.getBasePath());
        }
        else
        {
            ConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                    getDefinitionBuilder();
            if (defBuilder instanceof FileBasedConfigurationBuilder<?>)
            {
                currentXMLParameters
                        .setBasePath(((FileBasedConfigurationBuilder<?>) defBuilder)
                                .getFileHandler().getBasePath());
            }
        }
    }

    /**
     * Initializes a parameters object for a file-based configuration with
     * properties already set for this parent builder. This method handles
     * properties like a default file system or a base path.
     *
     * @param params the parameters object
     */
    private void initChildFileBasedParameters(
            FileBasedBuilderProperties<?> params)
    {
        params.setBasePath(getBasePath());
        params.setFileSystem(currentXMLParameters.getFileHandler()
                .getFileSystem());
    }

    /**
     * Initializes a parameters object for an XML configuration with properties
     * already set for this parent builder.
     *
     * @param params the parameters object
     */
    private void initChildXMLParameters(XMLBuilderProperties<?> params)
    {
        params.setEntityResolver(currentXMLParameters.getEntityResolver());
    }

    /**
     * Obtains the data object for the configuration sources and the
     * corresponding builders. This object is created on first access and reset
     * when the definition builder sends a change event. This method is called
     * in a synchronized block.
     *
     * @return the object with information about configuration sources
     * @throws ConfigurationException if an error occurs
     */
    private ConfigurationSourceData getSourceData()
            throws ConfigurationException
    {
        if (sourceData == null)
        {
            if (currentParameters == null)
            {
                setUpCurrentParameters();
                setUpCurrentXMLParameters();
            }
            sourceData = createSourceData();
        }
        return sourceData;
    }

    /**
     * Creates the data object for configuration sources and the corresponding
     * builders.
     *
     * @return the newly created data object
     * @throws ConfigurationException if an error occurs
     */
    private ConfigurationSourceData createSourceData()
            throws ConfigurationException
    {
        ConfigurationSourceData result = new ConfigurationSourceData();
        result.initFromDefinitionConfiguration(getDefinitionConfiguration());
        return result;
    }

    /**
     * Returns the current base path of this configuration builder. This is used
     * for instance by all file-based child configurations.
     *
     * @return the base path
     */
    private String getBasePath()
    {
        return currentXMLParameters.getFileHandler().getBasePath();
    }

    /**
     * Registers providers defined in the configuration.
     *
     * @param defConfig the definition configuration
     * @throws ConfigurationException if an error occurs
     */
    private void registerConfiguredProviders(HierarchicalConfiguration defConfig)
            throws ConfigurationException
    {
        List<SubnodeConfiguration> nodes =
                defConfig.configurationsAt(KEY_CONFIGURATION_PROVIDERS);
        for (SubnodeConfiguration config : nodes)
        {
            XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
            String key = config.getString(KEY_PROVIDER_KEY);
            currentParameters.registerProvider(key,
                    (ConfigurationBuilderProvider) BeanHelper.createBean(decl));
        }
    }

    /**
     * Adds a listener at the given definition builder which resets this builder
     * when a reset of the definition builder happens. This way it is ensured
     * that this builder produces a new combined configuration when its
     * definition configuration changes.
     *
     * @param defBuilder the definition builder
     */
    private void addDefinitionBuilderChangeListener(
            final ConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder)
    {
        defBuilder.addBuilderListener(new BuilderListener()
        {
            public void builderReset(
                    ConfigurationBuilder<? extends Configuration> builder)
            {
                synchronized (CombinedConfigurationBuilder.this)
                {
                    reset();
                    definitionBuilder = defBuilder;
                }
            }
        });
    }

    /**
     * Creates a default builder for the definition configuration and
     * initializes it with a parameters object. The default builder creates an
     * {@code XMLConfiguration}; it expects a corresponding file specification.
     *
     * @param builderParams the parameters object for the builder
     * @return the standard builder for the definition configuration
     */
    private static ConfigurationBuilder<? extends HierarchicalConfiguration> createXMLDefinitionBuilder(
            BuilderParameters builderParams)
    {
        return new FileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class).configure(builderParams);
    }

    /**
     * Initializes the list nodes of the node combiner for the given combined
     * configuration. This information can be set in the header section of the
     * configuration definition file for both the override and the union
     * combiners.
     *
     * @param cc the combined configuration to initialize
     * @param defConfig the definition configuration
     * @param key the key for the list nodes
     */
    private static void initNodeCombinerListNodes(CombinedConfiguration cc,
            HierarchicalConfiguration defConfig, String key)
    {
        List<Object> listNodes = defConfig.getList(key);
        for (Object listNode : listNodes)
        {
            cc.getNodeCombiner().addListNode((String) listNode);
        }
    }

    /**
     * Queries the current {@code Configuration} objects from the given builders
     * and adds them to the specified combined configuration.
     *
     * @param cc the resulting combined configuration
     * @param builders the collection with configuration builders
     * @param srcData the data object for configuration sources
     * @throws ConfigurationException if an error occurs
     */
    private static void createAndAddConfigurations(CombinedConfiguration cc,
            Collection<ConfigurationBuilder<? extends Configuration>> builders,
            ConfigurationSourceData srcData) throws ConfigurationException
    {
        for (ConfigurationBuilder<? extends Configuration> builder : builders)
        {
            ConfigurationDeclaration decl = srcData.getDeclaration(builder);
            assert decl != null : "Cannot resolve builder!";
            try
            {
                cc.addConfiguration(
                        (AbstractConfiguration) builder.getConfiguration(),
                        decl.getName(), decl.getAt());
            }
            catch (ConfigurationException cex)
            {
                // ignore exceptions for optional configurations
                if (!decl.isOptional())
                {
                    throw cex;
                }
            }
        }
    }

    /**
     * Creates the map with the default configuration builder providers.
     *
     * @return the map with default providers
     */
    private static Map<String, ConfigurationBuilderProvider> createDefaultProviders()
    {
        Map<String, ConfigurationBuilderProvider> providers =
                new HashMap<String, ConfigurationBuilderProvider>();
        for (int i = 0; i < DEFAULT_TAGS.length; i++)
        {
            providers.put(DEFAULT_TAGS[i], DEFAULT_PROVIDERS[i]);
        }
        return providers;
    }

    static
    {
        DEFAULT_PROVIDERS_MAP = createDefaultProviders();
    }

    /**
     * A data class for storing information about all configuration sources
     * defined for a combined builder.
     */
    private class ConfigurationSourceData
    {
        /** A list with all builders for override configurations. */
        private final Collection<ConfigurationBuilder<? extends Configuration>> overrideBuilders;

        /** A list with all builders for union configurations. */
        private final Collection<ConfigurationBuilder<? extends Configuration>> unionBuilders;

        /** A list with all sub builders (override plus union). */
        private final Collection<ConfigurationBuilder<? extends Configuration>> allBuilders;

        /** A map for direct access to a builder by its name. */
        private final Map<String, ConfigurationBuilder<? extends Configuration>> namedBuilders;

        /**
         * A map for retrieving the bean declarations associated with
         * configuration builders.
         */
        private final Map<ConfigurationBuilder<? extends Configuration>, ConfigurationDeclaration> declarations;

        /** A listener for reacting on changes of sub builders. */
        private BuilderListener changeListener;

        /**
         * Creates a new instance of {@code ConfigurationSourceData}.
         */
        public ConfigurationSourceData()
        {
            overrideBuilders =
                    new LinkedList<ConfigurationBuilder<? extends Configuration>>();
            unionBuilders =
                    new LinkedList<ConfigurationBuilder<? extends Configuration>>();
            allBuilders =
                    new LinkedList<ConfigurationBuilder<? extends Configuration>>();
            namedBuilders =
                    new HashMap<String, ConfigurationBuilder<? extends Configuration>>();
            declarations =
                    new HashMap<ConfigurationBuilder<? extends Configuration>, ConfigurationDeclaration>();
        }

        /**
         * Initializes this object from the specified definition configuration.
         *
         * @param config the definition configuration
         * @throws ConfigurationException if an error occurs
         */
        public void initFromDefinitionConfiguration(
                HierarchicalConfiguration config) throws ConfigurationException
        {
            createBuilders(overrideBuilders,
                    fetchTopLevelOverrideConfigs(config));
            createBuilders(overrideBuilders,
                    config.childConfigurationsAt(KEY_OVERRIDE));
            createBuilders(unionBuilders,
                    config.childConfigurationsAt(KEY_UNION));

            allBuilders.addAll(overrideBuilders);
            allBuilders.addAll(unionBuilders);
            registerChangeListener();
        }

        /**
         * Frees resources used by this object and performs clean up. This
         * method is called when the owning builder is reset.
         */
        public void cleanUp()
        {
            for (ConfigurationBuilder<?> b : allBuilders)
            {
                b.removeBuilderListener(changeListener);
            }
        }

        /**
         * Returns a collection with all configuration builders defined in the
         * override section.
         *
         * @return the override configuration builders
         */
        public Collection<ConfigurationBuilder<? extends Configuration>> getOverrideBuilders()
        {
            return overrideBuilders;
        }

        /**
         * Returns a collection with all configuration builders defined in the
         * union section.
         *
         * @return the union configuration builders
         */
        public Collection<ConfigurationBuilder<? extends Configuration>> getUnionBuilders()
        {
            return unionBuilders;
        }

        /**
         * Returns the {@code ConfigurationBuilder} with the given name. If no
         * such builder is defined in the definition configuration, result is
         * <b>null</b>.
         *
         * @param name the name of the builder in question
         * @return the builder with this name or <b>null</b>
         */
        public ConfigurationBuilder<? extends Configuration> getNamedBuilder(
                String name)
        {
            return namedBuilders.get(name);
        }

        /**
         * Returns a set with the names of all known named builders.
         *
         * @return the names of the available sub builders
         */
        public Set<String> builderNames()
        {
            return namedBuilders.keySet();
        }

        /**
         * Returns the {@code ConfigurationDeclaration} associated with the
         * specified builder. If the builder is unknown, result is <b>null</b>.
         *
         * @param builder the builder in question
         * @return the {@code ConfigurationDeclaration} for this builder or
         *         <b>null</b>
         */
        public ConfigurationDeclaration getDeclaration(
                ConfigurationBuilder<?> builder)
        {
            return declarations.get(builder);
        }

        /**
         * Registers a change listener at all sub builders. Whenever one of the
         * sub builders is reset, the combined configuration managed by this
         * builder has to be reset, too.
         */
        private void registerChangeListener()
        {
            changeListener = new BuilderListener()
            {
                public void builderReset(
                        ConfigurationBuilder<? extends Configuration> builder)
                {
                    resetResult();
                }
            };

            for (ConfigurationBuilder<?> b : allBuilders)
            {
                b.addBuilderListener(changeListener);
            }
        }

        /**
         * Finds the override configurations that are defined as top level
         * elements in the configuration definition file. This method fetches
         * the child elements of the root node and removes the nodes that
         * represent other configuration sections. The remaining nodes are
         * treated as definitions for override configurations.
         *
         * @param config the definition configuration
         * @return a list with sub configurations for the top level override
         *         configurations
         */
        private List<SubnodeConfiguration> fetchTopLevelOverrideConfigs(
                HierarchicalConfiguration config)
        {
            List<SubnodeConfiguration> configs =
                    config.childConfigurationsAt(null);
            for (Iterator<SubnodeConfiguration> it = configs.iterator(); it
                    .hasNext();)
            {
                String nodeName = it.next().getRootElementName();
                for (int i = 0; i < CONFIG_SECTIONS.length; i++)
                {
                    if (CONFIG_SECTIONS[i].equals(nodeName))
                    {
                        it.remove();
                        break;
                    }
                }
            }
            return configs;
        }

        /**
         * Creates configuration builder objects from the given configurations
         * for configuration sources.
         *
         * @param builders the collection with builders to be filled
         * @param sources the definitions for the single configuration sources
         * @throws ConfigurationException if an error occurs
         */
        private void createBuilders(
                Collection<ConfigurationBuilder<? extends Configuration>> builders,
                Collection<? extends HierarchicalConfiguration> sources)
                throws ConfigurationException
        {
            for (HierarchicalConfiguration src : sources)
            {
                ConfigurationBuilderProvider provider =
                        providerForTag(src.getRootElementName());
                if (provider == null)
                {
                    throw new ConfigurationException(
                            "Unsupported configuration source: "
                                    + src.getRootElementName());
                }

                ConfigurationDeclaration decl =
                        new ConfigurationDeclaration(
                                CombinedConfigurationBuilder.this, src);
                ConfigurationBuilder<? extends Configuration> builder =
                        provider.getConfiguration(decl);
                builders.add(builder);
                declarations.put(builder, decl);
                if (decl.getName() != null)
                {
                    namedBuilders.put(decl.getName(), builder);
                }
            }
        }
    }
}
