/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * loads the configuration from a properties file. <p>
 *
 * <p>The properties file syntax is explained here:
 *
 * <ul>
 *  <li>
 *   Each property has the syntax <code>key = value</code>
 *  </li>
 *  <li>
 *   The <i>key</i> may use any character but the equal sign '='.
 *  </li>
 *  <li>
 *   <i>value</i> may be separated on different lines if a backslash
 *   is placed at the end of the line that continues below.
 *  </li>
 *  <li>
 *   If <i>value</i> is a list of strings, each token is separated
 *   by a comma ','.
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
 *   Blank lines and lines starting with character '#' are skipped.
 *  </li>
 *  <li>
 *   If a property is named "include" (or whatever is defined by
 *   setInclude() and getInclude() and the value of that property is
 *   the full path to a file on disk, that file will be included into
 *   the ConfigurationsRepository. You can also pull in files relative
 *   to the parent configuration file. So if you have something
 *   like the following:
 *
 *   include = additional.properties
 *
 *   Then "additional.properties" is expected to be in the same
 *   directory as the parent configuration file.
 *
 *   Duplicate name values will be replaced, so be careful.
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
 *      commas.excaped = Hi\, what'up?
 *
 *      # properties can reference other properties
 *      base.prop = /base
 *      first.prop = ${base.prop}/first
 *      second.prop = ${first.prop}/second
 * </pre>
 *
 * @version $Id: BasePropertiesConfiguration.java,v 1.13 2004/06/23 11:15:45 ebourg Exp $
 */
public abstract class BasePropertiesConfiguration extends BasePathConfiguration
{
    /** Allow file inclusion or not */
    private boolean includesAllowed = false;

    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     */
    protected static String include = "include";

    /**
     * Implementations of this class must implement this method.
     *
     * @param resourceName The Resource to load
     * @return An Input Stream
     *
     * @throws IOException Error while loading the properties file
     */
    protected abstract InputStream getPropertyStream(String resourceName) throws IOException;

    /**
     * Load the properties from the given input stream.
     *
     * @param input An InputStream.
     *
     * @throws ConfigurationException
     */
    public void load(InputStream input) throws ConfigurationException
    {
        load(input, null);
    }

    /**
     * Load the properties from the given input stream and using the specified
     * encoding.
     *
     * @param input An InputStream.
     * @param encoding An encoding.
     *
     * @throws ConfigurationException
     */
    public synchronized void load(InputStream input, String encoding) throws ConfigurationException
    {
        PropertiesReader reader = null;
        if (encoding != null)
        {
            try
            {
                reader = new PropertiesReader(new InputStreamReader(input, encoding));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ConfigurationException("Should look up and use default encoding.", e);
            }
        }

        if (reader == null)
        {
            reader = new PropertiesReader(new InputStreamReader(input));
        }

        try
        {
            while (true)
            {
                String line = reader.readProperty();

                if (line == null)
                {
                    break; // EOF
                }

                int equalSign = line.indexOf('=');
                if (equalSign > 0)
                {
                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();

                    // Though some software (e.g. autoconf) may produce
                    // empty values like foo=\n, emulate the behavior of
                    // java.util.Properties by setting the value to the
                    // empty string.

                    if (StringUtils.isNotEmpty(getInclude())
                        && key.equalsIgnoreCase(getInclude()))
                    {
                        if (getIncludesAllowed())
                        {
                            String [] files = StringUtils.split(value, DELIMITER);
                            for (int i = 0; i < files.length; i++)
                            {
                                load(getPropertyStream(files[i].trim()));
                            }
                        }
                    }
                    else
                    {
                        addProperty(key, unescapeJava(value));
                    }
                }
            }
        }
        catch (IOException ioe)
        {
        	throw new ConfigurationException("Could not load configuration from input stream.",ioe);
        }
    }

    /**
     * Save the configuration to a file. Properties with multiple values are
     * saved on multiple lines, one value per line.
     *
     * @param filename the name of the properties file
     *
     * @throws ConfigurationException
     */
    public void save(String filename) throws ConfigurationException
    {
        FileWriter writer = null;

        try
        {
            writer = new FileWriter(filename);
            save(writer);
        }
        catch (IOException e)
        {
        	throw new ConfigurationException("Could not save to file " + filename, e);
        }
        finally
        {
            // close the writer
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
            catch (IOException ioe2) { }
        }
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param out the output stream used to save the configuration
     */
    public void save(OutputStream out) throws IOException
    {
        save(out, null);
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param out the output stream used to save the configuration
     * @param encoding the charset used to write the configuration
     */
    public void save(OutputStream out, String encoding) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
        save(writer);
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param writer the output stream used to save the configuration
     */
    public void save(Writer writer) throws IOException
    {
        PropertiesWriter out = new PropertiesWriter(writer);

        out.writeComment("written by PropertiesConfiguration");
        out.writeComment(new Date().toString());

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

    /**
     * Gets the property value for including other properties files.
     * By default it is "include".
     *
     * @return A String.
     */
    public String getInclude()
    {
        return BasePropertiesConfiguration.include;
    }

    /**
     * Sets the property value for including other properties files.
     * By default it is "include".
     *
     * @param inc A String.
     */
    public void setInclude(String inc)
    {
        BasePropertiesConfiguration.include = inc;
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
     * This class is used to read properties lines.  These lines do
     * not terminate with new-line chars but rather when there is no
     * backslash sign a the end of the line.  This is used to
     * concatenate multiple lines for readability.
     */
    class PropertiesReader extends LineNumberReader
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
         * Skips lines beginning with "#" and empty lines.
         *
         * @return A string containing a property value or null
         *
         * @throws IOException
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

                if (StringUtils.isEmpty(line)
                    || (line.charAt(0) == '#'))
                {
                    continue;
                }

                if (line.endsWith("\\"))
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
    } // class PropertiesReader

    /**
     * This class is used to write properties lines.
     */
    class PropertiesWriter extends FilterWriter
    {
        /**
         * Constructor.
         *
         * @param writer a Writer object providing the underlying stream
         */
        public PropertiesWriter(Writer writer) throws IOException
        {
            super(writer);
        }

        /**
         * Write a property.
         *
         * @param key
         * @param value
         * @throws IOException
         */
        public void writeProperty(String key, Object value) throws IOException
        {
            write(key);
            write(" = ");
            if (value != null)
            {
                String v = StringEscapeUtils.escapeJava(String.valueOf(value));
                v = StringUtils.replace(v, String.valueOf(DELIMITER), "\\" + DELIMITER);
                write(v);
            }

            write('\n');
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
         * @param comment
         * @throws IOException
         */
        public void writeComment(String comment) throws IOException
        {
            write("# " + comment + "\n");
        }
    } // class PropertiesWriter

    /**
     * <p>Unescapes any Java literals found in the <code>String</code> to a
     * <code>Writer</code>.</p> This is a slightly modified version of the
     * StringEscapeUtils.unescapeJava() function in commons-lang that doesn't
     * drop escaped commas (i.e '\,').
     *
     * @param str  the <code>String</code> to unescape, may be null
     * 
     * @throws IllegalArgumentException if the Writer is <code>null</code>
     */
    protected static String unescapeJava(String str)
    {
        if (str == null)
        {
            return null;
        }
        int sz = str.length();
        StringBuffer out = new StringBuffer(sz);
        StringBuffer unicode = new StringBuffer(4);
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
                if (unicode.length() == 4)
                {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try
                    {
                        int value = Integer.parseInt(unicode.toString(), 16);
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
                switch (ch)
                {
                    case '\\':
                        out.append('\\');
                        break;
                    case '\'':
                        out.append('\'');
                        break;
                    case '\"':
                        out.append('"');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'b':
                        out.append('\b');
                        break;
                    case DELIMITER:
                        out.append("\\");
                        out.append(DELIMITER);
                        break;
                    case 'u':
                        {
                            // uh-oh, we're in unicode country....
                            inUnicode = true;
                            break;
                        }
                    default :
                        out.append(ch);
                        break;
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

}
