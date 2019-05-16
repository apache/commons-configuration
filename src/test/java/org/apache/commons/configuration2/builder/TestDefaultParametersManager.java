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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Map;

import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code DefaultParametersManager}.
 *
 */
public class TestDefaultParametersManager
{
    /** Constant for the default encoding. */
    private static final String DEF_ENCODING = "UTF-8";

    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    /** An object for creating new parameters objects. */
    private Parameters parameters;

    /** The manager to be tested. */
    private DefaultParametersManager manager;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        listHandler = EasyMock.createMock(ListDelimiterHandler.class);
    }

    @Before
    public void setUp() throws Exception
    {
        parameters = new Parameters();
        manager = new DefaultParametersManager();
    }

    /**
     * Tries to register a default handler without a class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDefaultsHandlerNoClass()
    {
        manager.registerDefaultsHandler(null, new FileBasedDefaultsHandler());
    }

    /**
     * Tries to register a null default handler.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDefaultsHandlerNoHandler()
    {
        manager.registerDefaultsHandler(BasicBuilderProperties.class, null);
    }

    /**
     * Checks whether the expected default values have been set on a parameters
     * object.
     *
     * @param map the map with parameters
     */
    private static void checkDefaultValues(final Map<String, Object> map)
    {
        assertEquals("Wrong delimiter handler", listHandler,
                map.get("listDelimiterHandler"));
        assertEquals("Wrong exception flag value", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
        final FileBasedBuilderParametersImpl fbparams =
                FileBasedBuilderParametersImpl.fromParameters(map);
        assertEquals("Wrong encoding", DEF_ENCODING, fbparams.getFileHandler()
                .getEncoding());
    }

    /**
     * Checks that no default values have been set on a parameters object.
     *
     * @param map the map with parameters
     */
    private static void checkNoDefaultValues(final Map<String, Object> map)
    {
        assertFalse("Got base properties",
                map.containsKey("throwExceptionOnMissing"));
        final FileBasedBuilderParametersImpl fbParams =
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
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        final FileBasedBuilderParameters params = parameters.fileBased();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests whether default values are also applied when a sub parameters class
     * is created.
     */
    @Test
    public void testApplyDefaultsOnSubClass()
    {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        final XMLBuilderParameters params = parameters.xml();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
        checkDefaultValues(map);
    }

    /**
     * Tests that default values are only applied if the start class provided at
     * registration time matches.
     */
    @Test
    public void testApplyDefaultsStartClass()
    {
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler(), XMLBuilderParameters.class);
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
     * Tests whether multiple handlers can be registered for the same classes
     * and whether they are called in the correct order.
     */
    @Test
    public void testApplyDefaultsMultipleHandlers()
    {
        final ExpressionEngine engine =
                EasyMock.createMock(ExpressionEngine.class);
        manager.registerDefaultsHandler(XMLBuilderParameters.class,
                new DefaultParametersHandler<XMLBuilderParameters>()
                {
                    @Override
                    public void initializeDefaults(
                            final XMLBuilderParameters parameters)
                    {
                        parameters
                                .setThrowExceptionOnMissing(false)
                                .setListDelimiterHandler(
                                        EasyMock.createMock(ListDelimiterHandler.class))
                                .setExpressionEngine(engine);
                    }
                });
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        final XMLBuilderParameters params = parameters.xml();
        manager.initializeParameters(params);
        final Map<String, Object> map = params.getParameters();
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
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                new FileBasedDefaultsHandler());
        manager.initializeParameters(null);
    }

    /**
     * Tests whether all occurrences of a given defaults handler can be removed.
     */
    @Test
    public void testUnregisterDefaultsHandlerAll()
    {
        final FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, XMLBuilderParameters.class);
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, PropertiesBuilderParameters.class);
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
    public void testUnregisterDefaultsHandlerSpecific()
    {
        final FileBasedDefaultsHandler handler = new FileBasedDefaultsHandler();
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, XMLBuilderParameters.class);
        manager.registerDefaultsHandler(FileBasedBuilderParameters.class,
                handler, PropertiesBuilderParameters.class);
        manager.unregisterDefaultsHandler(handler,
                PropertiesBuilderParameters.class);
        final XMLBuilderParameters paramsXml = parameters.xml();
        manager.initializeParameters(paramsXml);
        checkDefaultValues(paramsXml.getParameters());
        final PropertiesBuilderParameters paramsProps = parameters.properties();
        manager.initializeParameters(paramsProps);
        checkNoDefaultValues(paramsProps.getParameters());
    }

    /**
     * A test defaults handler implementation for testing the initialization of
     * parameters objects with default values. This class sets some hard-coded
     * default values.
     */
    private static class FileBasedDefaultsHandler implements
            DefaultParametersHandler<FileBasedBuilderParameters>
    {
        @Override
        public void initializeDefaults(final FileBasedBuilderParameters parameters)
        {
            parameters.setThrowExceptionOnMissing(true)
                    .setEncoding(DEF_ENCODING)
                    .setListDelimiterHandler(listHandler);
        }
    }
}
