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
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
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
import org.junit.jupiter.api.Test;

/**
 * Test case for the {@link SubsetConfiguration} class.
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

        assertTrue(subset.isEmpty());
        assertFalse(config.isEmpty());
    }

    /**
     * Tries to create an instance without a parent configuration.
     */
    @Test
    public void testConstructNullParent() {
        assertThrows(NullPointerException.class, () -> new SubsetConfiguration(null, ""));
    }

    @Test
    public void testGetChildKey() {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("key", subset.getChildKey("prefix.key"));
        assertEquals("", subset.getChildKey("prefix"));

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("key", subset.getChildKey("prefixkey"));
        assertEquals("", subset.getChildKey("prefix"));
    }

    @Test
    public void testGetKeys() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test", "value0");
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys();
        assertEquals("", it.next());
        assertEquals("key1", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetKeysWithPrefix() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0");
        conf.setProperty("test.abc.key1", "value1");
        conf.setProperty("test.abcdef.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        final Iterator<String> it = subset.getKeys("abc");
        assertEquals("abc", it.next());
        assertEquals("abc.key1", it.next());
        assertFalse(it.hasNext());
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
        assertSame(listHandler, subset.getListDelimiterHandler());
    }

    @Test
    public void testGetParent() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals(conf, subset.getParent());
    }

    @Test
    public void testGetParentKey() {
        final Configuration conf = new BaseConfiguration();
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");
        assertEquals("prefix.key", subset.getParentKey("key"));
        assertEquals("prefix", subset.getParentKey(""));

        // subset without delimiter
        subset = new SubsetConfiguration(conf, "prefix", null);
        assertEquals("prefixkey", subset.getParentKey("key"));
        assertEquals("prefix", subset.getParentKey(""));
    }

    @Test
    public void testGetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("prefix", subset.getPrefix());
    }

    @Test
    public void testGetProperty() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        final Configuration subset = new SubsetConfiguration(conf, "test", ".");
        assertFalse(subset.isEmpty());
        assertTrue(subset.containsKey("key1"));
        assertFalse(subset.containsKey("ng.key2"));
    }

    @Test
    public void testInterpolationForKeysOfTheParent() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("test", "junit");
        config.setProperty("prefix.key", "${test}");
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        assertEquals("junit", subset.getString("key", ""));
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
        assertEquals(3, config.getList("prefix.list").size());

        ((AbstractConfiguration) subset).setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        subset.addProperty("list2", "a;b;c");
        assertEquals(3, config.getList("prefix.list2").size());
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited() {
        final BaseConfiguration config = new BaseConfiguration();
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        interpolator.registerLookup("brackets", key -> "(" + key + ")");
        config.setProperty("prefix.var", "${brackets:x}");
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("prefix");
        assertEquals("(x)", subset.getString("var", ""));
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

    @Test
    public void testPrefixDelimiter(){
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("part1.part2@test.key1", "value1");
        config.setProperty("part1.part2", "value2");
        config.setProperty("part3.part4@testing.key2", "value3");

        final SubsetConfiguration subset = new SubsetConfiguration(config, "part1.part2", "@");
        // Check subset properties
        assertEquals("value1", subset.getString("test.key1"));
        assertEquals("value2", subset.getString(""));
        assertNull(subset.getString("testing.key2"));

        // Check for empty subset configuration and iterator
        assertEquals(2, subset.size());
        assertFalse(subset.isEmpty());
        assertTrue(subset.getKeys().hasNext());
    }

    @Test
    public void testPrefixDelimiterNegativeTest(){
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("part1.part2@test.key1", "value1");
        config.setProperty("part3.part4@testing.key2", "value2");

        final SubsetConfiguration subset = new SubsetConfiguration(config, "part1.part2", "@") {
            // Anonymous inner class declaration to override SubsetConfiguration.getKeysInternal() - Call
            // ImutableConfiguration.getKeys(String) on the parent configuration of the SubsetConfiguration in order to
            // not consequently pass the prefix delimiter
            @Override
            protected Iterator<String> getKeysInternal() {
                Class<?> subsetIteratorClass;
                try {
                    subsetIteratorClass = Class
                            .forName("org.apache.commons.configuration2.SubsetConfiguration$SubsetIterator");
                    final Constructor<?> ctor = subsetIteratorClass.getDeclaredConstructor(SubsetConfiguration.class,
                            Iterator.class);
                    ctor.setAccessible(true);

                    return (Iterator<String>) ctor.newInstance(this, parent.getKeys("part1.part2"));
                } catch (final Exception ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        };

        // Check subset properties - contains one property
        assertEquals("value1", subset.getString("test.key1"));
        assertNull(subset.getString("testing.key2"));

        // Check for empty subset configuration and iterator - even if the SubsetConfiguration contains properties, like
        // checked previously its states that it is empty
        assertEquals(0, subset.size());
        assertTrue(subset.isEmpty());
        assertFalse(subset.getKeys().hasNext());
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
        assertSame(listHandler, config.getListDelimiterHandler());
    }

    /**
     * Tests the case that the parent configuration is not derived from AbstractConfiguration and thus does not support a
     * list delimiter handler.
     */
    @Test
    public void testSetListDelimiterHandlerParentNotSupported() {
        final Configuration config = mock(Configuration.class);
        final SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        subset.setListDelimiterHandler(listHandler);
        assertSame(listHandler, subset.getListDelimiterHandler());
    }

    @Test
    public void testSetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final SubsetConfiguration subset = new SubsetConfiguration(conf, null, ".");
        subset.setPrefix("prefix");

        assertEquals("prefix", subset.getPrefix());
    }

    @Test
    public void testSetProperty() {
        final Configuration conf = new BaseConfiguration();
        final Configuration subset = new SubsetConfiguration(conf, "test", ".");

        // set a property in the subset and check the parent
        subset.setProperty("key1", "value1");
        assertEquals("value1", subset.getProperty("key1"));
        assertEquals("value1", conf.getProperty("test.key1"));

        // set a property in the parent and check in the subset
        conf.setProperty("test.key2", "value2");
        assertEquals("value2", conf.getProperty("test.key2"));
        assertEquals("value2", subset.getProperty("key2"));
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
