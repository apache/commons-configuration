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

import org.apache.commons.configuration2.ConversionException;

/**
 * Character converter.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
public class CharacterConverter implements TypeConverter
{
    private static final TypeConverter instance = new CharacterConverter();

    public static TypeConverter getInstance()
    {
        return instance;
    }

    public Object convert(Object value, Object[] params) throws ConversionException
    {
        if (value instanceof CharSequence && ((CharSequence) value).length() == 1)
        {
            return ((CharSequence) value).charAt(0);
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Character object");
        }
    }
}
