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

import java.math.BigInteger;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.configuration2.ConversionException;

/**
 * Base converter for numbers.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
abstract class NumberConverter<T extends Number> implements TypeConverter<T>
{
    /** Constant for the prefix of hex numbers.*/
    private static final String HEX_PREFIX = "0x";

    /** Constant for the radix of hex numbers.*/
    private static final int HEX_RADIX = 16;

    /**
     * Tries to convert the specified object into a number object. This method
     * is used by the conversion methods for number types. Note that the return
     * value is not in always of the specified target class, but only if a new
     * object has to be created.
     *
     * @param value the value to be converted (must not be <b>null</b>)
     * @param targetClass the target class of the conversion (must be derived
     * from <code>java.lang.Number</code>)
     * @return the converted number
     * @throws org.apache.commons.configuration2.ConversionException if the object cannot be converted
     */
    protected Number toNumber(Object value, Class<? extends Number> targetClass) throws ConversionException
    {
        if (value instanceof Number)
        {
            return (Number) value;
        }
        else
        {
            String str = value.toString();
            if (str.startsWith(HEX_PREFIX))
            {
                try
                {
                    return new BigInteger(str.substring(HEX_PREFIX.length()), HEX_RADIX);
                }
                catch (NumberFormatException nex)
                {
                    throw new ConversionException("Could not convert " + str
                            + " to " + targetClass.getName()
                            + "! Invalid hex number.", nex);
                }
            }

            try
            {
                Constructor<? extends Number> constr = targetClass.getConstructor(String.class);
                return (Number) constr.newInstance(str);
            }
            catch (InvocationTargetException itex)
            {
                throw new ConversionException("Could not convert " + str
                        + " to " + targetClass.getName(), itex.getTargetException());
            }
            catch (Exception ex)
            {
                // Treat all possible exceptions the same way
                throw new ConversionException("Conversion error when trying to convert " + str
                                + " to " + targetClass.getName(), ex);
            }
        }
    }
}
