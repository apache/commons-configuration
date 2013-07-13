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
package org.apache.commons.configuration.builder.fluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.configuration.ListDelimiterHandler;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.builder.BasicBuilderParameters;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.CombinedBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.MultiFileBuilderParametersImpl;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code Parameters}.
 *
 * @version $Id$
 */
public class TestParameters
{
    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        listHandler = EasyMock.createMock(ListDelimiterHandler.class);
    }

    /**
     * Tests whether a basic parameters object can be created.
     */
    @Test
    public void testBasic()
    {
        BasicBuilderParameters basic = Parameters.basic();
        assertNotNull("No result object", basic);
    }

    /**
     * Checks whether the given parameters map contains the standard values for
     * basic properties.
     *
     * @param map the map to be tested
     */
    private static void checkBasicProperties(Map<String, Object> map)
    {
        assertEquals("Wrong delimiter handler", listHandler,
                map.get("listDelimiterHandler"));
        assertEquals("Wrong exception flag value", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether a file-based parameters object can be created.
     */
    @Test
    public void testFileBased()
    {
        Map<String, Object> map =
                Parameters.fileBased().setThrowExceptionOnMissing(true)
                        .setEncoding("UTF-8").setListDelimiterHandler(listHandler)
                        .setFileName("test.xml").getParameters();
        FileBasedBuilderParametersImpl fbparams =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong file name", "test.xml", fbparams.getFileHandler()
                .getFileName());
        assertEquals("Wrong encoding", "UTF-8", fbparams.getFileHandler()
                .getEncoding());
        checkBasicProperties(map);
    }

    /**
     * Tests whether the proxy parameters object can deal with methods inherited
     * from Object.
     */
    @Test
    public void testProxyObjectMethods()
    {
        FileBasedBuilderParameters params = Parameters.fileBased();
        String s = params.toString();
        assertTrue(
                "Wrong string: " + s,
                s.indexOf(FileBasedBuilderParametersImpl.class.getSimpleName()) >= 0);
        assertTrue("No hash code", params.hashCode() != 0);
    }

    /**
     * Tests whether a combined parameters object can be created.
     */
    @Test
    public void testCombined()
    {
        Map<String, Object> map =
                Parameters.combined().setThrowExceptionOnMissing(true)
                        .setBasePath("test").setListDelimiterHandler(listHandler)
                        .getParameters();
        CombinedBuilderParametersImpl cparams =
                CombinedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong base path", "test", cparams.getBasePath());
        checkBasicProperties(map);
    }

    /**
     * Tests whether a JNDI parameters object can be created.
     */
    @Test
    public void testJndi()
    {
        Map<String, Object> map =
                Parameters.jndi().setThrowExceptionOnMissing(true)
                        .setPrefix("test").setListDelimiterHandler(listHandler)
                        .getParameters();
        assertEquals("Wrong prefix", "test", map.get("prefix"));
        checkBasicProperties(map);
    }

    /**
     * Tests whether a parameters object for a hierarchical configuration can be
     * created.
     */
    @Test
    public void testHierarchical()
    {
        ExpressionEngine engine = EasyMock.createMock(ExpressionEngine.class);
        Map<String, Object> map =
                Parameters.hierarchical().setThrowExceptionOnMissing(true)
                        .setExpressionEngine(engine).setFileName("test.xml")
                        .setListDelimiterHandler(listHandler).getParameters();
        checkBasicProperties(map);
        FileBasedBuilderParametersImpl fbp =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong file name", "test.xml", fbp.getFileHandler()
                .getFileName());
        assertEquals("Wrong expression engine", engine,
                map.get("expressionEngine"));
    }

    /**
     * Tests whether a parameters object for an XML configuration can be
     * created.
     */
    @Test
    public void testXml()
    {
        ExpressionEngine engine = EasyMock.createMock(ExpressionEngine.class);
        Map<String, Object> map =
                Parameters.xml().setThrowExceptionOnMissing(true)
                        .setFileName("test.xml").setValidating(true)
                        .setExpressionEngine(engine).setListDelimiterHandler(listHandler)
                        .setSchemaValidation(true).getParameters();
        checkBasicProperties(map);
        FileBasedBuilderParametersImpl fbp =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong file name", "test.xml", fbp.getFileHandler()
                .getFileName());
        assertEquals("Wrong validation flag", Boolean.TRUE,
                map.get("validating"));
        assertEquals("Wrong schema flag", Boolean.TRUE,
                map.get("schemaValidation"));
        assertEquals("Wrong expression engine", engine,
                map.get("expressionEngine"));
    }

    /**
     * Tests whether a parameters object for a properties configuration can be
     * created.
     */
    @Test
    public void testProperties()
    {
        PropertiesConfiguration.IOFactory factory =
                EasyMock.createMock(PropertiesConfiguration.IOFactory.class);
        Map<String, Object> map =
                Parameters.properties().setThrowExceptionOnMissing(true)
                        .setFileName("test.properties").setIOFactory(factory)
                        .setListDelimiterHandler(listHandler).setIncludesAllowed(false)
                        .getParameters();
        checkBasicProperties(map);
        FileBasedBuilderParametersImpl fbp =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong file name", "test.properties", fbp.getFileHandler()
                .getFileName());
        assertEquals("Wrong includes flag", Boolean.FALSE,
                map.get("includesAllowed"));
        assertSame("Wrong factory", factory, map.get("iOFactory"));
    }

    /**
     * Tests whether a {@code MultiFileBuilderParameters} object can be created.
     */
    @Test
    public void testMultiFile()
    {
        BuilderParameters bp = EasyMock.createMock(BuilderParameters.class);
        String pattern = "a pattern";
        Map<String, Object> map =
                Parameters.multiFile().setThrowExceptionOnMissing(true)
                        .setFilePattern(pattern).setListDelimiterHandler(listHandler)
                        .setManagedBuilderParameters(bp).getParameters();
        checkBasicProperties(map);
        MultiFileBuilderParametersImpl params =
                MultiFileBuilderParametersImpl.fromParameters(map);
        assertSame("Wrong builder parameters", bp,
                params.getManagedBuilderParameters());
        assertEquals("Wrong pattern", pattern, params.getFilePattern());
    }

    /**
     * Tests whether a parameters object for a database configuration can be
     * created.
     */
    @Test
    public void testDatabase()
    {
        Map<String, Object> map =
                Parameters.database().setThrowExceptionOnMissing(true)
                        .setAutoCommit(true).setTable("table")
                        .setListDelimiterHandler(listHandler).setKeyColumn("keyColumn")
                        .getParameters();
        checkBasicProperties(map);
        assertEquals("Wrong table name", "table", map.get("table"));
        assertEquals("Wrong key column name", "keyColumn", map.get("keyColumn"));
        assertEquals("Wrong auto commit flag", Boolean.TRUE,
                map.get("autoCommit"));
    }
}
