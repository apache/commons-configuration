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
package org.apache.commons.configuration2.resolver;

import java.net.URL;
import java.util.Map;

/**
 * Interface used for registering and retrieving PUBLICID to URL mappings.
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.7
 */
public interface EntityRegistry
{
    /**
     * <p>
     * Registers the specified URL for the specified public identifier.
     * </p>
     * <p>
     * This implementation maps {@code PUBLICID}'s to URLs (from which
     * the resource will be loaded). A common use case for this method is to
     * register local URLs (possibly computed at runtime by a class loader) for
     * DTDs and Schemas. This allows the performance advantage of using a local
     * version without having to ensure every {@code SYSTEM} URI on every
     * processed XML document is local. This implementation provides only basic
     * functionality. If more sophisticated features are required, either calling
     * {@code XMLConfiguration.setDocumentBuilder(DocumentBuilder)} to set a custom
     * {@code DocumentBuilder} (which also can be initialized with a
     * custom {@code EntityResolver}) or creating a custom entity resolver
     * and registering it with the XMLConfiguration is recommended.
     * </p>
     *
     * @param publicId Public identifier of the Entity to be resolved
     * @param entityURL The URL to use for reading this Entity
     * @throws IllegalArgumentException if the public ID is undefined
     */
    void registerEntityId(String publicId, URL entityURL);

    /**
     * Returns a map with the entity IDs that have been registered using the
     * {@code registerEntityId()} method.
     *
     * @return a map with the registered entity IDs
     */
    Map<String, URL> getRegisteredEntities();
}
