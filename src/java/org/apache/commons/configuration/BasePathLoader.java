package org.apache.commons.configuration;

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

/**
 * <p>Definition of an interface for objects that load configuration data
 * from a URL.</p>
 * <p>The interface defines methods for getting and setting a base path.
 * A file name will then be interpreted relative to this base path.</p>
 *
 * @version $Id: BasePathLoader.java,v 1.2 2004/02/27 17:41:35 epugh Exp $
 */
public interface BasePathLoader
{
    /**
     * Returns the base path. Relative path names will be resolved based on
     * this path.
     * @return the base path
     */
    String getBasePath();

    /**
     * Sets the base path. Relative path names will be resolved based on
     * this path. For maximum flexibility this base path should be a URL.
     * @param path the base path
     */
    void setBasePath(String path);
}
