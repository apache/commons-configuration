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

import java.util.List;
import java.util.Set;

/**
 * <p>
 * An abstract base class for decorators of a {@code NodeHandler}.
 * </p>
 * <p>
 * This class implements all methods of the {@code NodeHandler} interface by
 * delegating to another instance. This is convenient if specific functionality
 * of a {@code NodeHandler} is to be adapted for a special use case. Concrete
 * sub classes have to implement the {@code getDecoratedNodeHandler()} method to
 * provide the underlying handler.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the nodes supported by this handler
 */
public abstract class NodeHandlerDecorator<T> implements NodeHandler<T>
{
    @Override
    public String nodeName(final T node)
    {
        return getDecoratedNodeHandler().nodeName(node);
    }

    @Override
    public Object getValue(final T node)
    {
        return getDecoratedNodeHandler().getValue(node);
    }

    @Override
    public T getParent(final T node)
    {
        return getDecoratedNodeHandler().getParent(node);
    }

    @Override
    public List<T> getChildren(final T node)
    {
        return getDecoratedNodeHandler().getChildren(node);
    }

    @Override
    public <C> List<T> getMatchingChildren(final T node, final NodeMatcher<C> matcher,
            final C criterion)
    {
        return getDecoratedNodeHandler().getMatchingChildren(node, matcher,
                criterion);
    }

    @Override
    public <C> int getMatchingChildrenCount(final T node, final NodeMatcher<C> matcher,
            final C criterion)
    {
        return getDecoratedNodeHandler().getMatchingChildrenCount(node,
                matcher, criterion);
    }

    @Override
    public List<T> getChildren(final T node, final String name)
    {
        return getDecoratedNodeHandler().getChildren(node, name);
    }

    @Override
    public T getChild(final T node, final int index)
    {
        return getDecoratedNodeHandler().getChild(node, index);
    }

    @Override
    public int indexOfChild(final T parent, final T child)
    {
        return getDecoratedNodeHandler().indexOfChild(parent, child);
    }

    @Override
    public int getChildrenCount(final T node, final String name)
    {
        return getDecoratedNodeHandler().getChildrenCount(node, name);
    }

    @Override
    public Set<String> getAttributes(final T node)
    {
        return getDecoratedNodeHandler().getAttributes(node);
    }

    @Override
    public boolean hasAttributes(final T node)
    {
        return getDecoratedNodeHandler().hasAttributes(node);
    }

    @Override
    public Object getAttributeValue(final T node, final String name)
    {
        return getDecoratedNodeHandler().getAttributeValue(node, name);
    }

    @Override
    public boolean isDefined(final T node)
    {
        return getDecoratedNodeHandler().isDefined(node);
    }

    @Override
    public T getRootNode()
    {
        return getDecoratedNodeHandler().getRootNode();
    }

    /**
     * Returns the {@code NodeHandler} object that is decorated by this
     * instance. All method calls are delegated to this object.
     *
     * @return the decorated {@code NodeHandler}
     */
    protected abstract NodeHandler<T> getDecoratedNodeHandler();
}
