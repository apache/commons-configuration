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
package org.apache.commons.configuration2.builder.combined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code MultiFileBuilderParametersImpl}.
 *
 */
public class TestMultiFileBuilderParametersImpl
{
    /** The parameters object to be tested. */
    private MultiFileBuilderParametersImpl params;

    @Before
    public void setUp() throws Exception
    {
        params = new MultiFileBuilderParametersImpl();
    }

    /**
     * Tests whether an instance can be obtained from a map if it cannot be
     * found.
     */
    @Test
    public void testFromParatersNotFound()
    {
        assertNull("Got an instance",
                MultiFileBuilderParametersImpl
                        .fromParameters(new HashMap<String, Object>()));
    }

    /**
     * Tests whether an instance can be obtained from a parameters map.
     */
    @Test
    public void testFromParametersFound()
    {
        final Map<String, Object> map = params.getParameters();
        assertSame("Instance not found", params,
                MultiFileBuilderParametersImpl.fromParameters(map, true));
    }

    /**
     * Tests whether a new instance is created if the parameters map does not
     * contain one.
     */
    @Test
    public void testFromParametersNewInstance()
    {
        params =
                MultiFileBuilderParametersImpl.fromParameters(
                        new HashMap<String, Object>(), true);
        assertNotNull("No new instance", params);
    }

    /**
     * Tests whether a file pattern can be set.
     */
    @Test
    public void testSetFilePattern()
    {
        final String pattern = "somePattern";
        assertSame("Wrong result", params, params.setFilePattern(pattern));
        assertEquals("Pattern not set", pattern, params.getFilePattern());
    }

    /**
     * Tests whether parameters for managed configurations can be set.
     */
    @Test
    public void testSetManagedBuilderParameters()
    {
        final BuilderParameters bp = EasyMock.createMock(BuilderParameters.class);
        EasyMock.replay(bp);
        assertSame("Wrong result", params, params.setManagedBuilderParameters(bp));
        assertSame("Parameters not set", bp,
                params.getManagedBuilderParameters());
    }

    /**
     * Tests whether bean property access is possible.
     */
    @Test
    public void testBeanProperties() throws Exception
    {
        final BuilderParameters bp = EasyMock.createMock(BuilderParameters.class);
        EasyMock.replay(bp);
        final String pattern = "testPattern";
        BeanHelper.setProperty(params, "filePattern", pattern);
        BeanHelper.setProperty(params, "managedBuilderParameters", bp);
        BeanHelper.setProperty(params, "throwExceptionOnMissing",
                Boolean.TRUE);
        final Map<String, Object> map = params.getParameters();
        assertEquals("Exception flag not set", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
        assertSame("Wrong parameters instance", params,
                MultiFileBuilderParametersImpl.fromParameters(map));
        assertEquals("Wrong pattern", pattern, params.getFilePattern());
        assertSame("Wrong managed parameters", bp,
                params.getManagedBuilderParameters());
    }

    /**
     * Tests extended cloning functionality.
     */
    @Test
    public void testClone()
    {
        final FileBasedBuilderParametersImpl managedParams =
                new FileBasedBuilderParametersImpl();
        managedParams.setFileName("test.xml");
        params.setManagedBuilderParameters(managedParams);
        params.setFilePattern("somePattern");
        final MultiFileBuilderParametersImpl clone = params.clone();
        assertEquals("Wrong pattern", params.getFilePattern(),
                clone.getFilePattern());
        assertNotSame("Managed parameters not cloned",
                params.getManagedBuilderParameters(),
                clone.getManagedBuilderParameters());
        assertEquals("Wrong file name", managedParams.getFileHandler()
                .getFileName(),
                ((FileBasedBuilderParametersImpl) clone
                        .getManagedBuilderParameters()).getFileHandler()
                        .getFileName());
    }
}
