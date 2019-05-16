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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code CombinedBeanDeclaration}.
 *
 */
public class TestCombinedBeanDeclaration
{
    /** An array with the mocks for the child bean declarations. */
    private BeanDeclaration[] declarations;

    /**
     * Helper method for replaying the mock objects used by the tests.
     */
    private void replay()
    {
        if (declarations != null)
        {
            EasyMock.replay((Object[]) declarations);
        }
    }

    /**
     * Helper method for verifying the mock objects used by the tests.
     */
    private void verify()
    {
        if (declarations != null)
        {
            EasyMock.verify((Object[]) declarations);
        }
    }

    /**
     * Convenience method for accessing a mock declaration with the given index.
     *
     * @param idx the index
     * @return the corresponding mock child bean declaration
     */
    private BeanDeclaration decl(final int idx)
    {
        return declarations[idx];
    }

    /**
     * Creates a test instance with a number of mock child declarations.
     *
     * @return the test instance
     */
    private CombinedBeanDeclaration createCombinedDeclaration()
    {
        declarations = new BeanDeclaration[3];
        for (int i = 0; i < declarations.length; i++)
        {
            declarations[i] = EasyMock.createMock(BeanDeclaration.class);
        }
        return new CombinedBeanDeclaration(declarations);
    }

    /**
     * Tests whether the bean factory can be obtained if it is set for one child
     * declarations.
     */
    @Test
    public void testGetBeanFactoryNameDefined()
    {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final String name = "someTestBeanFactory";
        EasyMock.expect(decl(0).getBeanFactoryName()).andReturn(null);
        EasyMock.expect(decl(1).getBeanFactoryName()).andReturn(name);
        replay();
        assertEquals("Wrong factory name", name, cd.getBeanFactoryName());
        verify();
    }

    /**
     * Tests getBeanFactoryName() if none of the child declarations provide a
     * value.
     */
    @Test
    public void testGetBeanFactoryNameUndefined()
    {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull("Got a factory name", cd.getBeanFactoryName());
    }

    /**
     * Tests whether the bean factory parameter can be obtained if it is set for
     * one of the child declarations.
     */
    @Test
    public void testGetBeanFactoryParameterDefined()
    {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final Object param = new Object();
        EasyMock.expect(decl(0).getBeanFactoryParameter()).andReturn(null);
        EasyMock.expect(decl(1).getBeanFactoryParameter()).andReturn(param);
        replay();
        assertSame("Wrong parameter", param, cd.getBeanFactoryParameter());
        verify();
    }

    /**
     * Tests getBeanFactoryParameter() if none of the child declarations provide
     * a value.
     */
    @Test
    public void testGetBeanFactoryParameterUndefined()
    {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull("Got a factory parameter", cd.getBeanFactoryParameter());
    }

    /**
     * Tests whether the bean class name can be obtained if it is set for one of
     * the child declarations.
     */
    @Test
    public void testGetBeanClassNameDefined()
    {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        EasyMock.expect(decl(0).getBeanClassName()).andReturn(null);
        EasyMock.expect(decl(1).getBeanClassName()).andReturn(
                getClass().getName());
        replay();
        assertEquals("Wrong bean class", getClass().getName(),
                cd.getBeanClassName());
        verify();
    }

    /**
     * Tests getBeanClassName() if none of the child declarations provide a
     * value.
     */
    @Test
    public void testGetBeanClassNameUndefined()
    {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull("Got a bean class name", cd.getBeanClassName());
    }

    /**
     * Tests whether constructor arguments can be obtained if one of the child
     * declarations provide this data.
     */
    @Test
    public void testGetConstructorArgsDefined()
    {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final Collection<ConstructorArg> args =
                Arrays.asList(ConstructorArg.forValue(42));
        EasyMock.expect(decl(0).getConstructorArgs()).andReturn(null);
        EasyMock.expect(decl(1).getConstructorArgs()).andReturn(args);
        replay();
        assertSame("Wrong constructor arguments", args, cd.getConstructorArgs());
        verify();
    }

    /**
     * Tests getConstructorArgs() if none of the child declarations provide a
     * value.
     */
    @Test
    public void testGetConstructorArgsUndefined()
    {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        EasyMock.expect(decl(0).getConstructorArgs()).andReturn(null);
        EasyMock.expect(decl(1).getConstructorArgs()).andReturn(
                new ArrayList<ConstructorArg>());
        EasyMock.expect(decl(2).getConstructorArgs()).andReturn(null);
        replay();
        assertTrue("Got constructor arguments", cd.getConstructorArgs()
                .isEmpty());
        verify();
    }

    /**
     * Tests whether a combined map of bean properties can be obtained.
     */
    @Test
    public void testGetBeanProperties()
    {
        final Map<String, Object> props1 = new HashMap<>();
        final Map<String, Object> props2 = new HashMap<>();
        final Map<String, Object> props3 = new HashMap<>();
        props1.put("param1", "value1");
        props1.put("param2", "value2");
        props2.put("param2", "othervalue");
        props2.put("param3", "value3");
        props3.put("param1", "differentvalue");
        props3.put("param4", "value4");
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        EasyMock.expect(decl(0).getBeanProperties()).andReturn(props1);
        EasyMock.expect(decl(1).getBeanProperties()).andReturn(props2);
        EasyMock.expect(decl(2).getBeanProperties()).andReturn(props3);
        replay();
        final Map<String, Object> props = cd.getBeanProperties();
        assertEquals("Wrong number of properties", 4, props.size());
        for (int i = 1; i <= 4; i++)
        {
            assertEquals("Wrong property", "value" + i, props.get("param" + i));
        }
        verify();
    }

    /**
     * Tests whether null return values of bean property maps are handled
     * correctly.
     */
    @Test
    public void testGetBeanPropertiesNull()
    {
        final BeanDeclaration child = EasyMock.createMock(BeanDeclaration.class);
        EasyMock.expect(child.getBeanProperties()).andReturn(null);
        EasyMock.replay(child);
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration(child);
        assertTrue("Got bean properties", cd.getBeanProperties().isEmpty());
    }

    /**
     * Tests whether a combined map of nested bean declarations can be obtained.
     */
    @Test
    public void testGetNestedBeanDeclarations()
    {
        final Map<String, Object> decls1 = new HashMap<>();
        final Map<String, Object> decls2 = new HashMap<>();
        final Map<String, Object> decls3 = new HashMap<>();
        decls1.put("param1", "value1");
        decls1.put("param2", "value2");
        decls2.put("param2", "othervalue");
        decls2.put("param3", "value3");
        decls3.put("param1", "differentvalue");
        decls3.put("param4", "value4");
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        EasyMock.expect(decl(0).getNestedBeanDeclarations()).andReturn(decls1);
        EasyMock.expect(decl(1).getNestedBeanDeclarations()).andReturn(decls2);
        EasyMock.expect(decl(2).getNestedBeanDeclarations()).andReturn(decls3);
        replay();
        final Map<String, Object> decls = cd.getNestedBeanDeclarations();
        assertEquals("Wrong number of declarations", 4, decls.size());
        for (int i = 1; i <= 4; i++)
        {
            assertEquals("Wrong declaration", "value" + i,
                    decls.get("param" + i));
        }
        verify();
    }

    /**
     * Tests whether null return values of bean declaration maps are handled
     * correctly.
     */
    @Test
    public void testGetNestedBeanDeclarationsNull()
    {
        final BeanDeclaration child = EasyMock.createMock(BeanDeclaration.class);
        EasyMock.expect(child.getNestedBeanDeclarations()).andReturn(null);
        EasyMock.replay(child);
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration(child);
        assertTrue("Got bean declarations", cd.getNestedBeanDeclarations()
                .isEmpty());
    }
}
