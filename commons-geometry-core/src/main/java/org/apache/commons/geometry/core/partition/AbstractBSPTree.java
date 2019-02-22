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

public abstract class AbstractBSPTree<P extends Point<P>, N extends AbstractBSPTree.Node<P, N>> implements BSPTree<P, N> {

    private final Function<AbstractBSPTree<P, N>, N> nodeFactory;

    private final N root;

    protected AbstractBSPTree(final Function<AbstractBSPTree<P, N>, N> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.apply(this);
    }

    @Override
    public N getRoot() {
        return root;
    }

    public N createNode() {
        return nodeFactory.apply(this);
    }

    public boolean cut(final N node, final Hyperplane<P> cutter) {
        // cut the hyperplane using all hyperplanes from this node
        // up
        ConvexSubHyperplane<P> cut = fitToCell(node, cutter.wholeHyperplane());
        if (cut == null || cut.isEmpty()) {
            node.setCut(null, null, null);
        }
        else {
            node.setCut(cut, createNode(), createNode());
        }

        return false;
    }

    protected ConvexSubHyperplane<P> fitToCell(final N node, final ConvexSubHyperplane<P> sub) {

        ConvexSubHyperplane<P> result = sub;

        N parentNode = node.getParent();
        N currentNode = node;

        while (parentNode != null && result != null) {
            SplitConvexSubHyperplane<P> split = result.split(parentNode.getCut().getHyperplane());

            result = currentNode.isPlus() ? split.getPlus() : split.getMinus();

            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return result;
    }

    protected abstract static class Node<T extends Point<T>, U extends Node<T, U>> implements BSPTree.Node<T> {

        private final AbstractBSPTree<T, U> tree;

        private U parent;

        private ConvexSubHyperplane<T> cut;

        private U plus;

        private U minus;

        public Node(final AbstractBSPTree<T, U> tree) {
            this.tree = tree;
        }

        @Override
        public U getParent() {
            return parent;
        }

        protected void setParent(U parent) {
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
        public ConvexSubHyperplane<T> getCut() {
            return cut;
        }

        @Override
        public U getPlus() {
            return plus;
        }

        @Override
        public U getMinus() {
            return minus;
        }

        @Override
        public U findNode(T p) {
            return null;
        }

        protected void setCut(ConvexSubHyperplane<T> cut, U plus, U minus) {
            this.cut = cut;

            if (plus != null) {
                plus.setParent(getSelf());
            }
            this.plus = plus;

            if (minus != null) {
                minus.setParent(getSelf());
            }
            this.minus = minus;
        }

        protected abstract U getSelf();
    }
}
