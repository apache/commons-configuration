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
