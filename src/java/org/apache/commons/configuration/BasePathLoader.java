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
 * <p>Definition of an interface for objects that load configuration data
 * from a URL.</p>
 * <p>The interface defines methods for getting and setting a base path.
 * A file name will then be interpreted relative to this base path.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: BasePathLoader.java,v 1.3 2004/06/24 12:35:14 ebourg Exp $
 */
public interface BasePathLoader
{
    /**
     * Returns the base path. Relative path names will be resolved based on
     * this path.
     *
     * @return the base path
     */
    String getBasePath();

    /**
     * Sets the base path. Relative path names will be resolved based on
     * this path. For maximum flexibility this base path should be a URL.
     *
     * @param path the base path
     */
    void setBasePath(String path);
}
