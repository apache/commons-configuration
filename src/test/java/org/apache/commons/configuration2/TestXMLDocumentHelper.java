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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Test class for {@code XMLDocumentHelper}.
 */
public class TestXMLDocumentHelper {
    /** Constant for the name of an element. */
    private static final String ELEMENT = "testElementName";

    /** Constant for the name of the test XML file. */
    private static final String TEST_FILE = "testcombine1.xml";

    /**
     * Serializes the specified document to a string.
     *
     * @param document the document
     * @return the document serialized to a string
     * @throws ConfigurationException if an error occurs
     */
    private static String documentToString(final Document document) throws ConfigurationException {
        final Transformer transformer = XMLDocumentHelper.createTransformer();
        final StringWriter writer = new StringWriter();
        final Result result = new StreamResult(writer);
        XMLDocumentHelper.transform(transformer, new DOMSource(document.getDocumentElement()), result);
        return writer.toString();
    }

    /**
     * Serializes the document wrapped by the given helper to a string.
     *
     * @param helper the document helper
     * @return the document serialized to a string
     * @throws ConfigurationException if an error occurs
     */
    private static String documentToString(final XMLDocumentHelper helper) throws ConfigurationException {
        return documentToString(helper.getDocument());
    }

    /**
     * Obtains all text elements contained in the given document.
     *
     * @param document the document
     * @return a collection with all text elements
     */
    private static Collection<Node> findTextElements(final Document document) {
        final Collection<Node> texts = new HashSet<>();
        findTextElementsForNode(document.getDocumentElement(), texts);
        return texts;
    }

    /**
     * Recursively obtains all text elements for the given node.
     *
     * @param node the node
     * @param texts the collection with text elements
     */
    private static void findTextElementsForNode(final Node node, final Collection<Node> texts) {
        if (node instanceof Text) {
            texts.add(node);
        }
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            findTextElementsForNode(childNodes.item(i), texts);
        }
    }

    /**
     * Loads a test XML document.
     *
     * @return the test document
     */
    private static Document loadDocument() throws ParserConfigurationException, IOException, SAXException {
        return loadDocument(TEST_FILE);
    }

    /**
     * Loads the test document with the given name.
     *
     * @param name the name of the test document
     * @return the parsed document
     */
    private static Document loadDocument(final String name) throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(ConfigurationAssert.getTestFile(name));
    }

    /**
     * Helper method for testing the element mapping of a copied document.
     *
     * @param file the name of the test file
     */
    private void checkCopyElementMapping(final String file) throws Exception {
        final XMLDocumentHelper helper = XMLDocumentHelper.forSourceDocument(loadDocument(file));
        final XMLDocumentHelper copy = helper.createCopy();
        final Collection<Node> texts = findTextElements(helper.getDocument());
        assertFalse(texts.isEmpty());
        for (final Node n : texts) {
            final Text txtSrc = (Text) n;
            final Text txtCopy = (Text) copy.getElementMapping().get(n);
            assertNotNull(txtCopy, "No matching element for " + n);
            assertEquals(txtSrc.getData(), txtCopy.getData());
        }
    }

    /**
     * Tests whether a document can be copied.
     */
    @Test
    public void testCopyDocument() throws Exception {
        final XMLDocumentHelper helper = XMLDocumentHelper.forSourceDocument(loadDocument());
        final XMLDocumentHelper copy = helper.createCopy();
        assertNotSame(helper.getDocument(), copy.getDocument());
        final String doc1 = documentToString(helper);
        final String doc2 = documentToString(copy);
        assertEquals(doc1, doc2);
    }

    /**
     * Tests the element mapping of a copied document.
     */
    @Test
    public void testCopyElementMapping() throws Exception {
        checkCopyElementMapping(TEST_FILE);
    }

    /**
     * Tests whether the element is correctly constructed for a more complex document.
     */
    @Test
    public void testCopyElementMappingForComplexDocument() throws Exception {
        checkCopyElementMapping("test.xml");
    }

    /**
     * Tests whether an exception thrown by a document builder factory is handled correctly.
     */
    @Test
    public void testCreateDocumentBuilderFromFactoryException() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = mock(DocumentBuilderFactory.class);
        final ParserConfigurationException pcex = new ParserConfigurationException();

        when(factory.newDocumentBuilder()).thenThrow(pcex);

        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> XMLDocumentHelper.createDocumentBuilder(factory));
        assertEquals(pcex, cex.getCause());
    }

    /**
     * Tests whether a correct transformer factory can be created.
     */
    @Test
    public void testCreateTransformerFactory() {
        assertNotNull(XMLDocumentHelper.createTransformerFactory());
    }

    /**
     * Tests whether exceptions while creating transformers are correctly handled.
     */
    @Test
    public void testCreateTransformerFactoryException() throws TransformerConfigurationException {
        final TransformerFactory factory = mock(TransformerFactory.class);
        final TransformerConfigurationException cause = new TransformerConfigurationException();

        when(factory.newTransformer()).thenThrow(cause);

        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> XMLDocumentHelper.createTransformer(factory));
        assertEquals(cause, cex.getCause());
    }

    /**
     * Tests the content of the element mapping for a newly created document.
     */
    @Test
    public void testElementMappingForNewDocument() throws ConfigurationException {
        final XMLDocumentHelper helper = XMLDocumentHelper.forNewDocument(ELEMENT);
        assertTrue(helper.getElementMapping().isEmpty());
    }

    /**
     * Tests the content of the element mapping for a source document.
     */
    @Test
    public void testElementMappingForSourceDocument() throws Exception {
        final Document doc = loadDocument();
        final XMLDocumentHelper helper = XMLDocumentHelper.forSourceDocument(doc);
        assertTrue(helper.getElementMapping().isEmpty());
    }

    /**
     * Tests whether an instance can be created wrapping a new document.
     */
    @Test
    public void testInitForNewDocument() throws ConfigurationException {
        final XMLDocumentHelper helper = XMLDocumentHelper.forNewDocument(ELEMENT);
        final Document doc = helper.getDocument();
        final Element rootElement = doc.getDocumentElement();
        assertEquals(ELEMENT, rootElement.getNodeName());
        final NodeList childNodes = rootElement.getChildNodes();
        assertEquals(0, childNodes.getLength());
        assertNull(helper.getSourcePublicID());
        assertNull(helper.getSourceSystemID());
    }

    /**
     * Tests whether an instance can be created based on a source document.
     */
    @Test
    public void testInitForSourceDocument() throws Exception {
        final Document doc = loadDocument();
        final XMLDocumentHelper helper = XMLDocumentHelper.forSourceDocument(doc);
        assertNotSame(doc, helper.getDocument());
        assertEquals(documentToString(doc), documentToString(helper));
    }

    /**
     * Tests whether transform() handles a TransformerException.
     */
    @Test
    public void testTransformException() throws TransformerException {
        final Transformer transformer = mock(Transformer.class);
        final Source src = mock(Source.class);
        final Result res = mock(Result.class);
        final TransformerException tex = new TransformerException("Test Exception");

        doThrow(tex).when(transformer).transform(src, res);

        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> XMLDocumentHelper.transform(transformer, src, res));
        assertEquals(tex, cex.getCause());
    }
}
