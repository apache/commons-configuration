package org.apache.commons.configuration;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

/**
 * Test class for BaseConfigurationXMLReader.
 *
 * @version $Id: TestBaseConfigurationXMLReader.java,v 1.3 2004/02/27 17:41:34 epugh Exp $
 */
public class TestBaseConfigurationXMLReader extends TestCase
{
    private static final String[] CONTINENTS =
    {
        "Africa", "America", "Asia", "Australia", "Europe"
    };
    
    private BaseConfiguration config;
    private BaseConfigurationXMLReader configReader;
    
    protected void setUp() throws Exception
    {
        config = new BaseConfiguration();
        config.addProperty("world.continents.continent", Arrays.asList(CONTINENTS));
        config.addProperty("world.greeting", "Hello");
        config.addProperty("world.greeting", "Salute");
        config.addProperty("world.wish", "Peace");
        config.addProperty("application.mail.smtp", "smtp.mymail.org");
        config.addProperty("application.mail.pop", "pop3.mymail.org");
        config.addProperty("application.mail.account.type", "pop3");
        config.addProperty("application.mail.account.user", "postmaster");
        config.addProperty("application.mail.account.pwd", "?.-gulp*#");
        config.addProperty("application.mail.timeout", new Integer(42));
        config.addProperty("test", Boolean.TRUE);
        
        configReader = new BaseConfigurationXMLReader(config);
    }

    public void testParse() throws Exception
    {
        checkDocument(configReader, "config");
    }
    
    public void testParseSAXException() throws IOException
    {
        configReader.setContentHandler(new TestContentHandler());
        try
        {
            configReader.parse("systemID");
            fail("Expected exception was not thrown!");
        }
        catch(SAXException ex)
        {
        }
    }
    
    public void testParseIOException() throws SAXException
    {
        BaseConfigurationXMLReader reader = new BaseConfigurationXMLReader();
        try
        {
            reader.parse("document");
            fail("Expected exception was not thrown!");
        }
        catch(IOException ex)
        {
        }
    }
    
    public void testSetRootName() throws Exception
    {
        BaseConfigurationXMLReader reader = new BaseConfigurationXMLReader(config);
        reader.setRootName("apache");
        checkDocument(reader, "apache");
    }
    
    private void checkDocument(BaseConfigurationXMLReader creader,
    String rootName) throws Exception
    {
        SAXReader reader = new SAXReader(creader);
        Document document = reader.read("config");
        
        Element root = document.getRootElement();
        assertEquals(rootName, root.getName());
        assertEquals(3, root.elements().size());

        check(root, "world.continents.continent", CONTINENTS);
        check(root, "world.greeting", new String[] { "Hello", "Salute" });
        check(root, "world.wish", "Peace");
        check(root, "application.mail.smtp", "smtp.mymail.org");
        check(root, "application.mail.timeout", "42");
        check(root, "application.mail.account.type", "pop3");
        check(root, "application.mail.account.user", "postmaster");
        check(root, "test", "true");        
    }
    
    /**
     * Helper method for checking values in the DOM4J document.
     * @param root the root element
     * @param path the path to be checked
     * @param values the expected element values
     */
    private void check(Element root, String path, String[] values)
    {
        ConfigurationKey.KeyIterator keyIt =
        new ConfigurationKey(path).iterator();
        Element e = root;
        
        for(keyIt.nextKey(); keyIt.hasNext(); keyIt.nextKey())
        {
            Element child = e.element(keyIt.currentKey());
            assertNotNull(child);
            e = child;    
        }  /* for */
        
        List elems = e.elements(keyIt.currentKey());
        assertEquals(values.length, elems.size());
        Iterator it = elems.iterator();
        for(int i = 0; i < values.length; i++)
        {
            Element child = (Element) it.next();
            assertEquals(values[i], child.getTextTrim());
        }  /* for */
    }
    
    private void check(Element root, String path, String value)
    {
        check(root, path, new String[] { value });
    }
    
    // A ContentHandler that raises an exception
    private static class TestContentHandler extends DefaultHandler
     {
        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            throw new SAXException("Test exception during parsing");
        }
    }
}
