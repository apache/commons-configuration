package org.apache.commons.configuration.reloading;

import java.io.File;
import java.util.Random;

/**
 * A ReloadingStrategy that randomly returns true or false;
 */
public class FileRandomReloadingStrategy extends FileChangedReloadingStrategy
{
    Random random = new Random();
    /**
     * Checks whether a reload is necessary.
     *
     * @return a flag whether a reload is required
     */
    public boolean reloadingRequired()
    {
        return random.nextBoolean();
    }

    /**
     * Returns the file that is watched by this strategy.
     *
     * @return the monitored file
     */
    public File getMonitoredFile()
    {
        return getFile();
    }
}
