/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.tree.xpath;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.lang3.Strings;

/**
 * A specialized node iterator implementation that deals with attribute nodes.
 *
 * @param <T> the type of the nodes this iterator deals with
 */
final class ConfigurationNodeIteratorAttribute<T> extends AbstractConfigurationNodeIterator<T> {

    /** Constant for the wildcard node name. */
    private static final String WILDCARD = "*";

    /** Stores the parent node pointer. */
    private final ConfigurationNodePointer<T> parentPointer;

    /** A list with the names of the managed attributes. */
    private final List<String> attributeNames;

    /**
     * Creates a new instance of {@code ConfigurationNodeIteratorAttribute}.
     *
     * @param parent the parent node pointer
     * @param qName the name of the selected attribute
     */
    public ConfigurationNodeIteratorAttribute(final ConfigurationNodePointer<T> parent, final QName qName) {
        super(parent, false);
        parentPointer = parent;
        attributeNames = createAttributeDataList(parent, qName);
    }

    /**
     * Helper method for checking whether an attribute is defined and adding it to the list of attributes to iterate over.
     *
     * @param parent the parent node pointer
     * @param result the result list
     * @param name the name of the current attribute
     */
    private void addAttributeData(final ConfigurationNodePointer<T> parent, final List<String> result, final String name) {
        if (parent.getNodeHandler().getAttributeValue(parent.getConfigurationNode(), name) != null) {
            result.add(name);
        }
    }

    /**
     * Determines which attributes are selected based on the passed in node name.
     *
     * @param parent the parent node pointer
     * @param qName the name of the selected attribute
     * @return a list with the selected attributes
     */
    private List<String> createAttributeDataList(final ConfigurationNodePointer<T> parent, final QName qName) {
        final List<String> result = new ArrayList<>();
        if (!WILDCARD.equals(qName.getName())) {
            addAttributeData(parent, result, qualifiedName(qName));
        } else {
            final Set<String> names = new LinkedHashSet<>(parent.getNodeHandler().getAttributes(parent.getConfigurationNode()));
            final String prefix = qName.getPrefix() != null ? prefixName(qName.getPrefix(), null) : null;
            names.forEach(n -> {
                if (prefix == null || Strings.CS.startsWith(n, prefix)) {
                    addAttributeData(parent, result, n);
                }
            });
        }

        return result;
    }

    /**
     * Creates a pointer for the node at the specified position.
     *
     * @param position the desired position
     * @return a pointer for the attribute at this position
     */
    @Override
    protected NodePointer createNodePointer(final int position) {
        return new ConfigurationAttributePointer<>(parentPointer, attributeNames.get(position));
    }

    /**
     * Returns the size of the managed iteration.
     *
     * @return the iteration size
     */
    @Override
    protected int size() {
        return attributeNames.size();
    }
}
