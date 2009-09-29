package org.apache.commons.configuration.reloading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Random;

/**
 * A ReloadingStrategy that randomly returns true or false;
 */
public class FileRandomReloadingStrategy extends FileChangedReloadingStrategy
{
    Random random = new Random();

      /** The Log to use for diagnostic messages */
    private Log logger = LogFactory.getLog(FileRandomReloadingStrategy.class);

    /**
     * Checks whether a reload is necessary.
     *
     * @return a flag whether a reload is required
     */
    public boolean reloadingRequired()
    {
        boolean result = random.nextBoolean();
        if (result)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("File change detected: " + getName());
            }
        }
        return result;
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

    private String getName()
    {
        return getName(getFile());
    }

    private String getName(File file)
    {
        String name = configuration.getURL().toString();
        if (name == null)
        {
            if (file != null)
            {
                name = file.getAbsolutePath();
            }
            else
            {
                name = "base: " + configuration.getBasePath()
                       + "file: " + configuration.getFileName();
            }
        }
        return name;
    }
}
