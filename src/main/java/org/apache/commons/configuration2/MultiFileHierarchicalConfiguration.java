/**
 *
 */
package org.apache.commons.configuration2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.Reader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.event.ConfigurationErrorListener;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.tree.ConfigurationNode;

/**
 *
 */
public class MultiFileHierarchicalConfiguration extends AbstractHierarchicalFileConfiguration
    implements ConfigurationListener, ConfigurationErrorListener
{
    private ConcurrentMap<String, XMLConfiguration> configurationsMap = new ConcurrentHashMap<String, XMLConfiguration>();
    private String pattern;
    private boolean init = false;
    private static final String FILE_URL_PREFIX = "file:";

    /**
     * Default Constructor
     */
    public MultiFileHierarchicalConfiguration()
    {
        super();
        this.init = true;
    }

    /**
     * Constructor
     * @param pathPattern The pattern to use to location configuration files.
     */
    public MultiFileHierarchicalConfiguration(String pathPattern)
    {
        super();
        this.pattern = pathPattern;
        this.init = true;
    }

    /**
     * Set the File pattern
     * @param pathPattern The pattern for the path to the configuration.
     */
    public void setFilePattern(String pathPattern)
    {
        this.pattern = pathPattern;
    }


    public void addProperty(String key, Object value)
    {
        this.getConfiguration().addProperty(key, value);
    }

    public void clear()
    {
        this.getConfiguration().clear();
    }

    public void clearProperty(String key)
    {
        this.getConfiguration().clearProperty(key);
    }

    public boolean containsKey(String key)
    {
        return this.getConfiguration().containsKey(key);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return this.getConfiguration().getBigDecimal(key, defaultValue);
    }

    public BigDecimal getBigDecimal(String key)
    {
        return this.getConfiguration().getBigDecimal(key);
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return this.getConfiguration().getBigInteger(key, defaultValue);
    }

    public BigInteger getBigInteger(String key)
    {
        return this.getConfiguration().getBigInteger(key);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key)
    {
        return this.getConfiguration().getBoolean(key);
    }

    public byte getByte(String key, byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    public Byte getByte(String key, Byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    public byte getByte(String key)
    {
        return this.getConfiguration().getByte(key);
    }

    public double getDouble(String key, double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    public Double getDouble(String key, Double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    public double getDouble(String key)
    {
        return this.getConfiguration().getDouble(key);
    }

    public float getFloat(String key, float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    public Float getFloat(String key, Float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    public float getFloat(String key)
    {
        return this.getConfiguration().getFloat(key);
    }

    public int getInt(String key, int defaultValue)
    {
        return this.getConfiguration().getInt(key, defaultValue);
    }

    public int getInt(String key)
    {
        return this.getConfiguration().getInt(key);
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        return this.getConfiguration().getInteger(key, defaultValue);
    }

    @Override
    public Iterator<String> getKeys()
    {
        return this.getConfiguration().getKeys();
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return this.getConfiguration().getKeys(prefix);
    }

    @Override
    public <T> List<T> getList(String key, List<T> defaultValue)
    {
        return this.getConfiguration().getList(key, defaultValue);
    }

    @Override
    public <T> List<T> getList(String key)
    {
        return this.getConfiguration().getList(key);
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    @Override
    public long getLong(String key)
    {
        return this.getConfiguration().getLong(key);
    }

    @Override
    public Properties getProperties(String key)
    {
        return this.getConfiguration().getProperties(key);
    }

    @Override
    public Object getProperty(String key)
    {
        return this.getConfiguration().getProperty(key);
    }

    @Override
    public short getShort(String key, short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    @Override
    public short getShort(String key)
    {
        return this.getConfiguration().getShort(key);
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return this.getConfiguration().getString(key, defaultValue);
    }

    @Override
    public String getString(String key)
    {
        return this.getConfiguration().getString(key);
    }

    @Override
    public String[] getStringArray(String key)
    {
        return this.getConfiguration().getStringArray(key);
    }

    @Override
    public boolean isEmpty()
    {
        return this.getConfiguration().isEmpty();
    }

    @Override
    public void setProperty(String key, Object value)
    {
        if (init)
        {
            this.getConfiguration().setProperty(key, value);
        }
    }

    @Override
    public Configuration subset(String prefix)
    {
        return this.getConfiguration().subset(prefix);
    }

    @Override
    public ExpressionEngine getExpressionEngine()
    {
        return super.getExpressionEngine();
    }

    @Override
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        super.setExpressionEngine(expressionEngine);
    }

    @Override
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        this.getConfiguration().addNodes(key, nodes);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key, boolean supportUpdates)
    {
        return this.getConfiguration().configurationAt(key, supportUpdates);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key)
    {
        return this.getConfiguration().configurationAt(key);
    }

    @Override
    public List<SubConfiguration<ConfigurationNode>> configurationsAt(String key)
    {
        return this.getConfiguration().configurationsAt(key);
    }

    @Override
    public void clearTree(String key)
    {
        this.getConfiguration().clearTree(key);
    }

    @Override
    public int getMaxIndex(String key)
    {
        return this.getConfiguration().getMaxIndex(key);
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return this.getConfiguration().interpolatedConfiguration();
    }

    @Override
    public void addConfigurationListener(ConfigurationListener l)
    {
        super.addConfigurationListener(l);
    }

    @Override
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return super.removeConfigurationListener(l);
    }

    @Override
    public Collection getConfigurationListeners()
    {
        return super.getConfigurationListeners();
    }

    @Override
    public void clearConfigurationListeners()
    {
        super.clearConfigurationListeners();
    }

    @Override
    public void addErrorListener(ConfigurationErrorListener l)
    {
        super.addErrorListener(l);
    }

    @Override
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return super.removeErrorListener(l);
    }

    @Override
    public void clearErrorListeners()
    {
        super.clearErrorListeners();
    }

    @Override
    public Collection getErrorListeners()
    {
        return super.getErrorListeners();
    }


    public void save(Writer writer) throws ConfigurationException
    {
        if (init)
        {
            this.getConfiguration().save(writer);
        }
    }

    public void load(Reader reader) throws ConfigurationException
    {
        if (init)
        {
            this.getConfiguration().load(reader);
        }
    }

    public void load() throws ConfigurationException
    {
        this.getConfiguration().load();
    }

    public void load(String fileName) throws ConfigurationException
    {
        this.getConfiguration().load(fileName);
    }

    public void load(File file) throws ConfigurationException
    {
        this.getConfiguration().load(file);
    }

    public void load(URL url) throws ConfigurationException
    {
        this.getConfiguration().load(url);
    }

    public void load(InputStream in) throws ConfigurationException
    {
        this.getConfiguration().load(in);
    }

    public void load(InputStream in, String encoding) throws ConfigurationException
    {
        this.getConfiguration().load(in, encoding);
    }

    public void save() throws ConfigurationException
    {
        this.getConfiguration().save();
    }

    public void save(String fileName) throws ConfigurationException
    {
        this.getConfiguration().save(fileName);
    }

    public void save(File file) throws ConfigurationException
    {
        this.getConfiguration().save(file);
    }

    public void save(URL url) throws ConfigurationException
    {
        this.getConfiguration().save(url);
    }

    public void save(OutputStream out) throws ConfigurationException
    {
        this.getConfiguration().save(out);
    }

    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        this.getConfiguration().save(out, encoding);
    }

    @Override
    public ConfigurationNode getRootNode()
    {
        return getConfiguration().getRootNode();
    }

    @Override
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (init)
        {
            getConfiguration().setRootNode(rootNode);
        }
        else
        {
            super.setRootNode(rootNode);
        }
    }

    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Collection<ConfigurationListener> listeners = getConfigurationListeners();
            for (ConfigurationListener listener : listeners)
            {
                listener.configurationChanged(event);
            }
        }
    }

    public void configurationError(ConfigurationErrorEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Collection<ConfigurationErrorListener> listeners = getErrorListeners();
            for (ConfigurationErrorListener listener : listeners)
            {
                listener.configurationError(event);
            }
        }
    }

    /**
     * First checks to see if the cache exists, if it does, get the associated Configuration.
     * If not it will load a new Configuration and save it in the cache.
     *
     * @return the Configuration associated with the current value of the path pattern.
     */
    private AbstractHierarchicalFileConfiguration getConfiguration()
    {
        if (pattern == null)
        {
            throw new ConfigurationRuntimeException("File pattern must be defined");
        }
        String path = getSubstitutor().replace(pattern);

        if (configurationsMap.containsKey(path))
        {
            return configurationsMap.get(path);
        }

        if (path.equals(pattern))
        {
            XMLConfiguration configuration = new XMLConfiguration()
            {
                public void load() throws ConfigurationException
                {
                }
                public void save() throws ConfigurationException
                {
                }
            };

            configurationsMap.putIfAbsent(pattern, configuration);

            return configuration;
        }

        XMLConfiguration configuration = new XMLConfiguration();
        try
        {
            URL url = getURL(path);
            configuration.setURL(url);
            configuration.load();
            configuration.setExpressionEngine(getExpressionEngine());
            configuration.setReloadingStrategy(getReloadingStrategy());
            configuration.addConfigurationListener(this);
            configuration.addErrorListener(this);
            configurationsMap.putIfAbsent(path, configuration);
            configuration = configurationsMap.get(path);
        }
        catch (ConfigurationException ce)
        {
            throw new ConfigurationRuntimeException(ce);
        }
        catch (FileNotFoundException fnfe)
        {
            throw new ConfigurationRuntimeException(fnfe);
        }

        return configuration;
    }

    private URL getURL(String resourceLocation) throws FileNotFoundException
    {
        if (resourceLocation == null)
        {
            throw new IllegalArgumentException("A path pattern must be configured");
        }
        try
        {
            // try URL
            return new URL(resourceLocation);
        }
        catch (MalformedURLException ex)
        {
            // no URL -> treat as file path
            try
            {
                return new URL(FILE_URL_PREFIX + resourceLocation);
            }
            catch (MalformedURLException ex2)
            {
                throw new FileNotFoundException("Resource location [" + resourceLocation +
                        "] is not a URL or a well-formed file path");
            }
        }
    }
}
