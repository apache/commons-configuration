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

import org.xml.sax.EntityResolver;

/**
 * <p>
 * Definition of a parameters interface for XML configurations.
 * </p>
 * <p>
 * The {@code XMLConfiguration} class defines a bunch of additional properties
 * related to XML processing.
 * </p>
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface XMLBuilderProperties<T>
{
    /**
     * Allows setting the {@code DocumentBuilder} for parsing an XML document.
     * This is the most flexible way of customizing XML processing.
     *
     * @param docBuilder the {@code DocumentBuilder} to use
     * @return a reference to this object for method chaining
     */
    T setDocumentBuilder(DocumentBuilder docBuilder);

    /**
     * Allows setting the {@code EntityResolver} which maps entity references
     * during XML parsing.
     *
     * @param resolver the {@code EntityResolver} to use
     * @return a reference to this object for method chaining
     */
    T setEntityResolver(EntityResolver resolver);

    /**
     * Sets the public ID of the DOCTYPE declaration.
     *
     * @param pubID the public ID
     * @return a reference to this object for method chaining
     */
    T setPublicID(String pubID);

    /**
     * Sets the system ID of the DOCTYPE declaration.
     *
     * @param sysID the system ID
     * @return a reference to this object for method chaining
     */
    T setSystemID(String sysID);

    /**
     * Sets a flag whether schema/DTD validation should be performed.
     *
     * @param f the validation flag
     * @return a reference to this object for method chaining
     */
    T setValidating(boolean f);

    /**
     * Sets the value of the schemaValidation flag. This flag determines whether
     * DTD or Schema validation should be used.
     *
     * @param f the flag value, <b>true</b> for schema validation, <b>false</b>
     *        for DTD validation
     * @return a reference to this object for method chaining
     */
    T setSchemaValidation(boolean f);
}
