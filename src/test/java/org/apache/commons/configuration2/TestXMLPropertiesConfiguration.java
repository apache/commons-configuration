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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Test class for {@code XMLPropertiesConfiguration}.
 */
public class TestXMLPropertiesConfiguration {
    /** Constant for the name of the test file. */
    private static final String TEST_PROPERTIES_FILE = "test.properties.xml";

    /**
     * Helper method for loading a configuration file.
     *
     * @param fileName the name of the file to be loaded
     * @return the configuration instance
     * @throws ConfigurationException if an error occurs
     */
    private static XMLPropertiesConfiguration load(final String fileName) throws ConfigurationException {
        final XMLPropertiesConfiguration conf = new XMLPropertiesConfiguration();
        final FileHandler handler = new FileHandler(conf);
        handler.load(fileName);
        return conf;
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    @Test
    public void testDOMLoad() throws Exception {
        // Edge case
        assertThrows(NullPointerException.class, () -> new XMLPropertiesConfiguration(null));
        // Normal case
        final URL location = ConfigurationAssert.getTestURL(TEST_PROPERTIES_FILE);
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.setEntityResolver((publicId, systemId) -> new InputSource(getClass().getClassLoader().getResourceAsStream("properties.dtd")));
        final File file = new File(location.toURI());
        final Document doc = dBuilder.parse(file);
        final XMLPropertiesConfiguration conf = new XMLPropertiesConfiguration(doc.getDocumentElement());

        assertEquals("Description of the property list", conf.getHeader());

        assertFalse(conf.isEmpty());
        assertEquals("value1", conf.getProperty("key1"));
        assertEquals("value2", conf.getProperty("key2"));
        assertEquals("value3", conf.getProperty("key3"));
    }

    @Test
    public void testDOMSave() throws Exception {
        // load the configuration
        final XMLPropertiesConfiguration conf = load(TEST_PROPERTIES_FILE);

        // update the configuration
        conf.addProperty("key4", "value4");
        conf.clearProperty("key2");
        conf.setHeader("Description of the new property list");

        // save the configuration
        final File saveFile = newFile("test2.properties.xml", tempFolder);

        // save as DOM into saveFile
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        final Document document = dBuilder.newDocument();
        conf.save(document, document);
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        final Transformer transformer = tFactory.newTransformer();
        final DOMSource source = new DOMSource(document);
        final Result result = new StreamResult(saveFile);
        transformer.transform(source, result);

        // reload the configuration
        final XMLPropertiesConfiguration conf2 = load(saveFile.getAbsolutePath());

        // test the configuration
        assertEquals("Description of the new property list", conf2.getHeader());

        assertFalse(conf2.isEmpty());
        assertEquals("value1", conf2.getProperty("key1"));
        assertEquals("value3", conf2.getProperty("key3"));
        assertEquals("value4", conf2.getProperty("key4"));
    }

    @Test
    public void testLoad() throws Exception {
        final XMLPropertiesConfiguration conf = load(TEST_PROPERTIES_FILE);
        assertEquals("Description of the property list", conf.getHeader());

        assertFalse(conf.isEmpty());
        assertEquals("value1", conf.getProperty("key1"));
        assertEquals("value2", conf.getProperty("key2"));
        assertEquals("value3", conf.getProperty("key3"));
    }

    @Test
    public void testSave() throws Exception {
        // load the configuration
        final XMLPropertiesConfiguration conf = load(TEST_PROPERTIES_FILE);

        // update the configuration
        conf.addProperty("key4", "value4");
        conf.clearProperty("key2");
        conf.setHeader("Description of the new property list");

        // save the configuration
        final File saveFile = newFile("test2.properties.xml", tempFolder);
        final FileHandler saveHandler = new FileHandler(conf);
        saveHandler.save(saveFile);

        // reload the configuration
        final XMLPropertiesConfiguration conf2 = load(saveFile.getAbsolutePath());

        // test the configuration
        assertEquals("Description of the new property list", conf2.getHeader());

        assertFalse(conf2.isEmpty());
        assertEquals("value1", conf2.getProperty("key1"));
        assertEquals("value3", conf2.getProperty("key3"));
        assertEquals("value4", conf2.getProperty("key4"));
    }
}
