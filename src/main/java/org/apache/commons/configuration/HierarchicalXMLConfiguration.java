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

package org.apache.commons.configuration;

/**
 * A specialized hierarchical configuration class that is able to parse XML
 * documents.
 *
 * <p>The parsed document will be stored keeping its structure. The contained
 * properties can be accessed using all methods supported by the base class
 * {@code HierarchicalConfiguration}.
 *
 * @since commons-configuration 1.0
 *
 * @author J&ouml;rg Schaible
 * @version $Id$
 * @deprecated This class is deprecated. Use {@code XMLConfiguration}
 * instead, which supports all features this class had offered before.
 */
@Deprecated
public class HierarchicalXMLConfiguration extends XMLConfiguration
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5597530014798917521L;
}
