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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A utility class to convert the configuration properties into any type.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/18 09:54:37 $
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
                return new Byte((String) value);
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
                return new Short((String) value);
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
                return new Integer((String) value);
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
                return new Long((String) value);
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
                return new BigInteger((String) value);
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
            String[] elements = split((String) value, "_");

            if (elements.length >= 1 && (elements[0].length() == 2 || elements[0].length() == 0))
            {
                String language = elements[0];
                String country = elements.length >= 2 ? elements[1] : "";
                String variant = elements.length >= 3 ? elements[2] : "";

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
     * Split a string on the specified separator. To be removed when
     * commons-lang has a better replacement available (Tokenizer?).
     *
     * todo: replace with a commons-lang equivalent
     *
     * @param s          the string to split
     * @param separator  the separator
     */
    private static String[] split(String s, String separator)
    {
        if (s == null)
        {
            return new String[0];
        }

        List list = new ArrayList();

        int begin = 0;
        while (begin < s.length())
        {
            int index = s.indexOf(separator, begin);
            int end = index != -1 ? index : s.length();
            list.add(s.substring(begin , end));

            begin = end + 1;
        }

        return (String[]) list.toArray(new String[list.size()]);
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
}
