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

import javax.sql.DataSource;

/**
 * <p>
 * A specialized parameters object for database configurations.
 * </p>
 * <p>
 * This class has properties for defining the database structures the
 * configuration operates on.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @since 2.0
 */
public class DatabaseBuilderParametersImpl extends BasicBuilderParameters
        implements DatabaseBuilderProperties<DatabaseBuilderParametersImpl>
{
    /** Constant for the data source property. */
    private static final String PROP_DATA_SOURCE = "dataSource";

    /** Constant for the table property. */
    private static final String PROP_TABLE = "table";

    /** Constant for the key column property. */
    private static final String PROP_KEY_COLUMN = "keyColumn";

    /** Constant for the value column property. */
    private static final String PROP_VALUE_COLUMN = "valueColumn";

    /** Constant for the configuration name column property. */
    private static final String PROP_CONFIG_NAME_COLUMN =
            "configurationNameColumn";

    /** Constant for the configuration name property. */
    private static final String PROP_CONFIG_NAME = "configurationName";

    /** Constant for the auto commit property. */
    private static final String PROP_AUTO_COMMIT = "autoCommit";

    @Override
    public DatabaseBuilderParametersImpl setDataSource(final DataSource src)
    {
        storeProperty(PROP_DATA_SOURCE, src);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setTable(final String tname)
    {
        storeProperty(PROP_TABLE, tname);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setKeyColumn(final String name)
    {
        storeProperty(PROP_KEY_COLUMN, name);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setValueColumn(final String name)
    {
        storeProperty(PROP_VALUE_COLUMN, name);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setConfigurationNameColumn(final String name)
    {
        storeProperty(PROP_CONFIG_NAME_COLUMN, name);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setConfigurationName(final String name)
    {
        storeProperty(PROP_CONFIG_NAME, name);
        return this;
    }

    @Override
    public DatabaseBuilderParametersImpl setAutoCommit(final boolean f)
    {
        storeProperty(PROP_AUTO_COMMIT, Boolean.valueOf(f));
        return this;
    }
}
