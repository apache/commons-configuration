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
package org.apache.commons.configuration2.interpol;

/**
 * <p>
 * Definition of an interface for looking up variables during interpolation.
 * </p>
 * <p>
 * Objects implementing this interface can be assigned a variable prefix and
 * added to a {@link ConfigurationInterpolator} object. Whenever the
 * {@code ConfigurationInterpolator} encounters a property value referencing a
 * variable, e.g. <code>${prefix:variableName}</code>, it extracts the prefix
 * and finds the matching {@code Lookup} object. Then this object is asked to
 * resolve the variable name and provide the corresponding value.
 * </p>
 * <p>
 * This interface defines a single method for performing variable lookup. It is
 * passed the name of a variable and has to return the corresponding value. It
 * is of course up to a specific implementation how this is done. If the
 * variable name cannot be resolved, an implementation has to return
 * <b>null</b>.
 * </p>
 * <p>
 * Note: Implementations must be aware that they can be accessed concurrently.
 * This is for instance the case if a configuration object is read by multiple
 * threads or if a {@code Lookup} object is shared by multiple configurations.
 * </p>
 *
 * @since 2.0
 */
public interface Lookup
{
    /**
     * Looks up the value of the specified variable. This method is called by
     * {@link ConfigurationInterpolator} with the variable name extracted from
     * the expression to interpolate (i.e. the prefix name has already been
     * removed). A concrete implementation has to return the value of this
     * variable or <b>null</b> if the variable name is unknown.
     *
     * @param variable the name of the variable to be resolved
     * @return the value of this variable or <b>null</b>
     */
    Object lookup(String variable);
}
