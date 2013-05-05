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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.Lookup;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * A test class for some of the basic functionality implemented by
 * AbstractConfiguration.
 *
 * @version $Id$
 */
public class TestAbstractConfigurationBasicFeatures
{
    /** Constant for the prefix of test keys.*/
    private static final String KEY_PREFIX = "key";

    /** Constant for the number of properties in tests for copy operations.*/
    private static final int PROP_COUNT = 12;

    /**
     * Tests the clear() implementation of AbstractConfiguration if the iterator
     * returned by getKeys() does not support the remove() operation.
     */
    @Test
    public void testClearIteratorNoRemove()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new BaseConfiguration())
        {
            // return an iterator that does not support remove operations
            @Override
            protected Iterator<String> getKeysInternal()
            {
                Collection<String> keyCol = new ArrayList<String>();
                CollectionUtils.addAll(keyCol, getUnderlyingConfiguration()
                        .getKeys());
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
    @Test
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
    @Test
    public void testAddPropertyList()
    {
        checkAddListProperty(new TestConfigurationImpl(
                new PropertiesConfiguration()));
    }

    /**
     * Tests adding list properties when delimiter parsing is disabled.
     */
    @Test
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
        List<Object> lst = config.getList("test");
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testCopyNull()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.copy(null);
        ConfigurationAssert.assertEquals(setUpDestConfig(), config);
    }

    /**
     * Tests the append() method.
     */
    @Test
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
                List<Object> values = config.getList(key);
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
    @Test
    public void testAppendWithLists()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.append(setUpSourceConfig());
        checkListProperties(config);
    }

    /**
     * Tests the events generated by an append() operation.
     */
    @Test
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
    @Test
    public void testAppendNull()
    {
        AbstractConfiguration config = setUpDestConfig();
        config.append(null);
        ConfigurationAssert.assertEquals(setUpDestConfig(), config);
    }

    /**
     * Tests whether environment variables can be interpolated.
     */
    @Test
    public void testInterpolateEnvironmentVariables()
    {
        AbstractConfiguration config = new TestConfigurationImpl(
                new PropertiesConfiguration());
        InterpolationTestHelper.testInterpolationEnvironment(config);
    }

    /**
     * Tests whether prefix lookups can be added to an existing
     * {@code ConfigurationInterpolator}.
     */
    @Test
    public void testSetPrefixLookupsExistingInterpolator()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        int count = config.getInterpolator().getLookups().size();
        Map<String, Lookup> lookups = new HashMap<String, Lookup>();
        lookups.put("test", look);
        config.setPrefixLookups(lookups);
        Map<String, Lookup> lookups2 = config.getInterpolator().getLookups();
        assertEquals("Not added", count + 1, lookups2.size());
        assertSame("Not found", look, lookups2.get("test"));
    }

    /**
     * Tests whether prefix lookups can be added if no
     * {@code ConfigurationInterpolator} exists yet.
     */
    @Test
    public void testSetPrefixLookupsNoInterpolator()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setPrefixLookups(Collections.singletonMap("test", look));
        Map<String, Lookup> lookups = config.getInterpolator().getLookups();
        assertEquals("Wrong number of lookups", 1, lookups.size());
        assertSame("Not found", look, lookups.get("test"));
    }

    /**
     * Tests whether default lookups can be added to an already existing
     * {@code ConfigurationInterpolator}.
     */
    @Test
    public void testSetDefaultLookupsExistingInterpolator()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        config.getInterpolator().addDefaultLookup(
                new ConfigurationLookup(new PropertiesConfiguration()));
        config.setDefaultLookups(Collections.singleton(look));
        List<Lookup> lookups = config.getInterpolator().getDefaultLookups();
        assertEquals("Wrong number of default lookups", 3, lookups.size());
        assertSame("Wrong lookup at 1", look, lookups.get(1));
        assertTrue("Wrong lookup at 2: " + lookups,
                lookups.get(2) instanceof ConfigurationLookup);
    }

    /**
     * Tests whether default lookups can be added if not
     * {@code ConfigurationInterpolator} exists yet.
     */
    @Test
    public void testSetDefaultLookupsNoInterpolator()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setDefaultLookups(Collections.singleton(look));
        List<Lookup> lookups = config.getInterpolator().getDefaultLookups();
        assertEquals("Wrong number of default lookups", 2, lookups.size());
        assertSame("Wrong lookup at 0", look, lookups.get(0));
        assertTrue("Wrong lookup at 1",
                lookups.get(1) instanceof ConfigurationLookup);
    }

    /**
     * Tests whether a new {@code ConfigurationInterpolator} can be installed
     * without providing custom lookups.
     */
    @Test
    public void testInstallInterpolatorNull()
    {
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        config.installInterpolator(null, null);
        assertTrue("Got prefix lookups", config.getInterpolator().getLookups()
                .isEmpty());
        List<Lookup> defLookups = config.getInterpolator().getDefaultLookups();
        assertEquals("Wrong number of default lookups", 1, defLookups.size());
        assertTrue("Wrong default lookup",
                defLookups.get(0) instanceof ConfigurationLookup);
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set if
     * already a {@code ConfigurationInterpolator} is available.
     */
    @Test
    public void testSetParentInterpolatorExistingInterpolator()
    {
        ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(parent);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        ConfigurationInterpolator ci = config.getInterpolator();
        config.setParentInterpolator(parent);
        assertSame("Parent was not set", parent, config.getInterpolator()
                .getParentInterpolator());
        assertSame("Interpolator was changed", ci, config.getInterpolator());
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set if
     * currently no {@code ConfigurationInterpolator} is available.
     */
    @Test
    public void testSetParentInterpolatorNoInterpolator()
    {
        ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(parent);
        AbstractConfiguration config =
                new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setParentInterpolator(parent);
        assertSame("Parent was not set", parent, config.getInterpolator()
                .getParentInterpolator());
    }

    /**
     * Tests getList() for single non-string values.
     */
    @Test
    public void testGetListNonString()
    {
        checkGetListScalar(Integer.valueOf(42));
        checkGetListScalar(Long.valueOf(42));
        checkGetListScalar(Short.valueOf((short) 42));
        checkGetListScalar(Byte.valueOf((byte) 42));
        checkGetListScalar(Float.valueOf(42));
        checkGetListScalar(Double.valueOf(42));
        checkGetListScalar(Boolean.TRUE);
}

    /**
     * Tests getStringArray() for single son-string values.
     */
    @Test
    public void testGetStringArrayNonString()
    {
        checkGetStringArrayScalar(Integer.valueOf(42));
        checkGetStringArrayScalar(Long.valueOf(42));
        checkGetStringArrayScalar(Short.valueOf((short) 42));
        checkGetStringArrayScalar(Byte.valueOf((byte) 42));
        checkGetStringArrayScalar(Float.valueOf(42));
        checkGetStringArrayScalar(Double.valueOf(42));
        checkGetStringArrayScalar(Boolean.TRUE);
    }

    /**
     * Helper method for checking getList() if the property value is a scalar.
     * @param value the value of the property
     */
    private void checkGetListScalar(Object value)
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(KEY_PREFIX, value);
        List<Object> lst = config.getList(KEY_PREFIX);
        assertEquals("Wrong number of values", 1, lst.size());
        assertEquals("Wrong value", value.toString(), lst.get(0));
    }

    /**
     * Helper method for checking getStringArray() if the property value is a
     * scalar.
     *
     * @param value the value of the property
     */
    private void checkGetStringArrayScalar(Object value)
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(KEY_PREFIX, value);
        String[] array = config.getStringArray(KEY_PREFIX);
        assertEquals("Weong number of elements", 1, array.length);
        assertEquals("Wrong value", value.toString(), array[0]);
    }

    /**
     * Tests whether interpolation works in variable names.
     */
    @Test
    public void testNestedVariableInterpolation()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.getInterpolator().setEnableSubstitutionInVariables(true);
        config.addProperty("java.version", "1.4");
        config.addProperty("jre-1.4", "C:\\java\\1.4");
        config.addProperty("jre.path", "${jre-${java.version}}");
        assertEquals("Wrong path", "C:\\java\\1.4",
                config.getString("jre.path"));
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
        List<Object> values = config.getList("list1");
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

        for (Iterator<String> it = src.getKeys(); it.hasNext();)
        {
            String key = it.next();
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

        @Override
        protected void addPropertyDirect(String key, Object value)
        {
            config.addPropertyDirect(key, value);
        }

        public boolean containsKey(String key)
        {
            return config.containsKey(key);
        }

        @Override
        protected Iterator<String> getKeysInternal()
        {
            return config.getKeys();
        }

        @Override
        protected Object getPropertyInternal(String key)
        {
            return config.getProperty(key);
        }

        @Override
        protected boolean isEmptyInternal()
        {
            return config.isEmpty();
        }

        @Override
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
