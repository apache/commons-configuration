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
 * @version $Id: ClassPropertiesConfiguration.java,v 1.3 2004/02/24 13:08:03 epugh Exp $
 */
public class ClassPropertiesConfiguration
    extends BasePropertiesConfiguration
    implements Configuration
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
     * @throws IOException Error while loading the properties file
     */
    public ClassPropertiesConfiguration(Class baseClass, String resource)
        throws ConfigurationException
    {
        this.baseClass = baseClass;
        // According to javadocs, getClassLoader() might return null
        // if it represents the "bootstrap class loader"
        // Use the System class loader in this case.
        classLoader = (baseClass.getClassLoader() == null) 
            ? ClassLoader.getSystemClassLoader()
            : baseClass.getClassLoader();
        
        setIncludesAllowed(true);
        try {
        	load(getPropertyStream(resource));
        }
        catch (IOException ioe){
        	throw new ConfigurationException("Could not load input stream from resource " + resource,ioe);
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
    protected InputStream getPropertyStream(String resourceName)
        throws IOException
    {
        InputStream resource = null;
        //For backwards compatibility with earlier versions, 
        //strip a leading "./" from the 
            if (resourceName.startsWith("./"))
            {
                //classPath.append(resourceName.substring(2));
            }
        
        //First try to load from within the package of the provided class
        resource = baseClass.getResourceAsStream(resourceName);
        
        if (resource == null)
        {
          resource = classLoader.getResourceAsStream(resourceName);
        }

        if (resource == null)
        {
            throw new FileNotFoundException("Could not open Resource "
                                            + resourceName);
        }

        return resource;
    }
}

     

    
