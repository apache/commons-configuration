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

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.digester.Digester;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.xml.sax.XMLReader;

import junit.framework.TestCase;

/**
 * Test class for ConfigurationXMLDocument.
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: TestConfigurationXMLDocument.java,v 1.4 2004/03/08 23:27:09 epugh Exp $
 */
public class TestConfigurationXMLDocument extends TestCase
{
    private static final String DEF_FILE = "conf/testConfigurationXMLDocument.xml";
    
    private Configuration config;
    private ConfigurationFactory configFactory;
    private ConfigurationXMLDocument configDoc;
    
    private ArrayList tables;
    private int counter;
    
    public TestConfigurationXMLDocument(String arg0)
    {
        super(arg0);
    }

    protected void setUp() throws Exception
    {
        configFactory = new ConfigurationFactory();
        configFactory.setConfigurationURL(new File(DEF_FILE).toURL());
        configDoc = new ConfigurationXMLDocument(configFactory.getConfiguration());
        config = configDoc.getConfiguration();
        
        tables = new ArrayList();
    }

    public void testCreateXMLReader()
    {
        BaseConfiguration bc = new BaseConfiguration();
        bc.addProperty("test", "value");
        XMLReader reader = ConfigurationXMLDocument.createXMLReader(bc);
        assertTrue(reader instanceof BaseConfigurationXMLReader);
        
        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        hc.addProperty("test", "value");
        reader = ConfigurationXMLDocument.createXMLReader(hc);
        assertTrue(reader instanceof HierarchicalConfigurationXMLReader);
        
        reader = configDoc.createXMLReader();
        assertTrue(reader instanceof BaseConfigurationXMLReader);
        ConfigurationXMLDocument doc = 
        new ConfigurationXMLDocument(config.subset("database"));
        reader = doc.createXMLReader();
        assertTrue(reader instanceof HierarchicalConfigurationXMLReader);
        
        try
        {
            configDoc.createXMLReader("unexisting.property");
            fail();
        }  /* try */
        catch(NoSuchElementException ex)
        {
            // fine
        }  /* catch */
    }
    
    public void testGetDocument() throws Exception
    {
        Document doc;
        ConfigurationXMLDocument xmlDoc = 
        new ConfigurationXMLDocument(config.subset("database"));
        
        doc = configDoc.getDocument("database");
        checkDocument(doc, "config");
        
        doc = xmlDoc.getDocument();
        checkDocument(doc, "config");
        
        doc = xmlDoc.getDocument(null, "database");
        checkDocument(doc, "database");
        
        doc = configDoc.getDocument("database", "db-config");
        checkDocument(doc, "db-config");
    }
    
    public void testGetW3CDocument() throws Exception
    {
        org.w3c.dom.Document doc;
        ConfigurationXMLDocument xmlDoc = 
        new ConfigurationXMLDocument(config.subset("database"));
        
        doc = configDoc.getW3cDocument("database");
        checkDocument(doc, "config");
        
        doc = xmlDoc.getW3cDocument();
        checkDocument(doc, "config");
        
        doc = xmlDoc.getW3cDocument(null, "database");
        checkDocument(doc, "database");
        
        doc = configDoc.getW3cDocument("database", "db-config");
        checkDocument(doc, "db-config");
    }
    
    public void testCallDigester() throws Exception
    {
        Object obj = configDoc.callDigester("database.connection");
        assertNotNull(obj);
        assertTrue(obj instanceof ConnectionData);
        
        ConnectionData cd = (ConnectionData) obj;
        assertEquals("MyData", cd.getDsn());
        assertEquals("scott", cd.getUser());
        assertEquals("tiger", cd.getPasswd()); 
    }
    
    private void checkDocument(Document doc, String root)
    throws Exception
    {
        Element rt = doc.getRootElement();
        assertEquals(root, rt.getName());
        
        check(rt, "tables.table(0).name", "users");
        check(rt, "tables.table(1).name", "documents");
        check(rt, "tables.table(2).name", "tasks");
        check(rt, "tables.table(0).fields.field(0).type", "long");
        check(rt, "tables.table(2).fields.field(6).name", "endDate");
        
        checkAttribute(rt, "connection.class.property(0)", "name", "dsn");
        checkAttribute(rt, "connection.class.property(0)", "value", "MyData");
        checkAttribute(rt, "connection.class.property(1)", "name", "user");
        checkAttribute(rt, "connection.class.property(1)", "value", "scott");
    }
    
    private void checkDocument(org.w3c.dom.Document doc, String root)
    throws Exception
    {
        DOMReader reader = new DOMReader();
        checkDocument(reader.read(doc), root);
    }
    
    /**
     * Helper method for checking values in the DOM4J document.
     * @param root the root element
     * @param path the path to be checked
     * @param value the expected element value
     */
    private void check(Element root, String path, String value)
    {
        assertEquals(value, findElement(root, path).getTextTrim());        
    }
    
    /**
     * Helper method for checking the value of an attribute in the DOM4J doc. 
     * @param root the root element
     * @param path the path to the element
     * @param attr the name of the attribute
     * @param value the expected value
     */
    private void checkAttribute(Element root, String path, String attr, String value)
    {
        Element elem = findElement(root, path);
        assertEquals(value, elem.attributeValue(attr));
    }
    
    /**
     * Helper method for searching an element in an document.
     * @param root the root element
     * @param path the path for the element
     * @return the found element
     */
    private Element findElement(Element root, String path)
    {
        ConfigurationKey.KeyIterator keyIt =
        new ConfigurationKey(path).iterator();
        Element e = root;
        
        while(keyIt.hasNext())
        {
            List elems = e.elements(keyIt.nextKey());
            assertNotNull(elems);
            int idx = (keyIt.hasIndex()) ? keyIt.getIndex() : 0;
            assertTrue(idx >= 0 && idx < elems.size());
            e = (Element) elems.get(idx);    
        }  /* while */
        
        return e;
    }
    
    /**
     * Adds a new table object. Called by Digester.
     * @param table the new table
     */
    public void addTable(Table table)
    {
        tables.add(table);
    }
    
    public void testCallDigesterComplex() throws Exception
    {
        Digester digester = 
            new Digester(configDoc.createXMLReader("database.tables"));
        digester.addObjectCreate("config/table", Table.class);
        digester.addSetProperties("config/table");
        digester.addCallMethod("config/table/name", "setName", 0);
        digester.addObjectCreate("config/table/fields/field", TableField.class);
        digester.addCallMethod("config/table/fields/field/name",
            "setName", 0);
        digester.addCallMethod("config/table/fields/field/type",
            "setType", 0);
        digester.addSetNext("config/table/fields/field",
            "addField", TableField.class.getName());
        digester.addSetNext("config/table", "addTable", Table.class.getName());
        
        digester.push(this);
        digester.parse("config");
        
        assertEquals(3, tables.size());
        Table table = (Table) tables.get(0);
        assertEquals("system", table.getTableType());
        assertEquals("users", table.getName());
        assertEquals(5, table.size());
        TableField field = table.getField(1);
        assertEquals("uname", field.getName());
        assertEquals(String.class.getName(), field.getType());
        
        table = (Table) tables.get(2);
        assertEquals("application", table.getTableType());
        assertEquals("tasks", table.getName());
        assertEquals(7, table.size());
        field = table.getField(4);
        assertEquals("creatorID", field.getName());
        assertEquals("long", field.getType());
    }
    
    public void testWrite() throws Exception
    {
        StringWriter out;
        String doc;
        
        out = new StringWriter();
        configDoc.write(out, "database.tables");
        doc = out.toString();
        assertEquals(3, countElements("config/table", doc));
        assertEquals(17, countElements("config/table/fields/field", doc));
        out = new StringWriter();
        configDoc.write(out, "database.tables", false);
        assertTrue(out.toString().length() <= doc.length());
        
        out = new StringWriter();
        configDoc.write(out, "database.tables.table(2)", "table");
        doc = out.toString();
        assertEquals(1, countElements("table", doc));
        assertEquals(7, countElements("table/fields/field", doc));
        out = new StringWriter();
        configDoc.write(out, "database.tables.table(2)", "table", false);
        assertTrue(out.toString().length() <= doc.length());
    }
    
    /**
     * Helper method for counting the number of occurrences of a certain
     * element constellation in a XML string.
     * @param match the match string for the element constellation
     * @param doc the XML as string
     * @return the number of occurrences
     * @throws Exception if an error occurs
     */
    private int countElements(String match, String doc) throws Exception
    {
        Digester digester = new Digester();
        digester.addCallMethod(match, "incrementCounter");
        digester.push(this);
        counter = 0;
        digester.parse(new StringReader(doc));
        return counter;
    }
    
    /**
     * Increments the internal counter. Called by Digester during testing.
     */
    public void incrementCounter()
    {
        counter++;
    }
    
    /**
     * An inner class for connection information. An instance will be
     * created by Digester.
     */
    public static class ConnectionData
    {
        private String dsn;
        private String user;
        private String passwd;
        
        public String getDsn()
        {
            return dsn;
        }

        public String getPasswd()
        {
            return passwd;
        }

        public String getUser()
        {
            return user;
        }

        public void setDsn(String string)
        {
            dsn = string;
        }

        public void setPasswd(String string)
        {
            passwd = string;
        }

        public void setUser(String string)
        {
            user = string;
        }
    }
    
    /**
     * A simple bean class for storing information about a table field.
     * Used for the Digester test.
     */
    public static class TableField
    {
        private String name;
        private String type;
        
        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }

        public void setName(String string)
        {
            name = string;
        }

        public void setType(String string)
        {
            type = string;
        }
    }
    
    /**
     * A simple bean class for storing information about a database table.
     * Used for the Digester test.
     */
    public static class Table
    {
        /** Stores the list of fields.*/
        private ArrayList fields;
        
        /** Stores the table name.*/
        private String name;
        
        /** Stores the table type.*/
        private String tableType;
        
        public Table()
        {
            fields = new ArrayList();
        }
        
        public String getName()
        {
            return name;
        }

        public String getTableType()
        {
            return tableType;
        }

        public void setName(String string)
        {
            name = string;
        }

        public void setTableType(String string)
        {
            tableType = string;
        }
        
        /**
         * Adds a field.
         * @param field the new field
         */
        public void addField(TableField field)
        {
            fields.add(field);
        }
        
        /**
         * Returns the field with the given index.
         * @param idx the index
         * @return the field with this index
         */
        public TableField getField(int idx)
        {
            return (TableField) fields.get(idx);
        }
        
        /**
         * Returns the number of fields.
         * @return the number of fields
         */
        public int size()
        {
            return fields.size();
        }
    }
}
