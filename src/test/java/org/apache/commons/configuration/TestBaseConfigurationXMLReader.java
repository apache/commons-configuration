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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;

/**
 * Test class for BaseConfigurationXMLReader.
 *
 * @version $Id$
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
        SAXSource source = new SAXSource(creader, new InputSource());
        DOMResult result = new DOMResult();
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        try
        {
            //When executed on a JDK 1.3 this line throws a NoSuchMethodError
            //somewhere deep in Xalan. We simply ignore this.
            trans.transform(source, result);
        }
        catch(NoSuchMethodError ex)
        {
            return;
        }
        Node root = ((Document) result.getNode()).getDocumentElement();
        JXPathContext ctx = JXPathContext.newContext(root);
        
        assertEquals("Wrong root name", rootName, root.getNodeName());
        assertEquals("Wrong number of children", 3, ctx.selectNodes("/*").size());

        check(ctx, "world/continents/continent", CONTINENTS);
        check(ctx, "world/greeting", new String[] { "Hello", "Salute" });
        check(ctx, "world/wish", "Peace");
        check(ctx, "application/mail/smtp", "smtp.mymail.org");
        check(ctx, "application/mail/timeout", "42");
        check(ctx, "application/mail/account/type", "pop3");
        check(ctx, "application/mail/account/user", "postmaster");
        check(ctx, "test", "true");        
    }
    
    /**
     * Helper method for checking values in the created document.
     *
     * @param ctx the JXPath context
     * @param path the path to be checked
     * @param values the expected element values
     */
    private void check(JXPathContext ctx, String path, String[] values)
    {
        Iterator it = ctx.iterate(path);
        for (int i = 0; i < values.length; i++)
        {
            assertTrue("Too few values", it.hasNext());
            assertEquals("Wrong property value", values[i], it.next());
        } /* for */
        assertFalse("Too many values", it.hasNext());
    }

    private void check(JXPathContext ctx, String path, String value)
    {
        check(ctx, path, new String[]
        { value });
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
