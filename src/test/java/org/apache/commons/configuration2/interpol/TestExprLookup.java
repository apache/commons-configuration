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

import junit.framework.TestCase;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ConfigurationAssert;

/**
 * Test class for ExprLookup.
 *
 * @version $Id$
 */
public class TestExprLookup extends TestCase
{
    private static File TEST_FILE = ConfigurationAssert.getTestFile("test.xml");

    private static String PATTERN1 =
        "String.replace(Util.message, 'Hello', 'Goodbye') + System.getProperty('user.name')";
    private static String PATTERN2 =
        "'$[element] ' + String.trimToEmpty('$[space.description]')";

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * Clears the test environment. Here the static cache of the constant lookup
     * class is wiped out.
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLookup() throws Exception
    {
        ConsoleHandler handler = new ConsoleHandler();
        Logger logger = Logger.getLogger("TestLogger");
        logger.addHandler(handler);
        logger.setLevel(Level.FINE);
        ExprLookup.Variables vars = new ExprLookup.Variables();
        vars.add(new ExprLookup.Variable("String", org.apache.commons.lang.StringUtils.class));
        vars.add(new ExprLookup.Variable("Util", new Utility("Hello")));
        vars.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
        XMLConfiguration config = new XMLConfiguration(TEST_FILE);
        config.setLogger(logger);
        ExprLookup lookup = new ExprLookup(vars);
        lookup.setConfiguration(config);
        String str = lookup.lookup(PATTERN1);
        assertTrue(str.startsWith("Goodbye"));
        str = lookup.lookup(PATTERN2);
        assertTrue("Incorrect value: " + str, str.equals("value Some text"));
        logger.removeHandler(handler);

    }



    public static class Utility
    {
        String message;

        public Utility(String msg)
        {
            this.message = msg;
        }

        public String getMessage()
        {
            return message;
        }

        public String str(String str)
        {
            return str;
        }
    }
}
