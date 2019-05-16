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
import java.util.List;

/**
 * A simple bean class used for testing bean creation operations that has
 * a list of children of a different bean type.
 *
 */
public class BeanCreationTestBeanWithListChild
{
    private String stringValue;

    private int intValue;

    private final List<BeanCreationTestBean> children = new ArrayList<>();

    public List<BeanCreationTestBean> getChildren()
    {
        return children;
    }

    public void setChildren(final List<BeanCreationTestBean> buddies)
    {
        this.children.clear();
        this.children.addAll(buddies);
    }

    public int getIntValue()
    {
        return intValue;
    }

    public void setIntValue(final int intValue)
    {
        this.intValue = intValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(final String stringValue)
    {
        this.stringValue = stringValue;
    }
}
