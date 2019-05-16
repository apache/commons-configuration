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
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.readers.CatalogReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Thin wrapper around xml commons CatalogResolver to allow list of catalogs
 * to be provided.
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.7
 */
public class CatalogResolver implements EntityResolver
{
    /**
     * Debug everything.
     */
    private static final int DEBUG_ALL = 9;

    /**
     * Normal debug setting.
     */
    private static final int DEBUG_NORMAL = 4;

    /**
     * Debug nothing.
     */
    private static final int DEBUG_NONE = 0;

    /**
     * The CatalogManager
     */
    private final CatalogManager manager = new CatalogManager();

    /**
     * The FileSystem in use.
     */
    private FileSystem fs = FileLocatorUtils.DEFAULT_FILE_SYSTEM;

    /**
     * The CatalogResolver
     */
    private org.apache.xml.resolver.tools.CatalogResolver resolver;

    /**
     * Stores the logger.
     */
    private ConfigurationLogger log;

    /**
     * Constructs the CatalogResolver
     */
    public CatalogResolver()
    {
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        manager.setFileSystem(fs);
        initLogger(null);
    }

    /**
     * Set the list of catalog file names
     *
     * @param catalogs The delimited list of catalog files.
     */
    public void setCatalogFiles(final String catalogs)
    {
        manager.setCatalogFiles(catalogs);
    }

    /**
     * Set the FileSystem.
     * @param fileSystem The FileSystem.
     */
    public void setFileSystem(final FileSystem fileSystem)
    {
        this.fs = fileSystem;
        manager.setFileSystem(fileSystem);
    }

    /**
     * Set the base path.
     * @param baseDir The base path String.
     */
    public void setBaseDir(final String baseDir)
    {
        manager.setBaseDir(baseDir);
    }

    /**
     * Set the {@code ConfigurationInterpolator}.
     * @param ci the {@code ConfigurationInterpolator}
     */
    public void setInterpolator(final ConfigurationInterpolator ci)
    {
        manager.setInterpolator(ci);
    }

    /**
     * Enables debug logging of xml-commons Catalog processing.
     * @param debug True if debugging should be enabled, false otherwise.
     */
    public void setDebug(final boolean debug)
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
     * <p>
     * Implements the {@code resolveEntity} method
     * for the SAX interface.
     * </p>
     * <p>Presented with an optional public identifier and a system
     * identifier, this function attempts to locate a mapping in the
     * catalogs.</p>
     * <p>If such a mapping is found, the resolver attempts to open
     * the mapped value as an InputSource and return it. Exceptions are
     * ignored and null is returned if the mapped value cannot be opened
     * as an input source.</p>
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
    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException
    {
        String resolved = getResolver().getResolvedEntity(publicId, systemId);

        if (resolved != null)
        {
            final String badFilePrefix = "file://";
            final String correctFilePrefix = "file:///";

            // Java 5 has a bug when constructing file URLS
            if (resolved.startsWith(badFilePrefix) && !resolved.startsWith(correctFilePrefix))
            {
                resolved = correctFilePrefix + resolved.substring(badFilePrefix.length());
            }

            try
            {
                final URL url = locate(fs, null, resolved);
                if (url == null)
                {
                    throw new ConfigurationException("Could not locate "
                            + resolved);
                }
                final InputStream is = fs.getInputStream(url);
                final InputSource iSource = new InputSource(resolved);
                iSource.setPublicId(publicId);
                iSource.setByteStream(is);
                return iSource;
            }
            catch (final Exception e)
            {
                log.warn("Failed to create InputSource for " + resolved, e);
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
    public ConfigurationLogger getLogger()
    {
        return log;
    }

    /**
     * Allows setting the logger to be used by this object. This
     * method makes it possible for clients to exactly control logging behavior.
     * Per default a logger is set that will ignore all log messages. Derived
     * classes that want to enable logging should call this method during their
     * initialization with the logger to be used. Passing in <b>null</b> as
     * argument disables logging.
     *
     * @param log the new logger
     */
    public void setLogger(final ConfigurationLogger log)
    {
        initLogger(log);
    }

    /**
     * Initializes the logger. Checks for null parameters.
     *
     * @param log the new logger
     */
    private void initLogger(final ConfigurationLogger log)
    {
        this.log = (log != null) ? log : ConfigurationLogger.newDummyLogger();
    }

    private synchronized org.apache.xml.resolver.tools.CatalogResolver getResolver()
    {
        if (resolver == null)
        {
            resolver = new org.apache.xml.resolver.tools.CatalogResolver(manager);
        }
        return resolver;
    }

    /**
     * Helper method for locating a given file. This implementation delegates to
     * the corresponding method in {@link FileLocatorUtils}.
     *
     * @param fs the {@code FileSystem}
     * @param basePath the base path
     * @param name the file name
     * @return the URL pointing to the file
     */
    private static URL locate(final FileSystem fs, final String basePath, final String name)
    {
        final FileLocator locator =
                FileLocatorUtils.fileLocator().fileSystem(fs)
                        .basePath(basePath).fileName(name).create();
        return FileLocatorUtils.locate(locator);
    }

    /**
     * Extend the CatalogManager to make the FileSystem and base directory accessible.
     */
    public static class CatalogManager extends org.apache.xml.resolver.CatalogManager
    {
        /** The static catalog used by this manager. */
        private static org.apache.xml.resolver.Catalog staticCatalog;

        /** The FileSystem */
        private FileSystem fs;

        /** The base directory */
        private String baseDir = System.getProperty("user.dir");

        /** The object for handling interpolation. */
        private ConfigurationInterpolator interpolator;

        /**
         * Set the FileSystem
         * @param fileSystem The FileSystem in use.
         */
        public void setFileSystem(final FileSystem fileSystem)
        {
            this.fs = fileSystem;
        }

        /**
         * Retrieve the FileSystem.
         * @return The FileSystem.
         */
        public FileSystem getFileSystem()
        {
            return this.fs;
        }

        /**
         * Set the base directory.
         * @param baseDir The base directory.
         */
        public void setBaseDir(final String baseDir)
        {
            if (baseDir != null)
            {
                this.baseDir = baseDir;
            }
        }

        /**
         * Return the base directory.
         * @return The base directory.
         */
        public String getBaseDir()
        {
            return this.baseDir;
        }

        public void setInterpolator(final ConfigurationInterpolator ci)
        {
            interpolator = ci;
        }

        public ConfigurationInterpolator getInterpolator()
        {
            return interpolator;
        }


        /**
         * Get a new catalog instance. This method is only overridden because xml-resolver
         * might be in a parent ClassLoader and will be incapable of loading our Catalog
         * implementation.
         *
         * This method always returns a new instance of the underlying catalog class.
         * @return the Catalog.
         */
        @Override
        public org.apache.xml.resolver.Catalog getPrivateCatalog()
        {
            org.apache.xml.resolver.Catalog catalog = staticCatalog;

            if (catalog == null || !getUseStaticCatalog())
            {
                try
                {
                    catalog = new Catalog();
                    catalog.setCatalogManager(this);
                    catalog.setupReaders();
                    catalog.loadSystemCatalogs();
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                }

                if (getUseStaticCatalog())
                {
                    staticCatalog = catalog;
                }
            }

            return catalog;
        }

        /**
         * Get a catalog instance.
         *
         * If this manager uses static catalogs, the same static catalog will
         * always be returned. Otherwise a new catalog will be returned.
         * @return The Catalog.
         */
        @Override
        public org.apache.xml.resolver.Catalog getCatalog()
        {
            return getPrivateCatalog();
        }
    }

    /**
     * Overrides the Catalog implementation to use the underlying FileSystem.
     */
    public static class Catalog extends org.apache.xml.resolver.Catalog
    {
        /** The FileSystem */
        private FileSystem fs;

        /** FileNameMap to determine the mime type */
        private final FileNameMap fileNameMap = URLConnection.getFileNameMap();

        /**
         * Load the catalogs.
         * @throws IOException if an error occurs.
         */
        @Override
        public void loadSystemCatalogs() throws IOException
        {
            fs = ((CatalogManager) catalogManager).getFileSystem();
            final String base = ((CatalogManager) catalogManager).getBaseDir();

            // This is safe because the catalog manager returns a vector of strings.
            @SuppressWarnings("unchecked")
            final
            Vector<String> catalogs = catalogManager.getCatalogFiles();
            if (catalogs != null)
            {
                for (int count = 0; count < catalogs.size(); count++)
                {
                    final String fileName = catalogs.elementAt(count);

                    URL url = null;
                    InputStream is = null;

                    try
                    {
                        url = locate(fs, base, fileName);
                        if (url != null)
                        {
                            is = fs.getInputStream(url);
                        }
                    }
                    catch (final ConfigurationException ce)
                    {
                        final String name = url.toString();
                        // Ignore the exception.
                        catalogManager.debug.message(DEBUG_ALL,
                            "Unable to get input stream for " + name + ". " + ce.getMessage());
                    }
                    if (is != null)
                    {
                        final String mimeType = fileNameMap.getContentTypeFor(fileName);
                        try
                        {
                            if (mimeType != null)
                            {
                                parseCatalog(mimeType, is);
                                continue;
                            }
                        }
                        catch (final Exception ex)
                        {
                            // Ignore the exception.
                            catalogManager.debug.message(DEBUG_ALL,
                                "Exception caught parsing input stream for " + fileName + ". "
                                + ex.getMessage());
                        }
                        finally
                        {
                            is.close();
                        }
                    }
                    parseCatalog(base, fileName);
                }
            }

        }

        /**
         * Parse the specified catalog file.
         * @param baseDir The base directory, if not included in the file name.
         * @param fileName The catalog file. May be a full URI String.
         * @throws IOException If an error occurs.
         */
        public void parseCatalog(final String baseDir, final String fileName) throws IOException
        {
            base = locate(fs, baseDir, fileName);
            catalogCwd = base;
            default_override = catalogManager.getPreferPublic();
            catalogManager.debug.message(DEBUG_NORMAL, "Parse catalog: " + fileName);

            boolean parsed = false;

            for (int count = 0; !parsed && count < readerArr.size(); count++)
            {
                final CatalogReader reader = (CatalogReader) readerArr.get(count);
                InputStream inStream;

                try
                {
                    inStream = fs.getInputStream(base);
                }
                catch (final Exception ex)
                {
                    catalogManager.debug.message(DEBUG_NORMAL, "Unable to access " + base
                        + ex.getMessage());
                    break;
                }

                try
                {
                    reader.readCatalog(this, inStream);
                    parsed = true;
                }
                catch (final CatalogException ce)
                {
                    catalogManager.debug.message(DEBUG_NORMAL, "Parse failed for " + fileName
                            + ce.getMessage());
                    if (ce.getExceptionType() == CatalogException.PARSE_FAILED)
                    {
                        break;
                    }
                    // try again!
                    continue;
                }
                finally
                {
                    try
                    {
                        inStream.close();
                    }
                    catch (final IOException ioe)
                    {
                        // Ignore the exception.
                        inStream = null;
                    }
                }
            }

            if (parsed)
            {
                parsePendingCatalogs();
            }
        }

        /**
         * Perform character normalization on a URI reference.
         *
         * @param uriref The URI reference
         * @return The normalized URI reference.
         */
        @Override
        protected String normalizeURI(final String uriref)
        {
            final ConfigurationInterpolator ci = ((CatalogManager) catalogManager).getInterpolator();
            final String resolved = ci != null ? String.valueOf(ci.interpolate(uriref)) : uriref;
            return super.normalizeURI(resolved);
        }
    }
}
