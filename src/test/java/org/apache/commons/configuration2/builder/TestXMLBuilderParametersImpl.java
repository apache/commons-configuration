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
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.EntityResolver;

/**
 * Test class for {@code XMLBuilderParametersImpl}.
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
        final EntityResolver resolver = mock(EntityResolver.class);
        final DocumentBuilder builder = mock(DocumentBuilder.class);
        BeanHelper.setProperty(params, "throwExceptionOnMissing", Boolean.TRUE);
        BeanHelper.setProperty(params, "fileName", "test.xml");
        BeanHelper.setProperty(params, "entityResolver", resolver);
        BeanHelper.setProperty(params, "documentBuilder", builder);
        assertEquals("test.xml", params.getFileHandler().getFileName());
        final Map<String, Object> paramsMap = params.getParameters();
        assertEquals(Boolean.TRUE, paramsMap.get("throwExceptionOnMissing"));
        assertSame(resolver, paramsMap.get("entityResolver"));
        assertSame(builder, paramsMap.get("documentBuilder"));
    }

    /**
     * Tests whether properties can be inherited.
     */
    @Test
    public void testInheritFrom() {
        final EntityResolver resolver = mock(EntityResolver.class);
        final DocumentBuilder builder = mock(DocumentBuilder.class);
        params.setDocumentBuilder(builder).setEntityResolver(resolver).setSchemaValidation(true).setValidating(true);
        params.setThrowExceptionOnMissing(true);
        final XMLBuilderParametersImpl params2 = new XMLBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"));
        assertEquals(resolver, parameters.get("entityResolver"));
        assertEquals(builder, parameters.get("documentBuilder"));
        assertEquals(Boolean.TRUE, parameters.get("validating"));
        assertEquals(Boolean.TRUE, parameters.get("schemaValidation"));
    }

    /**
     * Tests whether a document builder can be set.
     */
    @Test
    public void testSetDocumentBuilder() {
        final DocumentBuilder builder = mock(DocumentBuilder.class);
        assertSame(params, params.setDocumentBuilder(builder));
        assertSame(builder, params.getParameters().get("documentBuilder"));
    }

    /**
     * Tests whether an entity resolver can be set.
     */
    @Test
    public void testSetEntityResolver() {
        final EntityResolver resolver = mock(EntityResolver.class);
        assertSame(params, params.setEntityResolver(resolver));
        assertSame(resolver, params.getEntityResolver());
        assertSame(resolver, params.getParameters().get("entityResolver"));
    }

    /**
     * Tests whether a public ID can be set.
     */
    @Test
    public void testSetPublicID() {
        final String pubID = "testPublicID";
        assertSame(params, params.setPublicID(pubID));
        assertEquals(pubID, params.getParameters().get("publicID"));
    }

    /**
     * Tests whether the schema validation flag can be set.
     */
    @Test
    public void testSetSchemaValidation() {
        assertSame(params, params.setSchemaValidation(false));
        assertEquals(Boolean.FALSE, params.getParameters().get("schemaValidation"));
    }

    /**
     * Tests whether a system ID can be set.
     */
    @Test
    public void testSetSystemID() {
        final String sysID = "testSystemID";
        assertSame(params, params.setSystemID(sysID));
        assertEquals(sysID, params.getParameters().get("systemID"));
    }

    /**
     * Tests whether validating property can be set.
     */
    @Test
    public void testSetValidating() {
        assertSame(params, params.setValidating(true));
        assertEquals(Boolean.TRUE, params.getParameters().get("validating"));
    }
}
