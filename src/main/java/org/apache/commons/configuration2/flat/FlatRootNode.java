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
package org.apache.commons.configuration2.flat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * <p>
 * The root node in the hierarchy of flat nodes.
 * </p>
 * <p>
 * A flat configuration has two kinds of nodes: a single root node and an
 * arbitrary number of child nodes. This class represents the root node. The
 * root node is somewhat special. It does not have a name nor a value. It is the
 * only node in the whole structure that has children.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class FlatRootNode extends FlatNode
{
    /** Stores the child nodes of this root node. */
    private List<FlatNode> children;

    /**
     * Creates a new instance of <code>FlatRootNode</code>.
     */
    public FlatRootNode()
    {
        children = new ArrayList<FlatNode>();
    }

    /**
     * Creates a new child node and adds it to the list of children. This is a
     * short cut of <code>addChild(name, <b>false</b>)</code>.
     *
     * @param name the name of the child node
     * @return the newly created child node
     */
    @Override
    public FlatNode addChild(String name)
    {
        return addChild(name, false);
    }

    /**
     * Creates a new child node, adds it to the list of children, and sets its
     * <code>hasValue</code> flag. This implementation will create a new
     * <code>FlatLeafNode</code> instance.
     *
     * @param name the name of the child node
     * @param hasValue a flag whether the node already has a value; this flag
     *        impacts the behavior of the <code>setValue()</code> method: if
     *        it is <b>false</code>, the next <code>setValue()</code> call
     *        will add a new property to the configuration; otherwise an
     *        existing property value is overridden
     * @return the newly created child node
     */
    public FlatNode addChild(String name, boolean hasValue)
    {
        FlatLeafNode child = new FlatLeafNode(this, name, hasValue);
        children.add(child);
        return child;
    }

    /**
     * Returns the child node with the given index.
     *
     * @param index the index (0-based)
     * @return the child node with this index
     */
    @Override
    public FlatNode getChild(int index)
    {
        return children.get(index);
    }

    /**
     * Returns a list with all child nodes of this node.
     *
     * @return a list with all child nodes
     */
    @Override
    public List<FlatNode> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns a list with all child nodes with the given name.
     *
     * @param name the name of the desired children
     * @return a list with all children with this name
     */
    @Override
    public List<FlatNode> getChildren(String name)
    {
        List<FlatNode> result = new ArrayList<FlatNode>();
        for (FlatNode c : children)
        {
            if (name.equals(c.getName()))
            {
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Returns the number of children with the given name. If the name is
     * <b>null</b>, the total number of children is returned.
     *
     * @param name the desired name
     * @return the number of children with this name
     */
    @Override
    public int getChildrenCount(String name)
    {
        if (name == null)
        {
            return children.size();
        }

        else
        {
            int count = 0;
            for (FlatNode n : children)
            {
                if (name.equals(n.getName()))
                {
                    count++;
                }
            }

            return count;
        }
    }

    /**
     * Returns the name of this node. A root node does not have a name.
     *
     * @return the name of this node
     */
    @Override
    public String getName()
    {
        return null;
    }

    /**
     * Returns the parent of this node. A root node does not have a parent.
     *
     * @return the parent node
     */
    @Override
    public FlatNode getParent()
    {
        return null;
    }

    /**
     * Returns the value of this node. The root node does not have a value, so
     * result is always <b>null</b>.
     *
     * @param config the associated configuration
     * @return the value of this node
     */
    @Override
    public Object getValue(AbstractFlatConfiguration config)
    {
        return null;
    }

    /**
     * Returns the index of the associated values. For root nodes, this is an
     * undefined index.
     *
     * @return the value index
     */
    @Override
    public int getValueIndex()
    {
        return INDEX_UNDEFINED;
    }

    /**
     * Removes the specified child node. The corresponding value in the
     * associated configuration will also be removed.
     *
     * @param config the associated configuration
     * @param child the node to be removed
     * @throws ConfigurationRuntimeException if this node is not a child of this
     *         node
     */
    @Override
    public void removeChild(AbstractFlatConfiguration config, FlatNode child)
    {
        for (FlatNode c : children)
        {
            if (c == child)
            {
                int index = c.getValueIndex();
                if (index != INDEX_UNDEFINED)
                {
                    config.clearPropertyValue(c.getName(), index);
                }
                else
                {
                    config.clearProperty(c.getName());
                }
                children.remove(c);
                return;
            }
        }

        // child was not found
        throw new ConfigurationRuntimeException(
                "Node to remove is no child of this node!");
    }

    /**
     * Sets the value of this node. A root node cannot have a value. This
     * implementation will throw an exception.
     *
     * @param config the associated configuration
     * @param value the new value
     * @throws ConfigurationRuntimeException if the value cannot be set
     */
    @Override
    public void setValue(AbstractFlatConfiguration config, Object value)
    {
        throw new ConfigurationRuntimeException(
                "Cannot set the value of the root node of a flat configuration!");
    }

    /**
     * Returns the value index for the specified child node. This method is used
     * to determine the index of the value of a property with multiple values
     * that corresponds to the given child node. It counts the occurrences of
     * child nodes with the same name as the given child node.
     *
     * @param child the child node
     * @return the value index for this child node
     */
    int getChildValueIndex(FlatNode child)
    {
        int index = -1;
        boolean found = false;

        for (FlatNode c : children)
        {
            if (c == child)
            {
                if (++index > 0)
                {
                    return index;
                }
                found = true;
            }

            else if (child.getName().equals(c.getName()))
            {
                if (found)
                {
                    return index;
                }
                index++;
            }
        }

        return INDEX_UNDEFINED;
    }
}
