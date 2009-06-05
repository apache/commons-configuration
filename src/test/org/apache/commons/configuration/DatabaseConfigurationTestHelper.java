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
    private static HsqlDB hsqlDB = null;

    /** The data source. */
    private DataSource datasource;

    /**
     * Initializes this helper object. This method can be called from a
     * <code>setUp()</code> method of a unit test class. It creates the database
     * instance if necessary and populates it with test data.
     *
     * @throws Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        if (hsqlDB == null)
        {
            hsqlDB = new HsqlDB(DATABASE_URL, DATABASE_DRIVER,
                    "conf/testdb.script");
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
                "conf/dataset.xml"));

        try
        {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        }
        finally
        {
            connection.close();
        }
    }

    /**
     * Frees the resources used by this helper class. This method can be called
     * by a <code>tearDown()</code> method of a unit test class.
     *
     * @throws Exception if an error occurs
     */
    public void tearDown() throws Exception
    {
        datasource.getConnection().commit();
        datasource.getConnection().close();
    }

    /**
     * Creates a database configuration with default values.
     *
     * @return the configuration
     */
    public DatabaseConfiguration setUpConfig()
    {
        return new DatabaseConfiguration(datasource, TABLE, COL_KEY, COL_VALUE);
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table with default values.
     *
     * @return the configuration
     */
    public DatabaseConfiguration setUpMultiConfig()
    {
        return new DatabaseConfiguration(datasource, TABLE_MULTI, COL_NAME,
                COL_KEY, COL_VALUE, CONFIG_NAME);
    }

    /**
     * Returns the <code>DataSource</code> managed by this class.
     *
     * @return the <code>DataSource</code>
     */
    public DataSource getDatasource()
    {
        return datasource;
    }
}
