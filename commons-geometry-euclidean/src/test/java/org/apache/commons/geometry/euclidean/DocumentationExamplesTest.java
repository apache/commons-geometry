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
package org.apache.commons.geometry.euclidean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Boundaries3D;
import org.apache.commons.geometry.euclidean.threed.ConvexSubPlane;
import org.apache.commons.geometry.euclidean.threed.Line3D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Transform3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Polyline;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Segment;
import org.apache.commons.geometry.euclidean.twod.Transform2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

/** This class contains code listed as examples in the user guide and other documentation.
 * If any portion of this code changes, the corresponding examples in the documentation <em>must</em> be updated.
 */
public class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-12;

    @Test
    public void testIndexPageExample() {
        // construct a precision context to handle floating-point comparisons
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a binary space partitioning tree representing the unit cube
        // centered on the origin
        RegionBSPTree3D region = Boundaries3D.rect(
                Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5), precision)
                .toTree();

        // create a rotated copy of the region
        Transform3D rotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.25 * Math.PI);

        RegionBSPTree3D copy = region.copy();
        copy.transform(rotation);

        // compute the intersection of the regions, storing the result back into the caller
        // (the result could also have been placed into a third region)
        region.intersection(copy);

        // compute some properties of the intersection region
        double size = region.getSize(); // 0.8284271247461903
        Vector3D center = region.getBarycenter(); // (0, 0, 0)

        // -----------
        Assert.assertEquals(0.8284271247461903, size, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, center, TEST_EPS);
    }

    @Test
    public void testPrecisionContextExample() {
        // create a precision context with an epsilon (aka, tolerance) value of 1e-3
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        // test for equality
        precision.eq(1.0009, 1.0); // true; difference is less than epsilon
        precision.eq(1.002, 1.0); // false; difference is greater than epsilon

        // compare
        precision.compare(1.0009, 1.0); // 0
        precision.compare(1.002, 1.0); // 1

        // ------------------
        Assert.assertTrue(precision.eq(1.0009, 1.0));
        Assert.assertFalse(precision.eq(1.002, 1.0));

        Assert.assertEquals(0, precision.compare(1.0009, 1.0));
        Assert.assertEquals(1, precision.compare(1.002, 1.0));
    }

    @Test
    public void testManualBSPTreeExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a tree representing an empty space (nothing "inside")
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // get the root node
        RegionBSPTree2D.RegionNode2D currentNode = tree.getRoot();

        // cut each minus node with the next hyperplane in the shape
        currentNode.insertCut(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision));
        currentNode = currentNode.getMinus();

        currentNode.insertCut(Line.fromPointAndDirection(Vector2D.Unit.PLUS_X, Vector2D.of(-1, 1), precision));
        currentNode = currentNode.getMinus();

        currentNode.insertCut(Line.fromPointAndDirection(Vector2D.Unit.PLUS_Y, Vector2D.Unit.MINUS_Y, precision));
        currentNode = currentNode.getMinus();

        currentNode.isInside(); // true (node is inside)
        currentNode.getParent().getPlus().isInside(); // false (sibling node is outside)
        tree.getSize(); // size of the region = 0.5
        tree.count(); // number of nodes in the tree = 7

        // ---------
        Assert.assertTrue(currentNode.isInside());
        Assert.assertFalse(currentNode.getParent().getPlus().isInside());
        Assert.assertEquals(0.5, tree.getSize(), TEST_EPS);
        Assert.assertEquals(7, tree.count());
    }

    @Test
    public void testSubHyperplaneBSPTreeExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a tree representing an empty space (nothing "inside")
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // insert the subhyperplanes
        tree.insert(Arrays.asList(
                    Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision),
                    Segment.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y, precision),
                    Segment.fromPoints(Vector2D.Unit.PLUS_Y, Vector2D.ZERO, precision)
                ));

        tree.getSize(); // size of the region = 0.5
        tree.count(); // number of nodes in the tree = 7

        // ---------
        Assert.assertEquals(0.5, tree.getSize(), TEST_EPS);
        Assert.assertEquals(7, tree.count());
    }

    @Test
    public void testIntervalExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a closed interval and a half-open interval with a min but no max
        Interval closed = Interval.of(1, 2, precision);
        Interval halfOpen = Interval.min(1, precision);

        // classify some points against the intervals
        closed.contains(0.0); // false
        halfOpen.contains(Vector1D.ZERO); // false

        RegionLocation closedOneLoc = closed.classify(Vector1D.of(1)); // RegionLocation.BOUNDARY
        RegionLocation halfOpenOneLoc = halfOpen.classify(Vector1D.of(1)); // RegionLocation.BOUNDARY

        RegionLocation closedThreeLoc = closed.classify(3.0); // RegionLocation.OUTSIDE
        RegionLocation halfOpenThreeLoc = halfOpen.classify(3.0); // RegionLocation.INSIDE

        // --------------------
        Assert.assertFalse(closed.contains(0));
        Assert.assertFalse(halfOpen.contains(0));

        Assert.assertEquals(RegionLocation.BOUNDARY, closedOneLoc);
        Assert.assertEquals(RegionLocation.BOUNDARY, halfOpenOneLoc);

        Assert.assertEquals(RegionLocation.OUTSIDE, closedThreeLoc);
        Assert.assertEquals(RegionLocation.INSIDE, halfOpenThreeLoc);
    }

    @Test
    public void testRegionBSPTree1DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // build a bsp tree from the union of several intervals
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        tree.add(Interval.of(1, 2, precision));
        tree.add(Interval.of(1.5, 3, precision));
        tree.add(Interval.of(-1, -2, precision));

        // compute the size;
        double size = tree.getSize(); // 3

        // convert back to intervals
        List<Interval> intervals = tree.toIntervals(); // size = 2

        // ----------------------
        Assert.assertEquals(3, size, TEST_EPS);
        Assert.assertEquals(2, intervals.size());
    }

    @Test
    public void testLineIntersectionExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create some lines
        Line a = Line.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), precision);
        Line b = Line.fromPointAndDirection(Vector2D.of(1, -1), Vector2D.Unit.PLUS_Y, precision);

        // compute the intersection and angles
        Vector2D intersection = a.intersection(b); // (1, 1)
        double angleAtoB = a.angle(b); // pi/4
        double angleBtoA = b.angle(a); // -pi/4

        // ----------------------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), intersection, TEST_EPS);
        Assert.assertEquals(0.25 * Math.PI, angleAtoB, TEST_EPS);
        Assert.assertEquals(-0.25 * Math.PI, angleBtoA, TEST_EPS);
    }

    @Test
    public void testLineSegmentIntersectionExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create some line segments
        Segment closedPosX = Segment.fromPoints(Vector2D.of(3, -1), Vector2D.of(3, 1), precision);
        Segment closedNegX = Segment.fromPoints(Vector2D.of(-3, -1), Vector2D.of(-3, 1), precision);
        Segment halfOpen = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision)
                .segmentFrom(Vector2D.of(2, 0));

        // compute some intersections
        Vector2D posXIntersection = closedPosX.intersection(halfOpen); // (3, 0)
        Vector2D negXIntersection = closedNegX.intersection(halfOpen); // null - no intersection

        // ----------------------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0), posXIntersection, TEST_EPS);
        Assert.assertNull(negXIntersection);
    }

    @Test
    public void testRegionBSPTree2DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a connected sequence of line segments forming the unit square
        Polyline path = Polyline.builder(precision)
                .append(Vector2D.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.of(1, 1))
                .append(Vector2D.Unit.PLUS_Y)
                .build(true); // build the path, ending it with the starting point

        // convert to a tree
        RegionBSPTree2D tree = path.toTree();

        // copy the tree
        RegionBSPTree2D copy = tree.copy();

        // translate the copy
        Vector2D translation = Vector2D.of(0.5, 0.5);
        copy.transform(Transform2D.from(v -> v.add(translation)));

        // compute the union of the regions, storing the result back into the
        // first tree
        tree.union(copy);

        // compute some properties
        double size = tree.getSize(); // 1.75
        Vector2D center = tree.getBarycenter(); // (0.75, 0.75)

        // get a polyline representing the boundary; a list is returned since trees
        // can represent disjoint regions
        List<Polyline> boundaries = tree.getBoundaryPaths(); // size = 1

        // ----------------
        Assert.assertEquals(1.75, size, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.75, 0.75), center, TEST_EPS);
        Assert.assertEquals(1, boundaries.size());
    }

    @Test
    public void testPlaneIntersectionExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create two planes
        Plane a = Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, precision);
        Plane b = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
                Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y, precision);

        // compute the intersection
        Line3D line = a.intersection(b);

        Vector3D dir = line.getDirection(); // (0, 1, 0)

        // ----------------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, dir, TEST_EPS);
    }

    @Test
    public void testTransform3DExample() {
        List<Vector3D> inputPts = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.PLUS_Z);

        // create a 4x4 transform matrix and quaternion rotation
        AffineTransformMatrix3D mat = AffineTransformMatrix3D.createScale(2)
                .translate(Vector3D.of(1, 2, 3));

        QuaternionRotation rot = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z,
                PlaneAngleRadians.PI_OVER_TWO);

        // transform the input points
        List<Vector3D> matOutput = inputPts.stream()
                .map(mat)
                .collect(Collectors.toList()); // [(1, 2, 3), (3, 2, 3), (1, 4, 3), (1, 2, 5)]

        List<Vector3D> rotOutput = inputPts.stream()
                .map(rot)
                .collect(Collectors.toList()); // [(0, 0, 0), (0, 1, 0), (-1, 0, 0), (0, 0, 1)]

        // ----------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), matOutput.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 3), matOutput.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 4, 3), matOutput.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 5), matOutput.get(3), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), rotOutput.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), rotOutput.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0), rotOutput.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), rotOutput.get(3), TEST_EPS);
    }

    @Test
    public void testRegionBSPTree3DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create the faces of a pyrmaid with a square base and its apex pointing along the
        // positive z axis
        Vector3D a1 = Vector3D.Unit.PLUS_Z;
        Vector3D b1 = Vector3D.of(0.5, 0.5, 0.0);
        Vector3D b2 = Vector3D.of(0.5, -0.5, 0.0);
        Vector3D b3 = Vector3D.of(-0.5, -0.5, 0.0);
        Vector3D b4 = Vector3D.of(-0.5, 0.5, 0.0);

        Vector3D[][] faces = {
            {b1, a1, b2},
            {b2, a1, b3},
            {b3, a1, b4},
            {b4, a1, b1},
            {b1, b2, b3, b4}
        };

        // convert the faces to convex sub planes and insert into a bsp tree
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        Arrays.stream(faces)
            .map(vertices -> ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices), precision))
            .forEach(tree::insert);

        // split the region through its barycenter along a diagonal of the base
        Plane cutter = Plane.fromPointAndNormal(tree.getBarycenter(), Vector3D.Unit.from(1, 1, 0), precision);
        Split<RegionBSPTree3D> split = tree.split(cutter);

        // compute some properties for the minus side of the split and convert back to subhyperplanes
        // (ie, facets)
        RegionBSPTree3D minus = split.getMinus();

        double minusSize = minus.getSize(); // 1/6
        List<ConvexSubPlane> minusFacets = minus.getBoundaries(); // size = 4

        // ---------------------
        Assert.assertEquals(1.0 / 6.0, minusSize, TEST_EPS);
        Assert.assertEquals(4, minusFacets.size());
    }
}
