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

import org.apache.commons.geometry.core.Point;

/** Simple {@link BSPTree} implementation allowing arbitrary values to be
 * associated with each node.
 */
public class AttributeBSPTree<P extends Point<P>, T> extends AbstractBSPTree<P, AttributeBSPTree.AttributeNode<P, T>> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190306L;

    protected AttributeBSPTree() {
        super(AttributeNode<P, T>::new);
    }

    /** {@inheritDoc} */
    @Override
    protected AttributeBSPTree<P, T> createTree() {
        return new AttributeBSPTree<P, T>();
    }

    public static class AttributeNode<P extends Point<P>, T> extends AbstractBSPTree.AbstractNode<P, AttributeNode<P, T>> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** The node attribute */
        private T attribute;

        protected AttributeNode(AbstractBSPTree<P, AttributeNode<P, T>> tree) {
            super(tree);
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
        public void setAttribute(T attribute) {
            this.attribute = attribute;
        }

        /** Set the attribute for this node. The node is returned.
         * @param attribute attribute to set for the node
         * @return the node instance
         */
        public AttributeNode<P, T> attr(final T attribute) {
            setAttribute(attribute);

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
