/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A utility class to convert the configuration properties into any type.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.3 $, $Date$
 * @since 1.1
 */
public final class PropertyConverter
{
    /**
     * Convert the specified object into a Boolean.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a boolean
     */
    public static Boolean toBoolean(Object value) throws ConversionException
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            Boolean b = BooleanUtils.toBooleanObject((String) value);
            if (b == null)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Boolean object");
            }
            return b;
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Boolean object");
        }
    }

    /**
     * Convert the specified object into a Byte.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a byte
     */
    public static Byte toByte(Object value) throws ConversionException
    {
        if (value instanceof Byte)
        {
            return (Byte) value;
        }
        else if (value instanceof String)
        {
            try
            {
                String string = (String) value;
                if (string.startsWith("0x"))
                {
                    return new Byte((byte) Integer.parseInt(string.substring(2), 16));
                }
                else
                {
                    return new Byte(string);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Byte object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Byte object");
        }
    }

    /**
     * Convert the specified object into a Short.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a short
     */
    public static Short toShort(Object value) throws ConversionException
    {
        if (value instanceof Short)
        {
            return (Short) value;
        }
        else if (value instanceof String)
        {
            try
            {
                String string = (String) value;
                if (string.startsWith("0x"))
                {
                    return new Short((short) Integer.parseInt(string.substring(2), 16));
                }
                else
                {
                    return new Short(string);
                }

            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Short object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Short object");
        }
    }

    /**
     * Convert the specified object into an Integer.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to an integer
     */
    public static Integer toInteger(Object value) throws ConversionException
    {
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof String)
        {
            try
            {
                String string = (String) value;
                if (string.startsWith("0x"))
                {
                    return new Integer((int) Long.parseLong(string.substring(2), 16));
                }
                else
                {
                    return new Integer(string);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to an Integer object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to an Integer object");
        }
    }

    /**
     * Convert the specified object into a Long.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a Long
     */
    public static Long toLong(Object value) throws ConversionException
    {
        if (value instanceof Long)
        {
            return (Long) value;
        }
        else if (value instanceof String)
        {
            try
            {
                String string = (String) value;
                if (string.startsWith("0x"))
                {
                    return new Long(new BigInteger(string.substring(2), 16).longValue());
                }
                else
                {
                    return new Long(string);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Long object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Long object");
        }
    }

    /**
     * Convert the specified object into a Float.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a Float
     */
    public static Float toFloat(Object value) throws ConversionException
    {
        if (value instanceof Float)
        {
            return (Float) value;
        }
        else if (value instanceof String)
        {
            try
            {
                return new Float((String) value);
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Float object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Float object");
        }
    }

    /**
     * Convert the specified object into a Double.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a Double
     */
    public static Double toDouble(Object value) throws ConversionException
    {
        if (value instanceof Double)
        {
            return (Double) value;
        }
        else if (value instanceof String)
        {
            try
            {
                return new Double((String) value);
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Double object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Double object");
        }
    }

    /**
     * Convert the specified object into a BigInteger.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a BigInteger
     */
    public static BigInteger toBigInteger(Object value) throws ConversionException
    {
        if (value instanceof BigInteger)
        {
            return (BigInteger) value;
        }
        else if (value instanceof String)
        {
            try
            {
                String string = (String) value;
                if (string.startsWith("0x"))
                {
                    return new BigInteger(string.substring(2), 16);
                }
                else
                {
                    return new BigInteger(string);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a BigInteger object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a BigInteger object");
        }
    }

    /**
     * Convert the specified object into a BigDecimal.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value) throws ConversionException
    {
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        else if (value instanceof String)
        {
            try
            {
                return new BigDecimal((String) value);
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a BigDecimal object", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a BigDecimal object");
        }
    }

    /**
     * Convert the specified object into an URL.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to an URL
     */
    public static URL toURL(Object value) throws ConversionException
    {
        if (value instanceof URL)
        {
            return (URL) value;
        }
        else if (value instanceof String)
        {
            try
            {
                return new URL((String) value);
            }
            catch (MalformedURLException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to an URL", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to an URL");
        }
    }

    /**
     * Convert the specified object into a Locale.
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a Locale
     */
    public static Locale toLocale(Object value) throws ConversionException
    {
        if (value instanceof Locale)
        {
            return (Locale) value;
        }
        else if (value instanceof String)
        {
            List elements = split((String) value, '_');
            int size = elements.size();

            if (size >= 1 && (((String) elements.get(0)).length() == 2 || ((String) elements.get(0)).length() == 0))
            {
                String language = (String) elements.get(0);
                String country = (String) ((size >= 2) ? elements.get(1) : "");
                String variant = (String) ((size >= 3) ? elements.get(2) : "");

                return new Locale(language, country, variant);
            }
            else
            {
                throw new ConversionException("The value " + value + " can't be converted to a Locale");
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Locale");
        }
    }

    /**
     * Split a string on the specified delimiter. To be removed when
     * commons-lang has a better replacement available (Tokenizer?).
     *
     * todo: replace with a commons-lang equivalent
     *
     * @param s          the string to split
     * @param delimiter  the delimiter
     */
    public static List split(String s, char delimiter)
    {
        if (s == null)
        {
            return new ArrayList();
        }

        List list = new ArrayList();

        StringBuffer token = new StringBuffer();
        int begin = 0;
        int end = 0;
        while (begin <= s.length())
        {
            // find the next delimiter
            int index = s.indexOf(delimiter, end);

            // move the end index at the end of the string if the delimiter is not found
            end = (index != -1) ? index : s.length();

            // extract the chunk
            String chunk = s.substring(begin , end);

            if (chunk.endsWith("\\") && end != s.length())
            {
                token.append(chunk.substring(0, chunk.length() - 1));
                token.append(delimiter);
            }
            else
            {
                // append the chunk to the token
                token.append(chunk);

                // add the token to the list
                list.add(token.toString().trim());

                // reset the token
                token = new StringBuffer();
            }

            // move to the next chunk
            end = end + 1;
            begin = end;
        }

        return list;
    }

    /**
     * Convert the specified object into a Color. If the value is a String,
     * the format allowed is (#)?[0-9A-F]{6}([0-9A-F]{2})?. Examples:
     * <ul>
     *   <li>FF0000 (red)</li>
     *   <li>0000FFA0 (semi transparent blue)</li>
     *   <li>#CCCCCC (gray)</li>
     *   <li>#00FF00A0 (semi transparent green)</li>
     * </ul>
     *
     * @param value the value to convert
     * @throws ConversionException thrown if the value cannot be converted to a Color
     */
    public static Color toColor(Object value) throws ConversionException
    {
        if (value instanceof Color)
        {
            return (Color) value;
        }
        else if (value instanceof String && !StringUtils.isBlank((String) value) && ((String) value).length() >= 6)
        {
            try
            {
                String color = ((String) value).trim();

                // remove the leading #
                if (color.startsWith("#"))
                {
                    color = color.substring(1);
                }

                int red = Integer.parseInt(color.substring(0, 2), 16);
                int green = Integer.parseInt(color.substring(2, 4), 16);
                int blue = Integer.parseInt(color.substring(4, 6), 16);
                int alpha = 255;

                // parse the transparency
                if (color.length() >= 8)
                {
                    alpha = Integer.parseInt(color.substring(6, 8), 16);
                }

                return new Color(red, green, blue, alpha);
            }
            catch (Exception e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Color", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Color");
        }
    }

    /**
     * Convert the specified object into a Calendar.
     *
     * @param value  the value to convert
     * @param format the DateFormat pattern to parse String values
     * @throws ConversionException thrown if the value cannot be converted to a Calendar
     */
    public static Date toDate(Object value, String format) throws ConversionException
    {
        if (value instanceof Date)
        {
            return (Date) value;
        }
        else if (value instanceof Calendar)
        {
            return ((Calendar) value).getTime();
        }
        else if (value instanceof String)
        {
            try
            {
                return new SimpleDateFormat(format).parse((String) value);
            }
            catch (ParseException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Date", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Date");
        }
    }

    /**
     * Convert the specified object into a Calendar.
     *
     * @param value  the value to convert
     * @param format the DateFormat pattern to parse String values
     * @throws ConversionException thrown if the value cannot be converted to a Calendar
     */
    public static Calendar toCalendar(Object value, String format) throws ConversionException
    {
        if (value instanceof Calendar)
        {
            return (Calendar) value;
        }
        else if (value instanceof Date)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((Date) value);
            return calendar;
        }
        else if (value instanceof String)
        {
            try
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat(format).parse((String) value));
                return calendar;
            }
            catch (ParseException e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Calendar", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Calendar");
        }
    }

    /**
     * Return an iterator over the simple values of a composite value. The value
     * specified is handled depending on its type:
     * <ul>
     *   <li>Strings are checked for delimiter characters and splitted if necessary.</li>
     *   <li>For collections the single elements are checked.</li>
     *   <li>Arrays are treated like collections.</li>
     *   <li>All other types are directly inserted.</li>
     *   <li>Recursive combinations are supported, e.g. a collection containing array that contain strings.</li>
     * </ul>
     *
     * @param value     the value to "split"
     * @param delimiter the delimiter for String values
     */
    public static Iterator toIterator(Object value, char delimiter)
    {
        if (value == null)
        {
            return IteratorUtils.emptyIterator();
        }
        if (value instanceof String)
        {
            String s = (String) value;
            if (s.indexOf(delimiter) > 0)
            {
                return split((String) value, delimiter).iterator();
            }
            else
            {
                return new SingletonIterator(value);
            }
        }
        else if (value instanceof Collection)
        {
            return toIterator(((Collection) value).iterator(), delimiter);
        }
        else if (value.getClass().isArray())
        {
            return toIterator(IteratorUtils.arrayIterator(value), delimiter);
        }
        else if (value instanceof Iterator)
        {
            Iterator iterator = (Iterator) value;
            IteratorChain chain = new IteratorChain();
            while (iterator.hasNext())
            {
                chain.addIterator(toIterator(iterator.next(), delimiter));
            }
            return chain;
        }
        else
        {
            return new SingletonIterator(value);
        }
    }

}
