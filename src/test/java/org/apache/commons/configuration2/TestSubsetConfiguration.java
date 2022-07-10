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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

/**
 * Test case for the {@link SubsetConfiguration} class.
 *
 */
public class TestSubsetConfiguration {
    static final String TEST_DIR = ConfigurationAssert.TEST_DIR_NAME;
    static final String TEST_FILE = "testDigesterConfiguration2.xml";

    @Test
    public void testClear() {
        final Configuration config = new BaseConfiguration();
        config.setProperty("test.key1", "value1");
        config.setProperty("testing.key2", "value1");

        final Configuration subset = config.subset("test");
        subset.clear();

        assertTrue(subset.isEmpty(), "the subset is not empty");
        assertFalse(config.isEmpty(), "the parent configuration is empty");
    }

    @Test
    public void testGetChildKey() {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("key", subset.getChildKey("prefix.key"), "parent key for \"prefixkey\"");
        assertEquals("", subset.getChildKey("prefix"), "parent key for \"prefix\"");

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("key", subset.getChildKey("prefixkey"), "parent key for \"prefixkey\"");
        assertEquals("", subset.getChildKey("prefix"), "parent key for \"prefix\"");
    }

    @Test
    public void testGetKeys() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test", "value0");
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys();
        assertEquals("", it.next(), "1st key");
        assertEquals("key1", it.next(), "2nd key");
        assertFalse(it.hasNext(), "too many elements");
    }

    @Test
    public void testGetKeysWithPrefix() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0");
        conf.setProperty("test.abc.key1", "value1");
        conf.setProperty("test.abcdef.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys("abc");
        assertEquals("abc", it.next(), "1st key");
        assertEquals("abc.key1", it.next(), "2nd key");
        assertFalse(it.hasNext(), "too many elements");
    }

    @Test
    public void testGetList() {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf.setProperty("test.abc", "value0,value1");
        conf.addProperty("test.abc", "value3");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");
        final List<Object> list = subset.getList("abc", new ArrayList<>());
        assertEquals(3, list.size());
    }

    /**
     * Tests whether the list delimiter handler from the parent configuration is used.
     */
    @Test
    public void testGetListDelimiterHandlerFromParent() {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        config.setListDelimiterHandler(listHandler);
        assertSame(listHandler, subset.getListDelimiterHandler(), "Not list handler from parent");
    }

    @Test
    public void testGetParent() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals(conf, subset.getParent(), "parent");
    }

    @Test
    public void testGetParentKey() {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("prefix.key", subset.getParentKey("key"), "parent key for \"key\"");
        assertEquals("prefix", subset.getParentKey(""), "parent key for \"\"");

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("prefixkey", subset.getParentKey("key"), "parent key for \"key\"");
        assertEquals("prefix", subset.getParentKey(""), "parent key for \"\"");
    }

    @Test
    public void testGetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("prefix", subset.getPrefix(), "prefix");
    }

    @Test
    public void testGetProperty() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");
        assertFalse(subset.isEmpty(), "the subset is empty");
        assertTrue(subset.containsKey("key1"), "'key1' not found in the subset");
        assertFalse(subset.containsKey("ng.key2"), "'ng.key2' found in the subset");
    }

    /**
     * Tries to create an instance without a parent configuration.
     */
    @Test
    public void testInitNoParent() {
        assertThrows(IllegalArgumentException.class, () -> new SubsetConfiguration(null, ""));
    }

    @Test
    public void testInterpolationForKeysOfTheParent() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("test", "junit");
        config.setProperty("prefix.key", "${test}");
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        assertEquals("junit", subset.getString("key", ""), "Interpolation does not resolve parent keys");
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator() {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        InterpolationTestHelper.testGetInterpolator(subset);
    }

    /**
     * Tests whether a list delimiter handler is used correctly.
     */
    @Test
    public void testListDelimiterHandling() {
        final BaseConfiguration config = new BaseConfiguration();
        final Configuration subset = config.subset("prefix");
        config.setListDelimiterHandler(new DefaultListDelimiterHandler('/'));
        subset.addProperty("list", "a/b/c");
        assertEquals(3, config.getList("prefix.list").size(), "Wrong size of list");

        ((AbstractConfiguration) subset).setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        subset.addProperty("list2", "a;b;c");
        assertEquals(3, config.getList("prefix.list2").size(), "Wrong size of list2");
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited() {
        final BaseConfiguration config = new BaseConfiguration();
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        interpolator.registerLookup("brackets", key -> "(" + key + ")");
        config.setProperty("prefix.var", "${brackets:x}");
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        assertEquals("(x)", subset.getString("var", ""), "Local lookup was not inherited");
    }

    @Test
    public void testNested() throws Exception {
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFile(ConfigurationAssert.getTestFile(TEST_FILE)));
        final Configuration config = builder.getConfiguration();
        final Configuration subConf = config.subset("tables.table(0)");
        assertTrue(subConf.getKeys().hasNext());
        final Configuration subSubConf = subConf.subset("fields.field(1)");
        final Iterator<String> itKeys = subSubConf.getKeys();
        final Set<String> keys = new HashSet<>();
        keys.add("name");
        keys.add("type");
        while (itKeys.hasNext()) {
            final String k = itKeys.next();
            assertTrue(keys.contains(k));
            keys.remove(k);
        }
        assertTrue(keys.isEmpty());
    }

    /**
     * Tests whether the list delimiter handler is also set for the parent configuration.
     */
    @Test
    public void testSetListDelimiterHandlerInParent() {
        final BaseConfiguration config = new BaseConfiguration();
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        subset.setListDelimiterHandler(listHandler);
        assertSame(listHandler, config.getListDelimiterHandler(), "Handler not passed to parent");
    }

    /**
     * Tests the case that the parent configuration is not derived from AbstractConfiguration and thus does not support a
     * list delimiter handler.
     */
    @Test
    public void testSetListDelimiterHandlerParentNotSupported() {
        final Configuration config = EasyMock.createNiceMock(Configuration.class);
        EasyMock.replay(config);
        final SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        subset.setListDelimiterHandler(listHandler);
        assertSame(listHandler, subset.getListDelimiterHandler(), "List delimiter handler not set");
    }

    @Test
    public void testSetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, null, ".");
        subset.setPrefix("prefix");

        assertEquals("prefix", subset.getPrefix(), "prefix");
    }

    @Test
    public void testSetProperty() {
        final Configuration conf = new BaseConfiguration();
        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        // set a property in the subset and check the parent
        subset.setProperty("key1", "value1");
        assertEquals("value1", subset.getProperty("key1"), "key1 in the subset configuration");
        assertEquals("value1", conf.getProperty("test.key1"), "test.key1 in the parent configuration");

        // set a property in the parent and check in the subset
        conf.setProperty("test.key2", "value2");
        assertEquals("value2", conf.getProperty("test.key2"), "test.key2 in the parent configuration");
        assertEquals("value2", subset.getProperty("key2"), "key2 in the subset configuration");
    }

    @Test
    public void testThrowExceptionOnMissing() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);

        final SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");

        assertThrows(NoSuchElementException.class, () -> subset.getString("foo"));

        config.setThrowExceptionOnMissing(false);
        assertNull(subset.getString("foo"));

        subset.setThrowExceptionOnMissing(true);
        assertThrows(NoSuchElementException.class, () -> config.getString("foo"));
    }
}
