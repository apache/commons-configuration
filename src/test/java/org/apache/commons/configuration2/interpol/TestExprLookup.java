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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Test;

/**
 * Test class for ExprLookup.
 *
 */
public class TestExprLookup
{
    private static File TEST_FILE = ConfigurationAssert.getTestFile("test.xml");

    private static String PATTERN1 =
        "String.replace(Util.message, 'Hello', 'Goodbye') + System.getProperty('user.name')";
    private static String PATTERN2 =
        "'$[element] ' + String.trimToEmpty('$[space.description]')";

    /**
     * Loads the test configuration.
     *
     * @return the test configuration
     * @throws ConfigurationException if an error occurs
     */
    private static XMLConfiguration loadConfig() throws ConfigurationException
    {
        final XMLConfiguration config = new XMLConfiguration();
        final FileHandler handler = new FileHandler(config);
        handler.load(TEST_FILE);
        return config;
    }

    @Test
    public void testLookup() throws Exception
    {
        final ConsoleAppender app = new ConsoleAppender(new SimpleLayout());
        final Log log = LogFactory.getLog("TestLogger");
        final Logger logger = ((Log4JLogger)log).getLogger();
        logger.addAppender(app);
        logger.setLevel(Level.DEBUG);
        logger.setAdditivity(false);
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("String", org.apache.commons.lang3.StringUtils.class));
        vars.add(new ExprLookup.Variable("Util", new Utility("Hello")));
        vars.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
        final XMLConfiguration config = loadConfig();
        final ConfigurationLogger testLogger = new ConfigurationLogger("TestLogger");
        config.setLogger(testLogger);
        final ExprLookup lookup = new ExprLookup(vars);
        lookup.setInterpolator(config.getInterpolator());
        lookup.setLogger(testLogger);
        String str = lookup.lookup(PATTERN1);
        assertTrue(str.startsWith("Goodbye"));
        str = lookup.lookup(PATTERN2);
        assertTrue("Incorrect value: " + str, str.equals("value Some text"));
        logger.removeAppender(app);
    }

    /**
     * Tests a lookup() operation if no ConfigurationInterpolator object has been set.
     */
    @Test
    public void testLookupNoConfigurationInterpolator()
    {
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("String", org.apache.commons.lang3.StringUtils.class));
        final ExprLookup lookup = new ExprLookup(vars);
        final String value = "test";
        assertEquals("Wrong result", value, lookup.lookup(value));
    }

    /**
     * Tests whether variables can be queried.
     */
    @Test
    public void testGetVariables()
    {
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("String", org.apache.commons.lang3.StringUtils.class));
        final ExprLookup lookup = new ExprLookup(vars);
        assertEquals("Wrong variables", vars, lookup.getVariables());
    }

    /**
     * Tests that getVariables() returns a copy of the original variables.
     */
    @Test
    public void testGetVariablesDefensiveCopy()
    {
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("String", org.apache.commons.lang3.StringUtils.class));
        final ExprLookup lookup = new ExprLookup(vars);
        final ExprLookup.Variables vars2 = lookup.getVariables();
        vars2.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
        assertEquals("Modified variables", vars, lookup.getVariables());
    }

    /**
     * Tests an expression that does not yield a string.
     */
    @Test
    public void testLookupNonStringExpression() throws ConfigurationException
    {
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
        final ExprLookup lookup = new ExprLookup(vars);
        final XMLConfiguration config = loadConfig();
        lookup.setInterpolator(config.getInterpolator());
        final String pattern = "System.currentTimeMillis()";
        final String result = lookup.lookup(pattern);
        assertNotEquals("Not replaced", pattern, result);
    }

    /**
     * Tests an expression that yields a null value.
     */
    @Test
    public void testLookupNullExpression() throws ConfigurationException
    {
        final ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
        final ExprLookup lookup = new ExprLookup(vars);
        final XMLConfiguration config = loadConfig();
        lookup.setInterpolator(config.getInterpolator());
        assertNull("Wrong result",
                lookup.lookup("System.getProperty('undefined.property')"));
    }

    public static class Utility
    {
        String message;

        public Utility(final String msg)
        {
            this.message = msg;
        }

        public String getMessage()
        {
            return message;
        }

        public String str(final String str)
        {
            return str;
        }
    }
}
