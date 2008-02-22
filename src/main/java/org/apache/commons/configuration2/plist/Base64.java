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

package org.apache.commons.configuration2.plist;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * A Base64 encoder/decoder that leverages the internal Base64 encoder of the Preferences API.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
class Base64 extends AbstractPreferences
{
    private static final Base64 instance = new Base64();

    /** The current value being converted. */
    private String value;

    private Base64()
    {
        super(null, "");
    }

    public static synchronized String encodeBase64(byte[] array)
    {
        instance.putByteArray(null, array);
        return instance.get(null, null);
    }

    public static synchronized byte[] decodeBase64(String base64String)
    {
        instance.put(null, base64String);
        return instance.getByteArray(null, null);
    }

    public String get(String key, String defaultValue)
    {
        return value;
    }

    /**
     * Overrides the default implementation to avoid the constraint on the length of the value.
     */
    public void put(String key, String value)
    {
        this.value = value;
    }

    protected void putSpi(String key, String value)
    {
    }

    protected String getSpi(String key)
    {
        return null;
    }

    protected void removeSpi(String key)
    {
    }

    protected void removeNodeSpi() throws BackingStoreException
    {
    }

    protected String[] keysSpi() throws BackingStoreException
    {
        return new String[0];
    }

    protected String[] childrenNamesSpi() throws BackingStoreException
    {
        return new String[0];
    }

    protected AbstractPreferences childSpi(String name)
    {
        return null;
    }

    protected void syncSpi() throws BackingStoreException
    {
    }

    protected void flushSpi() throws BackingStoreException
    {
    }
}
