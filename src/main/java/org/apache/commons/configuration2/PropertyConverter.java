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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
    /** Constant for the list delimiter as char.*/
    static final char LIST_ESC_CHAR = '\\';

    /** Constant for the list delimiter escaping character as string.*/
    static final String LIST_ESCAPE = String.valueOf(LIST_ESC_CHAR);

    /**
     * Private constructor prevents instances from being created.
     */
    private PropertyConverter()
    {
        // to prevent instantiation...
    }

    /**
     * Split a string on the specified delimiter. To be removed when
     * commons-lang has a better replacement available (Tokenizer?).
     *
     * todo: replace with a commons-lang equivalent
     *
     * @param s          the string to split
     * @param delimiter  the delimiter
     * @param trim       a flag whether the single elements should be trimmed
     * @return a list with the single tokens
     */
    public static List<String> split(String s, char delimiter, boolean trim)
    {
        if (s == null)
        {
            return new ArrayList<String>();
        }

        List<String> list = new ArrayList<String>();

        StringBuilder token = new StringBuilder();
        int begin = 0;
        boolean inEscape = false;

        while (begin < s.length())
        {
            char c = s.charAt(begin);
            if (inEscape)
            {
                // last character was the escape marker
                // can current character be escaped?
                if (c != delimiter && c != LIST_ESC_CHAR)
                {
                    // no, also add escape character
                    token.append(LIST_ESC_CHAR);
                }
                token.append(c);
                inEscape = false;
            }

            else
            {
                if (c == delimiter)
                {
                    // found a list delimiter -> add token and reset buffer
                    String t = token.toString();
                    if (trim)
                    {
                        t = t.trim();
                    }
                    list.add(t);
                    token = new StringBuilder();
                }
                else if (c == LIST_ESC_CHAR)
                {
                    // eventually escape next character
                    inEscape = true;
                }
                else
                {
                    token.append(c);
                }
            }

            begin++;
        }

        // Trailing delimiter?
        if (inEscape)
        {
            token.append(LIST_ESC_CHAR);
        }
        // Add last token
        String t = token.toString();
        if (trim)
        {
            t = t.trim();
        }
        list.add(t);

        return list;
    }

    /**
     * Split a string on the specified delimiter always trimming the elements.
     * This is a shortcut for <code>split(s, delimiter, true)</code>.
     *
     * @param s          the string to split
     * @param delimiter  the delimiter
     * @return a list with the single tokens
     */
    public static List<String> split(String s, char delimiter)
    {
        return split(s, delimiter, true);
    }

    /**
     * Escapes the delimiters that might be contained in the given string. This
     * method ensures that list delimiter characters that are part of a
     * property's value are correctly escaped when a configuration is saved to a
     * file. Otherwise when loaded again the property will be treated as a list
     * property. A single backslash will also be escaped.
     *
     * @param s the string with the value
     * @param delimiter the list delimiter to use
     * @return the correctly esaped string
     */
    public static String escapeDelimiters(String s, char delimiter)
    {
        String s1 = StringUtils.replace(s, LIST_ESCAPE, LIST_ESCAPE + LIST_ESCAPE);
        return StringUtils.replace(s1, String.valueOf(delimiter), LIST_ESCAPE + delimiter);
    }

    /**
     * Returns an iterator over the simple values of a composite value. This
     * implementation calls <code>{@link #flatten(Object, char)}</code> and
     * returns an iterator over the returned collection.
     *
     * @param value the value to "split"
     * @param delimiter the delimiter for String values
     * @return an iterator for accessing the single values
     */
    public static Iterator<?> toIterator(Object value, char delimiter)
    {
        return flatten(value, delimiter).iterator();
    }

    /**
     * Returns a collection with all values contained in the specified object.
     * This method is used for instance by the <code>addProperty()</code>
     * implementation of the default configurations to gather all values of the
     * property to add. Depending on the type of the passed in object the
     * following things happen:
     * <ul>
     * <li>Strings are checked for delimiter characters and split if necessary.</li>
     * <li>For objects implementing the <code>Iterable</code> interface, the
     * corresponding <code>Iterator</code> is obtained, and contained elements
     * are added to the resulting collection.</li>
     * <li>Arrays are treated as <code>Iterable</code> objects.</li>
     * <li>All other types are directly inserted.</li>
     * <li>Recursive combinations are supported, e.g. a collection containing
     * an array that contains strings: The resulting collection will only
     * contain primitive objects (hence the name &quot;flatten&quot;).</li>
     * </ul>
     *
     * @param value the value to be processed
     * @param delimiter the delimiter for String values
     * @return a &quot;flat&quot; collection containing all primitive values of
     *         the passed in object
     */
    public static Collection<?> flatten(Object value, char delimiter)
    {
        if (value instanceof String)
        {
            String s = (String) value;
            if (s.indexOf(delimiter) > 0)
            {
                return split((String) s, delimiter);
            }
        }

        Collection<Object> result = new LinkedList<Object>();
        if (value instanceof Iterable)
        {
            flattenIterator(result, ((Iterable<?>) value).iterator(), delimiter);
        }
        else if (value instanceof Iterator)
        {
            flattenIterator(result, (Iterator<?>) value, delimiter);
        }
        else if (value != null)
        {
            if (value.getClass().isArray())
            {
                for (int len = Array.getLength(value), idx = 0; idx < len; idx++)
                {
                    result.addAll(flatten(Array.get(value, idx), delimiter));
                }
            }
            else
            {
                result.add(value);
            }
        }

        return result;
    }

    /**
     * Flattens the given iterator. For each element in the iteration
     * <code>flatten()</code> will be called recursively.
     *
     * @param target the target collection
     * @param it the iterator to process
     * @param delimiter the delimiter for String values
     */
    private static void flattenIterator(Collection<Object> target,
            Iterator<?> it, char delimiter)
    {
        while (it.hasNext())
        {
            target.addAll(flatten(it.next(), delimiter));
        }
    }

    /**
     * Performs interpolation of the specified value. This method checks if the
     * given value contains variables of the form <code>${...}</code>. If
     * this is the case, all occurrences will be substituted by their current
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
            return config.getSubstitutor().replace((String) value);
        }
        else
        {
            return value;
        }
    }
}
