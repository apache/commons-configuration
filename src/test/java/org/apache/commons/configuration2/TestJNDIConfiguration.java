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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

/**
 * Test to see if the JNDIConfiguration works properly.
 *
 * @version $Id$
 */
public class TestJNDIConfiguration extends TestCase
{
    public static final String CONTEXT_FACTORY = MockInitialContextFactory.class.getName();

    private JNDIConfiguration conf;
    private NonStringTestHolder nonStringTestHolder;

    /** A test error listener for counting internal errors.*/
    private ConfigurationErrorListenerImpl listener;

    @Override
    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", CONTEXT_FACTORY);

        conf = new JNDIConfiguration();

        nonStringTestHolder = new NonStringTestHolder();
        nonStringTestHolder.setConfiguration(conf);

        listener = new ConfigurationErrorListenerImpl();
        conf.addErrorListener(listener);
    }

    /**
     * Clears the test environment. If an error listener is defined, checks
     * whether no error event was received.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (listener != null)
        {
            listener.verify();
        }
        super.tearDown();
    }

    public void testBoolean() throws Exception
    {
        nonStringTestHolder.testBoolean();
    }

    public void testBooleanDefaultValue() throws Exception
    {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    public void testByte() throws Exception
    {
        nonStringTestHolder.testByte();
    }

    public void testDouble() throws Exception
    {
        nonStringTestHolder.testDouble();
    }

    public void testDoubleDefaultValue() throws Exception
    {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    public void testFloat() throws Exception
    {
        nonStringTestHolder.testFloat();
    }

    public void testFloatDefaultValue() throws Exception
    {
        nonStringTestHolder.testFloatDefaultValue();
    }

    public void testInteger() throws Exception
    {
        nonStringTestHolder.testInteger();
    }

    public void testIntegerDefaultValue() throws Exception
    {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    public void testLong() throws Exception
    {
        nonStringTestHolder.testLong();
    }

    public void testLongDefaultValue() throws Exception
    {
        nonStringTestHolder.testLongDefaultValue();
    }

    public void testShort() throws Exception
    {
        nonStringTestHolder.testShort();
    }

    public void testShortDefaultValue() throws Exception
    {
        nonStringTestHolder.testShortDefaultValue();
    }

    public void testListMissing() throws Exception
    {
        nonStringTestHolder.testListMissing();
    }

    public void testSubset() throws Exception
    {
        nonStringTestHolder.testSubset();
    }

    public void testSimpleGet() throws Exception
    {
        String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
    }

    public void testMoreGets() throws Exception
    {
        assertEquals("jndivalue", conf.getString("test.key"));
        assertEquals("jndivalue2", conf.getString("test.key2"));
    }

    public void testGetMissingKey() throws Exception
    {
        try
        {
            conf.setThrowExceptionOnMissing(true);
            conf.getString("test.imaginarykey");
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("test.imaginarykey"));
        }
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

        conf.clearProperty("test");

        assertTrue("the configuration should be empty", conf.isEmpty());
    }

    public void testGetKeys() throws Exception
    {
        boolean found = false;
        Iterator<String> it = conf.getKeys();

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
        Iterator<String> it = conf.getKeys("foo.bar");
        assertFalse("no key should be found", it.hasNext());
        listener.verify();
    }

    public void testGetKeysWithExistingPrefix()
    {
        // test for an existing prefix
        Iterator<String> it = conf.getKeys("test");
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
        // todo fails due to CONFIGURATION-321
        /*
        Iterator it = conf.getKeys("test.boolean");
        boolean found = false;
        while (it.hasNext() && !found)
        {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
        */
    }

    public void testGetProperty() throws Exception {
        Object o = conf.getProperty("test.boolean");
        assertNotNull(o);
        assertEquals("true", o.toString());
    }

    public void testContainsKey()
    {
        String key = "test.boolean";
        assertTrue("'" + key + "' not found", conf.containsKey(key));

        conf.clearProperty(key);
        assertFalse("'" + key + "' still found", conf.containsKey(key));

        assertTrue(conf.containsKey("test.key"));
        assertFalse(conf.containsKey("test.imaginarykey"));
    }

    public void testSetProperty()
    {
        conf.setProperty("test.new.value", "foo");

        assertEquals("test.new.value", "foo", conf.getProperty("test.new.value"));
    }

    public void testReplaceProperty()
    {
        conf.setProperty("test.foo", "bar");
        assertEquals("test.foo", "bar", conf.getProperty("test.foo"));

        conf.setProperty("test.foo", "baz");
        assertEquals("test.foo", "baz", conf.getProperty("test.foo"));
    }

    public void testOverwriteProperty()
    {
        conf.setProperty("test.foo", "value1");
        assertEquals("test.foo", "value1", conf.getProperty("test.foo"));

        conf.setProperty("test.foo.bar", "value2");
        assertEquals("test.foo.bar", "value2", conf.getProperty("test.foo.bar"));
        assertEquals("test.foo", null, conf.getProperty("test.foo"));

        conf.setProperty("test.foo", "value1");
        assertEquals("test.foo.bar", null, conf.getProperty("test.foo.bar"));
        assertEquals("test.foo", "value1", conf.getProperty("test.foo"));
    }

    public void testChangePrefix()
    {
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));
        assertEquals("'boolean' property", null, conf.getString("boolean"));

        // change the prefix
        conf.setPrefix("test");
        assertEquals("'test.boolean' property", null, conf.getString("test.boolean"));
        assertEquals("'boolean' property", "true", conf.getString("boolean"));
    }

    public void testResetRemovedProperties() throws Exception
    {
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));

        // remove the property
        conf.clearProperty("test.boolean");
        assertEquals("'test.boolean' property", null, conf.getString("test.boolean"));

        // change the context
        conf.setContext(new InitialContext());

        // get the property
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));
    }

    public void testConstructor() throws Exception
    {
        // test the constructor accepting a context
        conf = new JNDIConfiguration(new InitialContext());

        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));

        // test the constructor accepting a context and a prefix
        conf = new JNDIConfiguration(new InitialContext(), "test");

        assertEquals("'boolean' property", "true", conf.getString("boolean"));
    }

    /**
     * Tests whether a JNDI configuration registers an error log listener.
     */
    public void testLogListener() throws NamingException
    {
        conf = new JNDIConfiguration();
        assertEquals("No error log listener registered", 1, conf.getErrorListeners().size());
    }

    /**
     * Tests the getKeys() method when there are cycles in the tree.
     */
    public void testGetKeysWithCycles() throws NamingException
    {
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        env.put(MockInitialContextFactory.PROP_CYCLES, Boolean.TRUE);
        InitialContext initCtx = new InitialContext(env);
        conf = new JNDIConfiguration(initCtx);
        conf.getKeys("cycle");
    }

    public void testSetMaxDepth()
    {
        conf.setMaxDepth(0);
        assertFalse("Key found with depth set to 0", conf.getKeys().hasNext());

        conf.setMaxDepth(1);
        assertTrue("No key found with depth set to 1", conf.getKeys().hasNext());
    }
}
