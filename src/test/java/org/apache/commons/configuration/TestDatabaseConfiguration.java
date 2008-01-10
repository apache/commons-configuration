/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.test.HsqlDB;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * Test for database stored configurations.  Note, when running this Unit
 * Test in Eclipse it sometimes takes a couple tries. Otherwise you may get
 * database is already in use by another process errors.
 *
 * @version $Revision$, $Date$
 */
public class TestDatabaseConfiguration extends TestCase
{
    public final String DATABASE_DRIVER = "org.hsqldb.jdbcDriver";
    public final String DATABASE_URL = "jdbc:hsqldb:target/test-classes/testdb";
    public final String DATABASE_USERNAME = "sa";
    public final String DATABASE_PASSWORD = "";

    /** Constant for the configuration table.*/
    private static final String TABLE = "configuration";

    /** Constant for the multi configuration table.*/
    private static final String TABLE_MULTI = "configurations";

    /** Constant for the column with the keys.*/
    private static final String COL_KEY = "key";

    /** Constant for the column with the values.*/
    private static final String COL_VALUE = "value";

    /** Constant for the column with the configuration name.*/
    private static final String COL_NAME = "name";

    /** Constant for the name of the test configuration.*/
    private static final String CONFIG_NAME = "test";

    private static HsqlDB hsqlDB = null;

    private DataSource datasource;

    /** An error listener for testing whether internal errors occurred.*/
    private ConfigurationErrorListenerImpl listener;

    protected void setUp() throws Exception
    {
        /*
         * Thread.sleep may or may not help with the database is already in
         * use exception.
         */
        //Thread.sleep(1000);

        // set up the datasource

        if (hsqlDB == null)
        {
            hsqlDB = new HsqlDB(DATABASE_URL, DATABASE_DRIVER,
                    ConfigurationAssert.getTestFile("testdb.script")
                            .getAbsolutePath());
        }

        BasicDataSource datasource = new BasicDataSource();
        datasource.setDriverClassName(DATABASE_DRIVER);
        datasource.setUrl(DATABASE_URL);
        datasource.setUsername(DATABASE_USERNAME);
        datasource.setPassword(DATABASE_PASSWORD);

        this.datasource = datasource;

        // prepare the database
        IDatabaseConnection connection = new DatabaseConnection(datasource
                .getConnection());
        IDataSet dataSet = new XmlDataSet(new FileInputStream(
                ConfigurationAssert.getTestFile("dataset.xml")));

        try
        {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        }
        finally
        {
            connection.close();
        }
    }

    protected void tearDown() throws Exception{
        datasource.getConnection().commit();
        datasource.getConnection().close();

        // if an error listener is defined, we check whether an error occurred
        if(listener != null)
        {
            assertEquals("An internal error occurred", 0, listener.getErrorCount());
        }
        super.tearDown();
    }

    /**
     * Creates a database configuration with default values.
     *
     * @return the configuration
     */
    private PotentialErrorDatabaseConfiguration setUpConfig()
    {
        return new PotentialErrorDatabaseConfiguration(datasource, TABLE, COL_KEY, COL_VALUE);
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table with default values.
     *
     * @return the configuration
     */
    private DatabaseConfiguration setUpMultiConfig()
    {
        return new DatabaseConfiguration(datasource, TABLE_MULTI, COL_NAME, COL_KEY, COL_VALUE, CONFIG_NAME);
    }

    /**
     * Creates an error listener and adds it to the specified configuration.
     *
     * @param config the configuration
     */
    private void setUpErrorListener(PotentialErrorDatabaseConfiguration config)
    {
        // remove log listener to avoid exception longs
        config.removeErrorListener((ConfigurationErrorListener) config.getErrorListeners().iterator().next());
        listener = new ConfigurationErrorListenerImpl();
        config.addErrorListener(listener);
        config.failOnConnect = true;
    }

    /**
     * Prepares a test for a database error. Sets up a config and registers an
     * error listener.
     *
     * @return the initialized configuration
     */
    private PotentialErrorDatabaseConfiguration setUpErrorConfig()
    {
        PotentialErrorDatabaseConfiguration config = setUpConfig();
        setUpErrorListener(config);
        return config;
    }

    /**
     * Checks the error listener for an expected error. The properties of the
     * error event will be compared with the expected values.
     *
     * @param type the expected type of the error event
     * @param key the expected property key
     * @param value the expected property value
     */
    private void checkErrorListener(int type, String key, Object value)
    {
        listener.verify(type, key, value);
        assertTrue(
                "Wrong event source",
                listener.getLastEvent().getSource() instanceof DatabaseConfiguration);
        assertTrue("Wrong exception",
                listener.getLastEvent().getCause() instanceof SQLException);
        listener = null; // mark as checked
    }

    public void testAddPropertyDirectSingle()
    {
        DatabaseConfiguration config = setUpConfig();
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testAddPropertyDirectMultiple()
    {
        DatabaseConfiguration config = setUpMultiConfig();
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testAddNonStringProperty()
    {
        DatabaseConfiguration config = setUpConfig();
        config.addPropertyDirect("boolean", Boolean.TRUE);

        assertTrue("missing property", config.containsKey("boolean"));
    }

    public void testGetPropertyDirectSingle()
    {
        Configuration config = setUpConfig();

        assertEquals("property1", "value1", config.getProperty("key1"));
        assertEquals("property2", "value2", config.getProperty("key2"));
        assertEquals("unknown property", null, config.getProperty("key3"));
    }

    public void testGetPropertyDirectMultiple()
    {
        Configuration config = setUpMultiConfig();

        assertEquals("property1", "value1", config.getProperty("key1"));
        assertEquals("property2", "value2", config.getProperty("key2"));
        assertEquals("unknown property", null, config.getProperty("key3"));
    }

    public void testClearPropertySingle()
    {
        Configuration config = setUpConfig();
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testClearPropertyMultiple()
    {
        Configuration config = setUpMultiConfig();
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testClearSingle()
    {
        Configuration config = setUpConfig();
        config.clear();

        assertTrue("configuration is not cleared", config.isEmpty());
    }

    public void testClearMultiple()
    {
        Configuration config = setUpMultiConfig();
        config.clear();

        assertTrue("configuration is not cleared", config.isEmpty());
    }

    public void testGetKeysSingle()
    {
        Configuration config = setUpConfig();
        Iterator it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testGetKeysMultiple()
    {
        Configuration config = setUpMultiConfig();
        Iterator it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testContainsKeySingle()
    {
        Configuration config = setUpConfig();
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testContainsKeyMultiple()
    {
        Configuration config = setUpMultiConfig();
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testIsEmptySingle()
    {
        Configuration config1 = setUpConfig();
        assertFalse("The configuration is empty", config1.isEmpty());
    }

    public void testIsEmptyMultiple()
    {
        Configuration config1 = setUpMultiConfig();
        assertFalse("The configuration named 'test' is empty", config1.isEmpty());

        Configuration config2 = new DatabaseConfiguration(datasource, TABLE_MULTI, COL_NAME, COL_KEY, COL_VALUE, "testIsEmpty");
        assertTrue("The configuration named 'testIsEmpty' is not empty", config2.isEmpty());
    }

    public void testGetList()
    {
        Configuration config1 = new DatabaseConfiguration(datasource, "configurationList", COL_KEY, COL_VALUE);
        List list = config1.getList("key3");
        assertEquals(3,list.size());
    }

    public void testGetKeys()
    {
        Configuration config1 = new DatabaseConfiguration(datasource, "configurationList", COL_KEY, COL_VALUE);
        Iterator i = config1.getKeys();
        assertTrue(i.hasNext());
        Object key = i.next();
        assertEquals("key3",key.toString());
        assertFalse(i.hasNext());
    }

    public void testClearSubset()
    {
        Configuration config = setUpConfig();

        Configuration subset = config.subset("key1");
        subset.clear();

        assertTrue("the subset is not empty", subset.isEmpty());
        assertFalse("the parent configuration is empty", config.isEmpty());
    }

    /**
     * Tests whether the configuration has already an error listener registered
     * that is used for logging.
     */
    public void testLogErrorListener()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(datasource, TABLE, COL_KEY, COL_VALUE);
        assertEquals("No error listener registered", 1, config.getErrorListeners().size());
    }

    /**
     * Tests handling of errors in getProperty().
     */
    public void testGetPropertyError()
    {
        setUpErrorConfig().getProperty("key1");
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in addPropertyDirect().
     */
    public void testAddPropertyError()
    {
        setUpErrorConfig().addProperty("key1", "value");
        checkErrorListener(AbstractConfiguration.EVENT_ADD_PROPERTY, "key1", "value");
    }

    /**
     * Tests handling of errors in isEmpty().
     */
    public void testIsEmptyError()
    {
        assertTrue("Wrong return value for failure", setUpErrorConfig().isEmpty());
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null, null);
    }

    /**
     * Tests handling of errors in containsKey().
     */
    public void testContainsKeyError()
    {
        assertFalse("Wrong return value for failure", setUpErrorConfig().containsKey("key1"));
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in clearProperty().
     */
    public void testClearPropertyError()
    {
        setUpErrorConfig().clearProperty("key1");
        checkErrorListener(AbstractConfiguration.EVENT_CLEAR_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in clear().
     */
    public void testClearError()
    {
        setUpErrorConfig().clear();
        checkErrorListener(AbstractConfiguration.EVENT_CLEAR, null, null);
    }

    /**
     * Tests handling of errors in getKeys().
     */
    public void testGetKeysError()
    {
        Iterator it = setUpErrorConfig().getKeys();
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null, null);
        assertFalse("Iteration is not empty", it.hasNext());
    }

    /**
     * Tests obtaining a property as list whose value contains the list
     * delimiter. Multiple values should be returned.
     */
    public void testGetListWithDelimiter()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        List values = config.getList("keyMulti");
        assertEquals("Wrong number of list elements", 3, values.size());
        assertEquals("Wrong list element 0", "a", values.get(0));
        assertEquals("Wrong list element 2", "c", values.get(2));
    }

    /**
     * Tests obtaining a property whose value contains the list delimiter when
     * delimiter parsing is disabled.
     */
    public void testGetListWithDelimiterParsingDisabled()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        config.setDelimiterParsingDisabled(true);
        assertEquals("Wrong value of property", "a;b;c", config.getString("keyMulti"));
    }

    /**
     * Tests adding a property containing the list delimiter. When this property
     * is queried multiple values should be returned.
     */
    public void testAddWithDelimiter()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        config.addProperty("keyList", "1;2;3");
        String[] values = config.getStringArray("keyList");
        assertEquals("Wrong number of property values", 3, values.length);
        assertEquals("Wrong value at index 1", "2", values[1]);
    }

    /**
     * A specialized database configuration implementation that can be
     * configured to throw an exception when obtaining a connection. This way
     * database exceptions can be simulated.
     */
    static class PotentialErrorDatabaseConfiguration extends DatabaseConfiguration
    {
        /** A flag whether a getConnection() call should fail. */
        boolean failOnConnect;

        public PotentialErrorDatabaseConfiguration(DataSource datasource,
                String table, String keyColumn, String valueColumn)
        {
            super(datasource, table, keyColumn, valueColumn);
        }

        protected Connection getConnection() throws SQLException
        {
            if (failOnConnect)
            {
                throw new SQLException("Simulated DB error");
            }
            return super.getConnection();
        }
    }
}
