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
 *
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
        assertEquals(0, config.getMaxIndex("database.tables.table(0)[@id]"), "Wrong number of attributes");
        assertEquals(1, config.getInt("database.tables.table(0)[@id](0)"), "Wrong value of attribute");
    }

    /**
     * Tests combination of lists.
     */
    @Test
    public void testLists() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(2, config.getMaxIndex("net.service.url"), "Too few list elements");
        assertEquals("http://service1.org", config.getString("net.service.url(0)"), "Wrong first service");
        assertEquals("http://service2.org", config.getString("net.service.url(1)"), "Wrong second service");
        assertEquals(2, config.getInt("net.service.url(2)[@type]"), "Wrong service attribute");
        assertEquals(3, config.getMaxIndex("net.server.url"), "Wrong number of server elements");
    }

    /**
     * Tests combination of simple values (no lists).
     */
    @Test
    public void testSimpleValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getMaxIndex("gui.bgcolor"), "Too few bgcolors");
        assertEquals("green", config.getString("gui.bgcolor(0)"), "Wrong first color");
        assertEquals("black", config.getString("gui.bgcolor(1)"), "Wrong second color");
        assertEquals(0, config.getMaxIndex("gui.selcolor"), "Wrong number of selcolors");
        assertEquals("yellow", config.getString("gui.selcolor"), "Wrong selcolor");
    }

    /**
     * Tests combinations of elements with attributes.
     */
    @Test
    public void testSimpleValuesWithAttributes() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getMaxIndex("gui.level"), "Too few level elements");
        assertEquals(1, config.getInt("gui.level(0)"), "Wrong value of first element");
        assertEquals(4, config.getInt("gui.level(1)"), "Wrong value of second element");
        assertEquals(2, config.getInt("gui.level(0)[@default]"), "Wrong value of first attribute");
        assertFalse(config.containsKey("gui.level(0)[@min]"), "Found wrong attribute");
        assertEquals(1, config.getInt("gui.level(1)[@min]"), "Wrong value of second attribute");
    }

    /**
     * Tests combining a list of tables. Per default the table elements will be combined. But if they are defined as list
     * elements, the resulting tree should contain two table nodes.
     */
    @Test
    public void testTableList() throws ConfigurationException {
        combiner.addListNode("table");
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("documents", config.getString("database.tables.table(0).name"), "Wrong name of first table");
        assertEquals(1, config.getInt("database.tables.table(0)[@id]"), "Wrong id of first table");
        assertEquals("tasks", config.getString("database.tables.table(1).name"), "Wrong name of second table");
        assertEquals(2, config.getInt("database.tables.table(1)[@id]"), "Wrong id of second table");
    }
}
