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

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code DatabaseBuilderParametersImpl}.
 *
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
        assertEquals("testTable", map.get("table"), "Wrong table name");
        assertEquals(Boolean.FALSE, map.get("autoCommit"), "Wrong auto commit");
    }

    /**
     * Tests whether the auto commit flag can be set.
     */
    @Test
    public void testSetAutoCommit() {
        assertSame(params, params.setAutoCommit(true), "Wrong result");
        assertEquals(Boolean.TRUE, params.getParameters().get("autoCommit"), "Wrong auto commit flag");
    }

    /**
     * Tests whether the configuration name can be set.
     */
    @Test
    public void testSetConfigurationName() {
        final String confName = "TestConfiguration";
        assertSame(params, params.setConfigurationName(confName), "Wrong result");
        assertEquals(confName, params.getParameters().get("configurationName"), "Wrong configuration name");
    }

    /**
     * Tests whether the configuration name column can be set.
     */
    @Test
    public void testSetConfigurationNameColumn() {
        final String colName = "CONFIG_COLUMN";
        assertSame(params, params.setConfigurationNameColumn(colName), "Wrong result");
        assertEquals(colName, params.getParameters().get("configurationNameColumn"), "Wrong configuration name column");
    }

    /**
     * Tests whether the data source property can be set.
     */
    @Test
    public void testSetDataSource() {
        final DataSource src = EasyMock.createMock(DataSource.class);
        EasyMock.replay(src);
        assertSame(params, params.setDataSource(src), "Wrong result");
        assertSame(src, params.getParameters().get("dataSource"), "Data source not set");
    }

    /**
     * Tests whether the key column name can be set.
     */
    @Test
    public void testSetKeyColumn() {
        final String colName = "KEY_COLUMN";
        assertSame(params, params.setKeyColumn(colName), "Wrong result");
        assertEquals(colName, params.getParameters().get("keyColumn"), "Wrong key column name");
    }

    /**
     * Tests whether the table name can be set.
     */
    @Test
    public void testSetTable() {
        final String table = "TestTable";
        assertSame(params, params.setTable(table), "Wrong result");
        assertEquals(table, params.getParameters().get("table"), "Wrong table name");
    }

    /**
     * Tests whether the value column name can be set.
     */
    @Test
    public void testSetValueColumn() {
        final String colName = "VALUE_COLUMN";
        assertSame(params, params.setValueColumn(colName), "Wrong result");
        assertEquals(colName, params.getParameters().get("valueColumn"), "Wrong value column name");
    }
}
