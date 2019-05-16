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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * A data class representing a single query result produced by an
 * {@link ExpressionEngine}.
 * </p>
 * <p>
 * When passing a key to the {@code query()} method of {@code ExpressionEngine}
 * the result can be a set of nodes or attributes - depending on the key. This
 * class can represent both types of results. The aim is to give a user of
 * {@code ExpressionEngine} all information needed for evaluating the results
 * returned.
 * </p>
 * <p>
 * Implementation note: Instances are immutable. They are created using the
 * static factory methods.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the result nodes
 */
public final class QueryResult<T>
{
    /** The node result. */
    private final T node;

    /** The name of the result attribute. */
    private final String attributeName;

    /**
     * Creates a new instance of {@code QueryResult}.
     *
     * @param nd the node
     * @param attr the attribute name
     */
    private QueryResult(final T nd, final String attr)
    {
        node = nd;
        attributeName = attr;
    }

    /**
     * Creates a {@code QueryResult} instance representing the specified result
     * node.
     *
     * @param <T> the type of the result node
     * @param resultNode the result node
     * @return the newly created instance
     */
    public static <T> QueryResult<T> createNodeResult(final T resultNode)
    {
        return new QueryResult<>(resultNode, null);
    }

    /**
     * Creates a {@code QueryResult} instance representing an attribute result.
     * An attribute result consists of the node the attribute belongs to and the
     * attribute name. (The value can be obtained based on this information.)
     *
     * @param parentNode the node which owns the attribute
     * @param attrName the attribute name
     * @param <T> the type of the parent node
     * @return the newly created instance
     */
    public static <T> QueryResult<T> createAttributeResult(final T parentNode,
                                                           final String attrName)
    {
        return new QueryResult<>(parentNode, attrName);
    }

    /**
     * Returns the node referenced by this object. Depending on the result type,
     * this is either the result node or the parent node of the represented
     * attribute.
     *
     * @return the referenced node
     */
    public T getNode()
    {
        return node;
    }

    /**
     * Returns the name of the attribute. This method is defined only for
     * results of type attribute.
     *
     * @return the attribute name
     */
    public String getAttributeName()
    {
        return attributeName;
    }

    /**
     * Returns a flag whether this is a result of type attribute. If result is
     * <b>true</b>, the attribute name and value can be queried. Otherwise, only
     * the result node is available.
     *
     * @return <b>true</b> for an attribute result, <b>false</b> otherwise
     */
    public boolean isAttributeResult()
    {
        return StringUtils.isNotEmpty(getAttributeName());
    }

    /**
     * Returns the attribute value if this is an attribute result. If this is
     * not an attribute result, an exception is thrown.
     *
     * @param handler the {@code NodeHandler}
     * @return the attribute value
     * @throws IllegalStateException if this is not an attribute result
     */
    public Object getAttributeValue(final NodeHandler<T> handler)
    {
        if (!isAttributeResult())
        {
            throw new IllegalStateException("This is not an attribute result! "
                    + "Attribute value cannot be fetched.");
        }
        return handler.getAttributeValue(getNode(), getAttributeName());
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getNode())
                .append(getAttributeName()).toHashCode();
    }

    /**
     * Compares this object with another one. Two instances of
     * {@code QueryResult} are considered equal if they are of the same result
     * type and have the same properties.
     *
     * @param obj the object to compare to
     * @return a flag whether these objects are equal
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof QueryResult))
        {
            return false;
        }

        final QueryResult<?> c = (QueryResult<?>) obj;
        return new EqualsBuilder().append(getNode(), c.getNode())
                .append(getAttributeName(), c.getAttributeName()).isEquals();
    }

    /**
     * Returns a string representation of this object. Depending on the result
     * type either the result node or the parent node and the attribute name are
     * contained in this string.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        final ToStringBuilder sb = new ToStringBuilder(this);
        if (isAttributeResult())
        {
            sb.append("parentNode", getNode()).append("attribute",
                    getAttributeName());
        }
        else
        {
            sb.append("resultNode", getNode());
        }
        return sb.toString();
    }
}
