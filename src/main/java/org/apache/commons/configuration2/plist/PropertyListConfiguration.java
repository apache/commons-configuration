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

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.lang.StringUtils;

/**
 * NeXT / OpenStep style configuration. This configuration can read and write
 * ASCII plist files. It support the GNUStep extension to specify date objects.
 * <p>
 * References:
 * <ul>
 *   <li><a
 * href="http://developer.apple.com/documentation/Cocoa/Conceptual/PropertyLists/Articles/OldStylePListsConcept.html">
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
 *     data = &lt;4f3e0145ab>;
 *
 *     date = &lt;*D2007-05-05 20:05:00 +0100>;
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
 * @version $Revision$, $Date$
 */
public class PropertyListConfiguration extends AbstractHierarchicalFileConfiguration
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

    /** The serial version UID. */
    private static final long serialVersionUID = 3227248503779092127L;

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
     * Creates a new instance of <code>PropertyListConfiguration</code> and
     * copies the content of the specified configuration into this object.
     *
     * @param c the configuration to copy
     * @since 1.4
     */
    public PropertyListConfiguration(HierarchicalConfiguration c)
    {
        super(c);
    }

    /**
     * Creates and loads the property list from the specified file.
     *
     * @param fileName The name of the plist file to load.
     * @throws ConfigurationException Error while loading the plist file
     */
    public PropertyListConfiguration(String fileName) throws ConfigurationException
    {
        super(fileName);
    }

    /**
     * Creates and loads the property list from the specified file.
     *
     * @param file The plist file to load.
     * @throws ConfigurationException Error while loading the plist file
     */
    public PropertyListConfiguration(File file) throws ConfigurationException
    {
        super(file);
    }

    /**
     * Creates and loads the property list from the specified URL.
     *
     * @param url The location of the plist file to load.
     * @throws ConfigurationException Error while loading the plist file
     */
    public PropertyListConfiguration(URL url) throws ConfigurationException
    {
        super(url);
    }

    public void setProperty(String key, Object value)
    {
        // special case for byte arrays, they must be stored as is in the configuration
        if (value instanceof byte[])
        {
            fireEvent(EVENT_SET_PROPERTY, key, value, true);
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
            fireEvent(EVENT_SET_PROPERTY, key, value, false);
        }
        else
        {
            super.setProperty(key, value);
        }
    }

    public void addProperty(String key, Object value)
    {
        if (value instanceof byte[])
        {
            fireEvent(EVENT_ADD_PROPERTY, key, value, true);
            addPropertyDirect(key, value);
            fireEvent(EVENT_ADD_PROPERTY, key, value, false);
        }
        else
        {
            super.addProperty(key, value);
        }
    }

    public void load(Reader in) throws ConfigurationException
    {
        PropertyListParser parser = new PropertyListParser(in);
        try
        {
            HierarchicalConfiguration config = parser.parse();
            setRoot(config.getRoot());
        }
        catch (ParseException e)
        {
            throw new ConfigurationException(e);
        }
    }

    public void save(Writer out) throws ConfigurationException
    {
        PrintWriter writer = new PrintWriter(out);
        printNode(writer, 0, getRoot());
        writer.flush();
    }

    /**
     * Append a node to the writer, indented according to a specific level.
     */
    private void printNode(PrintWriter out, int indentLevel, Node node)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (node.getName() != null)
        {
            out.print(padding + quoteString(node.getName()) + " = ");
        }

        // get all non trivial nodes
        List<ConfigurationNode> children = new ArrayList<ConfigurationNode>(node.getChildren());
        Iterator it = children.iterator();
        while (it.hasNext())
        {
            Node child = (Node) it.next();
            if (child.getValue() == null && (child.getChildren() == null || child.getChildren().isEmpty()))
            {
                it.remove();
            }
        }

        if (!children.isEmpty())
        {
            // skip a line, except for the root dictionary
            if (indentLevel > 0)
            {
                out.println();
            }

            out.println(padding + "{");

            // display the children
            it = children.iterator();
            while (it.hasNext())
            {
                Node child = (Node) it.next();

                printNode(out, indentLevel + 1, child);

                // add a semi colon for elements that are not dictionaries
                Object value = child.getValue();
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
            if (node.getParent() != null)
            {
                out.println();
            }
        }
        else
        {
            // display the leaf value
            Object value = node.getValue();
            printValue(out, indentLevel, value);
        }
    }

    /**
     * Append a value to the writer, indented according to a specific level.
     */
    private void printValue(PrintWriter out, int indentLevel, Object value)
    {
        String padding = StringUtils.repeat(" ", indentLevel * INDENT_SIZE);

        if (value instanceof List)
        {
            out.print("( ");
            Iterator it = ((List) value).iterator();
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
        else if (value instanceof HierarchicalConfiguration)
        {
            printNode(out, indentLevel, ((HierarchicalConfiguration) value).getRoot());
        }
        else if (value instanceof Configuration)
        {
            // display a flat Configuration as a dictionary
            out.println();
            out.println(padding + "{");

            Configuration config = (Configuration) value;
            Iterator it = config.getKeys();
            while (it.hasNext())
            {
                String key = (String) it.next();
                Node node = new Node(key);
                node.setValue(config.getProperty(key));

                printNode(out, indentLevel + 1, node);
                out.println(";");
            }
            out.println(padding + "}");
        }
        else if (value instanceof Map)
        {
            // display a Map as a dictionary
            Map map = (Map) value;
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
            s = StringUtils.replace(s, "\"", "\\\"");
            s = "\"" + s + "\"";
        }

        return s;
    }

    /**
     * Parses a date in a format like
     * <code>&lt;*D2002-03-22 11:30:00 +0100&gt;</code>.
     *
     * @param s the string with the date to be parsed
     * @return the parsed date
     * @throws ParseException if an error occurred while parsing the string
     */
    static Date parseDate(String s) throws ParseException
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        int index = 0;

        for (int i = 0; i < DATE_PARSERS.length; i++)
        {
            index += DATE_PARSERS[i].parseComponent(s, index, cal);
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
    static String formatDate(Calendar cal)
    {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < DATE_PARSERS.length; i++)
        {
            DATE_PARSERS[i].formatComponent(buf, cal);
        }

        return buf.toString();
    }

    /**
     * Returns a string representation for the specified date.
     *
     * @param date the date
     * @return a string for this date
     */
    static String formatDate(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return formatDate(cal);
    }

    /**
     * A helper class for parsing and formatting date literals. Usually we would
     * use <code>SimpleDateFormat</code> for this purpose, but in Java 1.3 the
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
         * Checks whether the given string has at least <code>length</code>
         * characters starting from the given parsing position. If this is not
         * the case, an exception will be thrown.
         *
         * @param s the string to be tested
         * @param index the current index
         * @param length the minimum length after the index
         * @throws ParseException if the string is too short
         */
        protected void checkLength(String s, int index, int length)
                throws ParseException
        {
            int len = (s == null) ? 0 : s.length();
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
        protected void padNum(StringBuilder buf, int num, int length)
        {
            buf.append(StringUtils.leftPad(String.valueOf(num), length, PAD_CHAR));
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
        private int calendarField;

        /** Stores the length of this field. */
        private int length;

        /** An optional offset to add to the calendar field. */
        private int offset;

        /**
         * Creates a new instance of <code>DateFieldParser</code>.
         *
         * @param calFld the calendar field code
         * @param len the length of this field
         */
        public DateFieldParser(int calFld, int len)
        {
            this(calFld, len, 0);
        }

        /**
         * Creates a new instance of <code>DateFieldParser</code> and fully
         * initializes it.
         *
         * @param calFld the calendar field code
         * @param len the length of this field
         * @param ofs an offset to add to the calendar field
         */
        public DateFieldParser(int calFld, int len, int ofs)
        {
            calendarField = calFld;
            length = len;
            offset = ofs;
        }

        public void formatComponent(StringBuilder buf, Calendar cal)
        {
            padNum(buf, cal.get(calendarField) + offset, length);
        }

        public int parseComponent(String s, int index, Calendar cal)
                throws ParseException
        {
            checkLength(s, index, length);
            try
            {
                cal.set(calendarField, Integer.parseInt(s.substring(index, index + length)) - offset);
                return length;
            }
            catch (NumberFormatException nfex)
            {
                throw new ParseException("Invalid number: " + s + ", index " + index);
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
        private String separator;

        /**
         * Creates a new instance of <code>DateSeparatorParser</code> and sets
         * the separator string.
         *
         * @param sep the separator string
         */
        public DateSeparatorParser(String sep)
        {
            separator = sep;
        }

        public void formatComponent(StringBuilder buf, Calendar cal)
        {
            buf.append(separator);
        }

        public int parseComponent(String s, int index, Calendar cal)
                throws ParseException
        {
            checkLength(s, index, separator.length());
            if (!s.startsWith(separator, index))
            {
                throw new ParseException("Invalid input: " + s + ", index " + index + ", expected " + separator);
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
        public void formatComponent(StringBuilder buf, Calendar cal)
        {
            TimeZone tz = cal.getTimeZone();
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
            int hour = ofs / MINUTES_PER_HOUR;
            int min = ofs % MINUTES_PER_HOUR;
            padNum(buf, hour, 2);
            padNum(buf, min, 2);
        }

        public int parseComponent(String s, int index, Calendar cal)
                throws ParseException
        {
            checkLength(s, index, TIME_ZONE_LENGTH);
            TimeZone tz = TimeZone.getTimeZone(TIME_ZONE_PREFIX
                    + s.substring(index, index + TIME_ZONE_LENGTH));
            cal.setTimeZone(tz);
            return TIME_ZONE_LENGTH;
        }
    }
}
