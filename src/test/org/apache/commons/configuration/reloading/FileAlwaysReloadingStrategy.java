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

import java.io.File;

/**
 * A specialized reloading strategy for files that will always report a change
 * of the monitored file. Thus it is well suited for testing reloading
 * operations on file-based configurations.
 *
 * @version $Id$
 */
public class FileAlwaysReloadingStrategy extends FileChangedReloadingStrategy
{
    /**
     * Checks whether a reload is necessary. This implementation returns always
     * <b>true</b>.
     *
     * @return a flag whether a reload is required
     */
    public boolean reloadingRequired()
    {
        return true;
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
