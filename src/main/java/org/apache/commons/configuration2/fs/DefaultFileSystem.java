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
package org.apache.commons.configuration2.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FileSystem that uses java.io.File or HttpClient
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public class DefaultFileSystem extends FileSystem
{
    /** Constant for the file URL protocol */
    private static final String FILE_SCHEME = "file:";
    /**
     * The Log for diagnostic messages.
     */
    private Log log = LogFactory.getLog(DefaultFileSystem.class);

    @Override
    public InputStream getInputStream(String basePath, String fileName)
        throws ConfigurationException
    {
        try
        {
            URL url = ConfigurationUtils.locate(this, basePath, fileName);

            if (url == null)
            {
                throw new ConfigurationException("Cannot locate configuration source " + fileName);
            }
            return getInputStream(url);
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to load the configuration file " + fileName, e);
        }
    }

    @Override
    public InputStream getInputStream(URL url) throws ConfigurationException
    {
        // throw an exception if the target URL is a directory
        File file = ConfigurationUtils.fileFromURL(url);
        if (file != null && file.isDirectory())
        {
            throw new ConfigurationException("Cannot load a configuration from a directory");
        }

        try
        {
            return url.openStream();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to load the configuration from the URL " + url, e);
        }
    }

    @Override
    public OutputStream getOutputStream(URL url) throws ConfigurationException
    {
        // file URLs have to be converted to Files since FileURLConnection is
        // read only (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4191800)
        File file = ConfigurationUtils.fileFromURL(url);
        if (file != null)
        {
            return getOutputStream(file);
        }
        else
        {
            // for non file URLs save through an URLConnection
            OutputStream out;
            try
            {
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);

                // use the PUT method for http URLs
                if (connection instanceof HttpURLConnection)
                {
                    HttpURLConnection conn = (HttpURLConnection) connection;
                    conn.setRequestMethod("PUT");
                }

                out = connection.getOutputStream();

                // check the response code for http URLs and throw an exception if an error occured
                if (connection instanceof HttpURLConnection)
                {
                    out = new HttpOutputStream(out, (HttpURLConnection) connection);
                }
                return out;
            }
            catch (IOException e)
            {
                throw new ConfigurationException("Could not save to URL " + url, e);
            }
        }
    }

    @Override
    public OutputStream getOutputStream(File file) throws ConfigurationException
    {
        try
        {
            // create the file if necessary
            createPath(file);
            return new FileOutputStream(file);
        }
        catch (FileNotFoundException e)
        {
            throw new ConfigurationException("Unable to save to file " + file, e);
        }
    }

    @Override
    public String getPath(File file, URL url, String basePath, String fileName)
    {
        String path = null;
        // if resource was loaded from jar file may be null
        if (file != null)
        {
            path = file.getAbsolutePath();
        }

        // try to see if file was loaded from a jar
        if (path == null)
        {
            if (url != null)
            {
                path = url.getPath();
            }
            else
            {
                try
                {
                    path = getURL(basePath, fileName).getPath();
                }
                catch (Exception e)
                {
                    // simply ignore it and return null
                    return null;
                }
            }
        }

        return path;
    }

    @Override
    public String getBasePath(String path)
    {
        URL url;
        try
        {
            url = getURL(null, path);
            return getBasePath(url);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public String getFileName(String path)
    {
        URL url;
        try
        {
            url = getURL(null, path);
            return getFileName(url);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    @Override
    public URL getURL(String basePath, String file) throws MalformedURLException
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


    @Override
    public URL locateFromURL(String basePath, String fileName)
    {
        try
        {
            URL url;
            if (basePath == null)
            {
                return new URL(fileName);
                //url = new URL(name);
            }
            else
            {
                URL baseURL = new URL(basePath);
                url = new URL(baseURL, fileName);

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
                return url;
            }
        }
        catch (IOException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Could not locate file " + fileName + " at " + basePath + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Helper method for constructing a {@code File} object from a base path and
     * a file name.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    public static File constructFile(String basePath, String fileName)
    {
        File file;

        File absolute = null;
        if (fileName != null)
        {
            absolute = new File(fileName);
        }

        if (StringUtils.isEmpty(basePath)
                || (absolute != null && absolute.isAbsolute()))
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
     * Extracts the file name from the specified URL.
     *
     * @param url the URL from which to extract the file name
     * @return the extracted file name
     */
    public static String getFileName(URL url)
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
     * Return the path without the file name, for example http://xyz.net/foo/bar.xml
     * results in http://xyz.net/foo/
     *
     * @param url the URL from which to extract the path
     * @return the path component of the passed in URL
     */
    public static String getBasePath(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String s = url.toString();
        if (s.startsWith(FILE_SCHEME) && !s.startsWith("file://"))
        {
            s = "file://" + s.substring(FILE_SCHEME.length());
        }

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
     * Create the path to the specified file.
     *
     * @param file the target file
     */
    private void createPath(File file)
    {
        if (file != null)
        {
            // create the path to the file if the file doesn't exist
            if (!file.exists())
            {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                {
                    parent.mkdirs();
                }
            }
        }
    }

    /**
     * Wraps the output stream so errors can be detected in the HTTP response.
     *
     * @author <a
     *         href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
     * @since 1.7
     */
    private static class HttpOutputStream extends VerifiableOutputStream
    {
        /**
         * The wrapped OutputStream
         */
        private final OutputStream stream;

        /**
         * The HttpURLConnection
         */
        private final HttpURLConnection connection;

        public HttpOutputStream(OutputStream stream, HttpURLConnection connection)
        {
            this.stream = stream;
            this.connection = connection;
        }

        @Override
        public void write(byte[] bytes) throws IOException
        {
            stream.write(bytes);
        }

        @Override
        public void write(byte[] bytes, int i, int i1) throws IOException
        {
            stream.write(bytes, i, i1);
        }

        @Override
        public void flush() throws IOException
        {
            stream.flush();
        }

        @Override
        public void close() throws IOException
        {
            stream.close();
        }

        @Override
        public void write(int i) throws IOException
        {
            stream.write(i);
        }

        @Override
        public String toString()
        {
            return stream.toString();
        }

        @Override
        public void verify() throws IOException
        {
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
            {
                throw new IOException("HTTP Error " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
        }
    }
}
