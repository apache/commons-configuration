/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.util.Iterator;

import junit.framework.TestCase;

public class TestNullJNDIEnvironmentValues extends TestCase
{
    private JNDIConfiguration conf = null;

    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", TestJNDIConfiguration.CONTEXT_FACTORY);
        
        conf = new JNDIConfiguration();
        conf.setThrowExceptionOnMissing(false);
    }

    public void testThrowExceptionOnMissing()
    {
        assertFalse("Throw Exception Property is set!", conf.isThrowExceptionOnMissing());
    }

    public void testSimpleGet() throws Exception
    {
        String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
    }

    public void testMoreGets() throws Exception
    {
        String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
        assertEquals("jndivalue2", conf.getString("test.key2"));
        assertEquals(1, conf.getShort("test.short"));
    }

    public void testGetMissingKey() throws Exception
    {
        assertNull("Missing Key is not null!", conf.getString("test.imaginarykey"));
    }

    public void testGetMissingKeyWithDefault() throws Exception
    {
        String result = conf.getString("test.imaginarykey", "bob");
        assertEquals("bob", result);
    }

    public void testContainsKey() throws Exception
    {
        assertTrue(conf.containsKey("test.key"));
        assertTrue(!conf.containsKey("test.imaginerykey"));
    }
    
    public void testClearProperty()
    {
        assertNotNull("null short for the 'test.short' key", conf.getShort("test.short", null));
        conf.clearProperty("test.short");
        assertNull("'test.short' property not cleared", conf.getShort("test.short", null));
    }
    
    public void testIsEmpty()
    {
        assertFalse("the configuration shouldn't be empty", conf.isEmpty());
    }
    
    public void testGetKeys() throws Exception
    {
        boolean found = false;
        Iterator it = conf.getKeys();

        assertTrue("no key found", it.hasNext());

        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

    public void testGetKeysWithUnknownPrefix()
    {
        // test for a unknown prefix
        Iterator it = conf.getKeys("foo.bar");
        assertFalse("no key should be found", it.hasNext());
    }

    public void testGetKeysWithExistingPrefix()
    {
        // test for an existing prefix
        Iterator it = conf.getKeys("test");
        boolean found = false;
        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

    public void testGetKeysWithKeyAsPrefix()
    {
        // test for a prefix matching exactly the key of a property
        Iterator it = conf.getKeys("test.boolean");
        boolean found = false;
        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

}
