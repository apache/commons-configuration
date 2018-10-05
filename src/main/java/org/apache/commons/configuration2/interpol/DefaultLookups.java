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
package org.apache.commons.configuration2.interpol;

import org.apache.commons.text.lookup.StringLookupFactory;

/**
 * <p>
 * An enumeration class defining constants for the {@code Lookup} objects available for each {@code Configuration}
 * object per default.
 * </p>
 * <p>
 * When a new configuration object derived from {@code AbstractConfiguration} is created it installs a
 * {@link ConfigurationInterpolator} with a default set of {@link Lookup} objects. These lookups are defined by this
 * enumeration class.
 * </p>
 * <p>
 * All the default {@code Lookup} classes are state-less, thus their instances can be shared between multiple
 * configuration objects. Therefore, it makes sense to keep shared instances in this enumeration class.
 * </p>
 * 
 * Provides access to lookups defined in Apache Commons Text:
 * <ul>
 * <li>"base64" for the {@code Base64StringLookup} since Apache Commons Text 1.5.</li>
 * <li>"const" for the {@code ConstantStringLookup} since Apache Commons Text 1.5.</li>
 * <li>"date" for the {@code DateStringLookup}.</li>
 * <li>"env" for the {@code EnvironmentVariableStringLookup}.</li>
 * <li>"file" for the {@code FileStringLookup} since Apache Commons Text 1.5.</li>
 * <li>"java" for the {@code JavaPlatformStringLookup}.</li>
 * <li>"localhost" for the {@code LocalHostStringLookup}, see {@code #localHostStringLookup()} for key names; since
 * Apache Commons Text 1.3.</li>
 * <li>"properties" for the {@code PropertiesStringLookup} since Apache Commons Text 1.5.</li>
 * <li>"resourceBundle" for the {@code ResourceBundleStringLookup} since Apache Commons Text 1.5.</li>
 * <li>"script" for the {@code ScriptStringLookup} since Apache Commons Text 1.5.</li>
 * <li>"sys" for the {@code SystemPropertyStringLookup}.</li>
 * <li>"url" for the {@code UrlStringLookup} since Apache Commons Text 1.5.</li>
// * <li>"urlDecode" for the {@code UrlDecoderStringLookup} since Apache Commons Text 1.6.</li>
// * <li>"urlEncode" for the {@code UrlEncoderStringLookup} since Apache Commons Text 1.6.</li>
 * <li>"xml" for the {@code XmlStringLookup} since Apache Commons Text 1.5.</li>
 * </ul>
 * 
 * @version $Id$
 * @since 2.0
 */
public enum DefaultLookups {

    /**
     * The lookup for Base64.
     * 
     * @since 2.4
     */
    BASE64("base64", new StringLookupAdapter(StringLookupFactory.INSTANCE.base64StringLookup())),

    /**
     * The lookup for constants.
     * 
     * @since 2.4
     */
    CONST("const", new StringLookupAdapter(StringLookupFactory.INSTANCE.constantStringLookup())),

    /**
     * The lookup for dates.
     * 
     * @since 2.4
     */
    DATE("date", new StringLookupAdapter(StringLookupFactory.INSTANCE.dateStringLookup())),

    /**
     * The lookup for environment properties.
     */
    ENVIRONMENT("env", new StringLookupAdapter(StringLookupFactory.INSTANCE.environmentVariableStringLookup())),

    /**
     * The lookup for files.
     * 
     * @since 2.4
     */
    FILE("file", new StringLookupAdapter(StringLookupFactory.INSTANCE.fileStringLookup())),

    /**
     * The lookup for Java platform information.
     * 
     * @since 2.4
     */
    JAVA("java", new StringLookupAdapter(StringLookupFactory.INSTANCE.javaPlatformStringLookup())),

    /**
     * The lookup for localhost information.
     * 
     * @since 2.4
     */
    LOCAL_HOST("localhost", new StringLookupAdapter(StringLookupFactory.INSTANCE.localHostStringLookup())),

    /**
     * The lookup for properties.
     * 
     * @since 2.4
     */
    PROPERTIES("properties", new StringLookupAdapter(StringLookupFactory.INSTANCE.propertiesStringLookup())),

    /**
     * The lookup for resource bundles.
     * 
     * @since 2.4
     */
    RESOURCE_BUNDLE("resourceBundle",
            new StringLookupAdapter(StringLookupFactory.INSTANCE.resourceBundleStringLookup())),

    /**
     * The lookup for scripts.
     * 
     * @since 2.4
     */
    SCRIPT("script", new StringLookupAdapter(StringLookupFactory.INSTANCE.scriptStringLookup())),

    /**
     * The lookup for system properties.
     */
    SYSTEM_PROPERTIES("sys", new StringLookupAdapter(StringLookupFactory.INSTANCE.systemPropertyStringLookup())),

    /**
     * The lookup for URLs.
     * 
     * @since 2.4
     */
    URL("url", new StringLookupAdapter(StringLookupFactory.INSTANCE.urlStringLookup())),

    /**
     * The lookup for URL decoding.
     * 
     * @since 2.4
     */
    // URL_DECODE("urlDecoder", new StringLookupAdapter(StringLookupFactory.INSTANCE.urlDecoderStringLookup())),

    /**
     * The lookup for URL decoding.
     * 
     * @since 2.4
     */
    // URL_ENCODE("urlEncoder", new StringLookupAdapter(StringLookupFactory.INSTANCE.urlEncoderStringLookup())),

    /**
     * The lookup for URL decoding.
     * 
     * @since 2.4
     */
    XML("xml", new StringLookupAdapter(StringLookupFactory.INSTANCE.xmlStringLookup()));

    /** The associated lookup instance. */
    private final Lookup lookup;

    /** The prefix under which the associated lookup object is registered. */
    private final String prefix;

    /**
     * Creates a new instance of {@code DefaultLookups} and sets the prefix and the associated lookup instance.
     *
     * @param prefix
     *            the prefix
     * @param lookup
     *            the {@code Lookup} instance
     */
    private DefaultLookups(final String prefix, final Lookup lookup) {
        this.prefix = prefix;
        this.lookup = lookup;
    }

    /**
     * Returns the standard {@code Lookup} instance of this kind.
     *
     * @return the associated {@code Lookup} object
     */
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Returns the standard prefix for the lookup object of this kind.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
