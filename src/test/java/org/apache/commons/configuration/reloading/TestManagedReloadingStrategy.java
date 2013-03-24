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

package org.apache.commons.configuration.reloading;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for the ManagedReloadingStrategy class.
 *
 * @author Nicolas De loof
 * @version $Id$
 */
public class TestManagedReloadingStrategy
{
    /** Constant for the name of the test property. */
    private static final String PROPERTY = "string";

    /** Constant for the XML fragment to be written. */
    private static final String FMT_XML = "<configuration><" + PROPERTY
            + ">%s</" + PROPERTY + "></configuration>";

    /** A helper object for creating temporary files. */
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Writes a test configuration file containing a single property with the
     * given value.
     *
     * @param file the file to be written
     * @param value the value of the test property
     * @throws IOException if an error occurs
     */
    private void writeTestFile(File file, String value) throws IOException
    {
        FileWriter out = new FileWriter(file);
        out.write(String.format(FMT_XML, value));
        out.close();
    }

    @Test
    public void testManagedRefresh() throws Exception
    {
        File file = folder.newFile();
        // create the configuration file
        writeTestFile(file, "value1");

        // load the configuration
        XMLConfiguration config = new XMLConfiguration();
        config.setFile(file);
        config.load();
        ManagedReloadingStrategy strategy = new ManagedReloadingStrategy();
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString(PROPERTY));

        // change the file
        writeTestFile(file, "value2");

        // test the automatic reloading
        assertEquals("No automatic reloading", "value1", config.getString(PROPERTY));
        strategy.refresh();
        assertEquals("Modified value with enabled reloading", "value2", config.getString(PROPERTY));
    }

}
