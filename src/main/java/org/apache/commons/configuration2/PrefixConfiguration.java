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

public class PrefixConfiguration extends AbstractConfiguration {
    /** The parent configuration. */
    protected Configuration child;

    /** The prefix used to select the properties. */
    protected String prefix;

    /** The prefix delimiter */
    protected String delimiter;

    public PrefixConfiguration(Configuration child, String prefix) {
        this(child, prefix, null);
    }

    public PrefixConfiguration(Configuration child, String prefix, String delimiter) {
        if (child == null) {
            throw new IllegalArgumentException("Child configuration must not be null!");
        }

        this.child = child;
        this.prefix = prefix;
        this.delimiter = delimiter;
        initInterpolator();
    }

    /**
     * Initializes the {@code ConfigurationInterpolator} for this sub configuration.
     * This is a standard {@code ConfigurationInterpolator} which also references
     * the {@code ConfigurationInterpolator} of the parent configuration.
     */
    private void initInterpolator() {
        getInterpolator().setParentInterpolator(getChild().getInterpolator());
    }

    /**
     * Return the key in the child configuration associated to the specified key in
     * the configuration.
     *
     * @param key The key in the configuration.
     * @return the key in the context of this child configuration
     */
    protected String getChildKey(final String key) {
        if (key.startsWith(prefix)) {
            String modifiedKey = null;
            if (key.length() == prefix.length()) {
                modifiedKey = "";
            } else {
                final int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        } else {
            return key;
        }
    }

    /**
     * Return the key in the configuration associated to the specified key in this
     * child.
     *
     * @param key The key in the child.
     * @return the key as to be used by the configuration
     */
    protected String getParentKey(final String key) {
        if ("".equals(key) || key == null) {
            return prefix;
        }
        return delimiter == null ? prefix + key : prefix + delimiter + key;
    }

    /**
     * Return the child configuration for this configuration.
     *
     * @return the child configuration
     */
    public Configuration getChild() {
        return child;
    }

    /**
     * Return the prefix used to select the properties in the child configuration.
     *
     * @return the prefix used by this configuration
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix used to select the properties in the child configuration.
     *
     * @param prefix the prefix
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void addPropertyDirect(String key, Object value) {
        this.child.addProperty(this.getChildKey(key), value);
    }

    @Override
    protected void clearPropertyDirect(String key) {
        this.child.clearProperty(this.getChildKey(key));
    }

    @Override
    protected Iterator<String> getKeysInternal() {
        return new PrefixIterator(this.child.getKeys());
    }

    @Override
    protected Object getPropertyInternal(String key) {
        return this.child.getProperty(this.getChildKey(key));
    }

    @Override
    protected boolean isEmptyInternal() {
        return this.child.isEmpty();
    }

    @Override
    protected boolean containsKeyInternal(String key) {
        return this.child.containsKey(this.getChildKey(key));
    }

    /**
     * {@inheritDoc}
     *
     * Change the behavior of the child configuration if it supports this feature.
     */
    @Override
    public void setThrowExceptionOnMissing(final boolean throwExceptionOnMissing) {
        if (child instanceof AbstractConfiguration) {
            ((AbstractConfiguration) child).setThrowExceptionOnMissing(throwExceptionOnMissing);
        } else {
            super.setThrowExceptionOnMissing(throwExceptionOnMissing);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The prefix inherits this feature from its child if it supports this feature.
     */
    @Override
    public boolean isThrowExceptionOnMissing() {
        if (child instanceof AbstractConfiguration) {
            return ((AbstractConfiguration) child).isThrowExceptionOnMissing();
        }
        return super.isThrowExceptionOnMissing();
    }

    /**
     * {@inheritDoc} If the child configuration extends
     * {@link AbstractConfiguration}, the list delimiter handler is obtained from
     * there.
     */
    @Override
    public ListDelimiterHandler getListDelimiterHandler() {
        return child instanceof AbstractConfiguration ? ((AbstractConfiguration) child).getListDelimiterHandler()
                : super.getListDelimiterHandler();
    }

    /**
     * {@inheritDoc} If the child configuration extends
     * {@link AbstractConfiguration}, the list delimiter handler is passed to the
     * child.
     */
    @Override
    public void setListDelimiterHandler(final ListDelimiterHandler listDelimiterHandler) {
        if (child instanceof AbstractConfiguration) {
            ((AbstractConfiguration) child).setListDelimiterHandler(listDelimiterHandler);
        } else {
            super.setListDelimiterHandler(listDelimiterHandler);
        }
    }

    private class PrefixIterator implements Iterator<String> {
        /** Stores the wrapped iterator. */
        private final Iterator<String> parentIterator;

        /**
         * Creates a new instance of {@code SubsetIterator} and initializes it with the
         * parent iterator.
         *
         * @param it the iterator of the parent configuration
         */
        public PrefixIterator(final Iterator<String> it) {
            parentIterator = it;
        }

        /**
         * Checks whether there are more elements. Delegates to the parent iterator.
         *
         * @return a flag whether there are more elements
         */
        @Override
        public boolean hasNext() {
            return parentIterator.hasNext();
        }

        /**
         * Returns the next element in the iteration. This is the next key from the
         * parent configuration, transformed to correspond to the point of view of this
         * subset configuration.
         *
         * @return the next element
         */
        @Override
        public String next() {
            return PrefixConfiguration.this.getParentKey(parentIterator.next());
        }

        /**
         * Removes the current element from the iteration. Delegates to the parent
         * iterator.
         */
        @Override
        public void remove() {
            parentIterator.remove();
        }
    }
}
