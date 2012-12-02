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
package org.apache.commons.configuration.builder.combined;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration.builder.ConfigurationBuilder;
import org.junit.Test;

/**
 * Test class for {@code CombinedBuilderParameters}.
 *
 * @version $Id$
 */
public class TestCombinedBuilderParameters
{
    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound()
    {
        assertNull("Got an instance",
                CombinedBuilderParameters
                        .fromParameters(new HashMap<String, Object>()));
    }

    /**
     * Tests whether a new instance can be created if none is found in the
     * parameters map.
     */
    @Test
    public void testFromParametersCreate()
    {
        CombinedBuilderParameters params =
                CombinedBuilderParameters.fromParameters(
                        new HashMap<String, Object>(), true);
        assertNotNull("No instance", params);
        assertNull("Got data", params.getDefinitionBuilder());
    }

    /**
     * Tests whether an instance can be obtained from a parameters map.
     */
    @Test
    public void testFromParametersExisting()
    {
        CombinedBuilderParameters params = new CombinedBuilderParameters();
        Map<String, Object> map = params.getParameters();
        assertSame("Wrong result", params,
                CombinedBuilderParameters.fromParameters(map));
    }

    /**
     * Tests whether the definition builder can be set.
     */
    @Test
    public void testSetDefinitionBuilder()
    {
        CombinedBuilderParameters params = new CombinedBuilderParameters();
        assertNull("Got a definition builder", params.getDefinitionBuilder());
        ConfigurationBuilder<XMLConfiguration> builder =
                new BasicConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class);
        assertSame("Wrong result", params, params.setDefinitionBuilder(builder));
        assertSame("Builder was not set", builder,
                params.getDefinitionBuilder());
    }
}
