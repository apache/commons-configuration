/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.commons.configuration.plist;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.SetNextRule;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Mac OS X configuration file (http://www.apple.com/DTDs/PropertyList-1.0.dtd).
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
 *         &lt;date>2005-01-01T12:00:00-0700&lt;/date>
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
 * @version $Revision$, $Date$
 */
public class XMLPropertyListConfiguration extends AbstractHierarchicalFileConfiguration
{
    /** Size of the indentation for the generated file. */
    private static final int INDENT_SIZE = 4;

    /**
     * Creates an empty XMLPropertyListConfiguration object which can be
     * used to synthesize a new plist file by adding values and
     * then saving().
     */
    public XMLPropertyListConfiguration()
    {
    }

    /**
     * Creates and loads the property list from the specified file.
     *
     * @param fileName The name of the plist file to load.
     * @throws org.apache.commons.configuration.ConfigurationException Error while loading the plist file
     */
    public XMLPropertyListConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
    }

    /**
     * Creates and loads the property list from the specified file.
     *
     * @param file The plist file to load.
     * @throws ConfigurationException Error while loading the plist file
     */
    public XMLPropertyListConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Creates and loads the property list from the specified URL.
     *
     * @param url The location of the plist file to load.
     * @throws ConfigurationException Error while loading the plist file
     */
    public XMLPropertyListConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }

    public void load(Reader in) throws ConfigurationException
    {
        // set up the digester
        Digester digester = new Digester();

        // set up the DTD validation
        digester.setEntityResolver(new EntityResolver()
        {
            public InputSource resolveEntity(String publicId, String systemId)
            {
                return new InputSource(getClass().getClassLoader().getResourceAsStream("PropertyList-1.0.dtd"));
            }
        });
        digester.setValidating(true);

        // dictionary rules
        digester.addRule("*/key", new ObjectCreateRule(PListNode.class)
        {
            public void end() throws Exception
            {
                // leave the node on the stack to set the value
            }
        });

        digester.addCallMethod("*/key", "setName", 0);

        digester.addRule("*/dict/string", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/data", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/integer", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/real", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/true", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/false", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/date", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/dict", new SetNextAndPopRule("addChild"));

        digester.addCallMethod("*/dict/string", "addValue", 0);
        digester.addCallMethod("*/dict/data", "addDataValue", 0);
        digester.addCallMethod("*/dict/integer", "addIntegerValue", 0);
        digester.addCallMethod("*/dict/real", "addRealValue", 0);
        digester.addCallMethod("*/dict/true", "addTrueValue");
        digester.addCallMethod("*/dict/false", "addFalseValue");
        digester.addCallMethod("*/dict/date", "addDateValue", 0);

        // rules for arrays
        digester.addRule("*/dict/array", new SetNextAndPopRule("addChild"));
        digester.addRule("*/dict/array", new ObjectCreateRule(ArrayNode.class));
        digester.addSetNext("*/dict/array", "addList");

        digester.addRule("*/array/array", new ObjectCreateRule(ArrayNode.class));
        digester.addSetNext("*/array/array", "addList");

        digester.addCallMethod("*/array/string", "addValue", 0);
        digester.addCallMethod("*/array/data", "addDataValue", 0);
        digester.addCallMethod("*/array/integer", "addIntegerValue", 0);
        digester.addCallMethod("*/array/real", "addRealValue", 0);
        digester.addCallMethod("*/array/true", "addTrueValue");
        digester.addCallMethod("*/array/false", "addFalseValue");
        digester.addCallMethod("*/array/date", "addDateValue", 0);

        // rule for a dictionary in an array
        digester.addFactoryCreate("*/array/dict", new AbstractObjectCreationFactory()
        {
            public Object createObject(Attributes attributes) throws Exception
            {
                // create the configuration
                XMLPropertyListConfiguration config = new XMLPropertyListConfiguration();

                // add it to the ArrayNode
                ArrayNode node = (ArrayNode) getDigester().peek();
                node.addValue(config);

                // push the root on the stack
                return config.getRoot();
            }
        });

        // parse the file
        digester.push(getRoot());
        try
        {
            digester.parse(in);
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to parse the configuration file", e);
        }
    }

    /**
     * Digester rule that sets the object on the stack to the n-1 object
     * and remove both of them from the stack. This rule is used to remove
     * the configuration node from the stack once its value has been parsed.
     */
    private class SetNextAndPopRule extends SetNextRule
    {
        public SetNextAndPopRule(String methodName)
        {
            super(methodName);
        }

        public void end(String namespace, String name) throws Exception
        {
            super.end(namespace, name);
            digester.pop();
        }
    }

    public void save(Writer out) throws ConfigurationException
    {
        PrintWriter writer = new PrintWriter(out);

        if (getEncoding() != null)
        {
            writer.println("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\"?>");
        }
        else
        {
            writer.println("<?xml version=\"1.0\"?>");
        }

        writer.println("<!DOCTYPE plist SYSTEM \"file://localhost/System/Library/DTDs/PropertyList.dtd\">");
        writer.println("<plist version=\"1.0\">");

        printNode(writer, 1, getRoot());

        writer.println("</plist>");
        writer.flush();
    }

    /**
     * Append a node to the writer, indented according to a specific level.
     */
    private void printNode(PrintWriter out, int indentLevel, Node node)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (node.getName() != null)
        {
            out.println(padding + "<key>" + StringEscapeUtils.escapeXml(node.getName()) + "</key>");
        }

        List children = node.getChildren();
        if (!children.isEmpty())
        {
            out.println(padding + "<dict>");

            Iterator it = children.iterator();
            while (it.hasNext())
            {
                Node child = (Node) it.next();
                printNode(out, indentLevel + 1, child);

                if (it.hasNext())
                {
                    out.println();
                }
            }

            out.println(padding + "</dict>");
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
            out.println(padding + "<date>" + PListNode.format.format((Date) value) + "</date>");
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
            Iterator it = ((List) value).iterator();
            while (it.hasNext())
            {
                printValue(out, indentLevel + 1, it.next());
            }
            out.println(padding + "</array>");
        }
        else if (value instanceof HierarchicalConfiguration)
        {
            printNode(out, indentLevel, ((HierarchicalConfiguration) value).getRoot());
        }
        else if (value instanceof Configuration)
        {
            // display a flat Configuration as a dictionary
            out.println(padding + "<dict>");

            Configuration config = (Configuration) value;
            Iterator it = config.getKeys();
            while (it.hasNext())
            {
                // create a node for each property
                String key = (String) it.next();
                Node node = new Node(key);
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
            Map map = (Map) value;
            printValue(out, indentLevel, new MapConfiguration(map));
        }
        else if (value instanceof byte[])
        {
            String base64 = new String(Base64.encodeBase64((byte[]) value));
            out.println(padding + "<data>" + StringEscapeUtils.escapeXml(base64) + "</data>");
        }
        else
        {
            out.println(padding + "<string>" + StringEscapeUtils.escapeXml(String.valueOf(value)) + "</string>");
        }
    }


    /**
     * Node extension with addXXX methods to parse the typed data passed by Digester.
     * <b>Do not use this class !</b> It is used internally by XMLPropertyConfiguration
     * to parse the configuration file, it may be removed at any moment in the future.
     */
    public static class PListNode extends Node
    {
        /** The standard format of dates in plist files. */
        private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

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
            else if (getValue() instanceof List)
            {
                List list = (List) getValue();
                list.add(value);
            }
            else
            {
                List list = new ArrayList();
                list.add(getValue());
                list.add(value);
                setValue(list);
            }
        }

        /**
         * Parse the specified string as a date and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addDateValue(String value)
        {
            try
            {
                addValue(format.parse(value));
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Parse the specified string as a byte array in base 64 format
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
            addValue(new Integer(value));
        }

        /**
         * Parse the specified string as a Double and add it to the values of the node.
         *
         * @param value the value to be added
         */
        public void addRealValue(String value)
        {
            addValue(new Double(value));
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
        /** The list of values in the array. */
        private List list = new ArrayList();

        /**
         * Add an object to the array.
         *
         * @param value the value to be added
         */
        public void addValue(Object value)
        {
            list.add(value);
        }

        /**
         * Return the list of values in the array.
         *
         * @return the {@link List} of values
         */
        public Object getValue()
        {
            return list;
        }
    }
}
