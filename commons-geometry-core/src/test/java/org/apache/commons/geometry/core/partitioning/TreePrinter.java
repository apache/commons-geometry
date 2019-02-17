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
package org.apache.commons.geometry.core.partitioning;

import java.util.Objects;

import org.apache.commons.geometry.core.Point;

/** Base for classes that create string representations of {@link BSPTree_Old}s.
 * @param <P> Point type defining the space
 */
public abstract class TreePrinter<P extends Point<P>> implements BSPTreeVisitor_Old<P> {

    /** Indent per tree level */
    protected static final String INDENT = "    ";

    /** Current depth in the tree */
    protected int depth;

    /** Contains the string output */
    protected StringBuilder output = new StringBuilder();

    /** Returns a string representation of the given {@link BSPTree_Old}.
     * @param tree
     * @return
     */
    public String writeAsString(BSPTree_Old<P> tree) {
        output.delete(0, output.length());

        tree.visit(this);

        return output.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(BSPTree_Old<P> node) {
        return Order.SUB_MINUS_PLUS;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(BSPTree_Old<P> node) {
        writeLinePrefix(node);
        writeInternalNode(node);

        write("\n");

        ++depth;
    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(BSPTree_Old<P> node) {
        writeLinePrefix(node);
        writeLeafNode(node);

        write("\n");

        BSPTree_Old<P> cur = node;
        while (cur.getParent() != null && cur.getParent().getPlus() == cur) {
            --depth;
            cur = cur.getParent();
        }
    }

    /** Writes the prefix for the current line in the output. This includes
     * the line indent, the plus/minus node indicator, and a string identifier
     * for the node itself.
     * @param node
     */
    protected void writeLinePrefix(BSPTree_Old<P> node) {
        for (int i=0; i<depth; ++i) {
            write(INDENT);
        }

        if (node.getParent() != null) {
            if (node.getParent().getMinus() == node) {
                write("[-] ");
            }
            else {
                write("[+] ");
            }
        }

        write(nodeIdString(node) + " | ");
    }

    /** Returns a short string identifier for the given node.
     * @param node
     * @return
     */
    protected String nodeIdString(BSPTree_Old<P> node) {
        String str = Objects.toString(node);
        int idx = str.lastIndexOf('.');
        if (idx > -1) {
            return str.substring(idx + 1, str.length());
        }
        return str;
    }

    /** Adds the given string to the output.
     * @param str
     */
    protected void write(String str) {
        output.append(str);
    }

    /** Method for subclasses to provide their own string representation
     * of the given internal node.
     */
    protected abstract void writeInternalNode(BSPTree_Old<P> node);

    /** Writes a leaf node. The default implementation here simply writes
     * the node attribute as a string.
     * @param node
     */
    protected void writeLeafNode(BSPTree_Old<P> node) {
        write(String.valueOf(node.getAttribute()));
    }
}