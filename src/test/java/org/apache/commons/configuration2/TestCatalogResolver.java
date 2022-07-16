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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CatalogResolver.
 *
 */
public class TestCatalogResolver {
    private static final String CATALOG_FILES = "catalog.xml";
    private static final String PUBLIC_FILE = "testResolver.xml";
    private static final String REWRITE_SYSTEM_FILE = "test.properties.xml";
    private static final String REWRITE_SCHEMA_FILE = "sample.xml";

    private CatalogResolver resolver;
    private XMLConfiguration config;

    /**
     * Loads the test configuration from the specified file.
     *
     * @param fileName the file name
     * @throws ConfigurationException if an error occurs
     */
    private void load(final String fileName) throws ConfigurationException {
        final FileHandler handler = new FileHandler(config);
        handler.load(fileName);
    }

    @BeforeEach
    public void setUp() throws Exception {
        resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        // resolver.setDebug(true);
        config = new XMLConfiguration();
        config.setEntityResolver(resolver);
    }

    @AfterEach
    public void tearDown() throws Exception {
        resolver = null;
        config = null;
    }

    @Test
    public void testDebug() throws Exception {
        resolver.setDebug(true);
        // There is no really good way to check this except to do something
        // that causes debug output.
    }

    @Test
    public void testLogger() throws Exception {
        final ConfigurationLogger log = new ConfigurationLogger(this.getClass());
        resolver.setLogger(log);
        assertNotNull(resolver.getLogger());
        assertSame(log, resolver.getLogger());
    }

    @Test
    public void testPublic() {
        assertDoesNotThrow(() -> load(PUBLIC_FILE));
    }

    @Test
    public void testRewriteSystem() {
        assertDoesNotThrow(() -> load(REWRITE_SYSTEM_FILE));
    }

    /**
     * Tests that the schema can be resolved and that XMLConfiguration will validate the file using the schema.
     */
    @Test
    public void testSchemaResolver() {
        assertDoesNotThrow(() -> load(REWRITE_SCHEMA_FILE));
    }
}
