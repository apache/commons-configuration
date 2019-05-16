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
package org.apache.commons.configuration2.convert;

import java.util.Collection;

import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

/**
 * <p>
 * An interface defining the possible data type conversions supported by the
 * configuration framework.
 * </p>
 * <p>
 * This interface defines a couple of methods related to different kinds of data
 * type conversion:
 * </p>
 * <ul>
 * <li>Conversion to an object of a specific type</li>
 * <li>Conversion to an array of a specific type</li>
 * <li>Conversion to a collection of a specific type</li>
 * </ul>
 * <p>
 * Data type conversion is related to variable substitution (aka interpolation).
 * Before a value can be converted to a target type substitution has to be
 * performed first, and the conversion is done on the resulting value. In order
 * to support this, the conversion methods expect a
 * {@link ConfigurationInterpolator} object; {@code Configuration}
 * implementations here pass in their associated instance.
 * </p>
 * <p>
 * A {@code Configuration} object is associated with a concrete
 * {@code ConversionHandler} implementation. Whenever a data type conversion is
 * required it delegates to this handler. By providing a custom
 * {@code ConversionHandler} object, the type conversion performed by the
 * configuration object can be adapted.
 * </p>
 *
 * @since 2.0
 */
public interface ConversionHandler
{
    /**
     * Converts a single object to the specified target type. A concrete
     * implementation has to attempt a conversion. If this is not possible, a
     * {@link ConversionException} is thrown. It is up to a concrete
     * implementation how <b>null</b> values are handled; a default strategy
     * would be to return <b>null</b> if the source object is <b>null</b>.
     *
     * @param <T> the type of the desired result
     * @param src the object to be converted
     * @param targetCls the target class of the conversion
     * @param ci an object for performing variable substitution
     * @return the converted object
     * @throws ConversionException if the requested conversion is not possible
     */
    <T> T to(Object src, Class<T> targetCls, ConfigurationInterpolator ci);

    /**
     * Converts the given object to an array of the specified element type. The
     * object can be a single value (e.g. a String, a primitive, etc.) or a
     * complex object containing multiple values (like a collection or another
     * array). In the latter case all elements contained in the complex object
     * are converted to the target type. If the value(s) cannot be converted to
     * the desired target class, a {@link ConversionException} is thrown. Note
     * that the result type of this method is {@code Object}; because this
     * method can also produce arrays of a primitive type the return type
     * {@code Object[]} cannot be used.
     *
     * @param src the object to be converted
     * @param elemClass the element class of the resulting array
     * @param ci an object for performing variable substitution
     * @return the array with the converted values
     * @throws ConversionException if the conversion of an element is not
     *         possible
     */
    Object toArray(Object src, Class<?> elemClass, ConfigurationInterpolator ci);

    /**
     * Converts the given object to a collection of the specified type. The
     * target collection must be provided (here callers have the option to
     * specify different types of collections like lists or sets). All values
     * contained in the specified source object (or the source object itself if
     * it is a single value) are converted to the desired target class and added
     * to the destination collection. If the conversion of an element is not
     * possible, a {@link ConversionException} is thrown.
     *
     * @param <T> the type of the elements of the destination collection
     * @param src the object to be converted
     * @param elemClass the element class of the destination collection
     * @param ci an object for performing variable substitution
     * @param dest the destination collection
     */
    <T> void toCollection(Object src, Class<T> elemClass,
            ConfigurationInterpolator ci, Collection<T> dest);
}
