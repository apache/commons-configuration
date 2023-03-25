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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code DatabaseBuilderParametersImpl}.
 */
public class TestDatabaseBuilderParametersImpl {
    /** The parameters object to be tested. */
    private DatabaseBuilderParametersImpl params;

    @BeforeEach
    public void setUp() throws Exception {
        params = new DatabaseBuilderParametersImpl();
    }

    /**
     * Tests whether properties can be set through BeanUtils.
     */
    @Test
    public void testBeanProperties() throws Exception {
        BeanHelper.setProperty(params, "table", "testTable");
        BeanHelper.setProperty(params, "autoCommit", Boolean.FALSE);
        final Map<String, Object> map = params.getParameters();
        assertEquals("testTable", map.get("table"));
        assertEquals(Boolean.FALSE, map.get("autoCommit"));
    }

    /**
     * Tests whether the auto commit flag can be set.
     */
    @Test
    public void testSetAutoCommit() {
        assertSame(params, params.setAutoCommit(true));
        assertEquals(Boolean.TRUE, params.getParameters().get("autoCommit"));
    }

    /**
     * Tests whether the configuration name can be set.
     */
    @Test
    public void testSetConfigurationName() {
        final String confName = "TestConfiguration";
        assertSame(params, params.setConfigurationName(confName));
        assertEquals(confName, params.getParameters().get("configurationName"));
    }

    /**
     * Tests whether the configuration name column can be set.
     */
    @Test
    public void testSetConfigurationNameColumn() {
        final String colName = "CONFIG_COLUMN";
        assertSame(params, params.setConfigurationNameColumn(colName));
        assertEquals(colName, params.getParameters().get("configurationNameColumn"));
    }

    /**
     * Tests whether the data source property can be set.
     */
    @Test
    public void testSetDataSource() {
        final DataSource src = mock(DataSource.class);
        assertSame(params, params.setDataSource(src));
        assertSame(src, params.getParameters().get("dataSource"));
    }

    /**
     * Tests whether the key column name can be set.
     */
    @Test
    public void testSetKeyColumn() {
        final String colName = "KEY_COLUMN";
        assertSame(params, params.setKeyColumn(colName));
        assertEquals(colName, params.getParameters().get("keyColumn"));
    }

    /**
     * Tests whether the table name can be set.
     */
    @Test
    public void testSetTable() {
        final String table = "TestTable";
        assertSame(params, params.setTable(table));
        assertEquals(table, params.getParameters().get("table"));
    }

    /**
     * Tests whether the value column name can be set.
     */
    @Test
    public void testSetValueColumn() {
        final String colName = "VALUE_COLUMN";
        assertSame(params, params.setValueColumn(colName));
        assertEquals(colName, params.getParameters().get("valueColumn"));
    }
}
