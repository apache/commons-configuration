/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

/**
 * This is exactly the same as the BaseConfiguration but the backing
 * store is based on a path (e.g. a file path) from which it is loaded.
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: BasePathConfiguration.java,v 1.4 2004/06/24 12:35:14 ebourg Exp $
 */
public abstract class BasePathConfiguration extends BaseConfiguration implements BasePathLoader
{
    /**
     * Base path of the configuration file used to
     * create this Configuration object. Might be null, then a
     * "synthetic" PropertyConfiguration has been created which
     * is not loaded from a file
     */
    private String basePath = null;

    /**
     * Returns the Base path from which this Configuration Factory operates.
     * This is never null. If you set the BasePath to null, then "."
     * is returned.
     *
     * @return The base Path of this configuration factory.
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Sets the basePath for all file references from this Configuration
     * Factory. If you pass null in, this is interpreted as "current
     * directory".
     *
     * @param basePath The new basePath to set.
     */
    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

}
