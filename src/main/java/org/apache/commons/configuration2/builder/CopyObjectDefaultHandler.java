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
package org.apache.commons.configuration2.builder;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

/**
 * <p>
 * A specialized implementation of {@code DefaultParametersHandler} that copies
 * the properties of a {@link BuilderParameters} object (passed at construction
 * time) onto the object to be initialized.
 * </p>
 * <p>
 * Using this handler implementation makes specifying default values pretty
 * straight-forward: Just create a corresponding parameters object, initialize
 * it as desired, and pass it to this class. When invoked the handler uses
 * functionality from <em>Commons BeanUtils</em> to copy all properties defined
 * in the associated parameters object onto the target object. This is based on
 * reflection. Properties not available for the target object are silently
 * ignored. If an exception occurs during the copy operation, it is re-thrown as
 * a runtime exception.
 * </p>
 * <p>
 * Note that there is no default way to create a defensive copy of the passed in
 * parameters object; therefore, the reference is stored. This makes it possible
 * to change the parameters object later on, and the changes will be effective
 * when initializing objects afterwards. Client code should not rely on this
 * feature.
 * </p>
 *
 * @since 2.0
 */
public class CopyObjectDefaultHandler implements
        DefaultParametersHandler<Object>
{
    /** The source object with the properties to be initialized. */
    private final BuilderParameters source;

    /**
     * Creates a new instance of {@code CopyObjectDefaultHandler} and
     * initializes it with the specified source object. The properties defined
     * by the source object are copied onto the objects to be initialized.
     *
     * @param src the source object (must not be <b>null</b>)
     * @throws IllegalArgumentException if the source object is <b>null</b>
     */
    public CopyObjectDefaultHandler(final BuilderParameters src)
    {
        if (src == null)
        {
            throw new IllegalArgumentException(
                    "Source object must not be null!");
        }
        source = src;
    }

    /**
     * Returns the source object of this handler. This is the object whose
     * properties are copied on the objects to be initialized.
     *
     * @return the source object of this {@code CopyObjectDefaultHandler}
     */
    public BuilderParameters getSource()
    {
        return source;
    }

    /**
     * {@inheritDoc} This implementation uses
     * {@code PropertyUtils.copyProperties()} to copy all defined properties
     * from the source object onto the passed in parameters object. Both the map
     * with properties (obtained via the {@code getParameters()} method of the
     * source parameters object) and other properties of the source object are
     * copied.
     *
     * @throws ConfigurationRuntimeException if an exception occurs
     * @see BuilderParameters#getParameters()
     */
    @Override
    public void initializeDefaults(final Object parameters)
    {
        try
        {
            BeanHelper.copyProperties(parameters, getSource()
                    .getParameters());
            BeanHelper.copyProperties(parameters, getSource());
        }
        catch (final Exception e)
        {
            // Handle all reflection-related exceptions the same way
            throw new ConfigurationRuntimeException(e);
        }
    }
}
