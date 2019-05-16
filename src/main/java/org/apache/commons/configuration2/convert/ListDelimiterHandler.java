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
import java.util.List;

/**
 * <p>
 * Definition of an interface that controls the handling of list delimiters in
 * configuration properties.
 * </p>
 * <p>
 * {@link org.apache.commons.configuration2.AbstractConfiguration
 * AbstractConfiguration} supports list delimiters in property values. If such a
 * delimiter is found, the value actually contains multiple values and has to be
 * split. This is useful for instance for
 * {@link org.apache.commons.configuration2.PropertiesConfiguration
 * PropertiesConfiguration}: properties files that have to be compatible with
 * the {@code java.util.Properties} class cannot have multiple occurrences of a
 * single property key, therefore a different storage scheme for multi-valued
 * properties is needed. A possible storage scheme could look as follows:
 * </p>
 *
 * <pre>
 * myProperty=value1,value2,value3
 * </pre>
 *
 * <p>
 * Here a comma is used as list delimiter. When parsing this property (and using
 * a corresponding {@code ListDelimiterHandler} implementation) the string value
 * is split, and three values are added for the property key.
 * </p>
 * <p>
 * A {@code ListDelimiterHandler} knows how to parse and to escape property
 * values. It is called by concrete {@code Configuration} implementations when
 * they have to deal with properties with multiple values.
 * </p>
 *
 * @since 2.0
 */
public interface ListDelimiterHandler
{
    /**
     * A specialized {@code ValueTransformer} implementation which does no
     * transformation. The {@code transformValue()} method just returns the
     * passed in object without changes. This instance can be used by
     * configurations which do not require additional encoding.
     */
    ValueTransformer NOOP_TRANSFORMER = new ValueTransformer()
    {
        @Override
        public Object transformValue(final Object value)
        {
            return value;
        }
    };

    /**
     * Parses the specified value for list delimiters and splits it if
     * necessary. The passed in object can be either a single value or a complex
     * one, e.g. a collection, an array, or an {@code Iterable}. It is the
     * responsibility of this method to return an {@code Iterable} which
     * contains all extracted values.
     *
     * @param value the value to be parsed
     * @return an {@code Iterable} allowing access to all extracted values
     */
    Iterable<?> parse(Object value);

    /**
     * Splits the specified string at the list delimiter and returns a
     * collection with all extracted components. A concrete implementation also
     * has to deal with escape characters which might mask a list delimiter
     * character at certain positions. The boolean {@code trim} flag determines
     * whether each extracted component should be trimmed. This typically makes
     * sense as the list delimiter may be surrounded by whitespace. However,
     * there may be specific use cases in which automatic trimming is not
     * desired.
     *
     * @param s the string to be split
     * @param trim a flag whether each component of the string is to be trimmed
     * @return a collection with all components extracted from the string
     */
    Collection<String> split(String s, boolean trim);

    /**
     * Escapes the specified single value object. This method is called for
     * properties containing only a single value. So this method can rely on the
     * fact that the passed in object is not a list. An implementation has to
     * check whether the value contains list delimiter characters and - if so -
     * escape them accordingly.
     *
     * @param value the value to be escaped
     * @param transformer a {@code ValueTransformer} for an additional encoding
     *        (must not be <b>null</b>)
     * @return the escaped value
     */
    Object escape(Object value, ValueTransformer transformer);

    /**
     * Escapes all values in the given list and concatenates them to a single
     * string. This operation is required by configurations that have to
     * represent properties with multiple values in a single line in their
     * external configuration representation. This may require an advanced
     * escaping in some cases.
     *
     * @param values the list with all the values to be converted to a single
     *        value
     * @param transformer a {@code ValueTransformer} for an additional encoding
     *        (must not be <b>null</b>)
     * @return the resulting escaped value
     */
    Object escapeList(List<?> values, ValueTransformer transformer);
}
