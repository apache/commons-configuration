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

import java.io.PrintStream;
import java.util.Iterator;

/**
 * Utility methods.
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
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
    public static void printTree(PrintStream stream, ConfigurationNode result)
    {
        if (stream != null)
        {
            printTree(stream, "", result);
        }
    }

    private static void printTree(PrintStream stream, String indent, ConfigurationNode result)
    {
        StringBuffer buffer = new StringBuffer(indent).append("<").append(result.getName());
        Iterator iter = result.getAttributes().iterator();
        while (iter.hasNext())
        {
            ConfigurationNode node = (ConfigurationNode) iter.next();
            buffer.append(" ").append(node.getName()).append("='").append(node.getValue()).append("'");
        }
        buffer.append(">");
        stream.print(buffer.toString());
        if (result.getValue() != null)
        {
            stream.print(result.getValue());
        }
        boolean newline = false;
        if (result.getChildrenCount() > 0)
        {
            stream.print("\n");
            iter = result.getChildren().iterator();
            while (iter.hasNext())
            {
                printTree(stream, indent + "  ", (ConfigurationNode) iter.next());
            }
            newline = true;
        }
        if (newline)
        {
            stream.print(indent);
        }
        stream.println("</" + result.getName() + ">");
    }
}
