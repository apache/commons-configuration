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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;
import org.apache.commons.configuration2.FileSystem;

import java.net.URL;
import java.io.InputStream;

/**
 * Thin wrapper around xml commons CatalogResolver to allow list of catalogs
 * to be provided.
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.7
 * @version $Id:  $
 */
public class CatalogResolver implements EntityResolver
{
    /**
     * Debug everything.
     */
    private static final int DEBUG_ALL = 9;

    /**
     * Debug nothing.
     */
    private static final int DEBUG_NONE = 0;

    /**
     * The CatalogResolver
     */
    protected org.apache.xml.resolver.tools.CatalogResolver resolver;

    /**
     * The CatalogManager
     */
    protected CatalogManager manager = new CatalogManager();

     /**
     * The FileSystem in use.
     */
    protected FileSystem fs = FileSystem.getDefaultFileSystem();

    /**
     * Stores the logger.
     */
    private Log log;

    /**
     * Constructs the CatalogResolver
     */
    public CatalogResolver()
    {
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        setLogger(null);
    }

    /**
     * Set the list of catalog file names
     *
     * @param catalogs The delimited list of catalog files.
     */
    public void setCatalogFiles(String catalogs)
    {
        manager.setCatalogFiles(catalogs);
        resolver = new org.apache.xml.resolver.tools.CatalogResolver(manager);
    }

    /**
     * Set the FileSystem.
     * @param fileSystem The FileSystem.
     */
    public void setFileSystem(FileSystem fileSystem)
    {
        this.fs = fileSystem;
    }

    /**
     * Enables debug logging of xml-commons Catalog processing.
     * @param debug True if debugging should be enabled, false otherwise.
     */
    public void setDebug(boolean debug)
    {
        if (debug)
        {
            manager.setVerbosity(DEBUG_ALL);
        }
        else
        {
            manager.setVerbosity(DEBUG_NONE);
        }
    }

    /**
     * Implements the <code>resolveEntity</code> method
     * for the SAX interface.
     * <p/>
     * <p>Presented with an optional public identifier and a system
     * identifier, this function attempts to locate a mapping in the
     * catalogs.</p>
     * <p/>
     * <p>If such a mapping is found, the resolver attempts to open
     * the mapped value as an InputSource and return it. Exceptions are
     * ignored and null is returned if the mapped value cannot be opened
     * as an input source.</p>
     * <p/>
     * <p>If no mapping is found (or an error occurs attempting to open
     * the mapped value as an input source), null is returned and the system
     * will use the specified system identifier as if no entityResolver
     * was specified.</p>
     *
     * @param publicId The public identifier for the entity in question.
     *                 This may be null.
     * @param systemId The system identifier for the entity in question.
     *                 XML requires a system identifier on all external entities, so this
     *                 value is always specified.
     * @return An InputSource for the mapped identifier, or null.
     * @throws SAXException if an error occurs.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException
    {
        String resolved = resolver.getResolvedEntity(publicId, systemId);

        if (resolved != null)
        {
            String badFilePrefix = "file://";
            String correctFilePrefix = "file:///";

            // Java 5 has a bug when constructing file URLS
            if (resolved.startsWith(badFilePrefix) && !resolved.startsWith(correctFilePrefix))
            {
                resolved = correctFilePrefix + resolved.substring(badFilePrefix.length());
            }

            try
            {
                InputStream is = fs.getInputStream(null, resolved);
                InputSource iSource = new InputSource(resolved);
                iSource.setPublicId(publicId);
                iSource.setByteStream(is);
                return iSource;
            }
            catch (Exception e)
            {
                log.debug("Failed to create InputSource for " + resolved + " ("
                                + e.toString() + ")");
                return null;
            }
        }

        return null;
    }

    /**
     * Returns the logger used by this configuration object.
     *
     * @return the logger
     */
    public Log getLogger()
    {
        return log;
    }

    /**
     * Allows to set the logger to be used by this configuration object. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used.
     *
     * @param log the new logger
     */
    public void setLogger(Log log)
    {
        this.log = (log != null) ? log : new NoOpLog();
    }

}
