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
package org.apache.commons.geometry.euclidean.threed.line;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Line3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 1, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0), line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, line.getDirection(), TEST_EPS);
        Assertions.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test
    public void testFromPointAndDirection_normalizesDirection() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, line.getOrigin(), TEST_EPS);

        final double invSqrt3 = 1.0 / Math.sqrt(3);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(invSqrt3, invSqrt3, invSqrt3), line.getDirection(), TEST_EPS);
        Assertions.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test
    public void testFromPointAndDirection_illegalDirectionNorm() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1e-12, 1e-12, 1e-12), TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoints() {
        // arrange
        final Line3D line = Lines3D.fromPoints(Vector3D.of(-1, 1, 0), Vector3D.of(-1, 7, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0), line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, line.getDirection(), TEST_EPS);
        Assertions.assertSame(TEST_PRECISION, line.getPrecision());
    }

    @Test
    public void testFromPoints_pointsTooClose() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Lines3D.fromPoints(Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, 1 + 1e-16), TEST_PRECISION));
    }

    @Test
    public void testTransform() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Line3D line = Lines3D.fromPointAndDirection(pt, Vector3D.of(1, 1, 1), TEST_PRECISION);

        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final Line3D result = line.transform(mat);

        // assert
        Assertions.assertTrue(result.contains(pt));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInOneAxis() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(v.getX(), v.getY(), -v.getZ()));

        // act
        final Line3D result = line.transform(transform);

        // assert
        Assertions.assertTrue(result.contains(Vector3D.of(1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInTwoAxes() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(v.getX(), -v.getY(), -v.getZ()));

        // act
        final Line3D result = line.transform(transform);

        // assert
        Assertions.assertTrue(result.contains(Vector3D.of(1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflectionInThreeAxes() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(Vector3D::negate);

        // act
        final Line3D result = line.transform(transform);

        // assert
        Assertions.assertTrue(result.contains(Vector3D.of(-1, 0, 0)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -1, -1).normalize(), result.getDirection(), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 0), TEST_PRECISION);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .translate(0.5, 1, 0)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final Line3D.SubspaceTransform result = line.subspaceTransform(transform);

        // assert
        final Line3D tLine = result.getLine();
        final Transform<Vector1D> tSub = result.getTransform();

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), tLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), tLine.getDirection(), TEST_EPS);

        Assertions.assertEquals(0.5, tSub.apply(Vector1D.ZERO).getX(), TEST_EPS);
        Assertions.assertEquals(4.5, tSub.apply(Vector1D.of(2)).getX(), TEST_EPS);
    }

    @Test
    public void testAbscissa() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0.0, line.abscissa(line.getOrigin()), TEST_EPS);

        Assertions.assertEquals(5.0, line.abscissa(Vector3D.of(4, 3, 0)), TEST_EPS);
        Assertions.assertEquals(5.0, line.abscissa(Vector3D.of(4, 3, 10)), TEST_EPS);

        Assertions.assertEquals(-5.0, line.abscissa(Vector3D.of(-4, -3, 0)), TEST_EPS);
        Assertions.assertEquals(-5.0, line.abscissa(Vector3D.of(-4, -3, -10)), TEST_EPS);
    }

    @Test
    public void testToSubspace() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0.0, line.toSubspace(line.getOrigin()).getX(), TEST_EPS);

        Assertions.assertEquals(5.0, line.toSubspace(Vector3D.of(4, 3, -1)).getX(), TEST_EPS);
        Assertions.assertEquals(5.0, line.toSubspace(Vector3D.of(4, 3, 10)).getX(), TEST_EPS);

        Assertions.assertEquals(-5.0, line.toSubspace(Vector3D.of(-4, -3, -1)).getX(), TEST_EPS);
        Assertions.assertEquals(-5.0, line.toSubspace(Vector3D.of(-4, -3, -10)).getX(), TEST_EPS);
    }

    @Test
    public void testPointAt() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(line.getOrigin(), line.pointAt(0.0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, -1), line.pointAt(5.0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -3, -1), line.pointAt(-5.0), TEST_EPS);
    }

    @Test
    public void testToSpace() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, -1), Vector3D.of(4, 3, 0), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(line.getOrigin(), line.toSpace(Vector1D.of(0.0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, -1), line.toSpace(Vector1D.of(5.0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -3, -1), line.toSpace(Vector1D.of(-5.0)), TEST_EPS);
    }

    @Test
    public void testContains() {
        final Vector3D p1 = Vector3D.of(0, 0, 1);
        final Line3D l = Lines3D.fromPoints(p1, Vector3D.of(0, 0, 2), TEST_PRECISION);
        Assertions.assertTrue(l.contains(p1));
        Assertions.assertTrue(l.contains(Vector3D.linearCombination(1.0, p1, 0.3, l.getDirection())));
        final Vector3D u = l.getDirection().orthogonal();
        final Vector3D v = l.getDirection().cross(u);
        for (double alpha = 0; alpha < 2 * Math.PI; alpha += 0.3) {
            Assertions.assertFalse(l.contains(p1.add(Vector3D.linearCombination(Math.cos(alpha), u,
                    Math.sin(alpha), v))));
        }
    }

    @Test
    public void testSimilar() {
        final Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        final Line3D lA  = Lines3D.fromPoints(p1, p2, TEST_PRECISION);
        final Line3D lB  = Lines3D.fromPoints(p2, p1, TEST_PRECISION);
        Assertions.assertTrue(lA.isSimilarTo(lB));
        Assertions.assertFalse(lA.isSimilarTo(Lines3D.fromPoints(p1, p1.add(lA.getDirection().orthogonal()), TEST_PRECISION)));
    }

    @Test
    public void testPointDistance() {
        final Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assertions.assertEquals(Math.sqrt(3.0 / 2.0), l.distance(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assertions.assertEquals(0, l.distance(Vector3D.of(0, -4, -4)), TEST_EPS);
    }

    @Test
    public void testLineDistance() {
        final Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assertions.assertEquals(1.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)),
                            1.0e-10);
        Assertions.assertEquals(0.5,
                            l.distance(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.distance(l),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)),
                            1.0e-10);
        Assertions.assertEquals(Math.sqrt(8),
                            l.distance(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)),
                            1.0e-10);
    }

    @Test
    public void testClosest() {
        final Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assertions.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.5,
                            l.closest(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)).distance(Vector3D.of(-0.5, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.closest(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.closest(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)).distance(Vector3D.of(0, -2, -2)),
                            1.0e-10);
    }

    @Test
    public void testIntersection() {
        final Line3D l = Lines3D.fromPoints(Vector3D.of(0, 1, 1), Vector3D.of(0, 2, 2), TEST_PRECISION);
        Assertions.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(1, 0, 2), TEST_PRECISION)));
        Assertions.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(-0.5, 0, 0), Vector3D.of(-0.5, -1, -1), TEST_PRECISION)));
        Assertions.assertEquals(0.0,
                            l.intersection(l).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -5, -5), TEST_PRECISION)).distance(Vector3D.of(0, 0, 0)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(0, -3, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assertions.assertEquals(0.0,
                            l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, -4), Vector3D.of(1, -4, -4), TEST_PRECISION)).distance(Vector3D.of(0, -4, -4)),
                            1.0e-10);
        Assertions.assertNull(l.intersection(Lines3D.fromPoints(Vector3D.of(0, -4, 0), Vector3D.of(1, -4, 0), TEST_PRECISION)));
    }

    @Test
    public void testReverse() {
        // arrange
        final Line3D line = Lines3D.fromPoints(Vector3D.of(1653345.6696423641, 6170370.041579291, 90000),
                             Vector3D.of(1650757.5050732433, 6160710.879908984, 0.9),
                             TEST_PRECISION);
        final Vector3D expected = line.getDirection().negate();

        // act
        final Line3D reversed = line.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(expected, reversed.getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan() {
        // arrange
        final Line3D line = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final LineConvexSubset3D span = line.span();

        // assert
        Assertions.assertTrue(span.isInfinite());
        Assertions.assertFalse(span.isFinite());

        Assertions.assertNull(span.getStartPoint());
        Assertions.assertNull(span.getEndPoint());

        Assertions.assertNull(span.getCentroid());
        Assertions.assertNull(span.getBounds());

        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(span.getSize());

        Assertions.assertSame(line, span.getLine());
        Assertions.assertTrue(span.getInterval().isFull());
    }

    @Test
    public void testSpan_contains() {
        // arrange
        final double delta = 1e-12;

        final LineConvexSubset3D span = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        for (double x = -10; x <= 10; x += 0.5) {

            // act/assert
            Assertions.assertFalse(span.contains(Vector3D.of(0, 1, 0)));
            Assertions.assertFalse(span.contains(Vector3D.of(0, 0, 1)));

            Assertions.assertTrue(span.contains(Vector3D.of(x, 0, 0)));
            Assertions.assertTrue(span.contains(Vector3D.of(x + delta, delta, delta)));
        }
    }

    @Test
    public void testSpan_transform() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        final LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION)
                .span();

        // act
        final LineConvexSubset3D result = span.transform(t);

        // assert
        Assertions.assertNull(result.getStartPoint());
        Assertions.assertNull(result.getEndPoint());

        Assertions.assertTrue(result.contains(Vector3D.of(0, 1, -1)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan_transform_reflection() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        final LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0),
                Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final LineConvexSubset3D result = span.transform(t);

        // assert
        Assertions.assertNull(result.getStartPoint());
        Assertions.assertNull(result.getEndPoint());

        Assertions.assertTrue(result.contains(Vector3D.of(0, 1, 2)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testSpan_toString() {
        // arrange
        final LineConvexSubset3D span = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)
                .span();

        // act
        final String str = span.toString();

        // assert
        GeometryTestUtils.assertContains("LineSpanningSubset3D[origin= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
    }

    @Test
    public void testSubsetMethods() {
        // arrange
        final Line3D line = Lines3D.fromPoints(Vector3D.of(0, 3, 0), Vector3D.of(1, 3, 0), TEST_PRECISION);

        // act/assert
        final Segment3D doubleArgResult = line.segment(3, 4);
        Assertions.assertSame(line, doubleArgResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 0), doubleArgResult.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 3, 0), doubleArgResult.getEndPoint(), TEST_EPS);

        final Segment3D ptArgResult = line.segment(Vector3D.of(0, 4, 0), Vector3D.of(2, 5, 1));
        Assertions.assertSame(line, ptArgResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), ptArgResult.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 0), ptArgResult.getEndPoint(), TEST_EPS);

        final Ray3D rayDoubleResult = line.rayFrom(2);
        Assertions.assertSame(line, rayDoubleResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 0), rayDoubleResult.getStartPoint(), TEST_EPS);
        Assertions.assertNull(rayDoubleResult.getEndPoint());

        final Ray3D rayPtResult = line.rayFrom(Vector3D.of(1, 4, 0));
        Assertions.assertSame(line, rayPtResult.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 0), rayPtResult.getStartPoint(), TEST_EPS);
        Assertions.assertNull(rayPtResult.getEndPoint());

        final ReverseRay3D toDoubleResult = line.reverseRayTo(-1);
        Assertions.assertSame(line, toDoubleResult.getLine());
        Assertions.assertNull(toDoubleResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 3, 0), toDoubleResult.getEndPoint(), TEST_EPS);

        final ReverseRay3D toPtResult = line.reverseRayTo(Vector3D.of(1, 4, 0));
        Assertions.assertSame(line, toPtResult.getLine());
        Assertions.assertNull(toPtResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 0), toPtResult.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testEq() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final Vector3D p = Vector3D.of(1, 2, 3);
        final Vector3D dir = Vector3D.of(1, 0, 0);

        final Line3D a = Lines3D.fromPointAndDirection(p, dir, precision);
        final Line3D b = Lines3D.fromPointAndDirection(Vector3D.ZERO, dir, precision);
        final Line3D c = Lines3D.fromPointAndDirection(p, Vector3D.of(1, 1, 0), precision);

        final Line3D d = Lines3D.fromPointAndDirection(p, dir, precision);
        final Line3D e = Lines3D.fromPointAndDirection(p.add(Vector3D.of(1e-4, 1e-4, 1e-4)), dir, precision);
        final Line3D f = Lines3D.fromPointAndDirection(p, Vector3D.of(1 + 1e-4, 1e-4, 1e-4), precision);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(d.eq(a, precision));

        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(e.eq(a, precision));

        Assertions.assertTrue(a.eq(f, precision));
        Assertions.assertTrue(f.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Line3D a = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);
        final Line3D b = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, -1), Vector3D.of(4, 5, 6), TEST_PRECISION);
        final Line3D c = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, -1), TEST_PRECISION);
        final Line3D d = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), Precision.doubleEquivalenceOfEpsilon(TEST_EPS + 1e-3));

        final Line3D e = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);

        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Line3D a = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);
        final Line3D b = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, -1), Vector3D.of(4, 5, 6), TEST_PRECISION);
        final Line3D c = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, -1), TEST_PRECISION);
        final Line3D d = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), Precision.doubleEquivalenceOfEpsilon(TEST_EPS + 1e-3));

        final Line3D e = Lines3D.fromPointAndDirection(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);

        Assertions.assertEquals(a, e);
        Assertions.assertEquals(e, a);
    }

    @Test
    public void testToString() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final String str = line.toString();

        // assert
        Assertions.assertTrue(str.contains("Line3D"));
        Assertions.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?, 0(\\.0)?\\).*"));
        Assertions.assertTrue(str.matches(".*direction= \\(1(\\.0)?, 0(\\.0)?, 0(\\.0)?\\).*"));
    }
}
