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
    @Override
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
