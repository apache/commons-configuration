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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import junit.framework.TestCase;

/**
 * A test class for some of the basic functionality implemented by
 * AbstractConfiguration.
 *
 * @version $Id$
 */
public class TestAbstractConfigurationBasicFeatures extends TestCase
{
    /**
     * Tests the clear() implementation of AbstractConfiguration if the iterator
     * returned by getKeys() does not support the remove() operation.
     */
    public void testClearIteratorNoRemove()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new BaseConfiguration())
        {
            // return an iterator that does not support remove operations
            public Iterator getKeys()
            {
                Collection keyCol = new ArrayList();
                CollectionUtils.addAll(keyCol, getUnderlyingConfiguration()
                        .getKeys());
                Object[] keys = keyCol.toArray();
                return Arrays.asList(keys).iterator();
            }
        };
        for (int i = 0; i < 20; i++)
        {
            config.addProperty("key" + i, "value" + i);
        }
        config.clear();
        assertTrue("Configuration not empty", config.isEmpty());
    }

    /**
     * Tests escaping the variable marker, so that no interpolation will be
     * performed.
     */
    public void testInterpolateEscape()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new PropertiesConfiguration());
        config
                .addProperty(
                        "mypath",
                        "$${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc.jar\\,$${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc_license_cu.jar");
        assertEquals(
                "Wrong interpolated value",
                "${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc.jar,${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc_license_cu.jar",
                config.getString("mypath"));
    }

    /**
     * Tests adding list properties. The single elements of the list should be
     * added.
     */
    public void testAddPropertyList()
    {
        checkAddListProperty(new TestConfigurationImpl(
                new PropertiesConfiguration()));
    }

    /**
     * Tests adding list properties when delimiter parsing is disabled.
     */
    public void testAddPropertyListNoDelimiterParsing()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new PropertiesConfiguration());
        config.setDelimiterParsingDisabled(true);
        checkAddListProperty(config);
    }

    /**
     * Helper method for adding properties with multiple values.
     *
     * @param config the configuration to be used for testing
     */
    private void checkAddListProperty(AbstractConfiguration config)
    {
        config.addProperty("test", "value1");
        Object[] lstValues1 = new Object[]
        { "value2", "value3" };
        Object[] lstValues2 = new Object[]
        { "value4", "value5", "value6" };
        config.addProperty("test", lstValues1);
        config.addProperty("test", Arrays.asList(lstValues2));
        List lst = config.getList("test");
        assertEquals("Wrong number of list elements", 6, lst.size());
        for (int i = 0; i < lst.size(); i++)
        {
            assertEquals("Wrong list element at " + i, "value" + (i + 1), lst
                    .get(i));
        }
    }

    /**
     * A test configuration implementation. This implementation inherits
     * directly from AbstractConfiguration. For implementing the required
     * functionality another implementation of AbstractConfiguration is used;
     * all methods that need to be implemented delegate to this wrapped
     * configuration.
     */
    static class TestConfigurationImpl extends AbstractConfiguration
    {
        /** Stores the underlying configuration. */
        private AbstractConfiguration config;

        public AbstractConfiguration getUnderlyingConfiguration()
        {
            return config;
        }

        public TestConfigurationImpl(AbstractConfiguration wrappedConfig)
        {
            config = wrappedConfig;
        }

        protected void addPropertyDirect(String key, Object value)
        {
            config.addPropertyDirect(key, value);
        }

        public boolean containsKey(String key)
        {
            return config.containsKey(key);
        }

        public Iterator getKeys()
        {
            return config.getKeys();
        }

        public Object getProperty(String key)
        {
            return config.getProperty(key);
        }

        public boolean isEmpty()
        {
            return config.isEmpty();
        }

        protected void clearPropertyDirect(String key)
        {
            config.clearPropertyDirect(key);
        }
    }
}
