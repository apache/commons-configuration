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
package org.apache.commons.configuration2.builder.fluent;

import org.apache.commons.configuration2.builder.BasicBuilderProperties;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration2.builder.PropertiesBuilderProperties;

/**
 * <p>
 * Definition of a parameters interface providing a fluent API for setting all
 * properties for a properties configuration.
 * </p>
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.0
 */
public interface PropertiesBuilderParameters extends
        BasicBuilderProperties<PropertiesBuilderParameters>,
        FileBasedBuilderProperties<PropertiesBuilderParameters>,
        PropertiesBuilderProperties<PropertiesBuilderParameters>,
        BuilderParameters
{
}
