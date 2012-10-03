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
package org.apache.commons.configuration.builder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * <p>
 * An implementation of {@code BuilderParameters} which handles the parameters
 * of a {@link ConfigurationBuilder} common to all concrete
 * {@code Configuration} implementations.
 * </p>
 * <p>
 * This class provides methods for setting standard properties supported by the
 * {@code AbstractConfiguration} base class. A fluent interface can be used to
 * set property values.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class BasicBuilderParameters implements BuilderParameters
{
    /** The key of the <em>throwExceptionOnMissing</em> property. */
    private static final String PROP_THROW_EXCEPTION_ON_MISSING =
            "throwExceptionOnMissing";

    /** The key of the <em>delimiterParsingDisabled</em> property. */
    private static final String PROP_DELIMITER_PARSING_DISABLED =
            "delimiterParsingDisabled";

    /** The key of the <em>listDelimiter</em> property. */
    private static final String PROP_LIST_DELIMITER = "listDelimiter";

    /** The key of the <em>logger</em> property. */
    private static final String PROP_LOGGER = "logger";

    /** The map for storing the current property values. */
    private final Map<String, Object> properties;

    /**
     * Creates a new instance of {@code BasicBuilderParameters}.
     */
    public BasicBuilderParameters()
    {
        properties = new HashMap<String, Object>();
        initDefaults();
    }

    /**
     * {@inheritDoc} This implementation returns a copy of the internal
     * parameters map with the values set so far.
     */
    public Map<String, Object> getParameters()
    {
        return new HashMap<String, Object>(properties);
    }

    /**
     * Sets the <em>logger</em> property. With this property a concrete
     * {@code Log} object can be set for the configuration. Thus logging
     * behavior can be controlled.
     *
     * @param log the {@code Log} for the configuration produced by this builder
     * @return a reference to this object for method chaining
     */
    public BasicBuilderParameters setLogger(Log log)
    {
        return setProperty(PROP_LOGGER, log);
    }

    /**
     * Sets the value of the <em>delimiterParsingDisabled</em> property. This
     * property controls whether the configuration should look for list
     * delimiter characters in the values of newly added properties. If the
     * property value is <b>true</b> and such characters are encountered,
     * multiple values are stored for the affected property.
     *
     * @param b the value of the property
     * @return a reference to this object for method chaining
     */
    public BasicBuilderParameters setDelimiterParsingDisabled(boolean b)
    {
        return setProperty(PROP_DELIMITER_PARSING_DISABLED, Boolean.valueOf(b));
    }

    /**
     * Sets the value of the <em>throwExceptionOnMissing</em> property. This
     * property controls the configuration's behavior if missing properties are
     * queried: a value of <b>true</b> causes the configuration to throw an
     * exception, for a value of <b>false</b> it will return <b>null</b> values.
     * (Note: Methods returning a primitive data type will always throw an
     * exception if the property is not defined.)
     *
     * @param b the value of the property
     * @return a reference to this object for method chaining
     */
    public BasicBuilderParameters setThrowExceptionOnMissing(boolean b)
    {
        return setProperty(PROP_THROW_EXCEPTION_ON_MISSING, Boolean.valueOf(b));
    }

    /**
     * Sets the value of the <em>listDelimiter</em> property. This property
     * defines the list delimiter character. It is evaluated only if the
     * <em>delimiterParsingDisabled</em> property is set to <b>false</b>.
     *
     * @param c the list delimiter character
     * @return a reference to this object for method chaining
     * @see #setDelimiterParsingDisabled(boolean)
     */
    public BasicBuilderParameters setListDelimiter(char c)
    {
        return setProperty(PROP_LIST_DELIMITER, Character.valueOf(c));
    }

    /**
     * Sets default parameter values.
     */
    private void initDefaults()
    {
        properties.put(PROP_DELIMITER_PARSING_DISABLED, Boolean.TRUE);
    }

    /**
     * Helper method for setting a property value.
     *
     * @param key the key of the property
     * @param value the value of the property
     * @return a reference to this object
     */
    private BasicBuilderParameters setProperty(String key, Object value)
    {
        properties.put(key, value);
        return this;
    }
}
