package org.apache.commons.configuration;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.Iterator;

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
 * @version $Id: BasePropertiesConfiguration.java,v 1.6 2004/03/28 14:43:04 epugh Exp $
 */
public abstract class BasePropertiesConfiguration
    extends BasePathConfiguration
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
    protected abstract InputStream getPropertyStream(String resourceName)
        throws IOException;

    /**
     * Load the properties from the given input stream.
     *
     * @param input An InputStream.
     * @throws IOException
     */
    public void load(InputStream input)
        throws ConfigurationException
    {
        load(input, null);
    }

    /**
     * Load the properties from the given input stream and using the specified
     * encoding.
     *
     * @param input An InputStream.
     * @param enc An encoding.
     * @exception IOException
     */
    public synchronized void load(InputStream input, String enc)
        throws ConfigurationException
    {
        PropertiesReader reader = null;
        if (enc != null)
        {
            try
            {
                reader =
                  new PropertiesReader(new InputStreamReader(input, enc));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ConfigurationException("Should look up and use default encoding.",e);
            }
        }

        if (reader == null)
        {
            reader = new PropertiesReader(new InputStreamReader(input));
        }
        try {
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
                        String [] files = StringUtils.split(value, ",");
                        for (int cnt = 0 ; cnt < files.length ; cnt++)
                        {
                            load(getPropertyStream(files[cnt].trim()));
                        }
                    }
                }
                else
                {
                    addProperty(key, StringEscapeUtils.unescapeJava(value));
                }
            }
        }
        }
        catch (IOException ioe){
        	throw new ConfigurationException("Could not load configuration from input stream.",ioe);
        }
    }

    /**
     * save properties to a file.
     * properties with multiple values are saved comma seperated.
     *
     * @param filename name of the properties file
     * @throws IOException
     */
    public void save(String filename)
        throws ConfigurationException
    {
        PropertiesWriter out = null;
        File file = new File(filename);
        try {
        	out = new PropertiesWriter(file);

        	out.writeComment("written by PropertiesConfiguration");
        	out.writeComment(new Date().toString());

        	for (Iterator i = this.getKeys(); i.hasNext();)
        	{
        		String key = (String) i.next();
        		String value = StringUtils.join(this.getStringArray(key), ", ");
        		out.writeProperty(key, value);
        	}
        	out.flush();
        	out.close();
        }
        catch (IOException ioe){
            try {
                if (out !=null){
                    out.close();
                }
            }
            catch (IOException ioe2){
                
            }
        	throw new ConfigurationException("Could not save to file " + filename,ioe);
        }
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
    class PropertiesReader
        extends LineNumberReader
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
         * @exception IOException
         */
        public String readProperty()
            throws IOException
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
    class PropertiesWriter
        extends FileWriter
    {
        /**
         * Constructor.
         *
         * @param file the proerties file
         * @throws IOException
         */
        public PropertiesWriter(File file)
            throws IOException
        {
            super(file);
        }

        /**
         * Write a property.
         *
         * @param key
         * @param value
         * @exception IOException
         */
        public void writeProperty(String key, String value)
            throws IOException
        {
            write(key);
            write(" = ");
            write(value != null ? StringEscapeUtils.escapeJava(value) : "");             
            write('\n');
        }

        /**
         * Write a comment.
         *
         * @param comment
         * @exception IOException
         */
        public void writeComment(String comment)
            throws IOException
        {
            write("# " + comment + "\n");
        }
    } // class PropertiesWriter
}
