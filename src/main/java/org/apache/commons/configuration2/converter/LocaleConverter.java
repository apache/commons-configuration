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

package org.apache.commons.configuration2.converter;

import java.util.Locale;

import org.apache.commons.configuration2.ConversionException;

/**
 * Locale converter.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
class LocaleConverter implements TypeConverter<Locale>
{
    private static final TypeConverter instance = new LocaleConverter();

    public static TypeConverter getInstance()
    {
        return instance;
    }

    public Locale convert(Object value, Object... params)
    {
        if (value instanceof String)
        {
            String[] elements = ((String) value).split("_");
            int size = elements.length;

            if (size >= 1 && ((elements[0]).length() == 2 || (elements[0]).length() == 0))
            {
                String language = elements[0];
                String country = (size >= 2) ? elements[1] : "";
                String variant = (size >= 3) ? elements[2] : "";

                return new Locale(language, country, variant);
            }
            else
            {
                throw new ConversionException("The value " + value + " can't be converted to a Locale");
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Locale");
        }
    }
}
