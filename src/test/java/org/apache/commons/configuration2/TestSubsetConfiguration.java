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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test case for the {@link SubsetConfiguration} class.
 *
 * @author Emmanuel Bourg
 */
public class TestSubsetConfiguration
{
    static final String TEST_DIR = ConfigurationAssert.TEST_DIR_NAME;
    static final String TEST_FILE = "testDigesterConfiguration2.xml";

    /**
     * Tries to create an instance without a parent configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoParent() {
        new SubsetConfiguration(null, "");
    }

    @Test
    public void testGetProperty()
    {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");
        assertFalse("the subset is empty", subset.isEmpty());
        assertTrue("'key1' not found in the subset", subset.containsKey("key1"));
        assertFalse("'ng.key2' found in the subset", subset.containsKey("ng.key2"));
    }

    @Test
    public void testSetProperty()
    {
        final Configuration conf = new BaseConfiguration();
        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        // set a property in the subset and check the parent
        subset.setProperty("key1", "value1");
        assertEquals("key1 in the subset configuration", "value1", subset.getProperty("key1"));
        assertEquals("test.key1 in the parent configuration", "value1", conf.getProperty("test.key1"));

        // set a property in the parent and check in the subset
        conf.setProperty("test.key2", "value2");
        assertEquals("test.key2 in the parent configuration", "value2", conf.getProperty("test.key2"));
        assertEquals("key2 in the subset configuration", "value2", subset.getProperty("key2"));
    }

    @Test
    public void testGetParentKey()
    {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("parent key for \"key\"", "prefix.key", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("parent key for \"key\"", "prefixkey", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));
    }

    @Test
    public void testGetChildKey()
    {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefix.key"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefixkey"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));
    }

    @Test
    public void testGetKeys()
    {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test", "value0");
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys();
        assertEquals("1st key", "", it.next());
        assertEquals("2nd key", "key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    @Test
    public void testGetKeysWithPrefix()
    {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0");
        conf.setProperty("test.abc.key1", "value1");
        conf.setProperty("test.abcdef.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys("abc");
        assertEquals("1st key", "abc", it.next());
        assertEquals("2nd key", "abc.key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    @Test
    public void testGetList()
    {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf.setProperty("test.abc", "value0,value1");
        conf.addProperty("test.abc", "value3");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");
        final List<Object> list = subset.getList("abc", new ArrayList<>());
        assertEquals(3, list.size());
    }

    @Test
    public void testGetParent()
    {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("parent", conf, subset.getParent());
    }

    @Test
    public void testGetPrefix()
    {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("prefix", "prefix", subset.getPrefix());
    }

    @Test
    public void testSetPrefix()
    {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, null, ".");
        subset.setPrefix("prefix");

        assertEquals("prefix", "prefix", subset.getPrefix());
    }

    @Test
    public void testThrowExceptionOnMissing()
    {
        final BaseConfiguration config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);

        final SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");

        try
        {
            subset.getString("foo");
            fail("NoSuchElementException expected");
        }
        catch (final NoSuchElementException e)
        {
            // expected
        }

        config.setThrowExceptionOnMissing(false);
        assertNull(subset.getString("foo"));


        subset.setThrowExceptionOnMissing(true);
        try
        {
            config.getString("foo");
            fail("NoSuchElementException expected");
        }
        catch (final NoSuchElementException e)
        {
            // expected
        }
    }

    @Test
    public void testNested() throws Exception
    {
        final CombinedConfigurationBuilder builder =
                new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl()
                .setFile(ConfigurationAssert.getTestFile(TEST_FILE)));
        final Configuration config = builder.getConfiguration();
        final Configuration subConf = config.subset("tables.table(0)");
        assertTrue(subConf.getKeys().hasNext());
        final Configuration subSubConf = subConf.subset("fields.field(1)");
        final Iterator<String> itKeys = subSubConf.getKeys();
        final Set<String> keys = new HashSet<>();
        keys.add("name");
        keys.add("type");
        while(itKeys.hasNext())
        {
            final String k = itKeys.next();
            assertTrue(keys.contains(k));
            keys.remove(k);
        }
        assertTrue(keys.isEmpty());
    }

    @Test
    public void testClear()
    {
        final Configuration config = new BaseConfiguration();
        config.setProperty("test.key1", "value1");
        config.setProperty("testing.key2", "value1");

        final Configuration subset = config.subset("test");
        subset.clear();

        assertTrue("the subset is not empty", subset.isEmpty());
        assertFalse("the parent configuration is empty", config.isEmpty());
    }

    /**
     * Tests whether a list delimiter handler is used correctly.
     */
    @Test
    public void testListDelimiterHandling()
    {
        final BaseConfiguration config = new BaseConfiguration();
        final Configuration subset = config.subset("prefix");
        config.setListDelimiterHandler(new DefaultListDelimiterHandler('/'));
        subset.addProperty("list", "a/b/c");
        assertEquals("Wrong size of list", 3, config.getList("prefix.list")
                .size());

        ((AbstractConfiguration) subset)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        subset.addProperty("list2", "a;b;c");
        assertEquals("Wrong size of list2", 3, config.getList("prefix.list2")
                .size());
    }

    /**
     * Tests whether the list delimiter handler is also set for the parent
     * configuration.
     */
    @Test
    public void testSetListDelimiterHandlerInParent()
    {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset =
                (AbstractConfiguration) config.subset("prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        subset.setListDelimiterHandler(listHandler);
        assertSame("Handler not passed to parent", listHandler,
                config.getListDelimiterHandler());
    }

    /**
     * Tests whether the list delimiter handler from the parent configuration is
     * used.
     */
    @Test
    public void testGetListDelimiterHandlerFromParent()
    {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset =
                (AbstractConfiguration) config.subset("prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        config.setListDelimiterHandler(listHandler);
        assertSame("Not list handler from parent", listHandler,
                subset.getListDelimiterHandler());
    }

    /**
     * Tests the case that the parent configuration is not derived from
     * AbstractConfiguration and thus does not support a list delimiter handler.
     */
    @Test
    public void testSetListDelimiterHandlerParentNotSupported()
    {
        final Configuration config = EasyMock.createNiceMock(Configuration.class);
        EasyMock.replay(config);
        final SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        subset.setListDelimiterHandler(listHandler);
        assertSame("List delimiter handler not set", listHandler,
                subset.getListDelimiterHandler());
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator()
    {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        InterpolationTestHelper.testGetInterpolator(subset);
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited() {
        final BaseConfiguration config = new BaseConfiguration();
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        interpolator.registerLookup("brackets", new Lookup(){

            @Override
            public String lookup(final String key) {
                return "(" + key +")";
            }

        });
        config.setProperty("prefix.var", "${brackets:x}");
        final AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        assertEquals("Local lookup was not inherited", "(x)", subset
                .getString("var", ""));
    }

    @Test
    public void testInterpolationForKeysOfTheParent() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("test", "junit");
        config.setProperty("prefix.key", "${test}");
        final AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        assertEquals("Interpolation does not resolve parent keys", "junit",
                subset.getString("key", ""));
    }
}
