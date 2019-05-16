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
import java.util.List;

/**
 * <p>
 * A specialized implementation of the {@code ListDelimiterHandler} interface
 * which disables list splitting.
 * </p>
 * <p>
 * This class does not recognize any list delimiters; passed in strings are
 * returned unchanged. Also the {@code escape()} method is a dummy - there is no
 * need for escaping delimiter characters as none are supported. Note that the
 * method for escaping a list throws an {@code UnsupportedOperationException}.
 * If list delimiters are not supported, there is no point in squashing multiple
 * values into a single one.
 * </p>
 * <p>
 * Implementation note: An instance of this class can be shared between multiple
 * configuration objects. It is state-less and thread-safe.
 * </p>
 *
 * @since 2.0
 */
public class DisabledListDelimiterHandler extends AbstractListDelimiterHandler
{
    /**
     * A default instance of this class. Because it is safe to share
     * {@code DisabledListDelimiterHandler} objects this instance can be used
     * whenever such an object is needed.
     */
    public static final ListDelimiterHandler INSTANCE =
            new DisabledListDelimiterHandler();

    /**
     * {@inheritDoc} This implementation always throws an
     * {@code UnsupportedOperationException} exception.
     */
    @Override
    public Object escapeList(final List<?> values, final ValueTransformer transformer)
    {
        throw new UnsupportedOperationException(
                "Escaping lists is not supported!");
    }

    /**
     * {@inheritDoc} This implementation always returns a collection containing
     * the passed in string as its single element. The string is not changed,
     * the {@code trim} flag is ignored. (The {@code trim} flag refers to the
     * components extracted from the string. Because no components are extracted
     * nothing is trimmed.)
     */
    @Override
    protected Collection<String> splitString(final String s, final boolean trim)
    {
        final Collection<String> result = new ArrayList<>(1);
        result.add(s);
        return result;
    }

    /**
     * {@inheritDoc} This implementation returns the passed in string without
     * any changes.
     */
    @Override
    protected String escapeString(final String s)
    {
        return s;
    }
}
