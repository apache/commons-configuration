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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.convert.ValueTransformer;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorAware;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.UnicodeEscaper;

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
 *   Each property has the syntax <code>key &lt;separator&gt; value</code>. The
 *   separators accepted are {@code '='}, {@code ':'} and any white
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
 *   The list delimiter facilities provided by {@link AbstractConfiguration}
 *   are supported, too. If an appropriate {@link ListDelimiterHandler} is
 *   set (for instance
 *   a {@link org.apache.commons.configuration2.convert.DefaultListDelimiterHandler D
 *   efaultListDelimiterHandler} object configured
 *   with a comma as delimiter character), <i>value</i> can contain <em>value
 *   delimiters</em> and will then be interpreted as a list of tokens. So the
 *   following property definition
 * <pre>
 *  key = This property, has multiple, values
 * </pre>
 *   will result in a property with three values. You can change the handling
 *   of delimiters using the
 *   {@link AbstractConfiguration#setListDelimiterHandler(ListDelimiterHandler)}
 *   method. Per default, list splitting is disabled.
 *  </li>
 *  <li>
 *   Commas in each token are escaped placing a backslash right before
 *   the comma.
 *  </li>
 *  <li>
 *   If a <i>key</i> is used more than once, the values are appended
 *   like if they were on the same line separated with commas. <em>Note</em>:
 *   When the configuration file is written back to disk the associated
 *   {@link PropertiesConfigurationLayout} object (see below) will
 *   try to preserve as much of the original format as possible, i.e. properties
 *   with multiple values defined on a single line will also be written back on
 *   a single line, and multiple occurrences of a single key will be written on
 *   multiple lines. If the {@code addProperty()} method was called
 *   multiple times for adding multiple values to a property, these properties
 *   will per default be written on multiple lines in the output file, too.
 *   Some options of the {@code PropertiesConfigurationLayout} class have
 *   influence on that behavior.
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
 * <p>Here is an example of a valid extended properties file:</p>
 *
 * <pre>
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
 * <p>A {@code PropertiesConfiguration} object is associated with an
 * instance of the {@link PropertiesConfigurationLayout} class,
 * which is responsible for storing the layout of the parsed properties file
 * (i.e. empty lines, comments, and such things). The {@code getLayout()}
 * method can be used to obtain this layout object. With {@code setLayout()}
 * a new layout object can be set. This should be done before a properties file
 * was loaded.
 * <p>Like other {@code Configuration} implementations, this class uses a
 * {@code Synchronizer} object to control concurrent access. By choosing a
 * suitable implementation of the {@code Synchronizer} interface, an instance
 * can be made thread-safe or not. Note that access to most of the properties
 * typically set through a builder is not protected by the {@code Synchronizer}.
 * The intended usage is that these properties are set once at construction
 * time through the builder and after that remain constant. If you wish to
 * change such properties during life time of an instance, you have to use
 * the {@code lock()} and {@code unlock()} methods manually to ensure that
 * other threads see your changes.
 * <p>As this class extends {@link AbstractConfiguration}, all basic features
 * like variable interpolation, list handling, or data type conversions are
 * available as well. This is described in the chapter
 * <a href="http://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html">
 * Basic features and AbstractConfiguration</a> of the user's guide. There is
 * also a separate chapter dealing with
 * <a href="commons.apache.org/proper/commons-configuration/userguide/howto_properties.html">
 * Properties files</a> in special.
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
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 */
public class PropertiesConfiguration extends BaseConfiguration
    implements FileBasedConfiguration, FileLocatorAware
{
    /**
     * The default encoding (ISO-8859-1 as specified by
     * http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html)
     */
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    /** Constant for the supported comment characters.*/
    static final String COMMENT_CHARS = "#!";

    /** Constant for the default properties separator.*/
    static final String DEFAULT_SEPARATOR = " = ";

    /**
     * Constant for the default {@code IOFactory}. This instance is used
     * when no specific factory was set.
     */
    private static final IOFactory DEFAULT_IO_FACTORY = new DefaultIOFactory();

    /**
     * A string with special characters that need to be unescaped when reading
     * a properties file. {@code java.util.Properties} escapes these characters
     * when writing out a properties file.
     */
    private static final String UNESCAPE_CHARACTERS = ":#=!\\\'\"";

    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     */
    private static String include = "include";

    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     * <p>
     * If the file is absent, processing continues normally.
     * </p>
     */
    private static String includeOptional = "includeoptional";

    /** The list of possible key/value separators */
    private static final char[] SEPARATORS = new char[] {'=', ':'};

    /** The white space characters used as key/value separators. */
    private static final char[] WHITE_SPACE = new char[]{' ', '\t', '\f'};

    /** Constant for the platform specific line separator.*/
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Constant for the radix of hex numbers.*/
    private static final int HEX_RADIX = 16;

    /** Constant for the length of a unicode literal.*/
    private static final int UNICODE_LEN = 4;

    /** Stores the layout object.*/
    private PropertiesConfigurationLayout layout;

    /** The IOFactory for creating readers and writers.*/
    private IOFactory ioFactory;

    /** The current {@code FileLocator}. */
    private FileLocator locator;

    /** Allow file inclusion or not */
    private boolean includesAllowed = true;

    /**
     * Creates an empty PropertyConfiguration object which can be
     * used to synthesize a new Properties file by adding values and
     * then saving().
     */
    public PropertiesConfiguration()
    {
        installLayout(createLayout());
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
     * Gets the property value for including other properties files.
     * By default it is "include".
     * <p>
     * If the file is absent, processing continues normally.
     * </p>
     *
     * @return A String.
     * @since 2.5
     */
    public static String getIncludeOptional()
    {
        return PropertiesConfiguration.includeOptional;
    }

    /**
     * Sets the property value for including other properties files.
     * By default it is "include".
     *
     * @param inc A String.
     */
    public static void setInclude(final String inc)
    {
        PropertiesConfiguration.include = inc;
    }

    /**
     * Sets the property value for including other properties files.
     * By default it is "include".
     * <p>
     * If the file is absent, processing continues normally.
     * </p>
     *
     * @param inc A String.
     * @since 2.5
     */
    public static void setIncludeOptional(final String inc)
    {
        PropertiesConfiguration.includeOptional = inc;
    }

    /**
     * Controls whether additional files can be loaded by the {@code include = <xxx>}
     * statement or not. This is <b>true</b> per default.
     *
     * @param includesAllowed True if Includes are allowed.
     */
    public void setIncludesAllowed(final boolean includesAllowed)
    {
        this.includesAllowed = includesAllowed;
    }

    /**
     * Reports the status of file inclusion.
     *
     * @return True if include files are loaded.
     */
    public boolean isIncludesAllowed()
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
        beginRead(false);
        try
        {
            return getLayout().getHeaderComment();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Set the comment header.
     *
     * @param header the header to use
     * @since 1.1
     */
    public void setHeader(final String header)
    {
        beginWrite(false);
        try
        {
            getLayout().setHeaderComment(header);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Returns the footer comment. This is a comment at the very end of the
     * file.
     *
     * @return the footer comment
     * @since 2.0
     */
    public String getFooter()
    {
        beginRead(false);
        try
        {
            return getLayout().getFooterComment();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the footer comment. If set, this comment is written after all
     * properties at the end of the file.
     *
     * @param footer the footer comment
     * @since 2.0
     */
    public void setFooter(final String footer)
    {
        beginWrite(false);
        try
        {
            getLayout().setFooterComment(footer);
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Returns the associated layout object.
     *
     * @return the associated layout object
     * @since 1.3
     */
    public PropertiesConfigurationLayout getLayout()
    {
        return layout;
    }

    /**
     * Sets the associated layout object.
     *
     * @param layout the new layout object; can be <b>null</b>, then a new
     * layout object will be created
     * @since 1.3
     */
    public void setLayout(final PropertiesConfigurationLayout layout)
    {
        installLayout(layout);
    }

    /**
     * Installs a layout object. It has to be ensured that the layout is
     * registered as change listener at this configuration. If there is already
     * a layout object installed, it has to be removed properly.
     *
     * @param layout the layout object to be installed
     */
    private void installLayout(final PropertiesConfigurationLayout layout)
    {
        // only one layout must exist
        if (this.layout != null)
        {
            removeEventListener(ConfigurationEvent.ANY, this.layout);
        }

        if (layout == null)
        {
            this.layout = createLayout();
        }
        else
        {
            this.layout = layout;
        }
        addEventListener(ConfigurationEvent.ANY, this.layout);
    }

    /**
     * Creates a standard layout object. This configuration is initialized with
     * such a standard layout.
     *
     * @return the newly created layout object
     */
    private PropertiesConfigurationLayout createLayout()
    {
        return new PropertiesConfigurationLayout();
    }

    /**
     * Returns the {@code IOFactory} to be used for creating readers and
     * writers when loading or saving this configuration.
     *
     * @return the {@code IOFactory}
     * @since 1.7
     */
    public IOFactory getIOFactory()
    {
        return (ioFactory != null) ? ioFactory : DEFAULT_IO_FACTORY;
    }

    /**
     * Sets the {@code IOFactory} to be used for creating readers and
     * writers when loading or saving this configuration. Using this method a
     * client can customize the reader and writer classes used by the load and
     * save operations. Note that this method must be called before invoking
     * one of the {@code load()} and {@code save()} methods.
     * Especially, if you want to use a custom {@code IOFactory} for
     * changing the {@code PropertiesReader}, you cannot load the
     * configuration data in the constructor.
     *
     * @param ioFactory the new {@code IOFactory} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code IOFactory} is
     *         <b>null</b>
     * @since 1.7
     */
    public void setIOFactory(final IOFactory ioFactory)
    {
        if (ioFactory == null)
        {
            throw new IllegalArgumentException("IOFactory must not be null!");
        }

        this.ioFactory = ioFactory;
    }

    /**
     * Stores the current {@code FileLocator} for a following IO operation. The
     * {@code FileLocator} is needed to resolve include files with relative file
     * names.
     *
     * @param locator the current {@code FileLocator}
     * @since 2.0
     */
    @Override
    public void initFileLocator(final FileLocator locator)
    {
        this.locator = locator;
    }

    /**
     * {@inheritDoc} This implementation delegates to the associated layout
     * object which does the actual loading. Note that this method does not
     * do any synchronization. This lies in the responsibility of the caller.
     * (Typically, the caller is a {@code FileHandler} object which takes
     * care for proper synchronization.)
     *
     * @since 2.0
     */
    @Override
    public void read(final Reader in) throws ConfigurationException, IOException
    {
        getLayout().load(this, in);
    }

    /**
     * {@inheritDoc} This implementation delegates to the associated layout
     * object which does the actual saving. Note that, analogous to
     * {@link #read(Reader)}, this method does not do any synchronization.
     *
     * @since 2.0
     */
    @Override
    public void write(final Writer out) throws ConfigurationException, IOException
    {
        getLayout().save(this, out);
    }

    /**
     * Creates a copy of this object.
     *
     * @return the copy
     */
    @Override
    public Object clone()
    {
        final PropertiesConfiguration copy = (PropertiesConfiguration) super.clone();
        if (layout != null)
        {
            copy.setLayout(new PropertiesConfigurationLayout(layout));
        }
        return copy;
    }

    /**
     * This method is invoked by the associated
     * {@link PropertiesConfigurationLayout} object for each
     * property definition detected in the parsed properties file. Its task is
     * to check whether this is a special property definition (e.g. the
     * {@code include} property). If not, the property must be added to
     * this configuration. The return value indicates whether the property
     * should be treated as a normal property. If it is <b>false</b>, the
     * layout object will ignore this property.
     *
     * @param key the property key
     * @param value the property value
     * @return a flag whether this is a normal property
     * @throws ConfigurationException if an error occurs
     * @since 1.3
     */
    boolean propertyLoaded(final String key, final String value)
            throws ConfigurationException
    {
        boolean result;

        if (StringUtils.isNotEmpty(getInclude())
                && key.equalsIgnoreCase(getInclude()))
        {
            if (isIncludesAllowed())
            {
                final Collection<String> files =
                        getListDelimiterHandler().split(value, true);
                for (final String f : files)
                {
                    loadIncludeFile(interpolate(f), false);
                }
            }
            result = false;
        }

        else if (StringUtils.isNotEmpty(getIncludeOptional())
            && key.equalsIgnoreCase(getIncludeOptional()))
        {
            if (isIncludesAllowed())
            {
                final Collection<String> files =
                        getListDelimiterHandler().split(value, true);
                for (final String f : files)
                {
                    loadIncludeFile(interpolate(f), true);
                }
            }
            result = false;
        }

        else
        {
            addPropertyInternal(key, value);
            result = true;
        }

        return result;
    }

    /**
     * Tests whether a line is a comment, i.e. whether it starts with a comment
     * character.
     *
     * @param line the line
     * @return a flag if this is a comment line
     * @since 1.3
     */
    static boolean isCommentLine(final String line)
    {
        final String s = line.trim();
        // blanc lines are also treated as comment lines
        return s.length() < 1 || COMMENT_CHARS.indexOf(s.charAt(0)) >= 0;
    }

    /**
     * Returns the number of trailing backslashes. This is sometimes needed for
     * the correct handling of escape characters.
     *
     * @param line the string to investigate
     * @return the number of trailing backslashes
     */
    private static int countTrailingBS(final String line)
    {
        int bsCount = 0;
        for (int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--)
        {
            bsCount++;
        }

        return bsCount;
    }

    /**
     * This class is used to read properties lines. These lines do
     * not terminate with new-line chars but rather when there is no
     * backslash sign a the end of the line.  This is used to
     * concatenate multiple lines for readability.
     */
    public static class PropertiesReader extends LineNumberReader
    {
        /** The regular expression to parse the key and the value of a property. */
        private static final Pattern PROPERTY_PATTERN = Pattern
                .compile("(([\\S&&[^\\\\" + new String(SEPARATORS)
                        + "]]|\\\\.)*)(\\s*(\\s+|[" + new String(SEPARATORS)
                        + "])\\s*)?(.*)");

        /** Constant for the index of the group for the key. */
        private static final int IDX_KEY = 1;

        /** Constant for the index of the group for the value. */
        private static final int IDX_VALUE = 5;

        /** Constant for the index of the group for the separator. */
        private static final int IDX_SEPARATOR = 3;

        /** Stores the comment lines for the currently processed property.*/
        private final List<String> commentLines;

        /** Stores the name of the last read property.*/
        private String propertyName;

        /** Stores the value of the last read property.*/
        private String propertyValue;

        /** Stores the property separator of the last read property.*/
        private String propertySeparator = DEFAULT_SEPARATOR;

        /**
         * Constructor.
         *
         * @param reader A Reader.
         */
        public PropertiesReader(final Reader reader)
        {
            super(reader);
            commentLines = new ArrayList<>();
        }

        /**
         * Reads a property line. Returns null if Stream is
         * at EOF. Concatenates lines ending with "\".
         * Skips lines beginning with "#" or "!" and empty lines.
         * The return value is a property definition (<code>&lt;name&gt;</code>
         * = <code>&lt;value&gt;</code>)
         *
         * @return A string containing a property value or null
         *
         * @throws IOException in case of an I/O error
         */
        public String readProperty() throws IOException
        {
            commentLines.clear();
            final StringBuilder buffer = new StringBuilder();

            while (true)
            {
                String line = readLine();
                if (line == null)
                {
                    // EOF
                    return null;
                }

                if (isCommentLine(line))
                {
                    commentLines.add(line);
                    continue;
                }

                line = line.trim();

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
         * Parses the next property from the input stream and stores the found
         * name and value in internal fields. These fields can be obtained using
         * the provided getter methods. The return value indicates whether EOF
         * was reached (<b>false</b>) or whether further properties are
         * available (<b>true</b>).
         *
         * @return a flag if further properties are available
         * @throws IOException if an error occurs
         * @since 1.3
         */
        public boolean nextProperty() throws IOException
        {
            final String line = readProperty();

            if (line == null)
            {
                return false; // EOF
            }

            // parse the line
            parseProperty(line);
            return true;
        }

        /**
         * Returns the comment lines that have been read for the last property.
         *
         * @return the comment lines for the last property returned by
         * {@code readProperty()}
         * @since 1.3
         */
        public List<String> getCommentLines()
        {
            return commentLines;
        }

        /**
         * Returns the name of the last read property. This method can be called
         * after {@link #nextProperty()} was invoked and its
         * return value was <b>true</b>.
         *
         * @return the name of the last read property
         * @since 1.3
         */
        public String getPropertyName()
        {
            return propertyName;
        }

        /**
         * Returns the value of the last read property. This method can be
         * called after {@link #nextProperty()} was invoked and
         * its return value was <b>true</b>.
         *
         * @return the value of the last read property
         * @since 1.3
         */
        public String getPropertyValue()
        {
            return propertyValue;
        }

        /**
         * Returns the separator that was used for the last read property. The
         * separator can be stored so that it can later be restored when saving
         * the configuration.
         *
         * @return the separator for the last read property
         * @since 1.7
         */
        public String getPropertySeparator()
        {
            return propertySeparator;
        }

        /**
         * Parses a line read from the properties file. This method is called
         * for each non-comment line read from the source file. Its task is to
         * split the passed in line into the property key and its value. The
         * results of the parse operation can be stored by calling the
         * {@code initPropertyXXX()} methods.
         *
         * @param line the line read from the properties file
         * @since 1.7
         */
        protected void parseProperty(final String line)
        {
            final String[] property = doParseProperty(line, true);
            initPropertyName(property[0]);
            initPropertyValue(property[1]);
            initPropertySeparator(property[2]);
        }

        /**
         * Sets the name of the current property. This method can be called by
         * {@code parseProperty()} for storing the results of the parse
         * operation. It also ensures that the property key is correctly
         * escaped.
         *
         * @param name the name of the current property
         * @since 1.7
         */
        protected void initPropertyName(final String name)
        {
            propertyName = unescapePropertyName(name);
        }

        /**
         * Performs unescaping on the given property name.
         *
         * @param name the property name
         * @return the unescaped property name
         * @since 2.4
         */
        protected String unescapePropertyName(final String name)
        {
            return StringEscapeUtils.unescapeJava(name);
        }

        /**
         * Sets the value of the current property. This method can be called by
         * {@code parseProperty()} for storing the results of the parse
         * operation. It also ensures that the property value is correctly
         * escaped.
         *
         * @param value the value of the current property
         * @since 1.7
         */
        protected void initPropertyValue(final String value)
        {
            propertyValue = unescapePropertyValue(value);
        }

        /**
         * Performs unescaping on the given property value.
         *
         * @param value the property value
         * @return the unescaped property value
         * @since 2.4
         */
        protected String unescapePropertyValue(final String value)
        {
            return unescapeJava(value);
        }

        /**
         * Sets the separator of the current property. This method can be called
         * by {@code parseProperty()}. It allows the associated layout
         * object to keep track of the property separators. When saving the
         * configuration the separators can be restored.
         *
         * @param value the separator used for the current property
         * @since 1.7
         */
        protected void initPropertySeparator(final String value)
        {
            propertySeparator = value;
        }

        /**
         * Checks if the passed in line should be combined with the following.
         * This is true, if the line ends with an odd number of backslashes.
         *
         * @param line the line
         * @return a flag if the lines should be combined
         */
        static boolean checkCombineLines(final String line)
        {
            return countTrailingBS(line) % 2 != 0;
        }

        /**
         * Parse a property line and return the key, the value, and the separator in an
         * array.
         *
         * @param line the line to parse
         * @param trimValue flag whether the value is to be trimmed
         * @return an array with the property's key, value, and separator
         */
        static String[] doParseProperty(final String line, final boolean trimValue)
        {
            final Matcher matcher = PROPERTY_PATTERN.matcher(line);

            final String[] result = {"", "", ""};

            if (matcher.matches())
            {
                result[0] = matcher.group(IDX_KEY).trim();

                String value = matcher.group(IDX_VALUE);
                if (trimValue)
                {
                    value = value.trim();
                }
                result[1] = value;

                result[2] = matcher.group(IDX_SEPARATOR);
            }

            return result;
        }
    } // class PropertiesReader

    /**
     * This class is used to write properties lines. The most important method
     * is {@code writeProperty(String, Object, boolean)}, which is called
     * during a save operation for each property found in the configuration.
     */
    public static class PropertiesWriter extends FilterWriter
    {

        /**
         * Properties escape map.
         */
        private static final Map<CharSequence, CharSequence> PROPERTIES_CHARS_ESCAPE;
        static
        {
            final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
            initialMap.put("\\", "\\\\");
            PROPERTIES_CHARS_ESCAPE = Collections.unmodifiableMap(initialMap);
        }

        /**
         * A translator for escaping property values. This translator performs a
         * subset of transformations done by the ESCAPE_JAVA translator from
         * Commons Lang 3.
         */
        private static final CharSequenceTranslator ESCAPE_PROPERTIES =
                new AggregateTranslator(
                        new LookupTranslator(PROPERTIES_CHARS_ESCAPE),
                        new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
                        UnicodeEscaper.outsideOf(32, 0x7f));

        /**
         * A {@code ValueTransformer} implementation used to escape property
         * values. This implementation applies the transformation defined by the
         * {@link #ESCAPE_PROPERTIES} translator.
         */
        private static final ValueTransformer DEFAULT_TRANSFORMER =
                new ValueTransformer()
                {
                    @Override
                    public Object transformValue(final Object value)
                    {
                        final String strVal = String.valueOf(value);
                        return ESCAPE_PROPERTIES.translate(strVal);
                    }
                };

        /** The value transformer used for escaping property values. */
        private final ValueTransformer valueTransformer;

        /** The list delimiter handler.*/
        private final ListDelimiterHandler delimiterHandler;

        /** The separator to be used for the current property. */
        private String currentSeparator;

        /** The global separator. If set, it overrides the current separator.*/
        private String globalSeparator;

        /** The line separator.*/
        private String lineSeparator;

        /**
         * Creates a new instance of {@code PropertiesWriter}.
         *
         * @param writer a Writer object providing the underlying stream
         * @param delHandler the delimiter handler for dealing with properties
         *        with multiple values
         */
        public PropertiesWriter(final Writer writer, final ListDelimiterHandler delHandler)
        {
            this(writer, delHandler, DEFAULT_TRANSFORMER);
        }

        /**
         * Creates a new instance of {@code PropertiesWriter}.
         *
         * @param writer a Writer object providing the underlying stream
         * @param delHandler the delimiter handler for dealing with properties
         *        with multiple values
         * @param valueTransformer the value transformer used to escape property values
         */
        public PropertiesWriter(final Writer writer, final ListDelimiterHandler delHandler,
            final ValueTransformer valueTransformer)
        {
            super(writer);
            delimiterHandler = delHandler;
            this.valueTransformer = valueTransformer;
        }

        /**
         * Returns the delimiter handler for properties with multiple values.
         * This object is used to escape property values so that they can be
         * read in correctly the next time they are loaded.
         *
         * @return the delimiter handler for properties with multiple values
         * @since 2.0
         */
        public ListDelimiterHandler getDelimiterHandler()
        {
            return delimiterHandler;
        }

        /**
         * Returns the current property separator.
         *
         * @return the current property separator
         * @since 1.7
         */
        public String getCurrentSeparator()
        {
            return currentSeparator;
        }

        /**
         * Sets the current property separator. This separator is used when
         * writing the next property.
         *
         * @param currentSeparator the current property separator
         * @since 1.7
         */
        public void setCurrentSeparator(final String currentSeparator)
        {
            this.currentSeparator = currentSeparator;
        }

        /**
         * Returns the global property separator.
         *
         * @return the global property separator
         * @since 1.7
         */
        public String getGlobalSeparator()
        {
            return globalSeparator;
        }

        /**
         * Sets the global property separator. This separator corresponds to the
         * {@code globalSeparator} property of
         * {@link PropertiesConfigurationLayout}. It defines the separator to be
         * used for all properties. If it is undefined, the current separator is
         * used.
         *
         * @param globalSeparator the global property separator
         * @since 1.7
         */
        public void setGlobalSeparator(final String globalSeparator)
        {
            this.globalSeparator = globalSeparator;
        }

        /**
         * Returns the line separator.
         *
         * @return the line separator
         * @since 1.7
         */
        public String getLineSeparator()
        {
            return (lineSeparator != null) ? lineSeparator : LINE_SEPARATOR;
        }

        /**
         * Sets the line separator. Each line written by this writer is
         * terminated with this separator. If not set, the platform-specific
         * line separator is used.
         *
         * @param lineSeparator the line separator to be used
         * @since 1.7
         */
        public void setLineSeparator(final String lineSeparator)
        {
            this.lineSeparator = lineSeparator;
        }

        /**
         * Write a property.
         *
         * @param key the key of the property
         * @param value the value of the property
         *
         * @throws IOException if an I/O error occurs
         */
        public void writeProperty(final String key, final Object value) throws IOException
        {
            writeProperty(key, value, false);
        }

        /**
         * Write a property.
         *
         * @param key The key of the property
         * @param values The array of values of the property
         *
         * @throws IOException if an I/O error occurs
         */
        public void writeProperty(final String key, final List<?> values) throws IOException
        {
            for (int i = 0; i < values.size(); i++)
            {
                writeProperty(key, values.get(i));
            }
        }

        /**
         * Writes the given property and its value. If the value happens to be a
         * list, the {@code forceSingleLine} flag is evaluated. If it is
         * set, all values are written on a single line using the list delimiter
         * as separator.
         *
         * @param key the property key
         * @param value the property value
         * @param forceSingleLine the &quot;force single line&quot; flag
         * @throws IOException if an error occurs
         * @since 1.3
         */
        public void writeProperty(final String key, final Object value,
                final boolean forceSingleLine) throws IOException
        {
            String v;

            if (value instanceof List)
            {
                v = null;
                final List<?> values = (List<?>) value;
                if (forceSingleLine)
                {
                    try
                    {
                        v = String.valueOf(getDelimiterHandler()
                                        .escapeList(values, valueTransformer));
                    }
                    catch (final UnsupportedOperationException uoex)
                    {
                        // the handler may not support escaping lists,
                        // then the list is written in multiple lines
                    }
                }
                if (v == null)
                {
                    writeProperty(key, values);
                    return;
                }
            }
            else
            {
                v = String.valueOf(getDelimiterHandler().escape(value, valueTransformer));
            }

            write(escapeKey(key));
            write(fetchSeparator(key, value));
            write(v);

            writeln(null);
        }

        /**
         * Write a comment.
         *
         * @param comment the comment to write
         * @throws IOException if an I/O error occurs
         */
        public void writeComment(final String comment) throws IOException
        {
            writeln("# " + comment);
        }

        /**
         * Escapes the key of a property before it gets written to file. This
         * method is called on saving a configuration for each property key.
         * It ensures that separator characters contained in the key are
         * escaped.
         *
         * @param key the key
         * @return the escaped key
         * @since 2.0
         */
        protected String escapeKey(final String key)
        {
            final StringBuilder newkey = new StringBuilder();

            for (int i = 0; i < key.length(); i++)
            {
                final char c = key.charAt(i);

                if (ArrayUtils.contains(SEPARATORS, c) || ArrayUtils.contains(WHITE_SPACE, c) || c == '\\')
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
         * @since 1.3
         */
        public void writeln(final String s) throws IOException
        {
            if (s != null)
            {
                write(s);
            }
            write(getLineSeparator());
        }

        /**
         * Returns the separator to be used for the given property. This method
         * is called by {@code writeProperty()}. The string returned here
         * is used as separator between the property key and its value. Per
         * default the method checks whether a global separator is set. If this
         * is the case, it is returned. Otherwise the separator returned by
         * {@code getCurrentSeparator()} is used, which was set by the
         * associated layout object. Derived classes may implement a different
         * strategy for defining the separator.
         *
         * @param key the property key
         * @param value the value
         * @return the separator to be used
         * @since 1.7
         */
        protected String fetchSeparator(final String key, final Object value)
        {
            return (getGlobalSeparator() != null) ? getGlobalSeparator()
                    : StringUtils.defaultString(getCurrentSeparator());
        }
    } // class PropertiesWriter

    /**
     * <p>
     * Definition of an interface that allows customization of read and write
     * operations.
     * </p>
     * <p>
     * For reading and writing properties files the inner classes
     * {@code PropertiesReader} and {@code PropertiesWriter} are used.
     * This interface defines factory methods for creating both a
     * {@code PropertiesReader} and a {@code PropertiesWriter}. An
     * object implementing this interface can be passed to the
     * {@code setIOFactory()} method of
     * {@code PropertiesConfiguration}. Every time the configuration is
     * read or written the {@code IOFactory} is asked to create the
     * appropriate reader or writer object. This provides an opportunity to
     * inject custom reader or writer implementations.
     * </p>
     *
     * @since 1.7
     */
    public interface IOFactory
    {
        /**
         * Creates a {@code PropertiesReader} for reading a properties
         * file. This method is called whenever the
         * {@code PropertiesConfiguration} is loaded. The reader returned
         * by this method is then used for parsing the properties file.
         *
         * @param in the underlying reader (of the properties file)
         * @return the {@code PropertiesReader} for loading the
         *         configuration
         */
        PropertiesReader createPropertiesReader(Reader in);

        /**
         * Creates a {@code PropertiesWriter} for writing a properties
         * file. This method is called before the
         * {@code PropertiesConfiguration} is saved. The writer returned by
         * this method is then used for writing the properties file.
         *
         * @param out the underlying writer (to the properties file)
         * @param handler the list delimiter delimiter for list parsing
         * @return the {@code PropertiesWriter} for saving the
         *         configuration
         */
        PropertiesWriter createPropertiesWriter(Writer out,
                ListDelimiterHandler handler);
    }

    /**
     * <p>
     * A default implementation of the {@code IOFactory} interface.
     * </p>
     * <p>
     * This class implements the {@code createXXXX()} methods defined by
     * the {@code IOFactory} interface in a way that the default objects
     * (i.e. {@code PropertiesReader} and {@code PropertiesWriter} are
     * returned. Customizing either the reader or the writer (or both) can be
     * done by extending this class and overriding the corresponding
     * {@code createXXXX()} method.
     * </p>
     *
     * @since 1.7
     */
    public static class DefaultIOFactory implements IOFactory
    {
        @Override
        public PropertiesReader createPropertiesReader(final Reader in)
        {
            return new PropertiesReader(in);
        }

        @Override
        public PropertiesWriter createPropertiesWriter(final Writer out,
                final ListDelimiterHandler handler)
        {
            return new PropertiesWriter(out, handler);
        }
    }

    /**
     * An alternative {@link IOFactory} that tries to mimic the behavior of
     * {@link java.util.Properties} (Jup) more closely. The goal is to allow both of
     * them be used interchangeably when reading and writing properties files
     * without losing or changing information.
     * <p>
     * It also has the option to <em>not</em> use Unicode escapes. When using UTF-8
     * encoding (which is e.g. the new default for resource bundle properties files
     * since Java 9), Unicode escapes are no longer required and avoiding them makes
     * properties files more readable with regular text editors.
     * <p>
     * Some of the ways this implementation differs from {@link DefaultIOFactory}:
     * <ul>
     * <li>Trailing whitespace will not be trimmed from each line.</li>
     * <li>Unknown escape sequences will have their backslash removed.</li>
     * <li>{@code \b} is not a recognized escape sequence.</li>
     * <li>Leading spaces in property values are preserved by escaping them.</li>
     * <li>All natural lines (i.e. in the file) of a logical property line will have
     * their leading whitespace trimmed.</li>
     * <li>Natural lines that look like comment lines within a logical line are not
     * treated as such; they're part of the property value.</li>
     * </ul>
     *
     * @since 2.4
     */
    public static class JupIOFactory implements IOFactory
    {

        /**
         * Whether characters less than {@code \u0020} and characters greater than
         * {@code \u007E} in property keys or values should be escaped using
         * Unicode escape sequences. Not necessary when e.g. writing as UTF-8.
         */
        private final boolean escapeUnicode;

        /**
         * Constructs a new {@link JupIOFactory} with Unicode escaping.
         */
        public JupIOFactory()
        {
            this(true);
        }

        /**
         * Constructs a new {@link JupIOFactory} with optional Unicode escaping. Whether
         * Unicode escaping is required depends on the encoding used to save the
         * properties file. E.g. for ISO-8859-1 this must be turned on, for UTF-8 it's
         * not necessary. Unfortunately this factory can't determine the encoding on its
         * own.
         *
         * @param escapeUnicode whether Unicode characters should be escaped
         */
        public JupIOFactory(final boolean escapeUnicode)
        {
            this.escapeUnicode = escapeUnicode;
        }

        @Override
        public PropertiesReader createPropertiesReader(final Reader in)
        {
            return new JupPropertiesReader(in);
        }

        @Override
        public PropertiesWriter createPropertiesWriter(final Writer out, final ListDelimiterHandler handler)
        {
            return new JupPropertiesWriter(out, handler, escapeUnicode);
        }

    }

    /**
     * A {@link PropertiesReader} that tries to mimic the behavior of
     * {@link java.util.Properties}.
     *
     * @since 2.4
     */
    public static class JupPropertiesReader extends PropertiesReader
    {

        /**
         * Constructor.
         *
         * @param reader A Reader.
         */
        public JupPropertiesReader(final Reader reader)
        {
            super(reader);
        }


        @Override
        public String readProperty() throws IOException
        {
            getCommentLines().clear();
            final StringBuilder buffer = new StringBuilder();

            while (true)
            {
                String line = readLine();
                if (line == null)
                {
                    // EOF
                    if (buffer.length() > 0)
                    {
                        break;
                    }
                    return null;
                }

                // while a property line continues there are no comments (even if the line from
                // the file looks like one)
                if (isCommentLine(line) && (buffer.length() == 0))
                {
                    getCommentLines().add(line);
                    continue;
                }

                // while property line continues left trim all following lines read from the
                // file
                if (buffer.length() > 0)
                {
                    // index of the first non-whitespace character
                    int i;
                    for (i = 0; i < line.length(); i++)
                    {
                        if (!Character.isWhitespace(line.charAt(i)))
                        {
                            break;
                        }
                    }

                    line = line.substring(i);
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

        @Override
        protected void parseProperty(final String line)
        {
            final String[] property = doParseProperty(line, false);
            initPropertyName(property[0]);
            initPropertyValue(property[1]);
            initPropertySeparator(property[2]);
        }

        @Override
        protected String unescapePropertyValue(final String value)
        {
            return unescapeJava(value, true);
        }

    }

    /**
     * A {@link PropertiesWriter} that tries to mimic the behavior of
     * {@link java.util.Properties}.
     *
     * @since 2.4
     */
    public static class JupPropertiesWriter extends PropertiesWriter
    {

        /**
         * The starting ASCII printable character.
         */
        private static final int PRINTABLE_INDEX_END = 0x7e;

        /**
         * The ending ASCII printable character.
         */
        private static final int PRINTABLE_INDEX_START = 0x20;

        /**
         * A UnicodeEscaper for characters outside the ASCII printable range.
         */
        private static final UnicodeEscaper ESCAPER = UnicodeEscaper.outsideOf(PRINTABLE_INDEX_START,
            PRINTABLE_INDEX_END);

        /**
         * Characters that need to be escaped when wring a properties file.
         */
        private static final Map<CharSequence, CharSequence> JUP_CHARS_ESCAPE;
        static
        {
            final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
            initialMap.put("\\", "\\\\");
            initialMap.put("\n", "\\n");
            initialMap.put("\t", "\\t");
            initialMap.put("\f", "\\f");
            initialMap.put("\r", "\\r");
            JUP_CHARS_ESCAPE = Collections.unmodifiableMap(initialMap);
        }

        /**
         * Creates a new instance of {@code JupPropertiesWriter}.
         *
         * @param writer a Writer object providing the underlying stream
         * @param delHandler the delimiter handler for dealing with properties with
         *        multiple values
         * @param escapeUnicode whether Unicode characters should be escaped using
         *        Unicode escapes
         */
        public JupPropertiesWriter(final Writer writer, final ListDelimiterHandler delHandler,
            final boolean escapeUnicode)
        {
            super(writer, delHandler, new ValueTransformer()
            {
                @Override
                public Object transformValue(final Object value)
                {
                    String valueString = String.valueOf(value);

                    CharSequenceTranslator translator;
                    if (escapeUnicode)
                    {
                        translator = new AggregateTranslator(new LookupTranslator(JUP_CHARS_ESCAPE), ESCAPER);
                    }
                    else
                    {
                        translator = new AggregateTranslator(new LookupTranslator(JUP_CHARS_ESCAPE));
                    }

                    valueString = translator.translate(valueString);

                    // escape the first leading space to preserve it (and all after it)
                    if (valueString.startsWith(" "))
                    {
                        valueString = "\\" + valueString;
                    }

                    return valueString;
                }
            });
        }

    }

    /**
     * <p>Unescapes any Java literals found in the {@code String} to a
     * {@code Writer}.</p> This is a slightly modified version of the
     * StringEscapeUtils.unescapeJava() function in commons-lang that doesn't
     * drop escaped separators (i.e '\,').
     *
     * @param str  the {@code String} to unescape, may be null
     * @return the processed string
     * @throws IllegalArgumentException if the Writer is {@code null}
     */
    protected static String unescapeJava(final String str)
    {
        return unescapeJava(str, false);
    }

    /**
     * Unescapes Java literals found in the {@code String} to a {@code Writer}.
     * </p>
     * When the parameter {@code jupCompatible} is {@code false}, the classic
     * behavior is used (see {@link #unescapeJava(String)}). When it's {@code true}
     * a slightly different behavior that's compatible with
     * {@link java.util.Properties} is used (see {@link JupIOFactory}).
     *
     * @param str the {@code String} to unescape, may be null
     * @param jupCompatible whether unescaping is compatible with
     *        {@link java.util.Properties}; otherwise the classic behavior is used
     * @return the processed string
     * @throws IllegalArgumentException if the Writer is {@code null}
     */
    protected static String unescapeJava(final String str, final boolean jupCompatible)
    {
        if (str == null)
        {
            return null;
        }
        final int sz = str.length();
        final StringBuilder out = new StringBuilder(sz);
        final StringBuilder unicode = new StringBuilder(UNICODE_LEN);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++)
        {
            final char ch = str.charAt(i);
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
                        final int value = Integer.parseInt(unicode.toString(), HEX_RADIX);
                        out.append((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    }
                    catch (final NumberFormatException nfe)
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

                if (ch == 'r')
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
                // JUP does not recognize \b
                else if (!jupCompatible && ch == 'b')
                {
                    out.append('\b');
                }
                else if (ch == 'u')
                {
                    // uh-oh, we're in unicode country....
                    inUnicode = true;
                }
                else if (needsUnescape(ch))
                {
                    out.append(ch);
                }
                else
                {
                    // JUP simply throws away the \ of unknown escape sequences
                    if (!jupCompatible)
                    {
                        out.append('\\');
                    }
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
     * Checks whether the specified character needs to be unescaped. This method
     * is called when during reading a property file an escape character ('\')
     * is detected. If the character following the escape character is
     * recognized as a special character which is escaped per default in a Java
     * properties file, it has to be unescaped.
     *
     * @param ch the character in question
     * @return a flag whether this character has to be unescaped
     */
    private static boolean needsUnescape(final char ch)
    {
        return UNESCAPE_CHARACTERS.indexOf(ch) >= 0;
    }

    /**
     * Helper method for loading an included properties file. This method is
     * called by {@code load()} when an {@code include} property
     * is encountered. It tries to resolve relative file names based on the
     * current base path. If this fails, a resolution based on the location of
     * this properties file is tried.
     *
     * @param fileName the name of the file to load
     * @param optional whether or not the {@code fileName} is optional
     * @throws ConfigurationException if loading fails
     */
    private void loadIncludeFile(final String fileName, final boolean optional) throws ConfigurationException
    {
        if (locator == null)
        {
            throw new ConfigurationException("Load operation not properly "
                    + "initialized! Do not call read(InputStream) directly,"
                    + " but use a FileHandler to load a configuration.");
        }

        URL url = locateIncludeFile(locator.getBasePath(), fileName);
        if (url == null)
        {
            final URL baseURL = locator.getSourceURL();
            if (baseURL != null)
            {
                url = locateIncludeFile(baseURL.toString(), fileName);
            }
        }

        if (optional && url == null)
        {
            return;
        }

        if (url == null)
        {
            throw new ConfigurationException("Cannot resolve include file "
                    + fileName);
        }

        final FileHandler fh = new FileHandler(this);
        fh.setFileLocator(locator);
        final FileLocator orgLocator = locator;
        try
        {
            fh.load(url);
        }
        finally
        {
            locator = orgLocator; // reset locator which is changed by load
        }
    }

    /**
     * Tries to obtain the URL of an include file using the specified (optional)
     * base path and file name.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the URL of the include file or <b>null</b> if it cannot be
     *         resolved
     */
    private URL locateIncludeFile(final String basePath, final String fileName)
    {
        final FileLocator includeLocator =
                FileLocatorUtils.fileLocator(locator).sourceURL(null)
                        .basePath(basePath).fileName(fileName).create();
        return FileLocatorUtils.locate(includeLocator);
    }
}
