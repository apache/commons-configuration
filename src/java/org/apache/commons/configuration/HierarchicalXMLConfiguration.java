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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * A specialized hierarchical configuration class that is able to parse
 * XML documents.
 *
 * <p>The parsed document will be stored keeping its structure. The
 * contained properties can be accessed using all methods supported by
 * the base class <code>HierarchicalConfiguration</code>.
 *
 * @since commons-configuration 1.0
 *
 * @author J&ouml;rg Schaible
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Revision: 1.1 $, $Date: 2004/07/12 12:14:38 $
 */
public class HierarchicalXMLConfiguration extends HierarchicalConfiguration implements BasePathLoader
{
    /** Stores the file name of the document to be parsed.*/
    private String file;

    /** Stores the base path of this configuration.*/
    private String basePath;


    /**
     * Constructs a HierarchicalXMLConfiguration.
     */
    public HierarchicalXMLConfiguration()
    {
        super();
    }

    /**
     * Returns the name of the file to be parsed by this object.
     * @return the file to be parsed
     */
    public String getFileName()
    {
        return file;
    }

    /**
     * Sets the name of the file to be parsed by this object.
     * @param file the file to be parsed
     */
    public void setFileName(String file)
    {
        this.file = file;
    }

    /**
     * Returns the base path.
     * @return the base path
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Allows to set a base path. Relative file names are resolved based on
     * this path.
     * @param path the base path; this can be a URL or a file path
     */
    public void setBasePath(String path)
    {
        basePath = path;
    }

    /**
     * Loads and parses an XML document. The file to be loaded must have
     * been specified before.
     * @throws ConfigurationException Thrown if an error occurs
     */
    public void load() throws ConfigurationException
    {
        try
        {
            load(ConfigurationUtils.getURL(getBasePath(), getFileName()));
        }
        catch (MalformedURLException mue)
        {
            throw new ConfigurationException("Could not load from " + getBasePath() + ", " + getFileName(), mue);
        }
    }

    /**
     * Loads and parses the specified XML document.
     * @param url the URL to the XML document
     *
     * @throws ConfigurationException Thrown if an error occurs
     */
    public void load(URL url) throws ConfigurationException
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            initProperties(builder.parse(url.toExternalForm()));
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Could not load from " + url);
        }
    }

    /**
     * Initializes this configuration from an XML document.
     *
     * @param document the document to be parsed
     */
    public void initProperties(Document document)
    {
        constructHierarchy(getRoot(), document.getDocumentElement());
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
        StringBuffer buffer = new StringBuffer();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element)
            {
                Element child = (Element) w3cNode;
                Node childNode = new Node(child.getTagName());
                constructHierarchy(childNode, child);
                node.addChild(childNode);
                processAttributes(childNode, child);
            }
            else if (w3cNode instanceof CharacterData)
            {
                CharacterData data = (CharacterData) w3cNode;
                buffer.append(data.getData());
            }
        }
        String text = buffer.toString().trim();
        if (text.length() > 0)
        {
            node.setValue(text);
        }
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
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            org.w3c.dom.Node w3cNode = attributes.item(i);
            if (w3cNode instanceof Attr)
            {
                Attr attr = (Attr) w3cNode;
                Node child =
                    new Node(
                        ConfigurationKey.constructAttributeKey(attr.getName()));
                child.setValue(attr.getValue());
                node.addChild(child);
            }
        }
    }
}
