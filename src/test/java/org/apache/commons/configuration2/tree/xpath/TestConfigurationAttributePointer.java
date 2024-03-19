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
package org.apache.commons.configuration2.tree.xpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code ConfigurationAttributePointer}.
 */
public class TestConfigurationAttributePointer {
    /** Constant for the name of the test attribute. */
    private static final String ATTR_NAME = "myAttr";

    /** Constant for the value of the test attribute. */
    private static final String ATTR_VALUE = "myValue";

    /** Stores the parent node pointer. */
    private ConfigurationNodePointer<ImmutableNode> parent;

    /** The attribute pointer to be tested. */
    private ConfigurationAttributePointer<ImmutableNode> pointer;

    @BeforeEach
    public void setUp() throws Exception {
        final ImmutableNode.Builder ndBuilder = new ImmutableNode.Builder();
        ndBuilder.name("parent").addAttribute(ATTR_NAME, ATTR_VALUE);
        final ImmutableNode nd = ndBuilder.create();
        parent = new ConfigurationNodePointer<>(nd, Locale.ENGLISH, new InMemoryNodeModel(nd).getNodeHandler());
        pointer = new ConfigurationAttributePointer<>(parent, ATTR_NAME);
    }

    /**
     * Tests querying an iterator for attributes. Result should be null.
     */
    @Test
    public void testAttributeIterator() {
        assertNull(pointer.attributeIterator(new QName(null, "test")));
    }

    /**
     * Tests querying an iterator for children. Result should be null.
     */
    @Test
    public void testChildIterator() {
        assertNull(pointer.childIterator(null, false, null));
    }

    /**
     * Tests querying the base value.
     */
    @Test
    public void testGetBaseValue() {
        assertEquals(ATTR_VALUE, pointer.getBaseValue());
    }

    /**
     * Tests querying the immediate node. Here a proxy for an attribute node should be returned.
     */
    @Test
    public void testGetImmediateNode() {
        final Object node = pointer.getImmediateNode();
        final QueryResult<?> proxy = assertInstanceOf(QueryResult.class, node);
        assertTrue(proxy.isAttributeResult());
        assertEquals(parent.getConfigurationNode(), proxy.getNode());
        assertEquals(ATTR_NAME, proxy.getAttributeName());
    }

    /**
     * Tests the length.
     */
    @Test
    public void testGetLength() {
        assertEquals(1, pointer.getLength());
    }

    /**
     * Tests querying the node name.
     */
    @Test
    public void testGetName() {
        final QName name = pointer.getName();
        assertEquals(ATTR_NAME, name.getName());
        assertNull(name.getPrefix());
    }

    /**
     * Tests whether the correct pointer is returned.
     */
    @Test
    public void testGetParentPointer() {
        assertSame(parent, pointer.getParentPointer());
    }

    /**
     * Tests querying the attribute's value.
     */
    @Test
    public void testGetValue() {
        assertEquals(ATTR_VALUE, pointer.getValue());
    }

    /**
     * Tests the attribute flag.
     */
    @Test
    public void testIsAttribute() {
        assertTrue(pointer.isAttribute());
    }

    /**
     * Tests the collection flag.
     */
    @Test
    public void testIsCollection() {
        assertFalse(pointer.isCollection());
    }

    /**
     * Tests the leaf flag.
     */
    @Test
    public void testIsLeaf() {
        assertTrue(pointer.isLeaf());
    }

    /**
     * Tries to set a new value.
     */
    @Test
    public void testSetValue() {
        assertThrows(UnsupportedOperationException.class, () -> pointer.setValue("newValue"));
    }

    /**
     * Tests the testNode() method.
     */
    @Test
    public void testTestNode() {
        NodeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue(pointer.testNode(test));
        test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertFalse(pointer.testNode(test));
    }
}
