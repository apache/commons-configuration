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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.apache.commons.configuration.reloading.ReloadingStrategy;

/**
 * A specialized hierarchical configuration class that is able to parse XML
 * documents.
 * 
 * <p>The parsed document will be stored keeping its structure. The class also
 * tries to preserve as much information from the loaded XML document as
 * possible, including comments and processing instructions. These will be
 * contained in documents created by the <code>save()</code> methods, too.
 * 
 * @since commons-configuration 1.0
 * 
 * @author J&ouml;rg Schaible
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger </a>
 * @version $Revision$, $Date$
 */
public class XMLConfiguration extends HierarchicalConfiguration implements FileConfiguration
{
    /** Constant for the default root element name. */
    private static final String DEFAULT_ROOT_NAME = "configuration";

    /** Delimiter character for attributes. */
    private static char ATTR_DELIMITER = ',';

    private FileConfigurationDelegate delegate = new FileConfigurationDelegate();

    /** The document from this configuration's data source. */
    private Document document;

    /** Stores the name of the root element. */
    private String rootElementName;

    /**
     * Creates a new instance of <code>XMLConfiguration</code>.
     */
    public XMLConfiguration()
    {
        super();
    }

    /**
     * Creates a new instance of <code>XMLConfiguration</code>.
     * The configuration is loaded from the specified file
     * 
     * @param fileName the name of the file to load
     * @throws ConfigurationException if the file cannot be loaded
     */
    public XMLConfiguration(String fileName) throws ConfigurationException
    {
        this();
        setFileName(fileName);
        load();
    }

    /**
     * Creates a new instance of <code>XMLConfiguration</code>.
     * The configuration is loaded from the specified file.
     * 
     * @param file the file
     * @throws ConfigurationException if an error occurs while loading the file
     */
    public XMLConfiguration(File file) throws ConfigurationException
    {
        this();
        setFile(file);
        if (file.exists())
        {
            load();
        }
    }

    /**
     * Creates a new instance of <code>XMLConfiguration</code>.
     * The configuration is loaded from the specified URL.
     * 
     * @param url the URL
     * @throws ConfigurationException if loading causes an error
     */
    public XMLConfiguration(URL url) throws ConfigurationException
    {
        this();
        setURL(url);
        load();
    }

    /**
     * Returns the name of the root element. If this configuration was loaded
     * from a XML document, the name of this document's root element is
     * returned. Otherwise it is possible to set a name for the root element
     * that will be used when this configuration is stored.
     * 
     * @return the name of the root element
     */
    public String getRootElementName()
    {
        if (getDocument() == null)
        {
            return (rootElementName == null) ? DEFAULT_ROOT_NAME : rootElementName;
        }
        else
        {
            return getDocument().getDocumentElement().getNodeName();
        }
    }

    /**
     * Sets the name of the root element. This name is used when this
     * configuration object is stored in an XML file. Note that setting the name
     * of the root element works only if this configuration has been newly
     * created. If the configuration was loaded from an XML file, the name
     * cannot be changed and an <code>UnsupportedOperationException</code>
     * exception is thrown. Whether this configuration has been loaded from an
     * XML document or not can be found out using the <code>getDocument()</code>
     * method.
     * 
     * @param name the name of the root element
     */
    public void setRootElementName(String name)
    {
        if (getDocument() != null)
        {
            throw new UnsupportedOperationException("The name of the root element "
                    + "cannot be changed when loaded from an XML document!");
        }
        rootElementName = name;
    }
    
    /**
     * Returns the XML document this configuration was loaded from. The return
     * value is <b>null</b> if this configuration was not loaded from a XML
     * document.
     * 
     * @return the XML document this configuration was loaded from
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * @inheritDoc
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        super.addPropertyDirect(key, obj);
        delegate.possiblySave();
    }

    /**
     * @inheritDoc
     */
    public void clearProperty(String key)
    {
        super.clearProperty(key);
        delegate.possiblySave();
    }

    /**
     * @inheritDoc
     */
    public void clearTree(String key)
    {
        super.clearTree(key);
        delegate.possiblySave();
    }
    
    /**
     * @inheritDoc
     */
    public void setProperty(String key, Object value)
    {
        super.setProperty(key, value);
        delegate.possiblySave();
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
        processAttributes(node, element);
        StringBuffer buffer = new StringBuffer();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element)
            {
                Element child = (Element) w3cNode;
                Node childNode = new XMLNode(child.getTagName(), child);
                constructHierarchy(childNode, child);
                node.addChild(childNode);
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
                for (Iterator it = PropertyConverter.split(attr.getValue(), ATTR_DELIMITER).iterator(); it.hasNext();)
                {
                    Node child = new XMLNode(ConfigurationKey.constructAttributeKey(attr.getName()), element);
                    child.setValue(it.next());
                    node.addChild(child);
                }
            }
        }
    }

    /**
     * Creates a DOM document from the internal tree of configuration nodes.
     * 
     * @return the new document
     * @throws ConfigurationException if an error occurs
     */
    protected Document createDocument() throws ConfigurationException
    {
        try
        {
            if (document == null)
            {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document newDocument = builder.newDocument();
                Element rootElem = newDocument.createElement(getRootElementName());
                newDocument.appendChild(rootElem);
                document = newDocument;
            }

            XMLBuilderVisitor builder = new XMLBuilderVisitor(document);
            builder.processDocument(getRoot());
            return document;
        } /* try */
        catch (DOMException domEx)
        {
            throw new ConfigurationException(domEx);
        }
        catch (ParserConfigurationException pex)
        {
            throw new ConfigurationException(pex);
        }
    }

    /**
     * Creates a new node object. This implementation returns an instance of the
     * <code>XMLNode</code> class.
     * 
     * @param name the node's name
     * @return the new node
     */
    protected Node createNode(String name)
    {
        return new XMLNode(name, null);
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
            Document newDocument = builder.parse(new InputSource(in));
            document = null;
            initProperties(newDocument);
            document = newDocument;
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
     * @param writer the writer used to save the configuration
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

    public String getEncoding()
    {
        return delegate.getEncoding();
    }

    public void setEncoding(String encoding)
    {
        delegate.setEncoding(encoding);
    }

    /**
     * A specialized <code>Node</code> class that is connected with an XML
     * element. Changes on a node are also performed on the associated element.
     */
    class XMLNode extends Node
    {
        /**
         * Creates a new instance of <code>XMLNode</code> and initializes it
         * with the corresponding XML element.
         * 
         * @param elem the XML element
         */
        public XMLNode(Element elem)
        {
            super();
            setReference(elem);
        }

        /**
         * Creates a new instance of <code>XMLNode</code> and initializes it
         * with a name and the corresponding XML element.
         * 
         * @param name the node's name
         * @param elem the XML element
         */
        public XMLNode(String name, Element elem)
        {
            super(name);
            setReference(elem);
        }

        /**
         * Sets the value of this node. If this node is associated with an XML
         * element, this element will be updated, too.
         * 
         * @param value the node's new value
         */
        public void setValue(Object value)
        {
            super.setValue(value);

            if (getReference() != null && document != null)
            {
                if (ConfigurationKey.isAttributeKey(getName()))
                {
                    updateAttribute();
                }
                else
                {
                    updateElement(value);
                }
            }
        }

        /**
         * Updates the associated XML elements when a node is removed.
         */
        protected void removeReference()
        {
            if (getReference() != null)
            {
                Element element = (Element) getReference();
                if (ConfigurationKey.isAttributeKey(getName()))
                {
                    updateAttribute();
                }
                else
                {
                    org.w3c.dom.Node parentElem = element.getParentNode();
                    if (parentElem != null)
                    {
                        parentElem.removeChild(element);
                    }
                }
            }
        }

        /**
         * Updates the node's value if it represents an element node.
         * 
         * @param value the new value
         */
        private void updateElement(Object value)
        {
            Text txtNode = findTextNodeForUpdate();
            if (value == null)
            {
                // remove text
                if (txtNode != null)
                {
                    ((Element) getReference()).removeChild(txtNode);
                }
            }
            else
            {
                if (txtNode == null)
                {
                    txtNode = document.createTextNode(value.toString());
                    if (((Element) getReference()).getFirstChild() != null)
                    {
                        ((Element) getReference()).insertBefore(txtNode, ((Element) getReference()).getFirstChild());
                    }
                    else
                    {
                        ((Element) getReference()).appendChild(txtNode);
                    }
                }
                else
                {
                    txtNode.setNodeValue(value.toString());
                }
            }
        }

        /**
         * Updates the node's value if it represents an attribute.
         *  
         */
        private void updateAttribute()
        {
            XMLBuilderVisitor.updateAttribute(getParent(), getName());
        }

        /**
         * Returns the only text node of this element for update. This method is
         * called when the element's text changes. Then all text nodes except
         * for the first are removed. A reference to the first is returned or
         * <b>null </b> if there is no text node at all.
         * 
         * @return the first and only text node
         */
        private Text findTextNodeForUpdate()
        {
            Text result = null;
            Element elem = (Element) getReference();
            // Find all Text nodes
            NodeList children = elem.getChildNodes();
            Collection textNodes = new ArrayList();
            for (int i = 0; i < children.getLength(); i++)
            {
                org.w3c.dom.Node nd = children.item(i);
                if (nd instanceof Text)
                {
                    if (result == null)
                    {
                        result = (Text) nd;
                    }
                    else
                    {
                        textNodes.add(nd);
                    }
                }
            }

            // We don't want CDATAs
            if (result instanceof CDATASection)
            {
                textNodes.add(result);
                result = null;
            }

            // Remove all but the first Text node
            for (Iterator it = textNodes.iterator(); it.hasNext();)
            {
                elem.removeChild((org.w3c.dom.Node) it.next());
            }
            return result;
        }
    }

    /**
     * A concrete <code>BuilderVisitor</code> that can construct XML
     * documents.
     */
    static class XMLBuilderVisitor extends BuilderVisitor
    {
        /** Stores the document to be constructed. */
        private Document document;

        /**
         * Creates a new instance of <code>XMLBuilderVisitor</code>
         * 
         * @param doc the document to be created
         */
        public XMLBuilderVisitor(Document doc)
        {
            document = doc;
        }

        /**
         * Processes the node hierarchy and adds new nodes to the document.
         * 
         * @param rootNode the root node
         */
        public void processDocument(Node rootNode)
        {
            rootNode.visit(this, null);
        }

        /**
         * @inheritDoc
         */
        protected Object insert(Node newNode, Node parent, Node sibling1, Node sibling2)
        {
            if (ConfigurationKey.isAttributeKey(newNode.getName()))
            {
                updateAttribute(parent, getElement(parent), newNode.getName());
                return null;
            }

            else
            {
                Element elem = document.createElement(newNode.getName());
                if (newNode.getValue() != null)
                {
                    elem.appendChild(document.createTextNode(newNode.getValue().toString()));
                }
                if (sibling2 == null)
                {
                    getElement(parent).appendChild(elem);
                }
                else if (sibling1 != null)
                {
                    getElement(parent).insertBefore(elem, getElement(sibling1).getNextSibling());
                }
                else
                {
                    getElement(parent).insertBefore(elem, getElement(parent).getFirstChild());
                }
                return elem;
            }
        }

        /**
         * Helper method for updating the value of the specified node's
         * attribute with the given name.
         * 
         * @param node the affected node
         * @param elem the element that is associated with this node
         * @param name the name of the affected attribute
         */
        private static void updateAttribute(Node node, Element elem, String name)
        {
            if (node != null && elem != null)
            {
                List attrs = node.getChildren(name);
                StringBuffer buf = new StringBuffer();
                for (Iterator it = attrs.iterator(); it.hasNext();)
                {
                    Node attr = (Node) it.next();
                    if (attr.getValue() != null)
                    {
                        if (buf.length() > 0)
                        {
                            buf.append(ATTR_DELIMITER);
                        }
                        buf.append(attr.getValue());
                    }
                    attr.setReference(elem);
                }

                if (buf.length() < 1)
                {
                    elem.removeAttribute(ConfigurationKey.removeAttributeMarkers(name));
                }
                else
                {
                    elem.setAttribute(ConfigurationKey.removeAttributeMarkers(name), buf.toString());
                }
            }
        }

        /**
         * Updates the value of the specified attribute of the given node.
         * Because there can be multiple child nodes representing this attribute
         * the new value is determined by iterating over all those child nodes.
         * 
         * @param node the affected node
         * @param name the name of the attribute
         */
        static void updateAttribute(Node node, String name)
        {
            if (node != null)
            {
                updateAttribute(node, (Element) node.getReference(), name);
            }
        }

        /**
         * Helper method for accessing the element of the specified node.
         * 
         * @param node the node
         * @return the element of this node
         */
        private Element getElement(Node node)
        {
            // special treatement for root node of the hierarchy
            return (node.getName() != null) ? (Element) node.getReference() : document.getDocumentElement();
        }
    }

    private class FileConfigurationDelegate extends AbstractFileConfiguration
    {
        public void load(Reader in) throws ConfigurationException
        {
            XMLConfiguration.this.load(in);
        }

        public void save(Writer out) throws ConfigurationException
        {
            XMLConfiguration.this.save(out);
        }
    }
}