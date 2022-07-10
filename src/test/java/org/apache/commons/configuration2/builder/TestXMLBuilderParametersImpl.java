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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.EntityResolver;

/**
 * Test class for {@code XMLBuilderParametersImpl}.
 *
 */
public class TestXMLBuilderParametersImpl {
    /** The parameters object to be tested. */
    private XMLBuilderParametersImpl params;

    @BeforeEach
    public void setUp() throws Exception {
        params = new XMLBuilderParametersImpl();
    }

    /**
     * Tests whether properties can be set through BeanUtils.
     */
    @Test
    public void testBeanPropertiesAccess() throws Exception {
        final EntityResolver resolver = EasyMock.createMock(EntityResolver.class);
        final DocumentBuilder builder = EasyMock.createMock(DocumentBuilder.class);
        EasyMock.replay(resolver, builder);
        BeanHelper.setProperty(params, "throwExceptionOnMissing", Boolean.TRUE);
        BeanHelper.setProperty(params, "fileName", "test.xml");
        BeanHelper.setProperty(params, "entityResolver", resolver);
        BeanHelper.setProperty(params, "documentBuilder", builder);
        assertEquals("test.xml", params.getFileHandler().getFileName(), "Wrong file name");
        final Map<String, Object> paramsMap = params.getParameters();
        assertEquals(Boolean.TRUE, paramsMap.get("throwExceptionOnMissing"), "Wrong exception flag");
        assertSame(resolver, paramsMap.get("entityResolver"), "Wrong resolver");
        assertSame(builder, paramsMap.get("documentBuilder"), "Wrong builder");
    }

    /**
     * Tests whether properties can be inherited.
     */
    @Test
    public void testInheritFrom() {
        final EntityResolver resolver = EasyMock.createMock(EntityResolver.class);
        final DocumentBuilder builder = EasyMock.createMock(DocumentBuilder.class);
        params.setDocumentBuilder(builder).setEntityResolver(resolver).setSchemaValidation(true).setValidating(true);
        params.setThrowExceptionOnMissing(true);
        final XMLBuilderParametersImpl params2 = new XMLBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"), "Exception flag not set");
        assertEquals(resolver, parameters.get("entityResolver"), "Entity resolver not set");
        assertEquals(builder, parameters.get("documentBuilder"), "Document builder not set");
        assertEquals(Boolean.TRUE, parameters.get("validating"), "Validation flag not set");
        assertEquals(Boolean.TRUE, parameters.get("schemaValidation"), "Schema flag not set");
    }

    /**
     * Tests whether a document builder can be set.
     */
    @Test
    public void testSetDocumentBuilder() {
        final DocumentBuilder builder = EasyMock.createMock(DocumentBuilder.class);
        EasyMock.replay(builder);
        assertSame(params, params.setDocumentBuilder(builder), "Wrong result");
        assertSame(builder, params.getParameters().get("documentBuilder"), "Builder not in parameters");
    }

    /**
     * Tests whether an entity resolver can be set.
     */
    @Test
    public void testSetEntityResolver() {
        final EntityResolver resolver = EasyMock.createMock(EntityResolver.class);
        EasyMock.replay(resolver);
        assertSame(params, params.setEntityResolver(resolver), "Wrong result");
        assertSame(resolver, params.getEntityResolver(), "Resolver not set");
        assertSame(resolver, params.getParameters().get("entityResolver"), "Resolver not in parameters");
    }

    /**
     * Tests whether a public ID can be set.
     */
    @Test
    public void testSetPublicID() {
        final String pubID = "testPublicID";
        assertSame(params, params.setPublicID(pubID), "Wrong result");
        assertEquals(pubID, params.getParameters().get("publicID"), "ID not in parameters");
    }

    /**
     * Tests whether the schema validation flag can be set.
     */
    @Test
    public void testSetSchemaValidation() {
        assertSame(params, params.setSchemaValidation(false), "Wrong result");
        assertEquals(Boolean.FALSE, params.getParameters().get("schemaValidation"), "Flag not in parameters");
    }

    /**
     * Tests whether a system ID can be set.
     */
    @Test
    public void testSetSystemID() {
        final String sysID = "testSystemID";
        assertSame(params, params.setSystemID(sysID), "Wrong result");
        assertEquals(sysID, params.getParameters().get("systemID"), "ID not in parameters");
    }

    /**
     * Tests whether validating property can be set.
     */
    @Test
    public void testSetValidating() {
        assertSame(params, params.setValidating(true), "Wrong result");
        assertEquals(Boolean.TRUE, params.getParameters().get("validating"), "Flag not in parameters");
    }
}
