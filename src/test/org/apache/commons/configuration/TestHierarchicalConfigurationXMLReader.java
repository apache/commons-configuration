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
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: TestHierarchicalConfigurationXMLReader.java,v 1.2 2004/01/16 14:23:39 epugh Exp $
 */
public class TestHierarchicalConfigurationXMLReader extends TestCase
{
    private static final String TEST_FILE =
    "conf/testHierarchicalDOM4JConfiguration.xml";
    
    private HierarchicalConfigurationXMLReader parser;
    
    protected void setUp() throws Exception
    {
        HierarchicalDOM4JConfiguration config =
        new HierarchicalDOM4JConfiguration();
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
