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

package org.apache.commons.configuration.reloading;

import org.apache.commons.configuration.FileConfiguration;

/**
 * A strategy to decide if a configuration should be reloaded.
 *
 * @author Emmanuel Bourg
 * @author Olivier Heger
 * @version $Revision: 1.1 $, $Date: 2004/10/18 15:45:10 $
 * @since 1.1
 */
public interface ReloadingStrategy {

    /**
     * Set the configuration managed by this strategy.
     */
    void setConfiguration(FileConfiguration configuration);

    /**
     * Initialize the strategy.
     */
    void init();

    /**
     * Tell if the evaluation of the strategy requires to reload the configuration.
     */
    boolean reloadingRequired();

    /**
     * Notify the strategy that the file has been reloaded.
     */
    void reloadingPerformed();

}
