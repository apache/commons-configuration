package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: TestBaseConfigurationXMLReader.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class TestBaseConfigurationXMLReader extends TestCase
{
    private static final String[] CONTINENTS =
    {
        "Africa", "America", "Asia", "Australia", "Europe"
    };
    
    private BaseConfiguration config;
    private BaseConfigurationXMLReader configReader;
    
    public TestBaseConfigurationXMLReader(String arg0)
    {
        super(arg0);
    }

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
