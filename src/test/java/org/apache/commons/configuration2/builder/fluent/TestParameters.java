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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationConsumer;
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
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code Parameters}.
 */
public class TestParameters {
    /** A default encoding. */
    private static final String DEF_ENCODING = StandardCharsets.UTF_8.name();

    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    /**
     * Checks whether the given parameters map contains the standard values for basic properties.
     *
     * @param map the map to be tested
     */
    private static void checkBasicProperties(final Map<String, Object> map) {
        assertEquals(listHandler, map.get("listDelimiterHandler"));
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
    }

    /**
     * Checks whether a given parameters object implements all the specified interfaces.
     *
     * @param params the parameters object to check
     * @param ifcClasses the interface classes to be implemented
     */
    private static void checkInheritance(final Object params, final Class<?>... ifcClasses) {
        assertInstanceOf(BasicBuilderProperties.class, params);
        for (final Class<?> c : ifcClasses) {
            assertInstanceOf(c, params);
        }
    }

    /**
     * Creates a mock for a defaults parameter handler.
     *
     * @return the mock object
     */
    @SuppressWarnings("unchecked")
    private static DefaultParametersHandler<XMLBuilderParameters> createHandlerMock() {
        return mock(DefaultParametersHandler.class);
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        listHandler = mock(ListDelimiterHandler.class);
    }

    /**
     * Tests whether default values are set for newly created parameters objects.
     */
    @Test
    public void testApplyDefaults() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final List<Object> initializedParams = new ArrayList<>(1);

        doAnswer(invocation -> {
            initializedParams.add(invocation.getArgument(0));
            return null;
        }).when(manager).initializeParameters(any());

        final Parameters params = new Parameters(manager);
        final XMLBuilderParameters xmlParams = params.xml();
        assertEquals(1, initializedParams.size());
        assertSame(xmlParams, initializedParams.get(0));

        verify(manager).initializeParameters(any());
        verifyNoMoreInteractions(manager);
    }

    /**
     * Tests whether a basic parameters object can be created.
     */
    @Test
    public void testBasic() {
        final BasicBuilderParameters basic = new Parameters().basic();
        assertNotNull(basic);
    }

    /**
     * Tests whether a combined parameters object can be created.
     */
    @Test
    public void testCombined() {
        final Map<String, Object> map = new Parameters().combined().setThrowExceptionOnMissing(true).setBasePath("test").setListDelimiterHandler(listHandler)
            .getParameters();
        final CombinedBuilderParametersImpl cparams = CombinedBuilderParametersImpl.fromParameters(map);
        assertEquals("test", cparams.getBasePath());
        checkBasicProperties(map);
    }

    /**
     * Tests whether a parameters object for a database configuration can be created.
     */
    @Test
    public void testDatabase() {
        final Map<String, Object> map = new Parameters().database().setThrowExceptionOnMissing(true).setAutoCommit(true).setTable("table")
            .setListDelimiterHandler(listHandler).setKeyColumn("keyColumn").getParameters();
        checkBasicProperties(map);
        assertEquals("table", map.get("table"));
        assertEquals("keyColumn", map.get("keyColumn"));
        assertEquals(Boolean.TRUE, map.get("autoCommit"));
    }

    /**
     * Tests whether an uninitialized default parameters manager is created at construction time.
     */
    @Test
    public void testDefaultParametersManager() {
        final Parameters parameters = new Parameters();
        assertNotNull(parameters.getDefaultParametersManager());
    }

    /**
     * Tests whether a file-based parameters object can be created.
     */
    @Test
    public void testFileBased() {
        final Map<String, Object> map = new Parameters().fileBased().setThrowExceptionOnMissing(true).setEncoding(DEF_ENCODING)
            .setListDelimiterHandler(listHandler).setFileName("test.xml").getParameters();
        final FileBasedBuilderParametersImpl fbparams = FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("test.xml", fbparams.getFileHandler().getFileName());
        assertEquals(DEF_ENCODING, fbparams.getFileHandler().getEncoding());
        checkBasicProperties(map);
    }

    /**
     * Tests the inheritance structure of a fileBased parameters object.
     */
    @Test
    public void testFileBasedInheritance() {
        checkInheritance(new Parameters().fileBased());
    }

    /**
     * Tests whether a parameters object for a hierarchical configuration can be created.
     */
    @Test
    public void testHierarchical() {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        final Map<String, Object> map = new Parameters().hierarchical().setThrowExceptionOnMissing(true).setExpressionEngine(engine).setFileName("test.xml")
            .setListDelimiterHandler(listHandler).getParameters();
        checkBasicProperties(map);
        final FileBasedBuilderParametersImpl fbp = FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("test.xml", fbp.getFileHandler().getFileName());
        assertEquals(engine, map.get("expressionEngine"));
    }

    /**
     * Tests the inheritance structure of a hierarchical parameters object.
     */
    @Test
    public void testHierarchicalInheritance() {
        checkInheritance(new Parameters().hierarchical(), FileBasedBuilderParameters.class);
    }

    /**
     * Tests whether the parameters objects created by the Parameters instance have a logic inheritance hierarchy. This
     * means that they also implement all base interfaces that make sense.
     */
    @Test
    public void testInheritance() {
        final Object params = new Parameters().xml();
        final FileBasedBuilderParameters fbParams = assertInstanceOf(FileBasedBuilderParameters.class, params);
        fbParams.setListDelimiterHandler(listHandler).setFileName("test.xml").setThrowExceptionOnMissing(true);
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        ((HierarchicalBuilderParameters) params).setExpressionEngine(engine);
        final Map<String, Object> map = fbParams.getParameters();
        checkBasicProperties(map);
        assertSame(engine, map.get("expressionEngine"));
    }

    /**
     * Tests whether a JNDI parameters object can be created.
     */
    @Test
    public void testJndi() {
        final Map<String, Object> map = new Parameters().jndi().setThrowExceptionOnMissing(true).setPrefix("test").setListDelimiterHandler(listHandler)
            .getParameters();
        assertEquals("test", map.get("prefix"));
        checkBasicProperties(map);
    }

    /**
     * Tests whether a {@code MultiFileBuilderParameters} object can be created.
     */
    @Test
    public void testMultiFile() {
        final BuilderParameters bp = mock(BuilderParameters.class);
        final String pattern = "a pattern";
        final Map<String, Object> map = new Parameters().multiFile().setThrowExceptionOnMissing(true).setFilePattern(pattern)
            .setListDelimiterHandler(listHandler).setManagedBuilderParameters(bp).getParameters();
        checkBasicProperties(map);
        final MultiFileBuilderParametersImpl params = MultiFileBuilderParametersImpl.fromParameters(map);
        assertSame(bp, params.getManagedBuilderParameters());
        assertEquals(pattern, params.getFilePattern());
    }

    /**
     * Tests whether a parameters object for a properties configuration can be created.
     */
    @Test
    public void testProperties() {
        final PropertiesConfiguration.IOFactory factory = mock(PropertiesConfiguration.IOFactory.class);
        @SuppressWarnings("unchecked")
        final ConfigurationConsumer<ConfigurationException> includeListener = mock(ConfigurationConsumer.class);
        // @formatter:off
        final Map<String, Object> map =
                new Parameters().properties()
                        .setThrowExceptionOnMissing(true)
                        .setFileName("test.properties")
                        .setIncludeListener(includeListener)
                        .setIOFactory(factory)
                        .setListDelimiterHandler(listHandler)
                        .setIncludesAllowed(false)
                        .getParameters();
        // @formatter:on
        checkBasicProperties(map);
        final FileBasedBuilderParametersImpl fbp = FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("test.properties", fbp.getFileHandler().getFileName());
        assertEquals(Boolean.FALSE, map.get("includesAllowed"));
        assertSame(includeListener, map.get("includeListener"));
        assertSame(factory, map.get("IOFactory"));
    }

    /**
     * Tests the inheritance structure of a properties parameters object.
     */
    @Test
    public void testPropertiesInheritance() {
        checkInheritance(new Parameters().properties(), FileBasedBuilderParameters.class);
    }

    /**
     * Tests whether the proxy parameters object can deal with methods inherited from Object.
     */
    @Test
    public void testProxyObjectMethods() {
        final FileBasedBuilderParameters params = new Parameters().fileBased();
        final String s = params.toString();
        assertTrue(s.contains(FileBasedBuilderParametersImpl.class.getSimpleName()));
        assertNotEquals(0, params.hashCode());
    }

    /**
     * Tests the registration of a defaults handler if no start class is provided.
     */
    @Test
    public void testRegisterDefaultsHandlerNoStartClass() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final DefaultParametersHandler<XMLBuilderParameters> handler = createHandlerMock();

        final Parameters params = new Parameters(manager);
        params.registerDefaultsHandler(XMLBuilderParameters.class, handler);

        verify(manager).registerDefaultsHandler(XMLBuilderParameters.class, handler);
        verifyNoMoreInteractions(manager);
    }

    /**
     * Tests whether a default handler with a start class can be registered.
     */
    @Test
    public void testRegisterDefaultsHandlerWithStartClass() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final DefaultParametersHandler<XMLBuilderParameters> handler = createHandlerMock();

        final Parameters params = new Parameters(manager);
        params.registerDefaultsHandler(XMLBuilderParameters.class, handler, FileBasedBuilderParameters.class);

        verify(manager).registerDefaultsHandler(XMLBuilderParameters.class, handler, FileBasedBuilderParameters.class);
        verifyNoMoreInteractions(manager);
    }

    /**
     * Tests whether a parameters object for an XML configuration can be created.
     */
    @Test
    public void testXml() {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        final Map<String, Object> map = new Parameters().xml().setThrowExceptionOnMissing(true).setFileName("test.xml").setValidating(true)
            .setExpressionEngine(engine).setListDelimiterHandler(listHandler).setSchemaValidation(true).getParameters();
        checkBasicProperties(map);
        final FileBasedBuilderParametersImpl fbp = FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("test.xml", fbp.getFileHandler().getFileName());
        assertEquals(Boolean.TRUE, map.get("validating"));
        assertEquals(Boolean.TRUE, map.get("schemaValidation"));
        assertEquals(engine, map.get("expressionEngine"));
    }

    /**
     * Tests the inheritance structure of an XML parameters object.
     */
    @Test
    public void testXmlInheritance() {
        checkInheritance(new Parameters().xml(), HierarchicalBuilderParameters.class, FileBasedBuilderParameters.class);
    }
}
