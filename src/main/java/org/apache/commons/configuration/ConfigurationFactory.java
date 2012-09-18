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

package org.apache.commons.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.digester.Substitutor;
import org.apache.commons.digester.substitution.MultiVariableExpander;
import org.apache.commons.digester.substitution.VariableSubstitutor;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>
 * Factory class to create a CompositeConfiguration from a .xml file using
 * Digester.  By default it can handle the Configurations from commons-
 * configuration.  If you need to add your own, then you can pass in your own
 * digester rules to use.  It is also namespace aware, by providing a
 * digesterRuleNamespaceURI.
 * </p>
 * <p>
 * <em>Note:</em> Almost all of the features provided by this class and many
 * more are also available for the {@link DefaultConfigurationBuilder}
 * class. {@code DefaultConfigurationBuilder} also has a more robust
 * merge algorithm for constructing combined configurations. So it is
 * recommended to use this class instead of {@code ConfigurationFactory}.
 * </p>
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 * @deprecated Use {@link DefaultConfigurationBuilder} instead; this class
 * provides the same features as ConfigurationFactory plus some more; it can
 * also process the same configuration definition files.
 */
@Deprecated
public class ConfigurationFactory
{
    /** Constant for the root element in the info file.*/
    private static final String SEC_ROOT = "configuration/";

    /** Constant for the override section.*/
    private static final String SEC_OVERRIDE = SEC_ROOT + "override/";

    /** Constant for the additional section.*/
    private static final String SEC_ADDITIONAL = SEC_ROOT + "additional/";

    /** Constant for the optional attribute.*/
    private static final String ATTR_OPTIONAL = "optional";

    /** Constant for the fileName attribute.*/
    private static final String ATTR_FILENAME = "fileName";

    /** Constant for the load method.*/
    private static final String METH_LOAD = "load";

    /** Constant for the default base path (points to actual directory).*/
    private static final String DEF_BASE_PATH = ".";

    /** static logger */
    private static Log log = LogFactory.getLog(ConfigurationFactory.class);

    /** The XML file with the details about the configuration to load */
    private String configurationFileName;

    /** The URL to the XML file with the details about the configuration to load. */
    private URL configurationURL;

    /**
     * The implicit base path for included files. This path is determined by
     * the configuration to load and used unless no other base path was
     * explicitly specified.
     */
    private String implicitBasePath;

    /** The basePath to prefix file paths for file based property files. */
    private String basePath;

    /** URL for xml digester rules file */
    private URL digesterRules;

    /** The digester namespace to parse */
    private String digesterRuleNamespaceURI;

    /**
     * Constructor
     */
    public ConfigurationFactory()
    {
        setBasePath(DEF_BASE_PATH);
    }
    /**
     * Constructor with ConfigurationFile Name passed
     *
     * @param configurationFileName The path to the configuration file
     */
    public ConfigurationFactory(String configurationFileName)
    {
        setConfigurationFileName(configurationFileName);
    }

    /**
     * Return the configuration provided by this factory. It loads the
     * configuration file which is a XML description of the actual
     * configurations to load. It can contain various different types of
     * configuration, e.g. Properties, XML and JNDI.
     *
     * @return A Configuration object
     * @throws ConfigurationException A generic exception that we had trouble during the
     * loading of the configuration data.
     */
    public Configuration getConfiguration() throws ConfigurationException
    {
        Digester digester;
        InputStream input = null;
        ConfigurationBuilder builder = new ConfigurationBuilder();
        URL url = getConfigurationURL();
        try
        {
            if (url == null)
            {
                url = ConfigurationUtils.locate(implicitBasePath, getConfigurationFileName());
            }
            input = url.openStream();
        }
        catch (Exception e)
        {
            log.error("Exception caught opening stream to URL", e);
            throw new ConfigurationException("Exception caught opening stream to URL", e);
        }

        if (getDigesterRules() == null)
        {
            digester = new Digester();
            configureNamespace(digester);
            initDefaultDigesterRules(digester);
        }
        else
        {
            digester = DigesterLoader.createDigester(getDigesterRules());
            // This might already be too late. As far as I can see, the namespace
            // awareness must be configured before the digester rules are loaded.
            configureNamespace(digester);
        }

        // Configure digester to always enable the context class loader
        digester.setUseContextClassLoader(true);
        // Add a substitutor to resolve system properties
        enableDigesterSubstitutor(digester);
        // Put the composite builder object below all of the other objects.
        digester.push(builder);
        // Parse the input stream to configure our mappings
        try
        {
            digester.parse(input);
            input.close();
        }
        catch (SAXException saxe)
        {
            log.error("SAX Exception caught", saxe);
            throw new ConfigurationException("SAX Exception caught", saxe);
        }
        catch (IOException ioe)
        {
            log.error("IO Exception caught", ioe);
            throw new ConfigurationException("IO Exception caught", ioe);
        }
        return builder.getConfiguration();
    }

    /**
     * Returns the configurationFile.
     *
     * @return The name of the configuration file. Can be null.
     */
    public String getConfigurationFileName()
    {
        return configurationFileName;
    }

    /**
     * Sets the configurationFile.
     *
     * @param configurationFileName  The name of the configurationFile to use.
     */
    public void setConfigurationFileName(String configurationFileName)
    {
        File file = new File(configurationFileName).getAbsoluteFile();
        this.configurationFileName = file.getName();
        implicitBasePath = file.getParent();
    }

    /**
     * Returns the URL of the configuration file to be loaded.
     *
     * @return the URL of the configuration to load
     */
    public URL getConfigurationURL()
    {
        return configurationURL;
    }

    /**
     * Sets the URL of the configuration to load. This configuration can be
     * either specified by a file name or by a URL.
     *
     * @param url the URL of the configuration to load
     */
    public void setConfigurationURL(URL url)
    {
        configurationURL = url;
        implicitBasePath = url.toString();
    }

    /**
     * Returns the digesterRules.
     *
     * @return URL
     */
    public URL getDigesterRules()
    {
        return digesterRules;
    }

    /**
     * Sets the digesterRules.
     *
     * @param digesterRules The digesterRules to set
     */
    public void setDigesterRules(URL digesterRules)
    {
        this.digesterRules = digesterRules;
    }

    /**
     * Adds a substitutor to interpolate system properties
     *
     * @param digester The digester to which we add the substitutor
     */
    protected void enableDigesterSubstitutor(Digester digester)
    {
        // This is ugly, but it is safe because the Properties object returned
        // by System.getProperties() (which is actually a Map<Object, Object>)
        // contains only String keys.
        @SuppressWarnings("unchecked")
        Map<String, Object> systemProperties =
                (Map<String, Object>) (Object) System.getProperties();
        MultiVariableExpander expander = new MultiVariableExpander();
        expander.addSource("$", systemProperties);

        // allow expansion in both xml attributes and element text
        Substitutor substitutor = new VariableSubstitutor(expander);
        digester.setSubstitutor(substitutor);
    }

    /**
     * Initializes the parsing rules for the default digester
     *
     * This allows the Configuration Factory to understand the default types:
     * Properties, XML and JNDI. Two special sections are introduced:
     * <code>&lt;override&gt;</code> and <code>&lt;additional&gt;</code>.
     *
     * @param digester The digester to configure
     */
    protected void initDefaultDigesterRules(Digester digester)
    {
        initDigesterSectionRules(digester, SEC_ROOT, false);
        initDigesterSectionRules(digester, SEC_OVERRIDE, false);
        initDigesterSectionRules(digester, SEC_ADDITIONAL, true);
    }

    /**
     * Sets up digester rules for a specified section of the configuration
     * info file.
     *
     * @param digester the current digester instance
     * @param matchString specifies the section
     * @param additional a flag if rules for the additional section are to be
     * added
     */
    protected void initDigesterSectionRules(Digester digester, String matchString, boolean additional)
    {
        setupDigesterInstance(
            digester,
            matchString + "properties",
            new PropertiesConfigurationFactory(),
            METH_LOAD,
            additional);

        setupDigesterInstance(
            digester,
            matchString + "plist",
            new PropertyListConfigurationFactory(),
            METH_LOAD,
            additional);

        setupDigesterInstance(
            digester,
            matchString + "xml",
            new FileConfigurationFactory(XMLConfiguration.class),
            METH_LOAD,
            additional);

        setupDigesterInstance(
            digester,
            matchString + "hierarchicalXml",
            new FileConfigurationFactory(XMLConfiguration.class),
            METH_LOAD,
            additional);

        setupDigesterInstance(
            digester,
            matchString + "jndi",
            new JNDIConfigurationFactory(),
            null,
            additional);

        setupDigesterInstance(
            digester,
            matchString + "system",
            new SystemConfigurationFactory(),
            null,
            additional);
    }

    /**
     * Sets up digester rules for a configuration to be loaded.
     *
     * @param digester the current digester
     * @param matchString the pattern to match with this rule
     * @param factory an ObjectCreationFactory instance to use for creating new
     * objects
     * @param method the name of a method to be called or <b>null</b> for none
     * @param additional a flag if rules for the additional section are to be
     * added
     */
    protected void setupDigesterInstance(
            Digester digester,
            String matchString,
            ObjectCreationFactory factory,
            String method,
            boolean additional)
    {
        if (additional)
        {
            setupUnionRules(digester, matchString);
        }

        digester.addFactoryCreate(matchString, factory);
        digester.addSetProperties(matchString);

        if (method != null)
        {
            digester.addRule(matchString, new CallOptionalMethodRule(method));
        }

        digester.addSetNext(matchString, "addConfiguration", Configuration.class.getName());
    }

    /**
     * Sets up rules for configurations in the additional section.
     *
     * @param digester the current digester
     * @param matchString the pattern to match with this rule
     */
    protected void setupUnionRules(Digester digester, String matchString)
    {
        digester.addObjectCreate(matchString,
        AdditionalConfigurationData.class);
        digester.addSetProperties(matchString);
        digester.addSetNext(matchString, "addAdditionalConfig",
        AdditionalConfigurationData.class.getName());
    }

    /**
     * Returns the digesterRuleNamespaceURI.
     *
     * @return A String with the digesterRuleNamespaceURI.
     */
    public String getDigesterRuleNamespaceURI()
    {
        return digesterRuleNamespaceURI;
    }

    /**
     * Sets the digesterRuleNamespaceURI.
     *
     * @param digesterRuleNamespaceURI The new digesterRuleNamespaceURI to use
     */
    public void setDigesterRuleNamespaceURI(String digesterRuleNamespaceURI)
    {
        this.digesterRuleNamespaceURI = digesterRuleNamespaceURI;
    }

    /**
     * Configure the current digester to be namespace aware and to have
     * a Configuration object to which all of the other configurations
     * should be added
     *
     * @param digester The Digester to configure
     */
    private void configureNamespace(Digester digester)
    {
        if (getDigesterRuleNamespaceURI() != null)
        {
            digester.setNamespaceAware(true);
            digester.setRuleNamespaceURI(getDigesterRuleNamespaceURI());
        }
        else
        {
            digester.setNamespaceAware(false);
        }
        digester.setValidating(false);
    }

    /**
     * Returns the Base path from which this Configuration Factory operates.
     * This is never null. If you set the BasePath to null, then a base path
     * according to the configuration to load is returned.
     *
     * @return The base Path of this configuration factory.
     */
    public String getBasePath()
    {
        String path = StringUtils.isEmpty(basePath)
                || DEF_BASE_PATH.equals(basePath) ? implicitBasePath : basePath;
        return StringUtils.isEmpty(path) ? DEF_BASE_PATH : path;
    }

    /**
     * Sets the basePath for all file references from this Configuration Factory.
     * Normally a base path need not to be set because it is determined by
     * the location of the configuration file to load. All relative pathes in
     * this file are resolved relative to this file. Setting a base path makes
     * sense if such relative pathes should be otherwise resolved, e.g. if
     * the configuration file is loaded from the class path and all sub
     * configurations it refers to are stored in a special config directory.
     *
     * @param basePath The new basePath to set.
     */
    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    /**
     * A base class for digester factory classes. This base class maintains
     * a default class for the objects to be created.
     * There will be sub classes for specific configuration implementations.
     */
    public class DigesterConfigurationFactory extends AbstractObjectCreationFactory
    {
        /** Actual class to use. */
        private Class<?> clazz;

        /**
         * Creates a new instance of {@code DigesterConfigurationFactory}.
         *
         * @param clazz the class which we should instantiate
         */
        public DigesterConfigurationFactory(Class<?> clazz)
        {
            this.clazz = clazz;
        }

        /**
         * Creates an instance of the specified class.
         *
         * @param attribs the attributes (ignored)
         * @return the new object
         * @throws Exception if object creation fails
         */
        @Override
        public Object createObject(Attributes attribs) throws Exception
        {
            return clazz.newInstance();
        }
    }

    /**
     * A tiny inner class that allows the Configuration Factory to
     * let the digester construct FileConfiguration objects
     * that already have the correct base Path set.
     *
     */
    public class FileConfigurationFactory extends DigesterConfigurationFactory
    {
        /**
         * C'tor
         *
         * @param clazz The class which we should instantiate.
         */
        public FileConfigurationFactory(Class<?> clazz)
        {
            super(clazz);
        }

        /**
         * Gets called by the digester.
         *
         * @param attributes the actual attributes
         * @return the new object
         * @throws Exception Couldn't instantiate the requested object.
         */
        @Override
        public Object createObject(Attributes attributes) throws Exception
        {
            FileConfiguration conf = createConfiguration(attributes);
            conf.setBasePath(getBasePath());
            return conf;
        }

        /**
         * Creates the object, a {@code FileConfiguration}.
         *
         * @param attributes the actual attributes
         * @return the file configuration
         * @throws Exception if the object could not be created
         */
        protected FileConfiguration createConfiguration(Attributes attributes) throws Exception
        {
            return (FileConfiguration) super.createObject(attributes);
        }
    }

    /**
     * A factory that returns an XMLPropertiesConfiguration for .xml files
     * and a PropertiesConfiguration for the others.
     *
     * @since 1.2
     */
    public class PropertiesConfigurationFactory extends FileConfigurationFactory
    {
        /**
         * Creates a new instance of {@code PropertiesConfigurationFactory}.
         */
        public PropertiesConfigurationFactory()
        {
            super(null);
        }

        /**
         * Creates the new configuration object. Based on the file name
         * provided in the attributes either a {@code PropertiesConfiguration}
         * or a {@code XMLPropertiesConfiguration} object will be
         * returned.
         *
         * @param attributes the attributes
         * @return the new configuration object
         * @throws Exception if an error occurs
         */
        @Override
        protected FileConfiguration createConfiguration(Attributes attributes) throws Exception
        {
            String filename = attributes.getValue(ATTR_FILENAME);

            if (filename != null && filename.toLowerCase().trim().endsWith(".xml"))
            {
                return new XMLPropertiesConfiguration();
            }
            else
            {
                return new PropertiesConfiguration();
            }
        }
    }

    /**
     * A factory that returns an XMLPropertyListConfiguration for .xml files
     * and a PropertyListConfiguration for the others.
     *
     * @since 1.2
     */
    public class PropertyListConfigurationFactory extends FileConfigurationFactory
    {
        /**
         * Creates a new instance of PropertyListConfigurationFactory</code>.
         */
        public PropertyListConfigurationFactory()
        {
            super(null);
        }

        /**
         * Creates the new configuration object. Based on the file name
         * provided in the attributes either a {@code XMLPropertyListConfiguration}
         * or a {@code PropertyListConfiguration} object will be
         * returned.
         *
         * @param attributes the attributes
         * @return the new configuration object
         * @throws Exception if an error occurs
         */
        @Override
        protected FileConfiguration createConfiguration(Attributes attributes) throws Exception
        {
            String filename = attributes.getValue(ATTR_FILENAME);

            if (filename != null && filename.toLowerCase().trim().endsWith(".xml"))
            {
                return new XMLPropertyListConfiguration();
            }
            else
            {
                return new PropertyListConfiguration();
            }
        }
    }

    /**
     * A tiny inner class that allows the Configuration Factory to
     * let the digester construct JNDIConfiguration objects.
     */
    private class JNDIConfigurationFactory extends DigesterConfigurationFactory
    {
        /**
         * Creates a new instance of {@code JNDIConfigurationFactory}.
         */
        public JNDIConfigurationFactory()
        {
            super(JNDIConfiguration.class);
        }
    }

    /**
     * A tiny inner class that allows the Configuration Factory to
     * let the digester construct SystemConfiguration objects.
     */
    private class SystemConfigurationFactory extends DigesterConfigurationFactory
    {
        /**
         * Creates a new instance of {@code SystemConfigurationFactory}.
         */
        public SystemConfigurationFactory()
        {
            super(SystemConfiguration.class);
        }
    }

    /**
     * A simple data class that holds all information about a configuration
     * from the <code>&lt;additional&gt;</code> section.
     */
    public static class AdditionalConfigurationData
    {
        /** Stores the configuration object.*/
        private Configuration configuration;

        /** Stores the location of this configuration in the global tree.*/
        private String at;

        /**
         * Returns the value of the {@code at} attribute.
         *
         * @return the at attribute
         */
        public String getAt()
        {
            return at;
        }

        /**
         * Sets the value of the {@code at} attribute.
         *
         * @param string the attribute value
         */
        public void setAt(String string)
        {
            at = string;
        }

        /**
         * Returns the configuration object.
         *
         * @return the configuration
         */
        public Configuration getConfiguration()
        {
            return configuration;
        }

        /**
         * Sets the configuration object. Note: Normally this method should be
         * named {@code setConfiguration()}, but the name
         * {@code addConfiguration()} is required by some of the digester
         * rules.
         *
         * @param config the configuration to set
         */
        public void addConfiguration(Configuration config)
        {
            configuration = config;
        }
    }

    /**
     * An internally used helper class for constructing the composite
     * configuration object.
     */
    public static class ConfigurationBuilder
    {
        /** Stores the composite configuration.*/
        private CompositeConfiguration config;

        /** Stores a collection with the configs from the additional section.*/
        private Collection<AdditionalConfigurationData> additionalConfigs;

        /**
         * Creates a new instance of {@code ConfigurationBuilder}.
         */
        public ConfigurationBuilder()
        {
            config = new CompositeConfiguration();
            additionalConfigs = new LinkedList<AdditionalConfigurationData>();
        }

        /**
         * Adds a new configuration to this object. This method is called by
         * Digester.
         *
         * @param conf the configuration to be added
         */
        public void addConfiguration(Configuration conf)
        {
            config.addConfiguration(conf);
        }

        /**
         * Adds information about an additional configuration. This method is
         * called by Digester.
         *
         * @param data the data about the additional configuration
         */
        public void addAdditionalConfig(AdditionalConfigurationData data)
        {
            additionalConfigs.add(data);
        }

        /**
         * Returns the final composite configuration.
         *
         * @return the final configuration object
         */
        public CompositeConfiguration getConfiguration()
        {
            if (!additionalConfigs.isEmpty())
            {
                Configuration unionConfig = createAdditionalConfiguration(additionalConfigs);
                if (unionConfig != null)
                {
                    addConfiguration(unionConfig);
                }
                additionalConfigs.clear();
            }

            return config;
        }

        /**
         * Creates a configuration object with the union of all properties
         * defined in the <code>&lt;additional&gt;</code> section. This
         * implementation returns a {@code HierarchicalConfiguration}
         * object.
         *
         * @param configs a collection with
         * {@code AdditionalConfigurationData} objects
         * @return the union configuration (can be <b>null</b>)
         */
        protected Configuration createAdditionalConfiguration(Collection<AdditionalConfigurationData> configs)
        {
            HierarchicalConfiguration result = new HierarchicalConfiguration();

            for (AdditionalConfigurationData cdata : configs)
            {
                result.addNodes(cdata.getAt(),
                createRootNode(cdata).getChildren());
            }

            return result.isEmpty() ? null : result;
        }

        /**
         * Creates a configuration root node for the specified configuration.
         *
         * @param cdata the configuration data object
         * @return a root node for this configuration
         */
        private ConfigurationNode createRootNode(AdditionalConfigurationData cdata)
        {
            if (cdata.getConfiguration() instanceof HierarchicalConfiguration)
            {
                // we can directly use this configuration's root node
                return ((HierarchicalConfiguration) cdata.getConfiguration()).getRootNode();
            }
            else
            {
                // transform configuration to a hierarchical root node
                HierarchicalConfiguration hc = new HierarchicalConfiguration();
                ConfigurationUtils.copy(cdata.getConfiguration(), hc);
                return hc.getRootNode();
            }
        }
    }

    /**
     * A special implementation of Digester's {@code CallMethodRule} that
     * is internally used for calling a file configuration's {@code load()}
     * method. This class differs from its ancestor that it catches all occurring
     * exceptions when the specified method is called. It then checks whether
     * for the corresponding configuration the optional attribute is set. If
     * this is the case, the exception will simply be ignored.
     *
     * @since 1.4
     */
    private static class CallOptionalMethodRule extends CallMethodRule
    {
        /** A flag whether the optional attribute is set for this node. */
        private boolean optional;

        /**
         * Creates a new instance of {@code CallOptionalMethodRule} and
         * sets the name of the method to invoke.
         *
         * @param methodName the name of the method
         */
        public CallOptionalMethodRule(String methodName)
        {
            super(methodName);
        }

        /**
         * Checks if the optional attribute is set.
         *
         * @param attrs the attributes
         * @throws Exception if an error occurs
         */
        @Override
        public void begin(Attributes attrs) throws Exception
        {
            optional = attrs.getValue(ATTR_OPTIONAL) != null
                    && PropertyConverter.toBoolean(
                            attrs.getValue(ATTR_OPTIONAL)).booleanValue();
            super.begin(attrs);
        }

        /**
         * Calls the method. If the optional attribute was set, occurring
         * exceptions will be ignored.
         *
         * @throws Exception if an error occurs
         */
        @Override
        public void end() throws Exception
        {
            try
            {
                super.end();
            }
            catch (Exception ex)
            {
                if (optional)
                {
                    log.warn("Could not create optional configuration!", ex);
                }
                else
                {
                    throw ex;
                }
            }
        }
    }
}
