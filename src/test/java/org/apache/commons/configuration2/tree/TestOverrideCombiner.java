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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * Test class for OverrideCombiner.
 *
 */
public class TestOverrideCombiner extends AbstractCombinerTest {
    /**
     * Helper method for checking the combined table structure.
     *
     * @param config the config
     * @return the node for the table element
     */
    private ImmutableNode checkTable(final BaseHierarchicalConfiguration config) {
        assertEquals(0, config.getMaxIndex("database.tables.table"));
        final HierarchicalConfiguration<ImmutableNode> c = config.configurationAt("database.tables.table");
        assertEquals("documents", c.getString("name"));
        assertEquals(2, c.getMaxIndex("fields.field.name"));
        assertEquals("docname", c.getString("fields.field(1).name"));

        final NodeHandler<ImmutableNode> nodeHandler = config.getNodeModel().getNodeHandler();
        final List<QueryResult<ImmutableNode>> nds = config.getExpressionEngine().query(nodeHandler.getRootNode(), "database.tables.table", nodeHandler);
        assertFalse(nds.isEmpty());
        assertFalse(nds.get(0).isAttributeResult());
        return nds.get(0).getNode();
    }

    /**
     * Creates the combiner.
     *
     * @return the combiner
     */
    @Override
    protected NodeCombiner createCombiner() {
        return new OverrideCombiner();
    }

    /**
     * Tests combination of attributes.
     */
    @Test
    public void testAttributes() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getInt("gui.level[@min]"));
        assertEquals(2, config.getInt("gui.level[@default]"));
        assertEquals(0, config.getMaxIndex("database.tables.table(0)[@id]"));
        assertEquals(1, config.getInt("database.tables.table(0)[@id]"));
    }

    /**
     * Tests the combination of the table structure if the table node is declared as a list node. In this case the first
     * table structure completely overrides the second and will be directly added to the resulting structure.
     */
    @Test
    public void testCombinedTableList() throws ConfigurationException {
        combiner.addListNode("table");
        checkTable(createCombinedConfiguration());
    }

    /**
     * Tests the combination of the table structure. Because the table node is not declared as a list node the structures
     * will be combined. But this won't make any difference because the values in the first table override the values in the
     * second table.
     */
    @Test
    public void testCombinedTableNoList() throws ConfigurationException {
        checkTable(createCombinedConfiguration());
    }

    /**
     * Tests a combine operation of non-hierarchical properties. This test is related to CONFIGURATION-604.
     */
    @Test
    public void testCombineProperties() {
        final PropertiesConfiguration c1 = new PropertiesConfiguration();
        c1.addProperty("x.y.simpleCase", false);
        c1.addProperty("x.y.between", false);
        c1.addProperty("x.y.isDistinctFrom", false);
        c1.addProperty("x.y", false);
        final PropertiesConfiguration c2 = new PropertiesConfiguration();
        c2.addProperty("x.y", true);
        c2.addProperty("x.y.between", true);
        c2.addProperty("x.y.comparison", true);
        c2.addProperty("x.y.in", true);
        c2.addProperty("x.y.isDistinctFrom", true);
        c2.addProperty("x.y.simpleCase", true);

        final CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());
        config.addConfiguration(c1);
        config.addConfiguration(c2);
        assertFalse(config.getBoolean("x.y"));
        assertFalse(config.getBoolean("x.y.between"));
        assertFalse(config.getBoolean("x.y.isDistinctFrom"));
        assertFalse(config.getBoolean("x.y.simpleCase"));
        assertTrue(config.getBoolean("x.y.in"));
        assertTrue(config.getBoolean("x.y.comparison"));
        assertEquals(6, config.size());
    }

    /**
     * Tests if a list from the first node structure overrides a list in the second structure.
     */
    @Test
    public void testListFromFirstStructure() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(0, config.getMaxIndex("net.service.url"));
        assertEquals("http://service1.org", config.getString("net.service.url"));
        assertFalse(config.containsKey("net.service.url[@type]"));
    }

    /**
     * Tests if a list from the second structure is added if it is not defined in the first structure.
     */
    @Test
    public void testListFromSecondStructure() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(3, config.getMaxIndex("net.server.url"));
        assertEquals("http://testsvr.com", config.getString("net.server.url(2)"));
    }

    /**
     * Tests whether property values are correctly overridden.
     */
    @Test
    public void testOverrideValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Admin", config.getString("base.services.security.login.user"));
        assertEquals("default", config.getString("base.services.security.login.user[@type]"));
        assertEquals("BeamMeUp", config.getString("base.services.security.login.passwd"));
        assertEquals("secret", config.getString("base.services.security.login.passwd[@type]"));
    }

    /**
     * Tests combination of simple elements.
     */
    @Test
    public void testSimpleValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(0, config.getMaxIndex("gui.bgcolor"));
        assertEquals("green", config.getString("gui.bgcolor"));
        assertEquals("yellow", config.getString("gui.selcolor"));
        assertEquals("blue", config.getString("gui.fgcolor"));
        assertEquals(1, config.getInt("gui.level"));
    }
}
