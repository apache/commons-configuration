/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 * @version $Revision$, $Date$
 * @since 1.1
 */
public final class PropertyConverter
{
    /** Constant for the list delimiter escaping character.*/
    static final String LIST_ESCAPE = "\\";

    /** Constant for the prefix of hex numbers.*/
    private static final String HEX_PREFIX = "0x";

    /** Constant for the radix of hex numbers.*/
    private static final int HEX_RADIX = 16;

    /** Constant for the argument classes of the Number constructor that takes
     * a String.
     */
    private static final Class[] CONSTR_ARGS = {String.class};

    /**
     * Private constructor prevents instances from being created.
     */
    private PropertyConverter()
    {
        // to prevent instanciation...
    }

    /**
     * Convert the specified object into a Boolean.
     *
     * @param value the value to convert
     * @return the converted value
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
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a byte
     */
    public static Byte toByte(Object value) throws ConversionException
    {
        Number n = toNumber(value, Byte.class);
        if (n instanceof Byte)
        {
            return (Byte) n;
        }
        else
        {
            return new Byte(n.byteValue());
        }
    }

    /**
     * Convert the specified object into a Short.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a short
     */
    public static Short toShort(Object value) throws ConversionException
    {
        Number n = toNumber(value, Short.class);
        if (n instanceof Short)
        {
            return (Short) n;
        }
        else
        {
            return new Short(n.shortValue());
        }
    }

    /**
     * Convert the specified object into an Integer.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to an integer
     */
    public static Integer toInteger(Object value) throws ConversionException
    {
        Number n = toNumber(value, Integer.class);
        if (n instanceof Integer)
        {
            return (Integer) n;
        }
        else
        {
            return new Integer(n.intValue());
        }
    }

    /**
     * Convert the specified object into a Long.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a Long
     */
    public static Long toLong(Object value) throws ConversionException
    {
        Number n = toNumber(value, Long.class);
        if (n instanceof Long)
        {
            return (Long) n;
        }
        else
        {
            return new Long(n.longValue());
        }
    }

    /**
     * Convert the specified object into a Float.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a Float
     */
    public static Float toFloat(Object value) throws ConversionException
    {
        Number n = toNumber(value, Float.class);
        if (n instanceof Float)
        {
            return (Float) n;
        }
        else
        {
            return new Float(n.floatValue());
        }
    }

    /**
     * Convert the specified object into a Double.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a Double
     */
    public static Double toDouble(Object value) throws ConversionException
    {
        Number n = toNumber(value, Double.class);
        if (n instanceof Double)
        {
            return (Double) n;
        }
        else
        {
            return new Double(n.doubleValue());
        }
    }

    /**
     * Convert the specified object into a BigInteger.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a BigInteger
     */
    public static BigInteger toBigInteger(Object value) throws ConversionException
    {
        Number n = toNumber(value, BigInteger.class);
        if (n instanceof BigInteger)
        {
            return (BigInteger) n;
        }
        else
        {
            return BigInteger.valueOf(n.longValue());
        }
    }

    /**
     * Convert the specified object into a BigDecimal.
     *
     * @param value the value to convert
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value) throws ConversionException
    {
        Number n = toNumber(value, BigDecimal.class);
        if (n instanceof BigDecimal)
        {
            return (BigDecimal) n;
        }
        else
        {
            return new BigDecimal(n.doubleValue());
        }
    }

    /**
     * Tries to convert the specified object into a number object. This method
     * is used by the conversion methods for number types. Note that the return
     * value is not in always of the specified target class, but only if a new
     * object has to be created.
     *
     * @param value the value to be converted (must not be <b>null</b>)
     * @param targetClass the target class of the conversion (must be derived
     * from <code>java.lang.Number</code>)
     * @return the converted number
     * @throws ConversionException if the object cannot be converted
     */
    static Number toNumber(Object value, Class targetClass)
            throws ConversionException
    {
        if (value instanceof Number)
        {
            return (Number) value;
        }
        else
        {
            String str = value.toString();
            if (str.startsWith(HEX_PREFIX))
            {
                try
                {
                    return new BigInteger(str.substring(HEX_PREFIX.length()),
                            HEX_RADIX);
                }
                catch (NumberFormatException nex)
                {
                    throw new ConversionException("Could not convert " + str
                            + " to " + targetClass.getName()
                            + "! Invalid hex number.", nex);
                }
            }

            try
            {
                Constructor constr = targetClass.getConstructor(CONSTR_ARGS);
                return (Number) constr.newInstance(new Object[]{str});
            }
            catch (InvocationTargetException itex)
            {
                throw new ConversionException("Could not convert " + str
                        + " to " + targetClass.getName(), itex
                        .getTargetException());
            }
            catch (Exception ex)
            {
                // Treat all possible exceptions the same way
                throw new ConversionException(
                        "Conversion error when trying to convert " + str
                                + " to " + targetClass.getName(), ex);
            }
        }
    }

    /**
     * Convert the specified object into an URL.
     *
     * @param value the value to convert
     * @return the converted value
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
     * @return the converted value
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
     * @return a list with the single tokens
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

            if (chunk.endsWith(LIST_ESCAPE) && end != s.length())
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
     * Escapes the delimiters that might be contained in the given string. This
     * method ensures that list delimiter characters that are part of a
     * property's value are correctly escaped when a configuration is saved to a
     * file. Otherwise when loaded again the property will be treated as a list
     * property.
     *
     * @param s the string with the value
     * @param delimiter the list delimiter to use
     * @return the correctly esaped string
     */
    public static String escapeDelimiters(String s, char delimiter)
    {
        return StringUtils.replace(s, String.valueOf(delimiter), LIST_ESCAPE
                + delimiter);
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
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to a Color
     */
    public static Color toColor(Object value) throws ConversionException
    {
        if (value instanceof Color)
        {
            return (Color) value;
        }
        else if (value instanceof String && !StringUtils.isBlank((String) value))
        {
            String color = ((String) value).trim();

            int[] components = new int[3];

            // check the size of the string
            int minlength = components.length * 2;
            if (color.length() < minlength)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Color");
            }

            // remove the leading #
            if (color.startsWith("#"))
            {
                color = color.substring(1);
            }

            try
            {
                // parse the components
                for (int i = 0; i < components.length; i++)
                {
                    components[i] = Integer.parseInt(color.substring(2 * i, 2 * i + 2), HEX_RADIX);
                }

                // parse the transparency
                int alpha;
                if (color.length() >= minlength + 2)
                {
                    alpha = Integer.parseInt(color.substring(minlength, minlength + 2), HEX_RADIX);
                }
                else
                {
                    alpha = Color.black.getAlpha();
                }

                return new Color(components[0], components[1], components[2], alpha);
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
     * Convert the specified object into a Date.
     *
     * @param value  the value to convert
     * @param format the DateFormat pattern to parse String values
     * @return the converted value
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
     * @return the converted value
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
     * @return an iterator for accessing the single values
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

    /**
     * Performs interpolation of the specified value. This method checks if the
     * given value contains variables of the form <code>${...}</code>. If
     * this is the case, all occurrances will be substituted by their current
     * values.
     *
     * @param value the value to be interpolated
     * @param config the current configuration object
     * @return the interpolated value
     */
    public static Object interpolate(Object value, AbstractConfiguration config)
    {
        if (value instanceof String)
        {
            return interpolateHelper((String) value, null, config);
        }
        else
        {
            return value;
        }
    }

    /**
     * Recursive handler for multple levels of interpolation. This will be
     * replaced when Commons Lang provides an interpolation feature. When called
     * the first time, priorVariables should be null.
     *
     * @param base string with the ${key} variables
     * @param priorVariables serves two purposes: to allow checking for loops,
     * and creating a meaningful exception message should a loop occur. It's
     * 0'th element will be set to the value of base from the first call. All
     * subsequent interpolated variables are added afterward.
     * @param config the current configuration
     * @return the string with the interpolation taken care of
     */
    private static String interpolateHelper(String base, List priorVariables,
            AbstractConfiguration config)
    {
        if (base == null)
        {
            return null;
        }

        // on the first call initialize priorVariables
        // and add base as the first element
        if (priorVariables == null)
        {
            priorVariables = new ArrayList();
            priorVariables.add(base);
        }

        int begin = -1;
        int end = -1;
        int prec = 0 - AbstractConfiguration.END_TOKEN.length();
        StringBuffer result = new StringBuffer();

        // FIXME: we should probably allow the escaping of the start token
        while (((begin = base.indexOf(AbstractConfiguration.START_TOKEN, prec
                + AbstractConfiguration.END_TOKEN.length())) > -1)
                && ((end = base.indexOf(AbstractConfiguration.END_TOKEN, begin)) > -1))
        {
            result.append(base.substring(prec
                    + AbstractConfiguration.END_TOKEN.length(), begin));
            String variable = base.substring(begin
                    + AbstractConfiguration.START_TOKEN.length(), end);

            // if we've got a loop, create a useful exception message and throw
            if (priorVariables.contains(variable))
            {
                String initialBase = priorVariables.remove(0).toString();
                priorVariables.add(variable);
                StringBuffer priorVariableSb = new StringBuffer();

                // create a nice trace of interpolated variables like so:
                // var1->var2->var3
                for (Iterator it = priorVariables.iterator(); it.hasNext();)
                {
                    priorVariableSb.append(it.next());
                    if (it.hasNext())
                    {
                        priorVariableSb.append("->");
                    }
                }

                throw new IllegalStateException(
                        "infinite loop in property interpolation of "
                                + initialBase + ": "
                                + priorVariableSb.toString());
            }
            // otherwise, add this variable to the interpolation list.
            else
            {
                priorVariables.add(variable);
            }

            Object value = config.resolveContainerStore(variable);
            if (value != null)
            {
                result.append(interpolateHelper(value.toString(),
                        priorVariables, config));

                // pop the interpolated variable off the stack
                // this maintains priorVariables correctness for
                // properties with multiple interpolations, e.g.
                // prop.name=${some.other.prop1}/blahblah/${some.other.prop2}
                priorVariables.remove(priorVariables.size() - 1);
            }
            else
            {
                // variable not defined - so put it back in the value
                result.append(AbstractConfiguration.START_TOKEN);
                result.append(variable);
                result.append(AbstractConfiguration.END_TOKEN);
            }

            prec = end;
        }
        result.append(base.substring(prec
                + AbstractConfiguration.END_TOKEN.length(), base.length()));
        return result.toString();
    }
}
