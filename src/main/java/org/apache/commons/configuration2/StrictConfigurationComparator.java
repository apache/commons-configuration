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

import java.util.Iterator;

/**
 * Strict comparator for configurations.
 *
 * @since 1.0
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
    @Override
    public boolean compare(final Configuration a, final Configuration b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if (a == null || b == null)
        {
            return false;
        }

        for (final Iterator<String> keys = a.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final Object value = a.getProperty(key);
            if (!value.equals(b.getProperty(key)))
            {
                return false;
            }
        }

        for (final Iterator<String> keys = b.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final Object value = b.getProperty(key);
            if (!value.equals(a.getProperty(key)))
            {
                return false;
            }
        }

        return true;
    }
}
