package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @author <a href="mailto:herve.quiroz@esil.univ-mrs.fr">Herve Quiroz</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 */
public class ConfigurationUtils
{
    /** File separator. */
    protected static String fileSeparator = System.getProperty("file.separator");

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
