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
package org.apache.commons.configuration2.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code DefaultParametersManager}.
 */
public class TestDefaultParametersManager {
    /**
     * A test defaults handler implementation for testing the initialization of parameters objects with default values. This
     * class sets some hard-coded default values.
     */
    private static final class FileBasedDefaultsHandler implements DefaultParametersHandler<FileBasedBuilderParameters> {
        @Override
        public void initializeDefaults(final FileBasedBuilderParameters parameters) {
            parameters.setThrowExceptionOnMissing(true).setEncoding(DEF_ENCODING).setListDelimiterHandler(listHandler);
        }
    }

    /** Constant for the default encoding. */
    private static final String DEF_ENCODING = StandardCharsets.UTF_8.name();

    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    /**
     * Checks whether the expected default values have been set on a parameters object.
     *
     * @param map the map with parameters
     */
    private static void checkDefaultValues(final Map<String, Object> map) {
        assertEquals(listHandler, map.get("listDelimiterHandler"));
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
        final FileBasedBuilderParametersImpl fbparams = FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals(DEF_ENCODING, fbparams.getFileHandler().getEncoding());
    }

    /**
     * Checks that no default values have been set on a parameters object.
     *
     * @param map the map with parameters
     */
    private static void checkNoDefaultValues(final Map<String, Object> map) {
        assertFalse(map.containsKey("throwExceptionOnMissing"));
        final FileBasedBuilderParametersImpl fbParams = FileBasedBuilderParametersImpl.fromParameters(map, true);
        assertNull(fbParams.getFileHandler().getEncoding());
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        listHandler = mock(ListDelimiterHandler.class);
    }

    /** An object for creating new parameters objects. */
    private Parameters parameters;

    /** The manager to be tested. */
    private DefaultParametersManager manager;

    @BeforeEach
    public void setUp() throws Exception {
        parameters = new Parameters();
        manager = new DefaultParametersManager();
    }

    /**
     * Tests whether default values are set for newly created parameters objects.
     */
    @Test
    public void testApplyDefaults() {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, new FileBasedDefaultsHandler());
        final FileBasedBuilderParameters params = parameters.fileBased();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests whether multiple handlers can be registered for the same classes and whether they are called in the correct
     * order.
     */
    @Test
    public void testApplyDefaultsMultipleHandlers() {
        final ExpressionEngine engine = mock(ExpressionEngine.class);
        manager.registerDefaultsHandler(XMLBuilderParameters.class, parameters -> parameters.setThrowExceptionOnMissing(false)
            .setListDelimiterHandler(mock(ListDelimiterHandler.class)).setExpressionEngine(engine));
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, new FileBasedDefaultsHandler());
        final XMLBuilderParameters params = parameters.xml();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
        checkDefaultValues(map);
        assertSame(engine, map.get("expressionEngine"));
    }

    /**
     * Tests whether default values are also applied when a sub parameters class is created.
     */
    @Test
    public void testApplyDefaultsOnSubClass() {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, new FileBasedDefaultsHandler());
        final XMLBuilderParameters params = parameters.xml();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests that default values are only applied if the start class provided at registration time matches.
     */
    @Test
    public void testApplyDefaultsStartClass() {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, new FileBasedDefaultsHandler(), XMLBuilderParameters.class);
        final XMLBuilderParameters paramsXml = parameters.xml();
        manager.initializeParameters(paramsXml);
        Map<String, Object> map = paramsXml.getParameters();
        checkDefaultValues(map);
        final PropertiesBuilderParameters paramsProps = parameters.properties();
        manager.initializeParameters(paramsProps);
        map = paramsProps.getParameters();
        checkNoDefaultValues(map);
    }

    /**
     * Tests whether initializeParameters() ignores null input. (We can only test that no exception is thrown.)
     */
    @Test
    public void testInitializeParametersNull() {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, new FileBasedDefaultsHandler());
        manager.initializeParameters(null);
    }

    /**
     * Tries to register a default handler without a class.
     */
    @Test
    public void testRegisterDefaultsHandlerNoClass() {
        final FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        assertThrows(IllegalArgumentException.class, () -> manager.registerDefaultsHandler(null, handler));
    }

    /**
     * Tries to register a null default handler.
     */
    @Test
    public void testRegisterDefaultsHandlerNoHandler() {
        assertThrows(IllegalArgumentException.class, () -> manager.registerDefaultsHandler(BasicBuilderProperties.class, null));
    }

    /**
     * Tests whether all occurrences of a given defaults handler can be removed.
     */
    @Test
    public void testUnregisterDefaultsHandlerAll() {
        final FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, handler, XMLBuilderParameters.class);
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, handler, PropertiesBuilderParameters.class);
        manager.unregisterDefaultsHandler(handler);

        final XMLBuilderParameters paramsXml = parameters.xml();
        manager.initializeParameters(paramsXml);
        checkNoDefaultValues(paramsXml.getParameters());
        final PropertiesBuilderParameters paramsProps = parameters.properties();
        manager.initializeParameters(paramsProps);
        checkNoDefaultValues(paramsProps.getParameters());
    }

    /**
     * Tests whether a specific occurrence of a defaults handler can be removed.
     */
    @Test
    public void testUnregisterDefaultsHandlerSpecific() {
        final FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, handler, XMLBuilderParameters.class);
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class, handler, PropertiesBuilderParameters.class);
        manager.unregisterDefaultsHandler(handler, PropertiesBuilderParameters.class);
        final XMLBuilderParameters paramsXml = parameters.xml();
        manager.initializeParameters(paramsXml);
        checkDefaultValues(paramsXml.getParameters());
        final PropertiesBuilderParameters paramsProps = parameters.properties();
        manager.initializeParameters(paramsProps);
        checkNoDefaultValues(paramsProps.getParameters());
    }
}
