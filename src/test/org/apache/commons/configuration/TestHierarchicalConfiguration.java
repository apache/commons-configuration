package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalConfiguration.
 * 
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: TestHierarchicalConfiguration.java,v 1.2 2004/01/16 14:23:39 epugh Exp $
 */
public class TestHierarchicalConfiguration extends TestCase
{
    private static String[] tables = { "users", "documents" };
    
    private static String[][] fields =
    {
        { "uid", "uname", "firstName", "lastName", "email" },
        { "docid", "name", "creationDate", "authorID", "version" }
    };
        
    private HierarchicalConfiguration config;

    protected void setUp() throws Exception
    {
        config = new HierarchicalConfiguration();
        HierarchicalConfiguration.Node nodeTables =
        new HierarchicalConfiguration.Node("tables");
        for(int i = 0; i < tables.length; i++)
        {
            HierarchicalConfiguration.Node nodeTable = 
            new HierarchicalConfiguration.Node("table");
            nodeTables.addChild(nodeTable);
            HierarchicalConfiguration.Node nodeName =
            new HierarchicalConfiguration.Node("name");
            nodeName.setValue(tables[i]);
            nodeTable.addChild(nodeName);
            HierarchicalConfiguration.Node nodeFields =
            new HierarchicalConfiguration.Node("fields");
            nodeTable.addChild(nodeFields);
            for(int j = 0; j < fields[i].length; j++)
            {
                HierarchicalConfiguration.Node nodeField =
                new HierarchicalConfiguration.Node("field");
                HierarchicalConfiguration.Node nodeFieldName =
                new HierarchicalConfiguration.Node("name");
                nodeFieldName.setValue(fields[i][j]);
                nodeField.addChild(nodeFieldName);
                nodeFields.addChild(nodeField);
            }  /* for */
        }  /* for */
        config.getRoot().addChild(nodeTables);
    }

    public void testIsEmpty()
    {
        assertFalse(config.isEmpty());
        HierarchicalConfiguration conf2 = new HierarchicalConfiguration();
        assertTrue(conf2.isEmpty());
        HierarchicalConfiguration.Node child1 = 
        new HierarchicalConfiguration.Node("child1");
        HierarchicalConfiguration.Node child2 = 
        new HierarchicalConfiguration.Node("child2");
        child1.addChild(child2);
        conf2.getRoot().addChild(child1);
        assertTrue(conf2.isEmpty());
    }

    public void testGetProperty()
    {
        assertNull(config.getProperty("tables.table.resultset"));
        assertNull(config.getProperty("tables.table.fields.field"));
        
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
        
        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());
        
        prop = config.getProperty("tables.table.fields.field(3).name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection) prop).size());
        
        prop = config.getProperty("tables.table(1).fields.field(2).name");
        assertNotNull(prop);
        assertEquals("creationDate", prop.toString());
    }
    
    public void testClearProperty()
    {
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        config.clearProperty("tables.table(0).fields.field(3)");
        prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(4, ((Collection) prop).size());
        
        config.clearProperty("tables.table(0).fields");
        assertNull(config.getProperty("tables.table(0).fields.field.name"));
        prop = config.getProperty("tables.table.fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
        
        config.clearProperty("tables.table(1)");
        assertNull(config.getProperty("tables.table.fields.field.name"));
    }
    
    public void testContainsKey()
    {
        assertTrue(config.containsKey("tables.table(0).name"));
        assertTrue(config.containsKey("tables.table(1).name"));
        assertFalse(config.containsKey("tables.table(2).name"));
        
        assertTrue(config.containsKey("tables.table(0).fields.field.name"));
        assertFalse(config.containsKey("tables.table(0).fields.field"));
        config.clearProperty("tables.table(0).fields");
        assertFalse(config.containsKey("tables.table(0).fields.field.name"));
        
        assertTrue(config.containsKey("tables.table.fields.field.name"));
    }
    
    public void testGetKeys()
    {
        List keys = new ArrayList();
        for(Iterator it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next()); 
        }  /* for */
        
        assertEquals(2, keys.size());
        assertTrue(keys.contains("tables.table.name"));
        assertTrue(keys.contains("tables.table.fields.field.name"));
    }
    
    public void testAddProperty()
    {
        config.addProperty("tables.table(0).fields.field(-1).name", "phone");
        Object prop = config.getProperty("tables.table(0).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(6, ((Collection) prop).size());
        
        config.addProperty("tables.table(0).fields.field.name", "fax");
        prop = config.getProperty("tables.table.fields.field(5).name");
        assertNotNull(prop);
        assertTrue(prop instanceof List);
        List list = (List) prop;
        assertEquals("phone", list.get(0));
        assertEquals("fax", list.get(1));
        
        config.addProperty("tables.table(-1).name", "config");
        prop = config.getProperty("tables.table.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection) prop).size());
        config.addProperty("tables.table(2).fields.field(0).name", "cid");
        config.addProperty("tables.table(2).fields.field(-1).name",
        "confName");
        prop = config.getProperty("tables.table(2).fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(2, ((Collection) prop).size());
        assertEquals("confName",
        config.getProperty("tables.table(2).fields.field(1).name"));
        
        config.addProperty("connection.user", "scott");
        config.addProperty("connection.passwd", "tiger");
        assertEquals("tiger", config.getProperty("connection.passwd"));
        
        ConfigurationKey key = new ConfigurationKey();
        key.append("tables").append("table").appendIndex(0);
        key.appendAttribute("tableType");
        config.addProperty(key.toString(), "system");
        assertEquals("system", config.getProperty(key.toString()));
    }
    
    public void testGetMaxIndex()
    {
        assertEquals(4, config.getMaxIndex("tables.table(0).fields.field"));
        assertEquals(4, config.getMaxIndex("tables.table(1).fields.field"));
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals(1, config.getMaxIndex("tables.table.name"));
        assertEquals(0, config.getMaxIndex("tables.table(0).name"));
        assertEquals(0, config.getMaxIndex("tables.table(1).fields.field(1)"));
        assertEquals(-1, config.getMaxIndex("tables.table(2).fields"));
        
        int maxIdx = config.getMaxIndex("tables.table(0).fields.field.name");
        for(int i = 0; i <= maxIdx; i++)
        {
            ConfigurationKey key = new ConfigurationKey("tables.table(0).fields");
            key.append("field").appendIndex(i).append("name");
            assertNotNull(config.getProperty(key.toString()));
        }  /* for */
    }
    
    public void testSubset()
    {
        Configuration conf = config.subset("tables.table(0)");
        assertEquals("users", conf.getProperty("name"));
        Object prop = conf.getProperty("fields.field.name");
        assertNotNull(prop);
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
        
        for(int i = 0; i < fields[0].length; i++)
        {
            ConfigurationKey key = new ConfigurationKey();
            key.append("fields").append("field").appendIndex(i);
            key.append("name");
            assertEquals(fields[0][i], conf.getProperty(key.toString()));
        }  /* for */
        
        assertNull(config.subset("tables.table(2)"));
        
        conf = config.subset("tables.table.fields.field.name");
        prop = conf.getProperty("name");
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());
    }
}
