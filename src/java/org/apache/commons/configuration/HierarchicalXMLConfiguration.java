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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.apache.commons.configuration.reloading.ReloadingStrategy;

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
 * @version $Revision: 1.6 $, $Date: 2004/11/19 19:26:48 $
 */
public class HierarchicalXMLConfiguration extends HierarchicalConfiguration implements FileConfiguration
{
    /** Constant for the default root element name.*/
    private static final String DEFAULT_ROOT_NAME = "configuration";
    
    private FileConfiguration delegate = new FileConfigurationDelegate();
    
    /** Stores the name of the root element.*/
    private String rootElementName;
    
    /**
     * Returns the name of the root element.
     * @return the name of the root element
     */
    public String getRootElementName()
    {
        return (rootElementName == null) ? DEFAULT_ROOT_NAME : rootElementName;
    }
    
    /**
     * Sets the name of the root element. This name is used when this
     * configuration object is stored in an XML file.
     * @param name the name of the root element
     */
    public void setRootElementName(String name)
    {
        rootElementName = name;
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
            else if (w3cNode instanceof Text)
            {
                Text data = (Text) w3cNode;
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
    
    /**
     * Creates a DOM document from the internal tree of configuration nodes.
     * @return the new document
     * @throws ConfigurationException if an error occurs
     */
    protected Document createDocument() throws ConfigurationException
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElem = document.createElement(getRootElementName());
            document.appendChild(rootElem);
            constructDOM(document, getRoot(), rootElem);
            return document;
        }  /* try */
        catch(DOMException domEx)
        {
            throw new ConfigurationException(domEx);
        }
        catch(ParserConfigurationException pex)
        {
            throw new ConfigurationException(pex);
        }
    }
    
    /**
     * Creates a DOM from the tree of configuration nodes.
     * @param document the document
     * @param node the actual node
     * @param element the actual XML element
     * @throws DOMException if an error occurs
     */
    private void constructDOM(Document document, Node node, Element element)
    throws DOMException
    {
        for(Iterator it = node.getChildren().iterator(); it.hasNext();)
        {
            Node child = (Node) it.next();
            if(ConfigurationKey.isAttributeKey(child.getName()))
            {
                if (child.getValue() != null)
                {
                    element.setAttribute(ConfigurationKey.removeAttributeMarkers(
                            child.getName()), child.getValue().toString());
                }
            }
            else
            {
                Element childElem = document.createElement(child.getName());
                if(child.getValue() != null)
                {
                    Text text = document.createTextNode(child.getValue().toString());
                    childElem.appendChild(text);
                    
                }
                constructDOM(document, child, childElem);
                element.appendChild(childElem);
            }
        }
    }
    
    public void load() throws ConfigurationException
    {
        delegate.load();
    }

    public void load(String fileName) throws ConfigurationException
    {
        delegate.load(fileName);
    }

    public void load(File file) throws ConfigurationException
    {
        delegate.load(file);
    }

    public void load(URL url) throws ConfigurationException
    {
        delegate.load(url);
    }

    public void load(InputStream in) throws ConfigurationException
    {
        delegate.load(in);
    }

    public void load(InputStream in, String encoding) throws ConfigurationException
    {
        delegate.load(in, encoding);
    }

    public void load(Reader in) throws ConfigurationException
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            initProperties(builder.parse(new InputSource(in)));
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    public void save() throws ConfigurationException
    {
        delegate.save();
    }

    public void save(String fileName) throws ConfigurationException
    {
        delegate.save(fileName);
    }

    public void save(File file) throws ConfigurationException
    {
        delegate.save(file);
    }

    public void save(URL url) throws ConfigurationException
    {
        delegate.save(url);
    }

    public void save(OutputStream out) throws ConfigurationException
    {
        delegate.save(out);
    }

    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        delegate.save(out, encoding);
    }

        /**
         * Saves the configuration to the specified writer.
         * 
         * @param writer
         *            the writer used to save the configuration
         * @throws ConfigurationException if an error occurs
         */
        public void save(Writer writer) throws ConfigurationException
        {
            try 
            {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                Source source = new DOMSource(createDocument());
                Result result = new StreamResult(writer);
    
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
            } 
            catch (TransformerException e) 
            {
                throw new ConfigurationException(e.getMessage(), e);
            }
         }

    public String getFileName()
    {
        return delegate.getFileName();
    }

    public void setFileName(String fileName)
    {
        delegate.setFileName(fileName);
    }

    public String getBasePath()
    {
        return delegate.getBasePath();
    }

    public void setBasePath(String basePath)
    {
        delegate.setBasePath(basePath);
    }

    public File getFile()
    {
        return delegate.getFile();
    }

    public void setFile(File file)
    {
        delegate.setFile(file);
    }

    public URL getURL()
    {
        return delegate.getURL();
    }

    public void setURL(URL url)
    {
        delegate.setURL(url);
    }

    public void setAutoSave(boolean autoSave)
    {
        delegate.setAutoSave(autoSave);
    }

    public boolean isAutoSave()
    {
        return delegate.isAutoSave();
    }

    public ReloadingStrategy getReloadingStrategy()
    {
        return delegate.getReloadingStrategy();
    }

    public void setReloadingStrategy(ReloadingStrategy strategy)
    {
        delegate.setReloadingStrategy(strategy);
    }

    public void reload()
    {
        delegate.reload();
    }

    private class FileConfigurationDelegate extends AbstractFileConfiguration {

        public void load(Reader in) throws ConfigurationException
        {
            HierarchicalXMLConfiguration.this.load(in);
        }

        public void save(Writer out) throws ConfigurationException
        {
            HierarchicalXMLConfiguration.this.save(out);
        }

    }
}
