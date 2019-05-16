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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ErrorListenerTestImpl;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test to see if the JNDIConfiguration works properly.
 *
 */
public class TestJNDIConfiguration {

    public static final String CONTEXT_FACTORY = MockInitialContextFactory.class.getName();

    private PotentialErrorJNDIConfiguration conf;
    private NonStringTestHolder nonStringTestHolder;

    /** A test error listener for counting internal errors.*/
    private ErrorListenerTestImpl listener;

    @Before
    public void setUp() throws Exception {

        System.setProperty("java.naming.factory.initial", CONTEXT_FACTORY);

        final Properties props = new Properties();
        props.put("java.naming.factory.initial", CONTEXT_FACTORY);
        final Context ctx = new InitialContext(props);
        conf = new PotentialErrorJNDIConfiguration(ctx);

        nonStringTestHolder = new NonStringTestHolder();
        nonStringTestHolder.setConfiguration(conf);

        listener = new ErrorListenerTestImpl(conf);
        conf.addEventListener(ConfigurationErrorEvent.ANY, listener);
    }

    /**
     * Clears the test environment. If an error listener is defined, checks
     * whether no error event was received.
     */
    @After
    public void tearDown() throws Exception
    {
        if (listener != null)
        {
            listener.done();
        }
    }

    @Test
    public void testBoolean() throws Exception {
        nonStringTestHolder.testBoolean();
    }

    @Test
    public void testBooleanDefaultValue() throws Exception {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    @Test
    public void testByte() throws Exception {
        nonStringTestHolder.testByte();
    }

    @Test
    public void testDouble() throws Exception {
        nonStringTestHolder.testDouble();
    }

    @Test
    public void testDoubleDefaultValue() throws Exception {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    @Test
    public void testFloat() throws Exception {
        nonStringTestHolder.testFloat();
    }

    @Test
    public void testFloatDefaultValue() throws Exception {
        nonStringTestHolder.testFloatDefaultValue();
    }

    @Test
    public void testInteger() throws Exception {
        nonStringTestHolder.testInteger();
    }

    @Test
    public void testIntegerDefaultValue() throws Exception {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    @Test
    public void testLong() throws Exception {
        nonStringTestHolder.testLong();
    }

    @Test
    public void testLongDefaultValue() throws Exception {
        nonStringTestHolder.testLongDefaultValue();
    }

    @Test
    public void testShort() throws Exception {
        nonStringTestHolder.testShort();
    }

    @Test
    public void testShortDefaultValue() throws Exception {
        nonStringTestHolder.testShortDefaultValue();
    }

    @Test
    public void testListMissing() throws Exception {
        nonStringTestHolder.testListMissing();
    }

    @Test
    public void testSubset() throws Exception {
        nonStringTestHolder.testSubset();
    }

    @Test
    public void testProperties() throws Exception {
        final Object o = conf.getProperty("test.boolean");
        assertNotNull(o);
        assertEquals("true", o.toString());
    }

    @Test
    public void testContainsKey()
    {
        final String key = "test.boolean";
        assertTrue("'" + key + "' not found", conf.containsKey(key));

        conf.clearProperty(key);
        assertFalse("'" + key + "' still found", conf.containsKey(key));
    }

    @Test
    public void testChangePrefix()
    {
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));
        assertEquals("'boolean' property", null, conf.getString("boolean"));

        // change the prefix
        conf.setPrefix("test");
        assertEquals("'test.boolean' property", null, conf.getString("test.boolean"));
        assertEquals("'boolean' property", "true", conf.getString("boolean"));
    }

    @Test
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

    @Test
    public void testConstructor() throws Exception
    {
        // test the constructor accepting a context
        JNDIConfiguration c = new JNDIConfiguration(new InitialContext());

        assertEquals("'test.boolean' property", "true", c.getString("test.boolean"));

        // test the constructor accepting a context and a prefix
        c = new JNDIConfiguration(new InitialContext(), "test");

        assertEquals("'boolean' property", "true", c.getString("boolean"));
    }

    /**
     * Configures the test config to throw an exception.
     */
    private PotentialErrorJNDIConfiguration setUpErrorConfig()
    {
        conf.installException();
        // remove log error listener to avoid output in tests
        final Iterator<EventListener<? super ConfigurationErrorEvent>> iterator =
                conf.getEventListeners(ConfigurationErrorEvent.ANY).iterator();
        conf.removeEventListener(ConfigurationErrorEvent.ANY, iterator.next());
        return conf;
    }

    /**
     * Tests whether the expected error events have been received.
     *
     * @param type the expected event type
     * @param opEventType the event type of the failed operation
     * @param propName the name of the property
     * @param propValue the property value
     */
    private void checkErrorListener(
            final EventType<? extends ConfigurationErrorEvent> type,
            final EventType<?> opEventType, final String propName, final Object propValue)
    {
        final Throwable exception =
                listener.checkEvent(type, opEventType, propName, propValue);
        assertTrue("Wrong exception class",
                exception instanceof NamingException);
        listener = null;
    }

    /**
     * Tests whether a JNDI configuration registers an error log listener.
     */
    @Test
    public void testLogListener() throws NamingException
    {
        final JNDIConfiguration c = new JNDIConfiguration();
        assertEquals("No error log listener registered", 1, c
                .getEventListeners(ConfigurationErrorEvent.ANY).size());
    }

    /**
     * Tests handling of errors in getKeys().
     */
    @Test
    public void testGetKeysError()
    {
        assertFalse("Iteration not empty", setUpErrorConfig().getKeys()
                .hasNext());
        checkErrorListener(ConfigurationErrorEvent.READ,
                ConfigurationErrorEvent.READ, null, null);
    }

    /**
     * Tests handling of errors in isEmpty().
     */
    @Test
    public void testIsEmptyError() throws Exception
    {
        assertTrue("Error config not empty", setUpErrorConfig().isEmpty());
        checkErrorListener(ConfigurationErrorEvent.READ,
                ConfigurationErrorEvent.READ, null, null);
    }

    /**
     * Tests handling of errors in the containsKey() method.
     */
    @Test
    public void testContainsKeyError()
    {
        assertFalse("Key contained after error", setUpErrorConfig()
                .containsKey("key"));
        checkErrorListener(ConfigurationErrorEvent.READ,
                ConfigurationErrorEvent.READ, "key", null);
    }

    /**
     * Tests handling of errors in getProperty().
     */
    @Test
    public void testGetPropertyError()
    {
        assertNull("Wrong property value after error", setUpErrorConfig()
                .getProperty("key"));
        checkErrorListener(ConfigurationErrorEvent.READ,
                ConfigurationErrorEvent.READ, "key", null);
    }

    /**
     * Tests the getKeys() method when there are cycles in the tree.
     */
    @Test
    public void testGetKeysWithCycles() throws NamingException
    {
        final Hashtable<Object, Object> env = new Hashtable<>();
        env.put(MockInitialContextFactory.PROP_CYCLES, Boolean.TRUE);
        final InitialContext initCtx = new InitialContext(env);
        final JNDIConfiguration c = new JNDIConfiguration(initCtx);
        c.getKeys("cycle");
    }

    /**
     * Tests getKeys() if no data is found. This should not cause a problem and
     * not notify the error listeners.
     */
    @Test
    public void testGetKeysNoData()
    {
        conf.installException(new NameNotFoundException("Test exception"));
        assertFalse("Got keys", conf.getKeys().hasNext());
        listener.done();
    }

    /**
     * A special JNDI configuration implementation that can be configured to
     * throw an exception when accessing the base context. Used for testing the
     * exception handling.
     */
    public static class PotentialErrorJNDIConfiguration extends
            JNDIConfiguration
    {
        /** An exception to be thrown by getBaseContext(). */
        private NamingException exception;

        public PotentialErrorJNDIConfiguration(final Context ctx)
        {
            super(ctx);
        }

        /**
         * Prepares this object to throw an exception when the JNDI context is
         * queried.
         *
         * @param nex the exception to be thrown
         */
        public void installException(final NamingException nex)
        {
            exception = nex;
        }

        /**
         * Prepares this object to throw a standard exception when the JNDI
         * context is queried.
         */
        public void installException()
        {
            installException(new NamingException("Simulated JNDI exception!"));
        }

        /**
         * Returns the JNDI context. Optionally throws an exception.
         */
        @Override
        public Context getBaseContext() throws NamingException
        {
            if (exception != null)
            {
                throw exception;
            }
            return super.getBaseContext();
        }
    }
}