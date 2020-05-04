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
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.lines.Lines3D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.junit.Assert;
import org.junit.Test;

public class ConvexVolumeTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        ConvexVolume vol = ConvexVolume.full();

        // assert
        Assert.assertTrue(vol.isFull());
        Assert.assertFalse(vol.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(vol.getSize());
        Assert.assertNull(vol.getBarycenter());

        Assert.assertEquals(0, vol.getBoundaries().size());
        Assert.assertEquals(0, vol.getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        ConvexVolume volume = ConvexVolume.fromBounds(plane);

        // act
        List<PlaneConvexSubset> boundaries = volume.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, boundaries.size());

        PlaneConvexSubset sp = boundaries.get(0);
        Assert.assertEquals(0, sp.getSubspaceRegion().getBoundaries().size());
        Assert.assertSame(plane, sp.getPlane());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        ConvexVolume volume = ConvexVolume.full();

        // act
        List<PlaneConvexSubset> boundaries = volume.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, boundaries.size());
    }

    @Test
    public void testToTree_full() {
        // arrange
        ConvexVolume volume = ConvexVolume.full();

        // act
        RegionBSPTree3D tree = volume.toTree();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION),

                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                );

        // act
        RegionBSPTree3D tree = volume.toTree();

        // assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

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
        ConvexVolume vol = ConvexVolume.fromBounds();

        // assert
        Assert.assertSame(ConvexVolume.full(), vol);
    }

    @Test
    public void testFromBounds_halfspace() {
        // act
        ConvexVolume vol = ConvexVolume.fromBounds(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // assert
        Assert.assertFalse(vol.isFull());
        Assert.assertFalse(vol.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(vol.getSize());
        Assert.assertNull(vol.getBarycenter());

        Assert.assertEquals(1, vol.getBoundaries().size());
        GeometryTestUtils.assertPositiveInfinity(vol.getBoundarySize());

        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
        EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testFromBounds_cube() {
        // act
        ConvexVolume vol = rect(Vector3D.of(1, 1, 1), 0.5, 1, 2);

        // assert
        Assert.assertFalse(vol.isFull());
        Assert.assertFalse(vol.isEmpty());

        Assert.assertEquals(8, vol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), vol.getBarycenter(), TEST_EPS);

        Assert.assertEquals(6, vol.getBoundaries().size());
        Assert.assertEquals(28, vol.getBoundarySize(), TEST_EPS);

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
        ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        PlaneConvexSubset subplane = Planes.subsetFromConvexArea(
                Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION), ConvexArea.full());

        // act
        PlaneConvexSubset trimmed = vol.trim(subplane);

        // assert
        Assert.assertEquals(1, trimmed.getSize(), TEST_EPS);

        List<Vector3D> vertices = trimmed.getPlane().toSpace(
                trimmed.getSubspaceRegion().getBoundaryPaths().get(0).getVertexSequence());

        Assert.assertEquals(5, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0.5, -0.5), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0.5, 0.5), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -0.5, 0.5), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -0.5, -0.5), vertices.get(3), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0.5, -0.5), vertices.get(4), TEST_EPS);
    }

    @Test
    public void testSplit() {
        // arrange
        ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<ConvexVolume> split = vol.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexVolume minus = split.getMinus();
        Assert.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.25, 0, 0), minus.getBarycenter(), TEST_EPS);

        ConvexVolume plus = split.getPlus();
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.25, 0, 0), plus.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testLinecast_full() {
        // arrange
        ConvexVolume volume = ConvexVolume.full();

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
        ConvexVolume volume = rect(Vector3D.of(0.5, 0.5, 0.5), 0.5, 0.5, 0.5);

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
        ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .translate(Vector3D.of(1, 2, 3))
                .scale(Vector3D.of(2, 1, 1));

        // act
        ConvexVolume transformed = vol.transform(transform);

        // assert
        Assert.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assert.assertEquals(10, transformed.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 3), transformed.getBarycenter(), TEST_EPS);
    }

    private static ConvexVolume rect(Vector3D center, double xDelta, double yDelta, double zDelta) {
        List<Plane> planes = Arrays.asList(
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
