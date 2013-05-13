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
package org.apache.commons.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ViewNode;

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
 * A typical ini file could look something like:
 * </p>
 * <pre>
 * [section1]
 * ; this is a comment!
 * var1 = foo
 * var2 = bar
 *
 * [section2]
 * var1 = doo
 * </pre>
 * <p>
 * The format of ini files is fairly straight forward and is composed of three
 * components:<br>
 * <ul>
 * <li><b>Sections:</b> Ini files are split into sections, each section starting
 * with a section declaration. A section declaration starts with a '[' and ends
 * with a ']'. Sections occur on one line only.</li>
 * <li><b>Parameters:</b> Items in a section are known as parameters. Parameters
 * have a typical {@code key = value} format.</li>
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
 * <li><b>Key value separator:</b> The ':' character is also accepted in place of
 * '=' to separate keys and values in parameters, for example
 * {@code var1 : foo}.</li>
 * <li><b>Duplicate sections:</b> Typically duplicate sections are not allowed,
 * this configuration does however support this feature. In the event of a duplicate
 * section, the two section's values are merged so that there is only a single
 * section. <strong>Note</strong>: This also affects the internal data of the
 * configuration. If it is saved, only a single section is written!</li>
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
 * {@code section1.var1} in this configuration. (This is the default
 * behavior. Because this is a hierarchical configuration you can change this by
 * setting a different {@link org.apache.commons.configuration.tree.ExpressionEngine}.)
 * </p>
 * <p>
 * <h3>Implementation Details:</h3> Consider the following ini file:<br>
 * <pre>
 *  default = ok
 *
 *  [section1]
 *  var1 = foo
 *  var2 = doodle
 *
 *  [section2]
 *  ; a comment
 *  var1 = baz
 *  var2 = shoodle
 *  bad =
 *  = worse
 *
 *  [section3]
 *  # another comment
 *  var1 : foo
 *  var2 : bar
 *  var5 : test1
 *
 *  [section3]
 *  var3 = foo
 *  var4 = bar
 *  var5 = test2
 *
 *  [sectionSeparators]
 *  passwd : abc=def
 *  a:b = "value"
 *  </pre>
 * </p>
 * <p>
 * This ini file will be parsed without error. Note:
 * <ul>
 * <li>The parameter named "default" is added to the global section, it's value
 * is accessed simply using {@code getProperty("default")}.</li>
 * <li>Section 1's parameters can be accessed using
 * {@code getProperty("section1.var1")}.</li>
 * <li>The parameter named "bad" simply adds the parameter with an empty value.</li>
 * <li>The empty key with value "= worse" is added using a key consisting of a
 * single space character. This key is still added to section 2 and the value
 * can be accessed using {@code getProperty("section2. ")}, notice the
 * period '.' and the space following the section name.</li>
 * <li>Section three uses both '=' and ':' to separate keys and values.</li>
 * <li>Section 3 has a duplicate key named "var5". The value for this key is
 * [test1, test2], and is represented as a List.</li>
 * <li>The section called <em>sectionSeparators</em> demonstrates how the
 * configuration deals with multiple occurrences of separator characters. Per
 * default the first separator character in a line is detected and used to
 * split the key from the value. Therefore the first property definition in this
 * section has the key {@code passwd} and the value {@code abc=def}.
 * This default behavior can be changed by using quotes. If there is a separator
 * character before the first quote character (ignoring whitespace), this
 * character is used as separator. Thus the second property definition in the
 * section has the key {@code a:b} and the value {@code value}.</li>
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
 * also use other methods of {@link HierarchicalConfiguration} for querying or
 * manipulating the hierarchy of configuration nodes, for instance the
 * {@code configurationAt()} method for obtaining the data of a specific
 * section. However, be careful that the storage scheme described above is not
 * violated (e.g. by adding multiple levels of nodes or inserting duplicate
 * section nodes). Otherwise, the special methods for ini configurations may not
 * work correctly!
 * </p>
 * <p>
 * The set of sections in this configuration can be retrieved using the
 * {@code getSections()} method. For obtaining a
 * {@code SubnodeConfiguration} with the content of a specific section the
 * {@code getSection()} method can be used.
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
 * @since 1.6
 */
public class INIConfiguration extends BaseHierarchicalConfiguration implements
        FileBasedConfiguration
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
     * The serial version UID.
     */
    private static final long serialVersionUID = 2548006161386850670L;

    /**
     * Constant for the line separator.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * The characters used for quoting values.
     */
    private static final String QUOTE_CHARACTERS = "\"'";

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
     * Creates a new instance of {@code INIConfiguration} with the
     * content of the specified {@code HierarchicalConfiguration}.
     *
     * @param c the configuration to be copied
     * @since 2.0
     */
    public INIConfiguration(HierarchicalConfiguration c)
    {
        super(c);
    }

    /**
     * Save the configuration to the specified writer.
     *
     * @param writer - The writer to save the configuration to.
     * @throws ConfigurationException If an error occurs while writing the
     *         configuration
     * @throws IOException if an I/O error occurs
     */
    public void write(Writer writer) throws ConfigurationException, IOException
    {
        PrintWriter out = new PrintWriter(writer);
        Iterator<String> it = getSections().iterator();
        while (it.hasNext())
        {
            String section = it.next();
            Configuration subset;
            if (section != null)
            {
                out.print("[");
                out.print(section);
                out.print("]");
                out.println();
                subset = createSubnodeConfiguration(getSectionNode(section), null);
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
                if (value instanceof Collection)
                {
                    Iterator<?> values = ((Collection<?>) value).iterator();
                    while (values.hasNext())
                    {
                        value = values.next();
                        out.print(key);
                        out.print(" = ");
                        out.print(formatValue(value.toString()));
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
     * {@code clear()} method is not called so the configuration read in will
     * be merged with the current configuration.
     *
     * @param reader The reader to read the configuration from.
     * @throws ConfigurationException If an error occurs while reading the
     *         configuration
     * @throws IOException if an I/O error occurs
     */
    public void read(Reader in) throws ConfigurationException, IOException
    {
        BufferedReader bufferedReader = new BufferedReader(in);
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
                    int index = findSeparator(line);
                    if (index >= 0)
                    {
                        key = line.substring(0, index);
                        value =
                                parseValue(line.substring(index + 1),
                                        bufferedReader);
                    }
                    else
                    {
                        key = line;
                    }
                    key = key.trim();
                    if (key.length() < 1)
                    {
                        // use space for properties with no key
                        key = " ";
                    }
                    createValueNodes(sectionNode, key, value);
                }
            }

            line = bufferedReader.readLine();
        }
    }

    /**
     * Creates the node(s) for the given key value-pair. If delimiter parsing is
     * enabled, the value string is split if possible, and for each single value
     * a node is created. Otherwise only a single node is added to the section.
     *
     * @param sectionNode the section node new nodes have to be added
     * @param key the key
     * @param value the value string
     */
    private void createValueNodes(ConfigurationNode sectionNode, String key,
            String value)
    {
        Collection<String> values;
        if (isDelimiterParsingDisabled())
        {
            values = Collections.singleton(value);
        }
        else
        {
            values = PropertyConverter.split(value, getListDelimiter(), false);
        }

        for (String v : values)
        {
            ConfigurationNode node = createNode(key);
            node.setValue(v);
            sectionNode.addChild(node);
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
     * Tries to find the index of the separator character in the given string.
     * This method checks for the presence of separator characters in the given
     * string. If multiple characters are found, the first one is assumed to be
     * the correct separator. If there are quoting characters, they are taken
     * into account, too.
     *
     * @param line the line to be checked
     * @return the index of the separator character or -1 if none is found
     */
    private static int findSeparator(String line)
    {
        int index =
                findSeparatorBeforeQuote(line,
                        findFirstOccurrence(line, QUOTE_CHARACTERS));
        if (index < 0)
        {
            index = findFirstOccurrence(line, SEPARATOR_CHARS);
        }
        return index;
    }

    /**
     * Checks for the occurrence of the specified separators in the given line.
     * The index of the first separator is returned.
     *
     * @param line the line to be investigated
     * @param separators a string with the separator characters to look for
     * @return the lowest index of a separator character or -1 if no separator
     *         is found
     */
    private static int findFirstOccurrence(String line, String separators)
    {
        int index = -1;

        for (int i = 0; i < separators.length(); i++)
        {
            char sep = separators.charAt(i);
            int pos = line.indexOf(sep);
            if (pos >= 0)
            {
                if (index < 0 || pos < index)
                {
                    index = pos;
                }
            }
        }

        return index;
    }

    /**
     * Searches for a separator character directly before a quoting character.
     * If the first non-whitespace character before a quote character is a
     * separator, it is considered the "real" separator in this line - even if
     * there are other separators before.
     *
     * @param line the line to be investigated
     * @param quoteIndex the index of the quote character
     * @return the index of the separator before the quote or &lt; 0 if there is
     *         none
     */
    private static int findSeparatorBeforeQuote(String line, int quoteIndex)
    {
        int index = quoteIndex - 1;
        while (index >= 0 && Character.isWhitespace(line.charAt(index)))
        {
            index--;
        }

        if (index >= 0 && SEPARATOR_CHARS.indexOf(line.charAt(index)) < 0)
        {
            index = -1;
        }

        return index;
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
            return '"' + value.replaceAll("\"", "\\\\\\\"") + '"';
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
     * @return true if the line contains a section
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

        beginRead();
        try
        {
            for (ConfigurationNode node : getRootNode().getChildren())
            {
                if (isSectionNode(node))
                {
                    inSection = true;
                    sections.add(node.getName());
                }
                else
                {
                    if (!inSection && !globalSection)
                    {
                        globalSection = true;
                        sections.add(null);
                    }
                }
            }
        }
        finally
        {
            endRead();
        }

        return sections;
    }

    /**
     * Returns a configuration with the content of the specified section. This
     * provides an easy way of working with a single section only. The way this
     * configuration is structured internally, this method is very similar to
     * calling {@link HierarchicalConfiguration#configurationAt(String)} with
     * the name of the section in question. There are the following differences
     * however:
     * <ul>
     * <li>This method never throws an exception. If the section does not exist,
     * it is created now. The configuration returned in this case is empty.</li>
     * <li>If section is contained multiple times in the configuration, the
     * configuration returned by this method is initialized with the first
     * occurrence of the section. (This can only happen if
     * {@code addProperty()} has been used in a way that does not conform
     * to the storage scheme used by {@code INIConfiguration}.
     * If used correctly, there will not be duplicate sections.)</li>
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
    public SubnodeConfiguration getSection(String name)
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
                // obtain the node for the section, create it on demand
                // (creation of a SubnodeConfiguration has to be synchronized)
                beginWrite();
                try
                {
                    return createAndInitializeSubnodeConfiguration(
                            getSectionNode(name), null, false);
                }
                finally
                {
                    endWrite();
                }
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

        ConfigurationNode node = createNode(sectionName);
        markSectionNode(node);
        getRootNode().addChild(node);
        return node;
    }

    /**
     * Creates a sub configuration for the global section of the represented INI
     * configuration.
     *
     * @return the sub configuration for the global section
     */
    private SubnodeConfiguration getGlobalSection()
    {
        ViewNode parent = new ViewNode();

        beginWrite();
        try
        {
            for (ConfigurationNode node : getRootNode().getChildren())
            {
                if (!isSectionNode(node))
                {
                    parent.addChild(node);
                }
            }

            return createAndInitializeSubnodeConfiguration(parent, null, false);
        }
        finally
        {
            endWrite();
        }
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
