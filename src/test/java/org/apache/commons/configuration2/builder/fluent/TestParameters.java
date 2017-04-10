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
package org.apache.commons.configuration2.builder.fluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicBuilderProperties;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.DefaultParametersManager;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.CombinedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.MultiFileBuilderParametersImpl;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        listHandler = EasyMock.createMock(ListDelimiterHandler.class);
    }

    /**
     * Tests whether an uninitialized default parameters manager is created at
     * construction time.
     */
    @Test
    public void testDefaultParametersManager()
    {
        Parameters parameters = new Parameters();
        assertNotNull("No default manager",
                parameters.getDefaultParametersManager());
    }

    /**
     * Tests whether a basic parameters object can be created.
     */
    @Test
    public void testBasic()
    {
        BasicBuilderParameters basic = new Parameters().basic();
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
                new Parameters().fileBased().setThrowExceptionOnMissing(true)
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
        checkInheritance(new Parameters().fileBased());
    }

    /**
     * Tests whether the proxy parameters object can deal with methods inherited
     * from Object.
     */
    @Test
    public void testProxyObjectMethods()
    {
        FileBasedBuilderParameters params = new Parameters().fileBased();
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
                new Parameters().combined().setThrowExceptionOnMissing(true)
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
                new Parameters().jndi().setThrowExceptionOnMissing(true)
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
                new Parameters().hierarchical().setThrowExceptionOnMissing(true)
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
        checkInheritance(new Parameters().hierarchical(),
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
                new Parameters().xml().setThrowExceptionOnMissing(true)
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
        checkInheritance(new Parameters().xml(), HierarchicalBuilderParameters.class,
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
                new Parameters().properties().setThrowExceptionOnMissing(true)
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
        assertSame("Wrong factory", factory, map.get("IOFactory"));
    }

    /**
     * Tests the inheritance structure of a properties parameters object.
     */
    @Test
    public void testPropertiesInheritance()
    {
        checkInheritance(new Parameters().properties(),
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
                new Parameters().multiFile().setThrowExceptionOnMissing(true)
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
                new Parameters().database().setThrowExceptionOnMissing(true)
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
        Object params = new Parameters().xml();
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
     * Tests whether default values are set for newly created parameters
     * objects.
     */
    @Test
    public void testApplyDefaults()
    {
        DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        final List<Object> initializedParams = new ArrayList<>(1);
        manager.initializeParameters(EasyMock
                .anyObject(BuilderParameters.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            @Override
            public Object answer() throws Throwable
            {
                initializedParams.add(EasyMock.getCurrentArguments()[0]);
                return null;
            }
        });
        EasyMock.replay(manager);

        Parameters params = new Parameters(manager);
        XMLBuilderParameters xmlParams = params.xml();
        assertEquals("Wrong number of initializations", 1,
                initializedParams.size());
        assertSame("Wrong initialized object", xmlParams,
                initializedParams.get(0));
    }

    /**
     * Creates a mock for a defaults parameter handler.
     *
     * @return the mock object
     */
    private static DefaultParametersHandler<XMLBuilderParameters> createHandlerMock()
    {
        @SuppressWarnings("unchecked")
        DefaultParametersHandler<XMLBuilderParameters> handler =
                EasyMock.createMock(DefaultParametersHandler.class);
        return handler;
    }

    /**
     * Tests whether a default handler with a start class can be registered.
     */
    @Test
    public void testRegisterDefaultsHandlerWithStartClass()
    {
        DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        DefaultParametersHandler<XMLBuilderParameters> handler =
                createHandlerMock();
        manager.registerDefaultsHandler(XMLBuilderParameters.class, handler,
                FileBasedBuilderParameters.class);
        EasyMock.replay(manager, handler);

        Parameters params = new Parameters(manager);
        params.registerDefaultsHandler(XMLBuilderParameters.class, handler,
                FileBasedBuilderParameters.class);
        EasyMock.verify(manager);
    }

    /**
     * Tests the registration of a defaults handler if no start class is
     * provided.
     */
    @Test
    public void testRegisterDefaultsHandlerNoStartClass()
    {
        DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        DefaultParametersHandler<XMLBuilderParameters> handler =
                createHandlerMock();
        manager.registerDefaultsHandler(XMLBuilderParameters.class, handler);
        EasyMock.replay(manager, handler);

        Parameters params = new Parameters(manager);
        params.registerDefaultsHandler(XMLBuilderParameters.class, handler);
        EasyMock.verify(manager);
    }
}
