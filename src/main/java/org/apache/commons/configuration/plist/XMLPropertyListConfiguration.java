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

package org.apache.commons.configuration.plist;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.BaseHierarchicalConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileBasedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.ex.ConfigurationException;
import org.apache.commons.configuration.io.FileLocator;
import org.apache.commons.configuration.io.FileLocatorAware;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Property list file (plist) in XML FORMAT as used by Mac OS X (http://www.apple.com/DTDs/PropertyList-1.0.dtd).
 * This configuration doesn't support the binary FORMAT used in OS X 10.4.
 *
 * <p>Example:</p>
 * <pre>
 * &lt;?xml version="1.0"?>
 * &lt;!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">
 * &lt;plist version="1.0">
 *     &lt;dict>
 *         &lt;key>string&lt;/key>
 *         &lt;string>value1&lt;/string>
 *
 *         &lt;key>integer&lt;/key>
 *         &lt;integer>12345&lt;/integer>
 *
 *         &lt;key>real&lt;/key>
 *         &lt;real>-123.45E-1&lt;/real>
 *
 *         &lt;key>boolean&lt;/key>
 *         &lt;true/>
 *
 *         &lt;key>date&lt;/key>
 *         &lt;date>2005-01-01T12:00:00Z&lt;/date>
 *
 *         &lt;key>data&lt;/key>
 *         &lt;data>RHJhY28gRG9ybWllbnMgTnVucXVhbSBUaXRpbGxhbmR1cw==&lt;/data>
 *
 *         &lt;key>array&lt;/key>
 *         &lt;array>
 *             &lt;string>value1&lt;/string>
 *             &lt;string>value2&lt;/string>
 *             &lt;string>value3&lt;/string>
 *         &lt;/array>
 *
 *         &lt;key>dictionnary&lt;/key>
 *         &lt;dict>
 *             &lt;key>key1&lt;/key>
 *             &lt;string>value1&lt;/string>
 *             &lt;key>key2&lt;/key>
 *             &lt;string>value2&lt;/string>
 *             &lt;key>key3&lt;/key>
 *             &lt;string>value3&lt;/string>
 *         &lt;/dict>
 *
 *         &lt;key>nested&lt;/key>
 *         &lt;dict>
 *             &lt;key>node1&lt;/key>
 *             &lt;dict>
 *                 &lt;key>node2&lt;/key>
 *                 &lt;dict>
 *                     &lt;key>node3&lt;/key>
 *                     &lt;string>value&lt;/string>
 *                 &lt;/dict>
 *             &lt;/dict>
 *         &lt;/dict>
 *
 *     &lt;/dict>
 * &lt;/plist>
 * </pre>
 *
 * @since 1.2
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class XMLPropertyListConfiguration extends BaseHierarchicalConfiguration
    implements FileBasedConfiguration, FileLocatorAware
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -3162063751042475985L;

    /** Size of the indentation for the generated file. */
    private static final int INDENT_SIZE = 4;

    /** Temporarily stores the current file location. */
    private FileLocator locator;

    /**
     * Creates an empty XMLPropertyListConfiguration object which can be
     * used to synthesize a new plist file by adding values and
     * then saving().
     */
    public XMLPropertyListConfiguration()
    {
        initRoot();
    }

    /**
     * Creates a new instance of {@code XMLPropertyListConfiguration} and
     * copies the content of the specified configuration into this object.
     *
     * @param configuration the configuration to copy
     * @since 1.4
     */
    public XMLPropertyListConfiguration(HierarchicalConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected void setPropertyInternal(String key, Object value)
    {
        // special case for byte arrays, they must be stored as is in the configuration
        if (value instanceof byte[])
        {
            setDetailEvents(false);
            try
            {
                clearProperty(key);
                addPropertyDirect(key, value);
            }
            finally
            {
                setDetailEvents(true);
            }
        }
        else
        {
            super.setPropertyInternal(key, value);
        }
    }

    @Override
    protected void addPropertyInternal(String key, Object value)
    {
        if (value instanceof byte[])
        {
            addPropertyDirect(key, value);
        }
        else
        {
            super.addPropertyInternal(key, value);
        }
    }

    /**
     * Stores the current file locator. This method is called before I/O
     * operations.
     *
     * @param locator the current {@code FileLocator}
     */
    @Override
    public void initFileLocator(FileLocator locator)
    {
        this.locator = locator;
    }

    @Override
    public void read(Reader in) throws ConfigurationException
    {
        // We have to make sure that the root node is actually a PListNode.
        // If this object was not created using the standard constructor, the
        // root node is a plain Node.
        if (!(getRootNode() instanceof PListNode))
        {
            initRoot();
        }

        // set up the DTD validation
        EntityResolver resolver = new EntityResolver()
        {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
            {
                return new InputSource(getClass().getClassLoader().getResourceAsStream("PropertyList-1.0.dtd"));
            }
        };

        // parse the file
        XMLPropertyListHandler handler = new XMLPropertyListHandler(getRootNode());
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);

            SAXParser parser = factory.newSAXParser();
            parser.getXMLReader().setEntityResolver(resolver);
            parser.getXMLReader().setContentHandler(handler);
            parser.getXMLReader().parse(new InputSource(in));
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to parse the configuration file", e);
        }
    }

    @Override
    public void write(Writer out) throws ConfigurationException
    {
        PrintWriter writer = new PrintWriter(out);

        if (locator.getEncoding() != null)
        {
            writer.println("<?xml version=\"1.0\" encoding=\"" + locator.getEncoding() + "\"?>");
        }
        else
        {
            writer.println("<?xml version=\"1.0\"?>");
        }

        writer.println("<!DOCTYPE plist SYSTEM \"file://localhost/System/Library/DTDs/PropertyList.dtd\">");
        writer.println("<plist version=\"1.0\">");

        printNode(writer, 1, getRootNode());

        writer.println("</plist>");
        writer.flush();
    }

    /**
     * Append a node to the writer, indented according to a specific level.
     */
    private void printNode(PrintWriter out, int indentLevel, ConfigurationNode node)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (node.getName() != null)
        {
            out.println(padding + "<key>" + StringEscapeUtils.escapeXml(node.getName()) + "</key>");
        }

        List<ConfigurationNode> children = node.getChildren();
        if (!children.isEmpty())
        {
            out.println(padding + "<dict>");

            Iterator<ConfigurationNode> it = children.iterator();
            while (it.hasNext())
            {
                ConfigurationNode child = it.next();
                printNode(out, indentLevel + 1, child);

                if (it.hasNext())
                {
                    out.println();
                }
            }

            out.println(padding + "</dict>");
        }
        else if (node.getValue() == null)
        {
            out.println(padding + "<dict/>");
        }
        else
        {
            Object value = node.getValue();
            printValue(out, indentLevel, value);
        }
    }

    /**
     * Append a value to the writer, indented according to a specific level.
     */
    private void printValue(PrintWriter out, int indentLevel, Object value)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (value instanceof Date)
        {
            synchronized (PListNode.FORMAT)
            {
                out.println(padding + "<date>" + PListNode.FORMAT.format((Date) value) + "</date>");
            }
        }
        else if (value instanceof Calendar)
        {
            printValue(out, indentLevel, ((Calendar) value).getTime());
        }
        else if (value instanceof Number)
        {
            if (value instanceof Double || value instanceof Float || value instanceof BigDecimal)
            {
                out.println(padding + "<real>" + value.toString() + "</real>");
            }
            else
            {
                out.println(padding + "<integer>" + value.toString() + "</integer>");
            }
        }
        else if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
            {
                out.println(padding + "<true/>");
            }
            else
            {
                out.println(padding + "<false/>");
            }
        }
        else if (value instanceof List)
        {
            out.println(padding + "<array>");
            Iterator<?> it = ((List<?>) value).iterator();
            while (it.hasNext())
            {
                printValue(out, indentLevel + 1, it.next());
            }
            out.println(padding + "</array>");
        }
        else if (value instanceof HierarchicalConfiguration)
        {
            printNode(out, indentLevel, ((HierarchicalConfiguration) value).getRootNode());
        }
        else if (value instanceof Configuration)
        {
            // display a flat Configuration as a dictionary
            out.println(padding + "<dict>");

            Configuration config = (Configuration) value;
            Iterator<String> it = config.getKeys();
            while (it.hasNext())
            {
                // create a node for each property
                String key = it.next();
                ConfigurationNode node = new DefaultConfigurationNode(key);
                node.setValue(config.getProperty(key));

                // print the node
                printNode(out, indentLevel + 1, node);

                if (it.hasNext())
                {
                    out.println();
                }
            }
            out.println(padding + "</dict>");
        }
        else if (value instanceof Map)
        {
            // display a Map as a dictionary
            Map<String, Object> map = transformMap((Map<?, ?>) value);
            printValue(out, indentLevel, new MapConfiguration(map));
        }
        else if (value instanceof byte[])
        {
            String base64 = new String(Base64.encodeBase64((byte[]) value));
            out.println(padding + "<data>" + StringEscapeUtils.escapeXml(base64) + "</data>");
        }
        else if (value != null)
        {
            out.println(padding + "<string>" + StringEscapeUtils.escapeXml(String.valueOf(value)) + "</string>");
        }
        else
        {
            out.println(padding + "<string/>");
        }
    }

    /**
     * Helper method for initializing the configuration's root node.
     */
    private void initRoot()
    {
        setRootNode(new PListNode());
    }

    /**
     * Transform a map of arbitrary types into a map with string keys and object
     * values. All keys of the source map which are not of type String are
     * dropped.
     *
     * @param src the map to be converted
     * @return the resulting map
     */
    private static Map<String, Object> transformMap(Map<?, ?> src)
    {
        Map<String, Object> dest = new HashMap<String, Object>();
        for (Map.Entry<?, ?> e : src.entrySet())
        {
            if (e.getKey() instanceof String)
            {
                dest.put((String) e.getKey(), e.getValue());
            }
        }
        return dest;
    }

    /**
     * SAX Handler to build the configuration nodes while the document is being parsed.
     */
    private class XMLPropertyListHandler extends DefaultHandler
    {
        /** The buffer containing the text node being read */
        private final StringBuilder buffer = new StringBuilder();

        /** The stack of configuration nodes */
        private final List<ConfigurationNode> stack = new ArrayList<ConfigurationNode>();

        public XMLPropertyListHandler(ConfigurationNode root)
        {
            push(root);
        }

        /**
         * Return the node on the top of the stack.
         */
        private ConfigurationNode peek()
        {
            if (!stack.isEmpty())
            {
                return stack.get(stack.size() - 1);
            }
            else
            {
                return null;
            }
        }

        /**
         * Remove and return the node on the top of the stack.
         */
        private ConfigurationNode pop()
        {
            if (!stack.isEmpty())
            {
                return stack.remove(stack.size() - 1);
            }
            else
            {
                return null;
            }
        }

        /**
         * Put a node on the top of the stack.
         */
        private void push(ConfigurationNode node)
        {
            stack.add(node);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            if ("array".equals(qName))
            {
                push(new ArrayNode());
            }
            else if ("dict".equals(qName))
            {
                if (peek() instanceof ArrayNode)
                {
                    // create the configuration
                    XMLPropertyListConfiguration config = new XMLPropertyListConfiguration();

                    // add it to the ArrayNode
                    ArrayNode node = (ArrayNode) peek();
                    node.addValue(config);

                    // push the root on the stack
                    push(config.getRootNode());
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if ("key".equals(qName))
            {
                // create a new node, link it to its parent and push it on the stack
                PListNode node = new PListNode();
                node.setName(buffer.toString());
                peek().addChild(node);
                push(node);
            }
            else if ("dict".equals(qName))
            {
                // remove the root of the XMLPropertyListConfiguration previously pushed on the stack
                pop();
            }
            else
            {
                if ("string".equals(qName))
                {
                    ((PListNode) peek()).addValue(buffer.toString());
                }
                else if ("integer".equals(qName))
                {
                    ((PListNode) peek()).addIntegerValue(buffer.toString());
                }
                else if ("real".equals(qName))
                {
                    ((PListNode) peek()).addRealValue(buffer.toString());
                }
                else if ("true".equals(qName))
                {
                    ((PListNode) peek()).addTrueValue();
                }
                else if ("false".equals(qName))
                {
                    ((PListNode) peek()).addFalseValue();
                }
                else if ("data".equals(qName))
                {
                    ((PListNode) peek()).addDataValue(buffer.toString());
                }
                else if ("date".equals(qName))
                {
                    try
                    {
                        ((PListNode) peek()).addDateValue(buffer.toString());
                    }
                    catch (IllegalArgumentException iex)
                    {
                        getLogger().warn(
                                "Ignoring invalid date property " + buffer);
                    }
                }
                else if ("array".equals(qName))
                {
                    ArrayNode array = (ArrayNode) pop();
                    ((PListNode) peek()).addList(array);
                }

                // remove the plist node on the stack once the value has been parsed,
                // array nodes remains on the stack for the next values in the list
                if (!(peek() instanceof ArrayNode))
                {
                    pop();
                }
            }

            buffer.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            buffer.append(ch, start, length);
        }
    }

    /**
     * Node extension with addXXX methods to parse the typed data passed by the SAX handler.
     * <b>Do not use this class !</b> It is used internally by XMLPropertyConfiguration
     * to parse the configuration file, it may be removed at any moment in the future.
     */
    public static class PListNode extends DefaultConfigurationNode
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = -7614060264754798317L;

        /**
         * The MacOS FORMAT of dates in plist files. Note: Because
         * {@code SimpleDateFormat} is not thread-safe, each access has to be
         * synchronized.
         */
        private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        static
        {
            FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        /**
         * The GNUstep FORMAT of dates in plist files. Note: Because
         * {@code SimpleDateFormat} is not thread-safe, each access has to be
         * synchronized.
         */
        private static final DateFormat GNUSTEP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        /**
         * Update the value of the node. If the existing value is null, it's
         * replaced with the new value. If the existing value is a list, the
         * specified value is appended to the list. If the existing value is
         * not null, a list with the two values is built.
         *
         * @param value the value to be added
         */
        public void addValue(Object value)
        {
            if (getValue() == null)
            {
                setValue(value);
            }
            else if (getValue() instanceof Collection)
            {
                // This is safe because we create the collections ourselves
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) getValue();
                collection.add(value);
            }
            else
            {
                List<Object> list = new ArrayList<Object>();
                list.add(getValue());
                list.add(value);
                setValue(list);
            }
        }

        /**
         * Parse the specified string as a date and add it to the values of the node.
         *
         * @param value the value to be added
         * @throws IllegalArgumentException if the date string cannot be parsed
         */
        public void addDateValue(String value)
        {
            try
            {
                if (value.indexOf(' ') != -1)
                {
                    // parse the date using the GNUstep FORMAT
                    synchronized (GNUSTEP_FORMAT)
                    {
                        addValue(GNUSTEP_FORMAT.parse(value));
                    }
                }
                else
                {
                    // parse the date using the MacOS X FORMAT
                    synchronized (FORMAT)
                    {
                        addValue(FORMAT.parse(value));
                    }
                }
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException(String.format(
                        "'%s' cannot be parsed to a date!", value), e);
            }
        }

        /**
         * Parse the specified string as a byte array in base 64 FORMAT
         * and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addDataValue(String value)
        {
            addValue(Base64.decodeBase64(value.getBytes()));
        }

        /**
         * Parse the specified string as an Interger and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addIntegerValue(String value)
        {
            addValue(new BigInteger(value));
        }

        /**
         * Parse the specified string as a Double and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addRealValue(String value)
        {
            addValue(new BigDecimal(value));
        }

        /**
         * Add a boolean value 'true' to the values of the node.
         */
        public void addTrueValue()
        {
            addValue(Boolean.TRUE);
        }

        /**
         * Add a boolean value 'false' to the values of the node.
         */
        public void addFalseValue()
        {
            addValue(Boolean.FALSE);
        }

        /**
         * Add a sublist to the values of the node.
         *
         * @param node the node whose value will be added to the current node value
         */
        public void addList(ArrayNode node)
        {
            addValue(node.getValue());
        }
    }

    /**
     * Container for array elements. <b>Do not use this class !</b>
     * It is used internally by XMLPropertyConfiguration to parse the
     * configuration file, it may be removed at any moment in the future.
     */
    public static class ArrayNode extends PListNode
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = 5586544306664205835L;

        /** The list of values in the array. */
        private final List<Object> list = new ArrayList<Object>();

        /**
         * Add an object to the array.
         *
         * @param value the value to be added
         */
        @Override
        public void addValue(Object value)
        {
            list.add(value);
        }

        /**
         * Return the list of values in the array.
         *
         * @return the {@link List} of values
         */
        @Override
        public Object getValue()
        {
            return list;
        }
    }
}
