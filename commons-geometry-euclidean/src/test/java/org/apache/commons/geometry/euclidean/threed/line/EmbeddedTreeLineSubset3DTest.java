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

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmbeddedTreeLineSubset3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private final Line3D testLine = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

    @Test
    void testCtor_default() {
        // act
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine);

        // assert
        Assertions.assertSame(testLine, sub.getLine());
        Assertions.assertTrue(sub.getSubspaceRegion().isEmpty());
        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    void testCtor_true() {
        // act
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        // assert
        Assertions.assertSame(testLine, sub.getLine());
        Assertions.assertTrue(sub.getSubspaceRegion().isFull());
        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
    }

    @Test
    void testCtor_false() {
        // act
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, false);

        // assert
        Assertions.assertSame(testLine, sub.getLine());
        Assertions.assertTrue(sub.getSubspaceRegion().isEmpty());
        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    void testCtor_lineAndRegion() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        // assert
        Assertions.assertSame(testLine, sub.getLine());
        Assertions.assertSame(tree, sub.getSubspaceRegion());
        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    void testProperties_full() {
        // arrange
        final EmbeddedTreeLineSubset3D full = new EmbeddedTreeLineSubset3D(testLine, true);

        // act/assert
        Assertions.assertTrue(full.isInfinite());
        Assertions.assertFalse(full.isFinite());

        GeometryTestUtils.assertPositiveInfinity(full.getSize());
        Assertions.assertNull(full.getCentroid());
        Assertions.assertNull(full.getBounds());
    }

    @Test
    void testProperties_empty() {
        // arrange
        final EmbeddedTreeLineSubset3D empty = new EmbeddedTreeLineSubset3D(testLine, false);

        // act/assert
        Assertions.assertFalse(empty.isInfinite());
        Assertions.assertTrue(empty.isFinite());

        Assertions.assertEquals(0, empty.getSize(), TEST_EPS);
        Assertions.assertNull(empty.getCentroid());
        Assertions.assertNull(empty.getBounds());
    }

    @Test
    void testProperties_half() {
        // arrange
        final EmbeddedTreeLineSubset3D half = new EmbeddedTreeLineSubset3D(testLine, false);
        half.getSubspaceRegion().add(Interval.min(1, TEST_PRECISION));

        // act/assert
        Assertions.assertTrue(half.isInfinite());
        Assertions.assertFalse(half.isFinite());

        GeometryTestUtils.assertPositiveInfinity(half.getSize());
        Assertions.assertNull(half.getCentroid());
        Assertions.assertNull(half.getBounds());
    }

    @Test
    void testProperties_finite() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), TEST_PRECISION);
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(line);

        final double sqrt2 = Math.sqrt(2);
        sub.getSubspaceRegion().add(Interval.of(0, sqrt2, TEST_PRECISION));
        sub.getSubspaceRegion().add(Interval.of(-2 * sqrt2, -sqrt2, TEST_PRECISION));

        // act/assert
        Assertions.assertFalse(sub.isInfinite());
        Assertions.assertTrue(sub.isFinite());

        Assertions.assertEquals(2 * sqrt2, sub.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -0.5, 1), sub.getCentroid(), TEST_EPS);

        final Bounds3D bounds = sub.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, -2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    void testTransform_full() {
        // arrange
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final EmbeddedTreeLineSubset3D result = sub.transform(transform);

        // assert
        final Line3D resultLine = result.getLine();

        final Vector3D expectedOrigin = Lines3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
                .getOrigin();

        EuclideanTestUtils.assertCoordinatesEqual(expectedOrigin, resultLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2).normalize(), resultLine.getDirection(), TEST_EPS);

        Assertions.assertTrue(result.getSubspaceRegion().isFull());
    }

    @Test
    void testTransform_finite() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(
                testLine.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                testLine.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final EmbeddedTreeLineSubset3D result = sub.transform(transform);

        // assert
        final Line3D resultLine = result.getLine();

        final Vector3D expectedOrigin = Lines3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
                .getOrigin();

        EuclideanTestUtils.assertCoordinatesEqual(expectedOrigin, resultLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2).normalize(), resultLine.getDirection(), TEST_EPS);

        Assertions.assertFalse(result.getSubspaceRegion().isFull());

        final List<Interval> intervals = result.getSubspaceRegion().toIntervals();
        Assertions.assertEquals(1, intervals.size());

        final Interval resultInterval = intervals.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -4),
                resultLine.toSpace(resultInterval.getMin()), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, -6),
                resultLine.toSpace(resultInterval.getMax()), TEST_EPS);
    }

    @Test
    void testToConvex_full() {
        // arrange
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        // act
        final List<LineConvexSubset3D> segments = sub.toConvex();

        // assert
        Assertions.assertEquals(1, segments.size());
        Assertions.assertTrue(segments.get(0).getSubspaceRegion().isFull());
    }

    @Test
    void testToConvex_finite() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(
                testLine.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                testLine.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        // act
        final List<LineConvexSubset3D> segments = sub.toConvex();

        // assert
        Assertions.assertEquals(1, segments.size());

        final LineConvexSubset3D segment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 0), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    void testToString() {
        // arrange
        final EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine);

        // act
        final String str = sub.toString();

        // assert
        Assertions.assertTrue(str.contains("EmbeddedTreeLineSubset3D[lineOrigin= "));
        Assertions.assertTrue(str.contains(", lineDirection= "));
        Assertions.assertTrue(str.contains(", region= "));
    }
}
