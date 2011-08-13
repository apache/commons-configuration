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
package org.apache.commons.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: rgoers
 * Date: Sep 29, 2009
 * Time: 12:50:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lock
{
    private final String name;
    private final int instanceId;

    private static String counterLock = "Lock";
    private static int counter = 0;

    public Lock(String name)
    {
        this.name = name;
        synchronized(counterLock)
        {
            instanceId = ++counter;
        }
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return "Lock: " + name + " id = " + instanceId + ": " + super.toString();
    }
}
