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
package org.apache.commons.configuration2.flat;

import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * <p>
 * A base class for <code>Configuration</code> implementations that are not
 * hierarchical.
 * </p>
 * <p>
 * Not all configuration classes store their data in a hierarchical manner. This
 * class can serve as a base class for those non-hierarchical configurations. It
 * does not make any assumptions about the concrete way of storing data. This is
 * up to a concrete sub class, which has to implement the access methods like
 * <code>getProperty()</code>, <code>setProperty()</code>, or
 * <code>addPropertyDirect()</code> accordingly.
 * </p>
 * <p>
 * What this class does is to implement a pseudo hierarchical structure on top
 * of the data contained in the configuration. Whenever the root node of this
 * configuration is requested, a hierarchy of nodes corresponding to the actual
 * data is constructed on the fly. This node structure can then be used for
 * performing queries or integrating this configuration into a combined
 * configuration.
 * </p>
 * <p>
 * Internally, this class uses some tricks to keep the node structure up to date
 * and to avoid unnecessary operations on these nodes: It registers itself as a
 * configuration listener at itself to get notified whenever the configuration
 * is changed. When a change is detected, the node structure is invalidated, so
 * that it has to be re-created when it is accessed the next time. On the other
 * hand, this node structure is only created when it is needed, i.e. if truly
 * hierarchical operations are to be performed. This means that as long as the
 * configuration is only accessed and manipulated through the typical access
 * methods, no additional overhead is created.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public abstract class AbstractFlatConfiguration extends AbstractConfiguration
{
    /**
     * Modifies a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used to alter a specific
     * value (identified by its 0-based index) without affecting the other
     * values. If the index is invalid (i.e. less than 0 or greater than the
     * number of existing values), the value will be added to the existing
     * values of this property.
     *
     * @param key the key of the property
     * @param index the index of the value to change
     * @param value the new value; this should be a simple object; arrays or
     *        collections won't be treated specially, but directly added
     */
    public abstract void setPropertyValue(String key, int index, Object value);

    /**
     * Removes a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used for removing a
     * single value (identified by its 0-based index). If the index is out of
     * range, no action is performed; in this case <b>false</b> is returned.
     *
     * @param key the key of the property
     * @param index the index of the value to delete
     * @return a flag whether the value could be removed
     */
    public abstract boolean clearPropertyValue(String key, int index);
}
