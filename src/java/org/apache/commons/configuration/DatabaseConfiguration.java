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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration stored in a database.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/01/18 16:15:46 $
 */
public class DatabaseConfiguration extends AbstractConfiguration
{
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

    private static Log log = LogFactory.getLog(DatabaseConfiguration.class);

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
    public DatabaseConfiguration(DataSource datasource, String table, String nameColumn, String keyColumn, String valueColumn, String name)
    {
        this.datasource = datasource;
        this.table = table;
        this.nameColumn = nameColumn;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.name = name;
    }

    /**
     * Build a configuration from a table.
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
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return result;
    }

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
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

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
                empty = (rs.getInt(1) == 0);
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            // clean up
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return empty;
    }

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
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return found;
    }

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
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

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
            try
            {
                if (pstmt != null) { pstmt.close(); }
                if (conn != null) { conn.close(); }
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return keys.iterator();
    }
}
