/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

import org.apache.commons.collections.iterators.SingletonIterator;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A specialized hierarchical configuration class that is able to parse XML
 * documents.</p>
 *
 * <p>The parsed document will be stored keeping its structure. The class also
 * tries to preserve as much information from the loaded XML document as
 * possible, including comments and processing instructions. These will be
 * contained in documents created by the <code>save()</code> methods, too.</p>
 *
 * <p>Like other file based configuration classes this class maintains the name
 * and path to the loaded configuration file. These properties can be altered
 * using several setter methods, but they are not modified by <code>save()</code>
 * and <code>load()</code> methods. If XML documents contain relative paths to
 * other documents (e.g. to a DTD), these references are resolved based on the
 * path set for this configuration.</p>
 *
 * <p>By inheriting from <code>{@link AbstractConfiguration}</code> this class
 * provides some extended functionaly, e.g. interpolation of property values.
 * Like in <code>{@link PropertiesConfiguration}</code> property values can
 * contain delimiter characters (the comma ',' per default) and are then splitted
 * into multiple values. This works for XML attributes and text content of
 * elements as well. The delimiter can be escaped by a backslash. As an example
 * consider the following XML fragment:</p>
 *
 * <p>
 * <pre>
 * &lt;config&gt;
 *   &lt;array&gt;10,20,30,40&lt;/array&gt;
 *   &lt;scalar&gt;3\,1415&lt;/scalar&gt;
 *   &lt;cite text="To be or not to be\, this is the question!"/&gt;
 * &lt;/config&gt;
 * </pre>
 * </p>
 * <p>Here the content of the <code>array</code> element will be splitted at
 * the commas, so the <code>array</code> key will be assigned 4 values. In the
 * <code>scalar</code> property and the <code>text</code> attribute of the
 * <code>cite</code> element the comma is escaped, so that no splitting is
 * performed.</p>
 *
 * <p><code>XMLConfiguration</code> implements the <code>{@link FileConfiguration}</code>
 * interface and thus provides full support for loading XML documents from
 * different sources like files, URLs, or streams. A full description of these
 * features can be found in the documentation of
 * <code>{@link AbstractFileConfiguration}</code>.</p>
 *
 * @since commons-configuration 1.0
 *
 * @author J&ouml;rg Schaible
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger </a>
 * @version $Revision$, $Date$
 */
public class XMLConfiguration extends AbstractHierarchicalFileConfiguration
{
    /** Constant for the default root element name. */
    private static final String DEFAULT_ROOT_NAME = "configuration";

    /** The document from this configuration's data source. */
    private Document document;

    /** Stores the name of the root element. */
    private String rootElementName;

    /** Stores the public ID from the DOCTYPE.*/
    private String publicID;

    /** Stores the system ID from the DOCTYPE.*/
    private String systemID;

    /** Stores the document builder that should be used for loading.*/
    private DocumentBuilder documentBuilder;

    /** Stores a flag whether DTD validation should be performed.*/
    private boolean validating;

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
        super(fileName);
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
        super(file);
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
        super(url);
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
     * Returns the <code>DocumentBuilder</code> object that is used for
     * loading documents. If no specific builder has been set, this method
     * returns <b>null</b>.
     *
     * @return the <code>DocumentBuilder</code> for loading new documents
     * @since 1.2
     */
    public DocumentBuilder getDocumentBuilder()
    {
        return documentBuilder;
    }

    /**
     * Sets the <code>DocumentBuilder</code> object to be used for loading
     * documents. This method makes it possible to specify the exact document
     * builder. So an application can create a builder, configure it for its
     * special needs, and then pass it to this method.
     *
     * @param documentBuilder the document builder to be used; if undefined, a
     * default builder will be used
     * @since 1.2
     */
    public void setDocumentBuilder(DocumentBuilder documentBuilder)
    {
        this.documentBuilder = documentBuilder;
    }

    /**
     * Returns the public ID of the DOCTYPE declaration from the loaded XML
     * document. This is <b>null</b> if no document has been loaded yet or if
     * the document does not contain a DOCTYPE declaration with a public ID.
     *
     * @return the public ID
     * @since 1.3
     */
    public String getPublicID()
    {
        return publicID;
    }

    /**
     * Sets the public ID of the DOCTYPE declaration. When this configuration is
     * saved, a DOCTYPE declaration will be constructed that contains this
     * public ID.
     *
     * @param publicID the public ID
     * @since 1.3
     */
    public void setPublicID(String publicID)
    {
        this.publicID = publicID;
    }

    /**
     * Returns the system ID of the DOCTYPE declaration from the loaded XML
     * document. This is <b>null</b> if no document has been loaded yet or if
     * the document does not contain a DOCTYPE declaration with a system ID.
     *
     * @return the system ID
     * @since 1.3
     */
    public String getSystemID()
    {
        return systemID;
    }

    /**
     * Sets the system ID of the DOCTYPE declaration. When this configuration is
     * saved, a DOCTYPE declaration will be constructed that contains this
     * system ID.
     *
     * @param publicID the public ID
     * @since 1.3
     */
    public void setSystemID(String systemID)
    {
        this.systemID = systemID;
    }

    /**
     * Returns the value of the validating flag.
     *
     * @return the validating flag
     * @since 1.2
     */
    public boolean isValidating()
    {
        return validating;
    }

    /**
     * Sets the value of the validating flag. This flag determines whether
     * DTD validation should be performed when loading XML documents. This
     * flag is evaluated only if no custom <code>DocumentBuilder</code> was set.
     *
     * @param validating the validating flag
     * @since 1.2
     */
    public void setValidating(boolean validating)
    {
        this.validating = validating;
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
     * Removes all properties from this configuration. If this configuration
     * was loaded from a file, the associated DOM document is also cleared.
     */
    public void clear()
    {
        super.clear();
        document = null;
    }

    /**
     * Initializes this configuration from an XML document.
     *
     * @param document the document to be parsed
     * @param elemRefs a flag whether references to the XML elements should be set
     */
    public void initProperties(Document document, boolean elemRefs)
    {
        if (document.getDoctype() != null)
        {
            setPublicID(document.getDoctype().getPublicId());
            setSystemID(document.getDoctype().getSystemId());
        }
        constructHierarchy(getRoot(), document.getDocumentElement(), elemRefs);
    }

    /**
     * Helper method for building the internal storage hierarchy. The XML
     * elements are transformed into node objects.
     *
     * @param node the actual node
     * @param element the actual XML element
     * @param elemRefs a flag whether references to the XML elements should be set
     */
    private void constructHierarchy(Node node, Element element, boolean elemRefs)
    {
        processAttributes(node, element, elemRefs);
        StringBuffer buffer = new StringBuffer();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element)
            {
                Element child = (Element) w3cNode;
                Node childNode = new XMLNode(child.getTagName(),
                        elemRefs ? child : null);
                constructHierarchy(childNode, child, elemRefs);
                node.addChild(childNode);
                handleDelimiters(node, childNode);
            }
            else if (w3cNode instanceof Text)
            {
                Text data = (Text) w3cNode;
                buffer.append(data.getData());
            }
        }
        String text = buffer.toString().trim();
        if (text.length() > 0 || !node.hasChildren())
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
     * @param elemRefs a flag whether references to the XML elements should be set
     */
    private void processAttributes(Node node, Element element, boolean elemRefs)
    {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            org.w3c.dom.Node w3cNode = attributes.item(i);
            if (w3cNode instanceof Attr)
            {
                Attr attr = (Attr) w3cNode;
                Iterator it;
                if (isDelimiterParsingDisabled())
                {
                    it = new SingletonIterator(attr.getValue());
                }
                else
                {
                    it = PropertyConverter.split(attr.getValue(), getListDelimiter()).iterator();
                }
                while (it.hasNext())
                {
                    Node child = new XMLNode(attr.getName(),
                            elemRefs ? element : null);
                    child.setValue(it.next());
                    node.addAttribute(child);
                }
            }
        }
    }

    /**
     * Deals with elements whose value is a list. In this case multiple child
     * elements must be added.
     *
     * @param parent the parent element
     * @param child the child element
     */
    private void handleDelimiters(Node parent, Node child)
    {
        if (child.getValue() != null)
        {
            List values;
            if (isDelimiterParsingDisabled())
            {
                values = new ArrayList();
                values.add(child.getValue().toString());
            }
            else
            {
                values = PropertyConverter.split(child.getValue().toString(),
                    getListDelimiter());
            }

            if (values.size() > 1)
            {
                // remove the original child
                parent.remove(child);
                // add multiple new children
                for (Iterator it = values.iterator(); it.hasNext();)
                {
                    Node c = new XMLNode(child.getName(), null);
                    c.setValue(it.next());
                    parent.addChild(c);
                }
            }
            else if (values.size() == 1)
            {
                // we will have to replace the value because it might
                // contain escaped delimiters
                child.setValue(values.get(0));
            }
        }
    }

    /**
     * Creates the <code>DocumentBuilder</code> to be used for loading files.
     * This implementation checks whether a specific
     * <code>DocumentBuilder</code> has been set. If this is the case, this
     * one is used. Otherwise a default builder is created. Depending on the
     * value of the validating flag this builder will be a validating or a non
     * validating <code>DocumentBuilder</code>.
     *
     * @return the <code>DocumentBuilder</code> for loading configuration
     * files
     * @throws ParserConfigurationException if an error occurs
     * @since 1.2
     */
    protected DocumentBuilder createDocumentBuilder()
            throws ParserConfigurationException
    {
        if (getDocumentBuilder() != null)
        {
            return getDocumentBuilder();
        }
        else
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setValidating(isValidating());
            DocumentBuilder result = factory.newDocumentBuilder();

            if (isValidating())
            {
                // register an error handler which detects validation errors
                result.setErrorHandler(new DefaultHandler()
                {
                    public void error(SAXParseException ex) throws SAXException
                    {
                        throw ex;
                    }
                });
            }
            return result;
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

            XMLBuilderVisitor builder = new XMLBuilderVisitor(document, getListDelimiter());
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

    /**
     * Loads the configuration from the given input stream.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    public void load(InputStream in) throws ConfigurationException
    {
        load(new InputSource(in));
    }

    /**
     * Load the configuration from the given reader.
     * Note that the <code>clear()</code> method is not called, so
     * the properties contained in the loaded file will be added to the
     * actual set of properties.
     *
     * @param in An InputStream.
     *
     * @throws ConfigurationException if an error occurs
     */
    public void load(Reader in) throws ConfigurationException
    {
        load(new InputSource(in));
    }

    /**
     * Loads a configuration file from the specified input source.
     * @param source the input source
     * @throws ConfigurationException if an error occurs
     */
    private void load(InputSource source) throws ConfigurationException
    {
        try
        {
            URL sourceURL = getDelegate().getURL();
            if (sourceURL != null)
            {
                source.setSystemId(sourceURL.toString());
            }

            DocumentBuilder builder = createDocumentBuilder();
            Document newDocument = builder.parse(source);
            Document oldDocument = document;
            document = null;
            initProperties(newDocument, oldDocument == null);
            document = (oldDocument == null) ? newDocument : oldDocument;
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
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
            Transformer transformer = createTransformer();
            Source source = new DOMSource(createDocument());
            Result result = new StreamResult(writer);
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Creates and initializes the transformer used for save operations. This
     * base implementation initializes all of the default settings like
     * indention mode and the DOCTYPE. Derived classes may overload this method
     * if they have specific needs.
     *
     * @return the transformer to use for a save operation
     * @throws TransformerException if an error occurs
     * @since 1.3
     */
    protected Transformer createTransformer() throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        if (getEncoding() != null)
        {
            transformer.setOutputProperty(OutputKeys.ENCODING, getEncoding());
        }
        if (getPublicID() != null)
        {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                    getPublicID());
        }
        if (getSystemID() != null)
        {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    getSystemID());
        }

        return transformer;
    }

    /**
     * Creates a copy of this object. The new configuration object will contain
     * the same properties as the original, but it will lose any connection to a
     * source document (if one exists). This is to avoid race conditions if both
     * the original and the copy are modified and then saved.
     *
     * @return the copy
     */
    public Object clone()
    {
        XMLConfiguration copy = (XMLConfiguration) super.clone();

        // clear document related properties
        copy.document = null;
        copy.setDelegate(createDelegate());
        // clear all references in the nodes, too
        copy.getRoot().visit(new NodeVisitor()
        {
            public void visitBeforeChildren(Node node, ConfigurationKey key)
            {
                node.setReference(null);
            }
        }, null);

        return copy;
    }

    /**
     * Creates the file configuration delegate for this object. This implementation
     * will return an instance of a class derived from <code>FileConfigurationDelegate</code>
     * that deals with some specialities of <code>XMLConfiguration</code>.
     * @return the delegate for this object
     */
    protected FileConfigurationDelegate createDelegate()
    {
        return new XMLFileConfigurationDelegate();
    }

    /**
     * A specialized <code>Node</code> class that is connected with an XML
     * element. Changes on a node are also performed on the associated element.
     */
    class XMLNode extends Node
    {
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
                if (isAttribute())
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
                if (isAttribute())
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
                    txtNode = document
                            .createTextNode(PropertyConverter.escapeDelimiters(
                                    value.toString(), getListDelimiter()));
                    if (((Element) getReference()).getFirstChild() != null)
                    {
                        ((Element) getReference()).insertBefore(txtNode,
                                ((Element) getReference()).getFirstChild());
                    }
                    else
                    {
                        ((Element) getReference()).appendChild(txtNode);
                    }
                }
                else
                {
                    txtNode.setNodeValue(PropertyConverter.escapeDelimiters(
                            value.toString(), getListDelimiter()));
                }
            }
        }

        /**
         * Updates the node's value if it represents an attribute.
         *
         */
        private void updateAttribute()
        {
            XMLBuilderVisitor.updateAttribute(getParent(), getName(), getListDelimiter());
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

        /** Stores the list delimiter.*/
        private char listDelimiter = AbstractConfiguration.
                getDefaultListDelimiter();

        /**
         * Creates a new instance of <code>XMLBuilderVisitor</code>
         *
         * @param doc the document to be created
         * @param listDelimiter the delimiter for attribute properties with multiple values
         */
        public XMLBuilderVisitor(Document doc, char listDelimiter)
        {
            document = doc;
            this.listDelimiter = listDelimiter;
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
         * Inserts a new node. This implementation ensures that the correct
         * XML element is created and inserted between the given siblings.
         *
         * @param newNode the node to insert
         * @param parent the parent node
         * @param sibling1 the first sibling
         * @param sibling2 the second sibling
         * @return the new node
         */
        protected Object insert(Node newNode, Node parent, Node sibling1, Node sibling2)
        {
            if (newNode.isAttribute())
            {
                updateAttribute(parent, getElement(parent), newNode.getName(), listDelimiter);
                return null;
            }

            else
            {
                Element elem = document.createElement(newNode.getName());
                if (newNode.getValue() != null)
                {
                    elem.appendChild(document.createTextNode(
                            PropertyConverter.escapeDelimiters(newNode.getValue().toString(), listDelimiter)));
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
         * @param listDelimiter the delimiter vor attributes with multiple values
         */
        private static void updateAttribute(Node node, Element elem, String name, char listDelimiter)
        {
            if (node != null && elem != null)
            {
                List attrs = node.getAttributes(name);
                StringBuffer buf = new StringBuffer();
                for (Iterator it = attrs.iterator(); it.hasNext();)
                {
                    Node attr = (Node) it.next();
                    if (attr.getValue() != null)
                    {
                        if (buf.length() > 0)
                        {
                            buf.append(listDelimiter);
                        }
                        buf.append(PropertyConverter.escapeDelimiters(attr
                                .getValue().toString(), getDefaultListDelimiter()));
                    }
                    attr.setReference(elem);
                }

                if (buf.length() < 1)
                {
                    elem.removeAttribute(name);
                }
                else
                {
                    elem.setAttribute(name, buf.toString());
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
         * @param listDelimiter the delimiter vor attributes with multiple values
         */
        static void updateAttribute(Node node, String name, char listDelimiter)
        {
            if (node != null)
            {
                updateAttribute(node, (Element) node.getReference(), name, listDelimiter);
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

    /**
     * A special implementation of the <code>FileConfiguration</code> interface that is
     * used internally to implement the <code>FileConfiguration</code> methods
     * for <code>XMLConfiguration</code>, too.
     */
    private class XMLFileConfigurationDelegate extends FileConfigurationDelegate
    {
        public void load(InputStream in) throws ConfigurationException
        {
            XMLConfiguration.this.load(in);
        }
    }
}
