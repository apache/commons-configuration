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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

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
 * @since commons-configuragtion 1.0
 */
public class DOMConfiguration extends XMLConfiguration
{
    // For conformance with xpath
    private static final char ATTRIB_MARKER = '@';
    private static final String ATTRIB_START_MARKER = "[" + ATTRIB_MARKER;

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
     * and call the load method
     *
     */
    public DOMConfiguration()
    {
    }

    /**
     * Attempts to load the XML file as a resource from the
     * classpath. The XML file must be located somewhere in the
     * classpath.
     *
     * @param resource Name of the resource
     * @exception ConfigurationException If error reading data source.
     * @see DOMConfiguration#DOMConfiguration(String)
     */
    public DOMConfiguration(String resource) throws ConfigurationException
    {
        setFile(resourceURLToFile(resource));
        load();
    }

    /**
     * Attempts to load the XML file.
     *
     * @param file File object representing the XML file.
     * @exception ConfigurationException If error reading data source.
     */
    public DOMConfiguration(File file) throws ConfigurationException
    {
        setFile(file);
        load();
    }

    public void load() throws ConfigurationException
    {
        File file = null;
        try
            {
            file = new File(getBasePath(), getFileName());
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(file);
        }
        catch (IOException de)
        {
            throw new ConfigurationException("Could not load from " + file.getAbsolutePath());
        }
        catch (ParserConfigurationException ex)
        {
            throw new ConfigurationException("Could not configure parser");
		}
        catch (FactoryConfigurationError ex)
        {
            throw new ConfigurationException("Could not create parser");
        }
        catch (SAXException ex)
        {
            throw new ConfigurationException("Error parsing file " + file.getAbsolutePath());
		}
        initProperties(document.getDocumentElement(), new StringBuffer());

    }

    private static File resourceURLToFile(String resource)
    {
        URL confURL = DOMConfiguration.class.getClassLoader().getResource(resource);
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
     * should supply the root element of the document.
     * @param hierarchy
     */
    private void initProperties(final Element element, final StringBuffer hierarchy)
    {
        final StringBuffer buffer = new StringBuffer();
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            final org.w3c.dom.Node w3cNode = list.item(i);
            if(w3cNode instanceof Element)
            {
                final StringBuffer subhierarchy = 
                    new StringBuffer(hierarchy.toString());
                final Element child = (Element)w3cNode;
                subhierarchy.append(child.getTagName());
                processAttributes(subhierarchy.toString(), child);
                initProperties(child, 
                        new StringBuffer(subhierarchy.toString()).append('.'));
            } 
            else if(w3cNode instanceof CharacterData)
            {
                final CharacterData data = (CharacterData)w3cNode;
                buffer.append(data.getData());
            }
        }
        final String text = buffer.toString().trim();
        if (text.length() > 0 && hierarchy.length() > 0)
        {
            super.addProperty(
                    hierarchy.substring(0, hierarchy.length()-1), text);
        }
    }

    /**
     * Helper method for constructing properties for the attributes of the
     * given XML element.
     * @param hierarchy the actual hierarchy
     * @param element the actual XML element
     */
    private void processAttributes(String hierarchy, Element element)
    {
        // Add attributes as x.y{ATTRIB_START_MARKER}att{ATTRIB_END_MARKER}
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            final org.w3c.dom.Node w3cNode = attributes.item(i);
            if (w3cNode instanceof Attr)
            {
                Attr attr = (Attr) w3cNode;
                String attrName = 
                    hierarchy + '[' + ATTRIB_MARKER + attr.getName() + ']';
                super.addProperty(attrName, attr.getValue());
            }
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
        setXmlProperty(name, value);
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
        String[] nodes = StringUtils.split(name, NODE_DELIMITER);
        String attName = null;
        Element element = document.getDocumentElement();
        for (int i = 0; i < nodes.length; i++)
        {
            String eName = nodes[i];
            int index = eName.indexOf(ATTRIB_START_MARKER);
            if (index > -1)
            {
                attName = eName.substring(index + ATTRIB_START_MARKER.length(), eName.length() - 1);
                eName = eName.substring(0, index);
            }
            
            Element child = null;
            final NodeList list = element.getChildNodes();
            for (int j = 0; j < list.getLength(); j++)
            {
                final org.w3c.dom.Node w3cNode = list.item(j);
                if (w3cNode instanceof Element)
                {
                    child = (Element) w3cNode;
                    if (eName.equals(child.getTagName()))
                    {
                        break;
                    }
                    child = null;
                }
            }
            // If we don't find this part of the property in the XML hierarchy
            // we add it as a new node
            if (child == null && attName == null)
            {
                child = document.createElement(eName);
                element.appendChild(child);
            }
            element = child;
        }

        if (attName == null)
        {
            final CharacterData data = document.createTextNode((String) value);
            element.appendChild(data);
        }
        else
        {
            element.setAttribute(attName, (String) value);
        }
    }

    /**
     * Calls super method, and also ensures the underlying {@link
     * Document} is modified so changes are persisted when saved.
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
        String[] nodes = StringUtils.split(name, NODE_DELIMITER);
        String attName = null;
        Element element = null;
        Element child = document.getDocumentElement();
        for (int i = 0; i < nodes.length; i++)
        {
            element = child;
            String eName = nodes[i];
            int index = eName.indexOf(ATTRIB_START_MARKER);
            if (index > -1)
            {
                attName = eName.substring(index + ATTRIB_START_MARKER.length(), eName.length() - 1);
                eName = eName.substring(0, index);
            }
            
            final NodeList list = element.getChildNodes();
            for (int j = 0; j < list.getLength(); j++) {
                final org.w3c.dom.Node w3cNode = list.item(j);
                if (w3cNode instanceof Element)
                {
                    child = (Element) w3cNode;
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

    private void possiblySave()
    {
        if (autoSave)
        {
            try
            {
                save();
            }
            catch (ConfigurationException ce)
            {
                throw new NestableRuntimeException("Failed to auto-save", ce);
            }
        }
    }

    /**
     * If true, changes are automatically persisted.
     * @param autoSave
     */
    public void setAutoSave(boolean autoSave)
    {
        this.autoSave = autoSave;
    }

    public synchronized void save() throws ConfigurationException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(getFile());
            writer.write(toString());
        }
        catch (IOException ioe){
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
     * Returns the file.
     * @return File
     */
    public File getFile()
    {
        return ConfigurationUtils.constructFile(getBasePath(), getFileName());
    }

    /**
     * Sets the file.
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
     * @return String
     */
    public String getFileName()
    {
        return fileName;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        toXML(document, buffer);
    	return buffer.toString();
    }
    
    private void toXML(final org.w3c.dom.Node element, final StringBuffer buffer)
	{
    	final NodeList nodeList = element.getChildNodes();
    	for (int i=0; i<nodeList.getLength(); i++) {
    		final org.w3c.dom.Node node = nodeList.item(i);
    		if (node instanceof Element)
            {
    			buffer.append("<" + node.getNodeName());
                if (node.hasAttributes())
                {
                	final NamedNodeMap map = node.getAttributes();
                    for (int j = 0; j < map.getLength(); j++)
                    {
                    	final Attr attr = (Attr) map.item(j);
                        buffer.append(" " + attr.getName());
                        buffer.append("=\"" + attr.getValue() + "\"");
                    }
                }
                buffer.append(">");
    			toXML(node, buffer);
    			buffer.append("</" + node.getNodeName() + ">");
    		} 
    		else if(node instanceof CharacterData)
    		{
    			final CharacterData data = (CharacterData)node;
    			buffer.append(data.getData());
    		}
    	}
    }
}
