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

package org.apache.commons.configuration2.convert;

import java.util.Objects;

/**
 * A custom {@link Number}.
 */
public final class MyNumber extends Number {

    private static final long serialVersionUID = 1L;

    private final long value;

    public MyNumber() {
        this("0");
    }

    public MyNumber(final long value) {
        this.value = value;
    }

    public MyNumber(final String string) {
        value = string != null ? Long.parseLong(string) : 0;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MyNumber)) {
            return false;
        }
        final MyNumber other = (MyNumber) obj;
        return value == other.value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
