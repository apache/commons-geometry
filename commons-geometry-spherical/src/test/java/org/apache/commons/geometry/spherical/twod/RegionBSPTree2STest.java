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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
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
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree2STest {

    private static final double TEST_EPS = 1e-10;

    // alternative epsilon value for checking the barycenters of complex
    // or very small regions
    private static final double BARYCENTER_EPS = 1e-5;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle EQUATOR = GreatCircle.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle X_MERIDIAN = GreatCircle.fromPoleAndU(
            Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle Y_MERIDIAN = GreatCircle.fromPoleAndU(
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_booleanArg_true() {
        // act
        RegionBSPTree2S tree = new RegionBSPTree2S(true);

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_booleanArg_false() {
        // act
        RegionBSPTree2S tree = new RegionBSPTree2S(false);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_default() {
        // act
        RegionBSPTree2S tree = new RegionBSPTree2S();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        RegionBSPTree2S tree = RegionBSPTree2S.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testFrom_boundaries_noBoundaries() {
        // act
        RegionBSPTree2S tree = RegionBSPTree2S.from(Arrays.asList());

        // assert
        Assert.assertNull(tree.getBarycenter());
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testFrom_boundaries() {
        // act
        RegionBSPTree2S tree = RegionBSPTree2S.from(Arrays.asList(
                    EQUATOR.arc(Point2S.PLUS_I, Point2S.PLUS_J),
                    X_MERIDIAN.arc(Point2S.PLUS_K, Point2S.PLUS_I),
                    Y_MERIDIAN.arc(Point2S.PLUS_J, Point2S.PLUS_K)
                ));

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.of(1, 0.5));
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(-1, 0.5), Point2S.of(Math.PI, 0.5 * Math.PI));
    }

    @Test
    public void testFromBoundarySource() {
        // arrange
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.of(0.1, 0.1), Point2S.of(0, 0.5),
                    Point2S.of(0.15, 0.75), Point2S.of(0.3, 0.5),
                    Point2S.of(0.1, 0.1)
                ), TEST_PRECISION);

        // act
        RegionBSPTree2S tree = RegionBSPTree2S.from(area);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(area.getSize(), tree.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(area.getBarycenter(), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFromBoundarySource_noBoundaries() {
        // arrange
        BoundarySource2S src = () -> new ArrayList<GreatArc>().stream();

        // act
        RegionBSPTree2S tree = RegionBSPTree2S.from(src);

        // assert
        Assert.assertTrue(tree.isFull());
    }

    @Test
    public void testCopy() {
        // arrange
        RegionBSPTree2S tree = new RegionBSPTree2S(true);
        tree.getRoot().cut(EQUATOR);

        // act
        RegionBSPTree2S copy = tree.copy();

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(3, copy.count());
    }

    @Test
    public void testBoundaries() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        List<GreatArc> arcs = new ArrayList<>();
        tree.boundaries().forEach(arcs::add);

        // assert
        Assert.assertEquals(3, arcs.size());
    }

    @Test
    public void testGetBoundaries() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        List<GreatArc> arcs = tree.getBoundaries();

        // assert
        Assert.assertEquals(3, arcs.size());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        List<GreatArc> arcs = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(3, arcs.size());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act
        List<GreatArc> arcs = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, arcs.size());
    }

    @Test
    public void testToTree_returnsNewInstance() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        RegionBSPTree2S result = tree.toTree();

        // assert
        Assert.assertNotSame(tree, result);
        Assert.assertEquals(3, result.getBoundaries().size());
    }

    @Test
    public void testGetBoundaryPaths_cachesResult() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        insertPositiveQuadrant(tree);

        // act
        List<GreatArcPath> a = tree.getBoundaryPaths();
        List<GreatArcPath> b = tree.getBoundaryPaths();

        // assert
        Assert.assertSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_recomputesResultOnChange() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(EQUATOR.span());

        // act
        List<GreatArcPath> a = tree.getBoundaryPaths();
        tree.insert(X_MERIDIAN.span());
        List<GreatArcPath> b = tree.getBoundaryPaths();

        // assert
        Assert.assertNotSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_isUnmodifiable() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(EQUATOR.span());

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            tree.getBoundaryPaths().add(GreatArcPath.empty());
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testToConvex_full() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.full();

        // act
        List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act
        List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_doubleLune() {
        // arrange
        RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // act
        List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assert.assertEquals(2, result.size());

        double size = result.stream().collect(Collectors.summingDouble(a -> a.getSize()));
        Assert.assertEquals(PlaneAngleRadians.TWO_PI, size, TEST_EPS);
    }

    @Test
    public void testToConvex_doubleLune_complement() {
        // arrange
        RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // act
        List<ConvexArea2S> result = tree.toConvex();

        // assert
        Assert.assertEquals(2, result.size());

        double size = result.stream().collect(Collectors.summingDouble(a -> a.getSize()));
        Assert.assertEquals(PlaneAngleRadians.TWO_PI, size, TEST_EPS);
    }

    @Test
    public void testProject() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
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

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, tree.project(tree.getBarycenter()), TEST_EPS);
    }

    @Test
    public void testProject_noBoundaries() {
        // act/assert
        Assert.assertNull(RegionBSPTree2S.empty().project(Point2S.PLUS_I));
        Assert.assertNull(RegionBSPTree2S.full().project(Point2S.PLUS_I));
    }

    @Test
    public void testGeometricProperties_full() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.full();

        // act/assert
        Assert.assertEquals(4 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assert.assertEquals(0, tree.getBoundaries().size());
        Assert.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_empty() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.empty();

        // act/assert
        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assert.assertEquals(0, tree.getBoundaries().size());
        Assert.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_halfSpace() {
        // arrange
        RegionBSPTree2S tree = RegionBSPTree2S.full();
        tree.getRoot().cut(EQUATOR);

        // act/assert
        Assert.assertEquals(PlaneAngleRadians.TWO_PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.TWO_PI, tree.getBoundarySize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, tree.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(tree);

        List<GreatArc> arcs = tree.getBoundaries();
        Assert.assertEquals(1, arcs.size());

        GreatArc arc = arcs.get(0);
        Assert.assertSame(EQUATOR, arc.getCircle());
        Assert.assertNull(arc.getStartPoint());
        Assert.assertNull(arc.getEndPoint());

        List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        GreatArcPath path = paths.get(0);
        Assert.assertEquals(1, path.getArcs().size());
        Assert.assertTrue(path.getArcs().get(0).isFull());
    }

    @Test
    public void testGeometricProperties_doubleLune() {
        // act
        RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .append(EQUATOR.arc(0,  PlaneAngleRadians.PI))
                .append(X_MERIDIAN.arc(PlaneAngleRadians.PI, 0))
                .append(EQUATOR.reverse().arc(0, PlaneAngleRadians.PI))
                .append(X_MERIDIAN.reverse().arc(PlaneAngleRadians.PI, 0))
                .build()
                .toTree();

        // assert
        Assert.assertEquals(2 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(4 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

        List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(2, paths.size());

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
        RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .appendVertices(Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J)
                .close()
                .toTree();

        // assert
        Assert.assertEquals(0.5 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);

        Point2S center = Point2S.from(Point2S.MINUS_K.getVector()
                .add(Point2S.PLUS_I.getVector())
                .add(Point2S.MINUS_J.getVector()));
        SphericalTestUtils.assertPointsEq(center, tree.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(tree);

        List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        assertPath(paths.get(0), Point2S.MINUS_J, Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.of(1.75 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.PLUS_J, Point2S.PLUS_K, Point2S.MINUS_I);
    }

    @Test
    public void testGeometricProperties_quadrant_complement() {
        // arrange
        RegionBSPTree2S tree = GreatArcPath.builder(TEST_PRECISION)
                .appendVertices(Point2S.MINUS_K, Point2S.PLUS_I, Point2S.MINUS_J)
                .close()
                .toTree();

        // act
        tree.complement();

        // assert
        Assert.assertEquals(3.5 * PlaneAngleRadians.PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);

        Point2S center = Point2S.from(Point2S.MINUS_K.getVector()
                .add(Point2S.PLUS_I.getVector())
                .add(Point2S.MINUS_J.getVector()));
        SphericalTestUtils.assertPointsEq(center.antipodal(), tree.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(tree);

        List<GreatArcPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        assertPath(paths.get(0), Point2S.MINUS_J, Point2S.PLUS_I, Point2S.MINUS_K, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.of(1.75 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE,
                Point2S.PLUS_J, Point2S.PLUS_K, Point2S.MINUS_I);
    }

    @Test
    public void testGeometricProperties_polygonWithHole() {
        // arrange
        Point2S center = Point2S.of(0.5, 2);

        double outerRadius = 1;
        double innerRadius = 0.5;

        RegionBSPTree2S outer = buildDiamond(center, outerRadius);
        RegionBSPTree2S inner = buildDiamond(center, innerRadius);

        // rotate the inner diamond a quarter turn to become a square
        inner.transform(Transform2S.createRotation(center, 0.25 * Math.PI));

        // act
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.difference(outer, inner);

        // assert
        double area = 4 * (rightTriangleArea(outerRadius, outerRadius) - rightTriangleArea(innerRadius, innerRadius));
        Assert.assertEquals(area, tree.getSize(), TEST_EPS);

        double outerSideLength = sphericalHypot(outerRadius, outerRadius);
        double innerSideLength = sphericalHypot(innerRadius, innerRadius);
        double boundarySize = 4 * (outerSideLength + innerSideLength);
        Assert.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(center, tree.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, center);
    }

    @Test
    public void testGeometricProperties_polygonWithHole_complex() {
        // arrange
        Point2S center = Point2S.of(0.5, 2);

        double outerRadius = 2;
        double midRadius = 1;
        double innerRadius = 0.5;

        RegionBSPTree2S outer = buildDiamond(center, outerRadius);
        RegionBSPTree2S mid = buildDiamond(center, midRadius);
        RegionBSPTree2S inner = buildDiamond(center, innerRadius);

        // rotate the middle diamond a quarter turn to become a square
        mid.transform(Transform2S.createRotation(center, 0.25 * Math.PI));

        // act
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.difference(outer, mid);
        tree.union(inner);
        tree.complement();

        // assert
        // compute the area, adjusting the first computation for the fact that the triangles comprising the
        // outer diamond have lengths greater than pi/2
        double nonComplementedArea = 4 * ((PlaneAngleRadians.PI - rightTriangleArea(outerRadius, outerRadius) -
                rightTriangleArea(midRadius, midRadius) + rightTriangleArea(innerRadius, innerRadius)));
        double area = (4 * PlaneAngleRadians.PI) - nonComplementedArea;
        Assert.assertEquals(area, tree.getSize(), TEST_EPS);

        double outerSideLength = sphericalHypot(outerRadius, outerRadius);
        double midSideLength = sphericalHypot(midRadius, midRadius);
        double innerSideLength = sphericalHypot(innerRadius, innerRadius);
        double boundarySize = 4 * (outerSideLength + midSideLength + innerSideLength);
        Assert.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(center.antipodal(), tree.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(tree);

        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, center);
    }

    @Test
    public void testGeometricProperties_equalAndOppositeRegions() {
        // arrange
        Point2S center = Point2S.PLUS_I;
        double radius = 0.25 * Math.PI;

        RegionBSPTree2S a = buildDiamond(center, radius);
        RegionBSPTree2S b = buildDiamond(center.antipodal(), radius);

        // act
        RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.union(a, b);

        // assert
        double area = 8 * rightTriangleArea(radius, radius);
        Assert.assertEquals(area, tree.getSize(), TEST_EPS);

        double boundarySize = 8 * sphericalHypot(radius, radius);
        Assert.assertEquals(boundarySize, tree.getBoundarySize(), TEST_EPS);

        // should be null since no unique barycenter exists
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testSplit_both() {
        // arrange
        GreatCircle c1 = GreatCircle.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        RegionBSPTree2S tree = ConvexArea2S.fromBounds(c1, c2).toTree();

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        Point2S p1 = c1.intersection(splitter);
        Point2S p2 = splitter.intersection(c2);

        RegionBSPTree2S minus = split.getMinus();
        List<GreatArcPath> minusPaths = minus.getBoundaryPaths();
        Assert.assertEquals(1, minusPaths.size());
        assertPath(minusPaths.get(0), Point2S.PLUS_K, p1, p2, Point2S.PLUS_K);

        RegionBSPTree2S plus = split.getPlus();
        List<GreatArcPath> plusPaths = plus.getBoundaryPaths();
        Assert.assertEquals(1, plusPaths.size());
        assertPath(plusPaths.get(0), p1, Point2S.MINUS_K, p2, p1);

        Assert.assertEquals(tree.getSize(), minus.getSize() + plus.getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_minus() {
        // arrange
        RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION).toTree();

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, -1, 1), TEST_PRECISION);

        // act
        Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        RegionBSPTree2S minus = split.getMinus();
        Assert.assertNotSame(tree, minus);
        Assert.assertEquals(tree.getSize(), minus.getSize(), TEST_EPS);

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_plus() {
        // arrange
        RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION).toTree();

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, 1, -1), TEST_PRECISION);

        // act
        Split<RegionBSPTree2S> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        RegionBSPTree2S plus = split.getPlus();
        Assert.assertNotSame(tree, plus);
        Assert.assertEquals(tree.getSize(), plus.getSize(), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        Transform2S t = Transform2S.createReflection(Point2S.PLUS_J);
        RegionBSPTree2S tree = ConvexArea2S.fromVertexLoop(
                Arrays.asList(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K), TEST_PRECISION).toTree();

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, tree.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.MINUS_J, Point2S.PLUS_I, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, tree.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(tree);

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
        RegionBSPTree2S tree = RegionBSPTree2S.empty();

        RegionNode2S root = tree.getRoot();
        RegionNode2S minus = root.cut(EQUATOR).getMinus();
        RegionNode2S minusPlus = minus.cut(X_MERIDIAN).getPlus();

        // act/assert
        ConvexArea2S rootRegion = root.getNodeRegion();
        Assert.assertEquals(4 * PlaneAngleRadians.PI, rootRegion.getSize(), TEST_EPS);
        Assert.assertNull(rootRegion.getBarycenter());

        ConvexArea2S minusRegion = minus.getNodeRegion();
        Assert.assertEquals(2 * PlaneAngleRadians.PI, minusRegion.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, minusRegion.getBarycenter(), TEST_EPS);

        ConvexArea2S minusPlusRegion = minusPlus.getNodeRegion();
        Assert.assertEquals(PlaneAngleRadians.PI, minusPlusRegion.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI),
                minusPlusRegion.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGeographicMap() {
        // arrange
        RegionBSPTree2S continental = latLongToTree(new double[][] {
                {51.14850,  2.51357}, {50.94660,  1.63900}, {50.12717,  1.33876}, {49.34737, -0.98946},
                {49.77634, -1.93349}, {48.64442, -1.61651}, {48.90169, -3.29581}, {48.68416, -4.59234},
                {47.95495, -4.49155}, {47.57032, -2.96327}, {46.01491, -1.19379}, {44.02261, -1.38422},
                {43.42280, -1.90135}, {43.03401, -1.50277}, {42.34338,  1.82679}, {42.47301,  2.98599},
                {43.07520,  3.10041}, {43.39965,  4.55696}, {43.12889,  6.52924}, {43.69384,  7.43518},
                {44.12790,  7.54959}, {45.02851,  6.74995}, {45.33309,  7.09665}, {46.42967,  6.50009},
                {46.27298,  6.02260}, {46.72577,  6.03738}, {47.62058,  7.46675}, {49.01778,  8.09927},
                {49.20195,  6.65822}, {49.44266,  5.89775}, {49.98537,  4.79922}
            });
        RegionBSPTree2S corsica = latLongToTree(new double[][] {
                {42.15249,  9.56001}, {43.00998,  9.39000}, {42.62812,  8.74600}, {42.25651,  8.54421},
                {41.58361,  8.77572}, {41.38000,  9.22975}
            });

        // act
        RegionBSPTree2S france = RegionBSPTree2S.empty();
        france.union(continental, corsica);

        // assert
        Assert.assertEquals(0.6316801448267251, france.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.013964220234478741, france.getSize(), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(0.04368552749392928, 0.7590839905197961),
                france.getBarycenter(), BARYCENTER_EPS);

        checkBarycenterConsistency(france);
    }

    @Test
    public void testCircleToPolygonBarycenter() {
        double radius = 0.0001;
        Point2S center = Point2S.of(1.0, 1.0);
        int numPts = 200;

        // counterclockwise
        RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false);
        SphericalTestUtils.assertPointsEq(center, ccw.getBarycenter(), BARYCENTER_EPS);

        // clockwise; barycenter should just be antipodal for the circle center
        RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true);
        SphericalTestUtils.assertPointsEq(center.antipodal(), cw.getBarycenter(), BARYCENTER_EPS);
    }

    @Test
    public void testCircleToPolygonSize() {
        double radius = 0.0001;
        Point2S center = Point2S.of(1.0, 1.0);
        int numPts = 200;

        // https://en.wikipedia.org/wiki/Spherical_cap
        double ccwArea = 4.0 * PlaneAngleRadians.PI * Math.pow(Math.sin(radius / 2.0), 2.0);
        double cwArea = 4.0 * PlaneAngleRadians.PI - ccwArea;

        RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false);
        Assert.assertEquals("Counterclockwise size", ccwArea, ccw.getSize(), TEST_EPS);

        RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true);
        Assert.assertEquals("Clockwise size", cwArea, cw.getSize(), TEST_EPS);
    }

    @Test
    public void testCircleToPolygonBoundarySize() {
        double radius = 0.0001;
        Point2S center = Point2S.of(1.0, 1.0);
        int numPts = 200;

        // boundary size is independent from winding
        double boundary = PlaneAngleRadians.TWO_PI * Math.sin(radius);

        RegionBSPTree2S ccw = circleToPolygon(center, radius, numPts, false);
        Assert.assertEquals("Counterclockwise boundary size", boundary, ccw.getBoundarySize(), 1.0e-7);

        RegionBSPTree2S cw = circleToPolygon(center, radius, numPts, true);
        Assert.assertEquals("Clockwise boundary size", boundary, cw.getBoundarySize(), 1.0e-7);
    }

    /**
     * Insert convex subhyperplanes defining the positive quadrant area.
     * @param tree
     */
    private static void insertPositiveQuadrant(RegionBSPTree2S tree) {
        tree.insert(Arrays.asList(
                EQUATOR.arc(Point2S.PLUS_I, Point2S.PLUS_J),
                X_MERIDIAN.arc(Point2S.PLUS_K, Point2S.PLUS_I),
                Y_MERIDIAN.arc(Point2S.PLUS_J, Point2S.PLUS_K)
            ));
    }

    private static Point2S triangleBarycenter(Point2S p1, Point2S p2, Point2S p3) {
        // compute the barycenter using intersection mid point arcs
        GreatCircle c1 = GreatCircle.fromPoints(p1, p2.slerp(p3, 0.5), TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPoints(p2, p1.slerp(p3, 0.5), TEST_PRECISION);

        return c1.intersection(c2);
    }

    private static void assertPath(GreatArcPath path, Point2S... vertices) {
        List<Point2S> expected = Arrays.asList(vertices);
        List<Point2S> actual = path.getVertices();

        if (expected.size() != actual.size()) {
            Assert.fail("Unexpected path size. Expected path " + expected +
                    " but was " + actual);
        }

        for (int i = 0; i < expected.size(); ++i) {
            if (!expected.get(i).eq(actual.get(i), TEST_PRECISION)) {
                Assert.fail("Unexpected path vertex at index " + i + ". Expected path " + expected +
                        " but was " + actual);
            }
        }
    }

    private static RegionBSPTree2S latLongToTree(double[][] points) {
        GreatArcPath.Builder pathBuilder = GreatArcPath.builder(TEST_PRECISION);

        for (int i = 0; i < points.length; ++i) {
            pathBuilder.append(latLongToPoint(points[i][0], points[i][1]));
        }

        return pathBuilder.close().toTree();
    }

    private static Point2S latLongToPoint(double latitude, double longitude) {
        return Point2S.of(Math.toRadians(longitude), Math.toRadians(90.0 - latitude));
    }

    private static void checkBarycenterConsistency(RegionBSPTree2S region) {
        Point2S barycenter = region.getBarycenter();
        double size = region.getSize();

        GreatCircle circle = GreatCircle.fromPole(barycenter.getVector(), TEST_PRECISION);
        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.2) {
            Point2S pt = circle.toSpace(Point1S.of(az));
            GreatCircle splitter = GreatCircle.fromPoints(barycenter, pt, TEST_PRECISION);

            Split<RegionBSPTree2S> split = region.split(splitter);

            Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

            RegionBSPTree2S minus = split.getMinus();
            double minusSize = minus.getSize();

            RegionBSPTree2S plus = split.getPlus();
            double plusSize = plus.getSize();

            Point2S computedBarycenter = Point2S.from(weightedBarycenterVector(minus)
                    .add(weightedBarycenterVector(plus)));

            Assert.assertEquals(size, minusSize + plusSize, TEST_EPS);
            SphericalTestUtils.assertPointsEq(barycenter, computedBarycenter, TEST_EPS);
        }
    }

    private static Vector3D weightedBarycenterVector(RegionBSPTree2S tree) {
        Vector3D sum = Vector3D.ZERO;
        for (ConvexArea2S convex : tree.toConvex()) {
            sum = sum.add(convex.getWeightedBarycenterVector());
        }

        return sum;
    }

    private static RegionBSPTree2S buildDiamond(Point2S center, double radius) {
        Vector3D u = center.getVector();
        Vector3D w = u.orthogonal(Vector3D.Unit.PLUS_Z);
        Vector3D v = w.cross(u);

        Transform2S rotV = Transform2S.createRotation(v, radius);
        Transform2S rotW = Transform2S.createRotation(w, radius);

        Point2S top = rotV.inverse().apply(center);
        Point2S bottom = rotV.apply(center);

        Point2S right = rotW.apply(center);
        Point2S left = rotW.inverse().apply(center);

        return GreatArcPath.fromVertexLoop(Arrays.asList(top, left, bottom, right), TEST_PRECISION)
                .toTree();
    }

    /** Solve for the hypotenuse of a spherical right triangle, given the lengths of the
     * other two side. The sides must have lengths less than pi/2.
     * @param a first side; must be less than pi/2
     * @param b second side; must be less than pi/2
     * @return the hypotenuse of the spherical right triangle with sides of the given lengths
     */
    private static double sphericalHypot(double a, double b) {
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
    private static double rightTriangleArea(double a, double b) {
        double c = sphericalHypot(a, b);

        // use the spherical law of sines to determine the interior angles
        // https://en.wikipedia.org/wiki/Spherical_trigonometry#Sine_rules
        double sinC = Math.sin(c);
        double angleA = Math.asin(Math.sin(a) / sinC);
        double angleB = Math.asin(Math.sin(b) / sinC);

        // use Girard's theorem
        return angleA + angleB  - (0.5 * PlaneAngleRadians.PI);
    }

    private static RegionBSPTree2S circleToPolygon(Point2S center, double radius, int numPts, boolean clockwise) {
        List<Point2S> pts = new ArrayList<>(numPts);

        // get an arbitrary point on the circle boundary
        pts.add(Transform2S.createRotation(center.getVector().orthogonal(), radius).apply(center));

        // create the list of boundary points by rotating the previous point around the circle center
        double span = PlaneAngleRadians.TWO_PI / numPts;

        // negate the span for clockwise winding
        Transform2S rotate = Transform2S.createRotation(center, clockwise ? -span : span);
        for (int i = 1; i < numPts; ++i) {
            pts.add(rotate.apply(pts.get(i - 1)));
        }

        return GreatArcPath.fromVertexLoop(pts, TEST_PRECISION).toTree();
    }
}
