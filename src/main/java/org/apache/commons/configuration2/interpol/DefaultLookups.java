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
 * An enumeration class defining constants for built-in {@code Lookup} objects available for
 * {@code Configuration} instances.
 * </p>
 * <p>
 * When a new configuration object derived from {@code AbstractConfiguration} is created, it installs a
 * {@link ConfigurationInterpolator} containing a default set of {@link Lookup} objects. These lookups are
 * defined by this enumeration class, however not all lookups may be included in the defaults. See
 * {@link ConfigurationInterpolator#getDefaultPrefixLookups()} for details.
 * </p>
 * <p>
 * All the {@code Lookup}s defined here are state-less, thus their instances can be shared between multiple
 * configuration objects. Therefore, it makes sense to keep shared instances in this enumeration class.
 * </p>
 *
 * Provides access to lookups defined in Apache Commons Text:
 * <ul>
 * <li>"base64Decoder" for the {@code Base64DecoderStringLookup} since Apache Commons Text 1.6.</li>
 * <li>"base64Encoder" for the {@code Base64EncoderStringLookup} since Apache Commons Text 1.6.</li>
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
 * <li>"urlDecoder" for the {@code UrlDecoderStringLookup} since Apache Commons Text 1.6.</li>
 * <li>"urlEncoder" for the {@code UrlEncoderStringLookup} since Apache Commons Text 1.6.</li>
 * <li>"xml" for the {@code XmlStringLookup} since Apache Commons Text 1.5.</li>
 * </ul>
 *
 * @since 2.0
 */
public enum DefaultLookups {

    /**
     * The lookup for Base64 decoding, accessed using the prefix {@code "base64Decoder"}.
     *
     * @see StringLookupFactory#base64DecoderStringLookup()
     * @since 2.4
     */
    BASE64_DECODER(StringLookupFactory.KEY_BASE64_DECODER, new StringLookupAdapter(StringLookupFactory.INSTANCE.base64DecoderStringLookup())),

    /**
     * The lookup for Base64 encoding, accessed using the prefix {@code "base64Encoder"}.
     *
     * @see StringLookupFactory#base64EncoderStringLookup()
     * @since 2.4
     */
    BASE64_ENCODER(StringLookupFactory.KEY_BASE64_ENCODER, new StringLookupAdapter(StringLookupFactory.INSTANCE.base64EncoderStringLookup())),

    /**
     * The lookup for Java constants, accessed using the prefix {@code "const"}.
     *
     * @see StringLookupFactory#constantStringLookup()
     * @since 2.4
     */
    CONST(StringLookupFactory.KEY_CONST, new StringLookupAdapter(StringLookupFactory.INSTANCE.constantStringLookup())),

    /**
     * The lookup for the current date in a specified format, accessed using the prefix {@code "date"}.
     *
     * @see StringLookupFactory#dateStringLookup()
     * @since 2.4
     */
    DATE(StringLookupFactory.KEY_DATE, new StringLookupAdapter(StringLookupFactory.INSTANCE.dateStringLookup())),

    /**
     * The lookup for DNS, accessed using the prefix {@code "dns"}.
     *
     * @see StringLookupFactory#dnsStringLookup()
     * @since 2.6
     */
    DNS(StringLookupFactory.KEY_DNS, new StringLookupAdapter(StringLookupFactory.INSTANCE.dnsStringLookup())),

    /**
     * The lookup for environment properties, accessed using the prefix {@code "env"}.
     *
     * @see StringLookupFactory#environmentVariableStringLookup()
     */
    ENVIRONMENT(StringLookupFactory.KEY_ENV, new StringLookupAdapter(StringLookupFactory.INSTANCE.environmentVariableStringLookup())),

    /**
     * The lookup for file content, accessed using the prefix {@code "file"}.
     *
     * @see StringLookupFactory#fileStringLookup()
     * @since 2.4
     */
    FILE(StringLookupFactory.KEY_FILE, new StringLookupAdapter(StringLookupFactory.INSTANCE.fileStringLookup())),

    /**
     * The lookup for Java platform information, accessed using the prefix {@code "java"}.
     *
     * @see StringLookupFactory#javaPlatformStringLookup()
     * @since 2.4
     */
    JAVA(StringLookupFactory.KEY_JAVA, new StringLookupAdapter(StringLookupFactory.INSTANCE.javaPlatformStringLookup())),

    /**
     * The lookup for localhost information, accessed using the prefix {@code "localhost"}.
     *
     * @see StringLookupFactory#localHostStringLookup()
     * @since 2.4
     */
    LOCAL_HOST(StringLookupFactory.KEY_LOCALHOST, new StringLookupAdapter(StringLookupFactory.INSTANCE.localHostStringLookup())),

    /**
     * The lookup for properties, accessed using the prefix {@code "properties"}.
     *
     * @see StringLookupFactory#propertiesStringLookup()
     * @since 2.4
     */
    PROPERTIES(StringLookupFactory.KEY_PROPERTIES, new StringLookupAdapter(StringLookupFactory.INSTANCE.propertiesStringLookup())),

    /**
     * The lookup for resource bundles, accessed using the prefix {@code "resourceBundle"}.
     *
     * @see StringLookupFactory#resourceBundleStringLookup()
     * @since 2.4
     */
    RESOURCE_BUNDLE(StringLookupFactory.KEY_RESOURCE_BUNDLE, new StringLookupAdapter(StringLookupFactory.INSTANCE.resourceBundleStringLookup())),

    /**
     * The lookup for scripts, accessed using the prefix {@code "script"}.
     *
     * @see StringLookupFactory#scriptStringLookup()
     * @since 2.4
     */
    SCRIPT(StringLookupFactory.KEY_SCRIPT, new StringLookupAdapter(StringLookupFactory.INSTANCE.scriptStringLookup())),

    /**
     * The lookup for system properties, accessed using the prefix {@code "sys"}.
     *
     * @see StringLookupFactory#systemPropertyStringLookup()
     */
    SYSTEM_PROPERTIES(StringLookupFactory.KEY_SYS, new StringLookupAdapter(StringLookupFactory.INSTANCE.systemPropertyStringLookup())),

    /**
     * The lookup for URLs, accessed using the prefix {@code "url"}.
     *
     * @see StringLookupFactory#urlStringLookup()
     * @since 2.4
     */
    URL(StringLookupFactory.KEY_URL, new StringLookupAdapter(StringLookupFactory.INSTANCE.urlStringLookup())),

    /**
     * The lookup for URL decoding, accessed using the prefix {@code "urlDecoder"}.
     *
     * @see StringLookupFactory#urlDecoderStringLookup()
     * @since 2.4
     */
    URL_DECODER(StringLookupFactory.KEY_URL_DECODER, new StringLookupAdapter(StringLookupFactory.INSTANCE.urlDecoderStringLookup())),

    /**
     * The lookup for URL encoding, accessed using the prefix {@code "urlEncoder"}.
     *
     * @see StringLookupFactory#urlEncoderStringLookup()
     * @since 2.4
     */
    URL_ENCODER(StringLookupFactory.KEY_URL_ENCODER, new StringLookupAdapter(StringLookupFactory.INSTANCE.urlEncoderStringLookup())),

    /**
     * The lookup for XML content, accessed using the prefix {@code "xml"}.
     *
     * @see StringLookupFactory#xmlStringLookup()
     * @since 2.4
     */
    XML(StringLookupFactory.KEY_XML, new StringLookupAdapter(StringLookupFactory.INSTANCE.xmlStringLookup()));

    /** The associated lookup instance. */
    private final Lookup lookup;

    /** The prefix under which the associated lookup object is registered. */
    private final String prefix;

    /**
     * Creates a new instance of {@code DefaultLookups} and sets the prefix and the associated lookup instance.
     *
     * @param prefix the prefix
     * @param lookup the {@code Lookup} instance
     */
    DefaultLookups(final String prefix, final Lookup lookup) {
        this.prefix = prefix;
        this.lookup = lookup;
    }

    /**
     * Gets the standard {@code Lookup} instance of this kind.
     *
     * @return the associated {@code Lookup} object
     */
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Gets the standard prefix for the lookup object of this kind.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
