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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test case for the {@link PrefixConfiguration} class.
 *
 */
public class TestPrefixConfiguration {

    /**
     * Tries to create an instance without a parent configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoParent() {
        new PrefixConfiguration(null, "");
    }

    @Test
    public void testGetProperty() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("key1", "value1");

        final Configuration prefix = new PrefixConfiguration(conf, "testing", ".");
        assertFalse("the prefix is empty", prefix.isEmpty());
        assertTrue("'key1' not found in the subset", prefix.containsKey("testing.key1"));
    }

    @Test
    public void testSetProperty() {
        final Configuration conf = new BaseConfiguration();
        final Configuration prefix = new PrefixConfiguration(conf, "test", ".");

        // set a property in the prefix and check the parent
        prefix.setProperty("test.key1", "value1");
        assertEquals("key1 in the prefix configuration", "value1", prefix.getProperty("test.key1"));
        assertEquals("test.key1 in the parent configuration", "value1", conf.getProperty("key1"));

        // set a property in the parent and check in the subset
        conf.setProperty("key2", "value2");
        assertEquals("test.key2 in the parent configuration", "value2", conf.getProperty("key2"));
        assertEquals("key2 in the prefix configuration", "value2", prefix.getProperty("test.key2"));
    }

    @Test
    public void testGetParentKey() {
        final Configuration conf = new BaseConfiguration();
        // prefix with delimiter
        PrefixConfiguration prefix = new PrefixConfiguration(conf, "prefix", ".");
        assertEquals("parent key for \"key\"", "prefix.key", prefix.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", prefix.getParentKey(""));

        // prefix without delimiter
        prefix = new PrefixConfiguration(conf, "prefix", null);
        assertEquals("parent key for \"key\"", "prefixkey", prefix.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", prefix.getParentKey(""));
    }

    @Test
    public void testGetChildKey() {
        final Configuration conf = new BaseConfiguration();
        // prefix with delimiter
        PrefixConfiguration prefix = new PrefixConfiguration(conf, "prefix", ".");
        assertEquals("parent key for \"prefixkey\"", "key", prefix.getChildKey("prefix.key"));
        assertEquals("parent key for \"prefix\"", "", prefix.getChildKey("prefix"));

        // prefix without delimiter
        prefix = new PrefixConfiguration(conf, "prefix", null);
        assertEquals("parent key for \"prefixkey\"", "key", prefix.getChildKey("prefixkey"));
        assertEquals("parent key for \"prefix\"", "", prefix.getChildKey("prefix"));
    }

    @Test
    public void testGetKeys() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("key1", "value1");
        conf.setProperty("key2", "value1");

        final Configuration prefix = new PrefixConfiguration(conf, "test", ".");

        final Iterator<String> it = prefix.getKeys();
        assertEquals("1st key", "test.key1", it.next());
        assertEquals("2nd key", "test.key2", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    @Test
    public void testGetKeysWithPrefix() {
        final Configuration conf = new BaseConfiguration();
        conf.setProperty("abc", "value0");
        conf.setProperty("abc.key1", "value1");

        final Configuration prefix = new PrefixConfiguration(conf, "test", ".");

        final Iterator<String> it = prefix.getKeys("test.abc");
        assertEquals("1st key", "test.abc", it.next());
        assertEquals("2nd key", "test.abc.key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    @Test
    public void testGetList() {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf.setProperty("abc", "value0,value1");
        conf.addProperty("abc", "value3");

        final Configuration prefix = new PrefixConfiguration(conf, "test", ".");
        final List<Object> list = prefix.getList("test.abc", new ArrayList<>());
        assertEquals(3, list.size());
    }

    @Test
    public void testGetChild() {
        final Configuration conf = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(conf, "prefix", ".");

        assertEquals("parent", conf, prefix.getChild());
    }

    @Test
    public void testGetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(conf, "prefix", ".");

        assertEquals("prefix", "prefix", prefix.getPrefix());
    }

    @Test
    public void testSetPrefix() {
        final Configuration conf = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(conf, null, ".");
        prefix.setPrefix("prefix");

        assertEquals("prefix", "prefix", prefix.getPrefix());
    }

    @Test
    public void testThrowExceptionOnMissing() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);

        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");

        try {
            prefix.getString("prefix.foo");
            fail("NoSuchElementException expected");
        } catch (final NoSuchElementException e) {
            // expected
        }

        config.setThrowExceptionOnMissing(false);
        assertNull(prefix.getString("prefix.foo"));

        prefix.setThrowExceptionOnMissing(true);
        try {
            config.getString("prefix.foo");
            fail("NoSuchElementException expected");
        } catch (final NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testClear() {
        final Configuration config = new BaseConfiguration();
        config.setProperty("key1", "value1");

        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");
        prefix.clear();

        assertTrue("the prefix is not empty", prefix.isEmpty());
    }

    /**
     * Tests whether a list delimiter handler is used correctly.
     */
    @Test
    public void testListDelimiterHandling() {
        final BaseConfiguration config = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");

        config.setListDelimiterHandler(new DefaultListDelimiterHandler('/'));
        prefix.addProperty("prefix.list", "a/b/c");
        assertEquals("Wrong size of list", 3, prefix.getList("prefix.list").size());

        ((AbstractConfiguration) prefix).setListDelimiterHandler(new DefaultListDelimiterHandler(';'));
        prefix.addProperty("prefix.list2", "a;b;c");
        assertEquals("Wrong size of list2", 3, prefix.getList("prefix.list2").size());
    }

    /**
     * Tests whether the list delimiter handler from the parent configuration is
     * used.
     */
    @Test
    public void testGetListDelimiterHandlerFromParent() {
        final BaseConfiguration config = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        config.setListDelimiterHandler(listHandler);
        assertSame("Not list handler from parent", listHandler, prefix.getListDelimiterHandler());
    }

    /**
     * Tests the case that the parent configuration is not derived from
     * AbstractConfiguration and thus does not support a list delimiter handler.
     */
    @Test
    public void testSetListDelimiterHandlerParentNotSupported() {
        final Configuration config = EasyMock.createNiceMock(Configuration.class);
        EasyMock.replay(config);
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        prefix.setListDelimiterHandler(listHandler);
        assertSame("List delimiter handler not set", listHandler, prefix.getListDelimiterHandler());
    }

    /**
     * Tests manipulating the interpolator.
     */
    @Test
    public void testInterpolator() {
        final BaseConfiguration config = new BaseConfiguration();
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix");
        InterpolationTestHelper.testGetInterpolator(prefix);
    }

    @Test
    public void testLocalLookupsInInterpolatorAreInherited() {
        final BaseConfiguration config = new BaseConfiguration();
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        interpolator.registerLookup("brackets", key -> "(" + key + ")");
        config.setProperty("var", "${brackets:x}");
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix", ".");
        assertEquals("Local lookup was not inherited", "(x)", prefix.getString("prefix.var", ""));
    }

    @Test
    public void testInterpolationForKeysOfTheParent() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setProperty("test", "junit");
        config.setProperty("key", "${test}");
        final PrefixConfiguration prefix = new PrefixConfiguration(config, "prefix", ".");
        assertEquals("Interpolation does not resolve parent keys", "junit", prefix.getString("prefix.key", ""));
    }
}
