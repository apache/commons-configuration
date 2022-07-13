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
package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

/**
 * A helper class that defines a bunch of tests related to variable interpolation. It can be used for running these
 * tests on different configuration implementations.
 */
public class InterpolationTestHelper {
    /**
     * Tests accessing and manipulating the interpolator object.
     *
     * @param config the configuration to test
     */
    public static void testGetInterpolator(final AbstractConfiguration config) {
        config.addProperty("var", "${echo:testVar}");
        final ConfigurationInterpolator interpol = config.getInterpolator();
        interpol.registerLookup("echo", varName -> "Value of variable " + varName);
        assertEquals("Value of variable testVar", config.getString("var"));
    }

    /**
     * Tests obtaining a configuration with all variables replaced by their actual values.
     *
     * @param config the configuration to test
     * @return the interpolated configuration
     */
    public static Configuration testInterpolatedConfiguration(final AbstractConfiguration config) {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        config.setProperty("inttest.interpol", "${unknown.property}");
        config.setProperty("intkey.code", "${const:java.awt.event.KeyEvent.VK_CANCEL}");
        config.setProperty("inttest.sysprop", "${sys:java.version}");
        config.setProperty("inttest.numvalue", "3\\,1415");
        config.setProperty("inttest.value", "${inttest.numvalue}");
        config.setProperty("inttest.list", "${db}");
        config.addProperty("inttest.list", "${inttest.value}");

        final Configuration c = config.interpolatedConfiguration();
        assertEquals("/home/applicationRoot/db/hypersonic", c.getProperty("db"));
        assertEquals(KeyEvent.VK_CANCEL, c.getInt("intkey.code"));
        assertEquals(System.getProperty("java.version"), c.getProperty("inttest.sysprop"));
        assertEquals("3,1415", c.getProperty("inttest.value"));
        final List<?> lst = (List<?>) c.getProperty("inttest.list");
        assertEquals(Arrays.asList("/home/applicationRoot/db/hypersonic", "3,1415"), lst);
        assertEquals("${unknown.property}", c.getProperty("inttest.interpol"));

        return c;
    }

    /**
     * Tests basic interpolation facilities of the specified configuration.
     *
     * @param config the configuration to test
     */
    public static void testInterpolation(final Configuration config) {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        final String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        final String dbProp = "/home/applicationRoot/db/hypersonic";

        assertEquals(dbProp, config.getString("db"));
        assertEquals(unInterpolatedValue, config.getString("dbFailedInterpolate"));

        config.setProperty("arrayInt", "${applicationRoot}/1");
        final String[] arrayInt = config.getStringArray("arrayInt");
        assertEquals("/home/applicationRoot/1", arrayInt[0]);

        config.addProperty("path", Arrays.asList("/temp", "C:\\Temp", "/usr/local/tmp"));
        config.setProperty("path.current", "${path}");
        assertEquals("/temp", config.getString("path.current"));
    }

    /**
     * Tests interpolation of constant values.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationConstants(final Configuration config) {
        config.addProperty("key.code", "${const:java.awt.event.KeyEvent.VK_CANCEL}");
        assertEquals(KeyEvent.VK_CANCEL, config.getInt("key.code"));
        assertEquals(KeyEvent.VK_CANCEL, config.getInt("key.code"));
    }

    /**
     * Tests interpolation of environment properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationEnvironment(final Configuration config) {
        final Map<String, String> env = System.getenv();
        for (final Map.Entry<String, String> e : env.entrySet()) {
            config.addProperty("prop" + e.getKey(), "${env:" + e.getKey() + "}");
        }

        for (final Map.Entry<String, String> e : env.entrySet()) {
            assertEquals(e.getValue(), config.getString("prop" + e.getKey()), "Wrong value for environment property " + e.getKey());
        }
    }

    /**
     * Tests whether a variable can be escaped, so that it won't be interpolated.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationEscaped(final Configuration config) {
        config.addProperty("var", "x");
        config.addProperty("escVar", "Use the variable $${${var}}.");
        assertEquals("Use the variable ${x}.", config.getString("escVar"));
    }

    /**
     * Tests interpolation of localhost properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationLocalhost(final Configuration config) {
        final String[] localhostKeys = {"name", "canonical-name", "address"};
        final InetAddress localHost = assertDoesNotThrow(InetAddress::getLocalHost);
        final String[] localhostValues = {localHost.getHostName(), localHost.getCanonicalHostName(), localHost.getHostAddress()};
        for (int i = 0; i < localhostKeys.length; i++) {
            config.addProperty("prop" + i, "${localhost:" + localhostKeys[i] + "}");
        }

        for (int i = 0; i < localhostKeys.length; i++) {
            assertEquals(localhostValues[i], config.getString("prop" + i), "Wrong value for system property " + localhostKeys[i]);
        }
    }

    /**
     * Tests an invalid interpolation that results in an infinite loop. This loop should be detected and an exception should
     * be thrown.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationLoop(final Configuration config) {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");

        assertThrows(IllegalStateException.class, () -> config.getString("test.a"));
    }

    /**
     * Tests interpolation when a subset configuration is involved.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationSubset(final Configuration config) {
        config.addProperty("test.a", Integer.valueOf(42));
        config.addProperty("test.b", "${test.a}");
        assertEquals(42, config.getInt("test.b"));
        final Configuration subset = config.subset("test");
        assertEquals("42", subset.getString("b"));
        assertEquals(42, subset.getInt("b"));
    }

    /**
     * Tests interpolation of system properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationSystemProperties(final Configuration config) {
        final String[] sysProperties = {"java.version", "java.vendor", "os.name", "java.class.path"};
        for (int i = 0; i < sysProperties.length; i++) {
            config.addProperty("prop" + i, "${sys:" + sysProperties[i] + "}");
        }

        for (int i = 0; i < sysProperties.length; i++) {
            assertEquals(System.getProperty(sysProperties[i]), config.getString("prop" + i), "Wrong value for system property " + sysProperties[i]);
        }
    }

    /**
     * Tests interpolation when the referred property is not found.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationUnknownProperty(final Configuration config) {
        config.addProperty("test.interpol", "${unknown.property}");
        assertEquals("${unknown.property}", config.getString("test.interpol"));
    }

    /**
     * Tests an interpolation over multiple levels (i.e. the replacement of a variable is another variable and so on).
     *
     * @param config the configuration to test
     */
    public static void testMultipleInterpolation(final Configuration config) {
        config.setProperty("test.base-level", "/base-level");
        config.setProperty("test.first-level", "${test.base-level}/first-level");
        config.setProperty("test.second-level", "${test.first-level}/second-level");
        config.setProperty("test.third-level", "${test.second-level}/third-level");

        final String expectedValue = "/base-level/first-level/second-level/third-level";

        assertEquals(expectedValue, config.getString("test.third-level"));
    }
}
