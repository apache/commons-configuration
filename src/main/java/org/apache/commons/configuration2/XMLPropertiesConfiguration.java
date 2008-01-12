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

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

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
 * &lt;?xml version="1.0"?>
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
 * &lt;properties>
 *   &lt;comment>Description of the property list&lt;/comment>
 *   &lt;entry key="key1">value1&lt;/entry>
 *   &lt;entry key="key2">value2&lt;/entry>
 *   &lt;entry key="key3">value3&lt;/entry>
 * &lt;/properties>
 * </pre>
 *
 * The Java 5.0 runtime is not required to use this class. The default encoding
 * for this configuration format is UTF-8. Note that unlike
 * <code>PropertiesConfiguration</code>, <code>XMLPropertiesConfiguration</code>
 * does not support includes.
 *
 * <em>Note:</em>Configuration objects of this type can be read concurrently
 * by multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.
 *
 * @author Emmanuel Bourg
 * @author Alistair Young
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class XMLPropertiesConfiguration extends PropertiesConfiguration
{
    /**
     * The default encoding (UTF-8 as specified by http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html)
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    // initialization block to set the encoding before loading the file in the constructors
    {
        setEncoding(DEFAULT_ENCODING);
    }

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
     * Creates and loads the xml properties from the specified file.
     * The specified file can contain "include" properties which then
     * are loaded and merged into the properties.
     *
     * @param fileName The name of the properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public XMLPropertiesConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
    }

    /**
     * Creates and loads the xml properties from the specified file.
     * The specified file can contain "include" properties which then
     * are loaded and merged into the properties.
     *
     * @param file The properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public XMLPropertiesConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Creates and loads the xml properties from the specified URL.
     * The specified file can contain "include" properties which then
     * are loaded and merged into the properties.
     *
     * @param url The location of the properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public XMLPropertiesConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }

    public void load(Reader in) throws ConfigurationException
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(true);

        try
        {
            SAXParser parser = factory.newSAXParser();

            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setEntityResolver(new EntityResolver()
            {
                public InputSource resolveEntity(String publicId, String systemId)
                {
                    return new InputSource(getClass().getClassLoader().getResourceAsStream("properties.dtd"));
                }
            });
            xmlReader.setContentHandler(new XMLPropertiesHandler());
            xmlReader.parse(new InputSource(in));
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to parse the configuration file", e);
        }

        // todo: support included properties ?
    }

    public void save(Writer out) throws ConfigurationException
    {
        PrintWriter writer = new PrintWriter(out);

        String encoding = getEncoding() != null ? getEncoding() : DEFAULT_ENCODING;
        writer.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        writer.println("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        writer.println("<properties>");

        if (getHeader() != null)
        {
            writer.println("  <comment>" + StringEscapeUtils.escapeXml(getHeader()) + "</comment>");
        }

        Iterator keys = getKeys();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            Object value = getProperty(key);

            if (value instanceof List)
            {
                writeProperty(writer, key, (List) value);
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
    private void writeProperty(PrintWriter out, String key, Object value)
    {
        // escape the key
        String k = StringEscapeUtils.escapeXml(key);

        if (value != null)
        {
            // escape the value
            String v = StringEscapeUtils.escapeXml(String.valueOf(value));
            v = StringUtils.replace(v, String.valueOf(getListDelimiter()), "\\" + getListDelimiter());

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
    private void writeProperty(PrintWriter out, String key, List values)
    {
        for (int i = 0; i < values.size(); i++)
        {
            writeProperty(out, key, values.get(i));
        }
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
        private StringBuffer value = new StringBuffer();

        /** Indicates that a comment is being parsed. */
        private boolean inCommentElement;

        /** Indicates that an entry is being parsed. */
        private boolean inEntryElement;

        public void startElement(String uri, String localName, String qName, Attributes attrs)
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

        public void endElement(String uri, String localName, String qName)
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
            value = new StringBuffer();
        }

        public void characters(char[] chars, int start, int length)
        {
            /**
             * We're currently processing an element. All character data from now until
             * the next endElement() call will be the data for this  element.
             */
            value.append(chars, start, length);
        }
    }
}
