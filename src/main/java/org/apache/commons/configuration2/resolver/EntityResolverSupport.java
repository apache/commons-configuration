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
package org.apache.commons.configuration2.resolver;

import org.xml.sax.EntityResolver;

/**
 * Interface that identifies the class as using an EntityResolver
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.7
 */
public interface EntityResolverSupport
{
    /**
     * Return the EntityResolver associated with the class.
     * @return The EntityResolver.
     */
    EntityResolver getEntityResolver();

    /**
     * Set the EntityResolver to associate with this class.
     * @param resolver The EntityResolver
     */
    void setEntityResolver(EntityResolver resolver);
}
