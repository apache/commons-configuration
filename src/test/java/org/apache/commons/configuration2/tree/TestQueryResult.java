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

import static org.apache.commons.configuration2.ConfigurationAssert.checkEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code QueryResult}.
 *
 */
public class TestQueryResult
{
    /** Constant for an attribute name. */
    private static final String ATTR = "testAttribute";

    /** Constant for an attribute value. */
    private static final Object VALUE = "Value of my attribute";

    /** A test result node. */
    private static ImmutableNode resultNode;

    /** A test parent node for an attribute. */
    private static ImmutableNode attributeNode;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        resultNode =
                new ImmutableNode.Builder().name("resultNode").value(42)
                        .create();
        attributeNode =
                new ImmutableNode.Builder().name("attributeNode")
                        .addAttribute(ATTR, VALUE).create();
    }

    /**
     * Tests isAttributeResult() if the expected result is true.
     */
    @Test
    public void testIsAttributeResultTrue()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createAttributeResult(attributeNode, ATTR);
        assertTrue("Not an attribute result", result.isAttributeResult());
    }

    /**
     * Tests is attributeResult() if the expected result is false.
     */
    @Test
    public void testIsAttributeResultFalse()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(resultNode);
        assertFalse("An attribute result", result.isAttributeResult());
    }

    /**
     * Tests whether the attribute's value can be queried.
     */
    @Test
    public void testGetAttributeValue()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createAttributeResult(attributeNode, ATTR);
        assertEquals("Wrong value", VALUE,
                result.getAttributeValue(new InMemoryNodeModel()
                        .getNodeHandler()));
    }

    /**
     * Tries to query an attribute value for a non-attribute result.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetAttributeValueNoAttributeResult()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(resultNode);
        result.getAttributeValue(new InMemoryNodeModel().getNodeHandler());
    }

    /**
     * Tests equals() if the expected result is true.
     */
    @Test
    public void testEqualsTrue()
    {
        QueryResult<ImmutableNode> r1 =
                QueryResult.createNodeResult(resultNode);
        checkEquals(r1, r1, true);
        QueryResult<ImmutableNode> r2 =
                QueryResult.createNodeResult(resultNode);
        checkEquals(r1, r2, true);
        r1 = QueryResult.createAttributeResult(attributeNode, ATTR);
        r2 = QueryResult.createAttributeResult(attributeNode, ATTR);
        checkEquals(r1, r2, true);
    }

    /**
     * Tests equals() if the expected result is false.
     */
    @Test
    public void testEqualsFalse()
    {
        final QueryResult<ImmutableNode> nodeRes =
                QueryResult.createNodeResult(resultNode);
        final QueryResult<ImmutableNode> attrRes =
                QueryResult.createAttributeResult(attributeNode, ATTR);
        checkEquals(nodeRes, attrRes, false);
        QueryResult<ImmutableNode> res =
                QueryResult.createNodeResult(attributeNode);
        checkEquals(nodeRes, res, false);
        res = QueryResult.createAttributeResult(attributeNode, "otherAttr");
        checkEquals(attrRes, res, false);
        res = QueryResult.createAttributeResult(resultNode, ATTR);
        checkEquals(attrRes, res, false);
    }

    /**
     * Tests equals() with other objects.
     */
    @Test
    public void testEqualsOtherObjects()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(resultNode);
        checkEquals(result, null, false);
        checkEquals(result, this, false);
    }

    /**
     * Tests the string representation of a node result.
     */
    @Test
    public void testToStringNodeResult()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(resultNode);
        assertThat(result.toString(),
                containsString("resultNode=" + resultNode));
    }

    /**
     * Tests the string representation of an attribute result.
     */
    @Test
    public void testToStringAttributeResult()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createAttributeResult(attributeNode, ATTR);
        final String s = result.toString();
        assertThat(s, containsString("attribute=" + ATTR));
        assertThat(s, containsString("parentNode=" + attributeNode));
    }
}
