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

import java.util.Objects;

import org.apache.commons.geometry.core.partition.BSPTree;
import org.apache.commons.geometry.core.partition.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.BSPTree_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;

/** Class containing utility methods for tests related to the
 * partition package.
 */
public class PartitionTestUtils {

    public static final double EPS = 1e-6;

    public static final DoublePrecisionContext PRECISION =
            new EpsilonDoublePrecisionContext(EPS);

    /**
     * Asserts that corresponding values in the given points are equal.
     * @param expected
     * @param actual
     */
    public static void assertPointsEqual(TestPoint2D expected, TestPoint2D actual) {
        String msg = "Expected points to equal " + expected + " but was " + actual + ";";
        Assert.assertEquals(msg, expected.getX(), actual.getX(), EPS);
        Assert.assertEquals(msg, expected.getY(), actual.getY(), EPS);
    }

    public static void assertSegmentsEqual(TestLineSegment expected, TestLineSegment actual) {
        String msg = "Expected line segment to equal " + expected + " but was " + actual;

        Assert.assertEquals(msg, expected.getStartPoint().getX(),
                actual.getStartPoint().getX(), EPS);
        Assert.assertEquals(msg, expected.getStartPoint().getY(),
                actual.getStartPoint().getY(), EPS);

        Assert.assertEquals(msg, expected.getEndPoint().getX(),
                actual.getEndPoint().getX(), EPS);
        Assert.assertEquals(msg, expected.getEndPoint().getY(),
                actual.getEndPoint().getY(), EPS);
    }

    public static <T> void printTree(TestBSPTree tree) {
        TestTreePrinter printer = new TestTreePrinter();
        String str = printer.writeAsString(tree);

        System.out.println(str);
    }

    public static class TestTreePrinter implements BSPTreeVisitor<TestPoint2D, String> {

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
        public String writeAsString(TestBSPTree tree) {
            output.delete(0, output.length());

            tree.getRoot().visit(this);

            return output.toString();
        }

        @Override
        public void visit(BSPTree.Node<TestPoint2D, String> node) {
            writeLinePrefix(node);

            if (node.isLeaf()) {
                visitLeafNode(node);
            }
            else {
                visitInternalNode(node);
            }
        }

        public void visitInternalNode(BSPTree.Node<TestPoint2D, String> node) {
            writeInternalNode(node);

            write("\n");

            ++depth;
        }

        public void visitLeafNode(BSPTree.Node<TestPoint2D, String> node) {
            writeLeafNode(node);

            write("\n");

            BSPTree.Node<TestPoint2D, String> cur = node;
            while (cur.isPlus()) {
                --depth;
                cur = cur.getParent();
            }
        }

        /** Writes the prefix for the current line in the output. This includes
         * the line indent, the plus/minus node indicator, and a string identifier
         * for the node itself.
         * @param node
         */
        protected void writeLinePrefix(BSPTree.Node<TestPoint2D, String>node) {
            for (int i=0; i<depth; ++i) {
                write(INDENT);
            }

            if (node.getParent() != null) {
                if (node.isMinus()) {
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
        protected String nodeIdString(BSPTree.Node<TestPoint2D, String> node) {
            return node.getClass().getSimpleName() + "@"  + Objects.hashCode(node);
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
        protected void writeInternalNode(BSPTree.Node<TestPoint2D, String> node) {
            write(node.toString());
        }

        /** Writes a leaf node. The default implementation here simply writes
         * the node attribute as a string.
         * @param node
         */
        protected void writeLeafNode(BSPTree.Node<TestPoint2D, String> node) {
            write(node.toString());
        }
    }
}
