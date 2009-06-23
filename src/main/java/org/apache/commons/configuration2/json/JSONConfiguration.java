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

package org.apache.commons.configuration2.json;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.AbstractHierarchicalConfiguration;
import org.apache.commons.configuration2.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Configuration based on the JavaScript Object Notation format (JSON).
 * 
 * <p>The file must begin with an object declaration, it can't start 
 * with an array declaration.</p>
 * 
 * <pre>
 * {
 *     "string"  : "foo",
 *     "number"  : 123456,
 *     "boolean" : true,
 *     "null"    : null,
 *     "array"   : [ 1, 2, 3 ],
 *     "map"     : { "foo" : "bar" }
 * }
 * </pre> 
 * 
 * <p>null values are ignored and removed when the configuration is saved.</p>
 * 
 * @see <a href="http://www.json.org">JavaScript Object Notation homepage</a>
 * @see <a href="http://tools.ietf.org/html/rfc4627">RFC 4627</a>
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.7
 */
public class JSONConfiguration extends AbstractHierarchicalFileConfiguration
{
    /** Size of the indentation for the generated file. */
    private static final int INDENT_SIZE = 4;
    
    /** The default encoding */
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    // initialization block to set the encoding before loading the file in the constructors
    {
        setEncoding(DEFAULT_ENCODING);
    }

    /**
     * Creates an empty JSONConfiguration object which can be
     * used to synthesize a new json file by adding values and
     * then saving().
     */
    public JSONConfiguration()
    {
    }

    /**
     * Creates a new instance of <code>JSONConfiguration</code> and
     * copies the content of the specified configuration into this object.
     *
     * @param c the configuration to copy
     */
    public JSONConfiguration(AbstractHierarchicalConfiguration<? extends ConfigurationNode> c)
    {
        super(c);
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param fileName The name of the json file to load.
     * @throws ConfigurationException Error while loading the json file
     */
    public JSONConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param file The json file to load.
     * @throws ConfigurationException Error while loading the json file
     */
    public JSONConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Creates and loads the configuration from the specified URL.
     *
     * @param url The location of the json file to load.
     * @throws ConfigurationException Error while loading the json file
     */
    public JSONConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }
    
    public void load(Reader in) throws ConfigurationException
    {
        JSONParser parser = new JSONParser(in);
        try
        {
            AbstractHierarchicalConfiguration<ConfigurationNode> config = parser.parse();
            setRootNode(config.getRootNode());
        }
        catch (ParseException e)
        {
            throw new ConfigurationException(e);
        }
    }

    public void save(Writer out) throws ConfigurationException
    {
        PrintWriter writer = new PrintWriter(out);
        printNode(writer, 0, getRootNode());
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
            out.print(padding + quoteString(node.getName()) + " : ");
        }

        List<ConfigurationNode> children = new ArrayList<ConfigurationNode>(node.getChildren());
        if (!children.isEmpty())
        {
            // skip a line, except for the root object
            if (indentLevel > 0)
            {
                out.println();
            }

            out.println(padding + "{");

            // display the children
            Iterator<ConfigurationNode> it = children.iterator();
            while (it.hasNext())
            {
                ConfigurationNode child = it.next();

                printNode(out, indentLevel + 1, child);

                if (it.hasNext())
                {
                    out.println(",");
                }
            }

            out.println();
            out.print(padding + "}");
        }
        else if (node.getValue() == null)
        {
            out.print("{ }");
        }
        else
        {
            // display the leaf value
            printValue(out, indentLevel, node.getValue());
        }
    }

    /**
     * Append a value to the writer, indented according to a specific level.
     */
    private void printValue(PrintWriter out, int indentLevel, Object value)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (value instanceof List)
        {
            out.print("[");
            Iterator it = ((List) value).iterator();
            while (it.hasNext())
            {
                out.print(" ");
                printValue(out, indentLevel + 1, it.next());
                if (it.hasNext())
                {
                    out.print(",");
                }
            }
            out.print(" ]");
        }
        else if (value instanceof AbstractHierarchicalConfiguration)
        {
            printNode(out, indentLevel, ((AbstractHierarchicalConfiguration<ConfigurationNode>) value).getRootNode());
        }
        else if (value instanceof Configuration)
        {
            // display a flat Configuration as a map
            Configuration config = (Configuration) value;
            Iterator it = config.getKeys();
            
            if (!it.hasNext())
            {
                out.print("{ }");
            }
            else
            {
                out.println();
                out.println(padding + "{");

                while (it.hasNext())
                {
                    String key = (String) it.next();
                    ConfigurationNode node = new DefaultConfigurationNode(key);
                    node.setValue(config.getProperty(key));

                    printNode(out, indentLevel + 1, node);
                    if (it.hasNext())
                    {
                        out.println(",");
                    }
                }
                out.println();
                out.print(padding + "}");
            }
            
        }
        else if (value instanceof Map)
        {
            Map map = (Map) value;
            printValue(out, indentLevel, new MapConfiguration(map));
        }
        else if (value instanceof Number || value instanceof Boolean)
        {
            out.print(value.toString());
        }
        else if (value != null)
        {
            out.print(quoteString(String.valueOf(value)));
        }
    }

    /**
     * Escape the characters using the Java String rules
     * and surround the result with double quotes.
     */
    String quoteString(String s)
    {
        if (s == null)
        {
            return null;
        }

        return '"' + StringEscapeUtils.escapeJava(s) + '"';
    }
}
