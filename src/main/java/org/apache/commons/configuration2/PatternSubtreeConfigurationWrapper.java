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
package org.apache.commons.configuration2;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Wraps a BaseHierarchicalConfiguration and allows subtrees to be accessed via a configured path with
 * replaceable tokens derived from the ConfigurationInterpolator. When used with injection frameworks
 * such as Spring it allows components to be injected with subtrees of the configuration.
 * @since 1.6
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class PatternSubtreeConfigurationWrapper extends BaseHierarchicalConfiguration
    implements FileBasedConfiguration
{
    /** The wrapped configuration */
    private final HierarchicalConfiguration<ImmutableNode> config;

    /** The path to the subtree */
    private final String path;

    /** True if the path ends with '/', false otherwise */
    private final boolean trailing;

    /** True if the constructor has finished */
    private final boolean init;

    /**
     * Constructor
     * @param config The Configuration to be wrapped.
     * @param path The base path pattern.
     */
    public PatternSubtreeConfigurationWrapper(
            HierarchicalConfiguration<ImmutableNode> config, String path)
    {
        this.config = config;
        this.path = path;
        this.trailing = path.endsWith("/");
        this.init = true;
    }

    @Override
    protected void addPropertyInternal(String key, Object value)
    {
        config.addProperty(makePath(key), value);
    }

    @Override
    protected void clearInternal()
    {
        getConfig().clear();
    }

    @Override
    protected void clearPropertyDirect(String key)
    {
        config.clearProperty(makePath(key));
    }

    @Override
    protected boolean containsKeyInternal(String key)
    {
        return config.containsKey(makePath(key));
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return config.getBigDecimal(makePath(key), defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key)
    {
        return config.getBigDecimal(makePath(key));
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return config.getBigInteger(makePath(key), defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key)
    {
        return config.getBigInteger(makePath(key));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    @Override
    public boolean getBoolean(String key)
    {
        return config.getBoolean(makePath(key));
    }

    @Override
    public byte getByte(String key, byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    @Override
    public byte getByte(String key)
    {
        return config.getByte(makePath(key));
    }

    @Override
    public double getDouble(String key, double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    @Override
    public double getDouble(String key)
    {
        return config.getDouble(makePath(key));
    }

    @Override
    public float getFloat(String key, float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    @Override
    public float getFloat(String key)
    {
        return config.getFloat(makePath(key));
    }

    @Override
    public int getInt(String key, int defaultValue)
    {
        return config.getInt(makePath(key), defaultValue);
    }

    @Override
    public int getInt(String key)
    {
        return config.getInt(makePath(key));
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue)
    {
        return config.getInteger(makePath(key), defaultValue);
    }

    @Override
    protected Iterator<String> getKeysInternal()
    {
        return config.getKeys(makePath());
    }

    @Override
    protected Iterator<String> getKeysInternal(String prefix)
    {
        return config.getKeys(makePath(prefix));
    }

    @Override
    public List<Object> getList(String key, List<?> defaultValue)
    {
        return config.getList(makePath(key), defaultValue);
    }

    @Override
    public List<Object> getList(String key)
    {
        return config.getList(makePath(key));
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    @Override
    public long getLong(String key)
    {
        return config.getLong(makePath(key));
    }

    @Override
    public Properties getProperties(String key)
    {
        return config.getProperties(makePath(key));
    }

    @Override
    protected Object getPropertyInternal(String key)
    {
        return config.getProperty(makePath(key));
    }

    @Override
    public short getShort(String key, short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    @Override
    public short getShort(String key)
    {
        return config.getShort(makePath(key));
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return config.getString(makePath(key), defaultValue);
    }

    @Override
    public String getString(String key)
    {
        return config.getString(makePath(key));
    }

    @Override
    public String[] getStringArray(String key)
    {
        return config.getStringArray(makePath(key));
    }

    @Override
    protected boolean isEmptyInternal()
    {
        return getConfig().isEmpty();
    }

    @Override
    protected void setPropertyInternal(String key, Object value)
    {
        getConfig().setProperty(key, value);
    }

    @Override
    public Configuration subset(String prefix)
    {
        return getConfig().subset(prefix);
    }

    @Override
    public ExpressionEngine getExpressionEngine()
    {
        return config.getExpressionEngine();
    }

    @Override
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        if (init)
        {
            config.setExpressionEngine(expressionEngine);
        }
        else
        {
            super.setExpressionEngine(expressionEngine);
        }
    }

    @Override
    protected void addNodesInternal(String key, Collection<? extends ImmutableNode> nodes)
    {
        getConfig().addNodes(key, nodes);
    }

    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(String key, boolean supportUpdates)
    {
        return config.configurationAt(makePath(key), supportUpdates);
    }

    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(String key)
    {
        return config.configurationAt(makePath(key));
    }

    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(String key)
    {
        return config.configurationsAt(makePath(key));
    }

    @Override
    protected Object clearTreeInternal(String key)
    {
        config.clearTree(makePath(key));
        return Collections.emptyList();
    }

    @Override
    protected int getMaxIndexInternal(String key)
    {
        return config.getMaxIndex(makePath(key));
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return getConfig().interpolatedConfiguration();
    }

    @Override
    public <T extends Event> void addEventListener(EventType<T> eventType,
            EventListener<? super T> listener)
    {
        getConfig().addEventListener(eventType, listener);
    }

    @Override
    public <T extends Event> boolean removeEventListener(
            EventType<T> eventType, EventListener<? super T> listener)
    {
        return getConfig().removeEventListener(eventType, listener);
    }

    @Override
    public <T extends Event> Collection<EventListener<? super T>> getEventListeners(
            EventType<T> eventType)
    {
        return getConfig().getEventListeners(eventType);
    }

    @Override
    public void clearEventListeners()
    {
        getConfig().clearEventListeners();
    }

    @Override
    public void clearErrorListeners()
    {
        getConfig().clearErrorListeners();
    }

    @Override
    public void write(Writer writer) throws ConfigurationException, IOException
    {
        fetchFileBased().write(writer);
    }

    @Override
    public void read(Reader reader) throws ConfigurationException, IOException
    {
        fetchFileBased().read(reader);
    }

    private BaseHierarchicalConfiguration getConfig()
    {
        return (BaseHierarchicalConfiguration) config.configurationAt(makePath());
    }

    private String makePath()
    {
        String pathPattern = trailing ? path.substring(0, path.length() - 1) : path;
        return substitute(pathPattern);
    }

    /*
     * Resolve the root expression and then add the item being retrieved. Insert a
     * separator character as required.
     */
    private String makePath(String item)
    {
        String pathPattern;
        if ((item.length() == 0 || item.startsWith("/")) && trailing)
        {
            pathPattern = path.substring(0, path.length() - 1);
        }
        else  if (!item.startsWith("/") || !trailing)
        {
            pathPattern = path + "/";
        }
        else
        {
            pathPattern = path;
        }
        return substitute(pathPattern) + item;
    }

    /**
     * Uses this configuration's {@code ConfigurationInterpolator} to perform
     * variable substitution on the given pattern string.
     *
     * @param pattern the pattern string
     * @return the string with variables replaced
     */
    private String substitute(String pattern)
    {
        Object value = getInterpolator().interpolate(pattern);
        return (value != null) ? value.toString() : null;
    }

    /**
     * Returns the wrapped configuration as a {@code FileBased} object. If this
     * cast is not possible, an exception is thrown.
     *
     * @return the wrapped configuration as {@code FileBased}
     * @throws ConfigurationException if the wrapped configuration does not
     *         implement {@code FileBased}
     */
    private FileBased fetchFileBased() throws ConfigurationException
    {
        if (!(config instanceof FileBased))
        {
            throw new ConfigurationException(
                    "Wrapped configuration does not implement FileBased!"
                            + " No I/O operations are supported.");
        }
        return (FileBased) config;
    }
}
