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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration2.builder.fluent.DatabaseBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ErrorListenerTestImpl;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for database stored configurations. Note, when running this Unit Test in Eclipse it sometimes takes a couple
 * tries. Otherwise you may get database is already in use by another process errors.
 */
public class TestDatabaseConfiguration {
    /**
     * A specialized database configuration implementation that can be configured to throw an exception when obtaining a
     * connection. This way database exceptions can be simulated.
     */
    public static class PotentialErrorDatabaseConfiguration extends DatabaseConfiguration {
        /** A flag whether a getConnection() call should fail. */
        boolean failOnConnect;

        @Override
        public DataSource getDatasource() {
            if (failOnConnect) {
                final DataSource ds = mock(DataSource.class);
                assertDoesNotThrow(() -> when(ds.getConnection()).thenThrow(new SQLException("Simulated DB error")));
                return ds;
            }
            return super.getDatasource();
        }
    }

    /** Constant for another configuration name. */
    private static final String CONFIG_NAME2 = "anotherTestConfig";

    /** An error listener for testing whether internal errors occurred. */
    private ErrorListenerTestImpl listener;

    /** The test helper. */
    private DatabaseConfigurationTestHelper helper;

    /**
     * Checks the error listener for an expected error. The properties of the error event will be compared with the expected
     * values.
     *
     * @param type the expected type of the error event
     * @param opType the expected operation type
     * @param key the expected property key
     * @param value the expected property value
     */
    private void checkErrorListener(final EventType<? extends ConfigurationErrorEvent> type, final EventType<?> opType, final String key, final Object value) {
        final Throwable exception = listener.checkEvent(type, opType, key, value);
        assertInstanceOf(SQLException.class, exception);
        listener = null; // mark as checked
    }

    @BeforeEach
    public void setUp() throws Exception {
        /*
         * Thread.sleep may or may not help with the database is already in use exception.
         */
        // Thread.sleep(1000);

        // set up the datasource

        helper = new DatabaseConfigurationTestHelper();
        helper.setUp();
    }

    /**
     * Creates a database configuration with default values.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    private PotentialErrorDatabaseConfiguration setUpConfig() throws ConfigurationException {
        return helper.setUpConfig(PotentialErrorDatabaseConfiguration.class);
    }

    /**
     * Prepares a test for a database error. Sets up a config and registers an error listener.
     *
     * @return the initialized configuration
     * @throws ConfigurationException if an error occurs
     */
    private PotentialErrorDatabaseConfiguration setUpErrorConfig() throws ConfigurationException {
        final PotentialErrorDatabaseConfiguration config = setUpConfig();
        setUpErrorListener(config);
        return config;
    }

    /**
     * Creates an error listener and adds it to the specified configuration.
     *
     * @param config the configuration
     */
    private void setUpErrorListener(final PotentialErrorDatabaseConfiguration config) {
        // remove log listener to avoid exception longs
        config.clearErrorListeners();
        listener = new ErrorListenerTestImpl(config);
        config.addEventListener(ConfigurationErrorEvent.ANY, listener);
        config.failOnConnect = true;
    }

    @AfterEach
    public void tearDown() throws Exception {
        // if an error listener is defined, we check whether an error occurred
        if (listener != null) {
            listener.done();
        }
        helper.tearDown();
    }

    @Test
    public void testAddNonStringProperty() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpConfig();
        config.addPropertyDirect("boolean", Boolean.TRUE);

        assertTrue(config.containsKey("boolean"));
    }

    /**
     * Tests whether a commit is performed after a property was added.
     */
    @Test
    public void testAddPropertyDirectCommit() throws ConfigurationException {
        helper.setAutoCommit(false);
        final DatabaseConfiguration config = helper.setUpConfig();
        config.addPropertyDirect("key", "value");
        assertTrue(config.containsKey("key"));
    }

    @Test
    public void testAddPropertyDirectMultiple() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpMultiConfig();
        config.addPropertyDirect("key", "value");

        assertTrue(config.containsKey("key"));
    }

    @Test
    public void testAddPropertyDirectSingle() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpConfig();
        config.addPropertyDirect("key", "value");

        assertTrue(config.containsKey("key"));
    }

    /**
     * Tests handling of errors in addPropertyDirect().
     */
    @Test
    public void testAddPropertyError() throws ConfigurationException {
        setUpErrorConfig().addProperty("key1", "value");
        checkErrorListener(ConfigurationErrorEvent.WRITE, ConfigurationEvent.ADD_PROPERTY, "key1", "value");
    }

    /**
     * Tests adding a property containing the list delimiter. When this property is queried multiple values should be
     * returned.
     */
    @Test
    public void testAddWithDelimiter() throws ConfigurationException {
        final DatabaseConfiguration config = setUpConfig();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        config.addProperty("keyList", "1;2;3");
        final String[] values = config.getStringArray("keyList");
        assertArrayEquals(new String[] {"1", "2", "3"}, values);
    }

    /**
     * Tests whether a commit is performed after a clear operation.
     */
    @Test
    public void testClearCommit() throws ConfigurationException {
        helper.setAutoCommit(false);
        final Configuration config = helper.setUpConfig();
        config.clear();
        assertTrue(config.isEmpty());
    }

    /**
     * Tests handling of errors in clear().
     */
    @Test
    public void testClearError() throws ConfigurationException {
        setUpErrorConfig().clear();
        checkErrorListener(ConfigurationErrorEvent.WRITE, ConfigurationEvent.CLEAR, null, null);
    }

    @Test
    public void testClearMultiple() throws ConfigurationException {
        final Configuration config = helper.setUpMultiConfig();
        config.clear();

        assertTrue(config.isEmpty());
    }

    /**
     * Tests whether a commit is performed after a property was cleared.
     */
    @Test
    public void testClearPropertyCommit() throws ConfigurationException {
        helper.setAutoCommit(false);
        final Configuration config = helper.setUpConfig();
        config.clearProperty("key1");
        assertFalse(config.containsKey("key1"));
    }

    /**
     * Tests handling of errors in clearProperty().
     */
    @Test
    public void testClearPropertyError() throws ConfigurationException {
        setUpErrorConfig().clearProperty("key1");
        checkErrorListener(ConfigurationErrorEvent.WRITE, ConfigurationEvent.CLEAR_PROPERTY, "key1", null);
    }

    @Test
    public void testClearPropertyMultiple() throws ConfigurationException {
        final Configuration config = helper.setUpMultiConfig();
        config.clearProperty("key1");

        assertFalse(config.containsKey("key1"));
    }

    /**
     * Tests that another configuration is not affected when clearing properties.
     */
    @Test
    public void testClearPropertyMultipleOtherConfig() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpMultiConfig();
        final DatabaseConfiguration config2 = helper.setUpMultiConfig(DatabaseConfiguration.class, CONFIG_NAME2);
        config2.addProperty("key1", "some test");
        config.clearProperty("key1");
        assertFalse(config.containsKey("key1"));
        assertTrue(config2.containsKey("key1"));
    }

    @Test
    public void testClearPropertySingle() throws ConfigurationException {
        final Configuration config = helper.setUpConfig();
        config.clearProperty("key1");

        assertFalse(config.containsKey("key1"));
    }

    @Test
    public void testClearSingle() throws ConfigurationException {
        final Configuration config = helper.setUpConfig();
        config.clear();

        assertTrue(config.isEmpty());
    }

    @Test
    public void testClearSubset() throws ConfigurationException {
        final Configuration config = setUpConfig();

        final Configuration subset = config.subset("key1");
        subset.clear();

        assertTrue(subset.isEmpty());
        assertFalse(config.isEmpty());
    }

    /**
     * Tests handling of errors in containsKey().
     */
    @Test
    public void testContainsKeyError() throws ConfigurationException {
        assertFalse(setUpErrorConfig().containsKey("key1"));
        checkErrorListener(ConfigurationErrorEvent.READ, ConfigurationErrorEvent.READ, "key1", null);
    }

    @Test
    public void testContainsKeyMultiple() throws ConfigurationException {
        final Configuration config = helper.setUpMultiConfig();
        assertTrue(config.containsKey("key1"));
        assertTrue(config.containsKey("key2"));
    }

    @Test
    public void testContainsKeySingle() throws ConfigurationException {
        final Configuration config = setUpConfig();
        assertTrue(config.containsKey("key1"));
        assertTrue(config.containsKey("key2"));
    }

    /**
     * Tests whether a CLOB as a property value is handled correctly.
     */
    @Test
    public void testExtractPropertyValueCLOB() throws ConfigurationException, SQLException {
        final ResultSet rs = mock(ResultSet.class);
        final Clob clob = mock(Clob.class);
        final String content = "This is the content of the test CLOB!";

        when(rs.getObject(DatabaseConfigurationTestHelper.COL_VALUE)).thenReturn(clob);
        when(clob.length()).thenReturn(Long.valueOf(content.length()));
        when(clob.getSubString(1, content.length())).thenReturn(content);

        final DatabaseConfiguration config = helper.setUpConfig();
        assertEquals(content, config.extractPropertyValue(rs));

        verify(rs).getObject(DatabaseConfigurationTestHelper.COL_VALUE);
        verify(clob).length();
        verify(clob).getSubString(1, content.length());
        verifyNoMoreInteractions(rs, clob);
    }

    /**
     * Tests whether an empty CLOB is correctly handled by extractPropertyValue().
     */
    @Test
    public void testExtractPropertyValueCLOBEmpty() throws ConfigurationException, SQLException {
        final ResultSet rs = mock(ResultSet.class);
        final Clob clob = mock(Clob.class);

        when(rs.getObject(DatabaseConfigurationTestHelper.COL_VALUE)).thenReturn(clob);
        when(clob.length()).thenReturn(0L);

        final DatabaseConfiguration config = helper.setUpConfig();
        assertEquals("", config.extractPropertyValue(rs));

        verify(rs).getObject(DatabaseConfigurationTestHelper.COL_VALUE);
        verify(clob).length();
        verifyNoMoreInteractions(rs, clob);
    }

    @Test
    public void testGetKeys() throws ConfigurationException {
        final DatabaseBuilderParameters params = helper.setUpDefaultParameters().setTable("configurationList");
        final Configuration config1 = helper.createConfig(DatabaseConfiguration.class, params);
        final Iterator<String> i = config1.getKeys();
        assertTrue(i.hasNext());
        final Object key = i.next();
        assertEquals("key3", key.toString());
        assertFalse(i.hasNext());
    }

    /**
     * Tests handling of errors in getKeys().
     */
    @Test
    public void testGetKeysError() throws ConfigurationException {
        final Iterator<String> it = setUpErrorConfig().getKeys();
        checkErrorListener(ConfigurationErrorEvent.READ, ConfigurationErrorEvent.READ, null, null);
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetKeysInternalNoDatasource() throws Exception {
        ConfigurationUtils.toString(new DatabaseConfiguration());
    }

    @Test
    public void testGetKeysMultiple() throws ConfigurationException {
        final Configuration config = helper.setUpMultiConfig();
        final Iterator<String> it = config.getKeys();

        assertEquals("key1", it.next());
        assertEquals("key2", it.next());
    }

    @Test
    public void testGetKeysSingle() throws ConfigurationException {
        final Configuration config = setUpConfig();
        final Iterator<String> it = config.getKeys();

        assertEquals("key1", it.next());
        assertEquals("key2", it.next());
    }

    @Test
    public void testGetList() throws ConfigurationException {
        final DatabaseBuilderParameters params = helper.setUpDefaultParameters().setTable("configurationList");
        final Configuration config1 = helper.createConfig(DatabaseConfiguration.class, params);
        final List<Object> list = config1.getList("key3");
        assertEquals(3, list.size());
    }

    /**
     * Tests obtaining a property as list whose value contains the list delimiter. Multiple values should be returned.
     */
    @Test
    public void testGetListWithDelimiter() throws ConfigurationException {
        final DatabaseConfiguration config = setUpConfig();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        final List<Object> values = config.getList("keyMulti");
        assertEquals(Arrays.asList("a", "b", "c"), values);
    }

    /**
     * Tests obtaining a property whose value contains the list delimiter when delimiter parsing is disabled.
     */
    @Test
    public void testGetListWithDelimiterParsingDisabled() throws ConfigurationException {
        final DatabaseConfiguration config = setUpConfig();
        assertEquals("a;b;c", config.getString("keyMulti"));
    }

    @Test
    public void testGetPropertyDirectMultiple() throws ConfigurationException {
        final Configuration config = helper.setUpMultiConfig();

        assertEquals("value1", config.getProperty("key1"));
        assertEquals("value2", config.getProperty("key2"));
        assertNull(config.getProperty("key3"));
    }

    @Test
    public void testGetPropertyDirectSingle() throws ConfigurationException {
        final Configuration config = setUpConfig();

        assertEquals("value1", config.getProperty("key1"));
        assertEquals("value2", config.getProperty("key2"));
        assertNull(config.getProperty("key3"));
    }

    /**
     * Tests handling of errors in getProperty().
     */
    @Test
    public void testGetPropertyError() throws ConfigurationException {
        setUpErrorConfig().getProperty("key1");
        checkErrorListener(ConfigurationErrorEvent.READ, ConfigurationErrorEvent.READ, "key1", null);
    }

    /**
     * Tests handling of errors in isEmpty().
     */
    @Test
    public void testIsEmptyError() throws ConfigurationException {
        assertTrue(setUpErrorConfig().isEmpty());
        checkErrorListener(ConfigurationErrorEvent.READ, ConfigurationErrorEvent.READ, null, null);
    }

    @Test
    public void testIsEmptyMultiple() throws ConfigurationException {
        final Configuration config1 = helper.setUpMultiConfig();
        assertFalse(config1.isEmpty());

        final Configuration config2 = helper.setUpMultiConfig(DatabaseConfiguration.class, "testIsEmpty");
        assertTrue(config2.isEmpty());
    }

    @Test
    public void testIsEmptySingle() throws ConfigurationException {
        final Configuration config1 = setUpConfig();
        assertFalse(config1.isEmpty());
    }

    /**
     * Tests whether the configuration has already an error listener registered that is used for logging.
     */
    @Test
    public void testLogErrorListener() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpConfig();
        assertEquals(1, config.getEventListeners(ConfigurationErrorEvent.ANY).size());
    }

    /**
     * Tests setProperty() if the property value contains the list delimiter.
     */
    @Test
    public void testSetPropertyWithDelimiter() throws ConfigurationException {
        final DatabaseConfiguration config = helper.setUpMultiConfig();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        config.setProperty("keyList", "1;2;3");
        final String[] values = config.getStringArray("keyList");
        assertArrayEquals(new String[] {"1", "2", "3"}, values);
    }

}
