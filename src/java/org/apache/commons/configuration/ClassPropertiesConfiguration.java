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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads the configuration from the classpath utilizing a specified class to get
 * the classloader from. The properties file will be attempted to be loaded
 * first from the classes package directory and then from the class path in
 * general.
 * <p>
 * This class does not support an empty constructor and saving of a
 * synthesized properties file. Use PropertiesConfiguration for this.
 *
 * @see org.apache.commons.configuration.BasePropertiesConfiguration
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: ClassPropertiesConfiguration.java,v 1.8 2004/06/24 12:35:14 ebourg Exp $
 */
public class ClassPropertiesConfiguration extends BasePropertiesConfiguration implements Configuration
{
    /** Base class, which is used to load all relative class references */
    private Class baseClass = null;

    /** Class Loader which we will use to load the resources */
    private ClassLoader classLoader = null;

    /**
     * Creates and loads an extended properties file from the Class
     * Resources. Uses the class loader.
     *
     * @param baseClass The class providing the FileStream.
     * @param resource The name of the Resource.
     * @throws ConfigurationException Error while loading the properties file
     */
    public ClassPropertiesConfiguration(Class baseClass, String resource) throws ConfigurationException
    {
        this.baseClass = baseClass;
        // According to javadocs, getClassLoader() might return null
        // if it represents the "bootstrap class loader"
        // Use the System class loader in this case.
        classLoader = (baseClass.getClassLoader() == null)
            ? ClassLoader.getSystemClassLoader()
            : baseClass.getClassLoader();

        setIncludesAllowed(true);
        try
        {
            load(getPropertyStream(resource));
        }
        catch (IOException ioe)
        {
            throw new ConfigurationException("Could not load input stream from resource " + resource, ioe);
        }
    }

    /**
     * Gets a resource relative to the supplied base class or
     * from the class loader if it is not found from the supplied base class.
     *
     * @param resourceName The resource Name
     * @return An Input Stream
     * @throws IOException Error while loading the properties file
     */
    protected InputStream getPropertyStream(String resourceName) throws IOException
    {
        InputStream resource = null;

        //First try to load from within the package of the provided class
        resource = baseClass.getResourceAsStream(resourceName);

        if (resource == null)
        {
          resource = classLoader.getResourceAsStream(resourceName);
        }

        if (resource == null)
        {
            throw new FileNotFoundException("Could not open Resource " + resourceName);
        }

        return resource;
    }
}
