package org.apache.commons.configuration;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalConfiguration.
 * 
 * @version $Id: TestHierarchicalConfiguration.java,v 1.4 2004/03/09 10:31:31 epugh Exp $
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

        assertTrue("subset is not empty", config.subset("tables.table(2)").isEmpty());

        conf = config.subset("tables.table.fields.field");
        prop = conf.getProperty("name");
        assertTrue("prop is not a collection", prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());
    }
}
