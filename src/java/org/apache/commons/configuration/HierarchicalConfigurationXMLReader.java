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

import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>A specialized SAX2 XML parser that "parses" hierarchical
 * configuration objects.</p>
 * <p>This class mimics to be a SAX conform XML parser. Instead of parsing
 * XML documents it processes a <code>Configuration</code> object and
 * generates SAX events for the single properties defined there. This enables
 * the whole world of XML processing for configuration objects.</p>
 * <p>The <code>HierarchicalConfiguration</code> object to be parsed can be
 * specified using a constructor or the <code>setConfiguration()</code> method.
 * This object will be processed by the <code>parse()</code> methods. Note
 * that these methods ignore their argument.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: HierarchicalConfigurationXMLReader.java,v 1.2 2004/02/12 12:59:19 epugh Exp $
 */
public class HierarchicalConfigurationXMLReader
extends ConfigurationXMLReader
{
    /** Stores the configuration object to be parsed.*/
    private HierarchicalConfiguration configuration;

    /**
     * Creates a new instance of
     * <code>HierarchicalConfigurationXMLReader</code>.
     */
    public HierarchicalConfigurationXMLReader()
    {
        super();
    }

    /**
     * Creates a new instance of
     * <code>HierarchicalConfigurationXMLReader</code> and sets the
     * configuration to be parsed.
     * @param config the configuration object
     */
    public HierarchicalConfigurationXMLReader(
    HierarchicalConfiguration config)
    {
        this();
        setConfiguration(config);
    }

    /**
     * Returns the configuration object to be parsed.
     * @return the configuration object to be parsed
     */
    public HierarchicalConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration object to be parsed.
     * @param config the configuration object to be parsed
     */
    public void setConfiguration(HierarchicalConfiguration config)
    {
        configuration = config;
    }

    /**
     * Returns the configuration object to be processed.
     * @return the actual configuration object
     */
    public Configuration getParsedConfiguration()
    {
        return getConfiguration();
    }

    /**
     * Processes the actual configuration object to generate SAX parsing
     * events.
     * @throws IOException if no configuration has been specified
     * @throws SAXException if an error occurs during parsing
     */
    protected void processKeys()
    {
        getConfiguration().getRoot().visit(new SAXVisitor(), null);
    }

    /**
     * A specialized visitor class for generating SAX events for a
     * hierarchical node structure.
     *
     * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
     */
    class SAXVisitor extends HierarchicalConfiguration.NodeVisitor
    {
        /** Constant for the attribute type.*/
        private static final String ATTR_TYPE = "CDATA";

        /**
         * Visits the specified node after its children have been processed.
         * @param node the actual node
         * @param key the key of this node
         */
        public void visitAfterChildren(Node node, ConfigurationKey key)
        {
            if(!isAttributeNode(node))
            {
                fireElementEnd(nodeName(node));
            }  /* if */
        }

        /**
         * Visits the specified node.
         * @param node the actual node
         * @param key the key of this node
         */
        public void visitBeforeChildren(Node node, ConfigurationKey key)
        {
            if(!isAttributeNode(node))
            {
                fireElementStart(nodeName(node), fetchAttributes(node));

                if(node.getValue() != null)
                {
                    fireCharacters(node.getValue().toString());
                }  /* if */
            }  /* if */
        }

        /**
         * Checks if iteration should be terminated. This implementation stops
         * iteration after an exception has occurred.
         * @return a flag if iteration should be stopped
         */
        public boolean terminate()
        {
            return getException() != null;
        }

        /**
         * Returns an object with all attributes for the specified node.
         * @param node the actual node
         * @return an object with all attributes of this node
         */
        protected Attributes fetchAttributes(Node node)
        {
            AttributesImpl attrs = new AttributesImpl();
            AbstractConfiguration.Container children = node.getChildren();

            for(int i = 0; i < children.size(); i++)
            {
                Node child = (Node) children.get(i);
                if(isAttributeNode(child) && child.getValue() != null)
                {
                    String attr = ConfigurationKey.attributeName(
                    child.getName());
                    attrs.addAttribute(NS_URI, attr, attr, ATTR_TYPE,
                    child.getValue().toString());
                }  /* if */
            }  /* for */

            return attrs;
        }

        /**
         * Helper method for determining the name of a node. If a node has no
         * name (which is true for the root node), the specified default name
         * will be used.
         * @param node the node to be checked
         * @return the name for this node
         */
        private String nodeName(Node node)
        {
            return (node.getName() == null) ? getRootName() : node.getName();
        }

        /**
         * Checks if the specified node is an attribute node. In the node
         * hierarchy attributes are stored as normal child nodes, but with
         * special names.
         * @param node the node to be checked
         * @return a flag if this is an attribute node
         */
        private boolean isAttributeNode(Node node)
        {
            return ConfigurationKey.isAttributeKey(node.getName());
        }
    }
}
