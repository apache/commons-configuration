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

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This is the "classic" Properties loader which loads the values from
 * a single or multiple files (which can be chained with "include =".
 * All given path references are either absolute or relative to the
 * file name supplied in the Constructor.
 * <p>
 * In this class, empty PropertyConfigurations can be built, properties
 * added and later saved. include statements are (obviously) not supported
 * if you don't construct a PropertyConfiguration from a file.
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
 *   by a comma ',' by default.
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
 * @author <a href="mailto:e.bourg@cross-systems.com">Emmanuel Bourg</a>
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
 * @version $Id: PropertiesConfiguration.java,v 1.17 2004/12/04 15:45:40 oheger Exp $
 */
public class PropertiesConfiguration extends AbstractFileConfiguration
{
    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     */
    static String include = "include";

    /** Allow file inclusion or not */
    private boolean includesAllowed = true;

    /**
     * Creates an empty PropertyConfiguration object which can be
     * used to synthesize a new Properties file by adding values and
     * then saving(). An object constructed by this C'tor can not be
     * tickled into loading included files because it cannot supply a
     * base for relative includes.
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
     * Load the properties from the given input stream and using the specified
     * encoding.
     *
     * @param in An InputStream.
     *
     * @throws ConfigurationException
     */
    public synchronized void load(Reader in) throws ConfigurationException
    {
        PropertiesReader reader = new PropertiesReader(in);

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
                            String [] files = StringUtils.split(value, getDelimiter());
                            for (int i = 0; i < files.length; i++)
                            {
                                load(ConfigurationUtils.locate(getBasePath(), files[i].trim()));
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
            throw new ConfigurationException("Could not load configuration from input stream.", ioe);
        }
    }

    /**
     * Save the configuration to the specified stream.
     *
     * @param writer the output stream used to save the configuration
     */
    public void save(Writer writer) throws ConfigurationException
    {
        try
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
        catch (IOException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
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
    public static class PropertiesWriter extends FilterWriter
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
                v = StringUtils.replace(v, String.valueOf(getDelimiter()), "\\" + getDelimiter());
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

                if (ch=='\\'){
                    out.append('\\');
                }
                else if (ch=='\''){
                    out.append('\'');
                }
                else if (ch=='\"'){
                    out.append('"');
                }
                else if (ch=='r'){
                    out.append('\r');
                }
                else if (ch=='f'){
                    out.append('\f');
                }
                else if (ch=='t'){
                    out.append('\t');
                }
                else if (ch=='n'){
                    out.append('\n');
                }
                else if (ch=='b'){
                    out.append('\b');
                }
                else if (ch==getDelimiter()){
                    out.append('\\');
                    out.append(getDelimiter());
                }
                else if (ch=='u'){
                    //                  uh-oh, we're in unicode country....
                    inUnicode = true;
                }
                else {
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

}
