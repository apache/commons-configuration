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
import java.util.Map;

/**
 * Utility methods.
 *
 * @since 1.7
 */
public final class TreeUtils
{
    /** Prevent creating this class. */
    private TreeUtils()
    {
    }

    /**
     * Print out the data in the configuration.
     * @param stream The OutputStream.
     * @param result The root node of the tree.
     */
    public static void printTree(final PrintStream stream, final ImmutableNode result)
    {
        if (stream != null)
        {
            printTree(stream, "", result);
        }
    }

    private static void printTree(final PrintStream stream, final String indent, final ImmutableNode result)
    {
        final StringBuilder buffer = new StringBuilder(indent).append("<").append(result.getNodeName());
        for (final Map.Entry<String, Object> e : result.getAttributes().entrySet())
        {
            buffer.append(' ').append(e.getKey()).append("='").append(e.getValue()).append("'");
        }
        buffer.append(">");
        stream.print(buffer.toString());
        if (result.getValue() != null)
        {
            stream.print(result.getValue());
        }
        boolean newline = false;
        if (!result.getChildren().isEmpty())
        {
            stream.print("\n");
            for (final ImmutableNode child : result.getChildren())
            {
                printTree(stream, indent + "  ", child);
            }
            newline = true;
        }
        if (newline)
        {
            stream.print(indent);
        }
        stream.println("</" + result.getNodeName() + ">");
    }
}
