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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration stored in a database.
 *
 * @since 1.0
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.7 $, $Date: 2004/06/24 14:01:03 $
 */
public class DatabaseConfiguration extends AbstractConfiguration
{
    /** Logger */
    private static Log log = LogFactory.getLog(DatabaseConfiguration.class);

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
     * {@inheritDoc}
     */
    protected Object getPropertyDirect(String key)
    {
        Object result = null;

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
            conn = datasource.getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, key);
            if (nameColumn != null)
            {
                pstmt.setString(2, name);
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                result = rs.getObject(valueColumn);
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeQuietly(conn, pstmt);
        }

        return result;
    }

    /**
     * {@inheritDoc}
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
            conn = datasource.getConnection();

            // bind the parameters
            pstmt = conn.prepareStatement(query.toString());
            int index = 1;
            if (nameColumn != null)
            {
                pstmt.setString(index++, name);
            }
            pstmt.setString(index++, key);
            pstmt.setString(index++, (String) obj);

            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            closeQuietly(conn, pstmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        boolean empty = false;

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
            conn = datasource.getConnection();

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
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            closeQuietly(conn, pstmt);
        }

        return empty;
    }

    /**
     * {@inheritDoc}
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
            conn = datasource.getConnection();

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
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            closeQuietly(conn, pstmt);
        }

        return found;
    }

    /**
     * {@inheritDoc}
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
            conn = datasource.getConnection();

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
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            closeQuietly(conn, pstmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys()
    {
        Collection keys = new ArrayList();

        // build the query
        StringBuffer query = new StringBuffer("SELECT " + keyColumn + " FROM " + table);
        if (nameColumn != null)
        {
            query.append(" WHERE " + nameColumn + "=?");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try
        {
            conn = datasource.getConnection();

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
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            closeQuietly(conn, pstmt);
        }

        return keys.iterator();
    }

    /**
     * Close a <code>Connection</code> and, <code>Statement</code>.
     * Avoid closing if null and hide any SQLExceptions that occur.
     *
     * @param conn The database connection to close
     * @param stmt The statement to close
     */
    private void closeQuietly(Connection conn, Statement stmt)
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
