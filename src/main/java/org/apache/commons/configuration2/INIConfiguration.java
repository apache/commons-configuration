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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.InMemoryNodeModelSupport;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeHandlerDecorator;
import org.apache.commons.configuration2.tree.NodeSelector;
import org.apache.commons.configuration2.tree.TrackedNodeModel;

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
 * components:</p>
 * <ul>
 * <li><b>Sections:</b> Ini files are split into sections, each section starting
 * with a section declaration. A section declaration starts with a '[' and ends
 * with a ']'. Sections occur on one line only.</li>
 * <li><b>Parameters:</b> Items in a section are known as parameters. Parameters
 * have a typical {@code key = value} format.</li>
 * <li><b>Comments:</b> Lines starting with a ';' are assumed to be comments.</li>
 * </ul>
 * <p>
 * There are various implementations of the ini file format by various vendors
 * which has caused a number of differences to appear. As far as possible this
 * configuration tries to be lenient and support most of the differences.
 * </p>
 * <p>
 * Some of the differences supported are as follows:
 * </p>
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
 * setting a different {@link org.apache.commons.configuration2.tree.ExpressionEngine}.)
 * </p>
 * <h3>Implementation Details:</h3> Consider the following ini file:
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
 * <p>
 * This ini file will be parsed without error. Note:</p>
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
 * <p>
 * Internally, this configuration maps the content of the represented ini file
 * to its node structure in the following way:</p>
 * <ul>
 * <li>Sections are represented by direct child nodes of the root node.</li>
 * <li>For the content of a section, corresponding nodes are created as children
 * of the section node.</li>
 * </ul>
 * <p>
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
 * Like other {@code Configuration} implementations, this class uses a
 * {@code Synchronizer} object to control concurrent access. By choosing a
 * suitable implementation of the {@code Synchronizer} interface, an instance
 * can be made thread-safe or not. Note that access to most of the properties
 * typically set through a builder is not protected by the {@code Synchronizer}.
 * The intended usage is that these properties are set once at construction
 * time through the builder and after that remain constant. If you wish to
 * change such properties during life time of an instance, you have to use
 * the {@code lock()} and {@code unlock()} methods manually to ensure that
 * other threads see your changes.
 * </p>
 * <p>
 * As this class extends {@link AbstractConfiguration}, all basic features
 * like variable interpolation, list handling, or data type conversions are
 * available as well. This is described in the chapter
 * <a href="http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html">
 * Basic features and AbstractConfiguration</a> of the user's guide.
 * </p>
 * <p>
 * Note that this configuration does not support properties with null values.
 * Such properties are considered to be section nodes.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @since 1.6
 */
public class INIConfiguration extends BaseHierarchicalConfiguration implements
        FileBasedConfiguration
{
    /**
     * The default characters that signal the start of a comment line.
     */
    protected static final String COMMENT_CHARS = "#;";

    /**
     * The default characters used to separate keys from values.
     */
    protected static final String SEPARATOR_CHARS = "=:";

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
     * The separator used when writing an INI file.
     */
    private String separatorUsedInOutput = " = ";

    /**
     * The separator used when reading an INI file.
     */
    private String separatorUsedInInput = SEPARATOR_CHARS;

    /**
     * The characters used to separate keys from values
     * when reading an INI file.
     */
    private String commentCharsUsedInInput = COMMENT_CHARS;

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
    public INIConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
    }

    /**
     * Get separator used in INI output. see {@code setSeparatorUsedInOutput}
     * for further explanation
     *
     * @return the current separator for writing the INI output
     * @since 2.2
     */
    public String getSeparatorUsedInOutput()
    {
        beginRead(false);
        try
        {
            return separatorUsedInOutput;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Allows setting the key and value separator which is used for the creation
     * of the resulting INI output
     *
     * @param separator String of the new separator for INI output
     * @since 2.2
     */
    public void setSeparatorUsedInOutput(final String separator)
    {
        beginWrite(false);
        try
        {
            this.separatorUsedInOutput = separator;
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Get separator used in INI reading. see {@code setSeparatorUsedInInput}
     * for further explanation
     *
     * @return the current separator for reading the INI input
     * @since 2.5
     */
    public String getSeparatorUsedInInput()
    {
        beginRead(false);
        try
        {
            return separatorUsedInInput;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Allows setting the key and value separator which is used in reading
     * an INI file
     *
     * @param separator String of the new separator for INI reading
     * @since 2.5
     */
    public void setSeparatorUsedInInput(final String separator)
    {
        beginRead(false);
        try
        {
            this.separatorUsedInInput = separator;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Get comment leading separator used in INI reading.
     * see {@code setCommentLeadingCharsUsedInInput} for further explanation
     *
     * @return the current separator for reading the INI input
     * @since 2.5
     */
    public String getCommentLeadingCharsUsedInInput()
    {
        beginRead(false);
        try
        {
            return commentCharsUsedInInput;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Allows setting the leading comment separator which is used in reading
     * an INI file
     *
     * @param separator String of the new separator for INI reading
     * @since 2.5
     */
    public void setCommentLeadingCharsUsedInInput(final String separator)
    {
        beginRead(false);
        try
        {
            this.commentCharsUsedInInput = separator;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Save the configuration to the specified writer.
     *
     * @param writer - The writer to save the configuration to.
     * @throws ConfigurationException If an error occurs while writing the
     *         configuration
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final Writer writer) throws ConfigurationException, IOException
    {
        final PrintWriter out = new PrintWriter(writer);
        boolean first = true;
        final String separator = getSeparatorUsedInOutput();

        beginRead(false);
        try
        {
            for (final ImmutableNode node : getModel().getNodeHandler().getRootNode()
                    .getChildren())
            {
                if (isSectionNode(node))
                {
                    if (!first)
                    {
                        out.println();
                    }
                    out.print("[");
                    out.print(node.getNodeName());
                    out.print("]");
                    out.println();

                    for (final ImmutableNode child : node.getChildren())
                    {
                        writeProperty(out, child.getNodeName(),
                                child.getValue(), separator);
                    }
                }
                else
                {
                    writeProperty(out, node.getNodeName(), node.getValue(), separator);
                }
                first = false;
            }
            out.println();
            out.flush();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Load the configuration from the given reader. Note that the
     * {@code clear()} method is not called so the configuration read in will
     * be merged with the current configuration.
     *
     * @param in the reader to read the configuration from.
     * @throws ConfigurationException If an error occurs while reading the
     *         configuration
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void read(final Reader in) throws ConfigurationException, IOException
    {
        final BufferedReader bufferedReader = new BufferedReader(in);
        final Map<String, ImmutableNode.Builder> sectionBuilders = new LinkedHashMap<>();
        final ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();

        createNodeBuilders(bufferedReader, rootBuilder, sectionBuilders);
        final ImmutableNode rootNode = createNewRootNode(rootBuilder, sectionBuilders);
        addNodes(null, rootNode.getChildren());
    }

    /**
     * Creates a new root node from the builders constructed while reading the
     * configuration file.
     *
     * @param rootBuilder the builder for the top-level section
     * @param sectionBuilders a map storing the section builders
     * @return the root node of the newly created hierarchy
     */
    private static ImmutableNode createNewRootNode(
            final ImmutableNode.Builder rootBuilder,
            final Map<String, ImmutableNode.Builder> sectionBuilders)
    {
        for (final Map.Entry<String, ImmutableNode.Builder> e : sectionBuilders
                .entrySet())
        {
            rootBuilder.addChild(e.getValue().name(e.getKey()).create());
        }
        return rootBuilder.create();
    }

    /**
     * Reads the content of an INI file from the passed in reader and creates a
     * structure of builders for constructing the {@code ImmutableNode} objects
     * representing the data.
     *
     * @param in the reader
     * @param rootBuilder the builder for the top-level section
     * @param sectionBuilders a map storing the section builders
     * @throws IOException if an I/O error occurs
     */
    private void createNodeBuilders(final BufferedReader in,
            final ImmutableNode.Builder rootBuilder,
            final Map<String, ImmutableNode.Builder> sectionBuilders)
            throws IOException
    {
        ImmutableNode.Builder sectionBuilder = rootBuilder;
        String line = in.readLine();
        while (line != null)
        {
            line = line.trim();
            if (!isCommentLine(line))
            {
                if (isSectionLine(line))
                {
                    final String section = line.substring(1, line.length() - 1);
                    sectionBuilder = sectionBuilders.get(section);
                    if (sectionBuilder == null)
                    {
                        sectionBuilder = new ImmutableNode.Builder();
                        sectionBuilders.put(section, sectionBuilder);
                    }
                }

                else
                {
                    String key;
                    String value = "";
                    final int index = findSeparator(line);
                    if (index >= 0)
                    {
                        key = line.substring(0, index);
                        value = parseValue(line.substring(index + 1), in);
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
                    createValueNodes(sectionBuilder, key, value);
                }
            }

            line = in.readLine();
        }
    }

    /**
     * Creates the node(s) for the given key value-pair. If delimiter parsing is
     * enabled, the value string is split if possible, and for each single value
     * a node is created. Otherwise only a single node is added to the section.
     *
     * @param sectionBuilder the section builder for adding new nodes
     * @param key the key
     * @param value the value string
     */
    private void createValueNodes(final ImmutableNode.Builder sectionBuilder,
            final String key, final String value)
    {
        final Collection<String> values =
                getListDelimiterHandler().split(value, false);

        for (final String v : values)
        {
            sectionBuilder.addChild(new ImmutableNode.Builder().name(key)
                    .value(v).create());
        }
    }

    /**
     * Writes data about a property into the given stream.
     *
     * @param out the output stream
     * @param key the key
     * @param value the value
     */
    private void writeProperty(final PrintWriter out, final String key, final Object value, final String separator)
    {
        out.print(key);
        out.print(separator);
        out.print(escapeValue(value.toString()));
        out.println();
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
    private String parseValue(final String val, final BufferedReader reader)
        throws IOException
    {
        final StringBuilder propertyValue = new StringBuilder();
        boolean lineContinues;
        String value = val.trim();

        do
        {
            final boolean quoted = value.startsWith("\"") || value.startsWith("'");
            boolean stop = false;
            boolean escape = false;

            final char quote = quoted ? value.charAt(0) : 0;

            int i = quoted ? 1 : 0;

            final StringBuilder result = new StringBuilder();
            char lastChar = 0;
            while (i < value.length() && !stop)
            {
                final char c = value.charAt(i);

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
    private static boolean lineContinues(final String line)
    {
        final String s = line.trim();
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
    private boolean lineContinues(final String line, final int pos)
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
    private boolean isCommentChar(final char c)
    {
        return getCommentLeadingCharsUsedInInput().indexOf(c) >= 0;
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
    private int findSeparator(final String line)
    {
        int index =
                findSeparatorBeforeQuote(line,
                        findFirstOccurrence(line, QUOTE_CHARACTERS));
        if (index < 0)
        {
            index = findFirstOccurrence(line, getSeparatorUsedInInput());
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
    private static int findFirstOccurrence(final String line, final String separators)
    {
        int index = -1;

        for (int i = 0; i < separators.length(); i++)
        {
            final char sep = separators.charAt(i);
            final int pos = line.indexOf(sep);
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
    private static int findSeparatorBeforeQuote(final String line, final int quoteIndex)
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
     * Escapes the given property value before it is written. This method add
     * quotes around the specified value if it contains a comment character and
     * handles list delimiter characters.
     *
     * @param value the string to be escaped
     */
    private String escapeValue(final String value)
    {
        return String.valueOf(getListDelimiterHandler().escape(
                escapeComments(value), ListDelimiterHandler.NOOP_TRANSFORMER));
    }

    /**
     * Escapes comment characters in the given value.
     *
     * @param value the value to be escaped
     * @return the value with comment characters escaped
     */
    private String escapeComments(final String value)
    {
        final String commentChars = getCommentLeadingCharsUsedInInput();
        boolean quoted = false;

        for (int i = 0; i < commentChars.length() && !quoted; i++)
        {
            final char c = commentChars.charAt(i);
            if (value.indexOf(c) != -1)
            {
                quoted = true;
            }
        }

        if (quoted)
        {
            return '"' + value.replaceAll("\"", "\\\\\\\"") + '"';
        }
        return value;
    }

    /**
     * Determine if the given line is a comment line.
     *
     * @param line The line to check.
     * @return true if the line is empty or starts with one of the comment
     *         characters
     */
    protected boolean isCommentLine(final String line)
    {
        if (line == null)
        {
            return false;
        }
        // blank lines are also treated as comment lines
        return line.length() < 1
            || getCommentLeadingCharsUsedInInput().indexOf(line.charAt(0)) >= 0;
    }

    /**
     * Determine if the given line is a section.
     *
     * @param line The line to check.
     * @return true if the line contains a section
     */
    protected boolean isSectionLine(final String line)
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
        final Set<String> sections = new LinkedHashSet<>();
        boolean globalSection = false;
        boolean inSection = false;

        beginRead(false);
        try
        {
            for (final ImmutableNode node : getModel().getNodeHandler().getRootNode()
                    .getChildren())
            {
                if (isSectionNode(node))
                {
                    inSection = true;
                    sections.add(node.getNodeName());
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
    public SubnodeConfiguration getSection(final String name)
    {
        if (name == null)
        {
            return getGlobalSection();
        }
        try
        {
            return (SubnodeConfiguration) configurationAt(name, true);
        }
        catch (final ConfigurationRuntimeException iex)
        {
            // the passed in key does not map to exactly one node
            // obtain the node for the section, create it on demand
            final InMemoryNodeModel parentModel = getSubConfigurationParentModel();
            final NodeSelector selector = parentModel.trackChildNodeWithCreation(null, name, this);
            return createSubConfigurationForTrackedNode(selector, this);
        }
    }

    /**
     * Creates a sub configuration for the global section of the represented INI
     * configuration.
     *
     * @return the sub configuration for the global section
     */
    private SubnodeConfiguration getGlobalSection()
    {
        final InMemoryNodeModel parentModel = getSubConfigurationParentModel();
        final NodeSelector selector = new NodeSelector(null); // selects parent
        parentModel.trackNode(selector, this);
        final GlobalSectionNodeModel model =
                new GlobalSectionNodeModel(this, selector);
        final SubnodeConfiguration sub = new SubnodeConfiguration(this, model);
        initSubConfigurationForThisParent(sub);
        return sub;
    }

    /**
     * Checks whether the specified configuration node represents a section.
     *
     * @param node the node in question
     * @return a flag whether this node represents a section
     */
    private static boolean isSectionNode(final ImmutableNode node)
    {
        return node.getValue() == null;
    }

    /**
     * A specialized node model implementation for the sub configuration
     * representing the global section of the INI file. This is a regular
     * {@code TrackedNodeModel} with one exception: The {@code NodeHandler} used
     * by this model applies a filter on the children of the root node so that
     * only nodes are visible that are no sub sections.
     */
    private static class GlobalSectionNodeModel extends TrackedNodeModel
    {
        /**
         * Creates a new instance of {@code GlobalSectionNodeModel} and
         * initializes it with the given underlying model.
         *
         * @param modelSupport the underlying {@code InMemoryNodeModel}
         * @param selector the {@code NodeSelector}
         */
        public GlobalSectionNodeModel(final InMemoryNodeModelSupport modelSupport,
                final NodeSelector selector)
        {
            super(modelSupport, selector, true);
        }

        @Override
        public NodeHandler<ImmutableNode> getNodeHandler()
        {
            return new NodeHandlerDecorator<ImmutableNode>()
            {
                @Override
                public List<ImmutableNode> getChildren(final ImmutableNode node)
                {
                    final List<ImmutableNode> children = super.getChildren(node);
                    return filterChildrenOfGlobalSection(node, children);
                }

                @Override
                public List<ImmutableNode> getChildren(final ImmutableNode node,
                        final String name)
                {
                    final List<ImmutableNode> children =
                            super.getChildren(node, name);
                    return filterChildrenOfGlobalSection(node, children);
                }

                @Override
                public int getChildrenCount(final ImmutableNode node, final String name)
                {
                    final List<ImmutableNode> children =
                            (name != null) ? super.getChildren(node, name)
                                    : super.getChildren(node);
                    return filterChildrenOfGlobalSection(node, children).size();
                }

                @Override
                public ImmutableNode getChild(final ImmutableNode node, final int index)
                {
                    final List<ImmutableNode> children = super.getChildren(node);
                    return filterChildrenOfGlobalSection(node, children).get(
                            index);
                }

                @Override
                public int indexOfChild(final ImmutableNode parent,
                        final ImmutableNode child)
                {
                    final List<ImmutableNode> children = super.getChildren(parent);
                    return filterChildrenOfGlobalSection(parent, children)
                            .indexOf(child);
                }

                @Override
                protected NodeHandler<ImmutableNode> getDecoratedNodeHandler()
                {
                    return GlobalSectionNodeModel.super.getNodeHandler();
                }

                /**
                 * Filters the child nodes of the global section. This method
                 * checks whether the passed in node is the root node of the
                 * configuration. If so, from the list of children all nodes are
                 * filtered which are section nodes.
                 *
                 * @param node the node in question
                 * @param children the children of this node
                 * @return a list with the filtered children
                 */
                private List<ImmutableNode> filterChildrenOfGlobalSection(
                        final ImmutableNode node, final List<ImmutableNode> children)
                {
                    List<ImmutableNode> filteredList;
                    if (node == getRootNode())
                    {
                        filteredList =
                                new ArrayList<>(children.size());
                        for (final ImmutableNode child : children)
                        {
                            if (!isSectionNode(child))
                            {
                                filteredList.add(child);
                            }
                        }
                    }
                    else
                    {
                        filteredList = children;
                    }

                    return filteredList;
                }
            };
        }
    }
}
