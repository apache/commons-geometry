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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvexVolumeTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        final ConvexVolume vol = ConvexVolume.full();

        // assert
        Assertions.assertTrue(vol.isFull());
        Assertions.assertFalse(vol.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(vol.getSize());
        Assertions.assertNull(vol.getCentroid());

        Assertions.assertEquals(0, vol.getBoundaries().size());
        Assertions.assertEquals(0, vol.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final ConvexVolume volume = ConvexVolume.fromBounds(plane);

        // act
        final List<PlaneConvexSubset> boundaries = volume.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(1, boundaries.size());

        final PlaneConvexSubset sp = boundaries.get(0);
        Assertions.assertEquals(0, sp.getEmbedded().getSubspaceRegion().getBoundaries().size());
        EuclideanTestUtils.assertCoordinatesEqual(plane.getOrigin(), sp.getPlane().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(plane.getNormal(), sp.getPlane().getNormal(), TEST_EPS);
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        final ConvexVolume volume = ConvexVolume.full();

        // act
        final List<PlaneConvexSubset> boundaries = volume.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, boundaries.size());
    }

    @Test
    public void testTriangleStream_noBoundaries() {
        // arrange
        final ConvexVolume full = ConvexVolume.full();

        // act
        final List<Triangle3D> tris = full.triangleStream().collect(Collectors.toList());

        // act/assert
        Assertions.assertEquals(0, tris.size());
    }

    @Test
    public void testTriangleStream_infinite() {
        // arrange
        final Pattern pattern = Pattern.compile("^Cannot convert infinite plane subset to triangles: .*");

        final ConvexVolume half = ConvexVolume.fromBounds(
                Planes.fromNormal(Vector3D.Unit.MINUS_X, TEST_PRECISION)
            );

        final ConvexVolume quadrant = ConvexVolume.fromBounds(
                    Planes.fromNormal(Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Planes.fromNormal(Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Planes.fromNormal(Vector3D.Unit.MINUS_Z, TEST_PRECISION)
                );

        // act/assert
        assertThrows(IllegalStateException.class, () -> half.triangleStream().collect(Collectors.toList()));
        assertThrows(IllegalStateException.class, () -> quadrant.triangleStream().collect(Collectors.toList()));
    }

    @Test
    public void testTriangleStream_finite() {
        // arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final ConvexVolume box = ConvexVolume.fromBounds(
                    Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_Z, TEST_PRECISION),

                    Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                );

        // act
        final List<Triangle3D> tris = box.triangleStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(12, tris.size());

        final Bounds3D.Builder boundsBuilder = Bounds3D.builder();
        tris.forEach(t -> boundsBuilder.addAll(t.getVertices()));

        final Bounds3D bounds = boundsBuilder.build();
        EuclideanTestUtils.assertCoordinatesEqual(min, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        final ConvexVolume full = ConvexVolume.full();
        final ConvexVolume halfFull = ConvexVolume.fromBounds(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act/assert
        Assertions.assertNull(full.getBounds());
        Assertions.assertNull(halfFull.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final ConvexVolume vol = rect(Vector3D.of(1, 1, 1), 0.5, 1, 2);

        // act
        final Bounds3D bounds = vol.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0, -1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2, 3), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testToTree_full() {
        // arrange
        final ConvexVolume volume = ConvexVolume.full();

        // act
        final RegionBSPTree3D tree = volume.toTree();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree() {
        // arrange
        final ConvexVolume volume = ConvexVolume.fromBounds(
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION),

                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                );

        // act
        final RegionBSPTree3D tree = volume.toTree();

        // assert
        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));
    }

    @Test
    public void testFromBounds_noPlanes() {
        // act
        final ConvexVolume vol = ConvexVolume.fromBounds();

        // assert
        Assertions.assertSame(ConvexVolume.full(), vol);
    }

    @Test
    public void testFromBounds_halfspace() {
        // act
        final ConvexVolume vol = ConvexVolume.fromBounds(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // assert
        Assertions.assertFalse(vol.isFull());
        Assertions.assertFalse(vol.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(vol.getSize());
        Assertions.assertNull(vol.getCentroid());

        Assertions.assertEquals(1, vol.getBoundaries().size());
        GeometryTestUtils.assertPositiveInfinity(vol.getBoundarySize());

        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testFromBounds_cube() {
        // act
        final ConvexVolume vol = rect(Vector3D.of(1, 1, 1), 0.5, 1, 2);

        // assert
        Assertions.assertFalse(vol.isFull());
        Assertions.assertFalse(vol.isEmpty());

        Assertions.assertEquals(8, vol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), vol.getCentroid(), TEST_EPS);

        Assertions.assertEquals(6, vol.getBoundaries().size());
        Assertions.assertEquals(28, vol.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.INSIDE, Vector3D.of(1, 1, 1));

        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.BOUNDARY,
                Vector3D.of(0.5, 0, -1), Vector3D.of(1.5, 2, 3));

        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(2, 1, 1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, 3, 1),
                Vector3D.of(1, 1, -2), Vector3D.of(1, 1, 4));
    }

    @Test
    public void testTrim() {
        // arrange
        final ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        final PlaneConvexSubset subplane = Planes.subsetFromConvexArea(
                Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION).getEmbedding(), ConvexArea.full());

        // act
        final PlaneConvexSubset trimmed = vol.trim(subplane);

        // assert
        Assertions.assertEquals(1, trimmed.getSize(), TEST_EPS);

        final List<Vector3D> vertices = trimmed.getVertices();

        Assertions.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0.5, -0.5), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0.5, 0.5), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -0.5, 0.5), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -0.5, -0.5), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testSplit() {
        // arrange
        final ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        final Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<ConvexVolume> split = vol.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexVolume minus = split.getMinus();
        Assertions.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.25, 0, 0), minus.getCentroid(), TEST_EPS);

        final ConvexVolume plus = split.getPlus();
        Assertions.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.25, 0, 0), plus.getCentroid(), TEST_EPS);
    }

    @Test
    public void testLinecast_full() {
        // arrange
        final ConvexVolume volume = ConvexVolume.full();

        // act/assert
        LinecastChecker3D.with(volume)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(volume)
            .expectNothing()
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        final ConvexVolume volume = rect(Vector3D.of(0.5, 0.5, 0.5), 0.5, 0.5, 0.5);

        // act/assert
        LinecastChecker3D.with(volume)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.of(0, 5, 5), Vector3D.of(1, 5, 5), TEST_PRECISION));

        LinecastChecker3D.with(volume)
            .expect(Vector3D.ZERO, Vector3D.Unit.MINUS_X)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION));

        LinecastChecker3D.with(volume)
            .expect(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1, 1, 1), TEST_PRECISION));
    }

    @Test
    public void testTransform() {
        // arrange
        final ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 2, 3))
                .scale(Vector3D.of(2, 1, 1));

        // act
        final ConvexVolume transformed = vol.transform(transform);

        // assert
        Assertions.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assertions.assertEquals(10, transformed.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 3), transformed.getCentroid(), TEST_EPS);
    }

    private static ConvexVolume rect(final Vector3D center, final double xDelta, final double yDelta, final double zDelta) {
        final List<Plane> planes = Arrays.asList(
                    Planes.fromPointAndNormal(center.add(Vector3D.of(xDelta, 0, 0)), Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(center.add(Vector3D.of(-xDelta, 0, 0)), Vector3D.Unit.MINUS_X, TEST_PRECISION),

                    Planes.fromPointAndNormal(center.add(Vector3D.of(0, yDelta, 0)), Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(center.add(Vector3D.of(0, -yDelta, 0)), Vector3D.Unit.MINUS_Y, TEST_PRECISION),

                    Planes.fromPointAndNormal(center.add(Vector3D.of(0, 0, zDelta)), Vector3D.Unit.PLUS_Z, TEST_PRECISION),
                    Planes.fromPointAndNormal(center.add(Vector3D.of(0, 0, -zDelta)), Vector3D.Unit.MINUS_Z, TEST_PRECISION)
                );

        return ConvexVolume.fromBounds(planes);
    }
}
