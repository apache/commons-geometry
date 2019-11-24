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
package org.apache.commons.geometry.euclidean.threed;

import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

public class SubLine3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

    @Test
    public void testCtor_default() {
        // act
        SubLine3D sub = new SubLine3D(line);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());
        Assert.assertTrue(sub.getSubspaceRegion().isEmpty());
    }

    @Test
    public void testCtor_true() {
        // act
        SubLine3D sub = new SubLine3D(line, true);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());
        Assert.assertTrue(sub.getSubspaceRegion().isFull());
    }

    @Test
    public void testCtor_false() {
        // act
        SubLine3D sub = new SubLine3D(line, false);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());
        Assert.assertTrue(sub.getSubspaceRegion().isEmpty());
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act
        SubLine3D sub = new SubLine3D(line, tree);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());
        Assert.assertSame(tree, sub.getSubspaceRegion());
    }

    @Test
    public void testTransform_full() {
        // arrange
        SubLine3D sub = new SubLine3D(line, true);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Geometry.HALF_PI));

        // act
        SubLine3D result = sub.transform(transform);

        // assert
        Line3D resultLine = result.getLine();

        Vector3D expectedOrigin = Line3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
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
                line.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                line.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        SubLine3D sub = new SubLine3D(line, tree);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 0, 0))
                .scale(Vector3D.of(2, 1, 1))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Geometry.HALF_PI));

        // act
        SubLine3D result = sub.transform(transform);

        // assert
        Line3D resultLine = result.getLine();

        Vector3D expectedOrigin = Line3D.fromPoints(Vector3D.of(0, 0, -2), Vector3D.of(0, 1, -4), TEST_PRECISION)
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
        SubLine3D sub = new SubLine3D(line, true);

        // act
        List<Segment3D> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(segments.get(0).getSubspaceRegion().isFull());
    }

    @Test
    public void testToConvex_finite() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(
                line.toSubspace(Vector3D.of(1, 1, 0)).getX(),
                line.toSubspace(Vector3D.of(2, 2, 0)).getX(), TEST_PRECISION));

        SubLine3D sub = new SubLine3D(line, tree);

        // act
        List<Segment3D> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());

        Segment3D segment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 0), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testToString() {
        // arrange
        SubLine3D sub = new SubLine3D(line);

        // act
        String str = sub.toString();

        // assert
        Assert.assertTrue(str.contains("SubLine3D[lineOrigin= "));
        Assert.assertTrue(str.contains(", lineDirection= "));
        Assert.assertTrue(str.contains(", region= "));
    }
}
