/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

/**
 * test for loading and saving xml properties files
 *
 * @version $Id: TestXMLConfiguration.java,v 1.2 2004/07/12 14:38:29 ebourg Exp $
 */
public class TestXMLConfiguration extends TestCase
{
    /** The File that we test with */
    private String testProperties = new File("conf/test.xml").getAbsolutePath();
    private String testBasePath = new File("conf").getAbsolutePath();
    private XMLConfiguration conf;

    protected void setUp() throws Exception
    {
        conf = new XMLConfiguration(new File(testProperties));
    }

    public void testGetProperty() throws Exception
    {
        assertEquals("value", conf.getProperty("element"));
    }

    public void testGetAttribute() throws Exception
    {
        assertEquals("element3{name}", "foo", conf.getProperty("element3[@name]"));
    }

    public void testClearAttribute() throws Exception
    {
        conf.clearProperty("element3[@name]");
        assertEquals("element3[@name]", null, conf.getProperty("element3[@name]"));
    }

    public void testSetAttribute() throws Exception
    {
        conf.setProperty("element3[@name]", "bar");
        assertEquals("element3[@name]", "bar", conf.getProperty("element3[@name]"));
    }

    public void testAddAttribute() throws Exception
    {
        conf.addProperty("element3[@name]", "bar");

        List list = conf.getList("element3[@name]");
        assertNotNull("null list", list);
        assertTrue("'foo' element missing", list.contains("foo"));
        assertTrue("'bar' element missing", list.contains("bar"));
        assertEquals("list size", 2, list.size());
    }

    public void testGetComplexProperty() throws Exception
    {
        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    public void testSettingFileNames() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFileName(testProperties);
        assertEquals(testProperties.toString(), conf.getFileName());

        conf.setBasePath(testBasePath);
        conf.setFileName("hello.xml");
        assertEquals("hello.xml", conf.getFileName());
        assertEquals(testBasePath.toString(), conf.getBasePath());
        assertEquals(new File(testBasePath, "hello.xml"), conf.getFile());

        conf.setBasePath(testBasePath);
        conf.setFileName("/subdir/hello.xml");
        assertEquals("/subdir/hello.xml", conf.getFileName());
        assertEquals(testBasePath.toString(), conf.getBasePath());
        assertEquals(new File(testBasePath, "/subdir/hello.xml"), conf.getFile());
    }

    public void testLoad() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFileName(testProperties);
        conf.load();

        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadWithBasePath() throws Exception
    {
        conf = new XMLConfiguration();

        conf.setFileName("test.xml");
        conf.setBasePath(testBasePath);
        conf.load();

        assertEquals("I'm complex!", conf.getProperty("element2.subelement.subsubelement"));
    }

    public void testAddProperty()
    {
        // add a property to a non initialized xml configuration
        XMLConfiguration config = new XMLConfiguration();
        config.addProperty("test.string", "hello");

        assertEquals("'test.string'", "hello", config.getString("test.string"));
    }
}
