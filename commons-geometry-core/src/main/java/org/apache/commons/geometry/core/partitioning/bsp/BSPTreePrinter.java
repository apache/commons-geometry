/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partitioning.bsp;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTree.Node;

/** Internal class for creating simple string representations of BSP trees.
 * @param <P> Point implementation type
 * @param <N> Node implementation type
 */
final class BSPTreePrinter<P extends Point<P>, N extends Node<P, N>>
    implements BSPTreeVisitor<P, N> {

    /** Line indent string. */
    private static final String INDENT = "    ";

    /** New line character. */
    private static final String NEW_LINE = "\n";

    /** Entry prefix for nodes on the minus side of their parent. */
    private static final String MINUS_CHILD = "[-] ";

    /** Entry prefix for nodes on the plus side of their parent. */
    private static final String PLUS_CHILD = "[+] ";

    /** Ellipsis for truncated representations. */
    private static final String ELLIPSIS = "...";

    /** Maximum depth of nodes that will be printed. */
    private final int maxDepth;

    /** Contains the string output. */
    private final StringBuilder output = new StringBuilder();

    /** Simple constructor.
     * @param maxDepth maximum depth of nodes to be printed
     */
    BSPTreePrinter(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /** {@inheritDoc} */
    @Override
    public Result visit(final N node) {
        final int depth = node.depth();

        if (depth <= maxDepth) {
            startLine(node);
            writeNode(node);
        } else if (depth == maxDepth + 1 && node.isPlus()) {
            startLine(node);
            write(ELLIPSIS);
        }

        return Result.CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(final N node) {
        if (node.depth() > maxDepth + 1) {
            return Order.NONE;
        }
        return Order.NODE_MINUS_PLUS;
    }

    /** Start a line for the given node.
     * @param node the node to begin a line for
     */
    private void startLine(final N node) {
        if (node.getParent() != null) {
            write(NEW_LINE);
        }

        final int depth = node.depth();
        for (int i = 0; i < depth; ++i) {
            write(INDENT);
        }
    }

    /** Writes the given node to the output.
     * @param node the node to write
     */
    private void writeNode(final N node) {
        if (node.getParent() != null) {
            if (node.isMinus()) {
                write(MINUS_CHILD);
            } else {
                write(PLUS_CHILD);
            }
        }

        write(node.toString());
    }

    /** Add the given string to the output.
     * @param str the string to add
     */
    private void write(String str) {
        output.append(str);
    }

    /** Return the string representation of the visited tree. */
    @Override
    public String toString() {
        return output.toString();
    }
}
