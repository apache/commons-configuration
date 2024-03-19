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
package org.apache.commons.configuration2.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code HierarchicalBuilderParametersImpl}.
 */
public class TestHierarchicalBuilderParametersImpl {
    /** The parameters object to be tested. */
    private HierarchicalBuilderParametersImpl params;

    @BeforeEach
    public void setUp() throws Exception {
        params = new HierarchicalBuilderParametersImpl();
    }

    /**
     * Tests whether properties can be set via BeanUtils.
     */
    @Test
    public void testBeanPropertiesAccess() throws Exception {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        BeanHelper.setProperty(params, "expressionEngine", engine);
        BeanHelper.setProperty(params, "throwExceptionOnMissing", Boolean.TRUE);
        final Map<String, Object> map = params.getParameters();
        assertSame(engine, map.get("expressionEngine"));
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether inheritFrom() copies additional properties.
     */
    @Test
    public void testInheritFrom() {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        final HierarchicalBuilderParametersImpl params = new HierarchicalBuilderParametersImpl();
        params.setExpressionEngine(engine);
        params.setThrowExceptionOnMissing(true);
        final HierarchicalBuilderParametersImpl params2 = new HierarchicalBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"));
        assertEquals(engine, parameters.get("expressionEngine"));
    }

    /**
     * Tests whether the expression engine can be set.
     */
    @Test
    public void testSetExpressionEngine() {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        assertSame(params, params.setExpressionEngine(engine));
        assertSame(engine, params.getParameters().get("expressionEngine"));
    }
}
