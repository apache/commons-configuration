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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * A class representing the various symbols that are supported in keys
 * recognized by {@link DefaultExpressionEngine}.
 * </p>
 * <p>
 * An instance of this class is associated with each instance of
 * {@code DefaultExpressionEngine}. It determines which concrete symbols are
 * used to define elements like separators, attributes, etc. within a
 * configuration key.
 * </p>
 * <p>
 * Instances are created using the nested {@code Builder} class. They are
 * immutable and can be shared between arbitrary components.
 * </p>
 *
 * @since 2.0
 */
public final class DefaultExpressionEngineSymbols
{
    /** Constant for the default property delimiter. */
    public static final String DEFAULT_PROPERTY_DELIMITER = ".";

    /** Constant for the default escaped property delimiter. */
    public static final String DEFAULT_ESCAPED_DELIMITER = DEFAULT_PROPERTY_DELIMITER
            + DEFAULT_PROPERTY_DELIMITER;

    /** Constant for the default attribute start marker. */
    public static final String DEFAULT_ATTRIBUTE_START = "[@";

    /** Constant for the default attribute end marker. */
    public static final String DEFAULT_ATTRIBUTE_END = "]";

    /** Constant for the default index start marker. */
    public static final String DEFAULT_INDEX_START = "(";

    /** Constant for the default index end marker. */
    public static final String DEFAULT_INDEX_END = ")";

    /**
     * An instance with default symbols. This instance is used by the default
     * instance of {@code DefaultExpressionEngine}.
     */
    public static final DefaultExpressionEngineSymbols DEFAULT_SYMBOLS =
            createDefaultSmybols();

    /** Stores the property delimiter. */
    private final String propertyDelimiter;

    /** Stores the escaped property delimiter. */
    private final String escapedDelimiter;

    /** Stores the attribute start marker. */
    private final String attributeStart;

    /** Stores the attribute end marker. */
    private final String attributeEnd;

    /** Stores the index start marker. */
    private final String indexStart;

    /** stores the index end marker. */
    private final String indexEnd;

    /**
     * Creates a new instance of {@code DefaultExpressionEngineSymbols}.
     *
     * @param b the builder for defining the properties of this instance
     */
    private DefaultExpressionEngineSymbols(final Builder b)
    {
        propertyDelimiter = b.propertyDelimiter;
        escapedDelimiter = b.escapedDelimiter;
        indexStart = b.indexStart;
        indexEnd = b.indexEnd;
        attributeStart = b.attributeStart;
        attributeEnd = b.attributeEnd;
    }

    /**
     * Returns the string used as delimiter in property keys.
     *
     * @return the property delimiter
     */
    public String getPropertyDelimiter()
    {
        return propertyDelimiter;
    }

    /**
     * Returns the string representing an escaped property delimiter.
     *
     * @return the escaped property delimiter
     */
    public String getEscapedDelimiter()
    {
        return escapedDelimiter;
    }

    /**
     * Returns the string representing an attribute start marker.
     *
     * @return the attribute start marker
     */
    public String getAttributeStart()
    {
        return attributeStart;
    }

    /**
     * Returns the string representing an attribute end marker.
     *
     * @return the attribute end marker
     */
    public String getAttributeEnd()
    {
        return attributeEnd;
    }

    /**
     * Returns the string representing the start of an index in a property key.
     *
     * @return the index start marker
     */
    public String getIndexStart()
    {
        return indexStart;
    }

    /**
     * Returns the string representing the end of an index in a property key.
     *
     * @return the index end marker
     */
    public String getIndexEnd()
    {
        return indexEnd;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getPropertyDelimiter())
                .append(getEscapedDelimiter()).append(getIndexStart())
                .append(getIndexEnd()).append(getAttributeStart())
                .append(getAttributeEnd()).toHashCode();
    }

    /**
     * Compares this object with another one. Two instances of
     * {@code DefaultExpressionEngineSymbols} are considered equal if all of
     * their properties are equal.
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
        if (!(obj instanceof DefaultExpressionEngineSymbols))
        {
            return false;
        }

        final DefaultExpressionEngineSymbols c = (DefaultExpressionEngineSymbols) obj;
        return new EqualsBuilder()
                .append(getPropertyDelimiter(), c.getPropertyDelimiter())
                .append(getEscapedDelimiter(), c.getEscapedDelimiter())
                .append(getIndexStart(), c.getIndexStart())
                .append(getIndexEnd(), c.getIndexEnd())
                .append(getAttributeStart(), c.getAttributeStart())
                .append(getAttributeEnd(), c.getAttributeEnd()).isEquals();
    }

    /**
     * Returns a string representation for this object. This string contains the
     * values of all properties.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("propertyDelimiter", getPropertyDelimiter())
                .append("escapedDelimiter", getEscapedDelimiter())
                .append("indexStart", getIndexStart())
                .append("indexEnd", getIndexEnd())
                .append("attributeStart", getAttributeStart())
                .append("attributeEnd", getAttributeEnd()).toString();
    }

    /**
     * Creates the {@code DefaultExpressionEngineSymbols} object with default
     * symbols.
     *
     * @return the default symbols instance
     */
    private static DefaultExpressionEngineSymbols createDefaultSmybols()
    {
        return new Builder().setPropertyDelimiter(DEFAULT_PROPERTY_DELIMITER)
                .setEscapedDelimiter(DEFAULT_ESCAPED_DELIMITER)
                .setIndexStart(DEFAULT_INDEX_START)
                .setIndexEnd(DEFAULT_INDEX_END)
                .setAttributeStart(DEFAULT_ATTRIBUTE_START)
                .setAttributeEnd(DEFAULT_ATTRIBUTE_END).create();
    }

    /**
     * A builder class for creating instances of
     * {@code DefaultExpressionEngineSymbols}.
     */
    public static class Builder
    {
        /** Stores the property delimiter. */
        private String propertyDelimiter;

        /** Stores the escaped property delimiter. */
        private String escapedDelimiter;

        /** Stores the attribute start marker. */
        private String attributeStart;

        /** Stores the attribute end marker. */
        private String attributeEnd;

        /** Stores the index start marker. */
        private String indexStart;

        /** stores the index end marker. */
        private String indexEnd;

        /**
         * Creates a new, uninitialized instance of {@code Builder}. All symbols
         * are undefined.
         */
        public Builder()
        {
        }

        /**
         * Creates a new instance of {@code Builder} whose properties are
         * initialized from the passed in {@code DefaultExpressionEngineSymbols}
         * object. This is useful if symbols are to be created which are similar
         * to the passed in instance.
         *
         * @param c the {@code DefaultExpressionEngineSymbols} object serving as
         *        starting point for this builder
         */
        public Builder(final DefaultExpressionEngineSymbols c)
        {
            propertyDelimiter = c.getPropertyDelimiter();
            escapedDelimiter = c.getEscapedDelimiter();
            indexStart = c.getIndexStart();
            indexEnd = c.getIndexEnd();
            attributeStart = c.getAttributeStart();
            attributeEnd = c.getAttributeEnd();
        }

        /**
         * Sets the string representing a delimiter for properties.
         *
         * @param d the property delimiter
         * @return a reference to this object for method chaining
         */
        public Builder setPropertyDelimiter(final String d)
        {
            propertyDelimiter = d;
            return this;
        }

        /**
         * Sets the string representing an escaped property delimiter. With this
         * string a delimiter that belongs to the key of a property can be
         * escaped. If for instance &quot;.&quot; is used as property delimiter,
         * you can set the escaped delimiter to &quot;\.&quot; and can then
         * escape the delimiter with a back slash.
         *
         * @param ed the escaped property delimiter
         * @return a reference to this object for method chaining
         */
        public Builder setEscapedDelimiter(final String ed)
        {
            escapedDelimiter = ed;
            return this;
        }

        /**
         * Sets the string representing the start of an index in a property key.
         * Index start and end marker are used together to detect indices in a
         * property key.
         *
         * @param is the index start
         * @return a reference to this object for method chaining
         */
        public Builder setIndexStart(final String is)
        {
            indexStart = is;
            return this;
        }

        /**
         * Sets the string representing the end of an index in a property key.
         *
         * @param ie the index end
         * @return a reference to this object for method chaining
         */
        public Builder setIndexEnd(final String ie)
        {
            indexEnd = ie;
            return this;
        }

        /**
         * Sets the string representing the start marker of an attribute in a
         * property key. Attribute start and end marker are used together to
         * detect attributes in a property key.
         *
         * @param as the attribute start marker
         * @return a reference to this object for method chaining
         */
        public Builder setAttributeStart(final String as)
        {
            attributeStart = as;
            return this;
        }

        /**
         * Sets the string representing the end marker of an attribute in a
         * property key.
         *
         * @param ae the attribute end marker
         * @return a reference to this object for method chaining
         */
        public Builder setAttributeEnd(final String ae)
        {
            attributeEnd = ae;
            return this;
        }

        /**
         * Creates the {@code DefaultExpressionEngineSymbols} instance based on
         * the properties set for this builder object. This method does not
         * change the state of this builder. So it is possible to change
         * properties and create another {@code DefaultExpressionEngineSymbols}
         * instance.
         *
         * @return the newly created {@code DefaultExpressionEngineSymbols}
         *         instance
         */
        public DefaultExpressionEngineSymbols create()
        {
            return new DefaultExpressionEngineSymbols(this);
        }
    }
}
