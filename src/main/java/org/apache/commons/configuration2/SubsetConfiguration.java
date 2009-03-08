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

package org.apache.commons.configuration2;

import java.util.Iterator;

/**
 * <p>A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.</p>
 * <p>It is usually not necessary to use this class directly. Instead the
 * <code>{@link Configuration#subset(String)}</code> method should be used,
 * which will return a correctly initialized instance.</p>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class SubsetConfiguration extends AbstractConfiguration
{
    /** The parent configuration. */
    protected Configuration parent;

    /** The prefix used to select the properties. */
    protected String prefix;

    /** The prefix delimiter */
    protected String delimiter;

    /**
     * Create a subset of the specified configuration
     *
     * @param parent The parent configuration
     * @param prefix The prefix used to select the properties
     */
    public SubsetConfiguration(Configuration parent, String prefix)
    {
        this.parent = parent;
        this.prefix = prefix;
    }

    /**
     * Create a subset of the specified configuration
     *
     * @param parent    The parent configuration
     * @param prefix    The prefix used to select the properties
     * @param delimiter The prefix delimiter
     */
    public SubsetConfiguration(Configuration parent, String prefix, String delimiter)
    {
        this.parent = parent;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    /**
     * Return the key in the parent configuration associated to the specified
     * key in this subset.
     *
     * @param key The key in the subset.
     * @return the key as to be used by the parent
     */
    protected String getParentKey(String key)
    {
        if ("".equals(key) || key == null)
        {
            return prefix;
        }
        else
        {
            return delimiter == null ? prefix + key : prefix + delimiter + key;
        }
    }

    /**
     * Return the key in the subset configuration associated to the specified
     * key in the parent configuration.
     *
     * @param key The key in the parent configuration.
     * @return the key in the context of this subset configuration
     */
    protected String getChildKey(String key)
    {
        if (!key.startsWith(prefix))
        {
            throw new IllegalArgumentException("The parent key '" + key + "' is not in the subset.");
        }
        else
        {
            String modifiedKey = null;
            if (key.length() == prefix.length())
            {
                modifiedKey = "";
            }
            else
            {
                int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        }
    }

    /**
     * Return the parent configuration for this subset.
     *
     * @return the parent configuration
     */
    public Configuration getParent()
    {
        return parent;
    }

    /**
     * Return the prefix used to select the properties in the parent configuration.
     *
     * @return the prefix used by this subset
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Set the prefix used to select the properties in the parent configuration.
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public Configuration subset(String prefix)
    {
        return parent.subset(getParentKey(prefix));
    }

    public boolean isEmpty()
    {
        return !getKeys().hasNext();
    }

    public boolean containsKey(String key)
    {
        return parent.containsKey(getParentKey(key));
    }

    @Override
    public void addPropertyDirect(String key, Object value)
    {
        parent.addProperty(getParentKey(key), value);
    }

    @Override
    public void setProperty(String key, Object value)
    {
        parent.setProperty(getParentKey(key), value);
    }

    @Override
    public void clearProperty(String key)
    {
        parent.clearProperty(getParentKey(key));
    }

    public Object getProperty(String key)
    {
        return parent.getProperty(getParentKey(key));
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return new SubsetIterator(parent.getKeys(getParentKey(prefix)));
    }

    public Iterator<String> getKeys()
    {
        return new SubsetIterator(parent.getKeys(prefix));
    }

    @Override
    protected Object interpolate(Object base)
    {
        if (delimiter == null && "".equals(prefix))
        {
            return super.interpolate(base);
        }
        else
        {
            SubsetConfiguration config = new SubsetConfiguration(parent, "");
            return config.interpolate(base);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Change the behaviour of the parent configuration if it supports this feature.
     */
    @Override
    public void setThrowExceptionOnMissing(boolean throwExceptionOnMissing)
    {
        if (parent instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) parent).setThrowExceptionOnMissing(throwExceptionOnMissing);
        }
        else
        {
            super.setThrowExceptionOnMissing(throwExceptionOnMissing);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The subset inherits this feature from its parent if it supports this feature.
     */
    @Override
    public boolean isThrowExceptionOnMissing()
    {
        if (parent instanceof AbstractConfiguration)
        {
            return ((AbstractConfiguration) parent).isThrowExceptionOnMissing();
        }
        else
        {
            return super.isThrowExceptionOnMissing();
        }
    }

    /**
     * Returns the list delimiter. This property will be fetched from the parent
     * configuration if supported.
     *
     * @return the list delimiter
     * @since 1.4
     */
    @Override
    public char getListDelimiter()
    {
        return (parent instanceof AbstractConfiguration) ? ((AbstractConfiguration) parent)
                .getListDelimiter()
                : super.getListDelimiter();
    }

    /**
     * Sets the list delimiter. If the parent configuration supports this
     * feature, the delimiter will be set at the parent.
     *
     * @param delim the new list delimiter
     * @since 1.4
     */
    @Override
    public void setListDelimiter(char delim)
    {
        if (parent instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) parent).setListDelimiter(delim);
        }
        else
        {
            super.setListDelimiter(delim);
        }
    }

    /**
     * Returns a flag whether string properties should be checked for list
     * delimiter characters. This implementation ensures that this flag is kept
     * in sync with the parent configuration if this object supports this
     * feature.
     *
     * @return the delimiter parsing disabled flag
     * @since 1.4
     */
    @Override
    public boolean isDelimiterParsingDisabled()
    {
        return (parent instanceof AbstractConfiguration) ? ((AbstractConfiguration) parent)
                .isDelimiterParsingDisabled()
                : super.isDelimiterParsingDisabled();
    }

    /**
     * Sets a flag whether list parsing is disabled. This implementation will
     * also set the flag at the parent configuration if this object supports
     * this feature.
     *
     * @param delimiterParsingDisabled the delimiter parsing disabled flag
     * @since 1.4
     */
    @Override
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled)
    {
        if (parent instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) parent)
                    .setDelimiterParsingDisabled(delimiterParsingDisabled);
        }
        else
        {
            super.setDelimiterParsingDisabled(delimiterParsingDisabled);
        }
    }

    /**
     * A specialized iterator to be returned by the <code>getKeys()</code>
     * methods. This implementation wraps an iterator from the parent
     * configuration. The keys returned by this iterator are correspondigly
     * transformed.
     */
    private class SubsetIterator implements Iterator<String>
    {
        /** Stores the wrapped iterator. */
        private final Iterator<String> parentIterator;

        /**
         * Creates a new instance of <code>SubsetIterator</code> and
         * initializes it with the parent iterator.
         *
         * @param it the iterator of the parent configuration
         */
        public SubsetIterator(Iterator<String> it)
        {
            parentIterator = it;
        }

        /**
         * Checks whether there are more elements. Delegates to the parent
         * iterator.
         *
         * @return a flag whether there are more elements
         */
        public boolean hasNext()
        {
            return parentIterator.hasNext();
        }

        /**
         * Returns the next element in the iteration. This is the next key from
         * the parent configuration, transformed to correspond to the point of
         * view of this subset configuration.
         *
         * @return the next element
         */
        public String next()
        {
            return getChildKey(parentIterator.next());
        }

        /**
         * Removes the current element from the iteration. Delegates to the
         * parent iterator.
         */
        public void remove()
        {
            parentIterator.remove();
        }
    }
}
