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

import javax.xml.parsers.DocumentBuilder;

import java.util.Map;

import org.xml.sax.EntityResolver;

/**
 * <p>
 * A specialized parameters class for XML configuration.
 * </p>
 * <p>
 * This parameters class defines some properties which allow customizing the
 * parsing of XML documents. The location of the XML document to be loaded can
 * be specified, too.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @since 2.0
 */
public class XMLBuilderParametersImpl extends HierarchicalBuilderParametersImpl
        implements XMLBuilderProperties<XMLBuilderParametersImpl>
{
    /** The key for the entity resolver property. */
    private static final String PROP_ENTITY_RESOLVER = "entityResolver";

    /** The key for the document builder property. */
    private static final String PROP_DOCUMENT_BUILDER = "documentBuilder";

    /** The key for the public ID property. */
    private static final String PROP_PUBLIC_ID = "publicID";

    /** The key for the system ID property. */
    private static final String PROP_SYSTEM_ID = "systemID";

    /** The key for the validating property. */
    private static final String PROP_VALIDATING = "validating";

    /** The key for the schema validation flag. */
    private static final String PROP_SCHEMA_VALIDATION = "schemaValidation";

    @Override
    public void inheritFrom(final Map<String, ?> source)
    {
        super.inheritFrom(source);
        copyPropertiesFrom(source, PROP_DOCUMENT_BUILDER, PROP_ENTITY_RESOLVER,
                PROP_SCHEMA_VALIDATION, PROP_VALIDATING);
    }

    @Override
    public XMLBuilderParametersImpl setDocumentBuilder(
            final DocumentBuilder docBuilder)
    {
        storeProperty(PROP_DOCUMENT_BUILDER, docBuilder);
        return this;
    }

    @Override
    public XMLBuilderParametersImpl setEntityResolver(final EntityResolver resolver)
    {
        storeProperty(PROP_ENTITY_RESOLVER, resolver);
        return this;
    }

    /**
     * Returns the {@code EntityResolver} stored in this parameters object.
     * Result is <b>null</b> if no such object has been set.
     *
     * @return the {@code EntityResolver} or <b>null</b>
     */
    public EntityResolver getEntityResolver()
    {
        return (EntityResolver) fetchProperty(PROP_ENTITY_RESOLVER);
    }

    @Override
    public XMLBuilderParametersImpl setPublicID(final String pubID)
    {
        storeProperty(PROP_PUBLIC_ID, pubID);
        return this;
    }

    @Override
    public XMLBuilderParametersImpl setSystemID(final String sysID)
    {
        storeProperty(PROP_SYSTEM_ID, sysID);
        return this;
    }

    @Override
    public XMLBuilderParametersImpl setValidating(final boolean f)
    {
        storeProperty(PROP_VALIDATING, Boolean.valueOf(f));
        return this;
    }

    @Override
    public XMLBuilderParametersImpl setSchemaValidation(final boolean f)
    {
        storeProperty(PROP_SCHEMA_VALIDATION, Boolean.valueOf(f));
        return this;
    }
}
