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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.VFSFileSystem;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A specialized test class for {@code CombinedConfigurationBuilder} which uses
 * a VFS file system for accessing file-based configurations.
 *
 * @version $Id$
 */
public class TestCombinedConfigurationBuilderVFS extends
        TestCombinedConfigurationBuilder
{
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        FileSystem.setDefaultFileSystem(new VFSFileSystem());
    }

    @After
    public void tearDown() throws Exception
    {
        FileSystem.resetDefaultFileSystem();
    }

    /**
     * Tests if the base path is correctly evaluated.
     */
    @Test
    public void testSetConfigurationBasePath() throws ConfigurationException
    {
        File deepDir = new File(ConfigurationAssert.TEST_DIR, "config/deep");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fileName", "test.properties");
        BasicConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                createDefinitionBuilder(createDefinitionConfig("properties",
                        params));
        builder.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(defBuilder).setBasePath(
                        deepDir.getAbsolutePath()));
        Configuration config = builder.getConfiguration();
        assertEquals("Wrong property value", "somevalue",
                config.getString("somekey"));
    }
}
