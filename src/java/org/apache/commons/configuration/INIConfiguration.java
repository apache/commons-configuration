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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * An initialization or ini file is a configuration file tpically found on
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
 * The format of ini files is fairly straight forward and is comosed of three
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
 *
 * @author trevor.miller
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

    /** Constant for the used line separator.*/
    private static final String LINE_SEPARATOR = "\r\n";

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
        PrintWriter pw = new PrintWriter(writer);
        Iterator iter = this.getSections().iterator();
        while (iter.hasNext())
        {
            String section = (String) iter.next();
            pw.print("[");
            pw.print(section);
            pw.print("]");
            pw.print(LINE_SEPARATOR);

            Configuration values = this.subset(section);
            Iterator iterator = values.getKeys();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                String value = values.getString(key);
                pw.print(key);
                pw.print(" = ");
                pw.print(value);
                pw.print(LINE_SEPARATOR);
            }

            pw.print(LINE_SEPARATOR);
        }
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
                            value = line.substring(index + 1);
                        }
                        else
                        {
                            index = line.indexOf(":");
                            if (index >= 0)
                            {
                                key = section + line.substring(0, index);
                                value = line.substring(index + 1);
                            }
                            else
                            {
                                key = section + line;
                            }
                        }
                        this.addProperty(key.trim(), value.trim());
                    }
                }
                line = bufferedReader.readLine();
            }
        }
        catch (IOException ioe)
        {
            throw new ConfigurationException(ioe.getMessage());
        }
    }

    /**
     * Determine if the given line is a comment line.
     *
     * @param s The line to check.
     * @return true if the line is empty or starts with one of the comment
     * characters
     */
    protected boolean isCommentLine(String s)
    {
        if (s == null)
        {
            return false;
        }
        // blank lines are also treated as comment lines
        return s.length() < 1 || COMMENT_CHARS.indexOf(s.charAt(0)) >= 0;
    }

    /**
     * Determine if the given line is a section.
     *
     * @param s The line to check.
     * @return true if the line contains a secion
     */
    protected boolean isSectionLine(String s)
    {
        if (s == null)
        {
            return false;
        }
        return s.startsWith("[") && s.endsWith("]");
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
        Iterator iter = this.getKeys();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            int index = key.indexOf(".");
            if (index >= 0)
            {
                sections.add(key.substring(0, index));
            }
        }
        return sections;
    }
}
