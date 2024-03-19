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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@code SystemConfiguration}.
 */
public class TestSystemConfiguration {
    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Tests an append operation with a system configuration while system properties are modified from another thread. This
     * is related to CONFIGURATION-570.
     */
    @Test
    public void testAppendWhileConcurrentAccess() throws InterruptedException {
        final AtomicBoolean stop = new AtomicBoolean();
        final String property = SystemConfiguration.class.getName() + ".testProperty";
        final Thread t = new Thread(() -> {
            boolean setValue = true;
            while (!stop.get()) {
                if (setValue) {
                    System.setProperty(property, "true");
                } else {
                    System.clearProperty(property);
                }
                setValue = !setValue;
            }
        });
        try {
            t.start();

            final SystemConfiguration config = new SystemConfiguration();
            final PropertiesConfiguration props = new PropertiesConfiguration();
            props.append(config);

            stop.set(true);
            t.join();
            for (final Iterator<String> keys = config.getKeys(); keys.hasNext();) {
                final String key = keys.next();
                if (!property.equals(key)) {
                    assertEquals(config.getString(key), props.getString(key), "Wrong value for " + key);
                }
            }
        } finally {
            System.clearProperty(property);
        }
    }

    /**
     * Tests whether the configuration can be used to change system properties.
     */
    @Test
    public void testChangeSystemProperties() {
        final String testProperty = "someTest";
        final SystemConfiguration config = new SystemConfiguration();
        config.setProperty(testProperty, "true");
        assertEquals("true", System.getProperty(testProperty));
    }

    @Test
    public void testSetSystemProperties() {
        final PropertiesConfiguration props = new PropertiesConfiguration();
        props.addProperty("test.name", "Apache");
        SystemConfiguration.setSystemProperties(props);
        assertEquals("Apache", System.getProperty("test.name"));
    }

    /**
     * Tests whether system properties can be set from a configuration file.
     */
    @Test
    public void testSetSystemPropertiesFromPropertiesFile() throws ConfigurationException, IOException {
        final File file = newFile("sys.properties", tempFolder);
        final PropertiesConfiguration pconfig = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(pconfig);
        pconfig.addProperty("fromFile", Boolean.TRUE);
        handler.setFile(file);
        handler.save();
        SystemConfiguration.setSystemProperties(handler.getBasePath(), handler.getFileName());
        final SystemConfiguration sconf = new SystemConfiguration();
        assertTrue(sconf.getBoolean("fromFile"));
    }

    @Test
    public void testSystemConfiguration() {
        final Properties props = System.getProperties();
        props.put("test.number", "123");

        final Configuration conf = new SystemConfiguration();
        assertEquals(123, conf.getInt("test.number"));
    }
}
