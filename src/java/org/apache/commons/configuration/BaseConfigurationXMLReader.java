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


/**
 * <p>A specialized SAX2 XML parser that processes configuration objects.</p>
 * <p>This class mimics to be a SAX compliant XML parser. It is able to iterate
 * over the keys in a configuration object and to generate corresponding SAX
 * events. By registering a <code>ContentHandler</code> at an instance
 * it is possible to perform XML processing on a configuration object.</p>
 *
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: BaseConfigurationXMLReader.java,v 1.2 2004/02/12 12:59:19 epugh Exp $
 */
public class BaseConfigurationXMLReader extends ConfigurationXMLReader
{
    /** Stores the actual configuration.*/
    private Configuration config;

    /**
     * Creates a new instance of <code>BaseConfigurationXMLReader</code>.
     */
    public BaseConfigurationXMLReader()
    {
        super();
    }

    /**
     * Creates a new instance of <code>BaseConfigurationXMLReader</code> and
     * sets the configuration object to be parsed.
     * @param conf the configuration to be parsed
     */
    public BaseConfigurationXMLReader(Configuration conf)
    {
        this();
        setConfiguration(conf);
    }

    /**
     * Returns the actual configuration to be processed.
     * @return the actual configuration
     */
    public Configuration getConfiguration()
    {
        return config;
    }

    /**
     * Sets the configuration to be processed.
     * @param conf the configuration
     */
    public void setConfiguration(Configuration conf)
    {
        config = conf;
    }

    /**
     * Returns the configuration to be processed.
     * @return the actual configuration
     */
    public Configuration getParsedConfiguration()
    {
        return getConfiguration();
    }

    /**
     * The main SAX event generation method. This element uses an internal
     * <code>HierarchicalConfigurationConverter</code> object to iterate over
     * all keys in the actual configuration and to generate corresponding SAX
     * events.
     */
    protected void processKeys()
    {
        fireElementStart(getRootName(), null);
        new SAXConverter().process(getConfiguration());
        fireElementEnd(getRootName());
    }

    /**
     * An internally used helper class to iterate over all configuration keys
     * ant to generate corresponding SAX events.
     *
     * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
     */
    class SAXConverter extends HierarchicalConfigurationConverter
    {
        /**
         * Callback for the start of an element.
         * @param name the element name
         * @param value the element value
         */
        protected void elementStart(String name, Object value)
        {
            fireElementStart(name, null);
            if(value != null)
            {
                fireCharacters(value.toString());
            }  /* if */
        }

        /**
         * Callback for the end of an element.
         * @param name the element name
         */
        protected void elementEnd(String name)
        {
            fireElementEnd(name);
        }
    }
}
