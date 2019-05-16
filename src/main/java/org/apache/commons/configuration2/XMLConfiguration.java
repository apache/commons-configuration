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

package org.apache.commons.configuration2;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorAware;
import org.apache.commons.configuration2.io.InputStreamSupport;
import org.apache.commons.configuration2.resolver.DefaultEntityResolver;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeTreeWalker;
import org.apache.commons.configuration2.tree.ReferenceNodeHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * A specialized hierarchical configuration class that is able to parse XML
 * documents.
 * </p>
 * <p>
 * The parsed document will be stored keeping its structure. The class also
 * tries to preserve as much information from the loaded XML document as
 * possible, including comments and processing instructions. These will be
 * contained in documents created by the {@code save()} methods, too.
 * </p>
 * <p>
 * Like other file based configuration classes this class maintains the name and
 * path to the loaded configuration file. These properties can be altered using
 * several setter methods, but they are not modified by {@code save()} and
 * {@code load()} methods. If XML documents contain relative paths to other
 * documents (e.g. to a DTD), these references are resolved based on the path
 * set for this configuration.
 * </p>
 * <p>
 * By inheriting from {@link AbstractConfiguration} this class provides some
 * extended functionality, e.g. interpolation of property values. Like in
 * {@link PropertiesConfiguration} property values can contain delimiter
 * characters (the comma ',' per default) and are then split into multiple
 * values. This works for XML attributes and text content of elements as well.
 * The delimiter can be escaped by a backslash. As an example consider the
 * following XML fragment:
 * </p>
 *
 * <pre>
 * &lt;config&gt;
 *   &lt;array&gt;10,20,30,40&lt;/array&gt;
 *   &lt;scalar&gt;3\,1415&lt;/scalar&gt;
 *   &lt;cite text="To be or not to be\, this is the question!"/&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * Here the content of the {@code array} element will be split at the commas, so
 * the {@code array} key will be assigned 4 values. In the {@code scalar}
 * property and the {@code text} attribute of the {@code cite} element the comma
 * is escaped, so that no splitting is performed.
 * </p>
 * <p>
 * The configuration API allows setting multiple values for a single attribute,
 * e.g. something like the following is legal (assuming that the default
 * expression engine is used):
 * </p>
 *
 * <pre>
 * XMLConfiguration config = new XMLConfiguration();
 * config.addProperty(&quot;test.dir[@name]&quot;, &quot;C:\\Temp\\&quot;);
 * config.addProperty(&quot;test.dir[@name]&quot;, &quot;D:\\Data\\&quot;);
 * </pre>
 *
 * <p>
 * However, in XML such a constellation is not supported; an attribute can
 * appear only once for a single element. Therefore, an attempt to save a
 * configuration which violates this condition will throw an exception.
 * </p>
 * <p>
 * Like other {@code Configuration} implementations, {@code XMLConfiguration}
 * uses a {@link ListDelimiterHandler} object for controlling list split
 * operations. Per default, a list delimiter handler object is set which
 * disables this feature. XML has a built-in support for complex structures
 * including list properties; therefore, list splitting is not that relevant for
 * this configuration type. Nevertheless, by setting an alternative
 * {@code ListDelimiterHandler} implementation, this feature can be enabled. It
 * works as for any other concrete {@code Configuration} implementation.
 * </p>
 * <p>
 * Whitespace in the content of XML documents is trimmed per default. In most
 * cases this is desired. However, sometimes whitespace is indeed important and
 * should be treated as part of the value of a property as in the following
 * example:
 * </p>
 * <pre>
 *   &lt;indent&gt;    &lt;/indent&gt;
 * </pre>
 *
 * <p>
 * Per default the spaces in the {@code indent} element will be trimmed
 * resulting in an empty element. To tell {@code XMLConfiguration} that spaces
 * are relevant the {@code xml:space} attribute can be used, which is defined in
 * the <a href="http://www.w3.org/TR/REC-xml/#sec-white-space">XML
 * specification</a>. This will look as follows:
 * </p>
 * <pre>
 *   &lt;indent <strong>xml:space=&quot;preserve&quot;</strong>&gt;    &lt;/indent&gt;
 * </pre>
 *
 * <p>
 * The value of the {@code indent} property will now contain the spaces.
 * </p>
 * <p>
 * {@code XMLConfiguration} implements the {@link FileBasedConfiguration}
 * interface and thus can be used together with a file-based builder to load XML
 * configuration files from various sources like files, URLs, or streams.
 * </p>
 * <p>
 * Like other {@code Configuration} implementations, this class uses a
 * {@code Synchronizer} object to control concurrent access. By choosing a
 * suitable implementation of the {@code Synchronizer} interface, an instance
 * can be made thread-safe or not. Note that access to most of the properties
 * typically set through a builder is not protected by the {@code Synchronizer}.
 * The intended usage is that these properties are set once at construction time
 * through the builder and after that remain constant. If you wish to change
 * such properties during life time of an instance, you have to use the
 * {@code lock()} and {@code unlock()} methods manually to ensure that other
 * threads see your changes.
 * </p>
 * <p>
 * More information about the basic functionality supported by
 * {@code XMLConfiguration} can be found at the user's guide at
 * <a href="http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html">
 * Basic features and AbstractConfiguration</a>. There is
 * also a separate chapter dealing with
 * <a href="commons.apache.org/proper/commons-configuration/userguide/howto_xml.html">
 * XML Configurations</a> in special.
 * </p>
 *
 * @since commons-configuration 1.0
 * @author J&ouml;rg Schaible
 */
public class XMLConfiguration extends BaseHierarchicalConfiguration implements
        FileBasedConfiguration, FileLocatorAware, InputStreamSupport
{
    /** Constant for the default root element name. */
    private static final String DEFAULT_ROOT_NAME = "configuration";

    /** Constant for the name of the space attribute.*/
    private static final String ATTR_SPACE = "xml:space";

    /** Constant for an internally used space attribute. */
    private static final String ATTR_SPACE_INTERNAL = "config-xml:space";

    /** Constant for the xml:space value for preserving whitespace.*/
    private static final String VALUE_PRESERVE = "preserve";

    /** Schema Langauge key for the parser */
    private static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** Schema Language for the parser */
    private static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    /** Stores the name of the root element. */
    private String rootElementName;

    /** Stores the public ID from the DOCTYPE.*/
    private String publicID;

    /** Stores the system ID from the DOCTYPE.*/
    private String systemID;

    /** Stores the document builder that should be used for loading.*/
    private DocumentBuilder documentBuilder;

    /** Stores a flag whether DTD or Schema validation should be performed.*/
    private boolean validating;

    /** Stores a flag whether DTD or Schema validation is used */
    private boolean schemaValidation;

    /** The EntityResolver to use */
    private EntityResolver entityResolver = new DefaultEntityResolver();

    /** The current file locator. */
    private FileLocator locator;

    /**
     * Creates a new instance of {@code XMLConfiguration}.
     */
    public XMLConfiguration()
    {
        super();
        initLogger(new ConfigurationLogger(XMLConfiguration.class));
    }

    /**
     * Creates a new instance of {@code XMLConfiguration} and copies the
     * content of the passed in configuration into this object. Note that only
     * the data of the passed in configuration will be copied. If, for instance,
     * the other configuration is a {@code XMLConfiguration}, too,
     * things like comments or processing instructions will be lost.
     *
     * @param c the configuration to copy
     * @since 1.4
     */
    public XMLConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
        rootElementName =
                (c != null) ? c.getRootElementName() : null;
        initLogger(new ConfigurationLogger(XMLConfiguration.class));
    }

    /**
     * Returns the name of the root element. If this configuration was loaded
     * from a XML document, the name of this document's root element is
     * returned. Otherwise it is possible to set a name for the root element
     * that will be used when this configuration is stored.
     *
     * @return the name of the root element
     */
    @Override
    protected String getRootElementNameInternal()
    {
        final Document doc = getDocument();
        if (doc == null)
        {
            return (rootElementName == null) ? DEFAULT_ROOT_NAME : rootElementName;
        }
        return doc.getDocumentElement().getNodeName();
    }

    /**
     * Sets the name of the root element. This name is used when this
     * configuration object is stored in an XML file. Note that setting the name
     * of the root element works only if this configuration has been newly
     * created. If the configuration was loaded from an XML file, the name
     * cannot be changed and an {@code UnsupportedOperationException}
     * exception is thrown. Whether this configuration has been loaded from an
     * XML document or not can be found out using the {@code getDocument()}
     * method.
     *
     * @param name the name of the root element
     */
    public void setRootElementName(final String name)
    {
        beginRead(true);
        try
        {
            if (getDocument() != null)
            {
                throw new UnsupportedOperationException(
                        "The name of the root element "
                                + "cannot be changed when loaded from an XML document!");
            }
            rootElementName = name;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns the {@code DocumentBuilder} object that is used for
     * loading documents. If no specific builder has been set, this method
     * returns <b>null</b>.
     *
     * @return the {@code DocumentBuilder} for loading new documents
     * @since 1.2
     */
    public DocumentBuilder getDocumentBuilder()
    {
        return documentBuilder;
    }

    /**
     * Sets the {@code DocumentBuilder} object to be used for loading
     * documents. This method makes it possible to specify the exact document
     * builder. So an application can create a builder, configure it for its
     * special needs, and then pass it to this method.
     *
     * @param documentBuilder the document builder to be used; if undefined, a
     * default builder will be used
     * @since 1.2
     */
    public void setDocumentBuilder(final DocumentBuilder documentBuilder)
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
        beginRead(false);
        try
        {
            return publicID;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the public ID of the DOCTYPE declaration. When this configuration is
     * saved, a DOCTYPE declaration will be constructed that contains this
     * public ID.
     *
     * @param publicID the public ID
     * @since 1.3
     */
    public void setPublicID(final String publicID)
    {
        beginWrite(false);
        try
        {
            this.publicID = publicID;
        }
        finally
        {
            endWrite();
        }
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
        beginRead(false);
        try
        {
            return systemID;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the system ID of the DOCTYPE declaration. When this configuration is
     * saved, a DOCTYPE declaration will be constructed that contains this
     * system ID.
     *
     * @param systemID the system ID
     * @since 1.3
     */
    public void setSystemID(final String systemID)
    {
        beginWrite(false);
        try
        {
            this.systemID = systemID;
        }
        finally
        {
            endWrite();
        }
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
     * DTD/Schema validation should be performed when loading XML documents. This
     * flag is evaluated only if no custom {@code DocumentBuilder} was set.
     *
     * @param validating the validating flag
     * @since 1.2
     */
    public void setValidating(final boolean validating)
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
     * @since 1.7
     */
    public boolean isSchemaValidation()
    {
        return schemaValidation;
    }

    /**
     * Sets the value of the schemaValidation flag. This flag determines whether
     * DTD or Schema validation should be used. This
     * flag is evaluated only if no custom {@code DocumentBuilder} was set.
     * If set to true the XML document must contain a schemaLocation definition
     * that provides resolvable hints to the required schemas.
     *
     * @param schemaValidation the validating flag
     * @since 1.7
     */
    public void setSchemaValidation(final boolean schemaValidation)
    {
        this.schemaValidation = schemaValidation;
        if (schemaValidation)
        {
            this.validating = true;
        }
    }

    /**
     * Sets a new EntityResolver. Setting this will cause RegisterEntityId to have no
     * effect.
     * @param resolver The EntityResolver to use.
     * @since 1.7
     */
    public void setEntityResolver(final EntityResolver resolver)
    {
        this.entityResolver = resolver;
    }

    /**
     * Returns the EntityResolver.
     * @return The EntityResolver.
     * @since 1.7
     */
    public EntityResolver getEntityResolver()
    {
        return this.entityResolver;
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
        final XMLDocumentHelper docHelper = getDocumentHelper();
        return (docHelper != null) ? docHelper.getDocument() : null;
    }

    /**
     * Returns the helper object for managing the underlying document.
     *
     * @return the {@code XMLDocumentHelper}
     */
    private XMLDocumentHelper getDocumentHelper()
    {
        final ReferenceNodeHandler handler = getReferenceHandler();
        return (XMLDocumentHelper) handler.getReference(handler.getRootNode());
    }

    /**
     * Returns the extended node handler with support for references.
     *
     * @return the {@code ReferenceNodeHandler}
     */
    private ReferenceNodeHandler getReferenceHandler()
    {
        return getSubConfigurationParentModel().getReferenceNodeHandler();
    }

    /**
     * Initializes this configuration from an XML document.
     *
     * @param docHelper the helper object with the document to be parsed
     * @param elemRefs a flag whether references to the XML elements should be set
     */
    private void initProperties(final XMLDocumentHelper docHelper, final boolean elemRefs)
    {
        final Document document = docHelper.getDocument();
        setPublicID(docHelper.getSourcePublicID());
        setSystemID(docHelper.getSourceSystemID());

        final ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        final MutableObject<String> rootValue = new MutableObject<>();
        final Map<ImmutableNode, Object> elemRefMap =
                elemRefs ? new HashMap<>() : null;
        final Map<String, String> attributes =
                constructHierarchy(rootBuilder, rootValue,
                        document.getDocumentElement(), elemRefMap, true, 0);
        attributes.remove(ATTR_SPACE_INTERNAL);
        final ImmutableNode top =
                rootBuilder.value(rootValue.getValue())
                        .addAttributes(attributes).create();
        getSubConfigurationParentModel().mergeRoot(top,
                document.getDocumentElement().getTagName(), elemRefMap,
                elemRefs ? docHelper : null, this);
    }

    /**
     * Helper method for building the internal storage hierarchy. The XML
     * elements are transformed into node objects.
     *
     * @param node a builder for the current node
     * @param refValue stores the text value of the element
     * @param element the current XML element
     * @param elemRefs a map for assigning references objects to nodes; can be
     *        <b>null</b>, then reference objects are irrelevant
     * @param trim a flag whether the text content of elements should be
     *        trimmed; this controls the whitespace handling
     * @param level the current level in the hierarchy
     * @return a map with all attribute values extracted for the current node;
     *         this map also contains the value of the trim flag for this node
     *         under the key {@value #ATTR_SPACE}
     */
    private Map<String, String> constructHierarchy(final ImmutableNode.Builder node,
            final MutableObject<String> refValue, final Element element,
            final Map<ImmutableNode, Object> elemRefs, final boolean trim, final int level)
    {
        final boolean trimFlag = shouldTrim(element, trim);
        final Map<String, String> attributes = processAttributes(element);
        attributes.put(ATTR_SPACE_INTERNAL, String.valueOf(trimFlag));
        final StringBuilder buffer = new StringBuilder();
        final NodeList list = element.getChildNodes();
        boolean hasChildren = false;

        for (int i = 0; i < list.getLength(); i++)
        {
            final org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element)
            {
                final Element child = (Element) w3cNode;
                final ImmutableNode.Builder childNode = new ImmutableNode.Builder();
                childNode.name(child.getTagName());
                final MutableObject<String> refChildValue =
                        new MutableObject<>();
                final Map<String, String> attrmap =
                        constructHierarchy(childNode, refChildValue, child,
                                elemRefs, trimFlag, level + 1);
                final Boolean childTrim = Boolean.valueOf(attrmap.remove(ATTR_SPACE_INTERNAL));
                childNode.addAttributes(attrmap);
                final ImmutableNode newChild =
                        createChildNodeWithValue(node, childNode, child,
                                refChildValue.getValue(),
                                childTrim.booleanValue(), attrmap, elemRefs);
                if (elemRefs != null && !elemRefs.containsKey(newChild))
                {
                    elemRefs.put(newChild, child);
                }
                hasChildren = true;
            }
            else if (w3cNode instanceof Text)
            {
                final Text data = (Text) w3cNode;
                buffer.append(data.getData());
            }
        }

        boolean childrenFlag = false;
        if (hasChildren || trimFlag)
        {
            childrenFlag = hasChildren || attributes.size() > 1;
        }
        final String text = determineValue(buffer.toString(), childrenFlag, trimFlag);
        if (text.length() > 0 || (!childrenFlag && level != 0))
        {
            refValue.setValue(text);
        }
        return attributes;
    }

    /**
     * Determines the value of a configuration node. This method mainly checks
     * whether the text value is to be trimmed or not. This is normally defined
     * by the trim flag. However, if the node has children and its content is
     * only whitespace, then it makes no sense to store any value; this would
     * only scramble layout when the configuration is saved again.
     *
     * @param content the text content of this node
     * @param hasChildren a flag whether the node has children
     * @param trimFlag the trim flag
     * @return the value to be stored for this node
     */
    private static String determineValue(final String content, final boolean hasChildren,
            final boolean trimFlag)
    {
        final boolean shouldTrim =
                trimFlag || (StringUtils.isBlank(content) && hasChildren);
        return shouldTrim ? content.trim() : content;
    }

    /**
     * Helper method for initializing the attributes of a configuration node
     * from the given XML element.
     *
     * @param element the current XML element
     * @return a map with all attribute values extracted for the current node
     */
    private static Map<String, String> processAttributes(final Element element)
    {
        final NamedNodeMap attributes = element.getAttributes();
        final Map<String, String> attrmap = new HashMap<>();

        for (int i = 0; i < attributes.getLength(); ++i)
        {
            final org.w3c.dom.Node w3cNode = attributes.item(i);
            if (w3cNode instanceof Attr)
            {
                final Attr attr = (Attr) w3cNode;
                attrmap.put(attr.getName(), attr.getValue());
            }
        }

        return attrmap;
    }

    /**
     * Creates a new child node, assigns its value, and adds it to its parent.
     * This method also deals with elements whose value is a list. In this case
     * multiple child elements must be added. The return value is the first
     * child node which was added.
     *
     * @param parent the builder for the parent element
     * @param child the builder for the child element
     * @param elem the associated XML element
     * @param value the value of the child element
     * @param trim flag whether texts of elements should be trimmed
     * @param attrmap a map with the attributes of the current node
     * @param elemRefs a map for assigning references objects to nodes; can be
     *        <b>null</b>, then reference objects are irrelevant
     * @return the first child node added to the parent
     */
    private ImmutableNode createChildNodeWithValue(final ImmutableNode.Builder parent,
            final ImmutableNode.Builder child, final Element elem, final String value,
            final boolean trim, final Map<String, String> attrmap,
            final Map<ImmutableNode, Object> elemRefs)
    {
        ImmutableNode addedChildNode;
        Collection<String> values;

        if (value != null)
        {
            values = getListDelimiterHandler().split(value, trim);
        }
        else
        {
            values = Collections.emptyList();
        }

        if (values.size() > 1)
        {
            final Map<ImmutableNode, Object> refs = isSingleElementList(elem) ? elemRefs : null;
            final Iterator<String> it = values.iterator();
            // Create new node for the original child's first value
            child.value(it.next());
            addedChildNode = child.create();
            parent.addChild(addedChildNode);
            XMLListReference.assignListReference(refs, addedChildNode, elem);

            // add multiple new children
            while (it.hasNext())
            {
                final ImmutableNode.Builder c = new ImmutableNode.Builder();
                c.name(addedChildNode.getNodeName());
                c.value(it.next());
                c.addAttributes(attrmap);
                final ImmutableNode newChild = c.create();
                parent.addChild(newChild);
                XMLListReference.assignListReference(refs, newChild, null);
            }
        }
        else if (values.size() == 1)
        {
            // we will have to replace the value because it might
            // contain escaped delimiters
            child.value(values.iterator().next());
            addedChildNode = child.create();
            parent.addChild(addedChildNode);
        }
        else
        {
            addedChildNode = child.create();
            parent.addChild(addedChildNode);
        }

        return addedChildNode;
    }

    /**
     * Checks whether an element defines a complete list. If this is the case,
     * extended list handling can be applied.
     *
     * @param element the element to be checked
     * @return a flag whether this is the only element defining the list
     */
    private static boolean isSingleElementList(final Element element)
    {
        final Node parentNode = element.getParentNode();
        return countChildElements(parentNode, element.getTagName()) == 1;
    }

    /**
     * Determines the number of child elements of this given node with the
     * specified node name.
     *
     * @param parent the parent node
     * @param name the name in question
     * @return the number of child elements with this name
     */
    private static int countChildElements(final Node parent, final String name)
    {
        final NodeList childNodes = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node item = childNodes.item(i);
            if (item instanceof Element)
            {
                if (name.equals(((Element) item).getTagName()))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks whether the content of the current XML element should be trimmed.
     * This method checks whether a {@code xml:space} attribute is
     * present and evaluates its value. See <a
     * href="http://www.w3.org/TR/REC-xml/#sec-white-space">
     * http://www.w3.org/TR/REC-xml/#sec-white-space</a> for more details.
     *
     * @param element the current XML element
     * @param currentTrim the current trim flag
     * @return a flag whether the content of this element should be trimmed
     */
    private static boolean shouldTrim(final Element element, final boolean currentTrim)
    {
        final Attr attr = element.getAttributeNode(ATTR_SPACE);

        if (attr == null)
        {
            return currentTrim;
        }
        return !VALUE_PRESERVE.equals(attr.getValue());
    }

    /**
     * Creates the {@code DocumentBuilder} to be used for loading files.
     * This implementation checks whether a specific
     * {@code DocumentBuilder} has been set. If this is the case, this
     * one is used. Otherwise a default builder is created. Depending on the
     * value of the validating flag this builder will be a validating or a non
     * validating {@code DocumentBuilder}.
     *
     * @return the {@code DocumentBuilder} for loading configuration
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
        final DocumentBuilderFactory factory = DocumentBuilderFactory
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

        final DocumentBuilder result = factory.newDocumentBuilder();
        result.setEntityResolver(this.entityResolver);

        if (isValidating())
        {
            // register an error handler which detects validation errors
            result.setErrorHandler(new DefaultHandler()
            {
                @Override
                public void error(final SAXParseException ex) throws SAXException
                {
                    throw ex;
                }
            });
        }
        return result;
    }

    /**
     * Creates and initializes the transformer used for save operations. This
     * base implementation initializes all of the default settings like
     * indention mode and the DOCTYPE. Derived classes may overload this method
     * if they have specific needs.
     *
     * @return the transformer to use for a save operation
     * @throws ConfigurationException if an error occurs
     * @since 1.3
     */
    protected Transformer createTransformer() throws ConfigurationException
    {
        final Transformer transformer = XMLDocumentHelper.createTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        if (locator.getEncoding() != null)
        {
            transformer.setOutputProperty(OutputKeys.ENCODING, locator.getEncoding());
        }
        if (publicID != null)
        {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                    publicID);
        }
        if (systemID != null)
        {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    systemID);
        }

        return transformer;
    }

    /**
     * Creates a DOM document from the internal tree of configuration nodes.
     *
     * @return the new document
     * @throws ConfigurationException if an error occurs
     */
    private Document createDocument() throws ConfigurationException
    {
        final ReferenceNodeHandler handler = getReferenceHandler();
        final XMLDocumentHelper docHelper =
                (XMLDocumentHelper) handler.getReference(handler.getRootNode());
        final XMLDocumentHelper newHelper =
                (docHelper == null) ? XMLDocumentHelper
                        .forNewDocument(getRootElementName()) : docHelper
                        .createCopy();

        final XMLBuilderVisitor builder =
                new XMLBuilderVisitor(newHelper, getListDelimiterHandler());
        builder.handleRemovedNodes(handler);
        builder.processDocument(handler);
        initRootElementText(newHelper.getDocument(), getModel()
                .getNodeHandler().getRootNode().getValue());
        return newHelper.getDocument();
    }

    /**
     * Sets the text of the root element of a newly created XML Document.
     *
     * @param doc the document
     * @param value the new text to be set
     */
    private void initRootElementText(final Document doc, final Object value)
    {
        final Element elem = doc.getDocumentElement();
        final NodeList children = elem.getChildNodes();

        // Remove all existing text nodes
        for (int i = 0; i < children.getLength(); i++)
        {
            final org.w3c.dom.Node nd = children.item(i);
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
     * {@inheritDoc} Stores the passed in locator for the upcoming IO operation.
     */
    @Override
    public void initFileLocator(final FileLocator loc)
    {
        locator = loc;
    }

    /**
     * Loads the configuration from the given reader.
     * Note that the {@code clear()} method is not called, so
     * the properties contained in the loaded file will be added to the
     * current set of properties.
     *
     * @param in the reader
     * @throws ConfigurationException if an error occurs
     * @throws IOException if an IO error occurs
     */
    @Override
    public void read(final Reader in) throws ConfigurationException, IOException
    {
        load(new InputSource(in));
    }

    /**
     * Loads the configuration from the given input stream. This is analogous to
     * {@link #read(Reader)}, but data is read from a stream. Note that this
     * method will be called most time when reading an XML configuration source.
     * By reading XML documents directly from an input stream, the file's
     * encoding can be correctly dealt with.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     * @throws IOException if an IO error occurs
     */
    @Override
    public void read(final InputStream in) throws ConfigurationException, IOException
    {
        load(new InputSource(in));
    }

    /**
     * Loads a configuration file from the specified input source.
     *
     * @param source the input source
     * @throws ConfigurationException if an error occurs
     */
    private void load(final InputSource source) throws ConfigurationException
    {
        if (locator == null)
        {
            throw new ConfigurationException("Load operation not properly "
                    + "initialized! Do not call read(InputStream) directly,"
                    + " but use a FileHandler to load a configuration.");
        }

        try
        {
            final URL sourceURL = locator.getSourceURL();
            if (sourceURL != null)
            {
                source.setSystemId(sourceURL.toString());
            }

            final DocumentBuilder builder = createDocumentBuilder();
            final Document newDocument = builder.parse(source);
            final Document oldDocument = getDocument();
            initProperties(XMLDocumentHelper.forSourceDocument(newDocument),
                    oldDocument == null);
        }
        catch (final SAXParseException spe)
        {
            throw new ConfigurationException("Error parsing " + source.getSystemId(), spe);
        }
        catch (final Exception e)
        {
            this.getLogger().debug("Unable to load the configuration: " + e);
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }

    /**
     * Saves the configuration to the specified writer.
     *
     * @param writer the writer used to save the configuration
     * @throws ConfigurationException if an error occurs
     * @throws IOException if an IO error occurs
     */
    @Override
    public void write(final Writer writer) throws ConfigurationException, IOException
    {
        final Transformer transformer = createTransformer();
        final Source source = new DOMSource(createDocument());
        final Result result = new StreamResult(writer);
        XMLDocumentHelper.transform(transformer, source, result);
    }

    /**
     * Validate the document against the Schema.
     * @throws ConfigurationException if the validation fails.
     */
    public void validate() throws ConfigurationException
    {
        beginWrite(false);
        try
        {
            final Transformer transformer = createTransformer();
            final Source source = new DOMSource(createDocument());
            final StringWriter writer = new StringWriter();
            final Result result = new StreamResult(writer);
            XMLDocumentHelper.transform(transformer, source, result);
            final Reader reader = new StringReader(writer.getBuffer().toString());
            final DocumentBuilder builder = createDocumentBuilder();
            builder.parse(new InputSource(reader));
        }
        catch (final SAXException e)
        {
            throw new ConfigurationException("Validation failed", e);
        }
        catch (final IOException e)
        {
            throw new ConfigurationException("Validation failed", e);
        }
        catch (final ParserConfigurationException pce)
        {
            throw new ConfigurationException("Validation failed", pce);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * A concrete {@code BuilderVisitor} that can construct XML
     * documents.
     */
    static class XMLBuilderVisitor extends BuilderVisitor
    {
        /** Stores the document to be constructed. */
        private final Document document;

        /** The element mapping. */
        private final Map<Node, Node> elementMapping;

        /** A mapping for the references for new nodes. */
        private final Map<ImmutableNode, Element> newElements;

        /** Stores the list delimiter handler .*/
        private final ListDelimiterHandler listDelimiterHandler;

        /**
         * Creates a new instance of {@code XMLBuilderVisitor}.
         *
         * @param docHelper the document helper
         * @param handler the delimiter handler for properties with multiple
         *        values
         */
        public XMLBuilderVisitor(final XMLDocumentHelper docHelper,
                final ListDelimiterHandler handler)
        {
            document = docHelper.getDocument();
            elementMapping = docHelper.getElementMapping();
            listDelimiterHandler = handler;
            newElements = new HashMap<>();
        }

        /**
         * Processes the specified document, updates element values, and adds
         * new nodes to the hierarchy.
         *
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        public void processDocument(final ReferenceNodeHandler refHandler)
        {
            updateAttributes(refHandler.getRootNode(), document.getDocumentElement());
            NodeTreeWalker.INSTANCE.walkDFS(refHandler.getRootNode(), this,
                    refHandler);
        }

        /**
         * Updates the current XML document regarding removed nodes. The
         * elements associated with removed nodes are removed from the document.
         *
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        public void handleRemovedNodes(final ReferenceNodeHandler refHandler)
        {
            for (final Object ref : refHandler.removedReferences())
            {
                if (ref instanceof Node)
                {
                    final Node removedElem = (Node) ref;
                    removeReference((Element) elementMapping.get(removedElem));
                }
            }
        }

        /**
         * {@inheritDoc} This implementation ensures that the correct XML
         * element is created and inserted between the given siblings.
         */
        @Override
        protected void insert(final ImmutableNode newNode, final ImmutableNode parent,
                final ImmutableNode sibling1, final ImmutableNode sibling2,
                final ReferenceNodeHandler refHandler)
        {
            if (XMLListReference.isListNode(newNode, refHandler))
            {
                return;
            }

            final Element elem = document.createElement(newNode.getNodeName());
            newElements.put(newNode, elem);
            updateAttributes(newNode, elem);
            if (newNode.getValue() != null)
            {
                final String txt =
                        String.valueOf(listDelimiterHandler.escape(
                                newNode.getValue(),
                                ListDelimiterHandler.NOOP_TRANSFORMER));
                elem.appendChild(document.createTextNode(txt));
            }
            if (sibling2 == null)
            {
                getElement(parent, refHandler).appendChild(elem);
            }
            else if (sibling1 != null)
            {
                getElement(parent, refHandler).insertBefore(elem,
                        getElement(sibling1, refHandler).getNextSibling());
            }
            else
            {
                getElement(parent, refHandler).insertBefore(elem,
                        getElement(parent, refHandler).getFirstChild());
            }
        }

        /**
         * {@inheritDoc} This implementation determines the XML element
         * associated with the given node. Then this element's value and
         * attributes are set accordingly.
         */
        @Override
        protected void update(final ImmutableNode node, final Object reference,
                final ReferenceNodeHandler refHandler)
        {
            if (XMLListReference.isListNode(node, refHandler))
            {
                if (XMLListReference.isFirstListItem(node, refHandler))
                {
                    final String value = XMLListReference.listValue(node, refHandler, listDelimiterHandler);
                    updateElement(node, refHandler, value);
                }
            }
            else
            {
                final Object value = listDelimiterHandler.escape(refHandler.getValue(node),
                        ListDelimiterHandler.NOOP_TRANSFORMER);
                updateElement(node, refHandler, value);
            }
        }

        private void updateElement(final ImmutableNode node, final ReferenceNodeHandler refHandler,
                                   final Object value)
        {
            final Element element = getElement(node, refHandler);
            updateElement(element, value);
            updateAttributes(node, element);
        }

        /**
         * Updates the node's value if it represents an element node.
         *
         * @param element the element
         * @param value the new value
         */
        private void updateElement(final Element element, final Object value)
        {
            Text txtNode = findTextNodeForUpdate(element);
            if (value == null)
            {
                // remove text
                if (txtNode != null)
                {
                    element.removeChild(txtNode);
                }
            }
            else
            {
                final String newValue = String.valueOf(value);
                if (txtNode == null)
                {
                    txtNode = document.createTextNode(newValue);
                    if (element.getFirstChild() != null)
                    {
                        element.insertBefore(txtNode, element.getFirstChild());
                    }
                    else
                    {
                        element.appendChild(txtNode);
                    }
                }
                else
                {
                    txtNode.setNodeValue(newValue);
                }
            }
        }

        /**
         * Updates the associated XML elements when a node is removed.
         * @param element the element to be removed
         */
        private void removeReference(final Element element)
        {
            final org.w3c.dom.Node parentElem = element.getParentNode();
            if (parentElem != null)
            {
                parentElem.removeChild(element);
            }
        }

        /**
         * Helper method for accessing the element of the specified node.
         *
         * @param node the node
         * @param refHandler the {@code ReferenceNodeHandler}
         * @return the element of this node
         */
        private Element getElement(final ImmutableNode node,
                final ReferenceNodeHandler refHandler)
        {
            final Element elementNew = newElements.get(node);
            if (elementNew != null)
            {
                return elementNew;
            }

            // special treatment for root node of the hierarchy
            final Object reference = refHandler.getReference(node);
            Node element;
            if (reference instanceof XMLDocumentHelper)
            {
                element =
                        ((XMLDocumentHelper) reference).getDocument()
                                .getDocumentElement();
            }
            else if (reference instanceof XMLListReference)
            {
                element = ((XMLListReference) reference).getElement();
            }
            else
            {
                element = (Node) reference;
            }
            return (element != null) ? (Element) elementMapping.get(element)
                    : document.getDocumentElement();
        }

        /**
         * Helper method for updating the values of all attributes of the
         * specified node.
         *
         * @param node the affected node
         * @param elem the element that is associated with this node
         */
        private static void updateAttributes(final ImmutableNode node, final Element elem)
        {
            if (node != null && elem != null)
            {
                clearAttributes(elem);
                for (final Map.Entry<String, Object> e : node.getAttributes()
                        .entrySet())
                {
                    if (e.getValue() != null)
                    {
                        elem.setAttribute(e.getKey(), e.getValue().toString());
                    }
                }
            }
        }

        /**
         * Removes all attributes of the given element.
         *
         * @param elem the element
         */
        private static void clearAttributes(final Element elem)
        {
            final NamedNodeMap attributes = elem.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
                elem.removeAttribute(attributes.item(i).getNodeName());
            }
        }

        /**
         * Returns the only text node of an element for update. This method is
         * called when the element's text changes. Then all text nodes except
         * for the first are removed. A reference to the first is returned or
         * <b>null</b> if there is no text node at all.
         *
         * @param elem the element
         * @return the first and only text node
         */
        private static Text findTextNodeForUpdate(final Element elem)
        {
            Text result = null;
            // Find all Text nodes
            final NodeList children = elem.getChildNodes();
            final Collection<org.w3c.dom.Node> textNodes =
                    new ArrayList<>();
            for (int i = 0; i < children.getLength(); i++)
            {
                final org.w3c.dom.Node nd = children.item(i);
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
            for (final org.w3c.dom.Node tn : textNodes)
            {
                elem.removeChild(tn);
            }
            return result;
        }
    }
}
