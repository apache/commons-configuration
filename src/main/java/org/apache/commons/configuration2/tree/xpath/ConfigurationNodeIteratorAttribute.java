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

package org.apache.commons.configuration2.tree.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A specialized node iterator implementation that deals with attribute nodes.
 *
 * @author Oliver Heger
 * @version $Id$
 */
class ConfigurationNodeIteratorAttribute extends
        ConfigurationNodeIteratorBase
{
    /** Constant for the wildcard node name.*/
    private static final String WILDCARD = "*";

    /**
     * Creates a new instance of <code>ConfigurationNodeIteratorAttribute</code>.
     * @param parent the parent node pointer
     * @param name the name of the selected attribute
     */
    public ConfigurationNodeIteratorAttribute(NodePointer parent, QName name)
    {
        super(parent, false);
        initSubNodeList(createSubNodeList((ConfigurationNode) parent.getNode(), name));
    }

    /**
     * Determines which attributes are selected based on the passed in node
     * name.
     * @param node the current node
     * @param name the name of the selected attribute
     * @return a list with the selected attributes
     */
    protected List<ConfigurationNode> createSubNodeList(ConfigurationNode node, QName name)
    {
        if (name.getPrefix() != null)
        {
            // namespace prefixes are not supported
            return Collections.emptyList();
        }

        List<ConfigurationNode> result = new ArrayList<ConfigurationNode>();
        if (!WILDCARD.equals(name.getName()))
        {
            result.addAll(node.getAttributes(name.getName()));
        }
        else
        {
            result.addAll(node.getAttributes());
        }

        return result;
    }
}
