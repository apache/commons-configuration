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
package org.apache.commons.configuration2.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized implementation of {@code ListDelimiterHandler} which simulates
 * the list delimiter handling as it was used by {@code PropertiesConfiguration}
 * in Commons Configuration 1.x.
 * </p>
 * <p>
 * This class mainly exists for compatibility reasons. It is intended to be used
 * by applications which have to deal with properties files created by an older
 * version of this library.
 * </p>
 * <p>
 * In the 1.x series of Commons Configuration list handling was not fully
 * consistent. The escaping of property values was done in a different way if
 * they contained a list delimiter or not. From version 2.0 on, escaping is more
 * stringent which might cause slightly different results when parsing
 * properties files created by or for Configuration 1.x. If you encounter such
 * problems, you can switch to this {@code ListDelimiterHandler} implementation
 * rather than the default one. In other cases, this class should not be used!
 * </p>
 * <p>
 * Implementation note: An instance of this class can safely be shared between
 * multiple {@code Configuration} instances.
 * </p>
 *
 * @since 2.0
 */
public class LegacyListDelimiterHandler extends AbstractListDelimiterHandler
{
    /** Constant for the escaping character. */
    private static final String ESCAPE = "\\";

    /** Constant for the escaped escaping character. */
    private static final String DOUBLE_ESC = ESCAPE + ESCAPE;

    /** Constant for a duplicated sequence of escaping characters. */
    private static final String QUAD_ESC = DOUBLE_ESC + DOUBLE_ESC;

    /** The list delimiter character. */
    private final char delimiter;

    /**
     * Creates a new instance of {@code LegacyListDelimiterHandler} and sets the
     * list delimiter character.
     *
     * @param listDelimiter the list delimiter character
     */
    public LegacyListDelimiterHandler(final char listDelimiter)
    {
        delimiter = listDelimiter;
    }

    /**
     * Returns the list delimiter character.
     *
     * @return the list delimiter character
     */
    public char getDelimiter()
    {
        return delimiter;
    }

    /**
     * {@inheritDoc} This implementation performs delimiter escaping for a
     * single value (which is not part of a list).
     */
    @Override
    public Object escape(final Object value, final ValueTransformer transformer)
    {
        return escapeValue(value, false, transformer);
    }

    /**
     * {@inheritDoc} This implementation performs a special encoding of
     * backslashes at the end of a string so that they are not interpreted as
     * escape character for a following list delimiter.
     */
    @Override
    public Object escapeList(final List<?> values, final ValueTransformer transformer)
    {
        if (!values.isEmpty())
        {
            final Iterator<?> it = values.iterator();
            String lastValue = escapeValue(it.next(), true, transformer);
            final StringBuilder buf = new StringBuilder(lastValue);
            while (it.hasNext())
            {
                // if the last value ended with an escape character, it has
                // to be escaped itself; otherwise the list delimiter will
                // be escaped
                if (lastValue.endsWith(ESCAPE)
                        && (countTrailingBS(lastValue) / 2) % 2 != 0)
                {
                    buf.append(ESCAPE).append(ESCAPE);
                }
                buf.append(getDelimiter());
                lastValue = escapeValue(it.next(), true, transformer);
                buf.append(lastValue);
            }
            return buf.toString();
        }
        return null;
    }

    /**
     * {@inheritDoc} This implementation simulates the old splitting algorithm.
     * The string is split at the delimiter character if it is not escaped. If
     * the delimiter character is not found, the input is returned unchanged.
     */
    @Override
    protected Collection<String> splitString(final String s, final boolean trim)
    {
        if (s.indexOf(getDelimiter()) < 0)
        {
            return Collections.singleton(s);
        }

        final List<String> list = new ArrayList<>();

        StringBuilder token = new StringBuilder();
        int begin = 0;
        boolean inEscape = false;
        final char esc = ESCAPE.charAt(0);

        while (begin < s.length())
        {
            final char c = s.charAt(begin);
            if (inEscape)
            {
                // last character was the escape marker
                // can current character be escaped?
                if (c != getDelimiter() && c != esc)
                {
                    // no, also add escape character
                    token.append(esc);
                }
                token.append(c);
                inEscape = false;
            }

            else
            {
                if (c == getDelimiter())
                {
                    // found a list delimiter -> add token and
                    // resetDefaultFileSystem buffer
                    String t = token.toString();
                    if (trim)
                    {
                        t = t.trim();
                    }
                    list.add(t);
                    token = new StringBuilder();
                }
                else if (c == esc)
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
            token.append(esc);
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
     * {@inheritDoc} This is just a dummy implementation. It is never called.
     */
    @Override
    protected String escapeString(final String s)
    {
        return null;
    }

    /**
     * Performs the escaping of backslashes in the specified properties value.
     * Because a double backslash is used to escape the escape character of a
     * list delimiter, double backslashes also have to be escaped if the
     * property is part of a (single line) list. In addition, because the output
     * is written into a properties file, each occurrence of a backslash again
     * has to be doubled. This method is called by {@code escapeValue()}.
     *
     * @param value the value to be escaped
     * @param inList a flag whether the value is part of a list
     * @return the value with escaped backslashes as string
     */
    protected String escapeBackslashs(final Object value, final boolean inList)
    {
        String strValue = String.valueOf(value);

        if (inList && strValue.indexOf(DOUBLE_ESC) >= 0)
        {
            strValue = StringUtils.replace(strValue, DOUBLE_ESC, QUAD_ESC);
        }

        return strValue;
    }

    /**
     * Escapes the given property value. This method is called on saving the
     * configuration for each property value. It ensures a correct handling of
     * backslash characters and also takes care that list delimiter characters
     * in the value are escaped.
     *
     * @param value the property value
     * @param inList a flag whether the value is part of a list
     * @param transformer the {@code ValueTransformer}
     * @return the escaped property value
     */
    protected String escapeValue(final Object value, final boolean inList,
            final ValueTransformer transformer)
    {
        String escapedValue =
                String.valueOf(transformer.transformValue(escapeBackslashs(
                        value, inList)));
        if (getDelimiter() != 0)
        {
            escapedValue =
                    StringUtils.replace(escapedValue,
                            String.valueOf(getDelimiter()), ESCAPE
                                    + getDelimiter());
        }
        return escapedValue;
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
}
