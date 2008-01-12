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

package org.apache.commons.configuration2.reloading;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.reloading.ManagedReloadingStrategy;

import junit.framework.TestCase;

/**
 * Test case for the ManagedReloadingStrategy class.
 *
 * @author Nicolas De loof
 */
public class TestManagedReloadingStrategy extends TestCase
{

    public void testManagedRefresh() throws Exception
    {
        File file = new File("target/testReload.properties");
        if (file.exists())
        {
            file.delete();
        }
        // create the configuration file
        FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.flush();
        out.close();

        // load the configuration
        PropertiesConfiguration config = new PropertiesConfiguration("target/testReload.properties");
        ManagedReloadingStrategy strategy = new ManagedReloadingStrategy();
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString("string"));

        // change the file
        out = new FileWriter(file);
        out.write("string=value2");
        out.flush();
        out.close();

        // test the automatic reloading
        assertEquals("No automatic reloading", "value1", config.getString("string"));
        strategy.refresh();
        assertEquals("Modified value with enabled reloading", "value2", config.getString("string"));
    }

}
