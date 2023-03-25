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

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple MultiConfigurationTest.
 */
public class TestPatternSubtreeConfiguration {
    private static final File CONFIG_FILE = ConfigurationAssert.getTestFile("testPatternSubtreeConfig.xml");
    private static final String PATTERN = "BusinessClient[@name='${sys:Id}']";
    private XMLConfiguration conf;

    @BeforeEach
    public void setUp() throws Exception {
        conf = new XMLConfiguration();
        new FileHandler(conf).load(CONFIG_FILE);
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testMultiConfiguration() {
        final PatternSubtreeConfigurationWrapper config = new PatternSubtreeConfigurationWrapper(this.conf, PATTERN);
        config.setExpressionEngine(new XPathExpressionEngine());

        System.setProperty("Id", "1001");
        assertEquals(15, config.getInt("rowsPerPage"));

        System.setProperty("Id", "1002");
        assertEquals(25, config.getInt("rowsPerPage"));

        System.setProperty("Id", "1003");
        assertEquals(35, config.getInt("rowsPerPage"));
    }

    /**
     * Tests a read operation if the wrapped configuration does not implement FileBased.
     */
    @Test
    public void testReadNotFileBased() {
        final HierarchicalConfiguration<ImmutableNode> hc = new BaseHierarchicalConfiguration();
        final PatternSubtreeConfigurationWrapper config = new PatternSubtreeConfigurationWrapper(hc, PATTERN);
        final FileHandler fileHandler = new FileHandler(config);
        assertThrows(ConfigurationException.class, () -> fileHandler.load(CONFIG_FILE));
    }

    /**
     * Tests a write operation if the wrapped configuration does not implement FileBased.
     */
    @Test
    public void testSaveNotFileBased() {
        final HierarchicalConfiguration<ImmutableNode> hc = new BaseHierarchicalConfiguration();
        final PatternSubtreeConfigurationWrapper config = new PatternSubtreeConfigurationWrapper(hc, PATTERN);
        final FileHandler fileHandler = new FileHandler(config);
        final StringWriter writer = new StringWriter();
        assertThrows(ConfigurationException.class, () -> fileHandler.save(writer));
    }
}
