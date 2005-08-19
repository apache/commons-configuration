/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>A simple class that supports creation of and iteration on complex
 * configuration keys.</p>
 *
 * <p>For key creation the class works similar to a StringBuffer: There are
 * several <code>appendXXXX()</code> methods with which single parts
 * of a key can be constructed. All these methods return a reference to the
 * actual object so they can be written in a chain. When using this methods
 * the exact syntax for keys need not be known.</p>
 *
 * <p>This class also defines a specialized iterator for configuration keys.
 * With such an iterator a key can be tokenized into its single parts. For
 * each part it can be checked whether it has an associated index.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id$
 */
public class ConfigurationKey implements Serializable
{
    /** Constant for a property delimiter.*/
    public static final char PROPERTY_DELIMITER = '.';
    
    /** Constant for an escaped delimiter. */
    public static final String ESCAPED_DELIMITER = 
        String.valueOf(PROPERTY_DELIMITER) + String.valueOf(PROPERTY_DELIMITER);

    /** Constant for an attribute start marker.*/
    private static final String ATTRIBUTE_START = "[@";

    /** Constant for an attribute end marker.*/
    private static final String ATTRIBUTE_END = "]";

    /** Constant for an index start marker.*/
    private static final char INDEX_START = '(';

    /** Constant for an index end marker.*/
    private static final char INDEX_END = ')';

    /** Constant for the initial StringBuffer size.*/
    private static final int INITIAL_SIZE = 32;

    /** Holds a buffer with the so far created key.*/
    private StringBuffer keyBuffer;

    /**
     * Creates a new, empty instance of <code>ConfigurationKey</code>.
     */
    public ConfigurationKey()
    {
        keyBuffer = new StringBuffer(INITIAL_SIZE);
    }

    /**
     * Creates a new instance of <code>ConfigurationKey</code> and
     * initializes it with the given key.
     *
     * @param key the key as a string
     */
    public ConfigurationKey(String key)
    {
        keyBuffer = new StringBuffer(key);
        removeTrailingDelimiter();
    }

    /**
     * Appends the name of a property to this key. If necessary, a
     * property delimiter will be added.
     *
     * @param property the name of the property to be added
     * @return a reference to this object
     */
    public ConfigurationKey append(String property)
    {
        if (keyBuffer.length() > 0 && !hasDelimiter() && !isAttributeKey(property))
        {
            keyBuffer.append(PROPERTY_DELIMITER);
        }

        keyBuffer.append(property);
        removeTrailingDelimiter();
        return this;
    }

    /**
     * Appends an index to this configuration key.
     *
     * @param index the index to be appended
     * @return a reference to this object
     */
    public ConfigurationKey appendIndex(int index)
    {
        keyBuffer.append(INDEX_START).append(index);
        keyBuffer.append(INDEX_END);
        return this;
    }

    /**
     * Appends an attribute to this configuration key.
     *
     * @param attr the name of the attribute to be appended
     * @return a reference to this object
     */
    public ConfigurationKey appendAttribute(String attr)
    {
        keyBuffer.append(constructAttributeKey(attr));
        return this;
    }
    
    /**
     * Checks if this key is an attribute key.
     * 
     * @return a flag if this key is an attribute key
     */
    public boolean isAttributeKey()
    {
        return isAttributeKey(keyBuffer.toString());
    }

    /**
     * Checks if the passed in key is an attribute key. Such attribute keys
     * start and end with certain marker strings. In some cases they must be
     * treated slightly different.
     *
     * @param key the key (part) to be checked
     * @return a flag if this key is an attribute key
     */
    public static boolean isAttributeKey(String key)
    {
        return key != null
        && key.startsWith(ATTRIBUTE_START)
        && key.endsWith(ATTRIBUTE_END);
    }

    /**
     * Decorates the given key so that it represents an attribute. Adds
     * special start and end markers.
     *
     * @param key the key to be decorated
     * @return the decorated attribute key
     */
    public static String constructAttributeKey(String key)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(ATTRIBUTE_START).append(key).append(ATTRIBUTE_END);
        return buf.toString();
    }

    /**
     * Extracts the name of the attribute from the given attribute key.
     * This method removes the attribute markers - if any - from the
     * specified key.
     *
     * @param key the attribute key
     * @return the name of the corresponding attribute
     */
    public static String attributeName(String key)
    {
        return isAttributeKey(key) ? removeAttributeMarkers(key) : key;
    }

    /**
     * Helper method for removing attribute markers from a key.
     *
     * @param key the key
     * @return the key with removed attribute markers
     */
    static String removeAttributeMarkers(String key)
    {
        return key.substring(ATTRIBUTE_START.length(), key.length() - ATTRIBUTE_END.length());
    }

    /**
     * Helper method that checks if the actual buffer ends with a property
     * delimiter.
     *
     * @return a flag if there is a trailing delimiter
     */
    private boolean hasDelimiter()
    {
        int count = 0;
        for (int idx = keyBuffer.length() - 1; idx >= 0
                && keyBuffer.charAt(idx) == PROPERTY_DELIMITER; idx--, count++)
            ;
        return count % 2 == 1;
    }

    /**
     * Removes a trailing delimiter if there is any.
     */
    private void removeTrailingDelimiter()
    {
        while (hasDelimiter())
        {
            keyBuffer.deleteCharAt(keyBuffer.length() - 1);
        }
    }

    /**
     * Returns a string representation of this object. This is the
     * configuration key as a plain string.
     *
     * @return a string for this object
     */
    public String toString()
    {
        return keyBuffer.toString();
    }

    /**
     * Returns an iterator for iterating over the single components of
     * this configuration key.
     *
     * @return an iterator for this key
     */
    public KeyIterator iterator()
    {
        return new KeyIterator();
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
     * some <code>append()</code> methods. The semantic is the same as
     * the <code>setLength()</code> method of <code>StringBuffer</code>.
     *
     * @param len the new length of the key
     */
    public void setLength(int len)
    {
        keyBuffer.setLength(len);
    }

    /**
     * Checks if two <code>ConfigurationKey</code> objects are equal. The
     * method can be called with strings or other objects, too.
     *
     * @param c the object to compare
     * @return a flag if both objects are equal
     */
    public boolean equals(Object c)
    {
        if (c == null)
        {
            return false;
        }

        return keyBuffer.toString().equals(c.toString());
    }

    /**
     * Returns the hash code for this object.
     *
     * @return the hash code
     */
    public int hashCode()
    {
        return keyBuffer.toString().hashCode();
    }

    /**
     * Returns a configuration key object that is initialized with the part
     * of the key that is common to this key and the passed in key.
     *
     * @param other the other key
     * @return a key object with the common key part
     */
    public ConfigurationKey commonKey(ConfigurationKey other)
    {
        if (other == null)
        {
            throw new IllegalArgumentException("Other key must no be null!");
        }

        ConfigurationKey result = new ConfigurationKey();
        KeyIterator it1 = iterator();
        KeyIterator it2 = other.iterator();

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
     * <code>other = key.commonKey(other) + key.differenceKey(other)</code>
     * for an arbitrary configuration key <code>key</code>.
     *
     * @param other the key for which the difference is to be calculated
     * @return the difference key
     */
    public ConfigurationKey differenceKey(ConfigurationKey other)
    {
        ConfigurationKey common = commonKey(other);
        ConfigurationKey result = new ConfigurationKey();

        if (common.length() < other.length())
        {
            String k = other.toString().substring(common.length());
            // skip trailing delimiters
            int i = 0;
            while (i < k.length() && k.charAt(i) == PROPERTY_DELIMITER)
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
     * Helper method for comparing two key parts.
     *
     * @param it1 the iterator with the first part
     * @param it2 the iterator with the second part
     * @return a flag if both parts are equal
     */
    private static boolean partsEqual(KeyIterator it1, KeyIterator it2)
    {
        return it1.nextKey().equals(it2.nextKey())
        && it1.getIndex() == it2.getIndex()
        && it1.isAttribute() == it2.isAttribute();
    }

    /**
     * A specialized iterator class for tokenizing a configuration key.
     * This class implements the normal iterator interface. In addition it
     * provides some specific methods for configuration keys.
     */
    public class KeyIterator implements Iterator, Cloneable
    {
        /** Stores the current key name.*/
        private String current;

        /** Stores the start index of the actual token.*/
        private int startIndex;

        /** Stores the end index of the actual token.*/
        private int endIndex;

        /** Stores the index of the actual property if there is one.*/
        private int indexValue;

        /** Stores a flag if the actual property has an index.*/
        private boolean hasIndex;

        /** Stores a flag if the actual property is an attribute.*/
        private boolean attribute;

        /**
         * Helper method for determining the next indices.
         * 
         * @return the next key part
         */
        private String findNextIndices()
        {
            startIndex = endIndex;
            // skip empty names
            while (startIndex < keyBuffer.length()
                    && keyBuffer.charAt(startIndex) == PROPERTY_DELIMITER)
            {
                startIndex++;
            }

            // Key ends with a delimiter?
            if (startIndex >= keyBuffer.length())
            {
                endIndex = keyBuffer.length();
                startIndex = endIndex - 1;
                return keyBuffer.substring(startIndex, endIndex);
            }
            else
            {
                return nextKeyPart();
            }
        }

        /**
         * Helper method for extracting the next key part. Takes escaping of
         * delimiter characters into account.
         * 
         * @return the next key part
         */
        private String nextKeyPart()
        {
            StringBuffer key = new StringBuffer(32);
            int idx = startIndex;
            int endIdx = keyBuffer.toString().indexOf(ATTRIBUTE_START,
                    startIndex);
            if (endIdx < 0 || endIdx == startIndex)
            {
                endIdx = keyBuffer.length();
            }
            boolean found = false;

            while (!found && idx < endIdx)
            {
                char c = keyBuffer.charAt(idx);
                if (c == PROPERTY_DELIMITER)
                {
                    // a duplicated delimiter means escaping
                    if (idx == endIdx - 1
                            || keyBuffer.charAt(idx + 1) != PROPERTY_DELIMITER)
                    {
                        found = true;
                    }
                    else
                    {
                        idx++;
                    }
                }
                if (!found)
                {
                    key.append(c);
                    idx++;
                }
            }

            endIndex = idx;
            return key.toString();
        }

        /**
         * Returns the next key part of this configuration key. This is a short
         * form of <code>nextKey(false)</code>.
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
        public String nextKey(boolean decorated)
        {
            if (!hasNext())
            {
                throw new NoSuchElementException("No more key parts!");
            }

            hasIndex = false;
            indexValue = -1;
            String key = findNextIndices();

            attribute = checkAttribute(key);
            if (!attribute)
            {
                hasIndex = checkIndex(key);
                if (!hasIndex)
                {
                    current = key;
                }
            }

            return currentKey(decorated);
        }

        /**
         * Helper method for checking if the passed key is an attribute.
         * If this is the case, the internal fields will be set.
         *
         * @param key the key to be checked
         * @return a flag if the key is an attribute
         */
        private boolean checkAttribute(String key)
        {
            if (isAttributeKey(key))
            {
                current = removeAttributeMarkers(key);
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * Helper method for checking if the passed key contains an index.
         * If this is the case, internal fields will be set.
         *
         * @param key the key to be checked
         * @return a flag if an index is defined
         */
        private boolean checkIndex(String key)
        {
            boolean result = false;

            int idx = key.lastIndexOf(INDEX_START);
            if (idx > 0)
            {
                int endidx = key.indexOf(INDEX_END, idx);

                if (endidx > idx + 1)
                {
                    indexValue = Integer.parseInt(key.substring(idx + 1, endidx));
                    current = key.substring(0, idx);
                    result = true;
                }
            }

            return result;
        }

        /**
         * Checks if there is a next element.
         *
         * @return a flag if there is a next element
         */
        public boolean hasNext()
        {
            return endIndex < keyBuffer.length();
        }

        /**
         * Returns the next object in the iteration.
         *
         * @return the next object
         */
        public Object next()
        {
            return nextKey();
        }

        /**
         * Removes the current object in the iteration. This method is not
         * supported by this iterator type, so an exception is thrown.
         */
        public void remove()
        {
            throw new UnsupportedOperationException("Remove not supported!");
        }

        /**
         * Returns the current key of the iteration (without skipping to the
         * next element). This is the same key the previous <code>next()</code>
         * call had returned. (Short form of <code>currentKey(false)</code>.
         *
         * @return the current key
         */
        public String currentKey()
        {
            return currentKey(false);
        }

        /**
         * Returns the current key of the iteration (without skipping to the
         * next element). The boolean parameter indicates wheter a decorated
         * key should be returned. This affects only attribute keys: if the
         * parameter is <b>false</b>, the attribute markers are stripped from
         * the key; if it is <b>true</b>, they remain.
         *
         * @param decorated a flag if the decorated key is to be returned
         * @return the current key
         */
        public String currentKey(boolean decorated)
        {
            return (decorated && isAttribute()) ? constructAttributeKey(current) : current;
        }

        /**
         * Returns a flag if the current key is an attribute. This method can
         * be called after <code>next()</code>.
         *
         * @return a flag if the current key is an attribute
         */
        public boolean isAttribute()
        {
            return attribute;
        }

        /**
         * Returns the index value of the current key. If the current key does
         * not have an index, return value is -1. This method can be called
         * after <code>next()</code>.
         *
         * @return the index value of the current key
         */
        public int getIndex()
        {
            return indexValue;
        }

        /**
         * Returns a flag if the current key has an associated index.
         * This method can be called after <code>next()</code>.
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
        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException cex)
            {
                // should not happen
                return null;
            }
        }
    }
}
