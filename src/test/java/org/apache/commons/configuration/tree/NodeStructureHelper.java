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
package org.apache.commons.configuration.tree;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * A helper class for tests related to hierarchies of {@code ImmutableNode}
 * objects. This class provides functionality for creating test trees and
 * accessing single nodes. It can be used by various test classes.
 *
 * @version $Id$
 */
public class NodeStructureHelper
{
    /** A pattern for parsing node keys with optional indices. */
    private static final Pattern PAT_KEY_WITH_INDEX = Pattern
            .compile("(\\w+)\\((\\d+)\\)");

    /** The character for splitting node path elements. */
    private static final String PATH_SEPARATOR = "/";

    /** An array with authors. */
    private static final String[] AUTHORS = {
            "Shakespeare", "Homer", "Simmons"
    };

    /** An array with the works of the test authors. */
    private static final String[][] WORKS = {
            {
                    "Troilus and Cressida", "The Tempest",
                    "A Midsummer Night's Dream"
            }, {
                "Ilias"
            }, {
                    "Ilium", "Hyperion"
            }
    };

    /** An array with the personae in the works. */
    private static final String[][][] PERSONAE = {
            {
                    // Works of Shakespeare
                    {
                            "Troilus", "Cressidia", "Ajax", "Achilles"
                    }, {
                            "Prospero", "Ariel"
                    }, {
                            "Oberon", "Titania", "Puck"
                    }
            }, {
                // Works of Homer
                {
                        "Achilles", "Agamemnon", "Hektor"
                }
            }, {
                    // Works of Dan Simmons
                    {
                            "Hockenberry", "Achilles"
                    }, {
                            "Shrike", "Moneta", "Consul", "Weintraub"
                    }
            }
    };

    /** Constant for the author attribute. */
    public static final String ATTR_AUTHOR = "author";

    /** Constant for the original value element in the personae tree. */
    public static final String ELEM_ORG_VALUE = "originalValue";

    /** Constant for the tested attribute. */
    public static final String ATTR_TESTED = "tested";

    /** The root node of the authors tree. */
    public static final ImmutableNode ROOT_AUTHORS_TREE = createAuthorsTree();

    /** The root node of the personae tree. */
    public static final ImmutableNode ROOT_PERSONAE_TREE = createPersonaeTree();

    /**
     * Returns the number of authors.
     *
     * @return the number of authors
     */
    public static int authorsLength()
    {
        return AUTHORS.length;
    }

    /**
     * Returns the name of the author at the given index.
     *
     * @param idx the index
     * @return the name of this author
     */
    public static String author(int idx)
    {
        return AUTHORS[idx];
    }

    /**
     * Returns the number of works for the author with the given index.
     *
     * @param authorIdx the author index
     * @return the number of works of this author
     */
    public static int worksLength(int authorIdx)
    {
        return WORKS[authorIdx].length;
    }

    /**
     * Returns the work of an author with a given index.
     *
     * @param authorIdx the author index
     * @param idx the index of the work
     * @return the desired work
     */
    public static String work(int authorIdx, int idx)
    {
        return WORKS[authorIdx][idx];
    }

    /**
     * Returns the number of personae in the given work of the specified author.
     *
     * @param authorIdx the author index
     * @param workIdx the index of the work
     * @return the number of personae in this work
     */
    public static int personaeLength(int authorIdx, int workIdx)
    {
        return PERSONAE[authorIdx][workIdx].length;
    }

    /**
     * Returns the name of a persona.
     *
     * @param authorIdx the author index
     * @param workIdx the index of the work
     * @param personaIdx the index of the persona
     * @return the name of this persona
     */
    public static String persona(int authorIdx, int workIdx, int personaIdx)
    {
        return PERSONAE[authorIdx][workIdx][personaIdx];
    }

    /**
     * Appends a component to a node path. The component is added separated by a
     * path separator.
     *
     * @param path the path
     * @param component the component to be added
     * @return the resulting path
     */
    public static String appendPath(String path, String component)
    {
        StringBuilder buf =
                new StringBuilder(StringUtils.length(path)
                        + StringUtils.length(component) + 1);
        buf.append(path).append(PATH_SEPARATOR).append(component);
        return buf.toString();
    }

    /**
     * Evaluates the given key and finds the corresponding child node of the
     * specified root. Keys have the form {@code path/to/node}. If there are
     * multiple sibling nodes with the same name, a numerical index can be
     * specified in parenthesis.
     *
     * @param root the root node
     * @param key the key to the desired node
     * @return the node with this key
     * @throws NoSuchElementException if the key cannot be resolved
     */
    public static ImmutableNode nodeForKey(ImmutableNode root, String key)
    {
        String[] components = key.split(PATH_SEPARATOR);
        return findNode(root, components, 0);
    }

    /**
     * Evaluates the given key and finds the corresponding child node of the
     * root node of the specified model. This is a convenience method that works
     * like the method with the same name, but obtains the root node from the
     * given model.
     *
     * @param model the node model
     * @param key the key to the desired node
     * @return the found target node
     * @throws NoSuchElementException if the desired node cannot be found
     */
    public static ImmutableNode nodeForKey(InMemoryNodeModel model, String key)
    {
        return nodeForKey(model.getRootNode(), key);
    }

    /**
     * Convenience method for creating a path for accessing a node based on the
     * node names.
     *
     * @param path an array with the expected node names on the path
     * @return the resulting path as string
     */
    public static String nodePath(String... path)
    {
        return StringUtils.join(path, PATH_SEPARATOR);
    }

    /**
     * Convenience method for creating a node path with a special end node.
     *
     * @param endNode the name of the last path component
     * @param path an array with the expected node names on the path
     * @return the resulting path as string
     */
    public static String nodePathWithEndNode(String endNode, String... path)
    {
        return nodePath(path) + PATH_SEPARATOR + endNode;
    }

    /**
     * Creates a tree with a root node whose children are the test authors. Each
     * other has his works as child nodes. Each work has its personae as
     * children.
     *
     * @return the root node of the authors tree
     */
    private static ImmutableNode createAuthorsTree()
    {
        ImmutableNode.Builder rootBuilder =
                new ImmutableNode.Builder(AUTHORS.length);
        for (int author = 0; author < AUTHORS.length; author++)
        {
            ImmutableNode.Builder authorBuilder = new ImmutableNode.Builder();
            authorBuilder.name(AUTHORS[author]);
            for (int work = 0; work < WORKS[author].length; work++)
            {
                ImmutableNode.Builder workBuilder = new ImmutableNode.Builder();
                workBuilder.name(WORKS[author][work]);
                for (String person : PERSONAE[author][work])
                {
                    workBuilder.addChild(new ImmutableNode.Builder().name(
                            person).create());
                }
                authorBuilder.addChild(workBuilder.create());
            }
            rootBuilder.addChild(authorBuilder.create());
        }
        return rootBuilder.name("authorTree").create();
    }

    /**
     * Creates a tree with a root node whose children are the test personae.
     * Each node represents a person and has an attribute pointing to the author
     * who invented this person. There is a single child node for the associated
     * work which has again a child and an attribute.
     *
     * @return the root node of the personae tree
     */
    private static ImmutableNode createPersonaeTree()
    {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        for (int author = 0; author < AUTHORS.length; author++)
        {
            for (int work = 0; work < WORKS[author].length; work++)
            {
                for (String person : PERSONAE[author][work])
                {
                    ImmutableNode orgValue =
                            new ImmutableNode.Builder().name(ELEM_ORG_VALUE)
                                    .value("yes")
                                    .addAttribute(ATTR_TESTED, Boolean.FALSE)
                                    .create();
                    ImmutableNode workNode =
                            new ImmutableNode.Builder(1)
                                    .name(WORKS[author][work])
                                    .addChild(orgValue).create();
                    ImmutableNode personNode =
                            new ImmutableNode.Builder(1).name(person)
                                    .addAttribute(ATTR_AUTHOR, AUTHORS[author])
                                    .addChild(workNode).create();
                    rootBuilder.addChild(personNode);
                }
            }
        }
        return rootBuilder.create();
    }

    /**
     * Helper method for evaluating a single component of a node key.
     *
     * @param parent the current parent node
     * @param components the array with the components of the node key
     * @param currentIdx the index of the current path component
     * @return the found target node
     * @throws NoSuchElementException if the desired node cannot be found
     */
    private static ImmutableNode findNode(ImmutableNode parent,
            String[] components, int currentIdx)
    {
        if (currentIdx >= components.length)
        {
            return parent;
        }

        Matcher m = PAT_KEY_WITH_INDEX.matcher(components[currentIdx]);
        String childName;
        int childIndex;
        if (m.matches())
        {
            childName = m.group(1);
            childIndex = Integer.parseInt(m.group(2));
        }
        else
        {
            childName = components[currentIdx];
            childIndex = 0;
        }

        int foundIdx = 0;
        for (ImmutableNode node : parent.getChildren())
        {
            if (childName.equals(node.getNodeName()))
            {
                if (foundIdx++ == childIndex)
                {
                    return findNode(node, components, currentIdx + 1);
                }
            }
        }
        throw new NoSuchElementException("Cannot resolve child "
                + components[currentIdx]);
    }
}
