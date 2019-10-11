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

import java.io.File;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Test;

/**
 * Test class for default lookups.
 *
 */
public class TestDefaultLookups
{
    private static File TEST_FILE = ConfigurationAssert.getTestFile("testDefaultLookups.xml");

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
    public void testLookups() throws Exception
    {
        final XMLConfiguration config = loadConfig();
        
        config.getInterpolator().registerLookup(DefaultLookups.DATE.getPrefix(), DefaultLookups.DATE.getLookup());
        config.getInterpolator().registerLookup(DefaultLookups.SYSTEM_PROPERTIES.getPrefix(), DefaultLookups.SYSTEM_PROPERTIES.getLookup());
        assertEquals("Wrong value for date lookup result.", 15, config.getString("element").length()); // result should be like 201910/08211122 
        assertEquals("Wrong value for system_properties lookup result.","" + System.getProperty("user.name") + "/"
        		+ System.getProperty("os.name"), config.getString("element2"));
    }

    
}
