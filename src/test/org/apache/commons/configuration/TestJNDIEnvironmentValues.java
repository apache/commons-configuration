package org.apache.commons.configuration;

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

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class TestJNDIEnvironmentValues extends TestCase
{
    private JNDIConfiguration conf = null;
    public TestJNDIEnvironmentValues(String testName)
    {
        super(testName);
    }
    
    public void setUp() throws Exception{
        System.setProperty("java.naming.factory.initial","org.apache.commons.configuration.MockStaticMemoryInitialContextFactory");
        
        conf = new JNDIConfiguration();
        conf.setPrefix("");
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
        try
        {
            conf.getString("test.imaginarykey");
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException nsee)
        {
            assertTrue(nsee.getMessage(),nsee.getMessage().indexOf("test.imaginarykey")!=-1);
        }

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
    
    public void testClearProperty() {

        assertNotNull("null short for the 'test.short' key", conf.getShort("test.short", null));
        conf.clearProperty("test.short");
        assertNull("'test.short' property not cleared", conf.getShort("test.short", null));
    }
    
    public void testIsEmpty() {
        assertFalse("the configuration shouldn't be empty", conf.isEmpty());
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
}
