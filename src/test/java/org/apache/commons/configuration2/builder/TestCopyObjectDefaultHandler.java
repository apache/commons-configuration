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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code CopyObjectDefaultHandler}.
 *
 */
public class TestCopyObjectDefaultHandler
{
    /**
     * Tries to create an instance without a source object.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNull()
    {
        new CopyObjectDefaultHandler(null);
    }

    /**
     * Tests whether default values can be copied onto an object of the same
     * type.
     */
    @Test
    public void testInitializeDefaultsSameType()
    {
        final Long refresh = 50000L;
        final FileBasedBuilderParametersImpl source =
                new FileBasedBuilderParametersImpl();
        source.setReloadingRefreshDelay(refresh).setThrowExceptionOnMissing(
                true);
        final CopyObjectDefaultHandler handler = new CopyObjectDefaultHandler(source);
        final FileBasedBuilderParametersImpl copy =
                new FileBasedBuilderParametersImpl();
        handler.initializeDefaults(copy);
        final Map<String, Object> map = copy.getParameters();
        assertEquals("Wrong exception flag", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
        assertEquals("Wrong refresh", refresh, copy.getReloadingRefreshDelay());
    }

    /**
     * Tests whether a base type can be initialized with default values. Unknown
     * properties should silently be ignored.
     */
    @Test
    public void testInitializeDefaultsBaseType()
    {
        final Long refresh = 50000L;
        final XMLBuilderParametersImpl paramsXml = new XMLBuilderParametersImpl();
        paramsXml
                .setValidating(true)
                .setExpressionEngine(
                        EasyMock.createMock(ExpressionEngine.class))
                .setReloadingRefreshDelay(refresh);
        final CopyObjectDefaultHandler handler =
                new CopyObjectDefaultHandler(paramsXml);
        final FileBasedBuilderParametersImpl paramsFb =
                new FileBasedBuilderParametersImpl();
        handler.initializeDefaults(paramsFb);
        assertEquals("Wrong refresh", refresh,
                paramsFb.getReloadingRefreshDelay());
    }

    /**
     * Tests whether exceptions during copying are re-thrown as runtime
     * exceptions.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testInitializeDefaultsException()
    {
        final ExpressionEngine engine = EasyMock.createMock(ExpressionEngine.class);
        final XMLBuilderParametersImpl source = new XMLBuilderParametersImpl();
        source.setExpressionEngine(engine);
        final XMLBuilderParametersImpl dest = new XMLBuilderParametersImpl()
        {
            @Override
            public HierarchicalBuilderParametersImpl setExpressionEngine(
                    final ExpressionEngine engine)
            {
                throw new ConfigurationRuntimeException("Test exception");
            }
        };

        final CopyObjectDefaultHandler handler = new CopyObjectDefaultHandler(source);
        handler.initializeDefaults(dest);
    }
}
