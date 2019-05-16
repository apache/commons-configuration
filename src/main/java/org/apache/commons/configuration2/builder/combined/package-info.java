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

/**
 * <p>
 * A package containing the implementation of the builder for combined
 * configurations.
 * </p>
 * <p>
 * The {@code CombinedConfigurationBuilder} class defined in this package can
 * create a {@code CombinedConfiguration} object from various configuration
 * sources that are declared in a so-called <em>configuration definition
 * file</em>. This is a convenient means to collect distributed configuration
 * information and access them as a single logic source.
 * </p>
 * <p>
 * In addition, the {@code MultiFileConfigurationBuilder} class is located in
 * this package. This builder class selects one file-based configuration out of
 * a set based on dynamic variable substitution. A typical use case would be
 * the definition of multiple configuration files for the different stages of a
 * project: development, integration test, production, etc.
 * </p>
 *
 */
package org.apache.commons.configuration2.builder.combined;
