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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>
 * A special implementation of the {@code BeanDeclaration} interface which allows combining multiple
 * {@code BeanDeclaration} objects.
 * </p>
 * <p>
 * An instance of this class can be used if a bean is defined using multiple sources. For instance, there can be one
 * definition with default values and one with actual values; if actual values are provided, they are used; otherwise,
 * the default values apply.
 * </p>
 * <p>
 * When constructing an instance an arbitrary number of child {@code BeanDeclaration} objects can be specified. The
 * implementations of the {@code BeanDeclaration} methods implement a logical combination of the data returned by these
 * child declarations. The order in which child declarations are added is relevant; first entries take precedence over
 * later ones. The comments of the single methods explain in which way a combination of the child declarations is built.
 * </p>
 *
 * @since 2.0
 */
public class CombinedBeanDeclaration implements BeanDeclaration {

    /** A list with the child declarations. */
    private final ArrayList<BeanDeclaration> childDeclarations;

    /**
     * Constructs a new instance of {@code CombinedBeanDeclaration} and initializes it with the given child declarations.
     *
     * @param decl the child declarations
     * @throws NullPointerException if the array with child declarations is <b>null</b>
     */
    public CombinedBeanDeclaration(final BeanDeclaration... decl) {
        childDeclarations = new ArrayList<>(Arrays.asList(decl));
    }

    /**
     * {@inheritDoc} This implementation iterates over the list of child declarations and asks them for a bean factory name.
     * The first non-<b>null</b> value is returned. If none of the child declarations have a defined bean factory name,
     * result is <b>null</b>.
     */
    @Override
    public String getBeanFactoryName() {
        return findFirst(BeanDeclaration::getBeanFactoryName);
    }

    private <T> T findFirst(final Function<? super BeanDeclaration, ? extends T> mapper) {
        return childDeclarations.stream().map(mapper).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * {@inheritDoc} This implementation iterates over the list of child declarations and asks them for a bean factory
     * parameter. The first non-<b>null</b> value is returned. If none of the child declarations have a defined bean factory
     * parameter, result is <b>null</b>.
     */
    @Override
    public Object getBeanFactoryParameter() {
        return findFirst(BeanDeclaration::getBeanFactoryParameter);
    }

    /**
     * {@inheritDoc} This implementation iterates over the list of child declarations and asks them for the bean class name.
     * The first non-<b>null</b> value is returned. If none of the child declarations have a defined bean class, result is
     * <b>null</b>.
     */
    @Override
    public String getBeanClassName() {
        return findFirst(BeanDeclaration::getBeanClassName);
    }

    /**
     * {@inheritDoc} This implementation creates a union of the properties returned by all child declarations. If a property
     * is defined in multiple child declarations, the declaration that comes before in the list of children takes
     * precedence.
     */
    @Override
    public Map<String, Object> getBeanProperties() {
        return get(BeanDeclaration::getBeanProperties);
    }

    private Map<String, Object> get(final Function<? super BeanDeclaration, ? extends Map<String, Object>> mapper) {
        @SuppressWarnings("unchecked")
        final ArrayList<BeanDeclaration> temp = (ArrayList<BeanDeclaration>) childDeclarations.clone();
        Collections.reverse(temp);
        return temp.stream().map(mapper).filter(Objects::nonNull).collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }

    /**
     * {@inheritDoc} This implementation creates a union of the nested bean declarations returned by all child declarations.
     * If a complex property is defined in multiple child declarations, the declaration that comes before in the list of
     * children takes precedence.
     */
    @Override
    public Map<String, Object> getNestedBeanDeclarations() {
        return get(BeanDeclaration::getNestedBeanDeclarations);
    }

    /**
     * {@inheritDoc} This implementation iterates over the list of child declarations and asks them for constructor
     * arguments. The first non-<b>null</b> and non empty collection is returned. If none of the child declarations provide
     * constructor arguments, result is an empty collection.
     */
    @Override
    public Collection<ConstructorArg> getConstructorArgs() {
        for (final BeanDeclaration d : childDeclarations) {
            final Collection<ConstructorArg> args = d.getConstructorArgs();
            if (args != null && !args.isEmpty()) {
                return args;
            }
        }
        return Collections.emptyList();
    }
}
