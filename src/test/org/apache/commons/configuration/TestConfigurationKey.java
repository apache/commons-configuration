package org.apache.commons.configuration;

/*
 * Copyright 2002-2005 The Apache Software Foundation.
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

/**
 * Test class for ConfigurationKey. 
 * 
 * @version $Id$
 */
public class TestConfigurationKey extends TestCase
{
    private static final String TESTPROPS = "tables.table(0).fields.field(1)";
    
    private static final String TESTATTR = "[@dataType]";
    
    private static final String TESTKEY = TESTPROPS + TESTATTR;
    
    public void testAppend()
    {
        ConfigurationKey key = new ConfigurationKey();
        key.append("tables").append("table.").appendIndex(0);
        key.append("fields.").append("field").appendIndex(1);
        key.appendAttribute("dataType");
        assertEquals(TESTKEY, key.toString());
    }
    
    public void testIterate()
    {
        ConfigurationKey key = new ConfigurationKey(TESTKEY);
        ConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("tables", it.nextKey());
        assertEquals("table", it.nextKey());
        assertTrue(it.hasIndex());
        assertEquals(0, it.getIndex());
        assertEquals("fields", it.nextKey());
        assertFalse(it.hasIndex());
        assertEquals("field", it.nextKey(true));
        assertEquals(1, it.getIndex());
        assertFalse(it.isAttribute());
        assertEquals("field", it.currentKey(true));
        assertEquals("dataType", it.nextKey());
        assertEquals("[@dataType]", it.currentKey(true));
        assertTrue(it.isAttribute());
        assertFalse(it.hasNext());
        try
        {
            it.next();
            fail("Could iterate over the iteration's end!");
        }
        catch(NoSuchElementException nex)
        {
            //ok
        }
        
        key = new ConfigurationKey();
        assertFalse(key.iterator().hasNext());
        key.append("simple");
        it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("simple", it.next());
        try
        {
            it.remove();
            fail("Could remove key component!");
        }
        catch(UnsupportedOperationException uex)
        {
            //ok
        }
    }
    
    public void testAttribute()
    {
        assertTrue(ConfigurationKey.isAttributeKey(TESTATTR));
        assertFalse(ConfigurationKey.isAttributeKey(TESTPROPS));
        assertFalse(ConfigurationKey.isAttributeKey(TESTKEY));
        
        ConfigurationKey key = new ConfigurationKey(TESTPROPS);
        key.append(TESTATTR);
        assertEquals(TESTKEY, key.toString());
    }
    
    public void testLength()
    {
        ConfigurationKey key = new ConfigurationKey(TESTPROPS);
        assertEquals(TESTPROPS.length(), key.length());
        key.appendAttribute("dataType");
        assertEquals(TESTKEY.length(), key.length());
        key.setLength(TESTPROPS.length());
        assertEquals(TESTPROPS.length(), key.length());
        assertEquals(TESTPROPS, key.toString());
    }
    
    public void testConstructAttributeKey()
    {
        assertEquals("[@attribute]", ConfigurationKey.constructAttributeKey("attribute"));
        assertEquals("attribute", ConfigurationKey.attributeName("[@attribute]"));
        assertEquals("attribute", ConfigurationKey.attributeName("attribute"));
    }
    
    public void testEquals()
    {
        ConfigurationKey k1 = new ConfigurationKey(TESTKEY);
        ConfigurationKey k2 = new ConfigurationKey(TESTKEY);
        assertTrue(k1.equals(k2));
        assertTrue(k2.equals(k1));
        assertEquals(k1.hashCode(), k2.hashCode());
        k2.append("anotherPart");
        assertFalse(k1.equals(k2));
        assertFalse(k2.equals(k1));
        assertFalse(k1.equals(null));
        assertTrue(k1.equals(TESTKEY));        
    }
    
    public void testCommonKey()
    {
        ConfigurationKey k1 = new ConfigurationKey(TESTKEY);
        ConfigurationKey k2 = new ConfigurationKey("tables.table(0).name");
        ConfigurationKey kc = k1.commonKey(k2);
        assertEquals(new ConfigurationKey("tables.table(0)"), kc);
        assertEquals(kc, k2.commonKey(k1));
        
        k2 = new ConfigurationKey("tables.table(1).fields.field(1)");
        kc = k1.commonKey(k2);
        assertEquals(new ConfigurationKey("tables"), kc);
        
        k2 = new ConfigurationKey("completely.different.key");
        kc = k1.commonKey(k2);
        assertEquals(0, kc.length());
        
        k2 = new ConfigurationKey();
        kc = k1.commonKey(k2);
        assertEquals(0, kc.length());
        
        kc = k1.commonKey(k1);
        assertEquals(kc, k1);
        
        try
        {
            kc.commonKey(null);
            fail("Could construct common key with null key!");
        }
        catch(IllegalArgumentException iex)
        {
            //ok
        }
    }
    
    public void testDifferenceKey()
    {
        ConfigurationKey k1 = new ConfigurationKey(TESTKEY);
        ConfigurationKey kd = k1.differenceKey(k1);
        assertEquals(0, kd.length());
        
        ConfigurationKey k2 = new ConfigurationKey("tables.table(0).name");
        kd = k1.differenceKey(k2);
        assertEquals("name", kd.toString());
        
        k2 = new ConfigurationKey("tables.table(1).fields.field(1)");
        kd = k1.differenceKey(k2);
        assertEquals("table(1).fields.field(1)", kd.toString());
        
        k2 = new ConfigurationKey("completely.different.key");
        kd = k1.differenceKey(k2);
        assertEquals(k2, kd);
    }
    
    public void testEscapedDelimiters()
    {
        ConfigurationKey k = new ConfigurationKey();
        k.append("my..elem");
        k.append("trailing..dot..");
        k.append("strange");
        assertEquals("my..elem.trailing..dot...strange", k.toString());
        
        ConfigurationKey.KeyIterator kit = k.iterator();
        assertEquals("my.elem", kit.nextKey());
        assertEquals("trailing.dot.", kit.nextKey());
        assertEquals("strange", kit.nextKey());
        assertFalse(kit.hasNext());
    }
    
    /**
     * Tests some funny keys.
     */
    public void testIterateStrangeKeys()
    {
        ConfigurationKey k = new ConfigurationKey("key.");
        ConfigurationKey.KeyIterator it = k.iterator();
        assertTrue(it.hasNext());
        assertEquals("key", it.next());
        assertFalse(it.hasNext());
        
        k = new ConfigurationKey(".");
        it = k.iterator();
        assertFalse(it.hasNext());
        
        k = new ConfigurationKey("key().index()undefined(0).test");
        it = k.iterator();
        assertEquals("key()", it.next());
        assertFalse(it.hasIndex());
        assertEquals("index()undefined", it.nextKey(false));
        assertTrue(it.hasIndex());
        assertEquals(0, it.getIndex());
    }
    
    /**
     * Tests iterating over an attribute key that has an index.
     */
    public void testAttributeKeyWithIndex()
    {
        ConfigurationKey k = new ConfigurationKey(TESTATTR);
        k.appendIndex(0);
        assertEquals("Wrong attribute key with index", TESTATTR + "(0)", k.toString());
        
        ConfigurationKey.KeyIterator it = k.iterator();
        assertTrue("No first element", it.hasNext());
        it.next();
        assertTrue("Index not found", it.hasIndex());
        assertEquals("Incorrect index", 0, it.getIndex());
        assertTrue("Attribute not found", it.isAttribute());
        assertEquals("Wrong plain key", "dataType", it.currentKey(false));
        assertEquals("Wrong decorated key", TESTATTR, it.currentKey(true));
    }
}
