package org.apache.commons.configuration;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.reloading.Reloadable;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

/**
 *
 */
public class HierarchicalReloadableConfiguration extends HierarchicalConfiguration
    implements Reloadable
{
    private final Object reloadLock;

    /**
     * Creates a new instance of <code>HierarchicalReloadableConfiguration</code>.
     */
    public HierarchicalReloadableConfiguration()
    {
        super();
        reloadLock = new Object();
    }

    public HierarchicalReloadableConfiguration(Object lock)
    {
        super();
        reloadLock = lock == null ? new Object() : lock;
    }

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code> and
     * copies all data contained in the specified configuration into the new
     * one.
     *
     * @param c the configuration that is to be copied (if <b>null</b>, this
     * constructor will behave like the standard constructor)
     * @since 1.4
     */
    public HierarchicalReloadableConfiguration(HierarchicalConfiguration c)
    {
        super(c);
        reloadLock = new Object();
    }


    public Object getReloadLock()
    {
        return reloadLock;
    }

    public Object getProperty(String key)
    {
        synchronized(reloadLock)
        {
            return super.getProperty(key);
        }
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        synchronized(reloadLock)
        {
            super.addPropertyDirect(key, obj);
        }
    }

    public void addNodes(String key, Collection nodes)
    {
        synchronized(reloadLock)
        {
            super.addNodes(key, nodes);
        }
    }

    public boolean isEmpty()
    {
        synchronized(reloadLock)
        {
            return super.isEmpty();
        }
    }

    public Configuration subset(String prefix)
    {
        synchronized(reloadLock)
        {
            return super.subset(prefix);
        }
    }

    public SubnodeConfiguration configurationAt(String key, boolean supportUpdates)
    {
        synchronized(reloadLock)
        {
            return super.configurationAt(key, supportUpdates);
        }
    }

    public SubnodeConfiguration configurationAt(String key)
    {
        synchronized(reloadLock)
        {
            return super.configurationAt(key);
        }
    }

    public List configurationsAt(String key)
    {
        synchronized(reloadLock)
        {
            return super.configurationsAt(key);
        }
    }

    protected SubnodeConfiguration createSubnodeConfiguration(ConfigurationNode node)
    {
        synchronized(reloadLock)
        {
            return super.createSubnodeConfiguration(node);
        }
    }

    protected SubnodeConfiguration createSubnodeConfiguration(ConfigurationNode node, String subnodeKey)
    {
        synchronized(reloadLock)
        {
            return super.createSubnodeConfiguration(node, subnodeKey);
        }
    }

    protected void subnodeConfigurationChanged(ConfigurationEvent event)
    {
        synchronized(reloadLock)
        {
            super.subnodeConfigurationChanged(event);
        }
    }

    void registerSubnodeConfiguration(SubnodeConfiguration config)
    {
        synchronized(reloadLock)
        {
            super.registerSubnodeConfiguration(config);
        }
    }

    public boolean containsKey(String key)
    {
        synchronized(reloadLock)
        {
            return super.containsKey(key);
        }
    }

    public void setProperty(String key, Object value)
    {
        synchronized(reloadLock)
        {
            super.setProperty(key, value);
        }
    }

    public void clearTree(String key)
    {
        synchronized(reloadLock)
        {
            super.clearTree(key);
        }
    }

    public void clearProperty(String key)
    {
        synchronized(reloadLock)
        {
            super.clearProperty(key);
        }
    }

    public Iterator getKeys()
    {
        synchronized(reloadLock)
        {
            return super.getKeys();
        }
    }

    public Iterator getKeys(String prefix)
    {
        synchronized(reloadLock)
        {
            return super.getKeys(prefix);
        }
    }

    public int getMaxIndex(String key)
    {
        synchronized(reloadLock)
        {
            return super.getMaxIndex(key);
        }
    }
}
