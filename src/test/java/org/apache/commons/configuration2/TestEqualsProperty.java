package org.apache.commons.configuration2;

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

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Test;

/**
 * test if properties that contain a "=" will be loaded correctly.
 *
 */
public class TestEqualsProperty
{
    /** The File that we test with */
    private final String testProperties = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();

    @Test
    public void testEquals() throws Exception
    {
        final PropertiesConfiguration conf = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.setFileName(testProperties);
        handler.load();

        final String equals = conf.getString("test.equals");
        assertEquals("value=one", equals);
    }
}
