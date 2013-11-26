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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.builder.BasicBuilderParameters;
import org.apache.commons.configuration.builder.BasicBuilderProperties;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.DefaultParametersHandler;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.CombinedBuilderParametersImpl;
import org.apache.commons.configuration.builder.combined.MultiFileBuilderParametersImpl;
import org.apache.commons.configuration.convert.ListDelimiterHandler;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code Parameters}.
 *
 * @version $Id$
 */
public class TestParameters
{
    /** A default encoding. */
    private static final String DEF_ENCODING = "UTF-8";

    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    /** The parameters object to be tested. */
    private Parameters parameters;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        listHandler = EasyMock.createMock(ListDelimiterHandler.class);
    }

    @Before
    public void setUp() throws Exception
    {
        parameters = new Parameters();
    }

    /**
     * Tests whether a basic parameters object can be created.
     */
    @Test
    public void testBasic()
    {
        BasicBuilderParameters basic = parameters.basic();
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
                parameters.fileBased().setThrowExceptionOnMissing(true)
                        .setEncoding(DEF_ENCODING).setListDelimiterHandler(listHandler)
                        .setFileName("test.xml").getParameters();
        FileBasedBuilderParametersImpl fbparams =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong file name", "test.xml", fbparams.getFileHandler()
                .getFileName());
        assertEquals("Wrong encoding", DEF_ENCODING, fbparams.getFileHandler()
                .getEncoding());
        checkBasicProperties(map);
    }

    /**
     * Helper method for testing whether the given object is an instance of the
     * provided class.
     *
     * @param obj the object to be checked
     * @param cls the class
     */
    private static void checkInstanceOf(Object obj, Class<?> cls)
    {
        assertTrue(obj + " is not an instance of " + cls, cls.isInstance(obj));
    }

    /**
     * Checks whether a given parameters object implements all the specified
     * interfaces.
     *
     * @param params the parameters object to check
     * @param ifcClasses the interface classes to be implemented
     */
    private static void checkInheritance(Object params, Class<?>... ifcClasses)
    {
        checkInstanceOf(params, BasicBuilderProperties.class);
        for (Class<?> c : ifcClasses)
        {
            checkInstanceOf(params, c);
        }
    }

    /**
     * Tests the inheritance structure of a fileBased parameters object.
     */
    @Test
    public void testFileBasedInheritance()
    {
        checkInheritance(parameters.fileBased());
    }

    /**
     * Tests whether the proxy parameters object can deal with methods inherited
     * from Object.
     */
    @Test
    public void testProxyObjectMethods()
    {
        FileBasedBuilderParameters params = parameters.fileBased();
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
                parameters.combined().setThrowExceptionOnMissing(true)
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
                parameters.jndi().setThrowExceptionOnMissing(true)
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
                parameters.hierarchical().setThrowExceptionOnMissing(true)
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
     * Tests the inheritance structure of a hierarchical parameters object.
     */
    @Test
    public void testHierarchicalInheritance()
    {
        checkInheritance(parameters.hierarchical(),
                FileBasedBuilderParameters.class);
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
                parameters.xml().setThrowExceptionOnMissing(true)
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
     * Tests the inheritance structure of an XML parameters object.
     */
    @Test
    public void testXmlInheritance()
    {
        checkInheritance(parameters.xml(), HierarchicalBuilderParameters.class,
                FileBasedBuilderParameters.class);
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
                parameters.properties().setThrowExceptionOnMissing(true)
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
     * Tests the inheritance structure of a properties parameters object.
     */
    @Test
    public void testPropertiesInheritance()
    {
        checkInheritance(parameters.properties(),
                FileBasedBuilderParameters.class);
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
                parameters.multiFile().setThrowExceptionOnMissing(true)
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
                parameters.database().setThrowExceptionOnMissing(true)
                        .setAutoCommit(true).setTable("table")
                        .setListDelimiterHandler(listHandler).setKeyColumn("keyColumn")
                        .getParameters();
        checkBasicProperties(map);
        assertEquals("Wrong table name", "table", map.get("table"));
        assertEquals("Wrong key column name", "keyColumn", map.get("keyColumn"));
        assertEquals("Wrong auto commit flag", Boolean.TRUE,
                map.get("autoCommit"));
    }

    /**
     * Tests whether the parameters objects created by the Parameters instance
     * have a logic inheritance hierarchy. This means that they also implement
     * all base interfaces that make sense.
     */
    @Test
    public void testInheritance()
    {
        Object params = parameters.xml();
        assertTrue("No instance of base interface",
                params instanceof FileBasedBuilderParameters);
        assertTrue("No instance of base interface (dynamic)",
                FileBasedBuilderParameters.class.isInstance(params));
        FileBasedBuilderParameters fbParams =
                (FileBasedBuilderParameters) params;
        fbParams.setListDelimiterHandler(listHandler).setFileName("test.xml")
                .setThrowExceptionOnMissing(true);
        ExpressionEngine engine = EasyMock.createMock(ExpressionEngine.class);
        ((HierarchicalBuilderParameters) params).setExpressionEngine(engine);
        Map<String, Object> map = fbParams.getParameters();
        checkBasicProperties(map);
        assertSame("Wrong expression engine", engine, map.get("expressionEngine"));
    }

    /**
     * Tries to register a default handler without a class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDefaultsHandlerNoClass()
    {
        parameters
                .registerDefaultsHandler(null, new FileBasedDefaultsHandler());
    }

    /**
     * Tries to register a null default handler.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDefaultsHandlerNoHandler()
    {
        parameters.registerDefaultsHandler(BasicBuilderProperties.class, null);
    }

    /**
     * Checks whether the expected default values have been set on a parameters
     * object.
     *
     * @param map the map with parameters
     */
    private static void checkDefaultValues(Map<String, Object> map)
    {
        checkBasicProperties(map);
        FileBasedBuilderParametersImpl fbparams =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong encoding", DEF_ENCODING, fbparams.getFileHandler()
                .getEncoding());
    }

    /**
     * Checks that no default values have been set on a parameters object.
     *
     * @param map the map with parameters
     */
    private static void checkNoDefaultValues(Map<String, Object> map)
    {
        assertFalse("Got base properties",
                map.containsKey("throwExceptionOnMissing"));
        FileBasedBuilderParametersImpl fbParams =
                FileBasedBuilderParametersImpl.fromParameters(map, true);
        assertNull("Got an encoding", fbParams.getFileHandler().getEncoding());
    }

    /**
     * Tests whether default values are set for newly created parameters
     * objects.
     */
    @Test
    public void testApplyDefaults()
    {
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        Map<String, Object> map = parameters.fileBased().getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests whether default values are also applied when a sub parameters class
     * is created.
     */
    @Test
    public void testApplyDefaultsOnSubClass()
    {
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        Map<String, Object> map = parameters.xml().getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests that default values are only applied if the start class provided at
     * registration time matches.
     */
    @Test
    public void testApplyDefaultsStartClass()
    {
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler(), XMLBuilderParameters.class);
        Map<String, Object> map = parameters.xml().getParameters();
        checkDefaultValues(map);
        map = parameters.properties().getParameters();
        checkNoDefaultValues(map);
    }

    /**
     * Tests whether multiple handlers can be registered for the same classes
     * and whether they are called in the correct order.
     */
    @Test
    public void testApplyDefaultsMultipleHandlers()
    {
        final ExpressionEngine engine =
                EasyMock.createMock(ExpressionEngine.class);
        parameters.registerDefaultsHandler(XMLBuilderParameters.class,
                new DefaultParametersHandler<XMLBuilderParameters>()
                {
                    public void initializeDefaults(
                            XMLBuilderParameters parameters)
                    {
                        parameters
                                .setThrowExceptionOnMissing(false)
                                .setListDelimiterHandler(
                                        EasyMock.createMock(ListDelimiterHandler.class))
                                .setExpressionEngine(engine);
                    }
                });
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        Map<String, Object> map = parameters.xml().getParameters();
        checkDefaultValues(map);
        assertSame("Expression engine not set", engine,
                map.get("expressionEngine"));
    }

    /**
     * Tests whether initializeParameters() ignores null input. (We can only
     * test that no exception is thrown.)
     */
    @Test
    public void testInitializeParametersNull()
    {
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        parameters.initializeParameters(null);
    }

    /**
     * Tests whether a parameters object created externally can be initialized.
     */
    @Test
    public void testInitializeParameters()
    {
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        XMLBuilderParameters params = new Parameters().xml();
        parameters.initializeParameters(params);
        checkDefaultValues(params.getParameters());
    }

    /**
     * Tests whether all occurrences of a given defaults handler can be removed.
     */
    @Test
    public void testUnregisterDefaultsHandlerAll()
    {
        FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, XMLBuilderParameters.class);
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, PropertiesBuilderParameters.class);
        parameters.unregisterDefaultsHandler(handler);
        checkNoDefaultValues(parameters.fileBased().getParameters());
        checkNoDefaultValues(parameters.xml().getParameters());
        checkNoDefaultValues(parameters.properties().getParameters());
    }

    /**
     * Tests whether a specific occurrence of a defaults handler can be removed.
     */
    @Test
    public void testUnregisterDefaultsHandlerSpecific()
    {
        FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, XMLBuilderParameters.class);
        parameters.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, PropertiesBuilderParameters.class);
        parameters.unregisterDefaultsHandler(handler,
                PropertiesBuilderParameters.class);
        checkDefaultValues(parameters.xml().getParameters());
        checkNoDefaultValues(parameters.properties().getParameters());
    }

    /**
     * A test defaults handler implementation for testing the initialization of
     * parameters objects with default values. This class sets some hard-coded
     * default values.
     */
    private static class FileBasedDefaultsHandler implements
            DefaultParametersHandler<FileBasedBuilderParameters>
    {
        public void initializeDefaults(FileBasedBuilderParameters parameters)
        {
            parameters.setThrowExceptionOnMissing(true)
                    .setEncoding(DEF_ENCODING)
                    .setListDelimiterHandler(listHandler);
        }
    }
}
