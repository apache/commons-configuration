package org.apache.commons.configuration;

/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.Writer;
import java.util.NoSuchElementException;

import org.apache.commons.digester.Digester;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * <p>A helper class that supports XML-like processing for configuration
 * objects.</p>
 * <p>This class provides a set of methods that all have something to do with
 * treating a <code>Configuration</code> object as a XML document. So a
 * configuration can be transformed into a <code>Document</code> (either
 * dom4j or w3c), saved as an XML file or passed to Digester.</p>
 * <p><strong>Implementation note:</strong> This class is not thread safe.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: ConfigurationXMLDocument.java,v 1.4 2004/03/08 23:42:16 epugh Exp $
 */
public class ConfigurationXMLDocument
{
    /** Constant for the class element.*/
    protected static final String ELEM_CLASS = "config/class";

    /** Constant for the property element.*/
    protected static final String ELEM_PROPERTY = "config/class/property";

    /** Constant for the name attribute.*/
    protected static final String ATTR_NAME = "name";

    /** Constant for the value attribute.*/
    protected static final String ATTR_VALUE = "value";

    /** Stores the configuration object this object operates on.*/
    private Configuration configuration;

    /**
     * Creates a new instance of <code>ConfigurationXMLDocument</code>
     * and sets the configuration object to be processed.
     * @param config the configuration object
     */
    public ConfigurationXMLDocument(Configuration config)
    {
        setConfiguration(config);
    }

    /**
     * Returns the <code>Configuration</code> object for this document.
     * @return the <code>Configuration</code> object
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the <code>Configuration</code> object this document operates on.
     * @param configuration the <code>Configuration</code> object
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Returns a <code>XMLReader</code> object for the specified configuration
     * object. This reader can then be used to perform XML-like processing on
     * the configuration.
     * @param config the configuration object
     * @return a XMLReader for this configuration
     */
    public static ConfigurationXMLReader createXMLReader(Configuration config)
    {
        if (config instanceof HierarchicalConfiguration)
        {
            return new HierarchicalConfigurationXMLReader(
                (HierarchicalConfiguration) config);
        } /* if */
        else
        {
            return new BaseConfigurationXMLReader(config);
        } /* else */
    }

    /**
     * Returns a <code>XMLReader</code> object for the actual configuration
     * object.
     * @return a XMLReader for the actual configuration
     */
    public ConfigurationXMLReader createXMLReader()
    {
        return createXMLReader((String) null);
    }

    /**
     * Returns a <code>ConfigurationXMLReader</code> object for the subset
     * configuration specified by the given prefix. If no properties are found
     * under this prefix, a <code>NoSuchElementException</code>
     * exception will be thrown.
     * @param prefix the prefix of the configuration keys that belong to the
     * subset; can be <b>null</b>, then the whole configuration is affected
     * @return a XMLReader for the specified subset configuration
     */
    public ConfigurationXMLReader createXMLReader(String prefix)
    {
        return createXMLReader(configForKey(prefix));
    }

    /**
     * Transforms the wrapped configuration into a dom4j document.
     * @param prefix a prefix for the keys to process; can be <b>null</b>,
     * then all keys in the configuration will be added to the document
     * @param rootName the name of the root element in the document; can be
     * <b>null</b>, then a default name will be used
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public Document getDocument(String prefix, String rootName)
        throws DocumentException
    {
        ConfigurationXMLReader xmlReader = createXMLReader(prefix);
        if (rootName != null)
        {
            xmlReader.setRootName(rootName);
        } /* if */

        SAXReader reader = new SAXReader(xmlReader);
        return reader.read(getClass().getName());
    }

    /**
     * Transforms the wrapped configuration into a dom4j document. The root
     * element will be given a default name.
     * @param prefix a prefix for the keys to process; can be <b>null</b>,
     * then all keys in the configuration will be added to the document
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public Document getDocument(String prefix) throws DocumentException
    {
        return getDocument(prefix, null);
    }

    /**
     * Transforms the wrapped configuration into a dom4j document. The root
     * element will be given a default name.
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public Document getDocument() throws DocumentException
    {
        return getDocument(null, null);
    }

    /**
     * Transforms the wrapped configuration into a w3c document.
     * @param prefix a prefix for the keys to process; can be <b>null</b>,
     * then all keys in the configuration will be added to the document
     * @param rootName the name of the root element in the document; can be
     * <b>null</b>, then a default name will be used
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public org.w3c.dom.Document getW3cDocument(String prefix, String rootName)
        throws DocumentException
    {
        return toW3cDocument(getDocument(prefix, rootName));
    }

    /**
     * Transforms the wrapped configuration into a w3c document. The root
     * element will be given a default name.
     * @param prefix a prefix for the keys to process; can be <b>null</b>,
     * then all keys in the configuration will be added to the document
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public org.w3c.dom.Document getW3cDocument(String prefix)
        throws DocumentException
    {
        return getW3cDocument(prefix, null);
    }

    /**
     * Transforms the wrapped configuration into a w3c document. The root
     * element will be given a default name.
     * @return the document
     * @throws DocumentException if an error occurs
     */
    public org.w3c.dom.Document getW3cDocument() throws DocumentException
    {
        return getW3cDocument(null, null);
    }

    /**
     * Converts a dom4j document into a w3c document.
     * @param doc the dom4j document
     * @return the w3c document
     * @throws DocumentException if an error occurs
     */
    static org.w3c.dom.Document toW3cDocument(Document doc)
        throws DocumentException
    {
        return new DOMWriter().write(doc);
    }

    /**
     * Helper method for constructing a subset if necessary. Depending on
     * the passed in key this method either returns the wrapped configuration
     * or the specified subset of it.
     * @param key the key
     * @return the configuration for that key
     */
    private Configuration configForKey(String key)
    {
        Configuration conf = (key == null)
            ? getConfiguration()
            : getConfiguration().subset(key);
            
        // undefined?
        if(conf == null || (conf instanceof CompositeConfiguration
        && ((CompositeConfiguration) conf).getNumberOfConfigurations() < 2))
        {
            throw new NoSuchElementException("No subset with key " + key);
        }  /* if */
        
        return conf;
    }

    /**
     * <p>Creates and initializes an object specified in the configuration
     * using Digester.</p>
     * <p>This method first constructs a subset configuration with the keys
     * starting with the given prefix. It then transforms this subset into a
     * XML document and let that be processed by Digester. The result of this
     * processing is returned.</p>
     * <p>The method is intended to be used for creating simple objects that
     * are specified somewhere in the configuration in a standard way. The
     * following fragment shows how a configuration file must look like to be
     * understood by the default Digester rule set used by this method:</p>
     * <p><pre>
     * ...
     *   &lt;class name="mypackage.MyClass"/&gt;
     *   &lt;args&gt;
     *     &lt;property name="myFirstProperty" value="myFirstValue"/&gt;
     *     &lt;property name="MySecondProperty" value="mySecondValue"/&gt;
     *     ...
     *   &lt;/args&gt;
     * ...
     * </pre></p>
     * @param prefix the prefix of the keys that are passed to Digester; can
     * be <b>null</b>, then the whole configuration will be processed
     * @return the result of the Digester processing
     * @throws IOException if an IOException occurs
     * @throws SAXException if a SAXException occurs
     */
    public Object callDigester(String prefix) throws IOException, SAXException
    {
        Digester digester = getDefaultDigester(prefix);
        return digester.parse(getClass().getName());
    }

    /**
     * Returns a default Digester instance. This instance is used for the
     * simple object creation feature.
     * @param prefix the prefix of the keys to be processed; can be
     * <b>null</b>, then the whole configuration is meant
     * @return the default Digester instance
     */
    protected Digester getDefaultDigester(String prefix)
    {
        Digester digester = createDefaultDigester(prefix);
        setupDefaultDigester(digester);

        return digester;
    }

    /**
     * Creates the default Digester instance for the given prefix. This method
     * is called by <code>getDefaultDigester()</code>.
     * @param prefix the prefix of the keys to be processed; can be
     * <b>null</b>, then the whole configuration is meant
     * @return the default Digester instance
     */
    protected Digester createDefaultDigester(String prefix)
    {
        return new Digester(createXMLReader(prefix));
    }

    /**
     * Initializes the default digester instance used for simple object
     * creation. Here all needed properties and rules can be set. This base
     * implementation sets default rules for object creation as explained in
     * the comment for the <code>callDigester()</code> methods.
     * @param digester the digester instance to be initialized
     */
    protected void setupDefaultDigester(Digester digester)
    {
        digester.addObjectCreate(ELEM_CLASS, ATTR_NAME, Object.class);
        digester.addSetProperty(ELEM_PROPERTY, ATTR_NAME, ATTR_VALUE);
    }

    /**
     * Writes a configuration (or parts of it) to the given writer.
     * @param out the output writer
     * @param prefix the prefix of the subset to write; if <b>null</b>, the
     * whole configuration is written
     * @param root the name of the root element of the resulting document;
     * <b>null</b> for a default name
     * @param pretty flag for the pretty print mode
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out, String prefix, String root, boolean pretty)
        throws IOException, DocumentException
    {
        OutputFormat format =
            (pretty)
                ? OutputFormat.createPrettyPrint()
                : OutputFormat.createCompactFormat();

        XMLWriter writer = new XMLWriter(out, format);
        writer.write(getDocument(prefix, root));
    }

    /**
     * Writes a configuration (or parts of it) to the given writer. 
     * This overloaded version always uses pretty print mode.
     * @param out the output writer
     * @param prefix the prefix of the subset to write; if <b>null</b>, the
     * whole configuration is written
     * @param root the name of the root element of the resulting document;
     * <b>null</b> for a default name
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out, String prefix, String root)
        throws IOException, DocumentException
    {
        write(out, prefix, root, true);
    }

    /**
     * Writes a configuration (or parts of it) to the given writer.
     * The resulting document's root element will be given a default name.
     * @param out the output writer
     * @param prefix the prefix of the subset to write; if <b>null</b>, the
     * whole configuration is written
     * @param pretty flag for the pretty print mode
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out, String prefix, boolean pretty)
        throws IOException, DocumentException
    {
        write(out, prefix, null, pretty);
    }

    /**
     * Writes a configuration (or parts of it) to the given writer.
     * The resulting document's root element will be given a default name.
     * This overloaded version always uses pretty print mode.
     * @param out the output writer
     * @param prefix the prefix of the subset to write; if <b>null</b>, the
     * whole configuration is written
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out, String prefix)
        throws IOException, DocumentException
    {
        write(out, prefix, true);
    }

    /**
     * Writes the wrapped configuration to the given writer.
     * The resulting document's root element will be given a default name.
     * @param out the output writer
     * @param pretty flag for the pretty print mode
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out, boolean pretty)
        throws IOException, DocumentException
    {
        write(out, null, null, pretty);
    }

    /**
     * Writes the wrapped configuration to the given writer.
     * The resulting document's root element will be given a default name.
     * This overloaded version always uses pretty print mode.
     * @param out the output writer
     * @throws IOException if an IO error occurs
     * @throws DocumentException if there is an error during processing
     */
    public void write(Writer out) throws IOException, DocumentException
    {
        write(out, true);
    }
}
