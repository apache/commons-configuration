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

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.configuration.test.HsqlDB;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * A helper class for performing tests for {@link DatabaseConfiguration}. This
 * class maintains an in-process database that stores configuration data and can
 * be accessed from a {@link DatabaseConfiguration} instance. Constants for
 * table and column names and database connection settings are provided, too.
 *
 * @version $Id$
 */
public class DatabaseConfigurationTestHelper
{
    /** Constant for the JDBC driver class. */
    public final String DATABASE_DRIVER = "org.hsqldb.jdbcDriver";

    /** Constant for the connection URL. */
    public final String DATABASE_URL = "jdbc:hsqldb:mem:testdb";

    /** Constant for the DB user name. */
    public final String DATABASE_USERNAME = "sa";

    /** Constant for the DB password. */
    public final String DATABASE_PASSWORD = "";

    /** Constant for the configuration table. */
    public static final String TABLE = "configuration";

    /** Constant for the multi configuration table. */
    public static final String TABLE_MULTI = "configurations";

    /** Constant for the column with the keys. */
    public static final String COL_KEY = "key";

    /** Constant for the column with the values. */
    public static final String COL_VALUE = "value";

    /** Constant for the column with the configuration name. */
    public static final String COL_NAME = "name";

    /** Constant for the name of the test configuration. */
    public static final String CONFIG_NAME = "test";

    /** Stores the in-process database. */
    private HsqlDB hsqlDB;

    /** The data source. */
    private DataSource datasource;

    /**
     * The auto-commit mode for the connections created by the managed data
     * source.
     */
    private boolean autoCommit = true;

    /**
     * Returns the auto-commit mode of the connections created by the managed
     * data source.
     *
     * @return the auto-commit mode
     */
    public boolean isAutoCommit()
    {
        return autoCommit;
    }

    /**
     * Sets the auto-commit mode of the connections created by the managed data
     * source.
     *
     * @param autoCommit the auto-commit mode
     */
    public void setAutoCommit(boolean autoCommit)
    {
        this.autoCommit = autoCommit;
    }

    /**
     * Initializes this helper object. This method can be called from a
     * {@code setUp()} method of a unit test class. It creates the database
     * instance if necessary.
     *
     * @throws Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        File script = ConfigurationAssert.getTestFile("testdb.script");
        hsqlDB = new HsqlDB(DATABASE_URL, DATABASE_DRIVER, script.getAbsolutePath());
    }

    /**
     * Frees the resources used by this helper class. This method can be called
     * by a {@code tearDown()} method of a unit test class.
     *
     * @throws Exception if an error occurs
     */
    public void tearDown() throws Exception
    {
        if (datasource != null)
        {
            datasource.getConnection().close();
        }
        hsqlDB.close();
    }

    /**
     * Creates a database configuration with default values.
     *
     * @return the configuration
     */
    public DatabaseConfiguration setUpConfig()
    {
        return new DatabaseConfiguration(getDatasource(), TABLE, COL_KEY,
                COL_VALUE, !isAutoCommit());
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table with default values.
     *
     * @return the configuration
     */
    public DatabaseConfiguration setUpMultiConfig()
    {
        return setUpMultiConfig(CONFIG_NAME);
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table and sets the specified configuration name.
     *
     * @param configName the name of the configuration
     * @return the configuration
     */
    public DatabaseConfiguration setUpMultiConfig(String configName)
    {
        return new DatabaseConfiguration(getDatasource(), TABLE_MULTI,
                COL_NAME, COL_KEY, COL_VALUE, configName, !isAutoCommit());
    }

    /**
     * Returns the {@code DataSource} managed by this class. The data
     * source is created on first access.
     *
     * @return the {@code DataSource}
     */
    public DataSource getDatasource()
    {
        if (datasource == null)
        {
            try
            {
                datasource = setUpDataSource();
            }
            catch (Exception ex)
            {
                throw new ConfigurationRuntimeException(
                        "Could not create data source", ex);
            }
        }
        return datasource;
    }

    /**
     * Creates the internal data source. This method also initializes the
     * database.
     *
     * @return the data source
     * @throws Exception if an error occurs
     */
    private DataSource setUpDataSource() throws Exception
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DATABASE_DRIVER);
        ds.setUrl(DATABASE_URL);
        ds.setUsername(DATABASE_USERNAME);
        ds.setPassword(DATABASE_PASSWORD);
        ds.setDefaultAutoCommit(isAutoCommit());

        // prepare the database
        Connection conn = ds.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(conn);
        IDataSet dataSet = new XmlDataSet(new FileInputStream(
                ConfigurationAssert.getTestFile("dataset.xml")));

        try
        {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        }
        finally
        {
            if (!isAutoCommit())
            {
                conn.commit();
            }
            connection.close();
        }

        return ds;
    }
}
