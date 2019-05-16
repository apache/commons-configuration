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

package org.apache.commons.configuration2.plist;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorAware;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
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
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd"&gt;
 * &lt;plist version="1.0"&gt;
 *     &lt;dict&gt;
 *         &lt;key&gt;string&lt;/key&gt;
 *         &lt;string&gt;value1&lt;/string&gt;
 *
 *         &lt;key&gt;integer&lt;/key&gt;
 *         &lt;integer&gt;12345&lt;/integer&gt;
 *
 *         &lt;key&gt;real&lt;/key&gt;
 *         &lt;real&gt;-123.45E-1&lt;/real&gt;
 *
 *         &lt;key&gt;boolean&lt;/key&gt;
 *         &lt;true/&gt;
 *
 *         &lt;key&gt;date&lt;/key&gt;
 *         &lt;date&gt;2005-01-01T12:00:00Z&lt;/date&gt;
 *
 *         &lt;key&gt;data&lt;/key&gt;
 *         &lt;data&gt;RHJhY28gRG9ybWllbnMgTnVucXVhbSBUaXRpbGxhbmR1cw==&lt;/data&gt;
 *
 *         &lt;key&gt;array&lt;/key&gt;
 *         &lt;array&gt;
 *             &lt;string&gt;value1&lt;/string&gt;
 *             &lt;string&gt;value2&lt;/string&gt;
 *             &lt;string&gt;value3&lt;/string&gt;
 *         &lt;/array&gt;
 *
 *         &lt;key&gt;dictionnary&lt;/key&gt;
 *         &lt;dict&gt;
 *             &lt;key&gt;key1&lt;/key&gt;
 *             &lt;string&gt;value1&lt;/string&gt;
 *             &lt;key&gt;key2&lt;/key&gt;
 *             &lt;string&gt;value2&lt;/string&gt;
 *             &lt;key&gt;key3&lt;/key&gt;
 *             &lt;string&gt;value3&lt;/string&gt;
 *         &lt;/dict&gt;
 *
 *         &lt;key&gt;nested&lt;/key&gt;
 *         &lt;dict&gt;
 *             &lt;key&gt;node1&lt;/key&gt;
 *             &lt;dict&gt;
 *                 &lt;key&gt;node2&lt;/key&gt;
 *                 &lt;dict&gt;
 *                     &lt;key&gt;node3&lt;/key&gt;
 *                     &lt;string&gt;value&lt;/string&gt;
 *                 &lt;/dict&gt;
 *             &lt;/dict&gt;
 *         &lt;/dict&gt;
 *
 *     &lt;/dict&gt;
 * &lt;/plist&gt;
 * </pre>
 *
 * @since 1.2
 *
 * @author Emmanuel Bourg
 */
public class XMLPropertyListConfiguration extends BaseHierarchicalConfiguration
    implements FileBasedConfiguration, FileLocatorAware
{
    /** Size of the indentation for the generated file. */
    private static final int INDENT_SIZE = 4;

    /** Constant for the encoding for binary data. */
    private static final String DATA_ENCODING = "UTF-8";

    /** Temporarily stores the current file location. */
    private FileLocator locator;

    /**
     * Creates an empty XMLPropertyListConfiguration object which can be
     * used to synthesize a new plist file by adding values and
     * then saving().
     */
    public XMLPropertyListConfiguration()
    {
    }

    /**
     * Creates a new instance of {@code XMLPropertyListConfiguration} and
     * copies the content of the specified configuration into this object.
     *
     * @param configuration the configuration to copy
     * @since 1.4
     */
    public XMLPropertyListConfiguration(final HierarchicalConfiguration<ImmutableNode> configuration)
    {
        super(configuration);
    }

    /**
     * Creates a new instance of {@code XMLPropertyConfiguration} with the given
     * root node.
     *
     * @param root the root node
     */
    XMLPropertyListConfiguration(final ImmutableNode root)
    {
        super(new InMemoryNodeModel(root));
    }

    @Override
    protected void setPropertyInternal(final String key, final Object value)
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
    protected void addPropertyInternal(final String key, final Object value)
    {
        if (value instanceof byte[] || value instanceof List)
        {
            addPropertyDirect(key, value);
        }
        else if (value instanceof Object[])
        {
            addPropertyDirect(key, Arrays.asList((Object[]) value));
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
    public void initFileLocator(final FileLocator locator)
    {
        this.locator = locator;
    }

    @Override
    public void read(final Reader in) throws ConfigurationException
    {
        // set up the DTD validation
        final EntityResolver resolver = new EntityResolver()
        {
            @Override
            public InputSource resolveEntity(final String publicId, final String systemId)
            {
                return new InputSource(getClass().getClassLoader()
                        .getResourceAsStream("PropertyList-1.0.dtd"));
            }
        };

        // parse the file
        final XMLPropertyListHandler handler = new XMLPropertyListHandler();
        try
        {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);

            final SAXParser parser = factory.newSAXParser();
            parser.getXMLReader().setEntityResolver(resolver);
            parser.getXMLReader().setContentHandler(handler);
            parser.getXMLReader().parse(new InputSource(in));

            getNodeModel().mergeRoot(handler.getResultBuilder().createNode(),
                    null, null, null, this);
        }
        catch (final Exception e)
        {
            throw new ConfigurationException(
                    "Unable to parse the configuration file", e);
        }
    }

    @Override
    public void write(final Writer out) throws ConfigurationException
    {
        if (locator == null)
        {
            throw new ConfigurationException("Save operation not properly "
                    + "initialized! Do not call write(Writer) directly,"
                    + " but use a FileHandler to save a configuration.");
        }
        final PrintWriter writer = new PrintWriter(out);

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

        printNode(writer, 1, getNodeModel().getNodeHandler().getRootNode());

        writer.println("</plist>");
        writer.flush();
    }

    /**
     * Append a node to the writer, indented according to a specific level.
     */
    private void printNode(final PrintWriter out, final int indentLevel, final ImmutableNode node)
    {
        final String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (node.getNodeName() != null)
        {
            out.println(padding + "<key>" + StringEscapeUtils.escapeXml10(node.getNodeName()) + "</key>");
        }

        final List<ImmutableNode> children = node.getChildren();
        if (!children.isEmpty())
        {
            out.println(padding + "<dict>");

            final Iterator<ImmutableNode> it = children.iterator();
            while (it.hasNext())
            {
                final ImmutableNode child = it.next();
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
            final Object value = node.getValue();
            printValue(out, indentLevel, value);
        }
    }

    /**
     * Append a value to the writer, indented according to a specific level.
     */
    private void printValue(final PrintWriter out, final int indentLevel, final Object value)
    {
        final String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (value instanceof Date)
        {
            synchronized (PListNodeBuilder.FORMAT)
            {
                out.println(padding + "<date>" + PListNodeBuilder.FORMAT.format((Date) value) + "</date>");
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
            for (final Object o : (List<?>) value)
            {
                printValue(out, indentLevel + 1, o);
            }
            out.println(padding + "</array>");
        }
        else if (value instanceof HierarchicalConfiguration)
        {
            // This is safe because we have created this configuration
            @SuppressWarnings("unchecked")
            final
            HierarchicalConfiguration<ImmutableNode> config =
                    (HierarchicalConfiguration<ImmutableNode>) value;
            printNode(out, indentLevel, config.getNodeModel().getNodeHandler()
                    .getRootNode());
        }
        else if (value instanceof ImmutableConfiguration)
        {
            // display a flat Configuration as a dictionary
            out.println(padding + "<dict>");

            final ImmutableConfiguration config = (ImmutableConfiguration) value;
            final Iterator<String> it = config.getKeys();
            while (it.hasNext())
            {
                // create a node for each property
                final String key = it.next();
                final ImmutableNode node =
                        new ImmutableNode.Builder().name(key)
                                .value(config.getProperty(key)).create();

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
            final Map<String, Object> map = transformMap((Map<?, ?>) value);
            printValue(out, indentLevel, new MapConfiguration(map));
        }
        else if (value instanceof byte[])
        {
            String base64;
            try
            {
                base64 = new String(Base64.encodeBase64((byte[]) value), DATA_ENCODING);
            }
            catch (final UnsupportedEncodingException e)
            {
                // Cannot happen as UTF-8 is a standard encoding
                throw new AssertionError(e);
            }
            out.println(padding + "<data>" + StringEscapeUtils.escapeXml10(base64) + "</data>");
        }
        else if (value != null)
        {
            out.println(padding + "<string>" + StringEscapeUtils.escapeXml10(String.valueOf(value)) + "</string>");
        }
        else
        {
            out.println(padding + "<string/>");
        }
    }

    /**
     * Transform a map of arbitrary types into a map with string keys and object
     * values. All keys of the source map which are not of type String are
     * dropped.
     *
     * @param src the map to be converted
     * @return the resulting map
     */
    private static Map<String, Object> transformMap(final Map<?, ?> src)
    {
        final Map<String, Object> dest = new HashMap<>();
        for (final Map.Entry<?, ?> e : src.entrySet())
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
        private final List<PListNodeBuilder> stack = new ArrayList<>();

        /** The builder for the resulting node. */
        private final PListNodeBuilder resultBuilder;

        public XMLPropertyListHandler()
        {
            resultBuilder = new PListNodeBuilder();
            push(resultBuilder);
        }

        /**
         * Returns the builder for the result node.
         *
         * @return the result node builder
         */
        public PListNodeBuilder getResultBuilder()
        {
            return resultBuilder;
        }

        /**
         * Return the node on the top of the stack.
         */
        private PListNodeBuilder peek()
        {
            if (!stack.isEmpty())
            {
                return stack.get(stack.size() - 1);
            }
            return null;
        }

        /**
         * Returns the node on top of the non-empty stack. Throws an exception if the
         * stack is empty.
         *
         * @return the top node of the stack
         * @throws ConfigurationRuntimeException if the stack is empty
         */
        private PListNodeBuilder peekNE()
        {
            final PListNodeBuilder result = peek();
            if (result == null)
            {
                throw new ConfigurationRuntimeException("Access to empty stack!");
            }
            return result;
        }

        /**
         * Remove and return the node on the top of the stack.
         */
        private PListNodeBuilder pop()
        {
            if (!stack.isEmpty())
            {
                return stack.remove(stack.size() - 1);
            }
            return null;
        }

        /**
         * Put a node on the top of the stack.
         */
        private void push(final PListNodeBuilder node)
        {
            stack.add(node);
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
        {
            if ("array".equals(qName))
            {
                push(new ArrayNodeBuilder());
            }
            else if ("dict".equals(qName))
            {
                if (peek() instanceof ArrayNodeBuilder)
                {
                    // push the new root builder on the stack
                    push(new PListNodeBuilder());
                }
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException
        {
            if ("key".equals(qName))
            {
                // create a new node, link it to its parent and push it on the stack
                final PListNodeBuilder node = new PListNodeBuilder();
                node.setName(buffer.toString());
                peekNE().addChild(node);
                push(node);
            }
            else if ("dict".equals(qName))
            {
                // remove the root of the XMLPropertyListConfiguration previously pushed on the stack
                final PListNodeBuilder builder = pop();
                assert builder != null : "Stack was empty!";
                if (peek() instanceof ArrayNodeBuilder)
                {
                    // create the configuration
                    final XMLPropertyListConfiguration config = new XMLPropertyListConfiguration(builder.createNode());

                    // add it to the ArrayNodeBuilder
                    final ArrayNodeBuilder node = (ArrayNodeBuilder) peekNE();
                    node.addValue(config);
                }
            }
            else
            {
                if ("string".equals(qName))
                {
                    peekNE().addValue(buffer.toString());
                }
                else if ("integer".equals(qName))
                {
                    peekNE().addIntegerValue(buffer.toString());
                }
                else if ("real".equals(qName))
                {
                    peekNE().addRealValue(buffer.toString());
                }
                else if ("true".equals(qName))
                {
                    peekNE().addTrueValue();
                }
                else if ("false".equals(qName))
                {
                    peekNE().addFalseValue();
                }
                else if ("data".equals(qName))
                {
                    peekNE().addDataValue(buffer.toString());
                }
                else if ("date".equals(qName))
                {
                    try
                    {
                        peekNE().addDateValue(buffer.toString());
                    }
                    catch (final IllegalArgumentException iex)
                    {
                        getLogger().warn(
                                "Ignoring invalid date property " + buffer);
                    }
                }
                else if ("array".equals(qName))
                {
                    final ArrayNodeBuilder array = (ArrayNodeBuilder) pop();
                    peekNE().addList(array);
                }

                // remove the plist node on the stack once the value has been parsed,
                // array nodes remains on the stack for the next values in the list
                if (!(peek() instanceof ArrayNodeBuilder))
                {
                    pop();
                }
            }

            buffer.setLength(0);
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException
        {
            buffer.append(ch, start, length);
        }
    }

    /**
     * A specialized builder class with addXXX methods to parse the typed data passed by the SAX handler.
     * It is used for creating the nodes of the configuration.
     */
    private static class PListNodeBuilder
    {
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

        /** A collection with child builders of this builder. */
        private final Collection<PListNodeBuilder> childBuilders =
                new LinkedList<>();

        /** The name of the represented node. */
        private String name;

        /** The current value of the represented node. */
        private Object value;

        /**
         * Update the value of the node. If the existing value is null, it's
         * replaced with the new value. If the existing value is a list, the
         * specified value is appended to the list. If the existing value is
         * not null, a list with the two values is built.
         *
         * @param v the value to be added
         */
        public void addValue(final Object v)
        {
            if (value == null)
            {
                value = v;
            }
            else if (value instanceof Collection)
            {
                // This is safe because we create the collections ourselves
                @SuppressWarnings("unchecked")
                final
                Collection<Object> collection = (Collection<Object>) value;
                collection.add(v);
            }
            else
            {
                final List<Object> list = new ArrayList<>();
                list.add(value);
                list.add(v);
                value = list;
            }
        }

        /**
         * Parse the specified string as a date and add it to the values of the node.
         *
         * @param value the value to be added
         * @throws IllegalArgumentException if the date string cannot be parsed
         */
        public void addDateValue(final String value)
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
            catch (final ParseException e)
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
        public void addDataValue(final String value)
        {
            try
            {
                addValue(Base64.decodeBase64(value.getBytes(DATA_ENCODING)));
            }
            catch (final UnsupportedEncodingException e)
            {
                //Cannot happen as UTF-8 is a default encoding
                throw new AssertionError(e);
            }
        }

        /**
         * Parse the specified string as an Interger and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addIntegerValue(final String value)
        {
            addValue(new BigInteger(value));
        }

        /**
         * Parse the specified string as a Double and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addRealValue(final String value)
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
        public void addList(final ArrayNodeBuilder node)
        {
            addValue(node.getNodeValue());
        }

        /**
         * Sets the name of the represented node.
         *
         * @param nodeName the node name
         */
        public void setName(final String nodeName)
        {
            name = nodeName;
        }

        /**
         * Adds the given child builder to this builder.
         *
         * @param child the child builder to be added
         */
        public void addChild(final PListNodeBuilder child)
        {
            childBuilders.add(child);
        }

        /**
         * Creates the configuration node defined by this builder.
         *
         * @return the newly created configuration node
         */
        public ImmutableNode createNode()
        {
            final ImmutableNode.Builder nodeBuilder =
                    new ImmutableNode.Builder(childBuilders.size());
            for (final PListNodeBuilder child : childBuilders)
            {
                nodeBuilder.addChild(child.createNode());
            }
            return nodeBuilder.name(name).value(getNodeValue()).create();
        }

        /**
         * Returns the final value for the node to be created. This method is
         * called when the represented configuration node is actually created.
         *
         * @return the value of the resulting configuration node
         */
        protected Object getNodeValue()
        {
            return value;
        }
    }

    /**
     * Container for array elements. <b>Do not use this class !</b>
     * It is used internally by XMLPropertyConfiguration to parse the
     * configuration file, it may be removed at any moment in the future.
     */
    private static class ArrayNodeBuilder extends PListNodeBuilder
    {
        /** The list of values in the array. */
        private final List<Object> list = new ArrayList<>();

        /**
         * Add an object to the array.
         *
         * @param value the value to be added
         */
        @Override
        public void addValue(final Object value)
        {
            list.add(value);
        }

        /**
         * Return the list of values in the array.
         *
         * @return the {@link List} of values
         */
        @Override
        protected Object getNodeValue()
        {
            return list;
        }
    }
}
