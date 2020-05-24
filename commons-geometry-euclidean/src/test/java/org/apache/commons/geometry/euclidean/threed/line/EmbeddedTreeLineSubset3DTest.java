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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedTreeLineSubset3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private Line3D testLine = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

    @Test
    public void testCtor_default() {
        // act
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine);

        // assert
        Assert.assertSame(testLine, sub.getLine());
        Assert.assertTrue(sub.getSubspaceRegion().isEmpty());
        Assert.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_true() {
        // act
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        // assert
        Assert.assertSame(testLine, sub.getLine());
        Assert.assertTrue(sub.getSubspaceRegion().isFull());
        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
    }

    @Test
    public void testCtor_false() {
        // act
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, false);

        // assert
        Assert.assertSame(testLine, sub.getLine());
        Assert.assertTrue(sub.getSubspaceRegion().isEmpty());
        Assert.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        // assert
        Assert.assertSame(testLine, sub.getLine());
        Assert.assertSame(tree, sub.getSubspaceRegion());
        Assert.assertEquals(0, sub.getSize(), TEST_EPS);
    }

    @Test
    public void testProperties_full() {
        // arrange
        EmbeddedTreeLineSubset3D full = new EmbeddedTreeLineSubset3D(testLine, true);

        // act/assert
        Assert.assertTrue(full.isInfinite());
        Assert.assertFalse(full.isFinite());

        GeometryTestUtils.assertPositiveInfinity(full.getSize());
        Assert.assertNull(full.getBarycenter());
        Assert.assertNull(full.getBounds());
    }

    @Test
    public void testProperties_empty() {
        // arrange
        EmbeddedTreeLineSubset3D empty = new EmbeddedTreeLineSubset3D(testLine, false);

        // act/assert
        Assert.assertFalse(empty.isInfinite());
        Assert.assertTrue(empty.isFinite());

        Assert.assertEquals(0, empty.getSize(), TEST_EPS);
        Assert.assertNull(empty.getBarycenter());
        Assert.assertNull(empty.getBounds());
    }

    @Test
    public void testProperties_half() {
        // arrange
        EmbeddedTreeLineSubset3D half = new EmbeddedTreeLineSubset3D(testLine, false);
        half.getSubspaceRegion().add(Interval.min(1, TEST_PRECISION));

        // act/assert
        Assert.assertTrue(half.isInfinite());
        Assert.assertFalse(half.isFinite());

        GeometryTestUtils.assertPositiveInfinity(half.getSize());
        Assert.assertNull(half.getBarycenter());
        Assert.assertNull(half.getBounds());
    }

    @Test
    public void testProperties_finite() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), TEST_PRECISION);
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(line);

        double sqrt2 = Math.sqrt(2);
        sub.getSubspaceRegion().add(Interval.of(0, sqrt2, TEST_PRECISION));
        sub.getSubspaceRegion().add(Interval.of(-2 * sqrt2, -sqrt2, TEST_PRECISION));

        // act/assert
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());

        Assert.assertEquals(2 * sqrt2, sub.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -0.5, 1), sub.getBarycenter(), TEST_EPS);

        Bounds3D bounds = sub.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, -2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testTransform_full() {
        // arrange
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        EmbeddedTreeLineSubset3D result = sub.transform(transform);

        // assert
        Line3D resultLine = result.getLine();

        Vector3D expectedOrigin = Lines3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
                .getOrigin();

        EuclideanTestUtils.assertCoordinatesEqual(expectedOrigin, resultLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2).normalize(), resultLine.getDirection(), TEST_EPS);

        Assert.assertTrue(result.getSubspaceRegion().isFull());
    }

    @Test
    public void testTransform_finite() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(
                testLine.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                testLine.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        EmbeddedTreeLineSubset3D result = sub.transform(transform);

        // assert
        Line3D resultLine = result.getLine();

        Vector3D expectedOrigin = Lines3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
                .getOrigin();

        EuclideanTestUtils.assertCoordinatesEqual(expectedOrigin, resultLine.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2).normalize(), resultLine.getDirection(), TEST_EPS);

        Assert.assertFalse(result.getSubspaceRegion().isFull());

        List<Interval> intervals = result.getSubspaceRegion().toIntervals();
        Assert.assertEquals(1, intervals.size());

        Interval resultInterval = intervals.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -4),
                resultLine.toSpace(resultInterval.getMin()), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, -6),
                resultLine.toSpace(resultInterval.getMax()), TEST_EPS);
    }

    @Test
    public void testToConvex_full() {
        // arrange
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, true);

        // act
        List<LineConvexSubset3D> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(segments.get(0).getSubspaceRegion().isFull());
    }

    @Test
    public void testToConvex_finite() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(
                testLine.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                testLine.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine, tree);

        // act
        List<LineConvexSubset3D> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());

        LineConvexSubset3D segment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 0), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testToString() {
        // arrange
        EmbeddedTreeLineSubset3D sub = new EmbeddedTreeLineSubset3D(testLine);

        // act
        String str = sub.toString();

        // assert
        Assert.assertTrue(str.contains("EmbeddedTreeLineSubset3D[lineOrigin= "));
        Assert.assertTrue(str.contains(", lineDirection= "));
        Assert.assertTrue(str.contains(", region= "));
    }
}
