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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

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
 * @since 0.8.1
 *
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan</a>
 * @author <a href="mailto:dlr@apache.org">Daniel Rall</a>
 * @version $Revision: 1.7 $, $Date: 2004/06/16 18:13:53 $
 */
public class DOM4JConfiguration extends XMLConfiguration
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
    public DOM4JConfiguration()
    {
    }

    /**
     * Attempts to load the XML file as a resource from the
     * classpath. The XML file must be located somewhere in the
     * classpath.
     *
     * @param resource Name of the resource
     * @throws ConfigurationException If error reading data source.
     * @see DOM4JConfiguration#DOM4JConfiguration(File)
     */
    public DOM4JConfiguration(String resource) throws ConfigurationException
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
    public DOM4JConfiguration(File file) throws ConfigurationException
    {
        setFile(file);
        load();
    }

    public void load() throws ConfigurationException
    {

    	try {
    		document = new SAXReader().read(
    				ConfigurationUtils.getURL(getBasePath(), getFileName()));
    	}
    	catch (MalformedURLException mue){
    		throw new ConfigurationException("Could not load from " + getBasePath() + ", " + getFileName());
    	}
    	catch (DocumentException de){
    		throw new ConfigurationException("Could not load from " + getBasePath() + ", " + getFileName());
    	}
        initProperties(document.getRootElement(), new StringBuffer());

    }

    private static File resourceURLToFile(String resource)
    {
        URL confURL = DOM4JConfiguration.class.getClassLoader().getResource(resource);
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
    private void initProperties(Element element, StringBuffer hierarchy)
    {
        for (Iterator it = element.elementIterator(); it.hasNext();)
        {
            StringBuffer subhierarchy = new StringBuffer(hierarchy.toString());
            Element child = (Element) it.next();
            String nodeName = child.getName();
            String nodeValue = child.getTextTrim();
            subhierarchy.append(nodeName);
            if (nodeValue.length() > 0)
            {
                super.addProperty(subhierarchy.toString(), nodeValue);
            }

            // Add attributes as x.y{ATTRIB_START_MARKER}att{ATTRIB_END_MARKER}
            List attributes = child.attributes();
            for (int j = 0, k = attributes.size(); j < k; j++)
            {
                Attribute a = (Attribute) attributes.get(j);
                String attName = subhierarchy.toString() + '[' + ATTRIB_MARKER + a.getName() + ']';
                String attValue = a.getValue();
                super.addProperty(attName, attValue);
            }
            StringBuffer buf = new StringBuffer(subhierarchy.toString());
            initProperties(child, buf.append('.'));
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
        Element element = document.getRootElement();
        for (int i = 0; i < nodes.length; i++)
        {
            String eName = nodes[i];
            int index = eName.indexOf(ATTRIB_START_MARKER);
            if (index > -1)
            {
                attName = eName.substring(index + ATTRIB_START_MARKER.length(), eName.length() - 1);
                eName = eName.substring(0, index);
            }
            // If we don't find this part of the property in the XML heirarchy
            // we add it as a new node
            if (element.element(eName) == null && attName == null)
            {
                element.addElement(eName);
            }
            element = element.element(eName);
        }

        if (attName == null)
        {
            element.setText((String) value);
        }
        else
        {
            element.addAttribute(attName, (String) value);
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
        Element element = document.getRootElement();
        for (int i = 0; i < nodes.length; i++)
        {
            String eName = nodes[i];
            int index = eName.indexOf(ATTRIB_START_MARKER);
            if (index > -1)
            {
                attName = eName.substring(index + ATTRIB_START_MARKER.length(), eName.length() - 1);
                eName = eName.substring(0, index);
            }
            element = element.element(eName);
            if (element == null)
            {
                return;
            }
        }

        if (attName == null)
        {
            element.remove(element.element(nodes[nodes.length - 1]));
        }
        else
        {
            element.remove(element.attribute(attName));
        }
    }

    /**
     * Save the configuration if the automatic persistence is enabled.
     */
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

    public synchronized void save() throws ConfigurationException
    {
        XMLWriter writer = null;
        OutputStream out = null;
        try
        {
            OutputFormat outputter = OutputFormat.createPrettyPrint();
            out = new BufferedOutputStream(new FileOutputStream(getFile()));
            writer = new XMLWriter(out, outputter);
            writer.write(document);
        }
        catch (IOException ioe){
        	throw new ConfigurationException("Could not save to " + getFile());
        }
        finally
        {
        	try {
            if (out != null)
            {
                out.close();
            }

            if (writer != null)
            {
                writer.close();
            }
        	}
        	catch (IOException ioe){
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
}
