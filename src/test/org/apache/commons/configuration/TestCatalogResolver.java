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

package org.apache.commons.configuration;

import junit.framework.TestCase;
import org.apache.commons.configuration.resolver.CatalogResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class TestCatalogResolver extends TestCase
{
    private static final String CATALOG_FILES = "conf/catalog.xml";
    private static final String PUBLIC_FILE = "conf/testResolver.xml";
    private static final String REWRITE_SYSTEM_FILE = "conf/test.properties.xml";
    private static final String REWRITE_SCHEMA_FILE = "conf/sample.xml";

    private CatalogResolver resolver;
    private XMLConfiguration config;

    protected void setUp() throws Exception
    {
        resolver = new CatalogResolver();
        resolver.setCatalogFiles(CATALOG_FILES);
        // resolver.setDebug(true);
        config = new XMLConfiguration();
        config.setEntityResolver(resolver);
    }

    protected void tearDown() throws Exception
    {
        resolver = null;
        config = null;
    }

    public void testPublic() throws Exception
    {
        config.setFileName(PUBLIC_FILE);
        config.load();
    }

    public void testRewriteSystem() throws Exception
    {
        config.setFileName(REWRITE_SYSTEM_FILE);
        config.load();
    }

    /**
     * Tests that the schema can be resolved and that XMLConfiguration will
     * validate the file using the schema.
     * @throws Exception
     */
    public void testSchemaResolver() throws Exception
    {
        config.setFileName(REWRITE_SCHEMA_FILE);
        config.setSchemaValidation(true);
        config.load();
    }

    public void testDebug() throws Exception
    {
        resolver.setDebug(true);
        // There is no really good way to check this except to do something
        // that causes debug output.
    }

    public void testLogger() throws Exception
    {
        Log log = LogFactory.getLog(this.getClass());
        resolver.setLogger(log);
        assertNotNull("No Logging returned", resolver.getLogger());
        assertTrue("Incorrect Logging", log == resolver.getLogger());
    }

}
