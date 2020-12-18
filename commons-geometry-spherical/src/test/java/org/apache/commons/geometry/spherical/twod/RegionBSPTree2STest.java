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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.twod.RegionBSPTree2S.RegionNode2S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegionBSPTree2STest {

    private static final double TEST_EPS = 1e-10;

    // alternative epsilon value for checking the centroids of complex
    // or very small regions
    private static final double CENTROID_EPS = 1e-5;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle EQUATOR = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle X_MERIDIAN = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle Y_MERIDIAN = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_booleanArg_true() {
        // act
        final RegionBSPTree2S tree = new RegionBSPTree2S(true);

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_booleanArg_false() {
        // act
        final RegionBSPTree2S tree = new RegionBSPTree2S(false);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_default() {
        // act
        final RegionBSPTree2S tree = new RegionBSPTree2S();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.full();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testFrom_boundaries_noBoundaries() {
        // act/assert
        Assertions.assertTrue(RegionBSPTree2S.from(Collections.emptyList()).isEmpty());
        Assertions.assertTrue(RegionBSPTree2S.from(Collections.emptyList(), true).isFull());
        Assertions.assertTrue(RegionBSPTree2S.from(Collections.emptyList(), false).isEmpty());
    }

    @Test
    public void testFrom_boundaries() {
        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.from(Arrays.asList(
                    EQUATOR.arc(Point2S.PLUS_I, Point2S.PLUS_J),
                    X_MERIDIAN.arc(Point2S.PLUS_K, Point2S.PLUS_I),
                    Y_MERIDIAN.arc(Point2S.PLUS_J, Point2S.PLUS_K)
                ));

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.of(1, 0.5));
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(-1, 0.5), Point2S.of(Math.PI, 0.5 * Math.PI));
    }

    @Test
    public void testFrom_boundaries_fullIsTrue() {
        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.from(Arrays.asList(
                    EQUATOR.arc(Point2S.PLUS_I, Point2S.PLUS_J),
                    X_MERIDIAN.arc(Point2S.PLUS_K, Point2S.PLUS_I),
                    Y_MERIDIAN.arc(Point2S.PLUS_J, Point2S.PLUS_K)
                ), true);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.of(1, 0.5));
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(-1, 0.5), Point2S.of(Math.PI, 0.5 * Math.PI));
    }

    @Test
    public void testCopy() {
        // arrange
        final RegionBSPTree2S tree = new RegionBSPTree2S(true);
        tree.getRoot().cut(EQUATOR);

        // act
        final RegionBSPTree2S copy = tree.copy();

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertEquals(3, copy.count());
    }

    @Test
    public void testBoundaries() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        final List<GreatArc> arcs = new ArrayList<>();
        tree.boundaries().forEach(arcs::add);

        // assert
        Assertions.assertEquals(3, arcs.size());
    }

    @Test
    public void testGetBoundaries() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        final List<GreatArc> arcs = tree.getBoundaries();

        // assert
        Assertions.assertEquals(3, arcs.size());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        final List<GreatArc> arcs = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(3, arcs.size());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act
        final List<GreatArc> arcs = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, arcs.size());
    }

    @Test
    public void testToList_fullAndEmpty() {
        // act/assert
        Assertions.assertEquals(0, RegionBSPTree2S.full().toList().count());
        Assertions.assertEquals(0, RegionBSPTree2S.empty().toList().count());
    }

    @Test
    public void testToList() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        final BoundaryList2S list = tree.toList();

        // assert
        Assertions.assertEquals(3, list.count());
        Assertions.assertEquals(0.5 * Math.PI, list.toTree().getSize(), TEST_EPS);
    }

    @Test
    public void testToTree_returnsSameInstance() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act/assert
        Assertions.assertSame(tree, tree.toTree());
    }

    @Test
    public void testGetBoundaryPaths_cachesResult() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        final List<GreatArcPath> a = tree.getBoundaryPaths();
        final List<GreatArcPath> b = tree.getBoundaryPaths();

        // assert
        Assertions.assertSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_recomputesResultOnChange() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(EQUATOR.span());

        // act
        final List<GreatArcPath> a = tree.getBoundaryPaths();
        tree.insert(X_MERIDIAN.span());
        final List<GreatArcPath> b = tree.getBoundaryPaths();

        // assert
        Assertions.assertNotSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_isUnmodifiable() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(EQUATOR.span());

        // act/assert
        Assertions.assertThrows(UnsupportedOperationException.class, () -> tree.getBoundaryPaths().add(GreatArcPath.empty()));
    }

    @Test
    public void testToConvex_full() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.full();

        // act
        final List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act
        final List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_doubleLune() {
        // arrange
        final RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // act
        final List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assertions.assertEquals(2, result.size());

        final double size = result.stream().mapToDouble(ConvexArea2S::getSize).sum();
        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, size, TEST_EPS);
    }

    @Test
    public void testToConvex_doubleLune_complement() {
        // arrange
        final RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // act
        final List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assertions.assertEquals(2, result.size());

        final double size = result.stream().mapToDouble(ConvexArea2S::getSize).sum();
        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, size, TEST_EPS);
    }

    @Test
    public void testProject() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(EQUATOR.arc(0, PlaneAngleRadians.PI));
        tree.insert(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0));

        // act/assert
        SphericalTestUtils.assertPointsEq(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                tree.project(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO + 0.2)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K,
                tree.project(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, 0.2)), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I,
                tree.project(Point2S.of(-0.5, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I,
                tree.project(Point2S.of(PlaneAngleRadians.PI + 0.5, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);

        final Point2S centroid = tree.getCentroid();
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K,
                tree.project(centroid.slerp(Point2S.PLUS_K, 1e-10)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J,
                tree.project(centroid.slerp(Point2S.PLUS_J, 1e-10)), TEST_EPS);
    }

    @Test
    public void testProject_noBoundaries() {
        // act/assert
        Assertions.assertNull(RegionBSPTree2S.empty().project(Point2S.PLUS_I));
        Assertions.assertNull(RegionBSPTree2S.full().project(Point2S.PLUS_I));
    }

    @Test
    public void testGeometricProperties_full() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.full();

        // act/assert
        Assertions.assertEquals(4 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assertions.assertEquals(0, tree.getBoundaries().size());
        Assertions.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_empty() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act/assert
        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assertions.assertEquals(0, tree.getBoundaries().size());
        Assertions.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_halfSpace() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.full();
        tree.getRoot().cut(EQUATOR);

        // act/assert
        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, tree.getBoundarySize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, tree.getCentroid(), TEST_EPS);

        checkCentroidConsistency(tree);

        final List<GreatArc> arcs = tree.getBoundaries();
        Assertions.assertEquals(1, arcs.size());

        final GreatArc arc = arcs.get(0);
        Assertions.assertSame(EQUATOR, arc.getCircle());
        Assertions.assertNull(arc.getStartPoint());
        Assertions.assertNull(arc.getEndPoint());

        final List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final GreatArcPath path = paths.get(0);
        Assertions.assertEquals(1, path.getArcs().size());
        Assertions.assertTrue(path.getArcs().get(0).isFull());
    }

    @Test
    public void testGeometricProperties_doubleLune() {
        // act
        final RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // assert
        Assertions.assertEquals(2 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(4 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());

        final List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(2, paths.size());

        assertPath(paths.get(0), Point2S.PLUS_I, Point2S.MINUS_I, Point2S.PLUS_I);
        assertPath(paths.get(1), Point2S.PLUS_I, Point2S.MINUS_I, Point2S.PLUS_I);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.of(0.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI),
                Point2S.of(1.5 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(0.5 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI),
                Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));
    }

    @Test
    public void testGeometricProperties_quadrant() {
        // act
        final RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .appendVertices(Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J)
                .close()
                .toTree();

        // assert
        Assertions.assertEquals(0.5 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);

        final Point2S center = Point2S.from(Point2S.MINUS_K.getVector()
                .add(Point2S.PLUS_I.getVector())
                .add(Point2S.MINUS_J.getVector()));
        SphericalTestUtils.assertPointsEq(center, tree.getCentroid(), TEST_EPS);

        checkCentroidConsistency(tree);

        final List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        assertPath(paths.get(0), Point2S.MINUS_J, Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.of(1.75 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.PLUS_J, Point2S.PLUS_K, Point2S.MINUS_I);
    }

    @Test
    public void testGeometricProperties_quadrant_complement() {
        // arrange
        final RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .appendVertices(Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J)
                .close()
                .toTree();

        // act
        tree.complement();

        // assert
        Assertions.assertEquals(3.5 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);

        final Point2S center = Point2S.from(Point2S.MINUS_K.getVector()
                .add(Point2S.PLUS_I.getVector())
                .add(Point2S.MINUS_J.getVector()));
        SphericalTestUtils.assertPointsEq(center.antipodal(), tree.getCentroid(), TEST_EPS);

        checkCentroidConsistency(tree);

        final List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        assertPath(paths.get(0), Point2S.MINUS_J, Point2S.PLUS_I, Point2S.MINUS_K, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(1.75 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.PLUS_J, Point2S.PLUS_K, Point2S.MINUS_I);
    }

    @Test
    public void testGeometricProperties_polygonWithHole() {
        // arrange
        final Point2S center = Point2S.of(0.5, 2);

        final double outerRadius = 1;
        final double innerRadius = 0.5;

        final RegionBSPTree2S outer = buildDiamond(center, outerRadius);
        final RegionBSPTree2S inner = buildDiamond(center, innerRadius);

        // rotate the inner diamond a quarter turn to become a square
        inner.transform(Transform2S.createRotation(center, 0.25 * Math.PI));

        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.difference(outer, inner);

        // assert
        final double area = 4 * (rightTriangleArea(outerRadius, outerRadius) - rightTriangleArea(innerRadius, innerRadius));
        Assertions.assertEquals(area, tree.getSize(), TEST_EPS);

        final double outerSideLength = sphericalHypot(outerRadius, outerRadius);
        final double innerSideLength = sphericalHypot(innerRadius, innerRadius);
        final double boundarySize = 4 * (outerSideLength + innerSideLength);
        Assertions.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(center, tree.getCentroid(), TEST_EPS);
        checkCentroidConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, center);
    }

    @Test
    public void testGeometricProperties_polygonWithHole_small() {
        // arrange
        final Point2S center = Point2S.of(0.5, 2);

        final double outerRadius = 1e-5;
        final double innerRadius = 1e-7;

        final RegionBSPTree2S outer = buildDiamond(center, outerRadius);
        final RegionBSPTree2S inner = buildDiamond(center, innerRadius);

        // rotate the inner diamond a quarter turn to become a square
        inner.transform(Transform2S.createRotation(center, 0.25 * Math.PI));

        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.difference(outer, inner);

        // assert

        // use Euclidean approximations of the area and boundary size since those will be more accurate
        // at these sizes
        final double area = (2 * outerRadius * outerRadius) - (2 * innerRadius * innerRadius);
        Assertions.assertEquals(area, tree.getSize(), TEST_EPS);

        final double outerSideLength = Math.hypot(outerRadius, outerRadius);
        final double innerSideLength = Math.hypot(innerRadius, innerRadius);
        final double boundarySize = 4 * (outerSideLength + innerSideLength);
        Assertions.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(center, tree.getCentroid(), TEST_EPS);
        checkCentroidConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, center);
    }

    @Test
    public void testGeometricProperties_polygonWithHole_complex() {
        // arrange
        final Point2S center = Point2S.of(0.5, 2);

        final double outerRadius = 2;
        final double midRadius = 1;
        final double innerRadius = 0.5;

        final RegionBSPTree2S outer = buildDiamond(center, outerRadius);
        final RegionBSPTree2S mid = buildDiamond(center, midRadius);
        final RegionBSPTree2S inner = buildDiamond(center, innerRadius);

        // rotate the middle diamond a quarter turn to become a square
        mid.transform(Transform2S.createRotation(center, 0.25 * Math.PI));

        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.difference(outer, mid);
        tree.union(inner);
        tree.complement();

        // assert
        // compute the area, adjusting the first computation for the fact that the triangles comprising the
        // outer diamond have lengths greater than pi/2
        final double nonComplementedArea = 4 * ((PlaneAngleRadians.PI - rightTriangleArea(outerRadius, outerRadius) -
                rightTriangleArea(midRadius, midRadius) + rightTriangleArea(innerRadius, innerRadius)));
        final double area = (4 * PlaneAngleRadians.PI) - nonComplementedArea;
        Assertions.assertEquals(area, tree.getSize(), TEST_EPS);

        final double outerSideLength = sphericalHypot(outerRadius, outerRadius);
        final double midSideLength = sphericalHypot(midRadius, midRadius);
        final double innerSideLength = sphericalHypot(innerRadius, innerRadius);
        final double boundarySize = 4 * (outerSideLength + midSideLength + innerSideLength);
        Assertions.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(center.antipodal(), tree.getCentroid(), TEST_EPS);
        checkCentroidConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, center);
    }

    @Test
    public void testGeometricProperties_smallRightTriangle() {
        // arrange
        final double azOffset = 1e-5;
        final double polarOffset = 1e-6;

        final double minAz = 0;
        final double maxAz = minAz + azOffset;
        final double maxPolar = PlaneAngleRadians.PI_OVER_TWO;
        final double minPolar = maxPolar - polarOffset;

        final Point2S p0 = Point2S.of(minAz, maxPolar);
        final Point2S p1 = Point2S.of(maxAz, maxPolar);
        final Point2S p2 = Point2S.of(maxAz, minPolar);

        // act
        final RegionBSPTree2S tree = GreatArcPath.fromVertexLoop(Arrays.asList(p0, p1, p2), TEST_PRECISION)
                .toTree();

        // assert

        // use Euclidean approximations of the area and boundary size since those will be more accurate
        // at these sizes
        final double expectedArea = 0.5 * azOffset * polarOffset;
        Assertions.assertEquals(expectedArea, tree.getSize(), TEST_EPS);

        final double expectedBoundarySize = azOffset + polarOffset + Math.hypot(azOffset, polarOffset);
        Assertions.assertEquals(expectedBoundarySize, tree.getBoundarySize(), TEST_EPS);

        Assertions.assertTrue(tree.contains(tree.getCentroid()));
        checkCentroidConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                tree.getCentroid(),
                Point2S.of(minAz + (0.75 * azOffset), minPolar + (0.75 * polarOffset)));

        SphericalTestUtils.checkClassify(tree, RegionLocation.BOUNDARY,
                p0, p1, p2, p0.slerp(p1, 0.5), p1.slerp(p2, 0.5), p2.slerp(p0, 0.5));

        final double midAz = minAz + (0.5 * azOffset);
        final double pastMinAz = minAz - azOffset;
        final double pastMaxAz = maxAz + azOffset;

        final double midPolar = minPolar + (0.5 * polarOffset);
        final double pastMinPolar = minPolar - polarOffset;
        final double pastMaxPolar = maxPolar + polarOffset;

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                tree.getCentroid().antipodal(),
                Point2S.of(pastMinAz, midPolar), Point2S.of(pastMaxAz, midPolar),
                Point2S.of(midAz, pastMinPolar), Point2S.of(midAz, pastMaxPolar));
    }

    @Test
    public void testGeometricProperties_equalAndOppositeRegions() {
        // arrange
        final Point2S center = Point2S.PLUS_I;
        final double radius = 0.25 * Math.PI;

        final RegionBSPTree2S a = buildDiamond(center, radius);
        final RegionBSPTree2S b = buildDiamond(center.antipodal(), radius);

        // act
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.union(a, b);

        // assert
        final double area = 8 * rightTriangleArea(radius, radius);
        Assertions.assertEquals(area, tree.getSize(), TEST_EPS);

        final double boundarySize = 8 * sphericalHypot(radius, radius);
        Assertions.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        // should be null since no unique centroid exists
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testSplit_both() {
        // arrange
        final GreatCircle c1 = GreatCircles.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final GreatCircle c2 = GreatCircles.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        final RegionBSPTree2S tree = ConvexArea2S.fromBounds(c1, c2).toTree();

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        final Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final Point2S p1 = c1.intersection(splitter);
        final Point2S p2 = splitter.intersection(c2);

        final RegionBSPTree2S minus = split.getMinus();
        final List<GreatArcPath> minusPaths = minus.getBoundaryPaths();
        Assertions.assertEquals(1, minusPaths.size());
        assertPath(minusPaths.get(0), Point2S.PLUS_K, p1, p2, Point2S.PLUS_K);

        final RegionBSPTree2S plus = split.getPlus();
        final List<GreatArcPath> plusPaths = plus.getBoundaryPaths();
        Assertions.assertEquals(1, plusPaths.size());
        assertPath(plusPaths.get(0), p1, Point2S.MINUS_K, p2, p1);

        Assertions.assertEquals(tree.getSize(), minus.getSize() + plus.getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_minus() {
        // arrange
        final RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION).toTree();

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, -1, 1), TEST_PRECISION);

        // act
        final Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final RegionBSPTree2S minus = split.getMinus();
        Assertions.assertNotSame(tree, minus);
        Assertions.assertEquals(tree.getSize(), minus.getSize(), TEST_EPS);

        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_plus() {
        // arrange
        final RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION).toTree();

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, 1, -1), TEST_PRECISION);

        // act
        final Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final RegionBSPTree2S plus = split.getPlus();
        Assertions.assertNotSame(tree, plus);
        Assertions.assertEquals(tree.getSize(), plus.getSize(), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        final Transform2S t = Transform2S.createReflection(Point2S.PLUS_J);
        final RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(
                Arrays.asList(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K), TEST_PRECISION).toTree();

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI_OVER_TWO, tree.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.MINUS_J, Point2S.PLUS_I, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, tree.getCentroid(), TEST_EPS);

        checkCentroidConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.of(-0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.MINUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, 0.304 * PlaneAngleRadians.PI),
                Point2S.of(-0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.PLUS_J, Point2S.MINUS_I, Point2S.MINUS_K);
    }

    @Test
    public void testRegionNode_getNodeRegion() {
        // arrange
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();

        final RegionNode2S root = tree.getRoot();
        final RegionNode2S minus = root.cut(EQUATOR).getMinus();
        final RegionNode2S minusPlus = minus.cut(X_MERIDIAN).getPlus();

        // act/assert
        final ConvexArea2S rootRegion = root.getNodeRegion();
        Assertions.assertEquals(4 * PlaneAngleRadians.PI, rootRegion.getSize(), TEST_EPS);
        Assertions.assertNull(rootRegion.getCentroid());

        final ConvexArea2S minusRegion = minus.getNodeRegion();
        Assertions.assertEquals(2 * PlaneAngleRadians.PI, minusRegion.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, minusRegion.getCentroid(), TEST_EPS);

        final ConvexArea2S minusPlusRegion = minusPlus.getNodeRegion();
        Assertions.assertEquals(PlaneAngleRadians.PI, minusPlusRegion.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI),
                minusPlusRegion.getCentroid(), TEST_EPS);
    }

    @Test
    public void testGeographicMap() {
        // arrange
        final RegionBSPTree2S continental = latLongToTree(TEST_PRECISION, new double[][] {
                {51.14850,  2.51357}, {50.94660,  1.63900}, {50.12717,  1.33876}, {49.34737, -0.98946},
                {49.77634, -1.93349}, {48.64442, -1.61651}, {48.90169, -3.29581}, {48.68416, -4.59234},
                {47.95495, -4.49155}, {47.57032, -2.96327}, {46.01491, -1.19379}, {44.02261, -1.38422},
                {43.42280, -1.90135}, {43.03401, -1.50277}, {42.34338,  1.82679}, {42.47301,  2.98599},
                {43.07520,  3.10041}, {43.39965,  4.55696}, {43.12889,  6.52924}, {43.69384,  7.43518},
                {44.12790,  7.54959}, {45.02851,  6.74995}, {45.33309,  7.09665}, {46.42967,  6.50009},
                {46.27298,  6.02260}, {46.72577,  6.03738}, {47.62058,  7.46675}, {49.01778,  8.09927},
                {49.20195,  6.65822}, {49.44266,  5.89775}, {49.98537,  4.79922}
            });
        final RegionBSPTree2S corsica = latLongToTree(TEST_PRECISION, new double[][] {
                {42.15249,  9.56001}, {43.00998,  9.39000}, {42.62812,  8.74600}, {42.25651,  8.54421},
                {41.58361,  8.77572}, {41.38000,  9.22975}
            });

        // act
        final RegionBSPTree2S france = RegionBSPTree2S.empty();
        france.union(continental, corsica);

        // assert
        Assertions.assertEquals(0.6316801448267251, france.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.013964220234478741, france.getSize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(0.04368552749392928, 0.7590839905197961),
                france.getCentroid(), CENTROID_EPS);

        checkCentroidConsistency(france);
    }

    @Test
    public void testCircleToPolygonCentroid() {
        final double radius = 0.0001;
        final Point2S center = Point2S.of(1.0, 1.0);
        final int numPts = 200;

        // counterclockwise
        final RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false, TEST_PRECISION);
        SphericalTestUtils.assertPointsEq(center, ccw.getCentroid(), TEST_EPS);

        // clockwise; centroid should just be antipodal for the circle center
        final RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true, TEST_PRECISION);

        SphericalTestUtils.assertPointsEq(center.antipodal(), cw.getCentroid(), CENTROID_EPS);
    }

    @Test
    public void testCircleToPolygonSize() {
        final double radius = 0.0001;
        final Point2S center = Point2S.of(1.0, 1.0);
        final int numPts = 200;

        // https://en.wikipedia.org/wiki/Spherical_cap
        final double ccwArea = 4.0 * PlaneAngleRadians.PI * Math.pow(Math.sin(radius / 2.0), 2.0);
        final double cwArea = 4.0 * PlaneAngleRadians.PI - ccwArea;

        final RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false, TEST_PRECISION);
        Assertions.assertEquals(ccwArea, ccw.getSize(), TEST_EPS, "Counterclockwise size");

        final RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true, TEST_PRECISION);
        Assertions.assertEquals(cwArea, cw.getSize(), TEST_EPS, "Clockwise size");
    }

    @Test
    public void testCircleToPolygonBoundarySize() {
        final double radius = 0.0001;
        final Point2S center = Point2S.of(1.0, 1.0);
        final int numPts = 200;

        // boundary size is independent from winding
        final double boundary = PlaneAngleRadians.TWO_PI * Math.sin(radius);

        final RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false, TEST_PRECISION);
        Assertions.assertEquals(boundary, ccw.getBoundarySize(), 1.0e-7, "Counterclockwise boundary size");

        final RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true, TEST_PRECISION);
        Assertions.assertEquals(boundary, cw.getBoundarySize(), 1.0e-7, "Clockwise boundary size");
    }

    @Test
    public void testSmallCircleToPolygon() {
        // arrange
        final double radius = 5.0e-8;
        final Point2S center = Point2S.of(0.5, 1.5);
        final int numPts = 100;

        // act
        final RegionBSPTree2S circle = circleToPolygon(center, radius, numPts, false, TEST_PRECISION);

        // assert
        // https://en.wikipedia.org/wiki/Spherical_cap
        final double area = 4.0 * PlaneAngleRadians.PI * Math.pow(Math.sin(radius / 2.0), 2.0);
        final double boundary = PlaneAngleRadians.TWO_PI * Math.sin(radius);

        SphericalTestUtils.assertPointsEq(center, circle.getCentroid(), TEST_EPS);
        Assertions.assertEquals(area, circle.getSize(), TEST_EPS);
        Assertions.assertEquals(boundary, circle.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testSmallGeographicalRectangle() {
        // arrange
        final double[][] vertices = {
            {42.656216727628696, -70.61919768884546},
            {42.65612858998112, -70.61938607250165},
            {42.65579098923594, -70.61909615581666},
            {42.655879126692355, -70.61890777301083}
        };

        // act
        final RegionBSPTree2S rectangle = latLongToTree(TEST_PRECISION, vertices);

        // assert
        // approximate the centroid as average of vertices
        final double avgLat = Stream.of(vertices).mapToDouble(v -> v[0]).average().getAsDouble();
        final double avgLon = Stream.of(vertices).mapToDouble(v -> v[1]).average().getAsDouble();
        final Point2S expectedCentroid = latLongToPoint(avgLat, avgLon);

        SphericalTestUtils.assertPointsEq(expectedCentroid, rectangle.getCentroid(), TEST_EPS);

        // expected results computed using GeographicLib (https://geographiclib.sourceforge.io/)
        Assertions.assertEquals(1.997213869978027E-11, rectangle.getSize(), TEST_EPS);
        Assertions.assertEquals(1.9669710464585642E-5, rectangle.getBoundarySize(), TEST_EPS);
    }

    /**
     * Insert hyperplane convex subsets defining the positive quadrant area.
     * @param tree
     */
    private static void insertPositiveQuadrant(final RegionBSPTree2S tree) {
        tree.insert(Arrays.asList(
                EQUATOR.arc(Point2S.PLUS_I, Point2S.PLUS_J),
                X_MERIDIAN.arc(Point2S.PLUS_K, Point2S.PLUS_I),
                Y_MERIDIAN.arc(Point2S.PLUS_J, Point2S.PLUS_K)
            ));
    }

    private static Point2S triangleCentroid(final Point2S p1, final Point2S p2, final Point2S p3) {
        // compute the centroid using intersection mid point arcs
        final GreatCircle c1 = GreatCircles.fromPoints(p1, p2.slerp(p3, 0.5), TEST_PRECISION);
        final GreatCircle c2 = GreatCircles.fromPoints(p2, p1.slerp(p3, 0.5), TEST_PRECISION);

        return c1.intersection(c2);
    }

    private static void assertPath(final GreatArcPath path, final Point2S... vertices) {
        final List<Point2S> expected = Arrays.asList(vertices);
        final List<Point2S> actual = path.getVertices();

        if (expected.size() != actual.size()) {
            Assertions.fail("Unexpected path size. Expected path " + expected +
                    " but was " + actual);
        }

        for (int i = 0; i < expected.size(); ++i) {
            if (!expected.get(i).eq(actual.get(i), TEST_PRECISION)) {
                Assertions.fail("Unexpected path vertex at index " + i + ". Expected path " + expected +
                        " but was " + actual);
            }
        }
    }

    private static RegionBSPTree2S latLongToTree(final DoublePrecisionContext precision, final double[][] points) {
        final GreatArcPath.Builder pathBuilder = GreatArcPath.builder(precision);

        for (final double[] point : points) {
            pathBuilder.append(latLongToPoint(point[0], point[1]));
        }

        return pathBuilder.close().toTree();
    }

    private static Point2S latLongToPoint(final double latitude, final double longitude) {
        return Point2S.of(Math.toRadians(longitude), Math.toRadians(90.0 - latitude));
    }

    private static void checkCentroidConsistency(final RegionBSPTree2S region) {
        final Point2S centroid = region.getCentroid();
        final double size = region.getSize();

        final GreatCircle circle = GreatCircles.fromPole(centroid.getVector(), TEST_PRECISION);
        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.2) {
            final Point2S pt = circle.toSpace(Point1S.of(az));
            final GreatCircle splitter = GreatCircles.fromPoints(centroid, pt, TEST_PRECISION);

            final Split<RegionBSPTree2S> split = region.split(splitter);

            Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

            final RegionBSPTree2S minus = split.getMinus();
            final double minusSize = minus.getSize();

            final RegionBSPTree2S plus = split.getPlus();
            final double plusSize = plus.getSize();

            final Point2S computedCentroid = Point2S.from(weightedCentroidVector(minus)
                    .add(weightedCentroidVector(plus)));

            Assertions.assertEquals(size, minusSize + plusSize, TEST_EPS);
            SphericalTestUtils.assertPointsEq(centroid, computedCentroid, TEST_EPS);
        }
    }

    private static Vector3D weightedCentroidVector(final RegionBSPTree2S tree) {
        Vector3D sum = Vector3D.ZERO;
        for (final ConvexArea2S convex : tree.toConvex()) {
            sum = sum.add(convex.getWeightedCentroidVector());
        }

        return sum;
    }

    private static RegionBSPTree2S buildDiamond(final Point2S center, final double radius) {
        final Vector3D u = center.getVector();
        final Vector3D w = u.orthogonal(Vector3D.Unit.PLUS_Z);
        final Vector3D v = w.cross(u);

        final Transform2S rotV = Transform2S.createRotation(v, radius);
        final Transform2S rotW = Transform2S.createRotation(w, radius);

        final Point2S top = rotV.inverse().apply(center);
        final Point2S bottom = rotV.apply(center);

        final Point2S right = rotW.apply(center);
        final Point2S left = rotW.inverse().apply(center);

        return GreatArcPath.fromVertexLoop(Arrays.asList(top, left, bottom, right), TEST_PRECISION)
                .toTree();
    }

    /** Solve for the hypotenuse of a spherical right triangle, given the lengths of the
     * other two side. The sides must have lengths less than pi/2.
     * @param a first side; must be less than pi/2
     * @param b second side; must be less than pi/2
     * @return the hypotenuse of the spherical right triangle with sides of the given lengths
     */
    private static double sphericalHypot(final double a, final double b) {
        // use the spherical law of cosines and the fact that cos(pi/2) = 0
        // https://en.wikipedia.org/wiki/Spherical_trigonometry#Cosine_rules
        return Math.acos(Math.cos(a) * Math.cos(b));
    }

    /**
     * Compute the area of the spherical right triangle with the given sides. The sides must have lengths
     * less than pi/2.
     * @param a first side; must be less than pi/2
     * @param b second side; must be less than pi/2
     * @return the area of the spherical right triangle
     */
    private static double rightTriangleArea(final double a, final double b) {
        final double c = sphericalHypot(a, b);

        // use the spherical law of sines to determine the interior angles
        // https://en.wikipedia.org/wiki/Spherical_trigonometry#Sine_rules
        final double sinC = Math.sin(c);
        final double angleA = Math.asin(Math.sin(a) / sinC);
        final double angleB = Math.asin(Math.sin(b) / sinC);

        // use Girard's theorem
        return angleA + angleB - PlaneAngleRadians.PI_OVER_TWO;
    }

    private static RegionBSPTree2S circleToPolygon(final Point2S center, final double radius, final int numPts,
                                                   final boolean clockwise, final DoublePrecisionContext precision) {
        final List<Point2S> pts = new ArrayList<>(numPts);

        // get an arbitrary point on the circle boundary
        pts.add(Transform2S.createRotation(center.getVector().orthogonal(), radius).apply(center));

        // create the list of boundary points by rotating the previous point around the circle center
        final double span = PlaneAngleRadians.TWO_PI / numPts;

        // negate the span for clockwise winding
        final Transform2S rotate = Transform2S.createRotation(center, clockwise ? -span : span);
        for (int i = 1; i < numPts; ++i) {
            pts.add(rotate.apply(pts.get(i - 1)));
        }

        return GreatArcPath.fromVertexLoop(pts, precision).toTree();
    }
}
