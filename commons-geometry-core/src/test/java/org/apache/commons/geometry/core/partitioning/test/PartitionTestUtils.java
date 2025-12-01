/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partitioning.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTree.Node;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

/** Class containing utility methods for tests related to the
 * partition package.
 */
public final class PartitionTestUtils {

    public static final double EPS = 1e-6;

    public static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);


    private PartitionTestUtils() {}

    /**
     * Asserts that corresponding values in the given points are equal.
     * @param expected
     * @param actual
     */
    public static void assertPointsEqual(final TestPoint2D expected, final TestPoint2D actual) {
        final String msg = "Expected points to equal " + expected + " but was " + actual + ";";
        Assertions.assertEquals(expected.getX(), actual.getX(), EPS, msg);
        Assertions.assertEquals(expected.getY(), actual.getY(), EPS, msg);
    }

    /** Assert that two line segments are equal using the default test epsilon.
     * @param expected
     * @param actual
     */
    public static void assertSegmentsEqual(final TestLineSegment expected, final TestLineSegment actual) {
        final String msg = "Expected line segment to equal " + expected + " but was " + actual;

        Assertions.assertEquals(expected.getStartPoint().getX(),
                actual.getStartPoint().getX(), EPS, msg);
        Assertions.assertEquals(expected.getStartPoint().getY(),
                actual.getStartPoint().getY(), EPS, msg);

        Assertions.assertEquals(expected.getEndPoint().getX(),
                actual.getEndPoint().getX(), EPS, msg);
        Assertions.assertEquals(expected.getEndPoint().getY(),
                actual.getEndPoint().getY(), EPS, msg);
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param region region to test
     * @param location expected location of all points
     * @param points points to test
     */
    public static void assertPointLocations(final Region<TestPoint2D> region, final RegionLocation location,
            final TestPoint2D... points) {
        assertPointLocations(region, location, Arrays.asList(points));
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param region region to test
     * @param location expected location of all points
     * @param points points to test
     */
    public static void assertPointLocations(final Region<TestPoint2D> region, final RegionLocation location,
            final List<TestPoint2D> points) {

        for (final TestPoint2D p : points) {
            Assertions.assertEquals(location, region.classify(p), "Unexpected location for point " + p);
        }
    }

    /** Assert that the given node is a consistent internal node.
     * @param node
     */
    public static void assertIsInternalNode(final Node<?, ?> node) {
        Assertions.assertNotNull(node.getCut());
        Assertions.assertNotNull(node.getMinus());
        Assertions.assertNotNull(node.getPlus());

        Assertions.assertTrue(node.isInternal());
        Assertions.assertFalse(node.isLeaf());
    }

    /** Assert that the given node is a consistent leaf node.
     * @param node
     */
    public static void assertIsLeafNode(final Node<?, ?> node) {
        Assertions.assertNull(node.getCut());
        Assertions.assertNull(node.getMinus());
        Assertions.assertNull(node.getPlus());

        Assertions.assertFalse(node.isInternal());
        Assertions.assertTrue(node.isLeaf());
    }

    /** Assert that the given tree for has a valid, consistent internal structure. This checks that all nodes
     * in the tree are owned by the tree, that the node depth values are correct, and the cut nodes have children
     * and non-cut nodes do not.
     * @param tree tree to check
     */
    public static <P extends Point<P>, N extends BSPTree.Node<P, N>> void assertTreeStructure(final BSPTree<P, N> tree) {
        assertTreeStructureRecursive(tree, tree.getRoot(), 0);
    }

    /** Recursive method to assert that a tree has a valid internal structure.
     * @param tree tree to check
     * @param node node to check
     * @param expectedDepth the expected depth of the node in the tree
     */
    private static <P extends Point<P>, N extends BSPTree.Node<P, N>> void assertTreeStructureRecursive(
            final BSPTree<P, N> tree, final BSPTree.Node<P, N> node, final int expectedDepth) {

        Assertions.assertSame(tree, node.getTree(), "Node has an incorrect owning tree");
        Assertions.assertEquals(node.depth(), expectedDepth, "Node has an incorrect depth property");

        if (node.getCut() == null) {
            final String msg = "Node without cut cannot have children";

            Assertions.assertNull(node.getMinus(), msg);
            Assertions.assertNull(node.getPlus(), msg);
        } else {
            final String msg = "Node with cut must have children";

            Assertions.assertNotNull(node.getMinus(), msg);
            Assertions.assertNotNull(node.getPlus(), msg);

            assertTreeStructureRecursive(tree, node.getPlus(), expectedDepth + 1);
            assertTreeStructureRecursive(tree, node.getMinus(), expectedDepth + 1);
        }
    }
}
