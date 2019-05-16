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
 * This package contains interfaces and classes related to data type conversions.
 * </p>
 * <p>
 * An <code>AbstractConfiguration</code> object is associated with an object
 * responsible for data type conversions. Each conversion is done by this object.
 * By replacing the default conversion handler by a custom version, client
 * applications can adapt and extend the type conversions available.
 * </p>
 * <p>
 * Related to data type conversion is also the topic of list delimiter parsing
 * and splitting; before a string value can be converted to a target data type,
 * it must be clear whether it is to be interpreted as a single value or as a
 * list containing multiple values. In this package there are classes implementing
 * different strategies for list delimiter handling. Client code can choose the
 * one which is most suitable for the current use case.
 * </p>
 *
 */
package org.apache.commons.configuration2.convert;
