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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Miscellaneous utility methods for configurations.
 *
 * @author <a href="mailto:herve.quiroz@esil.univ-mrs.fr">Herve Quiroz</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public final class ConfigurationUtils
{
    /** Constant for the file URL protocol.*/
    static final String PROTOCOL_FILE = "file";

    private static Log log = LogFactory.getLog(ConfigurationUtils.class);

    private ConfigurationUtils()
    {
        // to prevent instanciation...
    }

    /**
     * Dump the configuration key/value mappings to some ouput stream.
     *
     * @param configuration the configuration
     * @param out the output stream to dump the configuration to
     */
    public static void dump(Configuration configuration, PrintStream out)
    {
        dump(configuration, new PrintWriter(out));
    }

    /**
     * Dump the configuration key/value mappings to some writer.
     *
     * @param configuration the configuration
     * @param out the writer to dump the configuration to
     */
    public static void dump(Configuration configuration, PrintWriter out)
    {
        Iterator keys = configuration.getKeys();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            Object value = configuration.getProperty(key);
            out.print(key);
            out.print("=");
            out.print(value);

            if (keys.hasNext())
            {
                out.println();
            }
        }

        out.flush();
    }

    /**
     * Get a string representation of the key/value mappings of a
     * configuration.
     *
     * @param configuration the configuration
     * @return a string representation of the configuration
     */
    public static String toString(Configuration configuration)
    {
        StringWriter writer = new StringWriter();
        dump(configuration, new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Copy all properties from the source configuration to the target
     * configuration. Properties in the target configuration are replaced with
     * the properties with the same key in the source configuration.
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 1.1
     */
    public static void copy(Configuration source, Configuration target)
    {
        Iterator keys = source.getKeys();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            target.setProperty(key, source.getProperty(key));
        }
    }

    /**
     * Append all properties from the source configuration to the target
     * configuration. Properties in the source configuration are appended to
     * the properties with the same key in the target configuration.
     *
     * @param source the source configuration
     * @param target the target configuration
     * @since 1.1
     */
    public static void append(Configuration source, Configuration target)
    {
        Iterator keys = source.getKeys();
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            target.addProperty(key, source.getProperty(key));
        }
    }

    /**
     * Constructs a URL from a base path and a file name. The file name can
     * be absolute, relative or a full URL. If necessary the base path URL is
     * applied.
     *
     * @param basePath the base path URL (can be <b>null</b>)
     * @param file the file name
     * @return the resulting URL
     * @throws MalformedURLException if URLs are invalid
     */
    public static URL getURL(String basePath, String file) throws MalformedURLException
    {
        File f = new File(file);
        if (f.isAbsolute()) // already absolute?
        {
            return f.toURL();
        }

        try
        {
            if (basePath == null)
            {
                return new URL(file);
            }
            else
            {
                URL base = new URL(basePath);
                return new URL(base, file);
            }
        }
        catch (MalformedURLException uex)
        {
            return constructFile(basePath, file).toURL();
        }
    }

    /**
     * Helper method for constructing a file object from a base path and a
     * file name. This method is called if the base path passed to
     * <code>getURL()</code> does not seem to be a valid URL.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    static File constructFile(String basePath, String fileName)
    {
        File file = null;

        File absolute = null;
        if (fileName != null)
        {
            absolute = new File(fileName);
        }

        if (StringUtils.isEmpty(basePath) || (absolute != null && absolute.isAbsolute()))
        {
            file = new File(fileName);
        }
        else
        {
            StringBuffer fName = new StringBuffer();
            fName.append(basePath);

            // My best friend. Paranoia.
            if (!basePath.endsWith(File.separator))
            {
                fName.append(File.separator);
            }

            //
            // We have a relative path, and we have
            // two possible forms here. If we have the
            // "./" form then just strip that off first
            // before continuing.
            //
            if (fileName.startsWith("." + File.separator))
            {
                fName.append(fileName.substring(2));
            }
            else
            {
                fName.append(fileName);
            }

            file = new File(fName.toString());
        }

        return file;
    }

    /**
     * Return the location of the specified resource by searching the user home
     * directory, the current classpath and the system classpath.
     *
     * @param name the name of the resource
     *
     * @return the location of the resource
     */
    public static URL locate(String name)
    {
        return locate(null, name);
    }

    /**
     * Return the location of the specified resource by searching the user home
     * directory, the current classpath and the system classpath.
     *
     * @param base the base path of the resource
     * @param name the name of the resource
     *
     * @return the location of the resource
     */
    public static URL locate(String base, String name)
    {
        URL url = null;

        // attempt to create an URL directly
        try
        {
            if (base == null)
            {
                url = new URL(name);
            }
            else
            {
                URL baseURL = new URL(base);
                url = new URL(baseURL, name);

                // check if the file exists
                InputStream in = null;
                try
                {
                    in = url.openStream();
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
            }

            log.debug("Configuration loaded from the URL " + url);
        }
        catch (IOException e)
        {
            url = null;
        }

        // attempt to load from an absolute path
        if (url == null)
        {
            File file = new File(name);
            if (file.isAbsolute() && file.exists()) // already absolute?
            {
                try
                {
                    url = file.toURL();
                    log.debug("Configuration loaded from the absolute path " + name);
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // attempt to load from the base directory
        if (url == null)
        {
            try
            {
                File file = constructFile(base, name);
                if (file != null && file.exists())
                {
                    url = file.toURL();
                }

                if (url != null)
                {
                    log.debug("Configuration loaded from the base path " + name);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // attempt to load from the user home directory
        if (url == null)
        {
            try
            {
                File file = constructFile(System.getProperty("user.home"), name);
                if (file != null && file.exists())
                {
                    url = file.toURL();
                }

                if (url != null)
                {
                    log.debug("Configuration loaded from the home path " + name);
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // attempt to load from the context classpath
        if (url == null)
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            url = loader.getResource(name);

            if (url != null)
            {
                log.debug("Configuration loaded from the context classpath (" + name + ")");
            }
        }

        // attempt to load from the system classpath
        if (url == null)
        {
            url = ClassLoader.getSystemResource(name);

            if (url != null)
            {
                log.debug("Configuration loaded from the system classpath (" + name + ")");
            }
        }

        return url;
    }

    /**
     * Return the path without the file name, for example http://xyz.net/foo/bar.xml
     * results in http://xyz.net/foo/
     *
     * @param url
     * @return
     */
    static String getBasePath(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String s = url.toString();

        if (s.endsWith("/") || StringUtils.isEmpty(url.getPath()))
        {
            return s;
        }
        else
        {
            return s.substring(0, s.lastIndexOf("/") + 1);
        }
    }

    /**
     * Extract the file name from the specified URL.
     */
    static String getFileName(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String path = url.getPath();

        if (path.endsWith("/") || StringUtils.isEmpty(path))
        {
            return null;
        }
        else
        {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }

    /**
     * Tries to convert the specified base path and file name into a file object.
     * This method is called e.g. by the save() methods of file based
     * configurations. The parameter strings can be relative files, absolute
     * files and URLs as well.
     * 
     * @param basePath the base path
     * @param fileName the file name
     * @return the file object (<b>null</b> if no file can be obtained)
     */
    public static File getFile(String basePath, String fileName)
    {
        // Check if URLs are involved
        URL url;
        try
        {
            url = new URL(new URL(basePath), fileName);
        }
        catch (MalformedURLException mex1)
        {
            try
            {
                url = new URL(fileName);
            }
            catch (MalformedURLException mex2)
            {
                url = null;
            }
        }

        if (url != null)
        {
            return fileFromURL(url);
        }

        return constructFile(basePath, fileName);
    }

    /**
     * Tries to convert the specified URL to a file object. If this fails,
     * <b>null</b> is returned.
     * 
     * @param url the URL
     * @return the resulting file object
     */
    static File fileFromURL(URL url)
    {
        if (PROTOCOL_FILE.equals(url.getProtocol()))
        {
            return new File(url.getPath());
        }
        else
        {
            return null;
        }
    }
}