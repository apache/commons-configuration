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
package org.apache.commons.configuration2.beanutils;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * Definition of an interface for declaring a bean in a configuration file.
 * </p>
 * <p>
 * Commons Configurations allows to define beans (i.e. simple Java objects) in
 * configuration files, which can be created at runtime. This is especially
 * useful if you program against interfaces and want to define the concrete
 * implementation class is a configuration file.
 * </p>
 * <p>
 * This interface defines methods for retrieving all information about a bean
 * that should be created from a configuration file, e.g. the bean's properties
 * or the factory to use for creating the instance. With different
 * implementations different &quot;layouts&quot; of bean declarations can be
 * supported. For instance if an XML configuration file is used, all features of
 * XML (e.g. attributes, nested elements) can be used to define the bean. In a
 * properties file the declaration format is more limited. The purpose of this
 * interface is to abstract from the concrete declaration format.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public interface BeanDeclaration
{
    /**
     * Returns the name of the {@code BeanFactory} that should be used
     * for creating the bean instance. This can be <b>null</b>, then a default
     * factory will be used.
     *
     * @return the name of the bean factory
     */
    String getBeanFactoryName();

    /**
     * Here an arbitrary object can be returned that will be passed to the bean
     * factory. Its meaning is not further specified. The purpose of this
     * additional parameter is to support a further configuration of the bean
     * factory that can be placed directly at the bean declaration.
     *
     * @return a parameter for the bean factory
     */
    Object getBeanFactoryParameter();

    /**
     * Returns the name of the bean class, from which an instance is to be
     * created. This value must be defined unless a default class is provided
     * for the bean creation operation.
     *
     * @return the name of the bean class
     */
    String getBeanClassName();

    /**
     * Returns a map with properties that should be initialized on the newly
     * created bean. The map's keys are the names of the properties; the
     * corresponding values are the properties' values. The return value can be
     * <b>null</b> if no properties should be set.
     *
     * @return a map with properties to be initialized
     */
    Map<String, Object> getBeanProperties();

    /**
     * Returns a map with declarations for beans that should be set as
     * properties of the newly created bean. This allows for complex
     * initialization scenarios: a bean for a bean that contains complex
     * properties (e.g. other beans) can have nested declarations for defining
     * these complex properties. The returned map's key are the names of the
     * properties to initialize. The values are either {@code BeanDeclaration}
     * implementations or collections thereof. They will be treated like this
     * declaration (in a recursive manner), and the resulting beans are
     * assigned to the corresponding properties.
     *
     * @return a map with nested bean declarations
     */
    Map<String, Object> getNestedBeanDeclarations();

    /**
     * Returns a collection with constructor arguments. This data is used to
     * determine the constructor of the bean class to be invoked. The values of
     * the arguments are passed to the constructor. An implementation can return
     * <b>null</b> or an empty collection; then the standard constructor of the
     * bean class is called.
     *
     * @return a collection with the arguments to be passed to the bean class's
     *         constructor
     */
    Collection<ConstructorArg> getConstructorArgs();
}
