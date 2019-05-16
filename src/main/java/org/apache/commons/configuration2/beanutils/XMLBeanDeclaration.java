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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * An implementation of the {@code BeanDeclaration} interface that is
 * suitable for XML configuration files.
 * </p>
 * <p>
 * This class defines the standard layout of a bean declaration in an XML
 * configuration file. Such a declaration must look like the following example
 * fragment:
 * </p>
 *
 * <pre>
 *   ...
 *   &lt;personBean config-class=&quot;my.model.PersonBean&quot;
 *       lastName=&quot;Doe&quot; firstName=&quot;John&quot;&gt;
 *       &lt;config-constrarg config-value=&quot;ID03493&quot; config-type=&quot;java.lang.String&quot;/&gt;
 *       &lt;address config-class=&quot;my.model.AddressBean&quot;
 *           street=&quot;21st street 11&quot; zip=&quot;1234&quot;
 *           city=&quot;TestCity&quot;/&gt;
 *   &lt;/personBean&gt;
 * </pre>
 *
 * <p>
 * The bean declaration can be contained in an arbitrary element. Here it is the
 * {@code personBean} element. In the attributes of this element
 * there can occur some reserved attributes, which have the following meaning:
 * </p>
 * <dl>
 * <dt>{@code config-class}</dt>
 * <dd>Here the full qualified name of the bean's class can be specified. An
 * instance of this class will be created. If this attribute is not specified,
 * the bean class must be provided in another way, e.g. as the
 * {@code defaultClass} passed to the {@code BeanHelper} class.</dd>
 * <dt>{@code config-factory}</dt>
 * <dd>This attribute can contain the name of the
 * {@link BeanFactory} that should be used for creating the bean.
 * If it is defined, a factory with this name must have been registered at the
 * {@code BeanHelper} class. If this attribute is missing, the default
 * bean factory will be used.</dd>
 * <dt>{@code config-factoryParam}</dt>
 * <dd>With this attribute a parameter can be specified that will be passed to
 * the bean factory. This may be useful for custom bean factories.</dd>
 * </dl>
 * <p>
 * All further attributes starting with the {@code config-} prefix are
 * considered as meta data and will be ignored. All other attributes are treated
 * as properties of the bean to be created, i.e. corresponding setter methods of
 * the bean will be invoked with the values specified here.
 * </p>
 * <p>
 * If the bean to be created has also some complex properties (which are itself
 * beans), their values cannot be initialized from attributes. For this purpose
 * nested elements can be used. The example listing shows how an address bean
 * can be initialized. This is done in a nested element whose name must match
 * the name of a property of the enclosing bean declaration. The format of this
 * nested element is exactly the same as for the bean declaration itself, i.e.
 * it can have attributes defining meta data or bean properties and even further
 * nested elements for complex bean properties.
 * </p>
 * <p>
 * If the bean should be created using a specific constructor, the constructor
 * arguments have to be specified. This is done by an arbitrary number of
 * nested {@code <config-constrarg>} elements. Each element can either have the
 * {@code config-value} attribute - then it defines a simple value - or must be
 * again a bean declaration (conforming to the format defined here) defining
 * the complex value of this constructor argument.
 * </p>
 * <p>
 * A {@code XMLBeanDeclaration} object is usually created from a
 * {@code HierarchicalConfiguration}. From this it will derive a
 * {@code SubnodeConfiguration}, which is used to access the needed
 * properties. This subnode configuration can be obtained using the
 * {@link #getConfiguration()} method. All of its properties can
 * be accessed in the usual way. To ensure that the property keys used by this
 * class are understood by the configuration, the default expression engine will
 * be set.
 * </p>
 *
 * @since 1.3
 */
public class XMLBeanDeclaration implements BeanDeclaration
{
    /** Constant for the prefix of reserved attributes. */
    public static final String RESERVED_PREFIX = "config-";

    /** Constant for the prefix for reserved attributes.*/
    public static final String ATTR_PREFIX = "[@" + RESERVED_PREFIX;

    /** Constant for the bean class attribute. */
    public static final String ATTR_BEAN_CLASS = ATTR_PREFIX + "class]";

    /** Constant for the bean factory attribute. */
    public static final String ATTR_BEAN_FACTORY = ATTR_PREFIX + "factory]";

    /** Constant for the bean factory parameter attribute. */
    public static final String ATTR_FACTORY_PARAM = ATTR_PREFIX
            + "factoryParam]";

    /** Constant for the name of the bean class attribute. */
    private static final String ATTR_BEAN_CLASS_NAME = RESERVED_PREFIX + "class";

    /** Constant for the name of the element for constructor arguments. */
    private static final String ELEM_CTOR_ARG = RESERVED_PREFIX + "constrarg";

    /**
     * Constant for the name of the attribute with the value of a constructor
     * argument.
     */
    private static final String ATTR_CTOR_VALUE = RESERVED_PREFIX + "value";

    /**
     * Constant for the name of the attribute with the data type of a
     * constructor argument.
     */
    private static final String ATTR_CTOR_TYPE = RESERVED_PREFIX + "type";

    /** Stores the associated configuration. */
    private final HierarchicalConfiguration<?> configuration;

    /** Stores the configuration node that contains the bean declaration. */
    private final NodeData<?> node;

    /** The name of the default bean class. */
    private final String defaultBeanClassName;

    /**
     * Creates a new instance of {@code XMLBeanDeclaration} and initializes it
     * from the given configuration. The passed in key points to the bean
     * declaration.
     *
     * @param config the configuration (must not be <b>null</b>)
     * @param key the key to the bean declaration (this key must point to
     *        exactly one bean declaration or a {@code IllegalArgumentException}
     *        exception will be thrown)
     * @param <T> the node type of the configuration
     * @throws IllegalArgumentException if required information is missing to
     *         construct the bean declaration
     */
    public <T> XMLBeanDeclaration(final HierarchicalConfiguration<T> config, final String key)
    {
        this(config, key, false);
    }

    /**
     * Creates a new instance of {@code XMLBeanDeclaration} and initializes it
     * from the given configuration supporting optional declarations.
     *
     * @param config the configuration (must not be <b>null</b>)
     * @param key the key to the bean declaration
     * @param optional a flag whether this declaration is optional; if set to
     *        <b>true</b>, no exception will be thrown if the passed in key is
     *        undefined
     * @param <T> the node type of the configuration
     * @throws IllegalArgumentException if required information is missing to
     *         construct the bean declaration
     */
    public <T> XMLBeanDeclaration(final HierarchicalConfiguration<T> config, final String key,
            final boolean optional)
    {
        this(config, key, optional, null);
    }

    /**
     * Creates a new instance of {@code XMLBeanDeclaration} and initializes it
     * from the given configuration supporting optional declarations and a
     * default bean class name. The passed in key points to the bean
     * declaration. If the key does not exist and the boolean argument is
     * <b>true</b>, the declaration is initialized with an empty configuration.
     * It is possible to create objects from such an empty declaration if a
     * default class is provided. If the key on the other hand has multiple
     * values or is undefined and the boolean argument is <b>false</b>, a
     * {@code IllegalArgumentException} exception will be thrown. It is possible
     * to set a default bean class name; this name is used if the configuration
     * does not contain a bean class.
     *
     * @param config the configuration (must not be <b>null</b>)
     * @param key the key to the bean declaration
     * @param optional a flag whether this declaration is optional; if set to
     *        <b>true</b>, no exception will be thrown if the passed in key is
     *        undefined
     * @param defBeanClsName a default bean class name
     * @param <T> the node type of the configuration
     * @throws IllegalArgumentException if required information is missing to
     *         construct the bean declaration
     * @since 2.0
     */
    public <T> XMLBeanDeclaration(final HierarchicalConfiguration<T> config, final String key,
            final boolean optional, final String defBeanClsName)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Configuration must not be null!");
        }

        HierarchicalConfiguration<?> tmpconfiguration;
        try
        {
            tmpconfiguration = config.configurationAt(key);
        }
        catch (final ConfigurationRuntimeException iex)
        {
            // If we reach this block, the key does not have exactly one value
            if (!optional || config.getMaxIndex(key) > 0)
            {
                throw iex;
            }
            tmpconfiguration = new BaseHierarchicalConfiguration();
        }
        this.node = createNodeDataFromConfiguration(tmpconfiguration);
        this.configuration = tmpconfiguration;
        defaultBeanClassName = defBeanClsName;
        initSubnodeConfiguration(getConfiguration());
    }

    /**
     * Creates a new instance of {@code XMLBeanDeclaration} and
     * initializes it from the given configuration. The configuration's root
     * node must contain the bean declaration.
     *
     * @param config the configuration with the bean declaration
     * @param <T> the node type of the configuration
     */
    public <T> XMLBeanDeclaration(final HierarchicalConfiguration<T> config)
    {
        this(config, (String) null);
    }

    /**
     * Creates a new instance of {@code XMLBeanDeclaration} and
     * initializes it with the configuration node that contains the bean
     * declaration. This constructor is used internally.
     *
     * @param config the configuration
     * @param node the node with the bean declaration.
     */
    XMLBeanDeclaration(final HierarchicalConfiguration<?> config,
            final NodeData<?> node)
    {
        this.node = node;
        configuration = config;
        defaultBeanClassName = null;
        initSubnodeConfiguration(config);
    }

    /**
     * Returns the configuration object this bean declaration is based on.
     *
     * @return the associated configuration
     */
    public HierarchicalConfiguration<?> getConfiguration()
    {
        return configuration;
    }

    /**
     * Returns the name of the default bean class. This class is used if no bean
     * class is specified in the configuration. It may be <b>null</b> if no
     * default class was set.
     *
     * @return the default bean class name
     * @since 2.0
     */
    public String getDefaultBeanClassName()
    {
        return defaultBeanClassName;
    }

    /**
     * Returns the name of the bean factory. This information is fetched from
     * the {@code config-factory} attribute.
     *
     * @return the name of the bean factory
     */
    @Override
    public String getBeanFactoryName()
    {
        return getConfiguration().getString(ATTR_BEAN_FACTORY, null);
    }

    /**
     * Returns a parameter for the bean factory. This information is fetched
     * from the {@code config-factoryParam} attribute.
     *
     * @return the parameter for the bean factory
     */
    @Override
    public Object getBeanFactoryParameter()
    {
        return getConfiguration().getProperty(ATTR_FACTORY_PARAM);
    }

    /**
     * Returns the name of the class of the bean to be created. This information
     * is obtained from the {@code config-class} attribute.
     *
     * @return the name of the bean's class
     */
    @Override
    public String getBeanClassName()
    {
        return getConfiguration().getString(ATTR_BEAN_CLASS, getDefaultBeanClassName());
    }

    /**
     * Returns a map with the bean's (simple) properties. The properties are
     * collected from all attribute nodes, which are not reserved.
     *
     * @return a map with the bean's properties
     */
    @Override
    public Map<String, Object> getBeanProperties()
    {
        final Map<String, Object> props = new HashMap<>();
        for (final String key : getAttributeNames())
        {
            if (!isReservedAttributeName(key))
            {
                props.put(key, interpolate(getNode().getAttribute(key)));
            }
        }

        return props;
    }

    /**
     * Returns a map with bean declarations for the complex properties of the
     * bean to be created. These declarations are obtained from the child nodes
     * of this declaration's root node.
     *
     * @return a map with bean declarations for complex properties
     */
    @Override
    public Map<String, Object> getNestedBeanDeclarations()
    {
        final Map<String, Object> nested = new HashMap<>();
        for (final NodeData<?> child : getNode().getChildren())
        {
            if (!isReservedChildName(child.nodeName()))
            {
                if (nested.containsKey(child.nodeName()))
                {
                    final Object obj = nested.get(child.nodeName());
                    List<BeanDeclaration> list;
                    if (obj instanceof List)
                    {
                        // Safe because we created the lists ourselves.
                        @SuppressWarnings("unchecked")
                        final
                        List<BeanDeclaration> tmpList = (List<BeanDeclaration>) obj;
                        list = tmpList;
                    }
                    else
                    {
                        list = new ArrayList<>();
                        list.add((BeanDeclaration) obj);
                        nested.put(child.nodeName(), list);
                    }
                    list.add(createBeanDeclaration(child));
                }
                else
                {
                    nested.put(child.nodeName(), createBeanDeclaration(child));
                }
            }
        }

        return nested;
    }

    /**
     * {@inheritDoc} This implementation processes all child nodes with the name
     * {@code config-constrarg}. If such a node has a {@code config-class}
     * attribute, it is considered a nested bean declaration; otherwise it is
     * interpreted as a simple value. If no nested constructor argument
     * declarations are found, result is an empty collection.
     */
    @Override
    public Collection<ConstructorArg> getConstructorArgs()
    {
        final Collection<ConstructorArg> args = new LinkedList<>();
        for (final NodeData<?> child : getNode().getChildren(ELEM_CTOR_ARG))
        {
            args.add(createConstructorArg(child));
        }
        return args;
    }

    /**
     * Performs interpolation for the specified value. This implementation will
     * interpolate against the current subnode configuration's parent. If sub
     * classes need a different interpolation mechanism, they should override
     * this method.
     *
     * @param value the value that is to be interpolated
     * @return the interpolated value
     */
    protected Object interpolate(final Object value)
    {
        final ConfigurationInterpolator interpolator =
                getConfiguration().getInterpolator();
        return interpolator != null ? interpolator.interpolate(value) : value;
    }

    /**
     * Checks if the specified child node name is reserved and thus should be
     * ignored. This method is called when processing child nodes of this bean
     * declaration. It is then possible to ignore some nodes with a specific
     * meaning. This implementation delegates to {@link #isReservedName(String)}
     * .
     *
     * @param name the name of the child node to be checked
     * @return a flag whether this name is reserved
     * @since 2.0
     */
    protected boolean isReservedChildName(final String name)
    {
        return isReservedName(name);
    }

    /**
     * Checks if the specified attribute name is reserved and thus does not
     * point to a property of the bean to be created. This method is called when
     * processing the attributes of this bean declaration. It is then possible
     * to ignore some attributes with a specific meaning. This implementation
     * delegates to {@link #isReservedName(String)}.
     *
     * @param name the name of the attribute to be checked
     * @return a flag whether this name is reserved
     * @since 2.0
     */
    protected boolean isReservedAttributeName(final String name)
    {
        return isReservedName(name);
    }

    /**
     * Checks if the specified name of a node or attribute is reserved and thus
     * should be ignored. This method is called per default by the methods for
     * checking attribute and child node names. It checks whether the passed in
     * name starts with the reserved prefix.
     *
     * @param name the name to be checked
     * @return a flag whether this name is reserved
     */
    protected boolean isReservedName(final String name)
    {
        return name == null || name.startsWith(RESERVED_PREFIX);
    }

    /**
     * Returns a set with the names of the attributes of the configuration node
     * holding the data of this bean declaration.
     *
     * @return the attribute names of the underlying configuration node
     */
    protected Set<String> getAttributeNames()
    {
        return getNode().getAttributes();
    }

    /**
     * Returns the data about the associated node.
     *
     * @return the node with the bean declaration
     */
    NodeData<?> getNode()
    {
        return node;
    }

    /**
     * Creates a new {@code BeanDeclaration} for a child node of the
     * current configuration node. This method is called by
     * {@code getNestedBeanDeclarations()} for all complex sub properties
     * detected by this method. Derived classes can hook in if they need a
     * specific initialization. This base implementation creates a
     * {@code XMLBeanDeclaration} that is properly initialized from the
     * passed in node.
     *
     * @param node the child node, for which a {@code BeanDeclaration} is
     *        to be created
     * @return the {@code BeanDeclaration} for this child node
     */
    BeanDeclaration createBeanDeclaration(final NodeData<?> node)
    {
        for (final HierarchicalConfiguration<?> config : getConfiguration()
                .configurationsAt(node.escapedNodeName(getConfiguration())))
        {
            if (node.matchesConfigRootNode(config))
            {
                return new XMLBeanDeclaration(config, node);
            }
        }
        throw new ConfigurationRuntimeException("Unable to match node for "
                + node.nodeName());
    }

    /**
     * Initializes the internally managed sub configuration. This method
     * will set some default values for some properties.
     *
     * @param conf the configuration to initialize
     */
    private void initSubnodeConfiguration(final HierarchicalConfiguration<?> conf)
    {
        conf.setExpressionEngine(null);
    }

    /**
     * Creates a {@code ConstructorArg} object for the specified configuration
     * node.
     *
     * @param child the configuration node
     * @return the corresponding {@code ConstructorArg} object
     */
    private ConstructorArg createConstructorArg(final NodeData<?> child)
    {
        final String type = getAttribute(child, ATTR_CTOR_TYPE);
        if (isBeanDeclarationArgument(child))
        {
            return ConstructorArg.forValue(
                    getAttribute(child, ATTR_CTOR_VALUE), type);
        }
        return ConstructorArg.forBeanDeclaration(
                createBeanDeclaration(child), type);
    }

    /**
     * Helper method for obtaining an attribute of a configuration node.
     * This method also takes interpolation into account.
     *
     * @param nd the node
     * @param attr the name of the attribute
     * @return the string value of this attribute (can be <b>null</b>)
     */
    private String getAttribute(final NodeData<?> nd, final String attr)
    {
        final Object value = nd.getAttribute(attr);
        return value == null ? null : String.valueOf(interpolate(value));
    }

    /**
     * Checks whether the constructor argument represented by the given
     * configuration node is a bean declaration.
     *
     * @param nd the configuration node in question
     * @return a flag whether this constructor argument is a bean declaration
     */
    private static boolean isBeanDeclarationArgument(final NodeData<?> nd)
    {
        return !nd.getAttributes().contains(ATTR_BEAN_CLASS_NAME);
    }

    /**
     * Creates a {@code NodeData} object from the root node of the given
     * configuration.
     *
     * @param config the configuration
     * @param <T> the type of the nodes
     * @return the {@code NodeData} object
     */
    private static <T> NodeData<T> createNodeDataFromConfiguration(
            final HierarchicalConfiguration<T> config)
    {
        final NodeHandler<T> handler = config.getNodeModel().getNodeHandler();
        return new NodeData<>(handler.getRootNode(), handler);
    }

    /**
     * An internally used helper class which wraps the node with the bean
     * declaration and the corresponding node handler.
     *
     * @param <T> the type of the node
     */
    static class NodeData<T>
    {
        /** The wrapped node. */
        private final T node;

        /** The node handler for interacting with this node. */
        private final NodeHandler<T> handler;

        /**
         * Creates a new instance of {@code NodeData}.
         *
         * @param nd the node
         * @param hndlr the handler
         */
        public NodeData(final T nd, final NodeHandler<T> hndlr)
        {
            node = nd;
            handler = hndlr;
        }

        /**
         * Returns the name of the wrapped node.
         *
         * @return the node name
         */
        public String nodeName()
        {
            return handler.nodeName(node);
        }

        /**
         * Returns the unescaped name of the node stored in this data object.
         * This method handles the case that the node name may contain reserved
         * characters with a special meaning for the current expression engine.
         * In this case, the characters affected have to be escaped accordingly.
         *
         * @param config the configuration
         * @return the escaped node name
         */
        public String escapedNodeName(final HierarchicalConfiguration<?> config)
        {
            return config.getExpressionEngine().nodeKey(node,
                    StringUtils.EMPTY, handler);
        }

        /**
         * Returns a list with the children of the wrapped node, again wrapped
         * into {@code NodeData} objects.
         *
         * @return a list with the children
         */
        public List<NodeData<T>> getChildren()
        {
            return wrapInNodeData(handler.getChildren(node));
        }

        /**
         * Returns a list with the children of the wrapped node with the given
         * name, again wrapped into {@code NodeData} objects.
         *
         * @param name the name of the desired child nodes
         * @return a list with the children with this name
         */
        public List<NodeData<T>> getChildren(final String name)
        {
            return wrapInNodeData(handler.getChildren(node, name));
        }

        /**
         * Returns a set with the names of the attributes of the wrapped node.
         *
         * @return the attribute names of this node
         */
        public Set<String> getAttributes()
        {
            return handler.getAttributes(node);
        }

        /**
         * Returns the value of the attribute with the given name of the wrapped
         * node.
         *
         * @param key the key of the attribute
         * @return the value of this attribute
         */
        public Object getAttribute(final String key)
        {
            return handler.getAttributeValue(node, key);
        }

        /**
         * Returns a flag whether the wrapped node is the root node of the
         * passed in configuration.
         *
         * @param config the configuration
         * @return a flag whether this node is the configuration's root node
         */
        public boolean matchesConfigRootNode(final HierarchicalConfiguration<?> config)
        {
            return config.getNodeModel().getNodeHandler().getRootNode()
                    .equals(node);
        }

        /**
         * Wraps the passed in list of nodes in {@code NodeData} objects.
         *
         * @param nodes the list with nodes
         * @return the wrapped nodes
         */
        private List<NodeData<T>> wrapInNodeData(final List<T> nodes)
        {
            final List<NodeData<T>> result = new ArrayList<>(nodes.size());
            for (final T node : nodes)
            {
                result.add(new NodeData<>(node, handler));
            }
            return result;
        }
    }
}
