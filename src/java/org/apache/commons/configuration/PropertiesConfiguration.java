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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

/**
 * This is the "classic" Properties loader which loads the values from
 * a single or multiple files (which can be chained with "include =".
 * All given path references are either absolute or relative to the
 * file name supplied in the Constructor.
 * <p>
 * In this class, empty PropertyConfigurations can be built, properties
 * added and later saved. include statements are (obviously) not supported
 * if you don't construct a PropertyConfiguration from a file.
 * <p>
 * If you want to use the getResourceAsStream() trick to load your
 * resources without an absolute path, please take a look at the
 * ClassPropertiesConfiguration which is intended to be used for this.
 *
 * @version $Id: PropertiesConfiguration.java,v 1.10 2004/06/23 11:15:45 ebourg Exp $
 */
public class PropertiesConfiguration extends BasePropertiesConfiguration
{
    /** File separator. */
    protected String fileSeparator = System.getProperty("file.separator");

    /**
     * The name of the file to be loaded.  This is used in conjuction with
     * the load method. */
    protected String fileName = null;

    /**
     * Creates an empty PropertyConfiguration object which can be
     * used to synthesize a new Properties file by adding values and
     * then saving(). An object constructed by this C'tor can not be
     * tickled into loading included files because it cannot supply a
     * base for relative includes.
     */
    public PropertiesConfiguration()
    {
        setIncludesAllowed(false);
    }

    /**
     * Creates and loads the extended properties from the specified file.
     * The specified file can contain "include = " properties which then
     * are loaded and merged into the properties.
     *
     * @param fileName The name of the Properties File to load.
     * @throws ConfigurationException Error while loading the properties file
     */
    public PropertiesConfiguration(String fileName) throws ConfigurationException
    {
        load(fileName);
    }

    /**
     * Load the properties from the fileName set by setFileName
     *
     * @throws ConfigurationException
     */
    public void load() throws ConfigurationException
    {
        load(getFileName());
    }

    /**
     * Load the properties from the given fileName
     *
     * @param fileName A properties file to load
     * @throws ConfigurationException
     */
    public void load(String fileName) throws ConfigurationException
    {
        InputStream in = null;
        try
        {
            in = getPropertyStream(fileName);
            load(in);
        }
        catch (IOException e)
        {
            throw new ConfigurationException("Could not load from file " + fileName, e);
        }
        finally
        {
            // close the input stream
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Save the configuration to the file specified by the fileName attribute.
     */
    public void save() throws ConfigurationException
    {
        save(fileName);
    }

    /**
     * Gets a resource relative to the supplied base path. If the passed in
     * resource name is absolute, it is used directly.
     *
     * @param resourceName The resource Name
     * @return An Input Stream
     * @throws IOException Error while loading the properties file
     */
    protected InputStream getPropertyStream(String resourceName) throws IOException
    {
        InputStream resource = null;
        URL url = null;

        try
        {
            url = ConfigurationUtils.getURL(getBasePath(), resourceName);
        }  /* try */
        catch(MalformedURLException uex)
        {
            throw new IOException("Cannot obtain URL for resource "
            + resourceName);
        }  /* catch */

        resource = url.openStream();

        setBasePath(url.toString());
        setIncludesAllowed(true);

        return resource;
    }

    /**
     * Returns the fileName.
     * @return String
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Sets the fileName.
     * @param fileName The fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * Extend the setBasePath method to turn includes
     * on and off based on the existence of a base path.
     *
     * @param basePath The new basePath to set.
     */
    public void setBasePath(String basePath)
    {
        super.setBasePath(basePath);
        setIncludesAllowed(StringUtils.isNotEmpty(basePath));
    }
}
