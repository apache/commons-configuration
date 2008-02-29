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
package org.apache.commons.configuration2.expr;

import java.util.List;

/**
 * <p>
 * Definition of an interface for dealing with the nodes of a hierarchical
 * configuration.
 * </p>
 * <p>
 * Different configuration implementations internally use different node
 * structures. This interface provides a generic way of dealing with these
 * structures by providing common query and manipulation methods. That way large
 * parts of the functionality required by a hierarchical configuration can be
 * implemented in a central base class, while tasks specific for a concrete node
 * structure are delegated to an implementation of this interface.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @param <T> the type of the nodes this handler deals with
 */
public interface NodeHandler<T>
{
    /**
     * Returns the name of the specified node
     *
     * @param node the node
     * @return the name of this node
     */
    String nodeName(T node);

    /**
     * Returns the value of the specified node.
     *
     * @param node the node
     * @return the value of this node
     */
    Object getValue(T node);

    /**
     * Sets the value of the specified node.
     *
     * @param node the node
     * @param value the new value
     */
    void setValue(T node, Object value);

    /**
     * Returns the parent of the specified node.
     *
     * @param node the node
     * @return the parent node
     */
    T getParent(T node);

    /**
     * Adds a child with the given node name to the specified node.
     *
     * @param node the node
     * @param name the name of the new child
     * @return the newly added child
     */
    T addChild(T node, String name);

    /**
     * Returns a list with all children of the specified node.
     *
     * @param node the node
     * @return a list with the child nodes of this node
     */
    List<T> getChildren(T node);

    /**
     * Returns a list of all children of the specified node with the given name.
     *
     * @param node the node
     * @param name the name of the desired child nodes
     * @return a list with all children with the given name
     */
    List<T> getChildren(T node, String name);

    /**
     * Returns the child with the given index of the specified node.
     *
     * @param node the node
     * @param index the index (0-based)
     * @return the child with the given index
     */
    T getChild(T node, int index);

    /**
     * Removes the specified child from the given node.
     *
     * @param node the node
     * @param child the child to be removed
     */
    void removeChild(T node, T child);

    /**
     * Returns a list with the names of all attributes of the specified node.
     *
     * @param node the node
     * @return a list with all attributes of this node
     */
    List<String> getAttributes(T node);

    /**
     * Returns the value of the specified attribute from the given node.
     *
     * @param node the node
     * @param name the name of the attribute
     * @return the value of this attribute
     */
    Object getAttributeValue(T node, String name);

    /**
     * Sets the value of an attribute for the specified node.
     *
     * @param node the node
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    void setAttributeValue(T node, String name, Object value);

    /**
     * Removes the attribute with the specified name from the given node.
     *
     * @param node the node
     * @param name the name of the attribute to be removed
     */
    void removeAttribute(T node, String name);
}
