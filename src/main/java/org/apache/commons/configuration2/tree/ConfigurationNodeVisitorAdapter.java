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

/**
 * <p>
 * A simple adapter class that simplifies writing custom node visitor
 * implementations.
 * </p>
 * <p>
 * This class provides dummy implementations for the methods defined in the
 * {@code ConfigurationNodeVisitor} interface. Derived classes only need
 * to override the methods they really need.
 * </p>
 *
 * @param  <T> the type of the nodes processed by this visitor
 */
public class ConfigurationNodeVisitorAdapter<T> implements
        ConfigurationNodeVisitor<T>
{
    /**
     * {@inheritDoc} Empty dummy implementation of this interface method.
     */
    @Override
    public void visitBeforeChildren(final T node, final NodeHandler<T> handler)
    {
    }

    /**
     * {@inheritDoc} Empty dummy implementation of this interface method.
     */
    @Override
    public void visitAfterChildren(final T node, final NodeHandler<T> handler)
    {
    }

    /**
     * {@inheritDoc} This implementation returns always <b>false</b>; this means
     * that all nodes in the current hierarchy are traversed.
     */
    @Override
    public boolean terminate()
    {
        return false;
    }
}
