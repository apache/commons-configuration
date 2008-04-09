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
package org.apache.commons.configuration2.flat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;

import junit.framework.TestCase;

/**
 * A test class for the basic functionality of flat configuration classes.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestAbstractFlatConfiguration extends TestCase
{
    /** An array with the keys of the test configuration. */
    private static final String[] KEYS = {
            "prop1", "prop2", "prop3", "testProp", "anotherProp",
            "someMoreProp"
    };

    /** An array with the values of the test properties. */
    private static final Object[] VALUES = {
            "value1", new Object[] {
                    1, 3, 4
            }, "value3", new Object[] {
                    "yes", "no", "perhaps"
            }, "anotherValue", "moreValues"
    };

    /** The configuration to be tested. */
    private FlatConfigurationMockImpl config;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        config = new FlatConfigurationMockImpl();
    }

    /**
     * Prepares the test configuration to expect initialization steps for a
     * number of getRootNode() invocations. This method adds the corresponding
     * expectations for method calls.
     *
     * @param count the number of expected getRootNode() invocations (that
     *        require a re-creation of the node structure)
     */
    private void prepareGetRootNode(int count)
    {
        prepareGetRootNode(config, count);
    }

    /**
     * Prepares the specified mock configuration to expect initialization steps for a
     * number of getRootNode() invocations. This method adds the corresponding
     * expectations for method calls.
     *
     * @param config the configuration to initialize
     * @param count the number of expected getRootNode() invocations (that
     *        require a re-creation of the node structure)
     */
    private void prepareGetRootNode(FlatConfigurationMockImpl config, int count)
    {
        config.keyList = Arrays.asList(KEYS);
        for (int cnt = 0; cnt < count; cnt++)
        {
            for (int i = 0; i < KEYS.length; i++)
            {
                Object value = VALUES[i];
                int maxIndex = 0;
                if (value instanceof Object[])
                {
                    Object[] values = (Object[]) value;
                    maxIndex = values.length - 1;
                    value = Arrays.asList(values);
                    for (int j = 0; j < values.length; j++)
                    {
                        config.expectGetProperty(KEYS[i], value);
                    }
                }
                else
                {
                    config.expectGetProperty(KEYS[i], value);
                }
                config.expectGetMaxIndex(KEYS[i], maxIndex);
            }
        }
    }

    /**
     * Checks whether the node structure contains the test data.
     *
     * @param root the root node
     */
    private void checkNodeStructure(FlatNode root)
    {
        List<FlatNode> children = root.getChildren();
        Iterator<FlatNode> it = children.iterator();
        int index = 0;
        while (it.hasNext())
        {
            FlatNode nd = it.next();
            Object value = VALUES[index];
            if (value instanceof Object[])
            {
                Object[] values = (Object[]) value;
                for (int i = 0; i < values.length; i++)
                {
                    assertEquals("Wrong node name", KEYS[index], nd.getName());
                    assertEquals("Wrong value index", i, nd.getValueIndex());
                    assertEquals("Wrong value", values[i], config
                            .getNodeHandler().getValue(nd));
                    if (i < values.length - 1)
                    {
                        nd = it.next();
                    }
                }
            }
            else
            {
                assertEquals("Wrong node name", KEYS[index], nd.getName());
                assertEquals("Wrong value index", FlatNode.INDEX_UNDEFINED, nd
                        .getValueIndex());
                assertEquals("Wrong value", value, config.getNodeHandler()
                        .getValue(nd));
            }
            index++;
        }
    }

    /**
     * Tests querying the node handler of the flat configuration.
     */
    public void testGetNodeHandler()
    {
        FlatNodeHandler handler = (FlatNodeHandler) config.getNodeHandler();
        assertSame("Handler not associated with configuration", config, handler
                .getConfiguration());
    }

    /**
     * Tests querying the root node of a flat configuration.
     */
    public void testGetRootNode()
    {
        prepareGetRootNode(1);
        FlatNode root = config.getRootNode();
        checkNodeStructure(root);
    }

    /**
     * Tests an external update of the configuration. This should cause the node
     * structure to be invalidated.
     */
    public void testGetRootNodeExternalUpdate()
    {
        prepareGetRootNode(2);
        FlatNode root = config.getRootNode();
        config.expectAdd = true;
        config.addProperty(FlatConfigurationMockImpl.NAME, "yea!");
        FlatNode root2 = config.getRootNode();
        assertNotSame("Node structure not re-creacted", root, root2);
        checkNodeStructure(root2);
    }

    /**
     * Tests an internal update of the node structure. This should not cause the
     * node structure to be re-created.
     */
    public void testGetRootNodeInternalUpdate()
    {
        prepareGetRootNode(1);
        FlatNode root = config.getRootNode();
        int childCount = root.getChildrenCount(null);
        config.expectAdd = true;
        FlatNode child = root.addChild(FlatConfigurationMockImpl.NAME);
        config.getNodeHandler().setValue(child, "yea!");
        FlatNode root2 = config.getRootNode();
        assertSame("Node structure was re-created", root, root2);
        assertEquals("Child not added", childCount + 1, root
                .getChildrenCount(null));
    }

    /**
     * Tests whether setPropertyValue() sends the expected events.
     */
    public void testSetPropertyValueEvents()
    {
        ConfigPropertyChangeListener l = new ConfigPropertyChangeListener();
        config.addConfigurationListener(l);
        config.setPropertyValue(FlatConfigurationMockImpl.NAME, 0, "newValue");
        l.verify();
    }

    /**
     * Tests whether clearPropertyValue() sends the expected events.
     */
    public void testClearPropertyValueEvents()
    {
        ConfigPropertyChangeListener l = new ConfigPropertyChangeListener();
        config.addConfigurationListener(l);
        config.clearPropertyValue(FlatConfigurationMockImpl.NAME, 0);
        l.verify();
    }

    /**
     * Tests cloning a flat configuration. We have to check whether the event
     * listeners are correctly registered.
     */
    public void testClone() throws CloneNotSupportedException
    {
        prepareGetRootNode(1);
        checkNodeStructure(config.getRootNode());
        FlatConfigurationMockImpl copy = (FlatConfigurationMockImpl) config.clone();
        FlatNodeHandler handler = (FlatNodeHandler) copy.getNodeHandler();
        assertSame("Wrong config for node handler", copy, handler.getConfiguration());
        assertNotSame("Node handler not copied", handler, config.getNodeHandler());
        prepareGetRootNode(copy, 2);
        FlatNode root = copy.getRootNode();
        checkNodeStructure(root);
        copy.clearPropertyValue(FlatConfigurationMockImpl.NAME, 0);
        assertNotSame("Structure was not re-created", root, copy.getRootNode());
    }

    /**
     * A test event listener implementation for testing the events received for
     * a manipulation of a property value.
     */
    private class ConfigPropertyChangeListener implements ConfigurationListener
    {
        /** The number of received before events. */
        private int beforeCount;

        /** The number of received after events. */
        private int afterCount;

        public void configurationChanged(ConfigurationEvent event)
        {
            assertEquals("Wrong event type",
                    AbstractFlatConfiguration.EVENT_PROPERTY_CHANGED, event
                            .getType());
            assertEquals("Wrong event source", config, event.getSource());
            assertEquals("Wrong property name", FlatConfigurationMockImpl.NAME,
                    event.getPropertyName());
            assertNull("Value not null", event.getPropertyValue());
            if (event.isBeforeUpdate())
            {
                beforeCount++;
            }
            else
            {
                afterCount++;
            }
        }

        /**
         * Tests whether the expected events were received.
         */
        public void verify()
        {
            assertEquals("Wrong number of before events", 1, beforeCount);
            assertEquals("Wrong number of after events", 1, afterCount);
        }
    }
}
