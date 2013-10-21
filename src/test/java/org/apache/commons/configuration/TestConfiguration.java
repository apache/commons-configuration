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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class TestConfiguration
{
    @Test
    public void testConfigurationGetList()
    {
        final List<String> defaults = new ArrayList<String>();

        String key = UUID.randomUUID().toString();
        for (int i = 0; i < 10; i++) {
            defaults.add(UUID.randomUUID().toString());
        }

        final Configuration c = new MapConfiguration(Collections.<String, String>emptyMap());

        final List<Object> values = c.getList(key, defaults);

        Assert.assertEquals(defaults, values);
    }
}
