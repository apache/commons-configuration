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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * Test class for PrefixedKeysIterator.
 *
 * @version $Id$
 */
public class TestPrefixedKeysIterator extends TestCase
{
    /** Constant for the used prefix. */
    private static final String PREFIX = "test";

    /** Constant for the prefix of the valid keys. */
    private static final String KEY = ".key";

    /** An array with test keys. */
    private static final String[] TEST_KEYS = {
            PREFIX + KEY + "1", PREFIX + "AnotherKey", PREFIX + KEY + "2",
            "differentKey", PREFIX + KEY + "3", PREFIX + KEY + "4"
    };

    /** Constant for the number of keys in the iteration. */
    private static final int KEY_COUNT = 4;

    /**
     * A collection that stores test keys. Used for constructing the wrapped
     * iterator.
     */
    private List<String> keyList;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        keyList = new ArrayList<String>(Arrays.asList(TEST_KEYS));
    }

    /**
     * Creates a test iterator using the list with the test keys.
     *
     * @return the test iterator
     */
    private PrefixedKeysIterator setUpIterator()
    {
        return new PrefixedKeysIterator(keyList.iterator(), PREFIX);
    }

    /**
     * Tests whether the iterator returns the expected element.
     *
     * @param it the iterator
     * @param expected the index of the expected key
     */
    private static void checkNext(PrefixedKeysIterator it, int expected)
    {
        assertEquals("Wrong key", PREFIX + KEY + expected, it.next());
    }

    /**
     * Tests obtaining the next element.
     */
    public void testNext()
    {
        checkNext(setUpIterator(), 1);
    }

    /**
     * Tests the next() method after calling hasNext().
     */
    public void testNextAfterHasNext()
    {
        PrefixedKeysIterator it = setUpIterator();
        assertTrue("Iteration is empty", it.hasNext());
        checkNext(it, 1);
    }

    /**
     * Tests calling hasNext() multiple times.
     */
    public void testHasNextCached()
    {
        PrefixedKeysIterator it = setUpIterator();
        assertTrue("Iteration is empty", it.hasNext());
        assertTrue("Iteration is empty (2)", it.hasNext());
    }

    /**
     * Tests a default iteration.
     */
    public void testIteration()
    {
        PrefixedKeysIterator it = setUpIterator();
        int count = 0;

        while (it.hasNext())
        {
            checkNext(it, ++count);
        }
        assertEquals("Wrong number of elements", KEY_COUNT, count);
    }

    /**
     * Tests hasNext() for an empty iteration.
     */
    public void testHasNextEmpty()
    {
        keyList.clear();
        PrefixedKeysIterator it = setUpIterator();
        assertFalse("Found elements", it.hasNext());
    }

    /**
     * Tests calling next() after the end of the iteration.
     */
    public void testNextAfterEndOfIteration()
    {
        PrefixedKeysIterator it = setUpIterator();
        int count = 0;

        try
        {
            while (true)
            {
                it.next();
                count++;
            }
        }
        catch (NoSuchElementException nsex)
        {
            assertEquals("Wrong number of elements", KEY_COUNT, count);
        }
    }

    /**
     * Tests calling next() for an empty iteration.
     */
    public void testNextEmpty()
    {
        keyList.clear();
        PrefixedKeysIterator it = setUpIterator();
        try
        {
            it.next();
            fail("Could obtain element from empty iteration!");
        }
        catch (NoSuchElementException nsex)
        {
            // ok
        }
    }

    /**
     * Tests whether a key that matches exactly the prefix is allowed.
     */
    public void testNextPrefix()
    {
        keyList.add(PREFIX);
        PrefixedKeysIterator it = setUpIterator();
        for (int i = 1; i <= KEY_COUNT; i++)
        {
            checkNext(it, i);
        }
        assertTrue("No more elements found", it.hasNext());
        assertEquals("Prefix key not found", PREFIX, it.next());
        assertFalse("More elements found", it.hasNext());
    }

    /**
     * Tests removing an element.
     */
    public void testRemove()
    {
        PrefixedKeysIterator it = setUpIterator();
        checkNext(it, 1);
        it.remove();
        assertFalse("Element not removed", keyList.contains(PREFIX + KEY + "1"));
    }

    /**
     * Tests calling remove() when this is not allowed.
     */
    public void testRemoveInvalid()
    {
        PrefixedKeysIterator it = setUpIterator();
        it.next();
        it.hasNext();
        try
        {
            it.remove();
            fail("Could call remove() without a current object!");
        }
        catch (IllegalStateException istex)
        {
            // ok
        }
    }
}
