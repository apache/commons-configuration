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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;

import junitx.framework.Assert;

/**
 * A helper class that defines a bunch of tests related to variable
 * interpolation. It can be used for running these tests on different
 * configuration implementations.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class InterpolationTestHelper
{
    /**
     * Tests accessing and manipulating the interpolator object.
     *
     * @param config the configuration to test
     */
    public static void testGetInterpolator(final AbstractConfiguration config)
    {
        config.addProperty("var", "${echo:testVar}");
        final ConfigurationInterpolator interpol = config.getInterpolator();
        interpol.registerLookup("echo", new Lookup()
        {
            @Override
            public Object lookup(final String varName)
            {
                return "Value of variable " + varName;
            }
        });
        assertEquals("Wrong value of echo variable",
                "Value of variable testVar", config.getString("var"));
    }

    /**
     * Tests obtaining a configuration with all variables replaced by their
     * actual values.
     *
     * @param config the configuration to test
     * @return the interpolated configuration
     */
    public static Configuration testInterpolatedConfiguration(
            final AbstractConfiguration config)
    {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        config.setProperty("inttest.interpol", "${unknown.property}");
        config.setProperty("intkey.code",
                "${const:java.awt.event.KeyEvent.VK_CANCEL}");
        config.setProperty("inttest.sysprop", "${sys:java.version}");
        config.setProperty("inttest.numvalue", "3\\,1415");
        config.setProperty("inttest.value", "${inttest.numvalue}");
        config.setProperty("inttest.list", "${db}");
        config.addProperty("inttest.list", "${inttest.value}");

        final Configuration c = config.interpolatedConfiguration();
        assertEquals("Property not replaced",
                "/home/applicationRoot/db/hypersonic", c.getProperty("db"));
        assertEquals("Const variable not replaced", KeyEvent.VK_CANCEL,
                c.getInt("intkey.code"));
        assertEquals("Sys property not replaced", System
                .getProperty("java.version"), c.getProperty("inttest.sysprop"));
        assertEquals("Delimiter value not replaced", "3,1415", c
                .getProperty("inttest.value"));
        final List<?> lst = (List<?>) c.getProperty("inttest.list");
        assertEquals("Wrong number of list elements", 2, lst.size());
        assertEquals("List element 0 not replaced",
                "/home/applicationRoot/db/hypersonic", lst.get(0));
        assertEquals("List element 1 not replaced", "3,1415", lst
                        .get(1));
        assertEquals("Unresolvable variable not found",
                "${unknown.property}", c.getProperty("inttest.interpol"));

        return c;
    }

    /**
     * Tests basic interpolation facilities of the specified configuration.
     *
     * @param config the configuration to test
     */
    public static void testInterpolation(final Configuration config)
    {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        final String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        final String dbProp = "/home/applicationRoot/db/hypersonic";

        assertEquals("Checking interpolated variable", dbProp, config
                .getString("db"));
        assertEquals("lookup fails, leave variable as is", config
                .getString("dbFailedInterpolate"), unInterpolatedValue);

        config.setProperty("arrayInt", "${applicationRoot}/1");
        final String[] arrayInt = config.getStringArray("arrayInt");
        assertEquals("check first entry was interpolated",
                "/home/applicationRoot/1", arrayInt[0]);

        config.addProperty("path", Arrays.asList("/temp", "C:\\Temp","/usr/local/tmp"));
        config.setProperty("path.current", "${path}");
        assertEquals("Interpolation with multi-valued property",
                "/temp", config.getString("path.current"));
    }

    /**
     * Tests interpolation of constant values.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationConstants(final Configuration config)
    {
        config.addProperty("key.code",
                "${const:java.awt.event.KeyEvent.VK_CANCEL}");
        assertEquals("Wrong value of constant variable",
                KeyEvent.VK_CANCEL, config.getInt("key.code"));
        assertEquals("Wrong value when fetching constant from cache",
                KeyEvent.VK_CANCEL, config.getInt("key.code"));
    }

    /**
     * Tests interpolation of environment properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationEnvironment(final Configuration config)
    {
        final Map<String, String> env = System.getenv();
        for (final Map.Entry<String, String> e : env.entrySet())
        {
            config.addProperty("prop" + e.getKey(), "${env:" + e.getKey() + "}");
        }

        for (final Map.Entry<String, String> e : env.entrySet())
        {
            assertEquals("Wrong value for environment property " + e.getKey(),
                    e.getValue(), config.getString("prop" + e.getKey()));
        }
    }

    /**
     * Tests whether a variable can be escaped, so that it won't be
     * interpolated.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationEscaped(final Configuration config)
    {
        config.addProperty("var", "x");
        config.addProperty("escVar", "Use the variable $${${var}}.");
        assertEquals("Wrong escaped variable", "Use the variable ${x}.",
                config.getString("escVar"));
    }

    /**
     * Tests interpolation of localhost properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationLocalhost(final Configuration config)
    {
        final String[] localhostKeys =
        { "name", "canonical-name", "address" };
        String[] localhostValues = null;
        try {
            localhostValues = new String[] {
                    InetAddress.getLocalHost().getHostName(),
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    InetAddress.getLocalHost().getHostAddress() };
        } catch (final UnknownHostException e) {
            Assert.fail(e);
        }
        for (int i = 0; i < localhostKeys.length; i++)
        {
            config.addProperty("prop" + i, "${localhost:" + localhostKeys[i] + "}");
        }

        for (int i = 0; i < localhostKeys.length; i++)
        {
            assertEquals("Wrong value for system property "
                    + localhostKeys[i], localhostValues[i],
                    config.getString("prop" + i));
        }
    }

    /**
     * Tests an invalid interpolation that results in an infinite loop. This
     * loop should be detected and an exception should be thrown.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationLoop(final Configuration config)
    {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");

        try
        {
            config.getString("test.a");
            fail("IllegalStateException should have been thrown for looped property references");
        }
        catch (final IllegalStateException e)
        {
            // ok
        }

    }

    /**
     * Tests interpolation when a subset configuration is involved.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationSubset(final Configuration config)
    {
        config.addProperty("test.a", new Integer(42));
        config.addProperty("test.b", "${test.a}");
        assertEquals("Wrong interpolated value", 42, config
                .getInt("test.b"));
        final Configuration subset = config.subset("test");
        assertEquals("Wrong string property", "42", subset
                .getString("b"));
        assertEquals("Wrong int property", 42, subset.getInt("b"));
    }

    /**
     * Tests interpolation of system properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationSystemProperties(final Configuration config)
    {
        final String[] sysProperties =
        { "java.version", "java.vendor", "os.name", "java.class.path" };
        for (int i = 0; i < sysProperties.length; i++)
        {
            config.addProperty("prop" + i, "${sys:" + sysProperties[i] + "}");
        }

        for (int i = 0; i < sysProperties.length; i++)
        {
            assertEquals("Wrong value for system property "
                    + sysProperties[i], System.getProperty(sysProperties[i]),
                    config.getString("prop" + i));
        }
    }

    /**
     * Tests interpolation when the referred property is not found.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationUnknownProperty(final Configuration config)
    {
        config.addProperty("test.interpol", "${unknown.property}");
        assertEquals("Wrong interpolated unknown property",
                "${unknown.property}", config.getString("test.interpol"));
    }

    /**
     * Tests an interpolation over multiple levels (i.e. the replacement of a
     * variable is another variable and so on).
     *
     * @param config the configuration to test
     */
    public static void testMultipleInterpolation(final Configuration config)
    {
        config.setProperty("test.base-level", "/base-level");
        config
                .setProperty("test.first-level",
                        "${test.base-level}/first-level");
        config.setProperty("test.second-level",
                "${test.first-level}/second-level");
        config.setProperty("test.third-level",
                "${test.second-level}/third-level");

        final String expectedValue = "/base-level/first-level/second-level/third-level";

        assertEquals(config.getString("test.third-level"),
                        expectedValue);
    }
}
