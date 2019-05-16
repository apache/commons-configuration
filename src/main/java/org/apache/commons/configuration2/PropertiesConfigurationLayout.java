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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A helper class used by {@link PropertiesConfiguration} to keep
 * the layout of a properties file.
 * </p>
 * <p>
 * Instances of this class are associated with a
 * {@code PropertiesConfiguration} object. They are responsible for
 * analyzing properties files and for extracting as much information about the
 * file layout (e.g. empty lines, comments) as possible. When the properties
 * file is written back again it should be close to the original.
 * </p>
 * <p>
 * The {@code PropertiesConfigurationLayout} object associated with a
 * {@code PropertiesConfiguration} object can be obtained using the
 * {@code getLayout()} method of the configuration. Then the methods
 * provided by this class can be used to alter the properties file's layout.
 * </p>
 * <p>
 * Implementation note: This is a very simple implementation, which is far away
 * from being perfect, i.e. the original layout of a properties file won't be
 * reproduced in all cases. One limitation is that comments for multi-valued
 * property keys are concatenated. Maybe this implementation can later be
 * improved.
 * </p>
 * <p>
 * To get an impression how this class works consider the following properties
 * file:
 * </p>
 *
 * <pre>
 * # A demo configuration file
 * # for Demo App 1.42
 *
 * # Application name
 * AppName=Demo App
 *
 * # Application vendor
 * AppVendor=DemoSoft
 *
 *
 * # GUI properties
 * # Window Color
 * windowColors=0xFFFFFF,0x000000
 *
 * # Include some setting
 * include=settings.properties
 * # Another vendor
 * AppVendor=TestSoft
 * </pre>
 *
 * <p>
 * For this example the following points are relevant:
 * </p>
 * <ul>
 * <li>The first two lines are set as header comment. The header comment is
 * determined by the last blanc line before the first property definition.</li>
 * <li>For the property {@code AppName} one comment line and one
 * leading blanc line is stored.</li>
 * <li>For the property {@code windowColors} two comment lines and two
 * leading blanc lines are stored.</li>
 * <li>Include files is something this class cannot deal with well. When saving
 * the properties configuration back, the included properties are simply
 * contained in the original file. The comment before the include property is
 * skipped.</li>
 * <li>For all properties except for {@code AppVendor} the &quot;single
 * line&quot; flag is set. This is relevant only for {@code windowColors},
 * which has multiple values defined in one line using the separator character.</li>
 * <li>The {@code AppVendor} property appears twice. The comment lines
 * are concatenated, so that {@code layout.getComment("AppVendor");} will
 * result in <code>Application vendor&lt;CR&gt;Another vendor</code>, with
 * <code>&lt;CR&gt;</code> meaning the line separator. In addition the
 * &quot;single line&quot; flag is set to <b>false</b> for this property. When
 * the file is saved, two property definitions will be written (in series).</li>
 * </ul>
 *
 * @since 1.3
 */
public class PropertiesConfigurationLayout implements EventListener<ConfigurationEvent>
{
    /** Constant for the line break character. */
    private static final String CR = "\n";

    /** Constant for the default comment prefix. */
    private static final String COMMENT_PREFIX = "# ";

    /** Stores a map with the contained layout information. */
    private final Map<String, PropertyLayoutData> layoutData;

    /** Stores the header comment. */
    private String headerComment;

    /** Stores the footer comment. */
    private String footerComment;

    /** The global separator that will be used for all properties. */
    private String globalSeparator;

    /** The line separator.*/
    private String lineSeparator;

    /** A counter for determining nested load calls. */
    private final AtomicInteger loadCounter;

    /** Stores the force single line flag. */
    private boolean forceSingleLine;

    /**
     * Creates a new, empty instance of {@code PropertiesConfigurationLayout}.
     */
    public PropertiesConfigurationLayout()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code PropertiesConfigurationLayout} and
     * copies the data of the specified layout object.
     *
     * @param c the layout object to be copied
     */
    public PropertiesConfigurationLayout(final PropertiesConfigurationLayout c)
    {
        loadCounter = new AtomicInteger();
        layoutData = new LinkedHashMap<>();

        if (c != null)
        {
            copyFrom(c);
        }
    }

    /**
     * Returns the comment for the specified property key in a canonical form.
     * &quot;Canonical&quot; means that either all lines start with a comment
     * character or none. If the {@code commentChar} parameter is <b>false</b>,
     * all comment characters are removed, so that the result is only the plain
     * text of the comment. Otherwise it is ensured that each line of the
     * comment starts with a comment character. Also, line breaks in the comment
     * are normalized to the line separator &quot;\n&quot;.
     *
     * @param key the key of the property
     * @param commentChar determines whether all lines should start with comment
     * characters or not
     * @return the canonical comment for this key (can be <b>null</b>)
     */
    public String getCanonicalComment(final String key, final boolean commentChar)
    {
        return constructCanonicalComment(getComment(key), commentChar);
    }

    /**
     * Returns the comment for the specified property key. The comment is
     * returned as it was set (either manually by calling
     * {@code setComment()} or when it was loaded from a properties
     * file). No modifications are performed.
     *
     * @param key the key of the property
     * @return the comment for this key (can be <b>null</b>)
     */
    public String getComment(final String key)
    {
        return fetchLayoutData(key).getComment();
    }

    /**
     * Sets the comment for the specified property key. The comment (or its
     * single lines if it is a multi-line comment) can start with a comment
     * character. If this is the case, it will be written without changes.
     * Otherwise a default comment character is added automatically.
     *
     * @param key the key of the property
     * @param comment the comment for this key (can be <b>null</b>, then the
     * comment will be removed)
     */
    public void setComment(final String key, final String comment)
    {
        fetchLayoutData(key).setComment(comment);
    }

    /**
     * Returns the number of blanc lines before this property key. If this key
     * does not exist, 0 will be returned.
     *
     * @param key the property key
     * @return the number of blanc lines before the property definition for this
     * key
     */
    public int getBlancLinesBefore(final String key)
    {
        return fetchLayoutData(key).getBlancLines();
    }

    /**
     * Sets the number of blanc lines before the given property key. This can be
     * used for a logical grouping of properties.
     *
     * @param key the property key
     * @param number the number of blanc lines to add before this property
     * definition
     */
    public void setBlancLinesBefore(final String key, final int number)
    {
        fetchLayoutData(key).setBlancLines(number);
    }

    /**
     * Returns the header comment of the represented properties file in a
     * canonical form. With the {@code commentChar} parameter it can be
     * specified whether comment characters should be stripped or be always
     * present.
     *
     * @param commentChar determines the presence of comment characters
     * @return the header comment (can be <b>null</b>)
     */
    public String getCanonicalHeaderComment(final boolean commentChar)
    {
        return constructCanonicalComment(getHeaderComment(), commentChar);
    }

    /**
     * Returns the header comment of the represented properties file. This
     * method returns the header comment exactly as it was set using
     * {@code setHeaderComment()} or extracted from the loaded properties
     * file.
     *
     * @return the header comment (can be <b>null</b>)
     */
    public String getHeaderComment()
    {
        return headerComment;
    }

    /**
     * Sets the header comment for the represented properties file. This comment
     * will be output on top of the file.
     *
     * @param comment the comment
     */
    public void setHeaderComment(final String comment)
    {
        headerComment = comment;
    }

    /**
     * Returns the footer comment of the represented properties file in a
     * canonical form. This method works like
     * {@code getCanonicalHeaderComment()}, but reads the footer comment.
     *
     * @param commentChar determines the presence of comment characters
     * @return the footer comment (can be <b>null</b>)
     * @see #getCanonicalHeaderComment(boolean)
     * @since 2.0
     */
    public String getCanonicalFooterCooment(final boolean commentChar)
    {
        return constructCanonicalComment(getFooterComment(), commentChar);
    }

    /**
     * Returns the footer comment of the represented properties file. This
     * method returns the footer comment exactly as it was set using
     * {@code setFooterComment()} or extracted from the loaded properties
     * file.
     *
     * @return the footer comment (can be <b>null</b>)
     * @since 2.0
     */
    public String getFooterComment()
    {
        return footerComment;
    }

    /**
     * Sets the footer comment for the represented properties file. This comment
     * will be output at the bottom of the file.
     *
     * @param footerComment the footer comment
     * @since 2.0
     */
    public void setFooterComment(final String footerComment)
    {
        this.footerComment = footerComment;
    }

    /**
     * Returns a flag whether the specified property is defined on a single
     * line. This is meaningful only if this property has multiple values.
     *
     * @param key the property key
     * @return a flag if this property is defined on a single line
     */
    public boolean isSingleLine(final String key)
    {
        return fetchLayoutData(key).isSingleLine();
    }

    /**
     * Sets the &quot;single line flag&quot; for the specified property key.
     * This flag is evaluated if the property has multiple values (i.e. if it is
     * a list property). In this case, if the flag is set, all values will be
     * written in a single property definition using the list delimiter as
     * separator. Otherwise multiple lines will be written for this property,
     * each line containing one property value.
     *
     * @param key the property key
     * @param f the single line flag
     */
    public void setSingleLine(final String key, final boolean f)
    {
        fetchLayoutData(key).setSingleLine(f);
    }

    /**
     * Returns the &quot;force single line&quot; flag.
     *
     * @return the force single line flag
     * @see #setForceSingleLine(boolean)
     */
    public boolean isForceSingleLine()
    {
        return forceSingleLine;
    }

    /**
     * Sets the &quot;force single line&quot; flag. If this flag is set, all
     * properties with multiple values are written on single lines. This mode
     * provides more compatibility with {@code java.lang.Properties},
     * which cannot deal with multiple definitions of a single property. This
     * mode has no effect if the list delimiter parsing is disabled.
     *
     * @param f the force single line flag
     */
    public void setForceSingleLine(final boolean f)
    {
        forceSingleLine = f;
    }

    /**
     * Returns the separator for the property with the given key.
     *
     * @param key the property key
     * @return the property separator for this property
     * @since 1.7
     */
    public String getSeparator(final String key)
    {
        return fetchLayoutData(key).getSeparator();
    }

    /**
     * Sets the separator to be used for the property with the given key. The
     * separator is the string between the property key and its value. For new
     * properties &quot; = &quot; is used. When a properties file is read, the
     * layout tries to determine the separator for each property. With this
     * method the separator can be changed. To be compatible with the properties
     * format only the characters {@code =} and {@code :} (with or
     * without whitespace) should be used, but this method does not enforce this
     * - it accepts arbitrary strings. If the key refers to a property with
     * multiple values that are written on multiple lines, this separator will
     * be used on all lines.
     *
     * @param key the key for the property
     * @param sep the separator to be used for this property
     * @since 1.7
     */
    public void setSeparator(final String key, final String sep)
    {
        fetchLayoutData(key).setSeparator(sep);
    }

    /**
     * Returns the global separator.
     *
     * @return the global properties separator
     * @since 1.7
     */
    public String getGlobalSeparator()
    {
        return globalSeparator;
    }

    /**
     * Sets the global separator for properties. With this method a separator
     * can be set that will be used for all properties when writing the
     * configuration. This is an easy way of determining the properties
     * separator globally. To be compatible with the properties format only the
     * characters {@code =} and {@code :} (with or without whitespace)
     * should be used, but this method does not enforce this - it accepts
     * arbitrary strings. If the global separator is set to <b>null</b>,
     * property separators are not changed. This is the default behavior as it
     * produces results that are closer to the original properties file.
     *
     * @param globalSeparator the separator to be used for all properties
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
        return lineSeparator;
    }

    /**
     * Sets the line separator. When writing the properties configuration, all
     * lines are terminated with this separator. If no separator was set, the
     * platform-specific default line separator is used.
     *
     * @param lineSeparator the line separator
     * @since 1.7
     */
    public void setLineSeparator(final String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns a set with all property keys managed by this object.
     *
     * @return a set with all contained property keys
     */
    public Set<String> getKeys()
    {
        return layoutData.keySet();
    }

    /**
     * Reads a properties file and stores its internal structure. The found
     * properties will be added to the specified configuration object.
     *
     * @param config the associated configuration object
     * @param in the reader to the properties file
     * @throws ConfigurationException if an error occurs
     */
    public void load(final PropertiesConfiguration config, final Reader in)
            throws ConfigurationException
    {
        loadCounter.incrementAndGet();
        final PropertiesConfiguration.PropertiesReader reader =
                config.getIOFactory().createPropertiesReader(in);

        try
        {
            while (reader.nextProperty())
            {
                if (config.propertyLoaded(reader.getPropertyName(),
                        reader.getPropertyValue()))
                {
                    final boolean contained = layoutData.containsKey(reader
                            .getPropertyName());
                    int blancLines = 0;
                    int idx = checkHeaderComment(reader.getCommentLines());
                    while (idx < reader.getCommentLines().size()
                            && reader.getCommentLines().get(idx).length() < 1)
                    {
                        idx++;
                        blancLines++;
                    }
                    final String comment = extractComment(reader.getCommentLines(),
                            idx, reader.getCommentLines().size() - 1);
                    final PropertyLayoutData data = fetchLayoutData(reader
                            .getPropertyName());
                    if (contained)
                    {
                        data.addComment(comment);
                        data.setSingleLine(false);
                    }
                    else
                    {
                        data.setComment(comment);
                        data.setBlancLines(blancLines);
                        data.setSeparator(reader.getPropertySeparator());
                    }
                }
            }

            setFooterComment(extractComment(reader.getCommentLines(), 0, reader
                    .getCommentLines().size() - 1));
        }
        catch (final IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        finally
        {
            loadCounter.decrementAndGet();
        }
    }

    /**
     * Writes the properties file to the given writer, preserving as much of its
     * structure as possible.
     *
     * @param config the associated configuration object
     * @param out the writer
     * @throws ConfigurationException if an error occurs
     */
    public void save(final PropertiesConfiguration config, final Writer out) throws ConfigurationException
    {
        try
        {
            final PropertiesConfiguration.PropertiesWriter writer =
                    config.getIOFactory().createPropertiesWriter(out,
                            config.getListDelimiterHandler());
            writer.setGlobalSeparator(getGlobalSeparator());
            if (getLineSeparator() != null)
            {
                writer.setLineSeparator(getLineSeparator());
            }

            if (headerComment != null)
            {
                writeComment(writer, getCanonicalHeaderComment(true));
                writer.writeln(null);
            }

            for (final String key : getKeys())
            {
                if (config.containsKeyInternal(key))
                {

                    // Output blank lines before property
                    for (int i = 0; i < getBlancLinesBefore(key); i++)
                    {
                        writer.writeln(null);
                    }

                    // Output the comment
                    writeComment(writer, getCanonicalComment(key, true));

                    // Output the property and its value
                    final boolean singleLine = isForceSingleLine() || isSingleLine(key);
                    writer.setCurrentSeparator(getSeparator(key));
                    writer.writeProperty(key, config.getPropertyInternal(
                            key), singleLine);
                }
            }

            writeComment(writer, getCanonicalFooterCooment(true));
            writer.flush();
        }
        catch (final IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
    }

    /**
     * The event listener callback. Here event notifications of the
     * configuration object are processed to update the layout object properly.
     *
     * @param event the event object
     */
    @Override
    public void onEvent(final ConfigurationEvent event)
    {
        if (!event.isBeforeUpdate() && loadCounter.get() == 0)
        {
            if (ConfigurationEvent.ADD_PROPERTY.equals(event.getEventType()))
            {
                final boolean contained =
                        layoutData.containsKey(event.getPropertyName());
                final PropertyLayoutData data =
                        fetchLayoutData(event.getPropertyName());
                data.setSingleLine(!contained);
            }
            else if (ConfigurationEvent.CLEAR_PROPERTY.equals(event
                    .getEventType()))
            {
                layoutData.remove(event.getPropertyName());
            }
            else if (ConfigurationEvent.CLEAR.equals(event.getEventType()))
            {
                clear();
            }
            else if (ConfigurationEvent.SET_PROPERTY.equals(event
                    .getEventType()))
            {
                fetchLayoutData(event.getPropertyName());
            }
        }
    }

    /**
     * Returns a layout data object for the specified key. If this is a new key,
     * a new object is created and initialized with default values.
     *
     * @param key the key
     * @return the corresponding layout data object
     */
    private PropertyLayoutData fetchLayoutData(final String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Property key must not be null!");
        }

        PropertyLayoutData data = layoutData.get(key);
        if (data == null)
        {
            data = new PropertyLayoutData();
            data.setSingleLine(true);
            layoutData.put(key, data);
        }

        return data;
    }

    /**
     * Removes all content from this layout object.
     */
    private void clear()
    {
        layoutData.clear();
        setHeaderComment(null);
        setFooterComment(null);
    }

    /**
     * Tests whether a line is a comment, i.e. whether it starts with a comment
     * character.
     *
     * @param line the line
     * @return a flag if this is a comment line
     */
    static boolean isCommentLine(final String line)
    {
        return PropertiesConfiguration.isCommentLine(line);
    }

    /**
     * Trims a comment. This method either removes all comment characters from
     * the given string, leaving only the plain comment text or ensures that
     * every line starts with a valid comment character.
     *
     * @param s the string to be processed
     * @param comment if <b>true</b>, a comment character will always be
     * enforced; if <b>false</b>, it will be removed
     * @return the trimmed comment
     */
    static String trimComment(final String s, final boolean comment)
    {
        final StringBuilder buf = new StringBuilder(s.length());
        int lastPos = 0;
        int pos;

        do
        {
            pos = s.indexOf(CR, lastPos);
            if (pos >= 0)
            {
                final String line = s.substring(lastPos, pos);
                buf.append(stripCommentChar(line, comment)).append(CR);
                lastPos = pos + CR.length();
            }
        } while (pos >= 0);

        if (lastPos < s.length())
        {
            buf.append(stripCommentChar(s.substring(lastPos), comment));
        }
        return buf.toString();
    }

    /**
     * Either removes the comment character from the given comment line or
     * ensures that the line starts with a comment character.
     *
     * @param s the comment line
     * @param comment if <b>true</b>, a comment character will always be
     * enforced; if <b>false</b>, it will be removed
     * @return the line without comment character
     */
    static String stripCommentChar(final String s, final boolean comment)
    {
        if (StringUtils.isBlank(s) || (isCommentLine(s) == comment))
        {
            return s;
        }
        if (!comment)
        {
            int pos = 0;
            // find first comment character
            while (PropertiesConfiguration.COMMENT_CHARS.indexOf(s
                    .charAt(pos)) < 0)
            {
                pos++;
            }

            // Remove leading spaces
            pos++;
            while (pos < s.length()
                    && Character.isWhitespace(s.charAt(pos)))
            {
                pos++;
            }

            return (pos < s.length()) ? s.substring(pos)
                    : StringUtils.EMPTY;
        }
        return COMMENT_PREFIX + s;
    }

    /**
     * Extracts a comment string from the given range of the specified comment
     * lines. The single lines are added using a line feed as separator.
     *
     * @param commentLines a list with comment lines
     * @param from the start index
     * @param to the end index (inclusive)
     * @return the comment string (<b>null</b> if it is undefined)
     */
    private String extractComment(final List<String> commentLines, final int from, final int to)
    {
        if (to < from)
        {
            return null;
        }
        final StringBuilder buf = new StringBuilder(commentLines.get(from));
        for (int i = from + 1; i <= to; i++)
        {
            buf.append(CR);
            buf.append(commentLines.get(i));
        }
        return buf.toString();
    }

    /**
     * Checks if parts of the passed in comment can be used as header comment.
     * This method checks whether a header comment can be defined (i.e. whether
     * this is the first comment in the loaded file). If this is the case, it is
     * searched for the latest blanc line. This line will mark the end of the
     * header comment. The return value is the index of the first line in the
     * passed in list, which does not belong to the header comment.
     *
     * @param commentLines the comment lines
     * @return the index of the next line after the header comment
     */
    private int checkHeaderComment(final List<String> commentLines)
    {
        if (loadCounter.get() == 1 && layoutData.isEmpty())
        {
            // This is the first comment. Search for blanc lines.
            int index = commentLines.size() - 1;
            while (index >= 0
                    && commentLines.get(index).length() > 0)
            {
                index--;
            }
            if (getHeaderComment() == null)
            {
                setHeaderComment(extractComment(commentLines, 0, index - 1));
            }
            return index + 1;
        }
        return 0;
    }

    /**
     * Copies the data from the given layout object.
     *
     * @param c the layout object to copy
     */
    private void copyFrom(final PropertiesConfigurationLayout c)
    {
        for (final String key : c.getKeys())
        {
            final PropertyLayoutData data = c.layoutData.get(key);
            layoutData.put(key, data.clone());
        }

        setHeaderComment(c.getHeaderComment());
        setFooterComment(c.getFooterComment());
    }

    /**
     * Helper method for writing a comment line. This method ensures that the
     * correct line separator is used if the comment spans multiple lines.
     *
     * @param writer the writer
     * @param comment the comment to write
     * @throws IOException if an IO error occurs
     */
    private static void writeComment(
            final PropertiesConfiguration.PropertiesWriter writer, final String comment)
            throws IOException
    {
        if (comment != null)
        {
            writer.writeln(StringUtils.replace(comment, CR, writer
                    .getLineSeparator()));
        }
    }

    /**
     * Helper method for generating a comment string. Depending on the boolean
     * argument the resulting string either has no comment characters or a
     * leading comment character at each line.
     *
     * @param comment the comment string to be processed
     * @param commentChar determines the presence of comment characters
     * @return the canonical comment string (can be <b>null</b>)
     */
    private static String constructCanonicalComment(final String comment,
            final boolean commentChar)
    {
        return (comment == null) ? null : trimComment(comment, commentChar);
    }

    /**
     * A helper class for storing all layout related information for a
     * configuration property.
     */
    static class PropertyLayoutData implements Cloneable
    {
        /** Stores the comment for the property. */
        private StringBuffer comment;

        /** The separator to be used for this property. */
        private String separator;

        /** Stores the number of blanc lines before this property. */
        private int blancLines;

        /** Stores the single line property. */
        private boolean singleLine;

        /**
         * Creates a new instance of {@code PropertyLayoutData}.
         */
        public PropertyLayoutData()
        {
            singleLine = true;
            separator = PropertiesConfiguration.DEFAULT_SEPARATOR;
        }

        /**
         * Returns the number of blanc lines before this property.
         *
         * @return the number of blanc lines before this property
         */
        public int getBlancLines()
        {
            return blancLines;
        }

        /**
         * Sets the number of properties before this property.
         *
         * @param blancLines the number of properties before this property
         */
        public void setBlancLines(final int blancLines)
        {
            this.blancLines = blancLines;
        }

        /**
         * Returns the single line flag.
         *
         * @return the single line flag
         */
        public boolean isSingleLine()
        {
            return singleLine;
        }

        /**
         * Sets the single line flag.
         *
         * @param singleLine the single line flag
         */
        public void setSingleLine(final boolean singleLine)
        {
            this.singleLine = singleLine;
        }

        /**
         * Adds a comment for this property. If already a comment exists, the
         * new comment is added (separated by a newline).
         *
         * @param s the comment to add
         */
        public void addComment(final String s)
        {
            if (s != null)
            {
                if (comment == null)
                {
                    comment = new StringBuffer(s);
                }
                else
                {
                    comment.append(CR).append(s);
                }
            }
        }

        /**
         * Sets the comment for this property.
         *
         * @param s the new comment (can be <b>null</b>)
         */
        public void setComment(final String s)
        {
            if (s == null)
            {
                comment = null;
            }
            else
            {
                comment = new StringBuffer(s);
            }
        }

        /**
         * Returns the comment for this property. The comment is returned as it
         * is, without processing of comment characters.
         *
         * @return the comment (can be <b>null</b>)
         */
        public String getComment()
        {
            return (comment == null) ? null : comment.toString();
        }

        /**
         * Returns the separator that was used for this property.
         *
         * @return the property separator
         */
        public String getSeparator()
        {
            return separator;
        }

        /**
         * Sets the separator to be used for the represented property.
         *
         * @param separator the property separator
         */
        public void setSeparator(final String separator)
        {
            this.separator = separator;
        }

        /**
         * Creates a copy of this object.
         *
         * @return the copy
         */
        @Override
        public PropertyLayoutData clone()
        {
            try
            {
                final PropertyLayoutData copy = (PropertyLayoutData) super.clone();
                if (comment != null)
                {
                    // must copy string buffer, too
                    copy.comment = new StringBuffer(getComment());
                }
                return copy;
            }
            catch (final CloneNotSupportedException cnex)
            {
                // This cannot happen!
                throw new ConfigurationRuntimeException(cnex);
            }
        }
    }
}
