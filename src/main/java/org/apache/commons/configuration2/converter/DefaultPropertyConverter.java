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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.configuration2.ConversionException;

/**
 * Default implementation of the Converter interface.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
public class DefaultPropertyConverter implements Converter
{
    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> cls, Object value, Object... params) throws ConversionException
    {
        // return the value if it's already an instance of the requested type
        if (cls.isAssignableFrom(value.getClass()))
        {
            return (T) value;
        }

        // special cases for Enums
        if (cls.isEnum())
        {
            return (T) toEnum(value, cls.asSubclass(Enum.class));
        }

        TypeConverter<T> converter = getConverter(cls);

        if (converter == null)
        {
            throw new ConversionException("The value '" + value + "' (" + value.getClass() + ")"
                    + " can't be converted to a " + cls.getName() + " object");
        }
        else
        {
            return converter.convert(value, params);
        }
    }

    /**
     * Returns a converter suitable to converting a value into the specified type.
     *
     * @param cls the target class of the converter
     */
    @SuppressWarnings("unchecked")
    protected <T> TypeConverter<T> getConverter(Class<T> cls)
    {
       TypeConverter converter = null;

        if (Boolean.class.equals(cls) || Boolean.TYPE.equals(cls))
        {
            converter = BooleanConverter.getInstance();
        }
        else if (Character.class.equals(cls) || Character.TYPE.equals(cls))
        {
            converter = CharacterConverter.getInstance();
        }
        else if (Number.class.isAssignableFrom(cls) || cls.isPrimitive())
        {
            if (Integer.class.equals(cls) || Integer.TYPE.equals(cls))
            {
                converter = IntegerConverter.getInstance();
            }
            else if (Long.class.equals(cls) || Long.TYPE.equals(cls))
            {
                converter = LongConverter.getInstance();
            }
            else if (Byte.class.equals(cls) || Byte.TYPE.equals(cls))
            {
                converter = ByteConverter.getInstance();
            }
            else if (Short.class.equals(cls) || Short.TYPE.equals(cls))
            {
                converter = ShortConverter.getInstance();
            }
            else if (Float.class.equals(cls) || Float.TYPE.equals(cls))
            {
                converter = FloatConverter.getInstance();
            }
            else if (Double.class.equals(cls) || Double.TYPE.equals(cls))
            {
                converter = DoubleConverter.getInstance();
            }
            else if (BigInteger.class.equals(cls))
            {
                converter = BigIntegerConverter.getInstance();
            }
            else if (BigDecimal.class.equals(cls))
            {
                converter = BigDecimalConverter.getInstance();
            }
        }
        else if (Date.class.equals(cls))
        {
            converter = DateConverter.getInstance();
        }
        else if (Calendar.class.equals(cls))
        {
            converter = CalendarConverter.getInstance();
        }
        else if (URL.class.equals(cls))
        {
            converter = URLConverter.getInstance();
        }
        else if (Locale.class.equals(cls))
        {
            converter = LocaleConverter.getInstance();
        }
        else if (Color.class.equals(cls))
        {
            converter = ColorConverter.getInstance();
        }
        else if (cls.getName().equals("javax.mail.internet.InternetAddress"))
        {
            converter = InternetAddressConverter.getInstance();
        }
        else if (InetAddress.class.isAssignableFrom(cls))
        {
            converter = InetAddressConverter.getInstance();
        }

        return converter;
    }


    /**
     * Convert the specified value into a Java 5 enum.
     *
     * @param value the value to convert
     * @param cls   the type of the enumeration
     * @return the converted value
     * @throws ConversionException thrown if the value cannot be converted to an enumeration
     *
     * @since 1.5
     */
    static <T extends Enum<T>> T toEnum(Object value, Class<T> cls) throws ConversionException
    {
        if (value.getClass().equals(cls))
        {
            // already an enum => return directly
            return cls.cast(value);
        }
        else if (value instanceof String)
        {
            // For strings try to find the matching enum literal
            try
            {
                return Enum.valueOf(cls, String.valueOf(value));
            }
            catch (Exception e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a " + cls.getName());
            }
        }
        else if (value instanceof Number)
        {
            // A number is interpreted as the ordinal index of an enum literal
            try
            {
                T[] valuesArray = cls.getEnumConstants();
                return valuesArray[((Number) value).intValue()];
            }
            catch (Exception e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a " + cls.getName());
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a " + cls.getName());
        }
    }
}
