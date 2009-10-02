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
import org.apache.commons.configuration2.tree.ConfigurationNode;

/**
 * <p>
 * An adapter implementation for converting a &quot;flat&quot;
 * {@link FlatConfigurationSource} into a hierarchical one.
 * </p>
 * <p>
 * {@link ConfigurationImpl}, the main implementation of the
 * {@link Configuration} interface, requires a hierarchical configuration source
 * for accessing configuration settings. It does not work with sources
 * implementing only the {@link FlatConfigurationSource} interface out of the box.
 * With this adapter class a {@link FlatConfigurationSource} object can be treated
 * as a {@link HierarchicalConfigurationSource}.
 * </p>
 * <p>
 * The idea behind this class is that it dynamically populates a
 * {@link ConfigurationImpl} object (living in memory) with the properties
 * stored in the configuration source. This causes the data to be stored in a
 * truly hierarchical structure enabling sophisticated queries. The in-memory
 * configuration source of this configuration is then used to implement the
 * methods of the {@link HierarchicalConfigurationSource} interface.
 * </p>
 * <p>
 * Changes at the data of the configuration used for the transformation are not
 * written back into the original {@link FlatConfigurationSource} automatically.
 * This can be done by calling the {@code writeBack()} method. This will clear
 * the original source and then copy all data from this source into it.
 * </p>
 * <p>
 * It is possible to configure this adapter to register as an event listener at
 * the original {@link FlatConfigurationSource}. Every change event fired by the
 * {@link FlatConfigurationSource} causes the configuration used internally to be
 * re-constructed with the current data of the {@link FlatConfigurationSource}. This
 * will throw away all changes made at this hierarchical source! So this mode
 * should only be used for providing a read-only hierarchical view for a plain
 * {@link FlatConfigurationSource}.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class HierarchicalSourceAdapter implements
        HierarchicalConfigurationSource<ConfigurationNode>,
        ConfigurationSourceListener
{
    /**
     * Stores the original source that is transformed by this adapter.
     */
    private final FlatConfigurationSource originalSource;

    /** The configuration used internally for the transformation. */
    private Configuration<ConfigurationNode> transformedConfig;

    /**
     * A flag whether the original configuration source should be monitored to
     * react on changes.
     */
    private final boolean monitorChanges;

    /**
     * Creates a new instance of {@code HierarchicalSourceAdapter} and
     * initializes it with the {@code ConfigurationSource} to wrap and the flag
     * whether changes of this {@code ConfigurationSource} should be monitored.
     *
     * @param wrappedSource the original {@code ConfigurationSource} (must not
     *        be <b>null</b>)
     * @param monitorChanges the flag whether changes of the original source
     *        should cause this source to update its data
     * @throws IllegalArgumentException if the {@code ConfigurationSource} is
     *         <b>null</b>
     */
    public HierarchicalSourceAdapter(FlatConfigurationSource wrappedSource,
            boolean monitorChanges)
    {
        if (wrappedSource == null)
        {
            throw new IllegalArgumentException(
                    "Original ConfigurationSource must not be null!");
        }

        originalSource = wrappedSource;
        this.monitorChanges = monitorChanges;

        if (monitorChanges)
        {
            wrappedSource.addConfigurationSourceListener(this);
        }
    }

    /**
     * Creates a new instance of {@code HierarchicalSourceAdapter} and
     * initializes it with the {@code ConfigurationSource} to wrap. Changes of
     * the wrapped source are not monitored.
     *
     * @param wrappedSource the original {@code ConfigurationSource} (must not
     *        be <b>null</b>)
     * @throws IllegalArgumentException if the {@code ConfigurationSource} is
     *         <b>null</b>
     */
    public HierarchicalSourceAdapter(FlatConfigurationSource wrappedSource)
    {
        this(wrappedSource, false);
    }

    /**
     * An utility method for copying the content of the specified {@code
     * ConfigurationSource} into the given {@code Configuration}. This class
     * iterates over all properties stored in the {@code ConfigurationSource}
     * and adds their values to the {@code Configuration}. Existing values in
     * the {@code Configuration} are not overridden, but new values are added.
     *
     * @param config the target {@code Configuration} (must not be <b>null</b>)
     * @param source the {@code ConfigurationSource} to be copied (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public static void fillConfiguration(Configuration<?> config,
            FlatConfigurationSource source)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Configuration must not be null!");
        }
        if (source == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationSource must not be null!");
        }

        doFillConfiguration(config, source);
    }

    /**
     * An utility method for copying the content of the specified {@code
     * Configuration} into the given {@code ConfigurationSource}. This method is
     * the opposite of
     * {@link #fillConfiguration(Configuration, FlatConfigurationSource)}: It
     * iterates over the keys in the {@code Configuration} and adds their values
     * to the {@code ConfigurationSource}.
     *
     * @param source the target {@code ConfigurationSource} (must not be
     *        <b>null</b>)
     * @param config the {@code Configuration} to be copied (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public static void fillSource(FlatConfigurationSource source,
            Configuration<?> config)
    {
        if (source == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationSource must not be null!");
        }
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Configuration must not be null!");
        }

        doFillSource(source, config);
    }

    /**
     * Returns the original {@code ConfigurationSource} that is wrapped by this
     * adapter.
     *
     * @return the original {@code ConfigurationSource}
     */
    public FlatConfigurationSource getOriginalSource()
    {
        return originalSource;
    }

    /**
     * Returns a flag whether changes of the original {@code
     * ConfigurationSource} are monitored. A value of <b>true</b> means that
     * this adapter is registered as a change listener at the configuration
     * time. Every time a change event is received the data of this {@code
     * HierarchicalConfigurationSource} is invalidated, so it has to be
     * re-constructed on next access.
     *
     * @return a flag whether changes of the wrapped source are monitored
     */
    public boolean isMonitorChanges()
    {
        return monitorChanges;
    }

    /**
     * Writes the data stored in this {@code HierarchicalConfigurationSource}
     * into the original {@code ConfigurationSource}. This method can be called
     * to apply changes made at this source to the original source. Note that
     * the original {@code ConfigurationSource} may not be capable to fully deal
     * with the hierarchical structure of the data stored in this {@code
     * HierarchicalConfigurationSource}. So some structure might be lost during
     * this conversion.
     */
    public void writeBack()
    {
        if (isMonitorChanges())
        {
            // first de-register, so we do not receive our own updates
            getOriginalSource().removeConfigurationSourceListener(this);
        }

        try
        {
            getOriginalSource().clear();
            doFillSource(getOriginalSource(), getTransformedConfiguration());
        }
        finally
        {
            if (isMonitorChanges())
            {
                getOriginalSource().addConfigurationSourceListener(this);
            }
        }
    }

    /**
     * Adds a {@code ConfigurationSourceListener} to this source. This
     * implementation delegates to the transformed source.
     *
     * @param l the listener to add
     */
    public void addConfigurationSourceListener(ConfigurationSourceListener l)
    {
        getTransformedSource().addConfigurationSourceListener(l);
    }

    /**
     * Removes all data from this source. This implementation delegates to the
     * transformed source.
     */
    public void clear()
    {
        getTransformedSource().clear();
    }

    /**
     * Returns the {@code NodeHandler} of this source. This implementation
     * delegates to the transformed source.
     *
     * @return the {@code NodeHandler} of this source
     */
    public NodeHandler<ConfigurationNode> getNodeHandler()
    {
        return getTransformedSource().getNodeHandler();
    }

    /**
     * Returns the root node of this source. This implementation delegates to
     * the transformed source.
     *
     * @return the root node of this source
     */
    public ConfigurationNode getRootNode()
    {
        return getTransformedSource().getRootNode();
    }

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * source. This implementation delegates to the transformed source.
     *
     * @param l the listener to remove
     * @return a flag whether the listener could be removed
     */
    public boolean removeConfigurationSourceListener(
            ConfigurationSourceListener l)
    {
        return getTransformedSource().removeConfigurationSourceListener(l);
    }

    /**
     * Sets the root node of this source. This implementation delegates to the
     * transformed source.
     *
     * @param root the new root node
     */
    public void setRootNode(ConfigurationNode root)
    {
        getTransformedSource().setRootNode(root);
    }

    /**
     * Returns the capability of the the specified type. This implementation
     * delegates to the transformed source.
     *
     * @param <T> the type of the capability requested
     * @param cls the class of the capability interface
     * @return the object implementing the desired capability or <b>null</b> if
     *         this capability is not supported
     */
    public <T> T getCapability(Class<T> cls)
    {
        return getOriginalSource().getCapability(cls);
    }

    /**
     * Notifies this object about a change of a monitored {@code
     * ConfigurationSource}. If this {@code HierarchicalSourceAdapter} was
     * constructed with the {@code monitorChanges} flag set to <b>true</b>, it
     * has registered itself as a change listener at the original {@code
     * ConfigurationSource}. Thus it receives change notifications whenever the
     * original {@code ConfigurationSource} is manipulated. This implementation
     * just calls {@link #invalidate()} which indicates that the data of this
     * source has to be re-constructed.
     *
     * @param event the change event
     */
    public void configurationSourceChanged(ConfigurationSourceEvent event)
    {
        invalidate();
    }

    /**
     * Returns a {@code Configuration} that is used internally for the
     * transformation of the original source to a hierarchical one. This
     * configuration is populated with the data of the original source. Whenever
     * a change in the original source is detected, this configuration is
     * invalidated so that it has to be re-constructed.
     *
     * @return the {@code Configuration} used internally for the transformation
     */
    protected synchronized Configuration<ConfigurationNode> getTransformedConfiguration()
    {
        if (transformedConfig == null)
        {
            // need to re-construct the configuration
            transformedConfig = new ConfigurationImpl<ConfigurationNode>(
                    new InMemoryConfigurationSource());
            doFillConfiguration(transformedConfig, getOriginalSource());
        }

        return transformedConfig;
    }

    /**
     * Returns the <em>transformed source</em>. This is a true hierarchical
     * configuration source to which all operations on this adapter are
     * delegated. This implementation uses an in-memory configuration source
     * that is associated with a configuration to be populated and manipulated.
     *
     * @return the transformed {@code HierarchicalConfigurationSource}
     */
    protected HierarchicalConfigurationSource<ConfigurationNode> getTransformedSource()
    {
        return getTransformedConfiguration().getConfigurationSource();
    }

    /**
     * Invalidates the data in the transformed source. Calling this method
     * causes the configuration used internally for the transformation to be
     * reseted, so it has to be re-constructed when the next data access
     * happens. It can be called, for instance, if a change of the original
     * source is detected.
     */
    protected synchronized void invalidate()
    {
        transformedConfig = null;
    }

    /**
     * Helper method for copying the data of a configuration source into a
     * configuration.
     *
     * @param config the configuration to be filled
     * @param source the configuration source
     */
    private static void doFillConfiguration(Configuration<?> config,
            FlatConfigurationSource source)
    {
        for (Iterator<String> it = source.getKeys(); it.hasNext();)
        {
            String key = it.next();
            config.addProperty(key, source.getProperty(key));
        }
    }

    /**
     * Helper method for copying the data of a configuration into a
     * configuration source.
     *
     * @param source the configuration source
     * @param config the configuration
     */
    private static void doFillSource(FlatConfigurationSource source,
            Configuration<?> config)
    {
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            String key = it.next();
            source.addProperty(key, config.getProperty(key));
        }
    }
}
