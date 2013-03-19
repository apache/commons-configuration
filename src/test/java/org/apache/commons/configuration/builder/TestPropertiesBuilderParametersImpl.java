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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code PropertiesBuilderParametersImpl}.
 *
 * @version $Id: $
 */
public class TestPropertiesBuilderParametersImpl
{
    /** The parameters object to be tested. */
    private PropertiesBuilderParametersImpl params;

    @Before
    public void setUp() throws Exception
    {
        params = new PropertiesBuilderParametersImpl();
    }

    /**
     * Tests whether the includesAllowed property can be set.
     */
    @Test
    public void testSetIncludesAllowed()
    {
        assertSame("Wrong result", params, params.setIncludesAllowed(true));
        assertEquals("Value not set", Boolean.TRUE,
                params.getParameters().get("includesAllowed"));
    }

    /**
     * Tests whether the layout object can be set.
     */
    @Test
    public void testSetLayout()
    {
        PropertiesConfigurationLayout layout =
                new PropertiesConfigurationLayout();
        assertSame("Wrong result", params, params.setLayout(layout));
        assertSame("Layout not set", layout,
                params.getParameters().get("layout"));
    }

    /**
     * Tests whether the IO factory can be set.
     */
    @Test
    public void testSetIOFactory()
    {
        PropertiesConfiguration.IOFactory factory =
                EasyMock.createMock(PropertiesConfiguration.IOFactory.class);
        EasyMock.replay(factory);
        assertSame("Wrong result", params, params.setIOFactory(factory));
        assertSame("Factory not set", factory,
                params.getParameters().get("iOFactory"));
    }

    /**
     * Tests whether properties can be set using BeanUtils.
     */
    @Test
    public void testBeanPropertiesAccess() throws Exception
    {
        PropertiesConfiguration.IOFactory factory =
                EasyMock.createMock(PropertiesConfiguration.IOFactory.class);
        EasyMock.replay(factory);
        PropertyUtils.setProperty(params, "iOFactory", factory);
        PropertyUtils.setProperty(params, "throwExceptionOnMissing",
                Boolean.TRUE);
        PropertyUtils.setProperty(params, "fileName", "test.properties");
        assertEquals("Wrong file name", "test.properties", params
                .getFileHandler().getFileName());
        Map<String, Object> paramsMap = params.getParameters();
        assertEquals("Wrong exception flag", Boolean.TRUE,
                paramsMap.get("throwExceptionOnMissing"));
        assertSame("Factory not set", factory,
                params.getParameters().get("iOFactory"));
    }
}
