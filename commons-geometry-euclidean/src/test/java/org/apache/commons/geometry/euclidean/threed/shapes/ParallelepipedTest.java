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
package org.apache.commons.geometry.euclidean.threed.shapes;

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexSubPlane;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.Assert;
import org.junit.Test;

public class ParallelepipedTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testAxisAligned_minFirst() {
        // act
        List<ConvexSubPlane> boundaries =
                Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);

        // assert
        Assert.assertEquals(6, boundaries.size());

        Vector3D b1 = Vector3D.of(1, 2, 3);
        Vector3D b2 = Vector3D.of(4, 2, 3);
        Vector3D b3 = Vector3D.of(4, 5, 3);
        Vector3D b4 = Vector3D.of(1, 5, 3);

        Vector3D t1 = Vector3D.of(1, 2, 6);
        Vector3D t2 = Vector3D.of(4, 2, 6);
        Vector3D t3 = Vector3D.of(4, 5, 6);
        Vector3D t4 = Vector3D.of(1, 5, 6);

        checkVertices(boundaries.get(0), b1, b4, b3, b2, b1);
        checkVertices(boundaries.get(1), t1, t2, t3, t4, t1);

        checkVertices(boundaries.get(2), b1, t1, t4, b4, b1);
        checkVertices(boundaries.get(3), t2, b2, b3, t3, t2);

        checkVertices(boundaries.get(4), b1, b2, t2, t1, b1);
        checkVertices(boundaries.get(5), b4, t4, t3, b3, b4);
    }

    @Test
    public void testAxisAligned_maxFirst() {
        // act
        List<ConvexSubPlane> boundaries =
                Parallelepiped.axisAligned(Vector3D.of(4, 5, 6), Vector3D.of(1, 2, 3), TEST_PRECISION);

        // assert
        Assert.assertEquals(6, boundaries.size());

        Vector3D b1 = Vector3D.of(1, 2, 3);
        Vector3D b2 = Vector3D.of(4, 2, 3);
        Vector3D b3 = Vector3D.of(4, 5, 3);
        Vector3D b4 = Vector3D.of(1, 5, 3);

        Vector3D t1 = Vector3D.of(1, 2, 6);
        Vector3D t2 = Vector3D.of(4, 2, 6);
        Vector3D t3 = Vector3D.of(4, 5, 6);
        Vector3D t4 = Vector3D.of(1, 5, 6);

        checkVertices(boundaries.get(0), b1, b4, b3, b2, b1);
        checkVertices(boundaries.get(1), t1, t2, t3, t4, t1);

        checkVertices(boundaries.get(2), b1, t1, t4, b4, b1);
        checkVertices(boundaries.get(3), t2, b2, b3, t3, t2);

        checkVertices(boundaries.get(4), b1, b2, t2, t1, b1);
        checkVertices(boundaries.get(5), b4, t4, t3, b3, b4);
    }

    @Test
    public void testAxisAligned_toTree() {
        // arrange
        BoundarySource3D src = BoundarySource3D.from(
                Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION));

        // act
        RegionBSPTree3D tree = src.toTree();

        // assert
        Assert.assertEquals(27, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2.5, 3.5, 4.5), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testAxisAligned_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(1, 5, 6), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 2, 6), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(1, 5, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    private static void checkVertices(ConvexSubPlane sp, Vector3D... pts) {
        List<Vector3D> actual = sp.getPlane().toSpace(
                sp.getSubspaceRegion().getBoundaryPaths().get(0).getVertices());

        Assert.assertEquals(pts.length, actual.size());

        for (int i = 0; i < pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], actual.get(i), TEST_EPS);
        }
    }
}
