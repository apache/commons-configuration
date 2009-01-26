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

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.event.ConfigurationErrorListener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Collection;
import java.io.Writer;
import java.io.Reader;



/**
 * Wraps a HierarchicalConfiguration and allows subtrees to be access via a configured path with
 * replaceable tokens derived from the MDC.
 * @since 1.6
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id:  $
 */
public class PatternSubtreeConfigurationWrapper extends AbstractHierarchicalFileConfiguration
{
    /**
     * Prevent recursion while resolving unprefixed properties.
     */
    private static ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>()
    {
        protected synchronized Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };

    /** The wrapped configuration */
    private final AbstractHierarchicalFileConfiguration config;

    /** The path to the subtree */
    private final String path;

    /** True if the path ends with '/', false otherwise */
    private final boolean trailing;

    /** True if the constructor has finished */
    private boolean init;

    /**
     * Constructor
     * @param config The Configuration to be wrapped.
     * @param path The base path pattern.
     */
    public PatternSubtreeConfigurationWrapper(AbstractHierarchicalFileConfiguration config, String path)
    {
        this.config = config;
        this.path = path;
        this.trailing = path.endsWith("/");
        this.init = true;
    }

    public void addProperty(String key, Object value)
    {
        config.addProperty(makePath(key), value);
    }

    public void clear()
    {
        getConfig().clear();
    }

    public void clearProperty(String key)
    {
        config.clearProperty(makePath(key));
    }

    public boolean containsKey(String key)
    {
        return config.containsKey(makePath(key));
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return config.getBigDecimal(makePath(key), defaultValue);
    }

    public BigDecimal getBigDecimal(String key)
    {
        return config.getBigDecimal(makePath(key));
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return config.getBigInteger(makePath(key), defaultValue);
    }

    public BigInteger getBigInteger(String key)
    {
        return config.getBigInteger(makePath(key));
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return config.getBoolean(makePath(key), defaultValue);
    }

    public boolean getBoolean(String key)
    {
        return config.getBoolean(makePath(key));
    }

    public byte getByte(String key, byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    public Byte getByte(String key, Byte defaultValue)
    {
        return config.getByte(makePath(key), defaultValue);
    }

    public byte getByte(String key)
    {
        return config.getByte(makePath(key));
    }

    public double getDouble(String key, double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    public Double getDouble(String key, Double defaultValue)
    {
        return config.getDouble(makePath(key), defaultValue);
    }

    public double getDouble(String key)
    {
        return config.getDouble(makePath(key));
    }

    public float getFloat(String key, float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    public Float getFloat(String key, Float defaultValue)
    {
        return config.getFloat(makePath(key), defaultValue);
    }

    public float getFloat(String key)
    {
        return config.getFloat(makePath(key));
    }

    public int getInt(String key, int defaultValue)
    {
        return config.getInt(makePath(key), defaultValue);
    }

    public int getInt(String key)
    {
        return config.getInt(makePath(key));
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        return config.getInteger(makePath(key), defaultValue);
    }
    @Override
    public Iterator<String> getKeys()
    {
        return config.getKeys(makePath());
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return config.getKeys(makePath(prefix));
    }

    @Override
    public <T> List<T> getList(String key, List<T> defaultValue)
    {
        return config.getList(makePath(key), defaultValue);
    }

    @Override
    public <T> List<T> getList(String key)
    {
        return config.getList(makePath(key));
    }

    public long getLong(String key, long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    public Long getLong(String key, Long defaultValue)
    {
        return config.getLong(makePath(key), defaultValue);
    }

    public long getLong(String key)
    {
        return config.getLong(makePath(key));
    }

    public Properties getProperties(String key)
    {
        return config.getProperties(makePath(key));
    }

    public Object getProperty(String key)
    {
        return config.getProperty(makePath(key));
    }

    public short getShort(String key, short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    public Short getShort(String key, Short defaultValue)
    {
        return config.getShort(makePath(key), defaultValue);
    }

    public short getShort(String key)
    {
        return config.getShort(makePath(key));
    }

    public String getString(String key, String defaultValue)
    {
        return config.getString(makePath(key), defaultValue);
    }

    public String getString(String key)
    {
        return config.getString(makePath(key));
    }

    public String[] getStringArray(String key)
    {
        return config.getStringArray(makePath(key));
    }

    public boolean isEmpty()
    {
        return getConfig().isEmpty();
    }

    public void setProperty(String key, Object value)
    {
        getConfig().setProperty(key, value);
    }

    public Configuration subset(String prefix)
    {
        return getConfig().subset(prefix);
    }

    @Override
    public ConfigurationNode getRootNode()
    {
        return getConfig().getRootNode();
    }

    @Override
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (!init)
        {
            super.setRootNode(rootNode);
        }
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
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        config.addNodes(makePath(key), nodes);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key, boolean supportUpdates)
    {
        return config.configurationAt(makePath(key), supportUpdates);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key)
    {
        return config.configurationAt(makePath(key));
    }

    @Override
    public List<SubConfiguration<ConfigurationNode>> configurationsAt(String key)
    {
        return config.configurationsAt(makePath(key));
    }

    @Override
    public void clearTree(String key)
    {
        config.clearTree(makePath(key));
    }

    @Override
    public int getMaxIndex(String key)
    {
        return config.getMaxIndex(makePath(key));
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return getConfig().interpolatedConfiguration();
    }

    @Override
    public void addConfigurationListener(ConfigurationListener l)
    {
        getConfig().addConfigurationListener(l);
    }

    @Override
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return getConfig().removeConfigurationListener(l);
    }

    @Override
    public Collection getConfigurationListeners()
    {
        return getConfig().getConfigurationListeners();
    }

    @Override
    public void clearConfigurationListeners()
    {
        getConfig().clearConfigurationListeners();
    }

    @Override
    public void addErrorListener(ConfigurationErrorListener l)
    {
        getConfig().addErrorListener(l);
    }

    @Override
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return getConfig().removeErrorListener(l);
    }

    @Override
    public void clearErrorListeners()
    {
        getConfig().clearErrorListeners();
    }

    public void save(Writer writer) throws ConfigurationException
    {
        config.save(writer);
    }

    public void load(Reader reader) throws ConfigurationException
    {
        config.load(reader);
    }

    @Override
    public Collection getErrorListeners()
    {
        return getConfig().getErrorListeners();
    }

    /*
     * Don't allow resolveContainerStore to be called recursively. This happens
     * when the path pattern does not resolve and the ConfigurationInterpolator
     * calls resolveContainerStore, which in turn calls getProperty, which then
     * calls getConfiguration. GetConfiguration then calls the interpoloator
     * which starts it all over again.
     * @param key The key to resolve.
     * @return The value of the key.
     */
    protected Object resolveContainerStore(String key)
    {
        if (recursive.get())
        {
            return null;
        }
        recursive.set(Boolean.TRUE);
        try
        {
            return super.resolveContainerStore(key);
        }
        finally
        {
            recursive.set(Boolean.FALSE);
        }
    }

    private SubConfiguration<ConfigurationNode> getConfig()
    {
        return config.configurationAt(makePath());
    }

    private String makePath()
    {
        String pathPattern = trailing ? path.substring(0, path.length() - 1) : path;
        return getSubstitutor().replace(pathPattern);
    }

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
        return getSubstitutor().replace(pathPattern) + item;
    }
}
