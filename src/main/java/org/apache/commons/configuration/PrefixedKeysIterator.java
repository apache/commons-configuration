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

package org.apache.commons.configuration;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * * A specialized iterator implementation used by <code>{@link AbstractConfiguration}</code>
 * to return an iteration over all keys starting with a specified prefix.
 * 
 * <p>This class is basically a stripped-down version of the
 * <code>FilterIterator</code> class of Commons Collections</p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 */
class PrefixedKeysIterator implements Iterator
{
    /** Stores the wrapped iterator. */
    private final Iterator iterator;

    /** Stores the prefix. */
    private final String prefix;

    /** Stores the next element in the iteration. */
    private String nextElement;

    /** A flag whether the next element has been calculated. */
    private boolean nextElementSet;

    /**
     * Creates a new instance of <code>PrefixedKeysIterator</code> and sets
     * the wrapped iterator and the prefix for the accepted keys.
     *
     * @param wrappedIterator the wrapped iterator
     * @param keyPrefix the prefix of the allowed keys
     */
    public PrefixedKeysIterator(Iterator wrappedIterator, String keyPrefix)
    {
        iterator = wrappedIterator;
        prefix = keyPrefix;
    }

    /**
     * Returns a flag whether there are more elements in the iteration.
     *
     * @return a flag if there is a next element
     */
    public boolean hasNext()
    {
        return nextElementSet || setNextElement();
    }

    /**
     * Returns the next element in the iteration. This is the next key that
     * matches the specified prefix.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if there is no next element
     */
    public Object next()
    {
        if (!nextElementSet && !setNextElement())
        {
            throw new NoSuchElementException();
        }
        nextElementSet = false;
        return nextElement;
    }

    /**
     * Removes from the underlying collection of the base iterator the last
     * element returned by this iterator. This method can only be called if
     * <code>next()</code> was called, but not after <code>hasNext()</code>,
     * because the <code>hasNext()</code> call changes the base iterator.
     *
     * @throws IllegalStateException if <code>hasNext()</code> has already
     *         been called.
     */
    public void remove()
    {
        if (nextElementSet)
        {
            throw new IllegalStateException("remove() cannot be called");
        }
        iterator.remove();
    }

    /**
     * Determines the next element in the iteration. The return value indicates
     * whether such an element can be found.
     *
     * @return a flag whether a next element exists
     */
    private boolean setNextElement()
    {
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            if (key.startsWith(prefix + ".") || key.equals(prefix))
            {
                nextElement = key;
                nextElementSet = true;
                return true;
            }
        }
        return false;
    }
}
