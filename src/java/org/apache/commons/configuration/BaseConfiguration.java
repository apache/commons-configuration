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

import java.util.Iterator;

import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.collections.iterators.IteratorChain;

/**
 * Basic configuration classe. Stores the configuration data but does not
 * provide any load or save functions. If you want to load your Configuration
 * from a file use PropertiesConfiguration or XmlConfiguration.
 *
 * This class extends normal Java properties by adding the possibility
 * to use the same key many times concatenating the value strings
 * instead of overwriting them.
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
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 *
 * @version $Id: BaseConfiguration.java,v 1.2 2004/02/14 18:54:21 epugh Exp $
 */
public class BaseConfiguration extends AbstractConfiguration
{
    /** stores the configuration key-value pairs */
    private SequencedHashMap store = new SequencedHashMap();
    
    /**
     * Empty constructor.  You must add all the values to this configuration.
     */
    public BaseConfiguration()
    {
        super();
    }
    
    /**
     * Creates an empty BaseConfiguration object with
     * a Super-Object which is queries for every key.
     *
     * @param defaults Configuration defaults to use if key not in file
     */
    public BaseConfiguration(Configuration defaults) 
    {
        super(defaults);
    }

    /**
     * Adds a key/value pair to the map.  This routine does no magic morphing.
     * It ensures the keylist is maintained
     *
     * @param key key to use for mapping
     * @param obj object to store
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        Object o = getPropertyDirect(key);
        Object objAdd = null;
        
        if(o == null)
        {
            objAdd = obj;
        }
        else
        {
            if (o instanceof Container)
            {
                ((Container) o).add(obj);
            }
            else
            {
                // The token key is not a container.
                Container c = new Container();

                // There is an element. Put it into the container
                // at the first position
                c.add(o);

                // Now gobble up the supplied object
                c.add(obj);

                objAdd = c;
            }
        }
        
        if(objAdd != null)
        {
            store.put(key, objAdd);
        }
    }

    /**
     * Read property from underlying map.
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    protected Object getPropertyDirect(String key) 
    {
        return store.get(key);
    }

    /**
     * Check if the configuration is empty
     *
     * @return <code>true</code> if Configuration is empty,
     * <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return store.isEmpty() &((defaults != null) && defaults.isEmpty());
    }

    /**
     * check if the configuration contains the key
     *
     * @param key the configuration key
     * 
     * @return <code>true</code> if Configuration contain given key,
     * <code>false</code> otherwise.
     */
    public boolean containsKey(String key)
    {
        return store.containsKey(key)
        || ((defaults != null) && defaults.containsKey(key));
        
    }

    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    public void clearProperty(String key)
    {
        if (containsKey(key))
        {
            store.remove(key);
            if(defaults != null){
                defaults.clearProperty(key);
            }
        }
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys()
    {
        if(defaults != null){
            return new IteratorChain(defaults.getKeys(),store.iterator());
        }
        else {
            return store.iterator();
        }
    }
}
