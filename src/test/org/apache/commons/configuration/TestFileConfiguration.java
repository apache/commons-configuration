/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.URL;
import java.io.File;

import junit.framework.TestCase;

/**
 * @author Emmanuel Bourg
 * @version $Revision: 1.2 $, $Date: 2004/11/19 13:19:50 $
 */
public class TestFileConfiguration extends TestCase
{
    public void testSetURL() throws Exception
    {
        // http URL
        FileConfiguration config = new PropertiesConfiguration();
        config.setURL(new URL("http://jakarta.apache.org/commons/configuration/index.html"));

        assertEquals("base path", "http://jakarta.apache.org/commons/configuration/", config.getBasePath());
        assertEquals("file name", "index.html", config.getFileName());

        // file URL
        config.setURL(new URL("file:/temp/test.properties"));
        assertEquals("base path", "/temp/", config.getBasePath());
        assertEquals("file name", "test.properties", config.getFileName());
    }

    public void testCreateFile1() throws Exception
    {
        File file = new File("target/test-resources/foo/bar/test.properties");
        if (file.exists())
        {
            file.delete();
            file.getParentFile().delete();
        }

        assertFalse("The file should not exist", file.exists());

        FileConfiguration config = new PropertiesConfiguration(file);
        config.save();

        assertTrue("The file doesn't exist", file.exists());
    }

    public void testCreateFile2() throws Exception
    {
        File file = new File("target/test-resources/foo/bar/test.properties");
        if (file.exists())
        {
            file.delete();
            file.getParentFile().delete();
        }

        assertFalse("The file should not exist", file.exists());

        FileConfiguration config = new PropertiesConfiguration();
        config.setFile(file);
        config.save();

        assertTrue("The file doesn't exist", file.exists());
    }

    public void testCreateFile3() throws Exception
    {
        File file = new File("target/test-resources/foo/bar/test.properties");
        if (file.exists())
        {
            file.delete();
            file.getParentFile().delete();
        }

        assertFalse("The file should not exist", file.exists());

        FileConfiguration config = new PropertiesConfiguration();
        config.save(file);

        assertTrue("The file doesn't exist", file.exists());
    }
}
