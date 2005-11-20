/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

package org.apache.commons.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This is the "classic" Properties loader which loads the values from
 * a single or multiple files (which can be chained with "include =".
 * All given path references are either absolute or relative to the
 * file name supplied in the constructor.
 * <p>
 * In this class, empty PropertyConfigurations can be built, properties
 * added and later saved. include statements are (obviously) not supported
 * if you don't construct a PropertyConfiguration from a file.
 *
 * <p>The properties file syntax is explained here, basically it follows
 * the syntax of the stream parsed by {@link java.util.Properties#load} and
 * adds several useful extensions:
 *
 * <ul>
 *  <li>
 *   Each property has the syntax <code>key &lt;separator> value</code>. The
 *   separators accepted are <code>'='</code>, <code>':'</code> and any white
 *   space character. Examples:
 * <pre>
 *  key1 = value1
 *  key2 : value2
 *  key3   value3</pre>
 *  </li>
 *  <li>
 *   The <i>key</i> may use any character, separators must be escaped:
 * <pre>
 *  key\:foo = bar</pre>
 *  </li>
 *  <li>
 *   <i>value</i> may be separated on different lines if a backslash
 *   is placed at the end of the line that continues below.
 *  </li>
 *  <li>
 *   <i>value</i> can contain <em>value delimiters</em> and will then be interpreted
 *   as a list of tokens. Default value delimiter is the comma ','. So the
 *   following property definition
 * <pre>
 *  key = This property, has multiple, values
 * </pre>
 *   will result in a property with three values. You can change the value
 *   delmiter using the <code>{@link AbstractConfiguration#setDelimiter(char)}</code>
 *   method. Setting the delimiter to 0 will disable value splitting completely.
 *  </li>
 *  <li>
 *   Commas in each token are escaped placing a backslash right before
 *   the comma.
 *  </li>
 *  <li>
 *   If a <i>key</i> is used more than once, the values are appended
 *   like if they were on the same line separated with commas.
 *  </li>
 *  <li>
 *   Blank lines and lines starting with character '#' or '!' are skipped.
 *  </li>
 *  <li>
 *   If a property is named "include" (or whatever is defined by
 *   setInclude() and getInclude() and the value of that property is
 *   the full path to a file on disk, that file will be included into
 *   the configuration. You can also pull in files relative to the parent
 *   configuration file. So if you have something like the following:
 *
 *   include = additional.properties
 *
 *   Then "additional.properties" is expected to be in the same
 *   directory as the parent configuration file.
 *
 *   The properties in the included file are added to the parent configuration,
 *   they do not replace existing properties with the same key.
 *
 *  </li>
 * </ul>
 *
 * <p>Here is an example of a valid extended properties file:
 *
 * <p><pre>
 *      # lines starting with # are comments
 *
 *      # This is the simplest property
 *      key = value
 *
 *      # A long property may be separated on multiple lines
 *      longvalue = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
 *                  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
 *
 *      # This is a property with many tokens
 *      tokens_on_a_line = first token, second token
 *
 *      # This sequence generates exactly the same result
 *      tokens_on_multiple_lines = first token
 *      tokens_on_multiple_lines = second token
 *
 *      # commas may be escaped in tokens
 *      commas.escaped = Hi\, what'up?
 *
 *      # properties can reference other properties
 *      base.prop = /base
 *      first.prop = ${base.prop}/first
 *      second.prop = ${first.prop}/second
 * </pre>
 *
 * @see java.util.Properties#load
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:daveb@miceda-data">Dave Bryson</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:kjohnson@transparent.com">Kent Johnson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:ipriha@surfeu.fi">Ilkka Priha</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Id$
 */
public class PropertiesConfiguration extends AbstractFileConfiguration
{
    /** The list of possible key/value separators */
    private static final char[] SEPARATORS = new char[] { '=', ':' };

    /** The white space characters used as key/value separators. */
    private static final char[] WHITE_SPACE = new char[] { ' ', '\t', '\f' };

    /**
     * The default encoding (ISO-8859-1 as specified by
     * http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html)
     */
    private static final String DEFAULT_ENCODING = "ISO-8859-1";

    /** Constant for the platform specific line separator.*/
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Constant for the radix of hex numbers.*/
    private static final int HEX_RADIX = 16;

    /** Constant for the length of a unicode literal.*/
    private static final int UNICODE_LEN = 4;

    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     */
    static String include = "include";

    /** Allow file inclusion or not */
    private boolean includesAllowed;

    /** Comment header of the .properties file */
    private String header;

    // initialization block to set the encoding before loading the file in the constructors
    {
        setEncoding(DEFAULT_ENCODING);
    }

    /**
     * Creates an empty PropertyConfiguration object which can be
     * used to synthesize a new Properties file by adding values and
     * then saving().
     */
    public PropertiesConfiguration()
    {
        setIncludesAllowed(false);
    }

    /**
     * Creates and loads the extended properties from the specified file.
     * The specified file can contain "include = " properties which then
     * are loaded and merged into the properties.
     *
     * @param fileName The name of the properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public PropertiesConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
    }

    /**
     * Creates and loads the extended properties from the specified file.
     * The specified file can contain "include = " properties which then
     * are loaded and merged into the properties.
     *
     * @param file The properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public PropertiesConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Creates and loads the extended properties from the specified URL.
     * The specified file can contain "include = " properties which then
     * are loaded and merged into the properties.
     *
     * @param url The location of the properties file to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public PropertiesConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }

    /**
     * Gets the property value for including other properties files.
     * By default it is "include".
     *
     * @return A String.
     */
    public static String getInclude()
    {
        return PropertiesConfiguration.include;
    }

    /**
     * Sets the property value for including other properties files.
     * By default it is "include".
     *
     * @param inc A String.
     */
    public static void setInclude(String inc)
    {
        PropertiesConfiguration.include = inc;
    }

    /**
     * Controls whether additional files can be loaded by the include = <xxx>
     * statement or not. Base rule is, that objects created by the empty
     * C'tor can not have included files.
     *
     * @param includesAllowed includesAllowed True if Includes are allowed.
     */
    protected void setIncludesAllowed(boolean includesAllowed)
    {
        this.includesAllowed = includesAllowed;
    }

    /**
     * Reports the status of file inclusion.
     *
     * @return True if include files are loaded.
     */
    public boolean getIncludesAllowed()
    {
        return this.includesAllowed;
    }

    /**
     * Return the comment header.
     *
     * @return the comment header
     * @since 1.1
     */
    public String getHeader()
    {
        return header;
    }

    /**
     * Set the comment header.
     *
     * @param header the header to use
     * @since 1.1
     */
    public void setHeader(String header)
    {
        this.header = header;
    }

    /**
     * Load the properties from the given reader.
     * Note that the <code>clear()</code> method is not called, so
     * the properties contained in the loaded file will be added to the
     * actual set of properties.
     *
     * @param in An InputStream.
     *
     * @throws ConfigurationException if an error occurs
     */
    public synchronized void load(Reader in) throws ConfigurationException
    {
        PropertiesReader reader = new PropertiesReader(in);
        boolean oldAutoSave = isAutoSave();
        setAutoSave(false);

        try
        {
            while (true)
            {
                String line = reader.readProperty();

                if (line == null)
                {
                    break; // EOF
                }

                // parse the line
                String[] property = parseProperty(line);
                String key = property[0];
                String value = property[1];

                // Though some software (e.g. autoconf) may produce
                // empty values like foo=\n, emulate the behavior of
                // java.util.Properties by setting the value to the
                // empty string.

                if (StringUtils.isNotEmpty(getInclude()) && key.equalsIgnoreCase(getInclude()))
                {
                    if (getIncludesAllowed())
                    {
                        String [] files = StringUtils.split(value, getDelimiter());
                        for (int i = 0; i < files.length; i++)
                        {
                            loadIncludeFile(files[i].trim());
                        }
                    }
                }
                else
                {
                    addProperty(StringEscapeUtils.unescapeJava(key), unescapeJava(value, getDelimiter()));
                }

            }
        }
        catch (IOException ioe)
        {
            throw new ConfigurationException("Could not load configuration from input stream.", ioe);
        }
        finally
        {
            setAutoSave(oldAutoSave);
        }
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param writer the output stream used to save the configuration
     * @throws ConfigurationException if an error occurs
     */
    public void save(Writer writer) throws ConfigurationException
    {
        enterNoReload();
        try
        {
            PropertiesWriter out = new PropertiesWriter(writer, getDelimiter());

            if (header != null)
            {
                BufferedReader reader = new BufferedReader(new StringReader(header));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    out.writeComment(line);
                }
                out.writeln(null);
            }

            out.writeComment("written by PropertiesConfiguration");
            out.writeComment(new Date().toString());
            out.writeln(null);

            Iterator keys = getKeys();
            while (keys.hasNext())
            {
                String key = (String) keys.next();
                Object value = getProperty(key);

                if (value instanceof List)
                {
                    out.writeProperty(key, (List) value);
                }
                else
                {
                    out.writeProperty(key, value);
                }
            }

            out.flush();
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
        finally
        {
            exitNoReload();
        }
    }

    /**
     * Extend the setBasePath method to turn includes
     * on and off based on the existence of a base path.
     *
     * @param basePath The new basePath to set.
     */
    public void setBasePath(String basePath)
    {
        super.setBasePath(basePath);
        setIncludesAllowed(StringUtils.isNotEmpty(basePath));
    }

    /**
     * This class is used to read properties lines.  These lines do
     * not terminate with new-line chars but rather when there is no
     * backslash sign a the end of the line.  This is used to
     * concatenate multiple lines for readability.
     */
    public static class PropertiesReader extends LineNumberReader
    {
        /**
         * Constructor.
         *
         * @param reader A Reader.
         */
        public PropertiesReader(Reader reader)
        {
            super(reader);
        }

        /**
         * Read a property. Returns null if Stream is
         * at EOF. Concatenates lines ending with "\".
         * Skips lines beginning with "#" or "!" and empty lines.
         *
         * @return A string containing a property value or null
         *
         * @throws IOException in case of an I/O error
         */
        public String readProperty() throws IOException
        {
            StringBuffer buffer = new StringBuffer();

            while (true)
            {
                String line = readLine();
                if (line == null)
                {
                    // EOF
                    return null;
                }

                line = line.trim();

                // skip comments and empty lines
                if (StringUtils.isEmpty(line) || (line.charAt(0) == '#') || (line.charAt(0) == '!'))
                {
                    continue;
                }

                if (checkCombineLines(line))
                {
                    line = line.substring(0, line.length() - 1);
                    buffer.append(line);
                }
                else
                {
                    buffer.append(line);
                    break;
                }
            }
            return buffer.toString();
        }

        /**
         * Checks if the passed in line should be combined with the following.
         * This is true, if the line ends with an odd number of backslashes.
         *
         * @param line the line
         * @return a flag if the lines should be combined
         */
        private static boolean checkCombineLines(String line)
        {
            int bsCount = 0;
            for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--)
            {
                bsCount++;
            }

            return bsCount % 2 == 1;
        }
    } // class PropertiesReader

    /**
     * This class is used to write properties lines.
     */
    public static class PropertiesWriter extends FilterWriter
    {
        /** The delimiter for multi-valued properties.*/
        private char delimiter;

        /**
         * Constructor.
         *
         * @param writer a Writer object providing the underlying stream
         * @param delimiter the delimiter character for multi-valued properties
         */
        public PropertiesWriter(Writer writer, char delimiter)
        {
            super(writer);
            this.delimiter = delimiter;
        }

        /**
         * Write a property.
         *
         * @param key the key of the property
         * @param value the value of the property
         * @throws IOException if an I/O error occurs
         */
        public void writeProperty(String key, Object value) throws IOException
        {
            write(escapeKey(key));
            write(" = ");
            if (value != null)
            {
                String v = StringEscapeUtils.escapeJava(String.valueOf(value));
                v = StringUtils.replace(v, String.valueOf(delimiter), "\\" + delimiter);
                write(v);
            }

            writeln(null);
        }

        /**
         * Write a property.
         *
         * @param key The key of the property
         * @param values The array of values of the property
         */
        public void writeProperty(String key, List values) throws IOException
        {
            for (int i = 0; i < values.size(); i++)
            {
                writeProperty(key, values.get(i));
            }
        }

        /**
         * Write a comment.
         *
         * @param comment the comment to write
         * @throws IOException if an I/O error occurs
         */
        public void writeComment(String comment) throws IOException
        {
            writeln("# " + comment);
        }

        /**
         * Escape the separators in the key.
         *
         * @param key the key
         * @return the escaped key
         * @since 1.2
         */
        private String escapeKey(String key)
        {
            StringBuffer newkey = new StringBuffer();

            for (int i = 0; i < key.length(); i++)
            {
                char c = key.charAt(i);

                if (ArrayUtils.contains(SEPARATORS, c) || ArrayUtils.contains(WHITE_SPACE, c))
                {
                    // escape the separator
                    newkey.append('\\');
                    newkey.append(c);
                }
                else
                {
                    newkey.append(c);
                }
            }

            return newkey.toString();
        }

        /**
         * Helper method for writing a line with the platform specific line
         * ending.
         *
         * @param s the content of the line (may be <b>null</b>)
         * @throws IOException if an error occurs
         */
        private void writeln(String s) throws IOException
        {
            if (s != null)
            {
                write(s);
            }
            write(LINE_SEPARATOR);
        }

    } // class PropertiesWriter

    /**
     * <p>Unescapes any Java literals found in the <code>String</code> to a
     * <code>Writer</code>.</p> This is a slightly modified version of the
     * StringEscapeUtils.unescapeJava() function in commons-lang that doesn't
     * drop escaped separators (i.e '\,').
     *
     * @param str  the <code>String</code> to unescape, may be null
     * @param delimiter the delimiter for multi-valued properties
     * @return the processed string
     * @throws IllegalArgumentException if the Writer is <code>null</code>
     */
    protected static String unescapeJava(String str, char delimiter)
    {
        if (str == null)
        {
            return null;
        }
        int sz = str.length();
        StringBuffer out = new StringBuffer(sz);
        StringBuffer unicode = new StringBuffer(UNICODE_LEN);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++)
        {
            char ch = str.charAt(i);
            if (inUnicode)
            {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == UNICODE_LEN)
                {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try
                    {
                        int value = Integer.parseInt(unicode.toString(), HEX_RADIX);
                        out.append((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    }
                    catch (NumberFormatException nfe)
                    {
                        throw new ConfigurationRuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }

            if (hadSlash)
            {
                // handle an escaped value
                hadSlash = false;

                if (ch == '\\')
                {
                    out.append('\\');
                }
                else if (ch == '\'')
                {
                    out.append('\'');
                }
                else if (ch == '\"')
                {
                    out.append('"');
                }
                else if (ch == 'r')
                {
                    out.append('\r');
                }
                else if (ch == 'f')
                {
                    out.append('\f');
                }
                else if (ch == 't')
                {
                    out.append('\t');
                }
                else if (ch == 'n')
                {
                    out.append('\n');
                }
                else if (ch == 'b')
                {
                    out.append('\b');
                }
                else if (ch == delimiter)
                {
                    out.append('\\');
                    out.append(delimiter);
                }
                else if (ch == 'u')
                {
                    // uh-oh, we're in unicode country....
                    inUnicode = true;
                }
                else
                {
                    out.append(ch);
                }

                continue;
            }
            else if (ch == '\\')
            {
                hadSlash = true;
                continue;
            }
            out.append(ch);
        }

        if (hadSlash)
        {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.append('\\');
        }

        return out.toString();
    }

    /**
     * Parse a property line and return the key and the value in an array.
     *
     * @param line the line to parse
     * @return an array with the property's key and value
     * @since 1.2
     */
    private String[] parseProperty(String line)
    {
        // sorry for this spaghetti code, please replace it as soon as
        // possible with a regexp when the Java 1.3 requirement is dropped

        String[] result = new String[2];
        StringBuffer key = new StringBuffer();
        StringBuffer value = new StringBuffer();

        // state of the automaton:
        // 0: key parsing
        // 1: antislash found while parsing the key
        // 2: separator crossing
        // 3: value parsing
        int state = 0;

        for (int pos = 0; pos < line.length(); pos++)
        {
            char c = line.charAt(pos);

            switch (state)
            {
                case 0:
                    if (c == '\\')
                    {
                        state = 1;
                    }
                    else if (ArrayUtils.contains(WHITE_SPACE, c))
                    {
                        // switch to the separator crossing state
                        state = 2;
                    }
                    else if (ArrayUtils.contains(SEPARATORS, c))
                    {
                        // switch to the value parsing state
                        state = 3;
                    }
                    else
                    {
                        key.append(c);
                    }

                    break;

                case 1:
                    if (ArrayUtils.contains(SEPARATORS, c) || ArrayUtils.contains(WHITE_SPACE, c))
                    {
                        // this is an escaped separator or white space
                        key.append(c);
                    }
                    else
                    {
                        // another escaped character, the '\' is preserved
                        key.append('\\');
                        key.append(c);
                    }

                    // return to the key parsing state
                    state = 0;

                    break;

                case 2:
                    if (ArrayUtils.contains(WHITE_SPACE, c))
                    {
                        // do nothing, eat all white spaces
                    }
                    else if (ArrayUtils.contains(SEPARATORS, c))
                    {
                        // switch to the value parsing state
                        state = 3;
                    }
                    else
                    {
                        // any other character indicates we encoutered the beginning of the value
                        value.append(c);

                        // switch to the value parsing state
                        state = 3;
                    }

                    break;

                case 3:
                    value.append(c);
                    break;
            }
        }

        result[0] = key.toString().trim();
        result[1] = value.toString().trim();

        return result;
    }

    /**
     * Helper method for loading an included properties file. This method is
     * called by <code>load()</code> when an <code>include</code> property
     * is encountered. It tries to resolve relative file names based on the
     * current base path. If this fails, a resolution based on the location of
     * this properties file is tried.
     *
     * @param fileName the name of the file to load
     * @throws ConfigurationException if loading fails
     */
    private void loadIncludeFile(String fileName) throws ConfigurationException
    {
        URL url = ConfigurationUtils.locate(getBasePath(), fileName);
        if (url == null)
        {
            URL baseURL = getURL();
            if (baseURL != null)
            {
                url = ConfigurationUtils.locate(baseURL.toString(), fileName);
            }
        }

        if (url == null)
        {
            throw new ConfigurationException("Cannot resolve include file "
                    + fileName);
        }
        load(url);
    }
}
