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
package org.apache.commons.configuration2.expr.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A specialized node iterator implementation that deals with attribute nodes.
 *
 * @author Oliver Heger
 * @version $Id$
 */
class ConfigurationNodeIteratorAttribute<T> extends
        ConfigurationNodeIteratorBase<T>
{
    /** Constant for the wildcard node name. */
    private static final String WILDCARD = "*";

    /** Stores the parent node pointer. */
    private ConfigurationNodePointer<T> parentPointer;

    /** A list with the names of the managed attributes. */
    private List<String> attributeNames;

    /**
     * Creates a new instance of <code>ConfigurationNodeIteratorAttribute</code>.
     *
     * @param parent the parent node pointer
     * @param name the name of the selected attribute
     */
    public ConfigurationNodeIteratorAttribute(
            ConfigurationNodePointer<T> parent, QName name)
    {
        super(parent, false);
        parentPointer = parent;
        attributeNames = createAttributeNameList(parent, name);
    }

    /**
     * Determines which attributes are selected based on the passed in node
     * name.
     *
     * @param parent the parent node pointer
     * @param name the name of the selected attribute
     * @return a list with the selected attributes
     */
    protected List<String> createAttributeNameList(
            ConfigurationNodePointer<T> parent, QName name)
    {
        if (name.getPrefix() != null)
        {
            // namespace prefixes are not supported
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        if (!WILDCARD.equals(name.getName()))
        {
            if (parent.getNodeHandler().getAttributeValue(
                    parent.getConfigurationNode(), name.getName()) != null)
            {
                result.add(name.getName());
            }
        }
        else
        {
            result.addAll(parent.getNodeHandler().getAttributes(
                    parent.getConfigurationNode()));
        }

        return result;
    }

    /**
     * Creates a pointer for the node at the specified position.
     *
     * @param position
     * @return a pointer for the attribute at this position
     */
    @Override
    protected NodePointer createNodePointer(int position)
    {
        return new ConfigurationAttributePointer<T>(parentPointer,
                attributeNames.get(position));
    }

    /**
     * Returns the size of the managed iteration.
     *
     * @return the iteration size
     */
    @Override
    protected int size()
    {
        return attributeNames.size();
    }
}
