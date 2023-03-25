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
package org.apache.commons.configuration2.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * Test class for UnionCombiner.
 */
public class TestUnionCombiner extends AbstractCombinerTest {
    /**
     * Creates the combiner.
     *
     * @return the combiner
     */
    @Override
    protected NodeCombiner createCombiner() {
        return new UnionCombiner();
    }

    /**
     * Tests combination of attributes.
     */
    @Test
    public void testAttributes() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(0, config.getMaxIndex("database.tables.table(0)[@id]"));
        assertEquals(1, config.getInt("database.tables.table(0)[@id](0)"));
    }

    /**
     * Tests combination of lists.
     */
    @Test
    public void testLists() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(2, config.getMaxIndex("net.service.url"));
        assertEquals("http://service1.org", config.getString("net.service.url(0)"));
        assertEquals("http://service2.org", config.getString("net.service.url(1)"));
        assertEquals(2, config.getInt("net.service.url(2)[@type]"));
        assertEquals(3, config.getMaxIndex("net.server.url"));
    }

    /**
     * Tests combination of simple values (no lists).
     */
    @Test
    public void testSimpleValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getMaxIndex("gui.bgcolor"));
        assertEquals("green", config.getString("gui.bgcolor(0)"));
        assertEquals("black", config.getString("gui.bgcolor(1)"));
        assertEquals(0, config.getMaxIndex("gui.selcolor"));
        assertEquals("yellow", config.getString("gui.selcolor"));
    }

    /**
     * Tests combinations of elements with attributes.
     */
    @Test
    public void testSimpleValuesWithAttributes() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getMaxIndex("gui.level"));
        assertEquals(1, config.getInt("gui.level(0)"));
        assertEquals(4, config.getInt("gui.level(1)"));
        assertEquals(2, config.getInt("gui.level(0)[@default]"));
        assertFalse(config.containsKey("gui.level(0)[@min]"));
        assertEquals(1, config.getInt("gui.level(1)[@min]"));
    }

    /**
     * Tests combining a list of tables. Per default the table elements will be combined. But if they are defined as list
     * elements, the resulting tree should contain two table nodes.
     */
    @Test
    public void testTableList() throws ConfigurationException {
        combiner.addListNode("table");
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("documents", config.getString("database.tables.table(0).name"));
        assertEquals(1, config.getInt("database.tables.table(0)[@id]"));
        assertEquals("tasks", config.getString("database.tables.table(1).name"));
        assertEquals(2, config.getInt("database.tables.table(1)[@id]"));
    }
}
