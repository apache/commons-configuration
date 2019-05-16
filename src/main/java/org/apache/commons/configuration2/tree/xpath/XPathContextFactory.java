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
package org.apache.commons.configuration2.tree.xpath;

import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.jxpath.JXPathContext;

/**
 * <p>
 * An internally used helper class for creating new XPath context objects.
 * </p>
 * <p>
 * This class is used by {@link XPathExpressionEngine}. It simplifies testing.
 * </p>
 *
 */
class XPathContextFactory
{
    /**
     * Creates a new {@code JXPathContext} based on the passed in arguments.
     *
     * @param root the root node
     * @param handler the node handler
     * @param <T> the type of the nodes to be handled
     * @return the newly created context
     */
    public <T> JXPathContext createContext(final T root, final NodeHandler<T> handler)
    {
        final JXPathContext context =
                JXPathContext.newContext(ConfigurationNodePointerFactory
                        .wrapNode(root, handler));
        context.setLenient(true);
        return context;
    }
}
