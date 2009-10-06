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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;

import org.apache.commons.configuration2.flat.AbstractFlatConfiguration;

/**
 * Configuration stored in a database. The properties are retrieved from a
 * table containing at least one column for the keys, and one column for the
 * values. It's possible to store several configurations in the same table by
 * adding a column containing the name of the configuration. The name of the
 * table and the columns is specified in the constructor.
 *
 * <h4>Example 1 - One configuration per table</h4>
 *
 * <pre>
 * CREATE TABLE myconfig (
 *     `key`   VARCHAR NOT NULL PRIMARY KEY,
 *     `value` VARCHAR
 * );
 *
 * INSERT INTO myconfig (key, value) VALUES ('foo', 'bar');
 *
 *
 * Configuration config = new DatabaseConfiguration(datasource, "myconfig", "key", "value");
 * String value = config.getString("foo");
 * </pre>
 *
 * <h4>Example 2 - Multiple configurations per table</h4>
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
 *
 * Configuration config1 = new DatabaseConfiguration(datasource, "myconfigs", "name", "key", "value", "config1");
 * String value1 = conf.getString("key1");
 *
 * Configuration config2 = new DatabaseConfiguration(datasource, "myconfigs", "name", "key", "value", "config2");
 * String value2 = conf.getString("key2");
 * </pre>
 * <h1>Note: Like JDBC itself, protection against SQL injection is left to the user.</h1>
 *
 * @since 1.0
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision$, $Date$
 */
public class DatabaseConfiguration extends AbstractFlatConfiguration
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

    /** The datasource to connect to the database. */
    private DataSource datasource;

    /** The name of the table containing the configurations. */
    private String table;

    /** The column containing the name of the configuration. */
    private String nameColumn;

    /** The column containing the keys. */
    private String keyColumn;

    /** The column containing the values. */
    private String valueColumn;

    /** The name of the configuration. */
    private String name;

    /**
     * Build a configuration from a table containing multiple configurations.
     *
     * @param datasource    the datasource to connect to the database
     * @param table         the name of the table containing the configurations
     * @param nameColumn    the column containing the name of the configuration
     * @param keyColumn     the column containing the keys of the configuration
     * @param valueColumn   the column containing the values of the configuration
     * @param name          the name of the configuration
     */
    public DatabaseConfiguration(DataSource datasource, String table, String nameColumn,
            String keyColumn, String valueColumn, String name)
    {
        this.datasource = datasource;
        this.table = table;
        this.nameColumn = nameColumn;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.name = name;
        setLogger(LogFactory.getLog(getClass().getName()));
        addErrorLogListener();  // log errors per default
    }

    /**
     * Build a configuration from a table.-
     *
     * @param datasource    the datasource to connect to the database
     * @param table         the name of the table containing the configurations
     * @param keyColumn     the column containing the keys of the configuration
     * @param valueColumn   the column containing the values of the configuration
     */
    public DatabaseConfiguration(DataSource datasource, String table, String keyColumn, String valueColumn)
    {
        this(datasource, table, null, keyColumn, valueColumn, null);
    }

    /**
     * Returns the value of the specified property. If this causes a database
     * error, an error event will be generated of type
     * <code>EVENT_READ_PROPERTY</code> with the causing exception. The
     * event's <code>propertyName</code> is set to the passed in property key,
     * the <code>propertyValue</code> is undefined.
     *
     * @param key the key of the desired property
     * @return the value of this property
     */
    public Object getProperty(final String key)
    {
        JdbcOperation op = new JdbcOperation(EVENT_READ_PROPERTY, key, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement pstmt = initStatement(String.format(
                        SQL_GET_PROPERTY, table, keyColumn), true, key);
                ResultSet rs = pstmt.executeQuery();

                List<Object> results = new ArrayList<Object>();
                while (rs.next())
                {
                    Object value = rs.getObject(valueColumn);
                    if (isDelimiterParsingDisabled())
                    {
                        results.add(value);
                    }
                    else
                    {
                        // Split value if it contains the list delimiter
                        results.addAll(PropertyConverter.flatten(value,
                                getListDelimiter()));
                    }
                }

                if (!results.isEmpty())
                {
                    return (results.size() > 1) ? results : results.get(0);
                }
                else
                {
                    return null;
                }
            }
        };

        return op.execute();
    }

    /**
     * Adds a property to this configuration. If this causes a database error,
     * an error event will be generated of type <code>EVENT_ADD_PROPERTY</code>
     * with the causing exception. The event's <code>propertyName</code> is
     * set to the passed in property key, the <code>propertyValue</code>
     * points to the passed in value.
     *
     * @param key the property key
     * @param obj the value of the property to add
     */
    @Override
    protected void addPropertyDirect(final String key, final Object obj)
    {
        new JdbcOperation(EVENT_ADD_PROPERTY, key, obj)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                StringBuilder query = new StringBuilder("INSERT INTO ");
                query.append(table).append(" (");
                query.append(keyColumn).append(", ");
                query.append(valueColumn);
                if (nameColumn != null)
                {
                    query.append(", ").append(nameColumn);
                }
                query.append(") VALUES (?, ?");
                if (nameColumn != null)
                {
                    query.append(", ?");
                }
                query.append(")");

                PreparedStatement pstmt = initStatement(query.toString(),
                        false, key, String.valueOf(obj));
                if (nameColumn != null)
                {
                    pstmt.setString(3, name);
                }

                pstmt.executeUpdate();
                return null;
            }
        }.execute();
    }

    /**
     * Adds a property to this configuration. This implementation will
     * temporarily disable list delimiter parsing, so that even if the value
     * contains the list delimiter, only a single record will be written into
     * the managed table. The implementation of <code>getProperty()</code>
     * will take care about delimiters. So list delimiters are fully supported
     * by <code>DatabaseConfiguration</code>, but internally treated a bit
     * differently.
     *
     * @param key the key of the new property
     * @param value the value to be added
     */
    @Override
    public void addProperty(String key, Object value)
    {
        boolean parsingFlag = isDelimiterParsingDisabled();
        try
        {
            if (value instanceof String)
            {
                // temporarily disable delimiter parsing
                setDelimiterParsingDisabled(true);
            }
            super.addProperty(key, value);
        }
        finally
        {
            setDelimiterParsingDisabled(parsingFlag);
        }
    }

    /**
     * Checks if this configuration is empty. If this causes a database error,
     * an error event will be generated of type <code>EVENT_READ_PROPERTY</code>
     * with the causing exception. Both the event's <code>propertyName</code>
     * and <code>propertyValue</code> will be undefined.
     *
     * @return a flag whether this configuration is empty.
     */
    public boolean isEmpty()
    {
        JdbcOperation op = new JdbcOperation(EVENT_READ_PROPERTY, null, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement ps = initStatement(String.format(
                        SQL_IS_EMPTY, table), true);
                ResultSet rs = ps.executeQuery();

                return rs.next() ? rs.getInt(1) : null;
            }
        };

        Integer count = (Integer) op.execute();
        return count == null || count.intValue() == 0;
    }

    /**
     * Checks whether this configuration contains the specified key. If this
     * causes a database error, an error event will be generated of type
     * <code>EVENT_READ_PROPERTY</code> with the causing exception. The event's
     * <code>propertyName</code> will be set to the passed in key, the
     * <code>propertyValue</code> will be undefined.
     *
     * @param key the key to be checked
     * @return a flag whether this key is defined
     */
    public boolean containsKey(final String key)
    {
        JdbcOperation op = new JdbcOperation(EVENT_READ_PROPERTY, key, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement pstmt = initStatement(String.format(
                        SQL_GET_PROPERTY, table, keyColumn), true, key);
                ResultSet rs = pstmt.executeQuery();

                return rs.next();
            }
        };

        Boolean result = (Boolean) op.execute();
        return result != null && result.booleanValue();
    }

    /**
     * Removes the specified value from this configuration. If this causes a
     * database error, an error event will be generated of type
     * <code>EVENT_CLEAR_PROPERTY</code> with the causing exception. The event's
     * <code>propertyName</code> will be set to the passed in key, the
     * <code>propertyValue</code> will be undefined.
     *
     * @param key the key of the property to be removed
     */
    @Override
    protected void clearPropertyDirect(final String key)
    {
        new JdbcOperation(EVENT_CLEAR_PROPERTY, key, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement ps = initStatement(String.format(
                        SQL_CLEAR_PROPERTY, table, keyColumn), true, key);
                return ps.executeUpdate();
            }
        }.execute();
    }

    /**
     * Removes all entries from this configuration. If this causes a database
     * error, an error event will be generated of type <code>EVENT_CLEAR</code>
     * with the causing exception. Both the event's <code>propertyName</code>
     * and the <code>propertyValue</code> will be undefined.
     */
    @Override
    public void clear()
    {
        fireEvent(EVENT_CLEAR, null, null, true);
        new JdbcOperation(EVENT_CLEAR, null, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement ps = initStatement(String.format(SQL_CLEAR,
                        table), true);
                return ps.executeUpdate();
            }
        }.execute();
        fireEvent(EVENT_CLEAR, null, null, false);
    }

    /**
     * Returns an iterator with the names of all properties contained in this
     * configuration. If this causes a database error, an error event will be
     * generated of type <code>EVENT_READ_PROPERTY</code> with the causing
     * exception. Both the event's <code>propertyName</code> and the
     * <code>propertyValue</code> will be undefined.
     *
     * @return an iterator with the contained keys (an empty iterator in case of
     *         an error)
     */
    public Iterator<String> getKeys()
    {
        final Collection<String> keys = new ArrayList<String>();

        new JdbcOperation(EVENT_READ_PROPERTY, null, null)
        {
            @Override
            protected Object performOperation() throws SQLException
            {
                PreparedStatement ps = initStatement(String.format(
                        SQL_GET_KEYS, keyColumn, table), true);
                ResultSet rs = ps.executeQuery();

                while (rs.next())
                {
                    keys.add(rs.getString(1));
                }
                return null;
            }
        }.execute();

        return keys.iterator();
    }

    /**
     * Returns the used <code>DataSource</code> object.
     *
     * @return the data source
     * @since 1.4
     */
    public DataSource getDatasource()
    {
        return datasource;
    }

    /**
     * Close a <code>Connection</code> and, <code>Statement</code>.
     * Avoid closing if null and hide any SQLExceptions that occur.
     *
     * @param conn The database connection to close
     * @param stmt The statement to close
     */
    private void close(Connection conn, Statement stmt)
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
        }
        catch (SQLException e)
        {
            getLogger().error( "An error occured on closing the statement", e);
        }

        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            getLogger().error( "An error occured on closing the connection", e);
        }
    }

    /**
     * An internally used helper class for simplifying database access through
     * plain JDBC. This class provides a simple framework for creating and
     * executing a JDBC statement. It especially takes care of proper handling
     * of JDBC resources even in case of an error.
     */
    private abstract class JdbcOperation
    {
        /** Stores the connection. */
        private Connection conn;

        /** Stores the statement. */
        private PreparedStatement pstmt;

        /** The type of the event to send in case of an error. */
        private final int errorEventType;

        /** The property name for an error event. */
        private final String errorPropertyName;

        /** The property value for an error event. */
        private final Object errorPropertyValue;

        /**
         * Creates a new instance of <code>JdbcOperation</code> and initializes
         * the properties related to the error event.
         *
         * @param errEvType the type of the error event
         * @param errPropName the property name for the error event
         * @param errPropVal the property value for the error event
         */
        protected JdbcOperation(int errEvType, String errPropName,
                Object errPropVal)
        {
            errorEventType = errEvType;
            errorPropertyName = errPropName;
            errorPropertyValue = errPropVal;
        }

        /**
         * Executes this operation. This method obtains a database connection
         * and then delegates to <code>performOperation()</code>. Afterwards it
         * performs the necessary clean up. Exceptions that are thrown during
         * the JDBC operation are caught and transformed into configuration
         * error events.
         *
         * @return the result of the operation
         */
        public Object execute()
        {
            Object result = null;

            try
            {
                conn = getDatasource().getConnection();
                result = performOperation();
            }
            catch (SQLException e)
            {
                fireError(errorEventType, errorPropertyName,
                        errorPropertyValue, e);
            }
            finally
            {
                close(conn, pstmt);
            }

            return result;
        }

        /**
         * Returns the current connection. This method can be called while
         * <code>execute()</code> is running. It returns <b>null</b> otherwise.
         *
         * @return the current connection
         */
        protected Connection getConnection()
        {
            return conn;
        }

        /**
         * Creates a <code>PreparedStatement</code> object for executing the
         * specified SQL statement.
         *
         * @param sql the statement to be executed
         * @param nameCol a flag whether the name column should be taken into
         *        account
         * @return the prepared statement object
         * @throws SQLException if an SQL error occurs
         */
        protected PreparedStatement createStatement(String sql, boolean nameCol)
                throws SQLException
        {
            String statement;
            if (nameCol && nameColumn != null)
            {
                StringBuilder buf = new StringBuilder(sql);
                buf.append(" AND ").append(nameColumn).append("=?");
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
         * Creates an initializes a <code>PreparedStatement</code> object for
         * executing an SQL statement. This method first calls
         * <code>createStatement()</code> for creating the statement and then
         * initializes the statement's parameters.
         *
         * @param sql the statement to be executed
         * @param nameCol a flag whether the name column should be taken into
         *        account
         * @param params the parameters for the statement
         * @return the initialized statement object
         * @throws SQLException if an SQL error occurs
         */
        protected PreparedStatement initStatement(String sql, boolean nameCol,
                Object... params) throws SQLException
        {
            PreparedStatement ps = createStatement(sql, nameCol);

            int idx = 1;
            for (Object param : params)
            {
                ps.setObject(idx++, param);
            }
            if (nameCol && nameColumn != null)
            {
                ps.setString(idx, name);
            }

            return ps;
        }

        /**
         * Performs the JDBC operation. This method is called by
         * <code>execute()</code> after this object has been fully initialized.
         * Here the actual JDBC logic has to be placed.
         *
         * @return the result of the operation
         * @throws SQLException if an SQL error occurs
         */
        protected abstract Object performOperation() throws SQLException;
    }
}
