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

import org.apache.commons.configuration2.ConversionException;
import org.apache.commons.lang.StringUtils;

/**
 * Color converter. This converter supports String value matching the format
 * (#)?[0-9A-F]{6}([0-9A-F]{2})?. Examples:
 * <ul>
 *   <li>FF0000 (red)</li>
 *   <li>0000FFA0 (semi transparent blue)</li>
 *   <li>#CCCCCC (gray)</li>
 *   <li>#00FF00A0 (semi transparent green)</li>
 * </ul>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 2.0
 */
class ColorConverter implements TypeConverter<Color>
{
    /** Constant for the radix of hex numbers.*/
    private static final int HEX_RADIX = 16;

    private static final TypeConverter instance = new ColorConverter();

    public static TypeConverter getInstance()
    {
        return instance;
    }

    public Color convert(Object value, Object... params)
    {
        if (value instanceof String && !StringUtils.isBlank((String) value))
        {
            String color = ((String) value).trim();

            int[] components = new int[3];

            // check the size of the string
            int minlength = components.length * 2;
            if (color.length() < minlength)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Color");
            }

            // remove the leading #
            if (color.startsWith("#"))
            {
                color = color.substring(1);
            }

            try
            {
                // parse the components
                for (int i = 0; i < components.length; i++)
                {
                    components[i] = Integer.parseInt(color.substring(2 * i, 2 * i + 2), HEX_RADIX);
                }

                // parse the transparency
                int alpha;
                if (color.length() >= minlength + 2)
                {
                    alpha = Integer.parseInt(color.substring(minlength, minlength + 2), HEX_RADIX);
                }
                else
                {
                    alpha = Color.BLACK.getAlpha();
                }

                return new Color(components[0], components[1], components[2], alpha);
            }
            catch (Exception e)
            {
                throw new ConversionException("The value " + value + " can't be converted to a Color", e);
            }
        }
        else
        {
            throw new ConversionException("The value " + value + " can't be converted to a Color");
        }
    }
}
