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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.configuration2.ConversionException;

/**
 * Boolean converter.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
class BooleanConverter implements TypeConverter<Boolean>
{
    private static final BooleanConverter instance = new BooleanConverter();

    public static BooleanConverter getInstance()
    {
        return instance;
    }

    public Boolean convert(Object value, Object... params)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            Boolean b = BooleanUtils.toBooleanObject((String) value);
            if (b == null)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Boolean object");
            }
            return b;
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Boolean object");
        }
    }
}
