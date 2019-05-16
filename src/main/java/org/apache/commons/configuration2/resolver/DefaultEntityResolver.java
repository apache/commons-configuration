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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The DefaultEntityResolver used by XML Configurations.
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.7
 */
public class DefaultEntityResolver implements EntityResolver, EntityRegistry
{
    /** Stores a map with the registered public IDs.*/
    private final Map<String, URL> registeredEntities = new HashMap<>();

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
    @Override
    public void registerEntityId(final String publicId, final URL entityURL)
    {
        if (publicId == null)
        {
            throw new IllegalArgumentException("Public ID must not be null!");
        }
        getRegisteredEntities().put(publicId, entityURL);
    }

    /**
     * Resolves the requested external entity. This is the default
     * implementation of the {@code EntityResolver} interface. It checks
     * the passed in public ID against the registered entity IDs and uses a
     * local URL if possible.
     *
     * @param publicId the public identifier of the entity being referenced
     * @param systemId the system identifier of the entity being referenced
     * @return an input source for the specified entity
     * @throws org.xml.sax.SAXException if a parsing exception occurs
     */
    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException
    {
        // Has this system identifier been registered?
        URL entityURL = null;
        if (publicId != null)
        {
            entityURL = getRegisteredEntities().get(publicId);
        }

        if (entityURL != null)
        {
            // Obtain an InputSource for this URL. This code is based on the
            // createInputSourceFromURL() method of Commons Digester.
            try
            {
                final URLConnection connection = entityURL.openConnection();
                connection.setUseCaches(false);
                final InputStream stream = connection.getInputStream();
                final InputSource source = new InputSource(stream);
                source.setSystemId(entityURL.toExternalForm());
                return source;
            }
            catch (final IOException e)
            {
                throw new SAXException(e);
            }
        }
        // default processing behavior
        return null;
    }

    /**
     * Returns a map with the entity IDs that have been registered using the
     * {@code registerEntityId()} method.
     *
     * @return a map with the registered entity IDs
     */
    @Override
    public Map<String, URL> getRegisteredEntities()
    {
        return registeredEntities;
    }
}
