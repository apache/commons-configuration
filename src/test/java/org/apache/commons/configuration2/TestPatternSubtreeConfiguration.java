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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple MultiConfigurationTest.
 *
 */
public class TestPatternSubtreeConfiguration
{
    private static File CONFIG_FILE = ConfigurationAssert.getTestFile("testPatternSubtreeConfig.xml");
    private static String PATTERN = "BusinessClient[@name='${sys:Id}']";
    private XMLConfiguration conf;

    @Before
    public void setUp() throws Exception
    {
        conf = new XMLConfiguration();
        new FileHandler(conf).load(CONFIG_FILE);
    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testMultiConfiguration()
    {
        final PatternSubtreeConfigurationWrapper config = new PatternSubtreeConfigurationWrapper(this.conf, PATTERN);
        config.setExpressionEngine(new XPathExpressionEngine());

        System.setProperty("Id", "1001");
        assertTrue(config.getInt("rowsPerPage") == 15);

        System.setProperty("Id", "1002");
        assertTrue(config.getInt("rowsPerPage") == 25);

        System.setProperty("Id", "1003");
        assertTrue(config.getInt("rowsPerPage") == 35);
    }

    /**
     * Tests a read operation if the wrapped configuration does not implement
     * FileBased.
     */
    @Test(expected = ConfigurationException.class)
    public void testReadNotFileBased() throws ConfigurationException
    {
        final HierarchicalConfiguration<ImmutableNode> hc = new BaseHierarchicalConfiguration();
        final PatternSubtreeConfigurationWrapper config =
                new PatternSubtreeConfigurationWrapper(hc, PATTERN);
        new FileHandler(config).load(CONFIG_FILE);
    }

    /**
     * Tests a write operation if the wrapped configuration does not implement
     * FileBased.
     */
    @Test(expected = ConfigurationException.class)
    public void testSaveNotFileBased() throws ConfigurationException
    {
        final HierarchicalConfiguration<ImmutableNode> hc = new BaseHierarchicalConfiguration();
        final PatternSubtreeConfigurationWrapper config =
                new PatternSubtreeConfigurationWrapper(hc, PATTERN);
        new FileHandler(config).save(new StringWriter());
    }
}
