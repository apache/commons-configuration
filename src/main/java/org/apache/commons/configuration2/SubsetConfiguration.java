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

import org.apache.commons.configuration2.convert.ListDelimiterHandler;

/**
 * <p>A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.</p>
 * <p>It is usually not necessary to use this class directly. Instead the
 * {@link Configuration#subset(String)} method should be used,
 * which will return a correctly initialized instance.</p>
 *
 * @author Emmanuel Bourg
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
     * @param parent The parent configuration (must not be <b>null</b>)
     * @param prefix The prefix used to select the properties
     * @throws IllegalArgumentException if the parent configuration is <b>null</b>
     */
    public SubsetConfiguration(final Configuration parent, final String prefix)
    {
        this(parent, prefix, null);
    }

    /**
     * Create a subset of the specified configuration
     *
     * @param parent The parent configuration (must not be <b>null</b>)
     * @param prefix    The prefix used to select the properties
     * @param delimiter The prefix delimiter
     * @throws IllegalArgumentException if the parent configuration is <b>null</b>
     */
    public SubsetConfiguration(final Configuration parent, final String prefix, final String delimiter)
    {
        if (parent == null)
        {
            throw new IllegalArgumentException(
                    "Parent configuration must not be null!");
        }

        this.parent = parent;
        this.prefix = prefix;
        this.delimiter = delimiter;
        initInterpolator();
    }

    /**
     * Return the key in the parent configuration associated to the specified
     * key in this subset.
     *
     * @param key The key in the subset.
     * @return the key as to be used by the parent
     */
    protected String getParentKey(final String key)
    {
        if ("".equals(key) || key == null)
        {
            return prefix;
        }
        return delimiter == null ? prefix + key : prefix + delimiter + key;
    }

    /**
     * Return the key in the subset configuration associated to the specified
     * key in the parent configuration.
     *
     * @param key The key in the parent configuration.
     * @return the key in the context of this subset configuration
     */
    protected String getChildKey(final String key)
    {
        if (!key.startsWith(prefix))
        {
            throw new IllegalArgumentException("The parent key '" + key + "' is not in the subset.");
        }
        String modifiedKey = null;
        if (key.length() == prefix.length())
        {
            modifiedKey = "";
        }
        else
        {
            final int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
            modifiedKey = key.substring(i);
        }

        return modifiedKey;
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
    public void setPrefix(final String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public Configuration subset(final String prefix)
    {
        return parent.subset(getParentKey(prefix));
    }

    @Override
    protected boolean isEmptyInternal()
    {
        return !getKeysInternal().hasNext();
    }

    @Override
    protected boolean containsKeyInternal(final String key)
    {
        return parent.containsKey(getParentKey(key));
    }

    @Override
    public void addPropertyDirect(final String key, final Object value)
    {
        parent.addProperty(getParentKey(key), value);
    }

    @Override
    protected void clearPropertyDirect(final String key)
    {
        parent.clearProperty(getParentKey(key));
    }

    @Override
    protected Object getPropertyInternal(final String key)
    {
        return parent.getProperty(getParentKey(key));
    }

    @Override
    protected Iterator<String> getKeysInternal(final String prefix)
    {
        return new SubsetIterator(parent.getKeys(getParentKey(prefix)));
    }

    @Override
    protected Iterator<String> getKeysInternal()
    {
        return new SubsetIterator(parent.getKeys(prefix));
    }

    /**
     * {@inheritDoc}
     *
     * Change the behavior of the parent configuration if it supports this feature.
     */
    @Override
    public void setThrowExceptionOnMissing(final boolean throwExceptionOnMissing)
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
        return super.isThrowExceptionOnMissing();
    }

    /**
     * {@inheritDoc} If the parent configuration extends
     * {@link AbstractConfiguration}, the list delimiter handler is obtained
     * from there.
     */
    @Override
    public ListDelimiterHandler getListDelimiterHandler()
    {
        return (parent instanceof AbstractConfiguration) ? ((AbstractConfiguration) parent)
                .getListDelimiterHandler() : super.getListDelimiterHandler();
    }

    /**
     * {@inheritDoc} If the parent configuration extends
     * {@link AbstractConfiguration}, the list delimiter handler is passed to
     * the parent.
     */
    @Override
    public void setListDelimiterHandler(
            final ListDelimiterHandler listDelimiterHandler)
    {
        if (parent instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) parent)
                    .setListDelimiterHandler(listDelimiterHandler);
        }
        else
        {
            super.setListDelimiterHandler(listDelimiterHandler);
        }
    }

    /**
     * Initializes the {@code ConfigurationInterpolator} for this sub configuration.
     * This is a standard {@code ConfigurationInterpolator} which also references
     * the {@code ConfigurationInterpolator} of the parent configuration.
     */
    private void initInterpolator()
    {
        getInterpolator().setParentInterpolator(getParent().getInterpolator());
    }

    /**
     * A specialized iterator to be returned by the {@code getKeys()}
     * methods. This implementation wraps an iterator from the parent
     * configuration. The keys returned by this iterator are correspondingly
     * transformed.
     */
    private class SubsetIterator implements Iterator<String>
    {
        /** Stores the wrapped iterator. */
        private final Iterator<String> parentIterator;

        /**
         * Creates a new instance of {@code SubsetIterator} and
         * initializes it with the parent iterator.
         *
         * @param it the iterator of the parent configuration
         */
        public SubsetIterator(final Iterator<String> it)
        {
            parentIterator = it;
        }

        /**
         * Checks whether there are more elements. Delegates to the parent
         * iterator.
         *
         * @return a flag whether there are more elements
         */
        @Override
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
        @Override
        public String next()
        {
            return getChildKey(parentIterator.next());
        }

        /**
         * Removes the current element from the iteration. Delegates to the
         * parent iterator.
         */
        @Override
        public void remove()
        {
            parentIterator.remove();
        }
    }
}
