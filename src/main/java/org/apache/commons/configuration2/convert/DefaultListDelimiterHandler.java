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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * The default implementation of the {@code ListDelimiterHandler} interface.
 * </p>
 * <p>
 * This class supports list splitting and delimiter escaping using a delimiter
 * character that can be specified when constructing an instance. Splitting of
 * strings works by scanning the input for the list delimiter character. The
 * list delimiter character can be escaped by a backslash. So, provided that a
 * comma is configured as list delimiter, in the example {@code val1,val2,val3}
 * three values are recognized. In {@code 3\,1415} the list delimiter is escaped
 * so that only a single element is detected. (Note that when writing these
 * examples in Java code, each backslash has to be doubled. This is also true
 * for all other examples in this documentation.)
 * </p>
 * <p>
 * Because the backslash has a special meaning as escaping character it is
 * always treated in a special way. If it occurs as a normal character in a
 * property value, it has to be escaped using another backslash (similar to the
 * rules of the Java programming language). The following example shows the
 * correct way to define windows network shares: {@code \\\\Server\\path}. Note
 * that each backslash is doubled. When combining the list delimiter with
 * backslashes the same escaping rules apply. For instance, in
 * {@code C:\\Temp\\,D:\\data\\} the list delimiter is recognized; it is not
 * escaped by the preceding backslash because this backslash is itself escaped.
 * In contrast, {@code C:\\Temp\\\,D:\\data\\} defines a single element with a
 * comma being part of the value; two backslashes after {@code Temp} result in a
 * single one, the third backslash escapes the list delimiter.
 * </p>
 * <p>
 * As can be seen, there are some constellations which are a bit tricky and
 * cause a larger number of backslashes in sequence. Nevertheless, the escaping
 * rules are consistent and do not cause ambiguous results.
 * </p>
 * <p>
 * Implementation node: An instance of this class can safely be shared between
 * multiple {@code Configuration} instances.
 * </p>
 *
 * @since 2.0
 */
public class DefaultListDelimiterHandler extends AbstractListDelimiterHandler
{
    /** Constant for the escape character. */
    private static final char ESCAPE = '\\';

    /**
     * Constant for a buffer size for escaping strings. When a character is
     * escaped the string becomes longer. Therefore, the output buffer is longer
     * than the original string length. But we assume, that there are not too
     * many characters that need to be escaped.
     */
    private static final int BUF_SIZE = 16;

    /** Stores the list delimiter character. */
    private final char delimiter;

    /**
     * Creates a new instance of {@code DefaultListDelimiterHandler} and sets
     * the list delimiter character.
     *
     * @param listDelimiter the list delimiter character
     */
    public DefaultListDelimiterHandler(final char listDelimiter)
    {
        delimiter = listDelimiter;
    }

    /**
     * Returns the list delimiter character used by this instance.
     *
     * @return the list delimiter character
     */
    public char getDelimiter()
    {
        return delimiter;
    }

    @Override
    public Object escapeList(final List<?> values, final ValueTransformer transformer)
    {
        final Object[] escapedValues = new String[values.size()];
        int idx = 0;
        for (final Object v : values)
        {
            escapedValues[idx++] = escape(v, transformer);
        }
        return StringUtils.join(escapedValues, getDelimiter());
    }

    @Override
    protected String escapeString(final String s)
    {
        final StringBuilder buf = new StringBuilder(s.length() + BUF_SIZE);
        for (int i = 0; i < s.length(); i++)
        {
            final char c = s.charAt(i);
            if (c == getDelimiter() || c == ESCAPE)
            {
                buf.append(ESCAPE);
            }
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc} This implementation reverses the escaping done by the
     * {@code escape()} methods of this class. However, it tries to be tolerant
     * with unexpected escaping sequences: If after the escape character "\" no
     * allowed character follows, both the backslash and the following character
     * are output.
     */
    @Override
    protected Collection<String> splitString(final String s, final boolean trim)
    {
        final List<String> list = new LinkedList<>();
        StringBuilder token = new StringBuilder();
        boolean inEscape = false;

        for (int i = 0; i < s.length(); i++)
        {
            final char c = s.charAt(i);
            if (inEscape)
            {
                // last character was the escape marker
                // can current character be escaped?
                if (c != getDelimiter() && c != ESCAPE)
                {
                    // no, also add escape character
                    token.append(ESCAPE);
                }
                token.append(c);
                inEscape = false;
            }

            else
            {
                if (c == getDelimiter())
                {
                    // found a list delimiter -> add token and
                    // reset buffer
                    String t = token.toString();
                    if (trim)
                    {
                        t = t.trim();
                    }
                    list.add(t);
                    token = new StringBuilder();
                }
                else if (c == ESCAPE)
                {
                    // potentially escape next character
                    inEscape = true;
                }
                else
                {
                    token.append(c);
                }
            }
        }

        // Trailing delimiter?
        if (inEscape)
        {
            token.append(ESCAPE);
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
}
