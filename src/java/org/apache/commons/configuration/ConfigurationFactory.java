package org.apache.commons.configuration;
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
/**
 * Factory class to create a CompositeConfiguration from a .xml file using
 * Digester.  By default it can handle the Configurations from commons-
 * configuration.  If you need to add your own, then you can pass in your own
 * digester rules to use.  It is also namespace aware, by providing a
 * digesterRuleNamespaceURI.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: ConfigurationFactory.java,v 1.3 2004/01/16 14:56:45 epugh Exp $
 */
public class ConfigurationFactory implements BasePathLoader
{
    /** Constant for the root element in the info file.*/
    private static final String SEC_ROOT = "configuration/";

    /** Constant for the override section.*/
    private static final String SEC_OVERRIDE = SEC_ROOT + "override/";

    /** Constant for the additional section.*/
    private static final String SEC_ADDITIONAL = SEC_ROOT + "additional/";

    /** Constant for the name of the load method.*/
    private static final String METH_LOAD = "load";

    /** Constant for the default base path (points to actual directory).*/
    private static final String DEF_BASE_PATH = ".";

    /** The XML file with the details about the configuration to load */
    private String configurationFileName;
    /** The URL to the XML file with the details about the configuration to
     * load.
     */
    private URL configurationURL;
    /**
     * The implicit base path for included files. This path is determined by
     * the configuration to load and used unless no other base path was
     * explicitely specified.
     */
    private String impliciteBasePath;
    /** The basePath to prefix file paths for file based property files. */
    private String basePath;
    /** static logger */
    private static Log log = LogFactory.getLog(ConfigurationFactory.class);
    /** URL for xml digester rules file */
    private URL digesterRules;
    /** The digester namespace to parse */
    private String digesterRuleNamespaceURI;
    /**
     * C'tor
     */
    public ConfigurationFactory()
    {
        setBasePath(DEF_BASE_PATH);
    }
    /**
     * C'tor with ConfigurationFile Name passed
     *
     * @param configurationFileName The path to the configuration file
     */
    public ConfigurationFactory(String configurationFileName)
    {        
        this.configurationFileName = configurationFileName;
    }
    /**
     * Return the configuration provided by this factory. It
     * loads the configuration file which is a XML description of
     * the actual configurations to load. It can contain various
     * different types of configuration, currently Properties, XML and JNDI.
     *
     * @return A Configuration object
     * @throws Exception A generic exception that we had trouble during the
     * loading of the configuration data.
     */
    public Configuration getConfiguration() throws Exception
    {
        Digester digester;
        ConfigurationBuilder builder = new ConfigurationBuilder();
        URL url = getConfigurationURL();
        if(url == null)
        {
            url = ConfigurationUtils.getURL(impliciteBasePath,
            getConfigurationFileName());
        }  /* if */
        InputStream input = url.openStream();

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
        // Put the composite builder object below all of the other objects.
        digester.push(builder);
        // Parse the input stream to configure our mappings
        try
        {
            digester.parse(input);
            input.close();
        }
        catch (SAXException e)
        {
            log.error("SAX Exception caught", e);
            throw e;
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
     * @param configurationFileName  The name of the configurationFile to use.
     */
    public void setConfigurationFileName(String configurationFileName)
    {
        File file = new File(configurationFileName).getAbsoluteFile();
        this.configurationFileName = file.getName();
        impliciteBasePath = file.getParent();
    }

    /**
     * Returns the URL of the configuration file to be loaded.
     * @return the URL of the configuration to load
     */
    public URL getConfigurationURL()
    {
        return configurationURL;
    }

    /**
     * Sets the URL of the configuration to load. This configuration can be
     * either specified by a file name or by a URL.
     * @param url the URL of the configuration to load
     */
    public void setConfigurationURL(URL url)
    {
        configurationURL = url;
        impliciteBasePath = url.toString();

        // The following is a hack caused by the need to keep backwards
        // compatibility: Per default the base path is set to the current
        // directory. For loading from a URL this makes no sense. So
        // unless no specific base path was set we clear it.
        if(DEF_BASE_PATH.equals(getBasePath()))
        {
            setBasePath(null);
        }  /* if */
    }

    /**
     * Returns the digesterRules.
     * @return URL
     */
    public URL getDigesterRules()
    {
        return digesterRules;
    }
    /**
     * Sets the digesterRules.
     * @param digesterRules The digesterRules to set
     */
    public void setDigesterRules(URL digesterRules)
    {
        this.digesterRules = digesterRules;
    }
    /**
     * Initializes the parsing rules for the default digester
     *
     * This allows the Configuration Factory to understand the
     * default types: Properties, XML and JNDI. Two special sections are
     * introduced: <code>&lt;override&gt;</code> and
     * <code>&lt;additional&gt;</code>.
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
     * @param digester the current digester instance
     * @param matchString specifies the section
     * @param additional a flag if rules for the additional section are to be
     * added
     */
    protected void initDigesterSectionRules(Digester digester,
    String matchString, boolean additional)
    {
        setupDigesterInstance(
            digester,
            matchString + "properties",
            new BasePathConfigurationFactory(PropertiesConfiguration.class),
            METH_LOAD,
            additional);
        setupDigesterInstance(
            digester,
            matchString + "dom4j",
            new BasePathConfigurationFactory(DOM4JConfiguration.class),
            METH_LOAD,
            additional);
        setupDigesterInstance(
            digester,
            matchString + "jndi",
            new JNDIConfigurationFactory(),
            null,
            additional);
    }

    /**
     * Sets up digester rules for a configuration to be loaded.
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
        if(additional)
        {
            setupUnionRules(digester, matchString);
        }  /* if */
        digester.addFactoryCreate(matchString, factory);
        digester.addSetProperties(matchString);
        if(method != null)
        {
            digester.addCallMethod(matchString, method);
        }  /* if */
        digester.addSetNext(
            matchString,
            "addConfiguration",
            Configuration.class.getName());
    }

    /**
     * Sets up rules for configurations in the additional section.
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
        String path = StringUtils.isEmpty(basePath) ? 
        impliciteBasePath : basePath;
        return StringUtils.isEmpty(path) ? "." : path;
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
     * a default class for the objects to be created. It also supports a
     * <code>className</code> attribute for specifying a different class.
     * There will be sub classes for specific configuration implementations.
     */
    public class DigesterConfigurationFactory
        extends AbstractObjectCreationFactory
        implements ObjectCreationFactory
    {
        /** Constant for the className attribute.*/
        protected static final String ATTR_CLASSNAME = "className";

        /** Actual class to use. */
        private Class clazz;

        /**
         * Creates a new instance of <code>DigesterConfigurationFactory</code>.
         * @param clazz the class which we should instantiate
         */
        public DigesterConfigurationFactory(Class clazz)
        {
            this.clazz = clazz;
        }

        /**
         * Creates an instance of the specified class. If the passed in
         * attributes contain a <code>className</code> attribute, the value of
         * this attribute is interpreted as the full qualified class name of
         * the class to be instantiated. Otherwise the default class is used.
         * @param attribs the attributes
         * @return the new object
         * @exception Exception if object creation fails
         */
        public Object createObject(Attributes attribs) throws Exception
        {
            Class actCls;
            
            int idx = attribs.getIndex(ATTR_CLASSNAME);
            actCls = (idx < 0) ? clazz :
            Class.forName(attribs.getValue(idx));

            return actCls.newInstance();
        }
    }

    /**
     * A tiny inner class that allows the Configuration Factory to
     * let the digester construct BasePathConfiguration objects
     * that already have the correct base Path set.
     *
     */
    public class BasePathConfigurationFactory
        extends DigesterConfigurationFactory
    {
        /**
         * C'tor
         *
         * @param clazz The class which we should instantiate.
         */
        public BasePathConfigurationFactory(Class clazz)
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
        public Object createObject(Attributes attributes) throws Exception
        {
            BasePathLoader bpl =
                (BasePathLoader) super.createObject(attributes);
            bpl.setBasePath(getBasePath());
            return bpl;
        }
    }

    /**
     * A tiny inner class that allows the Configuration Factory to
     * let the digester construct JNDIPathConfiguration objects.
     *
     */
    public class JNDIConfigurationFactory
        extends DigesterConfigurationFactory
    {
        /**
         * C'tor
         */
        public JNDIConfigurationFactory()
        {
            super(JNDIConfiguration.class);
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
         * Returns the value of the <code>at</code> attribute.
         * @return the at attribute
         */
        public String getAt()
        {
            return at;
        }

        /**
         * Sets the value of the <code>at</code> attribute.
         * @param string the attribute value
         */
        public void setAt(String string)
        {
            at = string;
        }

        /**
         * Returns the configuration object.
         * @return the configuration
         */
        public Configuration getConfiguration()
        {
            return configuration;
        }

        /**
         * Sets the configuration object. Note: Normally this method should be
         * named <code>setConfiguration()</code>, but the name
         * <code>addConfiguration()</code> is required by some of the digester
         * rules.
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
        private Collection additionalConfigs;

        /**
         * Creates a new instance of <code>ConfigurationBuilder</code>.
         */
        public ConfigurationBuilder()
        {
            config = new CompositeConfiguration();
            additionalConfigs = new LinkedList();
        }

        /**
         * Adds a new configuration to this object. This method is called by
         * Digester.
         * @param conf the configuration to be added
         */
        public void addConfiguration(Configuration conf)
        {
            config.addConfiguration(conf);
        }

        /**
         * Adds information about an additional configuration. This method is
         * called by Digester.
         * @param data the data about the additional configuration
         */
        public void addAdditionalConfig(AdditionalConfigurationData data)
        {
            additionalConfigs.add(data);
        }

        /**
         * Returns the final composite configuration.
         * @return the final configuration object
         */
        public CompositeConfiguration getConfiguration()
        {
            if(!additionalConfigs.isEmpty())
            {
                Configuration unionConfig =
                createAdditionalConfiguration(additionalConfigs);
                if(unionConfig != null)
                {
                    addConfiguration(unionConfig);
                }  /* if */
                additionalConfigs.clear();
            }  /* if */

            return config;
        }

        /**
         * Creates a configuration object with the union of all properties
         * defined in the <code>&lt;additional&gt;</code> section. This
         * implementation returns a <code>HierarchicalConfiguration</code>
         * object.
         * @param configs a collection with
         * <code>AdditionalConfigurationData</code> objects
         * @return the union configuration (can be <b>null</b>)
         */
        protected Configuration createAdditionalConfiguration(
        Collection configs)
        {
            HierarchicalConfiguration result = new HierarchicalConfiguration();

            for(Iterator it = configs.iterator(); it.hasNext();)
            {
                AdditionalConfigurationData cdata =
                (AdditionalConfigurationData) it.next();
                result.addNodes(cdata.getAt(),
                createRootNode(cdata).getChildren().asList());
            }  /* for */

            return (result.isEmpty()) ? null : result;
        }

        /**
         * Creates a configuration root node for the specified configuration.
         * @param cdata the configuration data object
         * @return a root node for this configuration
         */
        private HierarchicalConfiguration.Node createRootNode(
        AdditionalConfigurationData cdata)
        {
            if(cdata.getConfiguration() instanceof HierarchicalConfiguration)
            {
                // we can directly use this configuration's root node
                return ((HierarchicalConfiguration) cdata.getConfiguration())
                .getRoot();
            }  /* if */

            else
            {
                // transform configuration to a hierarchical root node
                HierarchicalConfigurationNodeConverter conv =
                new HierarchicalConfigurationNodeConverter();
                conv.process(cdata.getConfiguration());
                return conv.getRootNode();
            }  /* else */
        }
    }

    /**
     * A specialized <code>HierarchicalConfigurationConverter</code> class
     * that creates a <code>HierarchicalConfiguration</code> root node from
     * an arbitrary <code>Configuration</code> object. This class is used to
     * add additional configuration objects to the hierarchical configuration
     * managed by the <code>ConfigurationBuilder</code>.
     */
    static class HierarchicalConfigurationNodeConverter
    extends HierarchicalConfigurationConverter
    {
        /** A stack for constructing the hierarchy.*/
        private Stack nodes;

        /** Stores the root node.*/
        private HierarchicalConfiguration.Node root;

        /**
         * Default constructor.
         */
        public HierarchicalConfigurationNodeConverter()
        {
            nodes = new Stack();
            root = new HierarchicalConfiguration.Node();
            nodes.push(root);
        }

        /**
         * Callback for an element start event. Creates a new node and adds
         * it to the actual parent.
         * @param name the name of the new node
         * @param value the node's value
         */
        protected void elementStart(String name, Object value)
        {
            HierarchicalConfiguration.Node parent =
            (HierarchicalConfiguration.Node) nodes.peek();
            HierarchicalConfiguration.Node child =
            new HierarchicalConfiguration.Node(name);
            if(value != null)
            {
                child.setValue(value);
            }  /* if */
            parent.addChild(child);
            nodes.push(child);
        }

        /**
         * Callback for an element end event. Clears the stack.
         * @param name the name of the element
         */
        protected void elementEnd(String name)
        {
            nodes.pop();
        }

        /**
         * Returns the constructed root node.
         * @return the root node
         */
        public HierarchicalConfiguration.Node getRootNode()
        {
            return root;
        }
    }
}
