package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import junit.framework.TestCase;

/**
 * Test class for ConfigurationKey. 
 * 
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: TestConfigurationKey.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class TestConfigurationKey extends TestCase
{
    private static final String TESTPROPS = "tables.table(0).fields.field(1)";
    
    private static final String TESTATTR = "[@dataType]";
    
    private static final String TESTKEY = TESTPROPS + TESTATTR;
    
    /**
     * Constructor for TestConfigurationKey.
     * @param arg0
     */
    public TestConfigurationKey(String arg0)
    {
        super(arg0);
    }

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
        
        key = new ConfigurationKey();
        assertFalse(key.iterator().hasNext());
        key.append("simple");
        it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("simple", it.next());
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
}
