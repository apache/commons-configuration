/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2004 The Apache Software Foundation.  All rights
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

package org.apache.commons.configuration;

import java.io.FileInputStream;
import java.util.Iterator;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * Test for database stored configurations.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/01/18 16:15:46 $
 */
public class TestDatabaseConfiguration extends TestCase
{
    private DataSource datasource;
    private IDataSet dataSet;

    protected void setUp() throws Exception
    {
        // set up the datasource
        BasicDataSource datasource = new BasicDataSource();
        datasource.setDriverClassName("org.hsqldb.jdbcDriver");
        datasource.setUrl("jdbc:hsqldb:conf/testdb");
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

}
