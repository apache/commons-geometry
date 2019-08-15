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
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
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
    public void testToConvex() {
        // arrange
        ConvexVolume vol = ConvexVolume.fromBounds(Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION));

        // act
        List<ConvexVolume> result = vol.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        Assert.assertSame(vol, result.get(0));
    }

    @Test
    public void testTOTree() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_Z, TEST_PRECISION),

                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_Z, TEST_PRECISION)
                );

        // act
        RegionBSPTree3D tree = volume.toTree();

        // assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
        checkClassify(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));
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
        ConvexVolume vol = ConvexVolume.fromBounds(Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION));

        // assert
        Assert.assertFalse(vol.isFull());
        Assert.assertFalse(vol.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(vol.getSize());
        Assert.assertNull(vol.getBarycenter());

        Assert.assertEquals(1, vol.getBoundaries().size());
        GeometryTestUtils.assertPositiveInfinity(vol.getBoundarySize());

        checkClassify(vol, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        checkClassify(vol, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
        checkClassify(vol, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
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

        checkClassify(vol, RegionLocation.INSIDE, Vector3D.of(1, 1, 1));

        checkClassify(vol, RegionLocation.BOUNDARY,
                Vector3D.of(0.5, 0, -1), Vector3D.of(1.5, 2, 3));

        checkClassify(vol, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(2, 1, 1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, 3, 1),
                Vector3D.of(1, 1, -2), Vector3D.of(1, 1, 4));
    }

    @Test
    public void testTrim() {
        // arrange
        ConvexVolume vol = rect(Vector3D.ZERO, 0.5, 0.5, 0.5);

        ConvexSubPlane subplane = ConvexSubPlane.fromConvexArea(
                Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION), ConvexArea.full());

        // act
        ConvexSubPlane trimmed = vol.trim(subplane);

        // assert
        Assert.assertEquals(1, trimmed.getSize(), TEST_EPS);

        List<Vector3D> vertices = trimmed.getPlane().toSpace(
                trimmed.getSubspaceRegion().getBoundaryPaths().get(0).getVertices());

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

        Plane splitter = Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION);

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
                    Plane.fromPointAndNormal(center.add(Vector3D.of(xDelta, 0, 0)), Vector3D.PLUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(center.add(Vector3D.of(-xDelta, 0, 0)), Vector3D.MINUS_X, TEST_PRECISION),

                    Plane.fromPointAndNormal(center.add(Vector3D.of(0, yDelta, 0)), Vector3D.PLUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(center.add(Vector3D.of(0, -yDelta, 0)), Vector3D.MINUS_Y, TEST_PRECISION),

                    Plane.fromPointAndNormal(center.add(Vector3D.of(0, 0, zDelta)), Vector3D.PLUS_Z, TEST_PRECISION),
                    Plane.fromPointAndNormal(center.add(Vector3D.of( 0, 0, -zDelta)), Vector3D.MINUS_Z, TEST_PRECISION)
                );

        return ConvexVolume.fromBounds(planes);
    }

    private static void checkClassify(Region<Vector3D> region, RegionLocation loc, Vector3D ... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, region.classify(pt));
        }
    }
}
