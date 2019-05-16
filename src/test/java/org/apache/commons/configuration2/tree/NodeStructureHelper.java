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
package org.apache.commons.configuration2.tree;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dbunit.dataset.common.handlers.NoHandler;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * A helper class for tests related to hierarchies of {@code ImmutableNode}
 * objects. This class provides functionality for creating test trees and
 * accessing single nodes. It can be used by various test classes.
 *
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

    /** An array with table names used for the TABLES tree. */
    private static final String[] TABLES = {
            "users", "documents"
    };

    /**
     * An array with the names of columns to be used for the TABLES tree.
     */
    private static final String[][] FIELDS = {
            {
                    "uid", "uname", "firstName", "lastName", "email"
            }, {
                    "docid", "name", "creationDate", "authorID", "version", "length"
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

    /** The root node of the TABLES tree. */
    public static final ImmutableNode ROOT_TABLES_TREE = createTablesTree();

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
    public static String author(final int idx)
    {
        return AUTHORS[idx];
    }

    /**
     * Returns the number of works for the author with the given index.
     *
     * @param authorIdx the author index
     * @return the number of works of this author
     */
    public static int worksLength(final int authorIdx)
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
    public static String work(final int authorIdx, final int idx)
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
    public static int personaeLength(final int authorIdx, final int workIdx)
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
    public static String persona(final int authorIdx, final int workIdx, final int personaIdx)
    {
        return PERSONAE[authorIdx][workIdx][personaIdx];
    }

    /**
     * Returns the number of tables in the tables tree.
     *
     * @return the number of tables
     */
    public static int tablesLength()
    {
        return TABLES.length;
    }

    /**
     * Returns the name of the test table with the given index.
     *
     * @param idx the index of the table
     * @return the name of the test table with this index
     */
    public static String table(final int idx)
    {
        return TABLES[idx];
    }

    /**
     * Returns the number of fields in the test table with the given index.
     *
     * @param tabIdx the index of the table
     * @return the number of fields in this table
     */
    public static int fieldsLength(final int tabIdx)
    {
        return FIELDS[tabIdx].length;
    }

    /**
     * Returns the name of the specified field in the tables tree.
     *
     * @param tabIdx the index of the table
     * @param fldIdx the index of the field
     * @return the name of this field
     */
    public static String field(final int tabIdx, final int fldIdx)
    {
        return FIELDS[tabIdx][fldIdx];
    }

    /**
     * Appends a component to a node path. The component is added separated by a
     * path separator.
     *
     * @param path the path
     * @param component the component to be added
     * @return the resulting path
     */
    public static String appendPath(final String path, final String component)
    {
        final StringBuilder buf =
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
    public static ImmutableNode nodeForKey(final ImmutableNode root, final String key)
    {
        final String[] components = key.split(PATH_SEPARATOR);
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
    public static ImmutableNode nodeForKey(final InMemoryNodeModel model, final String key)
    {
        return nodeForKey(model.getRootNode(), key);
    }

    /**
     * Evaluates the given key and finds the corresponding child node of the
     * root node of the specified {@code NodeHandler} object. This is a
     * convenience method that works like the method with the same name, but
     * obtains the root node from the given handler object.
     *
     * @param handler the {@code NodeHandler} object
     * @param key the key to the desired node
     * @return the found target node
     * @throws NoSuchElementException if the desired node cannot be found
     */
    public static ImmutableNode nodeForKey(final NodeHandler<ImmutableNode> handler,
            final String key)
    {
        return nodeForKey(handler.getRootNode(), key);
    }

    /**
     * Convenience method for creating a path for accessing a node based on the
     * node names.
     *
     * @param path an array with the expected node names on the path
     * @return the resulting path as string
     */
    public static String nodePath(final String... path)
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
    public static String nodePathWithEndNode(final String endNode, final String... path)
    {
        return nodePath(path) + PATH_SEPARATOR + endNode;
    }

    /**
     * Helper method for creating an immutable node with a name and a value.
     *
     * @param name the node's name
     * @param value the node's value
     * @return the new node
     */
    public static ImmutableNode createNode(final String name, final Object value)
    {
        return new ImmutableNode.Builder().name(name).value(value).create();
    }

    /**
     * Helper method for creating a field node with its children. Nodes of this
     * type are used within the tables tree. They define a single column of a
     * table.
     *
     * @param name the name of the field
     * @return the field node
     */
    public static ImmutableNode createFieldNode(final String name)
    {
        final ImmutableNode.Builder fldBuilder = new ImmutableNode.Builder(1);
        fldBuilder.addChild(createNode("name", name));
        return fldBuilder.name("field").create();
    }

    /**
     * Creates a mock for a resolver.
     *
     * @return the resolver mock
     */
    public static NodeKeyResolver<ImmutableNode> createResolverMock()
    {
        @SuppressWarnings("unchecked")
        final
        NodeKeyResolver<ImmutableNode> mock =
                EasyMock.createMock(NodeKeyResolver.class);
        return mock;
    }

    /**
     * Prepares a mock for a resolver to expect arbitrary resolve operations.
     * These operations are implemented on top of a default expression engine.
     *
     * @param resolver the mock resolver
     */
    @SuppressWarnings("unchecked")
    public static void expectResolveKeyForQueries(
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        EasyMock.expect(
                resolver.resolveKey(EasyMock.anyObject(ImmutableNode.class),
                        EasyMock.anyObject(String.class),
                        (NodeHandler<ImmutableNode>) EasyMock
                                .anyObject(NoHandler.class)))
                .andAnswer(new IAnswer<List<QueryResult<ImmutableNode>>>() {
                    @Override
                    public List<QueryResult<ImmutableNode>> answer()
                            throws Throwable {
                        final ImmutableNode root =
                                (ImmutableNode) EasyMock.getCurrentArguments()[0];
                        final String key = (String) EasyMock.getCurrentArguments()[1];
                        final NodeHandler<ImmutableNode> handler =
                                (NodeHandler<ImmutableNode>) EasyMock
                                        .getCurrentArguments()[2];
                        return DefaultExpressionEngine.INSTANCE.query(root,
                                key, handler);
                    }
                }).anyTimes();
    }

    /**
     * Prepares the passed in resolver mock to resolve add keys. They are
     * interpreted on a default expression engine.
     *
     * @param resolver the {@code NodeKeyResolver} mock
     */
    public static void expectResolveAddKeys(
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        EasyMock.expect(
                resolver.resolveAddKey(EasyMock.anyObject(ImmutableNode.class),
                        EasyMock.anyString(),
                        EasyMock.anyObject(TreeData.class)))
                .andAnswer(new IAnswer<NodeAddData<ImmutableNode>>() {
                    @Override
                    public NodeAddData<ImmutableNode> answer() throws Throwable {
                        final ImmutableNode root =
                                (ImmutableNode) EasyMock.getCurrentArguments()[0];
                        final String key = (String) EasyMock.getCurrentArguments()[1];
                        final TreeData handler =
                                (TreeData) EasyMock.getCurrentArguments()[2];
                        return DefaultExpressionEngine.INSTANCE.prepareAdd(
                                root, key, handler);
                    }
                }).anyTimes();
    }

    /**
     * Creates as tree with database table data based on the passed in arrays of
     * table names and fields for tables. Works like the method without
     * parameters, but allows defining the data of the structure.
     *
     * @param tables an array with the names of the tables
     * @param fields an array with the fields of the single tables
     * @return the resulting nodes structure
     */
    public static ImmutableNode createTablesTree(final String[] tables,
                                                 final String[][] fields)
    {
        final ImmutableNode.Builder bldTables =
                new ImmutableNode.Builder(tables.length);
        bldTables.name("tables");
        for (int i = 0; i < tables.length; i++)
        {
            final ImmutableNode.Builder bldTable = new ImmutableNode.Builder(2);
            bldTable.addChild(createNode("name", tables[i]));
            final ImmutableNode.Builder bldFields =
                    new ImmutableNode.Builder(fields[i].length);
            bldFields.name("fields");

            for (int j = 0; j < fields[i].length; j++)
            {
                bldFields.addChild(createFieldNode(fields[i][j]));
            }
            bldTable.addChild(bldFields.create());
            bldTables.addChild(bldTable.name("table").create());
        }
        return bldTables.create();
    }

    /**
     * Returns a clone of the array with the table names. This is useful if a
     * slightly different tree structure should be created.
     *
     * @return the cloned table names
     */
    public static String[] getClonedTables()
    {
        return TABLES.clone();
    }

    /**
     * Returns a clone of the array with the table fields. This is useful if a
     * slightly different tree structure should be created.
     *
     * @return the cloned field names
     */
    public static String[][] getClonedFields()
    {
        final String[][] fieldNamesNew = new String[FIELDS.length][];
        for (int i = 0; i < FIELDS.length; i++)
        {
            fieldNamesNew[i] = FIELDS[i].clone();
        }
        return fieldNamesNew;
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
        final ImmutableNode.Builder rootBuilder =
                new ImmutableNode.Builder(AUTHORS.length);
        for (int author = 0; author < AUTHORS.length; author++)
        {
            final ImmutableNode.Builder authorBuilder = new ImmutableNode.Builder();
            authorBuilder.name(AUTHORS[author]);
            for (int work = 0; work < WORKS[author].length; work++)
            {
                final ImmutableNode.Builder workBuilder = new ImmutableNode.Builder();
                workBuilder.name(WORKS[author][work]);
                for (final String person : PERSONAE[author][work])
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
        final ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        for (int author = 0; author < AUTHORS.length; author++)
        {
            for (int work = 0; work < WORKS[author].length; work++)
            {
                for (final String person : PERSONAE[author][work])
                {
                    final ImmutableNode orgValue =
                            new ImmutableNode.Builder().name(ELEM_ORG_VALUE)
                                    .value("yes")
                                    .addAttribute(ATTR_TESTED, Boolean.FALSE)
                                    .create();
                    final ImmutableNode workNode =
                            new ImmutableNode.Builder(1)
                                    .name(WORKS[author][work])
                                    .addChild(orgValue).create();
                    final ImmutableNode personNode =
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
     * Creates a tree with database table data with the following structure:
     *
     * tables
     *      table
     *         name
     *         fields
     *             field
     *                 name
     *             field
     *                 name
     * @return the resulting nodes structure
     */
    private static ImmutableNode createTablesTree()
    {
        return createTablesTree(TABLES, FIELDS);
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
    private static ImmutableNode findNode(final ImmutableNode parent,
            final String[] components, final int currentIdx)
    {
        if (currentIdx >= components.length)
        {
            return parent;
        }

        final Matcher m = PAT_KEY_WITH_INDEX.matcher(components[currentIdx]);
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
        for (final ImmutableNode node : parent.getChildren())
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
