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

package org.apache.commons.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Reads a XML configuration file.
 *
 * To retrieve the value of an attribute of an element, use
 * <code>X.Y.Z[@attribute]</code>.  The '@' symbol was chosen for
 * consistency with XPath.
 *
 * Setting property values will <b>NOT</b> automatically persist
 * changes to disk, unless <code>autoSave=true</code>.
 *
 * @since commons-configuration 1.0
 *
 * @author Jörg Schaible
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan</a>
 * @author <a href="mailto:dlr@apache.org">Daniel Rall</a>
 * @author Emmanuel Bourg
 * @version $Revision: 1.8 $, $Date: 2004/08/12 15:45:21 $
 */
public class XMLConfiguration extends BasePathConfiguration
{
    // For conformance with xpath
    private static final String ATTRIBUTE_START = "[@";
    private static final String ATTRIBUTE_END = "]";

    /**
     * For consistency with properties files.  Access nodes via an
     * "A.B.C" notation.
     */
    private static final String NODE_DELIMITER = ".";

    /**
     * A handle to our data source.
     */
    private String fileName;

    /**
     * The XML document from our data source.
     */
    private Document document;

    /**
     * If true, modifications are immediately persisted.
     */
    private boolean autoSave = false;

    /**
     * Empty construtor.  You must provide a file/fileName
     * to save the configuration.
     */
    public XMLConfiguration()
    {
        // build an empty document.
        DocumentBuilder builder = null;
        try
        {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new ConfigurationRuntimeException(e.getMessage(), e);
        }

        document = builder.newDocument();
        document.appendChild(document.createElement("configuration"));
    }

    /**
     * Attempts to load the XML file as a resource from the
     * classpath. The XML file must be located somewhere in the
     * classpath.
     *
     * @param resource Name of the resource
     * @throws ConfigurationException If error reading data source.
     */
    public XMLConfiguration(String resource) throws ConfigurationException
    {
        setFile(resourceURLToFile(resource));
        load();
    }

    /**
     * Attempts to load the XML file.
     *
     * @param file File object representing the XML file.
     * @throws ConfigurationException If error reading data source.
     */
    public XMLConfiguration(File file) throws ConfigurationException
    {
        setFile(file);
        load();
    }

    public void load() throws ConfigurationException
    {
        File file = null;
        try
        {
            URL url = ConfigurationUtils.getURL(getBasePath(), getFileName());
            file = new File(url.getFile());
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(file);
        }
        catch (IOException de)
        {
            throw new ConfigurationException("Could not load from " + file.getAbsolutePath(), de);
        }
        catch (ParserConfigurationException ex)
        {
            throw new ConfigurationException("Could not configure parser", ex);
		}
        catch (FactoryConfigurationError ex)
        {
            throw new ConfigurationException("Could not create parser", ex);
        }
        catch (SAXException ex)
        {
            throw new ConfigurationException("Error parsing file " + file.getAbsolutePath(), ex);
		}

        initProperties(document.getDocumentElement(), new StringBuffer());
    }

    private static File resourceURLToFile(String resource)
    {
        URL confURL = XMLConfiguration.class.getClassLoader().getResource(resource);
        if (confURL == null)
        {
            confURL = ClassLoader.getSystemResource(resource);
        }
        return new File(confURL.getFile());
    }

    /**
     * Loads and initializes from the XML file.
     *
     * @param element The element to start processing from.  Callers
     *                should supply the root element of the document.
     * @param hierarchy
     */
    private void initProperties(Element element, StringBuffer hierarchy)
    {
        StringBuffer buffer = new StringBuffer();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            if (node instanceof Element)
            {
                Element child = (Element) node;

                StringBuffer subhierarchy = new StringBuffer(hierarchy.toString());
                subhierarchy.append(child.getTagName());
                processAttributes(subhierarchy.toString(), child);
                initProperties(child, subhierarchy.append(NODE_DELIMITER));
            }
            else if (node instanceof CDATASection || node instanceof Text)
            {
                CharacterData data = (CharacterData)node;
                buffer.append(data.getData());
            }
        }

        String text = buffer.toString().trim();
        if (text.length() > 0 && hierarchy.length() > 0)
        {
            super.addProperty(hierarchy.substring(0, hierarchy.length() - 1), text);
        }
    }

    /**
     * Helper method for constructing properties for the attributes of the
     * given XML element.
     *
     * @param hierarchy the actual hierarchy
     * @param element the actual XML element
     */
    private void processAttributes(String hierarchy, Element element)
    {
        // Add attributes as x.y{ATTRIBUTE_START}att{ATTRIBUTE_END}
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            Attr attr = (Attr) attributes.item(i);
            String attrName = hierarchy + ATTRIBUTE_START + attr.getName() + ATTRIBUTE_END;
            super.addProperty(attrName, attr.getValue());
        }
    }

    /**
     * Calls super method, and also ensures the underlying {@link
     * Document} is modified so changes are persisted when saved.
     *
     * @param name
     * @param value
     */
    public void addProperty(String name, Object value)
    {
        super.addProperty(name, value);
        addXmlProperty(name, value);
        possiblySave();
    }

    /**
     * Calls super method, and also ensures the underlying {@link
     * Document} is modified so changes are persisted when saved.
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value)
    {
        super.setProperty(name, value);
        setXmlProperty(name, value);
        possiblySave();
    }

    /**
     * Sets the property value in our document tree, auto-saving if
     * appropriate.
     *
     * @param name The name of the element to set a value for.
     * @param value The value to set.
     */
    private void setXmlProperty(String name, Object value)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        Element element = document.getDocumentElement();
        for (int i = 0; i < nodes.length; i++)
        {
            String eName = nodes[i];

            Element child = null;
            NodeList list = element.getChildNodes();
            for (int j = 0; j < list.getLength(); j++)
            {
                Node node = list.item(j);
                if (node instanceof Element)
                {
                    child = (Element) node;
                    if (eName.equals(child.getTagName()))
                    {
                        break;
                    }
                    child = null;
                }
            }
            // If we don't find this part of the property in the XML hierarchy
            // we add it as a new node
            if (child == null)
            {
                child = document.createElement(eName);
                element.appendChild(child);
            }
            element = child;
        }

        if (attName == null)
        {
            CharacterData data = document.createTextNode((String) value);
            element.appendChild(data);
        }
        else
        {
            element.setAttribute(attName, (String) value);
        }
    }

    /**
     * Adds the property value in our document tree, auto-saving if
     * appropriate.
     *
     * @param name The name of the element to set a value for.
     * @param value The value to set.
     */
    private void addXmlProperty(String name, Object value)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        Element element = document.getDocumentElement();
        Element parent = element;

        for (int i = 0; i < nodes.length; i++)
        {
            if(element == null) break;
            parent = element;
            String eName = nodes[i];
            Element child = null;

            NodeList list = element.getChildNodes();
            for (int j = 0; j < list.getLength(); j++)
            {
                Node node = list.item(j);
                if (node instanceof Element)
                {
                    child = (Element) node;
                    if (eName.equals(child.getTagName()))
                    {
                        break;
                    }
                    child = null;
                }
            }

            element = child;
        }

        Element child = document.createElement(nodes[nodes.length-1]);
        parent.appendChild(child);
        if (attName == null)
        {
            CharacterData data = document.createTextNode((String) value);
            child.appendChild(data);
        }
        else
        {
            child.setAttribute(attName, (String) value);
        }
    }

    /**
     * Calls super method, and also ensures the underlying {@link Document} is
     * modified so changes are persisted when saved.
     *
     * @param name The name of the property to clear.
     */
    public void clearProperty(String name)
    {
        super.clearProperty(name);
        clearXmlProperty(name);
        possiblySave();
    }

    private void clearXmlProperty(String name)
    {
        // parse the key
        String[] nodes = parseElementNames(name);
        String attName = parseAttributeName(name);

        Element element = null;
        Element child = document.getDocumentElement();
        for (int i = 0; i < nodes.length; i++)
        {
            element = child;
            String eName = nodes[i];

            NodeList list = element.getChildNodes();
            for (int j = 0; j < list.getLength(); j++) {
                Node node = list.item(j);
                if (node instanceof Element)
                {
                    child = (Element) node;
                    if (eName.equals(child.getTagName()))
                    {
                        break;
                    }
                    child = null;
                }
            }
            if (child == null)
            {
                return;
            }
        }

        if (attName == null)
        {
            element.removeChild(child);
        }
        else
        {
            child.removeAttribute(attName);
        }
    }

    /**
     * Save the configuration if the automatic persistence is enabled and a
     * file is specified.
     */
    private void possiblySave()
    {
        if (autoSave && fileName != null)
        {
            try
            {
                save();
            }
            catch (ConfigurationException ce)
            {
                throw new ConfigurationRuntimeException("Failed to auto-save", ce);
            }
        }
    }

    /**
     * If true, changes are automatically persisted.
     *
     * @param autoSave
     */
    public void setAutoSave(boolean autoSave)
    {
        this.autoSave = autoSave;
    }

    /**
     * Save the configuration to the file specified by the fileName attribute.
     *
     * @throws ConfigurationException
     */
    public void save() throws ConfigurationException
    {
        save(getFile().toString());
    }

    /**
     * Save the configuration to a file.
     *
     * @param filename the name of the xml file
     *
     * @throws ConfigurationException
     */
    public void save(String filename) throws ConfigurationException
    {
        FileWriter writer = null;

        try
        {
            writer = new FileWriter(filename);
            save(writer);
        }
        catch (IOException ioe)
        {
        	throw new ConfigurationException("Could not save to " + getFile());
        }
        finally
        {
        	try
            {
                if (writer != null)
                {
                    writer.close();
                }
        	}
        	catch (IOException ioe)
            {
        		throw new ConfigurationException(ioe);
        	}
        }
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param out the output stream used to save the configuration
     */
    public void save(OutputStream out) throws ConfigurationException
    {
        save(out, null);
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param out the output stream used to save the configuration
     * @param encoding the charset used to write the configuration
     */
    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
            save(writer);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param writer the output stream used to save the configuration
     */
    public void save(Writer writer) throws ConfigurationException
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource(document);
            Result result = new StreamResult(writer);

            transformer.setOutputProperty("indent", "yes");
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Returns the file.
     *
     * @return File
     */
    public File getFile()
    {
        return ConfigurationUtils.constructFile(getBasePath(), getFileName());
    }

    /**
     * Sets the file.
     *
     * @param file The file to set
     */
    public void setFile(File file)
    {
        this.fileName = file.getAbsolutePath();
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * Returns the fileName.
     *
     * @return String
     */
    public String getFileName()
    {
        return fileName;
    }

    public String toString()
    {
        StringWriter writer = new StringWriter();
        try
        {
            save(writer);
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * Parse a property key and return an array of the element hierarchy it
     * specifies. For example the key "x.y.z[@abc]" will result in [x, y, z].
     *
     * @param key the key to parse
     *
     * @return the elements in the key
     */
    protected static String[] parseElementNames(String key)
    {
        if (key == null)
        {
            return new String[] {};
        }
        else
        {
            // find the beginning of the attribute name
            int attStart = key.indexOf(ATTRIBUTE_START);

            if (attStart > -1)
            {
                // remove the attribute part of the key
                key = key.substring(0, attStart);
            }

            return StringUtils.split(key, NODE_DELIMITER);
        }
    }

    /**
     * Parse a property key and return the attribute name if it existst.
     *
     * @param key the key to parse
     *
     * @return the attribute name, or null if the key doesn't contain one
     */
    protected static String parseAttributeName(String key)
    {
        String name = null;

        if (key != null)
        {
            // find the beginning of the attribute name
            int attStart = key.indexOf(ATTRIBUTE_START);

            if (attStart > -1)
            {
                // find the end of the attribute name
                int attEnd = key.indexOf(ATTRIBUTE_END);
                attEnd = attEnd > -1 ? attEnd : key.length();

                name = key.substring(attStart + ATTRIBUTE_START.length(), attEnd);
            }
        }

        return name;
    }
}
