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

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>A base class for &quot;faked&quot; {@code XMLReader} classes
 * that transform a configuration object in a set of SAX parsing events.</p>
 * <p>This class provides dummy implementations for most of the methods
 * defined in the {@code XMLReader} interface that are not used for this
 * special purpose. There will be concrete sub classes that process specific
 * configuration classes.</p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public abstract class ConfigurationXMLReader implements XMLReader
{
    /** Constant for the namespace URI.*/
    protected static final String NS_URI = "";

    /** Constant for the default name of the root element.*/
    private static final String DEFAULT_ROOT_NAME = "config";

    /** An empty attributes object.*/
    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

    /** Stores the content handler.*/
    private ContentHandler contentHandler;

    /** Stores an exception that occurred during parsing.*/
    private SAXException exception;

    /** Stores the name for the root element.*/
    private String rootName;

    /**
     * Creates a new instance of {@code ConfigurationXMLReader}.
     */
    protected ConfigurationXMLReader()
    {
        super();
        rootName = DEFAULT_ROOT_NAME;
    }

    /**
     * Parses the current configuration object. The passed system ID will be
     * ignored.
     *
     * @param systemId the system ID (ignored)
     * @throws IOException if no configuration was specified
     * @throws SAXException if an error occurs during parsing
     */
    @Override
    public void parse(final String systemId) throws IOException, SAXException
    {
        parseConfiguration();
    }

    /**
     * Parses the actual configuration object. The passed input source will be
     * ignored.
     *
     * @param input the input source (ignored)
     * @throws IOException if no configuration was specified
     * @throws SAXException if an error occurs during parsing
     */
    @Override
    public void parse(final InputSource input) throws IOException, SAXException
    {
        parseConfiguration();
    }

    /**
     * Dummy implementation of the interface method.
     *
     * @param name the name of the feature
     * @return always <b>false</b> (no features are supported)
     */
    @Override
    public boolean getFeature(final String name)
    {
        return false;
    }

    /**
     * Dummy implementation of the interface method.
     *
     * @param name the name of the feature to be set
     * @param value the value of the feature
     */
    @Override
    public void setFeature(final String name, final boolean value)
    {
    }

    /**
     * Returns the actually set content handler.
     *
     * @return the content handler
     */
    @Override
    public ContentHandler getContentHandler()
    {
        return contentHandler;
    }

    /**
     * Sets the content handler. The object specified here will receive SAX
     * events during parsing.
     *
     * @param handler the content handler
     */
    @Override
    public void setContentHandler(final ContentHandler handler)
    {
        contentHandler = handler;
    }

    /**
     * Returns the DTD handler. This class does not support DTD handlers,
     * so this method always returns <b>null</b>.
     *
     * @return the DTD handler
     */
    @Override
    public DTDHandler getDTDHandler()
    {
        return null;
    }

    /**
     * Sets the DTD handler. The passed value is ignored.
     *
     * @param handler the handler to be set
     */
    @Override
    public void setDTDHandler(final DTDHandler handler)
    {
    }

    /**
     * Returns the entity resolver. This class does not support an entity
     * resolver, so this method always returns <b>null</b>.
     *
     * @return the entity resolver
     */
    @Override
    public EntityResolver getEntityResolver()
    {
        return null;
    }

    /**
     * Sets the entity resolver. The passed value is ignored.
     *
     * @param resolver the entity resolver
     */
    @Override
    public void setEntityResolver(final EntityResolver resolver)
    {
    }

    /**
     * Returns the error handler. This class does not support an error handler,
     * so this method always returns <b>null</b>.
     *
     * @return the error handler
     */
    @Override
    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    /**
     * Sets the error handler. The passed value is ignored.
     *
     * @param handler the error handler
     */
    @Override
    public void setErrorHandler(final ErrorHandler handler)
    {
    }

    /**
     * Dummy implementation of the interface method. No properties are
     * supported, so this method always returns <b>null</b>.
     *
     * @param name the name of the requested property
     * @return the property value
     */
    @Override
    public Object getProperty(final String name)
    {
        return null;
    }

    /**
     * Dummy implementation of the interface method. No properties are
     * supported, so a call of this method just has no effect.
     *
     * @param name the property name
     * @param value the property value
     */
    @Override
    public void setProperty(final String name, final Object value)
    {
    }

    /**
     * Returns the name to be used for the root element.
     *
     * @return the name for the root element
     */
    public String getRootName()
    {
        return rootName;
    }

    /**
     * Sets the name for the root element.
     *
     * @param string the name for the root element.
     */
    public void setRootName(final String string)
    {
        rootName = string;
    }

    /**
     * Fires a SAX element start event.
     *
     * @param name the name of the actual element
     * @param attribs the attributes of this element (can be <b>null</b>)
     */
    protected void fireElementStart(final String name, final Attributes attribs)
    {
        if (getException() == null)
        {
            try
            {
                final Attributes at = (attribs == null) ? EMPTY_ATTRS : attribs;
                getContentHandler().startElement(NS_URI, name, name, at);
            }
            catch (final SAXException ex)
            {
                exception = ex;
            }
        }
    }

    /**
     * Fires a SAX element end event.
     *
     * @param name the name of the affected element
     */
    protected void fireElementEnd(final String name)
    {
        if (getException() == null)
        {
            try
            {
                getContentHandler().endElement(NS_URI, name, name);
            }
            catch (final SAXException ex)
            {
                exception = ex;
            }
        }
    }

    /**
     * Fires a SAX characters event.
     *
     * @param text the text
     */
    protected void fireCharacters(final String text)
    {
        if (getException() == null)
        {
            try
            {
                final char[] ch = text.toCharArray();
                getContentHandler().characters(ch, 0, ch.length);
            }
            catch (final SAXException ex)
            {
                exception = ex;
            }
        }
    }

    /**
     * Returns a reference to an exception that occurred during parsing.
     *
     * @return a SAXExcpetion or <b>null</b> if none occurred
     */
    public SAXException getException()
    {
        return exception;
    }

    /**
     * Parses the configuration object and generates SAX events. This is the
     * main processing method.
     *
     * @throws IOException if no configuration has been specified
     * @throws SAXException if an error occurs during parsing
     */
    protected void parseConfiguration() throws IOException, SAXException
    {
        if (getParsedConfiguration() == null)
        {
            throw new IOException("No configuration specified!");
        }

        if (getContentHandler() != null)
        {
            exception = null;
            getContentHandler().startDocument();
            processKeys();
            if (getException() != null)
            {
                throw getException();
            }
            getContentHandler().endDocument();
        }
    }

    /**
     * Returns a reference to the configuration that is parsed by this object.
     *
     * @return the parsed configuration
     */
    public abstract Configuration getParsedConfiguration();

    /**
     * Processes all keys stored in the actual configuration. This method is
     * called by {@code parseConfiguration()} to start the main parsing
     * process. {@code parseConfiguration()} calls the content handler's
     * {@code startDocument()} and {@code endElement()} methods
     * and cares for exception handling. The remaining actions are left to this
     * method that must be implemented in a concrete sub class.
     *
     * @throws IOException if an IO error occurs
     * @throws SAXException if a SAX error occurs
     */
    protected abstract void processKeys() throws IOException, SAXException;
}
