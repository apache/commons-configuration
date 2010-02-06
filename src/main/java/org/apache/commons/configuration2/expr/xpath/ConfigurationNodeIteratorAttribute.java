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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A specialized node iterator implementation that deals with attribute nodes.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class ConfigurationNodeIteratorAttribute<T> extends
        ConfigurationNodeIteratorBase<T>
{
    /** Constant for the wildcard node name. */
    private static final String WILDCARD = "*";

    /** Stores the parent node pointer. */
    private ConfigurationNodePointer<T> parentPointer;

    /** A list with the data of the managed attributes. */
    private List<AttributeData> attributeData;

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
        attributeData = createAttributeDataList(parent, name);
    }

    /**
     * Determines which attributes are selected based on the passed in node
     * name.
     *
     * @param parent the parent node pointer
     * @param name the name of the selected attribute
     * @return a list with the selected attributes
     */
    protected List<AttributeData> createAttributeDataList(
            ConfigurationNodePointer<T> parent, QName name)
    {
        if (name.getPrefix() != null)
        {
            // namespace prefixes are not supported
            return Collections.emptyList();
        }

        List<AttributeData> result = new ArrayList<AttributeData>();
        if (!WILDCARD.equals(name.getName()))
        {
            addAttributeData(parent, result, name.getName());
        }
        else
        {
            Set<String> names = new LinkedHashSet<String>(parent.getNodeHandler().getAttributes(parent.getConfigurationNode()));
            for(String n : names)
            {
                addAttributeData(parent, result, n);
            }
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
        AttributeData ad = attributeData.get(position);
        return new ConfigurationAttributePointer<T>(parentPointer,
                ad.name, ad.valueIndex);
    }

    /**
     * Returns the size of the managed iteration.
     *
     * @return the iteration size
     */
    @Override
    protected int size()
    {
        return attributeData.size();
    }

    /**
     * Helper method for adding data about an attribute to the data list. If the
     * attribute has multiple values, correct indices will be set.
     *
     * @param parent the parent pointer
     * @param lst the result list
     * @param name the name of the attribute
     */
    private void addAttributeData(ConfigurationNodePointer<T> parent,
            List<AttributeData> lst, String name)
    {
        Object value = parent.getNodeHandler().getAttributeValue(
                parent.getConfigurationNode(), name);
        if (value != null)
        {
            if (value instanceof Collection<?>)
            {
                // add entries for all values
                int idx = 0;
                for (Iterator<?> it = ((Collection<?>) value).iterator(); it
                        .hasNext(); idx++)
                {
                    lst.add(new AttributeData(name, idx));
                    it.next();
                }
            }

            else
            {
                lst.add(new AttributeData(name,
                        ConfigurationAttributePointer.IDX_UNDEF));
            }
        }
    }

    /**
     * A simple data class for storing the information required to select an
     * attribute.
     */
    private static class AttributeData
    {
        /** The name of the attribute. */
        String name;

        /** The index of the value. */
        int valueIndex;

        /**
         * Creates a new instance of <code>AttributeData</code>
         *
         * @param n the name
         * @param idx the value index
         */
        public AttributeData(String n, int idx)
        {
            name = n;
            valueIndex = idx;
        }
    }
}
