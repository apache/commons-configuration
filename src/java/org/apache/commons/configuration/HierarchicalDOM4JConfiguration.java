package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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

import java.net.URL;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * <p>A specialized hierarchical configuration class that is able to parse
 * XML documents using DOM4J.</p>
 * <p>The parsed document will be stored keeping its structure. The
 * contained properties can be accessed using all methods supported by
 * the base class <code>HierarchicalProperties</code>.
 * 
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: HierarchicalDOM4JConfiguration.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public class HierarchicalDOM4JConfiguration
    extends HierarchicalConfiguration
    implements BasePathLoader
{
    /** Stores the file name of the document to be parsed.*/
    private String file;

    /** Stores the base path of this configuration.*/
    private String basePath;

    /**
     * Creates a new instance of <code>HierarchicalDOM4JConfiguration</code>.
     */
    public HierarchicalDOM4JConfiguration()
    {
        super();
    }

    /**
     * Creates a new instance of <code>HierarchicalDOM4JConfiguration</code>
     * and sets the default properties.
     * @param defaults the default properties
     */
    public HierarchicalDOM4JConfiguration(Configuration defaults)
    {
        super(defaults);
    }

    /**
     * Returns the name of the file to be parsed by this object.
     * @return the file to be parsed
     */
    public String getFileName()
    {
        return file;
    }

    /**
     * Sets the name of the file to be parsed by this object.
     * @param file the file to be parsed
     */
    public void setFileName(String file)
    {
        this.file = file;
    }

    /**
     * Returns the base path.
     * @return the base path
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Allows to set a base path. Relative file names are resolved based on
     * this path.
     * @param path the base path; this can be a URL or a file path
     */
    public void setBasePath(String path)
    {
        basePath = path;
    }

    /**
     * Loads and parses an XML document. The file to be loaded must have
     * been specified before.
     * @throws Exception if an error occurs
     */
    public void load() throws Exception
    {
        load(ConfigurationUtils.getURL(getBasePath(), getFileName()));
    }

    /**
     * Loads and parses the specified XML document.
     * @param url the URL to the XML document
     * @throws Exception if an error occurs
     */
    public void load(URL url) throws Exception
    {
        initProperties(new SAXReader().read(url));
    }

    /**
     * Initializes this configuration from an XML document.
     * @param document the document to be parsed
     */
    public void initProperties(Document document)
    {
        constructHierarchy(getRoot(), document.getRootElement());
    }

    /**
     * Helper method for building the internal storage hierarchy. The XML
     * elements are transformed into node objects.
     * @param node the actual node
     * @param element the actual XML element
     */
    private void constructHierarchy(Node node, Element element)
    {
        if (element.getTextTrim().length() > 0)
        {
            node.setValue(element.getTextTrim());
        } /* if */
        processAttributes(node, element);

        for (Iterator it = element.elementIterator(); it.hasNext();)
        {
            Element child = (Element) it.next();
            Node childNode = new Node(child.getName());
            constructHierarchy(childNode, child);
            node.addChild(childNode);
        } /* for */
    }

    /**
     * Helper method for constructing node objects for the attributes of the
     * given XML element.
     * @param node the actual node
     * @param element the actual XML element
     */
    private void processAttributes(Node node, Element element)
    {
        for (Iterator it = element.attributeIterator(); it.hasNext();)
        {
            Attribute attr = (Attribute) it.next();
            Node child =
                new Node(
                    ConfigurationKey.constructAttributeKey(attr.getName()));
            child.setValue(attr.getValue());
            node.addChild(child);
        } /* for */
    }
}
