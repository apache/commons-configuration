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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.junit.jupiter.api.Test;

/**
 * Test class for MergeCombiner.
 *
 */
public class TestMergeCombiner extends AbstractCombinerTest {
    /**
     * Helper method for checking the combined table structure.
     *
     * @param config the config
     * @return the node for the table element
     */
    private ImmutableNode checkTable(final HierarchicalConfiguration<ImmutableNode> config) {
        assertEquals(1, config.getMaxIndex("database.tables.table"), "Wrong number of tables");
        final HierarchicalConfiguration<ImmutableNode> c = config.configurationAt("database.tables.table(0)");
        assertEquals("documents", c.getString("name"), "Wrong table name");
        assertEquals(2, c.getMaxIndex("fields.field.name"), "Wrong number of fields");
        assertEquals("docname", c.getString("fields.field(1).name"), "Wrong field");

        final NodeHandler<ImmutableNode> nodeHandler = config.getNodeModel().getNodeHandler();
        final List<QueryResult<ImmutableNode>> nds = config.getExpressionEngine().query(nodeHandler.getRootNode(), "database.tables.table", nodeHandler);
        assertFalse(nds.isEmpty(), "No node found");
        assertFalse(nds.get(0).isAttributeResult(), "Not a node result");
        return nds.get(0).getNode();
    }

    /**
     * Creates the combiner.
     *
     * @return the combiner
     */
    @Override
    protected NodeCombiner createCombiner() {
        return new MergeCombiner();
    }

    /**
     * Tests combination of attributes.
     */
    @Test
    public void testAttributes() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(1, config.getInt("gui.level[@min]"), "Wrong value of min attribute");
        assertEquals(2, config.getInt("gui.level[@default]"), "Wrong value of default attribute");
        assertEquals(0, config.getMaxIndex("database.tables.table(0)[@id]"), "Wrong number of id attributes");
        assertEquals(1, config.getInt("database.tables.table(0)[@id]"), "Wrong value of table id");
    }

    /**
     * Tests the combination of the table structure. With the merge combiner both table 1 and table 2 should be present.
     */
    @Test
    public void testCombinedTable() throws ConfigurationException {
        checkTable(createCombinedConfiguration());
    }

    /**
     * Tests if a list from the first node structure overrides a list in the second structure.
     */
    @Test
    public void testListFromFirstStructure() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(0, config.getMaxIndex("net.service.url"), "Wrong number of services");
        assertEquals("http://service1.org", config.getString("net.service.url"), "Wrong service");
        assertFalse(config.containsKey("net.service.url[@type]"), "Type attribute available");
    }

    /**
     * Tests if a list from the second structure is added if it is not defined in the first structure.
     */
    @Test
    public void testListFromSecondStructure() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(3, config.getMaxIndex("net.server.url"), "Wrong number of servers");
        assertEquals("http://testsvr.com", config.getString("net.server.url(2)"), "Wrong server");
    }

    @Test
    public void testMerge() throws ConfigurationException {
        // combiner.setDebugStream(System.out);
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        config.setExpressionEngine(new XPathExpressionEngine());
        assertEquals(3, config.getMaxIndex("Channels/Channel"), "Wrong number of Channels");
        assertEquals("My Channel", config.getString("Channels/Channel[@id='1']/Name"), "Bad Channel 1 Name");
        assertEquals("half", config.getString("Channels/Channel[@id='1']/@type"), "Bad Channel Type");
        assertEquals("Channel 2", config.getString("Channels/Channel[@id='2']/Name"), "Bad Channel 2 Name");
        assertEquals("full", config.getString("Channels/Channel[@id='2']/@type"), "Bad Channel Type");
        assertEquals("test 1 data", config.getString("Channels/Channel[@id='1']/ChannelData"), "Bad Channel Data");
        assertEquals("test 2 data", config.getString("Channels/Channel[@id='2']/ChannelData"), "Bad Channel Data");
        assertEquals("more test 2 data", config.getString("Channels/Channel[@id='2']/MoreChannelData"), "Bad Channel Data");

    }

    /**
     * Tests whether property values are correctly overridden.
     */
    @Test
    public void testOverrideValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals("Admin", config.getString("base.services.security.login.user"), "Wrong user");
        assertEquals("default", config.getString("base.services.security.login.user[@type]"), "Wrong user type");
        assertNull(config.getString("base.services.security.login.passwd"), "Wrong password");
        assertEquals("secret", config.getString("base.services.security.login.passwd[@type]"), "Wrong password type");
    }

    /**
     * Tests combination of simple elements.
     */
    @Test
    public void testSimpleValues() throws ConfigurationException {
        final BaseHierarchicalConfiguration config = createCombinedConfiguration();
        assertEquals(0, config.getMaxIndex("gui.bgcolor"), "Wrong number of bgcolors");
        assertEquals("green", config.getString("gui.bgcolor"), "Wrong bgcolor");
        assertEquals("yellow", config.getString("gui.selcolor"), "Wrong selcolor");
        assertEquals("blue", config.getString("gui.fgcolor"), "Wrong fgcolor");
        assertEquals(1, config.getInt("gui.level"), "Wrong level");
    }
}
