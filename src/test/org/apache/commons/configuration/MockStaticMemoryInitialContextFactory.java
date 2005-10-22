/*
 * Copyright (C) The Spice Group. All rights reserved.
 *
 * This software is published under the terms of the Spice
 * Software License version 1.1, a copy of which has been included
 * with this distribution in the LICENSE.txt file.
 */

package org.apache.commons.configuration;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.codehaus.spice.jndikit.DefaultNameParser;
import org.codehaus.spice.jndikit.DefaultNamespace;
import org.codehaus.spice.jndikit.memory.MemoryContext;

/**
 * Initial context factory for memorycontext. This factory will
 * retrieve the {@link MemoryContext} from a static variable.
 * Thus this factory will always return the same instance of
 * memory context.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision$
 */
public class MockStaticMemoryInitialContextFactory
    implements InitialContextFactory
{
    private static final MemoryContext MEMORY_CONTEXT = createMemoryContext();

    public Context getInitialContext(final Hashtable environment)
        throws NamingException
    {
        return MEMORY_CONTEXT;
    }

    /**
     * Method to create the inital {@link MemoryContext}.
     *
     * @return the new {@link MemoryContext}.
     */
    private static final MemoryContext createMemoryContext()
    {
        final DefaultNamespace namespace =
            new DefaultNamespace(new DefaultNameParser());
        MemoryContext me = new MemoryContext(namespace, new Hashtable(), null);
        
        try
        {
            Context testContext = me.createSubcontext("test");
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
        }
        catch (NamingException ne)
        {
            throw new RuntimeException(ne.getMessage());
        }
        return me;
    }
}
