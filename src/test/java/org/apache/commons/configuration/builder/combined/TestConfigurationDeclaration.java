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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration.BaseHierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.junit.Test;

/**
 * Test class for {@code ConfigurationDeclaration}.
 *
 * @version $Id$
 */
public class TestConfigurationDeclaration
{
    /**
     * Creates a default test instance.
     *
     * @param conf the configuration to be used (may be <b>null</b> for a
     *        default one)
     * @return the test object
     */
    private static ConfigurationDeclaration createDeclaration(
            HierarchicalConfiguration conf)
    {
        HierarchicalConfiguration config =
                (conf != null) ? conf : new BaseHierarchicalConfiguration();
        return new ConfigurationDeclaration(null, config);
    }

    /**
     * Tests the isReservedNode() method of ConfigurationDeclaration.
     */
    @Test
    public void testConfigurationDeclarationIsReserved()
    {
        ConfigurationDeclaration decl = createDeclaration(null);
        DefaultConfigurationNode parent = new DefaultConfigurationNode();
        DefaultConfigurationNode nd = new DefaultConfigurationNode("at");
        parent.addAttribute(nd);
        assertTrue("Attribute at not recognized", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("optional");
        parent.addAttribute(nd);
        assertTrue("Attribute optional not recognized", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("config-class");
        parent.addAttribute(nd);
        assertTrue("Inherited attribute not recognized",
                decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("different");
        parent.addAttribute(nd);
        assertFalse("Wrong reserved attribute", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("at");
        parent.addChild(nd);
        assertFalse("Node type not evaluated", decl.isReservedNode(nd));
    }

    /**
     * Tests if the at attribute is correctly detected as reserved attribute.
     */
    @Test
    public void testConfigurationDeclarationIsReservedAt()
    {
        checkOldReservedAttribute("at");
    }

    /**
     * Tests if the optional attribute is correctly detected as reserved
     * attribute.
     */
    @Test
    public void testConfigurationDeclarationIsReservedOptional()
    {
        checkOldReservedAttribute("optional");
    }

    /**
     * Tests if special reserved attributes are recognized by the
     * isReservedNode() method. For compatibility reasons the attributes "at"
     * and "optional" are also treated as reserved attributes, but only if there
     * are no corresponding attributes with the "config-" prefix.
     *
     * @param name the attribute name
     */
    private void checkOldReservedAttribute(String name)
    {
        ConfigurationDeclaration decl = createDeclaration(null);
        DefaultConfigurationNode parent = new DefaultConfigurationNode();
        DefaultConfigurationNode nd =
                new DefaultConfigurationNode("config-" + name);
        parent.addAttribute(nd);
        assertTrue("config-" + name + " attribute not recognized",
                decl.isReservedNode(nd));
        DefaultConfigurationNode nd2 = new DefaultConfigurationNode(name);
        parent.addAttribute(nd2);
        assertFalse(name + " is reserved though config- exists",
                decl.isReservedNode(nd2));
        assertTrue("config- attribute not recognized when " + name + " exists",
                decl.isReservedNode(nd));
    }

    /**
     * Tests access to certain reserved attributes of a
     * ConfigurationDeclaration.
     */
    @Test
    public void testConfigurationDeclarationGetAttributes()
    {
        HierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty("xml.fileName", "test.xml");
        ConfigurationDeclaration decl =
                createDeclaration(config.configurationAt("xml"));
        assertNull("Found an at attribute", decl.getAt());
        assertFalse("Found an optional attribute", decl.isOptional());
        config.addProperty("xml[@config-at]", "test1");
        assertEquals("Wrong value of at attribute", "test1", decl.getAt());
        config.addProperty("xml[@at]", "test2");
        assertEquals("Wrong value of config-at attribute", "test1",
                decl.getAt());
        config.clearProperty("xml[@config-at]");
        assertEquals("Old at attribute not detected", "test2", decl.getAt());
        config.addProperty("xml[@config-optional]", "true");
        assertTrue("Wrong value of optional attribute", decl.isOptional());
        config.addProperty("xml[@optional]", "false");
        assertTrue("Wrong value of config-optional attribute",
                decl.isOptional());
        config.clearProperty("xml[@config-optional]");
        config.setProperty("xml[@optional]", Boolean.TRUE);
        assertTrue("Old optional attribute not detected", decl.isOptional());
    }

    /**
     * Tests whether an invalid value of an optional attribute is detected.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testConfigurationDeclarationOptionalAttributeInvalid()
    {
        HierarchicalConfiguration factory = new BaseHierarchicalConfiguration();
        factory.addProperty("xml.fileName", "test.xml");
        ConfigurationDeclaration decl =
                createDeclaration(factory.configurationAt("xml"));
        factory.setProperty("xml[@optional]", "invalid value");
        decl.isOptional();
    }
}
