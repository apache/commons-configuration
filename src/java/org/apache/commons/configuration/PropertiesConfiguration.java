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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:daveb@miceda-data">Dave Bryson</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:kjohnson@transparent.com">Kent Johnson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:ipriha@surfeu.fi">Ilkka Priha</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: PropertiesConfiguration.java,v 1.3 2004/02/24 13:08:03 epugh Exp $
 */
public class PropertiesConfiguration
        extends BasePropertiesConfiguration
        implements Configuration
{
    /** Static logger */
    Log log = LogFactory.getLog(PropertiesConfiguration.class);

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
     * @throws IOException Error while loading the properties file
     */
    public PropertiesConfiguration(String fileName) throws ConfigurationException
    {

        load(fileName);
    }

    /**
     * Load the properties from the fileName set by setFileName 
     *
     * @throws IOException
     */
    public void load() throws ConfigurationException
    {
        load(getFileName());
    }

    /**
     * Load the properties from the given fileName
     *
     * @param fileName A properties file to load
     * @throws IOException
     */
    public void load(String fileName) throws ConfigurationException
    {
    	try {
    		load(getPropertyStream(fileName));
    	}
    	catch (IOException ioe){
    		throw new ConfigurationException("Could not load from file " + fileName,ioe);
    	}
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
