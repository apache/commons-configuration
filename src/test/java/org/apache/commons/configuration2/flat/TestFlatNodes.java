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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * Test class for the FlatNode classes.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestFlatNodes extends TestCase
{
    /** Constant for the name of the test node. */
    private static final String NAME = "testFlatNode";

    /** Constant for a test value. */
    static final Object VALUE = 42;

    /** The parent node. */
    private FlatRootNode parent;

    /** The node to be tested. */
    private FlatLeafNode node;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        parent = new FlatRootNode();
        node = (FlatLeafNode) parent.addChild(NAME);
    }

    /**
     * Tests querying the name of a leaf node.
     */
    public void testGetNameLeaf()
    {
        assertEquals("Wrong node name", NAME, node.getName());
    }

    /**
     * Tests querying the parent node.
     */
    public void testGetParent()
    {
        assertSame("Wrong parent node", parent, node.getParent());
    }

    /**
     * Tests querying a leaf node's children. A leaf node has no children, so
     * result should always be an empty list.
     */
    public void testGetChildrenLeaf()
    {
        assertTrue("Children not empty", node.getChildren().isEmpty());
    }

    /**
     * Tests querying a leaf's child nodes with a specific name. Because a leaf
     * node cannot have any children result should always be an empty list.
     */
    public void testGetChildrenNameLeaf()
    {
        assertTrue("Named children not empty", node.getChildren("test")
                .isEmpty());
    }

    /**
     * Tests querying the number of children of a leaf node. Result should
     * always be 0.
     */
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
    public void testGetValueIndexLeafSingle()
    {
        assertEquals("Wrong index for single child node",
                FlatNode.INDEX_UNDEFINED, node.getValueIndex());
    }

    /**
     * Tests querying the indices of child nodes.
     */
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
    public void testGetValueSimple()
    {
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty(NAME, VALUE);
        assertEquals("Wrong property value", VALUE, node.getValue(conf));
    }

    /**
     * Tests the getValue() method when the value is a collection, but no index
     * is specified. In this case the whole collection should be returned.
     */
    public void testGetValueCollectionNoIndex()
    {
        BaseConfiguration config = new BaseConfiguration();
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        config.addPropertyDirect(NAME, values);
        assertSame("Wrong value collection", values, node.getValue(config));
    }

    /**
     * Tests the getValue() method when multiple values are involved.
     */
    public void testGetValueCollection()
    {
        BaseConfiguration config = new BaseConfiguration();
        Collection<Object> values = new ArrayList<Object>();
        values.add(VALUE);
        values.add(2);
        config.setProperty(NAME, values);
        FlatNode c2 = parent.addChild(NAME);
        assertEquals("Wrong value index 1", VALUE, node.getValue(config));
        assertEquals("Wrong value index 2", 2, c2.getValue(config));
    }

    /**
     * Tests the getValue() method when multiple values are involved, but the
     * index is out of range. In this case null should be returned.
     */
    public void testGetValueCollectionInvalidIndex()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        config.addProperty(NAME, 2);
        parent.addChild(NAME);
        FlatNode c2 = parent.addChild(NAME);
        assertNull("Found value for invalid index", c2.getValue(config));
    }

    /**
     * Tests the setValue() method for a new value.
     */
    public void testSetValueNew()
    {
        BaseConfiguration config = new BaseConfiguration();
        node.setValue(config, VALUE);
        assertEquals("Value was not set", VALUE, config.getProperty(NAME));
    }

    /**
     * Tests the setValue() method for an existing value.
     */
    public void testSetValueExisting()
    {
        BaseConfiguration config = new BaseConfiguration();
        // remove node, so that there is only a single child with this name
        parent.removeChild(config, node);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue(config, VALUE);
        assertEquals("Value was not set", VALUE, config.getProperty(NAME));
    }

    /**
     * Tests setting a value for a property with multiple values.
     */
    public void testSetValueCollection()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, 1);
        config.addProperty(NAME, 2);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue(config, VALUE);
        List<?> values = config.getList(NAME);
        assertEquals("Wrong number of values", 2, values.size());
        assertEquals("Wrong value 1", 1, values.get(0));
        assertEquals("Wrong value 2", VALUE, values.get(1));
    }

    /**
     * Tests the modification of a property with multiple values if an invalid
     * value index is involved. This case should not happen normally. No
     * modification should be performed.
     */
    public void testSetValueCollectionInvalidIndex()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        config.addProperty(NAME, 2);
        parent.addChild(NAME, true);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue(config, "new");
        List<?> values = config.getList(NAME);
        assertEquals("Wrong number of values", 2, values.size());
        assertEquals("Wrong value 0", VALUE, values.get(0));
        assertEquals("Wrong value 1", 2, values.get(1));
    }

    /**
     * Tests the modification of a property with multiple values if the
     * configuration does not return a collection. This should normally not
     * happen. In this case no modification should be performed.
     */
    public void testSetValueCollectionInvalidValue()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        FlatNode child = parent.addChild(NAME, true);
        child.setValue(config, "new");
        assertEquals("Value was changed", VALUE, config.getProperty(NAME));
    }

    /**
     * Tests calling setValue() twice. The first time, the value should be
     * added. The second time it should be overridden.
     */
    public void testSetValueNewAndExisting()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty(NAME, 1);
        node.setValue(config, VALUE);
        List<?> values = config.getList(NAME);
        assertEquals("Value was not added", 2, values.size());
        assertEquals("Wrong value", VALUE, values.get(1));
        node.setValue(config, "new");
        assertEquals("Value was not changed", "new", config.getProperty(NAME));
    }

    /**
     * Tests removing a child node. The associated configuration should also be
     * updated,
     */
    public void testRemoveChild()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        parent.removeChild(config, node);
        assertFalse("Property not removed", config.containsKey(NAME));
    }

    /**
     * Tests removing a child node for a property with multiple values.
     */
    public void testRemoveChildCollection()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, new Object[] {
                1, 2, 3
        });
        FlatNode n2 = parent.addChild(NAME, true);
        parent.addChild(NAME, true);
        parent.removeChild(config, n2);
        List<?> values = config.getList(NAME);
        assertEquals("Wrong number of values", 2, values.size());
        assertEquals("Wrong value 1", 1, values.get(0));
        assertEquals("Wrong value 2", 3, values.get(1));
    }

    /**
     * Tests the behavior of removeChild() if after the operation only a single
     * collection element remains.
     */
    public void testRemoveChildCollectionSingleElement()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        config.addProperty(NAME, 2);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(config, n2);
        assertEquals("Wrong value", VALUE, config.getProperty(NAME));
    }

    /**
     * Tests removeChild() if the child has an invalid index. This should
     * normally not happen. In this case no modification should be performed.
     */
    public void testRemoveChildCollectionInvalidIndex()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        config.addProperty(NAME, 2);
        parent.addChild(NAME, true);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(config, n2);
        List<?> values = config.getList(NAME);
        assertEquals("Wrong number of values", 2, values.size());
        assertEquals("Wrong value 1", VALUE, values.get(0));
        assertEquals("Wrong value 2", 2, values.get(1));
    }

    /**
     * Tests removeChild() for a property with multiple values if the
     * configuration does not return a collection. This should normally not
     * happen. In this case no modification should be performed.
     */
    public void testRemoveChildeCollectionInvalidValue()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty(NAME, VALUE);
        FlatNode n2 = parent.addChild(NAME, true);
        parent.removeChild(config, n2);
        assertEquals("Wrong value", VALUE, config.getProperty(NAME));
    }

    /**
     * Tries to remove a child node that does not belong to the parent. This
     * should cause an exception.
     */
    public void testRemoveChildWrongParent()
    {
        FlatLeafNode child = new FlatLeafNode(null, "test", true);
        BaseConfiguration config = new BaseConfiguration();
        try
        {
            parent.removeChild(config, child);
            fail("Could remove non existing child!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tries to remove a null child node. This should cause an exception.
     */
    public void testRemoveChildNull()
    {
        try
        {
            parent.removeChild(new BaseConfiguration(), null);
            fail("Could remove null child!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests corner cases when adding and removing child nodes.
     */
    public void testAddAndRemoveChild()
    {
        final int count = 5;
        BaseConfiguration config = new BaseConfiguration();
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
                    .getValue(config));
        }
        for (int j = count - 1; j > 0; j--)
        {
            parent.removeChild(config, nodes.get(j));
            List<FlatNode> remainingChildren = parent.getChildren(NAME);
            assertEquals("Wrong children", nodes.subList(0, j),
                    remainingChildren);
        }
        assertEquals("Wrong remaining value", Integer.valueOf(0), config
                .getProperty(NAME));
        parent.removeChild(config, nodes.get(0));
        assertFalse("Property still found", config.containsKey(NAME));
        assertEquals("Wrong number of children", 0, parent
                .getChildrenCount(NAME));
    }

    /**
     * Tests adding a child node to a leaf. This should cause an exception.
     */
    public void testAddChildLeaf()
    {
        try
        {
            node.addChild(NAME);
            fail("Could add a child to a leaf node!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests removing a child from a leaf. This should cause an exception.
     */
    public void testRemoveChildLeaf()
    {
        BaseConfiguration config = new BaseConfiguration();
        try
        {
            node.removeChild(config, parent);
            fail("Could remove child from a leaf!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests the getChildren() method of a root node.
     */
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
    public void testGetChildrenNameUnknown()
    {
        List<FlatNode> children = parent.getChildren("unknownName");
        assertTrue("Found unknown children", children.isEmpty());
    }

    /**
     * Tests accessing child nodes by its index.
     */
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
    public void testGetChildrenCountUnknown()
    {
        assertEquals("Found unknown children", 0, parent
                .getChildrenCount("unknownName"));
    }

    /**
     * Tests querying the name of the root node. The answer should be null.
     */
    public void testGetNameRoot()
    {
        assertNull("Wrong root name", parent.getName());
    }

    /**
     * Tests querying the parent of the root node. Of course, this node has no
     * parent.
     */
    public void testGetParentRoot()
    {
        assertNull("Root node has a parent", parent.getParent());
    }

    /**
     * Tests querying the root node for its value index. This index is
     * undefined.
     */
    public void testGetValueIndexRoot()
    {
        assertEquals("Wrong root value index", FlatNode.INDEX_UNDEFINED, parent
                .getValueIndex());
    }

    /**
     * Tests querying the value from the root node. The root never has a value.
     */
    public void testGetValueRoot()
    {
        BaseConfiguration config = new BaseConfiguration();
        assertNull("Wrong value of root node", parent.getValue(config));
    }

    /**
     * Tests setting the value of the root node. This should cause an exception
     * because the root node does not support a value.
     */
    public void testSetValueRoot()
    {
        BaseConfiguration config = new BaseConfiguration();
        try
        {
            parent.setValue(config, VALUE);
            fail("Could set value of root node!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }
}
