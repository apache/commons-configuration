package org.apache.commons.configuration;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

/**
 * Strict comparator for configurations.
 *
 * @author <a href="mailto:herve.quiroz@esil.univ-mrs.fr">Herve Quiroz</a>
 * @author <a href="mailto:shapira@mpi.com">Yoav Shapira</a>
 */
public class StrictConfigurationComparator implements ConfigurationComparator
{
    /**
     * Create a new strict comparator.
     */
    public StrictConfigurationComparator()
    {
    }

    /**
     * Compare two configuration objects.
     *
     * @param a the first configuration
     * @param b the second configuration
     * @return true if keys from a are found in b and keys from b are
     *         found in a and for each key in a, the corresponding value
     *         is the sale in for the same key in b
     */
    public boolean compare(Configuration a, Configuration b)
    {
        if ((a == null) && (b == null))
        {
            return true;
        }
        else if (a == null)
        {
            return false;
        }
        else if (b == null)
        {
            return false;
        }

        for (Iterator i = a.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            Object value = a.getProperty(key);
            if (!value.equals(b.getProperty(key)))
                return false;
        }
        for (Iterator i = b.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            Object value = b.getProperty(key);
            if (!value.equals(a.getProperty(key)))
                return false;
        }
        return true;
    }
}
