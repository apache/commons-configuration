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

import java.util.Iterator;

/**
 * Test to see if the JNDIConfiguration works properly.  Currently excluded
 * in the project.xml unitTest section as our JNDI provider doesn't 
 * properly support the listBindings() method.
 * 
 * This does work fine with Tomcat's JNDI provider however.
 *
 * @version $Id: TestJNDIConfiguration.java,v 1.6 2004/03/28 14:42:45 epugh Exp $
 */
public class TestJNDIConfiguration extends TestCase {

    private JNDIConfiguration conf;
    private NonStringTestHolder nonStringTestHolder;

    public void setUp() throws Exception {
        //InitialContext context = new InitialContext();
        //assertNotNull(context);

        conf = new JNDIConfiguration();
        conf.setPrefix("");

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

    /**
     * Currently failing in that we don't get back any keys!
     * @throws Exception
     */
    public void testGetKeys() throws Exception {

        boolean found = false;
        Iterator it = conf.getKeys();

        assertTrue("no key found", it.hasNext());

        while (it.hasNext() && !found) {
            found = "test.boolean".equals(it.next());
        }

        assertTrue("'test.boolean' key not found", found);
    }

    public void testClearProperty() {
        assertNotNull("null boolean for the 'test.boolean' key", conf.getBoolean("test.boolean", null));
        conf.clearProperty("test.boolean");
        assertNull("'test.boolean' property not cleared", conf.getBoolean("test.boolean", null));
    }

    public void testIsEmpty() {
        assertFalse("the configuration shouldn't be empty", conf.isEmpty());
    }

}