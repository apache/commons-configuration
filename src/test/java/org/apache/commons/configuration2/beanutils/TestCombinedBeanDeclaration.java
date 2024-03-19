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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@code CombinedBeanDeclaration}.
 */
public class TestCombinedBeanDeclaration {
    /** An array with the mocks for the child bean declarations. */
    private BeanDeclaration[] declarations;

    /**
     * Creates a test instance with a number of mock child declarations.
     *
     * @return the test instance
     */
    private CombinedBeanDeclaration createCombinedDeclaration() {
        declarations = new BeanDeclaration[3];
        for (int i = 0; i < declarations.length; i++) {
            declarations[i] = mock(BeanDeclaration.class);
        }
        return new CombinedBeanDeclaration(declarations);
    }

    /**
     * Convenience method for accessing a mock declaration with the given index.
     *
     * @param idx the index
     * @return the corresponding mock child bean declaration
     */
    private BeanDeclaration decl(final int idx) {
        return declarations[idx];
    }

    /**
     * Tests whether the bean class name can be obtained if it is set for one of the child declarations.
     */
    @Test
    public void testGetBeanClassNameDefined() {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();

        when(decl(0).getBeanClassName()).thenReturn(null);
        when(decl(1).getBeanClassName()).thenReturn(getClass().getName());

        assertEquals(getClass().getName(), cd.getBeanClassName());

        verify(decl(0)).getBeanClassName();
        verify(decl(1)).getBeanClassName();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests getBeanClassName() if none of the child declarations provide a value.
     */
    @Test
    public void testGetBeanClassNameUndefined() {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull(cd.getBeanClassName());
    }

    /**
     * Tests whether the bean factory can be obtained if it is set for one child declarations.
     */
    @Test
    public void testGetBeanFactoryNameDefined() {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final String name = "someTestBeanFactory";

        when(decl(0).getBeanFactoryName()).thenReturn(null);
        when(decl(1).getBeanFactoryName()).thenReturn(name);

        assertEquals(name, cd.getBeanFactoryName());

        verify(decl(0)).getBeanFactoryName();
        verify(decl(1)).getBeanFactoryName();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests getBeanFactoryName() if none of the child declarations provide a value.
     */
    @Test
    public void testGetBeanFactoryNameUndefined() {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull(cd.getBeanFactoryName());
    }

    /**
     * Tests whether the bean factory parameter can be obtained if it is set for one of the child declarations.
     */
    @Test
    public void testGetBeanFactoryParameterDefined() {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final Object param = new Object();

        when(decl(0).getBeanFactoryParameter()).thenReturn(null);
        when(decl(1).getBeanFactoryParameter()).thenReturn(param);

        assertSame(param, cd.getBeanFactoryParameter());

        verify(decl(0)).getBeanFactoryParameter();
        verify(decl(1)).getBeanFactoryParameter();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests getBeanFactoryParameter() if none of the child declarations provide a value.
     */
    @Test
    public void testGetBeanFactoryParameterUndefined() {
        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration();
        assertNull(cd.getBeanFactoryParameter());
    }

    /**
     * Tests whether a combined map of bean properties can be obtained.
     */
    @Test
    public void testGetBeanProperties() {
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

        when(decl(0).getBeanProperties()).thenReturn(props1);
        when(decl(1).getBeanProperties()).thenReturn(props2);
        when(decl(2).getBeanProperties()).thenReturn(props3);

        final Map<String, Object> props = cd.getBeanProperties();

        final Map<String, String> expected = new HashMap<>();
        expected.put("param1", "value1");
        expected.put("param2", "value2");
        expected.put("param3", "value3");
        expected.put("param4", "value4");
        assertEquals(expected, props);

        verify(decl(0)).getBeanProperties();
        verify(decl(1)).getBeanProperties();
        verify(decl(2)).getBeanProperties();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests whether null return values of bean property maps are handled correctly.
     */
    @Test
    public void testGetBeanPropertiesNull() {
        final BeanDeclaration child = mock(BeanDeclaration.class);

        when(child.getBeanProperties()).thenReturn(null);

        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration(child);
        assertEquals(Collections.emptyMap(), cd.getBeanProperties());

        verify(child).getBeanProperties();
        verifyNoMoreInteractions(child);
    }

    /**
     * Tests whether constructor arguments can be obtained if one of the child declarations provide this data.
     */
    @Test
    public void testGetConstructorArgsDefined() {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();
        final Collection<ConstructorArg> args = Arrays.asList(ConstructorArg.forValue(42));

        when(decl(0).getConstructorArgs()).thenReturn(null);
        when(decl(1).getConstructorArgs()).thenReturn(args);

        assertSame(args, cd.getConstructorArgs());

        verify(decl(0)).getConstructorArgs();
        verify(decl(1)).getConstructorArgs();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests getConstructorArgs() if none of the child declarations provide a value.
     */
    @Test
    public void testGetConstructorArgsUndefined() {
        final CombinedBeanDeclaration cd = createCombinedDeclaration();

        when(decl(0).getConstructorArgs()).thenReturn(null);
        when(decl(1).getConstructorArgs()).thenReturn(new ArrayList<>());
        when(decl(2).getConstructorArgs()).thenReturn(null);

        assertEquals(Collections.emptyList(), new ArrayList<>(cd.getConstructorArgs()));

        verify(decl(0)).getConstructorArgs();
        verify(decl(1)).getConstructorArgs();
        verify(decl(2)).getConstructorArgs();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests whether a combined map of nested bean declarations can be obtained.
     */
    @Test
    public void testGetNestedBeanDeclarations() {
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

        when(decl(0).getNestedBeanDeclarations()).thenReturn(decls1);
        when(decl(1).getNestedBeanDeclarations()).thenReturn(decls2);
        when(decl(2).getNestedBeanDeclarations()).thenReturn(decls3);

        final Map<String, Object> decls = cd.getNestedBeanDeclarations();

        final Map<String, String> expected = new HashMap<>();
        expected.put("param1", "value1");
        expected.put("param2", "value2");
        expected.put("param3", "value3");
        expected.put("param4", "value4");
        assertEquals(expected, decls);

        verify(decl(0)).getNestedBeanDeclarations();
        verify(decl(1)).getNestedBeanDeclarations();
        verify(decl(2)).getNestedBeanDeclarations();
        verifyNoMoreDeclarationInteractions();
    }

    /**
     * Tests whether null return values of bean declaration maps are handled correctly.
     */
    @Test
    public void testGetNestedBeanDeclarationsNull() {
        final BeanDeclaration child = mock(BeanDeclaration.class);

        when(child.getNestedBeanDeclarations()).thenReturn(null);

        final CombinedBeanDeclaration cd = new CombinedBeanDeclaration(child);
        assertEquals(Collections.emptyMap(), cd.getNestedBeanDeclarations());

        verify(child).getNestedBeanDeclarations();
        verifyNoMoreInteractions(child);
    }

    /**
     * Helper method for verifying that no more interactions have been performed on all declarations.
     */
    private void verifyNoMoreDeclarationInteractions() {
        verifyNoMoreInteractions((Object[]) declarations);
    }
}
