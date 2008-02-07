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

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A helper class used by <code>{@link PropertiesConfiguration}</code> to keep
 * the layout of a properties file.
 * </p>
 * <p>
 * Instances of this class are associated with a
 * <code>PropertiesConfiguration</code> object. They are responsible for
 * analyzing properties files and for extracting as much information about the
 * file layout (e.g. empty lines, comments) as possible. When the properties
 * file is written back again it should be close to the original.
 * </p>
 * <p>
 * The <code>PropertiesConfigurationLayout</code> object associated with a
 * <code>PropertiesConfiguration</code> object can be obtained using the
 * <code>getLayout()</code> method of the configuration. Then the methods
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
 * <p>
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
 * </p>
 * <p>
 * For this example the following points are relevant:
 * </p>
 * <p>
 * <ul>
 * <li>The first two lines are set as header comment. The header comment is
 * determined by the last blanc line before the first property definition.</li>
 * <li>For the property <code>AppName</code> one comment line and one
 * leading blanc line is stored.</li>
 * <li>For the property <code>windowColors</code> two comment lines and two
 * leading blanc lines are stored.</li>
 * <li>Include files is something this class cannot deal with well. When saving
 * the properties configuration back, the included properties are simply
 * contained in the original file. The comment before the include property is
 * skipped.</li>
 * <li>For all properties except for <code>AppVendor</code> the &quot;single
 * line&quot; flag is set. This is relevant only for <code>windowColors</code>,
 * which has multiple values defined in one line using the separator character.</li>
 * <li>The <code>AppVendor</code> property appears twice. The comment lines
 * are concatenated, so that <code>layout.getComment("AppVendor");</code> will
 * result in <code>Application vendor&lt;CR&gt;Another vendor</code>, whith
 * <code>&lt;CR&gt;</code> meaning the line separator. In addition the
 * &quot;single line&quot; flag is set to <b>false</b> for this property. When
 * the file is saved, two property definitions will be written (in series).</li>
 * </ul>
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public class PropertiesConfigurationLayout implements ConfigurationListener
{
    /** Constant for the line break character. */
    private static final String CR = System.getProperty("line.separator");

    /** Constant for the default comment prefix. */
    private static final String COMMENT_PREFIX = "# ";

    /** Stores the associated configuration object. */
    private PropertiesConfiguration configuration;

    /** Stores a map with the contained layout information. */
    private Map<String, PropertyLayoutData> layoutData;

    /** Stores the header comment. */
    private String headerComment;

    /** A counter for determining nested load calls. */
    private int loadCounter;

    /** Stores the force single line flag. */
    private boolean forceSingleLine;

    /**
     * Creates a new instance of <code>PropertiesConfigurationLayout</code>
     * and initializes it with the associated configuration object.
     *
     * @param config the configuration (must not be <b>null</b>)
     */
    public PropertiesConfigurationLayout(PropertiesConfiguration config)
    {
        this(config, null);
    }

    /**
     * Creates a new instance of <code>PropertiesConfigurationLayout</code>
     * and initializes it with the given configuration object. The data of the
     * specified layout object is copied.
     *
     * @param config the configuration (must not be <b>null</b>)
     * @param c the layout object to be copied
     */
    public PropertiesConfigurationLayout(PropertiesConfiguration config,
            PropertiesConfigurationLayout c)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Configuration must not be null!");
        }
        configuration = config;
        layoutData = new LinkedHashMap<String, PropertyLayoutData>();
        config.addConfigurationListener(this);

        if (c != null)
        {
            copyFrom(c);
        }
    }

    /**
     * Returns the associated configuration object.
     *
     * @return the associated configuration
     */
    public PropertiesConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Returns the comment for the specified property key in a cononical form.
     * &quot;Canonical&quot; means that either all lines start with a comment
     * character or none. The <code>commentChar</code> parameter is <b>false</b>,
     * all comment characters are removed, so that the result is only the plain
     * text of the comment. Otherwise it is ensured that each line of the
     * comment starts with a comment character.
     *
     * @param key the key of the property
     * @param commentChar determines whether all lines should start with comment
     * characters or not
     * @return the canonical comment for this key (can be <b>null</b>)
     */
    public String getCanonicalComment(String key, boolean commentChar)
    {
        String comment = getComment(key);
        if (comment == null)
        {
            return null;
        }
        else
        {
            return trimComment(comment, commentChar);
        }
    }

    /**
     * Returns the comment for the specified property key. The comment is
     * returned as it was set (either manually by calling
     * <code>setComment()</code> or when it was loaded from a properties
     * file). No modifications are performed.
     *
     * @param key the key of the property
     * @return the comment for this key (can be <b>null</b>)
     */
    public String getComment(String key)
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
    public void setComment(String key, String comment)
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
    public int getBlancLinesBefore(String key)
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
    public void setBlancLinesBefore(String key, int number)
    {
        fetchLayoutData(key).setBlancLines(number);
    }

    /**
     * Returns the header comment of the represented properties file in a
     * canonical form. With the <code>commentChar</code> parameter it can be
     * specified whether comment characters should be stripped or be always
     * present.
     *
     * @param commentChar determines the presence of comment characters
     * @return the header comment (can be <b>null</b>)
     */
    public String getCanonicalHeaderComment(boolean commentChar)
    {
        return (getHeaderComment() == null) ? null : trimComment(
                getHeaderComment(), commentChar);
    }

    /**
     * Returns the header comment of the represented properties file. This
     * method returns the header comment exactly as it was set using
     * <code>setHeaderComment()</code> or extracted from the loaded properties
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
    public void setHeaderComment(String comment)
    {
        headerComment = comment;
    }

    /**
     * Returns a flag whether the specified property is defined on a single
     * line. This is meaningful only if this property has multiple values.
     *
     * @param key the property key
     * @return a flag if this property is defined on a single line
     */
    public boolean isSingleLine(String key)
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
    public void setSingleLine(String key, boolean f)
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
     * provides more compatibility with <code>java.lang.Properties</code>,
     * which cannot deal with multiple definitions of a single property. This
     * mode has no effect if the list delimiter parsing is disabled.
     *
     * @param f the force single line flag
     */
    public void setForceSingleLine(boolean f)
    {
        forceSingleLine = f;
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
     * properties will be added to the associated configuration object.
     *
     * @param in the reader to the properties file
     * @throws ConfigurationException if an error occurs
     */
    public void load(Reader in) throws ConfigurationException
    {
        if (++loadCounter == 1)
        {
            getConfiguration().removeConfigurationListener(this);
        }
        PropertiesConfiguration.PropertiesReader reader = new PropertiesConfiguration.PropertiesReader(
                in, getConfiguration().getListDelimiter());

        try
        {
            while (reader.nextProperty())
            {
                if (getConfiguration().propertyLoaded(reader.getPropertyName(),
                        reader.getPropertyValue()))
                {
                    boolean contained = layoutData.containsKey(reader
                            .getPropertyName());
                    int blancLines = 0;
                    int idx = checkHeaderComment(reader.getCommentLines());
                    while (idx < reader.getCommentLines().size()
                            && ((String) reader.getCommentLines().get(idx))
                                    .length() < 1)
                    {
                        idx++;
                        blancLines++;
                    }
                    String comment = extractComment(reader.getCommentLines(),
                            idx, reader.getCommentLines().size() - 1);
                    PropertyLayoutData data = fetchLayoutData(reader
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
                    }
                }
            }
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        finally
        {
            if (--loadCounter == 0)
            {
                getConfiguration().addConfigurationListener(this);
            }
        }
    }

    /**
     * Writes the properties file to the given writer, preserving as much of its
     * structure as possible.
     *
     * @param out the writer
     * @throws ConfigurationException if an error occurs
     */
    public void save(Writer out) throws ConfigurationException
    {
        try
        {
            char delimiter = getConfiguration().isDelimiterParsingDisabled() ? 0
                    : getConfiguration().getListDelimiter();
            PropertiesConfiguration.PropertiesWriter writer = new PropertiesConfiguration.PropertiesWriter(
                    out, delimiter);
            if (headerComment != null)
            {
                writer.writeln(getCanonicalHeaderComment(true));
                writer.writeln(null);
            }

            for (String key : layoutData.keySet())
            {
                if (getConfiguration().containsKey(key))
                {

                    // Output blank lines before property
                    for (int i = 0; i < getBlancLinesBefore(key); i++)
                    {
                        writer.writeln(null);
                    }

                    // Output the comment
                    if (getComment(key) != null)
                    {
                        writer.writeln(getCanonicalComment(key, true));
                    }

                    // Output the property and its value
                    boolean singleLine = (isForceSingleLine() || isSingleLine(key))
                            && !getConfiguration().isDelimiterParsingDisabled();
                    writer.writeProperty(key, getConfiguration().getProperty(
                            key), singleLine);
                }
            }
            writer.flush();
        }
        catch (IOException ioex)
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
    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.isBeforeUpdate())
        {
            if (AbstractFileConfiguration.EVENT_RELOAD == event.getType())
            {
                clear();
            }
        }

        else
        {
            switch (event.getType())
            {
            case AbstractConfiguration.EVENT_ADD_PROPERTY:
                boolean contained = layoutData.containsKey(event
                        .getPropertyName());
                PropertyLayoutData data = fetchLayoutData(event
                        .getPropertyName());
                data.setSingleLine(!contained);
                break;
            case AbstractConfiguration.EVENT_CLEAR_PROPERTY:
                layoutData.remove(event.getPropertyName());
                break;
            case AbstractConfiguration.EVENT_CLEAR:
                clear();
                break;
            case AbstractConfiguration.EVENT_SET_PROPERTY:
                fetchLayoutData(event.getPropertyName());
                break;
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
    private PropertyLayoutData fetchLayoutData(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Property key must not be null!");
        }

        PropertyLayoutData data = (PropertyLayoutData) layoutData.get(key);
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
    }

    /**
     * Tests whether a line is a comment, i.e. whether it starts with a comment
     * character.
     *
     * @param line the line
     * @return a flag if this is a comment line
     */
    static boolean isCommentLine(String line)
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
    static String trimComment(String s, boolean comment)
    {
        StringBuffer buf = new StringBuffer(s.length());
        int lastPos = 0;
        int pos;

        do
        {
            pos = s.indexOf(CR, lastPos);
            if (pos >= 0)
            {
                String line = s.substring(lastPos, pos);
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
    static String stripCommentChar(String s, boolean comment)
    {
        if (s.length() < 1 || (isCommentLine(s) == comment))
        {
            return s;
        }

        else
        {
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
            else
            {
                return COMMENT_PREFIX + s;
            }
        }
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
    private String extractComment(List<String> commentLines, int from, int to)
    {
        if (to < from)
        {
            return null;
        }

        else
        {
            StringBuilder buf = new StringBuilder(commentLines.get(from));
            for (int i = from + 1; i <= to; i++)
            {
                buf.append(CR);
                buf.append(commentLines.get(i));
            }
            return buf.toString();
        }
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
    private int checkHeaderComment(List<String> commentLines)
    {
        if (loadCounter == 1 && getHeaderComment() == null
                && layoutData.isEmpty())
        {
            // This is the first comment. Search for blanc lines.
            int index = commentLines.size() - 1;
            while (index >= 0 && commentLines.get(index).length() > 0)
            {
                index--;
            }
            setHeaderComment(extractComment(commentLines, 0, index - 1));
            return index + 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Copies the data from the given layout object.
     *
     * @param c the layout object to copy
     */
    private void copyFrom(PropertiesConfigurationLayout c)
    {
        for (String key : c.getKeys())
        {
            PropertyLayoutData data = (PropertyLayoutData) c.layoutData
                    .get(key);
            layoutData.put(key, (PropertyLayoutData) data.clone());
        }
    }

    /**
     * A helper class for storing all layout related information for a
     * configuration property.
     */
    static class PropertyLayoutData implements Cloneable
    {
        /** Stores the comment for the property. */
        private StringBuffer comment;

        /** Stores the number of blanc lines before this property. */
        private int blancLines;

        /** Stores the single line property. */
        private boolean singleLine;

        /**
         * Creates a new instance of <code>PropertyLayoutData</code>.
         */
        public PropertyLayoutData()
        {
            singleLine = true;
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
        public void setBlancLines(int blancLines)
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
        public void setSingleLine(boolean singleLine)
        {
            this.singleLine = singleLine;
        }

        /**
         * Adds a comment for this property. If already a comment exists, the
         * new comment is added (separated by a newline).
         *
         * @param s the comment to add
         */
        public void addComment(String s)
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
        public void setComment(String s)
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
         * Creates a copy of this object.
         *
         * @return the copy
         */
        public Object clone()
        {
            try
            {
                PropertyLayoutData copy = (PropertyLayoutData) super.clone();
                if (comment != null)
                {
                    // must copy string buffer, too
                    copy.comment = new StringBuffer(getComment());
                }
                return copy;
            }
            catch (CloneNotSupportedException cnex)
            {
                // This cannot happen!
                throw new ConfigurationRuntimeException(cnex);
            }
        }
    }
}
