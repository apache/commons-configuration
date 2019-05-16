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

/**
 * <p>
 * A class representing an argument for a constructor invocation to be used by a
 * {@link BeanDeclaration}.
 * </p>
 * <p>
 * A {@code BeanDeclaration} can provide a list of instances of this class to
 * define the constructor to be invoked on the bean class. Each constructor
 * argument can either be a simple value or a nested {@code BeanDeclaration}. In
 * the latter case, the bean is resolved recursively.
 * </p>
 * <p>
 * The constructor to be invoked on the bean class has to be determined based on
 * the types of the constructor arguments. To avoid ambiguity, the type name can
 * be explicitly provided.
 * </p>
 *
 * @since 2.0
 */
public final class ConstructorArg
{
    /** The bean declaration referenced by this constructor argument. */
    private final BeanDeclaration beanDeclaration;

    /** The value of this constructor argument. */
    private final Object value;

    /** The name of the argument type. */
    private final String typeName;

    /**
     * Creates a new instance of {@code ConstructorArg}.
     *
     * @param decl the associated bean declaration
     * @param val the value of the argument
     * @param type the type name
     */
    private ConstructorArg(final BeanDeclaration decl, final Object val, final String type)
    {
        beanDeclaration = decl;
        value = val;
        typeName = type;
    }

    /**
     * Creates a new instance of {@code ConstructorArg} for the specified
     * {@code BeanDeclaration}. The actual value of this argument is the
     * resolved {@code BeanDeclaration}.
     *
     * @param decl the {@code BeanDeclaration}
     * @return the newly created instance of this class
     * @throws NullPointerException if the {@code BeanDeclaration} is
     *         <b>null</b>
     */
    public static ConstructorArg forBeanDeclaration(final BeanDeclaration decl)
    {
        return forBeanDeclaration(decl, null);
    }

    /**
     * Creates a new instance of {@code ConstructorArg} for the specified
     * {@code BeanDeclaration} and sets the type name explicitly. The type name
     * is used to match this argument against the parameter type of a
     * constructor or the bean class.
     *
     * @param decl the {@code BeanDeclaration}
     * @param typeName the name of the data type of this argument
     * @return the newly created instance of this class
     * @throws NullPointerException if the {@code BeanDeclaration} is
     *         <b>null</b>
     */
    public static ConstructorArg forBeanDeclaration(final BeanDeclaration decl,
            final String typeName)
    {
        if (decl == null)
        {
            throw new NullPointerException("BeanDeclaration must not be null!");
        }
        return new ConstructorArg(decl, null, typeName);
    }

    /**
     * Creates a new instance of {@code ConstructorArg} for the specified simple
     * value. The value is passed to the constructor invocation.
     *
     * @param value the value of this constructor argument (may be <b>null</b>)
     * @return the newly created instance of this class
     */
    public static ConstructorArg forValue(final Object value)
    {
        return forValue(value, null);
    }

    /**
     * Creates a new instance of {@code ConstructorArg} for the specified simple
     * value and sets the type name explicitly. The type name is used to match
     * this argument against the parameter type of a constructor or the bean
     * class.
     *
     * @param value the value of this constructor argument (may be <b>null</b>)
     * @param typeName the name of the data type of this argument
     * @return the newly created instance of this class
     */
    public static ConstructorArg forValue(final Object value, final String typeName)
    {
        return new ConstructorArg(null, value, typeName);
    }

    /**
     * Returns the {@code BeanDeclaration} referenced by this constructor
     * argument. A return value of <b>null</b> means that this constructor
     * argument does not have a bean declaration as value; in this case, the
     * value can be queried using the {@link #getValue()} method.
     *
     * @return the referenced {@code BeanDeclaration} or <b>null</b>
     */
    public BeanDeclaration getBeanDeclaration()
    {
        return beanDeclaration;
    }

    /**
     * Returns a flag whether this constructor argument represents a
     * {@code BeanDeclaration}. If this method returns <b>true</b>, the actual
     * value of this argument can be obtained by resolving the bean declaration
     * returned by {@link #getBeanDeclaration()}. Otherwise, this argument has a
     * simple value which can be queried using {@link #getValue()}.
     *
     * @return a flag whether this constructor argument references a bean
     *         declaration
     */
    public boolean isNestedBeanDeclaration()
    {
        return getBeanDeclaration() != null;
    }

    /**
     * Returns the value of this constructor argument. This method can be
     * queried if {@link #isNestedBeanDeclaration()} returns <b>false</b>. Note
     * that a return value of <b>null</b> is legal (to pass <b>null</b> to a
     * constructor argument).
     *
     * @return the simple value of this constructor argument
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Returns the optional data type name of this constructor argument. The
     * type name can be specified as a hint to select a specific constructor if
     * there are ambiguities. Note that it does not necessarily has to match the
     * data type of this argument's value because a type conversion may be
     * performed before invoking the constructor.
     *
     * @return the data type name of this argument if defined or <b>null</b>
     *         otherwise
     */
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * Checks whether this constructor argument is compatible with the given
     * class. This method is called to determine a matching constructor. It
     * compares the argument's data type with the class name if it is defined.
     * If no type name has been set, result is <b>true</b> as it is assumed that
     * a type conversion can be performed when calling the constructor. This
     * means that per default only the number of constructor arguments is
     * checked to find a matching constructor. Only if there are multiple
     * constructors with the same number of arguments, explicit type names have
     * to be provided to select a specific constructor.
     *
     * @param argCls the class of the constructor argument to compare with
     * @return <b>true</b> if this constructor argument is compatible with this
     *         class, <b>false</b> otherwise
     */
    public boolean matches(final Class<?> argCls)
    {
        if (argCls == null)
        {
            return false;
        }

        return getTypeName() == null || getTypeName().equals(argCls.getName());
    }

    /**
     * Returns a string representation of this object. This string contains the
     * value of this constructor argument and the explicit type if provided.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append(" [ value = ");
        buf.append(isNestedBeanDeclaration() ? getBeanDeclaration()
                : getValue());
        if (getTypeName() != null)
        {
            buf.append(" (").append(getTypeName()).append(')');
        }
        buf.append(" ]");
        return buf.toString();
    }
}
