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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * <p>A specialized hierarchical configuration class that is able to parse
 * XML documents using DOM4J.</p>
 * <p>The parsed document will be stored keeping its structure. The
 * contained properties can be accessed using all methods supported by
 * the base class <code>HierarchicalProperties</code>.
 * 
 * @version $Id: HierarchicalDOM4JConfiguration.java,v 1.6 2004/06/21 18:42:39 ebourg Exp $
 */
public class HierarchicalDOM4JConfiguration extends HierarchicalConfiguration implements BasePathLoader
{
    /** Stores the file name of the document to be parsed.*/
    private String file;

    /** Stores the base path of this configuration.*/
    private String basePath;

    /**
     * Creates a new instance of <code>HierarchicalDOM4JConfiguration</code>.
     */
    public HierarchicalDOM4JConfiguration()
    {
        super();
    }

    /**
     * Returns the name of the file to be parsed by this object.
     *
     * @return the file to be parsed
     */
    public String getFileName()
    {
        return file;
    }

    /**
     * Sets the name of the file to be parsed by this object.
     *
     * @param file the file to be parsed
     */
    public void setFileName(String file)
    {
        this.file = file;
    }

    /**
     * Returns the base path.
     * 
     * @return the base path
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Allows to set a base path. Relative file names are resolved based on
     * this path.
     *
     * @param path the base path; this can be a URL or a file path
     */
    public void setBasePath(String path)
    {
        basePath = path;
    }

    /**
     * Loads and parses an XML document. The file to be loaded must have
     * been specified before.
     *
     * @throws ConfigurationException if an error occurs
     */
    public void load() throws ConfigurationException
    {
    	try
        {
    		load(ConfigurationUtils.getURL(getBasePath(), getFileName()));
    	}
    	catch (MalformedURLException e)
        {
    		throw new ConfigurationException("Could not load from " + getBasePath() + ", " + getFileName(), e);
    	}
    }

    /**
     * Loads and parses the specified XML document.
     *
     * @param url the URL to the XML document
     * @throws ConfigurationException if an error occurs
     */
    public void load(URL url) throws ConfigurationException
    {
    	try
        {
    		initProperties(new SAXReader().read(url));
    	}
    	catch (DocumentException e)
        {
    		throw new ConfigurationException("Could not load from " + url, e);
    	}
    }

    /**
     * Initializes this configuration from an XML document.
     *
     * @param document the document to be parsed
     */
    public void initProperties(Document document)
    {
        constructHierarchy(getRoot(), document.getRootElement());
    }

    /**
     * Helper method for building the internal storage hierarchy. The XML
     * elements are transformed into node objects.
     *
     * @param node the actual node
     * @param element the actual XML element
     */
    private void constructHierarchy(Node node, Element element)
    {
        if (element.getTextTrim().length() > 0)
        {
            node.setValue(element.getTextTrim());
        } /* if */
        processAttributes(node, element);

        for (Iterator it = element.elementIterator(); it.hasNext();)
        {
            Element child = (Element) it.next();
            Node childNode = new Node(child.getName());
            constructHierarchy(childNode, child);
            node.addChild(childNode);
        } /* for */
    }

    /**
     * Helper method for constructing node objects for the attributes of the
     * given XML element.
     *
     * @param node the actual node
     * @param element the actual XML element
     */
    private void processAttributes(Node node, Element element)
    {
        for (Iterator it = element.attributeIterator(); it.hasNext();)
        {
            Attribute attr = (Attribute) it.next();
            Node child =
                new Node(
                    ConfigurationKey.constructAttributeKey(attr.getName()));
            child.setValue(attr.getValue());
            node.addChild(child);
        } /* for */
    }
}
