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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * An initialization or ini file is a configuration file typically found on
 * Microsoft's Windows operating system and contains data for Windows based
 * applications.
 * </p>
 *
 * <p>
 * Although popularized by Windows, ini files can be used on any system or
 * platform due to the fact that they are merely text files that can easily be
 * parsed and modified by both humans and computers.
 * </p>
 *
 * <p>
 * A typcial ini file could look something like:
 * </p>
 * <code>
 * [section1]<br>
 * ; this is a comment!<br>
 * var1 = foo<br>
 * var2 = bar<br>
 *<br>
 * [section2]<br>
 * var1 = doo<br>
 * </code>
 *
 * <p>
 * The format of ini files is fairly straight forward and is composed of three
 * components:<br>
 * <ul>
 * <li><b>Sections:</b> Ini files are split into sections, each section
 * starting with a section declaration. A section declaration starts with a '['
 * and ends with a ']'. Sections occur on one line only.</li>
 * <li><b>Parameters:</b> Items in a section are known as parameters.
 * Parameters have a typical <code>key = value</code> format.</li>
 * <li><b>Comments:</b> Lines starting with a ';' are assumed to be comments.
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * There are various implementations of the ini file format by various vendors
 * which has caused a number of differences to appear. As far as possible this
 * configuration tries to be lenient and support most of the differences.
 * </p>
 *
 * <p>
 * Some of the differences supported are as follows:
 * <ul>
 * <li><b>Comments:</b> The '#' character is also accepted as a comment
 * signifier.</li>
 * <li><b>Key value separtor:</b> The ':' character is also accepted in place
 * of '=' to separate keys and values in parameters, for example
 * <code>var1 : foo</code>.</li>
 * <li><b>Duplicate sections:</b> Typically duplicate sections are not allowed ,
 * this configuration does however support it. In the event of a duplicate
 * section, the two section's values are merged.</li>
 * <li><b>Duplicate parameters:</b> Typically duplicate parameters are only
 * allowed if they are in two different sections, thus they are local to
 * sections; this configuration simply merges duplicates; if a section has a
 * duplicate parameter the values are then added to the key as a list. </li>
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
 * <code>section1.var1</code> in this configuration. Thus, a section's
 * parameters can easily be retrieved using the <code>subset</code> method
 * using the section name as the prefix.
 * </p>
 * <p>
 * <h3>Implementation Details:</h3>
 * Consider the following ini file:<br>
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
 * <li>The parameter named "bad" simply adds the parameter with an empty value.
 * </li>
 * <li>The empty key with value "= worse" is added using an empty key. This key
 * is still added to section 2 and the value can be accessed using
 * <code>getProperty("section2.")</code>, notice the period '.' following the
 * section name.</li>
 * <li>Section three uses both '=' and ':' to separate keys and values.</li>
 * <li>Section 3 has a duplicate key named "var5". The value for this key is
 * [test1, test2], and is represented as a List.</li>
 * </ul>
 * </p>
 * <p>
 * The set of sections in this configuration can be retrieved using the
 * <code>getSections</code> method.
 * </p>
 * <p>
 * <em>Note:</em> Configuration objects of this type can be read concurrently
 * by multiple threads. However if one of these threads modifies the object,
 * synchronization has to be performed manually.
 * </p>
 *
 * @author Trevor Miller
 * @version $Id$
 * @since 1.4
 */
public class INIConfiguration extends AbstractFileConfiguration
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
     * configuration
     */
    public void save(Writer writer) throws ConfigurationException
    {
        PrintWriter out = new PrintWriter(writer);
        Iterator it = getSections().iterator();
        while (it.hasNext())
        {
            String section = (String) it.next();
            out.print("[");
            out.print(section);
            out.print("]");
            out.println();

            Configuration subset = subset(section);
            Iterator keys = subset.getKeys();
            while (keys.hasNext())
            {
                String key = (String) keys.next();
                Object value = subset.getProperty(key);
                if (value instanceof Collection)
                {
                    Iterator values = ((Collection) value).iterator();
                    while (values.hasNext())
                    {
                        value = (Object) values.next();
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
     * <code>clear</code> method is not called so the configuration read in
     * will be merged with the current configuration.
     *
     * @param reader The reader to read the configuration from.
     * @throws ConfigurationException If an error occurs while reading the
     * configuration
     */
    public void load(Reader reader) throws ConfigurationException
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            String section = "";
            while (line != null)
            {
                line = line.trim();
                if (!isCommentLine(line))
                {
                    if (isSectionLine(line))
                    {
                        section = line.substring(1, line.length() - 1) + ".";
                    }
                    else
                    {
                        String key = "";
                        String value = "";
                        int index = line.indexOf("=");
                        if (index >= 0)
                        {
                            key = section + line.substring(0, index);
                            value = parseValue(line.substring(index + 1));
                        }
                        else
                        {
                            index = line.indexOf(":");
                            if (index >= 0)
                            {
                                key = section + line.substring(0, index);
                                value = parseValue(line.substring(index + 1));
                            }
                            else
                            {
                                key = section + line;
                            }
                        }
                        addProperty(key.trim(), value);
                    }
                }
                line = bufferedReader.readLine();
            }
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }

    /**
     * Parse the value to remove the quotes and ignoring the comment.
     * Example:
     *
     * <pre>"value" ; comment -> value</pre>
     *
     * <pre>'value' ; comment -> value</pre>
     *
     * @param value
     */
    private String parseValue(String value)
    {
        value = value.trim();

        boolean quoted = value.startsWith("\"") || value.startsWith("'");
        boolean stop = false;
        boolean escape = false;

        char quote = quoted ? value.charAt(0) : 0;

        int i = quoted ? 1 : 0;

        StringBuffer result = new StringBuffer();
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
                if (COMMENT_CHARS.indexOf(c) == -1)
                {
                    result.append(c);
                }
                else
                {
                    stop = true;
                }
            }

            i++;
        }

        String v = result.toString();
        if(!quoted)
        {
            v = v.trim();
        }
        return v;
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
     * characters
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
    public Set getSections()
    {
        Set sections = new TreeSet();

        Iterator keys = getKeys();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            int index = key.indexOf(".");
            if (index >= 0)
            {
                sections.add(key.substring(0, index));
            }
        }

        return sections;
    }
}
