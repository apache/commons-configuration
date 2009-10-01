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
package org.apache.commons.configuration2.base;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;
import org.easymock.EasyMock;

/**
 * Test class for {@code FlatNodeSourceAdapter}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestFlatNodeSourceAdapter extends TestCase
{
    /** An array with the names of the test properties. */
    private static final String[] KEYS = {
            "db.user", "db.pwd", "db.driver", "test"
    };

    /** An array with the values of the test properties. */
    private static final Object[] VALUES = {
            "scott", "elephant", "test.driver", true
    };

    /** Constant for a new property key. */
    private static final String NEW_KEY = "another.key";

    /** Constant for the value of the new property. */
    private static final Object NEW_VALUE = "new property value";

    /**
     * Creates a mock configuration source. The source is already prepared to
     * expect a registration of a change listener.
     *
     * @return the mock source
     */
    private static FlatConfigurationSource createMockSource()
    {
        FlatConfigurationSource src = EasyMock
                .createMock(FlatConfigurationSource.class);
        src
                .addConfigurationSourceListener((ConfigurationSourceListener) EasyMock
                        .anyObject());
        return src;
    }

    /**
     * Creates an adapter instance that wraps a source which was already
     * initialized with some test data.
     *
     * @return the adapter instance
     */
    private static FlatNodeSourceAdapter createTestAdapter()
    {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < KEYS.length; i++)
        {
            map.put(KEYS[i], VALUES[i]);
        }
        FlatConfigurationSource src = new ConfigurationSourceEventWrapper(
                new MapConfigurationSource(map));
        return new FlatNodeSourceAdapter(src);
    }

    /**
     * Creates a test configuration using the specified source. Because the
     * source deals with flat nodes using the dot (".") as regular property
     * character the configuration uses an expression engine with a different
     * property separator.
     *
     * @param src the source
     * @return the test configuration
     */
    private static Configuration<FlatNode> createTestConfig(
            HierarchicalConfigurationSource<FlatNode> src)
    {
        Configuration<FlatNode> config = new ConfigurationImpl<FlatNode>(src);
        DefaultExpressionEngine expr = new DefaultExpressionEngine();
        expr.setPropertyDelimiter("/");
        config.setExpressionEngine(expr);
        return config;
    }

    /**
     * Tries to create an adapter for a null source. This should cause an
     * exception.
     */
    public void testInitNull()
    {
        try
        {
            new FlatNodeSourceAdapter(null);
            fail("Could create instance without source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether the correct original source is returned.
     */
    public void testGetOriginalSource()
    {
        FlatConfigurationSource src = createMockSource();
        EasyMock.replay(src);
        FlatNodeSourceAdapter adapter = new FlatNodeSourceAdapter(src);
        assertEquals("Wrong original source", src, adapter.getOriginalSource());
        EasyMock.verify(src);
    }

    /**
     * Tests whether a listener can be added. It should be added at the wrapped
     * source.
     */
    public void testAddConfigurationSourceListener()
    {
        FlatConfigurationSource src = createMockSource();
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        src.addConfigurationSourceListener(l);
        EasyMock.replay(l, src);
        FlatNodeSourceAdapter adapter = new FlatNodeSourceAdapter(src);
        adapter.addConfigurationSourceListener(l);
        EasyMock.verify(l, src);
    }

    /**
     * Tests whether event listeners can be removed. The calls should be
     * delegated to the wrapped source.
     */
    public void testRemoveConfigurationSourceListener()
    {
        FlatConfigurationSource src = createMockSource();
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        EasyMock.expect(src.removeConfigurationSourceListener(l)).andReturn(
                Boolean.TRUE);
        EasyMock.expect(src.removeConfigurationSourceListener(l)).andReturn(
                Boolean.FALSE);
        EasyMock.replay(l, src);
        FlatNodeSourceAdapter adapter = new FlatNodeSourceAdapter(src);
        assertTrue("Wrong result for call 1", adapter
                .removeConfigurationSourceListener(l));
        assertFalse("Wrong result for call 2", adapter
                .removeConfigurationSourceListener(l));
        EasyMock.verify(l, src);
    }

    /**
     * Tests the clear() implementation.
     */
    public void testClear()
    {
        FlatConfigurationSource src = createMockSource();
        src.clear();
        EasyMock.replay(src);
        FlatNodeSourceAdapter adapter = new FlatNodeSourceAdapter(src);
        adapter.clear();
        EasyMock.verify(src);
    }

    /**
     * Tries to set a root node. This operation is not supported, so an
     * exception should be thrown.
     */
    public void testSetRootNode()
    {
        FlatConfigurationSource src = createMockSource();
        EasyMock.replay(src);
        FlatNodeSourceAdapter adapter = new FlatNodeSourceAdapter(src);
        try
        {
            adapter.setRootNode(new FlatRootNode());
            fail("Could set a new root node!");
        }
        catch (UnsupportedOperationException uex)
        {
            EasyMock.verify(src);
        }
    }

    /**
     * Tests whether the data of the wrapped configuration source is actually
     * transformed into a hierarchical structure. We test whether it can be
     * accessed from a configuration.
     */
    public void testTransformation()
    {
        FlatNodeSourceAdapter adapter = createTestAdapter();
        Configuration<FlatNode> config = createTestConfig(adapter);
        Iterator<String> it = config.getKeys();
        int idx = 0;
        while (it.hasNext())
        {
            String key = it.next();
            assertEquals("Wrong key at " + idx, KEYS[idx], key);
            assertEquals("Wrong value for " + key, VALUES[idx], config
                    .getProperty(key));
            idx++;
        }
        assertEquals("Wrong number of keys", KEYS.length, idx);
    }

    /**
     * Tests the transformation performed by the adapter if a property with
     * multiple values is involved.
     */
    public void testTransformationList()
    {
        FlatNodeSourceAdapter adapter = createTestAdapter();
        final int count = 5;
        for (int i = 0; i < count; i++)
        {
            adapter.getOriginalSource().addProperty(NEW_KEY, i);
        }
        Configuration<FlatNode> config = createTestConfig(adapter);
        List<?> values = config.getList(NEW_KEY);
        assertEquals("Wrong number of values", count, values.size());
        for (int i = 0; i < count; i++)
        {
            assertEquals("Wrong value at " + i, Integer.valueOf(i), values
                    .get(i));
        }
    }

    /**
     * Tests whether changes at the wrapped source are visible.
     */
    public void testConfigurationSourceChanged()
    {
        FlatNodeSourceAdapter adapter = createTestAdapter();
        Configuration<FlatNode> config = createTestConfig(adapter);
        FlatNode root = adapter.getRootNode();
        assertFalse("Key already found", config.containsKey(NEW_KEY));
        adapter.getOriginalSource().addProperty(NEW_KEY, NEW_VALUE);
        assertEquals("Wrong value for new property", NEW_VALUE, config
                .getProperty(NEW_KEY));
        assertNotSame("Root node not changed", root, adapter.getRootNode());
    }

    /**
     * Tests whether updates of the configuration are visible in the wrapped
     * source.
     */
    public void testUpdateOriginalSource()
    {
        FlatNodeSourceAdapter adapter = createTestAdapter();
        Configuration<FlatNode> config = createTestConfig(adapter);
        FlatNode root = adapter.getRootNode();
        config.addProperty(NEW_KEY, NEW_VALUE);
        assertEquals("Root node was changed", root, adapter.getRootNode());
        assertEquals("Property not found in source", NEW_VALUE, adapter
                .getOriginalSource().getProperty(NEW_KEY));
    }
}
