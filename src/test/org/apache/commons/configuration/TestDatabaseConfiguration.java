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

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * Test for database stored configurations.  Note, when running this Unit 
 * Test in Eclipse it sometimes takes a couple tries.  Otherwise you ma get
 * database is already in use by another process errors.
 *
 * @version $Revision: 1.6 $, $Date: 2004/07/24 16:26:10 $
 */
public class TestDatabaseConfiguration extends TestCase
{
    private DataSource datasource;
    private IDataSet dataSet;	

    protected void setUp() throws Exception
    {
    	/*
    	 * Thread.sleep may or may not help with the database is already in
    	 * use exception.
    	 */
    	//Thread.sleep(1000);
    	
        // set up the datasource
        BasicDataSource datasource = new BasicDataSource();
        datasource.setDriverClassName("org.hsqldb.jdbcDriver");
        datasource.setUrl("jdbc:hsqldb:target/test-classes/testdb");
        datasource.setUsername("sa");
        datasource.setPassword("");

        this.datasource = datasource;
        

        // prepare the database
        IDatabaseConnection connection = new DatabaseConnection(datasource.getConnection());
        dataSet = new XmlDataSet(new FileInputStream("conf/dataset.xml"));

        try
        {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        }
        finally
        {
            connection.close();
        }
    }
    
    protected void tearDown() throws SQLException{
        datasource.getConnection().commit();
        datasource.getConnection().close();
    }

    public void testAddPropertyDirectSingle()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configuration", "key", "value");
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testAddPropertyDirectMultiple()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testGetPropertyDirectSingle()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configuration", "key", "value");

        assertEquals("property1", "value1", config.getPropertyDirect("key1"));
        assertEquals("property2", "value2", config.getPropertyDirect("key2"));
        assertEquals("unknown property", null, config.getPropertyDirect("key3"));
    }

    public void testGetPropertyDirectMultiple()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");

        assertEquals("property1", "value1", config.getProperty("key1"));
        assertEquals("property2", "value2", config.getProperty("key2"));
        assertEquals("unknown property", null, config.getProperty("key3"));
    }

    public void testClearPropertySingle()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configuration", "key", "value");
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testClearPropertyMultiple()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testGetKeysSingle()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configuration", "key", "value");
        Iterator it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testGetKeysMultiple()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");
        Iterator it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testContainsKeySingle()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configuration", "key", "value");
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testContainsKeyMultiple()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testIsEmptySingle()
    {
        DatabaseConfiguration config1 = new DatabaseConfiguration(datasource, "configuration", "key", "value");
        assertFalse("The configuration is empty", config1.isEmpty());
    }

    public void testIsEmptyMultiple()
    {
        DatabaseConfiguration config1 = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "test");
        assertFalse("The configuration named 'test' is empty", config1.isEmpty());

        DatabaseConfiguration config2 = new DatabaseConfiguration(datasource, "configurations", "name", "key", "value", "testIsEmpty");
        assertTrue("The configuration named 'testIsEmpty' is not empty", config2.isEmpty());
    }
    
    public void testGetList()
    {
        DatabaseConfiguration config1 = new DatabaseConfiguration(datasource, "configurationList", "key", "value");
        List list = config1.getList("key3");
        assertEquals(3,list.size());
    }    
    
    public void testGetKeys()
    {
        DatabaseConfiguration config1 = new DatabaseConfiguration(datasource, "configurationList", "key", "value");
        Iterator i = config1.getKeys();
        assertTrue(i.hasNext());
        Object key = i.next();
        assertEquals("key3",key.toString());
        assertFalse(i.hasNext());
    }     

}
