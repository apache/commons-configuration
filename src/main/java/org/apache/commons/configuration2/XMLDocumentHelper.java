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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * An internally used helper class for dealing with XML documents.
 * </p>
 * <p>
 * This class is used by {@link XMLConfiguration}. It provides some basic
 * functionality for processing DOM documents and dealing with elements. The
 * main idea is that an instance holds the XML document associated with a XML
 * configuration object. When the configuration is to be saved the document has
 * to be manipulated according to the changes made on the configuration. To
 * ensure that this is possible even under concurrent access, a new temporary
 * instance is created as a copy of the original instance. Then, on this copy,
 * the changes of the configuration are applied. The resulting document can then
 * be serialized.
 * </p>
 * <p>
 * Nodes of an {@code XMLConfiguration} that was read from a file are associated
 * with the XML elements they represent. In order to apply changes on the copied
 * document, it is necessary to establish a mapping between the elements of the
 * old document and the elements of the copied document. This is also handled by
 * this class.
 * </p>
 *
 * @since 2.0
 */
class XMLDocumentHelper
{
    /** Stores the document managed by this instance. */
    private final Document document;

    /** The element mapping to the source document. */
    private final Map<Node, Node> elementMapping;

    /** Stores the public ID of the source document. */
    private final String sourcePublicID;

    /** Stores the system ID of the source document. */
    private final String sourceSystemID;

    /**
     * Creates a new instance of {@code XMLDocumentHelper} and initializes it
     * with the given XML document. Note: This constructor is package private
     * only for testing purposes. Instances should be created using the static
     * factory methods.
     *
     * @param doc the {@code Document}
     * @param elemMap the element mapping
     * @param pubID the public ID of the source document
     * @param sysID the system ID of the source document
     */
    XMLDocumentHelper(final Document doc, final Map<Node, Node> elemMap, final String pubID,
            final String sysID)
    {
        document = doc;
        elementMapping = elemMap;
        sourcePublicID = pubID;
        sourceSystemID = sysID;
    }

    /**
     * Creates a new instance of {@code XMLDocumentHelper} and initializes it
     * with a newly created, empty {@code Document}. The new document has a root
     * element with the given element name. This element has no further child
     * nodes.
     *
     * @param rootElementName the name of the root element
     * @return the newly created instance
     * @throws ConfigurationException if an error occurs when creating the
     *         document
     */
    public static XMLDocumentHelper forNewDocument(final String rootElementName)
            throws ConfigurationException
    {
        final Document doc =
                createDocumentBuilder(createDocumentBuilderFactory())
                        .newDocument();
        final Element rootElem = doc.createElement(rootElementName);
        doc.appendChild(rootElem);
        return new XMLDocumentHelper(doc, emptyElementMapping(), null, null);
    }

    /**
     * Creates a new instance of {@code XMLDocumentHelper} and initializes it
     * with a source document. This is a document created from a configuration
     * file. It is kept in memory so that the configuration can be saved with
     * the same format. Note that already a copy of this document is created.
     * This is done for the following reasons:
     * <ul>
     * <li>It is a defensive copy.</li>
     * <li>An identity transformation on a document may change certain nodes,
     * e.g. CDATA sections. When later on again copies of this document are
     * created it has to be ensured that these copies have the same structure
     * than the original document stored in this instance.</li>
     * </ul>
     *
     * @param srcDoc the source document
     * @return the newly created instance
     * @throws ConfigurationException if an error occurs
     */
    public static XMLDocumentHelper forSourceDocument(final Document srcDoc)
            throws ConfigurationException
    {
        String pubID;
        String sysID;
        if (srcDoc.getDoctype() != null)
        {
            pubID = srcDoc.getDoctype().getPublicId();
            sysID = srcDoc.getDoctype().getSystemId();
        }
        else
        {
            pubID = null;
            sysID = null;
        }

        return new XMLDocumentHelper(copyDocument(srcDoc),
                emptyElementMapping(), pubID, sysID);
    }

    /**
     * Returns the {@code Document} managed by this helper.
     *
     * @return the wrapped {@code Document}
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * Returns the element mapping to the source document. This map can be used
     * to obtain elements in the managed document which correspond to elements
     * in the source document. If this instance has not been created from a
     * source document, the mapping is empty.
     *
     * @return the element mapping to the source document
     */
    public Map<Node, Node> getElementMapping()
    {
        return elementMapping;
    }

    /**
     * Returns the public ID of the source document.
     *
     * @return the public ID of the source document
     */
    public String getSourcePublicID()
    {
        return sourcePublicID;
    }

    /**
     * Returns the system ID of the source document.
     *
     * @return the system ID of the source document
     */
    public String getSourceSystemID()
    {
        return sourceSystemID;
    }

    /**
     * Creates a new {@code Transformer} object. No initializations are
     * performed on the new instance.
     *
     * @return the new {@code Transformer}
     * @throws ConfigurationException if the {@code Transformer} could not be
     *         created
     */
    public static Transformer createTransformer() throws ConfigurationException
    {
        return createTransformer(createTransformerFactory());
    }

    /**
     * Performs an XSL transformation on the passed in operands. All possible
     * exceptions are caught and redirected as {@code ConfigurationException}
     * exceptions.
     *
     * @param transformer the transformer
     * @param source the source
     * @param result the result
     * @throws ConfigurationException if an error occurs
     */
    public static void transform(final Transformer transformer, final Source source,
            final Result result) throws ConfigurationException
    {
        try
        {
            transformer.transform(source, result);
        }
        catch (final TransformerException tex)
        {
            throw new ConfigurationException(tex);
        }
    }

    /**
     * Creates a copy of this object. This copy contains a copy of the document
     * and an element mapping which allows mapping elements from the source
     * document to elements of the copied document.
     *
     * @return the copy
     * @throws ConfigurationException if an error occurs
     */
    public XMLDocumentHelper createCopy() throws ConfigurationException
    {
        final Document docCopy = copyDocument(getDocument());
        return new XMLDocumentHelper(docCopy, createElementMapping(
                getDocument(), docCopy), getSourcePublicID(),
                getSourceSystemID());
    }

    /**
     * Creates a new {@code TransformerFactory}.
     *
     * @return the {@code TransformerFactory}
     */
    static TransformerFactory createTransformerFactory()
    {
        return TransformerFactory.newInstance();
    }

    /**
     * Creates a {@code Transformer} using the specified factory.
     *
     * @param factory the {@code TransformerFactory}
     * @return the newly created {@code Transformer}
     * @throws ConfigurationException if an error occurs
     */
    static Transformer createTransformer(final TransformerFactory factory)
            throws ConfigurationException
    {
        try
        {
            return factory.newTransformer();
        }
        catch (final TransformerConfigurationException tex)
        {
            throw new ConfigurationException(tex);
        }
    }

    /**
     * Creates a new {@code DocumentBuilder} using the specified factory.
     * Exceptions are rethrown as {@code ConfigurationException} exceptions.
     *
     * @param factory the {@code DocumentBuilderFactory}
     * @return the newly created {@code DocumentBuilder}
     * @throws ConfigurationException if an error occurs
     */
    static DocumentBuilder createDocumentBuilder(final DocumentBuilderFactory factory)
            throws ConfigurationException
    {
        try
        {
            return factory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException pcex)
        {
            throw new ConfigurationException(pcex);
        }
    }

    /**
     * Creates a copy of the specified document.
     *
     * @param doc the {@code Document}
     * @return the copy of this document
     * @throws ConfigurationException if an error occurs
     */
    private static Document copyDocument(final Document doc)
            throws ConfigurationException
    {
        final Transformer transformer = createTransformer();
        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();
        transform(transformer, source, result);

        return (Document) result.getNode();
    }

    /**
     * Creates a new {@code DocumentBuilderFactory} instance.
     *
     * @return the new factory object
     */
    private static DocumentBuilderFactory createDocumentBuilderFactory()
    {
        return DocumentBuilderFactory.newInstance();
    }

    /**
     * Creates an empty element mapping.
     *
     * @return the empty mapping
     */
    private static Map<Node, Node> emptyElementMapping()
    {
        return Collections.emptyMap();
    }

    /**
     * Creates the element mapping for the specified documents. For each node in
     * the source document an entry is created pointing to the corresponding
     * node in the destination object.
     *
     * @param doc1 the source document
     * @param doc2 the destination document
     * @return the element mapping
     */
    private static Map<Node, Node> createElementMapping(final Document doc1,
            final Document doc2)
    {
        final Map<Node, Node> mapping = new HashMap<>();
        createElementMappingForNodes(doc1.getDocumentElement(),
                doc2.getDocumentElement(), mapping);
        return mapping;
    }

    /**
     * Creates the element mapping for the specified nodes and all their child
     * nodes.
     *
     * @param n1 node 1
     * @param n2 node 2
     * @param mapping the mapping to be filled
     */
    private static void createElementMappingForNodes(final Node n1, final Node n2,
            final Map<Node, Node> mapping)
    {
        mapping.put(n1, n2);
        final NodeList childNodes1 = n1.getChildNodes();
        final NodeList childNodes2 = n2.getChildNodes();
        final int count = Math.min(childNodes1.getLength(), childNodes2.getLength());
        for (int i = 0; i < count; i++)
        {
            createElementMappingForNodes(childNodes1.item(i),
                    childNodes2.item(i), mapping);
        }
    }
}
