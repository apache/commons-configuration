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

import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalConfigurationXMLReader.
 *
 * @version $Id: TestHierarchicalConfigurationXMLReader.java,v 1.4 2004/07/12 12:14:38 ebourg Exp $
 */
public class TestHierarchicalConfigurationXMLReader extends TestCase
{
    private static final String TEST_FILE = "conf/testHierarchicalXMLConfiguration.xml";
    
    private HierarchicalConfigurationXMLReader parser;
    
    protected void setUp() throws Exception
    {
        HierarchicalXMLConfiguration config =
        new HierarchicalXMLConfiguration();
        config.setFileName(TEST_FILE);
        config.load();
        parser = new HierarchicalConfigurationXMLReader(config);
    }

    public void testParse() throws Exception
    {
        SAXReader reader = new SAXReader(parser);
        Document document = reader.read("someSysID");
        
        Element root = document.getRootElement();
        assertEquals("config", root.getName());
        assertEquals(1, root.elements().size());
        Iterator itRoot = root.elementIterator();
        Element elemTabs = (Element) itRoot.next();
        
        assertEquals(2, elemTabs.elements().size());
        List tables = elemTabs.elements();
        Element tabUsr = (Element) tables.get(0);
        assertEquals("table", tabUsr.getName());
        Element elemName = tabUsr.element("name");
        assertNotNull(elemName);
        assertEquals("users", elemName.getTextTrim());
        Element elemFields = tabUsr.element("fields");
        assertNotNull(elemFields);
        assertEquals(5, elemFields.elements().size());
        
        List attribs = tabUsr.attributes();
        assertEquals(1, attribs.size());
        Attribute attr = (Attribute) attribs.get(0);
        assertEquals("tableType", attr.getName());
        assertEquals("system", attr.getValue());
    }
}
