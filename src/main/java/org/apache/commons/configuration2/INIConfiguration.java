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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A specialized hierarchical configuration implementation for parsing ini
 * files.
 * </p>
 * <p>
 * An initialization or ini file is a configuration file typically found on
 * Microsoft's Windows operating system and contains data for Windows based
 * applications.
 * </p>
 * <p>
 * Although popularized by Windows, ini files can be used on any system or
 * platform due to the fact that they are merely text files that can easily be
 * parsed and modified by both humans and computers.
 * </p>
 * <p>
 * A typcial ini file could look something like:
 * </p>
 * <code>
 * [section1]<br>
 * ; this is a comment!<br>
 * var1 = foo<br>
 * var2 = bar<br>
 * <br>
 * [section2]<br>
 * var1 = doo<br>
 * </code>
 * <p>
 * The format of ini files is fairly straight forward and is composed of three
 * components:<br>
 * <ul>
 * <li><b>Sections:</b> Ini files are split into sections, each section starting
 * with a section declaration. A section declaration starts with a '[' and ends
 * with a ']'. Sections occur on one line only.</li>
 * <li><b>Parameters:</b> Items in a section are known as parameters. Parameters
 * have a typical <code>key = value</code> format.</li>
 * <li><b>Comments:</b> Lines starting with a ';' are assumed to be comments.</li>
 * </ul>
 * </p>
 * <p>
 * There are various implementations of the ini file format by various vendors
 * which has caused a number of differences to appear. As far as possible this
 * configuration tries to be lenient and support most of the differences.
 * </p>
 * <p>
 * Some of the differences supported are as follows:
 * <ul>
 * <li><b>Comments:</b> The '#' character is also accepted as a comment
 * signifier.</li>
 * <li><b>Key value separtor:</b> The ':' character is also accepted in place of
 * '=' to separate keys and values in parameters, for example
 * <code>var1 : foo</code>.</li>
 * <li><b>Duplicate sections:</b> Typically duplicate sections are not allowed,
 * this configuration does however support it. In the event of a duplicate
 * section, the two section's values are merged.</li>
 * <li><b>Duplicate parameters:</b> Typically duplicate parameters are only
 * allowed if they are in two different sections, thus they are local to
 * sections; this configuration simply merges duplicates; if a section has a
 * duplicate parameter the values are then added to the key as a list.</li>
 * </ul>
 * </p>
 * <p>
 * Global parameters are also allowed; any parameters declared before a section
 * is declared are added to a global section. It is important to note that this
 * global section does not have a name.
 * </p>
 * <p>
 * In all instances, a parameter's key is prepended with its section name and a
 * '.' (period). Thus a parameter named "var1" in "section1" will have the key
 * <code>section1.var1</code> in this configuration. (This is the default
 * behavior. Because this is a hierarchical configuration you can change this by
 * setting a different {@link ExpressionEngine}.)
 * </p>
 * <p>
 * <h3>Implementation Details:</h3> Consider the following ini file:<br>
 * <code>
 *  default = ok<br>
 *  <br>
 *  [section1]<br>
 *  var1 = foo<br>
 *  var2 = doodle<br>
 *   <br>
 *  [section2]<br>
 *  ; a comment<br>
 *  var1 = baz<br>
 *  var2 = shoodle<br>
 *  bad =<br>
 *  = worse<br>
 *  <br>
 *  [section3]<br>
 *  # another comment<br>
 *  var1 : foo<br>
 *  var2 : bar<br>
 *  var5 : test1<br>
 *  <br>
 *  [section3]<br>
 *  var3 = foo<br>
 *  var4 = bar<br>
 *  var5 = test2<br>
 *  </code>
 * </p>
 * <p>
 * This ini file will be parsed without error. Note:
 * <ul>
 * <li>The parameter named "default" is added to the global section, it's value
 * is accessed simply using <code>getProperty("default")</code>.</li>
 * <li>Section 1's parameters can be accessed using
 * <code>getProperty("section1.var1")</code>.</li>
 * <li>The parameter named "bad" simply adds the parameter with an empty value.</li>
 * <li>The empty key with value "= worse" is added using a key consisting of a
 * single space character. This key is still added to section 2 and the value
 * can be accessed using <code>getProperty("section2. ")</code>, notice the
 * period '.' and the space following the section name.</li>
 * <li>Section three uses both '=' and ':' to separate keys and values.</li>
 * <li>Section 3 has a duplicate key named "var5". The value for this key is
 * [test1, test2], and is represented as a List.</li>
 * </ul>
 * </p>
 * <p>
 * Internally, this configuration maps the content of the represented ini file
 * to its node structure in the following way:
 * <ul>
 * <li>Sections are represented by direct child nodes of the root node.</li>
 * <li>For the content of a section, corresponding nodes are created as children
 * of the section node.</li>
 * </ul>
 * This explains how the keys for the properties can be constructed. You can
 * also use other methods of {@link InMemoryConfiguration} for querying or
 * manipulating the hierarchy of configuration nodes, for instance the
 * {@code configurationAt()} method for obtaining the data of a specific
 * section.
 * </p>
 * <p>
 * The set of sections in this configuration can be retrieved using the
 * {@code getSections()} method. For obtaining a {@link SubConfiguration}
 * with the content of a specific section the {@code getSection()} method can be used.
 * </p>
 * <p>
 * <em>Note:</em> Configuration objects of this type can be read concurrently by
 * multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class INIConfiguration extends AbstractHierarchicalFileConfiguration
{
    /**
     * The characters that signal the start of a comment line.
     */
    protected static final String COMMENT_CHARS = "#;";

    /**
     * The characters used to separate keys from values.
     */
    protected static final String SEPARATOR_CHARS = "=:";

    /**
     * Constant for the line separator.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * The line continuation character.
     */
    private static final String LINE_CONT = "\\";

    /**
     * Create a new empty INI Configuration.
     */
    public INIConfiguration()
    {
        super();
    }

    /**
     * Create and load the ini configuration from the given file.
     *
     * @param filename The name pr path of the ini file to load.
     * @throws ConfigurationException If an error occurs while loading the file
     */
    public INIConfiguration(String filename) throws ConfigurationException
    {
        super(filename);
    }

    /**
     * Create and load the ini configuration from the given file.
     *
     * @param file The ini file to load.
     * @throws ConfigurationException If an error occurs while loading the file
     */
    public INIConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Create and load the ini configuration from the given url.
     *
     * @param url The url of the ini file to load.
     * @throws ConfigurationException If an error occurs while loading the file
     */
    public INIConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }

    /**
     * Save the configuration to the specified writer.
     *
     * @param writer - The writer to save the configuration to.
     * @throws ConfigurationException If an error occurs while writing the
     *         configuration
     */
    public void save(Writer writer) throws ConfigurationException
    {
        PrintWriter out = new PrintWriter(writer);
        for (String section : getSections())
        {
            Configuration subset;
            if (section != null)
            {
                out.print("[");
                out.print(section);
                out.print("]");
                out.println();
                subset = createSubnodeConfiguration(getSectionNode(section));
            }
            else
            {
                subset = getSection(null);
            }

            Iterator<String> keys = subset.getKeys();
            while (keys.hasNext())
            {
                String key = keys.next();
                Object value = subset.getProperty(key);
                if (value instanceof Collection<?>)
                {
                    for (Object v : (Collection<?>) value)
                    {
                        out.print(key);
                        out.print(" = ");
                        out.print(formatValue(String.valueOf(v)));
                        out.println();
                    }
                }
                else
                {
                    out.print(key);
                    out.print(" = ");
                    out.print(formatValue(value.toString()));
                    out.println();
                }
            }

            out.println();
        }

        out.flush();
    }

    /**
     * Load the configuration from the given reader. Note that the
     * <code>clear</code> method is not called so the configuration read in will
     * be merged with the current configuration.
     *
     * @param reader The reader to read the configuration from.
     * @throws ConfigurationException If an error occurs while reading the
     *         configuration
     */
    public void load(Reader reader) throws ConfigurationException
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(reader);
            ConfigurationNode sectionNode = getRootNode();

            String line = bufferedReader.readLine();
            while (line != null)
            {
                line = line.trim();
                if (!isCommentLine(line))
                {
                    if (isSectionLine(line))
                    {
                        String section = line.substring(1, line.length() - 1);
                        sectionNode = getSectionNode(section);
                    }

                    else
                    {
                        String key = "";
                        String value = "";
                        int index = line.indexOf("=");
                        if (index >= 0)
                        {
                            key = line.substring(0, index);
                            value = parseValue(line.substring(index + 1), bufferedReader);
                        }
                        else
                        {
                            index = line.indexOf(":");
                            if (index >= 0)
                            {
                                key = line.substring(0, index);
                                value = parseValue(line.substring(index + 1), bufferedReader);
                            }
                            else
                            {
                                key = line;
                            }
                        }
                        key = key.trim();
                        if (key.length() < 1)
                        {
                            // use space for properties with no key
                            key = " ";
                        }
                        createNode(sectionNode, key, value);
                    }
                }

                line = bufferedReader.readLine();
            }
        }
        catch (IOException e)
        {
            throw new ConfigurationException(
                    "Unable to load the configuration", e);
        }
    }

    /**
     * Parse the value to remove the quotes and ignoring the comment. Example:
     *
     * <pre>
     * &quot;value&quot; ; comment -&gt; value
     * </pre>
     *
     * <pre>
     * 'value' ; comment -&gt; value
     * </pre>
     * Note that a comment character is only recognized if there is at least one
     * whitespace character before it. So it can appear in the property value,
     * e.g.:
     * <pre>
     * C:\\Windows;C:\\Windows\\system32
     * </pre>
     *
     * @param val the value to be parsed
     * @param reader the reader (needed if multiple lines have to be read)
     * @throws IOException if an IO error occurs
     */
    private static String parseValue(String val, BufferedReader reader) throws IOException
    {
        StringBuilder propertyValue = new StringBuilder();
        boolean lineContinues;
        String value = val.trim();

        do
        {
            boolean quoted = value.startsWith("\"") || value.startsWith("'");
            boolean stop = false;
            boolean escape = false;

            char quote = quoted ? value.charAt(0) : 0;

            int i = quoted ? 1 : 0;

            StringBuilder result = new StringBuilder();
            char lastChar = 0;
            while (i < value.length() && !stop)
            {
                char c = value.charAt(i);

                if (quoted)
                {
                    if ('\\' == c && !escape)
                    {
                        escape = true;
                    }
                    else if (!escape && quote == c)
                    {
                        stop = true;
                    }
                    else if (escape && quote == c)
                    {
                        escape = false;
                        result.append(c);
                    }
                    else
                    {
                        if (escape)
                        {
                            escape = false;
                            result.append('\\');
                        }

                        result.append(c);
                    }
                }
                else
                {
                    if (isCommentChar(c) && Character.isWhitespace(lastChar))
                    {
                        stop = true;
                    }
                    else
                    {
                        result.append(c);
                    }
                }

                i++;
                lastChar = c;
            }

            String v = result.toString();
            if (!quoted)
            {
                v = v.trim();
                lineContinues = lineContinues(v);
                if (lineContinues)
                {
                    // remove trailing "\"
                    v = v.substring(0, v.length() - 1).trim();
                }
            }
            else
            {
                lineContinues = lineContinues(value, i);
            }
            propertyValue.append(v);

            if (lineContinues)
            {
                propertyValue.append(LINE_SEPARATOR);
                value = reader.readLine();
            }
        } while (lineContinues && value != null);

        return propertyValue.toString();
    }

    /**
     * Tests whether the specified string contains a line continuation marker.
     *
     * @param line the string to check
     * @return a flag whether this line continues
     */
    private static boolean lineContinues(String line)
    {
        String s = line.trim();
        return s.equals(LINE_CONT)
                || (s.length() > 2 && s.endsWith(LINE_CONT) && Character
                        .isWhitespace(s.charAt(s.length() - 2)));
    }

    /**
     * Tests whether the specified string contains a line continuation marker
     * after the specified position. This method parses the string to remove a
     * comment that might be present. Then it checks whether a line continuation
     * marker can be found at the end.
     *
     * @param line the line to check
     * @param pos the start position
     * @return a flag whether this line continues
     */
    private static boolean lineContinues(String line, int pos)
    {
        String s;

        if (pos >= line.length())
        {
            s = line;
        }
        else
        {
            int end = pos;
            while (end < line.length() && !isCommentChar(line.charAt(end)))
            {
                end++;
            }
            s = line.substring(pos, end);
        }

        return lineContinues(s);
    }

    /**
     * Tests whether the specified character is a comment character.
     *
     * @param c the character
     * @return a flag whether this character starts a comment
     */
    private static boolean isCommentChar(char c)
    {
        return COMMENT_CHARS.indexOf(c) >= 0;
    }

    /**
     * Add quotes around the specified value if it contains a comment character.
     */
    private String formatValue(String value)
    {
        boolean quoted = false;

        for (int i = 0; i < COMMENT_CHARS.length() && !quoted; i++)
        {
            char c = COMMENT_CHARS.charAt(i);
            if (value.indexOf(c) != -1)
            {
                quoted = true;
            }
        }

        if (quoted)
        {
            return '"' + StringUtils.replace(value, "\"", "\\\"") + '"';
        }
        else
        {
            return value;
        }
    }

    /**
     * Determine if the given line is a comment line.
     *
     * @param line The line to check.
     * @return true if the line is empty or starts with one of the comment
     *         characters
     */
    protected boolean isCommentLine(String line)
    {
        if (line == null)
        {
            return false;
        }
        // blank lines are also treated as comment lines
        return line.length() < 1 || COMMENT_CHARS.indexOf(line.charAt(0)) >= 0;
    }

    /**
     * Determine if the given line is a section.
     *
     * @param line The line to check.
     * @return true if the line contains a secion
     */
    protected boolean isSectionLine(String line)
    {
        if (line == null)
        {
            return false;
        }
        return line.startsWith("[") && line.endsWith("]");
    }

    /**
     * Return a set containing the sections in this ini configuration. Note that
     * changes to this set do not affect the configuration.
     *
     * @return a set containing the sections.
     */
    public Set<String> getSections()
    {
        Set<String> sections = new LinkedHashSet<String>();
        boolean globalSection = false;
        boolean inSection = false;

        for (ConfigurationNode node : getRootNode().getChildren())
        {
            if (isSectionNode(node))
            {
                inSection = true;
                sections.add(node.getName());
            }
            else
            {
                if(!inSection && !globalSection)
                {
                    globalSection = true;
                    sections.add(null);
                }
            }
        }

        return sections;
    }

    /**
     * Returns a configuration with the content of the specified section. This
     * provides an easy way of working with a single section only. The way this
     * configuration is structured internally, this method is very similar to
     * calling
     * {@link AbstractHierarchicalConfiguration#configurationAt(String)}
     * with the name of the section in question. There are the following
     * differences however:
     * <ul>
     * <li>This method never throws an exception. If the section does not exist,
     * an empty configuration is returned.</li>
     * <li>There is special support for the global section: Passing in
     * <b>null</b> as section name returns a configuration with the content of
     * the global section (which may also be empty).</li>
     * </ul>
     *
     * @param name the name of the section in question; <b>null</b> represents
     *        the global section
     * @return a configuration containing only the properties of the specified
     *         section
     */
    public SubConfiguration<ConfigurationNode> getSection(String name)
    {
        if (name == null)
        {
            return getGlobalSection();
        }

        else
        {
            try
            {
                return configurationAt(name);
            }
            catch (IllegalArgumentException iex)
            {
                // the passed in key does not map to exactly one node
                // return an empty configuration
                return new SubConfiguration<ConfigurationNode>(this,
                        new DefaultConfigurationNode());
            }
        }
    }

    /**
     * Obtains the node representing the specified section. This method is
     * called while the configuration is loaded. If a node for this section
     * already exists, it is returned. Otherwise a new node is created.
     *
     * @param sectionName the name of the section
     * @return the node for this section
     */
    private ConfigurationNode getSectionNode(String sectionName)
    {
        List<ConfigurationNode> nodes = getRootNode().getChildren(sectionName);
        if (!nodes.isEmpty())
        {
            return nodes.get(0);
        }

        ConfigurationNode node = createNode(getRootNode(), sectionName);
        markSectionNode(node);
        return node;
    }

    /**
     * Creates a sub configuration for the global section of the represented INI
     * configuration.
     *
     * @return the sub configuration for the global section
     */
    private SubConfiguration<ConfigurationNode> getGlobalSection()
    {
        ConfigurationNode parent = new DefaultConfigurationNode()
        {
            /**
             * Adds the specified child node to this node. This implementation
             * does not change the parent node of the child. So the child node
             * remains in its original hierarchy.
             *
             * @param child the child node to be added
             */
            @Override
            public void addChild(ConfigurationNode child)
            {
                synchronized (child)
                {
                    ConfigurationNode parent = child.getParentNode();
                    super.addChild(child);
                    child.setParentNode(parent);
                }
            }
        };

        for (ConfigurationNode node : getRootNode().getChildren())
        {
            if (!isSectionNode(node))
            {
                parent.addChild(node);
            }
        }

        return createSubnodeConfiguration(parent);
    }

    /**
     * Marks a configuration node as a section node. This means that this node
     * represents a section header. This implementation uses the node's
     * reference property to store a flag.
     *
     * @param node the node to be marked
     */
    private static void markSectionNode(ConfigurationNode node)
    {
        node.setReference(Boolean.TRUE);
    }

    /**
     * Checks whether the specified configuration node represents a section.
     *
     * @param node the node in question
     * @return a flag whether this node represents a section
     */
    private static boolean isSectionNode(ConfigurationNode node)
    {
        return node.getReference() != null || node.getChildrenCount() > 0;
    }
}
