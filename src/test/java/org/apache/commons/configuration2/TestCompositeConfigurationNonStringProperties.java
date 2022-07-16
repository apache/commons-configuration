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

import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test if non-string properties are handled correctly.
 *
 */
public class TestCompositeConfigurationNonStringProperties extends BaseNonStringProperties {
    /** The File that we test with */
    private final String testProperties = ConfigurationAssert.getTestFile("test.properties").getAbsolutePath();

    @BeforeEach
    public void setUp() throws Exception {
        final CompositeConfiguration cc = new CompositeConfiguration();
        final PropertiesConfiguration pc = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pc);
        handler.setFileName(testProperties);
        handler.load();
        cc.addConfiguration(pc);
        conf = cc;
        nonStringTestHolder.setConfiguration(conf);
    }
}
