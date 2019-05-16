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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test class for BaseConfigurationXMLReader.
 *
 */
public class TestBaseConfigurationXMLReader
{
    private static final String[] CONTINENTS =
    {
        "Africa", "America", "Asia", "Australia", "Europe"
    };

    private BaseConfiguration config;
    private BaseConfigurationXMLReader configReader;

    @Before
    public void setUp() throws Exception
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

    @Test
    public void testParse() throws Exception
    {
        checkDocument(configReader, "config");
    }

    @Test(expected = SAXException.class)
    public void testParseSAXException() throws IOException, SAXException
    {
        configReader.setContentHandler(new TestContentHandler());
        configReader.parse("systemID");
    }

    @Test(expected = IOException.class)
    public void testParseIOException() throws SAXException, IOException
    {
        final BaseConfigurationXMLReader reader = new BaseConfigurationXMLReader();
        reader.parse("document");
    }

    @Test
    public void testSetRootName() throws Exception
    {
        final BaseConfigurationXMLReader reader = new BaseConfigurationXMLReader(config);
        reader.setRootName("apache");
        checkDocument(reader, "apache");
    }

    private void checkDocument(final BaseConfigurationXMLReader creader,
    final String rootName) throws Exception
    {
        final SAXSource source = new SAXSource(creader, new InputSource());
        final DOMResult result = new DOMResult();
        final Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.transform(source, result);
        final Node root = ((Document) result.getNode()).getDocumentElement();
        final JXPathContext ctx = JXPathContext.newContext(root);

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
    private void check(final JXPathContext ctx, final String path, final String[] values)
    {
        final Iterator<?> it = ctx.iterate(path);
        for (final String value : values) {
            assertTrue("Too few values", it.hasNext());
            assertEquals("Wrong property value", value, it.next());
        }
        assertFalse("Too many values", it.hasNext());
    }

    private void check(final JXPathContext ctx, final String path, final String value)
    {
        check(ctx, path, new String[]
        { value });
    }

    // A ContentHandler that raises an exception
    private static class TestContentHandler extends DefaultHandler
     {
        @Override
        public void characters(final char[] ch, final int start, final int length)
            throws SAXException
        {
            throw new SAXException("Test exception during parsing");
        }
    }
}
