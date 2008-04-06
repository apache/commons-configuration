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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.flat.BaseConfiguration;

import junit.framework.TestCase;

/**
 * A test class for some of the basic functionality implemented by
 * AbstractConfiguration.
 *
 * @version $Id$
 */
public class TestAbstractConfigurationBasicFeatures extends TestCase
{
    /** Constant for the prefix of test keys.*/
    private static final String KEY_PREFIX = "key";

    /** Constant for the number of properties in tests for copy operations.*/
    private static final int PROP_COUNT = 12;

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
            public Iterator<String> getKeys()
            {
                Collection<String> keyCol = new ArrayList<String>();
                for(Iterator<?> it = getUnderlyingConfiguration().getKeys(); it.hasNext();)
                {
                    keyCol.add(it.next().toString());
                }
                String[] keys = keyCol.toArray(new String[keyCol.size()]);
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
        List<?> lst = config.getList("test");
        assertEquals("Wrong number of list elements", 6, lst.size());
        for (int i = 0; i < lst.size(); i++)
        {
            assertEquals("Wrong list element at " + i, "value" + (i + 1), lst
                    .get(i));
        }
    }

    /**
     * Tests the copy() method.
     */
    public void testCopy()
    {
        AbstractConfiguration config = setUpDestConfig();
        Configuration srcConfig = setUpSourceConfig();
        config.copy(srcConfig);
        for (int i = 0; i < PROP_COUNT; i++)
        {
            String key = KEY_PREFIX + i;
            if (srcConfig.containsKey(key))
            {
                assertEquals("Value not replaced: " + key, srcConfig
                        .getProperty(key), config.getProperty(key));
            }
            else
            {
                assertEquals("Value modified: " + key, "value" + i, config
                        .getProperty(key));
            }
        }
    }

    /**
     * Tests the copy() method when properties with multiple values and escaped
     * list delimiters are involved.
     */
    public void testCopyWithLists()
    {
        Configuration srcConfig = setUpSourceConfig();
        AbstractConfiguration config = setUpDestConfig();
        config.copy(srcConfig);
        checkListProperties(config);
    }

    /**
     * Tests the events generated by a copy() operation.
     */
    public void testCopyEvents()
    {
        AbstractConfiguration config = setUpDestConfig();
        Configuration srcConfig = setUpSourceConfig();
        CollectingConfigurationListener l = new CollectingConfigurationListener();
        config.addConfigurationListener(l);
        config.copy(srcConfig);
        checkCopyEvents(l, srcConfig, AbstractConfiguration.EVENT_SET_PROPERTY);
    }

    /**
     * Tests copying a null configuration. This should be a noop.
     */
    public void testCopyNull()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.copy(null);
        ConfigurationAssert.assertEquals(setUpDestConfig(), config);
    }

    /**
     * Tests the append() method.
     */
    public void testAppend()
    {
        AbstractConfiguration config = setUpDestConfig();
        Configuration srcConfig = setUpSourceConfig();
        config.append(srcConfig);
        for (int i = 0; i < PROP_COUNT; i++)
        {
            String key = KEY_PREFIX + i;
            if (srcConfig.containsKey(key))
            {
                List<?> values = config.getList(key);
                assertEquals("Value not added: " + key, 2, values.size());
                assertEquals("Wrong value 1 for " + key, "value" + i, values
                        .get(0));
                assertEquals("Wrong value 2 for " + key, "src" + i, values
                        .get(1));
            }
            else
            {
                assertEquals("Value modified: " + key, "value" + i, config
                        .getProperty(key));
            }
        }
    }

    /**
     * Tests the append() method when properties with multiple values and
     * escaped list delimiters are involved.
     */
    public void testAppendWithLists()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.append(setUpSourceConfig());
        checkListProperties(config);
    }

    /**
     * Tests the events generated by an append() operation.
     */
    public void testAppendEvents()
    {
        AbstractConfiguration config = setUpDestConfig();
        Configuration srcConfig = setUpSourceConfig();
        CollectingConfigurationListener l = new CollectingConfigurationListener();
        config.addConfigurationListener(l);
        config.append(srcConfig);
        checkCopyEvents(l, srcConfig, AbstractConfiguration.EVENT_ADD_PROPERTY);
    }

    /**
     * Tests appending a null configuration. This should be a noop.
     */
    public void testAppendNull()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.append(null);
        ConfigurationAssert.assertEquals(setUpDestConfig(), config);
    }

    public void testResolveContainerStore()
    {
        AbstractConfiguration config = new BaseConfiguration();

        // array of objects
        config.addPropertyDirect("array", new String[] { "foo", "bar" });

        assertEquals("first element of the 'array' property", "foo", config.resolveContainerStore("array"));

        // list of objects
        List<String> list = new ArrayList<String>();
        list.add("foo");
        list.add("bar");
        config.addPropertyDirect("list", list);

        assertEquals("first element of the 'list' property", "foo", config.resolveContainerStore("list"));

        // set of objects
        Set<String> set = new LinkedHashSet<String>();
        set.add("foo");
        set.add("bar");
        config.addPropertyDirect("set", set);

        assertEquals("first element of the 'set' property", "foo", config.resolveContainerStore("set"));

        // arrays of primitives
        config.addPropertyDirect("array.boolean", new boolean[] { true, false });
        assertEquals("first element of the 'array.boolean' property", true, config.getBoolean("array.boolean"));

        config.addPropertyDirect("array.byte", new byte[] { 1, 2 });
        assertEquals("first element of the 'array.byte' property", 1, config.getByte("array.byte"));

        config.addPropertyDirect("array.short", new short[] { 1, 2 });
        assertEquals("first element of the 'array.short' property", 1, config.getShort("array.short"));

        config.addPropertyDirect("array.int", new int[] { 1, 2 });
        assertEquals("first element of the 'array.int' property", 1, config.getInt("array.int"));

        config.addPropertyDirect("array.long", new long[] { 1, 2 });
        assertEquals("first element of the 'array.long' property", 1, config.getLong("array.long"));

        config.addPropertyDirect("array.float", new float[] { 1, 2 });
        assertEquals("first element of the 'array.float' property", 1, config.getFloat("array.float"), 0);

        config.addPropertyDirect("array.double", new double[] { 1, 2 });
        assertEquals("first element of the 'array.double' property", 1, config.getDouble("array.double"), 0);
    }

    /**
     * Creates the source configuration for testing the copy() and append()
     * methods. This configuration contains keys with an odd index and values
     * starting with the prefix "src". There are also some list properties.
     *
     * @return the source configuration for copy operations
     */
    private Configuration setUpSourceConfig()
    {
        BaseConfiguration config = new BaseConfiguration();
        for (int i = 1; i < PROP_COUNT; i += 2)
        {
            config.addProperty(KEY_PREFIX + i, "src" + i);
        }
        config.addProperty("list1", "1,2,3");
        config.addProperty("list2", "3\\,1415,9\\,81");
        return config;
    }

    /**
     * Creates the destination configuration for testing the copy() and append()
     * methods. This configuration contains keys with a running index and
     * corresponding values starting with the prefix "value".
     *
     * @return the destination configuration for copy operations
     */
    private AbstractConfiguration setUpDestConfig()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new PropertiesConfiguration());
        for (int i = 0; i < PROP_COUNT; i++)
        {
            config.addProperty(KEY_PREFIX + i, "value" + i);
        }
        return config;
    }

    /**
     * Tests the values of list properties after a copy operation.
     *
     * @param config the configuration to test
     */
    private void checkListProperties(Configuration config)
    {
        List<?> values = config.getList("list1");
        assertEquals("Wrong number of elements in list 1", 3, values.size());
        values = config.getList("list2");
        assertEquals("Wrong number of elements in list 2", 2, values.size());
        assertEquals("Wrong value 1", "3,1415", values.get(0));
        assertEquals("Wrong value 2", "9,81", values.get(1));
    }

    /**
     * Tests whether the correct events are received for a copy operation.
     *
     * @param l the event listener
     * @param src the configuration that was copied
     * @param eventType the expected event type
     */
    private void checkCopyEvents(CollectingConfigurationListener l,
            Configuration src, int eventType)
    {
        Map<String, ConfigurationEvent> events = new HashMap<String, ConfigurationEvent>();
        for (ConfigurationEvent e : l.events)
        {
            assertEquals("Wrong event type", eventType, e.getType());
            assertTrue("Unknown property: " + e.getPropertyName(), src
                    .containsKey(e.getPropertyName()));
            assertEquals("Wrong property value for " + e.getPropertyName(), e
                    .getPropertyValue(), src.getProperty(e.getPropertyName()));
            if (!e.isBeforeUpdate())
            {
                assertTrue("After event without before event", events
                        .containsKey(e.getPropertyName()));
            }
            else
            {
                events.put(e.getPropertyName(), e);
            }
        }

        for (Iterator<?> it = src.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            assertTrue("No event received for key " + key, events
                    .containsKey(key));
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

        public Iterator<String> getKeys()
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

    /**
     * An event listener implementation that simply collects all received
     * configuration events.
     */
    static class CollectingConfigurationListener implements
            ConfigurationListener
    {
        List<ConfigurationEvent> events = new ArrayList<ConfigurationEvent>();

        public void configurationChanged(ConfigurationEvent event)
        {
            events.add(event);
        }
    }
}
