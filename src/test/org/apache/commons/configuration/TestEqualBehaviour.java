package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Compare the behaviour of various methods between CompositeConfiguration
 * and normal (Properties) Configuration
 * 
 * @author <a href0"mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: TestEqualBehaviour.java,v 1.3 2004/01/16 14:23:39 epugh Exp $
 */
public class TestEqualBehaviour  extends TestCase
{
    private Configuration setupSimpleConfiguration()
            throws Exception
    {
        String simpleConfigurationFile = new File("conf/testEqual.properties").getAbsolutePath();
        return new PropertiesConfiguration(simpleConfigurationFile);
    }

    private Configuration setupCompositeConfiguration()
            throws Exception
    {
        String compositeConfigurationFile = new File("conf/testEqualDigester.xml").getAbsolutePath();

        ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.setConfigurationFileName(compositeConfigurationFile);
        return configurationFactory.getConfiguration();
    }

    /**
     * Checks whether two configurations have the same size, 
     * the same key sequence and contain the same key -> value mappings
     */
    private void checkEquality(String msg, Configuration c1, Configuration c2)
    {
        Iterator it1 = c1.getKeys();
        Iterator it2 = c2.getKeys();

        while(it1.hasNext() && it2.hasNext())
        {
            String key1 = (String) it1.next();
            String key2 = (String) it2.next();
            assertEquals(msg + ", Keys: ", key1, key2);
            assertEquals(msg + ", Contains: ", c1.containsKey(key1), c2.containsKey(key2));
        }
        assertEquals(msg + ", Iterator: ", it1.hasNext(), it2.hasNext());
    }

    /**
     * Checks whether two configurations have the same key -> value mapping
     */
    private void checkSameKey(String msg, String key, Configuration c1, Configuration c2)
    {
        String [] s1 = c1.getStringArray(key);
        String [] s2 = c2.getStringArray(key);

        assertEquals(msg + ", length: ", s1.length, s2.length);

        for (int i = 0; i < s1.length ; i++)
        {
            assertEquals(msg + ", String Array: ", s1[i], s2[i]);
        }

        List list1 = c1.getList(key);
        List list2 = c2.getList(key);

        assertEquals(msg + ", Size: ", list1.size(), list2.size());

        Iterator it1 = list1.iterator();
        Iterator it2 = list2.iterator();

        while(it1.hasNext() && it2.hasNext())
        {
            String val1 = (String) it1.next();
            String val2 = (String) it2.next();
            assertEquals(msg + ", List: ", val1, val2);
        }
        assertEquals(msg + ", Iterator End: ", it1.hasNext(), it2.hasNext());
    }

    /**
     * Are both configurations equal after loading?
     */
    public void testLoading() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        checkEquality("testLoading", simple, composite);
    }

    /**
     * If we delete a key, does it vanish? Does it leave all
     * the other keys unchanged? How about an unset key?
     */
    public void testDeletingExisting() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "clear.property";

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.clearProperty(key);
        composite.clearProperty(key);

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkEquality("testDeletingExisting", simple, composite);
    }

    public void testDeletingNonExisting() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "nonexisting.clear.property";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.clearProperty(key);
        composite.clearProperty(key);

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkEquality("testDeletingNonExisting", simple, composite);
    }

    /**
     * If we set a key, does it work? How about an existing
     * key? Can we change it?
     */
    public void testSettingNonExisting() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "nonexisting.property";
        String value = "new value";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.setProperty(key, value);
        composite.setProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testSettingNonExisting", key, simple, composite);
        checkEquality("testSettingNonExisting", simple, composite);
    }

    public void testSettingExisting() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "existing.property";
        String value = "new value";

        assertTrue(simple.containsKey(key));
        assertFalse(simple.getString(key).equals(value));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.setProperty(key, value);
        composite.setProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.getString(key), value);
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testSettingExisting", key, simple, composite);
        checkEquality("testSettingExisting", simple, composite);
    }

    /**
     * If we add a key, does it work?
     */
    public void testAddingUnset() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "nonexisting.property";
        String value = "new value";

        assertFalse(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.addProperty(key, value);
        composite.addProperty(key, value);

        checkSameKey("testAddingUnset", key, simple, composite);
        checkEquality("testAddingUnset", simple, composite);
    }

    /**
     * If we add a to an existing key, does it work?
     */
    public void testAddingSet() throws Exception
    {
        Configuration simple = setupSimpleConfiguration();
        Configuration composite = setupCompositeConfiguration();

        String key = "existing.property";
        String value = "new value";

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        simple.addProperty(key, value);
        composite.addProperty(key, value);

        assertTrue(simple.containsKey(key));
        assertEquals(simple.containsKey(key), composite.containsKey(key));

        checkSameKey("testAddingSet", key, simple, composite);
        checkEquality("testAddingSet", simple, composite);
    }
}
