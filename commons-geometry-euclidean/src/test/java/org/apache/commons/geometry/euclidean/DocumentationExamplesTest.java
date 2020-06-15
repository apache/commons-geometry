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
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.line.Ray3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LinecastPoint2D;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Ray;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Segment;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
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

        // create a binary space partitioning (BSP) tree representing the unit cube
        // centered on the origin
        RegionBSPTree3D region = Parallelepiped.builder(precision)
                .setPosition(Vector3D.ZERO)
                .build()
                .toTree();

        // create a rotated copy of the region
        RegionBSPTree3D copy = region.copy();
        copy.transform(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.25 * Math.PI));

        // compute the intersection of the regions, storing the result back into the caller
        // (the result could also have been placed into a third region)
        region.intersection(copy);

        // compute some properties of the intersection region
        double size = region.getSize(); // 0.8284271247461903
        Vector3D centroid = region.getCentroid(); // (0, 0, 0)

        // -----------
        Assert.assertEquals(0.8284271247461903, size, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, centroid, TEST_EPS);
    }

    @Test
    public void testPrecisionContextExample() {
        // create a precision context with an epsilon (aka, tolerance) value of 1e-3
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        // test for equality using the eq() method
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
    public void testEqualsVsEqExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        Vector2D v1 = Vector2D.of(1, 1); // (1.0, 1.0)
        Vector2D v2 = Vector2D.parse("(1, 1)"); // (1.0, 1.0)

        Vector2D v3 = Vector2D.of(Math.sqrt(2), 0).transform(
                AffineTransformMatrix2D.createRotation(0.25 * Math.PI)); // (1.0000000000000002, 1.0)

        v1.equals(v2); // true - exactly equal
        v1.equals(v3); // false - not exactly equal

        v1.eq(v3, precision); // true - approximately equal according to the given precision context

        // ---------------------
        Assert.assertEquals(v1, v2);
        Assert.assertNotEquals(v1, v3);
        Assert.assertTrue(v1.eq(v3, precision));
    }

    @Test
    public void testManualBSPTreeExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a tree representing an empty space (nothing "inside")
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // insert a "structural" cut, meaning a cut whose children have the same inside/outside
        // status as the parent; this will help keep our tree balanced and limit its overall height
        tree.getRoot().insertCut(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), precision),
                RegionCutRule.INHERIT);

        RegionBSPTree2D.RegionNode2D currentNode;

        // insert on the plus side of the structural diagonal cut
        currentNode = tree.getRoot().getPlus();

        currentNode.insertCut(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, precision));
        currentNode = currentNode.getMinus();

        currentNode.insertCut(Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_Y, precision));

        // insert on the plus side of the structural diagonal cut
        currentNode = tree.getRoot().getMinus();

        currentNode.insertCut(Lines.fromPointAndDirection(Vector2D.of(1, 1), Vector2D.Unit.MINUS_X, precision));
        currentNode = currentNode.getMinus();

        currentNode.insertCut(Lines.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.MINUS_Y, precision));

        // compute some tree properties
        int count = tree.count(); // number of nodes in the tree = 11
        int height = tree.height(); // height of the tree = 3
        double size = tree.getSize(); // size of the region = 1
        Vector2D centroid = tree.getCentroid(); // region centroid = (0.5, 0.5)

        // ---------
        Assert.assertEquals(1, size, TEST_EPS);
        Assert.assertEquals(11, count);
        Assert.assertEquals(3, height);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), centroid, TEST_EPS);
    }

    @Test
    public void testHyperplaneSubsetBSPTreeExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a tree representing an empty space (nothing "inside")
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // insert the hyperplane subsets
        tree.insert(Arrays.asList(
                    Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), precision),
                    Lines.segmentFromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), precision),
                    Lines.segmentFromPoints(Vector2D.of(1, 1), Vector2D.of(0, 1), precision),
                    Lines.segmentFromPoints(Vector2D.of(0, 1), Vector2D.ZERO, precision)
                ));

        // compute some tree properties
        int count = tree.count(); // number of nodes in the tree = 9
        int height = tree.height(); // height of the tree = 4
        double size = tree.getSize(); // size of the region = 1
        Vector2D centroid = tree.getCentroid(); // region centroid = (0.5, 0.5)

        // ---------
        Assert.assertEquals(1, size, TEST_EPS);
        Assert.assertEquals(9, count);
        Assert.assertEquals(4, height);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), centroid, TEST_EPS);
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
        Line a = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), precision);
        Line b = Lines.fromPointAndDirection(Vector2D.of(1, -1), Vector2D.Unit.PLUS_Y, precision);

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
        Segment segmentA = Lines.segmentFromPoints(Vector2D.of(3, -1), Vector2D.of(3, 1), precision);
        Segment segmentB = Lines.segmentFromPoints(Vector2D.of(-3, -1), Vector2D.of(-3, 1), precision);

        // create a ray to intersect against the segments
        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(2, 0), Vector2D.Unit.PLUS_X, precision);

        // compute some intersections
        Vector2D aIntersection = segmentA.intersection(ray); // (3, 0)
        Vector2D bIntersection = segmentB.intersection(ray); // null - no intersection

        // ----------------------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0), aIntersection, TEST_EPS);
        Assert.assertNull(bIntersection);
    }

    @Test
    public void testRegionBSPTree2DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a connected sequence of line segments forming the unit square
        LinePath path = LinePath.builder(precision)
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
        copy.transform(AffineTransformMatrix2D.createTranslation(Vector2D.of(0.5, 0.5)));

        // compute the union of the regions, storing the result back into the
        // first tree
        tree.union(copy);

        // compute some properties
        double size = tree.getSize(); // 1.75
        Vector2D centroid = tree.getCentroid(); // (0.75, 0.75)

        // get a line path representing the boundary; a list is returned since trees
        // can represent disjoint regions
        List<LinePath> boundaries = tree.getBoundaryPaths(); // size = 1

        // ----------------
        Assert.assertEquals(1.75, size, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.75, 0.75), centroid, TEST_EPS);
        Assert.assertEquals(1, boundaries.size());
    }

    @Test
    public void testLinecast2DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        Parallelogram box = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(2, 1), precision);

        LinecastPoint2D pt = box.linecastFirst(
                Lines.segmentFromPoints(Vector2D.of(1, 0.5), Vector2D.of(4, 0.5), precision));

        Vector2D intersection = pt.getPoint(); // (2.0, 0.5)
        Vector2D normal = pt.getNormal(); // (1.0, 0.0)

        // ----------------
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 0.5), intersection, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), normal, TEST_EPS);
    }

    @Test
    public void testPlaneIntersectionExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create two planes
        Plane a = Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, precision);
        Plane b = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1),
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

        // create the faces of a pyramid with a square base and its apex pointing along the
        // positive z axis
        Vector3D[] vertices = {
            Vector3D.Unit.PLUS_Z,
            Vector3D.of(0.5, 0.5, 0.0),
            Vector3D.of(0.5, -0.5, 0.0),
            Vector3D.of(-0.5, -0.5, 0.0),
            Vector3D.of(-0.5, 0.5, 0.0)
        };

        int[][] faceIndices = {
            {1, 0, 2},
            {2, 0, 3},
            {3, 0, 4},
            {4, 0, 1},
            {1, 2, 3, 4}
        };

        // convert the vertices and faces to convex polygons and use to construct a BSP tree
        List<ConvexPolygon3D> faces = Planes.indexedConvexPolygons(vertices, faceIndices, precision);
        RegionBSPTree3D tree = RegionBSPTree3D.from(faces);

        // split the region through its centroid along a diagonal of the base
        Plane cutter = Planes.fromPointAndNormal(tree.getCentroid(), Vector3D.Unit.from(1, 1, 0), precision);
        Split<RegionBSPTree3D> split = tree.split(cutter);

        // compute some properties for the minus side of the split and convert back to hyperplane subsets
        // (ie, boundary facets)
        RegionBSPTree3D minus = split.getMinus();

        double minusSize = minus.getSize(); // 1/6
        List<PlaneConvexSubset> minusBoundaries = minus.getBoundaries(); // size = 4

        // ---------------------
        Assert.assertEquals(1.0 / 6.0, minusSize, TEST_EPS);
        Assert.assertEquals(4, minusBoundaries.size());
    }

    @Test
    public void testLinecast3DExample() {
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-6);

        // create a BSP tree representing an axis-aligned cube with corners at (0, 0, 0) and (1, 1, 1)
        RegionBSPTree3D tree = Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(1, 1, 1), precision)
                .toTree();

        // create a ray starting on one side of the cube and pointing through its center
        Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.of(0.5, 0.5, -1), Vector3D.Unit.PLUS_Z, precision);

        // perform the linecast
        List<LinecastPoint3D> pts = tree.linecast(ray);

        // check the results
        int intersectionCount = pts.size(); // intersectionCount = 2
        Vector3D intersection = pts.get(0).getPoint(); // (0.5, 0.5, 0.0)
        Vector3D normal = pts.get(0).getNormal(); // (0.0, 0.0, -1.0)

        // ----------------
        Assert.assertEquals(2, intersectionCount);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0), intersection, TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), normal, TEST_EPS);
    }
}
