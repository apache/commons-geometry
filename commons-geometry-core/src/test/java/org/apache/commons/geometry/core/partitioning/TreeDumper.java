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

import java.util.Formatter;
import java.util.Locale;

import org.apache.commons.geometry.core.Space;
import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.Hyperplane;

/** Dumping visitor.
 * @param <S> Type of the space.
 */
public abstract class TreeDumper<S extends Space> implements BSPTreeVisitor<S> {
    /** Builder for the string representation of the dumped tree. */
    private final StringBuilder dump;

    /** Formatter for strings. */
    private final Formatter formatter;

    /** Current indentation prefix. */
    private String prefix;

    /** Simple constructor.
     * @param type type of the region to dump
     * @param tolerance tolerance of the region
     */
    public TreeDumper(final String type, final double tolerance) {
        this.dump      = new StringBuilder();
        this.formatter = new Formatter(dump, Locale.US);
        this.prefix    = "";
        formatter.format("%s%n", type);
        formatter.format("tolerance %22.15e%n", tolerance);
    }

    /** Get the string representation of the tree.
     * @return string representation of the tree.
     */
    public String getDump() {
        return dump.toString();
    }

    /** Get the formatter to use.
     * @return formatter to use
     */
    protected Formatter getFormatter() {
        return formatter;
    }

    /** Format a string representation of the hyperplane underlying a cut sub-hyperplane.
     * @param hyperplane hyperplane to format
     */
    protected abstract void formatHyperplane(Hyperplane<S> hyperplane);

    /** {@inheritDoc} */
    @Override
    public Order visitOrder(final BSPTree<S> node) {
        return Order.SUB_MINUS_PLUS;
    }

    /** {@inheritDoc} */
    @Override
    public void visitInternalNode(final BSPTree<S> node) {
        formatter.format("%s %s internal ", prefix, type(node));
        formatHyperplane(node.getCut().getHyperplane());
        formatter.format("%n");
        prefix = prefix + "  ";
    }

    /** {@inheritDoc} */
    @Override
    public void visitLeafNode(final BSPTree<S> node) {
        formatter.format("%s %s leaf %s%n",
                         prefix, type(node), node.getAttribute());
        for (BSPTree<S> n = node;
             n.getParent() != null && n == n.getParent().getPlus();
             n = n.getParent()) {
            prefix = prefix.substring(0, prefix.length() - 2);
        }
    }

    /** Get the type of the node.
     * @param node node to check
     * @return "plus " or "minus" depending on the node being the plus or minus
     * child of its parent ("plus " is arbitrarily returned for the root node)
     */
    private String type(final BSPTree<S> node) {
        return (node.getParent() != null && node == node.getParent().getMinus()) ? "minus" : "plus ";
    }
}
