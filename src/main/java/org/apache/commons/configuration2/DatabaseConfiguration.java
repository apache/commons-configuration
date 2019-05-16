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

import javax.sql.DataSource;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.lang3.StringUtils;

/**
 * Configuration stored in a database. The properties are retrieved from a
 * table containing at least one column for the keys, and one column for the
 * values. It's possible to store several configurations in the same table by
 * adding a column containing the name of the configuration. The name of the
 * table and the columns have to be specified using the corresponding
 * properties.
 * <p>
 * The recommended way to create an instance of {@code DatabaseConfiguration}
 * is to use a <em>configuration builder</em>. The builder is configured with
 * a special parameters object defining the database structures used by the
 * configuration. Such an object can be created using the {@code database()}
 * method of the {@code Parameters} class. See the examples below for more
 * details.
 * </p>
 *
 * <p>
 * <strong>Example 1 - One configuration per table</strong>
 * </p>
 *
 * <pre>
 * CREATE TABLE myconfig (
 *     `key`   VARCHAR NOT NULL PRIMARY KEY,
 *     `value` VARCHAR
 * );
 *
 * INSERT INTO myconfig (key, value) VALUES ('foo', 'bar');
 *
 * BasicConfigurationBuilder&lt;DatabaseConfiguration&gt; builder =
 *     new BasicConfigurationBuilder&lt;DatabaseConfiguration&gt;(DatabaseConfiguration.class);
 * builder.configure(
 *     Parameters.database()
 *         .setDataSource(dataSource)
 *         .setTable("myconfig")
 *         .setKeyColumn("key")
 *         .setValueColumn("value")
 * );
 * Configuration config = builder.getConfiguration();
 * String value = config.getString("foo");
 * </pre>
 *
 * <p>
 * <strong>Example 2 - Multiple configurations per table</strong>
 * </p>
 *
 * <pre>
 * CREATE TABLE myconfigs (
 *     `name`  VARCHAR NOT NULL,
 *     `key`   VARCHAR NOT NULL,
 *     `value` VARCHAR,
 *     CONSTRAINT sys_pk_myconfigs PRIMARY KEY (`name`, `key`)
 * );
 *
 * INSERT INTO myconfigs (name, key, value) VALUES ('config1', 'key1', 'value1');
 * INSERT INTO myconfigs (name, key, value) VALUES ('config2', 'key2', 'value2');
 *
 * BasicConfigurationBuilder&lt;DatabaseConfiguration&gt; builder =
 *     new BasicConfigurationBuilder&lt;DatabaseConfiguration&gt;(DatabaseConfiguration.class);
 * builder.configure(
 *     Parameters.database()
 *         .setDataSource(dataSource)
 *         .setTable("myconfigs")
 *         .setKeyColumn("key")
 *         .setValueColumn("value")
 *         .setConfigurationNameColumn("name")
 *         .setConfigurationName("config1")
 * );
 * Configuration config1 = new DatabaseConfiguration(dataSource, "myconfigs", "name", "key", "value", "config1");
 * String value1 = conf.getString("key1");
 * </pre>
 * The configuration can be instructed to perform commits after database updates.
 * This is achieved by setting the {@code commits} parameter of the
 * constructors to <b>true</b>. If commits should not be performed (which is the
 * default behavior), it should be ensured that the connections returned by the
 * {@code DataSource} are in auto-commit mode.
 *
 * <h1>Note: Like JDBC itself, protection against SQL injection is left to the user.</h1>
 * @since 1.0
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 */
public class DatabaseConfiguration extends AbstractConfiguration
{
    /** Constant for the statement used by getProperty.*/
    private static final String SQL_GET_PROPERTY = "SELECT * FROM %s WHERE %s =?";

    /** Constant for the statement used by isEmpty.*/
    private static final String SQL_IS_EMPTY = "SELECT count(*) FROM %s WHERE 1 = 1";

    /** Constant for the statement used by clearProperty.*/
    private static final String SQL_CLEAR_PROPERTY = "DELETE FROM %s WHERE %s =?";

    /** Constant for the statement used by clear.*/
    private static final String SQL_CLEAR = "DELETE FROM %s WHERE 1 = 1";

    /** Constant for the statement used by getKeys.*/
    private static final String SQL_GET_KEYS = "SELECT DISTINCT %s FROM %s WHERE 1 = 1";

    /** The data source to connect to the database. */
    private DataSource dataSource;

    /** The configurationName of the table containing the configurations. */
    private String table;

    /** The column containing the configurationName of the configuration. */
    private String configurationNameColumn;

    /** The column containing the keys. */
    private String keyColumn;

    /** The column containing the values. */
    private String valueColumn;

    /** The configurationName of the configuration. */
    private String configurationName;

    /** A flag whether commits should be performed by this configuration. */
    private boolean autoCommit;

    /**
     * Creates a new instance of {@code DatabaseConfiguration}.
     */
    public DatabaseConfiguration()
    {
        initLogger(new ConfigurationLogger(DatabaseConfiguration.class));
        addErrorLogListener();
    }

    /**
     * Returns the {@code DataSource} for obtaining database connections.
     *
     * @return the {@code DataSource}
     */
    public DataSource getDataSource()
    {
        return dataSource;
    }

    /**
     * Sets the {@code DataSource} for obtaining database connections.
     *
     * @param dataSource the {@code DataSource}
     */
    public void setDataSource(final DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Returns the name of the table containing configuration data.
     *
     * @return the name of the table to be queried
     */
    public String getTable()
    {
        return table;
    }

    /**
     * Sets the name of the table containing configuration data.
     *
     * @param table the table name
     */
    public void setTable(final String table)
    {
        this.table = table;
    }

    /**
     * Returns the name of the table column with the configuration name.
     *
     * @return the name of the configuration name column
     */
    public String getConfigurationNameColumn()
    {
        return configurationNameColumn;
    }

    /**
     * Sets the name of the table column with the configuration name.
     *
     * @param configurationNameColumn the name of the column with the
     *        configuration name
     */
    public void setConfigurationNameColumn(final String configurationNameColumn)
    {
        this.configurationNameColumn = configurationNameColumn;
    }

    /**
     * Returns the name of the column containing the configuration keys.
     *
     * @return the name of the key column
     */
    public String getKeyColumn()
    {
        return keyColumn;
    }

    /**
     * Sets the name of the column containing the configuration keys.
     *
     * @param keyColumn the name of the key column
     */
    public void setKeyColumn(final String keyColumn)
    {
        this.keyColumn = keyColumn;
    }

    /**
     * Returns the name of the column containing the configuration values.
     *
     * @return the name of the value column
     */
    public String getValueColumn()
    {
        return valueColumn;
    }

    /**
     * Sets the name of the column containing the configuration values.
     *
     * @param valueColumn the name of the value column
     */
    public void setValueColumn(final String valueColumn)
    {
        this.valueColumn = valueColumn;
    }

    /**
     * Returns the name of this configuration instance.
     *
     * @return the name of this configuration
     */
    public String getConfigurationName()
    {
        return configurationName;
    }

    /**
     * Sets the name of this configuration instance.
     *
     * @param configurationName the name of this configuration
     */
    public void setConfigurationName(final String configurationName)
    {
        this.configurationName = configurationName;
    }

    /**
     * Returns a flag whether this configuration performs commits after database
     * updates.
     *
     * @return a flag whether commits are performed
     */
    public boolean isAutoCommit()
    {
        return autoCommit;
    }

    /**
     * Sets the auto commit flag. If set to <b>true</b>, this configuration
     * performs a commit after each database update.
     *
     * @param autoCommit the auto commit flag
     */
    public void setAutoCommit(final boolean autoCommit)
    {
        this.autoCommit = autoCommit;
    }

    /**
     * Returns the value of the specified property. If this causes a database
     * error, an error event will be generated of type
     * {@code READ} with the causing exception. The
     * event's {@code propertyName} is set to the passed in property key,
     * the {@code propertyValue} is undefined.
     *
     * @param key the key of the desired property
     * @return the value of this property
     */
    @Override
    protected Object getPropertyInternal(final String key)
    {
        final JdbcOperation<Object> op =
                new JdbcOperation<Object>(ConfigurationErrorEvent.READ,
                        ConfigurationErrorEvent.READ, key, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                final List<Object> results = new ArrayList<>();
                try (final ResultSet rs =
                        openResultSet(String.format(SQL_GET_PROPERTY,
                                table, keyColumn), true, key))
                {
                    while (rs.next())
                    {
                        final Object value = extractPropertyValue(rs);
                        // Split value if it contains the list delimiter
                        for (final Object o : getListDelimiterHandler().parse(value))
                        {
                            results.add(o);
                        }
                    }
                }
                if (!results.isEmpty())
                {
                    return (results.size() > 1) ? results : results
                            .get(0);
                }
                return null;
            }
        };

        return op.execute();
    }

    /**
     * Adds a property to this configuration. If this causes a database error,
     * an error event will be generated of type {@code ADD_PROPERTY}
     * with the causing exception. The event's {@code propertyName} is
     * set to the passed in property key, the {@code propertyValue}
     * points to the passed in value.
     *
     * @param key the property key
     * @param obj the value of the property to add
     */
    @Override
    protected void addPropertyDirect(final String key, final Object obj)
    {
        new JdbcOperation<Void>(ConfigurationErrorEvent.WRITE,
                ConfigurationEvent.ADD_PROPERTY, key, obj)
        {
            @Override
            protected Void performOperation() throws SQLException
            {
                final StringBuilder query = new StringBuilder("INSERT INTO ");
                query.append(table).append(" (");
                query.append(keyColumn).append(", ");
                query.append(valueColumn);
                if (configurationNameColumn != null)
                {
                    query.append(", ").append(configurationNameColumn);
                }
                query.append(") VALUES (?, ?");
                if (configurationNameColumn != null)
                {
                    query.append(", ?");
                }
                query.append(")");

                try (final PreparedStatement pstmt = initStatement(query.toString(),
                        false, key, String.valueOf(obj)))
                {
                    if (configurationNameColumn != null)
                    {
                        pstmt.setString(3, configurationName);
                    }

                    pstmt.executeUpdate();
                    return null;
                }
            }
        }
        .execute();
    }

    /**
     * Adds a property to this configuration. This implementation
     * temporarily disables list delimiter parsing, so that even if the value
     * contains the list delimiter, only a single record is written into
     * the managed table. The implementation of {@code getProperty()}
     * takes care about delimiters. So list delimiters are fully supported
     * by {@code DatabaseConfiguration}, but internally treated a bit
     * differently.
     *
     * @param key the key of the new property
     * @param value the value to be added
     */
    @Override
    protected void addPropertyInternal(final String key, final Object value)
    {
        final ListDelimiterHandler oldHandler = getListDelimiterHandler();
        try
        {
            // temporarily disable delimiter parsing
            setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
            super.addPropertyInternal(key, value);
        }
        finally
        {
            setListDelimiterHandler(oldHandler);
        }
    }

    /**
     * Checks if this configuration is empty. If this causes a database error,
     * an error event will be generated of type {@code READ}
     * with the causing exception. Both the event's {@code propertyName}
     * and {@code propertyValue} will be undefined.
     *
     * @return a flag whether this configuration is empty.
     */
    @Override
    protected boolean isEmptyInternal()
    {
        final JdbcOperation<Integer> op =
                new JdbcOperation<Integer>(ConfigurationErrorEvent.READ,
                        ConfigurationErrorEvent.READ, null, null)
        {
            @Override
            protected Integer performOperation() throws SQLException
            {
                try (final ResultSet rs = openResultSet(String.format(
                        SQL_IS_EMPTY, table), true))
                {
                    return rs.next() ? Integer.valueOf(rs.getInt(1)) : null;
                }
            }
        };

        final Integer count = op.execute();
        return count == null || count.intValue() == 0;
    }

    /**
     * Checks whether this configuration contains the specified key. If this
     * causes a database error, an error event will be generated of type
     * {@code READ} with the causing exception. The
     * event's {@code propertyName} will be set to the passed in key, the
     * {@code propertyValue} will be undefined.
     *
     * @param key the key to be checked
     * @return a flag whether this key is defined
     */
    @Override
    protected boolean containsKeyInternal(final String key)
    {
        final JdbcOperation<Boolean> op =
                new JdbcOperation<Boolean>(ConfigurationErrorEvent.READ,
                        ConfigurationErrorEvent.READ, key, null)
        {
            @Override
            protected Boolean performOperation() throws SQLException
            {
                try (final ResultSet rs = openResultSet(
                        String.format(SQL_GET_PROPERTY, table, keyColumn), true, key))
                {
                    return rs.next();
                }
            }
        };

        final Boolean result = op.execute();
        return result != null && result.booleanValue();
    }

    /**
     * Removes the specified value from this configuration. If this causes a
     * database error, an error event will be generated of type
     * {@code CLEAR_PROPERTY} with the causing exception. The
     * event's {@code propertyName} will be set to the passed in key, the
     * {@code propertyValue} will be undefined.
     *
     * @param key the key of the property to be removed
     */
    @Override
    protected void clearPropertyDirect(final String key)
    {
        new JdbcOperation<Void>(ConfigurationErrorEvent.WRITE,
                ConfigurationEvent.CLEAR_PROPERTY, key, null)
        {
            @Override
            protected Void performOperation() throws SQLException
            {
                try (final PreparedStatement ps = initStatement(String.format(
                        SQL_CLEAR_PROPERTY, table, keyColumn), true, key))
                {
                    ps.executeUpdate();
                    return null;
                }
            }
        }
        .execute();
    }

    /**
     * Removes all entries from this configuration. If this causes a database
     * error, an error event will be generated of type
     * {@code CLEAR} with the causing exception. Both the
     * event's {@code propertyName} and the {@code propertyValue}
     * will be undefined.
     */
    @Override
    protected void clearInternal()
    {
        new JdbcOperation<Void>(ConfigurationErrorEvent.WRITE,
                ConfigurationEvent.CLEAR, null, null)
        {
            @Override
            protected Void performOperation() throws SQLException
            {
                initStatement(String.format(SQL_CLEAR,
                        table), true).executeUpdate();
                return null;
            }
        }
        .execute();
    }

    /**
     * Returns an iterator with the names of all properties contained in this
     * configuration. If this causes a database
     * error, an error event will be generated of type
     * {@code READ} with the causing exception. Both the
     * event's {@code propertyName} and the {@code propertyValue}
     * will be undefined.
     * @return an iterator with the contained keys (an empty iterator in case
     * of an error)
     */
    @Override
    protected Iterator<String> getKeysInternal()
    {
        final Collection<String> keys = new ArrayList<>();
        new JdbcOperation<Collection<String>>(ConfigurationErrorEvent.READ,
                ConfigurationErrorEvent.READ, null, null)
        {
            @Override
            protected Collection<String> performOperation() throws SQLException
            {
                try (final ResultSet rs = openResultSet(String.format(
                        SQL_GET_KEYS, keyColumn, table), true))
                {
                    while (rs.next())
                    {
                        keys.add(rs.getString(1));
                    }
                    return keys;
                }
            }
        }
        .execute();

        return keys.iterator();
    }

    /**
     * Returns the used {@code DataSource} object.
     *
     * @return the data source
     * @since 1.4
     */
    public DataSource getDatasource()
    {
        return dataSource;
    }

    /**
     * Close the specified database objects.
     * Avoid closing if null and hide any SQLExceptions that occur.
     *
     * @param conn The database connection to close
     * @param stmt The statement to close
     * @param rs the result set to close
     */
    protected void close(final Connection conn, final Statement stmt, final ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (final SQLException e)
        {
            getLogger().error("An error occurred on closing the result set", e);
        }

        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
        }
        catch (final SQLException e)
        {
            getLogger().error("An error occured on closing the statement", e);
        }

        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (final SQLException e)
        {
            getLogger().error("An error occured on closing the connection", e);
        }
    }

    /**
     * Extracts the value of a property from the given result set. The passed in
     * {@code ResultSet} was created by a SELECT statement on the underlying
     * database table. This implementation reads the value of the column
     * determined by the {@code valueColumn} property. Normally the contained
     * value is directly returned. However, if it is of type {@code CLOB}, text
     * is extracted as string.
     *
     * @param rs the current {@code ResultSet}
     * @return the value of the property column
     * @throws SQLException if an error occurs
     */
    protected Object extractPropertyValue(final ResultSet rs) throws SQLException
    {
        Object value = rs.getObject(valueColumn);
        if (value instanceof Clob)
        {
            value = convertClob((Clob) value);
        }
        return value;
    }

    /**
     * Converts a CLOB to a string.
     *
     * @param clob the CLOB to be converted
     * @return the extracted string value
     * @throws SQLException if an error occurs
     */
    private static Object convertClob(final Clob clob) throws SQLException
    {
        final int len = (int) clob.length();
        return (len > 0) ? clob.getSubString(1, len) : StringUtils.EMPTY;
    }

    /**
     * An internally used helper class for simplifying database access through
     * plain JDBC. This class provides a simple framework for creating and
     * executing a JDBC statement. It especially takes care of proper handling
     * of JDBC resources even in case of an error.
     * @param <T> the type of the results produced by a JDBC operation
     */
    private abstract class JdbcOperation<T>
    {
        /** Stores the connection. */
        private Connection conn;

        /** Stores the statement. */
        private PreparedStatement pstmt;

        /** Stores the result set. */
        private ResultSet resultSet;

        /** The type of the event to send in case of an error. */
        private final EventType<? extends ConfigurationErrorEvent> errorEventType;

        /** The type of the operation which caused an error. */
        private final EventType<?> operationEventType;

        /** The property configurationName for an error event. */
        private final String errorPropertyName;

        /** The property value for an error event. */
        private final Object errorPropertyValue;

        /**
         * Creates a new instance of {@code JdbcOperation} and initializes the
         * properties related to the error event.
         *
         * @param errEvType the type of the error event
         * @param opType the operation event type
         * @param errPropName the property configurationName for the error event
         * @param errPropVal the property value for the error event
         */
        protected JdbcOperation(
                final EventType<? extends ConfigurationErrorEvent> errEvType,
                final EventType<?> opType, final String errPropName, final Object errPropVal)
        {
            errorEventType = errEvType;
            operationEventType = opType;
            errorPropertyName = errPropName;
            errorPropertyValue = errPropVal;
        }

        /**
         * Executes this operation. This method obtains a database connection
         * and then delegates to {@code performOperation()}. Afterwards it
         * performs the necessary clean up. Exceptions that are thrown during
         * the JDBC operation are caught and transformed into configuration
         * error events.
         *
         * @return the result of the operation
         */
        public T execute()
        {
            T result = null;

            try
            {
                conn = getDatasource().getConnection();
                result = performOperation();

                if (isAutoCommit())
                {
                    conn.commit();
                }
            }
            catch (final SQLException e)
            {
                fireError(errorEventType, operationEventType, errorPropertyName,
                        errorPropertyValue, e);
            }
            finally
            {
                close(conn, pstmt, resultSet);
            }

            return result;
        }

        /**
         * Returns the current connection. This method can be called while
         * {@code execute()} is running. It returns <b>null</b> otherwise.
         *
         * @return the current connection
         */
        protected Connection getConnection()
        {
            return conn;
        }

        /**
         * Creates a {@code PreparedStatement} object for executing the
         * specified SQL statement.
         *
         * @param sql the statement to be executed
         * @param nameCol a flag whether the configurationName column should be taken into
         *        account
         * @return the prepared statement object
         * @throws SQLException if an SQL error occurs
         */
        protected PreparedStatement createStatement(final String sql, final boolean nameCol)
                throws SQLException
        {
            String statement;
            if (nameCol && configurationNameColumn != null)
            {
                final StringBuilder buf = new StringBuilder(sql);
                buf.append(" AND ").append(configurationNameColumn).append("=?");
                statement = buf.toString();
            }
            else
            {
                statement = sql;
            }

            pstmt = getConnection().prepareStatement(statement);
            return pstmt;
        }

        /**
         * Creates an initializes a {@code PreparedStatement} object for
         * executing an SQL statement. This method first calls
         * {@code createStatement()} for creating the statement and then
         * initializes the statement's parameters.
         *
         * @param sql the statement to be executed
         * @param nameCol a flag whether the configurationName column should be taken into
         *        account
         * @param params the parameters for the statement
         * @return the initialized statement object
         * @throws SQLException if an SQL error occurs
         */
        protected PreparedStatement initStatement(final String sql, final boolean nameCol,
                final Object... params) throws SQLException
        {
            final PreparedStatement ps = createStatement(sql, nameCol);

            int idx = 1;
            for (final Object param : params)
            {
                ps.setObject(idx++, param);
            }
            if (nameCol && configurationNameColumn != null)
            {
                ps.setString(idx, configurationName);
            }

            return ps;
        }

        /**
         * Creates a {@code PreparedStatement} for a query, initializes it and
         * executes it. The resulting {@code ResultSet} is returned.
         *
         * @param sql the statement to be executed
         * @param nameCol a flag whether the configurationName column should be taken into
         *        account
         * @param params the parameters for the statement
         * @return the {@code ResultSet} produced by the query
         * @throws SQLException if an SQL error occurs
         */
        protected ResultSet openResultSet(final String sql, final boolean nameCol,
                final Object... params) throws SQLException
        {
            return resultSet = initStatement(sql, nameCol, params).executeQuery();
        }

        /**
         * Performs the JDBC operation. This method is called by
         * {@code execute()} after this object has been fully initialized.
         * Here the actual JDBC logic has to be placed.
         *
         * @return the result of the operation
         * @throws SQLException if an SQL error occurs
         */
        protected abstract T performOperation() throws SQLException;
    }
}
