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

import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class TestJNDIEnvironmentValues extends TestCase
{
    public TestJNDIEnvironmentValues(String testName)
    {
        super(testName);
    }

    public void testSimpleGet() throws Exception
    {
        JNDIConfiguration conf = new JNDIConfiguration();
        conf.setPrefix("");
        String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
    }

    public void testMoreGets() throws Exception
    {
        JNDIConfiguration conf = new JNDIConfiguration();
        conf.setPrefix("");
        String s = conf.getString("test.key");
        assertEquals("jndivalue", s);
        assertEquals("jndivalue2", conf.getString("test.key2"));
        assertEquals(1, conf.getShort("test.short"));
    }

    public void testGetMissingKey() throws Exception
    {
        JNDIConfiguration conf = new JNDIConfiguration();
        conf.setPrefix("");
        try
        {
            conf.getString("test.imaginarykey");
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException nsee)
        {

        }

    }
    public void testGetMissingKeyWithDefault() throws Exception
    {
        JNDIConfiguration conf = new JNDIConfiguration();
        conf.setPrefix("");

        String result = conf.getString("test.imaginarykey", "bob");
        assertEquals("bob", result);

    }
    public void testContainsKey() throws Exception
    {
        JNDIConfiguration conf = new JNDIConfiguration();
        conf.setPrefix("");

        assertTrue(conf.containsKey("test.key"));

        assertTrue(!conf.containsKey("test.imaginerykey"));

    }
}
