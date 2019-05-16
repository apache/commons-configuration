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
package org.apache.commons.configuration2.builder.combined;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationLookup;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.CombinedBeanDeclaration;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration2.builder.XMLBuilderProperties;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.xml.sax.EntityResolver;

/**
 * <p>
 * A specialized {@code ConfigurationBuilder} implementation that creates a
 * {@link CombinedConfiguration} from multiple configuration sources defined by
 * an XML-based <em>configuration definition file</em>.
 * </p>
 * <p>
 * This class provides an easy and flexible means for loading multiple
 * configuration sources and combining the results into a single configuration
 * object. The sources to be loaded are defined in an XML document that can
 * contain certain tags representing the different supported configuration
 * classes. If such a tag is found, a corresponding {@code ConfigurationBuilder}
 * class is instantiated and initialized using the classes of the
 * {@code beanutils} package (namely
 * {@link org.apache.commons.configuration2.beanutils.XMLBeanDeclaration
 * XMLBeanDeclaration} will be used to extract the configuration's
 * initialization parameters, which allows for complex initialization
 * scenarios).
 * </p>
 * <p>
 * It is also possible to add custom tags to the configuration definition file.
 * For this purpose an implementation of
 * {@link CombinedConfigurationBuilderProvider} has to be created which is
 * responsible for the creation of a {@code ConfigurationBuilder} associated
 * with the custom tag. An instance of this class has to be registered at the
 * {@link CombinedBuilderParametersImpl} object which is used to initialize this
 * {@code CombinedConfigurationBuilder}. This provider will then be called when
 * the corresponding custom tag is detected. For many default configuration
 * classes providers are already registered.
 * </p>
 * <p>
 * The configuration definition file has the following basic structure:
 * </p>
 *
 * <pre>
 * &lt;configuration systemProperties="properties file name"&gt;
 *   &lt;header&gt;
 *     &lt;!-- Optional meta information about the combined configuration --&gt;
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
 * <p>
 * The name of the root element (here {@code configuration}) is arbitrary. The
 * optional {@code systemProperties} attribute identifies the path to a property
 * file containing properties that should be added to the system properties. If
 * specified on the root element, the system properties are set before the rest
 * of the configuration is processed.
 * </p>
 * <p>
 * There are two sections (both of them are optional) for declaring
 * <em>override</em> and <em>additional</em> configurations. Configurations in
 * the former section are evaluated in the order of their declaration, and
 * properties of configurations declared earlier hide those of configurations
 * declared later. Configurations in the latter section are combined to a union
 * configuration, i.e. all of their properties are added to a large hierarchical
 * configuration. Configuration declarations that occur as direct children of
 * the root element are treated as override declarations.
 * </p>
 * <p>
 * Each configuration declaration consists of a tag whose name is associated
 * with a {@code CombinedConfigurationBuilderProvider}. This can be one of the
 * predefined tags like {@code properties}, or {@code xml}, or a custom tag, for
 * which a configuration builder provider was registered (as described above).
 * Attributes and sub elements with specific initialization parameters can be
 * added. There are some reserved attributes with a special meaning that can be
 * used in every configuration declaration:
 * </p>
 * <table border="1">
 * <caption>Standard attributes for configuration declarations</caption>
 * <tr>
 * <th>Attribute</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-name}</td>
 * <td>Allows specifying a name for this configuration. This name can be used to
 * obtain a reference to the configuration from the resulting combined
 * configuration (see below). It can also be passed to the
 * {@link #getNamedBuilder(String)} method.</td>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-at}</td>
 * <td>With this attribute an optional prefix can be specified for the
 * properties of the corresponding configuration.</td>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-optional}</td>
 * <td>Declares a configuration source as optional. This means that errors that
 * occur when creating the configuration are ignored.</td>
 * </tr>
 * <tr>
 * <td valign="top">{@code config-reload}</td>
 * <td>Many configuration sources support a reloading mechanism. For those
 * sources it is possible to enable reloading by providing this attribute with a
 * value of <strong>true</strong>.</td>
 * </tr>
 * </table>
 * <p>
 * The optional <em>header</em> section can contain some meta data about the
 * created configuration itself. For instance, it is possible to set further
 * properties of the {@code NodeCombiner} objects used for constructing the
 * resulting configuration.
 * </p>
 * <p>
 * The default configuration object returned by this builder is an instance of
 * the {@link CombinedConfiguration} class. This allows for convenient access to
 * the configuration objects maintained by the combined configuration (e.g. for
 * updates of single configuration objects). It has also the advantage that the
 * properties stored in all declared configuration objects are collected and
 * transformed into a single hierarchical structure, which can be accessed using
 * different expression engines. The actual {@code CombinedConfiguration}
 * implementation can be overridden by specifying the class in the
 * <em>config-class</em> attribute of the result element.
 * </p>
 * <p>
 * A custom EntityResolver can be used for all XMLConfigurations by adding
 * </p>
 *
 * <pre>
 * &lt;entity-resolver config-class="EntityResolver fully qualified class name"&gt;
 * </pre>
 *
 * <p>
 * A specific CatalogResolver can be specified for all XMLConfiguration sources
 * by adding
 * </p>
 * <pre>
 * &lt;entity-resolver catalogFiles="comma separated list of catalog files"&gt;
 * </pre>
 *
 * <p>
 * Additional ConfigurationProviders can be added by configuring them in the
 * <em>header</em> section.
 * </p>
 *
 * <pre>
 * &lt;providers&gt;
 *   &lt;provider config-tag="tag name" config-class="provider fully qualified class name"/&gt;
 * &lt;/providers&gt;
 * </pre>
 *
 * <p>
 * Additional variable resolvers can be added by configuring them in the
 * <em>header</em> section.
 * </p>
 *
 * <pre>
 * &lt;lookups&gt;
 *   &lt;lookup config-prefix="prefix" config-class="StrLookup fully qualified class name"/&gt;
 * &lt;/lookups&gt;
 * </pre>
 *
 * <p>
 * All declared override configurations are directly added to the resulting
 * combined configuration. If they are given names (using the
 * {@code config-name} attribute), they can directly be accessed using the
 * {@code getConfiguration(String)} method of {@code CombinedConfiguration}. The
 * additional configurations are altogether added to another combined
 * configuration, which uses a union combiner. Then this union configuration is
 * added to the resulting combined configuration under the name defined by the
 * {@code ADDITIONAL_NAME} constant. The {@link #getNamedBuilder(String)} method
 * can be used to access the {@code ConfigurationBuilder} objects for all
 * configuration sources which have been assigned a name; care has to be taken
 * that these names are unique.
 * </p>
 *
 * @since 1.3
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
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

    /** Constant for the name of the configuration bean factory. */
    static final String CONFIG_BEAN_FACTORY_NAME = CombinedConfigurationBuilder.class
            .getName()
            + ".CONFIG_BEAN_FACTORY_NAME";

    /** Constant for the reserved name attribute. */
    static final String ATTR_NAME = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "name"
            + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the name of the at attribute. */
    static final String ATTR_ATNAME = "at";

    /** Constant for the reserved at attribute. */
    static final String ATTR_AT_RES = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + ATTR_ATNAME
            + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the at attribute without the reserved prefix. */
    static final String ATTR_AT = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + ATTR_ATNAME + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the name of the optional attribute. */
    static final String ATTR_OPTIONALNAME = "optional";

    /** Constant for the reserved optional attribute. */
    static final String ATTR_OPTIONAL_RES = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + ATTR_OPTIONALNAME
            + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the optional attribute without the reserved prefix. */
    static final String ATTR_OPTIONAL = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + ATTR_OPTIONALNAME + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the forceCreate attribute. */
    static final String ATTR_FORCECREATE = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "forceCreate"
            + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

    /** Constant for the reload attribute. */
    static final String ATTR_RELOAD = DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_START
            + XMLBeanDeclaration.RESERVED_PREFIX
            + "reload"
            + DefaultExpressionEngineSymbols.DEFAULT_ATTRIBUTE_END;

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
            "org.apache.commons.configuration2.builder.BasicConfigurationBuilder";

    /** Constant for the file-based configuration builder class. */
    private static final String FILE_BUILDER =
            "org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder";

    /** Constant for the reloading file-based configuration builder class. */
    private static final String RELOADING_BUILDER =
            "org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder";

    /** Constant for the name of the file-based builder parameters class. */
    private static final String FILE_PARAMS =
            "org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl";

    /** Constant for the provider for properties files. */
    private static final ConfigurationBuilderProvider PROPERTIES_PROVIDER =
            new FileExtensionConfigurationBuilderProvider(
                    FILE_BUILDER,
                    RELOADING_BUILDER,
                    "org.apache.commons.configuration2.XMLPropertiesConfiguration",
                    "org.apache.commons.configuration2.PropertiesConfiguration",
                    EXT_XML, Collections.singletonList(FILE_PARAMS));

    /** Constant for the provider for XML files. */
    private static final ConfigurationBuilderProvider XML_PROVIDER =
            new BaseConfigurationBuilderProvider(FILE_BUILDER, RELOADING_BUILDER,
                    "org.apache.commons.configuration2.XMLConfiguration",
                    Collections.singletonList("org.apache.commons.configuration2.builder.XMLBuilderParametersImpl"));

    /** Constant for the provider for JNDI sources. */
    private static final BaseConfigurationBuilderProvider JNDI_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration2.JNDIConfiguration",
                    Collections.singletonList("org.apache.commons.configuration2.builder.JndiBuilderParametersImpl"));

    /** Constant for the provider for system properties. */
    private static final BaseConfigurationBuilderProvider SYSTEM_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration2.SystemConfiguration",
                    Collections.singletonList("org.apache.commons.configuration2.builder.BasicBuilderParameters"));

    /** Constant for the provider for ini files. */
    private static final BaseConfigurationBuilderProvider INI_PROVIDER =
            new BaseConfigurationBuilderProvider(FILE_BUILDER, RELOADING_BUILDER,
                    "org.apache.commons.configuration2.INIConfiguration",
                    Collections.singletonList(FILE_PARAMS));

    /** Constant for the provider for environment properties. */
    private static final BaseConfigurationBuilderProvider ENV_PROVIDER =
            new BaseConfigurationBuilderProvider(
                    BASIC_BUILDER,
                    null,
                    "org.apache.commons.configuration2.EnvironmentConfiguration",
                    Collections.singletonList("org.apache.commons.configuration2.builder.BasicBuilderParameters"));

    /** Constant for the provider for plist files. */
    private static final BaseConfigurationBuilderProvider PLIST_PROVIDER =
            new FileExtensionConfigurationBuilderProvider(
                    FILE_BUILDER,
                    RELOADING_BUILDER,
                    "org.apache.commons.configuration2.plist.XMLPropertyListConfiguration",
                    "org.apache.commons.configuration2.plist.PropertyListConfiguration",
                    EXT_XML, Collections.singletonList(FILE_PARAMS));

    /** Constant for the provider for configuration definition files. */
    private static final BaseConfigurationBuilderProvider COMBINED_PROVIDER =
            new CombinedConfigurationBuilderProvider();

    /** Constant for the provider for multiple XML configurations. */
    private static final MultiFileConfigurationBuilderProvider MULTI_XML_PROVIDER =
            new MultiFileConfigurationBuilderProvider(
                    "org.apache.commons.configuration2.XMLConfiguration",
                    "org.apache.commons.configuration2.builder.XMLBuilderParametersImpl");

    /** An array with the names of the default tags. */
    private static final String[] DEFAULT_TAGS = {
            "properties", "xml", "hierarchicalXml", "plist",
            "ini", "system", "env", "jndi", "configuration", "multiFile"
    };

    /** An array with the providers for the default tags. */
    private static final ConfigurationBuilderProvider[] DEFAULT_PROVIDERS = {
            PROPERTIES_PROVIDER, XML_PROVIDER, XML_PROVIDER, PLIST_PROVIDER, INI_PROVIDER,
            SYSTEM_PROVIDER, ENV_PROVIDER, JNDI_PROVIDER, COMBINED_PROVIDER,
            MULTI_XML_PROVIDER
    };

    /** A map with the default configuration builder providers. */
    private static final Map<String, ConfigurationBuilderProvider> DEFAULT_PROVIDERS_MAP;

    /** The builder for the definition configuration. */
    private ConfigurationBuilder<? extends HierarchicalConfiguration<?>> definitionBuilder;

    /** Stores temporarily the configuration with the builder definitions. */
    private HierarchicalConfiguration<?> definitionConfiguration;

    /** The object with data about configuration sources. */
    private ConfigurationSourceData sourceData;

    /** Stores the current parameters object. */
    private CombinedBuilderParametersImpl currentParameters;

    /** The current XML parameters object. */
    private XMLBuilderParametersImpl currentXMLParameters;

    /** The configuration that is currently constructed. */
    private CombinedConfiguration currentConfiguration;

    /**
     * A {@code ConfigurationInterpolator} to be used as parent for all child
     * configurations to enable cross-source interpolation.
     */
    private ConfigurationInterpolator parentInterpolator;

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
    public CombinedConfigurationBuilder(final Map<String, Object> params)
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
    public CombinedConfigurationBuilder(final Map<String, Object> params, final boolean allowFailOnInit)
    {
        super(CombinedConfiguration.class, params, allowFailOnInit);
    }

    /**
     * Returns the {@code ConfigurationBuilder} which creates the definition
     * configuration.
     *
     * @return the builder for the definition configuration
     * @throws ConfigurationException if an error occurs
     */
    public synchronized ConfigurationBuilder<? extends HierarchicalConfiguration<?>> getDefinitionBuilder()
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
     * {@inheritDoc} This method is overridden to adapt the return type.
     */
    @Override
    public CombinedConfigurationBuilder configure(final BuilderParameters... params)
    {
        super.configure(params);
        return this;
    }

    /**
     * <p>
     * Returns the configuration builder with the given name. With this method a
     * builder of a child configuration which was given a name in the
     * configuration definition file can be accessed directly.
     * </p>
     * <p>
     * <strong>Important note:</strong> This method only returns a meaningful
     * result after the result configuration has been created by calling
     * {@code getConfiguration()}. If called before, always an exception is
     * thrown.
     * </p>
     *
     * @param name the name of the builder in question
     * @return the child configuration builder with this name
     * @throws ConfigurationException if information about named builders is not
     *         yet available or no builder with this name exists
     */
    public synchronized ConfigurationBuilder<? extends Configuration> getNamedBuilder(
            final String name) throws ConfigurationException
    {
        if (sourceData == null)
        {
            throw new ConfigurationException("Information about child builders"
                    + " has not been setup yet! Call getConfiguration() first.");
        }
        final ConfigurationBuilder<? extends Configuration> builder =
                sourceData.getNamedBuilder(name);
        if (builder == null)
        {
            throw new ConfigurationException("Builder cannot be resolved: "
                    + name);
        }
        return builder;
    }

    /**
     * <p>
     * Returns a set with the names of all child configuration builders. A tag
     * defining a configuration source in the configuration definition file can
     * have the {@code config-name} attribute. If this attribute is present, the
     * corresponding builder is assigned this name and can be directly accessed
     * through the {@link #getNamedBuilder(String)} method. This method returns
     * a collection with all available builder names.
     * </p>
     * <p>
     * <strong>Important note:</strong> This method only returns a meaningful
     * result after the result configuration has been created by calling
     * {@code getConfiguration()}. If called before, always an empty set is
     * returned.
     * </p>
     *
     * @return a set with the names of all builders
     */
    public synchronized Set<String> builderNames()
    {
        if (sourceData == null)
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(sourceData.builderNames());
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
    protected ConfigurationBuilder<? extends HierarchicalConfiguration<?>> setupDefinitionBuilder(
            final Map<String, Object> params) throws ConfigurationException
    {
        final CombinedBuilderParametersImpl cbParams =
                CombinedBuilderParametersImpl.fromParameters(params);
        if (cbParams != null)
        {
            final ConfigurationBuilder<? extends HierarchicalConfiguration<?>> defBuilder =
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

        final BuilderParameters fileParams =
                FileBasedBuilderParametersImpl.fromParameters(params);
        if (fileParams != null)
        {
            return createXMLDefinitionBuilder(fileParams);
        }

        throw new ConfigurationException(
                "No builder for configuration definition specified!");
    }

    /**
     * Creates a default builder for the definition configuration and
     * initializes it with a parameters object. This method is called if no
     * definition builder is defined in this builder's parameters. This
     * implementation creates a default file-based builder which produces an
     * {@code XMLConfiguration}; it expects a corresponding file specification.
     * Note: This method is called in a synchronized block.
     *
     * @param builderParams the parameters object for the builder
     * @return the standard builder for the definition configuration
     */
    protected ConfigurationBuilder<? extends HierarchicalConfiguration<?>> createXMLDefinitionBuilder(
            final BuilderParameters builderParams)
    {
        return new FileBasedConfigurationBuilder<>(
                XMLConfiguration.class).configure(builderParams);
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
    protected HierarchicalConfiguration<?> getDefinitionConfiguration()
            throws ConfigurationException
    {
        if (definitionConfiguration == null)
        {
            definitionConfiguration = getDefinitionBuilder().getConfiguration();
        }
        return definitionConfiguration;
    }

    /**
     * Returns a collection with the builders for all child configuration
     * sources. This method can be used by derived classes providing additional
     * functionality on top of the declared configuration sources. It only
     * returns a defined value during construction of the result configuration
     * instance.
     *
     * @return a collection with the builders for child configuration sources
     */
    protected synchronized Collection<ConfigurationBuilder<? extends Configuration>> getChildBuilders()
    {
        return sourceData.getChildBuilders();
    }

    /**
     * {@inheritDoc} This implementation evaluates the {@code result} property
     * of the definition configuration. It creates a combined bean declaration
     * with both the properties specified in the definition file and the
     * properties defined as initialization parameters.
     */
    @Override
    protected BeanDeclaration createResultDeclaration(final Map<String, Object> params)
            throws ConfigurationException
    {
        final BeanDeclaration paramsDecl = super.createResultDeclaration(params);
        final XMLBeanDeclaration resultDecl =
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
    protected void initResultInstance(final CombinedConfiguration result)
            throws ConfigurationException
    {
        super.initResultInstance(result);

        currentConfiguration = result;
        final HierarchicalConfiguration<?> config = getDefinitionConfiguration();
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
        registerConfiguredLookups(config, result);
        configureEntityResolver(config, currentXMLParameters);
        setUpParentInterpolator(currentConfiguration, config);

        final ConfigurationSourceData data = getSourceData();
        final boolean createBuilders = data.getChildBuilders().isEmpty();
        final List<ConfigurationBuilder<? extends Configuration>> overrideBuilders =
                data.createAndAddConfigurations(result,
                        data.getOverrideSources(), data.overrideBuilders);
        if (createBuilders)
        {
            data.overrideBuilders.addAll(overrideBuilders);
        }
        if (!data.getUnionSources().isEmpty())
        {
            final CombinedConfiguration addConfig = createAdditionalsConfiguration(result);
            result.addConfiguration(addConfig, ADDITIONAL_NAME);
            initNodeCombinerListNodes(addConfig, config, KEY_ADDITIONAL_LIST);
            final List<ConfigurationBuilder<? extends Configuration>> unionBuilders =
                    data.createAndAddConfigurations(addConfig,
                            data.unionDeclarations, data.unionBuilders);
            if (createBuilders)
            {
                data.unionBuilders.addAll(unionBuilders);
            }
        }

        result.isEmpty();  // this sets up the node structure
        currentConfiguration = null;
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
            final CombinedConfiguration resultConfig)
    {
        final CombinedConfiguration addConfig =
                new CombinedConfiguration(new UnionCombiner());
        addConfig.setListDelimiterHandler(resultConfig.getListDelimiterHandler());
        return addConfig;
    }

    /**
     * Processes custom {@link Lookup} objects that might be declared in the
     * definition configuration. Each {@code Lookup} object is registered at the
     * definition configuration and at the result configuration. It is also
     * added to all child configurations added to the resulting combined
     * configuration.
     *
     * @param defConfig the definition configuration
     * @param resultConfig the resulting configuration
     * @throws ConfigurationException if an error occurs
     */
    protected void registerConfiguredLookups(
            final HierarchicalConfiguration<?> defConfig, final Configuration resultConfig)
            throws ConfigurationException
    {
        final Map<String, Lookup> lookups = new HashMap<>();

        final List<? extends HierarchicalConfiguration<?>> nodes =
                defConfig.configurationsAt(KEY_CONFIGURATION_LOOKUPS);
        for (final HierarchicalConfiguration<?> config : nodes)
        {
            final XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
            final String key = config.getString(KEY_LOOKUP_KEY);
            final Lookup lookup = (Lookup) fetchBeanHelper().createBean(decl);
            lookups.put(key, lookup);
        }

        if (!lookups.isEmpty())
        {
            final ConfigurationInterpolator defCI = defConfig.getInterpolator();
            if (defCI != null)
            {
                defCI.registerLookups(lookups);
            }
            resultConfig.getInterpolator().registerLookups(lookups);
        }
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
    protected FileSystem initFileSystem(final HierarchicalConfiguration<?> config)
            throws ConfigurationException
    {
        if (config.getMaxIndex(FILE_SYSTEM) == 0)
        {
            final XMLBeanDeclaration decl =
                    new XMLBeanDeclaration(config, FILE_SYSTEM);
            return (FileSystem) fetchBeanHelper().createBean(decl);
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
    protected void initSystemProperties(final HierarchicalConfiguration<?> config,
            final String basePath) throws ConfigurationException
    {
        final String fileName = config.getString(KEY_SYSTEM_PROPS);
        if (fileName != null)
        {
            try
            {
                SystemConfiguration.setSystemProperties(basePath, fileName);
            }
            catch (final Exception ex)
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
    protected void configureEntityResolver(final HierarchicalConfiguration<?> config,
            final XMLBuilderParametersImpl xmlParams) throws ConfigurationException
    {
        if (config.getMaxIndex(KEY_ENTITY_RESOLVER) == 0)
        {
            final XMLBeanDeclaration decl =
                    new XMLBeanDeclaration(config, KEY_ENTITY_RESOLVER, true);
            final EntityResolver resolver =
                    (EntityResolver) fetchBeanHelper().createBean(decl,
                            CatalogResolver.class);
            final FileSystem fileSystem = xmlParams.getFileHandler().getFileSystem();
            if (fileSystem != null)
            {
                BeanHelper.setProperty(resolver, "fileSystem", fileSystem);
            }
            final String basePath = xmlParams.getFileHandler().getBasePath();
            if (basePath != null)
            {
                BeanHelper.setProperty(resolver, "baseDir", basePath);
            }
            final ConfigurationInterpolator ci = new ConfigurationInterpolator();
            ci.registerLookups(fetchPrefixLookups());
            BeanHelper.setProperty(resolver, "interpolator", ci);

            xmlParams.setEntityResolver(resolver);
        }
    }

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
    protected ConfigurationBuilderProvider providerForTag(final String tagName)
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
    protected void initChildBuilderParameters(final BuilderParameters params)
    {
        initDefaultChildParameters(params);

        if (params instanceof BasicBuilderParameters)
        {
            initChildBasicParameters((BasicBuilderParameters) params);
        }
        if (params instanceof XMLBuilderProperties<?>)
        {
            initChildXMLParameters((XMLBuilderProperties<?>) params);
        }
        if (params instanceof FileBasedBuilderProperties<?>)
        {
            initChildFileBasedParameters((FileBasedBuilderProperties<?>) params);
        }
        if (params instanceof CombinedBuilderParametersImpl)
        {
            initChildCombinedParameters((CombinedBuilderParametersImpl) params);
        }
    }

    /**
     * Initializes the event listeners of the specified builder from this
     * object. This method is used to inherit all listeners from a parent
     * builder.
     *
     * @param dest the destination builder object which is to be initialized
     */
    void initChildEventListeners(
            final BasicConfigurationBuilder<? extends Configuration> dest)
    {
        copyEventListeners(dest);
    }

    /**
     * Returns the configuration object that is currently constructed. This
     * method can be called during construction of the result configuration. It
     * is intended for internal usage, e.g. some specialized builder providers
     * need access to this configuration to perform advanced initialization.
     *
     * @return the configuration that us currently under construction
     */
    CombinedConfiguration getConfigurationUnderConstruction()
    {
        return currentConfiguration;
    }

    /**
     * Initializes a bean using the current {@code BeanHelper}. This is needed
     * by builder providers when the configuration objects for sub builders are
     * constructed.
     *
     * @param bean the bean to be initialized
     * @param decl the {@code BeanDeclaration}
     */
    void initBean(final Object bean, final BeanDeclaration decl)
    {
        fetchBeanHelper().initBean(bean, decl);
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
     * Sets up a parent {@code ConfigurationInterpolator} object. This object
     * has a default {@link Lookup} querying the resulting combined
     * configuration. Thus interpolation works globally across all configuration
     * sources.
     *
     * @param resultConfig the result configuration
     * @param defConfig the definition configuration
     */
    private void setUpParentInterpolator(final Configuration resultConfig,
            final Configuration defConfig)
    {
        parentInterpolator = new ConfigurationInterpolator();
        parentInterpolator.addDefaultLookup(new ConfigurationLookup(
                resultConfig));
        final ConfigurationInterpolator defInterpolator = defConfig.getInterpolator();
        if (defInterpolator != null)
        {
            defInterpolator.setParentInterpolator(parentInterpolator);
        }
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
            final ConfigurationBuilder<? extends HierarchicalConfiguration<?>> defBuilder =
                    getDefinitionBuilder();
            if (defBuilder instanceof FileBasedConfigurationBuilder)
            {
                @SuppressWarnings("rawtypes")
                final
                FileBasedConfigurationBuilder fileBuilder =
                        (FileBasedConfigurationBuilder) defBuilder;
                final URL url = fileBuilder.getFileHandler().getURL();
                currentXMLParameters.setBasePath((url != null) ? url
                        .toExternalForm() : fileBuilder.getFileHandler()
                        .getBasePath());
            }
        }
    }

    /**
     * Executes the {@link org.apache.commons.configuration2.builder.DefaultParametersManager
     * DefaultParametersManager} stored in the current
     * parameters on the passed in parameters object. If default handlers have been
     * registered for this type of parameters, an initialization is now
     * performed. This method is called before the parameters object is
     * initialized from the configuration definition file. So default values
     * can be overridden later with concrete property definitions.
     *
     * @param params the parameters to be initialized
     * @throws org.apache.commons.configuration2.ex.ConfigurationRuntimeException if an error
     *         occurs when copying properties
     */
    private void initDefaultChildParameters(final BuilderParameters params)
    {
        currentParameters.getChildDefaultParametersManager()
                .initializeParameters(params);
    }

    /**
     * Initializes basic builder parameters for a child configuration with
     * default settings set for this builder. This implementation ensures that
     * all {@code Lookup} objects are propagated to child configurations and
     * interpolation is setup correctly.
     *
     * @param params the parameters object
     */
    private void initChildBasicParameters(final BasicBuilderParameters params)
    {
        params.setPrefixLookups(fetchPrefixLookups());
        params.setParentInterpolator(parentInterpolator);
        if (currentParameters.isInheritSettings())
        {
            params.inheritFrom(getParameters());
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
            final FileBasedBuilderProperties<?> params)
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
    private void initChildXMLParameters(final XMLBuilderProperties<?> params)
    {
        params.setEntityResolver(currentXMLParameters.getEntityResolver());
    }

    /**
     * Initializes a parameters object for a combined configuration builder with
     * properties already set for this parent builder. This implementation deals
     * only with a subset of properties. Other properties are already handled by
     * the specialized builder provider.
     *
     * @param params the parameters object
     */
    private void initChildCombinedParameters(
            final CombinedBuilderParametersImpl params)
    {
        params.registerMissingProviders(currentParameters);
        params.setBasePath(getBasePath());
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
        final ConfigurationSourceData result = new ConfigurationSourceData();
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
    private void registerConfiguredProviders(final HierarchicalConfiguration<?> defConfig)
            throws ConfigurationException
    {
        final List<? extends HierarchicalConfiguration<?>> nodes =
                defConfig.configurationsAt(KEY_CONFIGURATION_PROVIDERS);
        for (final HierarchicalConfiguration<?> config : nodes)
        {
            final XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
            final String key = config.getString(KEY_PROVIDER_KEY);
            currentParameters.registerProvider(key,
                    (ConfigurationBuilderProvider) fetchBeanHelper().createBean(decl));
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
            final ConfigurationBuilder<? extends HierarchicalConfiguration<?>> defBuilder)
    {
        defBuilder.addEventListener(ConfigurationBuilderEvent.RESET,
                new EventListener<ConfigurationBuilderEvent>()
                {
            @Override
            public void onEvent(final ConfigurationBuilderEvent event)
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
     * Returns a map with the current prefix lookup objects. This map is
     * obtained from the {@code ConfigurationInterpolator} of the configuration
     * under construction.
     *
     * @return the map with current prefix lookups (may be <b>null</b>)
     */
    private Map<String, ? extends Lookup> fetchPrefixLookups()
    {
        final CombinedConfiguration cc = getConfigurationUnderConstruction();
        return (cc != null) ? cc.getInterpolator().getLookups() : null;
    }

    /**
     * Creates {@code ConfigurationDeclaration} objects for the specified
     * configurations.
     *
     * @param configs the list with configurations
     * @return a collection with corresponding declarations
     */
    private Collection<ConfigurationDeclaration> createDeclarations(
            final Collection<? extends HierarchicalConfiguration<?>> configs)
    {
        final Collection<ConfigurationDeclaration> declarations =
                new ArrayList<>(configs.size());
        for (final HierarchicalConfiguration<?> c : configs)
        {
            declarations.add(new ConfigurationDeclaration(this, c));
        }
        return declarations;
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
    private static void initNodeCombinerListNodes(final CombinedConfiguration cc,
            final HierarchicalConfiguration<?> defConfig, final String key)
    {
        final List<Object> listNodes = defConfig.getList(key);
        for (final Object listNode : listNodes)
        {
            cc.getNodeCombiner().addListNode((String) listNode);
        }
    }

    /**
     * Creates the map with the default configuration builder providers.
     *
     * @return the map with default providers
     */
    private static Map<String, ConfigurationBuilderProvider> createDefaultProviders()
    {
        final Map<String, ConfigurationBuilderProvider> providers =
                new HashMap<>();
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
        /** A list with data for all builders for override configurations. */
        private final List<ConfigurationDeclaration> overrideDeclarations;

        /** A list with data for all builders for union configurations. */
        private final List<ConfigurationDeclaration> unionDeclarations;

        /** A list with the builders for override configurations. */
        private final List<ConfigurationBuilder<? extends Configuration>> overrideBuilders;

        /** A list with the builders for union configurations. */
        private final List<ConfigurationBuilder<? extends Configuration>> unionBuilders;

        /** A map for direct access to a builder by its name. */
        private final Map<String, ConfigurationBuilder<? extends Configuration>> namedBuilders;

        /** A collection with all child builders. */
        private final Collection<ConfigurationBuilder<? extends Configuration>> allBuilders;

        /** A listener for reacting on changes of sub builders. */
        private final EventListener<ConfigurationBuilderEvent> changeListener;

        /**
         * Creates a new instance of {@code ConfigurationSourceData}.
         */
        public ConfigurationSourceData()
        {
            overrideDeclarations = new ArrayList<>();
            unionDeclarations = new ArrayList<>();
            overrideBuilders = new ArrayList<>();
            unionBuilders = new ArrayList<>();
            namedBuilders = new HashMap<>();
            allBuilders = new LinkedList<>();
            changeListener = createBuilderChangeListener();
        }

        /**
         * Initializes this object from the specified definition configuration.
         *
         * @param config the definition configuration
         * @throws ConfigurationException if an error occurs
         */
        public void initFromDefinitionConfiguration(
                final HierarchicalConfiguration<?> config) throws ConfigurationException
        {
            overrideDeclarations.addAll(createDeclarations(fetchTopLevelOverrideConfigs(config)));
            overrideDeclarations.addAll(createDeclarations(config.childConfigurationsAt(KEY_OVERRIDE)));
            unionDeclarations.addAll(createDeclarations(config.childConfigurationsAt(KEY_UNION)));
        }

        /**
         * Processes the declaration of configuration builder providers, creates
         * the corresponding builder if necessary, obtains configurations, and
         * adds them to the specified result configuration.
         *
         * @param ccResult the result configuration
         * @param srcDecl the collection with the declarations of configuration
         *        sources to process
         * @return a list with configuration builders
         * @throws ConfigurationException if an error occurs
         */
        public List<ConfigurationBuilder<? extends Configuration>> createAndAddConfigurations(
                final CombinedConfiguration ccResult,
                final List<ConfigurationDeclaration> srcDecl,
                final List<ConfigurationBuilder<? extends Configuration>> builders)
                throws ConfigurationException
        {
            final boolean createBuilders = builders.isEmpty();
            List<ConfigurationBuilder<? extends Configuration>> newBuilders;
            if (createBuilders)
            {
                newBuilders = new ArrayList<>(srcDecl.size());
            }
            else
            {
                newBuilders = builders;
            }

            for (int i = 0; i < srcDecl.size(); i++)
            {
                ConfigurationBuilder<? extends Configuration> b;
                if (createBuilders)
                {
                    b = createConfigurationBuilder(srcDecl.get(i));
                    newBuilders.add(b);
                }
                else
                {
                    b = builders.get(i);
                }
                addChildConfiguration(ccResult, srcDecl.get(i), b);
            }

            return newBuilders;
        }

        /**
         * Frees resources used by this object and performs clean up. This
         * method is called when the owning builder is reset.
         */
        public void cleanUp()
        {
            for (final ConfigurationBuilder<?> b : getChildBuilders())
            {
                b.removeEventListener(ConfigurationBuilderEvent.RESET,
                        changeListener);
            }
            namedBuilders.clear();
        }

        /**
         * Returns a collection containing the builders for all child
         * configuration sources.
         *
         * @return the child configuration builders
         */
        public Collection<ConfigurationBuilder<? extends Configuration>> getChildBuilders()
        {
            return allBuilders;
        }

        /**
         * Returns a collection with all configuration source declarations
         * defined in the override section.
         *
         * @return the override configuration builders
         */
        public List<ConfigurationDeclaration> getOverrideSources()
        {
            return overrideDeclarations;
        }

        /**
         * Returns a collection with all configuration source declarations
         * defined in the union section.
         *
         * @return the union configuration builders
         */
        public List<ConfigurationDeclaration> getUnionSources()
        {
            return unionDeclarations;
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
                final String name)
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
         * Creates a configuration builder based on a source declaration in the
         * definition configuration.
         *
         * @param decl the current {@code ConfigurationDeclaration}
         * @return the newly created builder
         * @throws ConfigurationException if an error occurs
         */
        private ConfigurationBuilder<? extends Configuration> createConfigurationBuilder(
                final ConfigurationDeclaration decl) throws ConfigurationException
        {
            final ConfigurationBuilderProvider provider =
                    providerForTag(decl.getConfiguration().getRootElementName());
            if (provider == null)
            {
                throw new ConfigurationException(
                        "Unsupported configuration source: "
                                + decl.getConfiguration().getRootElementName());
            }

            final ConfigurationBuilder<? extends Configuration> builder =
                    provider.getConfigurationBuilder(decl);
            if (decl.getName() != null)
            {
                namedBuilders.put(decl.getName(), builder);
            }
            allBuilders.add(builder);
            builder.addEventListener(ConfigurationBuilderEvent.RESET,
                    changeListener);
            return builder;
        }

        /**
         * Creates a new configuration using the specified builder and adds it
         * to the resulting combined configuration.
         *
         * @param ccResult the resulting combined configuration
         * @param decl the current {@code ConfigurationDeclaration}
         * @param builder the configuration builder
         * @throws ConfigurationException if an error occurs
         */
        private void addChildConfiguration(final CombinedConfiguration ccResult,
                final ConfigurationDeclaration decl,
                final ConfigurationBuilder<? extends Configuration> builder)
                throws ConfigurationException
        {
            try
            {
                ccResult.addConfiguration(
                        builder.getConfiguration(),
                        decl.getName(), decl.getAt());
            }
            catch (final ConfigurationException cex)
            {
                // ignore exceptions for optional configurations
                if (!decl.isOptional())
                {
                    throw cex;
                }
            }
        }

        /**
         * Creates a listener for builder change events. This listener is
         * registered at all builders for child configurations.
         */
        private EventListener<ConfigurationBuilderEvent> createBuilderChangeListener()
        {
            return new EventListener<ConfigurationBuilderEvent>()
            {
                @Override
                public void onEvent(final ConfigurationBuilderEvent event)
                {
                    resetResult();
                }
            };
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
        private List<? extends HierarchicalConfiguration<?>> fetchTopLevelOverrideConfigs(
                final HierarchicalConfiguration<?> config)
        {

            final List<? extends HierarchicalConfiguration<?>> configs =
                    config.childConfigurationsAt(null);
            for (final Iterator<? extends HierarchicalConfiguration<?>> it =
                    configs.iterator(); it.hasNext();)
            {
                final String nodeName = it.next().getRootElementName();
                for (final String element : CONFIG_SECTIONS)
                {
                    if (element.equals(nodeName))
                    {
                        it.remove();
                        break;
                    }
                }
            }
            return configs;
        }
    }
}
