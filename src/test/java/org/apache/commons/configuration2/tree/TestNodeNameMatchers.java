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
package org.apache.commons.configuration2.tree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code NodeNameMatchers}.
 *
 */
public class TestNodeNameMatchers {
    /** Constant for a test node name. */
    private static final String NODE_NAME = "TestNodeName";

    /**
     * Creates a node with the given name.
     *
     * @param name the name
     * @return the newly created node
     */
    private static ImmutableNode createNode(final String name) {
        return new ImmutableNode.Builder().name(name).create();
    }

    /** A node handler. */
    private NodeHandler<ImmutableNode> handler;

    /**
     * Tests whether a matcher can handle null input safely.
     *
     * @param matcher the matcher to be tested
     */
    private void checkMatcherWithNullInput(final NodeMatcher<String> matcher) {
        assertFalse(matcher.matches(createNode(NODE_NAME), handler, null));
        assertFalse(matcher.matches(createNode(null), handler, NODE_NAME));
    }

    @BeforeEach
    public void setUp() throws Exception {
        final InMemoryNodeModel model = new InMemoryNodeModel();
        handler = model.getNodeHandler();
    }

    /**
     * Tests the equalsIgnoreCase mather if the expected result is true.
     */
    @Test
    public void testEqualsIgnoreCaseMatch() {
        final ImmutableNode node = createNode(NODE_NAME);
        assertTrue(NodeNameMatchers.EQUALS_IGNORE_CASE.matches(node, handler, NODE_NAME));
        assertTrue(NodeNameMatchers.EQUALS_IGNORE_CASE.matches(node, handler, NODE_NAME.toLowerCase(Locale.ENGLISH)));
        assertTrue(NodeNameMatchers.EQUALS_IGNORE_CASE.matches(node, handler, NODE_NAME.toUpperCase(Locale.ENGLISH)));
    }

    /**
     * Tests the equalsIgnoreCase matcher if the expected result is false.
     */
    @Test
    public void testEqualsIgnoreCaseNoMatch() {
        final ImmutableNode node = createNode(NODE_NAME);
        assertFalse(NodeNameMatchers.EQUALS_IGNORE_CASE.matches(node, handler, NODE_NAME + "_other"));
    }

    /**
     * Tests whether the equalsIgnoreCase matcher is null-safe.
     */
    @Test
    public void testEqualsIgnoreCaseNullCriterion() {
        checkMatcherWithNullInput(NodeNameMatchers.EQUALS_IGNORE_CASE);
    }

    /**
     * Tests the equals matcher if the expected result is true.
     */
    @Test
    public void testEqualsMatch() {
        final ImmutableNode node = createNode(NODE_NAME);
        assertTrue(NodeNameMatchers.EQUALS.matches(node, handler, NODE_NAME));
    }

    /**
     * Tests the equals matcher for a non matching name.
     */
    @Test
    public void testEqualsNoMatch() {
        final ImmutableNode node = createNode(NODE_NAME);
        assertFalse(NodeNameMatchers.EQUALS.matches(node, handler, NODE_NAME + "_other"));
        assertFalse(NodeNameMatchers.EQUALS.matches(node, handler, NODE_NAME.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Tests whether the equals matcher can handle a null criterion.
     */
    @Test
    public void testEqualsNullCriterion() {
        checkMatcherWithNullInput(NodeNameMatchers.EQUALS);
    }
}
