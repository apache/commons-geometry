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

import java.util.Arrays;
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
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

public class AbstractPlaneSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final EmbeddingPlane XY_PLANE = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testBuilder_empty() {
        // act
        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        PlaneSubset result = (PlaneSubset) builder.build();

        // assert
        Assert.assertSame(XY_PLANE, result.getPlane());

        Assert.assertFalse(result.isFull());
        Assert.assertTrue(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(0, result.getSize(), TEST_EPS);

        checkPoints(result, RegionLocation.OUTSIDE, Vector3D.ZERO);
    }

    @Test
    public void testBuilder_addSingleConvex_returnsSameInstance() {
        // arrange
        PlaneConvexSubset convex = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO,
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(0, 1, 0)
                ), TEST_PRECISION);

        // act
        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        builder.add(convex);

        PlaneSubset result = (PlaneSubset) builder.build();

        // assert
        Assert.assertSame(convex, result);
    }

    @Test
    public void testBuilder_addSingleTreeSubset() {
        // arrange
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.of(1, 0),
                    Vector2D.of(0, 1)
                ), TEST_PRECISION);
        EmbeddedTreePlaneSubset treeSubset = new EmbeddedTreePlaneSubset(XY_PLANE, area.toTree());

        // act
        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        builder.add(treeSubset);

        PlaneSubset result = (PlaneSubset) builder.build();

        // assert
        Assert.assertNotSame(treeSubset, result);

        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(0.5, result.getSize(), TEST_EPS);

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(0.25, 0.25, 0));
        checkPoints(result, RegionLocation.OUTSIDE,
                Vector3D.of(0.25, 0.25, 1), Vector3D.of(0.25, 0.25, -1),
                Vector3D.of(1, 0.25, 0), Vector3D.of(-1, 0.25, 0),
                Vector3D.of(0.25, 1, 0), Vector3D.of(0.25, -1, 0));
        checkPoints(result, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
    }

    @Test
    public void testBuilder_addMixed_convexFirst() {
        // arrange
        EmbeddingPlane mainPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        ConvexArea a = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1).normalize()), TEST_PRECISION);
        ConvexArea b = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 1).normalize(), Vector2D.of(0, 1)), TEST_PRECISION);
        ConvexArea c = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y), TEST_PRECISION);

        EmbeddingPlane closePlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(1e-16, 0, 1), Vector3D.of(1, 1e-16, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        HyperplaneSubset.Builder<Vector3D> builder = mainPlane.span().builder();

        builder.add(Planes.subsetFromConvexArea(closePlane, a));
        builder.add(Planes.subsetFromConvexArea(closePlane, b));
        builder.add(new EmbeddedTreePlaneSubset(closePlane, c.toTree()));

        PlaneSubset result = (PlaneSubset) builder.build();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 1));
        checkPoints(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 1), Vector3D.of(2, 0.5, 1),
                Vector3D.of(0.5, -1, 1), Vector3D.of(0.5, 2, 1));
        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(0, 1, 1));
    }

    @Test
    public void testBuilder_addMixed_treeSubsetFirst() {
        // arrange
        EmbeddingPlane mainPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        ConvexArea a = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1).normalize()), TEST_PRECISION);
        ConvexArea b = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 1).normalize(), Vector2D.of(0, 1)), TEST_PRECISION);
        ConvexArea c = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y), TEST_PRECISION);

        EmbeddingPlane closePlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(1e-16, 0, 1), Vector3D.of(1, 1e-16, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        HyperplaneSubset.Builder<Vector3D> builder = mainPlane.span().builder();

        builder.add(new EmbeddedTreePlaneSubset(closePlane, c.toTree()));
        builder.add(Planes.subsetFromConvexArea(closePlane, a));
        builder.add(Planes.subsetFromConvexArea(closePlane, b));

        PlaneSubset result = (PlaneSubset) builder.build();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 1));
        checkPoints(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 1), Vector3D.of(2, 0.5, 1),
                Vector3D.of(0.5, -1, 1), Vector3D.of(0.5, 2, 1));
        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(0, 1, 1));
    }

    @Test
    public void testBuilder_add_nullArguments() {
        // arrange
        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneSubset<Vector3D>) null);
        }, NullPointerException.class, "Hyperplane subset must not be null");

        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneConvexSubset<Vector3D>) null);
        }, NullPointerException.class, "Hyperplane subset must not be null");
    }

    @Test
    public void testBuilder_add_argumentsFromDifferentPlanes() {
        // arrange
        ConvexPolygon3D convex = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 1),
                Vector3D.of(0, 1, 1)
            ), TEST_PRECISION);

        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.add(convex.getEmbedded().getSubspaceRegion());

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(convex);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new EmbeddedTreePlaneSubset(convex.getPlane().getEmbedding(), tree));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_add_addUnknownType() {
        // arrange
        HyperplaneSubset.Builder<Vector3D> builder = XY_PLANE.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(new StubPlaneSubset(XY_PLANE));
        }, IllegalArgumentException.class);
    }

    private static void checkPoints(PlaneSubset ps, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, ps.classify(pt));
        }
    }

    private static class StubPlaneSubset extends AbstractPlaneSubset {

        private final Plane plane;

        StubPlaneSubset(final Plane plane) {
            this.plane = plane;
        }

        @Override
        public Plane getPlane() {
            return plane;
        }

        @Override
        public List<PlaneConvexSubset> toConvex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Triangle3D> toTriangles() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isFull() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegionLocation classify(Vector3D pt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vector3D closest(Vector3D pt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HyperplaneSubset<Vector3D> transform(Transform<Vector3D> transform) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Split<? extends HyperplaneSubset<Vector3D>> split(Hyperplane<Vector3D> splitter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vector3D getCentroid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PlaneSubset.Embedded getEmbedded() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bounds3D getBounds() {
            throw new UnsupportedOperationException();
        }
    }
}
