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

package org.apache.commons.configuration2.plist;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * NeXT / OpenStep style configuration. This configuration can read and write
 * ASCII plist files. It supports the GNUStep extension to specify date objects.
 * <p>
 * References:
 * <ul>
 *   <li><a
 * href="http://developer.apple.com/documentation/Cocoa/Conceptual/PropertyLists/OldStylePlists/OldStylePLists.html">
 * Apple Documentation - Old-Style ASCII Property Lists</a></li>
 *   <li><a
 * href="http://www.gnustep.org/resources/documentation/Developer/Base/Reference/NSPropertyList.html">
 * GNUStep Documentation</a></li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>
 * {
 *     foo = "bar";
 *
 *     array = ( value1, value2, value3 );
 *
 *     data = &lt;4f3e0145ab&gt;;
 *
 *     date = &lt;*D2007-05-05 20:05:00 +0100&gt;;
 *
 *     nested =
 *     {
 *         key1 = value1;
 *         key2 = value;
 *         nested =
 *         {
 *             foo = bar
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 1.2
 *
 * @author Emmanuel Bourg
 */
public class PropertyListConfiguration extends BaseHierarchicalConfiguration
    implements FileBasedConfiguration
{
    /** Constant for the separator parser for the date part. */
    private static final DateComponentParser DATE_SEPARATOR_PARSER = new DateSeparatorParser(
            "-");

    /** Constant for the separator parser for the time part. */
    private static final DateComponentParser TIME_SEPARATOR_PARSER = new DateSeparatorParser(
            ":");

    /** Constant for the separator parser for blanks between the parts. */
    private static final DateComponentParser BLANK_SEPARATOR_PARSER = new DateSeparatorParser(
            " ");

    /** An array with the component parsers for dealing with dates. */
    private static final DateComponentParser[] DATE_PARSERS =
    {new DateSeparatorParser("<*D"), new DateFieldParser(Calendar.YEAR, 4),
            DATE_SEPARATOR_PARSER, new DateFieldParser(Calendar.MONTH, 2, 1),
            DATE_SEPARATOR_PARSER, new DateFieldParser(Calendar.DATE, 2),
            BLANK_SEPARATOR_PARSER,
            new DateFieldParser(Calendar.HOUR_OF_DAY, 2),
            TIME_SEPARATOR_PARSER, new DateFieldParser(Calendar.MINUTE, 2),
            TIME_SEPARATOR_PARSER, new DateFieldParser(Calendar.SECOND, 2),
            BLANK_SEPARATOR_PARSER, new DateTimeZoneParser(),
            new DateSeparatorParser(">")};

    /** Constant for the ID prefix for GMT time zones. */
    private static final String TIME_ZONE_PREFIX = "GMT";

    /** Constant for the milliseconds of a minute.*/
    private static final int MILLIS_PER_MINUTE = 1000 * 60;

    /** Constant for the minutes per hour.*/
    private static final int MINUTES_PER_HOUR = 60;

    /** Size of the indentation for the generated file. */
    private static final int INDENT_SIZE = 4;

    /** Constant for the length of a time zone.*/
    private static final int TIME_ZONE_LENGTH = 5;

    /** Constant for the padding character in the date format.*/
    private static final char PAD_CHAR = '0';

    /**
     * Creates an empty PropertyListConfiguration object which can be
     * used to synthesize a new plist file by adding values and
     * then saving().
     */
    public PropertyListConfiguration()
    {
    }

    /**
     * Creates a new instance of {@code PropertyListConfiguration} and
     * copies the content of the specified configuration into this object.
     *
     * @param c the configuration to copy
     * @since 1.4
     */
    public PropertyListConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
    }

    /**
     * Creates a new instance of {@code PropertyListConfiguration} with the
     * given root node.
     *
     * @param root the root node
     */
    PropertyListConfiguration(final ImmutableNode root)
    {
        super(new InMemoryNodeModel(root));
    }

    @Override
    protected void setPropertyInternal(final String key, final Object value)
    {
        // special case for byte arrays, they must be stored as is in the configuration
        if (value instanceof byte[])
        {
            setDetailEvents(false);
            try
            {
                clearProperty(key);
                addPropertyDirect(key, value);
            }
            finally
            {
                setDetailEvents(true);
            }
        }
        else
        {
            super.setPropertyInternal(key, value);
        }
    }

    @Override
    protected void addPropertyInternal(final String key, final Object value)
    {
        if (value instanceof byte[])
        {
            addPropertyDirect(key, value);
        }
        else
        {
            super.addPropertyInternal(key, value);
        }
    }

    @Override
    public void read(final Reader in) throws ConfigurationException
    {
        final PropertyListParser parser = new PropertyListParser(in);
        try
        {
            final PropertyListConfiguration config = parser.parse();
            getModel().setRootNode(
                    config.getNodeModel().getNodeHandler().getRootNode());
        }
        catch (final ParseException e)
        {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void write(final Writer out) throws ConfigurationException
    {
        final PrintWriter writer = new PrintWriter(out);
        final NodeHandler<ImmutableNode> handler = getModel().getNodeHandler();
        printNode(writer, 0, handler.getRootNode(), handler);
        writer.flush();
    }

    /**
     * Append a node to the writer, indented according to a specific level.
     */
    private void printNode(final PrintWriter out, final int indentLevel,
            final ImmutableNode node, final NodeHandler<ImmutableNode> handler)
    {
        final String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (node.getNodeName() != null)
        {
            out.print(padding + quoteString(node.getNodeName()) + " = ");
        }

        final List<ImmutableNode> children = new ArrayList<>(node.getChildren());
        if (!children.isEmpty())
        {
            // skip a line, except for the root dictionary
            if (indentLevel > 0)
            {
                out.println();
            }

            out.println(padding + "{");

            // display the children
            final Iterator<ImmutableNode> it = children.iterator();
            while (it.hasNext())
            {
                final ImmutableNode child = it.next();

                printNode(out, indentLevel + 1, child, handler);

                // add a semi colon for elements that are not dictionaries
                final Object value = child.getValue();
                if (value != null && !(value instanceof Map) && !(value instanceof Configuration))
                {
                    out.println(";");
                }

                // skip a line after arrays and dictionaries
                if (it.hasNext() && (value == null || value instanceof List))
                {
                    out.println();
                }
            }

            out.print(padding + "}");

            // line feed if the dictionary is not in an array
            if (handler.getParent(node) != null)
            {
                out.println();
            }
        }
        else if (node.getValue() == null)
        {
            out.println();
            out.print(padding + "{ };");

            // line feed if the dictionary is not in an array
            if (handler.getParent(node) != null)
            {
                out.println();
            }
        }
        else
        {
            // display the leaf value
            final Object value = node.getValue();
            printValue(out, indentLevel, value);
        }
    }

    /**
     * Append a value to the writer, indented according to a specific level.
     */
    private void printValue(final PrintWriter out, final int indentLevel, final Object value)
    {
        final String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (value instanceof List)
        {
            out.print("( ");
            final Iterator<?> it = ((List<?>) value).iterator();
            while (it.hasNext())
            {
                printValue(out, indentLevel + 1, it.next());
                if (it.hasNext())
                {
                    out.print(", ");
                }
            }
            out.print(" )");
        }
        else if (value instanceof PropertyListConfiguration)
        {
            final NodeHandler<ImmutableNode> handler =
                    ((PropertyListConfiguration) value).getModel()
                            .getNodeHandler();
            printNode(out, indentLevel, handler.getRootNode(), handler);
        }
        else if (value instanceof ImmutableConfiguration)
        {
            // display a flat Configuration as a dictionary
            out.println();
            out.println(padding + "{");

            final ImmutableConfiguration config = (ImmutableConfiguration) value;
            final Iterator<String> it = config.getKeys();
            while (it.hasNext())
            {
                final String key = it.next();
                final ImmutableNode node =
                        new ImmutableNode.Builder().name(key)
                                .value(config.getProperty(key)).create();
                final InMemoryNodeModel tempModel = new InMemoryNodeModel(node);
                printNode(out, indentLevel + 1, node, tempModel.getNodeHandler());
                out.println(";");
            }
            out.println(padding + "}");
        }
        else if (value instanceof Map)
        {
            // display a Map as a dictionary
            final Map<String, Object> map = transformMap((Map<?, ?>) value);
            printValue(out, indentLevel, new MapConfiguration(map));
        }
        else if (value instanceof byte[])
        {
            out.print("<" + new String(Hex.encodeHex((byte[]) value)) + ">");
        }
        else if (value instanceof Date)
        {
            out.print(formatDate((Date) value));
        }
        else if (value != null)
        {
            out.print(quoteString(String.valueOf(value)));
        }
    }

    /**
     * Quote the specified string if necessary, that's if the string contains:
     * <ul>
     *   <li>a space character (' ', '\t', '\r', '\n')</li>
     *   <li>a quote '"'</li>
     *   <li>special characters in plist files ('(', ')', '{', '}', '=', ';', ',')</li>
     * </ul>
     * Quotes within the string are escaped.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>abcd -> abcd</li>
     *   <li>ab cd -> "ab cd"</li>
     *   <li>foo"bar -> "foo\"bar"</li>
     *   <li>foo;bar -> "foo;bar"</li>
     * </ul>
     */
    String quoteString(String s)
    {
        if (s == null)
        {
            return null;
        }

        if (s.indexOf(' ') != -1
                || s.indexOf('\t') != -1
                || s.indexOf('\r') != -1
                || s.indexOf('\n') != -1
                || s.indexOf('"') != -1
                || s.indexOf('(') != -1
                || s.indexOf(')') != -1
                || s.indexOf('{') != -1
                || s.indexOf('}') != -1
                || s.indexOf('=') != -1
                || s.indexOf(',') != -1
                || s.indexOf(';') != -1)
        {
            s = s.replaceAll("\"", "\\\\\\\"");
            s = "\"" + s + "\"";
        }

        return s;
    }

    /**
     * Parses a date in a format like
     * {@code <*D2002-03-22 11:30:00 +0100>}.
     *
     * @param s the string with the date to be parsed
     * @return the parsed date
     * @throws ParseException if an error occurred while parsing the string
     */
    static Date parseDate(final String s) throws ParseException
    {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        int index = 0;

        for (final DateComponentParser parser : DATE_PARSERS)
        {
            index += parser.parseComponent(s, index, cal);
        }

        return cal.getTime();
    }

    /**
     * Returns a string representation for the date specified by the given
     * calendar.
     *
     * @param cal the calendar with the initialized date
     * @return a string for this date
     */
    static String formatDate(final Calendar cal)
    {
        final StringBuilder buf = new StringBuilder();

        for (final DateComponentParser element : DATE_PARSERS)
        {
            element.formatComponent(buf, cal);
        }

        return buf.toString();
    }

    /**
     * Returns a string representation for the specified date.
     *
     * @param date the date
     * @return a string for this date
     */
    static String formatDate(final Date date)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return formatDate(cal);
    }

    /**
     * Transform a map of arbitrary types into a map with string keys and object
     * values. All keys of the source map which are not of type String are
     * dropped.
     *
     * @param src the map to be converted
     * @return the resulting map
     */
    private static Map<String, Object> transformMap(final Map<?, ?> src)
    {
        final Map<String, Object> dest = new HashMap<>();
        for (final Map.Entry<?, ?> e : src.entrySet())
        {
            if (e.getKey() instanceof String)
            {
                dest.put((String) e.getKey(), e.getValue());
            }
        }
        return dest;
    }

    /**
     * A helper class for parsing and formatting date literals. Usually we would
     * use {@code SimpleDateFormat} for this purpose, but in Java 1.3 the
     * functionality of this class is limited. So we have a hierarchy of parser
     * classes instead that deal with the different components of a date
     * literal.
     */
    private abstract static class DateComponentParser
    {
        /**
         * Parses a component from the given input string.
         *
         * @param s the string to be parsed
         * @param index the current parsing position
         * @param cal the calendar where to store the result
         * @return the length of the processed component
         * @throws ParseException if the component cannot be extracted
         */
        public abstract int parseComponent(String s, int index, Calendar cal)
                throws ParseException;

        /**
         * Formats a date component. This method is used for converting a date
         * in its internal representation into a string literal.
         *
         * @param buf the target buffer
         * @param cal the calendar with the current date
         */
        public abstract void formatComponent(StringBuilder buf, Calendar cal);

        /**
         * Checks whether the given string has at least {@code length}
         * characters starting from the given parsing position. If this is not
         * the case, an exception will be thrown.
         *
         * @param s the string to be tested
         * @param index the current index
         * @param length the minimum length after the index
         * @throws ParseException if the string is too short
         */
        protected void checkLength(final String s, final int index, final int length)
                throws ParseException
        {
            final int len = (s == null) ? 0 : s.length();
            if (index + length > len)
            {
                throw new ParseException("Input string too short: " + s
                        + ", index: " + index);
            }
        }

        /**
         * Adds a number to the given string buffer and adds leading '0'
         * characters until the given length is reached.
         *
         * @param buf the target buffer
         * @param num the number to add
         * @param length the required length
         */
        protected void padNum(final StringBuilder buf, final int num, final int length)
        {
            buf.append(StringUtils.leftPad(String.valueOf(num), length,
                    PAD_CHAR));
        }
    }

    /**
     * A specialized date component parser implementation that deals with
     * numeric calendar fields. The class is able to extract fields from a
     * string literal and to format a literal from a calendar.
     */
    private static class DateFieldParser extends DateComponentParser
    {
        /** Stores the calendar field to be processed. */
        private final int calendarField;

        /** Stores the length of this field. */
        private final int length;

        /** An optional offset to add to the calendar field. */
        private final int offset;

        /**
         * Creates a new instance of {@code DateFieldParser}.
         *
         * @param calFld the calendar field code
         * @param len the length of this field
         */
        public DateFieldParser(final int calFld, final int len)
        {
            this(calFld, len, 0);
        }

        /**
         * Creates a new instance of {@code DateFieldParser} and fully
         * initializes it.
         *
         * @param calFld the calendar field code
         * @param len the length of this field
         * @param ofs an offset to add to the calendar field
         */
        public DateFieldParser(final int calFld, final int len, final int ofs)
        {
            calendarField = calFld;
            length = len;
            offset = ofs;
        }

        @Override
        public void formatComponent(final StringBuilder buf, final Calendar cal)
        {
            padNum(buf, cal.get(calendarField) + offset, length);
        }

        @Override
        public int parseComponent(final String s, final int index, final Calendar cal)
                throws ParseException
        {
            checkLength(s, index, length);
            try
            {
                cal.set(calendarField, Integer.parseInt(s.substring(index,
                        index + length))
                        - offset);
                return length;
            }
            catch (final NumberFormatException nfex)
            {
                throw new ParseException("Invalid number: " + s + ", index "
                        + index);
            }
        }
    }

    /**
     * A specialized date component parser implementation that deals with
     * separator characters.
     */
    private static class DateSeparatorParser extends DateComponentParser
    {
        /** Stores the separator. */
        private final String separator;

        /**
         * Creates a new instance of {@code DateSeparatorParser} and sets
         * the separator string.
         *
         * @param sep the separator string
         */
        public DateSeparatorParser(final String sep)
        {
            separator = sep;
        }

        @Override
        public void formatComponent(final StringBuilder buf, final Calendar cal)
        {
            buf.append(separator);
        }

        @Override
        public int parseComponent(final String s, final int index, final Calendar cal)
                throws ParseException
        {
            checkLength(s, index, separator.length());
            if (!s.startsWith(separator, index))
            {
                throw new ParseException("Invalid input: " + s + ", index "
                        + index + ", expected " + separator);
            }
            return separator.length();
        }
    }

    /**
     * A specialized date component parser implementation that deals with the
     * time zone part of a date component.
     */
    private static class DateTimeZoneParser extends DateComponentParser
    {
        @Override
        public void formatComponent(final StringBuilder buf, final Calendar cal)
        {
            final TimeZone tz = cal.getTimeZone();
            int ofs = tz.getRawOffset() / MILLIS_PER_MINUTE;
            if (ofs < 0)
            {
                buf.append('-');
                ofs = -ofs;
            }
            else
            {
                buf.append('+');
            }
            final int hour = ofs / MINUTES_PER_HOUR;
            final int min = ofs % MINUTES_PER_HOUR;
            padNum(buf, hour, 2);
            padNum(buf, min, 2);
        }

        @Override
        public int parseComponent(final String s, final int index, final Calendar cal)
                throws ParseException
        {
            checkLength(s, index, TIME_ZONE_LENGTH);
            final TimeZone tz = TimeZone.getTimeZone(TIME_ZONE_PREFIX
                    + s.substring(index, index + TIME_ZONE_LENGTH));
            cal.setTimeZone(tz);
            return TIME_ZONE_LENGTH;
        }
    }
}
