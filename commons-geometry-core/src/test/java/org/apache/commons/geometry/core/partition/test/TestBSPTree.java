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
package org.apache.commons.geometry.core.partition.test;

import org.apache.commons.geometry.core.partition.AbstractBSPTree;

/** BSP Tree implementation class for testing purposes.
 */
public class TestBSPTree extends AbstractBSPTree<TestPoint2D, TestBSPTree.TestNode> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190225L;

    /** {@inheritDoc} */
    @Override
    public TestBSPTree copy() {
        return (TestBSPTree) super.copy();
    }

    /** {@inheritDoc} */
    @Override
    protected TestBSPTree createTree() {
        return new TestBSPTree();
    }

    /** {@inheritDoc} */
    @Override
    protected TestNode createNode() {
        return new TestNode(this);
    }

    /** BSP Tree node class for {@link TestBSPTree}.
     */
    public static class TestNode extends AbstractBSPTree.AbstractNode<TestPoint2D,TestNode> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190225L;

        public TestNode(AbstractBSPTree<TestPoint2D, TestNode> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected TestNode getSelf() {
            return this;
        }
    }
}
