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
package org.apache.commons.geometry.core.partitioning.test;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;

/** Simple {@link org.apache.commons.geometry.core.partitioning.bsp.BSPTree} implementation allowing arbitrary values to be
 * associated with each node.
 * @param <P> Point implementation type
 * @param <T> Tree node attribute type
 */
public class AttributeBSPTree<P extends Point<P>, T>
    extends AbstractBSPTree<P, AttributeBSPTree.AttributeNode<P, T>> {
    /** The initial attribute value to use for newly created nodes. */
    private final T initialNodeAttribute;

    /** Create a new tree instance. New nodes in the tree are given an attribute
     * of null.
     */
    public AttributeBSPTree() {
        this(null);
    }

    /** Create a new tree instance. New nodes in the tree are assigned the given
     * initial attribute value.
     * @param initialNodeAttribute The attribute value to assign to newly created nodes.
     */
    public AttributeBSPTree(final T initialNodeAttribute) {
        this.initialNodeAttribute = initialNodeAttribute;

        this.getRoot().setAttribute(initialNodeAttribute);
    }

    /** {@inheritDoc} */
    @Override
    protected AttributeNode<P, T> createNode() {
        return new AttributeNode<>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void copyNodeProperties(final AttributeNode<P, T> src, final AttributeNode<P, T> dst) {
        dst.setAttribute(src.getAttribute());
    }

    /** {@link org.apache.commons.geometry.core.partitioning.bsp.BSPTree.Node} implementation for use with {@link AttributeBSPTree}s.
     * @param <P> Point implementation type
     * @param <T> Tree node attribute type
     */
    public static class AttributeNode<P extends Point<P>, T>
        extends AbstractBSPTree.AbstractNode<P, AttributeNode<P, T>> {
        /** The node attribute. */
        private T attribute;

        /** Simple constructor.
         * @param tree the owning tree; this must be an instance of {@link AttributeBSPTree}
         */
        protected AttributeNode(final AbstractBSPTree<P, AttributeNode<P, T>> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        public AttributeBSPTree<P, T> getTree() {
            // cast to our parent tree type
            return (AttributeBSPTree<P, T>) super.getTree();
        }

        /** Cut this node with the given hyperplane. If the hyperplane intersects the node's region,
         * then the node becomes an internal node with two child leaf node. If the hyperplane does
         * not intersect the node's region, then the node is made a leaf node. The same node is
         * returned, regardless of the outcome of the cut operation.
         * @param cutter hyperplane to cut the node with
         * @return this node
         */
        public AttributeNode<P, T> cut(final Hyperplane<P> cutter) {
            final AttributeBSPTree<P, T> tree = getTree();

            tree.cutNode(getSelf(), cutter, root -> {
                root.getMinus().setAttribute(tree.initialNodeAttribute);
                root.getPlus().setAttribute(tree.initialNodeAttribute);
            });

            return this;
        }

        /** Get the attribute associated with this node.
         * @return the attribute associated with this node
         */
        public T getAttribute() {
            return attribute;
        }

        /** Set the attribute associated with this node.
         * @param attribute the attribute to associate with this node
         */
        public void setAttribute(final T attribute) {
            this.attribute = attribute;
        }

        /** Set the attribute for this node. The node is returned.
         * @param attributeValue attribute to set for the node
         * @return the node instance
         */
        public AttributeNode<P, T> attr(final T attributeValue) {
            setAttribute(attributeValue);

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append(", attribute= ")
                .append(attribute)
                .append("]");

            return sb.toString();
        }

        /** {@inheritDoc} */
        @Override
        protected AttributeNode<P, T> getSelf() {
            return this;
        }
    }
}
