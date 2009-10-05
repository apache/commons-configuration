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

    private static final String LOCK_NAME = "HierarchicalReloadableConfigurationLock";

    /**
     * Creates a new instance of <code>HierarchicalReloadableConfiguration</code>.
     */
    public HierarchicalReloadableConfiguration()
    {
        super();
        reloadLock = new Lock(LOCK_NAME);
    }

    public HierarchicalReloadableConfiguration(Object lock)
    {
        super();
        reloadLock = lock == null ? new Lock(LOCK_NAME) : lock;
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
        reloadLock = new Lock(LOCK_NAME);
    }


    public Object getReloadLock()
    {
        return reloadLock;
    }
}
