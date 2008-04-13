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

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.codehaus.spice.jndikit.DefaultNameParser;
import org.codehaus.spice.jndikit.DefaultNamespace;
import org.codehaus.spice.jndikit.memory.MemoryContext;

/**
 * A mock implementation of the <code>InitialContextFactory</code> interface.
 * This implementation will return a mock context that contains some test data.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 */
public class MockInitialContextFactory implements InitialContextFactory
{
    /**
     * Constant for the use cycles environment property. If this property is
     * present in the environment, a cyclic context will be created.
     */
    public static final String PROP_CYCLES = "useCycles";

    public Context getInitialContext(Hashtable env) throws NamingException
    {
        DefaultNamespace namespace = new DefaultNamespace(new DefaultNameParser());
        MemoryContext context = new MemoryContext(namespace, new Hashtable(), null);

        Context testContext = context.createSubcontext("test");
        testContext.bind("key", "jndivalue");
        testContext.bind("key2","jndivalue2");
        testContext.bind("short","1");
        testContext.bind("boolean","true");
        testContext.bind("byte","10");
        testContext.bind("double","10.25");
        testContext.bind("float","20.25");
        testContext.bind("integer","10");
        testContext.bind("long","1000000");
        testContext.bind("onlyinjndi","true");

        if (env.containsKey(PROP_CYCLES))
        {
            Context cycleContext = context.createSubcontext("cycle");
            cycleContext.bind("cycle", cycleContext);
        }

        return context;
    }
}
