/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.apache.commons.configuration.tree.xpath;

import java.util.Locale;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

/**
 * Implementation of the <code>NodePointerFactory</code> interface for
 * configuration nodes.
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class ConfigurationNodePointerFactory implements NodePointerFactory
{
    /** Constant for the order of this factory. */
    public static final int CONFIGURATION_NODE_POINTER_FACTORY_ORDER = 200;

    /**
     * Returns the order of this factory between other factories.
     *
     * @return this order's factory
     */
    public int getOrder()
    {
        return CONFIGURATION_NODE_POINTER_FACTORY_ORDER;
    }

    /**
     * Creates a node pointer for the specified bean. If the bean is a
     * configuration node, a corresponding pointer is returned.
     *
     * @param name the name of the node
     * @param bean the bean
     * @param locale the locale
     * @return a pointer for a configuration node if the bean is such a node
     */
    public NodePointer createNodePointer(QName name, Object bean, Locale locale)
    {
        if (bean instanceof ConfigurationNode)
        {
            return new ConfigurationNodePointer((ConfigurationNode) bean,
                    locale);
        }
        return null;
    }

    /**
     * Creates a node pointer for the specified bean. If the bean is a
     * configuration node, a corresponding pointer is returned.
     *
     * @param parent the parent node
     * @param name the name
     * @param bean the bean
     * @return a pointer for a configuration node if the bean is such a node
     */
    public NodePointer createNodePointer(NodePointer parent, QName name,
            Object bean)
    {
        if (bean instanceof ConfigurationNode)
        {
            return new ConfigurationNodePointer(parent,
                    (ConfigurationNode) bean);
        }
        return null;
    }
}
