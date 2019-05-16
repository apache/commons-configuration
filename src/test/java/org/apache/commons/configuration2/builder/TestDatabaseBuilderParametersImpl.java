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
package org.apache.commons.configuration2.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code DatabaseBuilderParametersImpl}.
 *
 */
public class TestDatabaseBuilderParametersImpl
{
    /** The parameters object to be tested. */
    private DatabaseBuilderParametersImpl params;

    @Before
    public void setUp() throws Exception
    {
        params = new DatabaseBuilderParametersImpl();
    }

    /**
     * Tests whether the data source property can be set.
     */
    @Test
    public void testSetDataSource()
    {
        final DataSource src = EasyMock.createMock(DataSource.class);
        EasyMock.replay(src);
        assertSame("Wrong result", params, params.setDataSource(src));
        assertSame("Data source not set", src,
                params.getParameters().get("dataSource"));
    }

    /**
     * Tests whether the table name can be set.
     */
    @Test
    public void testSetTable()
    {
        final String table = "TestTable";
        assertSame("Wrong result", params, params.setTable(table));
        assertEquals("Wrong table name", table,
                params.getParameters().get("table"));
    }

    /**
     * Tests whether the key column name can be set.
     */
    @Test
    public void testSetKeyColumn()
    {
        final String colName = "KEY_COLUMN";
        assertSame("Wrong result", params, params.setKeyColumn(colName));
        assertEquals("Wrong key column name", colName, params.getParameters()
                .get("keyColumn"));
    }

    /**
     * Tests whether the value column name can be set.
     */
    @Test
    public void testSetValueColumn()
    {
        final String colName = "VALUE_COLUMN";
        assertSame("Wrong result", params, params.setValueColumn(colName));
        assertEquals("Wrong value column name", colName, params.getParameters()
                .get("valueColumn"));
    }

    /**
     * Tests whether the configuration name column can be set.
     */
    @Test
    public void testSetConfigurationNameColumn()
    {
        final String colName = "CONFIG_COLUMN";
        assertSame("Wrong result", params,
                params.setConfigurationNameColumn(colName));
        assertEquals("Wrong configuration name column", colName, params
                .getParameters().get("configurationNameColumn"));
    }

    /**
     * Tests whether the configuration name can be set.
     */
    @Test
    public void testSetConfigurationName()
    {
        final String confName = "TestConfiguration";
        assertSame("Wrong result", params,
                params.setConfigurationName(confName));
        assertEquals("Wrong configuration name", confName, params
                .getParameters().get("configurationName"));
    }

    /**
     * Tests whether the auto commit flag can be set.
     */
    @Test
    public void testSetAutoCommit()
    {
        assertSame("Wrong result", params, params.setAutoCommit(true));
        assertEquals("Wrong auto commit flag", Boolean.TRUE, params
                .getParameters().get("autoCommit"));
    }

    /**
     * Tests whether properties can be set through BeanUtils.
     */
    @Test
    public void testBeanProperties() throws Exception
    {
        BeanHelper.setProperty(params, "table", "testTable");
        BeanHelper.setProperty(params, "autoCommit", Boolean.FALSE);
        final Map<String, Object> map = params.getParameters();
        assertEquals("Wrong table name", "testTable", map.get("table"));
        assertEquals("Wrong auto commit", Boolean.FALSE, map.get("autoCommit"));
    }
}
