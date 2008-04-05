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
     * Constant for the property change event. This event is triggered by
     * <code>setPropertyValue()</code> and <code>clearPropertyValue()</code>,
     * which manipulate a value of a property with multiple values.
     */
    public static final int EVENT_PROPERTY_CHANGED = 9;
    
    /**
     * Modifies a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used to alter a specific
     * value (identified by its 0-based index) without affecting the other
     * values. If the index is invalid (i.e. less than 0 or greater than the
     * number of existing values), the value will be added to the existing
     * values of this property. This method takes care of firing the appropriate
     * events and delegates to <code>setPropertyValueDirect()</code>. It generates
     * a <code>EVENT_PROPERTY_CHANGED</code> event that contains the key of the
     * affected property.
     *
     * @param key the key of the property
     * @param index the index of the value to change
     * @param value the new value; this should be a simple object; arrays or
     *        collections won't be treated specially, but directly added
     */
    public void setPropertyValue(String key, int index, Object value)
    {
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, true);
        setPropertyValueDirect(key, index, value);
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, false);
    }

    /**
     * Removes a specific value of a property with multiple values. If a
     * property has multiple values, this method can be used for removing a
     * single value (identified by its 0-based index). If the index is out of
     * range, no action is performed; in this case <b>false</b> is returned.
     * This method takes care of firing the appropriate
     * events and delegates to <code>clearPropertyValueDirect()</code>. It generates
     * a <code>EVENT_PROPERTY_CHANGED</code> event that contains the key of the
     * affected property.
     *
     * @param key the key of the property
     * @param index the index of the value to delete
     * @return a flag whether the value could be removed
     */
    public boolean clearPropertyValue(String key, int index)
    {
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, true);
        boolean result = clearPropertyValueDirect(key, index);
        fireEvent(EVENT_PROPERTY_CHANGED, key, null, false);
        return result;
    }
    
    /**
     * Performs the actual modification of the specified property value. This
     * method is called by <code>setPropertyValue()</code>.
     * @param key the key of the property
     * @param index the index of the value to change
     * @param value the new value
     */
    protected abstract void setPropertyValueDirect(String key, int index, Object value);
    
    /**
     * Performs the actual remove property value operation. This method is called
     * by <code>clearPropertyValue()</code>.
     * @param key the key of the property
     * @param index the index of the value to delete
     * @return a flag whether the value could be removed
     */
    protected abstract boolean clearPropertyValueDirect(String key, int index);
}
