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

package org.apache.commons.configuration2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationErrorListener;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.lang.StringUtils;

/**
 * Miscellaneous utility methods for configurations.
 *
 * @see ConfigurationConverter Utility methods to convert configurations.
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

    /** Constant for the resource path separator.*/
    static final String RESOURCE_PATH_SEPARATOR = "/";

    /** Constant for the name of the clone() method.*/
    private static final String METHOD_CLONE = "clone";

    /** The logger.*/
    private static Logger log = Logger.getLogger(ConfigurationUtils.class.getName());

    /**
     * Private constructor. Prevents instances from being created.
     */
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
     * <p>Copy all properties from the source configuration to the target
     * configuration. Properties in the target configuration are replaced with
     * the properties with the same key in the source configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from <code>AbstractConfiguration</code> (e.g.
     * list delimiters). For a full support of all of these features the
     * <code>copy()</code> method of <code>AbstractConfiguration</code> should
     * be used. In a future release this method might become deprecated.</p>
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
     * <p>Append all properties from the source configuration to the target
     * configuration. Properties in the source configuration are appended to
     * the properties with the same key in the target configuration.</p>
     * <p><em>Note:</em> This method is not able to handle some specifics of
     * configurations derived from <code>AbstractConfiguration</code> (e.g.
     * list delimiters). For a full support of all of these features the
     * <code>copy()</code> method of <code>AbstractConfiguration</code> should
     * be used. In a future release this method might become deprecated.</p>
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
     * Converts the passed in configuration to a hierarchical one. If the
     * configuration is already hierarchical, it is directly returned. Otherwise
     * all properties are copied into a new hierarchical configuration.
     *
     * @param conf the configuration to convert
     * @return the new hierarchical configuration (the result is <b>null</b> if
     * and only if the passed in configuration is <b>null</b>)
     * @since 1.3
     */
    public static HierarchicalConfiguration convertToHierarchical(
            Configuration conf)
    {
        if (conf == null)
        {
            return null;
        }

        if (conf instanceof HierarchicalConfiguration)
        {
            return (HierarchicalConfiguration) conf;
        }
        else
        {
            HierarchicalConfiguration hc = new HierarchicalConfiguration();
            // Workaround for problem with copy()
            boolean delimiterParsingStatus = hc.isDelimiterParsingDisabled();
            hc.setDelimiterParsingDisabled(true);
            ConfigurationUtils.copy(conf, hc);
            hc.setDelimiterParsingDisabled(delimiterParsingStatus);
            return hc;
        }
    }

    /**
     * Clones the given configuration object if this is possible. If the passed
     * in configuration object implements the <code>Cloneable</code>
     * interface, its <code>clone()</code> method will be invoked. Otherwise
     * an exception will be thrown.
     *
     * @param config the configuration object to be cloned (can be <b>null</b>)
     * @return the cloned configuration (<b>null</b> if the argument was
     * <b>null</b>, too)
     * @throws ConfigurationRuntimeException if cloning is not supported for
     * this object
     * @since 1.3
     */
    public static Configuration cloneConfiguration(Configuration config)
            throws ConfigurationRuntimeException
    {
        if (config == null)
        {
            return null;
        }
        else
        {
            try
            {
                return (Configuration) clone(config);
            }
            catch (CloneNotSupportedException cnex)
            {
                throw new ConfigurationRuntimeException(cnex);
            }
        }
    }

    /**
     * An internally used helper method for cloning objects. This implementation
     * is not very sophisticated nor efficient. Maybe it can be replaced by an
     * implementation from Commons Lang later. The method checks whether the
     * passed in object implements the <code>Cloneable</code> interface. If
     * this is the case, the <code>clone()</code> method is invoked by
     * reflection. Errors that occur during the cloning process are re-thrown as
     * runtime exceptions.
     *
     * @param obj the object to be cloned
     * @return the cloned object
     * @throws CloneNotSupportedException if the object cannot be cloned
     */
    public static Object clone(Object obj) throws CloneNotSupportedException
    {
        if (obj instanceof Cloneable)
        {
            try
            {
                Method m = obj.getClass().getMethod(METHOD_CLONE);
                return m.invoke(obj);
            }
            catch (NoSuchMethodException nmex)
            {
                throw new CloneNotSupportedException("No clone() method found for class" + obj.getClass().getName());
            }
            catch (IllegalAccessException iaex)
            {
                throw new ConfigurationRuntimeException(iaex);
            }
            catch (InvocationTargetException itex)
            {
                throw new ConfigurationRuntimeException(itex);
            }
        }
        else
        {
            throw new CloneNotSupportedException(obj.getClass().getName() + " does not implement Cloneable");
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
            return f.toURI().toURL();
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
            return constructFile(basePath, file).toURI().toURL();
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
            StringBuilder fName = new StringBuilder();
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
        if (log.isLoggable(Level.FINE))
        {
            StringBuilder buf = new StringBuilder();
            buf.append("ConfigurationUtils.locate(): base is ").append(base);
            buf.append(", name is ").append(name);
            log.fine(buf.toString());
        }

        if (name == null)
        {
            // undefined, always return null
            return null;
        }

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

            log.fine("Loading configuration from the URL " + url);
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
                    url = file.toURI().toURL();
                    log.fine("Loading configuration from the absolute path " + name);
                }
                catch (MalformedURLException e)
                {
                    log.log(Level.WARNING, "Could not obtain URL from file", e);
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
                    url = file.toURI().toURL();
                }

                if (url != null)
                {
                    log.fine("Loading configuration from the path " + file);
                }
            }
            catch (MalformedURLException e)
            {
                log.log(Level.WARNING, "Could not obtain URL from file", e);
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
                    url = file.toURI().toURL();
                }

                if (url != null)
                {
                    log.fine("Loading configuration from the home path " + file);
                }

            }
            catch (MalformedURLException e)
            {
                log.log(Level.WARNING, "Could not obtain URL from file", e);
            }
        }

        // attempt to load from classpath
        if (url == null)
        {
            url = locateFromClasspath(name);
        }
        return url;
    }

    /**
     * Tries to find a resource with the given name in the classpath.
     * @param resourceName the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     * cannot be found
     */
    static URL locateFromClasspath(String resourceName)
    {
        URL url = null;
        // attempt to load from the context classpath
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null)
        {
            url = loader.getResource(resourceName);

            if (url != null)
            {
                log.fine("Loading configuration from the context classpath (" + resourceName + ")");
            }
        }

        // attempt to load from the system classpath
        if (url == null)
        {
            url = ClassLoader.getSystemResource(resourceName);

            if (url != null)
            {
                log.fine("Loading configuration from the system classpath (" + resourceName + ")");
            }
        }
        return url;
    }

    /**
     * Return the path without the file name, for example http://xyz.net/foo/bar.xml
     * results in http://xyz.net/foo/
     *
     * @param url the URL from which to extract the path
     * @return the path component of the passed in URL
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
     *
     * @param url the URL from which to extract the file name
     * @return the extracted file name
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
     * files and URLs as well. This implementation checks first whether the passed in
     * file name is absolute. If this is the case, it is returned. Otherwise
     * further checks are performed whether the base path and file name can be
     * combined to a valid URL or a valid file name. <em>Note:</em> The test
     * if the passed in file name is absolute is performed using
     * <code>java.io.File.isAbsolute()</code>. If the file name starts with a
     * slash, this method will return <b>true</b> on Unix, but <b>false</b> on
     * Windows. So to ensure correct behavior for relative file names on all
     * platforms you should never let relative paths start with a slash. E.g.
     * in a configuration definition file do not use something like that:
     * <pre>
     * &lt;properties fileName="/subdir/my.properties"/&gt;
     * </pre>
     * Under Windows this path would be resolved relative to the configuration
     * definition file. Under Unix this would be treated as an absolute path
     * name.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the file object (<b>null</b> if no file can be obtained)
     */
    public static File getFile(String basePath, String fileName)
    {
        // Check if the file name is absolute
        File f = new File(fileName);
        if (f.isAbsolute())
        {
            return f;
        }

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
    public static File fileFromURL(URL url)
    {
        if (PROTOCOL_FILE.equals(url.getProtocol()))
        {
            return new File(URLDecoder.decode(url.getPath()));
        }
        else
        {
            return null;
        }
    }

    /**
     * Enables runtime exceptions for the specified configuration object. This
     * method can be used for configuration implementations that may face errors
     * on normal property access, e.g. <code>DatabaseConfiguration</code> or
     * <code>JNDIConfiguration</code>. Per default such errors are simply
     * logged and then ignored. This implementation will register a special
     * <code>{@link ConfigurationErrorListener}</code> that throws a runtime
     * exception (namely a <code>ConfigurationRuntimeException</code>) on
     * each received error event.
     *
     * @param src the configuration, for which runtime exceptions are to be
     * enabled; this configuration must be derived from
     * <code>{@link EventSource}</code>
     */
    public static void enableRuntimeExceptions(Configuration src)
    {
        if (!(src instanceof EventSource))
        {
            throw new IllegalArgumentException("Configuration must be derived from EventSource!");
        }
        ((EventSource) src).addErrorListener(new ConfigurationErrorListener()
        {
            public void configurationError(ConfigurationErrorEvent event)
            {
                // Throw a runtime exception
                throw new ConfigurationRuntimeException(event.getCause());
            }
        });
    }
}
