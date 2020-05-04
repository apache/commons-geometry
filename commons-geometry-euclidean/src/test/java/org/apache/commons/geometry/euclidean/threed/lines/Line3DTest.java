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
package org.apache.commons.geometry.euclidean.threed.lines;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class Line3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 1, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0), line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, line.getDirection(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test
    public void testFromPointAndDirection_normalizesDirection() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, line.getOrigin(), TEST_EPS);

        double invSqrt3 = 1.0 / Math.sqrt(3);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(invSqrt3, invSqrt3, invSqrt3), line.getDirection(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test
    public void testFromPointAndDirection_illegalDirectionNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1e-12, 1e-12, 1e-12), TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoints() {
        // arrange
        Line3D line = Lines3D.fromPoints(Vector3D.of(-1, 1, 0), Vector3D.of(-1, 7, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0), line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, line.getDirection(), TEST_EPS);
        Assert.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromPoints_pointsTooClose() {
        // act/assert
        Lines3D.fromPoints(Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 1 + 1e-16), TEST_PRECISION);
    }

    @Test
    public void testTransform() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);
        Line3D line = Lines3D.fromPointAndDirection(pt, Vector3D.of(1, 1, 1), TEST_PRECISION);

        AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        Line3D result = line.transform(mat);

        // assert
        Assert.assertTrue(result.contains(pt));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInOneAxis() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(v.getX(), v.getY(), -v.getZ()));

        // act
        Line3D result = line.transform(transform);

        // assert
        Assert.assertTrue(result.contains(Vector3D.of(1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInTwoAxes() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(v.getX(), -v.getY(), -v.getZ()));

        // act
        Line3D result = line.transform(transform);

        // assert
        Assert.assertTrue(result.contains(Vector3D.of(1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInThreeAxes() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(Vector3D::negate);

        // act
        Line3D result = line.transform(transform);

        // assert
        Assert.assertTrue(result.contains(Vector3D.of(-1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 0), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .translate(0.5, 1, 0)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        Line3D.SubspaceTransform result = line.subspaceTransform(transform);

        // assert
        Line3D tLine = result.getLine();
        Transform<Vector1D> tSub = result.getTransform();

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), tLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), tLine.getDirection(), TEST_EPS);

        Assert.assertEquals(0.5, tSub.apply(Vector1D.ZERO).getX(), TEST_EPS);
        Assert.assertEquals(4.5, tSub.apply(Vector1D.of(2)).getX(), TEST_EPS);
    }

    @Test
    public void testAbscissa() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, line.abscissa(line.getOrigin()), TEST_EPS);

        Assert.assertEquals(5.0, line.abscissa(Vector3D.of(4, 3, 0)), TEST_EPS);
        Assert.assertEquals(5.0, line.abscissa(Vector3D.of(4, 3, 10)), TEST_EPS);

        Assert.assertEquals(-5.0, line.abscissa(Vector3D.of(-4, -3, 0)), TEST_EPS);
        Assert.assertEquals(-5.0, line.abscissa(Vector3D.of(-4, -3, -10)), TEST_EPS);
    }

    @Test
    public void testToSubspace() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, line.toSubspace(line.getOrigin()).getX(), TEST_EPS);

        Assert.assertEquals(5.0, line.toSubspace(Vector3D.of(4, 3, -1)).getX(), TEST_EPS);
        Assert.assertEquals(5.0, line.toSubspace(Vector3D.of(4, 3, 10)).getX(), TEST_EPS);

        Assert.assertEquals(-5.0, line.toSubspace(Vector3D.of(-4, -3, -1)).getX(), TEST_EPS);
        Assert.assertEquals(-5.0, line.toSubspace(Vector3D.of(-4, -3, -10)).getX(), TEST_EPS);
    }

    @Test
    public void testPointAt() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(line.getOrigin(), line.pointAt(0.0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, -1), line.pointAt(5.0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -3, -1), line.pointAt(-5.0), TEST_EPS);
    }

    @Test
    public void testToSpace() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(line.getOrigin(), line.toSpace(Vector1D.of(0.0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, -1), line.toSpace(Vector1D.of(5.0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -3, -1), line.toSpace(Vector1D.of(-5.0)), TEST_EPS);
    }

    @Test
    public void testContains() {
        Vector3D p1 = Vector3D.of(0, 0, 1);
        Line3D l = Lines3D.fromPoints(p1, Vector3D.of(0, 0, 2), TEST_PRECISION);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(Vector3D.linearCombination(1.0, p1, 0.3, l.getDirection())));
        Vector3D u = l.getDirection().orthogonal();
        Vector3D v = l.getDirection().cross(u);
        for (double alpha = 0; alpha < 2 * Math.PI; alpha += 0.3) {
            Assert.assertTrue(!l.contains(p1.add(Vector3D.linearCombination(Math.cos(alpha), u,
                                                               Math.sin(alpha), v))));
        }
    }

    @Test
    public void testSimilar() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Line3D lA  = Lines3D.fromPoints(p1, p2, TEST_PRECISION);
        Line3D lB  = Lines3D.fromPoints(p2, p1, TEST_PRECISION);
        Assert.assertTrue(lA.isSimilarTo(lB));
        Assert.assertTrue(!lA.isSimilarTo(Lines3D.fromPoints(p1, p1.add(lA.getDirection().orthogonal()), TEST_PRECISION)));
    }

    @Test
    public void testPointDistance() {
        Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(Math.sqrt(3.0 / 2.0), l.distance(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assert.assertEquals(0, l.distance(Vector3D.of(0, -4, -4)), TEST_EPS);
    }

    @Test
    public void testLineDistance() {
        Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(1.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.distance(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(l),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)),
                            1.0e-10);
        Assert.assertEquals(Math.sqrt(8),
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)),
                            1.0e-10);
    }

    @Test
    public void testClosest() {
        Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.closest(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)).distance(Vector3D.of(-0.5, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closest(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)).distance(Vector3D.of(0, -2, -2)),
                            1.0e-10);
    }

    @Test
    public void testIntersection() {
        Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assert.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)));
        Assert.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)));
        Assert.assertEquals(0.0,
                            l.intersection(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assert.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)));
    }

    @Test
    public void testReverse() {
        // arrange
        Line3D line = Lines3D.fromPoints(Vector3D.of(1653345.6696423641, 6170370.041579291, 90000),
                             Vector3D.of(1650757.5050732433, 6160710.879908984, 0.9),
                             TEST_PRECISION);
        Vector3D expected = line.getDirection().negate();

        // act
        Line3D reversed = line.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(expected, reversed.getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan() {
        // arrange
        Line3D line = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        LineConvexSubset3D span = line.span();

        // assert
        Assert.assertTrue(span.isInfinite());
        Assert.assertFalse(span.isFinite());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());

        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(span.getSize());

        Assert.assertSame(line, span.getLine());
        Assert.assertTrue(span.getInterval().isFull());
    }

    @Test
    public void testSpan_contains() {
        // arrange
        double delta = 1e-12;

        LineConvexSubset3D span = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        for (double x = -10; x <= 10; x += 0.5) {

            // act/assert
            Assert.assertFalse(span.contains(Vector3D.of(0, 1, 0)));
            Assert.assertFalse(span.contains(Vector3D.of(0, 0, 1)));

            Assert.assertTrue(span.contains(Vector3D.of(x, 0, 0)));
            Assert.assertTrue(span.contains(Vector3D.of(x + delta, delta, delta)));
        }
    }

    @Test
    public void testSpan_transform() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION)
                .span();

        // act
        LineConvexSubset3D result = span.transform(t);

        // assert
        Assert.assertNull(result.getStartPoint());
        Assert.assertNull(result.getEndPoint());

        Assert.assertTrue(result.contains(Vector3D.of(0, 1, -1)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan_transform_reflection() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0),
                Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        LineConvexSubset3D result = span.transform(t);

        // assert
        Assert.assertNull(result.getStartPoint());
        Assert.assertNull(result.getEndPoint());

        Assert.assertTrue(result.contains(Vector3D.of(0, 1, 2)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan_toString() {
        // arrange
        LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)
                .span();

        // act
        String str = span.toString();

        // assert
        GeometryTestUtils.assertContains("LineSpanningSubset3D[origin= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
    }

    @Test
    public void testSubsetMethods() {
        // arrange
        Line3D line = Lines3D.fromPoints(Vector3D.of(0, 3, 0), Vector3D.of(1, 3, 0), TEST_PRECISION);

        // act/assert
        Segment3D doubleArgResult = line.segment(3, 4);
        Assert.assertSame(line, doubleArgResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 0), doubleArgResult.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, 0), doubleArgResult.getEndPoint(), TEST_EPS);

        Segment3D ptArgResult = line.segment(Vector3D.of(0, 4, 0), Vector3D.of(2, 5, 1));
        Assert.assertSame(line, ptArgResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), ptArgResult.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 0), ptArgResult.getEndPoint(), TEST_EPS);

        Ray3D rayDoubleResult = line.rayFrom(2);
        Assert.assertSame(line, rayDoubleResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 0), rayDoubleResult.getStartPoint(), TEST_EPS);
        Assert.assertNull(rayDoubleResult.getEndPoint());

        Ray3D rayPtResult = line.rayFrom(Vector3D.of(1, 4, 0));
        Assert.assertSame(line, rayPtResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 0), rayPtResult.getStartPoint(), TEST_EPS);
        Assert.assertNull(rayPtResult.getEndPoint());

        ReverseRay3D toDoubleResult = line.reverseRayTo(-1);
        Assert.assertSame(line, toDoubleResult.getLine());
        Assert.assertNull(toDoubleResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 3, 0), toDoubleResult.getEndPoint(), TEST_EPS);

        ReverseRay3D toPtResult = line.reverseRayTo(Vector3D.of(1, 4, 0));
        Assert.assertSame(line, toPtResult.getLine());
        Assert.assertNull(toPtResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 0), toPtResult.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        Vector3D p = Vector3D.of(1, 2, 3);
        Vector3D dir = Vector3D.of(1, 0, 0);

        Line3D a = Lines3D.fromPointAndDirection(p, dir, precision);
        Line3D b = Lines3D.fromPointAndDirection(Vector3D.ZERO, dir, precision);
        Line3D c = Lines3D.fromPointAndDirection(p, Vector3D.of(1, 1, 0), precision);

        Line3D d = Lines3D.fromPointAndDirection(p, dir, precision);
        Line3D e = Lines3D.fromPointAndDirection(p.add(Vector3D.of(1e-4, 1e-4, 1e-4)), dir, precision);
        Line3D f = Lines3D.fromPointAndDirection(p, Vector3D.of(1 + 1e-4, 1e-4, 1e-4), precision);

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(d.eq(a, precision));

        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(e.eq(a, precision));

        Assert.assertTrue(a.eq(f, precision));
        Assert.assertTrue(f.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        Line3D a = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);
        Line3D b = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, -1), Vector3D.of(4, 5, 6), TEST_PRECISION);
        Line3D c = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, -1), TEST_PRECISION);
        Line3D d = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), new EpsilonDoublePrecisionContext(TEST_EPS + 1e-3));

        Line3D e = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), new EpsilonDoublePrecisionContext(TEST_EPS));

        int hash = a.hashCode();

        // act/assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Line3D a = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);
        Line3D b = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, -1), Vector3D.of(4, 5, 6), TEST_PRECISION);
        Line3D c = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, -1), TEST_PRECISION);
        Line3D d = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), new EpsilonDoublePrecisionContext(TEST_EPS + 1e-3));

        Line3D e = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), new EpsilonDoublePrecisionContext(TEST_EPS));

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
        Assert.assertTrue(e.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        String str = line.toString();

        // assert
        Assert.assertTrue(str.contains("Line3D"));
        Assert.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*direction= \\(1(\\.0)?, 0(\\.0)?, 0(\\.0)?\\).*"));
    }
}
