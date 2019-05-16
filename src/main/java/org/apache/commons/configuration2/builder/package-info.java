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
 * This package contains the implementations of <em>configuration builder</em>
 * classes used to create new {@code Configuration} objects.
 * </p>
 * <p>
 * In <em>Commons Configuration</em>, configuration builders are responsible for
 * the creation and initialization of {@code Configuration} objects. The typical
 * use case is that a builder is created and configured with initialization
 * parameters defining specific settings for the configuration to be created.
 * The builder can then be stored centrally. Each component requiring access to
 * configuration information queries the builder for its managed
 * {@code Configuration} and can read or write properties as its pleasure.
 * </p>
 * <h3>Important note</h3>
 * <p>
 * <strong>This package contains a number of interfaces that reflect the
 * initialization parameters available for supported configuration implementations.
 * These interfaces are not intended to be implemented by client code! When new
 * features are added to the represented configuration classes corresponding new
 * methods will be added to them. This can happen even in minor releases.</strong>
 * </p>
 *
 */
package org.apache.commons.configuration2.builder;
