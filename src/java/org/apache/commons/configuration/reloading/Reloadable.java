package org.apache.commons.configuration.reloading;

/**
 * Interface that allows other objects to synchronize on a root lock.
 */
public interface Reloadable
{
    Object getReloadLock();
}
