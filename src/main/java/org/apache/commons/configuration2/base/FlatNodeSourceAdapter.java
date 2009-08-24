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
package org.apache.commons.configuration2.base;

import java.util.Iterator;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * An adapter implementation for converting a &quot;flat&quot;
 * {@link ConfigurationSource} into a hierarchical one based on {@link FlatNode}
 * node objects.
 * </p>
 * <p>
 * An instance of this class is initialized with a reference to a {@code
 * ConfigurationSource} object. All the data of this source is extracted and
 * transformed into a hierarchical structure of {@link FlatNode} objects. On
 * this node structure the operations defined by the {@code
 * HierarchicalConfigurationSource} interface are implemented.
 * </p>
 * <p>
 * The way the flat nodes are implemented, all manipulations on the node
 * structure are directly reflected by the {@code ConfigurationSource}. The
 * other direction works, too: This adapter registers itself as {@code
 * ConfigurationSourceListener} at the wrapped {@code ConfigurationSource} (this
 * implies that the wrapped source supports event notifications). Whenever a
 * change event is received the node structure is invalidated, so it has to be
 * re-created at next access. This ensures that the content of the wrapped
 * {@code ConfigurationSource} and the flat node structure are always in sync.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class FlatNodeSourceAdapter implements
        HierarchicalConfigurationSource<FlatNode>, ConfigurationSourceListener
{
    /** The wrapped configuration source. */
    private final ConfigurationSource originalSource;

    /** The node handler for accessing the flat nodes. */
    private final FlatNodeHandler nodeHandler;

    /** The root node of the hierarchy of flat nodes. */
    private FlatNode root;

    /**
     * Creates a new instance of {@code FlatNodeSourceAdapter} and initializes
     * it with the {@code ConfigurationSource} to be transformed into a
     * hierarchical one.
     *
     * @param wrappedSource the wrapped {@code ConfigurationSource} (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the passed in {@code
     *         ConfigurationSource} is <b>null</b>
     */
    public FlatNodeSourceAdapter(ConfigurationSource wrappedSource)
    {
        if (wrappedSource == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationSource to be wrapped must not be null!");
        }

        originalSource = wrappedSource;
        nodeHandler = new FlatNodeHandler(wrappedSource);
        wrappedSource.addConfigurationSourceListener(this);
    }

    /**
     * Returns the original {@code ConfigurationSource} that is wrapped by this
     * adapter.
     *
     * @return the original {@code ConfigurationSource}
     */
    public ConfigurationSource getOriginalSource()
    {
        return originalSource;
    }

    /**
     * Notifies this adapter about a change in the original {@code
     * ConfigurationSource}. This implementation invalidates the root node if
     * this change was not caused by a change in the nodes structure.
     *
     * @param event the change event
     */
    public void configurationSourceChanged(ConfigurationSourceEvent event)
    {
        if (!event.isBeforeUpdate() && !nodeHandler.isInternalUpdate())
        {
            invalidateRootNode();
        }
    }

    /**
     * Adds a {@code ConfigurationSourceListener} to this source. This
     * implementation delegates to the original source. Therefore the listener
     * is notified each time the original source is modified. This includes
     * updates performed by this adapter.
     *
     * @param l the {@code ConfigurationSourceListener} to be added
     */
    public void addConfigurationSourceListener(ConfigurationSourceListener l)
    {
        getOriginalSource().addConfigurationSourceListener(l);
    }

    /**
     * Clears this {@code ConfigurationSource}. This implementation delegates to
     * the original source. This also causes the hierarchy of flat nodes managed
     * internally to be cleared.
     */
    public void clear()
    {
        getOriginalSource().clear();
    }

    /**
     * Returns the {@code NodeHandler} for dealing with the nodes used by this
     * {@code HierarchicalConfigurationSource}. This implementation returns a
     * handler for {@link FlatNode} objects.
     *
     * @return the {@code NodeHandler} for this source
     */
    public NodeHandler<FlatNode> getNodeHandler()
    {
        return nodeHandler;
    }

    /**
     * Returns the root node of this {@code HierarchicalConfigurationSource}. If
     * necessary, the hierarchy of nodes is created.
     *
     * @return the root node of this source
     */
    public synchronized FlatNode getRootNode()
    {
        if (root == null)
        {
            root = constructNodeHierarchy();
        }

        return root;
    }

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * source. As is true for
     * {@link #addConfigurationSourceListener(ConfigurationSourceListener)},
     * this implementation delegates to the original source.
     *
     * @param l the {@code ConfigurationSourceListener} to be removed
     * @return a flag whether the listener could be removed
     */
    public boolean removeConfigurationSourceListener(
            ConfigurationSourceListener l)
    {
        return getOriginalSource().removeConfigurationSourceListener(l);
    }

    /**
     * Sets a new root node. This operation is not supported, so an exception is
     * thrown.
     *
     * @param root the new root node
     * @throws UnsupportedOperationException as this operation is not supported
     */
    public void setRootNode(FlatNode root)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Creates a hierarchy of {@code FlatNode} objects that corresponds to the
     * data stored in the wrapped {@code ConfigurationSource}. This
     * implementation relies on the method {@code getKeys()} of the wrapped
     * source to obtain the data required for constructing the node hierarchy.
     *
     * @return the root node of the hierarchy
     */
    protected FlatNode constructNodeHierarchy()
    {
        FlatRootNode root = new FlatRootNode();
        for (Iterator<String> it = getOriginalSource().getKeys(); it.hasNext();)
        {
            String key = it.next();
            int count = ConfigurationSourceUtils.valueCount(
                    getOriginalSource(), key);
            for (int i = 0; i < count; i++)
            {
                root.addChild(key, true);
            }
        }

        return root;
    }

    /**
     * Invalidates the root node of the flat nodes hierarchy. This method is
     * called whenever a change of the wrapped source was detected. It sets the
     * root node to <b>null</b>, so that the hierarchy has to be re-created on
     * next access.
     */
    protected synchronized void invalidateRootNode()
    {
        root = null;
    }
}
