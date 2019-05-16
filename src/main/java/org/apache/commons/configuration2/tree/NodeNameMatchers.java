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
package org.apache.commons.configuration2.tree;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * An enumeration class with several pre-defined {@link NodeMatcher}
 * implementations based on node names.
 * </p>
 * <p>
 * Filtering nodes by their name is a typical use case. Therefore, some default
 * implementations for typical filter algorithms are already provided. They are
 * available as constants of this class. Because the algorithms are state-less
 * these instances can be shared and accessed concurrently.
 * </p>
 *
 * @since 2.0
 */
public enum NodeNameMatchers implements NodeMatcher<String>
{
    /**
     * A matcher for exact node name matches. This matcher returns <b>true</b>
     * if and only if the name of the passed in node equals exactly the given
     * criterion string.
     */
    EQUALS
    {
        @Override
        public <T> boolean matches(final T node, final NodeHandler<T> handler,
                final String criterion)
        {
            return StringUtils.equals(criterion, handler.nodeName(node));
        }
    },

    /**
     * A matcher for matches on node names which ignores case. For this matcher
     * the names {@code node}, {@code NODE}, or {@code NodE} are all the same.
     */
    EQUALS_IGNORE_CASE
    {
        @Override
        public <T> boolean matches(final T node, final NodeHandler<T> handler,
                final String criterion)
        {
            return StringUtils.equalsIgnoreCase(criterion,
                    handler.nodeName(node));
        }
    }
}
