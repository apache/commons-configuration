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

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

/**
 * Miscellaneous utility methods for configurations.
 *
 */
public class ConfigurationUtils
{
    /** File separator. */
    protected static final String fileSeparator = System.getProperty("file.separator");

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
        for (Iterator i = configuration.getKeys(); i.hasNext(); )
        {
            String key = (String) i.next();
            Object value = configuration.getProperty(key);
            out.print(key);
            out.print("=");
            out.print(value);
            if (i.hasNext())
            {
                out.println();
            }
        }
    }
    
    /**
     * Dump the configuration key/value mappings to some writer.
     *
     * @param configuration the configuration
     * @param out the writer to dump the configuration to
     */
    public static void dump(Configuration configuration, PrintWriter out)
    {
        for (Iterator i = configuration.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            Object value = configuration.getProperty(key);
            out.print(key);
            out.print("=");
            out.print(value);

            if (i.hasNext())
            {
                out.println();
            }
        }
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
     * Constructs a URL from a base path and a file name. The file name can
     * be absolute, relative or a full URL. If necessary the base path URL is
     * applied.
     * @param basePath the base path URL (can be <b>null</b>)
     * @param file the file name
     * @return the resulting URL
     * @throws MalformedURLException if URLs are invalid
     */
    public static URL getURL(String basePath, String file)
    throws MalformedURLException
    {
        File f = new File(file);
        if(f.isAbsolute())                          // already absolute?
        {
            return f.toURL();
        }  /* if */

        try
        {
            if(basePath == null)
            {
                return new URL(file);
            }  /* if */
            else
            {
                URL base = new URL(basePath);
                return new URL(base, file);
            }  /* else */
        }  /* try */
        catch(MalformedURLException uex)
        {
            return constructFile(basePath, file).toURL();
        }  /* catch */
    }

    /**
     * Helper method for constructing a file object from a base path and a
     * file name. This method is called if the base path passed to
     * <code>getURL()</code> does not seem to be a valid URL.
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    static File constructFile(String basePath, String fileName)
    {
        // code from DOM4JConfiguration
        File file = null;
        if (StringUtils.isEmpty(basePath))
        {
            // Good luck... This will fail 99 out of 100 times.
            file = new File(fileName);
        }
        else
        {
            StringBuffer fName = new StringBuffer();
            fName.append(basePath);

            // My best friend. Paranoia.
            if (!basePath.endsWith(fileSeparator))
            {
                fName.append(fileSeparator);
            }

            //
            // We have a relative path, and we have
            // two possible forms here. If we have the
            // "./" form then just strip that off first
            // before continuing.
            //
            if (fileName.startsWith("." + fileSeparator))
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
}
