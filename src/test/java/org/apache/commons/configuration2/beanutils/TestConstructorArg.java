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
package org.apache.commons.configuration2.beanutils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code ConstructorArg}.
 *
 */
public class TestConstructorArg
{
    /**
     * Tries to create an instance for a null bean declaration.
     */
    @Test(expected = NullPointerException.class)
    public void testForBeanDeclarationNull()
    {
        ConstructorArg.forBeanDeclaration(null);
    }

    /**
     * Tests whether an argument representing a bean declaration is detected.
     */
    @Test
    public void testIsNestedBeanDeclarationTrue()
    {
        final BeanDeclaration decl = EasyMock.createMock(BeanDeclaration.class);
        EasyMock.replay(decl);
        final ConstructorArg arg = ConstructorArg.forBeanDeclaration(decl);
        assertTrue("No bean declaration", arg.isNestedBeanDeclaration());
    }

    /**
     * Tests whether an argument with a simple value is detected.
     */
    @Test
    public void testIsNestedBeanDeclarationFalse()
    {
        final ConstructorArg arg = ConstructorArg.forValue("test");
        assertFalse("A bean declaration", arg.isNestedBeanDeclaration());
    }

    /**
     * Tests matches() if no data type is provided.
     */
    @Test
    public void testMatchesNoType()
    {
        final ConstructorArg arg = ConstructorArg.forValue(42);
        assertTrue("No match (1)", arg.matches(String.class));
        assertTrue("No match (2)", arg.matches(getClass()));
    }

    /**
     * Tests whether a specified data type is evaluated by matches().
     */
    @Test
    public void testMatchesWithType()
    {
        final ConstructorArg arg = ConstructorArg.forValue("42", int.class.getName());
        assertTrue("Wrong result (1)", arg.matches(Integer.TYPE));
        assertFalse("Wrong result (2)", arg.matches(Integer.class));
        assertFalse("Wrong result (3)", arg.matches(String.class));
    }

    /**
     * Tests whether matches() deals with a null argument.
     */
    @Test
    public void testMatchesNull()
    {
        final ConstructorArg arg = ConstructorArg.forValue(0);
        assertFalse("Wrong result", arg.matches(null));
    }
}
