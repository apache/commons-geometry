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
package org.apache.commons.geometry.core.partition;

import java.util.function.Function;

import org.apache.commons.geometry.core.Point;

public class AbstractBSPTree<P extends Point<P>, T> implements BSPTree<P, T> {

    public static interface AbstractBSPTreeNodeFactory<P extends Point<P>, T>
        extends Function<AbstractBSPTree<P, T>, SimpleBSPTreeNode<P, T>> {
    }

    private final AbstractBSPTreeNodeFactory<P, T> nodeFactory;

    private final SimpleBSPTreeNode<P, T> root;

    protected AbstractBSPTree()
    {
        this(SimpleBSPTreeNode::new);
    }

    protected AbstractBSPTree(final AbstractBSPTreeNodeFactory<P, T> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.apply(this);
    }

    @Override
    public BSPTreeNode<P, T> getRoot() {
        return getRootNode();
    }

    @Override
    public void visit(BSPTreeVisitor<P, T> visitor) {
        visit(getRoot(), visitor);
    }

    @Override
    public BSPTreeNode<P, T> findNode(P pt) {
        return findNode(getRootNode(), pt);
    }

    protected SimpleBSPTreeNode<P, T> getRootNode() {
        return root;
    }

    protected SimpleBSPTreeNode<P, T> createNode() {
        return nodeFactory.apply(this);
    }

    protected SimpleBSPTreeNode<P, T> findNode(SimpleBSPTreeNode<P, T> start, P pt) {
        Hyperplane<P> hyper = start.getCutHyperplane();
        if (hyper != null) {
            Side side = hyper.classify(pt);

            if (side == Side.PLUS) {
                return findNode(start.getPlus(), pt);
            }
            else if (side == Side.MINUS) {
                return findNode(start.getMinus(), pt);
            }
        }
        return start;
    }

    protected void visit(final BSPTreeNode<P, T> node, BSPTreeVisitor<P, T> visitor) {
        // simple recursive implementation of this; we'll probably
        // want to change this later
        if (node != null) {
            visitor.visit(node);

            if (!node.isLeaf()) {
                visit(node.getMinus(), visitor);
                visit(node.getPlus(), visitor);
            }
        }
    }

    protected boolean insertCut(final SimpleBSPTreeNode<P, T> node, final Hyperplane<P> cutter) {
        // cut the hyperplane using all hyperplanes from this node up
        // to the root
        ConvexSubHyperplane<P> cut = fitToCell(node, cutter.wholeHyperplane());
        if (cut == null || cut.isEmpty()) {
            // insertion failed; the node was not cut
            node.setCut(null, null, null);
            return false;
        }

        node.setCut(cut, createNode(), createNode());
        return true;
    }

    protected ConvexSubHyperplane<P> fitToCell(final SimpleBSPTreeNode<P, T> node, final ConvexSubHyperplane<P> sub) {

        ConvexSubHyperplane<P> result = sub;

        SimpleBSPTreeNode<P, T> parentNode = node.getParent();
        SimpleBSPTreeNode<P, T> currentNode = node;

        while (parentNode != null && result != null) {
            SplitConvexSubHyperplane<P> split = result.split(parentNode.getCut().getHyperplane());

            result = currentNode.isPlus() ? split.getPlus() : split.getMinus();

            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return result;
    }

    public static class SimpleBSPTreeNode<P extends Point<P>, T> implements BSPTreeNode<P, T> {

        private final AbstractBSPTree<P, T> tree;

        private SimpleBSPTreeNode<P, T> parent;

        private ConvexSubHyperplane<P> cut;

        private SimpleBSPTreeNode<P, T> plus;

        private SimpleBSPTreeNode<P, T> minus;

        private T attribute;

        public SimpleBSPTreeNode(final AbstractBSPTree<P, T> tree) {
            this.tree = tree;
        }

        @Override
        public AbstractBSPTree<P, T> getTree() {
            return tree;
        }

        @Override
        public SimpleBSPTreeNode<P, T> getParent() {
            return parent;
        }

        protected void setParent(SimpleBSPTreeNode<P, T> parent) {
            this.parent = parent;
        }

        @Override
        public boolean isLeaf() {
            return cut == null;
        }

        @Override
        public boolean isPlus() {
            return parent != null && parent.getPlus() == this;
        }

        @Override
        public boolean isMinus() {
            return parent != null && parent.getMinus() == this;
        }

        @Override
        public ConvexSubHyperplane<P> getCut() {
            return cut;
        }

        protected Hyperplane<P> getCutHyperplane() {
            return (cut != null) ? cut.getHyperplane() : null;
        }

        @Override
        public SimpleBSPTreeNode<P, T> getPlus() {
            return plus;
        }

        @Override
        public SimpleBSPTreeNode<P, T> getMinus() {
            return minus;
        }

        @Override
        public SimpleBSPTreeNode<P, T> findNode(P p) {
            return null;
        }

        @Override
        public void visit(BSPTreeVisitor<P, T> visitor) {
            tree.visit(this, visitor);
        }

        @Override
        public T getAttribute() {
            return attribute;
        }

        @Override
        public void setAttribute(T attribute) {
            this.attribute = attribute;
        }

        @Override
        public boolean insertCut(Hyperplane<P> cutter) {
            return tree.insertCut(this, cutter);
        }

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

        protected void setCut(ConvexSubHyperplane<P> cut, SimpleBSPTreeNode<P, T> plus, SimpleBSPTreeNode<P, T> minus) {
            this.cut = cut;

            if (plus != null) {
                plus.setParent(this);
            }
            this.plus = plus;

            if (minus != null) {
                minus.setParent(this);
            }
            this.minus = minus;
        }
    }
}
