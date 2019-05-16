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
package org.apache.commons.configuration2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * <p>
 * A specialized {@code InvocationHandler} implementation for supporting
 * immutable configurations.
 * </p>
 * <p>
 * An instance of this class is constructed with a reference to a
 * {@code Configuration} object. All method invocations (which stem from the
 * {@code ImmutableConfiguration} interface) are delegated to this object. That
 * way all functionality is actually backed by the underlying
 * {@code Configuration} implementation, but because the associated proxy only
 * implements the {@code ImmutableConfiguration} interface manipulations are not
 * possible.
 * </p>
 * <p>
 * There is one caveat however: Some methods of the
 * {@code ImmutableConfiguration} interface return an {@code Iterator} object.
 * Using the iterator's {@code remove()} method it may be possible to remove
 * keys from the underlying {@code Configuration} object. Therefore, in these
 * cases a specialized {@code Iterator} is returned which does not support the
 * remove operation.
 * </p>
 *
 * @since 2.0
 */
class ImmutableConfigurationInvocationHandler implements InvocationHandler
{
    /** The underlying configuration object. */
    private final Configuration wrappedConfiguration;

    /**
     * Creates a new instance of {@code ImmutableConfigurationInvocationHandler}
     * and initializes it with the wrapped configuration object.
     *
     * @param conf the wrapped {@code Configuration} (must not be <b>null</b>)
     * @throws NullPointerException if the {@code Configuration} is <b>null</b>
     */
    public ImmutableConfigurationInvocationHandler(final Configuration conf)
    {
        if (conf == null)
        {
            throw new NullPointerException(
                    "Wrapped configuration must not be null!");
        }
        wrappedConfiguration = conf;
    }

    /**
     * {@inheritDoc} This implementation delegates to the wrapped configuration
     * object. Result objects are wrapped if necessary.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable
    {
        try
        {
            return handleResult(method.invoke(wrappedConfiguration, args));
        }
        catch (final InvocationTargetException e)
        {
            // unwrap
            throw e.getCause();
        }
    }

    /**
     * Handles the result from the method invocation on the wrapped
     * configuration. This implementation wraps result objects if necessary so
     * that the underlying configuration cannot be manipulated.
     *
     * @param result the result object
     * @return the processed result object
     */
    private static Object handleResult(final Object result)
    {
        if (result instanceof Iterator)
        {
            return new ImmutableIterator((Iterator<?>) result);
        }
        return result;
    }

    /**
     * A specialized {@code Iterator} implementation which delegates to an
     * underlying iterator, but does not support the {@code remove()} method.
     */
    private static class ImmutableIterator implements Iterator<Object>
    {
        /** The underlying iterator. */
        private final Iterator<?> wrappedIterator;

        /**
         * Creates a new instance of {@code ImmutableIterator} and sets the
         * underlying iterator.
         *
         * @param it the underlying iterator
         */
        public ImmutableIterator(final Iterator<?> it)
        {
            wrappedIterator = it;
        }

        /**
         * {@inheritDoc} This implementation just delegates to the underlying
         * iterator.
         */
        @Override
        public boolean hasNext()
        {
            return wrappedIterator.hasNext();
        }

        /**
         * {@inheritDoc} This implementation just delegates to the underlying
         * iterator.
         */
        @Override
        public Object next()
        {
            return wrappedIterator.next();
        }

        /**
         * {@inheritDoc} This implementation just throws an exception: removing
         * objects is not supported.
         */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(
                    "remove() operation not supported!");
        }
    }
}
