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

import org.apache.commons.configuration2.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeTreeWalker;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * A specialized SAX2 XML parser that "parses" hierarchical configuration
 * objects.
 * </p>
 * <p>
 * This class mimics to be a SAX conform XML parser. Instead of parsing XML
 * documents it processes a {@code Configuration} object and generates SAX
 * events for the single properties defined there. This enables the whole world
 * of XML processing for configuration objects.
 * </p>
 * <p>
 * The {@code HierarchicalConfiguration} object to be parsed can be specified
 * using a constructor or the {@code setConfiguration()} method. This object
 * will be processed by the {@code parse()} methods. Note that these methods
 * ignore their argument.
 * </p>
 *
 * @param <T> the type of the nodes supported by this reader
 */
public class HierarchicalConfigurationXMLReader<T> extends
        ConfigurationXMLReader
{
    /** Stores the configuration object to be parsed. */
    private HierarchicalConfiguration<T> configuration;

    /**
     * Creates a new instance of {@code HierarchicalConfigurationXMLReader}.
     */
    public HierarchicalConfigurationXMLReader()
    {
        super();
    }

    /**
     * Creates a new instance of {@code HierarchicalConfigurationXMLReader} and
     * sets the configuration to be parsed.
     *
     * @param config the configuration object
     */
    public HierarchicalConfigurationXMLReader(
            final HierarchicalConfiguration<T> config)
    {
        this();
        setConfiguration(config);
    }

    /**
     * Returns the configuration object to be parsed.
     *
     * @return the configuration object to be parsed
     */
    public HierarchicalConfiguration<T> getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration object to be parsed.
     *
     * @param config the configuration object to be parsed
     */
    public void setConfiguration(final HierarchicalConfiguration<T> config)
    {
        configuration = config;
    }

    /**
     * Returns the configuration object to be processed.
     *
     * @return the actual configuration object
     */
    @Override
    public Configuration getParsedConfiguration()
    {
        return getConfiguration();
    }

    /**
     * Processes the actual configuration object to generate SAX parsing events.
     */
    @Override
    protected void processKeys()
    {
        final NodeHandler<T> nodeHandler =
                getConfiguration().getNodeModel().getNodeHandler();
        NodeTreeWalker.INSTANCE.walkDFS(nodeHandler.getRootNode(),
                new SAXVisitor(), nodeHandler);
    }

    /**
     * A specialized visitor class for generating SAX events for a hierarchical
     * node structure.
     */
    private class SAXVisitor extends ConfigurationNodeVisitorAdapter<T>
    {
        /** Constant for the attribute type. */
        private static final String ATTR_TYPE = "CDATA";

        /**
         * Visits the specified node after its children have been processed.
         *
         * @param node the actual node
         * @param handler the node handler
         */
        @Override
        public void visitAfterChildren(final T node, final NodeHandler<T> handler)
        {
            fireElementEnd(nodeName(node, handler));
        }

        /**
         * Visits the specified node.
         *
         * @param node the actual node
         * @param handler the node handler
         */
        @Override
        public void visitBeforeChildren(final T node, final NodeHandler<T> handler)
        {
            fireElementStart(nodeName(node, handler),
                    fetchAttributes(node, handler));

            final Object value = handler.getValue(node);
            if (value != null)
            {
                fireCharacters(value.toString());
            }
        }

        /**
         * Checks if iteration should be terminated. This implementation stops
         * iteration after an exception has occurred.
         *
         * @return a flag if iteration should be stopped
         */
        @Override
        public boolean terminate()
        {
            return getException() != null;
        }

        /**
         * Returns an object with all attributes for the specified node.
         *
         * @param node the current node
         * @param handler the node handler
         * @return an object with all attributes of this node
         */
        protected Attributes fetchAttributes(final T node, final NodeHandler<T> handler)
        {
            final AttributesImpl attrs = new AttributesImpl();

            for (final String attr : handler.getAttributes(node))
            {
                final Object value = handler.getAttributeValue(node, attr);
                if (value != null)
                {
                    attrs.addAttribute(NS_URI, attr, attr, ATTR_TYPE,
                            value.toString());
                }
            }

            return attrs;
        }

        /**
         * Helper method for determining the name of a node. If a node has no
         * name (which is true for the root node), the specified default name
         * will be used.
         *
         * @param node the node to be checked
         * @param handler the node handler
         * @return the name for this node
         */
        private String nodeName(final T node, final NodeHandler<T> handler)
        {
            final String nodeName = handler.nodeName(node);
            return (nodeName == null) ? getRootName() : nodeName;
        }
    }
}
