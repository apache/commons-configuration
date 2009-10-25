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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the FlatNode classes.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestFlatNodes
{
    /** Constant for the name of the test node. */
    private static final String NAME = "testFlatNode";

    /** Constant for a test value. */
    static final Object VALUE = 42;

    /** A mock object for the owning configuration. */
    private Configuration config;

    /** The parent node. */
    private FlatRootNode parent;

    /** The node to be tested. */
    private FlatLeafNode node;

    @Before
    public void setUp() throws Exception
    {
        config = EasyMock.createMock(Configuration.class);
        parent = new FlatRootNode(config);
        node = (FlatLeafNode) parent.addChild(NAME);
    }

    /**
     * Tests whether the root node returns the correct configuration.
     */
    @Test
    public void testGetConfigurationRoot()
    {
        assertEquals("Wrong configuration", config, parent.getConfiguration());
    }

    /**
     * Tests whether the leaf node returns the correct configuration.
     */
    @Test
    public void testGetConfigurationLeaf()
    {
        assertEquals("Wrong configuration", config, node.getConfiguration());
    }

    /**
     * Tests querying the name of a leaf node.
     */
    @Test
    public void testGetNameLeaf()
    {
        assertEquals("Wrong node name", NAME, node.getName());
    }

    /**
     * Tests querying the parent node.
     */
    @Test
    public void testGetParent()
    {
        assertSame("Wrong parent node", parent, node.getParent());
    }

    /**
     * Tests querying a leaf node's children. A leaf node has no children, so
     * result should always be an empty list.
     */
    @Test
    public void testGetChildrenLeaf()
    {
        assertTrue("Children not empty", node.getChildren().isEmpty());
    }

    /**
     * Tests querying a leaf's child nodes with a specific name. Because a leaf
     * node cannot have any children result should always be an empty list.
     */
    @Test
    public void testGetChildrenNameLeaf()
    {
        assertTrue("Named children not empty", node.getChildren("test")
                .isEmpty());
    }

    /**
     * Tests querying the number of children of a leaf node. Result should
     * always be 0.
     */
    @Test
    public void testGetChildrenCountLeaf()
    {
        assertEquals("Wrong number of total children", 0, node
                .getChildrenCount(null));
        assertEquals("Wrong number of named children", 0, node
                .getChildrenCount("test"));
    }

    /**
     * Tests the getChild() method on a leaf. Because leafs have no children,
     * this method will always throw an exception.
     */
    @Test
    public void testGetChildLeaf()
    {
        for (int i = 0; i < 10; i++)
        {
            try
            {
                node.getChild(i);
                fail("Could access child with index " + i);
            }
            catch (IndexOutOfBoundsException iobex)
            {
                // ok
            }
        }
    }

    /**
     * Tests querying the index of a child node if this node is the only child
     * with this name.
     */
    @Test
    public void testGetValueIndexLeafSingle()
    {
        assertEquals("Wrong index for single child node",
                FlatNode.INDEX_UNDEFINED, node.getValueIndex());
    }

    /**
     * Tests querying the indices of child nodes.
     */
    @Test
    public void testGetValueIndexLeafMulti()
    {
        FlatNode c1 = parent.addChild(NAME);
        FlatNode c2 = parent.addChild("anotherChild");
        assertEquals("Wrong index for child 1", 0, node.getValueIndex());
        assertEquals("Wrong index for child 2", 1, c1.getValueIndex());
        assertEquals("Wrong index for other child", FlatNode.INDEX_UNDEFINED,
                c2.getValueIndex());
    }

    /**
     * Tests querying a simple value.
     */
    @Test
    public void testGetValueSimple()
    {
        EasyMock.expect(config.getProperty(NAME)).andReturn(VALUE);
        EasyMock.replay(config);
        assertEquals("Wrong property value", VALUE, node.getValue());
        EasyMock.verify(config);
    }

    /**
     * Tests the getValue() method when the value is a collection, but no index
     * is specified. In this case the whole collection should be returned.
     */
    @Test
    public void testGetValueCollectionNoIndex()
    {
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        EasyMock.replay(config);
        assertSame("Wrong value collection", values, node.getValue());
        EasyMock.verify(config);
    }

    /**
     * Tests the getValue() method when multiple values are involved.
     */
    @Test
    public void testGetValueCollection()
    {
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values).times(2);
        EasyMock.replay(config);
        FlatNode c2 = parent.addChild(NAME);
        assertEquals("Wrong value index 1", VALUE, node.getValue());
        assertEquals("Wrong value index 2", 2, c2.getValue());
        EasyMock.verify(config);
    }

    /**
     * Tests the getValue() method when multiple values are involved, but the
     * index is out of range. In this case null should be returned.
     */
    @Test
    public void testGetValueCollectionInvalidIndex()
    {
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        EasyMock.replay(config);
        parent.addChild(NAME);
        FlatNode c2 = parent.addChild(NAME);
        assertNull("Found value for invalid index", c2.getValue());
        EasyMock.verify(config);
    }

    /**
     * Tests the setValue() method for a new value.
     */
    @Test
    public void testSetValueNew()
    {
        config.addProperty(NAME, VALUE);
        EasyMock.replay(config);
        node.setValue(VALUE);
        EasyMock.verify(config);
    }

    /**
     * Tests the setValue() method for an existing value.
     */
    @Test
    public void testSetValueExisting()
    {
        final String property = NAME + "_new";
        config.setProperty(property, VALUE);
        EasyMock.replay(config);
        FlatNode child = parent.addChild(property, true);
        child.setValue(VALUE);
        EasyMock.verify(config);
    }

    /**
     * Tests setting a value for a property with multiple values.
     */
    @Test
    public void testSetValueCollection()
    {
        Collection<Object> values = new ArrayList<Object>();
        values.add(1);
        values.add(2);
        Collection<Object> newValues = new ArrayList<Object>();
        newValues.add(1);
        newValues.add(VALUE);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        config.setProperty(NAME, newValues);
        EasyMock.replay(config);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue(VALUE);
        EasyMock.verify(config);
    }

    /**
     * Tests the modification of a property with multiple values if an invalid
     * value index is involved. This case should not happen normally. No
     * modification should be performed.
     */
    @Test
    public void testSetValueCollectionInvalidIndex()
    {
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        EasyMock.replay(config);
        parent.addChild(NAME, true);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue("new");
        EasyMock.verify(config);
    }

    /**
     * Tests the modification of a property with multiple values if the
     * configuration does not return a collection. This should normally not
     * happen. In this case no modification should be performed.
     */
    @Test
    public void testSetValueCollectionInvalidValue()
    {
        EasyMock.expect(config.getProperty(NAME)).andReturn(VALUE);
        EasyMock.replay(config);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue("new");
        EasyMock.verify(config);
    }

    /**
     * Tests calling setValue() twice. The first time, the value should be
     * added. The second time it should be overridden.
     */
    @Test
    public void testSetValueNewAndExisting()
    {
        config.addProperty(NAME, VALUE);
        config.setProperty(NAME, "new");
        EasyMock.replay(config);
        node.setValue(VALUE);
        node.setValue("new");
        EasyMock.verify(config);
    }

    /**
     * Tests removing a child node. The associated configuration should also be
     * updated,
     */
    @Test
    public void testRemoveChild()
    {
        config.clearProperty(NAME);
        EasyMock.replay(config);
        parent.removeChild(node);
        EasyMock.verify(config);
    }

    /**
     * Tests removing a child node for a property with multiple values.
     */
    @Test
    public void testRemoveChildCollection()
    {
        List<Object> values = Arrays.asList(new Object[] { 1, 2, 3 });
        List<Object> newValues = new ArrayList<Object>(values);
        newValues.remove(1);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        config.setProperty(NAME, newValues);
        EasyMock.replay(config);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.addChild(NAME, true);
        parent.removeChild(n2);
        EasyMock.verify(config);
    }

    /**
     * Tests the behavior of removeChild() if after the operation only a single
     * collection element remains.
     */
    @Test
    public void testRemoveChildCollectionSingleElement()
    {
        List<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        List<Object> newValues = new ArrayList<Object>();
        newValues.add(VALUE);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        config.setProperty(NAME, newValues);
        EasyMock.replay(config);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(n2);
        EasyMock.verify(config);
    }

    /**
     * Tests removeChild() if the child has an invalid index. This should
     * normally not happen. In this case no modification should be performed.
     */
    @Test
    public void testRemoveChildCollectionInvalidIndex()
    {
        List<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        EasyMock.expect(config.getProperty(NAME)).andReturn(values);
        EasyMock.replay(config);
        parent.addChild(NAME, true);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(n2);
        EasyMock.verify(config);
    }

    /**
     * Tests removeChild() for a property with multiple values if the
     * configuration does not return a collection. This should normally not
     * happen. In this case no modification should be performed.
     */
    @Test
    public void testRemoveChildCollectionInvalidValue()
    {
        EasyMock.expect(config.getProperty(NAME)).andReturn(VALUE);
        EasyMock.replay(config);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(n2);
        EasyMock.verify(config);
    }

    /**
     * Tries to remove a child node that does not belong to the parent. This
     * should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testRemoveChildWrongParent()
    {
        FlatLeafNode child = new FlatLeafNode(null, "test", true);
        parent.removeChild(child);
    }

    /**
     * Tries to remove a null child node. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testRemoveChildNull()
    {
        parent.removeChild(null);
    }

    /**
     * Tests corner cases when adding and removing child nodes.
     */
    @Test
    public void testAddAndRemoveChild()
    {
        final int count = 5;
        BaseConfiguration config = new BaseConfiguration();
        parent = new FlatRootNode(config);
        node = (FlatLeafNode) parent.addChild(NAME);
        List<FlatNode> nodes = new ArrayList<FlatNode>(count);
        nodes.add(node);
        for (int i = 0; i < count; i++)
        {
            config.addProperty(NAME, i);
            if (i > 0)
            {
                nodes.add(parent.addChild(NAME, true));
            }
        }
        for (int i = 0; i < count; i++)
        {
            assertEquals("Wrong value", Integer.valueOf(i), nodes.get(i)
                    .getValue());
        }
        for (int j = count - 1; j > 0; j--)
        {
            parent.removeChild(nodes.get(j));
            List<FlatNode> remainingChildren = parent.getChildren(NAME);
            assertEquals("Wrong children", nodes.subList(0, j),
                    remainingChildren);
        }
        assertEquals("Wrong remaining value", Integer.valueOf(0), config
                .getProperty(NAME));
        parent.removeChild(nodes.get(0));
        assertFalse("Property still found", config.containsKey(NAME));
        assertEquals("Wrong number of children", 0, parent
                .getChildrenCount(NAME));
    }

    /**
     * Tests adding a child node to a leaf. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testAddChildLeaf()
    {
        node.addChild(NAME);
    }

    /**
     * Tests removing a child from a leaf. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testRemoveChildLeaf()
    {
        node.removeChild(parent);
    }

    /**
     * Tests the getChildren() method of a root node.
     */
    @Test
    public void testGetChildren()
    {
        FlatNode c1 = parent.addChild("child1");
        FlatNode c2 = parent.addChild(NAME);
        List<FlatNode> children = parent.getChildren();
        assertEquals("Wrong number of children", 3, children.size());
        assertEquals("Wrong child 1", node, children.get(0));
        assertEquals("Wrong child 2", c1, children.get(1));
        assertEquals("Wrong child 3", c2, children.get(2));
    }

    /**
     * Tests querying child nodes by their name.
     */
    @Test
    public void testGetChildrenName()
    {
        FlatNode c1 = parent.addChild("child1");
        FlatNode c2 = parent.addChild(NAME);
        List<FlatNode> children = parent.getChildren(NAME);
        assertEquals("Wrong number of children", 2, children.size());
        assertEquals("Wrong child 1", node, children.get(0));
        assertEquals("Wrong child 2", c2, children.get(1));
        children = parent.getChildren("child1");
        assertEquals("Wrong number of children 2", 1, children.size());
        assertEquals("Wrong child", c1, children.get(0));
    }

    /**
     * Tests querying child nodes with a non existing name.
     */
    @Test
    public void testGetChildrenNameUnknown()
    {
        List<FlatNode> children = parent.getChildren("unknownName");
        assertTrue("Found unknown children", children.isEmpty());
    }

    /**
     * Tests accessing child nodes by its index.
     */
    @Test
    public void testGetChild()
    {
        FlatNode c1 = parent.addChild("child1");
        FlatNode c2 = parent.addChild(NAME);
        assertEquals("Wrong child 1", node, parent.getChild(0));
        assertEquals("Wrong child 2", c1, parent.getChild(1));
        assertEquals("Wrong child 3", c2, parent.getChild(2));
    }

    /**
     * Tests querying the number of child nodes.
     */
    @Test
    public void testGetChildrenCount()
    {
        parent.addChild("child1");
        parent.addChild(NAME);
        assertEquals("Wrong number of children 1", 2, parent
                .getChildrenCount(NAME));
        assertEquals("Wrong number of children 2", 1, parent
                .getChildrenCount("child1"));
    }

    /**
     * Tests querying the total number of child nodes.
     */
    @Test
    public void testGetChildrenCountTotal()
    {
        parent.addChild("child1");
        parent.addChild(NAME);
        assertEquals("Wrong number of child nodes", 3, parent
                .getChildrenCount(null));
    }

    /**
     * Tests querying the number of non existing children.
     */
    @Test
    public void testGetChildrenCountUnknown()
    {
        assertEquals("Found unknown children", 0, parent
                .getChildrenCount("unknownName"));
    }

    /**
     * Tests querying the name of the root node. The answer should be null.
     */
    @Test
    public void testGetNameRoot()
    {
        assertNull("Wrong root name", parent.getName());
    }

    /**
     * Tests querying the parent of the root node. Of course, this node has no
     * parent.
     */
    @Test
    public void testGetParentRoot()
    {
        assertNull("Root node has a parent", parent.getParent());
    }

    /**
     * Tests querying the root node for its value index. This index is
     * undefined.
     */
    @Test
    public void testGetValueIndexRoot()
    {
        assertEquals("Wrong root value index", FlatNode.INDEX_UNDEFINED, parent
                .getValueIndex());
    }

    /**
     * Tests querying the value from the root node. The root never has a value.
     */
    @Test
    public void testGetValueRoot()
    {
        assertNull("Wrong value of root node", parent.getValue());
    }

    /**
     * Tests setting the value of the root node. This should cause an exception
     * because the root node does not support a value.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testSetValueRoot()
    {
        parent.setValue(VALUE);
    }

    /**
     * Tests the implementation of isInternalUpdate() in the root node.
     */
    @Test
    public void testIsInternalUpdateRoot()
    {
        assertFalse("Already an internal update", parent.isInternalUpdate());
        parent.setInternalUpdate(true);
        assertTrue("No internal update", parent.isInternalUpdate());
        parent.setInternalUpdate(false);
        assertFalse("Still internal update", parent.isInternalUpdate());
    }

    /**
     * Tests the implementation of isInternalUpdate() in the leaf node.
     */
    @Test
    public void testIsInternalUpdateLeaf()
    {
        assertFalse("Already an internal update", node.isInternalUpdate());
        node.setInternalUpdate(true);
        assertTrue("No internal update", node.isInternalUpdate());
        assertTrue("No internal update in root", parent.isInternalUpdate());
        parent.setInternalUpdate(false);
        assertFalse("Still internal update", node.isInternalUpdate());
    }
}
