/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import junit.framework.TestCase;

import javax.naming.InitialContext;

/**
 * Test to see if the JNDIConfiguration works properly.  Currently excluded
 * in the project.xml unitTest section as our JNDI provider doesn't
 * properly support the listBindings() method.
 *
 * This does work fine with Tomcat's JNDI provider however.
 *
 * @version $Id$
 */
public class TestJNDIConfiguration extends TestCase {

    public static final String CONTEXT_FACTORY =
            "org.apache.commons.configuration.MockStaticMemoryInitialContextFactory";

    private JNDIConfiguration conf;
    private NonStringTestHolder nonStringTestHolder;

    public void setUp() throws Exception {

        System.setProperty("java.naming.factory.initial", CONTEXT_FACTORY);

        conf = new JNDIConfiguration();

        nonStringTestHolder = new NonStringTestHolder();
        nonStringTestHolder.setConfiguration(conf);
    }

    public void testBoolean() throws Exception {
        nonStringTestHolder.testBoolean();
    }

    public void testBooleanDefaultValue() throws Exception {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    public void testByte() throws Exception {
        nonStringTestHolder.testByte();
    }

    public void testDouble() throws Exception {
        nonStringTestHolder.testDouble();
    }

    public void testDoubleDefaultValue() throws Exception {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    public void testFloat() throws Exception {
        nonStringTestHolder.testFloat();
    }

    public void testFloatDefaultValue() throws Exception {
        nonStringTestHolder.testFloatDefaultValue();
    }

    public void testInteger() throws Exception {
        nonStringTestHolder.testInteger();
    }

    public void testIntegerDefaultValue() throws Exception {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    public void testLong() throws Exception {
        nonStringTestHolder.testLong();
    }

    public void testLongDefaultValue() throws Exception {
        nonStringTestHolder.testLongDefaultValue();
    }

    public void testShort() throws Exception {
        nonStringTestHolder.testShort();
    }

    public void testShortDefaultValue() throws Exception {
        nonStringTestHolder.testShortDefaultValue();
    }

    public void testListMissing() throws Exception {
        nonStringTestHolder.testListMissing();
    }

    public void testSubset() throws Exception {
        nonStringTestHolder.testSubset();
    }

    public void testProperties() throws Exception {
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

}