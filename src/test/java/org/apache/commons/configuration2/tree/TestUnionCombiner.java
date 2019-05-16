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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

/**
 * Test class for UnionCombiner.
 *
 */
public class TestUnionCombiner extends AbstractCombinerTest
{
    /**
     * Creates the combiner.
     *
     * @return the combiner
     */
    @Override
    protected NodeCombiner createCombiner()
    {
        return new UnionCombiner();
    }

    /**
     * Tests combination of simple values (no lists).
     */
    @Test
    public void testSimpleValues() throws ConfigurationException
    {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Too few bgcolors", 1, config.getMaxIndex("gui.bgcolor"));
        assertEquals("Wrong first color", "green", config
                .getString("gui.bgcolor(0)"));
        assertEquals("Wrong second color", "black", config
                .getString("gui.bgcolor(1)"));
        assertEquals("Wrong number of selcolors", 0, config
                .getMaxIndex("gui.selcolor"));
        assertEquals("Wrong selcolor", "yellow", config
                .getString("gui.selcolor"));
    }

    /**
     * Tests combinations of elements with attributes.
     */
    @Test
    public void testSimpleValuesWithAttributes() throws ConfigurationException
    {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Too few level elements", 1, config
                .getMaxIndex("gui.level"));
        assertEquals("Wrong value of first element", 1, config
                .getInt("gui.level(0)"));
        assertEquals("Wrong value of second element", 4, config
                .getInt("gui.level(1)"));
        assertEquals("Wrong value of first attribute", 2, config
                .getInt("gui.level(0)[@default]"));
        assertFalse("Found wrong attribute", config
                .containsKey("gui.level(0)[@min]"));
        assertEquals("Wrong value of second attribute", 1, config
                .getInt("gui.level(1)[@min]"));
    }

    /**
     * Tests combination of attributes.
     */
    @Test
    public void testAttributes() throws ConfigurationException
    {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong number of attributes", 0, config
                .getMaxIndex("database.tables.table(0)[@id]"));
        assertEquals("Wrong value of attribute", 1, config
                .getInt("database.tables.table(0)[@id](0)"));
    }

    /**
     * Tests combination of lists.
     */
    @Test
    public void testLists() throws ConfigurationException
    {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Too few list elements", 2, config
                .getMaxIndex("net.service.url"));
        assertEquals("Wrong first service", "http://service1.org", config
                .getString("net.service.url(0)"));
        assertEquals("Wrong second service", "http://service2.org", config
                .getString("net.service.url(1)"));
        assertEquals("Wrong service attribute", 2, config
                .getInt("net.service.url(2)[@type]"));
        assertEquals("Wrong number of server elements", 3, config
                .getMaxIndex("net.server.url"));
    }

    /**
     * Tests combining a list of tables. Per default the table elements will be
     * combined. But if they are defined as list elements, the resulting tree
     * should contain two table nodes.
     */
    @Test
    public void testTableList() throws ConfigurationException
    {
        combiner.addListNode("table");
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong name of first table", "documents", config
                .getString("database.tables.table(0).name"));
        assertEquals("Wrong id of first table", 1, config
                .getInt("database.tables.table(0)[@id]"));
        assertEquals("Wrong name of second table", "tasks", config
                .getString("database.tables.table(1).name"));
        assertEquals("Wrong id of second table", 2, config
                .getInt("database.tables.table(1)[@id]"));
    }
}
