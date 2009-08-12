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
package org.apache.commons.configuration2.base;

import java.awt.event.KeyEvent;

import junit.framework.Assert;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrLookup;

/**
 * A helper class that defines a bunch of tests related to variable
 * interpolation. It can be used for running these tests on different
 * configuration implementations.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class InterpolationTestHelper
{
    /**
     * Tests basic interpolation facilities of the specified configuration.
     *
     * @param config the configuration to test
     */
    public static void testInterpolation(Configuration<?> config)
    {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        String dbProp = "/home/applicationRoot/db/hypersonic";

        Assert.assertEquals("Checking interpolated variable", dbProp, config
                .getString("db"));
        Assert.assertEquals("lookup fails, leave variable as is", config
                .getString("dbFailedInterpolate"), unInterpolatedValue);

        config.setProperty("arrayInt", "${applicationRoot}/1");
        String[] arrayInt = config.getStringArray("arrayInt");
        Assert.assertEquals("check first entry was interpolated",
                "/home/applicationRoot/1", arrayInt[0]);

        config.addProperty("path", "/temp,C:\\Temp,/usr/local/tmp");
        config.setProperty("path.current", "${path}");
        Assert.assertEquals("Interpolation with multi-valued property",
                "/temp", config.getString("path.current"));
    }

    /**
     * Tests an interpolation over multiple levels (i.e. the replacement of a
     * variable is another variable and so on).
     *
     * @param config the configuration to test
     */
    public static void testMultipleInterpolation(Configuration<?> config)
    {
        config.setProperty("test.base-level", "/base-level");
        config
                .setProperty("test.first-level",
                        "${test.base-level}/first-level");
        config.setProperty("test.second-level",
                "${test.first-level}/second-level");
        config.setProperty("test.third-level",
                "${test.second-level}/third-level");

        String expectedValue = "/base-level/first-level/second-level/third-level";

        Assert
                .assertEquals(config.getString("test.third-level"),
                        expectedValue);
    }

    /**
     * Tests an invalid interpolation that results in an infinite loop. This
     * loop should be detected and an exception should be thrown.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationLoop(Configuration<?> config)
    {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");

        try
        {
            config.getString("test.a");
            Assert
                    .fail("IllegalStateException should have been thrown for looped property references");
        }
        catch (IllegalStateException e)
        {
            // ok
        }

    }

    /**
     * Tests interpolation when the referred property is not found.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationUnknownProperty(Configuration<?> config)
    {
        config.addProperty("test.interpol", "${unknown.property}");
        Assert.assertEquals("Wrong interpolated unknown property",
                "${unknown.property}", config.getString("test.interpol"));
    }

    /**
     * Tests interpolation of system properties.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationSystemProperties(Configuration<?> config)
    {
        String[] sysProperties =
        { "java.version", "java.vendor", "os.name", "java.class.path" };
        for (int i = 0; i < sysProperties.length; i++)
        {
            config.addProperty("prop" + i, "${sys:" + sysProperties[i] + "}");
        }

        for (int i = 0; i < sysProperties.length; i++)
        {
            Assert.assertEquals("Wrong value for system property "
                    + sysProperties[i], System.getProperty(sysProperties[i]),
                    config.getString("prop" + i));
        }
    }

    /**
     * Tests interpolation of constant values.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationConstants(Configuration<?> config)
    {
        config.addProperty("key.code",
                "${const:java.awt.event.KeyEvent.VK_CANCEL}");
        Assert.assertEquals("Wrong value of constant variable",
                KeyEvent.VK_CANCEL, config.getInt("key.code"));
        Assert.assertEquals("Wrong value when fetching constant from cache",
                KeyEvent.VK_CANCEL, config.getInt("key.code"));
    }

    /**
     * Tests whether a variable can be escaped, so that it won't be
     * interpolated.
     *
     * @param config the configuration to test
     */
    public static void testInterpolationEscaped(Configuration<?> config)
    {
        config.addProperty("var", "x");
        config.addProperty("escVar", "Use the variable $${${var}}.");
        Assert.assertEquals("Wrong escaped variable", "Use the variable ${x}.",
                config.getString("escVar"));
    }

    /**
     * Tests accessing and manipulating the interpolator object.
     *
     * @param config the configuration to test
     */
    public static void testGetInterpolator(Configuration<?> config)
    {
        config.addProperty("var", "${echo:testVar}");
        ConfigurationInterpolator interpol = config.getInterpolator();
        interpol.registerLookup("echo", new StrLookup()
        {
            @Override
            public String lookup(String varName)
            {
                return "Value of variable " + varName;
            }
        });
        Assert.assertEquals("Wrong value of echo variable",
                "Value of variable testVar", config.getString("var"));
    }
}
