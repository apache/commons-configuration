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
package org.apache.commons.configuration2.combined;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeHandlerRegistry;

/**
 * <p>
 * A special <code>NodeHandler</code> implementation that is used by a
 * <code>CombinedConfiguration</code>.
 * </p>
 * <p>
 * Depending on the contained configurations, a
 * <code>CombinedConfiguration</code> may have to deal with different node
 * objects at the same time. Thus it requires a powerful
 * <code>NodeHandler</code>. This class implements such a
 * <code>NodeHandler</code> that acts as a wrapper for multiple other concrete
 * <code>NodeHandler</code> implementations.
 * </p>
 * <p>
 * The basic idea is that the possible node types (that are currently contained
 * in the associated combined configuration) are registered at this class
 * together with the corresponding handlers. When then a
 * <code>NodeHandler</code> method is invoked, the class determines the
 * <code>NodeHandler</code> responsible for the affected node and delegates
 * the method call to it. That way a <code>CombinedConfiguration</code> can
 * deal with heterogeneous node structures in a transparent way making use of a
 * single node handler only.
 * </p>
 * <p>
 * In addition to the <code>NodeHandler</code> interface this class also
 * implements the <code>NodeHandlerRegistry</code> interface, which allows the
 * lookup of node handlers for given node objects. Supporting this interface is
 * important especially when combined configurations are contained in other
 * combined configurations. Because node handler registries can be combined to
 * hierarchical structures, too, it is then possible to query suitable node
 * handlers everywhere in the complex nodes hierarchy.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class CombinedConfigurationNodeHandler implements NodeHandler<Object>,
        NodeHandlerRegistry
{
    /**
     * Stores the currently known node handlers.
     */
    private Map<Class<?>, NodeHandler<?>> handlers;

    /** Stores the added sub registries. */
    private List<NodeHandlerRegistry> subRegistries;

    /** Stores the reference to the parent registry. */
    private NodeHandlerRegistry parentRegistry;

    /**
     * Creates a new instance of <code>CombinedConfigurationNodeHandler</code>.
     */
    public CombinedConfigurationNodeHandler()
    {
        subRegistries = new ArrayList<NodeHandlerRegistry>();
    }

    /**
     * Returns a map with the currently known node handlers.
     *
     * @return a map with the known node handlers
     */
    public Map<Class<?>, NodeHandler<?>> getHandlers()
    {
        return handlers;
    }

    /**
     * Sets a map with the currently known node handlers. This map allows to
     * query a node handler based on the class of a configuration node.
     *
     * @param handlers the map with the handlers
     */
    public void setHandlers(Map<Class<?>, NodeHandler<?>> handlers)
    {
        this.handlers = handlers;
    }

    /**
     * Returns the parent <code>NodeHandlerRegistry</code>. This can be
     * <b>null</b> if none has been set.
     *
     * @return the parent node handler registry
     */
    public NodeHandlerRegistry getParentRegistry()
    {
        return parentRegistry;
    }

    /**
     * Adds a value to an attribute. This implementation delegates to the node
     * handler responsible for the passed in node.
     *
     * @param node the node
     * @param name the name of the attribute
     * @param value the value to add
     */
    public void addAttributeValue(Object node, String name, Object value)
    {
        fetchHandler(node).addAttributeValue(node, name, value);
    }

    /**
     * Adds a new child node to a given node. This implementation delegates to
     * the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param name the name of the new child node
     * @return the newly created child node
     */
    public Object addChild(Object node, String name)
    {
        return fetchHandler(node).addChild(node, name);
    }

    /**
     * Adds a new child with a value to a parent node. This implementation
     * delegates to the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param name the name of the new child node
     * @param value the value
     * @return the newly created child node
     */
    public Object addChild(Object node, String name, Object value)
    {
        return fetchHandler(node).addChild(node, name, value);
    }

    /**
     * Returns the value of the specified attribute of the given node. This
     * implementation delegates to the node handler responsible for the passed
     * in node.
     *
     * @param node the node
     * @param name the name of the attribute
     * @return the value of this attribute
     */
    public Object getAttributeValue(Object node, String name)
    {
        return fetchHandler(node).getAttributeValue(node, name);
    }

    /**
     * Returns a list with the names of all attributes defined for the specified
     * node. This implementation delegates to the node handler responsible for
     * the passed in node.
     *
     * @param node the node
     * @return a list with the names of the attributes
     */
    public List<String> getAttributes(Object node)
    {
        return fetchHandler(node).getAttributes(node);
    }

    /**
     * Returns the child node at the given index from the specified node. This
     * implementation delegates to the node handler responsible for the passed
     * in node.
     *
     * @param node the node
     * @param index the index of the desired child
     * @return the child node at this index
     */
    public Object getChild(Object node, int index)
    {
        return fetchHandler(node).getChild(node, index);
    }

    /**
     * Returns a list with all children of the specified node. This
     * implementation delegates to the node handler responsible for the passed
     * in node.
     *
     * @param node the node
     * @return a list with all children of this node
     */
    public List<Object> getChildren(Object node)
    {
        return fetchHandler(node).getChildren(node);
    }

    /**
     * Returns a list with the child nodes of the specified node with the given
     * name. This implementation delegates to the node handler responsible for
     * the passed in node.
     *
     * @param node the node
     * @param name the name of the desired children
     * @return a list with all child nodes with this name
     */
    public List<Object> getChildren(Object node, String name)
    {
        return fetchHandler(node).getChildren(node, name);
    }

    /**
     * Returns the number of child nodes of the specified node with the given
     * name. This implementation delegates to the node handler responsible for
     * the passed in node.
     *
     * @param node the node
     * @param name the name of the children
     * @return the number of the selected children
     */
    public int getChildrenCount(Object node, String name)
    {
        return fetchHandler(node).getChildrenCount(node, name);
    }

    /**
     * Returns the parent node of the given node. This implementation delegates
     * to the node handler responsible for the passed in node.
     *
     * @param node the node
     * @return the parent node of this node
     */
    public Object getParent(Object node)
    {
        return fetchHandler(node).getParent(node);
    }

    /**
     * Returns the value of the specified node. This implementation delegates to
     * the node handler responsible for the passed in node.
     *
     * @param node the node
     * @return the value of this node
     */
    public Object getValue(Object node)
    {
        return fetchHandler(node).getValue(node);
    }

    /**
     * Returns a flag whether the specified node has any attributes. This
     * implementation delegates to the node handler responsible for the passed
     * in node.
     *
     * @param node the node
     * @return a flag whether this node has attributes
     */
    public boolean hasAttributes(Object node)
    {
        return fetchHandler(node).hasAttributes(node);
    }

    /**
     * Initializes the reference to the parent node handler registry. If a non
     * <b>null</b> is passed in, this object will register itself as a sub
     * registry.
     *
     * @param registry the parent registry
     */
    public void initNodeHandlerRegistry(NodeHandlerRegistry registry)
    {
        parentRegistry = registry;
        if (registry != null)
        {
            registry.addSubRegistry(this);
        }
    }

    /**
     * Tests whether the given node is defined. This implementation delegates to
     * the node handler responsible for the passed in node.
     *
     * @param node the node
     * @return a flag whether this node is defined
     */
    public boolean isDefined(Object node)
    {
        return fetchHandler(node).isDefined(node);
    }

    /**
     * Returns the name of the specified node. This implementation delegates to
     * the node handler responsible for the passed in node.
     *
     * @param node the node
     * @return the name of this node
     */
    public String nodeName(Object node)
    {
        return fetchHandler(node).nodeName(node);
    }

    /**
     * Removes an attribute from the specified node. This implementation
     * delegates to the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param name the name of the attribute to remove
     */
    public void removeAttribute(Object node, String name)
    {
        fetchHandler(node).removeAttribute(node, name);
    }

    /**
     * Removes a child node from the specified node. This implementation
     * delegates to the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param child the child node to be removed
     */
    public void removeChild(Object node, Object child)
    {
        fetchHandler(node).removeChild(node, child);
    }

    /**
     * Sets the value of an attribute of the specified node. This implementation
     * delegates to the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param name the name of the attribute to set
     * @param value the new value
     */
    public void setAttributeValue(Object node, String name, Object value)
    {
        fetchHandler(node).setAttributeValue(node, name, value);
    }

    /**
     * Sets the value of the specified node. This implementation delegates to
     * the node handler responsible for the passed in node.
     *
     * @param node the node
     * @param value the new value
     */
    public void setValue(Object node, Object value)
    {
        fetchHandler(node).setValue(node, value);
    }

    /**
     * Adds a new sub registry to this node handler registry.
     *
     * @param subreg the registry to add
     */
    public void addSubRegistry(NodeHandlerRegistry subreg)
    {
        subRegistries.add(subreg);
    }

    /**
     * Searches for a handler for the specified node in the internal map and the
     * registered sub registries.
     *
     * @param node the node in question
     * @param subClass a flag whether derived class are to be taken into account
     * @return the found handler or <b>null</b>
     */
    public NodeHandler<?> lookupHandler(Object node, boolean subClass)
    {
        NodeHandler<?> result = getHandlers().get(node.getClass());

        if (result == null && subClass)
        {
            // check for sub classes
            for (Class<?> cls : getHandlers().keySet())
            {
                if (cls.isInstance(node))
                {
                    result = getHandlers().get(cls);
                    // store directly in map for faster access the next time
                    getHandlers().put(node.getClass(), result);
                    break;
                }
            }
        }

        if (result == null)
        {
            result = searchSubRegistries(node, subClass);
        }

        return result;
    }

    /**
     * Tries to find a <code>NodeHandler</code> for the specified node. If a
     * parent registry is set, this call is delegated to it. Otherwise
     * <code>lookupHandler()</code> is called (first with a
     * <code>subClass</code> parameter of <b>false</b> and then <b>true</b>).
     * If the handler cannot be found in the local handler mapping and in the
     * sub registries either, an exception is thrown.
     *
     * @param node the node in question
     * @return a <code>NodeHandler</code> for this node
     * @throws ConfigurationRuntimeException if no compatible handler can be
     *         found
     */
    public NodeHandler<?> resolveHandler(Object node)
    {
        if (getParentRegistry() != null)
        {
            return getParentRegistry().resolveHandler(node);
        }

        else
        {
            NodeHandler<?> result = lookupHandler(node, false);
            if (result == null)
            {
                result = lookupHandler(node, true);
                if (result == null)
                {
                    throw new ConfigurationRuntimeException(
                            "Cannot find a compatible node handler for node "
                                    + node);
                }
            }

            return result;
        }
    }

    /**
     * Searches the sub registries for a handler for the specified node.
     *
     * @param node the node
     * @param subClass the sub class flag
     * @return the found handler or <b>null</b>
     */
    private NodeHandler<?> searchSubRegistries(Object node, boolean subClass)
    {
        for (NodeHandlerRegistry nhr : subRegistries)
        {
            NodeHandler<?> h = nhr.lookupHandler(node, subClass);
            if (h != null)
            {
                return h;
            }
        }

        return null;
    }

    /**
     * Helper method for obtaining a node handler for a given node. This method
     * casts the handler returned by <code>resolveHandler()</code> to a
     * <code>NodeHandler&lt;Object&gt;</code>, so it can be invoked directly
     * by other methods.
     *
     * @param node the affected node
     * @return the node handler for this node
     * @throws ConfigurationRuntimeException if no compatible handler can be
     *         found
     */
    @SuppressWarnings("unchecked")
    private NodeHandler<Object> fetchHandler(Object node)
    {
        return (NodeHandler<Object>) resolveHandler(node);
    }
}
