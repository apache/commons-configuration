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

import java.io.File;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.configuration2.event.ConfigurationErrorListener;
import org.codehaus.spice.jndikit.DefaultNameParser;
import org.codehaus.spice.jndikit.DefaultNamespace;
import org.codehaus.spice.jndikit.memory.MemoryContext;

/**
 * Test for database stored configurations.  Note, when running this Unit
 * Test in Eclipse it sometimes takes a couple tries. Otherwise you may get
 * database is already in use by another process errors.
 *
 * @version $Revision$, $Date$
 */
public class TestDatabaseConfiguration extends TestCase
{
    /** An error listener for testing whether internal errors occurred.*/
    private ConfigurationErrorListenerImpl listener;

    /** The test helper. */
    private DatabaseConfigurationTestHelper helper;

    @Override
    protected void setUp() throws Exception
    {
        /*
         * Thread.sleep may or may not help with the database is already in
         * use exception.
         */
        //Thread.sleep(1000);

        helper = new DatabaseConfigurationTestHelper();
        helper.setUp();
    }

    @Override
    protected void tearDown() throws Exception{
        // if an error listener is defined, we check whether an error occurred
        if(listener != null)
        {
            assertEquals("An internal error occurred", 0, listener.getErrorCount());
        }
        helper.tearDown();

        super.tearDown();
    }

    /**
     * Creates a database configuration with default values.
     *
     * @return the configuration
     */
    private DatabaseConfiguration setUpConfig()
    {
        return helper.setUpConfig();
    }

    /**
     * Creates a database configuration that supports multiple configurations in
     * a table with default values.
     *
     * @return the configuration
     */
    private DatabaseConfiguration setUpMultiConfig()
    {
        return helper.setUpMultiConfig();
    }

    /**
     * Creates an error listener and adds it to the specified configuration.
     *
     * @param config the configuration
     */
    private void setUpErrorListener(DatabaseConfiguration config)
    {
        // remove log listener to avoid exception longs
        config.removeErrorListener((ConfigurationErrorListener) config.getErrorListeners().iterator().next());
        listener = new ConfigurationErrorListenerImpl();
        config.addErrorListener(listener);
        helper.getDatasource().setFailOnConnect(true);
    }

    /**
     * Prepares a test for a database error. Sets up a config and registers an
     * error listener.
     *
     * @return the initialized configuration
     */
    private DatabaseConfiguration setUpErrorConfig()
    {
        DatabaseConfiguration config = setUpConfig();
        setUpErrorListener(config);
        return config;
    }

    /**
     * Checks the error listener for an expected error. The properties of the
     * error event will be compared with the expected values.
     *
     * @param type the expected type of the error event
     * @param key the expected property key
     * @param value the expected property value
     */
    private void checkErrorListener(int type, String key, Object value)
    {
        listener.verify(type, key, value);
        assertTrue("Wrong event source", listener.getLastEvent().getSource() instanceof DatabaseConfiguration);
        assertTrue("Wrong exception", listener.getLastEvent().getCause() instanceof SQLException);
        listener = null; // mark as checked
    }

    public void testAddPropertyDirectSingle()
    {
        DatabaseConfiguration config = setUpConfig();
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testAddPropertyDirectMultiple()
    {
        DatabaseConfiguration config = setUpMultiConfig();
        config.addPropertyDirect("key", "value");

        assertTrue("missing property", config.containsKey("key"));
    }

    public void testAddNonStringProperty()
    {
        DatabaseConfiguration config = setUpConfig();
        config.addPropertyDirect("boolean", Boolean.TRUE);

        assertTrue("missing property", config.containsKey("boolean"));
    }

    public void testGetPropertyDirectSingle()
    {
        Configuration config = setUpConfig();

        assertEquals("property1", "value1", config.getProperty("key1"));
        assertEquals("property2", "value2", config.getProperty("key2"));
        assertEquals("unknown property", null, config.getProperty("key3"));
    }

    public void testGetPropertyDirectMultiple()
    {
        Configuration config = setUpMultiConfig();

        assertEquals("property1", "value1", config.getProperty("key1"));
        assertEquals("property2", "value2", config.getProperty("key2"));
        assertEquals("unknown property", null, config.getProperty("key3"));
    }

    public void testClearPropertySingle()
    {
        Configuration config = setUpConfig();
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testClearPropertyMultiple()
    {
        Configuration config = setUpMultiConfig();
        config.clearProperty("key");

        assertFalse("property not cleared", config.containsKey("key"));
    }

    public void testClearSingle()
    {
        Configuration config = setUpConfig();
        config.clear();

        assertTrue("configuration is not cleared", config.isEmpty());
    }

    public void testClearMultiple()
    {
        Configuration config = setUpMultiConfig();
        config.clear();

        assertTrue("configuration is not cleared", config.isEmpty());
    }

    public void testGetKeysSingle()
    {
        Configuration config = setUpConfig();
        Iterator<?> it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testGetKeysMultiple()
    {
        Configuration config = setUpMultiConfig();
        Iterator<?> it = config.getKeys();

        assertEquals("1st key", "key1", it.next());
        assertEquals("2nd key", "key2", it.next());
    }

    public void testContainsKeySingle()
    {
        Configuration config = setUpConfig();
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testContainsKeyMultiple()
    {
        Configuration config = setUpMultiConfig();
        assertTrue("missing key1", config.containsKey("key1"));
        assertTrue("missing key2", config.containsKey("key2"));
    }

    public void testIsEmptySingle()
    {
        Configuration config1 = setUpConfig();
        assertFalse("The configuration is empty", config1.isEmpty());
    }

    public void testIsEmptyMultiple()
    {
        Configuration config1 = setUpMultiConfig();
        assertFalse("The configuration named 'test' is empty", config1
                .isEmpty());

        Configuration config2 = new DatabaseConfiguration(helper
                .getDatasource(), DatabaseConfigurationTestHelper.TABLE_MULTI,
                DatabaseConfigurationTestHelper.COL_NAME,
                DatabaseConfigurationTestHelper.COL_KEY,
                DatabaseConfigurationTestHelper.COL_VALUE, "testIsEmpty");
        assertTrue("The configuration named 'testIsEmpty' is not empty",
                config2.isEmpty());
    }

    public void testGetList()
    {
        Configuration config1 = new DatabaseConfiguration(helper
                .getDatasource(), "configurationList",
                DatabaseConfigurationTestHelper.COL_KEY,
                DatabaseConfigurationTestHelper.COL_VALUE);
        List<?> list = config1.getList("key3");
        assertEquals(3, list.size());
    }

    public void testGetKeys()
    {
        Configuration config1 = new DatabaseConfiguration(helper
                .getDatasource(), "configurationList",
                DatabaseConfigurationTestHelper.COL_KEY,
                DatabaseConfigurationTestHelper.COL_VALUE);
        Iterator<?> i = config1.getKeys();
        assertTrue(i.hasNext());
        Object key = i.next();
        assertEquals("key3", key.toString());
        assertFalse(i.hasNext());
    }

    public void testClearSubset()
    {
        Configuration config = setUpConfig();

        Configuration subset = config.subset("key1");
        subset.clear();

        assertTrue("the subset is not empty", subset.isEmpty());
        assertFalse("the parent configuration is empty", config.isEmpty());
    }

    /**
     * Tests whether the configuration has already an error listener registered
     * that is used for logging.
     */
    public void testLogErrorListener()
    {
        DatabaseConfiguration config = new DatabaseConfiguration(helper
                .getDatasource(), DatabaseConfigurationTestHelper.TABLE,
                DatabaseConfigurationTestHelper.COL_KEY,
                DatabaseConfigurationTestHelper.COL_VALUE);
        assertEquals("No error listener registered", 1, config
                .getErrorListeners().size());
    }

    /**
     * Tests handling of errors in getProperty().
     */
    public void testGetPropertyError()
    {
        setUpErrorConfig().getProperty("key1");
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in addPropertyDirect().
     */
    public void testAddPropertyError()
    {
        setUpErrorConfig().addProperty("key1", "value");
        checkErrorListener(AbstractConfiguration.EVENT_ADD_PROPERTY, "key1", "value");
    }

    /**
     * Tests handling of errors in isEmpty().
     */
    public void testIsEmptyError()
    {
        assertTrue("Wrong return value for failure", setUpErrorConfig().isEmpty());
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null, null);
    }

    /**
     * Tests handling of errors in containsKey().
     */
    public void testContainsKeyError()
    {
        assertFalse("Wrong return value for failure", setUpErrorConfig().containsKey("key1"));
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in clearProperty().
     */
    public void testClearPropertyError()
    {
        setUpErrorConfig().clearProperty("key1");
        checkErrorListener(AbstractConfiguration.EVENT_CLEAR_PROPERTY, "key1", null);
    }

    /**
     * Tests handling of errors in clear().
     */
    public void testClearError()
    {
        setUpErrorConfig().clear();
        checkErrorListener(AbstractConfiguration.EVENT_CLEAR, null, null);
    }

    /**
     * Tests handling of errors in getKeys().
     */
    public void testGetKeysError()
    {
        Iterator<?> it = setUpErrorConfig().getKeys();
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null, null);
        assertFalse("Iteration is not empty", it.hasNext());
    }

    /**
     * Tests obtaining a property as list whose value contains the list
     * delimiter. Multiple values should be returned.
     */
    public void testGetListWithDelimiter()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        List<?> values = config.getList("keyMulti");
        assertEquals("Wrong number of list elements", 3, values.size());
        assertEquals("Wrong list element 0", "a", values.get(0));
        assertEquals("Wrong list element 2", "c", values.get(2));
    }

    /**
     * Tests obtaining a property whose value contains the list delimiter when
     * delimiter parsing is disabled.
     */
    public void testGetListWithDelimiterParsingDisabled()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        config.setDelimiterParsingDisabled(true);
        assertEquals("Wrong value of property", "a;b;c", config.getString("keyMulti"));
    }

    /**
     * Tests adding a property containing the list delimiter. When this property
     * is queried multiple values should be returned.
     */
    public void testAddWithDelimiter()
    {
        DatabaseConfiguration config = setUpConfig();
        config.setListDelimiter(';');
        config.addProperty("keyList", "1;2;3");
        String[] values = config.getStringArray("keyList");
        assertEquals("Wrong number of property values", 3, values.length);
        assertEquals("Wrong value at index 1", "2", values[1]);
    }

    /**
     * Tests setProperty() if the property value contains the list delimiter.
     */
    public void testSetPropertyWithDelimiter()
    {
        DatabaseConfiguration config = setUpMultiConfig();
        config.setListDelimiter(';');
        config.setProperty("keyList", "1;2;3");
        String[] values = config.getStringArray("keyList");
        assertEquals("Wrong number of property values", 3, values.length);
        assertEquals("Wrong value at index 1", "2", values[1]);
    }

    /**
     * Test instantiating a DatabaseConfiguration from a configuration descriptor.
     */
    public void testConfigurationBuilder() throws Exception
    {
        // bind the datasource in the JNDI context
        TestInitialContextFactory.datasource = helper.getDatasource();
        System.setProperty("java.naming.factory.initial", TestInitialContextFactory.class.getName());

        File testFile = ConfigurationAssert.getTestFile("testDatabaseConfiguration.xml");
        ConfigurationBuilder builder = new DefaultConfigurationBuilder(testFile);
        Configuration config = builder.getConfiguration();

        assertNotNull("null configuration", config);

        assertEquals("property1", "value1", config.getProperty("conf1.key1"));
        assertEquals("property2", "value2", config.getProperty("conf1.key2"));
        assertEquals("unknown property", null, config.getProperty("conf1.key3"));
        assertEquals("list property", 3, config.getList("conf1.keyMulti").size());

        assertEquals("property1", "value1", config.getProperty("conf2.key1"));
        assertEquals("property2", "value2", config.getProperty("conf2.key2"));
        assertEquals("unknown property", null, config.getProperty("conf2.key3"));
    }

    /**
     * JNDI Context factory that returns the test datasource bound on the java:comp/env/jdbc/configuration key.
     */
    public static class TestInitialContextFactory implements InitialContextFactory
    {
        public static DataSource datasource;

        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
        {
            DefaultNamespace namespace = new DefaultNamespace(new DefaultNameParser());
            MemoryContext context = new MemoryContext(namespace, new Hashtable<Object, Object>(), null);
            context.createSubcontext("java:comp").createSubcontext("env").createSubcontext("jdbc").bind("configuration", datasource);

            return context;
        }
    }
}
