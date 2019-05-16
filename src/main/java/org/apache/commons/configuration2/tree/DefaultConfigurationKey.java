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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A simple class that supports creation of and iteration on configuration keys
 * supported by a {@link DefaultExpressionEngine} object.
 * </p>
 * <p>
 * For key creation the class works similar to a StringBuffer: There are several
 * {@code appendXXXX()} methods with which single parts of a key can be
 * constructed. All these methods return a reference to the actual object so
 * they can be written in a chain. When using this methods the exact syntax for
 * keys need not be known.
 * </p>
 * <p>
 * This class also defines a specialized iterator for configuration keys. With
 * such an iterator a key can be tokenized into its single parts. For each part
 * it can be checked whether it has an associated index.
 * </p>
 * <p>
 * Instances of this class are always associated with an instance of
 * {@link DefaultExpressionEngine}, from which the current
 * delimiters are obtained. So key creation and parsing is specific to this
 * associated expression engine.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class DefaultConfigurationKey
{
    /** Constant for the initial StringBuffer size. */
    private static final int INITIAL_SIZE = 32;

    /** Stores a reference to the associated expression engine. */
    private final DefaultExpressionEngine expressionEngine;

    /** Holds a buffer with the so far created key. */
    private final StringBuilder keyBuffer;

    /**
     * Creates a new instance of {@code DefaultConfigurationKey} and sets
     * the associated expression engine.
     *
     * @param engine the expression engine (must not be <b>null</b>)
     * @throws IllegalArgumentException if the expression engine is <b>null</b>
     */
    public DefaultConfigurationKey(final DefaultExpressionEngine engine)
    {
        this(engine, null);
    }

    /**
     * Creates a new instance of {@code DefaultConfigurationKey} and sets the
     * associated expression engine and an initial key.
     *
     * @param engine the expression engine (must not be <b>null</b>)
     * @param key the key to be wrapped
     * @throws IllegalArgumentException if the expression engine is <b>null</b>
     */
    public DefaultConfigurationKey(final DefaultExpressionEngine engine, final String key)
    {
        if (engine == null)
        {
            throw new IllegalArgumentException(
                    "Expression engine must not be null!");
        }
        expressionEngine = engine;
        if (key != null)
        {
            keyBuffer = new StringBuilder(trim(key));
        }
        else
        {
            keyBuffer = new StringBuilder(INITIAL_SIZE);
        }
    }

    /**
     * Returns the associated default expression engine.
     *
     * @return the associated expression engine
     */
    public DefaultExpressionEngine getExpressionEngine()
    {
        return expressionEngine;
    }

    /**
     * Appends the name of a property to this key. If necessary, a property
     * delimiter will be added. If the boolean argument is set to <b>true</b>,
     * property delimiters contained in the property name will be escaped.
     *
     * @param property the name of the property to be added
     * @param escape a flag if property delimiters in the passed in property name
     * should be escaped
     * @return a reference to this object
     */
    public DefaultConfigurationKey append(final String property, final boolean escape)
    {
        String key;
        if (escape && property != null)
        {
            key = escapeDelimiters(property);
        }
        else
        {
            key = property;
        }
        key = trim(key);

        if (keyBuffer.length() > 0 && !isAttributeKey(property)
                && key.length() > 0)
        {
            keyBuffer.append(getSymbols().getPropertyDelimiter());
        }

        keyBuffer.append(key);
        return this;
    }

    /**
     * Appends the name of a property to this key. If necessary, a property
     * delimiter will be added. Property delimiters in the given string will not
     * be escaped.
     *
     * @param property the name of the property to be added
     * @return a reference to this object
     */
    public DefaultConfigurationKey append(final String property)
    {
        return append(property, false);
    }

    /**
     * Appends an index to this configuration key.
     *
     * @param index the index to be appended
     * @return a reference to this object
     */
    public DefaultConfigurationKey appendIndex(final int index)
    {
        keyBuffer.append(getSymbols().getIndexStart());
        keyBuffer.append(index);
        keyBuffer.append(getSymbols().getIndexEnd());
        return this;
    }

    /**
     * Appends an attribute to this configuration key.
     *
     * @param attr the name of the attribute to be appended
     * @return a reference to this object
     */
    public DefaultConfigurationKey appendAttribute(final String attr)
    {
        keyBuffer.append(constructAttributeKey(attr));
        return this;
    }

    /**
     * Returns the actual length of this configuration key.
     *
     * @return the length of this key
     */
    public int length()
    {
        return keyBuffer.length();
    }

    /**
     * Sets the new length of this configuration key. With this method it is
     * possible to truncate the key, e.g. to return to a state prior calling
     * some {@code append()} methods. The semantic is the same as the
     * {@code setLength()} method of {@code StringBuilder}.
     *
     * @param len the new length of the key
     */
    public void setLength(final int len)
    {
        keyBuffer.setLength(len);
    }
    /**
     * Returns a configuration key object that is initialized with the part
     * of the key that is common to this key and the passed in key.
     *
     * @param other the other key
     * @return a key object with the common key part
     */
    public DefaultConfigurationKey commonKey(final DefaultConfigurationKey other)
    {
        if (other == null)
        {
            throw new IllegalArgumentException("Other key must no be null!");
        }

        final DefaultConfigurationKey result = new DefaultConfigurationKey(getExpressionEngine());
        final KeyIterator it1 = iterator();
        final KeyIterator it2 = other.iterator();

        while (it1.hasNext() && it2.hasNext() && partsEqual(it1, it2))
        {
            if (it1.isAttribute())
            {
                result.appendAttribute(it1.currentKey());
            }
            else
            {
                result.append(it1.currentKey());
                if (it1.hasIndex)
                {
                    result.appendIndex(it1.getIndex());
                }
            }
        }

        return result;
    }

    /**
     * Returns the &quot;difference key&quot; to a given key. This value
     * is the part of the passed in key that differs from this key. There is
     * the following relation:
     * {@code other = key.commonKey(other) + key.differenceKey(other)}
     * for an arbitrary configuration key {@code key}.
     *
     * @param other the key for which the difference is to be calculated
     * @return the difference key
     */
    public DefaultConfigurationKey differenceKey(final DefaultConfigurationKey other)
    {
        final DefaultConfigurationKey common = commonKey(other);
        final DefaultConfigurationKey result = new DefaultConfigurationKey(getExpressionEngine());

        if (common.length() < other.length())
        {
            final String k = other.toString().substring(common.length());
            // skip trailing delimiters
            int i = 0;
            while (i < k.length()
                    && String.valueOf(k.charAt(i)).equals(
                            getSymbols().getPropertyDelimiter()))
            {
                i++;
            }

            if (i < k.length())
            {
                result.append(k.substring(i));
            }
        }

        return result;
    }

    /**
     * Checks if two {@code ConfigurationKey} objects are equal. Two instances
     * of this class are considered equal if they have the same content (i.e.
     * their internal string representation is equal). The expression engine
     * property is not taken into account.
     *
     * @param obj the object to compare
     * @return a flag if both objects are equal
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof DefaultConfigurationKey))
        {
            return false;
        }

        final DefaultConfigurationKey c = (DefaultConfigurationKey) obj;
        return keyBuffer.toString().equals(c.toString());
    }

    /**
     * Returns the hash code for this object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return String.valueOf(keyBuffer).hashCode();
    }

    /**
     * Returns a string representation of this object. This is the configuration
     * key as a plain string.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return keyBuffer.toString();
    }

    /**
     * Tests if the specified key represents an attribute according to the
     * current expression engine.
     *
     * @param key the key to be checked
     * @return <b>true</b> if this is an attribute key, <b>false</b> otherwise
     */
    public boolean isAttributeKey(final String key)
    {
        if (key == null)
        {
            return false;
        }

        return key.startsWith(getSymbols().getAttributeStart())
                && (getSymbols().getAttributeEnd() == null || key
                        .endsWith(getSymbols().getAttributeEnd()));
    }

    /**
     * Decorates the given key so that it represents an attribute. Adds special
     * start and end markers. The passed in string will be modified only if does
     * not already represent an attribute.
     *
     * @param key the key to be decorated
     * @return the decorated attribute key
     */
    public String constructAttributeKey(final String key)
    {
        if (key == null)
        {
            return StringUtils.EMPTY;
        }
        if (isAttributeKey(key))
        {
            return key;
        }
        final StringBuilder buf = new StringBuilder();
        buf.append(getSymbols().getAttributeStart()).append(key);
        if (getSymbols().getAttributeEnd() != null)
        {
            buf.append(getSymbols().getAttributeEnd());
        }
        return buf.toString();
    }

    /**
     * Extracts the name of the attribute from the given attribute key. This
     * method removes the attribute markers - if any - from the specified key.
     *
     * @param key the attribute key
     * @return the name of the corresponding attribute
     */
    public String attributeName(final String key)
    {
        return isAttributeKey(key) ? removeAttributeMarkers(key) : key;
    }

    /**
     * Removes leading property delimiters from the specified key.
     *
     * @param key the key
     * @return the key with removed leading property delimiters
     */
    public String trimLeft(final String key)
    {
        if (key == null)
        {
            return StringUtils.EMPTY;
        }
        String result = key;
        while (hasLeadingDelimiter(result))
        {
            result = result.substring(getSymbols()
                    .getPropertyDelimiter().length());
        }
        return result;
    }

    /**
     * Removes trailing property delimiters from the specified key.
     *
     * @param key the key
     * @return the key with removed trailing property delimiters
     */
    public String trimRight(final String key)
    {
        if (key == null)
        {
            return StringUtils.EMPTY;
        }
        String result = key;
        while (hasTrailingDelimiter(result))
        {
            result = result
                    .substring(0, result.length()
                            - getSymbols().getPropertyDelimiter()
                                    .length());
        }
        return result;
    }

    /**
     * Removes delimiters at the beginning and the end of the specified key.
     *
     * @param key the key
     * @return the key with removed property delimiters
     */
    public String trim(final String key)
    {
        return trimRight(trimLeft(key));
    }

    /**
     * Returns an iterator for iterating over the single components of this
     * configuration key.
     *
     * @return an iterator for this key
     */
    public KeyIterator iterator()
    {
        return new KeyIterator();
    }

    /**
     * Helper method that checks if the specified key ends with a property
     * delimiter.
     *
     * @param key the key to check
     * @return a flag if there is a trailing delimiter
     */
    private boolean hasTrailingDelimiter(final String key)
    {
        return key.endsWith(getSymbols().getPropertyDelimiter())
                && (getSymbols().getEscapedDelimiter() == null || !key
                        .endsWith(getSymbols().getEscapedDelimiter()));
    }

    /**
     * Helper method that checks if the specified key starts with a property
     * delimiter.
     *
     * @param key the key to check
     * @return a flag if there is a leading delimiter
     */
    private boolean hasLeadingDelimiter(final String key)
    {
        return key.startsWith(getSymbols().getPropertyDelimiter())
                && (getSymbols().getEscapedDelimiter() == null || !key
                        .startsWith(getSymbols().getEscapedDelimiter()));
    }

    /**
     * Helper method for removing attribute markers from a key.
     *
     * @param key the key
     * @return the key with removed attribute markers
     */
    private String removeAttributeMarkers(final String key)
    {
        return key
                .substring(
                        getSymbols().getAttributeStart().length(),
                        key.length()
                                - ((getSymbols().getAttributeEnd() != null) ? getSymbols()
                                        .getAttributeEnd().length()
                                        : 0));
    }

    /**
     * Unescapes the delimiters in the specified string.
     *
     * @param key the key to be unescaped
     * @return the unescaped key
     */
    private String unescapeDelimiters(final String key)
    {
        return (getSymbols().getEscapedDelimiter() == null) ? key
                : StringUtils.replace(key, getSymbols()
                        .getEscapedDelimiter(), getSymbols()
                        .getPropertyDelimiter());
    }

    /**
     * Returns the symbols object from the associated expression engine.
     *
     * @return the {@code DefaultExpressionEngineSymbols}
     */
    private DefaultExpressionEngineSymbols getSymbols()
    {
        return getExpressionEngine().getSymbols();
    }

    /**
     * Escapes the delimiters in the specified string.
     *
     * @param key the key to be escaped
     * @return the escaped key
     */
    private String escapeDelimiters(final String key)
    {
        return (getSymbols().getEscapedDelimiter() == null || key
                .indexOf(getSymbols().getPropertyDelimiter()) < 0) ? key
                : StringUtils.replace(key, getSymbols()
                        .getPropertyDelimiter(), getSymbols()
                        .getEscapedDelimiter());
    }

    /**
     * Helper method for comparing two key parts.
     *
     * @param it1 the iterator with the first part
     * @param it2 the iterator with the second part
     * @return a flag if both parts are equal
     */
    private static boolean partsEqual(final KeyIterator it1, final KeyIterator it2)
    {
        return it1.nextKey().equals(it2.nextKey())
                && it1.getIndex() == it2.getIndex()
                && it1.isAttribute() == it2.isAttribute();
    }

    /**
     * A specialized iterator class for tokenizing a configuration key. This
     * class implements the normal iterator interface. In addition it provides
     * some specific methods for configuration keys.
     */
    public class KeyIterator implements Iterator<Object>, Cloneable
    {
        /** Stores the current key name. */
        private String current;

        /** Stores the start index of the actual token. */
        private int startIndex;

        /** Stores the end index of the actual token. */
        private int endIndex;

        /** Stores the index of the actual property if there is one. */
        private int indexValue;

        /** Stores a flag if the actual property has an index. */
        private boolean hasIndex;

        /** Stores a flag if the actual property is an attribute. */
        private boolean attribute;

        /**
         * Returns the next key part of this configuration key. This is a short
         * form of {@code nextKey(false)}.
         *
         * @return the next key part
         */
        public String nextKey()
        {
            return nextKey(false);
        }

        /**
         * Returns the next key part of this configuration key. The boolean
         * parameter indicates wheter a decorated key should be returned. This
         * affects only attribute keys: if the parameter is <b>false</b>, the
         * attribute markers are stripped from the key; if it is <b>true</b>,
         * they remain.
         *
         * @param decorated a flag if the decorated key is to be returned
         * @return the next key part
         */
        public String nextKey(final boolean decorated)
        {
            if (!hasNext())
            {
                throw new NoSuchElementException("No more key parts!");
            }

            hasIndex = false;
            indexValue = -1;
            final String key = findNextIndices();

            current = key;
            hasIndex = checkIndex(key);
            attribute = checkAttribute(current);

            return currentKey(decorated);
        }

        /**
         * Checks if there is a next element.
         *
         * @return a flag if there is a next element
         */
        @Override
        public boolean hasNext()
        {
            return endIndex < keyBuffer.length();
        }

        /**
         * Returns the next object in the iteration.
         *
         * @return the next object
         */
        @Override
        public Object next()
        {
            return nextKey();
        }

        /**
         * Removes the current object in the iteration. This method is not
         * supported by this iterator type, so an exception is thrown.
         */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Remove not supported!");
        }

        /**
         * Returns the current key of the iteration (without skipping to the
         * next element). This is the same key the previous {@code next()}
         * call had returned. (Short form of {@code currentKey(false)}.
         *
         * @return the current key
         */
        public String currentKey()
        {
            return currentKey(false);
        }

        /**
         * Returns the current key of the iteration (without skipping to the
         * next element). The boolean parameter indicates wheter a decorated key
         * should be returned. This affects only attribute keys: if the
         * parameter is <b>false</b>, the attribute markers are stripped from
         * the key; if it is <b>true</b>, they remain.
         *
         * @param decorated a flag if the decorated key is to be returned
         * @return the current key
         */
        public String currentKey(final boolean decorated)
        {
            return (decorated && !isPropertyKey()) ? constructAttributeKey(current)
                    : current;
        }

        /**
         * Returns a flag if the current key is an attribute. This method can be
         * called after {@code next()}.
         *
         * @return a flag if the current key is an attribute
         */
        public boolean isAttribute()
        {
            // if attribute emulation mode is active, the last part of a key is
            // always an attribute key, too
            return attribute || (isAttributeEmulatingMode() && !hasNext());
        }

        /**
         * Returns a flag whether the current key refers to a property (i.e. is
         * no special attribute key). Usually this method will return the
         * opposite of {@code isAttribute()}, but if the delimiters for
         * normal properties and attributes are set to the same string, it is
         * possible that both methods return <b>true</b>.
         *
         * @return a flag if the current key is a property key
         * @see #isAttribute()
         */
        public boolean isPropertyKey()
        {
            return !attribute;
        }

        /**
         * Returns the index value of the current key. If the current key does
         * not have an index, return value is -1. This method can be called
         * after {@code next()}.
         *
         * @return the index value of the current key
         */
        public int getIndex()
        {
            return indexValue;
        }

        /**
         * Returns a flag if the current key has an associated index. This
         * method can be called after {@code next()}.
         *
         * @return a flag if the current key has an index
         */
        public boolean hasIndex()
        {
            return hasIndex;
        }

        /**
         * Creates a clone of this object.
         *
         * @return a clone of this object
         */
        @Override
        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (final CloneNotSupportedException cex)
            {
                // should not happen
                return null;
            }
        }

        /**
         * Helper method for determining the next indices.
         *
         * @return the next key part
         */
        private String findNextIndices()
        {
            startIndex = endIndex;
            // skip empty names
            while (startIndex < length()
                    && hasLeadingDelimiter(keyBuffer.substring(startIndex)))
            {
                startIndex += getSymbols().getPropertyDelimiter()
                        .length();
            }

            // Key ends with a delimiter?
            if (startIndex >= length())
            {
                endIndex = length();
                startIndex = endIndex - 1;
                return keyBuffer.substring(startIndex, endIndex);
            }
            return nextKeyPart();
        }

        /**
         * Helper method for extracting the next key part. Takes escaping of
         * delimiter characters into account.
         *
         * @return the next key part
         */
        private String nextKeyPart()
        {
            int attrIdx = keyBuffer.toString().indexOf(
                    getSymbols().getAttributeStart(), startIndex);
            if (attrIdx < 0 || attrIdx == startIndex)
            {
                attrIdx = length();
            }

            int delIdx = nextDelimiterPos(keyBuffer.toString(), startIndex,
                    attrIdx);
            if (delIdx < 0)
            {
                delIdx = attrIdx;
            }

            endIndex = Math.min(attrIdx, delIdx);
            return unescapeDelimiters(keyBuffer.substring(startIndex, endIndex));
        }

        /**
         * Searches the next unescaped delimiter from the given position.
         *
         * @param key the key
         * @param pos the start position
         * @param endPos the end position
         * @return the position of the next delimiter or -1 if there is none
         */
        private int nextDelimiterPos(final String key, final int pos, final int endPos)
        {
            int delimiterPos = pos;
            boolean found = false;

            do
            {
                delimiterPos = key.indexOf(getSymbols()
                        .getPropertyDelimiter(), delimiterPos);
                if (delimiterPos < 0 || delimiterPos >= endPos)
                {
                    return -1;
                }
                final int escapePos = escapedPosition(key, delimiterPos);
                if (escapePos < 0)
                {
                    found = true;
                }
                else
                {
                    delimiterPos = escapePos;
                }
            }
            while (!found);

            return delimiterPos;
        }

        /**
         * Checks if a delimiter at the specified position is escaped. If this
         * is the case, the next valid search position will be returned.
         * Otherwise the return value is -1.
         *
         * @param key the key to check
         * @param pos the position where a delimiter was found
         * @return information about escaped delimiters
         */
        private int escapedPosition(final String key, final int pos)
        {
            if (getSymbols().getEscapedDelimiter() == null)
            {
                // nothing to escape
                return -1;
            }
            final int escapeOffset = escapeOffset();
            if (escapeOffset < 0 || escapeOffset > pos)
            {
                // No escaping possible at this position
                return -1;
            }

            final int escapePos = key.indexOf(getSymbols()
                    .getEscapedDelimiter(), pos - escapeOffset);
            if (escapePos <= pos && escapePos >= 0)
            {
                // The found delimiter is escaped. Next valid search position
                // is behind the escaped delimiter.
                return escapePos
                        + getSymbols().getEscapedDelimiter().length();
            }
            return -1;
        }

        /**
         * Determines the relative offset of an escaped delimiter in relation to
         * a delimiter. Depending on the used delimiter and escaped delimiter
         * tokens the position where to search for an escaped delimiter is
         * different. If, for instance, the dot character (&quot;.&quot;) is
         * used as delimiter, and a doubled dot (&quot;..&quot;) as escaped
         * delimiter, the escaped delimiter starts at the same position as the
         * delimiter. If the token &quot;\.&quot; was used, it would start one
         * character before the delimiter because the delimiter character
         * &quot;.&quot; is the second character in the escaped delimiter
         * string. This relation will be determined by this method. For this to
         * work the delimiter string must be contained in the escaped delimiter
         * string.
         *
         * @return the relative offset of the escaped delimiter in relation to a
         * delimiter
         */
        private int escapeOffset()
        {
            return getSymbols().getEscapedDelimiter().indexOf(
                    getSymbols().getPropertyDelimiter());
        }

        /**
         * Helper method for checking if the passed key is an attribute. If this
         * is the case, the internal fields will be set.
         *
         * @param key the key to be checked
         * @return a flag if the key is an attribute
         */
        private boolean checkAttribute(final String key)
        {
            if (isAttributeKey(key))
            {
                current = removeAttributeMarkers(key);
                return true;
            }
            return false;
        }

        /**
         * Helper method for checking if the passed key contains an index. If
         * this is the case, internal fields will be set.
         *
         * @param key the key to be checked
         * @return a flag if an index is defined
         */
        private boolean checkIndex(final String key)
        {
            boolean result = false;

            try
            {
                final int idx = key.lastIndexOf(getSymbols().getIndexStart());
                if (idx > 0)
                {
                    final int endidx = key.indexOf(getSymbols().getIndexEnd(),
                            idx);

                    if (endidx > idx + 1)
                    {
                        indexValue = Integer.parseInt(key.substring(idx + 1, endidx));
                        current = key.substring(0, idx);
                        result = true;
                    }
                }
            }
            catch (final NumberFormatException nfe)
            {
                result = false;
            }

            return result;
        }

        /**
         * Returns a flag whether attributes are marked the same way as normal
         * property keys. We call this the &quot;attribute emulating mode&quot;.
         * When navigating through node hierarchies it might be convenient to
         * treat attributes the same way than other child nodes, so an
         * expression engine supports to set the attribute markers to the same
         * value than the property delimiter. If this is the case, some special
         * checks have to be performed.
         *
         * @return a flag if attributes and normal property keys are treated the
         * same way
         */
        private boolean isAttributeEmulatingMode()
        {
            return getSymbols().getAttributeEnd() == null
                    && StringUtils.equals(getSymbols()
                            .getPropertyDelimiter(), getSymbols()
                            .getAttributeStart());
        }
    }
}
