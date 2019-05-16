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
package org.apache.commons.configuration2;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.DatabaseBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.test.HsqlDB;
import org.apache.commons.dbcp2.BasicDataSource;
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
     * The auto-commit mode for the configuration instances created by this
     * helper.
     */
    private boolean autoCommit;

    /**
     * Returns the auto-commit mode of the configuration instances created by
     * this helper.
     *
     * @return the auto-commit mode
     */
    public boolean isAutoCommit()
    {
        return autoCommit;
    }

    /**
     * Sets the auto-commit mode of the configuration instances created by this
     * helper.
     *
     * @param autoCommit the auto-commit mode
     */
    public void setAutoCommit(final boolean autoCommit)
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
        final File script = ConfigurationAssert.getTestFile("testdb.script");
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
     * Returns a parameters object with default settings.
     *
     * @return the parameters object
     */
    public DatabaseBuilderParameters setUpDefaultParameters()
    {
        return new Parameters().database().setDataSource(getDatasource())
                .setTable(TABLE).setKeyColumn(COL_KEY)
                .setValueColumn(COL_VALUE).setAutoCommit(isAutoCommit());
    }

    /**
     * Returns a parameters object with settings for a configuration table
     * containing the data of multiple configurations.
     *
     * @param configName the name of the configuration instance or <b>null</b>
     *        for the default name
     * @return the parameters object
     */
    public DatabaseBuilderParameters setUpMultiParameters(final String configName)
    {
        return setUpDefaultParameters()
                .setTable(TABLE_MULTI)
                .setConfigurationNameColumn(COL_NAME)
                .setConfigurationName(
                        (configName != null) ? configName : CONFIG_NAME);
    }

    /**
     * Creates a configuration instance of the specified class with the given
     * parameters.
     *
     * @param <T> the type of the result configuration
     * @param configCls the configuration class
     * @param params the parameters object
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    public <T extends DatabaseConfiguration> T createConfig(final Class<T> configCls,
            final DatabaseBuilderParameters params) throws ConfigurationException
    {
        return new BasicConfigurationBuilder<>(configCls).configure(params)
                .getConfiguration();
    }

    /**
     * Creates a database configuration with default settings of the specified
     * class.
     *
     * @param <T> the type of the result configuration
     * @param configCls the configuration class
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    public <T extends DatabaseConfiguration> T setUpConfig(final Class<T> configCls)
            throws ConfigurationException
    {
        return createConfig(configCls, setUpDefaultParameters());
    }

    /**
     * Creates a database configuration with default settings.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    public DatabaseConfiguration setUpConfig() throws ConfigurationException
    {
        return setUpConfig(DatabaseConfiguration.class);
    }

    /**
     * Creates a configuration with support for multiple configuration instances
     * in a single table of the specified class.
     *
     * @param <T> the type of the result configuration
     * @param configCls the configuration class
     * @param configName the name of the configuration instance or <b>null</b>
     *        for the default name
     * @return the newly created configuration instance
     * @throws ConfigurationException if an error occurs
     */
    public <T extends DatabaseConfiguration> T setUpMultiConfig(
            final Class<T> configCls, final String configName)
            throws ConfigurationException
    {
        return createConfig(configCls, setUpMultiParameters(configName));
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table with default values.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    public DatabaseConfiguration setUpMultiConfig() throws ConfigurationException
    {
        return setUpMultiConfig(DatabaseConfiguration.class, null);
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
            catch (final Exception ex)
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
        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DATABASE_DRIVER);
        ds.setUrl(DATABASE_URL);
        ds.setUsername(DATABASE_USERNAME);
        ds.setPassword(DATABASE_PASSWORD);
        ds.setDefaultAutoCommit(!isAutoCommit());

        // prepare the database
        final Connection conn = ds.getConnection();
        final IDatabaseConnection connection = new DatabaseConnection(conn);
        final IDataSet dataSet = new XmlDataSet(new FileInputStream(
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
