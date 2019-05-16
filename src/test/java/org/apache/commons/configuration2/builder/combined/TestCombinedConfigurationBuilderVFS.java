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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.VFSFileSystem;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

/**
 * A specialized test class for {@code CombinedConfigurationBuilder} which uses
 * a VFS file system for accessing file-based configurations.
 *
 */
public class TestCombinedConfigurationBuilderVFS extends
        TestCombinedConfigurationBuilder
{
    /**
     * {@inheritDoc} This implementation initializes the parameters object with
     * the VFS file system.
     */
    @Override
    protected FileBasedBuilderParameters createParameters()
    {
        final FileBasedBuilderParameters params = super.createParameters();
        return params.setFileSystem(new VFSFileSystem());
    }

    /**
     * Tests if the base path is correctly evaluated.
     */
    @Test
    public void testSetConfigurationBasePath() throws ConfigurationException
    {
        final File deepDir = new File(ConfigurationAssert.TEST_DIR, "config/deep");
        final Map<String, Object> params = new HashMap<>();
        params.put("fileName", "test.properties");
        final BaseHierarchicalConfiguration defConfig =
                createDefinitionConfig("properties", params);
        defConfig.addProperty("override.properties.fileSystem[@config-class]",
                VFSFileSystem.class.getName());
        final BasicConfigurationBuilder<? extends HierarchicalConfiguration<ImmutableNode>> defBuilder =
                createDefinitionBuilder(defConfig);
        builder.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(defBuilder).setBasePath(
                        deepDir.getAbsolutePath()));
        final Configuration config = builder.getConfiguration();
        assertEquals("Wrong property value", "somevalue",
                config.getString("somekey"));
    }
}
