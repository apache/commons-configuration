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
 * This package defines a fluent API for setting up fully configured configuration
 * builders.
 * </p>
 * <p>
 * From a client's point of view the most important class in this package is
 * {@code Parameters}. An instance can be used to create various parameters
 * objects defining the settings for a configuration builder. These objects
 * define {@code set} methods allowing the manipulation of all available
 * initialization properties.
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
package org.apache.commons.configuration2.builder.fluent;
