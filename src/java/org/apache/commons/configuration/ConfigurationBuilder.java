/*
 * Copyright 2006 The Apache Software Foundation.
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
 * <p>
 * Definition of an interface for objects that can create a configuration.
 * </p>
 * <p>
 * This interface defines an abstract way of creating a
 * <code>Configuration</code> object. It does not assume any specific way of
 * how this is done; this is completely in the responsibility of an
 * implementation class. There is just a single method that returns the
 * configuration constructed by this builder.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 */
public interface ConfigurationBuilder
{
    /**
     * Returns the configuration provided by this builder. An implementation has
     * to perform all necessary steps for creating and initializing a
     * <code>Configuration</code> object.
     *
     * @return the configuration
     * @throws ConfigurationException if an error occurs
     */
    Configuration getConfiguration() throws ConfigurationException;
}