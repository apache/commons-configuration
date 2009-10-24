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

import java.io.PrintStream;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * Utility methods.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 * @since 1.7
 */
public class TreeUtils
{
    /**
     * Print out the data in the configuration.
     * @param stream The OutputStream.
     * @param result The root node of the tree.
     */
    public static void printTree(PrintStream stream, ConfigurationNode result)
    {
        if (stream != null)
        {
            printTree(stream, "", result, new ConfigurationNodeHandler());
        }
    }

    /**
     * Prints the data stored in the specified node and its children.
     *
     * @param <T> the type of the node
     * @param stream the output stream
     * @param result the root node of the tree
     * @param handler the node handler
     */
    public static <T> void printTree(PrintStream stream, T result,
            NodeHandler<T> handler)
    {
        if (stream != null)
        {
            printTree(stream, "", result, handler);
        }
    }

    private static <T> void printTree(PrintStream stream, String indent, T result, NodeHandler<T> handler)
    {
        StringBuilder buffer = new StringBuilder(indent).append("<").append(handler.nodeName(result));

        for (String attr : handler.getAttributes(result))
        {
            buffer.append(" ").append(attr).append("='").append(handler.getAttributeValue(result, attr)).append("'");
        }
        buffer.append(">");
        stream.print(buffer.toString());

        if (handler.getValue(result) != null)
        {
            stream.print(handler.getValue(result));
        }

        boolean newline = false;
        if (handler.getChildrenCount(result, null) > 0)
        {
            stream.print("\n");
            for (T node : handler.getChildren(result))
            {
                printTree(stream, indent + "  ", node, handler);
            }
            newline = true;
        }

        if (newline)
        {
            stream.print(indent);
        }

        stream.println("</" + handler.nodeName(result) + ">");
    }
}
