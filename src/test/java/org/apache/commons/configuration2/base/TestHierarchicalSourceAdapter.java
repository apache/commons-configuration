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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.lang3.mutable.MutableObject;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test class for {@code HierarchicalSourceAdapter}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestHierarchicalSourceAdapter extends TestCase
{
    /** An array with the names of the test properties. */
    private static final String[] KEYS = {
            "db.user", "db.pwd", "db.driver", "gui.color.fg", "gui.color.bg",
            "gui.width", "gui.height", "test"
    };

    /** An array with the values of the test properties. */
    private static final Object[] VALUES = {
            "scott", "elephant", "test.driver", "pink", "black", 320, 200, true
    };

    /** Constant for the key of a new property. */
    private static final String NEW_KEY = "test2";

    /** Constant for the value of the new property. */
    private static final Object NEW_VALUE = Boolean.TRUE;

    /**
     * Helper method for creating a mock for a hierarchical source.
     *
     * @return the mock
     */
    private static HierarchicalConfigurationSource<ConfigurationNode> createHierarchicalSourceMock()
    {
        @SuppressWarnings("unchecked")
        HierarchicalConfigurationSource<ConfigurationNode> mock = EasyMock
                .createMock(HierarchicalConfigurationSource.class);
        return mock;
    }

    /**
     * Helper method for creating a mock for a configuration.
     *
     * @return the mock
     */
    private static Configuration<ConfigurationNode> createConfigurationMock()
    {
        @SuppressWarnings("unchecked")
        Configuration<ConfigurationNode> mock = EasyMock
                .createMock(Configuration.class);
        return mock;
    }

    /**
     * Prepares a mock source object to be queried for its properties for a
     * transformation. This method prepares the mock to expect a number of
     * getProperty() calls for all test properties.
     *
     * @param src the mock for the source
     */
    private static void prepareTransformPropertyValues(FlatConfigurationSource src)
    {
        for (int i = 0; i < KEYS.length; i++)
        {
            EasyMock.expect(src.getProperty(KEYS[i])).andReturn(VALUES[i]);
        }
    }

    /**
     * Prepares a mock source object for a writeBack() operation. The mock is
     * initialized to expect addProperty() for all test properties and the new
     * property.
     *
     * @param src the mock for the source
     * @param clear a flag whether the clear() call should be expected, too
     */
    private static void prepareWriteBack(FlatConfigurationSource src, boolean clear)
    {
        if (clear)
        {
            src.clear();
        }
        for (int i = 0; i < KEYS.length; i++)
        {
            src.addProperty(KEYS[i], VALUES[i]);
        }
        src.addProperty(NEW_KEY, NEW_VALUE);
    }

    /**
     * Tries to create an instance without a wrapped source. This should cause
     * an exception.
     */
    public void testInitNoWrappedSource()
    {
        try
        {
            new HierarchicalSourceAdapter(null);
            fail("Could create instance with null wrapped source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the constructor that takes only the wrapped source.
     */
    public void testInit()
    {
        FlatConfigurationSource src = EasyMock
                .createMock(FlatConfigurationSource.class);
        EasyMock.replay(src);
        HierarchicalSourceAdapter adapter = new HierarchicalSourceAdapter(src);
        assertEquals("Wrong wrapped source", src, adapter.getOriginalSource());
        assertFalse("Wrong monitor flag", adapter.isMonitorChanges());
        EasyMock.verify(src);
    }

    /**
     * Tests the constructor that enables monitoring changes.
     */
    public void testInitMonitor()
    {
        FlatConfigurationSource src = EasyMock
                .createMock(FlatConfigurationSource.class);
        EasyMock.replay(src);
        final MutableObject listener = new MutableObject();
        FlatConfigurationSource orgSrc = new ConfigurationSourceEventWrapper(src)
        {
            @Override
            public void addConfigurationSourceListener(
                    ConfigurationSourceListener l)
            {
                listener.setValue(l);
            }
        };
        HierarchicalSourceAdapter adapter = new HierarchicalSourceAdapter(
                orgSrc, true);
        assertTrue("Wrong monitor flag", adapter.isMonitorChanges());
        assertEquals("Listener not registered", adapter, listener.getValue());
    }

    /**
     * Tests the clear() implementation.
     */
    public void testClear()
    {
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        adapter.mockSource = createHierarchicalSourceMock();
        adapter.mockSource.clear();
        EasyMock.replay(adapter.mockSource, adapter.getOriginalSource());
        adapter.clear();
        EasyMock.verify(adapter.mockSource, adapter.getOriginalSource());
    }

    /**
     * Tests whether configuration source listener can be registered at the
     * transformed source.
     */
    public void testAddConfigurationSourceListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        HierarchicalConfigurationSource<ConfigurationNode> src = createHierarchicalSourceMock();
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        src.addConfigurationSourceListener(l);
        EasyMock.replay(l, src);
        adapter.mockSource = src;
        adapter.addConfigurationSourceListener(l);
        EasyMock.verify(l, src);
    }

    /**
     * Tests whether a configuration source listener can be removed from the
     * transformed source.
     */
    public void testRemoveConfigurationSourceListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createMock(ConfigurationSourceListener.class);
        HierarchicalConfigurationSource<ConfigurationNode> src = createHierarchicalSourceMock();
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        EasyMock.expect(src.removeConfigurationSourceListener(l)).andReturn(
                Boolean.TRUE);
        EasyMock.replay(l, src);
        adapter.mockSource = src;
        assertTrue("Wrong result", adapter.removeConfigurationSourceListener(l));
        EasyMock.verify(l, src);
    }

    /**
     * Tests whether the correct root node is returned.
     */
    public void testGetRootNode()
    {
        HierarchicalConfigurationSource<ConfigurationNode> src = createHierarchicalSourceMock();
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        ConfigurationNode root = EasyMock.createMock(ConfigurationNode.class);
        EasyMock.expect(src.getRootNode()).andReturn(root);
        EasyMock.replay(src, root, adapter.getOriginalSource());
        adapter.mockSource = src;
        assertEquals("Wrong root node", root, adapter.getRootNode());
        EasyMock.verify(src, root, adapter.getOriginalSource());
    }

    /**
     * Tests whether a new root node can be set.
     */
    public void testSetRootNode()
    {
        HierarchicalConfigurationSource<ConfigurationNode> src = createHierarchicalSourceMock();
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        ConfigurationNode root = EasyMock.createMock(ConfigurationNode.class);
        src.setRootNode(root);
        EasyMock.replay(src, root, adapter.getOriginalSource());
        adapter.mockSource = src;
        adapter.setRootNode(root);
        EasyMock.verify(src, root, adapter.getOriginalSource());
    }

    /**
     * Tests whether the correct node handler is returned.
     */
    public void testGetNodeHandler()
    {
        HierarchicalConfigurationSource<ConfigurationNode> src = createHierarchicalSourceMock();
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        NodeHandler<ConfigurationNode> handler = new ConfigurationNodeHandler();
        EasyMock.expect(src.getNodeHandler()).andReturn(handler);
        EasyMock.replay(src, adapter.getOriginalSource());
        adapter.mockSource = src;
        assertEquals("Wrong node handler", handler, adapter.getNodeHandler());
        EasyMock.verify(src, adapter.getOriginalSource());
    }

    /**
     * Tests whether the given configuration contains the expected data.
     *
     * @param config the configuration to check
     */
    private void checkTransformedConfiguration(
            Configuration<ConfigurationNode> config)
    {
        Set<String> keySet = new HashSet<String>(Arrays.asList(KEYS));
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            String key = it.next();
            assertTrue("Unexpected key: " + key, keySet.remove(key));
        }
        assertTrue("Remaining keys: " + keySet, keySet.isEmpty());
        for (int i = 0; i < KEYS.length; i++)
        {
            assertEquals("Wrong value for " + KEYS[i], VALUES[i], config
                    .getProperty(KEYS[i]));
        }
        Configuration<ConfigurationNode> sub = config.configurationAt("gui");
        assertEquals("Wrong sub property 1", 320, sub.getInt("width"));
        assertEquals("Wrong sub property 2", "black", sub.getString("color.bg"));
    }

    /**
     * Tests whether the plain configuration source is transformed into a
     * hierarchical one.
     */
    public void testTransformation()
    {
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        prepareTransformPropertyValues(adapter.getOriginalSource());
        EasyMock.expect(adapter.getOriginalSource().getKeys()).andReturn(
                Arrays.asList(KEYS).iterator());
        EasyMock.replay(adapter.getOriginalSource());
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                adapter);
        checkTransformedConfiguration(config);
        EasyMock.verify(adapter.getOriginalSource());
    }

    /**
     * Tests whether changes of the original source are monitored and whether
     * the transformed source is re-constructed if a change is noticed.
     */
    public void testTransformationMonitor()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        source
                .addConfigurationSourceListener((ConfigurationSourceListener) EasyMock
                        .anyObject());
        prepareTransformPropertyValues(source);
        EasyMock.expect(source.getKeys()).andReturn(
                Arrays.asList(KEYS).iterator());
        prepareTransformPropertyValues(source);
        EasyMock.expect(source.getProperty(NEW_KEY)).andReturn(NEW_VALUE);
        List<String> keyList = new ArrayList<String>(Arrays.asList(KEYS));
        keyList.add(NEW_KEY);
        EasyMock.expect(source.getKeys()).andReturn(keyList.iterator());
        EasyMock.replay(source);
        HierarchicalSourceAdapter adapter = new HierarchicalSourceAdapter(
                source, true);
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                adapter);
        assertFalse("New key already found", config.containsKey(NEW_KEY));
        // simulate a change event
        adapter.configurationSourceChanged(new ConfigurationSourceEvent(source,
                ConfigurationSourceEvent.Type.ADD_PROPERTY, NEW_KEY, NEW_VALUE,
                null, true));
        adapter.configurationSourceChanged(new ConfigurationSourceEvent(source,
                ConfigurationSourceEvent.Type.ADD_PROPERTY, NEW_KEY, NEW_VALUE,
                null, false));
        assertEquals("Wrong value of new property", NEW_VALUE, config
                .getProperty(NEW_KEY));
        assertEquals("Wrong value of old property", 320, config
                .getInt("gui.width"));
        EasyMock.verify(source);
    }

    /**
     * Tests whether data stored in the adapter can be written back into the
     * original source.
     */
    public void testWriteBack()
    {
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        EasyMock.expect(adapter.getOriginalSource().getKeys()).andReturn(
                Arrays.asList(KEYS).iterator());
        prepareTransformPropertyValues(adapter.getOriginalSource());
        prepareWriteBack(adapter.getOriginalSource(), true);
        EasyMock.replay(adapter.getOriginalSource());
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                adapter);
        assertFalse("Configuration is empty", config.isEmpty());
        config.addProperty(NEW_KEY, NEW_VALUE);
        adapter.writeBack();
        EasyMock.verify(adapter.getOriginalSource());
    }

    /**
     * Tests the writeBack() method if monitoring of the original source is
     * enabled. In this case the adapter should de-register itself before it
     * changes the original source.
     */
    public void testWriteBackMonitor()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        final MutableObject expectedListener = new MutableObject();
        source
                .addConfigurationSourceListener((ConfigurationSourceListener) EasyMock
                        .anyObject());
        EasyMock.expect(source.getKeys()).andReturn(
                Arrays.asList(KEYS).iterator());
        prepareTransformPropertyValues(source);
        EasyMock
                .expect(
                        source
                                .removeConfigurationSourceListener((ConfigurationSourceListener) EasyMock
                                        .anyObject())).andAnswer(
                        new IAnswer<Boolean>()
                        {
                            /* Check whether the expected listener is passed in.*/
                            public Boolean answer() throws Throwable
                            {
                                assertEquals("Wrong event listener to remove",
                                        expectedListener.getValue(), EasyMock
                                                .getCurrentArguments()[0]);
                                return Boolean.TRUE;
                            }
                        });
        prepareWriteBack(source, true);
        source
                .addConfigurationSourceListener((ConfigurationSourceListener) EasyMock
                        .anyObject());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            /* Check whether the expected listener is passed in.*/
            public Object answer() throws Throwable
            {
                assertEquals("Wrong event listener to add", expectedListener
                        .getValue(), EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });
        EasyMock.replay(source);
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl(
                source, true);
        expectedListener.setValue(adapter);
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                adapter);
        assertFalse("Configuration is empty", config.isEmpty());
        config.addProperty(NEW_KEY, NEW_VALUE);
        adapter.writeBack();
        EasyMock.verify(source);
    }

    /**
     * Tests the utility method for copying a source into a configuration.
     */
    public void testFillConfiguration()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        EasyMock.expect(source.getKeys()).andReturn(
                Arrays.asList(KEYS).iterator());
        prepareTransformPropertyValues(source);
        EasyMock.replay(source);
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                new InMemoryConfigurationSource());
        HierarchicalSourceAdapter.fillConfiguration(config, source);
        checkTransformedConfiguration(config);
        EasyMock.verify(source);
    }

    /**
     * Tries to call fillConfiguration() with a null configuration. This should
     * cause an exception.
     */
    public void testFillConfigurationNullConfig()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        EasyMock.replay(source);
        try
        {
            HierarchicalSourceAdapter.fillConfiguration(null, source);
            fail("Null configuration not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            EasyMock.verify(source);
        }
    }

    /**
     * Tries to call fillConfiguration() with a null source. This should cause
     * an exception.
     */
    public void testFillConfigurationNullSource()
    {
        Configuration<ConfigurationNode> config = createConfigurationMock();
        EasyMock.replay(config);
        try
        {
            HierarchicalSourceAdapter.fillConfiguration(config, null);
            fail("Null source not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            EasyMock.verify(config);
        }
    }

    /**
     * Tests the utility method for copying a configuration into a source.
     */
    public void testFillSource()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        prepareWriteBack(source, false);
        EasyMock.replay(source);
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(
                new InMemoryConfigurationSource());
        for (int i = 0; i < KEYS.length; i++)
        {
            config.addProperty(KEYS[i], VALUES[i]);
        }
        config.addProperty(NEW_KEY, NEW_VALUE);
        HierarchicalSourceAdapter.fillSource(source, config);
        EasyMock.verify(source);
    }

    /**
     * Tries to call fillSource() with a null configuration. This should cause
     * an exception.
     */
    public void testFillSourceNullConfig()
    {
        FlatConfigurationSource source = EasyMock
                .createMock(FlatConfigurationSource.class);
        EasyMock.replay(source);
        try
        {
            HierarchicalSourceAdapter.fillSource(source, null);
            fail("Null configuration not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            EasyMock.verify(source);
        }
    }

    /**
     * Tries to call fillSource() with a null source. This should cause an
     * exception.
     */
    public void testFillSourceNullSource()
    {
        Configuration<ConfigurationNode> config = createConfigurationMock();
        EasyMock.replay(config);
        try
        {
            HierarchicalSourceAdapter.fillSource(null, config);
            fail("Null source not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            EasyMock.verify(config);
        }
    }

    /**
     * Tests the implementation of getCapability().
     */
    public void testGetCapability()
    {
        HierarchicalSourceAdapterTestImpl adapter = new HierarchicalSourceAdapterTestImpl();
        final Object cap = new Object();
        EasyMock
                .expect(adapter.getOriginalSource().getCapability(Object.class))
                .andReturn(cap);
        EasyMock.replay(adapter.getOriginalSource());
        assertEquals("Wrong capability", cap, adapter
                .getCapability(Object.class));
        EasyMock.verify(adapter.getOriginalSource());
    }

    /**
     * A specialized implementation of {@code HierarchicalSourceAdapter} that
     * overrides some methods to inject mock objects.
     */
    private static class HierarchicalSourceAdapterTestImpl extends
            HierarchicalSourceAdapter
    {
        /** The mock transformed source. */
        HierarchicalConfigurationSource<ConfigurationNode> mockSource;

        public HierarchicalSourceAdapterTestImpl(
                FlatConfigurationSource wrappedSource, boolean monitorChanges)
        {
            super(wrappedSource, monitorChanges);
        }

        /**
         * Creates a new instance of {@code HierarchicalSourceAdapterTestImpl}
         * and sets a default mock object for the wrapped source.
         */
        public HierarchicalSourceAdapterTestImpl()
        {
            super(EasyMock.createMock(FlatConfigurationSource.class));
        }

        /**
         * Either returns the mock source or calls the super method.
         */
        @Override
        protected HierarchicalConfigurationSource<ConfigurationNode> getTransformedSource()
        {
            return (mockSource != null) ? mockSource : super
                    .getTransformedSource();
        }
    }
}
