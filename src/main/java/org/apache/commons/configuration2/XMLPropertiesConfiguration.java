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

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorAware;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This configuration implements the XML properties format introduced in Java
 * 5.0, see http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html.
 * An XML properties file looks like this:
 *
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
 * &lt;properties&gt;
 *   &lt;comment&gt;Description of the property list&lt;/comment&gt;
 *   &lt;entry key="key1"&gt;value1&lt;/entry&gt;
 *   &lt;entry key="key2"&gt;value2&lt;/entry&gt;
 *   &lt;entry key="key3"&gt;value3&lt;/entry&gt;
 * &lt;/properties&gt;
 * </pre>
 *
 * The Java 5.0 runtime is not required to use this class. The default encoding
 * for this configuration format is UTF-8. Note that unlike
 * {@code PropertiesConfiguration}, {@code XMLPropertiesConfiguration}
 * does not support includes.
 *
 * <em>Note:</em>Configuration objects of this type can be read concurrently
 * by multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.
 *
 * @author Emmanuel Bourg
 * @author Alistair Young
 * @version $Id$
 * @since 1.1
 */
public class XMLPropertiesConfiguration extends BaseConfiguration implements
        FileBasedConfiguration, FileLocatorAware
{
    /**
     * The default encoding (UTF-8 as specified by http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html)
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Default string used when the XML is malformed
     */
    private static final String MALFORMED_XML_EXCEPTION = "Malformed XML";

    /** The temporary file locator. */
    private FileLocator locator;

    /** Stores a header comment. */
    private String header;

    /**
     * Creates an empty XMLPropertyConfiguration object which can be
     * used to synthesize a new Properties file by adding values and
     * then saving(). An object constructed by this C'tor can not be
     * tickled into loading included files because it cannot supply a
     * base for relative includes.
     */
    public XMLPropertiesConfiguration()
    {
        super();
    }

    /**
     * Creates and loads the xml properties from the specified DOM node.
     *
     * @param element The DOM element
     * @throws ConfigurationException Error while loading the properties file
     * @since 2.0
     */
    public XMLPropertiesConfiguration(final Element element) throws ConfigurationException
    {
        super();
        this.load(element);
    }

    /**
     * Returns the header comment of this configuration.
     *
     * @return the header comment
     */
    public String getHeader()
    {
        return header;
    }

    /**
     * Sets the header comment of this configuration.
     *
     * @param header the header comment
     */
    public void setHeader(final String header)
    {
        this.header = header;
    }

    @Override
    public void read(final Reader in) throws ConfigurationException
    {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(true);

        try
        {
            final SAXParser parser = factory.newSAXParser();

            final XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setEntityResolver(new EntityResolver()
            {
                @Override
                public InputSource resolveEntity(final String publicId, final String systemId)
                {
                    return new InputSource(getClass().getClassLoader().getResourceAsStream("properties.dtd"));
                }
            });
            xmlReader.setContentHandler(new XMLPropertiesHandler());
            xmlReader.parse(new InputSource(in));
        }
        catch (final Exception e)
        {
            throw new ConfigurationException("Unable to parse the configuration file", e);
        }

        // todo: support included properties ?
    }

    /**
     * Parses a DOM element containing the properties. The DOM element has to follow
     * the XML properties format introduced in Java 5.0,
     * see http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html
     *
     * @param element The DOM element
     * @throws ConfigurationException Error while interpreting the DOM
     * @since 2.0
     */
    public void load(final Element element) throws ConfigurationException
    {
        if (!element.getNodeName().equals("properties"))
        {
            throw new ConfigurationException(MALFORMED_XML_EXCEPTION);
        }
        final NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            final Node item = childNodes.item(i);
            if (item instanceof Element)
            {
                if (item.getNodeName().equals("comment"))
                {
                    setHeader(item.getTextContent());
                }
                else if (item.getNodeName().equals("entry"))
                {
                    final String key = ((Element) item).getAttribute("key");
                    addProperty(key, item.getTextContent());
                }
                else
                {
                    throw new ConfigurationException(MALFORMED_XML_EXCEPTION);
                }
            }
        }
    }

    @Override
    public void write(final Writer out) throws ConfigurationException
    {
        final PrintWriter writer = new PrintWriter(out);

        String encoding = (locator != null) ? locator.getEncoding() : null;
        if (encoding == null)
        {
            encoding = DEFAULT_ENCODING;
        }
        writer.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        writer.println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        writer.println("<properties>");

        if (getHeader() != null)
        {
            writer.println("  <comment>" + StringEscapeUtils.escapeXml10(getHeader()) + "</comment>");
        }

        final Iterator<String> keys = getKeys();
        while (keys.hasNext())
        {
            final String key = keys.next();
            final Object value = getProperty(key);

            if (value instanceof List)
            {
                writeProperty(writer, key, (List<?>) value);
            }
            else
            {
                writeProperty(writer, key, value);
            }
        }

        writer.println("</properties>");
        writer.flush();
    }

    /**
     * Write a property.
     *
     * @param out the output stream
     * @param key the key of the property
     * @param value the value of the property
     */
    private void writeProperty(final PrintWriter out, final String key, final Object value)
    {
        // escape the key
        final String k = StringEscapeUtils.escapeXml10(key);

        if (value != null)
        {
            final String v = escapeValue(value);
            out.println("  <entry key=\"" + k + "\">" + v + "</entry>");
        }
        else
        {
            out.println("  <entry key=\"" + k + "\"/>");
        }
    }

    /**
     * Write a list property.
     *
     * @param out the output stream
     * @param key the key of the property
     * @param values a list with all property values
     */
    private void writeProperty(final PrintWriter out, final String key, final List<?> values)
    {
        for (final Object value : values)
        {
            writeProperty(out, key, value);
        }
    }

    /**
     * Writes the configuration as child to the given DOM node
     *
     * @param document The DOM document to add the configuration to
     * @param parent The DOM parent node
     * @since 2.0
     */
    public void save(final Document document, final Node parent)
    {
        final Element properties = document.createElement("properties");
        parent.appendChild(properties);
        if (getHeader() != null)
        {
            final Element comment = document.createElement("comment");
            properties.appendChild(comment);
            comment.setTextContent(StringEscapeUtils.escapeXml10(getHeader()));
        }

        final Iterator<String> keys = getKeys();
        while (keys.hasNext())
        {
            final String key = keys.next();
            final Object value = getProperty(key);

            if (value instanceof List)
            {
                writeProperty(document, properties, key, (List<?>) value);
            }
            else
            {
                writeProperty(document, properties, key, value);
            }
        }
    }

    /**
     * Initializes this object with a {@code FileLocator}. The locator is
     * accessed during load and save operations.
     *
     * @param locator the associated {@code FileLocator}
     */
    @Override
    public void initFileLocator(final FileLocator locator)
    {
        this.locator = locator;
    }

    private void writeProperty(final Document document, final Node properties, final String key, final Object value)
    {
        final Element entry = document.createElement("entry");
        properties.appendChild(entry);

        // escape the key
        final String k = StringEscapeUtils.escapeXml10(key);
        entry.setAttribute("key", k);

        if (value != null)
        {
            final String v = escapeValue(value);
            entry.setTextContent(v);
        }
    }

    private void writeProperty(final Document document, final Node properties, final String key, final List<?> values)
    {
        for (final Object value : values)
        {
            writeProperty(document, properties, key, value);
        }
    }

    /**
     * Escapes a property value before it is written to disk.
     *
     * @param value the value to be escaped
     * @return the escaped value
     */
    private String escapeValue(final Object value)
    {
        final String v = StringEscapeUtils.escapeXml10(String.valueOf(value));
        return String.valueOf(getListDelimiterHandler().escape(v,
                ListDelimiterHandler.NOOP_TRANSFORMER));
    }

    /**
     * SAX Handler to parse a XML properties file.
     *
     * @author Alistair Young
     * @since 1.2
     */
    private class XMLPropertiesHandler extends DefaultHandler
    {
        /** The key of the current entry being parsed. */
        private String key;

        /** The value of the current entry being parsed. */
        private StringBuilder value = new StringBuilder();

        /** Indicates that a comment is being parsed. */
        private boolean inCommentElement;

        /** Indicates that an entry is being parsed. */
        private boolean inEntryElement;

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attrs)
        {
            if ("comment".equals(qName))
            {
                inCommentElement = true;
            }

            if ("entry".equals(qName))
            {
                key = attrs.getValue("key");
                inEntryElement = true;
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName)
        {
            if (inCommentElement)
            {
                // We've just finished a <comment> element so set the header
                setHeader(value.toString());
                inCommentElement = false;
            }

            if (inEntryElement)
            {
                // We've just finished an <entry> element, so add the key/value pair
                addProperty(key, value.toString());
                inEntryElement = false;
            }

            // Clear the element value buffer
            value = new StringBuilder();
        }

        @Override
        public void characters(final char[] chars, final int start, final int length)
        {
            /**
             * We're currently processing an element. All character data from now until
             * the next endElement() call will be the data for this  element.
             */
            value.append(chars, start, length);
        }
    }
}
