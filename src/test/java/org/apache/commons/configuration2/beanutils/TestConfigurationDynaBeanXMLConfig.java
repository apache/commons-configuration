/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.beanutils;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.XMLConfiguration;

/**
 * An additional test class for ConfigurationDynaBean. This test class performs
 * the same tests as the default test class, but uses a XMLConfiguration as
 * underlying configuration object.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class TestConfigurationDynaBeanXMLConfig extends
        TestConfigurationDynaBean
{
    /**
     * Creates the underlying configuration object. This implementation will
     * create a XMLConfiguration.
     * @return the underlying configuration
     */
    @Override
    protected Configuration createConfiguration()
    {
        return new XMLConfiguration();
    }
}
