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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.configuration2.expr.AbstractNodeHandler;
import org.apache.commons.logging.LogFactory;

/**
 * This Configuration class allows you to interface with a JNDI datasource.
 * Unlike other configurations it's not possible to set a property to a path
 * that's the prefix of another property. For example the following properties
 * couldn't be stored simultaneously in a JNDIConfiguration:
 *
 * <pre>
 * test.foo = value1
 * test.foo.bar = value2
 * </pre>
 *
 * <p>In this case setting the <tt>test.foo.bar</tt> property will overwrite
 * <tt>test.foo</tt>, and reciprocally.</p>
 *
 * <p>A maximum depth is assigned to the configuration, it is set to 20
 * by default. Since JNDI directories can have cyclic paths, this depth
 * prevents infinite loops when searching through the tree.</p>
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Id$
 */
public class JNDIConfiguration extends AbstractHierarchicalConfiguration<JNDIConfiguration.JNDINode>
{
    /** The root node of the configuration. */
    private JNDINode root;

    /** The maximum depth for fetching the child nodes in the JNDI tree. */
    private int maxDepth = 20;

    /**
     * Creates a JNDIConfiguration using the default initial context as the
     * root of the properties.
     *
     * @throws NamingException thrown if an error occurs when initializing the default context
     */
    public JNDIConfiguration() throws NamingException
    {
        this("");
    }

    /**
     * Creates a JNDIConfiguration using the default initial context, shifted
     * with the specified prefix, as the root of the properties.
     *
     * @param prefix the prefix
     *
     * @throws NamingException thrown if an error occurs when initializing the default context
     */
    public JNDIConfiguration(String prefix) throws NamingException
    {
        this(new InitialContext(), prefix);
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context as the
     * root of the properties.
     *
     * @param context the initial context
     */
    public JNDIConfiguration(Context context)
    {
        this(context, "");
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context shifted
     * by the specified prefix as the root of the properties.
     *
     * @param context the initial context
     * @param prefix the prefix
     */
    public JNDIConfiguration(Context context, String prefix)
    {
        super(new JNDINodeHandler());
        ((JNDINodeHandler) getNodeHandler()).setConfiguration(this);

        root = new JNDINode(context, prefix, 0);

        setLogger(LogFactory.getLog(getClass().getName()));
        addErrorLogListener();
    }

    @Override
    public JNDINode getRootNode()
    {
        return root;
    }

    /**
     * Returns the maximum depth for searching in the JNDI tree.
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }

    /**
     * Sets the maximum depth for searching in the JNDI tree.
     *
     * @param maxDepth the maximum depth
     */
    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    /**
     * Returns the prefix.
     *
     * @return the prefix
     */
    public String getPrefix()
    {
        return root.name;
    }

    /**
     * Sets the prefix.
     *
     * @param prefix The prefix to set
     */
    public void setPrefix(String prefix)
    {
        root = new JNDINode(root.context, prefix, 0);
    }

    /**
     * Return the base context with the prefix applied.
     *
     * @return the base context
     * @throws NamingException if an error occurs
     */
    public Context getBaseContext() throws NamingException
    {
        return (Context) root.context.lookup(root.name);
    }

    /**
     * Return the initial context used by this configuration. This context is
     * independent of the prefix specified.
     *
     * @return the initial context
     */
    public Context getContext()
    {
        return root.context;
    }

    /**
     * Set the initial context of the configuration.
     *
     * @param context the context
     */
    public void setContext(Context context)
    {
        root = new JNDINode(context, root.name, 0);
    }

    /**
     * Node of a JNDI directory. A node consists in a base context and a name
     * of a property bound. An empty name refers to the context itself.
     */
    static class JNDINode {
        private Context context;
        private String name;
        private int depth;

        private JNDINode(Context context, String name, int depth)
        {
            this.context = context;
            this.name = name;
            this.depth = depth;
        }

        public Object getValue() throws NamingException
        {
            try
            {
                return context.lookup(name);
            }
            catch (NameNotFoundException e)
            {
                return null;
            }
        }
    }

    /**
     * Implementation of NodeHandler that operates on JNDI trees.
     */
    private static class JNDINodeHandler extends AbstractNodeHandler<JNDINode>
    {
        private JNDIConfiguration config;

        public void setConfiguration(JNDIConfiguration config)
        {
            this.config = config;
        }

        @Override
        public boolean hasAttributes(JNDINode node)
        {
            return false;
        }

        public String nodeName(JNDINode node)
        {
            return node.name;
        }

        public Object getValue(JNDINode node)
        {
            try
            {
                Object value = node.getValue();
                if (value instanceof Context)
                {
                    // contexts have no direct value bound
                    return null;
                }
                else
                {
                    return value;
                }
            }
            catch (NamingException e)
            {
                throw new ConfigurationRuntimeException("Unable to get the value of the JNDI node", e);
            }
        }

        public void setValue(JNDINode node, Object value)
        {
            try
            {
                if (value == null)
                {
                    node.context.unbind(node.name);
                }
                else
                {
                    node.context.rebind(node.name, value);
                }
            }
            catch (NamingException e)
            {
                throw new ConfigurationRuntimeException("Unable to set the value of the JNDI node", e);
            }
        }

        public JNDINode getParent(JNDINode node)
        {
            return null;  // todo
        }

        public JNDINode addChild(JNDINode node, String name)
        {
            try
            {
                Object value = node.getValue();

                if (!(value instanceof Context))
                {
                    // overwrite the existing property at this path
                    node.context.unbind(node.name);

                    value = node.context.createSubcontext(node.name);
                }

                Context context = (Context) value;
                context.createSubcontext(name);

                return new JNDINode(context, name, node.depth + 1);
            }
            catch (NamingException e)
            {
                throw new ConfigurationRuntimeException("Unable to add the child node '" + name + "'", e);
            }
        }

        public List<JNDINode> getChildren(JNDINode node)
        {
            List<JNDINode> children = new ArrayList<JNDINode>();

            try
            {
                Object value = node.getValue();
                if (value instanceof Context && node.depth <= config.getMaxDepth())
                {
                    Context context = (Context) value;

                    NamingEnumeration<NameClassPair> elements = null;

                    try
                    {
                        elements = context.list("");
                        while (elements.hasMore())
                        {
                            NameClassPair nameClassPair = elements.next();
                            String name = nameClassPair.getName();

                            children.add(new JNDINode(context, name, node.depth + 1));
                        }
                    }
                    finally
                    {
                        if (elements != null)
                        {
                            elements.close();
                        }
                    }
                }
            }
            catch (NamingException e)
            {
                config.fireError(EVENT_READ_PROPERTY, null, null, e);
            }

            return children;
        }

        public List<JNDINode> getChildren(JNDINode node, String name)
        {
            List<JNDINode> nodes = new ArrayList<JNDINode>(1);

            for (JNDINode n : getChildren(node))
            {
                if (name.equals(n.name)) {
                    nodes.add(n);
                    break;
                }
            }

            return nodes;
        }

        public JNDINode getChild(JNDINode node, int index)
        {
            return getChildren(node).get(index);
        }

        public int getChildrenCount(JNDINode node, String name)
        {
            return 1;
        }

        public void removeChild(JNDINode node, JNDINode child)
        {
            try
            {
                child.context.unbind(child.name);
            }
            catch (NamingException e)
            {
                throw new ConfigurationRuntimeException("Unable to remove the child JNDI node", e);
            }
        }

        public List<String> getAttributes(JNDINode node)
        {
            return Collections.emptyList();
        }

        public Object getAttributeValue(JNDINode node, String name)
        {
            return null;
        }

        public void setAttributeValue(JNDINode node, String name, Object value)
        {
        }

        public void addAttributeValue(JNDINode node, String name, Object value)
        {
        }

        public void removeAttribute(JNDINode node, String name)
        {
        }
    }
}
