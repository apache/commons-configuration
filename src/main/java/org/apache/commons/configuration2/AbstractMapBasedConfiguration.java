package org.apache.commons.configuration2;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author The-Alchemist
 */
public class AbstractMapBasedConfiguration extends BaseHierarchicalConfiguration {

    public AbstractMapBasedConfiguration() {
        super();

        initLogger(new ConfigurationLogger(getClass()));
    }

    protected void load(Map<String, Object> map) {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        ImmutableNode top = constructHierarchy(rootBuilder, map);
        getNodeModel().setRootNode(top);
    }

    /**
     * Constructs a YAML map, i.e. String -> Object from a given
     * configuration node.
     *
     * @param node The configuration node to create a map from.
     * @return A Map that contains the configuration node information.
     */
    protected Map<String, Object> constructMap(ImmutableNode node)
    {
        Map<String, Object> map = new HashMap<String, Object>(node.getChildren().size());
        for (ImmutableNode cNode : node.getChildren())
        {
            if (cNode.getChildren().isEmpty())
            {
                map.put(cNode.getNodeName(), cNode.getValue());
            }
            else
            {
                map.put(cNode.getNodeName(), constructMap(cNode));
            }
        }
        return map;
    }

    /**
     * Constructs the internal configuration nodes hierarchy.
     *  @param parent The configuration node that is the root of the current configuration section.
     * @param map The map with the yaml configurations nodes, i.e. String -> Object.
     */
    protected ImmutableNode constructHierarchy(ImmutableNode.Builder parent, Map<String, Object> map)
    {
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map)
            {
                ImmutableNode.Builder subtree = new ImmutableNode.Builder()
                        .name(key);
                ImmutableNode children = constructHierarchy(subtree, (Map) value);
                parent.addChild(children);
            }
            else
            {
                ImmutableNode leaf = new ImmutableNode.Builder()
                        .name(key)
                        .value(value)
                        .create();
                parent.addChild(leaf);
            }
        }
        return parent.create();
    }


    static void rethrowException(Exception e) throws ConfigurationException {
        if(e instanceof ClassCastException)
        {
            throw new ConfigurationException("Error parsing", e);
        }
        else
        {
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }
}
