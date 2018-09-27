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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class TestNullJNDIEnvironmentValues
{
    private JNDIConfiguration conf = null;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", TestJNDIConfiguration.CONTEXT_FACTORY);

        conf = new JNDIConfiguration();
        conf.setThrowExceptionOnMissing(false);
    }

    @Test
    public void testThrowExceptionOnMissing()
    {
        assertFalse("Throw Exception Property is set!", conf.isThrowExceptionOnMissing());
    }

    @Test
    public void testSimpleGet() throws Exception
    {
        final String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
    }

    @Test
    public void testMoreGets() throws Exception
    {
        final String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
        assertEquals("jndivalue2", conf.getString("test.key2"));
        assertEquals(1, conf.getShort("test.short"));
    }

    @Test
    public void testGetMissingKey() throws Exception
    {
        assertNull("Missing Key is not null!", conf.getString("test.imaginarykey"));
    }

    @Test
    public void testGetMissingKeyWithDefault() throws Exception
    {
        final String result = conf.getString("test.imaginarykey", "bob");
        assertEquals("bob", result);
    }

    @Test
    public void testContainsKey() throws Exception
    {
        assertTrue(conf.containsKey("test.key"));
        assertTrue(!conf.containsKey("test.imaginarykey"));
    }

    @Test
    public void testClearProperty()
    {
        assertNotNull("null short for the 'test.short' key", conf.getShort("test.short", null));
        conf.clearProperty("test.short");
        assertNull("'test.short' property not cleared", conf.getShort("test.short", null));
    }

    @Test
    public void testIsEmpty()
    {
        assertFalse("the configuration shouldn't be empty", conf.isEmpty());
    }

    @Test
    public void testGetKeys() throws Exception
    {
        boolean found = false;
        final Iterator<String> it = conf.getKeys();

        assertTrue("no key found", it.hasNext());

        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

    @Test
    public void testGetKeysWithUnknownPrefix()
    {
        // test for a unknown prefix
        final Iterator<String> it = conf.getKeys("foo.bar");
        assertFalse("no key should be found", it.hasNext());
    }

    @Test
    public void testGetKeysWithExistingPrefix()
    {
        // test for an existing prefix
        final Iterator<String> it = conf.getKeys("test");
        boolean found = false;
        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

    @Test
    public void testGetKeysWithKeyAsPrefix()
    {
        // test for a prefix matching exactly the key of a property
        final Iterator<String> it = conf.getKeys("test.boolean");
        boolean found = false;
        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }
}
