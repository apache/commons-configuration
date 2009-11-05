/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.base.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.base.Capability;
import org.apache.commons.configuration2.base.DefaultLocatorSupport;
import org.apache.commons.configuration2.base.InMemoryConfigurationSource;
import org.apache.commons.configuration2.base.LocatorSupport;
import org.apache.commons.configuration2.base.StreamBasedSource;
import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.resolver.DefaultEntityResolver;
import org.apache.commons.configuration2.resolver.EntityRegistry;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * A specialized hierarchical in-memory configuration source class that is able
 * to parse XML documents.
 * </p>
 * <p>
 * The parsed document will be stored keeping its structure. The class also
 * tries to preserve as much information from the loaded XML document as
 * possible, including comments and processing instructions. These will be
 * contained in documents created by the {@code save()} methods, too.
 * </p>
 * <p>
 * Whitespace in the content of XML documents is trimmed per default. In most
 * cases this is desired. However, sometimes whitespace is indeed important and
 * should be treated as part of the value of a property as in the following
 * example:
 *
 * <pre>
 *   &lt;indent&gt;    &lt;/indent&gt;
 * </pre>
 *
 * </p>
 * <p>
 * Per default the spaces in the {@code indent} element will be trimmed
 * resulting in an empty element. To tell {@code XMLConfigurationSource} that
 * spaces are relevant the {@code xml:space} attribute can be used, which is
 * defined in the <a href="http://www.w3.org/TR/REC-xml/#sec-white-space">XML
 * specification</a>. This will look as follows:
 *
 * <pre>
 *   &lt;indent xml:space=&quot;preserve&quot;&gt;    &lt;/indent&gt;
 * </pre>
 *
 * The value of the {@code indent} property will now contain the spaces.
 * </p>
 * <p>
 * {@code XMLConfigurationSource} implements the {@link LocatorSupport}
 * capability and thus provides full support for loading XML documents from
 * {@code Locator} objects. A full description of these features can be found in
 * the documentation of {@link LocatorSupport}.
 * </p>
 * <p>
 * <em>Note:</em>Configuration source objects of this type can be read
 * concurrently by multiple threads. However, if one of these threads modifies
 * the object, synchronization has to be performed manually.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class XMLConfigurationSource extends InMemoryConfigurationSource
        implements StreamBasedSource
{
    /** Constant for the default root element name. */
    private static final String DEFAULT_ROOT_NAME = "configuration";

    /** Constant for the name of the space attribute. */
    private static final String ATTR_SPACE = "xml:space";

    /** Constant for the xml:space value for preserving whitespace. */
    private static final String VALUE_PRESERVE = "preserve";

    /** Schema Language key for the parser */
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** Schema Language for the parser */
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    /** The node handler used by this instance. */
    private final NodeHandler<ConfigurationNode> xmlNodeHandler;

    /** The locator support object used by this configuration source. */
    private final LocatorSupport locatorSupport;

    /** The underlying XML document */
    private Document document;

    /** Stores the name of the root element. */
    private String rootElementName;

    /** Stores the public ID from the DOCTYPE. */
    private String publicID;

    /** Stores the system ID from the DOCTYPE. */
    private String systemID;

    /** Stores the document builder that should be used for loading. */
    private DocumentBuilder documentBuilder;

    /** Stores a flag whether DTD or Schema validation should be performed. */
    private boolean validating;

    /** Stores a flag whether DTD or Schema validation is used. */
    private boolean schemaValidation;

    /** The EntityResolver to use. */
    private EntityResolver entityResolver = new DefaultEntityResolver();

    /**
     * Creates a new instance of {@code XMLConfigurationSource}.
     */
    public XMLConfigurationSource()
    {
        xmlNodeHandler = new XMLNodeHandler();
        locatorSupport = new DefaultLocatorSupport(this);
    }

    /**
     * Returns the name of the root element. If this configuration source was
     * loaded from a XML document, the name of this document's root element is
     * returned. Otherwise, it is possible to set a name for the root element
     * that will be used when this configuration source is saved.
     *
     * @return the name of the root element
     */
    public String getRootElementName()
    {
        if (getDocument() == null)
        {
            return (rootElementName == null) ? DEFAULT_ROOT_NAME
                    : rootElementName;
        }
        else
        {
            return getDocument().getDocumentElement().getNodeName();
        }
    }

    /**
     * Sets the name of the root element. This name is used when this
     * configuration source object is stored in an XML file. Note that setting
     * the name of the root element works only if this configuration has been
     * newly created. If the configuration was loaded from an XML file, the name
     * cannot be changed and an {@code UnsupportedOperationException} exception
     * is thrown. Whether this configuration has been loaded from an XML
     * document or not can be found out using the {@code getDocument()} method.
     *
     * @param name the name of the root element
     */
    public void setRootElementName(String name)
    {
        if (getDocument() != null)
        {
            throw new UnsupportedOperationException(
                    "The name of the root element "
                            + "cannot be changed when loaded from an XML document!");
        }
        rootElementName = name;
        getRootNode().setName(name);
    }

    /**
     * Returns the {@code DocumentBuilder} object that is used for loading
     * documents. If no specific builder has been set, this method returns
     * <b>null</b>.
     *
     * @return the {@code DocumentBuilder} for loading new documents
     */
    public DocumentBuilder getDocumentBuilder()
    {
        return documentBuilder;
    }

    /**
     * Sets the {@code DocumentBuilder} object to be used for loading documents.
     * This method makes it possible to specify the exact document builder. So
     * an application can create a builder, configure it for its special needs,
     * and then pass it to this method.
     *
     * @param documentBuilder the {@code DocumentBuilder} to be used; if
     *        undefined, a default builder will be used
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
     * @param systemID the system ID
     */
    public void setSystemID(String systemID)
    {
        this.systemID = systemID;
    }

    /**
     * Returns the value of the validating flag.
     *
     * @return the validating flag
     */
    public boolean isValidating()
    {
        return validating;
    }

    /**
     * Sets the value of the validating flag. This flag determines whether
     * DTD/Schema validation should be performed when loading XML documents.
     * This flag is evaluated only if no custom {@code DocumentBuilder} was set.
     *
     * @param validating the validating flag
     */
    public void setValidating(boolean validating)
    {
        if (!schemaValidation)
        {
            this.validating = validating;
        }
    }

    /**
     * Returns the value of the schemaValidation flag.
     *
     * @return the schemaValidation flag
     */
    public boolean isSchemaValidation()
    {
        return schemaValidation;
    }

    /**
     * Sets the value of the schemaValidation flag. This flag determines whether
     * DTD or Schema validation should be used. This flag is evaluated only if
     * no custom {@code DocumentBuilder} was set. If set to true, the XML
     * document must contain a schemaLocation definition that provides
     * resolvable hints to the required schemas.
     *
     * @param schemaValidation the validating flag
     */
    public void setSchemaValidation(boolean schemaValidation)
    {
        this.schemaValidation = schemaValidation;
        if (schemaValidation)
        {
            this.validating = true;
        }
    }

    /**
     * Sets a new EntityResolver. Setting this will cause RegisterEntityId to
     * have no effect.
     *
     * @param resolver The EntityResolver to use.
     */
    public void setEntityResolver(EntityResolver resolver)
    {
        this.entityResolver = resolver;
    }

    /**
     * Returns the EntityResolver.
     *
     * @return The EntityResolver.
     */
    public EntityResolver getEntityResolver()
    {
        return this.entityResolver;
    }

    /**
     * <p>
     * Registers the specified DTD URL for the specified public identifier.
     * </p>
     * <p>
     * {@code XMLConfigurationSource} contains an internal {@code
     * EntityResolver} implementation. This maps {@code PUBLICID}'s to URLs
     * (from which the resource will be loaded). A common use case for this
     * method is to register local URLs (possibly computed at runtime by a class
     * loader) for DTDs. This allows the performance advantage of using a local
     * version without having to ensure every {@code SYSTEM} URI on every
     * processed XML document is local. This implementation provides only basic
     * functionality. If more sophisticated features are required, using
     * {@link #setDocumentBuilder(DocumentBuilder)} to set a custom {@code
     * DocumentBuilder} (which also can be initialized with a custom {@code
     * EntityResolver}) is recommended.
     * </p>
     * <p>
     * <strong>Note:</strong> This method will have no effect if a custom
     * {@code DocumentBuilder} has been set. (Setting a custom {@code
     * DocumentBuilder} overrides the internal implementation.)
     * </p>
     *
     * @param publicId Public identifier of the DTD to be resolved
     * @param entityURL The URL to use for reading this DTD
     * @throws IllegalArgumentException if the public ID is undefined
     * @since 1.5
     */
    public void registerEntityId(String publicId, URL entityURL)
    {
        if (entityResolver instanceof EntityRegistry)
        {
            ((EntityRegistry) entityResolver).registerEntityId(publicId,
                    entityURL);
        }
    }

    /**
     * Returns the XML document this configuration source was loaded from. The
     * return value is <b>null</b> if this configuration source was not loaded
     * from a XML document.
     *
     * @return the XML document this configuration was loaded from
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * Returns the {@code NodeHandler} used by this {@code
     * HierarchicalConfigurationSource}. This implementation returns a special
     * node handler for dealing with the XML nodes used internally.
     *
     * @return the {@code NodeHandler}
     */
    @Override
    public NodeHandler<ConfigurationNode> getNodeHandler()
    {
        return xmlNodeHandler;
    }

    /**
     * Removes all properties from this configuration source. If this
     * configuration source was loaded from a XML document, the associated DOM
     * document is also cleared.
     */
    @Override
    public void clear()
    {
        super.clear();
        document = null;
    }

    /**
     * Initializes this configuration source from an XML document. The elements
     * in the XML document are traversed and their data is added to the content
     * of this {@code ConfigurationSource}.
     *
     * @param document the document to be parsed
     * @param elemRefs a flag whether references to the XML elements should be
     *        set
     */
    public void initFromDocument(Document document, boolean elemRefs)
    {
        if (document.getDoctype() != null)
        {
            setPublicID(document.getDoctype().getPublicId());
            setSystemID(document.getDoctype().getSystemId());
        }

        constructHierarchy(getRootNode(), document.getDocumentElement(),
                elemRefs, true);
        getRootNode().setName(document.getDocumentElement().getNodeName());
        if (elemRefs)
        {
            getRootNode().setReference(document.getDocumentElement());
        }
    }

    /**
     * Loads the data from the specified {@code Reader} and adds it to this
     * {@code ConfigurationSource} object. Note that the{@code clear()} method
     * is not called, so the properties contained in the loaded file will be
     * added to the current set of properties.
     *
     * @param reader the reader to be read
     * @throws IOException if an I/O error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void load(Reader reader) throws IOException, ConfigurationException
    {
        load(new InputSource(reader));
    }

    /**
     * Writes the content of this configuration source to the specified {@code
     * Writer}. This implementation calls {@link #createTransformer()} for
     * obtaining a transformer. This transformer is then used to serialize the
     * internal document to XML.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void save(Writer writer) throws IOException, ConfigurationException
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
            throw new ConfigurationException(
                    "Unable to save the configuration", e);
        }
        catch (TransformerFactoryConfigurationError err)
        {
            throw new ConfigurationException(
                    "Unable to save the configuration", err);
        }
    }

    /**
     * Validate the document against the Schema.
     *
     * @throws ConfigurationException if the validation fails.
     */
    public void validate() throws ConfigurationException
    {
        try
        {
            Transformer transformer = createTransformer();
            Source source = new DOMSource(createDocument());
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            transformer.transform(source, result);
            Reader reader = new StringReader(writer.getBuffer().toString());
            DocumentBuilder builder = createDocumentBuilder();
            builder.parse(new InputSource(reader));
        }
        catch (SAXException e)
        {
            throw new ConfigurationException("Validation failed", e);
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Validation failed", e);
        }
        catch (TransformerException e)
        {
            throw new ConfigurationException("Validation failed", e);
        }
        catch (ParserConfigurationException pce)
        {
            throw new ConfigurationException("Validation failed", pce);
        }
    }

    /**
     * Creates the {@code DocumentBuilder} to be used for loading XML documents.
     * This implementation checks whether a specific {@code DocumentBuilder} has
     * been set. If this is the case, it is used directly. Otherwise a default
     * builder is created. Depending on the value of the validating flag this
     * builder will be a validating or a non validating {@code DocumentBuilder}.
     *
     * @return the {@code DocumentBuilder} for loading XML configuration files
     * @throws ParserConfigurationException if an error occurs
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
            if (isValidating())
            {
                factory.setValidating(true);
                if (isSchemaValidation())
                {
                    factory.setNamespaceAware(true);
                    factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                }
            }

            DocumentBuilder result = factory.newDocumentBuilder();
            result.setEntityResolver(this.entityResolver);

            if (isValidating())
            {
                // register an error handler which detects validation errors
                result.setErrorHandler(new DefaultHandler()
                {
                    @Override
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
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document newDocument = builder.newDocument();
                Element rootElem = newDocument
                        .createElement(getRootElementName());
                newDocument.appendChild(rootElem);
                document = newDocument;
            }

            XMLBuilderVisitor builder = new XMLBuilderVisitor(document);
            NodeVisitorAdapter.visit(builder, getRootNode(), getNodeHandler());
            initRootElementText(document, getRootNode().getValue());
            return document;
        }
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
     * Creates and initializes the transformer used for save operations. This
     * base implementation initializes all of the default settings like
     * indentation mode and the DOCTYPE. Derived classes may overload this
     * method if they have specific needs.
     *
     * @return the transformer to use for a save operation
     * @throws TransformerException if an error occurs
     */
    protected Transformer createTransformer() throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        if (locatorSupport.getEncoding() != null)
        {
            transformer.setOutputProperty(OutputKeys.ENCODING, locatorSupport
                    .getEncoding());
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
     * Defines additional capabilities used by this {@code ConfigurationSource}.
     * This implementation adds a {@code LocatorSupport} capability.
     *
     * @param caps the collection with additional capabilities
     */
    @Override
    protected void appendCapabilities(Collection<Capability> caps)
    {
        caps.add(new Capability(LocatorSupport.class, locatorSupport));
    }

    /**
     * Helper method for building the internal storage hierarchy. The XML
     * elements are transformed into node objects.
     *
     * @param node the actual node
     * @param element the actual XML element
     * @param elemRefs a flag whether references to the XML elements should be
     *        set
     * @param trim a flag whether the text content of elements should be
     *        trimmed; this controls the whitespace handling
     */
    private void constructHierarchy(ConfigurationNode node, Element element,
            boolean elemRefs, boolean trim)
    {
        boolean trimFlag = shouldTrim(element, trim);
        processAttributes(node, element, elemRefs);
        StringBuilder buffer = new StringBuilder();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element)
            {
                Element child = (Element) w3cNode;
                ConfigurationNode childNode = new XMLNode(null, child
                        .getTagName());
                if (elemRefs)
                {
                    childNode.setReference(child);
                }
                constructHierarchy(childNode, child, elemRefs, trimFlag);
                node.addChild(childNode);
            }
            else if (w3cNode instanceof Text)
            {
                Text data = (Text) w3cNode;
                buffer.append(data.getData());
            }
        }

        String text = buffer.toString();
        if (trimFlag)
        {
            text = text.trim();
        }
        if (text.length() > 0 || !hasChildren(node))
        {
            node.setValue(text);
        }
    }

    /**
     * Helper method for constructing node objects for the attributes of the
     * given XML element.
     *
     * @param node the current node
     * @param element the current XML element
     * @param elemRefs a flag whether references to the XML elements should be
     *        set
     */
    private void processAttributes(ConfigurationNode node, Element element,
            boolean elemRefs)
    {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i)
        {
            org.w3c.dom.Node w3cNode = attributes.item(i);
            if (w3cNode instanceof Attr)
            {
                Attr attr = (Attr) w3cNode;
                ConfigurationNode child = new XMLNode(null, attr.getName());
                child.setValue(attr.getValue());
                if (elemRefs)
                {
                    child.setReference(element);
                }
                node.addAttribute(child);
            }
        }
    }

    /**
     * Loads an XML document from the specified input source.
     *
     * @param source the input source
     * @throws ConfigurationException if an error occurs
     */
    private void load(InputSource source) throws IOException,
            ConfigurationException
    {
        try
        {
            if (locatorSupport.getLocator() != null)
            {
                source.setSystemId(locatorSupport.getLocator().getURL(false)
                        .toString());
            }

            DocumentBuilder builder = createDocumentBuilder();
            Document newDocument = builder.parse(source);
            Document oldDocument = document;
            document = null;
            initFromDocument(newDocument, oldDocument == null);
            document = (oldDocument == null) ? newDocument : oldDocument;
        }
        catch (SAXException spe)
        {
            throw new ConfigurationException("Error parsing "
                    + source.getSystemId(), spe);
        }
        catch (ParserConfigurationException pex)
        {
            throw new ConfigurationException(
                    "Error when creating document builder", pex);
        }
    }

    /**
     * Sets the text of the root element of a newly created XML Document.
     *
     * @param doc the document
     * @param value the new text to be set
     */
    private void initRootElementText(Document doc, Object value)
    {
        Element elem = doc.getDocumentElement();
        NodeList children = elem.getChildNodes();

        // Remove all existing text nodes
        for (int i = 0; i < children.getLength(); i++)
        {
            org.w3c.dom.Node nd = children.item(i);
            if (nd.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
            {
                elem.removeChild(nd);
            }
        }

        if (value != null)
        {
            // Add a new text node
            elem.appendChild(doc.createTextNode(String.valueOf(value)));
        }
    }

    /**
     * Tests whether the specified node has some child elements.
     *
     * @param node the node to check
     * @return a flag whether there are child elements
     */
    private static boolean hasChildren(ConfigurationNode node)
    {
        return node.getChildrenCount() > 0 || node.getAttributeCount() > 0;
    }

    /**
     * Checks whether the content of the current XML element should be trimmed.
     * This method checks whether a <code>xml:space</code> attribute is present
     * and evaluates its value. See <a
     * href="http://www.w3.org/TR/REC-xml/#sec-white-space">
     * http://www.w3.org/TR/REC-xml/#sec-white-space</a> for more details.
     *
     * @param element the current XML element
     * @param currentTrim the current trim flag
     * @return a flag whether the content of this element should be trimmed
     */
    private static boolean shouldTrim(Element element, boolean currentTrim)
    {
        Attr attr = element.getAttributeNode(ATTR_SPACE);

        if (attr == null)
        {
            return currentTrim;
        }
        else
        {
            return !VALUE_PRESERVE.equals(attr.getValue());
        }
    }

    /**
     * A specialized {@code ConfigurationNode} implementation class that is
     * connected with an XML element. Changes on a node are also performed on
     * the associated element.
     */
    private class XMLNode extends DefaultConfigurationNode
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = -4133988932174596562L;

        /**
         * Creates a new instance of {@code XMLNode}.
         *
         * @param parent the parent node
         * @param name the name of this node
         */
        public XMLNode(ConfigurationNode parent, String name)
        {
            super(name);
            setParentNode(parent);
        }

        /**
         * Sets the value of this node. If this node is associated with an XML
         * element, this element will be updated, too.
         *
         * @param value the node's new value
         */
        @Override
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
        @Override
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
                    String newValue = value.toString();
                    txtNode = document.createTextNode(newValue);
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
                    txtNode.setNodeValue(value.toString());
                }
            }
        }

        /**
         * Updates the node's value if it represents an attribute.
         */
        private void updateAttribute()
        {
            XMLBuilderVisitor.updateAttribute(getParentNode(), getName());
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
            Collection<org.w3c.dom.Node> textNodes = new ArrayList<org.w3c.dom.Node>();
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
            for (org.w3c.dom.Node child : textNodes)
            {
                elem.removeChild(child);
            }
            return result;
        }
    }

    /**
     * A specialized {@code NodeHandler} implementation for the nodes used by an
     * {@code XMLConfigurationSource}. This class differs from its base class in
     * the type of nodes that it creates: it uses the special node class {@code
     * XMLNode}.
     */
    private class XMLNodeHandler extends ConfigurationNodeHandler
    {
        @Override
        protected ConfigurationNode createNode(ConfigurationNode parent,
                String name)
        {
            return new XMLNode(parent, name);
        }
    }

    /**
     * A concrete {@code ConfigurationNodeBuilderVisitor} implementation that
     * can construct XML documents.
     */
    private static class XMLBuilderVisitor extends
            ConfigurationNodeBuilderVisitor
    {
        /** Stores the document to be constructed. */
        private Document document;

        /**
         * Creates a new instance of {@code XMLBuilderVisitor}.
         *
         * @param doc the document to be created
         */
        public XMLBuilderVisitor(Document doc)
        {
            document = doc;
        }

        /**
         * Inserts a new node. This implementation ensures that the correct XML
         * element is created and inserted between the given siblings.
         *
         * @param newNode the node to insert
         * @param parent the parent node
         * @param sibling1 the first sibling
         * @param sibling2 the second sibling
         * @return the new node
         */
        @Override
        protected Object insert(ConfigurationNode newNode,
                ConfigurationNode parent, ConfigurationNode sibling1,
                ConfigurationNode sibling2)
        {
            if (newNode.isAttribute())
            {
                updateAttribute(parent, getElement(parent), newNode.getName());
                return null;
            }

            else
            {
                Element elem = document.createElement(newNode.getName());
                if (newNode.getValue() != null)
                {
                    String txt = newNode.getValue().toString();
                    elem.appendChild(document.createTextNode(txt));
                }
                if (sibling2 == null)
                {
                    getElement(parent).appendChild(elem);
                }
                else if (sibling1 != null)
                {
                    getElement(parent).insertBefore(elem,
                            getElement(sibling1).getNextSibling());
                }
                else
                {
                    getElement(parent).insertBefore(elem,
                            getElement(parent).getFirstChild());
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
        private static void updateAttribute(ConfigurationNode node,
                Element elem, String name)
        {
            if (node != null && elem != null)
            {
                String attrValue = null;
                List<ConfigurationNode> attributes = node.getAttributes(name);
                if (attributes.size() > 0)
                {
                    ConfigurationNode attr = attributes.get(0);
                    if (attr.getValue() != null)
                    {
                        attrValue = attr.getValue().toString();
                    }
                }

                if (attrValue == null)
                {
                    elem.removeAttribute(name);
                }
                else
                {
                    elem.setAttribute(name, attrValue);
                }
            }
        }

        /**
         * Updates the value of the specified attribute of the given node.
         *
         * @param node the affected node
         * @param name the name of the attribute
         */
        static void updateAttribute(ConfigurationNode node, String name)
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
        private Element getElement(ConfigurationNode node)
        {
            // special treatment for root node of the hierarchy
            return (node.getName() != null && node.getReference() != null) ? (Element) node
                    .getReference()
                    : document.getDocumentElement();
        }
    }
}
