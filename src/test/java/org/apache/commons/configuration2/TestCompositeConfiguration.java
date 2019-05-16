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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code CompositeConfiguration}.
 *
 */
public class TestCompositeConfiguration
{
    /** Constant for a test property to be checked.*/
    private static final String TEST_PROPERTY = "test.source.property";

    protected PropertiesConfiguration conf1;
    protected PropertiesConfiguration conf2;
    protected XMLConfiguration xmlConf;
    protected CompositeConfiguration cc;

    /**
     * The File that we test with
     */
    private final String testProperties = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();
    private final String testProperties2 = ConfigurationAssert.getTestFile("test2.properties").getAbsolutePath();
    private final String testPropertiesXML = ConfigurationAssert.getTestFile("test.xml").getAbsolutePath();

    @Before
    public void setUp() throws Exception
    {
        cc = new CompositeConfiguration();
        final ListDelimiterHandler listHandler = new LegacyListDelimiterHandler(',');
        conf1 = new PropertiesConfiguration();
        conf1.setListDelimiterHandler(listHandler);
        final FileHandler handler1 = new FileHandler(conf1);
        handler1.setFileName(testProperties);
        handler1.load();
        conf2 = new PropertiesConfiguration();
        conf2.setListDelimiterHandler(listHandler);
        final FileHandler handler2 = new FileHandler(conf2);
        handler2.setFileName(testProperties2);
        handler2.load();
        xmlConf = new XMLConfiguration();
        final FileHandler handler3 = new FileHandler(xmlConf);
        handler3.load(new File(testPropertiesXML));

        cc.setThrowExceptionOnMissing(true);
    }

    @Test
    public void testThrowExceptionOnMissing()
    {
        assertTrue("Throw Exception Property is not set!", cc.isThrowExceptionOnMissing());
    }

    @Test
    public void testAddRemoveConfigurations() throws Exception
    {
        cc.addConfiguration(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.addConfiguration(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.addConfiguration(conf2);
        assertEquals("Number of configurations", 3, cc.getNumberOfConfigurations());
        cc.removeConfiguration(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.clear();
        assertEquals("Number of configurations", 1, cc.getNumberOfConfigurations());
    }

    @Test
    public void testAddFirstRemoveConfigurations() throws Exception
    {
        cc.addConfigurationFirst(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.addConfigurationFirst(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.addConfigurationFirst(conf2);
        assertEquals("Number of configurations", 3, cc.getNumberOfConfigurations());
        cc.removeConfiguration(conf1);
        assertEquals("Number of configurations", 2, cc.getNumberOfConfigurations());
        cc.clear();
        assertEquals("Number of configurations", 1, cc.getNumberOfConfigurations());
    }

    @Test
    public void testGetPropertyWIncludes() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
        final List<Object> l = cc.getList("packages");
        assertTrue(l.contains("packagea"));
    }

    @Test
    public void testGetProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
        assertEquals("Make sure we get the property from conf1 first", "test.properties", cc.getString("propertyInOrder"));
        cc.clear();

        cc.addConfiguration(conf2);
        cc.addConfiguration(conf1);
        assertEquals("Make sure we get the property from conf2 first", "test2.properties", cc.getString("propertyInOrder"));
        cc.clear();

        cc.addConfiguration(conf1);
        cc.addConfigurationFirst(conf2);
        assertEquals("Make sure we get the property from conf2 first", "test2.properties", cc.getString("propertyInOrder"));
        cc.clear();
    }

    @Test
    public void testCantRemoveMemoryConfig() throws Exception
    {
        cc.clear();
        assertEquals(1, cc.getNumberOfConfigurations());

        final Configuration internal = cc.getConfiguration(0);
        cc.removeConfiguration(internal);

        assertEquals(1, cc.getNumberOfConfigurations());
    }

    @Test
    public void testGetPropertyMissing() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
        try
        {
            assertNull(cc.getString("bogus.property"));
            fail("Should have thrown a NoSuchElementException");
        }
        catch (final NoSuchElementException nsee)
        {
            assertTrue(nsee.getMessage().contains("bogus.property"));
        }

        assertTrue("Should be false", !cc.getBoolean("test.missing.boolean", false));
        assertTrue("Should be true", cc.getBoolean("test.missing.boolean.true", true));
    }

    /**
     * Tests {@code List} parsing.
     */
    @Test
    public void testMultipleTypesOfConfigs() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        assertEquals("Make sure we get the property from conf1 first", 1, cc.getInt("test.short"));
        cc.clear();

        cc.addConfiguration(xmlConf);
        cc.addConfiguration(conf1);
        assertEquals("Make sure we get the property from xml", 8, cc.getInt("test.short"));
    }

    @Test
    public void testPropertyExistsInOnlyOneConfig() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        assertEquals("value", cc.getString("element"));
    }

    /**
     * Tests getting a default when the key doesn't exist
     */
    @Test
    public void testDefaultValueWhenKeyMissing() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        assertEquals("default", cc.getString("bogus", "default"));
        assertTrue(1.4 == cc.getDouble("bogus", 1.4));
        assertTrue(1.4 == cc.getDouble("bogus", 1.4));
    }

    @Test
    public void testGettingConfiguration() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        assertEquals(PropertiesConfiguration.class, cc.getConfiguration(0).getClass());
        assertEquals(XMLConfiguration.class, cc.getConfiguration(1).getClass());
    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    @Test
    public void testClearingProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        cc.clearProperty("test.short");
        assertTrue("Make sure test.short is gone!", !cc.containsKey("test.short"));
    }

    /**
     * Tests adding values.  Make sure they _DON'T_ override any other properties but add to the
     * existing properties  and keep sequence
     */
    @Test
    public void testAddingProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);

        String[] values = cc.getStringArray("test.short");

        assertEquals("Number of values before add is wrong!", 1, values.length);
        assertEquals("First Value before add is wrong", "1", values[0]);

        cc.addProperty("test.short", "88");

        values = cc.getStringArray("test.short");

        assertEquals("Number of values is wrong!", 2, values.length);
        assertEquals("First Value is wrong", "1", values[0]);
        assertEquals("Third Value is wrong", "88", values[1]);
    }

    /**
     * Tests setting values.  These are set in memory mode only!
     */
    @Test
    public void testSettingMissingProperty() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);
        cc.setProperty("my.new.property", "supernew");
        assertEquals("supernew", cc.getString("my.new.property"));
    }

    /**
     * Tests retrieving subsets of configurations
     */
    @Test
    public void testGettingSubset() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);

        Configuration subset = cc.subset("test");
        assertNotNull(subset);
        assertFalse("Shouldn't be empty", subset.isEmpty());
        assertEquals("Make sure the initial loaded configs subset overrides any later add configs subset", "1", subset.getString("short"));

        cc.setProperty("test.short", "43");
        subset = cc.subset("test");
        assertEquals("Make sure the initial loaded configs subset overrides any later add configs subset", "43", subset.getString("short"));
    }

    /**
     * Tests subsets and still can resolve elements
     */
    @Test
    public void testSubsetCanResolve() throws Exception
    {
        cc = new CompositeConfiguration();
        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty("subset.tempfile", "${java.io.tmpdir}/file.tmp");
        cc.addConfiguration(config);
        cc.addConfiguration(ConfigurationConverter.getConfiguration(System.getProperties()));

        final Configuration subset = cc.subset("subset");
        assertEquals(System.getProperty("java.io.tmpdir") + "/file.tmp", subset.getString("tempfile"));
    }

    /**
     * Tests {@code List} parsing.
     */
    @Test
    public void testList() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);

        List<Object> packages = cc.getList("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());

        final List<Object> defaultList = new ArrayList<>();
        defaultList.add("1");
        defaultList.add("2");

        packages = cc.getList("packages.which.dont.exist", defaultList);
        // we should get 2 packages here
        assertEquals(2, packages.size());

    }

    /**
     * Tests {@code String} array parsing.
     */
    @Test
    public void testStringArray() throws Exception
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(xmlConf);

        String[] packages = cc.getStringArray("packages");
        // we should get 3 packages here
        assertEquals(3, packages.length);

        packages = cc.getStringArray("packages.which.dont.exist");
        // we should get 0 packages here
        assertEquals(0, packages.length);
    }

    @Test
    public void testGetList()
    {
        final Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("array", "value1");
        conf1.addProperty("array", "value2");

        final Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("array", "value3");
        conf2.addProperty("array", "value4");

        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);

        // check the composite 'array' property
        List<Object> list = cc.getList("array");
        assertNotNull("null list", list);
        assertEquals("list size", 2, list.size());
        assertTrue("'value1' not found in the list", list.contains("value1"));
        assertTrue("'value2' not found in the list", list.contains("value2"));

        // add an element to the list in the composite configuration
        cc.addProperty("array", "value5");

        // test the new list
        list = cc.getList("array");
        assertNotNull("null list", list);
        assertEquals("list size", 3, list.size());
        assertTrue("'value1' not found in the list", list.contains("value1"));
        assertTrue("'value2' not found in the list", list.contains("value2"));
        assertTrue("'value5' not found in the list", list.contains("value5"));
    }

    /**
     * Tests {@code getKeys} preserves the order
     */
    @Test
    public void testGetKeysPreservesOrder() throws Exception
    {
        cc.addConfiguration(conf1);
        final List<String> orderedList = new ArrayList<>();
        for (final Iterator<String> keys = conf1.getKeys(); keys.hasNext();)
        {
            orderedList.add(keys.next());
        }
        final List<String> iteratedList = new ArrayList<>();
        for (final Iterator<String> keys = cc.getKeys(); keys.hasNext();)
        {
            iteratedList.add(keys.next());
        }
        assertEquals(orderedList.size(), iteratedList.size());
        for (int i = 0; i < orderedList.size(); i++)
        {
            assertEquals(orderedList.get(i), iteratedList.get(i));
        }
    }

    /**
     * Tests {@code getKeys(String key)} preserves the order
     */
    @Test
    public void testGetKeys2PreservesOrder() throws Exception
    {
        cc.addConfiguration(conf1);
        final List<String> orderedList = new ArrayList<>();
        for (final Iterator<String> keys = conf1.getKeys("test"); keys.hasNext();)
        {
            orderedList.add(keys.next());
        }
        final List<String> iteratedList = new ArrayList<>();
        for (final Iterator<String> keys = cc.getKeys("test"); keys.hasNext();)
        {
            iteratedList.add(keys.next());
        }
        assertEquals(orderedList.size(), iteratedList.size());
        for (int i = 0; i < orderedList.size(); i++)
        {
            assertEquals(orderedList.get(i), iteratedList.get(i));
        }
    }

    @Test
    public void testGetStringWithDefaults()
    {
        final BaseConfiguration defaults = new BaseConfiguration();
        defaults.addProperty("default", "default string");

        final CompositeConfiguration c = new CompositeConfiguration(defaults);
        c.setThrowExceptionOnMissing(cc.isThrowExceptionOnMissing());
        c.addProperty("string", "test string");

        assertEquals("test string", c.getString("string"));
        try
        {
            c.getString("XXX");
            fail("Should throw NoSuchElementException exception");
        }
        catch (final NoSuchElementException e)
        {
            //ok
        }
        catch (final Exception e)
        {
            fail("Should throw NoSuchElementException exception, not " + e);
        }

        //test defaults
        assertEquals("test string", c.getString("string", "some default value"));
        assertEquals("default string", c.getString("default"));
        assertEquals("default string", c.getString("default", "some default value"));
        assertEquals("some default value", c.getString("XXX", "some default value"));
    }

    @Test
    public void testCheckingInMemoryConfiguration() throws Exception
    {
        final String TEST_KEY = "testKey";
        final Configuration defaults = new PropertiesConfiguration();
        defaults.setProperty(TEST_KEY, "testValue");
        final Configuration testConfiguration = new CompositeConfiguration(defaults);
        assertTrue(testConfiguration.containsKey(TEST_KEY));
        assertFalse(testConfiguration.isEmpty());
        boolean foundTestKey = false;
        final Iterator<String> i = testConfiguration.getKeys();
        //assertTrue(i instanceof IteratorChain);
        //IteratorChain ic = (IteratorChain)i;
        //assertEquals(2,i.size());
        for (; i.hasNext();)
        {
            final String key = i.next();
            if (key.equals(TEST_KEY))
            {
                foundTestKey = true;
            }
        }
        assertTrue(foundTestKey);
        testConfiguration.clearProperty(TEST_KEY);
        assertFalse(testConfiguration.containsKey(TEST_KEY));
    }

    @Test
    public void testStringArrayInterpolation()
    {
        final CompositeConfiguration config = new CompositeConfiguration();
        config.addProperty("base", "foo");
        config.addProperty("list", "${base}.bar1");
        config.addProperty("list", "${base}.bar2");
        config.addProperty("list", "${base}.bar3");

        final String[] array = config.getStringArray("list");
        assertEquals("size", 3, array.length);
        assertEquals("1st element", "foo.bar1", array[0]);
        assertEquals("2nd element", "foo.bar2", array[1]);
        assertEquals("3rd element", "foo.bar3", array[2]);
    }

    /**
     * Tests whether global interpolation works with lists.
     */
    @Test
    public void testListInterpolation()
    {
        final PropertiesConfiguration c1 = new PropertiesConfiguration();
        c1.addProperty("c1.value", "test1");
        c1.addProperty("c1.value", "${c2.value}");
        cc.addConfiguration(c1);
        final PropertiesConfiguration c2 = new PropertiesConfiguration();
        c2.addProperty("c2.value", "test2");
        cc.addConfiguration(c2);
        final List<Object> lst = cc.getList("c1.value");
        assertEquals("Wrong list size", 2, lst.size());
        assertEquals("Wrong first element", "test1", lst.get(0));
        assertEquals("Wrong second element", "test2", lst.get(1));
    }

    @Test
    public void testInstanciateWithCollection()
    {
        final Collection<Configuration> configs = new ArrayList<>();
        configs.add(xmlConf);
        configs.add(conf1);
        configs.add(conf2);

        final CompositeConfiguration config = new CompositeConfiguration(configs);
        assertEquals("Number of configurations", 4, config.getNumberOfConfigurations());
        assertTrue("The in memory configuration is not empty", config.getInMemoryConfiguration().isEmpty());
    }

    @Test
    public void testClone()
    {
        final CompositeConfiguration cc2 = (CompositeConfiguration) cc.clone();
        assertEquals("Wrong number of contained configurations", cc
                .getNumberOfConfigurations(), cc2.getNumberOfConfigurations());

        final StrictConfigurationComparator comp = new StrictConfigurationComparator();
        for (int i = 0; i < cc.getNumberOfConfigurations(); i++)
        {
            assertEquals("Wrong configuration class at " + i, cc
                    .getConfiguration(i).getClass(), cc2.getConfiguration(i)
                    .getClass());
            assertNotSame("Configuration was not cloned", cc
                    .getConfiguration(i), cc2.getConfiguration(i));
            assertTrue("Configurations at " + i + " not equal", comp.compare(cc
                    .getConfiguration(i), cc2.getConfiguration(i)));
        }

        assertTrue("Configurations are not equal", comp.compare(cc, cc2));
    }

    /**
     * Tests cloning if one of the contained configurations does not support
     * this operation. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCloneNotSupported()
    {
        cc.addConfiguration(new NonCloneableConfiguration());
        cc.clone();
    }

    /**
     * Ensures that event listeners are not cloned.
     */
    @Test
    public void testCloneEventListener()
    {
        cc.addEventListener(ConfigurationEvent.ANY, new EventListenerTestImpl(null));
        final CompositeConfiguration cc2 = (CompositeConfiguration) cc.clone();
        assertTrue("Listeners have been cloned", cc2
                .getEventListeners(ConfigurationEvent.ANY).isEmpty());
    }

    /**
     * Tests whether interpolation works as expected after cloning.
     */
    @Test
    public void testCloneInterpolation()
    {
        final CompositeConfiguration cc2 = (CompositeConfiguration) cc.clone();
        assertNotSame("Interpolator was not cloned", cc.getInterpolator(),
                cc2.getInterpolator());
    }

    /**
     * Tests whether add property events are triggered.
     */
    @Test
    public void testEventAddProperty()
    {
        final EventListenerTestImpl listener = new EventListenerTestImpl(cc);
        cc.addEventListener(ConfigurationEvent.ANY, listener);
        cc.addProperty("test", "value");
        listener.checkEvent(ConfigurationEvent.ADD_PROPERTY, "test", "value", true);
        listener.checkEvent(ConfigurationEvent.ADD_PROPERTY, "test", "value", false);
        listener.done();
    }

    /**
     * Tests whether set property events are triggered.
     */
    @Test
    public void testEventSetProperty()
    {
        final EventListenerTestImpl listener = new EventListenerTestImpl(cc);
        cc.addEventListener(ConfigurationEvent.ANY, listener);
        cc.setProperty("test", "value");
        listener.checkEvent(ConfigurationEvent.SET_PROPERTY, "test", "value", true);
        listener.checkEvent(ConfigurationEvent.SET_PROPERTY, "test", "value", false);
        listener.done();
    }

    /**
     * Tests whether clear property events are triggered.
     */
    @Test
    public void testEventClearProperty()
    {
        cc.addConfiguration(conf1);
        final String key = "configuration.loaded";
        assertTrue("Wrong value for property", cc
                .getBoolean(key));
        final EventListenerTestImpl listener = new EventListenerTestImpl(cc);
        cc.addEventListener(ConfigurationEvent.ANY, listener);
        cc.clearProperty(key);
        assertFalse("Key still present", cc.containsKey(key));
        listener.checkEvent(ConfigurationEvent.CLEAR_PROPERTY, key, null, true);
        listener.checkEvent(ConfigurationEvent.CLEAR_PROPERTY, key, null, false);
        listener.done();
    }

    /**
     * Tests changing the list delimiter handler.
     */
    @Test
    public void testSetListDelimiter()
    {
        cc.setListDelimiterHandler(new DefaultListDelimiterHandler('/'));
        checkSetListDelimiterHandler();
    }

    /**
     * Tests whether the correct list delimiter handler is set after a clear
     * operation.
     */
    @Test
    public void testSetListDelimiterAfterClear()
    {
        cc.setListDelimiterHandler(new DefaultListDelimiterHandler('/'));
        cc.clear();
        checkSetListDelimiterHandler();
    }

    /**
     * Helper method for testing whether the list delimiter is correctly
     * handled.
     */
    private void checkSetListDelimiterHandler()
    {
        cc.addProperty("test.list", "a/b/c");
        cc.addProperty("test.property", "a,b,c");
        assertEquals("Wrong number of list elements", 3, cc
                .getList("test.list").size());
        assertEquals("Wrong value of property", "a,b,c", cc
                .getString("test.property"));

        final AbstractConfiguration config =
                (AbstractConfiguration) cc.getInMemoryConfiguration();
        final DefaultListDelimiterHandler listHandler =
                (DefaultListDelimiterHandler) config.getListDelimiterHandler();
        assertEquals("Wrong list delimiter", '/', listHandler.getDelimiter());
    }

    /**
     * Prepares a test of the getSource() method.
     */
    private void setUpSourceTest()
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
    }

    /**
     * Tests the getSource() method if the property is defined in a single child
     * configuration.
     */
    @Test
    public void testGetSourceSingle()
    {
        setUpSourceTest();
        conf1.addProperty(TEST_PROPERTY, Boolean.TRUE);
        assertSame("Wrong source configuration", conf1, cc
                .getSource(TEST_PROPERTY));
    }

    /**
     * Tests the getSource() method for an unknown property key.
     */
    @Test
    public void testGetSourceUnknown()
    {
        setUpSourceTest();
        assertNull("Wrong source for unknown key", cc.getSource(TEST_PROPERTY));
    }

    /**
     * Tests the getSource() method for a property contained in the in memory
     * configuration.
     */
    @Test
    public void testGetSourceInMemory()
    {
        setUpSourceTest();
        cc.addProperty(TEST_PROPERTY, Boolean.TRUE);
        assertSame("Source not found in in-memory config", cc
                .getInMemoryConfiguration(), cc.getSource(TEST_PROPERTY));
    }

    /**
     * Tests the getSource() method if the property is defined by multiple child
     * configurations. In this case an exception should be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSourceMultiple()
    {
        setUpSourceTest();
        conf1.addProperty(TEST_PROPERTY, Boolean.TRUE);
        cc.addProperty(TEST_PROPERTY, "a value");
        cc.getSource(TEST_PROPERTY);
    }

    /**
     * Tests the getSource() method for a null key. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSourceNull()
    {
        cc.getSource(null);
    }

    /**
     * Prepares a test for interpolation with multiple configurations and
     * similar properties.
     */
    private void prepareInterpolationTest()
    {
        final PropertiesConfiguration p = new PropertiesConfiguration();
        p.addProperty("foo", "initial");
        p.addProperty("bar", "${foo}");
        p.addProperty("prefix.foo", "override");

        cc.addConfiguration(p.subset("prefix"));
        cc.addConfiguration(p);
        assertEquals("Wrong value on direct access", "override", cc
                .getString("bar"));
    }

    /**
     * Tests querying a list when a tricky interpolation is involved. This is
     * related to CONFIGURATION-339.
     */
    @Test
    public void testGetListWithInterpolation()
    {
        prepareInterpolationTest();
        final List<Object> lst = cc.getList("bar");
        assertEquals("Wrong number of values", 1, lst.size());
        assertEquals("Wrong value in list", "override", lst.get(0));
    }

    /**
     * Tests querying a string array when a tricky interpolation is involved.
     */
    @Test
    public void testGetStringArrayWithInterpolation()
    {
        prepareInterpolationTest();
        final String[] values = cc.getStringArray("bar");
        assertEquals("Wrong number of values", 1, values.length);
        assertEquals("Wrong value in array", "override", values[0]);
    }

    /**
     * Tests whether interpolation works if multiple configurations are
     * involved. This test is related to CONFIGURATION-441.
     */
    @Test
    public void testInterpolationInMultipleConfigs()
    {
        final Configuration c1 = new PropertiesConfiguration();
        c1.addProperty("property.one", "one");
        c1.addProperty("property.two", "two");
        final Configuration c2 = new PropertiesConfiguration();
        c2.addProperty("property.one.ref", "${property.one}");
        cc.addConfiguration(c1);
        cc.addConfiguration(c2);
        assertEquals("Wrong interpolated value", "one",
                cc.getString("property.one.ref"));
    }

    /**
     * Tests whether interpolation works if a variable references a property
     * with multiple values. This test is related to CONFIGURATION-632.
     */
    @Test
    public void testInterpolationArrayReference()
    {
        final Configuration props = new PropertiesConfiguration();
        final String[] values = { "a", "property", "with", "multiple", "values" };
        props.addProperty("keyMultiValues", values);
        props.addProperty("keyReference", "${keyMultiValues}");
        cc.addConfiguration(props);
        assertArrayEquals("Wrong interpolated value", values,
                cc.getStringArray("keyReference"));
    }

    /**
     * Tests the behavior of setListDelimiterHandler() if the in-memory configuration
     * is not derived from BaseConfiguration. This test is related to
     * CONFIGURATION-476.
     */
    @Test
    public void testSetListDelimiterInMemoryConfigNonBaseConfig()
    {
        final Configuration inMemoryConfig = EasyMock.createMock(Configuration.class);
        EasyMock.replay(inMemoryConfig);
        cc = new CompositeConfiguration(inMemoryConfig);
        cc.setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
    }

    /**
     * Tests whether a configuration can act as both regular child configuration
     * and in-memory configuration. This test is related to CONFIGURATION-471.
     */
    @Test
    public void testUseChildConfigAsInMemoryConfig()
    {
        conf1.setProperty(TEST_PROPERTY, "conf1");
        conf2.setProperty(TEST_PROPERTY, "conf2");
        cc.addConfiguration(conf1, true);
        cc.addConfiguration(conf2);
        assertEquals("Wrong number of configurations", 2,
                cc.getNumberOfConfigurations());
        assertEquals("Wrong property", "conf1", cc.getString(TEST_PROPERTY));
        cc.addProperty("newProperty", "newValue");
        assertEquals("Not added to in-memory config", "newValue",
                conf1.getString("newProperty"));
    }

    /**
     * Tests whether the in-memory configuration can be replaced by a new child
     * configuration.
     */
    @Test
    public void testReplaceInMemoryConfig()
    {
        conf1.setProperty(TEST_PROPERTY, "conf1");
        conf2.setProperty(TEST_PROPERTY, "conf2");
        cc.addConfiguration(conf1, true);
        cc.addProperty("newProperty1", "newValue1");
        cc.addConfiguration(conf2, true);
        cc.addProperty("newProperty2", "newValue2");
        assertEquals("Wrong property", "conf1", cc.getString(TEST_PROPERTY));
        assertEquals("Not added to in-memory config", "newValue1",
                conf1.getString("newProperty1"));
        assertEquals("In-memory config not changed", "newValue2",
                conf2.getString("newProperty2"));
    }

    /**
     * Creates a test synchronizer and installs it at the test configuration.
     *
     * @return the test synchronizer
     */
    private SynchronizerTestImpl installSynchronizer()
    {
        cc.addConfiguration(conf1);
        cc.addConfiguration(conf2);
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        cc.setSynchronizer(sync);
        return sync;
    }

    /**
     * Tests whether adding a child configuration is synchronized.
     */
    @Test
    public void testAddConfigurationSynchronized()
    {
        final SynchronizerTestImpl sync = installSynchronizer();
        cc.addConfiguration(xmlConf);
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether removing a child configuration is synchronized.
     */
    @Test
    public void testRemoveConfigurationSynchronized()
    {
        final SynchronizerTestImpl sync = installSynchronizer();
        cc.removeConfiguration(conf1);
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether access to a configuration by index is synchronized.
     */
    @Test
    public void testGetConfigurationSynchronized()
    {
        final SynchronizerTestImpl sync = installSynchronizer();
        assertEquals("Wrong result", conf1, cc.getConfiguration(0));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether access to the in-memory configuration is synchronized.
     */
    @Test
    public void testGetInMemoryConfigurationSynchronized()
    {
        final SynchronizerTestImpl sync = installSynchronizer();
        cc.getInMemoryConfiguration();
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether querying the number of child configurations is
     * synchronized.
     */
    @Test
    public void testGetNumberOfConfigurationsSynchronized()
    {
        final SynchronizerTestImpl sync = installSynchronizer();
        assertEquals("Wrong number of configurations", 3,
                cc.getNumberOfConfigurations());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }
}
