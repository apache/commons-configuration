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
package org.apache.commons.configuration.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.Test;

/**
 * Test class for OverrideCombiner.
 *
 * @version $Id$
 */
public class TestOverrideCombiner extends AbstractCombinerTest
{
    /**
     * Creates the combiner.
     *
     * @return the combiner
     */
    @Override
    protected NodeCombiner createCombiner()
    {
        return new OverrideCombiner();
    }

    /**
     * Tests combination of simple elements.
     */
    @Test
    public void testSimpleValues() throws ConfigurationException
    {
        HierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong number of bgcolors", 0, config
                .getMaxIndex("gui.bgcolor"));
        assertEquals("Wrong bgcolor", "green", config.getString("gui.bgcolor"));
        assertEquals("Wrong selcolor", "yellow", config
                .getString("gui.selcolor"));
        assertEquals("Wrong fgcolor", "blue", config.getString("gui.fgcolor"));
        assertEquals("Wrong level", 1, config.getInt("gui.level"));
    }

    /**
     * Tests combination of attributes.
     */
    @Test
    public void testAttributes() throws ConfigurationException
    {
        HierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong value of min attribute", 1, config
                .getInt("gui.level[@min]"));
        assertEquals("Wrong value of default attribute", 2, config
                .getInt("gui.level[@default]"));
        assertEquals("Wrong number of id attributes", 0, config
                .getMaxIndex("database.tables.table(0)[@id]"));
        assertEquals("Wrong value of table id", 1, config
                .getInt("database.tables.table(0)[@id]"));
    }

    /**
     * Tests whether property values are correctly overridden.
     */
    @Test
    public void testOverrideValues() throws ConfigurationException
    {
        HierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong user", "Admin", config
                .getString("base.services.security.login.user"));
        assertEquals("Wrong user type", "default", config
                .getString("base.services.security.login.user[@type]"));
        assertEquals("Wrong password", "BeamMeUp", config
                .getString("base.services.security.login.passwd"));
        assertEquals("Wrong password type", "secret", config
                .getString("base.services.security.login.passwd[@type]"));
    }

    /**
     * Tests if a list from the first node structure overrides a list in the
     * second structure.
     */
    @Test
    public void testListFromFirstStructure() throws ConfigurationException
    {
        HierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong number of services", 0, config
                .getMaxIndex("net.service.url"));
        assertEquals("Wrong service", "http://service1.org", config
                .getString("net.service.url"));
        assertFalse("Type attribute available", config
                .containsKey("net.service.url[@type]"));
    }

    /**
     * Tests if a list from the second structure is added if it is not defined
     * in the first structure.
     */
    @Test
    public void testListFromSecondStructure() throws ConfigurationException
    {
        HierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Wrong number of servers", 3, config
                .getMaxIndex("net.server.url"));
        assertEquals("Wrong server", "http://testsvr.com", config
                .getString("net.server.url(2)"));
    }

    /**
     * Tests the combination of the table structure. Because the table node is
     * not declared as a list node the structures will be combined. But this
     * won't make any difference because the values in the first table override
     * the values in the second table. Only the node for the table element will
     * be a ViewNode.
     */
    @Test
    public void testCombinedTableNoList() throws ConfigurationException
    {
        ConfigurationNode tabNode = checkTable(createCombinedConfiguration());
        assertTrue("Node is not a view node", tabNode instanceof ViewNode);
    }

    /**
     * Tests the combination of the table structure when the table node is
     * declared as a list node. In this case the first table structure
     * completely overrides the second and will be directly added to the
     * resulting structure.
     */
    @Test
    public void testCombinedTableList() throws ConfigurationException
    {
        combiner.addListNode("table");
        ConfigurationNode tabNode = checkTable(createCombinedConfiguration());
        assertFalse("Node is a view node", tabNode instanceof ViewNode);
    }

    /**
     * Helper method for checking the combined table structure.
     *
     * @param config the config
     * @return the node for the table element
     */
    private ConfigurationNode checkTable(HierarchicalConfiguration config)
    {
        assertEquals("Wrong number of tables", 0, config
                .getMaxIndex("database.tables.table"));
        HierarchicalConfiguration c = config
                .configurationAt("database.tables.table");
        assertEquals("Wrong table name", "documents", c.getString("name"));
        assertEquals("Wrong number of fields", 2, c
                .getMaxIndex("fields.field.name"));
        assertEquals("Wrong field", "docname", c
                .getString("fields.field(1).name"));

        List<ConfigurationNode> nds = config.getExpressionEngine().query(config.getRoot(),
                "database.tables.table");
        assertFalse("No node found", nds.isEmpty());
        return nds.get(0);
    }
}
