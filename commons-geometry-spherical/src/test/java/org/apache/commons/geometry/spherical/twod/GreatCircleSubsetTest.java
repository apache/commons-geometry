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

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class GreatCircleSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle XY_CIRCLE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    public void testBuilder_empty() {
        // act
        HyperplaneSubset.Builder<Point2S> builder = XY_CIRCLE.span().builder();

        GreatCircleSubset result = (GreatCircleSubset) builder.build();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isInfinite());
        Assert.assertTrue(result.isFinite());

        Assert.assertEquals(0, result.getSize(), TEST_EPS);
    }

    @Test
    public void testBuilder_addSingleConvex_returnsSameInstance() {
        // arrange
        GreatArc convex = XY_CIRCLE.arc(0, 1);

        // act
        HyperplaneSubset.Builder<Point2S> builder = XY_CIRCLE.span().builder();

        builder.add(convex);

        GreatCircleSubset result = (GreatCircleSubset) builder.build();

        // assert
        Assert.assertSame(convex, result);
    }

    @Test
    public void testBuilder_addSingleTreeSubset() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.PLUS_J, TEST_PRECISION);

        RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI, TEST_PRECISION));
        region.add(AngularInterval.of(0, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, region);

        // act
        HyperplaneSubset.Builder<Point2S> builder = circle.span().builder();

        builder.add(sub);

        GreatCircleSubset result = (GreatCircleSubset) builder.build();

        // assert
        Assert.assertNotSame(sub, result);

        List<GreatArc> arcs = result.toConvex();

        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.MINUS_K, Point2S.PLUS_J);
        checkArc(arcs.get(1), Point2S.PLUS_K, Point2S.MINUS_J);
    }

    @Test
    public void testBuilder_addMixed_convexFirst() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);

        RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(PlaneAngleRadians.PI, 1.25 * PlaneAngleRadians.PI, TEST_PRECISION));
        region.add(AngularInterval.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, region);

        // act
        HyperplaneSubset.Builder<Point2S> builder = circle.span().builder();

        builder.add(circle.arc(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));
        builder.add(circle.arc(1.6 * PlaneAngleRadians.PI, 0.2 * PlaneAngleRadians.PI));
        builder.add(sub);

        GreatCircleSubset result = (GreatCircleSubset) builder.build();

        // assert
        List<GreatArc> arcs = result.toConvex();

        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI));
        checkArc(arcs.get(1), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    public void testBuilder_addMixed_treeSubsetFirst() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);

        RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(PlaneAngleRadians.PI, 1.25 * PlaneAngleRadians.PI, TEST_PRECISION));
        region.add(AngularInterval.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, region);

        // act
        HyperplaneSubset.Builder<Point2S> builder = circle.span().builder();

        builder.add(sub);
        builder.add(circle.arc(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        GreatCircleSubset result = (GreatCircleSubset) builder.build();

        // assert
        List<GreatArc> arcs = result.toConvex();

        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI));
        checkArc(arcs.get(1), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    public void testBuilder_nullArguments() {
     // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);

        HyperplaneSubset.Builder<Point2S> builder = circle.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneSubset<Point2S>) null);
        }, NullPointerException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneConvexSubset<Point2S>) null);
        }, NullPointerException.class, "Hyperplane subset must not be null");
    }

    @Test
    public void testBuilder_argumentsFromDifferentGreatCircle() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle otherCircle = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);

        EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);

        HyperplaneSubset.Builder<Point2S> builder = sub.builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(otherCircle.span());
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new EmbeddedTreeGreatCircleSubset(otherCircle));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new UnknownHyperplaneSubset());
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_unknownSubsetType() {
     // arrange
        GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);

        HyperplaneSubset.Builder<Point2S> builder = circle.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(new UnknownHyperplaneSubset());
        }, IllegalArgumentException.class);
    }

    private static void checkArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static class UnknownHyperplaneSubset implements HyperplaneSubset<Point2S> {

        @Override
        public Split<? extends HyperplaneSubset<Point2S>> split(Hyperplane<Point2S> splitter) {
            return null;
        }

        @Override
        public Hyperplane<Point2S> getHyperplane() {
            return null;
        }

        @Override
        public boolean isFull() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isInfinite() {
            return false;
        }

        @Override
        public boolean isFinite() {
            return false;
        }

        @Override
        public double getSize() {
            return 0;
        }

        @Override
        public Point2S getBarycenter() {
            return null;
        }

        @Override
        public RegionLocation classify(Point2S point) {
            return null;
        }

        @Override
        public Point2S closest(Point2S point) {
            return null;
        }

        @Override
        public Builder<Point2S> builder() {
            return null;
        }

        @Override
        public HyperplaneSubset<Point2S> transform(Transform<Point2S> transform) {
            return null;
        }

        @Override
        public List<? extends HyperplaneConvexSubset<Point2S>> toConvex() {
            return null;
        }
    }
}
