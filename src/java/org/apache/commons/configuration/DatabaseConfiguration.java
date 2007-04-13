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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration stored in a database.
 *
 * @since 1.0
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class DatabaseConfiguration extends AbstractConfiguration
{
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
        setLogger(LogFactory.getLog(getClass()));
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
    public Object getProperty(String key)
    {
        Object result = null;

        // build the query
        StringBuffer query = new StringBuffer("SELECT * FROM ");
        query.append(table).append(" WHERE ");
        query.append(keyColumn).append("=?");
        if (nameColumn != null)
        {
            query.append(" AND " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, key);
            if (nameColumn != null)
            {
                pstmt.setString(2, name);
            }

            ResultSet rs = pstmt.executeQuery();

            List results = new ArrayList();
            while (rs.next())
            {
                Object value = rs.getObject(valueColumn);
                if (isDelimiterParsingDisabled())
                {
                    results.add(value);
                }
                else
                {
                    // Split value if it containts the list delimiter
                    CollectionUtils.addAll(results, PropertyConverter.toIterator(value, getListDelimiter()));
                }
            }

            if (!results.isEmpty())
            {
                result = (results.size() > 1) ? results : results.get(0);
            }
        }
        catch (SQLException e)
        {
            fireError(EVENT_READ_PROPERTY, key, null, e);
        }
        finally
        {
            close(conn, pstmt);
        }

        return result;
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
    protected void addPropertyDirect(String key, Object obj)
    {
        // build the query
        StringBuffer query = new StringBuffer("INSERT INTO " + table);
        if (nameColumn != null)
        {
            query.append(" (" + nameColumn + ", " + keyColumn + ", " + valueColumn + ") VALUES (?, ?, ?)");
        }
        else
        {
            query.append(" (" + keyColumn + ", " + valueColumn + ") VALUES (?, ?)");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            int index = 1;
            if (nameColumn != null)
            {
                pstmt.setString(index++, name);
            }
            pstmt.setString(index++, key);
            pstmt.setString(index++, String.valueOf(obj));

            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            fireError(EVENT_ADD_PROPERTY, key, obj, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }
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
        boolean empty = true;

        // build the query
        StringBuffer query = new StringBuffer("SELECT count(*) FROM " + table);
        if (nameColumn != null)
        {
            query.append(" WHERE " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            if (nameColumn != null)
            {
                pstmt.setString(1, name);
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                empty = rs.getInt(1) == 0;
            }
        }
        catch (SQLException e)
        {
            fireError(EVENT_READ_PROPERTY, null, null, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }

        return empty;
    }

    /**
     * Checks whether this configuration contains the specified key. If this
     * causes a database error, an error event will be generated of type
     * <code>EVENT_READ_PROPERTY</code> with the causing exception. The
     * event's <code>propertyName</code> will be set to the passed in key, the
     * <code>propertyValue</code> will be undefined.
     *
     * @param key the key to be checked
     * @return a flag whether this key is defined
     */
    public boolean containsKey(String key)
    {
        boolean found = false;

        // build the query
        StringBuffer query = new StringBuffer("SELECT * FROM " + table + " WHERE " + keyColumn + "=?");
        if (nameColumn != null)
        {
            query.append(" AND " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, key);
            if (nameColumn != null)
            {
                pstmt.setString(2, name);
            }

            ResultSet rs = pstmt.executeQuery();

            found = rs.next();
        }
        catch (SQLException e)
        {
            fireError(EVENT_READ_PROPERTY, key, null, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }

        return found;
    }

    /**
     * Removes the specified value from this configuration. If this causes a
     * database error, an error event will be generated of type
     * <code>EVENT_CLEAR_PROPERTY</code> with the causing exception. The
     * event's <code>propertyName</code> will be set to the passed in key, the
     * <code>propertyValue</code> will be undefined.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        // build the query
        StringBuffer query = new StringBuffer("DELETE FROM " + table + " WHERE " + keyColumn + "=?");
        if (nameColumn != null)
        {
            query.append(" AND " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, key);
            if (nameColumn != null)
            {
                pstmt.setString(2, name);
            }

            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            fireError(EVENT_CLEAR_PROPERTY, key, null, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }
    }

    /**
     * Removes all entries from this configuration. If this causes a database
     * error, an error event will be generated of type
     * <code>EVENT_CLEAR</code> with the causing exception. Both the
     * event's <code>propertyName</code> and the <code>propertyValue</code>
     * will be undefined.
     */
    public void clear()
    {
        // build the query
        StringBuffer query = new StringBuffer("DELETE FROM " + table);
        if (nameColumn != null)
        {
            query.append(" WHERE " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            if (nameColumn != null)
            {
                pstmt.setString(1, name);
            }

            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            fireError(EVENT_CLEAR, null, null, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }
    }

    /**
     * Returns an iterator with the names of all properties contained in this
     * configuration. If this causes a database
     * error, an error event will be generated of type
     * <code>EVENT_READ_PROPERTY</code> with the causing exception. Both the
     * event's <code>propertyName</code> and the <code>propertyValue</code>
     * will be undefined.
     * @return an iterator with the contained keys (an empty iterator in case
     * of an error)
     */
    public Iterator getKeys()
    {
        Collection keys = new ArrayList();

        // build the query
        StringBuffer query = new StringBuffer("SELECT DISTINCT " + keyColumn + " FROM " + table);
        if (nameColumn != null)
        {
            query.append(" WHERE " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            if (nameColumn != null)
            {
                pstmt.setString(1, name);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                keys.add(rs.getString(1));
            }
        }
        catch (SQLException e)
        {
            fireError(EVENT_READ_PROPERTY, null, null, e);
        }
        finally
        {
            // clean up
            close(conn, pstmt);
        }

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
     * Returns a <code>Connection</code> object. This method is called when
     * ever the database is to be accessed. This implementation returns a
     * connection from the current <code>DataSource</code>.
     *
     * @return the <code>Connection</code> object to be used
     * @throws SQLException if an error occurs
     * @since 1.4
     */
    protected Connection getConnection() throws SQLException
    {
        return getDatasource().getConnection();
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
            getLogger().error("An error occured on closing the statement", e);
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
            getLogger().error("An error occured on closing the connection", e);
        }
    }
}
